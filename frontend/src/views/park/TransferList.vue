<template>
  <div class="transfer-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" size="default">
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.auditStatus" placeholder="全部" clearable style="width: 140px;">
            <el-option label="待审核" :value="1" />
            <el-option label="审核通过" :value="2" />
            <el-option label="审核拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>房产转让列表</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="transferNo" label="转让编号" width="180" />
        <el-table-column prop="propertyCode" label="房产编号" width="120" />
        <el-table-column prop="oldOwnerName" label="原业主" width="100" />
        <el-table-column prop="newOwnerName" label="新业主" width="100" />
        <el-table-column label="转让方式" width="100">
          <template #default="{ row }">{{ getTransferTypeLabel(row.transferType) }}</template>
        </el-table-column>
        <el-table-column prop="transferPrice" label="转让价格(万元)" width="130" />
        <el-table-column prop="transferDate" label="转让日期" width="120" />
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)">{{ getAuditStatusLabel(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 20px; text-align: right;" v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadList" @current-change="loadList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="转让详情" width="600px">
      <el-descriptions :column="2" border v-if="currentTransfer">
        <el-descriptions-item label="转让编号" :span="2">{{ currentTransfer.transferNo }}</el-descriptions-item>
        <el-descriptions-item label="房产编号">{{ currentTransfer.propertyCode }}</el-descriptions-item>
        <el-descriptions-item label="楼宇">{{ currentTransfer.buildingName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原业主">{{ currentTransfer.oldOwnerName }}</el-descriptions-item>
        <el-descriptions-item label="新业主">{{ currentTransfer.newOwnerName }}</el-descriptions-item>
        <el-descriptions-item label="转让方式">{{ getTransferTypeLabel(currentTransfer.transferType) }}</el-descriptions-item>
        <el-descriptions-item label="转让价格">{{ currentTransfer.transferPrice ? currentTransfer.transferPrice + ' 万元' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="转让日期">{{ currentTransfer.transferDate }}</el-descriptions-item>
        <el-descriptions-item label="新产权证号">{{ currentTransfer.newPropertyCertNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(currentTransfer.auditStatus)">{{ getAuditStatusLabel(currentTransfer.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentTransfer.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ currentTransfer.auditorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentTransfer.auditTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentTransfer.auditOpinion || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  auditStatus: null
})
const detailVisible = ref(false)
const currentTransfer = ref(null)

function loadList() {
  loading.value = true
  api.parkPropertyTransfer.list(queryForm).then(res => {
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  queryForm.auditStatus = null
  handleSearch()
}

function handleView(row) {
  api.parkPropertyTransfer.getById(row.id).then(res => {
    currentTransfer.value = res.data
    detailVisible.value = true
  })
}

function getTransferTypeLabel(type) {
  const map = { 1: '买卖', 2: '赠与', 3: '继承' }
  return map[type] || '-'
}

function getAuditStatusLabel(status) {
  const map = { 1: '待审核', 2: '审核通过', 3: '审核拒绝' }
  return map[status] || '-'
}

function getAuditStatusType(status) {
  const map = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.transfer-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
