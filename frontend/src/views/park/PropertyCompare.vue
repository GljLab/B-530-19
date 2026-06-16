<template>
  <div class="compare-container">
    <div class="compare-header">
      <el-button @click="handleBack"><el-icon><ArrowLeft /></el-icon>返回</el-button>
      <span class="compare-title">房产对比</span>
      <el-button type="success" @click="handleExport">导出对比</el-button>
    </div>

    <div v-loading="loading" class="compare-content" v-if="properties.length > 0">
      <table class="compare-table">
        <thead>
          <tr>
            <th class="label-col">对比项</th>
            <th v-for="p in properties" :key="p.id" class="data-col">
              <el-link type="primary" @click="goDetail(p)">{{ p.propertyCode }}</el-link>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr class="section-header"><td :colspan="properties.length + 1">基础信息</td></tr>
          <tr><td class="label-col">房产编号</td><td v-for="p in properties" :key="p.id">{{ p.propertyCode }}</td></tr>
          <tr><td class="label-col">楼宇</td><td v-for="p in properties" :key="p.id">{{ p.buildingName || '-' }}</td></tr>
          <tr><td class="label-col">楼层</td><td v-for="p in properties" :key="p.id">{{ p.floorNumber || '-' }}</td></tr>
          <tr><td class="label-col">房产类型</td><td v-for="p in properties" :key="p.id">{{ getPropertyTypeLabel(p.propertyType, p.propertySubType) }}</td></tr>

          <tr class="section-header"><td :colspan="properties.length + 1">房产属性</td></tr>
          <tr><td class="label-col">建筑面积(㎡)</td><td v-for="p in properties" :key="p.id">{{ p.buildingArea || '-' }}</td></tr>
          <tr><td class="label-col">套内面积(㎡)</td><td v-for="p in properties" :key="p.id">{{ p.insideArea || '-' }}</td></tr>
          <tr><td class="label-col">公摊面积(㎡)</td><td v-for="p in properties" :key="p.id">{{ p.sharedArea || '-' }}</td></tr>
          <tr><td class="label-col">户型</td><td v-for="p in properties" :key="p.id">{{ p.houseType || '-' }}</td></tr>
          <tr><td class="label-col">朝向</td><td v-for="p in properties" :key="p.id">{{ p.orientation || '-' }}</td></tr>
          <tr><td class="label-col">装修状况</td><td v-for="p in properties" :key="p.id">{{ p.decorationStatus || '-' }}</td></tr>

          <tr class="section-header"><td :colspan="properties.length + 1">产权信息</td></tr>
          <tr><td class="label-col">产权性质</td><td v-for="p in properties" :key="p.id">{{ p.propertyRightType || '-' }}</td></tr>
          <tr><td class="label-col">产权年限</td><td v-for="p in properties" :key="p.id">{{ p.propertyRightYears ? p.propertyRightYears + '年' : '-' }}</td></tr>
          <tr><td class="label-col">产权证号</td><td v-for="p in properties" :key="p.id">{{ p.propertyCertNo || '-' }}</td></tr>
          <tr><td class="label-col">交付日期</td><td v-for="p in properties" :key="p.id">{{ p.deliveryDate || '-' }}</td></tr>

          <tr class="section-header"><td :colspan="properties.length + 1">使用情况</td></tr>
          <tr><td class="label-col">当前状态</td><td v-for="p in properties" :key="p.id"><el-tag :type="getStatusTagType(p.status)" size="small">{{ getStatusLabel(p.status) }}</el-tag></td></tr>
          <tr><td class="label-col">产权人</td><td v-for="p in properties" :key="p.id">{{ p.ownerName || '待关联' }}</td></tr>
          <tr><td class="label-col">租户</td><td v-for="p in properties" :key="p.id">{{ p.tenantName || '-' }}</td></tr>

          <tr class="section-header"><td :colspan="properties.length + 1">其他信息</td></tr>
          <tr><td class="label-col">特殊标识</td><td v-for="p in properties" :key="p.id">
            <template v-if="p.specialTagList && p.specialTagList.length > 0">
              <el-tag v-for="tag in p.specialTagList" :key="tag" size="small" style="margin-right: 4px;">{{ tag }}</el-tag>
            </template>
            <span v-else>-</span>
          </td></tr>
          <tr><td class="label-col">房产标签</td><td v-for="p in properties" :key="p.id">
            <template v-if="p.tagList && p.tagList.length > 0">
              <el-tag v-for="tag in p.tagList" :key="tag.id" size="small" :color="tag.tagColor" style="color: #fff; margin-right: 4px;">{{ tag.tagName }}</el-tag>
            </template>
            <span v-else>-</span>
          </td></tr>
        </tbody>
      </table>
    </div>

    <el-empty v-if="!loading && properties.length === 0" description="请从房产列表选择2-4个房产进行对比" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const properties = ref([])

