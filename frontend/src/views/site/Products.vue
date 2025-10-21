<template>
  <div class="products-page">
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">商品列表</span>
        <div>
          <el-select v-model="selectedCategory" clearable filterable placeholder="按分类筛选" size="small" style="width:180px;margin-right:8px;">
            <el-option v-for="c in catOptions" :key="c.id" :label="c.name" :value="c.name"/>
          </el-select>
          <el-input v-model="keyword" size="small" placeholder="搜索游戏" style="width:240px;margin-right:8px;"/>
          <el-button size="small" type="primary" @click="fetch">搜索</el-button>
          <el-button size="small" style="margin-left:8px;" @click="$router.push('/home')">返回首页</el-button>
          <el-button size="small" style="margin-left:8px;" @click="$router.push('/cart')">购物车</el-button>
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
      <div style="text-align:right;margin-top:10px;">
        <el-pagination background layout="total, prev, pager, next, sizes"
          :page-sizes="[8,16,24,40]" :total="total" :page-size="size" :current-page.sync="page1"
          @size-change="(s)=>{size=s; fetch()}" @current-change="(p)=>{page1=p; fetch()}"/>
      </div>
    </el-card>
  </div>
</template>
<script>
import { searchProducts, productDetail, categories } from '@/api/site'
export default{
  name:'SiteProducts',
  data(){ return { items:[], page1:1, size:8, total:0, keyword:'', selectedCategory:null, catOptions:[] } },
  methods:{
    async fetch(){ const { data } = await searchProducts(this.keyword, this.page1-1, this.size, this.selectedCategory); const p = data && data.data; if(p){ this.items = p.items||[]; this.total = p.total||0 } },
    async view(p){ const { data } = await productDetail(p.id); const d = data && data.data; this.$alert(`游戏名称：${d.title}\n价格：￥${d.price}\n类型：${d.category||'-'}`, '商品详情', { confirmButtonText:'知道了' }) },
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
  },
  async created(){
    try{
      const { data } = await categories(0, 100, true);
      const payload = data && data.data; this.catOptions = (payload && payload.items) || []
    }catch(e){}
    // 从路由读取默认分类
    const qCat = this.$route && this.$route.query && this.$route.query.category
    this.selectedCategory = qCat ? String(qCat) : null
    // 从路由读取默认关键词
    const qKey = this.$route && this.$route.query && this.$route.query.keyword
    this.keyword = qKey ? String(qKey) : ''
    this.fetch()
  },
  watch:{
    '$route.query.category'(val){
      this.selectedCategory = val ? String(val) : null
      this.page1 = 1
      this.fetch()
    },
    '$route.query.keyword'(val){
      this.keyword = val ? String(val) : ''
      this.page1 = 1
      this.fetch()
    }
  }
}
</script>
<style scoped>
.products-page{ padding:10px; }
.grid{ margin-top:12px; display:grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap:14px; }
.card{ background:#fff; border:1px solid #eee; border-radius:10px; padding:10px; display:flex; flex-direction:column; transition: all .2s ease; }
.card:hover{ box-shadow:0 8px 18px rgba(0,0,0,.08); transform: translateY(-2px); }
.card-img{ width:100%; height:160px; border-radius:6px; overflow:hidden; background:#f6f7f8; }
.img-fallback{ width:100%; height:100%; display:flex; align-items:center; justify-content:center; color:#aaa; font-size:12px; background: #f8f8f8; }
.title{ margin-top:8px; font-weight:600; height:40px; line-height:20px; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; line-clamp: 2; }
.price{ color:#e91e63; margin:6px 0; font-weight:600; }
.price--free{ color:#67C23A; }
.actions{ display:grid; grid-template-columns: 1fr 1fr; gap:6px; }
.buy-btn{ grid-column: 1 / -1; }
</style>
