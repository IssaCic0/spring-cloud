<template>
  <div class="home-wrap">
    <el-container>
      <el-header height="60px" class="home-header">
        <div class="brand" @click="$router.push('/home')">在线游戏商城</div>
        <div class="actions">
          <template v-if="!isLogin">
            <el-button size="small" @click="$router.push('/login')">登录</el-button>
            <el-button size="small" @click="$router.push('/cart')">购物车</el-button>
          </template>
          <template v-else>
            <el-dropdown @command="onMenu">
              <span class="el-dropdown-link">
                你好，{{ user.username }}（{{ user.role | fmtRole }}）<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="user.role==='ADMIN'" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item v-if="user.role==='MERCHANT'" command="merchant">商家中心</el-dropdown-item>
                <el-dropdown-item command="cart">购物车</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </div>
      </el-header>
      <el-main class="home-main">
        <div class="hero">
          <h1>欢迎来到在线游戏商城</h1>
          <p>高效的商家管理与流畅的购物体验</p>
          <div style="margin-top:18px;">
            <el-button type="primary" @click="toExplore">逛一逛</el-button>
          </div>
        </div>
        <!-- 分类导航 -->
        <el-card class="cat-card">
          <div class="cat-bar">
            <el-button size="mini" :type="!selectedCategory?'primary':'default'" @click="goCategory(null)">全部游戏</el-button>
            <el-button
              v-for="c in catOptions"
              :key="c.id"
              size="mini"
              :type="selectedCategory===c.name?'primary':'default'"
              @click="goCategory(c.name)">{{ c.name }}</el-button>
          </div>
        </el-card>
        <el-card class="home-products">
          <div style="display:flex; justify-content: space-between; align-items:center;">
            <span style="font-weight:bold; font-size:16px;">热卖商品</span>
            <div>
              <el-input v-model="keyword" size="small" placeholder="搜索游戏" style="width:240px;margin-right:8px;"/>
              <el-button size="small" @click="goSearch">搜索</el-button>
              <el-button type="primary" size="small" style="margin-left:8px;" @click="goCategory(selectedCategory)">更多</el-button>
            </div>
          </div>
          <div class="grid">
            <div v-for="p in items" :key="p.id" class="card">
              <el-image :src="p.coverUrl" fit="cover" lazy class="card-img">
                <div slot="error" class="img-fallback">加载失败</div>
              </el-image>
              <div class="title" :title="p.title">{{ p.title }}</div>
              <div :class="['price', Number(p.price)===0 ? 'price--free' : '']">￥{{ p.price }}</div>
              <div class="actions">
                <el-button size="mini" @click="view(p)">详情</el-button>
                <el-button size="mini" @click="add(p)">加入购物车</el-button>
                <el-button class="buy-btn" size="mini" type="primary" @click="buyNow(p)">立即购买</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>

    <!-- 登录弹窗 -->
    <el-dialog title="登录" :visible.sync="loginVisible" width="420px">
      <el-form :model="loginForm" ref="loginFormRef" label-width="80px" size="small">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="loginVisible=false">取 消</el-button>
        <el-button type="primary" @click="doLogin">登 录</el-button>
      </span>
    </el-dialog>

  </div>
