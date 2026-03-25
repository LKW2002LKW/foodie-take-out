import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { addCart, subCart, getCartList, cleanCart } from '../../api/cart';
import { showConfirmDialog, showToast, showFailToast } from 'vant';

export const useCartStore = defineStore('cart', () => {
    const list = ref([]);
    
    // 计算属性：购物车总件数
    const totalNum = computed(() => {
        return list.value.reduce((total, item) => total + item.number, 0);
    });

    // 计算属性：购物车总金额 (前端计算)
    const totalPrice = computed(() => {
        return list.value.reduce((total, item) => {
            return total + (item.number * item.amount);
        }, 0);
    });

    // 获取当前购物车状态
    const fetchCartList = async () => {
        try {
            const res = await getCartList();
            if (res.code === 1) {
                list.value = res.data || [];
            }
        } catch (e) {
            console.error('Fetch cart failed', e);
        }
    };

    // 添加商品 (核心逻辑)
    const addToCart = async (item, flavorSelect = null) => {
        // 构建请求参数
        const params = {
            amount: item.price, // 使用当前单价
            dishFlavor: flavorSelect ? JSON.stringify(flavorSelect) : undefined,
            merchantId: item.merchantId, // 必传，商家ID
        };

        // 区分套餐还是菜品
        if (item.setmealId) {
            params.setmealId = item.setmealId;
            params.name = item.name;     // 冗余字段方便后端
            params.image = item.image;
        } else {
            params.dishId = item.id;
            params.name = item.name;
            params.image = item.image;
        }

        try {
            const res = await addCart(params);
            if (res.code === 1) {
                // 成功，刷新列表
                await fetchCartList();
            } else {
                // 业务异常处理，例如跨商户
                // 假设后端返回特定msg提示跨商户 (这里模拟常见的业务判断)
                if (res.msg && res.msg.includes('清空')) {
                    showConfirmDialog({
                        title: '提示',
                        message: '购物车中包含其他商家的商品，是否清空购物车并添加当前商品？',
                    })
                    .then(async () => {
                        // 用户确认清空
                        await cleanCart();
                        // 再次添加
                        await addToCart(item, flavorSelect);
                    })
                    .catch(() => {
                        // 取消
                    });
                } else {
                    showFailToast(res.msg || '添加失败');
                }
            }
        } catch (e) {
            showFailToast('请求失败');
        }
    };

    // 减少商品
    const subFromCart = async (item) => {
        // 构造减少参数，注意这里通常传递 id 或者是 (dishId + flavor)
        // 按照接口通常设计，sub 也是传同样的商品标识
        const params = {
             merchantId: item.merchantId
        };
        
        if (item.setmealId) {
            params.setmealId = item.setmealId;
        } else {
            params.dishId = item.dishId; // 注意：购物车里的item字段通常是 dishId, 而不是 id
            params.dishFlavor = item.dishFlavor;
        }

        try {
            const res = await subCart(params);
            if (res.code === 1) {
                await fetchCartList();
            } else {
                showFailToast(res.msg);
            }
        } catch (e) {
            showFailToast('操作失败');
        }
    };

    // 清空购物车
    const clearCartAction = async (merchantId) => {
        try {
            const res = await cleanCart({ merchantId }); //有些接口可能不需要参数
            if (res.code === 1) {
                list.value = [];
            }
        } catch (e) {
            showFailToast('清空失败');
        }
    };

    // 辅助函数：根据商品ID查找购物车中的数量 (用于 UI 显示加减号)
    // 注意：如果是多规格菜品，外部需要再根据 flavor 过滤，或者 UI 层面只显示总数
    // 这里提供一个精准查找
    const getCartItemCount = (dishId, setmealId, flavorStr = null) => {
        let count = 0;
        if (setmealId) {
            const target = list.value.find(i => i.setmealId === setmealId);
            return target ? target.number : 0;
        } else {
            // 如果是菜品，且没有指定flavor (列表页显示总数)，则累加所有该ID的记录
            if (flavorStr === null) {
                return list.value
                    .filter(i => i.dishId === dishId)
                    .reduce((sum, i) => sum + i.number, 0);
            } else {
                // 有规格，精准匹配
                const target = list.value.find(i => i.dishId === dishId && i.dishFlavor === flavorStr);
                return target ? target.number : 0;
            }
        }
    };

    return {
        list,
        totalNum,
        totalPrice,
        fetchCartList,
        addToCart,
        subFromCart,
        clearCartAction,
        getCartItemCount
    };
});
