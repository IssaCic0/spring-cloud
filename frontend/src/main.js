// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
// 引入element UI
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App';
// 引入路由
import router from './router';
// 引入状态管理
import store from './vuex/store';
// 引入icon
import './assets/icon/iconfont.css'
// 

// 引入echarts
import echarts from 'echarts'
Vue.prototype.$echarts = echarts

import axios from 'axios';
Vue.prototype.$axios = axios;

Vue.config.productionTip = false;

// 使用element UI
Vue.use(ElementUI);
// 过滤器
import * as custom from './utils/util'

Object.keys(custom).forEach(key => {
    Vue.filter(key, custom[key])
})

// 路由拦截器:vue全局前置 路由守卫router.beforeEach(to,from,next)（在页面跳转前即路径都没改变前执行）
//参数的含义：1.to:表示要进入到哪个路由去，2、from:从哪个路由跳转过来的 3、next：路由的控制参数，常用的有next(true)和next(false)
//路由守卫router.beforeEach表示在初始化时被调用，或者路由切换之前被调用，用于保护路由安全（登录权限验证）
router.beforeEach((to, from, next) => {
    console.log("路由拦截器----beforeEach")
    if (to.matched.length != 0) {
        console.log("-----to.matched.length != 0----if---"+to.matched.length)
        if (to.meta.requireAuth) { // 判断该路由是否需要登录权限
            console.log("-------判断该路由是否需要登录权限----if---"+to.meta.requireAuth)
            const userInfo = Boolean(localStorage.getItem('userInfo'))
            let siteUserRole = null
            try { const u = localStorage.getItem('siteUser'); if(u) siteUserRole = (JSON.parse(u).role) } catch(e) {}

            const isAdminRoute = to.path.indexOf('/admin') === 0
            const isMerchantRoute = to.path.indexOf('/merchant') === 0

            if (isAdminRoute) {
                if (userInfo) return next();
                return next({ path: '/home', query: { redirect: to.fullPath } })
            }
            if (isMerchantRoute) {
                if (siteUserRole === 'MERCHANT') return next();
                return next({ path: '/home', query: { redirect: to.fullPath } })
            }
            // 其他受保护路由，沿用原逻辑
            if (userInfo) return next();
            return next({ path: '/home', query: { redirect: to.fullPath } })
        } else {
            console.log("------判断该路由是否需要登录权限------else---")
            if (Boolean(localStorage.getItem("userInfo"))) { // 判断是否登录
                if (to.path != "/" && to.path != "/login") { //判断是否要跳到登录界面
                    next();
                } else {
                    /**
                     * 防刷新，如果登录，修改路由跳转到登录页面，修改路由为登录后的首页 
                     */
                    next({
                        path: '/admin/users'
                    })
                }
            } else {
                next();
            }
        }
    } else {
        console.log("-----to.matched.length != 0----else---")
        next({
            path: '/home',
            query: { redirect: to.fullPath } // 将跳转的路由path作为参数，登录成功后跳转到该路由
        })
    }
})

/* eslint-disable no-new */
new Vue({
    el: '#app',
    router,
    store, //使用store vuex状态管理
    components: { App },
    template: '<App/>',
    data: {
        // 空的实例放到根组件下，所有的子组件都能调用
        Bus: new Vue()
    }

})