/**
 * why: 配置页里的原生下拉框很多，统一在 hover + 滚轮时切换选项，
 * 既减少重复模板代码，也保证 v-model 和 change 监听都沿用原生事件更新。
 */
export function updateSelectByWheel(event: WheelEvent) {
  const select = event.currentTarget
  if (!(select instanceof HTMLSelectElement) || select.disabled) {
    return
  }

  const direction = Math.sign(event.deltaY)
  if (direction === 0) {
    return
  }

  const enabledOptions = Array.from(select.options).filter((option) => !option.disabled)
  if (enabledOptions.length <= 1) {
    return
  }

  const currentIndex = enabledOptions.findIndex((option) => option.value === select.value)
  const fallbackIndex = currentIndex >= 0 ? currentIndex : 0
  const nextIndex = Math.min(
    enabledOptions.length - 1,
    Math.max(0, fallbackIndex + (direction > 0 ? 1 : -1)),
  )

  if (nextIndex === fallbackIndex) {
    return
  }

  select.value = enabledOptions[nextIndex].value
  select.dispatchEvent(new Event('change', { bubbles: true }))
}
