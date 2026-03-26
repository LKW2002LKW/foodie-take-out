import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { addCart, subCart, getCartList, cleanCart } from '../../api/cart';
import { showConfirmDialog, showToast, showFailToast } from 'vant';

export const useCartStore = defineStore('cart', () => {
    const list = ref([]);
    
    // 计算属性：购物车总件数
    const totalNum = computed(() => {
        return list.value.reduce((total, item) => {
            const count = item.number || item.count || item.quantity || 0;
            return total + Number(count);
        }, 0);
    });

    // 计算属性：购物车总金额 (前端计算)
    const totalPrice = computed(() => {
        return list.value.reduce((total, item) => {
             const count = item.number || item.count || item.quantity || 0;
             const price = item.amount || item.price || 0;
            return total + (Number(count) * Number(price));
        }, 0);
    });

    // 获取当前购物车状态
    const fetchCartList = async (data) => {
        try {
            const res = await getCartList(data);
             if (res.code === 1) {
                list.value = res.data || [];
                // Debug log to help diagnose data structure
                if (list.value.length > 0) {
                    console.log("First item structure:", JSON.stringify(list.value[0]));
                    if (list.value[0].number === undefined) {
                        console.warn("Field 'number' is missing in cart item!");
                    }
                }
            } else {
                console.warn("Fetch cart list returned code:", res.code, res.msg);
            }
        } catch (e) {
            console.error('Fetch cart failed', e);
        }
    };

    const addToCart = async (item, flavorSelect = null, explicitMerchantId = null) => {
        const mId = explicitMerchantId || item.merchantId;

        // --- 数据适配 ---
        // 兼容菜品对象(List Item) 和 购物车对象(Cart Item)
        // 1. 金额：Cart Item 使用 amount, List Item 使用 price
        const actualAmount = (item.amount !== undefined) ? item.amount : item.price;
        
        // 2. ID处理
        // List Item: id 通常是 dishId/setmealId
        // Cart Item: dishId/setmealId 是明确的, id 可能是购物车记录ID
        let actualDishId = item.dishId;
        let actualSetmealId = item.setmealId;

        // 如果既没有 dishId 也没有 setmealId，说明这是 Menu List Item，id 就是 dishId
        if (!actualDishId && !actualSetmealId) {
            actualDishId = item.id;
        }

        // 3. 口味处理
        // 如果重新选择了 specific flavor (flavorSelect), 使用它
        // 否则如果已经有 dishFlavor (Cart Item), 沿用它
        let finalFlavor = undefined;
        if (flavorSelect) {
            finalFlavor = JSON.stringify(flavorSelect);
        } else if (item.dishFlavor) {
            finalFlavor = item.dishFlavor;
        }

        // 构建请求参数
        const params = {
            merchantId: mId,
            dishFlavor: finalFlavor,
            amount: actualAmount,
            number: 1, 
        };

        if (actualSetmealId) {
            params.setmealId = actualSetmealId;
        } else {
            params.dishId = actualDishId;
        }
        
        params.name = item.name;
        params.image = item.image || item.pic; // 兼容图片字段

        try {
            const res = await addCart(params);
            if (res.code === 1) {
                // 成功，刷新列表
                await fetchCartList({ merchantId: mId });
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
                await fetchCartList({ merchantId: item.merchantId });
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
