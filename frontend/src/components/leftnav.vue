/**
* 左边菜单
*/ 
<template>
  <el-menu default-active="2" :collapse="collapsed" collapse-transition router :default-active="$route.path" unique-opened class="el-menu-vertical-demo" background-color="#334157" text-color="#fff" active-text-color="#ffd04b">
    <div class="logobox" @click="$router.push('/home')" title="回到首页">
      <img class="logoimg" src="../assets/img/logo.png" alt="">
    </div>
    <el-submenu v-for="menu in allmenu" :key="menu.menuid" :index="menu.menuname">
      <template slot="title">
        <i class="iconfont" :class="menu.icon"></i>
        <span>{{menu.menuname}}</span>
      </template>
      <el-menu-item-group>
        <el-menu-item v-for="chmenu in menu.menus" :index="'/'+chmenu.url" :key="chmenu.menuid">
          <i class="iconfont" :class="chmenu.icon"></i>
          <span>{{chmenu.menuname}}</span>
        </el-menu-item>
      </el-menu-item-group>
    </el-submenu>
  </el-menu>
</template>
<script>
import { menu } from '../api/userMG'
export default {
  name: 'leftnav',
  data() {
    return {
      collapsed: false,
      allmenu: []
    }
  },
  // 创建完毕状态(里面是操作)
  created() {
    const buildMenus = () => {
      const path = this.$route.path || ''
      // 读取站点登录角色（用于 /charts/* 和 /ai/* 时保持对应菜单）
      let siteUserRole = null
      try { const su = localStorage.getItem('siteUser'); if (su) siteUserRole = (JSON.parse(su).role) } catch(e) {}
      const isCharts = path.indexOf('/charts') === 0
      const isAI = path.indexOf('/ai') === 0
      const keepByRole = isCharts || isAI
      const isMerchant = path.indexOf('/merchant') === 0 || (keepByRole && siteUserRole === 'MERCHANT')
      if (isMerchant) {
        // 商家后台菜单
        this.allmenu = [
          {
            menuid: 1100,
            icon: 'li-icon-xitongguanli',
            menuname: '商家功能',
            url: null,
            menus: [
              { menuid: 1101, icon: 'icon-cms-manage', menuname: '我的店铺', url: 'merchant/shop' },
              { menuid: 1102, icon: 'icon-cat-skuQuery', menuname: '我的商品', url: 'merchant/products' },
              { menuid: 1103, icon: 'icon-order-manage', menuname: '订单管理', url: 'merchant/orders' },
              { menuid: 1108, icon: 'icon-news-manage', menuname: '公告', url: 'merchant/announcements' }
            ]
          },
          {
            menuid: 1300,
            icon: 'li-icon-shangchengxitongtubiaozitihuayuanwenjian91',
            menuname: '图表',
            url: null,
            menus: [
              { menuid: 1301, icon: 'icon-provider-manage', menuname: '数据可视化', url: 'charts/statistics' },
              { menuid: 1302, icon: 'icon-provider-manage', menuname: 'AI助手', url: 'ai/chat' }
            ]
          }
        ]
      } else {
        // 管理员后台菜单
        this.allmenu = [
          {
            menuid: 100,
            icon: 'li-icon-xitongguanli',
            menuname: '管理员功能',
            url: null,
            menus: [
              { menuid: 101, icon: 'icon-cus-manage', menuname: '用户管理', url: 'admin/users' },
              { menuid: 102, icon: 'icon-cms-manage', menuname: '店铺管理', url: 'admin/shops' },
              { menuid: 103, icon: 'icon-cat-skuQuery', menuname: '商品管理', url: 'admin/products' },
              { menuid: 104, icon: 'icon-cat-skuQuery', menuname: '分类管理', url: 'admin/categories' },
              { menuid: 105, icon: 'icon-news-manage', menuname: '公告管理', url: 'admin/announcements' },
              
            ]
          },
          {
            menuid: 300,
            icon: 'li-icon-shangchengxitongtubiaozitihuayuanwenjian91',
            menuname: '图表',
            url: null,
            menus: [
              { menuid: 301, icon: 'icon-provider-manage', menuname: '数据可视化', url: 'charts/statistics' }
              ,{ menuid: 302, icon: 'icon-provider-manage', menuname: 'AI助手', url: 'ai/chat' }
            ]
          }
        ]
      }
    }

    buildMenus()

    // 监听侧边栏折叠
    this.$root.Bus.$on('toggle', value => {
      this.collapsed = !value
    })

    // 路由变化时重建菜单（在管理员与商家后台之间切换时）
    this.$watch('$route.path', () => buildMenus())
  }
}
</script>
<style>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 240px;
  min-height: 400px;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
  border: none;
  text-align: left;
}
.el-menu-item-group__title {
  padding: 0px;
}
.el-menu-bg {
  background-color: #1f2d3d !important;
}
.el-menu {
  border: none;
}
.logobox {
  height: 40px;
  line-height: 40px;
  color: #9d9d9d;
  font-size: 20px;
  text-align: center;
  padding: 20px 0px;
}
.logoimg {
  height: 40px;
  cursor: pointer;
}
</style>