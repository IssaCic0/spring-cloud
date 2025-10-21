<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <span style="font-weight:bold;font-size:16px;">我的店铺</span>
        <div>
          <el-button size="small" @click="fetchList">刷新</el-button>
          <el-button type="primary" size="small" @click="openCreate">新建店铺</el-button>
        </div>
      </div>

      <el-table :data="items" border size="small" style="width:100%;margin-top:12px;">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="name" label="店铺名称"/>
        <el-table-column prop="status" label="状态" width="140">
          <template slot-scope="scope">{{ scope.row.status }}</template>
        </el-table-column>
        <el-table-column label="Logo" width="100">
          <template slot-scope="scope">
            <el-image v-if="scope.row.logoUrl" :src="scope.row.logoUrl" style="width:60px;height:60px" fit="cover"/>
            <span v-else style="color:#999;">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="warning" @click="closeById(scope.row)">关闭</el-button>
            <el-button size="mini" type="danger" @click="deleteById(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="text-align:right;margin-top:10px;">
        <el-pagination background layout="total, prev, pager, next, sizes"
          :page-sizes="[5,10,20,50]" :total="total" :page-size="size" :current-page.sync="page"
          @size-change="(s)=>{size=s; fetchList()}" @current-change="(p)=>{page=p; fetchList()}"/>
      </div>
    </el-card>

    <!-- 新建/编辑 弹窗 -->
    <el-dialog :title="editId? '编辑店铺':'新建店铺'" :visible.sync="open" width="520px">
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="店铺名"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description"/></el-form-item>
        <el-form-item label="Logo">
          <el-upload
            class="avatar-uploader"
            action="/api/files/upload"
            name="file"
            :headers="uploadHeaders"
            :show-file-list="false"
            accept="image/*"
            :on-success="onUploadSuccess"
            :on-error="onUploadError">
            <el-image v-if="form.logoUrl" :src="form.logoUrl" style="width:100px;height:100px" fit="cover"/>
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div style="color:#999;font-size:12px;margin-top:4px;">也可直接填入图片URL</div>
          <el-input v-model="form.logoUrl" placeholder="图片URL" style="margin-top:6px;"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submit">保 存</el-button>
      </span>
    </el-dialog>
  </div>
 </template>
<script>
import { myShops, createMyShop, updateMyShop, closeMyShopById, deleteMyShopHardById } from '@/api/merchant'
export default{
  name:'MerchantShop',
  data(){return{ items:[], page:1, size:10, total:0, open:false, editId:null, form:{ name:'', description:'', logoUrl:'' } }},
  computed:{
    uploadHeaders(){
      let uid='2';
      try{ const su=localStorage.getItem('siteUser'); if(su){ const u=JSON.parse(su); if(u&&u.id) uid=String(u.id) } }catch(e){}
      return { 'X-Role':'MERCHANT', 'X-User-Id': uid }
    }
  },
  methods:{
    async fetchList(){
      try{
        const { data } = await myShops(this.page-1, this.size)
        const payload = data && data.data
        this.items = (payload && payload.items) || []
        this.total = (payload && payload.total) || 0
      }catch(e){ this.items=[]; this.total=0 }
    },
    onUploadSuccess(res){ if(res && res.success){ this.form.logoUrl = (res.data && res.data.url) || ''; this.$message.success('上传成功') } else { this.$message.error((res && res.message)||'上传失败') } },
    onUploadError(){ this.$message.error('上传失败') },
    openCreate(){ this.editId=null; this.form={ name:'', description:'', logoUrl:'' }; this.open=true },
    openEdit(row){ this.editId=row.id; this.form={ name:row.name, description:row.description, logoUrl:row.logoUrl }; this.open=true },
    async submit(){
      if(!this.form.name){ this.$message.error('请输入店铺名'); return }
      if(this.editId){ await updateMyShop(this.editId, this.form) } else { await createMyShop(this.form) }
      this.$message.success('已保存'); this.open=false; this.fetchList()
    },
    async closeById(row){
      try{ await this.$confirm('确定关闭该店铺？','提示') }catch(e){ return }
      try{ await closeMyShopById(row.id); this.$message.success('已关闭'); this.fetchList() }catch(e){ this.$message.error('操作失败') }
    },
    async deleteById(row){
      try{ await this.$confirm('确定删除该店铺？仅当没有商品/订单/支付记录时允许，且操作不可恢复。','提示',{type:'warning'}) }catch(e){ return }
      try{ await deleteMyShopHardById(row.id); this.$message.success('已删除'); this.fetchList() }catch(e){ this.$message.error('删除失败，可能存在商品/订单/支付记录') }
    }
  },
  created(){ this.fetchList() }
}
</script>
<style scoped>
.avatar-uploader .el-upload { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader .el-upload:hover { border-color: #409EFF; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; line-height: 100px; text-align: center; }
</style>
