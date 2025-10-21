// 导入组件
import Vue from 'vue';
import Router from 'vue-router';
// 登录
import login from '@/views/login';
// 首页
import index from '@/views/index';
// 站点主页面
import SiteHome from '@/views/site/Home';
import SiteProducts from '@/views/site/Products';
import SiteCart from '@/views/site/Cart';
// Admin 视图
import AdminUsers from '@/views/admin/Users';
import AdminShops from '@/views/admin/Shops';
import AdminProducts from '@/views/admin/Products';
import AdminCategories from '@/views/admin/Categories';
import AdminAnnouncements from '@/views/admin/Announcements';
// Merchant 视图
import MerchantShop from '@/views/merchant/Shop';
import MerchantProducts from '@/views/merchant/Products';
import MerchantOrders from '@/views/merchant/Orders';
import MerchantAnnouncements from '@/views/merchant/Announcements';
/**
 * 数据监控
 */
// 监控查询
import druidLogin from '@/views/druid/login';

// 图表界面
import statistics from '@/views/charts/statistics';
import MyChat from '@/views/ai/MyChat';

// 启用路由
Vue.use(Router);

// 导出路由 
export default new Router({
    routes: [{
        path: '/',
        name: '首页',
        component: SiteHome,
        hidden: false,
        meta: { requireAuth: false }
    }, {
        path: '/login',
        name: '登录',
        component: login,
        hidden: true,
        meta: {
            requireAuth: false
        }
    }, {
        path: '/home',
        name: '站点首页',
        component: SiteHome,
        hidden: true,
        meta: { requireAuth: false }
    }, {
        path: '/index',
        name: '后台框架',
        component: index,
        iconCls: 'el-icon-tickets',
        children: [{
            path: '/admin/users',
            name: '用户管理',
            component: AdminUsers,
            meta: { requireAuth: true }
        }, {
            path: '/admin/shops',
            name: '店铺管理',
            component: AdminShops,
            meta: { requireAuth: true }
        }, {
            path: '/admin/products',
            name: '商品管理',
            component: AdminProducts,
            meta: { requireAuth: true }
        }, {
            path: '/admin/categories',
            name: '分类管理',
            component: AdminCategories,
            meta: { requireAuth: true }
        }, {
            path: '/admin/announcements',
            name: '公告管理',
            component: AdminAnnouncements,
            meta: { requireAuth: true }
        }, {
            path: '/druid/login',
            name: '监控查询',
            component: druidLogin,
            meta: {
                requireAuth: true
            }
        }, {
            path: '/charts/statistics',
            name: '数据可视化',
            component: statistics,
            meta: {
                requireAuth: false
            }
        }, {
            path: '/ai/chat',
            name: 'AI助手',
            component: MyChat,
            meta: { requireAuth: true }
        }, {
            path: '/merchant/shop',
            name: '我的店铺',
            component: MerchantShop,
            meta: { requireAuth: true }
        }, {
            path: '/merchant/products',
            name: '我的商品',
            component: MerchantProducts,
            meta: { requireAuth: true }
        }, {
            path: '/merchant/orders',
            name: '店铺订单',
            component: MerchantOrders,
            meta: { requireAuth: true }
        }, {
            path: '/merchant/announcements',
            name: '公告(商家)',
            component: MerchantAnnouncements,
            meta: { requireAuth: true }
        }]
    }, {
        path: '/products',
        name: '商品',
        component: SiteProducts,
        meta: { requireAuth: false }
    }, {
        path: '/cart',
        name: '购物车',
        component: SiteCart,
        meta: { requireAuth: false }
    }, {
        path: '*',
        redirect: '/home'
    }]
})