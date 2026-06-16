<template>
  <div class="tenant-detail">
    <div class="detail-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-content">
      <div class="tenant-header">
        <div class="tenant-title">
          <el-avatar :size="64">
            {{ tenant.name?.charAt(0) || '租' }}
          </el-avatar>
          <div class="tenant-info">
            <h1>{{ tenant.name || '-' }}
              <span style="font-size: 16px; color: #909399; margin-left: 12px;">{{ tenant.tenantCode }}</span>
            </h1>
            <div class="tenant-tags">
              <el-tag :type="getTenantTypeTagType(tenant.tenantType)" size="large">
                {{ getTenantTypeText(tenant.tenantType) }}
              </el-tag>
              <el-tag :type="getTenantStatusTagType(tenant.tenantStatus)" size="large">
                {{ getTenantStatusText(tenant.tenantStatus) }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="tenant-actions">
          <el-button v-if="hasEditPermission" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button v-if="hasEditPermission && tenant.tenantStatus !== 3" type="warning" @click="handleSetBlacklist(true)">
            <el-icon><Warning /></el-icon>加入黑名单
          </el-button>
          <el-button v-if="hasEditPermission && tenant.tenantStatus === 3" type="info" @click="handleSetBlacklist(false)">
            <el-icon><CircleCheck /></el-icon>移出黑名单
          </el-button>
          <el-button v-if="hasDeletePermission" type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">基本信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="租户姓名">{{ tenant.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="租户类型">
                {{ getTenantTypeText(tenant.tenantType) }}
              </el-descriptions-item>
              <el-descriptions-item label="性别">{{ tenant.gender === 1 ? '男' : tenant.gender === 2 ? '女' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ tenant.age || '-' }}</el-descriptions-item>
              <el-descriptions-item label="证件号码">{{ formatIdCard(tenant.idCard) }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ tenant.birthDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="民族">{{ tenant.nation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="婚姻状况">{{ tenant.maritalStatusText || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ tenant.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备用手机">{{ tenant.backupPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="电子邮箱">{{ tenant.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="微信号">{{ tenant.wechat || '-' }}</el-descriptions-item>
              <el-descriptions-item label="户籍地址" :span="2">{{ tenant.householdAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="现居住地址" :span="2">{{ tenant.currentAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工作单位">{{ tenant.workUnit || '-' }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ tenant.occupation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="紧急联系人">{{ tenant.emergencyContact || '-' }}</el-descriptions-item>
              <el-descriptions-item label="紧急联系电话">{{ tenant.emergencyPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card" v-if="tenant.tenantType === 2">
            <template #header>
              <span class="card-title">企业信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="企业名称">{{ tenant.enterpriseName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="统一社会信用代码">{{ tenant.enterpriseCreditCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业类型">{{ tenant.enterpriseType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册资本">{{ tenant.registeredCapital ? tenant.registeredCapital + ' 万元' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="法定代表人">{{ tenant.legalPersonName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="法人身份证号">{{ formatIdCard(tenant.legalPersonIdCard) }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ tenant.contactPerson || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系人职务">{{ tenant.contactPosition || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系人电话">{{ tenant.contactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业地址" :span="2">{{ tenant.enterpriseAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="营业执照" :span="2">
                <el-image v-if="tenant.businessLicenseUrl" :src="tenant.businessLicenseUrl" :preview-src-list="[tenant.businessLicenseUrl]" style="width: 160px; height: 100px;" fit="cover" />
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="info-card" v-else>
            <template #header>
              <span class="card-title">紧急联系人与职业信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="紧急联系人">{{ tenant.emergencyContact || '-' }}</el-descriptions-item>
              <el-descriptions-item label="关系">{{ tenant.emergencyRelation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ tenant.emergencyPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ tenant.occupation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工作单位" :span="2">{{ tenant.workUnit || '-' }}</el-descriptions-item>
              <el-descriptions-item label="月收入">{{ tenant.monthlyIncome ? tenant.monthlyIncome + ' 元' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="社保情况">{{ tenant.socialSecurityText || '-' }}</el-descriptions-item>
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
            <el-descriptions :column="2" border class="current-lease">
              <el-descriptions-item label="当前房产">{{ tenant.currentPropertyCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属楼宇">{{ tenant.currentBuildingName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="建筑面积">{{ tenant.currentBuildingArea ? tenant.currentBuildingArea + ' ㎡' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="房屋户型">{{ tenant.currentHouseType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="租赁开始日期">{{ tenant.currentLeaseStartDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="租赁结束日期">{{ tenant.currentLeaseEndDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="月租金">{{ tenant.currentMonthlyRent ? tenant.currentMonthlyRent + ' 元' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="押金">{{ tenant.currentDeposit ? tenant.currentDeposit + ' 元' : '-' }}</el-descriptions-item>
            </el-descriptions>

            <div class="section-title">历史租赁记录</div>
            <el-timeline>
              <el-timeline-item
                v-for="(record, index) in leaseHistory"
                :key="record.id"
                :timestamp="record.leasePeriod || ''"
                :type="index === 0 ? 'primary' : ''"
                placement="top">
                <el-card shadow="never" class="lease-card">
                  <h4>{{ record.propertyCode || '-' }} - {{ record.buildingName || '-' }}</h4>
                  <p><span class="label">建筑面积：</span>{{ record.buildingArea || '-' }} ㎡</p>
                  <p><span class="label">租赁期限：</span>{{ record.leaseStartDate || '-' }} 至 {{ record.leaseEndDate || '-' }}</p>
                  <p><span class="label">月租金：</span>{{ record.monthlyRent || '-' }} 元</p>
                  <p><span class="label">退租原因：</span>{{ record.terminateReason || '正常到期' }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="leaseHistory.length === 0" class="empty-tip">
              暂无历史租赁记录
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">合同信息</span>
                <div v-if="currentContract" class="card-actions">
                  <el-button v-if="hasViewContractPermission" size="small" type="primary" @click="handleViewContract">
                    查看详情
                  </el-button>
                  <el-button v-if="hasRenewContractPermission" size="small" type="success" @click="handleRenewContract">
                    <el-icon><Refresh /></el-icon>续签
                  </el-button>
                  <el-button v-if="hasTerminateContractPermission" size="small" type="danger" @click="handleTerminateContract">
                    <el-icon><Close /></el-icon>终止
                  </el-button>
                </div>
              </div>
            </template>
            <div v-if="currentContract">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="合同编号">{{ currentContract.contractNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="合同状态">
                  <el-tag :type="getContractStatusTagType(currentContract.contractStatus)" size="small">
                    {{ currentContract.contractStatusText || '-' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="签订日期">{{ currentContract.signDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="生效日期">{{ currentContract.effectiveDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="租赁期限">{{ currentContract.leaseStartDate || '-' }} 至 {{ currentContract.leaseEndDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="合同期限">{{ currentContract.leaseTerm || '-' }} 个月</el-descriptions-item>
                <el-descriptions-item label="月租金">{{ currentContract.monthlyRent ? currentContract.monthlyRent + ' 元' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="押金">{{ currentContract.deposit ? currentContract.deposit + ' 元' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="付款方式">{{ currentContract.paymentMethodText || '-' }}</el-descriptions-item>
                <el-descriptions-item label="下次付款日">{{ currentContract.nextPaymentDate || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <div v-else class="empty-tip">
              暂无当前有效的租赁合同
            </div>
          </el-card>

          <el-card class="info-card" style="margin-top: 20px;">
            <template #header>
              <span class="card-title">缴费记录</span>
            </template>
            <div class="reserved-tip">
              <el-icon><Tickets /></el-icon>
              <p>该模块预留用于展示缴费记录信息</p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">备注信息</span>
            </template>
            <div class="remark-content">
              {{ tenant.remark || '暂无备注' }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="renewDialogVisible" title="续签合同" width="600px">
      <el-form :model="renewForm" label-width="100px">
        <el-form-item label="原合同编号">
          <span>{{ currentContract?.contractNo }}</span>
        </el-form-item>
        <el-form-item label="租赁房产">
          <span>{{ tenant.currentPropertyCode }} - {{ tenant.currentBuildingName }}</span>
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
      <el-form :model="terminateForm" label-width="100px">
        <el-form-item label="合同编号">
          <span>{{ currentContract?.contractNo }}</span>
        </el-form-item>
        <el-form-item label="租赁房产">
          <span>{{ tenant.currentPropertyCode }} - {{ tenant.currentBuildingName }}</span>
        </el-form-item>
        <el-form-item label="原租赁期限">
          <span>{{ currentContract?.leaseStartDate }} 至 {{ currentContract?.leaseEndDate }}</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Edit, Delete, Warning, CircleCheck,
  Refresh, Close, Tickets
} from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tenant = ref({})
const leaseHistory = ref([])
const currentContract = ref(null)

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
  terminationPenalty: 0
})

const hasEditPermission = computed(() => userStore.hasPermission('park:tenant:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:tenant:delete'))
const hasViewContractPermission = computed(() => userStore.hasPermission('park:contract:query'))
const hasRenewContractPermission = computed(() => userStore.hasPermission('park:contract:renew'))
const hasTerminateContractPermission = computed(() => userStore.hasPermission('park:contract:terminate'))

onMounted(() => {
  loadDetail()
})

function loadDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  api.parkTenant.getById(id).then(res => {
    tenant.value = res.data || {}
    leaseHistory.value = res.data.leaseHistory || []
    currentContract.value = res.data.currentContract || null
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

function getTenantTypeText(type) {
  const map = { 1: '个人租户', 2: '企业租户' }
  return map[type] || '-'
}

function getTenantTypeTagType(type) {
  const map = { 1: 'primary', 2: 'warning' }
  return map[type] || ''
}

function getTenantStatusText(status) {
  const map = {
    1: '在租',
    2: '已退租',
    3: '黑名单'
  }
  return map[status] || '-'
}

function getTenantStatusTagType(status) {
  const map = {
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] || 'info'
}

function getContractStatusTagType(status) {
  const map = {
    1: 'primary',
    2: 'success',
    3: 'info',
    4: 'warning',
    5: 'danger'
  }
  return map[status] || 'info'
}

function handleBack() {
  router.back()
}

function handleEdit() {
  router.push(`/park/tenant/edit/${tenant.value.id}`)
}

function handleDelete() {
  ElMessageBox.confirm(`确定要删除租户 "${tenant.value.name || tenant.value.tenantCode}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkTenant.delete(tenant.value.id).then(() => {
      ElMessage.success('删除成功')
      router.back()
    })
  }).catch(() => {})
}

function handleSetBlacklist(isBlacklist) {
  const action = isBlacklist ? '加入黑名单' : '移出黑名单'
  ElMessageBox.confirm(`确定要将租户 "${tenant.value.name || tenant.value.tenantCode}" ${action}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkTenant.setBlacklist({
      id: tenant.value.id,
      isBlacklist: isBlacklist,
      reason: ''
    }).then(() => {
      ElMessage.success(`${action}成功`)
      loadDetail()
    })
  }).catch(() => {})
}

function handleViewContract() {
  if (currentContract.value) {
    router.push(`/park/lease-contract/detail/${currentContract.value.id}`)
  }
}

function handleRenewContract() {
  renewForm.startDate = null
  renewForm.endDate = null
  renewForm.monthlyRent = currentContract.value?.monthlyRent || null
  renewForm.deposit = currentContract.value?.depositAmount || null
  renewForm.paymentMethod = currentContract.value?.paymentMethod || null
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
    contractId: currentContract.value.id,
    newStartDate: renewForm.startDate,
    newEndDate: renewForm.endDate,
    newMonthlyRent: renewForm.monthlyRent,
    newDepositAmount: renewForm.deposit
  }

  api.parkLeaseContract.renew(params).then(() => {
    ElMessage.success('续签成功')
    renewDialogVisible.value = false
    loadDetail()
  })
}

function handleTerminateContract() {
  terminateForm.terminateDate = null
  terminateForm.reasonType = null
  terminateForm.remark = ''
  terminateForm.terminationPenalty = 0
  terminateDialogVisible.value = true
}

function confirmTerminate() {
  if (!terminateForm.terminateDate) {
    ElMessage.warning('请选择解除日期')
    return
  }
  if (!terminateForm.reasonType) {
    ElMessage.warning('请选择解除原因')
    return
  }
  if (!terminateForm.remark.trim()) {
    ElMessage.warning('请输入详细说明')
    return
  }

  ElMessageBox.confirm('确定要申请解除此合同吗？解除申请提交后需审核通过才生效。', '确认解除', {
    confirmButtonText: '提交申请',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const params = {
      contractId: currentContract.value.id,
      terminationDate: terminateForm.terminateDate,
      terminationReason: terminateForm.remark,
      terminationPenalty: terminateForm.terminationPenalty || 0
    }

    api.parkLeaseContract.terminate(params).then(() => {
      ElMessage.success('解除申请已提交')
      terminateDialogVisible.value = false
      loadDetail()
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.tenant-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  .tenant-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .tenant-title {
      display: flex;
      align-items: center;
      gap: 16px;

      .tenant-info {
        h1 {
          margin: 0 0 8px 0;
          font-size: 22px;
          font-weight: bold;
        }

        .tenant-tags {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }

    .tenant-actions {
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

      .card-actions {
        display: flex;
        gap: 8px;
      }
    }

    .empty-tip {
      padding: 30px;
      text-align: center;
      color: #909399;
    }

    .remark-content {
      padding: 10px;
      line-height: 1.8;
      color: #606266;
      min-height: 80px;
    }

    .section-title {
      margin: 20px 0 15px 0;
      font-size: 14px;
      font-weight: bold;
      color: #303133;
      padding-left: 10px;
      border-left: 3px solid #409EFF;
    }

    .current-lease {
      margin-bottom: 10px;
    }

    .lease-card {
      :deep(h4) {
        margin: 0 0 10px 0;
        font-size: 14px;
        color: #303133;
      }

      :deep(p) {
        margin: 4px 0;
        color: #606266;
        font-size: 13px;

        .label {
          color: #909399;
        }
      }
    }

    .reserved-tip {
      padding: 40px 20px;
      text-align: center;
      color: #909399;

      :deep(.el-icon) {
        font-size: 48px;
        margin-bottom: 15px;
        color: #C0C4CC;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
}
</style>
