<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <span style="font-weight:bold; font-size:16px;">我的商品</span>
        <div>
          <el-input v-model="searchKeyword" placeholder="搜索游戏名/ID/类目" size="small" style="width:240px;margin-right:8px;"/>
          <el-button size="small" @click="fetch">刷新</el-button>
          <el-button type="primary" size="small" @click="openCreate">新增商品</el-button>
          <el-button :disabled="!selected.length" type="danger" size="small" @click="batchRemove" style="margin-left:8px;">批量删除</el-button>
        </div>
      </div>
      <el-table :data="filteredItems" style="width:100%;margin-top:12px;" size="small" border @selection-change="onSelect">
        <el-table-column type="selection" width="50"/>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column label="封面" width="100">
          <template slot-scope="scope">
            <el-image v-if="scope.row.coverUrl" :src="scope.row.coverUrl" :preview-src-list="[scope.row.coverUrl]" style="width:60px;height:60px" fit="cover"/>
            <span v-else style="color:#999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="游戏名称"/>
        <el-table-column prop="price" label="价格" width="120"/>
        <el-table-column prop="category" label="类目" width="140"/>
        <el-table-column label="销量" width="120">
          <template slot-scope="scope">{{ salesMap[scope.row.id] || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">{{ scope.row.status | fmtProductStatus }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="text-align:right;margin-top:10px;">
        <el-pagination background layout="total, prev, pager, next, sizes"
          :page-sizes="[5,10,20,50]" :total="total" :page-size="size" :current-page.sync="page1"
          @size-change="(s)=>{size=s; fetch()}" @current-change="(p)=>{page1=p; fetch()}"/>
      </div>
    </el-card>

    <!-- 创建/编辑 弹窗 -->
    <el-dialog :title="editId? '编辑商品':'新增商品'" :visible.sync="dlgVisible" width="560px">
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="游戏名称"><el-input v-model="form.title"/></el-form-item>
        <el-form-item label="价格"><el-input v-model.number="form.price"/></el-form-item>
        <el-form-item label="店铺">
          <el-select v-model="form.shopId" filterable clearable placeholder="选择店铺" style="width:100%;" @visible-change="(v)=>{ if(v) loadMyShops() }">
            <el-option v-for="s in shopOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="游戏类型">
          <el-select v-model="form.category" filterable clearable placeholder="选择分类" style="width:100%;" @visible-change="(v)=>{ if(v) loadCategories() }">
            <el-option v-for="c in catOptions" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="avatar-uploader"
            action="/api/files/upload"
            name="file"
            :headers="uploadHeaders"
            :show-file-list="false"
            accept="image/*"
            :on-success="onUploadSuccess"
            :on-error="onUploadError">
            <img v-if="form.coverUrl" :src="form.coverUrl" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div style="color:#999;font-size:12px;margin-top:4px;">也可直接填入图片URL</div>
          <el-input v-model="form.coverUrl" placeholder="图片URL" style="margin-top:6px;"/>
        </el-form-item>
        <el-form-item label="描述"><el-input type="textarea" :rows="3" v-model="form.description"/></el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dlgVisible=false">取 消</el-button>
        <el-button type="primary" @click="submit">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { myProducts, createProduct, updateProduct, deleteProduct, productSales, myShops } from '@/api/merchant'
export default {
  name:'MerchantProducts',
  data(){return{ items:[], page1:1, size:10, total:0, dlgVisible:false, editId:null, selected:[], searchKeyword:'', salesMap:{},
    form:{ shopId:null, title:'', price:0, category:'', coverUrl:'', description:'' }, catOptions: [], shopOptions: [] }},
  computed:{
    uploadHeaders(){
      let uid = '2'
      try{ const su = localStorage.getItem('siteUser'); if(su){ const u=JSON.parse(su); if(u && u.id) uid=String(u.id) } }catch(e){}
      return { 'X-Role':'MERCHANT', 'X-User-Id': uid }
    },
    filteredItems(){
      if(!this.searchKeyword) return this.items
      const k = this.searchKeyword.trim().toLowerCase()
      return this.items.filter(x =>
        (x.title||'').toLowerCase().includes(k)
        || String(x.id||'').includes(k)
        || (x.category||'').toLowerCase().includes(k)
      )
    }
  },
  methods:{
    async fetch(){
      const { data } = await myProducts(this.page1-1, this.size)
      const payload = data && data.data
      if(payload){
        this.items = payload.items||[]; this.total = payload.total||0
        // 拉取销量
        const ids = this.items.map(x=>x.id)
        if(ids.length){
          const res = await productSales(ids)
          const sales = res && res.data && res.data.data && res.data.data.sales
          this.salesMap = sales || {}
        } else { this.salesMap = {} }
      }
    },
    onUploadSuccess(res){
      if(res && res.success){ this.form.coverUrl = (res.data && res.data.url) || ''; this.$message.success('上传成功') }
      else { this.$message.error((res && res.message) || '上传失败') }
    },
    onUploadError(){ this.$message.error('上传失败') },
    onSelect(rows){ this.selected = rows || [] },
    openCreate(){ this.editId=null; this.form={ shopId:null, title:'', price:0, category:'', coverUrl:'', description:'' }; this.dlgVisible=true },
    openEdit(row){ this.editId=row.id; this.form={ shopId: row.shopId, title:row.title, price:row.price, category:row.category, coverUrl:row.coverUrl, description:row.description }; this.dlgVisible=true },
    async loadCategories(){
      try{
        const { categories } = await import('@/api/site')
        const res = await categories(0, 100, true)
        const payload = res && res.data && res.data.data
        this.catOptions = (payload && payload.items) || []
      }catch(e){ this.catOptions = [] }
    },
    async loadMyShops(){
      try{
        const res = await myShops(0, 100)
        const payload = res && res.data && res.data.data
        this.shopOptions = (payload && payload.items) || []
      }catch(e){ this.shopOptions = [] }
    },
    async submit(){
      if(!this.form.title){ this.$message.error('请输入游戏名称'); return }
      if(!this.form.shopId && !this.editId){ this.$message.error('请选择店铺'); return }
      try{
        if(this.editId){ await updateProduct(this.editId, this.form) } else { await createProduct(this.form) }
        this.$message.success('已保存'); this.dlgVisible=false; this.fetch()
      }catch(e){
        const msg = (e && e.response && e.response.data && (e.response.data.message||e.response.data.msg)) || '保存失败'
        this.$message.error(msg)
      }
    },
    async remove(row){ await this.$confirm('确定删除该商品？','提示'); await deleteProduct(row.id); this.$message.success('已删除'); this.fetch() }
    ,async batchRemove(){
      if(!this.selected.length) return
      await this.$confirm(`确定删除选中的 ${this.selected.length} 个商品？`, '提示')
      for(const r of this.selected){ await deleteProduct(r.id) }
      this.$message.success('已批量删除')
      this.fetch()
    }
  },
  created(){ this.fetch() }
}
</script>
<style scoped>
.avatar-uploader .el-upload { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader .el-upload:hover { border-color: #409EFF; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; line-height: 100px; text-align: center; }
.avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
</style>
