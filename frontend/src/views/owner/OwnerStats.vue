<template>
  <div class="owner-stats">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card primary">
          <div class="stat-icon">
            <el-icon :size="40"><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">业主总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="40"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.personalCount || 0 }}</div>
            <div class="stat-label">个人业主</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning">
          <div class="stat-icon">
            <el-icon :size="40"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.enterpriseCount || 0 }}</div>
            <div class="stat-label">企业业主</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card info">
          <div class="stat-icon">
            <el-icon :size="40"><House /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.propertyCount || 0 }}</div>
            <div class="stat-label">关联房产</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>业主类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="pie-chart">
              <div class="pie-circle" :style="pieStyle">
                <div class="pie-inner">
                  <span class="pie-total">{{ stats.totalCount || 0 }}</span>
                  <span class="pie-label">总计</span>
                </div>
              </div>
              <div class="pie-legend">
                <div class="legend-item">
                  <span class="legend-dot" style="background: #67C23A;"></span>
                  <span class="legend-label">个人业主</span>
                  <span class="legend-value">{{ stats.personalCount || 0 }}人</span>
                  <span class="legend-percent">{{ personalPercent }}%</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot" style="background: #E6A23C;"></span>
                  <span class="legend-label">企业业主</span>
                  <span class="legend-value">{{ stats.enterpriseCount || 0 }}家</span>
                  <span class="legend-percent">{{ enterprisePercent }}%</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>认证状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="status-list">
              <div v-for="(item, index) in auditStatusList" :key="index" class="status-item">
                <div class="status-info">
                  <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
                  <span class="status-count">{{ item.count }}人</span>
                </div>
                <div class="status-bar">
                  <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="status-percent">{{ item.percent }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>认证率统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="rate-stats">
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#67C23A 0deg, #67C23A ${auditedRate * 3.6}deg, #ebeef5 ${auditedRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ auditedRate }}%</span>
                    <span class="rate-label">已认证</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#E6A23C 0deg, #E6A23C ${pendingRate * 3.6}deg, #ebeef5 ${pendingRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ pendingRate }}%</span>
                    <span class="rate-label">待审核</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#909399 0deg, #909399 ${unauditedRate * 3.6}deg, #ebeef5 ${unauditedRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ unauditedRate }}%</span>
                    <span class="rate-label">未认证</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近6个月新增业主趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="line-chart">
              <div class="line-chart-grid">
                <div class="line-chart-y-axis">
                  <span v-for="(tick, index) in yAxisTicks" :key="index" class="y-tick">{{ tick }}</span>
                </div>
                <div class="line-chart-content">
                  <svg class="line-svg" viewBox="0 0 600 200" preserveAspectRatio="none">
                    <defs>
                      <linearGradient id="lineGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color:#409EFF;stop-opacity:0.3" />
                        <stop offset="100%" style="stop-color:#409EFF;stop-opacity:0" />
                      </linearGradient>
                    </defs>
                    <polyline
                      :points="linePoints"
                      fill="none"
                      stroke="#409EFF"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round" />
                    <polygon
                      :points="areaPoints"
                      fill="url(#lineGradient)" />
                    <circle
                      v-for="(point, index) in chartData"
                      :key="index"
                      :cx="50 + index * 100"
                      :cy="200 - (point.count / maxCount * 180)"
                      r="5"
                      fill="#fff"
                      stroke="#409EFF"
                      stroke-width="2" />
                  </svg>
                  <div class="line-chart-x-axis">
                    <span v-for="(point, index) in chartData" :key="index" class="x-tick">{{ point.month }}</span>
                  </div>
                </div>
              </div>
              <div class="line-chart-legend">
                <span class="legend-color"></span>
                <span class="legend-text">新增业主数</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>房产拥有量分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart">
              <div v-for="(item, index) in propertyDistribution" :key="index" class="bar-item">
                <span class="bar-label">{{ item.label }}</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="bar-value">{{ item.count }}人</span>
              </div>
            </div>
            <div class="avg-info">
              <span>平均每户拥有：<strong>{{ avgPropertyCount || 0 }}套</strong></span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>业主年龄分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart horizontal">
              <div v-for="(item, index) in ageDistribution" :key="index" class="bar-item">
                <span class="bar-label">{{ item.label }}</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: item.percent + '%', background: '#67C23A' }"></div>
                </div>
                <span class="bar-value">{{ item.count }}人</span>
              </div>
            </div>
            <div class="avg-info">
              <span>平均年龄：<strong>{{ avgAge || 0 }}岁</strong></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>性别分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="gender-chart">
              <div class="gender-item">
                <div class="gender-icon male">
                  <el-icon :size="48"><Male /></el-icon>
                </div>
                <div class="gender-info">
                  <div class="gender-count">{{ stats.maleCount || 0 }}</div>
                  <div class="gender-label">男性业主</div>
                  <div class="gender-percent">
                    <el-progress :percentage="malePercent" :stroke-width="12" color="#409EFF" />
                  </div>
                </div>
              </div>
              <div class="gender-item">
                <div class="gender-icon female">
                  <el-icon :size="48"><Female /></el-icon>
                </div>
                <div class="gender-info">
                  <div class="gender-count">{{ stats.femaleCount || 0 }}</div>
                  <div class="gender-label">女性业主</div>
                  <div class="gender-percent">
                    <el-progress :percentage="femalePercent" :stroke-width="12" color="#F56C6C" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>详细数据汇总</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value primary">{{ stats.totalCount || 0 }}</div>
            <div class="summary-label">业主总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value success">{{ stats.auditedCount || 0 }}</div>
            <div class="summary-label">已认证业主</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value warning">{{ stats.propertyCount || 0 }}</div>
            <div class="summary-label">关联房产总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value info">{{ stats.newThisMonth || 0 }}</div>
            <div class="summary-label">本月新增</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserFilled, User, OfficeBuilding, House, Male, Female } from '@element-plus/icons-vue'
