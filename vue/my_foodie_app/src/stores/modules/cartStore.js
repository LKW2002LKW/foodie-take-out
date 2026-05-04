import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { addCart, cleanCart, getCartList, subCart } from '@/api/modules/cart'

export const CART_ACTION_CODE = {
  ADD_FAILED: 'ADD_FAILED',
  BATCH_REMOVE_FAILED: 'BATCH_REMOVE_FAILED',
  CLEAR_ALL_FAILED: 'CLEAR_ALL_FAILED',
  CLEAR_FAILED: 'CLEAR_FAILED',
  CROSS_MERCHANT_CONFLICT: 'CROSS_MERCHANT_CONFLICT',
  FETCH_FAILED: 'FETCH_FAILED',
  SUB_FAILED: 'SUB_FAILED',
}

const createSuccessResult = (payload = {}) => ({
  ok: true,
  ...payload,
})

const createFailureResult = (code, message, payload = {}) => ({
  ok: false,
  code,
  message,
  ...payload,
})

export const useCartStore = defineStore('cart', () => {
  const list = ref([])

  const totalNum = computed(() => list.value.reduce((total, i) => total + Number(i.number), 0))
  const totalPrice = computed(() => list.value.reduce((total, i) => total + Number(i.number) * Number(i.amount), 0))

  /**
   * 获取购物车列表
   * @param {number|number[]|string} ids - 可选：商家ID。如果不传，则请求全量购物车。
   */
  const fetchCartList = async (ids) => {
    let merchantIds = ''
    if (ids) {
      merchantIds = Array.isArray(ids) ? ids.join(',') : String(ids)
    }
    // 如果 ids 为空（首页购物车管理情况），merchantIds 就是 ''，直接传给后端

    try {
      const res = await getCartList(merchantIds)
      if (res.code === 1) {
        list.value = res.data || []
        return createSuccessResult({ data: list.value })
      }

      return createFailureResult(CART_ACTION_CODE.FETCH_FAILED, res.msg || '购物车加载失败')
    } catch (e) {
      console.error('Fetch cart failed', e)
      return createFailureResult(CART_ACTION_CODE.FETCH_FAILED, '购物车加载失败')
    }
  }

  const addToCart = async (item, flavorSelect = null, merchantId) => {
    const mId = merchantId || item.merchantId
    const dishId = item.dishId || (item.setmealId ? null : item.id)
    const setmealId = item.setmealId || (item.dishId ? null : (item.setmealId ? item.id : null))
    // 确保口味统一为字符串或 null
    const dishFlavor = flavorSelect ? JSON.stringify(flavorSelect) : (item.dishFlavor || null)

    const dto = {
      merchantId: mId,
      dishId: dishId,
      setmealId: setmealId,
      dishFlavor: dishFlavor,
      amount: item.amount || item.price,
      name: item.name,
      image: item.image || item.pic,
      number: 1
    }

    try {
      const res = await addCart(dto)
      if (res.code === 1) {
        // 添加后刷新该商家购物车
        await fetchCartList(mId)
        return createSuccessResult()
      }

      if (res.msg && res.msg.includes('清空')) {
        return createFailureResult(
          CART_ACTION_CODE.CROSS_MERCHANT_CONFLICT,
          res.msg || '存在其他商家商品，是否清空？',
          { merchantId: mId },
        )
      }

      return createFailureResult(CART_ACTION_CODE.ADD_FAILED, res.msg || '操作失败')
    } catch (e) {
      return createFailureResult(CART_ACTION_CODE.ADD_FAILED, '操作失败')
    }
  }

  const subFromCart = async (item) => {
    const merchantId = item.merchantId
    const dishId = item.dishId || (item.setmealId ? null : item.id)
    const setmealId = item.setmealId || null
    let dishFlavor = item.dishFlavor || null

    // 菜单列表里的菜品对象只有 id，没有 dishId；如果是多规格菜品，这里优先从购物车里找一个实际条目来减一。
    if (dishId && !dishFlavor) {
      const matchedCartItem = list.value.find(cartItem =>
        Number(cartItem.merchantId) === Number(merchantId) &&
        Number(cartItem.dishId) === Number(dishId)
      )
      if (matchedCartItem) {
        dishFlavor = matchedCartItem.dishFlavor || null
      }
    }

    const dto = {
      merchantId: merchantId,
      dishId: dishId,
      setmealId: setmealId,
      dishFlavor: dishFlavor
    }
    try {
      const res = await subCart(dto)
      if (res.code === 1) {
        await fetchCartList(merchantId)
        return createSuccessResult()
      }

      return createFailureResult(CART_ACTION_CODE.SUB_FAILED, res.msg || '操作失败')
    } catch (e) {
      return createFailureResult(CART_ACTION_CODE.SUB_FAILED, '操作失败')
    }
  }

  const clearCartAction = async (merchantId) => {
    try {
      const res = await cleanCart(merchantId)
      if (res.code === 1) {
        const targetMerchantId = String(merchantId)
        list.value = list.value.filter(i => String(i.merchantId) !== targetMerchantId)
        return createSuccessResult()
      }

      return createFailureResult(CART_ACTION_CODE.CLEAR_FAILED, res.msg || '清空失败')
    } catch (e) {
      return createFailureResult(CART_ACTION_CODE.CLEAR_FAILED, '清空失败')
    }
  }

  const clearAllCartAction = async () => {
    try {
      const res = await cleanCart()
      if (res.code === 1) {
        list.value = []
        return createSuccessResult()
      }

      return createFailureResult(CART_ACTION_CODE.CLEAR_ALL_FAILED, res.msg || '清空失败')
    } catch (e) {
      return createFailureResult(CART_ACTION_CODE.CLEAR_ALL_FAILED, '清空失败')
    }
  }

  const batchRemoveItems = async (items) => {
    if (items.length === 0) {
      return createSuccessResult()
    }

    const merchantId = items[0].merchantId

    for (const item of items) {
      const res = await subCart({
        merchantId: item.merchantId,
        dishId: item.dishId,
        setmealId: item.setmealId,
        dishFlavor: item.dishFlavor,
      })

      if (res.code !== 1) {
        return createFailureResult(CART_ACTION_CODE.BATCH_REMOVE_FAILED, res.msg || '删除失败')
      }
    }

    await fetchCartList(merchantId)
    return createSuccessResult()
  }

  const getCartItemCount = (dishId, setmealId, flavorStr = null) => {
    const cartList = list.value || []
    if (setmealId) {
      const target = cartList.find(i => i.setmealId === setmealId)
      return target ? Number(target.number || 0) : 0
    }
    // 过滤出该菜品的所有记录
    const filtered = cartList.filter(i => String(i.dishId) === String(dishId))
    
    // 如果没有传特定口味，返回该菜品在购物车里的总数（所有口味之和）
    if (flavorStr === null || flavorStr === undefined) {
      return filtered.reduce((s, i) => s + Number(i.number || 0), 0)
    }
    
    // 如果传了特定口味，返回该特定口味的数量
    const target = filtered.find(i => i.dishFlavor === flavorStr)
    return target ? Number(target.number || 0) : 0
  }

  return {
    list, totalNum, totalPrice,
    fetchCartList, addToCart, subFromCart, clearCartAction, clearAllCartAction,
    getCartItemCount, batchRemoveItems
  }
})
