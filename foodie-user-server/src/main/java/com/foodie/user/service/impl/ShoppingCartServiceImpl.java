package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.RedisKeyConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.common.json.JsonUtil;
import com.foodie.dto.user.ShoppingCartDTO;
import com.foodie.entity.Dish;
import com.foodie.entity.Merchant;
import com.foodie.entity.Setmeal;
import com.foodie.entity.ShoppingCart;
import com.foodie.user.mapper.DishMapper;
import com.foodie.user.mapper.MerchantMapper;
import com.foodie.user.mapper.SetmealMapper;
import com.foodie.user.mapper.ShoppingCartMapper;
import com.foodie.user.service.ShoppingCartService;
import com.foodie.vo.user.ShoppingCartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    private static final String META_SUFFIX = ":meta";

    private static final String NUM_SUFFIX = ":num";

    private static final DefaultRedisScript<Long> DECREASE_CART_ITEM_SCRIPT = new DefaultRedisScript<>();

    static {
        DECREASE_CART_ITEM_SCRIPT.setResultType(Long.class);
        DECREASE_CART_ITEM_SCRIPT.setScriptText(
                "local current = redis.call('HGET', KEYS[1], ARGV[1]) "
                        + "if not current then return -1 end "
                        + "current = tonumber(current) "
                        + "if current <= 1 then "
                        + "  redis.call('HDEL', KEYS[1], ARGV[1], ARGV[2]) "
                        + "  if redis.call('HLEN', KEYS[1]) == 0 then redis.call('DEL', KEYS[1]) end "
                        + "  return 0 "
                        + "end "
                        + "return redis.call('HINCRBY', KEYS[1], ARGV[1], -1)"
        );
    }

    private final DishMapper dishMapper;

    private final SetmealMapper setmealMapper;

    private final MerchantMapper merchantMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 添加购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long userId, ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：userId={}, dto={}", userId, shoppingCartDTO);

        ShoppingCart cart = buildCartMeta(userId, shoppingCartDTO);
        Long latestNum;
        try {
            latestNum = increaseCartItemInRedis(userId, cart, 1);
        } catch (Exception e) {
            log.error("Redis添加购物车失败：userId={}, dto={}", userId, shoppingCartDTO, e);
            throw new BaseException("购物车操作失败");
        }

        cart.setNumber(latestNum.intValue());
        try {
            syncCartItemToDb(cart, cart.getNumber());
        } catch (Exception e) {
            restoreCartItemInRedis(userId, cart, latestNum.intValue() - 1);
            throw e;
        }

        log.info("购物车操作成功");
    }

    /**
     * 查看购物车
     */
    @Override
    public List<ShoppingCartVO> listCart(String merchantIds, Long userId) {
        log.info("查看购物车：userId={}, merchantIds={}", userId, merchantIds);

        List<Long> merchantIdList = parseMerchantIds(merchantIds);
        List<ShoppingCart> cartList = null;
        try {
            cartList = listCartFromRedis(userId, merchantIdList);
        } catch (Exception e) {
            log.warn("从Redis读取购物车失败：userId={}, merchantIds={}", userId, merchantIds, e);
        }

        if (cartList == null) {
            cartList = loadCartFromDbAndWarmRedis(userId, merchantIdList);
        }

        return toCartVOList(cartList);
    }

    /**
     * 减少购物车商品数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subCart(Long userId, ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车商品：userId={}, dto={}", userId, shoppingCartDTO);

        Long merchantId = resolveMerchantId(shoppingCartDTO);
        ShoppingCart currentCart = getCartItemFromRedis(userId, merchantId, shoppingCartDTO);
        if (currentCart == null) {
            loadCartFromDbAndWarmRedis(userId, List.of(merchantId));
            currentCart = getCartItemFromRedis(userId, merchantId, shoppingCartDTO);
        }

        if (currentCart == null) {
            throw new BaseException(MessageConstant.CART_ITEM_NOT_FOUND);
        }

        Long latestNum;
        try {
            latestNum = decreaseCartItemInRedis(userId, currentCart);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis减少购物车失败：userId={}, dto={}", userId, shoppingCartDTO, e);
            throw new BaseException("购物车操作失败");
        }

        try {
            if (latestNum == 0L) {
                removeCartItemFromDb(userId, shoppingCartDTO, merchantId);
            } else {
                syncCartItemToDb(currentCart, latestNum.intValue());
            }
        } catch (Exception e) {
            restoreCartItemInRedis(userId, currentCart, currentCart.getNumber());
            throw e;
        }

        log.info("购物车商品减少成功");
    }

    /**
     * 清空购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanCart(Long userId) {
        log.info("清空购物车：userId={}", userId);

        removeAllCartFromDb(userId);
        try {
            deleteAllCartInRedis(userId);
        } catch (Exception e) {
            log.error("清空Redis购物车失败：userId={}", userId, e);
            throw new BaseException("清空购物车失败");
        }

        log.info("购物车已清空");
    }

    /**
     * 清空指定商户的购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanCartByMerchant(Long userId, Long merchantId) {
        log.info("清空指定商户购物车：userId={}, merchantId={}", userId, merchantId);

        removeMerchantCartFromDb(userId, merchantId);
        try {
            deleteMerchantCartInRedis(userId, merchantId);
        } catch (Exception e) {
            log.error("清空指定商户Redis购物车失败：userId={}, merchantId={}", userId, merchantId, e);
            throw new BaseException("清空购物车失败");
        }

        log.info("指定商户购物车已清空");
    }

    private List<Long> parseMerchantIds(String merchantIds) {
        List<Long> merchantIdList = new ArrayList<>();
        if (merchantIds == null || merchantIds.trim().isEmpty()) {
            return merchantIdList;
        }

        String[] ids = merchantIds.split(",");
        for (String id : ids) {
            try {
                merchantIdList.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                log.warn("无效的商户ID: {}", id);
            }
        }
        return merchantIdList;
    }

    private ShoppingCart buildCartMeta(Long userId, ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setDishId(shoppingCartDTO.getDishId());
        cart.setSetmealId(shoppingCartDTO.getSetmealId());
        cart.setDishFlavor(normalizeFlavor(shoppingCartDTO.getDishFlavor()));
        cart.setCreateTime(LocalDateTime.now());

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new BaseException("菜品不存在");
            }
            if (!StatusConstant.DISH_ON_SALE.equals(dish.getStatus())) {
                throw new BaseException(MessageConstant.DISH_NOT_ON_SALE);
            }

            cart.setMerchantId(dish.getMerchantId());
            cart.setName(dish.getName());
            cart.setImage(dish.getImage());
            cart.setAmount(dish.getPrice());
            return cart;
        }

        if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new BaseException("套餐不存在");
            }
            if (!StatusConstant.SETMEAL_ON_SALE.equals(setmeal.getStatus())) {
                throw new BaseException(MessageConstant.SETMEAL_NOT_ON_SALE);
            }

            cart.setMerchantId(setmeal.getMerchantId());
            cart.setName(setmeal.getName());
            cart.setImage(setmeal.getImage());
            cart.setAmount(setmeal.getPrice());
            return cart;
        }

        throw new BaseException("请选择菜品或套餐");
    }

    private Long resolveMerchantId(ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO.getMerchantId() != null) {
            return shoppingCartDTO.getMerchantId();
        }

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new BaseException(MessageConstant.CART_ITEM_NOT_FOUND);
            }
            return dish.getMerchantId();
        }

        if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new BaseException(MessageConstant.CART_ITEM_NOT_FOUND);
            }
            return setmeal.getMerchantId();
        }

        throw new BaseException("请指定要减少的商品");
    }

    private String normalizeFlavor(String dishFlavor) {
        if (dishFlavor == null || dishFlavor.trim().isEmpty()) {
            return null;
        }
        return dishFlavor;
    }

    private String buildCartKey(Long userId, Long merchantId) {
        return String.format(RedisKeyConstant.SHOPPING_CART, userId, merchantId);
    }

    private String buildUserCartPattern(Long userId) {
        return String.format(RedisKeyConstant.SHOPPING_CART_PATTERN, userId);
    }

    private String buildItemKey(ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO.getDishId() != null) {
            StringBuilder builder = new StringBuilder("dish:").append(shoppingCartDTO.getDishId());
            String flavor = normalizeFlavor(shoppingCartDTO.getDishFlavor());
            if (flavor != null) {
                builder.append(":flavor:").append(flavor);
            }
            return builder.toString();
        }

        if (shoppingCartDTO.getSetmealId() != null) {
            return "setmeal:" + shoppingCartDTO.getSetmealId();
        }

        throw new BaseException("请选择菜品或套餐");
    }

    private String buildItemKey(ShoppingCart cart) {
        if (cart.getDishId() != null) {
            StringBuilder builder = new StringBuilder("dish:").append(cart.getDishId());
            if (cart.getDishFlavor() != null) {
                builder.append(":flavor:").append(cart.getDishFlavor());
            }
            return builder.toString();
        }
        return "setmeal:" + cart.getSetmealId();
    }

    private String buildMetaField(String itemKey) {
        return itemKey + META_SUFFIX;
    }

    private String buildNumField(String itemKey) {
        return itemKey + NUM_SUFFIX;
    }

    private Long increaseCartItemInRedis(Long userId, ShoppingCart cart, int delta) {
        String cartKey = buildCartKey(userId, cart.getMerchantId());
        String itemKey = buildItemKey(cart);
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.putIfAbsent(cartKey, buildMetaField(itemKey), JsonUtil.toJson(cart));
        Long latestNum = hashOperations.increment(cartKey, buildNumField(itemKey), delta);
        if (latestNum == null) {
            throw new BaseException("购物车操作失败");
        }
        return latestNum;
    }

    private Long decreaseCartItemInRedis(Long userId, ShoppingCart cart) {
        String cartKey = buildCartKey(userId, cart.getMerchantId());
        String itemKey = buildItemKey(cart);
        Long latestNum = redisTemplate.execute(
                DECREASE_CART_ITEM_SCRIPT,
                List.of(cartKey),
                buildNumField(itemKey),
                buildMetaField(itemKey)
        );
        if (latestNum == null) {
            throw new BaseException("购物车操作失败");
        }
        if (latestNum < 0) {
            throw new BaseException(MessageConstant.CART_ITEM_NOT_FOUND);
        }
        return latestNum;
    }

    private ShoppingCart getCartItemFromRedis(Long userId, Long merchantId, ShoppingCartDTO shoppingCartDTO) {
        String cartKey = buildCartKey(userId, merchantId);
        String itemKey = buildItemKey(shoppingCartDTO);
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Object metaJson = hashOperations.get(cartKey, buildMetaField(itemKey));
        Object numValue = hashOperations.get(cartKey, buildNumField(itemKey));
        if (metaJson == null || numValue == null) {
            return null;
        }

        ShoppingCart cart = JsonUtil.fromJson(metaJson.toString(), ShoppingCart.class);
        cart.setNumber(Integer.parseInt(numValue.toString()));
        return cart;
    }

    private void restoreCartItemInRedis(Long userId, ShoppingCart cart, Integer targetNum) {
        String cartKey = buildCartKey(userId, cart.getMerchantId());
        String itemKey = buildItemKey(cart);
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();

        if (targetNum == null || targetNum <= 0) {
            hashOperations.delete(cartKey, buildMetaField(itemKey), buildNumField(itemKey));
            if (Long.valueOf(0L).equals(hashOperations.size(cartKey))) {
                redisTemplate.delete(cartKey);
            }
            return;
        }

        hashOperations.put(cartKey, buildMetaField(itemKey), JsonUtil.toJson(cart));
        hashOperations.put(cartKey, buildNumField(itemKey), String.valueOf(targetNum));
    }

    private List<ShoppingCart> listCartFromRedis(Long userId, List<Long> merchantIdList) {
        List<ShoppingCart> cartList = new ArrayList<>();
        if (merchantIdList == null || merchantIdList.isEmpty()) {
            Set<String> cartKeys = redisTemplate.keys(buildUserCartPattern(userId));
            if (CollectionUtils.isEmpty(cartKeys)) {
                return null;
            }
            for (String cartKey : cartKeys) {
                cartList.addAll(readCartListByKey(cartKey));
            }
        } else {
            for (Long merchantId : merchantIdList) {
                String cartKey = buildCartKey(userId, merchantId);
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(cartKey))) {
                    return null;
                }
                cartList.addAll(readCartListByKey(cartKey));
            }
        }

        cartList.sort(Comparator.comparing(ShoppingCart::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return cartList;
    }

    private List<ShoppingCart> readCartListByKey(String cartKey) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, ShoppingCart> metaMap = new java.util.HashMap<>();
        Map<String, Integer> numMap = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey().toString();
            Object value = entry.getValue();
            if (field.endsWith(META_SUFFIX)) {
                String itemKey = field.substring(0, field.length() - META_SUFFIX.length());
                metaMap.put(itemKey, JsonUtil.fromJson(value.toString(), ShoppingCart.class));
            } else if (field.endsWith(NUM_SUFFIX)) {
                String itemKey = field.substring(0, field.length() - NUM_SUFFIX.length());
                numMap.put(itemKey, Integer.parseInt(value.toString()));
            }
        }

        List<ShoppingCart> cartList = new ArrayList<>();
        for (Map.Entry<String, ShoppingCart> entry : metaMap.entrySet()) {
            Integer number = numMap.get(entry.getKey());
            if (number == null || number <= 0) {
                continue;
            }
            ShoppingCart cart = entry.getValue();
            cart.setNumber(number);
            cartList.add(cart);
        }
        return cartList;
    }

    private List<ShoppingCart> loadCartFromDbAndWarmRedis(Long userId, List<Long> merchantIdList) {
        List<ShoppingCart> cartList;
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime);
        if (merchantIdList != null && !merchantIdList.isEmpty()) {
            wrapper.in(ShoppingCart::getMerchantId, merchantIdList);
        }
        cartList = this.list(wrapper);

        if (!CollectionUtils.isEmpty(cartList)) {
            try {
                warmCartCache(userId, cartList);
            } catch (Exception e) {
                log.warn("回填购物车缓存失败：userId={}", userId, e);
            }
        }
        return cartList;
    }

    private void warmCartCache(Long userId, List<ShoppingCart> cartList) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        for (ShoppingCart cart : cartList) {
            String cartKey = buildCartKey(userId, cart.getMerchantId());
            String itemKey = buildItemKey(cart);
            hashOperations.put(cartKey, buildMetaField(itemKey), JsonUtil.toJson(cart));
            hashOperations.put(cartKey, buildNumField(itemKey), String.valueOf(cart.getNumber()));
        }
    }

    private void syncCartItemToDb(ShoppingCart cart, Integer latestNum) {
        LambdaQueryWrapper<ShoppingCart> wrapper = buildCartItemWrapper(
                cart.getUserId(),
                cart.getMerchantId(),
                cart.getDishId(),
                cart.getSetmealId(),
                cart.getDishFlavor()
        );

        ShoppingCart existCart = this.getOne(wrapper);
        if (existCart != null) {
            existCart.setNumber(latestNum);
            existCart.setName(cart.getName());
            existCart.setImage(cart.getImage());
            existCart.setAmount(cart.getAmount());
            this.updateById(existCart);
            return;
        }

        cart.setNumber(latestNum);
        this.save(cart);
    }

    private void removeCartItemFromDb(Long userId, ShoppingCartDTO shoppingCartDTO, Long merchantId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = buildCartItemWrapper(
                userId,
                merchantId,
                shoppingCartDTO.getDishId(),
                shoppingCartDTO.getSetmealId(),
                normalizeFlavor(shoppingCartDTO.getDishFlavor())
        );
        this.remove(wrapper);
    }

    private void removeMerchantCartFromDb(Long userId, Long merchantId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getMerchantId, merchantId);
        this.remove(wrapper);
    }

    private void removeAllCartFromDb(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        this.remove(wrapper);
    }

    private LambdaQueryWrapper<ShoppingCart> buildCartItemWrapper(Long userId,
                                                                  Long merchantId,
                                                                  Long dishId,
                                                                  Long setmealId,
                                                                  String dishFlavor) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getMerchantId, merchantId);

        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
            if (dishFlavor != null) {
                wrapper.eq(ShoppingCart::getDishFlavor, dishFlavor);
            } else {
                wrapper.isNull(ShoppingCart::getDishFlavor);
            }
            return wrapper;
        }

        if (setmealId != null) {
            wrapper.eq(ShoppingCart::getSetmealId, setmealId);
            return wrapper;
        }

        throw new BaseException("请选择菜品或套餐");
    }

    private void deleteMerchantCartInRedis(Long userId, Long merchantId) {
        redisTemplate.delete(buildCartKey(userId, merchantId));
    }

    private void deleteAllCartInRedis(Long userId) {
        Set<String> cartKeys = redisTemplate.keys(buildUserCartPattern(userId));
        if (!CollectionUtils.isEmpty(cartKeys)) {
            redisTemplate.delete(cartKeys);
        }
    }

    private List<ShoppingCartVO> toCartVOList(List<ShoppingCart> cartList) {
        if (cartList == null || cartList.isEmpty()) {
            return new ArrayList<>();
        }
        return cartList.stream().map(cart -> {
            ShoppingCartVO vo = new ShoppingCartVO();
            BeanUtils.copyProperties(cart, vo);

            Merchant merchant = merchantMapper.selectById(cart.getMerchantId());
            if (merchant != null) {
                vo.setMerchantName(merchant.getMerchantName());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
