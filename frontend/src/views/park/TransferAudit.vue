<template>
  <div class="audit-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" v-loading="loading" border stripe>
          <el-table-column prop="transferNo" label="转让编号" width="180" />
          <el-table-column prop="propertyCode" label="房产编号" width="120" />
          <el-table-column prop="oldOwnerName" label="原业主" width="100" />
          <el-table-column prop="newOwnerName" label="新业主" width="100" />
          <el-table-column label="转让方式" width="100">
            <template #default="{ row }">{{ getTransferTypeLabel(row.transferType) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button type="success" size="small" @click="handleAudit(row, 2)">通过</el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, 3)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="已审核" name="done">
        <el-table :data="doneList" v-loading="loading" border stripe>
          <el-table-column prop="transferNo" label="转让编号" width="180" />
          <el-table-column prop="propertyCode" label="房产编号" width="120" />
          <el-table-column prop="oldOwnerName" label="原业主" width="100" />
          <el-table-column prop="newOwnerName" label="新业主" width="100" />
          <el-table-column label="转让方式" width="100">
            <template #default="{ row }">{{ getTransferTypeLabel(row.transferType) }}</template>
          </el-table-column>
          <el-table-column label="审核状态" width="110">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)">{{ getAuditStatusLabel(row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditorName" label="审核人" width="100" />
          <el-table-column prop="auditTime" label="审核时间" width="170" />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

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
        <el-descriptions-item label="申请人">{{ currentTransfer.applyOperatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentTransfer.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(currentTransfer.auditStatus)">{{ getAuditStatusLabel(currentTransfer.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentTransfer.auditOpinion || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" :title="auditResult === 2 ? '审核通过' : '审核拒绝'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审核意见">
          <el-input v-model="auditOpinion" type="textarea" :rows="4" :placeholder="auditResult === 2 ? '请输入审核意见（选填）' : '请输入拒绝原因（必填）'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button :type="auditResult === 2 ? 'success' : 'danger'" :loading="auditLoading" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const activeTab = ref('pending')
const loading = ref(false)
const pendingList = ref([])
const doneList = ref([])
const detailVisible = ref(false)
const currentTransfer = ref(null)
const auditDialogVisible = ref(false)
const auditResult = ref(2)
const auditOpinion = ref('')
const auditLoading = ref(false)
const currentAuditId = ref(null)

const loadPendingList = async () => {
  loading.value = true
  try {
    const res = await api.parkPropertyTransfer.list({ pageNum: 1, pageSize: 100, auditStatus: 1 })
    pendingList.value = res.data?.records || res.data || []
  } finally {
    loading.value = false
  }
}

const loadDoneList = async () => {
  loading.value = true
  try {
    const res = await api.parkPropertyTransfer.list({ pageNum: 1, pageSize: 100 })
    const list = res.data?.records || res.data || []
    doneList.value = list.filter(item => item.auditStatus !== 1)
  } finally {
    loading.value = false
  }
}

const handleView = async (row) => {
  const res = await api.parkPropertyTransfer.getById(row.id)
  currentTransfer.value = res.data
  detailVisible.value = true
}

const handleAudit = (row, result) => {
  currentAuditId.value = row.id
  auditResult.value = result
  auditOpinion.value = ''
  auditDialogVisible.value = true
}

const confirmAudit = async () => {
  if (auditResult.value === 3 && !auditOpinion.value) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  auditLoading.value = true
  try {
    await api.parkPropertyTransfer.audit({
      transferId: currentAuditId.value,
      auditResult: auditResult.value,
      auditOpinion: auditOpinion.value
    })
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    if (activeTab.value === 'pending') {
      loadPendingList()
    } else {
      loadDoneList()
    }
  } finally {
    auditLoading.value = false
  }
}

const getTransferTypeLabel = (type) => {
  const map = { 1: '买卖', 2: '赠与', 3: '继承' }
  return map[type] || '-'
}

const getAuditStatusLabel = (status) => {
  const map = { 1: '待审核', 2: '审核通过', 3: '审核拒绝' }
  return map[status] || '-'
}

const getAuditStatusType = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

watch(activeTab, (val) => {
  if (val === 'pending') {
    loadPendingList()
  } else {
    loadDoneList()
  }
})

onMounted(() => {
  loadPendingList()
})
</script>

<style scoped>
.audit-container {
  padding: 20px;
}
</style>