</template>
<script>
import { siteLogin, searchProducts, productDetail, categories } from '@/api/site'
export default {
  name:'SiteHome',
  data(){
    return{
      loginVisible:false,
      loginForm:{ username:'', password:'' },
      user: null,
      items: [],
      keyword: '',
      catOptions: [],
      selectedCategory: null,
    }
  },
  computed:{
    isLogin(){ return !!this.user }
  },
  created(){
    try{ const u = localStorage.getItem('siteUser'); if(u) this.user = JSON.parse(u) }catch(e){}
    if(!this.user){
      const ui = localStorage.getItem('userInfo')
      const aid = localStorage.getItem('adminUserId')
      if(ui === 'true' && aid){ this.user = { id: Number(aid), username: 'admin', role: 'ADMIN' } }
    }
    this.fetch()
    // 加载分类
    this.loadCats()
  },
  methods:{
    async doLogin(){
      if(!this.loginForm.username || !this.loginForm.password){ this.$message.error('请输入用户名和密码'); return }
      try{
        const { data } = await siteLogin(this.loginForm)
        if(data && data.success){
          this.user = data.data
          localStorage.setItem('siteUser', JSON.stringify(this.user))
          // 管理端识别：仅当管理员登录时设置 adminUserId 与 userInfo
          if(this.user.role === 'ADMIN'){
            localStorage.setItem('adminUserId', String(this.user.id))
            localStorage.setItem('userInfo', 'true')
          }else{
            localStorage.removeItem('adminUserId')
            localStorage.setItem('userInfo', '')
          }
          this.$message.success('登录成功')
          this.loginVisible=false
        }else{
          this.$message.error((data && (data.message||data.msg)) || '登录失败')
        }
      }catch(e){
        const msg = (e && e.response && e.response.data && (e.response.data.message||e.response.data.msg)) || e.message || '服务异常'
        this.$message.error(msg)
      }
    },
    async doRegister(){
      // 当前版本已移除注册功能
      this.$message.info('已移除注册功能，如需注册请联系管理员')
    },
    onMenu(cmd){
      if(cmd==='logout'){
        this.user=null
        localStorage.removeItem('siteUser')
        localStorage.removeItem('adminUserId')
        localStorage.setItem('userInfo','')
        this.$message.success('已退出登录')
      }else if(cmd==='admin'){
        this.$router.push('/admin/users')
      }else if(cmd==='merchant'){
        this.$router.push('/merchant/shop')
      }else if(cmd==='cart'){
        this.$router.push('/cart')
      }
    },
    toExplore(){ this.$router.push('/products') },
    async fetch(){ const { data } = await searchProducts(this.keyword, 0, 12, this.selectedCategory); const p = data && data.data; if(p){ this.items = p.items||[] } },
    async view(p){ const { data } = await productDetail(p.id); const d = data && data.data; this.$alert(`游戏名称：${d.title}\n价格：￥${d.price}\n类型：${d.category||'-'}`, '商品详情', { confirmButtonText:'知道了' }) },
    async loadCats(){ try{ const { data } = await categories(0, 100, true); const payload = data && data.data; this.catOptions = (payload && payload.items) || [] } catch(e){} },
    goCategory(cat){ this.selectedCategory = cat || null; this.$router.push({ path:'/products', query: { category: this.selectedCategory || undefined } }) },
    goSearch(){ this.$router.push({ path:'/products', query: { keyword: this.keyword || undefined, category: this.selectedCategory || undefined } }) },
    add(p){
      const item = { productId: p.id, shopId: p.shopId, title: p.title, price: p.price, coverUrl: p.coverUrl, quantity: 1 }
      let cart = []
      try{ const s = localStorage.getItem('cartItems'); if(s) cart = JSON.parse(s) }catch(e){}
      const idx = cart.findIndex(x => x.productId === item.productId)
      if(idx >= 0){ cart[idx].quantity += 1 } else { cart.push(item) }
      localStorage.setItem('cartItems', JSON.stringify(cart))
      this.$message.success('已加入购物车')
    },
    buyNow(p){ this.add(p); this.$router.push('/cart') }
  }
}
</script>
<style scoped>
.home-header{ display:flex; justify-content:space-between; align-items:center; box-shadow:0 2px 8px rgba(0,0,0,.06); background:#fff; }
.brand{ font-weight:bold; font-size:18px; cursor:pointer; }
.actions{ display:flex; align-items:center; gap:8px; }
.home-main{ background:#f5f7fa; min-height: calc(100vh - 60px); }
.hero{ text-align:center; padding:100px 0; }
.hero h1{ margin:0; font-size:32px; }
.hero p{ color:#666; margin-top:8px; }
.home-products{ margin: 16px; }
.grid{ margin-top:12px; display:grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap:12px; }
.card{ background:#fff; border:1px solid #eee; border-radius:6px; padding:10px; display:flex; flex-direction:column; overflow:hidden; box-sizing:border-box; }
.title{ margin-top:8px; font-weight:600; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.price{ color:#e91e63; margin:6px 0; }
.actions{ display:grid; grid-template-columns: 1fr 1fr; gap:6px; }
.actions .el-button{ width:100%; }
.buy-btn{ grid-column: 1 / -1; }
</style>
