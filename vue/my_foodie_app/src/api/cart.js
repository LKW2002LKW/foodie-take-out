import request from '../utils/request';

// 添加购物车
export const addCart = (params) => {
    return request({
        url: '/user/shoppingCart/add',
        method: 'post',
        data: params
    });
};

// 减少/移除 购物车
export const subCart = (params) => {
    return request({
        url: '/user/shoppingCart/sub',
        method: 'post',
        data: params
    });
};

// 获取购物车列表
export const getCartList = (params) => {
    return request({
        url: '/user/shoppingCart/list',
        method: 'get',
        params
    });
};

// 清空购物车
export const cleanCart = (params) => {
    return request({
        url: '/user/shoppingCart/clean',
        method: 'delete',
        params // typically empty or with merchantId if needed
    });
};
