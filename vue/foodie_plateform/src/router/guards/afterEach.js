// 后置守卫统一维护页面标题，避免每个页面重复设置。
export const createAfterEachGuard = () => (to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 吃货联盟管理平台`
  } else {
    document.title = '吃货联盟管理平台'
  }
}
