import axios from 'axios'

const site = axios.create({ baseURL: '/api' })

// 附带站点登录用户的角色与ID，供买家接口使用
site.interceptors.request.use((config)=>{
  try{
    const su = localStorage.getItem('siteUser')
    if(su){
      const u = JSON.parse(su)
      if(u && u.id){ config.headers['X-User-Id'] = String(u.id) }
      if(u && u.role){ config.headers['X-Role'] = u.role }
    }
  }catch(e){}
  return config
})

export const siteLogin = (payload) => site.post('/accounts/login', payload)
export const siteRegisterBuyer = (payload) => site.post('/accounts/register', payload)
export const siteRegisterMerchant = (payload) => site.post('/accounts/register/merchant', payload)

// （已精简）买家中心、促销/优惠券功能已下线，相关 API 已移除

// ---- 商品 ----
export const searchProducts = (keyword='', page=0, size=10, category) => site.get('/products/search', { params:{ keyword, category, page, size } })
export const productDetail = (productId) => site.get(`/products/${productId}`)

// ---- 订单 ----
export const placeOrder = (items, note='', addressId=null, promoId=null) => site.post('/orders', { items, note, addressId, promoId })
export const paymentByOrder = (orderId) => site.get(`/payments/by-order/${orderId}`)
export const pay = (paymentId) => site.post(`/payments/${paymentId}/pay`)

// （已精简）评价与退款模块已下线，相关 API 已移除

// ---- 公告 ----
export const announcements = (page=0,size=10,keyword) => site.get('/announcements', { params:{ page, size, keyword } })
export const announcementDetail = (id) => site.get(`/announcements/${id}`)

// ---- 分类 ----
export const categories = (page=0,size=100,enabled=true) => site.get('/categories', { params:{ page, size, enabled } })

export default site
