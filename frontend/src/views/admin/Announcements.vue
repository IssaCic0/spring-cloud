<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">公告管理</span>
        <div>
          <el-form :inline="true" size="small">
            <el-form-item label="关键词">
              <el-input v-model="filters.keyword" placeholder="按标题搜索" style="width:220px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="search">搜索</el-button>
              <el-button size="small" @click="reset">重置</el-button>
              <el-button size="small" @click="fetch()">刷新</el-button>
              <el-button type="primary" size="small" @click="openCreate">新增公告</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="title" label="公告标题"/>
        <el-table-column prop="createdAt" label="创建时间" width="180"/>
        <el-table-column prop="updatedAt" label="更新时间" width="180"/>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="删除该公告？" @confirm="del(scope.row)">
              <el-button size="mini" slot="reference" type="danger">删除</el-button>
            </el-popconfirm>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="editId? '编辑公告':'新增公告'" :visible.sync="dlg" width="680px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input type="textarea" :rows="8" v-model="form.content" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlg=false">取 消</el-button>
        <el-button type="primary" :loading="loading" @click="submit">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { adminAnnouncements, adminGetAnnouncement, adminCreateAnnouncement, adminUpdateAnnouncement, adminDeleteAnnouncement } from '@/api/admin'
export default {
  name: 'AdminAnnouncements',
  data(){
    return {
      items:[], page1:1, size:10, total:0,
      filters: { keyword: '' },
      dlg:false, editId:null,
      form:{ title:'', content:'' },
      loading:false
    }
  },
  methods:{
    async fetch(){
      const res = await adminAnnouncements(this.page1-1, this.size, this.filters.keyword)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = payload.items||[]; this.total = payload.total||0 }
    },
    search(){ this.page1=1; this.fetch() },
    reset(){ this.filters.keyword=''; this.search() },
    openCreate(){ this.editId=null; this.form={ title:'', content:'' }; this.dlg=true },
    async openEdit(row){
      const res = await adminGetAnnouncement(row.id)
      const a = res && res.data && res.data.data
      if(a){ this.editId=a.id; this.form={ title:a.title, content:a.content }; this.dlg=true }
    },
    async submit(){
      if(!this.form.title){ return this.$message.warning('请填写公告标题') }
      if(!this.form.content){ return this.$message.warning('请填写公告内容') }
      try{
        this.loading = true
        if(this.editId){ await adminUpdateAnnouncement(this.editId, this.form) }
        else { await adminCreateAnnouncement(this.form) }
        this.$message.success('已保存')
        this.dlg=false
        this.fetch()
      } finally { this.loading=false }
    },
    async del(row){ await adminDeleteAnnouncement(row.id); this.$message.success('已删除'); this.fetch() }
  },
  created(){ this.fetch() }
}
</script>
