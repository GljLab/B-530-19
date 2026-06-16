<template>
  <div class="tenant-stats">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card primary">
          <div class="stat-icon">
            <el-icon :size="40"><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">租户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="40"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.inRentCount || 0 }}</div>
            <div class="stat-label">在租</div>
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
            <div class="stat-label">企业租户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card info">
          <div class="stat-icon">
            <el-icon :size="40"><House /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.retiredCount || 0 }}</div>
            <div class="stat-label">已退租</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>租户类型分布</span>
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
                  <span class="legend-dot" style="background: #409EFF;"></span>
                  <span class="legend-label">个人租户</span>
                  <span class="legend-value">{{ stats.personalCount || 0 }}人</span>
                  <span class="legend-percent">{{ personalPercent }}%</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot" style="background: #E6A23C;"></span>
                  <span class="legend-label">企业租户</span>
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
              <span>租户状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="status-list">
              <div v-for="(item, index) in statusList" :key="index" class="status-item">
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
              <span>黑名单统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="rate-stats">
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#F56C6C 0deg, #F56C6C ${blacklistRate * 3.6}deg, #ebeef5 ${blacklistRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ stats.blacklistCount || 0 }}</span>
                    <span class="rate-label">黑名单</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#67C23A 0deg, #67C23A ${inRentRate * 3.6}deg, #ebeef5 ${inRentRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ inRentRate }}%</span>
                    <span class="rate-label">在租率</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#909399 0deg, #909399 ${retiredRate * 3.6}deg, #ebeef5 ${retiredRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ retiredRate }}%</span>
                    <span class="rate-label">退租率</span>
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
              <span>近6个月新增租户趋势</span>
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
                <span class="legend-text">新增租户数</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>租赁类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart">
              <div v-for="(item, index) in leaseTypeDistribution" :key="index" class="bar-item">
                <span class="bar-label">{{ item.label }}</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="bar-value">{{ item.count }}人</span>
              </div>
            </div>
            <div class="avg-info">
              <span>平均租期：<strong>{{ avgLeaseMonths || 0 }}个月</strong></span>
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
            <div class="summary-label">租户总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value success">{{ stats.inRentCount || 0 }}</div>
            <div class="summary-label">在租租户</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value warning">{{ stats.newThisMonth || 0 }}</div>
            <div class="summary-label">本月新增</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value info">{{ stats.blacklistCount || 0 }}</div>
            <div class="summary-label">黑名单</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserFilled, User, OfficeBuilding, House } from '@element-plus/icons-vue'
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
    background: `conic-gradient(#409EFF 0deg, #409EFF ${personal * 3.6}deg, #E6A23C ${personal * 3.6}deg, #E6A23C 360deg)`
  }
})

const statusList = computed(() => {
  const total = stats.value.totalCount || 1
  return [
    { label: '在租', type: 'success', color: '#67C23A', count: stats.value.inRentCount || 0, percent: (stats.value.inRentCount || 0) * 100 / total },
    { label: '已退租', type: 'info', color: '#909399', count: stats.value.retiredCount || 0, percent: (stats.value.retiredCount || 0) * 100 / total },
    { label: '黑名单', type: 'danger', color: '#F56C6C', count: stats.value.blacklistCount || 0, percent: (stats.value.blacklistCount || 0) * 100 / total }
  ].map(item => ({ ...item, percent: item.percent.toFixed(1) }))
})

const blacklistRate = computed(() => {
  const total = stats.value.totalCount || 1
  return ((stats.value.blacklistCount || 0) * 100 / total).toFixed(1)
})

const inRentRate = computed(() => {
  const total = stats.value.totalCount || 1
  return ((stats.value.inRentCount || 0) * 100 / total).toFixed(1)
})

const retiredRate = computed(() => {
  const total = stats.value.totalCount || 1
  return ((stats.value.retiredCount || 0) * 100 / total).toFixed(1)
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

const leaseTypeDistribution = computed(() => {
  const dist = stats.value.leaseTypeDistribution || { '整租': 0, '合租': 0, '商铺': 0, '办公': 0, '仓库': 0 }
  const entries = Object.entries(dist)
  const max = Math.max(...Object.values(dist), 1)
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
  return entries.map(([label, count], index) => ({
    label,
    count,
    percent: (count * 100 / max).toFixed(0),
    color: colors[index % colors.length]
  }))
})

const avgLeaseMonths = computed(() => {
  return (stats.value.avgLeaseMonths || 0).toFixed(1)
})

onMounted(() => {
  loadStats()
})

function loadStats() {
  api.parkTenant.stats().then(res => {
    stats.value = res.data || {}
  })
}
</script>

<style scoped lang="scss">
.tenant-stats {
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
