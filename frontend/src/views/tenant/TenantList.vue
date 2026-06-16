<template>
  <div class="tenant-container">
    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">租户总数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value success">{{ stats.inRentCount || 0 }}</div>
            <div class="stat-label">在租</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ stats.personalCount || 0 }}</div>
            <div class="stat-label">个人租户</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value warning">{{ stats.enterpriseCount || 0 }}</div>
            <div class="stat-label">企业租户</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #909399;">{{ stats.retiredCount || 0 }}</div>
            <div class="stat-label">已退租</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #F56C6C;">{{ stats.blacklistCount || 0 }}</div>
            <div class="stat-label">黑名单</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" size="default">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="姓名/手机号/身份证号" clearable @keyup.enter="handleSearch" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="租户类型">
          <el-select v-model="queryForm.tenantType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="个人" :value="1" />
            <el-option label="企业" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="租户状态">
          <el-select v-model="queryForm.tenantStatus" placeholder="全部" clearable style="width: 120px;">
            <el-option label="在租" :value="1" />
            <el-option label="已退租" :value="2" />
            <el-option label="黑名单" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="queryForm.sortField" placeholder="默认" clearable style="width: 120px;">
            <el-option label="租户编号" value="tenantCode" />
            <el-option label="姓名" value="name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>租户列表</span>
          <div class="header-buttons">
            <el-button v-if="hasAddPermission" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增租户
            </el-button>
            <el-button v-if="hasImportPermission" type="warning" @click="handleImport">
              <el-icon><Upload /></el-icon>导入租户
            </el-button>
            <el-button v-if="hasExportPermission" type="info" @click="handleExport">
              <el-icon><Download /></el-icon>导出数据
            </el-button>
            <el-button v-if="hasStatsPermission" type="success" @click="handleStats">
              <el-icon><DataLine /></el-icon>统计分析
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="tenantCode" label="租户编号" width="100" sortable="custom" />
        <el-table-column prop="name" label="姓名" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.name || '-' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.tenantType === 1 ? 'primary' : 'warning'" size="small">
              {{ row.tenantTypeText || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="idCard" label="身份证号" width="180">
          <template #default="{ row }">
            {{ formatIdCard(row.idCard) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.tenantStatus)" size="small">
              {{ row.tenantStatusText || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前房产" width="150">
          <template #default="{ row }">
            {{ row.currentPropertyCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="当前楼宇" width="120">
          <template #default="{ row }">
            {{ row.currentBuildingName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="租赁期限" width="200">
          <template #default="{ row }">
            {{ row.currentLeasePeriod || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="hasEditPermission" type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasEditPermission && row.tenantStatus !== 3" type="warning" size="small" @click="handleSetBlacklist(row, true)">黑名单</el-button>
            <el-button v-if="hasEditPermission && row.tenantStatus === 3" type="info" size="small" @click="handleSetBlacklist(row, false)">移出黑名单</el-button>
            <el-button v-if="hasDeletePermission" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="importDialogVisible" title="导入租户" width="700px">
      <div class="import-content">
        <div style="margin-bottom: 20px;">
          <el-button type="primary" @click="downloadTemplate">
            <el-icon><Download /></el-icon>下载导入模板
          </el-button>
        </div>
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          :disabled="!!importResult"
          accept=".xlsx,.xls">
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 xlsx/xls 格式文件
            </div>
          </template>
        </el-upload>
        <div v-if="importResult" style="margin-top: 20px;">
          <el-alert
            :title="'导入完成：成功 ' + importResult.successCount + ' 条，失败 ' + importResult.failCount + ' 条'"
            :type="importResult.failCount > 0 ? 'warning' : 'success'"
            show-icon />
          <div v-if="importResult.failList && importResult.failList.length > 0" style="margin-top: 15px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
              <span style="font-weight: bold;">失败列表</span>
              <el-button size="small" type="primary" @click="downloadFailList">
                <el-icon><Download /></el-icon>下载失败数据
              </el-button>
            </div>
            <el-table :data="importResult.failList" border size="small" max-height="200">
              <el-table-column prop="rowNum" label="行号" width="80" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="idCard" label="身份证号" width="180" />
              <el-table-column prop="reason" label="失败原因" />
            </el-table>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">{{ importResult ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!importResult" type="primary" :loading="importLoading" @click="handleImportConfirm">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Upload, Download, DataLine,
  UploadFilled
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref({})
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  tenantType: null,
  tenantStatus: null,
  sortField: null,
  sortOrder: 'asc'
})

const importDialogVisible = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const importLoading = ref(false)

const hasAddPermission = computed(() => userStore.hasPermission('park:tenant:add'))
const hasEditPermission = computed(() => userStore.hasPermission('park:tenant:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:tenant:delete'))
const hasImportPermission = computed(() => userStore.hasPermission('park:tenant:import'))
const hasExportPermission = computed(() => userStore.hasPermission('park:tenant:export'))
const hasStatsPermission = computed(() => userStore.hasPermission('park:tenant:stats'))

onMounted(() => {
  loadStats()
  loadList()
})

function loadStats() {
  api.parkTenant.stats().then(res => {
    stats.value = res.data || {}
  })
}

function loadList() {
  loading.value = true
  const params = {
    pageNum: queryForm.pageNum,
    pageSize: queryForm.pageSize,
    keyword: queryForm.keyword || undefined,
    tenantType: queryForm.tenantType || undefined,
    tenantStatus: queryForm.tenantStatus || undefined,
    sortField: queryForm.sortField || undefined,
    sortOrder: queryForm.sortOrder || undefined
  }

  api.parkTenant.list(params).then(res => {
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function formatIdCard(idCard) {
  if (!idCard) return '-'
  if (idCard.length === 18) {
    return idCard.substring(0, 6) + '********' + idCard.substring(14)
  }
  return idCard
}

function getStatusTagType(status) {
  const map = {
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] || 'info'
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  queryForm.keyword = ''
  queryForm.tenantType = null
  queryForm.tenantStatus = null
  queryForm.sortField = null
  queryForm.sortOrder = 'asc'
  handleSearch()
}

function handleSizeChange(size) {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadList()
}

function handlePageChange(page) {
  queryForm.pageNum = page
  loadList()
}

function handleSortChange({ prop, order }) {
  if (prop && order) {
    queryForm.sortField = prop
    queryForm.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryForm.sortField = null
    queryForm.sortOrder = 'asc'
  }
  loadList()
}

function handleView(row) {
  router.push(`/park/tenant/detail/${row.id}`)
}

function handleAdd() {
  router.push('/park/tenant/add')
}

function handleEdit(row) {
  router.push(`/park/tenant/edit/${row.id}`)
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除租户 "${row.name || row.tenantCode}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkTenant.delete(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function handleSetBlacklist(row, isBlacklist) {
  const action = isBlacklist ? '加入黑名单' : '移出黑名单'
  ElMessageBox.confirm(`确定要将租户 "${row.name || row.tenantCode}" ${action}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkTenant.setBlacklist({
      id: row.id,
      isBlacklist: isBlacklist,
      reason: ''
    }).then(() => {
      ElMessage.success(`${action}成功`)
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function handleStats() {
  router.push('/park/tenant/stats')
}

function handleImport() {
  importResult.value = null
  importFile.value = null
  importDialogVisible.value = true
}

function downloadTemplate() {
  api.parkTenant.getTemplate().then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '租户导入模板.xlsx'
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('模板下载成功')
  }).catch(() => {
    ElMessage.error('模板下载失败')
  })
}

function handleFileChange(file) {
  importFile.value = file.raw
  importResult.value = null
}

function handleImportConfirm() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  importLoading.value = true
  api.parkTenant.importData(importFile.value).then(res => {
    importResult.value = res.data
    loadList()
    loadStats()
    ElMessage.success('导入完成')
  }).finally(() => {
    importLoading.value = false
  })
}

function downloadFailList() {
  if (!importResult.value || !importResult.value.failList) return
  
  const headers = ['行号', '姓名', '身份证号', '失败原因']
  const rows = importResult.value.failList.map(item => [
    item.rowNum || '',
    item.name || '',
    item.idCard || '',
    item.reason || ''
  ])
  
  const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `导入失败数据_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

function handleExport() {
  const params = {
    keyword: queryForm.keyword || undefined,
    tenantType: queryForm.tenantType || undefined,
    tenantStatus: queryForm.tenantStatus || undefined
  }

  api.parkTenant.export(params).then(res => {
    const data = res.data || []
    exportToCSV(data)
  })
}

function exportToCSV(data) {
  const headers = ['租户编号', '姓名', '租户类型', '手机号', '身份证号', '租户状态', '当前房产', '当前楼宇', '租赁期限', '备注']
  
  const typeMap = { 1: '个人租户', 2: '企业租户' }
  const statusMap = { 1: '在租', 2: '已退租', 3: '黑名单' }
  
  const rows = data.map(item => [
    item.tenantCode || '',
    item.name || '',
    typeMap[item.tenantType] || '',
    item.phone || '',
    item.idCard || '',
    statusMap[item.tenantStatus] || '',
    item.currentPropertyCode || '',
    item.currentBuildingName || '',
    item.currentLeasePeriod || '',
    item.remark || ''
  ])
  
  const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `租户列表_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.tenant-container {
  padding: 20px;
}

.stats-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-item {
  text-align: center;
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 8px;
    
    &.primary { color: #409EFF; }
    &.success { color: #67C23A; }
    &.warning { color: #E6A23C; }
    &.info { color: #909399; }
  }
  
  .stat-label {
    font-size: 14px;
    color: #606266;
  }
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-buttons {
      display: flex;
      gap: 8px;
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}

.import-content {
  padding: 20px 0;
}
</style>
