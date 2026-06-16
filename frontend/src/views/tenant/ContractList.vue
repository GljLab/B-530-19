<template>
  <div class="contract-container">
    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">合同总数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #E6A23C;">{{ stats.pendingAuditCount || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value success">{{ stats.activeCount || 0 }}</div>
            <div class="stat-label">有效</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ stats.renewedCount || 0 }}</div>
            <div class="stat-label">已续租</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ stats.terminatedCount || 0 }}</div>
            <div class="stat-label">已终止</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #909399;">{{ stats.expiredCount || 0 }}</div>
            <div class="stat-label">已过期</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" size="default">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="合同编号/租户名称/房产编号" clearable @keyup.enter="handleSearch" style="width: 220px;" />
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待审核" :value="1" />
            <el-option label="有效" :value="2" />
            <el-option label="已续租" :value="3" />
            <el-option label="已终止" :value="4" />
            <el-option label="已过期" :value="5" />
            <el-option label="已拒绝" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="租赁类型">
          <el-select v-model="queryForm.leaseType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="整租" :value="1" />
            <el-option label="合租" :value="2" />
            <el-option label="商铺" :value="3" />
            <el-option label="办公" :value="4" />
            <el-option label="仓库" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="租户">
          <el-select v-model="queryForm.tenantId" placeholder="请选择" clearable filterable style="width: 160px;">
            <el-option v-for="tenant in tenantOptions" :key="tenant.id" :label="tenant.name" :value="tenant.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="签约日期">
          <el-date-picker
            v-model="queryForm.signDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker
            v-model="queryForm.expireDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;" />
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
          <span>合同列表</span>
          <div class="header-buttons">
            <el-button v-if="hasAddPermission" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增合同
            </el-button>
            <el-button v-if="hasAuditPermission" type="warning" :disabled="selectedIds.length === 0" @click="handleBatchAudit">
              <el-icon><Check /></el-icon>审核合同
            </el-button>
            <el-button v-if="hasListPermission" type="info" @click="handleCheckExpired">
              <el-icon><AlarmClock /></el-icon>检查过期
            </el-button>
            <el-button v-if="hasListPermission" type="success" @click="handleExport">
              <el-icon><Download /></el-icon>导出
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="contractCode" label="合同编号" width="140" sortable="custom">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.contractCode || '-' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="租户名称" width="140" />
        <el-table-column prop="propertyCode" label="房产编号" width="130" />
        <el-table-column label="租赁类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getLeaseTypeLabel(row.leaseType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="租赁期限" width="220">
          <template #default="{ row }">
            <div>{{ row.startDate || '-' }} 至 {{ row.endDate || '-' }}</div>
            <div style="font-size: 12px; color: #909399; margin-top: 4px;">
              {{ getLeaseDays(row.startDate, row.endDate) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="monthlyRent" label="月租金(元)" width="120" sortable="custom" align="right">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: 500;">{{ formatMoney(row.monthlyRent) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" sortable="custom" />
        <el-table-column prop="endDate" label="结束日期" width="120" sortable="custom" />
        <el-table-column label="合同状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="hasEditPermission" type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasAuditPermission && row.status === 1" type="warning" size="small" @click="handleAudit(row)">审核</el-button>
            <el-button v-if="hasRenewPermission && row.status === 2" type="primary" size="small" @click="handleRenew(row)">续租</el-button>
            <el-button v-if="hasTerminatePermission && (row.status === 2 || row.status === 3)" type="danger" size="small" @click="handleTerminate(row)">终止</el-button>
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

    <el-dialog v-model="auditDialogVisible" title="审核合同" width="550px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="合同编号">
          <span>{{ currentContract?.contractCode }}</span>
        </el-form-item>
        <el-form-item label="租户名称">
          <span>{{ currentContract?.tenantName }}</span>
        </el-form-item>
        <el-form-item label="房产编号">
          <span>{{ currentContract?.propertyCode }}</span>
        </el-form-item>
        <el-form-item label="月租金">
          <span style="color: #F56C6C; font-weight: 500;">{{ formatMoney(currentContract?.monthlyRent) }} 元</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentContract?.status)">
            {{ getStatusLabel(currentContract?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio :value="2">通过</el-radio>
            <el-radio :value="6">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（拒绝时必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="renewDialogVisible" title="续租合同" width="550px">
      <el-form :model="renewForm" label-width="100px" :rules="renewRules" ref="renewFormRef">
        <el-form-item label="合同编号">
          <span>{{ currentContract?.contractCode }}</span>
        </el-form-item>
        <el-form-item label="租户名称">
          <span>{{ currentContract?.tenantName }}</span>
        </el-form-item>
        <el-form-item label="原结束日期">
          <span>{{ currentContract?.endDate }}</span>
        </el-form-item>
        <el-form-item label="新月租金" prop="newMonthlyRent">
          <el-input-number v-model="renewForm.newMonthlyRent" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="新开始日期" prop="newStartDate">
          <el-date-picker
            v-model="renewForm.newStartDate"
            type="date"
            placeholder="请选择新开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="新结束日期" prop="newEndDate">
          <el-date-picker
            v-model="renewForm.newEndDate"
            type="date"
            placeholder="请选择新结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="renewForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRenew">确定续租</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="terminateDialogVisible" title="终止合同" width="550px">
      <el-form :model="terminateForm" label-width="100px" :rules="terminateRules" ref="terminateFormRef">
        <el-form-item label="合同编号">
          <span>{{ currentContract?.contractCode }}</span>
        </el-form-item>
        <el-form-item label="租户名称">
          <span>{{ currentContract?.tenantName }}</span>
        </el-form-item>
        <el-form-item label="原结束日期">
          <span>{{ currentContract?.endDate }}</span>
        </el-form-item>
        <el-form-item label="终止日期" prop="terminateDate">
          <el-date-picker
            v-model="terminateForm.terminateDate"
            type="date"
            placeholder="请选择终止日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="终止原因" prop="terminateReason">
          <el-select v-model="terminateForm.terminateReason" placeholder="请选择终止原因" style="width: 100%;">
            <el-option label="提前退租" value="1" />
            <el-option label="协商解除" value="2" />
            <el-option label="违约终止" value="3" />
            <el-option label="其他原因" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="terminateForm.remark" type="textarea" :rows="3" placeholder="请输入备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="terminateDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmTerminate">确认终止</el-button>
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
  Search, Refresh, Plus, Check, Download, AlarmClock
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref({})
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const tenantOptions = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: null,
  leaseType: null,
  tenantId: null,
  signDateRange: [],
  expireDateRange: [],
  sortField: null,
  sortOrder: 'asc'
})

const currentContract = ref(null)

const auditDialogVisible = ref(false)
const auditForm = reactive({
  auditResult: 2,
  auditRemark: ''
})

const renewDialogVisible = ref(false)
const renewFormRef = ref(null)
const renewForm = reactive({
  newMonthlyRent: 0,
  newStartDate: '',
  newEndDate: '',
  remark: ''
})
const renewRules = {
  newMonthlyRent: [{ required: true, message: '请输入新的月租金', trigger: 'blur' }],
  newStartDate: [{ required: true, message: '请选择新开始日期', trigger: 'change' }],
  newEndDate: [{ required: true, message: '请选择新结束日期', trigger: 'change' }]
}

const terminateDialogVisible = ref(false)
const terminateFormRef = ref(null)
const terminateForm = reactive({
  terminateDate: '',
  terminateReason: '',
  remark: ''
})
const terminateRules = {
  terminateDate: [{ required: true, message: '请选择终止日期', trigger: 'change' }],
  terminateReason: [{ required: true, message: '请选择终止原因', trigger: 'change' }],
  remark: [{ required: true, message: '请输入备注说明', trigger: 'blur' }]
}

const hasListPermission = computed(() => userStore.hasPermission('park:contract:list'))
const hasAddPermission = computed(() => userStore.hasPermission('park:contract:add'))
const hasEditPermission = computed(() => userStore.hasPermission('park:contract:edit'))
const hasAuditPermission = computed(() => userStore.hasPermission('park:contract:audit'))
const hasRenewPermission = computed(() => userStore.hasPermission('park:contract:renew'))
const hasTerminatePermission = computed(() => userStore.hasPermission('park:contract:terminate'))

onMounted(() => {
  loadTenants()
  loadStats()
  loadList()
})

function loadTenants() {
  api.parkTenant.list({ pageSize: 1000 }).then(res => {
    tenantOptions.value = res.data.list || []
  })
}

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
    status: queryForm.status || undefined,
    leaseType: queryForm.leaseType || undefined,
    tenantId: queryForm.tenantId || undefined,
    sortField: queryForm.sortField || undefined,
    sortOrder: queryForm.sortOrder || undefined
  }

  if (queryForm.signDateRange && queryForm.signDateRange.length === 2) {
    params.signStartDate = queryForm.signDateRange[0]
    params.signEndDate = queryForm.signDateRange[1]
  }

  if (queryForm.expireDateRange && queryForm.expireDateRange.length === 2) {
    params.expireStartDate = queryForm.expireDateRange[0]
    params.expireEndDate = queryForm.expireDateRange[1]
  }

  api.parkLeaseContract.list(params).then(res => {
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function getStatusLabel(status) {
  const map = {
    1: '待审核',
    2: '有效',
    3: '已续租',
    4: '已终止',
    5: '已过期',
    6: '已拒绝'
  }
  return map[status] || '-'
}

function getStatusTagType(status) {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'primary',
    4: 'info',
    5: 'info',
    6: 'danger'
  }
  return map[status] || 'info'
}

function getLeaseTypeLabel(type) {
  const map = {
    1: '整租',
    2: '合租',
    3: '商铺',
    4: '办公',
    5: '仓库'
  }
  return map[type] || '-'
}

function getLeaseDays(startDate, endDate) {
  if (!startDate || !endDate) return ''
  const start = new Date(startDate)
  const end = new Date(endDate)
  const diff = end - start
  const days = Math.floor(diff / (1000 * 60 * 60 * 24)) + 1
  const months = Math.floor(days / 30)
  const remainingDays = days % 30
  if (months > 0 && remainingDays > 0) {
    return `${months}个月${remainingDays}天 (共${days}天)`
  } else if (months > 0) {
    return `${months}个月 (共${days}天)`
  } else {
    return `共${days}天`
  }
}

function formatMoney(value) {
  if (!value && value !== 0) return '-'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  queryForm.keyword = ''
  queryForm.status = null
  queryForm.leaseType = null
  queryForm.tenantId = null
  queryForm.signDateRange = []
  queryForm.expireDateRange = []
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

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleView(row) {
  router.push(`/park/contract/detail/${row.id}`)
}

function handleAdd() {
  router.push('/park/contract/add')
}

function handleEdit(row) {
  router.push(`/park/contract/edit/${row.id}`)
}

function handleAudit(row) {
  currentContract.value = row
  auditForm.auditResult = 2
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

function handleBatchAudit() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要审核的合同')
    return
  }
  ElMessageBox.confirm(`确定要批量审核选中的 ${selectedIds.value.length} 份合同吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkLeaseContract.audit({
      ids: selectedIds.value,
      auditResult: 2,
      auditRemark: '批量审核通过'
    }).then(() => {
      ElMessage.success('批量审核成功')
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function confirmAudit() {
  if (auditForm.auditResult === 6 && !auditForm.auditRemark.trim()) {
    ElMessage.warning('拒绝时请填写审核意见')
    return
  }
  api.parkLeaseContract.audit({
    id: currentContract.value.id,
    auditResult: auditForm.auditResult,
    auditRemark: auditForm.auditRemark
  }).then(() => {
    ElMessage.success('审核完成')
    auditDialogVisible.value = false
    loadList()
    loadStats()
  })
}

function handleRenew(row) {
  currentContract.value = row
  renewForm.newMonthlyRent = row.monthlyRent || 0
  renewForm.newStartDate = row.endDate || ''
  renewForm.newEndDate = ''
  renewForm.remark = ''
  renewDialogVisible.value = true
}

async function confirmRenew() {
  if (!renewFormRef.value) return
  try {
    await renewFormRef.value.validate()
    api.parkLeaseContract.renew({
      id: currentContract.value.id,
      newStartDate: renewForm.newStartDate,
      newEndDate: renewForm.newEndDate,
      newMonthlyRent: renewForm.newMonthlyRent,
      remark: renewForm.remark
    }).then(() => {
      ElMessage.success('续租成功')
      renewDialogVisible.value = false
      loadList()
      loadStats()
    })
  } catch (e) {
    return
  }
}

function handleTerminate(row) {
  currentContract.value = row
  terminateForm.terminateDate = ''
  terminateForm.terminateReason = ''
  terminateForm.remark = ''
  terminateDialogVisible.value = true
}

async function confirmTerminate() {
  if (!terminateFormRef.value) return
  try {
    await terminateFormRef.value.validate()
    ElMessageBox.confirm(`确定要终止合同 "${currentContract.value?.contractCode}" 吗？终止后不可恢复！`, '警告', {
      confirmButtonText: '确认终止',
      cancelButtonText: '取消',
      type: 'danger',
      confirmButtonClass: 'el-button--danger'
    }).then(() => {
      api.parkLeaseContract.terminate({
        id: currentContract.value.id,
        terminateDate: terminateForm.terminateDate,
        terminateReason: terminateForm.terminateReason,
        remark: terminateForm.remark
      }).then(() => {
        ElMessage.success('合同已终止')
        terminateDialogVisible.value = false
        loadList()
        loadStats()
      })
    }).catch(() => {})
  } catch (e) {
    return
  }
}

function handleCheckExpired() {
  ElMessageBox.confirm('确定要执行过期合同检查吗？系统会自动标记已到期的合同为过期状态。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkLeaseContract.checkExpired().then(res => {
      const count = res.data || 0
      if (count > 0) {
        ElMessage.success(`检查完成，共处理 ${count} 份过期合同`)
      } else {
        ElMessage.success('检查完成，没有发现需要处理的过期合同')
      }
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function handleExport() {
  const params = {
    keyword: queryForm.keyword || undefined,
    status: queryForm.status || undefined,
    leaseType: queryForm.leaseType || undefined,
    tenantId: queryForm.tenantId || undefined
  }

  if (queryForm.signDateRange && queryForm.signDateRange.length === 2) {
    params.signStartDate = queryForm.signDateRange[0]
    params.signEndDate = queryForm.signDateRange[1]
  }

  if (queryForm.expireDateRange && queryForm.expireDateRange.length === 2) {
    params.expireStartDate = queryForm.expireDateRange[0]
    params.expireEndDate = queryForm.expireDateRange[1]
  }

  api.parkLeaseContract.list({ ...params, pageSize: 10000 }).then(res => {
    const data = res.data.list || []
    exportToCSV(data)
  })
}

function exportToCSV(data) {
  const headers = ['合同编号', '租户名称', '房产编号', '租赁类型', '开始日期', '结束日期', '月租金(元)', '合同状态', '备注']

  const statusMap = { 1: '待审核', 2: '有效', 3: '已续租', 4: '已终止', 5: '已过期', 6: '已拒绝' }
  const leaseTypeMap = { 1: '整租', 2: '合租', 3: '商铺', 4: '办公', 5: '仓库' }

  const rows = data.map(item => [
    item.contractCode || '',
    item.tenantName || '',
    item.propertyCode || '',
    leaseTypeMap[item.leaseType] || '',
    item.startDate || '',
    item.endDate || '',
    item.monthlyRent || '',
    statusMap[item.status] || '',
    item.remark || ''
  ])

  const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `合同列表_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)

  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.contract-container {
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
</style>
