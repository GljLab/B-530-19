<template>
  <div class="contract-detail">
    <div class="detail-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-content">
      <div class="contract-header">
        <div class="contract-title">
          <el-avatar :size="64" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon style="font-size: 28px;"><Document /></el-icon>
          </el-avatar>
          <div class="contract-info">
            <h1>{{ contract.contractCode || '-' }}
              <span style="font-size: 16px; color: #909399; margin-left: 12px;">
                <el-tag :type="getContractStatusTagType(contract.contractStatus)" size="large" effect="dark">
                  {{ getContractStatusText(contract.contractStatus) }}
                </el-tag>
              </span>
            </h1>
            <div class="contract-subtitle">
              <span>签订日期：{{ contract.signDate || '-' }}</span>
              <span style="margin-left: 20px;">生效日期：{{ contract.effectiveDate || '-' }}</span>
            </div>
          </div>
        </div>
        <div class="contract-actions">
          <el-button v-if="hasEditPermission && contract.contractStatus === 1" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button v-if="hasRenewPermission && contract.contractStatus === 2" type="success" @click="handleRenew">
            <el-icon><Refresh /></el-icon>续签
          </el-button>
          <el-button v-if="hasTerminatePermission && contract.contractStatus === 2" type="danger" @click="handleTerminate">
            <el-icon><Close /></el-icon>终止
          </el-button>
          <el-button type="info" @click="handlePrint">
            <el-icon><Printer /></el-icon>打印
          </el-button>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">租赁双方</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="租户姓名" :span="2">
                <div class="party-info">
                  <el-avatar :size="36" style="margin-right: 12px;">
                    {{ contract.tenantName?.charAt(0) || '租' }}
                  </el-avatar>
                  <div>
                    <div style="font-weight: bold;">{{ contract.tenantName || '-' }}</div>
                    <div style="color: #909399; font-size: 12px;">
                      {{ contract.tenantType === 2 ? '企业租户' : '个人租户' }}
                    </div>
                  </div>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ contract.tenantPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="证件号码">{{ formatIdCard(contract.tenantIdCard) }}</el-descriptions-item>
              <el-descriptions-item label="业主姓名" :span="2">
                <div class="party-info">
                  <el-avatar :size="36" style="margin-right: 12px; background: #67c23a;">
                    {{ contract.ownerName?.charAt(0) || '业' }}
                  </el-avatar>
                  <div>
                    <div style="font-weight: bold;">{{ contract.ownerName || '-' }}</div>
                    <div style="color: #909399; font-size: 12px;">
                      {{ contract.ownerType === 2 ? '企业业主' : '个人业主' }}
                    </div>
                  </div>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="业主电话">{{ contract.ownerPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">房产信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="房产编号">{{ contract.propertyCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属楼宇">{{ contract.buildingName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所在楼层">{{ contract.floorNumber ? contract.floorNumber + ' 层' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="房屋户型">{{ contract.houseType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="建筑面积" :span="2">
                {{ contract.buildingArea ? contract.buildingArea + ' ㎡' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="房产地址" :span="2">{{ contract.propertyAddress || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">租赁信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="租赁类型">
                {{ getLeaseTypeText(contract.leaseType) }}
              </el-descriptions-item>
              <el-descriptions-item label="租赁期限">
                {{ contract.leaseMonths ? contract.leaseMonths + ' 个月' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="开始日期">{{ contract.leaseStartDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结束日期">{{ contract.leaseEndDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="月租金">
                <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">
                  {{ contract.monthlyRent ? '¥ ' + contract.monthlyRent : '-' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="押金">
                <span style="color: #e6a23c; font-weight: bold;">
                  {{ contract.deposit ? '¥ ' + contract.deposit : '-' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="付款方式">
                {{ getPaymentMethodText(contract.paymentMethod) }}
              </el-descriptions-item>
              <el-descriptions-item label="下次付款日">{{ contract.nextPaymentDate || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">费用信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="物业费">
                <el-tag :type="contract.propertyFeeIncluded ? 'success' : 'warning'" size="small">
                  {{ contract.propertyFeeIncluded ? '已包含' : '不包含' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="物业费金额">
                {{ contract.propertyFeeAmount ? '¥ ' + contract.propertyFeeAmount + '/月' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="水费承担">{{ getFeeBearerText(contract.waterFeeBearer) }}</el-descriptions-item>
              <el-descriptions-item label="电费承担">{{ getFeeBearerText(contract.electricFeeBearer) }}</el-descriptions-item>
              <el-descriptions-item label="燃气费承担">{{ getFeeBearerText(contract.gasFeeBearer) }}</el-descriptions-item>
              <el-descriptions-item label="网络费承担">{{ getFeeBearerText(contract.internetFeeBearer) }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">违约条款</span>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="提前终止违约金">
                <el-tag type="danger" size="small">
                  {{ contract.earlyTerminationPenalty ? contract.earlyTerminationPenalty + ' 个月租金' : '-' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="逾期付款违约金率">
                <el-tag type="warning" size="small">
                  {{ contract.latePaymentPenaltyRate ? contract.latePaymentPenaltyRate + '%/日' : '-' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="押金扣除规则">
                {{ contract.depositDeductRule || '无特殊约定' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">合同条款</span>
            </template>
            <div v-if="contract.termsText" class="terms-content">
              {{ contract.termsText }}
            </div>
            <div v-else-if="contract.termsFileUrl" class="terms-file">
              <el-icon style="font-size: 48px; color: #409EFF; margin-bottom: 10px;"><Document /></el-icon>
              <p style="margin-bottom: 15px; color: #606266;">合同条款文件已上传</p>
              <el-button type="primary" size="small" @click="handleDownloadTerms">
                <el-icon><Download /></el-icon>下载合同条款
              </el-button>
            </div>
            <div v-else class="empty-tip">
              暂无合同条款
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">附件资料（{{ attachments.length }}）</span>
            </template>
            <div v-if="attachments.length > 0">
              <el-table :data="attachments" size="small" border>
                <el-table-column prop="fileName" label="文件名称" show-overflow-tooltip />
                <el-table-column prop="fileSize" label="大小" width="100">
                  <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <el-button v-if="isImage(row.fileName)" size="small" type="primary" link @click="handlePreviewImage(row)">
                      预览
                    </el-button>
                    <el-button size="small" type="success" link @click="handleDownloadAttachment(row)">
                      下载
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="empty-tip">
              暂无附件
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">操作日志</span>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="(log, index) in operationLogs"
                :key="log.id"
                :timestamp="log.operationTime"
                :type="getLogType(log.operationType)"
                placement="top">
                <el-card shadow="never" class="log-card">
                  <h4>
                    <el-tag :type="getLogTagType(log.operationType)" size="small">
                      {{ getOperationTypeText(log.operationType) }}
                    </el-tag>
                  </h4>
                  <p v-if="log.operationRemark" class="log-remark">{{ log.operationRemark }}</p>
                  <p class="log-operator">操作人：{{ log.operatorName || '系统' }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="operationLogs.length === 0" class="empty-tip">
              暂无操作日志
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="renewDialogVisible" title="续签合同" width="600px">
      <el-form :model="renewForm" label-width="120px">
        <el-form-item label="原合同编号">
          <span>{{ contract?.contractCode }}</span>
        </el-form-item>
        <el-form-item label="租赁房产">
          <span>{{ contract.propertyCode }} - {{ contract.buildingName }}</span>
        </el-form-item>
        <el-form-item label="新合同开始日期" required>
          <el-date-picker
            v-model="renewForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择开始日期"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="新合同结束日期" required>
          <el-date-picker
            v-model="renewForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择结束日期"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="月租金（元）" required>
          <el-input-number v-model="renewForm.monthlyRent" :min="0" :precision="2" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="押金（元）">
          <el-input-number v-model="renewForm.deposit" :min="0" :precision="2" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="付款方式" required>
          <el-select v-model="renewForm.paymentMethod" placeholder="请选择" style="width: 100%;">
            <el-option label="月付" :value="1" />
            <el-option label="季付" :value="2" />
            <el-option label="半年付" :value="3" />
            <el-option label="年付" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="renewForm.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRenew">确定续签</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="terminateDialogVisible" title="终止合同" width="500px">
      <el-form :model="terminateForm" label-width="120px">
        <el-form-item label="合同编号">
          <span>{{ contract?.contractCode }}</span>
        </el-form-item>
        <el-form-item label="租赁房产">
          <span>{{ contract.propertyCode }} - {{ contract.buildingName }}</span>
        </el-form-item>
        <el-form-item label="原租赁期限">
          <span>{{ contract.leaseStartDate }} 至 {{ contract.leaseEndDate }}</span>
        </el-form-item>
        <el-form-item label="终止日期" required>
          <el-date-picker
            v-model="terminateForm.terminateDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择终止日期"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="终止原因" required>
          <el-select v-model="terminateForm.reasonType" placeholder="请选择" style="width: 100%;">
            <el-option label="正常到期" :value="1" />
            <el-option label="提前退租" :value="2" />
            <el-option label="违约解除" :value="3" />
            <el-option label="协商解除" :value="4" />
            <el-option label="其他原因" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" required>
          <el-input v-model="terminateForm.remark" type="textarea" :rows="3" placeholder="请输入详细说明" />
        </el-form-item>
        <el-form-item label="押金处理">
          <el-radio-group v-model="terminateForm.depositHandle">
            <el-radio :value="1">全额退还</el-radio>
            <el-radio :value="2">部分退还</el-radio>
            <el-radio :value="3">不予退还</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退还金额（元）" v-if="terminateForm.depositHandle === 2">
          <el-input-number v-model="terminateForm.refundAmount" :min="0" :precision="2" style="width: 200px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="terminateDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmTerminate">确定终止</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <img v-if="previewImageUrl" :src="previewImageUrl" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Edit, Refresh, Close, Printer,
  Document, Download
} from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const contract = ref({})
const attachments = ref([])
const operationLogs = ref([])

const renewDialogVisible = ref(false)
const renewForm = reactive({
  startDate: null,
  endDate: null,
  monthlyRent: null,
  deposit: null,
  paymentMethod: null,
  remark: ''
})

const terminateDialogVisible = ref(false)
const terminateForm = reactive({
  terminateDate: null,
  reasonType: null,
  remark: '',
  depositHandle: 1,
  refundAmount: 0
})

const previewVisible = ref(false)
const previewImageUrl = ref('')

const hasEditPermission = computed(() => userStore.hasPermission('park:leaseContract:edit'))
const hasRenewPermission = computed(() => userStore.hasPermission('park:leaseContract:renew'))
const hasTerminatePermission = computed(() => userStore.hasPermission('park:leaseContract:terminate'))
const hasQueryPermission = computed(() => userStore.hasPermission('park:leaseContract:query'))

onMounted(() => {
  loadDetail()
})

function loadDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  api.parkLeaseContract.getById(id).then(res => {
    contract.value = res.data || {}
    attachments.value = res.data.attachments || []
    operationLogs.value = res.data.operationLogs || []
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

function formatFileSize(size) {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

function isImage(fileName) {
  if (!fileName) return false
  const ext = fileName.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
}

function getContractStatusText(status) {
  const map = {
    1: '待审核',
    2: '执行中',
    3: '已到期',
    4: '已终止',
    5: '已作废'
  }
  return map[status] || '-'
}

function getContractStatusTagType(status) {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'danger',
    5: 'info'
  }
  return map[status] || 'info'
}

function getLeaseTypeText(type) {
  const map = {
    1: '整租',
    2: '合租',
    3: '单间'
  }
  return map[type] || '-'
}

function getPaymentMethodText(method) {
  const map = {
    1: '月付',
    2: '季付',
    3: '半年付',
    4: '年付'
  }
  return map[method] || '-'
}

function getFeeBearerText(bearer) {
  const map = {
    1: '租户承担',
    2: '业主承担',
    3: '双方分摊'
  }
  return map[bearer] || '-'
}

function getOperationTypeText(type) {
  const map = {
    1: '创建合同',
    2: '审核通过',
    3: '审核拒绝',
    4: '续签合同',
    5: '终止合同',
    6: '合同到期',
    7: '修改合同',
    8: '作废合同'
  }
  return map[type] || '-'
}

function getLogType(type) {
  const map = {
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'success',
    5: 'warning',
    6: 'info',
    7: 'warning',
    8: 'danger'
  }
  return map[type] || ''
}

function getLogTagType(type) {
  const map = {
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'success',
    5: 'warning',
    6: 'info',
    7: 'warning',
    8: 'danger'
  }
  return map[type] || 'info'
}

function handleBack() {
  router.back()
}

function handleEdit() {
  router.push(`/park/lease-contract/edit/${contract.value.id}`)
}

function handlePrint() {
  window.print()
}

function handleRenew() {
  renewForm.startDate = null
  renewForm.endDate = null
  renewForm.monthlyRent = contract.value?.monthlyRent || null
  renewForm.deposit = contract.value?.deposit || null
  renewForm.paymentMethod = contract.value?.paymentMethod || null
  renewForm.remark = ''
  renewDialogVisible.value = true
}

function confirmRenew() {
  if (!renewForm.startDate) {
    ElMessage.warning('请选择新合同开始日期')
    return
  }
  if (!renewForm.endDate) {
    ElMessage.warning('请选择新合同结束日期')
    return
  }
  if (!renewForm.monthlyRent) {
    ElMessage.warning('请填写月租金')
    return
  }
  if (!renewForm.paymentMethod) {
    ElMessage.warning('请选择付款方式')
    return
  }
  if (new Date(renewForm.startDate) >= new Date(renewForm.endDate)) {
    ElMessage.warning('结束日期必须晚于开始日期')
    return
  }

  const params = {
    originalContractId: contract.value.id,
    tenantId: contract.value.tenantId,
    propertyId: contract.value.propertyId,
    startDate: renewForm.startDate,
    endDate: renewForm.endDate,
    monthlyRent: renewForm.monthlyRent,
    deposit: renewForm.deposit,
    paymentMethod: renewForm.paymentMethod,
    remark: renewForm.remark
  }

  api.parkLeaseContract.renew(params).then(() => {
    ElMessage.success('续签成功')
    renewDialogVisible.value = false
    loadDetail()
  })
}

function handleTerminate() {
  terminateForm.terminateDate = null
  terminateForm.reasonType = null
  terminateForm.remark = ''
  terminateForm.depositHandle = 1
  terminateForm.refundAmount = 0
  terminateDialogVisible.value = true
}

function confirmTerminate() {
  if (!terminateForm.terminateDate) {
    ElMessage.warning('请选择终止日期')
    return
  }
  if (!terminateForm.reasonType) {
    ElMessage.warning('请选择终止原因')
    return
  }
  if (!terminateForm.remark.trim()) {
    ElMessage.warning('请输入详细说明')
    return
  }
  if (terminateForm.depositHandle === 2 && !terminateForm.refundAmount) {
    ElMessage.warning('请填写退还金额')
    return
  }

  ElMessageBox.confirm('确定要终止此合同吗？终止后将无法恢复。', '确认终止', {
    confirmButtonText: '确定终止',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const params = {
      contractId: contract.value.id,
      terminateDate: terminateForm.terminateDate,
      reasonType: terminateForm.reasonType,
      remark: terminateForm.remark,
      depositHandle: terminateForm.depositHandle,
      refundAmount: terminateForm.depositHandle === 2 ? terminateForm.refundAmount : null
    }

    api.parkLeaseContract.terminate(params).then(() => {
      ElMessage.success('合同已终止')
      terminateDialogVisible.value = false
      loadDetail()
    })
  }).catch(() => {})
}

function handlePreviewImage(row) {
  previewImageUrl.value = row.fileUrl
  previewVisible.value = true
}

function handleDownloadAttachment(row) {
  if (row.fileUrl) {
    window.open(row.fileUrl, '_blank')
  }
}

function handleDownloadTerms() {
  if (contract.value.termsFileUrl) {
    window.open(contract.value.termsFileUrl, '_blank')
  }
}
</script>

<style scoped lang="scss">
.contract-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  .contract-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .contract-title {
      display: flex;
      align-items: center;
      gap: 16px;

      .contract-info {
        h1 {
          margin: 0 0 8px 0;
          font-size: 22px;
          font-weight: bold;
        }

        .contract-subtitle {
          color: #909399;
          font-size: 13px;
        }
      }
    }

    .contract-actions {
      display: flex;
      gap: 8px;
    }
  }

  .info-card {
    .card-title {
      font-size: 16px;
      font-weight: bold;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .empty-tip {
      padding: 30px;
      text-align: center;
      color: #909399;
    }

    .party-info {
      display: flex;
      align-items: center;
    }

    .terms-content {
      padding: 15px;
      line-height: 1.8;
      color: #606266;
      background: #f5f7fa;
      border-radius: 4px;
      max-height: 200px;
      overflow-y: auto;
    }

    .terms-file {
      padding: 30px;
      text-align: center;
    }

    .log-card {
      :deep(h4) {
        margin: 0 0 10px 0;
      }

      :deep(p) {
        margin: 6px 0;
        color: #606266;
      }

      .log-remark {
        color: #E6A23C;
      }

      .log-operator {
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

@media print {
  .detail-header,
  .contract-actions {
    display: none !important;
  }

  .contract-detail {
    padding: 0;
  }
}
</style>