function loadCompare() {
  const idsStr = route.query.ids
  if (!idsStr) return
  const ids = idsStr.split(',').map(id => Number(id))
  loading.value = true
  api.parkProperty.compare(ids).then(res => {
    properties.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

function handleBack() {
  router.back()
}

function handleExport() {
  if (properties.value.length === 0) {
    ElMessage.warning('暂无对比数据')
    return
  }
  const rows = [
    ['对比项', ...properties.value.map(p => p.propertyCode)],
    ['房产编号', ...properties.value.map(p => p.propertyCode)],
    ['楼宇', ...properties.value.map(p => p.buildingName || '-')],
    ['楼层', ...properties.value.map(p => p.floorNumber || '-')],
    ['房产类型', ...properties.value.map(p => getPropertyTypeLabel(p.propertyType, p.propertySubType))],
    ['建筑面积(㎡)', ...properties.value.map(p => p.buildingArea || '-')],
    ['套内面积(㎡)', ...properties.value.map(p => p.insideArea || '-')],
    ['公摊面积(㎡)', ...properties.value.map(p => p.sharedArea || '-')],
    ['户型', ...properties.value.map(p => p.houseType || '-')],
    ['朝向', ...properties.value.map(p => p.orientation || '-')],
    ['装修状况', ...properties.value.map(p => p.decorationStatus || '-')],
    ['产权性质', ...properties.value.map(p => p.propertyRightType || '-')],
    ['产权年限', ...properties.value.map(p => p.propertyRightYears ? p.propertyRightYears + '年' : '-')],
    ['产权证号', ...properties.value.map(p => p.propertyCertNo || '-')],
    ['交付日期', ...properties.value.map(p => p.deliveryDate || '-')],
    ['当前状态', ...properties.value.map(p => getStatusLabel(p.status))],
    ['产权人', ...properties.value.map(p => p.ownerName || '待关联')],
    ['租户', ...properties.value.map(p => p.tenantName || '-')]
  ]
  const csvContent = '\uFEFF' + rows.map(row => row.join(',')).join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '房产对比.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

function goDetail(p) {
  router.push(`/park/property/detail/${p.id}`)
}

function getPropertyTypeLabel(type, subType) {
  if (subType) return subType
  const map = { 1: '住宅', 2: '商业', 3: '其他' }
  return map[type] || '-'
}

function getStatusLabel(status) {
  const map = {
    1: '待售',
    2: '已售未交付',
    3: '业主自住',
    4: '业主出租',
    5: '空置',
    6: '装修中',
    7: '查封'
  }
  return map[status] || '-'
}

function getStatusTagType(status) {
  const map = {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: '',
    5: 'info',
    6: 'warning',
    7: 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadCompare()
})
</script>

<style scoped>
.compare-container { padding: 20px; }
.compare-header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.compare-title { font-size: 20px; font-weight: bold; flex: 1; }
.compare-table { width: 100%; border-collapse: collapse; }
.compare-table th, .compare-table td { border: 1px solid #ebeef5; padding: 12px 16px; text-align: center; vertical-align: middle; }
.compare-table .label-col { background: #f5f7fa; font-weight: 500; text-align: right; width: 140px; min-width: 140px; }
.compare-table .section-header td { background: #ecf5ff; font-weight: bold; color: #409EFF; text-align: left; }
.compare-table .data-col { min-width: 180px; }
</style>
