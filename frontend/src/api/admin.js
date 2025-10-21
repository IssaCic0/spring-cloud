import axios from 'axios';

// Base axios for admin, point to backend and attach ADMIN headers
const api = axios.create({
  baseURL: '/api'
});

// Default admin headers; you can set window.ADMIN_USER_ID to override
api.interceptors.request.use((config) => {
  const uid = (window.ADMIN_USER_ID || localStorage.getItem('adminUserId') || '1');
  config.headers['X-Role'] = 'ADMIN';
  config.headers['X-User-Id'] = uid;
  config.headers['Content-Type'] = 'application/json';
  return config;
});

// ---- Accounts ----
export const adminUsers = (page = 0, size = 10) => api.get(`/accounts/admin/users`, { params: { page, size } });
export const adminBanUser = (id, reason = '') => api.post(`/accounts/admin/users/${id}/ban`, reason ? { reason } : {});
export const adminChangeUserRole = (id, role) => api.patch(`/accounts/admin/users/${id}/role`, { role });
export const adminCreateUser = (payload) => api.post(`/accounts/admin/users`, payload);
export const adminUpdateUser = (id, payload) => api.put(`/accounts/admin/users/${id}`, payload);
export const adminDeleteUser = (id) => api.delete(`/accounts/admin/users/${id}`);

// ---- Shops ----
export const adminShops = (page = 0, size = 10, ownerId, name) => api.get(`/shops/admin`, { params: { page, size, ownerId, name } });
export const adminApproveShop = (shopId) => api.post(`/shops/admin/${shopId}/approve`);
export const adminBanShop = (shopId) => api.post(`/shops/admin/${shopId}/ban`);
export const adminCreateShop = (payload) => api.post(`/shops/admin`, payload);
export const adminGetShop = (shopId) => api.get(`/shops/admin/${shopId}`);
export const adminUpdateShop = (shopId, payload) => api.put(`/shops/admin/${shopId}`, payload);
export const adminDeleteShop = (shopId) => api.delete(`/shops/admin/${shopId}`);

// ---- Products ----
export const adminProducts = (page = 0, size = 10, keyword, shopId) => api.get(`/products/admin`, { params: { page, size, keyword, shopId } });
export const adminForceOfflineProduct = (productId) => api.post(`/products/admin/${productId}/force-offline`);
export const adminGetProduct = (productId) => api.get(`/products/admin/${productId}`);
export const adminUpdateProduct = (productId, payload) => api.put(`/products/admin/${productId}`, payload);
export const adminDeleteProduct = (productId) => api.delete(`/products/admin/${productId}`);
export const adminCreateProduct = (payload) => api.post(`/products/admin`, payload);

// ---- Announcements ----
export const adminAnnouncements = (page = 0, size = 10, keyword) => api.get(`/announcements/admin`, { params: { page, size, keyword } });
export const adminGetAnnouncement = (id) => api.get(`/announcements/admin/${id}`);
export const adminCreateAnnouncement = (payload) => api.post(`/announcements/admin`, payload); // { title, content }
export const adminUpdateAnnouncement = (id, payload) => api.put(`/announcements/admin/${id}`, payload);
export const adminDeleteAnnouncement = (id) => api.delete(`/announcements/admin/${id}`);

// ---- Categories ----
export const adminCategories = (page=0,size=10,keyword,enabled) => api.get(`/categories/admin`, { params: { page, size, keyword, enabled } });
export const adminGetCategory = (id) => api.get(`/categories/admin/${id}`);
export const adminCreateCategory = (payload) => api.post(`/categories/admin`, payload);
export const adminUpdateCategory = (id, payload) => api.put(`/categories/admin/${id}`, payload);
export const adminDeleteCategory = (id) => api.delete(`/categories/admin/${id}`);

// ---- Payments ----
// 管理员“支付记录”模块已下线，不再导出相关接口

// ---- Refunds ----
// 已下线：退款管理

// ---- Inventory ----
// 管理员不再直接调整商家商品库存，相关接口已移除

// ---- Reviews ----
// 管理员“评价互动”模块已下线，不再导出相关接口

// ---- Promotions ----
// 管理员“优惠券&促销”模块已下线，不再导出相关接口

export default api;