import api from '@/api'

const stats = ref({})

const personalPercent = computed(() => {
  const total = stats.value.totalCount || 1
  const count = stats.value.personalCount || 0
  return (count * 100 / total).toFixed(1)
})

const enterprisePercent = computed(() => {
  const total = stats.value.totalCount || 1
  const count = stats.value.enterpriseCount || 0
  return (count * 100 / total).toFixed(1)
})

const pieStyle = computed(() => {
  const personal = parseFloat(personalPercent.value)
  return {
    background: `conic-gradient(#67C23A 0deg, #67C23A ${personal * 3.6}deg, #E6A23C ${personal * 3.6}deg, #E6A23C 360deg)`
  }
})

const auditStatusList = computed(() => {
  const total = stats.value.totalCount || 1
  return [
    { label: '已通过', type: 'success', color: '#67C23A', count: stats.value.auditedCount || 0, percent: (stats.value.auditedCount || 0) * 100 / total },
    { label: '待审核', type: 'warning', color: '#E6A23C', count: stats.value.pendingAuditCount || 0, percent: (stats.value.pendingAuditCount || 0) * 100 / total },
    { label: '未认证', type: 'info', color: '#909399', count: stats.value.unauditedCount || 0, percent: (stats.value.unauditedCount || 0) * 100 / total },
    { label: '已拒绝', type: 'danger', color: '#F56C6C', count: stats.value.rejectedCount || 0, percent: (stats.value.rejectedCount || 0) * 100 / total }
  ].map(item => ({ ...item, percent: item.percent.toFixed(1) }))
})

const auditedRate = computed(() => {
  const total = stats.value.totalCount || 1
  return ((stats.value.auditedCount || 0) * 100 / total).toFixed(1)
})

