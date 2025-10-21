<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">公告</span>
        <div>
          <el-form :inline="true" size="small">
            <el-form-item label="关键词">
              <el-input v-model="keyword" placeholder="按标题搜索" style="width:220px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="search">搜索</el-button>
              <el-button size="small" @click="reset">重置</el-button>
              <el-button size="small" @click="fetch()">刷新</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border @row-dblclick="openDetail">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="title" label="公告标题"/>
        <el-table-column prop="createdAt" label="创建时间" width="180"/>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openDetail(scope.row)">查看</el-button>
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

    <el-dialog :title="detail.title||'公告'" :visible.sync="dlg" width="680px">
      <div style="white-space: pre-wrap; line-height:1.6;">{{ detail.content }}</div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlg=false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { announcements, announcementDetail } from '@/api/site'
export default {
  name: 'MerchantAnnouncements',
  data(){
    return { items:[], page1:1, size:10, total:0, keyword:'', dlg:false, detail:{} }
  },
  methods:{
    async fetch(){
      const res = await announcements(this.page1-1, this.size, this.keyword)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = (payload.items||payload.content)||[]; this.total = (payload.total||payload.totalElements)||0 }
    },
    search(){ this.page1=1; this.fetch() },
    reset(){ this.keyword=''; this.search() },
    async openDetail(row){
      const res = await announcementDetail(row.id)
      const a = res && res.data && res.data.data
      this.detail = a || {}
      this.dlg = true
    }
  },
  created(){ this.fetch() }
}
</script>
