<template>
  <div class="owner-container">
    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">业主总数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value success">{{ stats.personalCount || 0 }}</div>
            <div class="stat-label">个人业主</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value warning">{{ stats.enterpriseCount || 0 }}</div>
            <div class="stat-label">企业业主</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ stats.authPassedCount || 0 }}</div>
            <div class="stat-label">已认证</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #F56C6C;">{{ stats.authPendingCount || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #909399;">{{ stats.avgPropertyCount ? stats.avgPropertyCount.toFixed(1) : '0' }}</div>
            <div class="stat-label">户均房产</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" size="default">
        <el-form-item label="业主姓名">
          <el-input v-model="queryForm.ownerName" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="queryForm.phone" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="证件号码">
          <el-input v-model="queryForm.idCard" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="业主类型">
          <el-select v-model="queryForm.ownerType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="个人" :value="1" />
            <el-option label="企业" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="queryForm.authStatus" placeholder="全部" clearable style="width: 120px;">
            <el-option label="未认证" :value="1" />
            <el-option label="已认证" :value="2" />
            <el-option label="认证失败" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="房产编号">
          <el-input v-model="queryForm.propertyCode" placeholder="请输入" clearable @keyup.enter="handleSearch" />
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
          <span>业主列表</span>
          <div class="header-buttons">
            <el-button v-if="hasAddPermission" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增业主
            </el-button>
            <el-button v-if="hasImportPermission" type="warning" @click="handleImport">
              <el-icon><Upload /></el-icon>导入业主
            </el-button>
            <el-button v-if="hasExportPermission" type="info" @click="handleExport">
              <el-icon><Download /></el-icon>导出业主
            </el-button>
            <el-button v-if="hasStatsPermission" type="success" @click="handleStats">
              <el-icon><DataLine /></el-icon>统计分析
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="业主姓名" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.name || '-' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.ownerType === 1 ? 'primary' : (row.ownerType === 2 ? 'warning' : 'success')" size="small">
              {{ row.ownerType === 1 ? '个人' : (row.ownerType === 2 ? '企业' : '投资') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="idCard" label="证件号码" width="180">
          <template #default="{ row }">
            {{ formatIdCard(row.idCard) }}
          </template>
        </el-table-column>
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditTagType(row.authStatus)" size="small">
              {{ getAuditStatusLabel(row.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入住状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.occupancyStatus === 1 ? 'success' : (row.occupancyStatus === 2 ? 'info' : 'warning')">
              {{ row.occupancyStatus === 1 ? '已入住' : (row.occupancyStatus === 2 ? '未入住' : '委托出租') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联房产" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.propertyCount || 0 }}套</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerCode" label="业主编号" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="hasEditPermission" type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasAuditPermission && row.authStatus === 1" type="warning" size="small" @click="handleAudit(row)">审核</el-button>
            <el-button v-if="hasDeletePermission" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="auditDialogVisible" title="审核业主" width="500px">
      <el-form label-width="100px">
        <el-form-item label="业主姓名">
          <span>{{ currentOwner?.name }}</span>
        </el-form-item>
        <el-form-item label="业主编号">
          <span>{{ currentOwner?.ownerCode }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getAuditTagType(currentOwner?.authStatus)">
            {{ getAuditStatusLabel(currentOwner?.authStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditResult">
            <el-radio :value="2">通过</el-radio>
            <el-radio :value="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（拒绝时必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="导入业主" width="700px">
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
              <el-table-column prop="ownerName" label="业主姓名" width="120" />
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
  Search, Refresh, Plus, Upload, Download, Edit, Delete, DataLine,
  UploadFilled
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref({})
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  ownerName: '',
  phone: '',
  idCard: '',
  ownerType: null,
  auditStatus: null,
  propertyCode: ''
})

const auditDialogVisible = ref(false)
const currentOwner = ref(null)
const auditResult = ref(2)
const auditRemark = ref('')

const importDialogVisible = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const importLoading = ref(false)

const hasAddPermission = computed(() => userStore.hasPermission('park:owner:add'))
const hasEditPermission = computed(() => userStore.hasPermission('park:owner:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:owner:delete'))
const hasAuditPermission = computed(() => userStore.hasPermission('park:owner:audit'))
const hasImportPermission = computed(() => userStore.hasPermission('park:owner:import'))
const hasExportPermission = computed(() => userStore.hasPermission('park:owner:export'))
const hasStatsPermission = computed(() => userStore.hasPermission('park:owner:stats'))

onMounted(() => {
  loadStats()
  loadList()
})

function loadStats() {
  api.parkOwner.stats().then(res => {
    stats.value = res.data || {}
  })
}

function loadList() {
  loading.value = true
  const params = {
    pageNum: queryForm.pageNum,
    pageSize: queryForm.pageSize,
    ownerName: queryForm.ownerName || undefined,
    phone: queryForm.phone || undefined,
    idCard: queryForm.idCard || undefined,
    ownerType: queryForm.ownerType || undefined,
    auditStatus: queryForm.auditStatus || undefined,
    propertyCode: queryForm.propertyCode || undefined
  }

  api.parkOwner.list(params).then(res => {
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

function getAuditStatusLabel(status) {
  const map = {
    1: '未认证',
    2: '已认证',
    3: '认证失败'
  }
  return map[status] || '-'
}

function getAuditTagType(status) {
  const map = {
    1: 'info',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  queryForm.ownerName = ''
  queryForm.phone = ''
  queryForm.idCard = ''
  queryForm.ownerType = null
  queryForm.auditStatus = null
  queryForm.propertyCode = ''
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

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleView(row) {
  router.push(`/park/owner/detail/${row.id}`)
}

function handleAdd() {
  router.push('/park/owner/add')
}

function handleEdit(row) {
  router.push(`/park/owner/edit/${row.id}`)
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除业主 "${row.name || row.ownerCode}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkOwner.delete(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function handleAudit(row) {
  currentOwner.value = row
  auditResult.value = 2
  auditRemark.value = ''
  auditDialogVisible.value = true
}

function confirmAudit() {
  if (auditResult.value === 3 && !auditRemark.value.trim()) {
    ElMessage.warning('拒绝时请填写审核意见')
    return
  }
  api.parkOwner.audit({
    ownerId: currentOwner.value.id,
    auditResult: auditResult.value,
    auditOpinion: auditRemark.value,
    rejectReason: auditResult.value === 3 ? auditRemark.value : null
  }).then(() => {
    ElMessage.success('审核完成')
    auditDialogVisible.value = false
    loadList()
    loadStats()
  })
}

function handleStats() {
  router.push('/park/owner/stats')
}

function handleImport() {
  importResult.value = null
  importFile.value = null
  importDialogVisible.value = true
}

function downloadTemplate() {
  api.parkOwner.getTemplate().then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '业主导入模板.xlsx'
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
  api.parkOwner.importData(importFile.value).then(res => {
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
  
  const headers = ['行号', '业主姓名', '失败原因']
  const rows = importResult.value.failList.map(item => [
    item.rowNum || '',
    item.ownerName || '',
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
    ownerName: queryForm.ownerName || undefined,
    phone: queryForm.phone || undefined,
    idCard: queryForm.idCard || undefined,
    ownerType: queryForm.ownerType || undefined,
    auditStatus: queryForm.auditStatus || undefined,
    propertyCode: queryForm.propertyCode || undefined
  }

  api.parkOwner.export(params).then(res => {
    const data = res.data || []
    exportToCSV(data)
  })
}

function exportToCSV(data) {
  const headers = ['业主编号', '业主姓名', '业主类型', '联系电话', '证件号码', '认证状态', '入住状态', '关联房产数', '备注']
  
  const typeMap = { 1: '个人业主', 2: '企业业主', 3: '投资业主' }
  const authStatusMap = { 1: '未认证', 2: '已认证', 3: '认证失败' }
  const occupancyMap = { 1: '已入住', 2: '未入住', 3: '委托出租' }
  
  const rows = data.map(item => [
    item.ownerCode || '',
    item.name || '',
    typeMap[item.ownerType] || '',
    item.phone || '',
    item.idCard || '',
    authStatusMap[item.authStatus] || '',
    occupancyMap[item.occupancyStatus] || '',
    item.propertyCount || 0,
    item.remark || ''
  ])
  
  const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `业主列表_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.owner-container {
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
