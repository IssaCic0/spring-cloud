<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">分类管理</span>
        <div>
          <el-form :inline="true" size="small">
            <el-form-item label="关键词">
              <el-input v-model="filters.keyword" placeholder="按名称/别名搜索" style="width:220px;" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filters.enabled" clearable placeholder="全部" style="width:120px;">
                <el-option label="启用" :value="true"/>
                <el-option label="停用" :value="false"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="search">搜索</el-button>
              <el-button size="small" @click="reset">重置</el-button>
              <el-button size="small" @click="fetch()">刷新</el-button>
              <el-button type="primary" size="small" @click="openCreate">新增分类</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="slug" label="别名(slug)"/>
        <el-table-column prop="parentId" label="父级ID" width="100"/>
        <el-table-column prop="sort" label="排序" width="100"/>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.enabled? 'success':'info'">{{ scope.row.enabled? '启用':'停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="删除该分类？" @confirm="del(scope.row)">
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

    <el-dialog :title="editId? '编辑分类':'新增分类'" :visible.sync="dlg" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="别名(slug)"><el-input v-model="form.slug" placeholder="可留空，自动生成"/></el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="form.parentId" clearable filterable placeholder="选择父级(可空)" style="width:100%;">
            <el-option v-for="c in parentOptions" :key="c.id" :label="`${c.name} (#${c.id})`" :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :step="1"/></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled"/>
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
import { adminCategories, adminCreateCategory, adminGetCategory, adminUpdateCategory, adminDeleteCategory } from '@/api/admin'
import { categories as publicCategories } from '@/api/site'
export default {
  name: 'AdminCategories',
  data(){
    return {
      items:[], page1:1, size:10, total:0,
      filters:{ keyword:'', enabled:null },
      dlg:false, editId:null, loading:false,
      form:{ name:'', slug:'', parentId:null, sort:0, enabled:true },
      parentOptions:[]
    }
  },
  methods:{
    async fetch(){
      const res = await adminCategories(this.page1-1, this.size, this.filters.keyword, this.filters.enabled)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = payload.items||[]; this.total = payload.total||0 }
    },
    search(){ this.page1=1; this.fetch() },
    reset(){ this.filters={ keyword:'', enabled:null }; this.search() },
    async loadParents(){
      const res = await publicCategories(0, 100, true)
      const payload = res && res.data && res.data.data
      this.parentOptions = (payload && payload.items) || []
    },
    openCreate(){ this.editId=null; this.form={ name:'', slug:'', parentId:null, sort:0, enabled:true }; this.dlg=true; this.loadParents() },
    async openEdit(row){
      const res = await adminGetCategory(row.id)
      const c = res && res.data && res.data.data
      if(c){ this.editId=c.id; this.form={ name:c.name, slug:c.slug, parentId:c.parentId, sort:c.sort, enabled: !!c.enabled }; this.dlg=true; this.loadParents() }
    },
    async submit(){
      if(!this.form.name){ return this.$message.warning('请填写分类名称') }
      try{
        this.loading = true
        if(this.editId){ await adminUpdateCategory(this.editId, this.form) } else { await adminCreateCategory(this.form) }
        this.$message.success('已保存'); this.dlg=false; this.fetch()
      } finally { this.loading=false }
    },
    async del(row){ await adminDeleteCategory(row.id); this.$message.success('已删除'); this.fetch() }
  },
  created(){ this.fetch() }
}
</script>
<style scoped>
</style>
