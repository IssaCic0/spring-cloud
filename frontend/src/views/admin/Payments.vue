<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">支付记录</span>
        <div>
          <el-button size="small" @click="fetch()">刷新</el-button>
        </div>
      </div>
      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="orderId" label="订单ID" width="100"/>
        <el-table-column prop="buyerId" label="买家ID" width="100"/>
        <el-table-column prop="shopId" label="店铺ID" width="100"/>
        <el-table-column prop="amount" label="金额" width="120"/>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status==='PAID'?'success':(scope.row.status==='REFUNDED'?'info':(scope.row.status==='PENDING'?'warning':'danger'))">
              {{ scope.row.status | fmtPaymentStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="success" @click="reissue(scope.row)">补单</el-button>
            <el-button size="mini" type="warning" @click="markRefund(scope.row)">标记退款</el-button>
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
import { adminPayments, adminReissuePayment, adminMarkRefundPayment } from '@/api/admin'
export default {
  name: 'AdminPayments',
  data(){return{items:[], page1:1, size:10, total:0}},
  methods:{
    async fetch(){
      const res = await adminPayments(this.page1-1, this.size)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = payload.items||[]; this.total = payload.total||0 }
    },
    async reissue(row){ await adminReissuePayment(row.id); this.$message.success('已补单'); this.fetch() },
    async markRefund(row){ await adminMarkRefundPayment(row.id); this.$message.success('已标记退款'); this.fetch() }
  },
  created(){ this.fetch() }
}
</script>
