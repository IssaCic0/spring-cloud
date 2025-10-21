<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content: space-between; align-items:center;">
        <div>
          <span style="font-weight:bold; font-size:16px;">用户管理</span>
        </div>
        <div>
          <el-input v-model="searchKeyword" placeholder="按用户名/邮箱过滤(前端)" size="small" style="width:220px;margin-right:8px;"/>
          <el-button type="success" size="small" @click="openCreate">新增用户</el-button>
          <el-button type="primary" size="small" @click="fetch()">刷新</el-button>
        </div>
      </div>
      <el-table :data="filteredItems" style="width:100%;margin-top:12px;" size="small" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="username" label="用户名" width="140"/>
        <el-table-column prop="nickname" label="昵称" width="140"/>
        <el-table-column prop="email" label="邮箱" width="180"/>
        <el-table-column prop="phone" label="手机号" width="140"/>
        <el-table-column prop="address" label="地址"/>
        <el-table-column label="角色" width="120">
          <template slot-scope="scope">{{ scope.row.role | fmtRole }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="460" fixed="right">
          <template slot-scope="scope">
            <el-button type="danger" size="mini" @click="ban(scope.row)">封禁</el-button>
            <el-dropdown @command="(cmd) => changeRole(scope.row, cmd)">
              <el-button type="primary" size="mini">
                改角色<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="BUYER">{{ 'BUYER' | fmtRole }}</el-dropdown-item>
                <el-dropdown-item command="MERCHANT">{{ 'MERCHANT' | fmtRole }}</el-dropdown-item>
                <el-dropdown-item command="ADMIN">{{ 'ADMIN' | fmtRole }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-button type="warning" size="mini" @click="openEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="mini" plain @click="remove(scope.row)">删除</el-button>
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

    <!-- 新增用户弹窗 -->
    <el-dialog title="新增用户" :visible.sync="createVisible" width="520px">
      <el-form :model="createForm" :rules="createRules" ref="createForm" label-width="90px" size="small">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password"/>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="createForm.nickname"/>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email"/>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone"/>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="createForm.address"/>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.role" placeholder="选择角色" style="width:100%">
            <el-option v-for="r in roleOptions" :key="r" :label="(r | fmtRole)" :value="r"/>
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="createForm.enabled"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createVisible=false">取 消</el-button>
        <el-button type="primary" @click="submitCreate">保 存</el-button>
      </span>
    </el-dialog>

    <!-- 编辑用户弹窗 -->
    <el-dialog title="编辑用户" :visible.sync="editVisible" width="520px">
      <el-form :model="editForm" ref="editFormRef" label-width="90px" size="small">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname"/>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email"/>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone"/>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editForm.address"/>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="选择角色" style="width:100%">
            <el-option v-for="r in roleOptions" :key="r" :label="(r | fmtRole)" :value="r"/>
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="editForm.enabled"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editVisible=false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { adminUsers, adminBanUser, adminChangeUserRole, adminCreateUser, adminUpdateUser, adminDeleteUser } from '@/api/admin'
export default {
  name: 'AdminUsers',
  data(){
    return{
      items:[],
      page1:1,
      size:10,
      total:0,
      searchKeyword:'',
      roleOptions:['BUYER','MERCHANT','ADMIN'],
      createVisible:false,
      editVisible:false,
      createForm:{ username:'', password:'', nickname:'', email:'', phone:'', address:'', role:'BUYER', enabled:true },
      editForm:{ id:null, nickname:'', email:'', phone:'', address:'', role:'BUYER', enabled:true },
      createRules:{
        username:[{ required:true, message:'请输入用户名', trigger:'blur' }],
        password:[{ required:true, message:'请输入密码', trigger:'blur' }]
      }
    }
  },
  computed:{
    filteredItems(){
      if(!this.searchKeyword) return this.items
      const k=this.searchKeyword.toLowerCase()
      return this.items.filter(x=> (x.username||'').toLowerCase().includes(k) || (x.email||'').toLowerCase().includes(k))
    }
  },
  methods:{
    async fetch(){
      const res = await adminUsers(this.page1-1, this.size)
      const payload = res && res.data && res.data.data
      if(payload){
        this.items = payload.items||[]
        this.total = payload.total||0
      }
    },
    openCreate(){
      this.createForm = { username:'', password:'', nickname:'', email:'', phone:'', address:'', role:'BUYER', enabled:true }
      this.createVisible = true
    },
    async submitCreate(){
      this.$refs.createForm.validate(async valid=>{
        if(!valid) return;
        await adminCreateUser(this.createForm)
        this.$message.success('创建成功')
        this.createVisible=false
        this.fetch()
      })
    },
    openEdit(row){
      this.editForm = { id:row.id, nickname:row.nickname, email:row.email, phone:row.phone, address:row.address, role:row.role, enabled:row.enabled }
      this.editVisible = true
    },
    async submitEdit(){
      const payload = { nickname:this.editForm.nickname, email:this.editForm.email, phone:this.editForm.phone, address:this.editForm.address, role:this.editForm.role, enabled:this.editForm.enabled }
      await adminUpdateUser(this.editForm.id, payload)
      this.$message.success('已保存')
      this.editVisible=false
      this.fetch()
    },
    async remove(row){
      await this.$confirm(`确定删除用户 [${row.username}]?`, '提示')
      await adminDeleteUser(row.id)
      this.$message.success('已删除')
      this.fetch()
    },
    async ban(row){
      await this.$confirm(`确定封禁用户 [${row.username}]?`, '提示')
      await adminBanUser(row.id, '违规')
      this.$message.success('已封禁')
      this.fetch()
    },
    async changeRole(row, role){
      await adminChangeUserRole(row.id, role)
      this.$message.success('已修改角色')
      this.fetch()
    }
  },
  created(){ this.fetch() }
}
</script>
