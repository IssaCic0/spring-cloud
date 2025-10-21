<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">店铺订单</span>
        <div>
          <el-input v-model="qOrderId" placeholder="订单ID" size="small" style="width:120px;margin-right:8px;"/>
          <el-input v-model="qShopId" placeholder="店铺ID(可选)" size="small" style="width:120px;margin-right:8px;"/>
          <el-select v-model="qShip" placeholder="发货状态" size="small" style="width:140px; margin-right:8px;">
            <el-option v-for="opt in shipOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button size="small" type="primary" @click="doSearch">搜索</el-button>
          <el-button size="small" @click="resetSearch" style="margin-left:8px;">重置</el-button>
          <el-button size="small" @click="fetch" style="margin-left:8px;">刷新</el-button>
        </div>
      </div>
      <el-table :data="filteredItems" style="width:100%;margin-top:12px;" size="small" border @expand-change="onExpand">
        <el-table-column type="expand">
          <template slot-scope="props">
            <div style="padding:10px 0;">
              <el-table :data="orderItemsMap[props.row.id]||[]" size="mini" border style="width:100%;">
                <el-table-column prop="productId" label="商品ID" width="100"/>
                <el-table-column prop="productTitle" label="游戏名称"/>
                <el-table-column prop="productPrice" label="单价" width="100"/>
                <el-table-column prop="quantity" label="数量" width="80"/>
                <el-table-column prop="subtotal" label="小计" width="120"/>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="userId" label="买家ID" width="100"/>
        <el-table-column prop="totalAmount" label="金额" width="120"/>
        <el-table-column label="发货状态" width="120">
          <template slot-scope="scope">{{ scope.row.shipStatus | fmtShipStatus }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.shipStatus!=='SHIPPED'" size="mini" type="primary" @click="ship(scope.row)">发货</el-button>
            <el-button v-if="scope.row.shipStatus==='SHIPPED'" size="mini" @click="revertShip(scope.row)">撤回发货</el-button>
            <el-button size="mini" @click="onExpand(scope.row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="text-align:right;margin-top:10px;">
        <el-pagination background layout="total, prev, pager, next, sizes"
          :page-sizes="[5,10,20,50]" :total="total" :page-size="size" :current-page.sync="page1"
          @size-change="(s)=>{size=s; fetch()}" @current-change="(p)=>{page1=p; fetch()}"/>
      </div>
    </el-card>
  </div>
</template>
<script>
import { merchantOrders, updateShipStatus, orderItems } from '@/api/merchant'
export default {
  name:'MerchantOrders',
data(){return{ items:[], page1:1, size:10, total:0, shipStatuses:['CREATED','SHIPPED','DELIVERED'], shipNext:'', qOrderId:'', qShip:'', qShopId:'', orderItemsMap:{} }},
  computed:{
    shipOptions(){
      const fmt = this.$options.filters && this.$options.filters.fmtShipStatus
      return this.shipStatuses.map(s => ({ value: s, label: fmt ? fmt(s) : s }))
    },
    filteredItems(){
      let list = this.items
      if(this.qOrderId){ list = list.filter(x => String(x.id||'').includes(String(this.qOrderId).trim())) }
      if(this.qShip){ list = list.filter(x => String(x.shipStatus||'') === this.qShip) }
      return list
    }
  },
  methods:{
    async fetch(){
      try{
        const shopId = this.qShopId ? Number(this.qShopId) : undefined;
        const { data } = await merchantOrders(this.page1-1, this.size, shopId);
        const payload = data && data.data;
        if(payload){ this.items = (payload.items||payload.content)||[]; this.total = (payload.total||payload.totalElements)||0 }
        else { this.items = []; this.total = 0 }
      }catch(e){
        const msg = (e && e.response && e.response.data && (e.response.data.message||e.response.data.msg)) || e.message || '加载失败'
        this.$message.error(msg)
        this.items = []; this.total = 0
      }
    },
    async updateShip(row, status){ await updateShipStatus(row.id, status); this.$message.success('发货状态已更新'); this.fetch() },
    async ship(row){ await this.updateShip(row, 'SHIPPED') },
    async revertShip(row){ await this.updateShip(row, 'CREATED') },
    doSearch(){ /* 由于后端暂未提供筛选参数，这里采用前端过滤，分页统计仍基于原总数 */ },
    resetSearch(){ this.qOrderId=''; this.qShip='' },
    async onExpand(row, expandedRows){
      if(this.orderItemsMap[row.id]) return
      const { data } = await orderItems(row.id)
      const payload = data && data.data
      const list = Array.isArray(payload) ? payload : ((payload && payload.items) || [])
      this.$set(this.orderItemsMap, row.id, list)
    }
  },
  created(){ this.fetch() }
}
</script>
