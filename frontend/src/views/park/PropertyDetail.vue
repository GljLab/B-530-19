<template>
  <div class="property-detail">
    <div class="detail-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-content">
      <div class="property-header">
        <div class="property-title">
          <h1>{{ property.propertyCode }}</h1>
          <el-tag :type="getStatusTagType(property.status)" size="large">
            {{ getStatusLabel(property.status) }}
          </el-tag>
          <el-tag v-if="property.status === 5 && property.vacancyDays" type="danger" size="large" style="margin-left: 8px;">
            已空置 {{ property.vacancyDays }} 天
          </el-tag>
          <el-icon v-if="property.isLongTermVacant === 1" style="color: #F56C6C; font-size: 24px; margin-left: 8px;" title="长期空置"><Warning /></el-icon>
        </div>
        <div class="property-actions">
          <el-button v-if="hasEditPermission" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑房产
          </el-button>
          <el-button v-if="hasChangeStatusPermission" type="warning" @click="handleChangeStatus">
            <el-icon><Promotion /></el-icon>修改状态
          </el-button>
          <el-button v-if="hasTransferPermission" type="success" @click="handleTransfer">
            <el-icon><Switch /></el-icon>发起转让
          </el-button>
          <el-button v-if="hasDeletePermission" type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>删除房产
          </el-button>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">基础信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="楼宇名称">{{ property.buildingName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所在楼层">{{ property.floorNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="房产类型">
                {{ getPropertyTypeLabel(property.propertyType, property.propertySubType) }}
              </el-descriptions-item>
              <el-descriptions-item label="房产状态">
                <el-tag :type="getStatusTagType(property.status)" size="small">
                  {{ getStatusLabel(property.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="建筑面积">{{ property.buildingArea || '-' }} ㎡</el-descriptions-item>
              <el-descriptions-item label="套内面积">{{ property.insideArea || '-' }} ㎡</el-descriptions-item>
              <el-descriptions-item label="公摊面积">{{ property.sharedArea || '-' }} ㎡</el-descriptions-item>
              <el-descriptions-item label="户型">{{ property.houseType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="朝向">{{ property.orientation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="装修状况">{{ property.decorationStatus || '-' }}</el-descriptions-item>
              <el-descriptions-item label="特殊标识">
                <div v-if="property.specialTagList && property.specialTagList.length > 0">
                  <el-tag v-for="tag in property.specialTagList" :key="tag" size="small" style="margin-right: 5px;">
                    {{ tag }}
                  </el-tag>
                </div>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="房产标签">
                <div v-if="property.tagList && property.tagList.length > 0">
                  <el-tag v-for="tag in property.tagList" :key="tag.id" size="small" :color="tag.tagColor" style="color: #fff; margin-right: 5px;">{{ tag.tagName }}</el-tag>
                </div>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">产权信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="产权性质">{{ property.propertyRightType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产权年限">
                {{ property.propertyRightYears ? property.propertyRightYears + '年' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="产权证号" :span="2">
                {{ property.propertyCertNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="交付日期" :span="2">
                {{ property.deliveryDate || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">使用情况</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getStatusTagType(property.status)" size="small">
                  {{ getStatusLabel(property.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="产权人">{{ property.ownerName || '待关联' }}</el-descriptions-item>
              <el-descriptions-item label="租户信息">{{ property.tenantName || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card" v-if="property.status === 5">
            <template #header>
              <span class="card-title">空置信息</span>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="空置天数">{{ property.vacancyDays || '-' }} 天</el-descriptions-item>
              <el-descriptions-item label="空置开始日期">{{ property.vacantSince || '-' }}</el-descriptions-item>
              <el-descriptions-item label="空置原因">{{ property.vacancyReason || '-' }}</el-descriptions-item>
              <el-descriptions-item label="空置原因备注">{{ property.vacancyReasonRemark || '-' }}</el-descriptions-item>
            </el-descriptions>
            <el-button v-if="hasVacancyPermission && !property.vacancyReason" type="primary" size="small" style="margin-top: 10px;" @click="handleSetVacancyReason">记录空置原因</el-button>
          </el-card>
          <el-card class="info-card" v-else>
            <template #header>
              <span class="card-title">备注信息</span>
            </template>
            <div class="remark-content">{{ property.remark || '暂无备注' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="info-card" style="margin-top: 20px;">
        <el-tabs v-model="historyTab">
          <el-tab-pane label="状态变更历史" name="status">
            <el-timeline>
              <el-timeline-item v-for="(log, index) in statusLogs" :key="log.id" :timestamp="log.createTime" :type="index === 0 ? 'primary' : ''" placement="top">
                <el-card shadow="never" class="log-card">
                  <h4>{{ getStatusLabel(log.newStatus) }}</h4>
                  <p v-if="log.oldStatus">由 <el-tag size="small" :type="getStatusTagType(log.oldStatus)">{{ getStatusLabel(log.oldStatus) }}</el-tag> 变更</p>
                  <p v-else>初始状态</p>
                  <p v-if="log.changeReason" class="reason">变更原因：{{ log.changeReason }}</p>
                  <p class="operator">操作人：{{ log.operatorName || '系统' }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="statusLogs.length === 0" class="empty-tip">暂无状态变更记录</div>
          </el-tab-pane>
          <el-tab-pane label="转让历史" name="transfer">
            <el-timeline>
              <el-timeline-item v-for="t in transferHistory" :key="t.id" :timestamp="t.transferDate" placement="top">
                <el-card shadow="never" class="log-card">
                  <h4>{{ getTransferTypeLabel(t.transferType) }}</h4>
                  <p>原业主：{{ t.oldOwnerName }} → 新业主：{{ t.newOwnerName }}</p>
                  <p v-if="t.transferPrice">转让价格：{{ t.transferPrice }} 万元</p>
                  <p class="operator">审核人：{{ t.auditorName || '-' }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="transferHistory.length === 0" class="empty-tip">暂无转让记录</div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <el-dialog v-model="editVisible" title="编辑房产" width="700px" @close="handleEditClose">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-tabs v-model="editActiveTab">
          <el-tab-pane label="房产属性" name="attr">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="建筑面积" prop="buildingArea">
                  <el-input-number v-model="editForm.buildingArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="套内面积" prop="insideArea">
                  <el-input-number v-model="editForm.insideArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="公摊面积" prop="sharedArea">
                  <el-input-number v-model="editForm.sharedArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="户型" prop="houseType">
                  <el-select v-model="editForm.houseType" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="一室" value="一室" />
                    <el-option label="两室" value="两室" />
                    <el-option label="三室" value="三室" />
                    <el-option label="四室及以上" value="四室及以上" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="朝向" prop="orientation">
                  <el-select v-model="editForm.orientation" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="东" value="东" />
                    <el-option label="南" value="南" />
                    <el-option label="西" value="西" />
                    <el-option label="北" value="北" />
                    <el-option label="东南" value="东南" />
                    <el-option label="西南" value="西南" />
                    <el-option label="东北" value="东北" />
                    <el-option label="西北" value="西北" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="装修状况" prop="decorationStatus">
                  <el-select v-model="editForm.decorationStatus" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="毛坯" value="毛坯" />
                    <el-option label="简装" value="简装" />
                    <el-option label="精装" value="精装" />
                    <el-option label="豪装" value="豪装" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="产权信息" name="right">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权性质" prop="propertyRightType">
                  <el-select v-model="editForm.propertyRightType" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="商品房" value="商品房" />
                    <el-option label="经济适用房" value="经济适用房" />
                    <el-option label="公租房" value="公租房" />
                    <el-option label="商铺产权" value="商铺产权" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="产权年限" prop="propertyRightYears">
                  <el-select v-model="editForm.propertyRightYears" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="40年" :value="40" />
                    <el-option label="50年" :value="50" />
                    <el-option label="70年" :value="70" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权证号" prop="propertyCertNo">
                  <el-input v-model="editForm.propertyCertNo" :disabled="!isManager" placeholder="请输入产权证号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="交付日期" prop="deliveryDate">
                  <el-date-picker v-model="editForm.deliveryDate" type="date" placeholder="选择日期" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="其他信息" name="other">
            <el-form-item label="特殊标识">
              <el-checkbox-group v-model="editForm.specialTagList">
                <el-checkbox label="学区房">学区房</el-checkbox>
                <el-checkbox label="人才房">人才房</el-checkbox>
                <el-checkbox label="拆迁房">拆迁房</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="editForm.remark" type="textarea" :rows="4" placeholder="请输入备注" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="修改状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(property.status)">{{ getStatusLabel(property.status) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" placeholder="请选择" style="width: 100%;">
            <el-option label="待售" :value="1" />
            <el-option label="已售未交付" :value="2" />
            <el-option label="业主自住" :value="3" />
            <el-option label="业主出租" :value="4" />
            <el-option label="空置" :value="5" />
            <el-option label="装修中" :value="6" />
            <el-option label="查封" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="changeReason" type="textarea" :rows="3" placeholder="请输入变更原因（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeStatus">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferDialogVisible" title="发起房产转让" width="600px">
      <el-form :model="transferForm" :rules="transferRules" ref="transferFormRef" label-width="100px">
        <el-form-item label="房产编号">
          <el-input :model-value="property.propertyCode" disabled />
        </el-form-item>
        <el-form-item label="原业主">
          <el-input :model-value="property.ownerName || '待关联'" disabled />
        </el-form-item>
        <el-form-item label="新业主" prop="newOwnerId">
          <el-select v-model="transferForm.newOwnerId" filterable placeholder="请选择业主" style="width: 100%;">
            <el-option v-for="o in ownerList" :key="o.id" :label="o.name + (o.phone ? ' (' + o.phone + ')' : '')" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转让方式" prop="transferType">
          <el-radio-group v-model="transferForm.transferType">
            <el-radio :value="1">买卖</el-radio>
            <el-radio :value="2">赠与</el-radio>
            <el-radio :value="3">继承</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="转让价格(万元)" prop="transferPrice" v-if="transferForm.transferType === 1">
          <el-input-number v-model="transferForm.transferPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="转让日期" prop="transferDate">
          <el-date-picker v-model="transferForm.transferDate" type="date" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="新产权证号">
          <el-input v-model="transferForm.newPropertyCertNo" placeholder="请输入新产权证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="transferLoading" @click="confirmTransfer">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="vacancyDialogVisible" title="记录空置原因" width="400px">
      <el-form :model="vacancyForm" label-width="80px">
        <el-form-item label="空置原因">
          <el-select v-model="vacancyForm.vacancyReason" placeholder="请选择" style="width: 100%;">
            <el-option label="装修中" value="装修中" />
            <el-option label="待出租" value="待出租" />
            <el-option label="待出售" value="待出售" />
            <el-option label="业主外地" value="业主外地" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" v-if="vacancyForm.vacancyReason === '其他'">
          <el-input v-model="vacancyForm.vacancyReasonRemark" type="textarea" :rows="3" placeholder="请输入其他原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vacancyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmVacancyReason">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete, Promotion, Switch, Warning } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const property = ref({})
const statusLogs = ref([])

const editVisible = ref(false)
const editActiveTab = ref('attr')
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  buildingArea: null,
  insideArea: null,
  sharedArea: null,
  houseType: '',
  orientation: '',
  decorationStatus: '',
  propertyRightType: '',
  propertyRightYears: null,
  propertyCertNo: '',
  deliveryDate: null,
  specialTagList: [],
  remark: ''
})

const editRules = {}

const statusDialogVisible = ref(false)
const newStatus = ref(null)
const changeReason = ref('')

const historyTab = ref('status')
const transferHistory = ref([])
const transferDialogVisible = ref(false)
const transferLoading = ref(false)
const transferFormRef = ref(null)
const ownerList = ref([])
const transferForm = reactive({
  newOwnerId: null,
  transferType: 1,
  transferPrice: null,
  transferDate: null,
  newPropertyCertNo: ''
})
const transferRules = {
  newOwnerId: [{ required: true, message: '请选择新业主', trigger: 'change' }],
  transferType: [{ required: true, message: '请选择转让方式', trigger: 'change' }],
  transferDate: [{ required: true, message: '请选择转让日期', trigger: 'change' }]
}
const vacancyDialogVisible = ref(false)
const vacancyForm = reactive({ vacancyReason: '', vacancyReasonRemark: '' })

const hasEditPermission = computed(() => userStore.hasPermission('park:property:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:property:delete'))
const hasChangeStatusPermission = computed(() => userStore.hasPermission('park:property:changeStatus'))
const isManager = computed(() => userStore.hasPermission('park:property:delete'))
const hasTransferPermission = computed(() => userStore.hasPermission('park:transfer:apply'))
const hasVacancyPermission = computed(() => userStore.hasPermission('park:property:setVacancy'))

onMounted(() => {
  loadDetail()
})

function loadDetail() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  api.parkProperty.getDetail(id).then(res => {
    property.value = res.data || {}
    statusLogs.value = res.data.statusLogs || []
    transferHistory.value = res.data.transferHistory || []
  }).finally(() => { loading.value = false })
}

function handleBack() {
  router.back()
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

function handleEdit() {
  Object.assign(editForm, property.value)
  editForm.specialTagList = property.value.specialTagList || []
  editActiveTab.value = 'attr'
  editVisible.value = true
}

function handleEditClose() {
  editFormRef.value?.resetFields()
}

function handleEditSubmit() {
  const submitData = { ...editForm }
  if (submitData.specialTagList && submitData.specialTagList.length > 0) {
    submitData.specialTags = submitData.specialTagList.join(',')
  }

  api.parkProperty.update(submitData).then(() => {
    ElMessage.success('修改成功')
    editVisible.value = false
    loadDetail()
  })
}

function handleDelete() {
  ElMessageBox.confirm(`确定要删除房产 "${property.value.propertyCode}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkProperty.delete(property.value.id).then(() => {
      ElMessage.success('删除成功')
      router.back()
    })
  }).catch(() => {})
}

function handleChangeStatus() {
  newStatus.value = null
  changeReason.value = ''
  statusDialogVisible.value = true
}

function confirmChangeStatus() {
  if (!newStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  api.parkProperty.changeStatus(property.value.id, newStatus.value, changeReason.value).then(() => {
    ElMessage.success('状态修改成功')
    statusDialogVisible.value = false
    loadDetail()
  })
}

function getTransferTypeLabel(type) {
  const map = { 1: '买卖', 2: '赠与', 3: '继承' }
  return map[type] || '-'
}

function handleTransfer() {
  Object.assign(transferForm, { newOwnerId: null, transferType: 1, transferPrice: null, transferDate: null, newPropertyCertNo: '' })
  api.parkOwner.list({ pageNum: 1, pageSize: 1000 }).then(res => {
    ownerList.value = (res.data.list || []).filter(o => o.id !== property.value.ownerId)
  })
  transferDialogVisible.value = true
}

function confirmTransfer() {
  transferFormRef.value.validate(valid => {
    if (!valid) return
    transferLoading.value = true
    api.parkPropertyTransfer.apply({
      propertyId: property.value.id,
      newOwnerId: transferForm.newOwnerId,
      transferType: transferForm.transferType,
      transferPrice: transferForm.transferPrice,
      transferDate: transferForm.transferDate,
      newPropertyCertNo: transferForm.newPropertyCertNo
    }).then(() => {
      ElMessage.success('转让申请已提交')
      transferDialogVisible.value = false
    }).finally(() => { transferLoading.value = false })
  })
}

function handleSetVacancyReason() {
  vacancyForm.vacancyReason = ''
  vacancyForm.vacancyReasonRemark = ''
  vacancyDialogVisible.value = true
}

function confirmVacancyReason() {
  if (!vacancyForm.vacancyReason) { ElMessage.warning('请选择空置原因'); return }
  api.parkPropertyVacancy.recordReason({
    propertyId: property.value.id,
    vacancyReason: vacancyForm.vacancyReason,
    vacancyReasonRemark: vacancyForm.vacancyReasonRemark
  }).then(() => {
    ElMessage.success('空置原因已记录')
    vacancyDialogVisible.value = false
    loadDetail()
  })
}
</script>

<style scoped lang="scss">
.property-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  .property-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .property-title {
      display: flex;
      align-items: center;
      gap: 16px;

      h1 {
        margin: 0;
        font-size: 28px;
        font-weight: bold;
        color: #303133;
      }
    }

    .property-actions {
      display: flex;
      gap: 10px;
    }
  }
}

.info-card {
  .card-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
  }

  .remark-content {
    min-height: 120px;
    padding: 10px;
    color: #606266;
    line-height: 1.6;
  }
}

.log-card {
  margin-bottom: 0;

  h4 {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: #303133;
  }

  p {
    margin: 4px 0;
    font-size: 13px;
    color: #606266;

    &.reason {
      color: #909399;
    }

    &.operator {
      color: #909399;
      font-size: 12px;
    }
  }
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}
</style>
