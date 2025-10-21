/**
* 图表界面
*/ 
<template>
  <!-- 组件主盒子 -->
  <div class="stbox">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>数据可视化</el-breadcrumb-item>
    </el-breadcrumb>
    <!-- 仅保留：生成混合图（销量+库存） -->

    <el-row :gutter="23" style="margin-top: 12px;">
      <el-col :span="24">
        <div class="stbgc">
          <el-form :inline="true" size="small" class="py-form">
            <el-form-item label="指标">
              <el-select v-model="metric" placeholder="请选择指标" style="width: 140px;">
                <el-option label="销量" value="sales" />
                <el-option label="库存" value="inventory" />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" :loading="loading.both" @click="generateBoth">生成图片</el-button>
            </el-form-item>
          </el-form>
          <div v-if="pieImgSrc" class="img-wrap" style="margin-bottom:10px;">
            <img :src="pieImgSrc" alt="饼图" class="chart-img" />
          </div>
          <div v-if="gridImgSrc" class="img-wrap">
            <img :src="gridImgSrc" alt="混合图" class="chart-img" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script type="text/ecmascript-6">
import axios from 'axios'
export default {
  name: 'statistics',
  data() {
    return {
      
      metric: 'sales',
      // 结果图片
      pieImgSrc: '',
      gridImgSrc: '',
      loading: { both: false }
    }
  },
  mounted() {},
  methods: {
    async generateBoth() {
      try {
        this.loading.both = true
        const params = {
          dimension: 'product',
          topN: 10,
          pieMetric: this.metric,
        }
        // 不再按商品名称筛选，后端将按整体统计生成
        const { data } = await axios.get('/api/py/generateAll', { params })
        if (data && data.success && data.data) {
          const { piePath, gridPath } = data.data
          this.pieImgSrc = piePath ? `/api/py/showImages?path=${encodeURIComponent(piePath)}` : ''
          this.gridImgSrc = gridPath ? `/api/py/showImages?path=${encodeURIComponent(gridPath)}` : ''
        } else {
          const msg = (data && data.message) ? data.message : '生成失败'
          this.$message && this.$message.error ? this.$message.error(msg) : alert(msg)
        }
      } catch (e) {
        console.error(e)
        this.$message && this.$message.error ? this.$message.error('请求失败') : alert('请求失败')
      } finally {
        this.loading.both = false
      }
    }
  }
}
</script>
<style>
.stbox {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}
.stbgc {
  background-color: #fff;
  height: 60px;
  line-height: 60px;
  border-radius: 5px;
  padding: 0px 16px;
}
.stsearch {
  text-align: center;
}
.text-c {
  text-align: center;
}
.st-gbox {
  background-color: #fff;
  margin-top: 20px;
  border-radius: 5px;
  height: 30vh;
  box-sizing: border-box;
  padding: 10px;
}
.cavasbox {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
}
.paybox {
  width: 100%;
  background-color: #fff;
  box-sizing: border-box;
  border-radius: 5px;
  margin-top: 20px;
  height: 32vh;
}
.py-form {
  padding: 8px 0;
}
.img-wrap {
  margin-top: 10px;
  border: 1px dashed #e5e5e5;
  padding: 8px;
  border-radius: 6px;
}
.chart-img {
  display: block;
  max-width: 100%;
  width: 640px; /* 限制显示宽度，避免过大 */
}
</style>