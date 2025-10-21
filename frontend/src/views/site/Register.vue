<template>
  <div class="reg-wrap">
    <el-card class="reg-card">
      <div class="title">用户注册</div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" size="small">
        <el-form-item label="注册身份">
          <el-radio-group v-model="role">
            <el-radio-button label="BUYER">买家</el-radio-button>
            <el-radio-button label="MERCHANT">商家</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">注 册</el-button>
          <el-button @click="$router.push('/home')">返 回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script>
import { siteRegisterBuyer, siteRegisterMerchant } from '@/api/site'
export default {
  name:'SiteRegister',
  data(){
    return{
      role:'BUYER',
      form:{ username:'', password:'', email:'', phone:'' },
      rules:{
        username:[{ required:true, message:'请输入用户名', trigger:'blur' }],
        password:[{ required:true, message:'请输入密码', trigger:'blur' }]
      }
    }
  },
  methods:{
    submit(){
      this.$refs.formRef.validate(async valid=>{
        if(!valid) return
        try{
          const api = this.role==='MERCHANT'? siteRegisterMerchant : siteRegisterBuyer
          const { data } = await api(this.form)
          if(data && data.success){
            this.$message.success('注册成功，请登录')
            this.$router.push('/login')
          }else{
            this.$message.error((data && (data.message||data.msg)) || '注册失败')
          }
        }catch(e){
          const msg = (e && e.response && e.response.data && (e.response.data.message||e.response.data.msg)) || e.message || '服务异常'
          this.$message.error(msg)
        }
      })
    }
  }
}
</script>
<style scoped>
.reg-wrap{ display:flex; justify-content:center; align-items:flex-start; padding-top:60px; min-height:100vh; background:#f5f7fa; }
.reg-card{ width:520px; }
.title{ font-size:18px; font-weight:bold; margin-bottom:12px; }
</style>
