<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">商品管理</span>
        <div>
          <el-form :inline="true" size="small">
            <el-form-item label="关键词">
              <el-input v-model="filters.keyword" placeholder="按游戏名/ID搜索" style="width:220px;" />
            </el-form-item>
            <el-form-item label="店铺ID">
              <el-input v-model.number="filters.shopId" placeholder="按店铺ID过滤" style="width:140px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="search">搜索</el-button>
              <el-button size="small" @click="reset">重置</el-button>
              <el-button size="small" @click="fetch()">刷新</el-button>
              <el-button type="primary" size="small" @click="openCreate">新增商品</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="shopId" label="店铺ID" width="100"/>
        <el-table-column prop="title" label="游戏名称"/>
        <el-table-column prop="price" label="价格" width="120"/>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">{{ scope.row.status | fmtProductStatus }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="warning" @click="forceOffline(scope.row)">强制下架</el-button>
            <el-popconfirm title="删除该商品？" @confirm="del(scope.row)">
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

    <!-- 编辑弹窗 -->
    <el-dialog title="编辑商品" :visible.sync="dlgEdit" width="560px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="游戏名称">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model.number="editForm.price" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" filterable clearable placeholder="选择分类"
                     style="width:100%;" @visible-change="(v)=>{ if(v) loadCategories() }">
            <el-option v-for="c in catOptions" :key="c.id" :label="c.name" :value="c.name"/>
          </el-select>
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="editForm.coverUrl" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="选择状态">
            <el-option label="上架" value="ACTIVE" />
            <el-option label="下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlgEdit=false">取 消</el-button>
        <el-button type="primary" :loading="loading.edit" @click="submitEdit">保 存</el-button>
      </span>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog title="新增商品" :visible.sync="dlgCreate" width="560px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="所属店铺">
          <el-select v-model="createForm.shopId" filterable remote clearable reserve-keyword placeholder="搜索店铺名称或关键字"
                     :remote-method="remoteShops" :loading="shopLoading" style="width:100%;" @visible-change="handleShopSelectToggle">
            <el-option v-for="s in shopOptions" :key="s.id" :label="`${s.name} (#${s.id})`" :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="游戏名称">
          <el-input v-model="createForm.title" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model.number="createForm.price" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="createForm.category" filterable clearable placeholder="选择分类"
                     style="width:100%;" @visible-change="(v)=>{ if(v) loadCategories() }">
            <el-option v-for="c in catOptions" :key="c.id" :label="c.name" :value="c.name"/>
          </el-select>
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="createForm.coverUrl" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="createForm.status" placeholder="选择状态">
            <el-option label="上架" value="ACTIVE" />
            <el-option label="下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlgCreate=false">取 消</el-button>
        <el-button type="primary" :loading="loading.create" @click="submitCreate">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { adminProducts, adminForceOfflineProduct, adminGetProduct, adminUpdateProduct, adminDeleteProduct, adminCreateProduct, adminShops } from '@/api/admin'
import { categories } from '@/api/site'
export default {
  name: 'AdminProducts',
  data(){
    return {
      items:[], page1:1, size:10, total:0,
      filters: { keyword: '', shopId: null },
      dlgEdit: false,
      editForm: { id:null, title:'', price:null, description:'', coverUrl:'', category:'', status:'ACTIVE' },
      dlgCreate: false,
      createForm: { shopId:null, title:'', price:null, description:'', coverUrl:'', category:'', status:'ACTIVE' },
      shopOptions: [], shopLoading: false,
      loading: { edit: false, create: false },
      catOptions: []
    }
  },
  methods:{
    async fetch(){
      const res = await adminProducts(this.page1-1, this.size, this.filters.keyword, this.filters.shopId)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = payload.items||[]; this.total = payload.total||0 }
    },
    search(){ this.page1 = 1; this.fetch() },
    reset(){ this.filters = { keyword:'', shopId:null }; this.search() },
    async forceOffline(row){ await adminForceOfflineProduct(row.id); this.$message.success('已下架'); this.fetch() },
    async openEdit(row){
      const res = await adminGetProduct(row.id)
      const p = res && res.data && res.data.data
      if(p){
        this.editForm = { id:p.id, title:p.title, price:p.price, description:p.description, coverUrl:p.coverUrl, category:p.category, status:p.status||'ACTIVE' }
        this.dlgEdit = true
        this.loadCategories()
      }
    },
    async submitEdit(){
      try{
        this.loading.edit = true
        const payload = { title:this.editForm.title, price:this.editForm.price, description:this.editForm.description, coverUrl:this.editForm.coverUrl, category:this.editForm.category, status:this.editForm.status }
        await adminUpdateProduct(this.editForm.id, payload)
        this.$message.success('保存成功')
        this.dlgEdit = false
        this.fetch()
      } finally {
        this.loading.edit = false
      }
    },
    async del(row){ await adminDeleteProduct(row.id); this.$message.success('已删除'); this.fetch() },
    openCreate(){ this.createForm = { shopId:null, title:'', price:null, description:'', coverUrl:'', category:'', status:'ACTIVE' }; this.dlgCreate=true; this.remoteShops(''); this.loadCategories() },
    async remoteShops(query){
      this.shopLoading = true
      try{
        const keyword = (query == null) ? '' : String(query)
        const res = await adminShops(0, 20, null, keyword)
        const payload = res && res.data && res.data.data
        this.shopOptions = payload && payload.items ? payload.items : []
      } finally {
        this.shopLoading = false
      }
    },
    handleShopSelectToggle(visible){ if(visible && (!this.shopOptions || this.shopOptions.length===0)) { this.remoteShops('') } },
    async loadCategories(){
      try{
        const res = await categories(0, 100, true)
        const payload = res && res.data && res.data.data
        this.catOptions = (payload && payload.items) || []
      }catch(e){ this.catOptions = [] }
    },
    async submitCreate(){
      if(!this.createForm.shopId){ return this.$message.warning('请选择店铺') }
      if(!this.createForm.title || this.createForm.price == null){ return this.$message.warning('请填写游戏名称与价格') }
      try{
        this.loading.create = true
        await adminCreateProduct(this.createForm)
        this.$message.success('创建成功')
        this.dlgCreate = false
        this.fetch()
      } finally {
        this.loading.create = false
      }
    }
  },
  created(){ this.fetch() }
}
</script>
