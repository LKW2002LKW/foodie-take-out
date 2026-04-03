import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { addCart, subCart, getCartList, cleanCart } from '../../services/cart'
import { showConfirmDialog, showToast, showFailToast } from 'vant'

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
      }
    } catch (e) {
      console.error('Fetch cart failed', e)
    }
  }

  const addToCart = async (item, flavorSelect = null, merchantId) => {
    const mId = merchantId || item.merchantId
    const dto = {
      merchantId: mId,
      dishId: item.dishId || (item.setmealId ? null : item.id),
      setmealId: item.setmealId || (item.dishId ? null : (item.setmealId ? item.id : null)),
      dishFlavor: flavorSelect ? JSON.stringify(flavorSelect) : item.dishFlavor,
      amount: item.amount || item.price,
      name: item.name,
      image: item.image || item.pic
    }

    try {
      const res = await addCart(dto)
      if (res.code === 1) {
        // 添加后刷新该商家购物车
        await fetchCartList(mId)
      } else if (res.msg && res.msg.includes('清空')) {
        showConfirmDialog({ title: '提示', message: '存在其他商家商品，是否清空？' }).then(async () => {
          await cleanCart(mId)
          await addToCart(item, flavorSelect, merchantId)
        })
      }
    } catch (e) {
      showFailToast('操作失败')
    }
  }

  const subFromCart = async (item) => {
    const dto = {
      merchantId: item.merchantId,
      dishId: item.dishId,
      setmealId: item.setmealId,
      dishFlavor: item.dishFlavor
    }
    try {
      const res = await subCart(dto)
      if (res.code === 1) {
        await fetchCartList(item.merchantId)
      }
    } catch (e) {
      showFailToast('操作失败')
    }
  }

  const clearCartAction = async (merchantId) => {
    try {
      const res = await cleanCart(merchantId)
      if (res.code === 1) {
        list.value = list.value.filter(i => i.merchantId !== merchantId)
      }
    } catch (e) {
      showFailToast('清空失败')
    }
  }

  const batchRemoveItems = async (items) => {
    if (items.length === 0) return
    const merchantId = items[0].merchantId
    for (const item of items) {
      await subCart({
        merchantId: item.merchantId,
        dishId: item.dishId,
        setmealId: item.setmealId,
        dishFlavor: item.dishFlavor
      })
    }
    // 批量删除后，如果是详情页则刷新该商家，如果是首页全量则传空刷新全量
    await fetchCartList(items.length > 0 ? null : merchantId) 
  }

  const getCartItemCount = (dishId, setmealId, flavorStr = null) => {
    if (setmealId) {
      const target = list.value.find(i => i.setmealId === setmealId)
      return target ? target.number : 0
    }
    const filtered = list.value.filter(i => i.dishId === dishId)
    if (flavorStr === null) return filtered.reduce((s, i) => s + i.number, 0)
    const target = filtered.find(i => i.dishFlavor === flavorStr)
    return target ? target.number : 0
  }

  return {
    list, totalNum, totalPrice,
    fetchCartList, addToCart, subFromCart, clearCartAction,
    getCartItemCount, batchRemoveItems
  }
})
