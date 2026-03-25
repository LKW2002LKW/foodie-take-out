import request from '@/utils/request'

// 辅助函数：获取请求头中的merchantId，实际项目中可能从UserStore或LocalStorage获取
const getHeaders = () => {
    // 假设merchantId存储在localStorage中，如果没有则默认使用文档示例中的1
    const merchantId = localStorage.getItem('merchantId') || 1;
    // 假设管理员ID也存储在类似位置，或者从用户信息中获取
    const merchantAdminId = localStorage.getItem('merchantAdminId') || 1;
    
    return {
        merchantId,
        merchantAdminId
    }
}

export default {
    /**
     * 分页查询员工列表
     * @param {Object} params { page, pageSize, keyword, role, status }
     */
    getEmployeePage(params) {
        return request({
            url: '/merchant/employee/page',
            method: 'get',
            params,
            headers: { merchantId: getHeaders().merchantId }
        })
    },

    /**
     * 查询员工详情
     * @param {Number} id 
     */
    getEmployeeDetail(id) {
        return request({
            url: `/merchant/employee/${id}`,
            method: 'get',
            headers: { merchantId: getHeaders().merchantId }
        })
    },

    /**
     * 新增员工
     * @param {Object} data 
     */
    addEmployee(data) {
        const { merchantId, merchantAdminId } = getHeaders();
        return request({
            url: '/merchant/employee/add',
            method: 'post',
            data,
            headers: { merchantId, merchantAdminId }
        })
    },

    /**
     * 更新员工信息
     * @param {Object} data 
     */
    updateEmployee(data) {
        const { merchantId, merchantAdminId } = getHeaders();
        return request({
            url: '/merchant/employee/update',
            method: 'put',
            data,
            headers: { merchantId, merchantAdminId }
        })
    },

    /**
     * 删除员工
     * @param {Number} id 
     */
    deleteEmployee(id) {
        return request({
            url: `/merchant/employee/${id}`,
            method: 'delete',
            headers: { merchantId: getHeaders().merchantId }
        })
    },

    /**
     * 重置员工密码
     * @param {Object} data { id, newPassword }
     */
    resetPassword(data) {
        return request({
            url: '/merchant/employee/reset-password',
            method: 'put',
            data,
            headers: { merchantId: getHeaders().merchantId }
        })
    },

    /**
     * 启用/禁用员工
     * @param {Number} id 
     * @param {Number} status 0-禁用 1-启用
     */
    updateStatus(id, status) {
        return request({
            url: `/merchant/employee/status/${id}`,
            method: 'put',
            params: { status },
            headers: { merchantId: getHeaders().merchantId }
        })
    }
}
