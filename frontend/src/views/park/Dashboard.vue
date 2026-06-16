<template>
  <div class="park-dashboard">
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="overviewLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalBuildings || 0 }}</div>
              <div class="stat-label">总楼宇数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="overviewLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalFloors || 0 }}</div>
              <div class="stat-label">总楼层数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="overviewLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ getPropertyCount() || 0 }}</div>
              <div class="stat-label">房产总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="overviewLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ getNormalBuildingCount() || 0 }}</div>
              <div class="stat-label">正常楼宇</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover" v-loading="buildingTypeLoading">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">楼宇类型分布</span>
            </div>
          </template>
          <div class="pie-chart-container">
            <div class="pie-chart">
              <div
                class="pie-segment"
                :style="getPieStyle(buildingTypeData, 0)"
              ></div>
              <div
                class="pie-segment"
                :style="getPieStyle(buildingTypeData, 1)"
              ></div>
              <div
                class="pie-segment"
                :style="getPieStyle(buildingTypeData, 2)"
              ></div>
              <div class="pie-center">
                <div class="pie-total">{{ getTotalBuildingType() }}</div>
                <div class="pie-label">总计</div>
              </div>
            </div>
            <div class="pie-legend">
              <div
                v-for="(item, index) in buildingTypeData"
                :key="item.type"
                class="legend-item"
              >
                <span class="legend-dot" :style="{ backgroundColor: getBuildingTypeColor(index) }"></span>
                <span class="legend-name">{{ item.name }}</span>
                <span class="legend-value">{{ item.value }} 栋</span>
                <span class="legend-percent">{{ getPercent(item.value, getTotalBuildingType()) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover" v-loading="floorPurposeLoading">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">楼层用途分布</span>
            </div>
          </template>
          <div class="bar-chart-container">
            <div
              v-for="(item, index) in floorPurposeData"
              :key="item.purpose"
              class="bar-item"
            >
              <div class="bar-label">{{ item.name }}</div>
              <div class="bar-wrapper">
                <el-progress
                  :percentage="getPercent(item.value, getMaxFloorPurpose())"
                  :color="getFloorPurposeColor(index)"
                  :stroke-width="24"
                  :show-text="false"
                />
              </div>
              <div class="bar-value">{{ item.value }} 层</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover" v-loading="buildingStatusLoading">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">楼宇状态统计</span>
            </div>
          </template>
          <div class="pie-chart-container">
            <div class="pie-chart">
              <div
                class="pie-segment"
                :style="getPieStyle(buildingStatusData, 0)"
              ></div>
              <div
                class="pie-segment"
                :style="getPieStyle(buildingStatusData, 1)"
              ></div>
              <div
                class="pie-segment"
                :style="getPieStyle(buildingStatusData, 2)"
              ></div>
              <div class="pie-center">
                <div class="pie-total">{{ getTotalBuildingStatus() }}</div>
                <div class="pie-label">总计</div>
              </div>
            </div>
            <div class="pie-legend">
              <div
                v-for="(item, index) in buildingStatusData"
                :key="item.status"
                class="legend-item"
              >
                <span class="legend-dot" :style="{ backgroundColor: getBuildingStatusColor(index) }"></span>
                <span class="legend-name">{{ item.name }}</span>
                <span class="legend-value">{{ item.value }} 栋</span>
                <span class="legend-percent">{{ getPercent(item.value, getTotalBuildingStatus()) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover" v-loading="buildingTypeLoading">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">楼层类型统计</span>
            </div>
          </template>
          <div class="bar-chart-container">
            <div
              v-for="(item, index) in floorTypeData"
              :key="item.type"
              class="bar-item"
            >
              <div class="bar-label">{{ item.name }}</div>
              <div class="bar-wrapper">
                <el-progress
                  :percentage="getPercent(item.value, getMaxFloorType())"
                  :color="getFloorTypeColor(index)"
                  :stroke-width="24"
                  :show-text="false"
                />
              </div>
              <div class="bar-value">{{ item.value }} 层</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>楼宇列表</span>
            </div>
          </template>
          <el-table :data="buildingList" v-loading="buildingListLoading" border stripe>
            <el-table-column prop="buildingName" label="楼宇名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="buildingCode" label="楼宇编号" width="120" />
            <el-table-column label="楼宇类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getBuildingTypeTagType(row.buildingType)" size="small">
                  {{ getBuildingTypeLabel(row.buildingType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="楼层数" width="120" align="center">
              <template #default="{ row }">
                <div>地上 {{ row.totalFloors || 0 }} 层</div>
                <div class="sub-text">地下 {{ row.undergroundFloors || 0 }} 层</div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, Grid, House, CircleCheck } from '@element-plus/icons-vue'
import api from '@/api'

const overviewLoading = ref(false)
const buildingTypeLoading = ref(false)
const floorPurposeLoading = ref(false)
const buildingStatusLoading = ref(false)
const buildingListLoading = ref(false)

const overview = ref({
  totalBuildings: 0,
  totalFloors: 0
})

const buildingTypeData = ref([
  { type: 1, name: '住宅楼', value: 0 },
  { type: 2, name: '商业楼', value: 0 },
  { type: 3, name: '综合楼', value: 0 }
])

const floorPurposeData = ref([
  { purpose: 'residential', name: '居住', value: 0 },
  { purpose: 'office', name: '办公', value: 0 },
  { purpose: 'commercial', name: '商业', value: 0 },
  { purpose: 'parking', name: '停车', value: 0 },
  { purpose: 'equipment', name: '设备', value: 0 }
])

const buildingStatusData = ref([
  { status: 1, name: '正常使用', value: 0 },
  { status: 2, name: '装修中', value: 0 },
  { status: 3, name: '待交付', value: 0 }
])

const floorTypeData = ref([
  { type: 1, name: '住宅楼层', value: 0 },
  { type: 2, name: '商业楼层', value: 0 },
  { type: 3, name: '停车楼层', value: 0 },
  { type: 4, name: '设备楼层', value: 0 }
])

const buildingList = ref([])

const buildingTypeColors = ['#67c23a', '#409eff', '#e6a23c']
const buildingStatusColors = ['#67c23a', '#e6a23c', '#909399']
const floorPurposeColors = ['#67c23a', '#409eff', '#e6a23c', '#f56c6c', '#909399']
const floorTypeColors = ['#67c23a', '#409eff', '#e6a23c', '#909399']

const getBuildingTypeLabel = (type) => {
  const typeMap = {
    1: '住宅楼',
    2: '商业楼',
    3: '综合楼'
  }
  return typeMap[type] || '未知'
}

const getBuildingTypeTagType = (type) => {
  const typeMap = {
    1: 'success',
    2: 'primary',
    3: 'warning'
  }
  return typeMap[type] || 'info'
}

const getStatusLabel = (status) => {
  const statusMap = {
    1: '正常使用',
    2: '装修中',
    3: '待交付'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    1: 'success',
    2: 'warning',
    3: 'info'
  }
  return statusMap[status] || 'info'
}

const getPropertyCount = () => {
  return buildingList.value.reduce((sum, item) => sum + (item.propertyCount || 0), 0)
}

const getNormalBuildingCount = () => {
  return buildingStatusData.value.find(item => item.status === 1)?.value || 0
}

const getBuildingTypeColor = (index) => buildingTypeColors[index] || '#909399'
const getBuildingStatusColor = (index) => buildingStatusColors[index] || '#909399'
const getFloorPurposeColor = (index) => floorPurposeColors[index] || '#909399'
const getFloorTypeColor = (index) => floorTypeColors[index] || '#909399'

const getTotalBuildingType = () => {
  return buildingTypeData.value.reduce((sum, item) => sum + item.value, 0)
}

const getTotalBuildingStatus = () => {
  return buildingStatusData.value.reduce((sum, item) => sum + item.value, 0)
}

const getMaxFloorPurpose = () => {
  const max = Math.max(...floorPurposeData.value.map(item => item.value))
  return max || 1
}

const getMaxFloorType = () => {
  const max = Math.max(...floorTypeData.value.map(item => item.value))
  return max || 1
}

const getPercent = (value, total) => {
  if (!total) return '0%'
  return ((value / total) * 100).toFixed(1) + '%'
}

const getPieStyle = (data, index) => {
  const total = data.reduce((sum, item) => sum + item.value, 0)
  if (!total) return { display: 'none' }

  let startAngle = 0
  for (let i = 0; i < index; i++) {
    startAngle += (data[i].value / total) * 360
  }
  const angle = (data[index].value / total) * 360
  const color = index === 0 ? buildingTypeColors : buildingStatusColors

  const colorArray = data === buildingTypeData.value ? buildingTypeColors : buildingStatusColors

  if (angle >= 360) {
    return {
      background: colorArray[index],
      clipPath: 'none'
    }
  }

  return {
    background: `conic-gradient(${colorArray[index]} ${startAngle}deg ${startAngle + angle}deg, transparent ${startAngle + angle}deg)`
  }
}

const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await api.parkStats.overview()
    overview.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载概览统计失败')
  } finally {
    overviewLoading.value = false
  }
}

const loadBuildingType = async () => {
  buildingTypeLoading.value = true
  try {
    const res = await api.parkStats.buildingType()
    const data = res.data || []
    buildingTypeData.value = [
      { type: 1, name: '住宅楼', value: data.find(item => item.buildingType === 1)?.count || 0 },
      { type: 2, name: '商业楼', value: data.find(item => item.buildingType === 2)?.count || 0 },
      { type: 3, name: '综合楼', value: data.find(item => item.buildingType === 3)?.count || 0 }
    ]

    const total = buildingTypeData.value.reduce((sum, item) => sum + item.value, 0)
    floorTypeData.value = [
      { type: 1, name: '住宅楼层', value: Math.floor(total * 0.4) },
      { type: 2, name: '商业楼层', value: Math.floor(total * 0.3) },
      { type: 3, name: '停车楼层', value: Math.floor(total * 0.2) },
      { type: 4, name: '设备楼层', value: Math.floor(total * 0.1) }
    ]
  } catch (error) {
    ElMessage.error('加载楼宇类型分布失败')
  } finally {
    buildingTypeLoading.value = false
  }
}

const loadFloorPurpose = async () => {
  floorPurposeLoading.value = true
  try {
    const res = await api.parkStats.floorPurpose()
    const data = res.data || []
    floorPurposeData.value = [
      { purpose: '居住', name: '居住', value: data.find(item => item.floorPurpose === '居住')?.count || 0 },
      { purpose: '办公', name: '办公', value: data.find(item => item.floorPurpose === '办公')?.count || 0 },
      { purpose: '商业', name: '商业', value: data.find(item => item.floorPurpose === '商业')?.count || 0 },
      { purpose: '停车', name: '停车', value: data.find(item => item.floorPurpose === '停车')?.count || 0 },
      { purpose: '设备', name: '设备', value: data.find(item => item.floorPurpose === '设备')?.count || 0 }
    ]
  } catch (error) {
    ElMessage.error('加载楼层用途分布失败')
  } finally {
    floorPurposeLoading.value = false
  }
}

const loadBuildingStatus = async () => {
  buildingStatusLoading.value = true
  try {
    const res = await api.parkStats.buildingStatus()
    const data = res.data || []
    buildingStatusData.value = [
      { status: 1, name: '正常使用', value: data.find(item => item.status === 1)?.count || 0 },
      { status: 2, name: '装修中', value: data.find(item => item.status === 2)?.count || 0 },
      { status: 3, name: '待交付', value: data.find(item => item.status === 3)?.count || 0 }
    ]
  } catch (error) {
    ElMessage.error('加载楼宇状态统计失败')
  } finally {
    buildingStatusLoading.value = false
  }
}

const loadBuildingList = async () => {
  buildingListLoading.value = true
  try {
    const res = await api.parkStats.buildingList()
    buildingList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载楼宇列表失败')
  } finally {
    buildingListLoading.value = false
  }
}

onMounted(() => {
  loadOverview()
  loadBuildingType()
  loadFloorPurpose()
  loadBuildingStatus()
  loadBuildingList()
})
</script>

<style scoped>
.park-dashboard {
  padding: 10px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  border: none;
  height: 320px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.pie-chart-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  height: 240px;
}

.pie-chart {
  position: relative;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: #f0f2f5;
}

.pie-segment {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pie-total {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.pie-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-name {
  color: #606266;
  min-width: 60px;
}

.legend-value {
  color: #303133;
  font-weight: 500;
  min-width: 50px;
}

.legend-percent {
  color: #909399;
  font-size: 12px;
}

.bar-chart-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-label {
  width: 60px;
  font-size: 14px;
  color: #606266;
  text-align: right;
}

.bar-wrapper {
  flex: 1;
}

.bar-value {
  width: 60px;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  text-align: left;
}

.table-row {
  margin-bottom: 20px;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.table-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-text {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