const pendingRate = computed(() => {
  const total = stats.value.totalCount || 1
  return ((stats.value.pendingAuditCount || 0) * 100 / total).toFixed(1)
})

const unauditedRate = computed(() => {
  const total = stats.value.totalCount || 1
  return ((stats.value.unauditedCount || 0) * 100 / total).toFixed(1)
})

const chartData = computed(() => {
  return stats.value.monthlyTrend || [
    { month: '1月', count: 0 },
    { month: '2月', count: 0 },
    { month: '3月', count: 0 },
    { month: '4月', count: 0 },
    { month: '5月', count: 0 },
    { month: '6月', count: 0 }
  ]
})

const maxCount = computed(() => {
  const counts = chartData.value.map(d => d.count)
  return Math.max(...counts, 1)
})

const yAxisTicks = computed(() => {
  const max = maxCount.value
  const step = Math.ceil(max / 4)
  return [max, step * 3, step * 2, step, 0]
})

const linePoints = computed(() => {
  return chartData.value.map((d, i) => `${50 + i * 100},${200 - (d.count / maxCount.value * 180)}`).join(' ')
})

const areaPoints = computed(() => {
  const points = chartData.value.map((d, i) => `${50 + i * 100},${200 - (d.count / maxCount.value * 180)}`)
  return `${50},200 ${points.join(' ')} ${50 + (chartData.value.length - 1) * 100},200`
})

const propertyDistribution = computed(() => {
  const dist = stats.value.propertyDistribution || { '1套': 0, '2套': 0, '3套': 0, '4套及以上': 0 }
  const entries = Object.entries(dist)
  const max = Math.max(...Object.values(dist), 1)
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
  return entries.map(([label, count], index) => ({
    label,
    count,
    percent: (count * 100 / max).toFixed(0),
    color: colors[index % colors.length]
  }))
})

const avgPropertyCount = computed(() => {
  return (stats.value.avgPropertyCount || 0).toFixed(1)
})

const ageDistribution = computed(() => {
  const dist = stats.value.ageDistribution || { '20-30岁': 0, '31-40岁': 0, '41-50岁': 0, '51-60岁': 0, '60岁以上': 0 }
  const entries = Object.entries(dist)
  const max = Math.max(...Object.values(dist), 1)
  return entries.map(([label, count]) => ({
    label,
    count,
    percent: (count * 100 / max).toFixed(0)
  }))
})

const avgAge = computed(() => {
  return (stats.value.avgAge || 0).toFixed(1)
})

const malePercent = computed(() => {
  const total = (stats.value.maleCount || 0) + (stats.value.femaleCount || 0) || 1
  return ((stats.value.maleCount || 0) * 100 / total).toFixed(0)
})

const femalePercent = computed(() => {
  const total = (stats.value.maleCount || 0) + (stats.value.femaleCount || 0) || 1
  return ((stats.value.femaleCount || 0) * 100 / total).toFixed(0)
})

onMounted(() => {
  loadStats()
})

function loadStats() {
  api.parkOwner.stats().then(res => {
    stats.value = res.data || {}
  })
}
</script>

