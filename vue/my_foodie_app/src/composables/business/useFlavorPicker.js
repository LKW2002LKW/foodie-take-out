import { computed, ref, watch } from 'vue'

const parseFlavorValue = (val) => {
  if (Array.isArray(val)) return val
  try {
    return JSON.parse(val)
  } catch (error) {
    return []
  }
}

// 规格口味选择器逻辑，统一处理默认值解析、选中状态与确认输出。
export const useFlavorPicker = (props, emit) => {
  const selections = ref({})

  const selectedText = computed(() => (
    Object.values(selections.value).filter(Boolean).join('/')
  ))

  const isAllSelected = computed(() => {
    if (!props.food?.flavors) return true
    return props.food.flavors.every((item) => selections.value[item.name])
  })

  const onSelect = (name, val) => {
    selections.value[name] = val
  }

  const onConfirm = () => {
    const flavorRes = Object.entries(selections.value).map(([name, value]) => ({
      name,
      value,
    }))
    emit('confirm', flavorRes)
    emit('update:show', false)
  }

  const onClose = () => {
    selections.value = {}
  }

  watch(() => props.food, (newFood) => {
    selections.value = {}
    if (newFood?.flavors) {
      newFood.flavors.forEach((item) => {
        const options = parseFlavorValue(item.value)
        if (options.length > 0) {
          selections.value[item.name] = options[0]
        }
      })
    }
  }, { immediate: true })

  return {
    isAllSelected,
    onClose,
    onConfirm,
    onSelect,
    parseFlavorValue,
    selectedText,
    selections,
  }
}
