<template>
  <div class="parking-stats">
    <el-row :gutter="20">
      <el-col :span="4">
        <el-card class="stat-card primary">
          <div class="stat-icon">
            <el-icon :size="36"><Van /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">车位总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="36"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.freeCount || 0 }}</div>
            <div class="stat-label">空闲车位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card warning">
          <div class="stat-icon">
            <el-icon :size="36"><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.rentedCount || 0 }}</div>
            <div class="stat-label">已出租车位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card info">
          <div class="stat-icon">
            <el-icon :size="36"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ usageRate }}%</div>
            <div class="stat-label">使用率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" style="background: linear-gradient(135deg, #909399 0%, #606266 100%);">
          <div class="stat-icon">
            <el-icon :size="36"><Box /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ vacancyRate }}%</div>
            <div class="stat-label">空置率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" style="background: linear-gradient(135deg, #F56C6C 0%, #E6A23C 100%);">
          <div class="stat-icon">
            <el-icon :size="36"><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.estimatedMonthlyIncome || 0 }}</div>
            <div class="stat-label">月租金预估(元)</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>车位类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="pie-chart-placeholder">
              <div class="chart-total">
                <span class="total-num">{{ stats.totalCount || 0 }}</span>
                <span class="total-label">总计</span>
              </div>
              <div class="pie-legend">
                <div v-for="(item, idx) in typeLegendList" :key="idx" class="legend-item">
                  <span class="dot" :style="{ background: item.color }"></span>
                  <span class="label">{{ item.label }}</span>
                  <span class="value">{{ item.count }}个</span>
                  <span class="percent">{{ item.percent }}%</span>
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
              <span>使用状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="status-list">
              <div v-for="(item, key) in statusList" :key="key" class="status-item">
                <div class="status-info">
                  <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
                  <span class="count">{{ item.count }}个</span>
                </div>
                <div class="status-bar">
                  <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="percent-text">{{ item.percent }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>使用率统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="rate-stats">
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#67C23A 0deg, #67C23A ${usageRate * 3.6}deg, #ebeef5 ${usageRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ usageRate }}%</span>
                    <span class="rate-label">使用率</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#909399 0deg, #909399 ${vacancyRate * 3.6}deg, #ebeef5 ${vacancyRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ vacancyRate }}%</span>
                    <span class="rate-label">空置率</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" :style="{ background: `conic-gradient(#409EFF 0deg, #409EFF ${rentalRate * 3.6}deg, #ebeef5 ${rentalRate * 3.6}deg)` }">
                  <div class="rate-inner">
                    <span class="rate-value">{{ rentalRate }}%</span>
                    <span class="rate-label">出租率</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>产权分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="pie-chart-placeholder">
              <div class="chart-total">
                <span class="total-num">{{ stats.totalCount || 0 }}</span>
                <span class="total-label">总计</span>
              </div>
              <div class="pie-legend">
                <div v-for="(item, idx) in propertyRightLegendList" :key="idx" class="legend-item">
                  <span class="dot" :style="{ background: item.color }"></span>
                  <span class="label">{{ item.label }}</span>
                  <span class="value">{{ item.count }}个</span>
                  <span class="percent">{{ item.percent }}%</span>
                </div>
              </div>
            </div>
            <div style="margin-top: 12px; text-align: center;">
              <span>产权销售率：<strong style="color: #409EFF;">{{ propertySalesRate }}%</strong></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>月租金分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart">
              <div v-for="(item, idx) in rentDistributionList" :key="idx" class="bar-item">
                <span class="bar-label">{{ item.label }}</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: item.percent + '%', background: '#E6A23C' }"></div>
                </div>
                <span class="bar-value">{{ item.count }}个</span>
              </div>
            </div>
            <div class="avg-area">
              <span>平均月租金：<strong>{{ stats.avgMonthlyRent || 0 }}元</strong></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>平均租金与收益</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="simple-stats">
              <div class="simple-stat-item">
                <span class="simple-label">平均月租金</span>
                <span class="simple-value primary">{{ stats.avgMonthlyRent || 0 }} 元</span>
              </div>
              <div class="simple-stat-item">
                <span class="simple-label">预估月收入</span>
                <span class="simple-value success">{{ stats.estimatedMonthlyIncome || 0 }} 元</span>
              </div>
              <div class="simple-stat-item">
                <span class="simple-label">产权销售率</span>
                <span class="simple-value warning">{{ propertySalesRate }}%</span>
              </div>
              <div class="simple-stat-item">
                <span class="simple-label">充电车位数</span>
                <span class="simple-value info">{{ chargingCount }} 个</span>
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
              <span>各区域车位总数</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart">
              <div v-for="item in areaStatsList" :key="item.area" class="bar-item">
                <span class="bar-label">{{ item.area }}</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: areaTotalPercent(item.totalCount) + '%', background: '#409EFF' }"></div>
                </div>
                <span class="bar-value">{{ item.totalCount }}个</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各区域使用率与平均租金</span>
            </div>
          </template>
          <div class="chart-container">
            <el-table :data="areaStatsList" border size="small">
              <el-table-column prop="area" label="区域" width="140" />
              <el-table-column prop="totalCount" label="车位总数" width="100" />
              <el-table-column prop="usedCount" label="已使用" width="80" />
              <el-table-column label="使用率" width="120">
                <template #default="{ row }">
                  <el-progress :percentage="Number(row.usageRate.toFixed(1))" :color="getUsageColor(row.usageRate)" :stroke-width="14" />
                </template>
              </el-table-column>
              <el-table-column label="平均月租金(元)" width="120">
                <template #default="{ row }">
                  {{ row.avgRent || 0 }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api'
import { Van, CircleCheck, Money, TrendCharts, Box, Wallet } from '@element-plus/icons-vue'

const stats = ref({})

const usageRate = computed(() => Number((stats.value.usageRate || 0).toFixed(1)))
const vacancyRate = computed(() => Number((stats.value.vacancyRate || 0).toFixed(1)))
const rentalRate = computed(() => Number((stats.value.rentalRate || 0).toFixed(1)))
const propertySalesRate = computed(() => Number((stats.value.propertySalesRate || 0).toFixed(1)))

const statusOptions = [
  { label: '空闲', value: 1, type: 'success', color: '#67C23A' },
  { label: '业主自用', value: 2, type: 'primary', color: '#409EFF' },
  { label: '已出租', value: 3, type: 'warning', color: '#E6A23C' },
  { label: '临时占用', value: 4, type: '', color: '#F56C6C' },
  { label: '维护中', value: 5, type: 'info', color: '#909399' },
  { label: '禁用', value: 6, type: 'danger', color: '#F56C6C' }
]

const statusList = computed(() => {
  const total = stats.value.totalCount || 0
  const dist = stats.value.statusDistribution || {}
  return statusOptions.map(s => {
    const count = dist[s.label] || 0
    return {
      ...s,
      count,
      percent: total > 0 ? Number((count * 100 / total).toFixed(1)) : 0
    }
  })
})

const typeColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
const typeLegendList = computed(() => {
  const total = stats.value.totalCount || 0
  const dist = stats.value.typeDistribution || {}
  const types = ['标准车位', '大型车位', '微型车位', '充电车位', '无障碍车位']
  return types.map((t, idx) => {
    const count = dist[t] || 0
    return {
      label: t,
      count,
      color: typeColors[idx],
      percent: total > 0 ? Number((count * 100 / total).toFixed(1)) : 0
    }
  })
})

const propertyRightColors = ['#409EFF', '#67C23A', '#E6A23C']
const propertyRightLegendList = computed(() => {
  const total = stats.value.totalCount || 0
  const dist = stats.value.propertyRightDistribution || {}
  const rights = ['开发商', '业主', '公共']
  return rights.map((r, idx) => {
    const count = dist[r] || 0
    return {
      label: r,
      count,
      color: propertyRightColors[idx],
      percent: total > 0 ? Number((count * 100 / total).toFixed(1)) : 0
    }
  })
})

const chargingCount = computed(() => {
  return (stats.value.typeDistribution && stats.value.typeDistribution['充电车位']) || 0
})

const maxAreaTotal = computed(() => {
  const list = stats.value.areaStats || []
  let max = 0
  list.forEach(item => { if (item.totalCount > max) max = item.totalCount })
  return max || 1
})

const areaStatsList = computed(() => {
  return (stats.value.areaStats || []).filter(item => item.totalCount > 0)
})

const areaTotalPercent = (count) => {
  return Number((count * 100 / maxAreaTotal.value).toFixed(1))
}

const maxRentCount = computed(() => {
  let max = 0
  const dist = stats.value.rentDistribution || {}
  Object.values(dist).forEach(v => { if (v > max) max = v })
  return max || 1
})

const rentDistributionList = computed(() => {
  const dist = stats.value.rentDistribution || {}
  const ranges = ['300以下', '300-500', '500-800', '800以上']
  return ranges.map(r => {
    const count = dist[r] || 0
    return {
      label: r + '元',
      count,
      percent: Number((count * 100 / maxRentCount.value).toFixed(1))
    }
  })
})

const getUsageColor = (rate) => {
  if (rate >= 80) return '#67C23A'
  if (rate >= 50) return '#409EFF'
  if (rate >= 30) return '#E6A23C'
  return '#F56C6C'
}

const loadStats = async () => {
  try {
    const res = await api.parkParking.stats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {}
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.parking-stats {
  .stat-card {
    color: #fff;
    background: linear-gradient(135deg, #409EFF 0%, #2e7fe0 100%);
    border: none;
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
    }
    &.primary { background: linear-gradient(135deg, #409EFF 0%, #2e7fe0 100%); }
    &.success { background: linear-gradient(135deg, #67C23A 0%, #4fae25 100%); }
    &.warning { background: linear-gradient(135deg, #E6A23C 0%, #cf8e2e 100%); }
    &.info { background: linear-gradient(135deg, #909399 0%, #606266 100%); }
    .stat-icon {
      opacity: 0.9;
    }
    .stat-content {
      .stat-value {
        font-size: 26px;
        font-weight: bold;
        line-height: 1.2;
      }
      .stat-label {
        font-size: 13px;
        opacity: 0.9;
        margin-top: 2px;
      }
    }
  }
  .chart-card {
    .card-header {
      font-size: 15px;
      font-weight: 600;
    }
    .chart-container {
      min-height: 260px;
    }
  }
  .pie-chart-placeholder {
    position: relative;
    .chart-total {
      text-align: center;
      margin-bottom: 16px;
      .total-num {
        display: block;
        font-size: 32px;
        font-weight: bold;
        color: #303133;
      }
      .total-label {
        color: #909399;
        font-size: 13px;
      }
    }
    .pie-legend {
      .legend-item {
        display: flex;
        align-items: center;
        padding: 6px 0;
        font-size: 13px;
        .dot {
          width: 10px;
          height: 10px;
          border-radius: 50%;
          margin-right: 8px;
        }
        .label {
          width: 70px;
          color: #606266;
        }
        .value {
          width: 50px;
          color: #303133;
          font-weight: 500;
        }
        .percent {
          margin-left: auto;
          color: #909399;
        }
      }
    }
  }
  .status-list {
    .status-item {
      display: flex;
      align-items: center;
      padding: 6px 0;
      gap: 10px;
      .status-info {
        display: flex;
        align-items: center;
        gap: 8px;
        width: 120px;
        .count {
          font-size: 13px;
          color: #606266;
        }
      }
      .status-bar {
        flex: 1;
        height: 12px;
        background: #f0f2f5;
        border-radius: 6px;
        overflow: hidden;
        .bar-fill {
          height: 100%;
          border-radius: 6px;
          transition: width 0.3s;
        }
      }
      .percent-text {
        width: 50px;
        text-align: right;
        font-size: 12px;
        color: #909399;
      }
    }
  }
  .rate-stats {
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 20px 0;
    .rate-item {
      text-align: center;
      .rate-circle {
        width: 90px;
        height: 90px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 8px;
        .rate-inner {
          width: 68px;
          height: 68px;
          background: #fff;
          border-radius: 50%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          .rate-value {
            font-size: 16px;
            font-weight: bold;
            color: #303133;
          }
          .rate-label {
            font-size: 11px;
            color: #909399;
          }
        }
      }
    }
  }
  .bar-chart {
    .bar-item {
      display: flex;
      align-items: center;
      padding: 7px 0;
      gap: 10px;
      .bar-label {
        width: 100px;
        font-size: 13px;
        color: #606266;
      }
      .bar-wrap {
        flex: 1;
        height: 20px;
        background: #f0f2f5;
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
        width: 60px;
        text-align: right;
        font-size: 12px;
        color: #303133;
        font-weight: 500;
      }
    }
  }
  .avg-area {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
    text-align: center;
    color: #606266;
    font-size: 13px;
  }
  .simple-stats {
    padding: 10px 0;
    .simple-stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      margin-bottom: 8px;
      background: #f8f9fa;
      border-radius: 6px;
      .simple-label {
        color: #606266;
        font-size: 13px;
      }
      .simple-value {
        font-weight: 600;
        font-size: 15px;
        &.primary { color: #409EFF; }
        &.success { color: #67C23A; }
        &.warning { color: #E6A23C; }
        &.info { color: #909399; }
      }
    }
  }
}
</style>
