<template>
  <div class="property-stats">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="综合统计" name="general">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card primary">
          <div class="stat-icon">
            <el-icon :size="40"><House /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">房产总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="40"><HomeFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.typeDistribution?.residential || 0 }}</div>
            <div class="stat-label">住宅</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning">
          <div class="stat-icon">
            <el-icon :size="40"><Shop /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.typeDistribution?.commercial || 0 }}</div>
            <div class="stat-label">商业</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card info">
          <div class="stat-icon">
            <el-icon :size="40"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ occupancyRate }}%</div>
            <div class="stat-label">入住率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>房产类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="pie-chart-placeholder">
              <div class="chart-total">
                <span class="total-num">{{ stats.totalCount || 0 }}</span>
                <span class="total-label">总计</span>
              </div>
              <div class="pie-legend">
                <div class="legend-item">
                  <span class="dot" style="background: #67C23A;"></span>
                  <span class="label">住宅</span>
                  <span class="value">{{ stats.typeDistribution?.residential || 0 }}套</span>
                  <span class="percent">{{ typePercent('residential') }}%</span>
                </div>
                <div class="legend-item">
                  <span class="dot" style="background: #E6A23C;"></span>
                  <span class="label">商业</span>
                  <span class="value">{{ stats.typeDistribution?.commercial || 0 }}套</span>
                  <span class="percent">{{ typePercent('commercial') }}%</span>
                </div>
                <div class="legend-item">
                  <span class="dot" style="background: #909399;"></span>
                  <span class="label">其他</span>
                  <span class="value">{{ stats.typeDistribution?.other || 0 }}套</span>
                  <span class="percent">{{ typePercent('other') }}%</span>
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
              <span>房产状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="status-list">
              <div v-for="(item, key) in statusList" :key="key" class="status-item">
                <div class="status-info">
                  <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
                  <span class="count">{{ item.count }}套</span>
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
              <span>入住率统计</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="rate-stats">
              <div class="rate-item">
                <div class="rate-circle" style="background: conic-gradient(#67C23A 0deg, #67C23A {{ occupancyRate * 3.6 }}deg, #ebeef5 {{ occupancyRate * 3.6 }}deg);">
                  <div class="rate-inner">
                    <span class="rate-value">{{ occupancyRate }}%</span>
                    <span class="rate-label">入住率</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" style="background: conic-gradient(#909399 0deg, #909399 {{ vacancyRate * 3.6 }}deg, #ebeef5 {{ vacancyRate * 3.6 }}deg);">
                  <div class="rate-inner">
                    <span class="rate-value">{{ vacancyRate }}%</span>
                    <span class="rate-label">空置率</span>
                  </div>
                </div>
              </div>
              <div class="rate-item">
                <div class="rate-circle" style="background: conic-gradient(#409EFF 0deg, #409EFF {{ rentalRate * 3.6 }}deg, #ebeef5 {{ rentalRate * 3.6 }}deg);">
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
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>户型分布（住宅）</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart">
              <div v-for="(count, type) in stats.houseTypeDistribution || {}" :key="type" class="bar-item">
                <span class="bar-label">{{ type }}</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: houseTypePercent(type) + '%' }"></div>
                </div>
                <span class="bar-value">{{ count }}套</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>面积分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="bar-chart">
              <div v-for="(count, range) in stats.areaDistribution || {}" :key="range" class="bar-item">
                <span class="bar-label">{{ range }}㎡</span>
                <div class="bar-wrap">
                  <div class="bar-fill" :style="{ width: areaPercent(count) + '%', background: '#E6A23C' }"></div>
                </div>
                <span class="bar-value">{{ count }}套</span>
              </div>
            </div>
            <div class="avg-area">
              <span>平均面积：<strong>{{ stats.avgArea || 0 }}㎡</strong></span>
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
              <span>楼宇房产分布</span>
            </div>
          </template>
          <div class="chart-container">
            <el-table :data="stats.buildingStats || []" size="small">
              <el-table-column prop="buildingName" label="楼宇名称" />
              <el-table-column prop="totalCount" label="房产数量" width="120" align="center" />
              <el-table-column label="入住率" width="180">
                <template #default="{ row }">
                  <div class="building-occupancy">
                    <el-progress :percentage="row.occupancyRate.toFixed(0)" :stroke-width="8" />
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>朝向分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="orientation-grid">
              <div v-for="(count, orientation) in stats.orientationDistribution || {}" :key="orientation" class="orientation-item">
                <div class="orientation-value">{{ count }}</div>
                <div class="orientation-label">{{ orientation }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>面积和产权统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value primary">{{ stats.totalBuildingArea || 0 }}</div>
            <div class="summary-label">总建筑面积(㎡)</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value success">{{ stats.totalInsideArea || 0 }}</div>
            <div class="summary-label">总套内面积(㎡)</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value warning">{{ stats.avgArea || 0 }}</div>
            <div class="summary-label">平均面积(㎡)</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-value info">{{ stats.typeDistribution?.residential || 0 }}</div>
            <div class="summary-label">住宅套数</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
      </el-tab-pane>
      <el-tab-pane label="空置分析" name="vacancy">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="stat-card primary">
              <div class="stat-content">
                <div class="stat-value">{{ vacancyStats.vacantTotal || 0 }}</div>
                <div class="stat-label">空置总数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card warning">
              <div class="stat-content">
                <div class="stat-value">{{ vacancyStats.vacancyRate ? vacancyStats.vacancyRate.toFixed(1) : 0 }}%</div>
                <div class="stat-label">空置率</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card danger">
              <div class="stat-content">
                <div class="stat-value">{{ vacancyStats.longTermVacantCount || 0 }}</div>
                <div class="stat-label">长期空置数</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header"><span>楼宇空置统计</span></div>
              </template>
              <el-table :data="vacancyStats.buildingVacancyStats || []" size="small">
                <el-table-column prop="buildingName" label="楼宇名称" />
                <el-table-column prop="totalPropertyCount" label="房产总数" width="100" align="center" />
                <el-table-column prop="vacantCount" label="空置数" width="80" align="center" />
                <el-table-column label="空置率" width="180">
                  <template #default="{ row }">
                    <el-progress :percentage="parseFloat((row.vacancyRate || 0).toFixed(1))" :stroke-width="8" />
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header"><span>户型空置统计</span></div>
              </template>
              <div class="bar-chart">
                <div v-for="item in vacancyStats.houseTypeVacancyStats || []" :key="item.houseType" class="bar-item">
                  <span class="bar-label">{{ item.houseType }}</span>
                  <div class="bar-wrap">
                    <div class="bar-fill" :style="{ width: houseVacancyPercent(item.vacantCount) + '%', background: '#F56C6C' }"></div>
                  </div>
                  <span class="bar-value">{{ item.vacantCount }}套</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header"><span>空置时长分布</span></div>
              </template>
              <div class="bar-chart">
                <div v-for="(count, label) in vacancyStats.vacancyDurationDistribution || {}" :key="label" class="bar-item">
                  <span class="bar-label">{{ label }}</span>
                  <div class="bar-wrap">
                    <div class="bar-fill" :style="{ width: durationPercent(count) + '%', background: '#E6A23C' }"></div>
                  </div>
                  <span class="bar-value">{{ count }}套</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header"><span>空置原因统计</span></div>
              </template>
              <div class="pie-chart-placeholder">
                <div class="pie-legend" style="width: 100%;">
                  <div v-for="(count, reason) in vacancyStats.vacancyReasonDistribution || {}" :key="reason" class="legend-item">
                    <span class="label" style="width: 80px;">{{ reason || '未记录' }}</span>
                    <span class="value">{{ count }}套</span>
                    <span class="percent">{{ vacancyReasonPercent(count) }}%</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
              <span>长期空置房产列表</span>
              <el-button type="primary" size="small" @click="handleExportVacant">导出清单</el-button>
            </div>
          </template>
          <el-table :data="longTermVacantList" size="small" border>
            <el-table-column prop="propertyCode" label="房产编号" width="120" />
            <el-table-column prop="buildingName" label="楼宇" width="120" />
            <el-table-column prop="floorNumber" label="楼层" width="80" />
            <el-table-column prop="vacancyDays" label="空置天数" width="100" align="center">
              <template #default="{ row }">
                <span style="color: #F56C6C; font-weight: bold;">{{ row.vacancyDays || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="vacancyReason" label="空置原因" width="120" />
            <el-table-column prop="vacantSince" label="空置开始日期" width="120" />
          </el-table>
          <el-pagination style="margin-top: 15px; text-align: right;" v-model:current-page="vacantPageNum" v-model:page-size="vacantPageSize" :total="vacantTotal" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadLongTermVacant" @current-change="loadLongTermVacant" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { House, HomeFilled, Shop, TrendCharts } from '@element-plus/icons-vue'
import api from '@/api'

const stats = ref({})
const activeTab = ref('general')
const vacancyStats = ref({})
const longTermVacantList = ref([])
const vacantPageNum = ref(1)
const vacantPageSize = ref(10)
const vacantTotal = ref(0)

const occupancyRate = computed(() => {
  return (stats.value.occupancyRate || 0).toFixed(1)
})

const vacancyRate = computed(() => {
  return (stats.value.vacancyRate || 0).toFixed(1)
})

const rentalRate = computed(() => {
  return (stats.value.rentalRate || 0).toFixed(1)
})

const statusList = computed(() => {
  const statusMap = {
    1: { label: '待售', type: 'warning', color: '#E6A23C' },
    2: { label: '已售未交付', type: 'info', color: '#909399' },
    3: { label: '业主自住', type: 'success', color: '#67C23A' },
    4: { label: '业主出租', type: '', color: '#409EFF' },
    5: { label: '空置', type: 'info', color: '#909399' },
    6: { label: '装修中', type: 'warning', color: '#F56C6C' },
    7: { label: '查封', type: 'danger', color: '#F44336' }
  }
  const total = stats.value.totalCount || 1
  const dist = stats.value.statusDistribution || {}
  
  return Object.keys(statusMap).map(key => ({
    ...statusMap[key],
    count: dist[key] || 0,
    percent: ((dist[key] || 0) * 100 / total).toFixed(1)
  }))
})

function typePercent(type) {
  const total = stats.value.totalCount || 1
  const count = stats.value.typeDistribution?.[type] || 0
  return (count * 100 / total).toFixed(1)
}

function houseTypePercent(type) {
  const dist = stats.value.houseTypeDistribution || {}
  const max = Math.max(...Object.values(dist), 1)
  const count = dist[type] || 0
  return (count * 100 / max).toFixed(0)
}

function areaPercent(count) {
  const dist = stats.value.areaDistribution || {}
  const max = Math.max(...Object.values(dist), 1)
  return (count * 100 / max).toFixed(0)
}

function loadVacancyStats() {
  api.parkPropertyVacancy.stats().then(res => {
    vacancyStats.value = res.data || {}
  })
}

function loadLongTermVacant() {
  api.parkPropertyVacancy.longTermList({ pageNum: vacantPageNum.value, pageSize: vacantPageSize.value }).then(res => {
    longTermVacantList.value = res.data.list || []
    vacantTotal.value = res.data.total || 0
  })
}

function houseVacancyPercent(count) {
  const list = vacancyStats.value.houseTypeVacancyStats || []
  const max = Math.max(...list.map(i => i.vacantCount), 1)
  return ((count || 0) * 100 / max).toFixed(0)
}

function durationPercent(count) {
  const dist = vacancyStats.value.vacancyDurationDistribution || {}
  const max = Math.max(...Object.values(dist), 1)
  return ((count || 0) * 100 / max).toFixed(0)
}

function vacancyReasonPercent(count) {
  const dist = vacancyStats.value.vacancyReasonDistribution || {}
  const total = Object.values(dist).reduce((a, b) => a + b, 0) || 1
  return ((count || 0) * 100 / total).toFixed(1)
}

function handleExportVacant() {
  api.parkPropertyVacancy.exportList().then(res => {
    const data = res.data || []
    const headers = ['房产编号', '楼宇', '楼层', '空置天数', '空置原因', '空置开始日期']
    const rows = data.map(item => [item.propertyCode, item.buildingName, item.floorNumber, item.vacancyDays || '', item.vacancyReason || '', item.vacantSince || ''])
    const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `长期空置房产清单_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(link.href)
  })
}

watch(activeTab, (val) => {
  if (val === 'vacancy') {
    loadVacancyStats()
    loadLongTermVacant()
  }
})

onMounted(() => {
  loadStats()
})

function loadStats() {
  api.parkProperty.stats().then(res => {
    stats.value = res.data || {}
  })
}
</script>

<style scoped lang="scss">
.property-stats {
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
  &.danger .stat-icon { color: #F56C6C; }
  &.danger .stat-value { color: #F56C6C; }
  
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

.pie-chart-placeholder {
  display: flex;
  align-items: center;
  gap: 30px;
  
  .chart-total {
    text-align: center;
    
    .total-num {
      display: block;
      font-size: 32px;
      font-weight: bold;
      color: #303133;
    }
    
    .total-label {
      font-size: 14px;
      color: #606266;
    }
  }
  
  .pie-legend {
    flex: 1;
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;
      
      .dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
      }
      
      .label {
        width: 50px;
        font-size: 14px;
        color: #606266;
      }
      
      .value {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }
      
      .percent {
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
    margin-bottom: 15px;
    
    .status-info {
      width: 100px;
      display: flex;
      flex-direction: column;
      gap: 5px;
      
      .count {
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
    
    .percent-text {
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
        font-size: 20px;
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

.bar-chart {
  .bar-item {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
    
    .bar-label {
      width: 80px;
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
        background: #67C23A;
        border-radius: 4px;
        transition: width 0.3s;
      }
    }
    
    .bar-value {
      width: 60px;
      text-align: right;
      font-size: 13px;
      color: #303133;
      font-weight: 500;
    }
  }
}

.avg-area {
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

.building-occupancy {
  width: 100%;
}

.orientation-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  
  .orientation-item {
    text-align: center;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 8px;
    
    .orientation-value {
      font-size: 24px;
      font-weight: bold;
      color: #409EFF;
      margin-bottom: 5px;
    }
    
    .orientation-label {
      font-size: 14px;
      color: #606266;
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
