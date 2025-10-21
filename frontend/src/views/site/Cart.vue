<template>
  <div class="cart-page">
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">购物车</span>
        <div>
          <el-button size="small" @click="load">刷新</el-button>
          <el-button size="small" type="danger" :disabled="!items.length" @click="clearCart">清空</el-button>
        </div>
      </div>

      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border stripe>
        <template slot="empty">
          <div style="padding:12px;color:#999;">购物车还是空的</div>
          <el-button type="primary" size="mini" @click="$router.push('/products')">去逛逛</el-button>
        </template>
        <el-table-column prop="productId" label="商品ID" width="100"/>
        <el-table-column label="封面" width="100">
          <template slot-scope="s">
            <el-image :src="s.row.coverUrl" fit="cover" style="width:60px;height:60px"/>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="游戏名称"/>
        <el-table-column label="单价" width="120" align="right">
          <template slot-scope="s">￥{{ Number(s.row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="180" align="center">
          <template slot-scope="s">
            <el-input-number class="qty-input" v-model="s.row.quantity" :min="1" :step="1" controls-position="right" size="small" @change="onQtyChange(s.row)"/>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="140" align="right">
          <template slot-scope="s">{{ (Number(s.row.price) * Number(s.row.quantity)).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="s">
            <el-button type="text" size="mini" @click="removeOne(s.row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <div class="total">合计：￥{{ totalAmount.toFixed(2) }}</div>
        <div class="footer-actions">
          <el-button @click="$router.push('/home')">返回首页</el-button>
          <el-button :disabled="!items.length" @click="$router.push('/products')">继续逛逛</el-button>
          <el-button type="primary" :disabled="!items.length" @click="checkout">去结算</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script>
export default{
  name:'SiteCart',
  data(){ return { items:[] } },
  computed:{
    totalAmount(){ return this.items.reduce((s,x)=> s + Number(x.price) * Number(x.quantity), 0) }
  },
  methods:{
    load(){ try{ const s = localStorage.getItem('cartItems'); this.items = s ? JSON.parse(s) : [] }catch(e){ this.items=[] } },
    save(){ localStorage.setItem('cartItems', JSON.stringify(this.items)) },
    clearCart(){ this.items=[]; this.save() },
    removeOne(row){ this.items = this.items.filter(x=>x.productId!==row.productId); this.save() },
    onQtyChange(row){ if(row.quantity<1) row.quantity=1; this.save() },
    checkout(){
      if(!this.items.length) return
    }
  },
  created(){ this.load() }
}
</script>
<style scoped>
.cart-page{ padding:10px; }
.footer{ display:flex; justify-content: space-between; align-items:center; padding:12px 0; }
.footer-actions .el-button + .el-button{ margin-left:8px; }
.total{ font-size:18px; color:#e91e63; }
.qty-input{ width: 140px; }
.qty-input .el-input__inner{ text-align:center; }
</style>
