<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">订单管理</span>
        <div>
          <el-button size="small" @click="fetch()">刷新</el-button>
        </div>
      </div>
      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="buyerId" label="买家ID" width="100"/>
        <el-table-column prop="shopId" label="店铺ID" width="100"/>
        <el-table-column label="订单状态" width="130">
          <template slot-scope="scope">{{ scope.row.status | fmtOrderStatus }}</template>
        </el-table-column>
        <el-table-column label="发货状态" width="120">
          <template slot-scope="scope">{{ scope.row.shipStatus | fmtShipStatus }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="120"/>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="warning" @click="refund(scope.row)">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="text-align:right;margin-top:10px;">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :page-sizes="[5,10,20,50]"
          :total="total"
          :page-size="size"
          :current-page.sync="page1"
          @size-change="(s)=>{size=s; fetch()}"
          @current-change="(p)=>{page1=p; fetch()}"
        />
      </div>
    </el-card>
  </div>
</template>
<script>
import { adminOrders, adminRefundByOrder } from '@/api/admin'
export default {
  name:'AdminOrders',
  data(){return{items:[], page1:1, size:10, total:0}},
  methods:{
    async fetch(){
      const res = await adminOrders(this.page1-1, this.size)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = payload.items||[]; this.total = payload.total||0 }
    },
    async refund(row){
      try{
        const { value } = await this.$prompt(`对订单 ${row.id} 进行退款，请填写备注（可空）：`, '退款', { inputPlaceholder:'备注（可选）' })
        await adminRefundByOrder(row.id, value || '')
        this.$message.success('已退款')
        this.fetch()
      }catch(e){ /* cancel or error */ }
    }
  },
  created(){ this.fetch() }
}
</script>