<style scoped lang="scss">
.owner-stats {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  
  &.primary .stat-icon { color: #409EFF; }
  &.success .stat-icon { color: #67C23A; }
  &.warning .stat-icon { color: #E6A23C; }
  &.info .stat-icon { color: #909399; }
  
  .stat-icon {
    padding: 15px;
    border-radius: 10px;
    background: #f5f7fa;
  }
  
  .stat-content {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }
    
    .stat-label {
      font-size: 14px;
      color: #606266;
      margin-top: 5px;
    }
  }
}

.chart-card {
  .card-header {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
  }
  
  .chart-container {
    min-height: 250px;
  }
}

.pie-chart {
  display: flex;
  align-items: center;
  gap: 30px;
  
  .pie-circle {
    width: 160px;
    height: 160px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .pie-inner {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .pie-total {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .pie-label {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .pie-legend {
    flex: 1;
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 15px;
      
      .legend-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
      }
      
      .legend-label {
        width: 70px;
        font-size: 14px;
        color: #606266;
      }
      
      .legend-value {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }
      
      .legend-percent {
        margin-left: auto;
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

.status-list {
  .status-item {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 18px;
    
    .status-info {
      width: 110px;
      display: flex;
      flex-direction: column;
      gap: 5px;
      
      .status-count {
        font-size: 12px;
        color: #606266;
      }
    }
    
    .status-bar {
      flex: 1;
      height: 20px;
      background: #ebeef5;
      border-radius: 10px;
      overflow: hidden;
      
      .bar-fill {
        height: 100%;
        border-radius: 10px;
        transition: width 0.3s;
      }
    }
    
    .status-percent {
      width: 60px;
      text-align: right;
      font-size: 13px;
      color: #606266;
    }
  }
}

.rate-stats {
  display: flex;
  justify-content: space-around;
  
  .rate-item {
    text-align: center;
  }
  
  .rate-circle {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 10px;
    
    .rate-inner {
      width: 75px;
      height: 75px;
      border-radius: 50%;
      background: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .rate-value {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
      }
      
      .rate-label {
        font-size: 12px;
        color: #606266;
      }
    }
  }
}

.line-chart {
  .line-chart-grid {
    display: flex;
    gap: 10px;
    
    .line-chart-y-axis {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: 200px;
      padding-bottom: 25px;
      
      .y-tick {
        font-size: 12px;
        color: #909399;
        text-align: right;
        width: 40px;
      }
    }
    
    .line-chart-content {
      flex: 1;
      position: relative;
      
      .line-svg {
        width: 100%;
        height: 200px;
      }
      
      .line-chart-x-axis {
        display: flex;
        justify-content: space-around;
        margin-top: 5px;
        
        .x-tick {
          font-size: 12px;
          color: #909399;
          flex: 1;
          text-align: center;
        }
      }
    }
  }
  
  .line-chart-legend {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-top: 10px;
    
    .legend-color {
      width: 16px;
      height: 3px;
      background: #409EFF;
      border-radius: 2px;
    }
    
    .legend-text {
      font-size: 12px;
      color: #606266;
    }
  }
}

.bar-chart {
  .bar-item {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
    
    .bar-label {
      width: 100px;
      font-size: 13px;
      color: #606266;
    }
    
    .bar-wrap {
      flex: 1;
      height: 24px;
      background: #ebeef5;
      border-radius: 4px;
      overflow: hidden;
      
      .bar-fill {
        height: 100%;
        background: #409EFF;
        border-radius: 4px;
        transition: width 0.3s;
      }
    }
    
    .bar-value {
      width: 70px;
      text-align: right;
      font-size: 13px;
      color: #303133;
      font-weight: 500;
    }
  }
}

.avg-info {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  font-size: 14px;
  color: #606266;
  
  strong {
    color: #409EFF;
    font-size: 18px;
  }
}

.gender-chart {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
  
  .gender-item {
    text-align: center;
    
    .gender-icon {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 10px;
      
      &.male {
        background: #ecf5ff;
        color: #409EFF;
      }
      
      &.female {
        background: #fef0f0;
        color: #F56C6C;
      }
    }
    
    .gender-info {
      .gender-count {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .gender-label {
        font-size: 14px;
        color: #606266;
        margin: 5px 0;
      }
      
      .gender-percent {
        width: 120px;
      }
    }
  }
}

.summary-item {
  text-align: center;
  padding: 15px;
  
  .summary-value {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 8px;
    
    &.primary { color: #409EFF; }
    &.success { color: #67C23A; }
    &.warning { color: #E6A23C; }
    &.info { color: #909399; }
  }
  
  .summary-label {
    font-size: 14px;
    color: #606266;
  }
}
</style>
