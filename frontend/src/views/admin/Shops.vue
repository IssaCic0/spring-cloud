<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">店铺管理</span>
        <div>
          <el-button type="primary" size="small" @click="openCreate">新增店铺</el-button>
          <el-button size="small" @click="fetch()">刷新</el-button>
        </div>
      </div>

      <el-form :inline="true" size="small" style="margin-top:10px;">
        <el-form-item label="商家ID">
          <el-input v-model.number="filters.ownerId" placeholder="按商家ID查询" style="width:160px;" />
        </el-form-item>
        <el-form-item label="店铺名">
          <el-input v-model="filters.name" placeholder="按店铺名称查询" style="width:200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="items" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="ownerId" label="商家ID" width="100"/>
        <el-table-column prop="name" label="店铺名"/>
        <el-table-column prop="description" label="简介"/>
        <el-table-column prop="logoUrl" label="Logo"/>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status==='OPEN'?'success':(scope.row.status==='BANNED'?'danger':(scope.row.status==='CLOSED'?'info':'warning'))">
              {{ scope.row.status | fmtShopStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="warning" @click="approve(scope.row)">审核通过</el-button>
            <el-button size="mini" type="danger" @click="ban(scope.row)">封店</el-button>
            <el-popconfirm title="删除该店铺？" @confirm="delById(scope.row)">
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

    <!-- 新增弹窗 -->
    <el-dialog title="新增店铺" :visible.sync="dlgCreate" width="520px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="商家ID">
          <el-input v-model.number="createForm.ownerId" placeholder="对应商家账号ID"/>
        </el-form-item>
        <el-form-item label="店铺名">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="createForm.description" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="createForm.logoUrl" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlgCreate=false">取 消</el-button>
        <el-button type="primary" :loading="loading.create" @click="submitCreate">保 存</el-button>
      </span>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog title="编辑店铺" :visible.sync="dlgEdit" width="520px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="店铺名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.description" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="editForm.logoUrl" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="选择状态">
            <el-option label="待审核" value="PENDING_APPROVAL"/>
            <el-option label="营业中" value="OPEN"/>
            <el-option label="已关闭" value="CLOSED"/>
            <el-option label="已封禁" value="BANNED"/>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlgEdit=false">取 消</el-button>
        <el-button type="primary" :loading="loading.edit" @click="submitEdit">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { 
  adminShops, adminApproveShop, adminBanShop,
  adminCreateShop, adminGetShop, adminUpdateShop,
  adminDeleteShop
} from '@/api/admin'
export default {
  name: 'AdminShops',
  data(){
    return {
      items:[], page1:1, size:10, total:0,
      filters: { ownerId: null, name: '' },
      dlgCreate: false, dlgEdit: false,
      createForm: { ownerId: null, name: '', description: '', logoUrl: '' },
      editForm: { id: null, name: '', description: '', logoUrl: '', status: 'PENDING_APPROVAL' },
      loading: { create: false, edit: false }
    }
  },
  methods:{
    async fetch(){
      const res = await adminShops(this.page1-1, this.size, this.filters.ownerId, this.filters.name)
      const payload = res && res.data && res.data.data
      if(payload){ this.items = payload.items||[]; this.total = payload.total||0 }
    },
    search(){ this.page1=1; this.fetch() },
    reset(){ this.filters.ownerId=null; this.filters.name=''; this.search() },
    async approve(row){ await adminApproveShop(row.id); this.$message.success('已通过'); this.fetch() },
    async ban(row){ await adminBanShop(row.id); this.$message.success('已封店'); this.fetch() },
    openCreate(){ this.createForm={ ownerId:null, name:'', description:'', logoUrl:'' }; this.dlgCreate=true },
    async submitCreate(){
      if(!this.createForm.ownerId || !this.createForm.name){ return this.$message.warning('请填写商家ID和店铺名') }
      try{ this.loading.create=true; await adminCreateShop(this.createForm); this.$message.success('创建成功'); this.dlgCreate=false; this.fetch() } finally { this.loading.create=false }
    },
    async openEdit(row){
      const res = await adminGetShop(row.id)
      const s = res && res.data && res.data.data
      if(s){ this.editForm = { id: s.id, name: s.name, description: s.description, logoUrl: s.logoUrl, status: s.status || 'PENDING_APPROVAL' }; this.dlgEdit=true }
    },
    async submitEdit(){
      try{ this.loading.edit=true; const payload = { name:this.editForm.name, description:this.editForm.description, logoUrl:this.editForm.logoUrl, status:this.editForm.status }; await adminUpdateShop(this.editForm.id, payload); this.$message.success('保存成功'); this.dlgEdit=false; this.fetch() } finally { this.loading.edit=false }
    },
    async delById(row){ await adminDeleteShop(row.id); this.$message.success('已删除'); this.fetch() }
  },
  created(){ this.fetch() }
}
</script>
<style>

</style>
