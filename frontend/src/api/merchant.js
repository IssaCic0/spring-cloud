import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config)=>{
  const userStr = localStorage.getItem('siteUser')
  let uid = '2'
  try { if(userStr){ uid = String(JSON.parse(userStr).id || uid) } } catch(e) {}
  config.headers['X-Role'] = 'MERCHANT'
  config.headers['X-User-Id'] = uid
  config.headers['Content-Type'] = 'application/json'
  return config
})

// ---- Shop ----
export const meShop = () => api.get('/shops/me')
export const openShop = (payload) => api.post('/shops/me', payload)
export const updateShop = (payload) => api.put('/shops/me', payload)
export const closeShop = () => api.delete('/shops/me')
export const deleteMyShopHard = () => api.delete('/shops/me/hard')

// Multi-shop
export const myShops = (page=0,size=10) => api.get('/shops/my', { params:{ page, size } })
export const createMyShop = (payload) => api.post('/shops', payload)
export const getMyShopById = (shopId) => api.get(`/shops/my/${shopId}`)
export const updateMyShop = (shopId, payload) => api.put(`/shops/${shopId}`, payload)
export const closeMyShopById = (shopId) => api.delete(`/shops/${shopId}`)
export const deleteMyShopHardById = (shopId) => api.delete(`/shops/${shopId}/hard`)

// ---- Products ----
export const myProducts = (page=0,size=10) => api.get('/products/me', { params:{ page, size } })
export const createProduct = (payload) => api.post('/products', payload)
export const updateProduct = (id, payload) => api.put(`/products/${id}`, payload)
export const deleteProduct = (id) => api.delete(`/products/${id}`)
export const productSales = (ids) => api.get('/products/merchant/sales', { params:{ ids } })

// ---- Orders ----
export const merchantOrders = (page=0,size=10,shopId) => {
  const params = { page, size }
  if (typeof shopId === 'number' && !isNaN(shopId)) params.shopId = shopId
  return api.get('/orders/merchant', { params })
}
export const updateShipStatus = (orderId, status) => api.patch(`/orders/${orderId}/ship-status`, { status })
export const orderItems = (orderId) => api.get(`/orders/merchant/${orderId}/items`)

// 以下模块（库存/促销/评价/退款）已下线，对应 API 已移除

export default api
