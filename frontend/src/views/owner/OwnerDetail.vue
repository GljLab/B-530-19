<template>
  <div class="owner-detail">
    <div class="detail-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-content">
      <div class="owner-header">
        <div class="owner-title">
          <el-avatar :size="64">
            {{ owner.name?.charAt(0) || '业' }}
          </el-avatar>
          <div class="owner-info">
            <h1>{{ owner.name || '-' }}
              <span style="font-size: 16px; color: #909399; margin-left: 12px;">{{ owner.ownerCode }}</span>
            </h1>
            <div class="owner-tags">
              <el-tag :type="getOwnerTypeTagType(owner.ownerType)" size="large">
                {{ getOwnerTypeText(owner.ownerType) }}
              </el-tag>
              <el-tag :type="getAuditTagType(owner.authStatus)" size="large">
                {{ getAuditStatusLabel(owner.authStatus) }}
              </el-tag>
              <el-tag v-for="tag in owner.ownerTagList" :key="tag" style="margin-left: 6px;" size="large" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="owner-actions">
          <el-button v-if="hasEditPermission" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑业主
          </el-button>
          <el-button v-if="hasAuditPermission && owner.authStatus === 1" type="warning" @click="handleAudit">
            <el-icon><Promotion /></el-icon>审核认证
          </el-button>
          <el-button v-if="hasBindPropertyPermission" type="success" @click="handleBindProperty">
            <el-icon><Link /></el-icon>关联房产
          </el-button>
          <el-button v-if="hasDeletePermission" type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>删除业主
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
              <el-descriptions-item label="业主姓名">{{ owner.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="业主类型">
                {{ getOwnerTypeText(owner.ownerType) }}
              </el-descriptions-item>
              <el-descriptions-item label="性别">{{ owner.gender === 1 ? '男' : owner.gender === 2 ? '女' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ owner.age || '-' }}</el-descriptions-item>
              <el-descriptions-item label="证件号码">{{ formatIdCard(owner.idCard) }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ owner.birthDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="民族">{{ owner.nation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="婚姻状况">{{ owner.maritalStatusText || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ owner.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备用手机">{{ owner.backupPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="电子邮箱">{{ owner.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="微信号">{{ owner.wechat || '-' }}</el-descriptions-item>
              <el-descriptions-item label="户籍地址" :span="2">{{ owner.householdAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="现居住地址" :span="2">{{ owner.currentAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工作单位">{{ owner.workUnit || '-' }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ owner.occupation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="家庭人口数">{{ owner.familyCount || '-' }}</el-descriptions-item>
              <el-descriptions-item label="入住状态">{{ owner.occupancyStatusText || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card" v-if="owner.ownerType === 2">
            <template #header>
              <span class="card-title">企业信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="企业名称">{{ owner.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="统一社会信用代码">{{ owner.enterpriseCreditCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业类型">{{ owner.enterpriseType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册资本">{{ owner.registeredCapital ? owner.registeredCapital + ' 万元' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="法定代表人">{{ owner.legalPersonName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="法人身份证号">{{ formatIdCard(owner.legalPersonIdCard) }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ owner.contactPerson || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系人职务">{{ owner.contactPosition || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系人电话">{{ owner.contactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业地址" :span="2">{{ owner.enterpriseAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="营业执照" :span="2">
                <el-image v-if="owner.businessLicenseUrl" :src="owner.businessLicenseUrl" :preview-src-list="[owner.businessLicenseUrl]" style="width: 160px; height: 100px;" fit="cover" />
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="info-card" v-else>
            <template #header>
              <span class="card-title">家庭成员与紧急联系人</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="家庭人口数">{{ owner.familyCount || '-' }}</el-descriptions-item>
              <el-descriptions-item label="入住状态">{{ owner.occupancyStatusText || '-' }}</el-descriptions-item>
              <el-descriptions-item label="紧急联系人">{{ owner.emergencyContact || '-' }}</el-descriptions-item>
              <el-descriptions-item label="关系">{{ owner.emergencyRelation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ owner.emergencyPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ owner.occupation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="工作单位" :span="2">{{ owner.workUnit || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">认证信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="认证状态">
                <el-tag :type="getAuditTagType(owner.authStatus)" size="small">
                  {{ getAuditStatusLabel(owner.authStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="业主编号">{{ owner.ownerCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="认证拒绝原因" :span="2">{{ owner.authRejectReason || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ owner.createTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ owner.updateTime || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">备注信息</span>
            </template>
            <div class="remark-content">
              {{ owner.remark || '暂无备注' }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span class="card-title">关联房产（{{ propertyList.length }}套）</span>
            <el-button v-if="hasBindPropertyPermission" size="small" type="primary" @click="handleBindProperty">
              <el-icon><Plus /></el-icon>关联房产
            </el-button>
          </div>
        </template>
        <el-table :data="propertyList" border stripe>
          <el-table-column prop="propertyCode" label="房产编号" width="130" />
          <el-table-column prop="buildingName" label="楼宇" width="120" />
          <el-table-column prop="floorNumber" label="楼层" width="80" />
          <el-table-column label="建筑面积(㎡)" width="120">
            <template #default="{ row }">{{ row.buildingArea || '-' }}</template>
          </el-table-column>
          <el-table-column prop="houseType" label="户型" width="100" />
          <el-table-column label="产权关系" width="100">
            <template #default="{ row }">{{ row.propertyRightTypeText || '-' }}</template>
          </el-table-column>
          <el-table-column label="产权比例" width="100">
            <template #default="{ row }">{{ row.propertyRightRatio ? row.propertyRightRatio + '%' : '-' }}</template>
          </el-table-column>
          <el-table-column label="取得方式" width="100">
            <template #default="{ row }">{{ row.acquireTypeText || '-' }}</template>
          </el-table-column>
          <el-table-column label="入住日期" width="120">
            <template #default="{ row }">{{ row.moveInDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="是否自住" width="90">
            <template #default="{ row }">{{ row.isSelfOccupyText || '-' }}</template>
          </el-table-column>
          <el-table-column label="关联状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.relationStatus === 1 ? 'success' : (row.relationStatus === 2 ? 'info' : 'danger')" size="small">
                {{ row.relationStatusText || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button v-if="hasUnbindPropertyPermission && row.relationStatus === 1" type="danger" size="small" @click="handleUnbindProperty(row)">
                解除关联
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="propertyList.length === 0" class="empty-tip">
          暂无关联房产
        </div>
      </el-card>

      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <span class="card-title">审核记录</span>
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in auditLogs"
            :key="log.id"
            :timestamp="log.createTime"
            :type="index === 0 ? 'primary' : ''"
            placement="top">
            <el-card shadow="never" class="log-card">
              <h4>
                <el-tag :type="getAuditTagType(log.auditResult)" size="small">
                  {{ log.auditResultText }}
                </el-tag>
              </h4>
              <p v-if="log.auditOpinion" class="reason">审核意见：{{ log.auditOpinion }}</p>
              <p v-if="log.rejectReason" class="reason">拒绝原因：{{ log.rejectReason }}</p>
              <p class="operator">操作人：{{ log.operatorName || '系统' }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div v-if="auditLogs.length === 0" class="empty-tip">
          暂无审核记录
        </div>
      </el-card>
    </div>

    <el-dialog v-model="auditDialogVisible" title="审核认证" width="500px">
      <el-form label-width="100px">
        <el-form-item label="业主姓名">
          <span>{{ owner?.name }}</span>
        </el-form-item>
        <el-form-item label="业主编号">
          <span>{{ owner?.ownerCode }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getAuditTagType(owner?.authStatus)">
            {{ getAuditStatusLabel(owner?.authStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditResult">
            <el-radio :value="2">通过</el-radio>
            <el-radio :value="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" required>
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（拒绝时必填原因）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindDialogVisible" title="关联房产" width="600px" @close="handleBindClose">
      <el-form label-width="100px">
        <el-form-item label="选择房产" required>
          <el-select v-model="selectedPropertyId" placeholder="请选择要关联的房产" style="width: 100%;" filterable>
            <el-option 
              v-for="p in unboundPropertyList" 
              :key="p.id" 
              :label="`${p.propertyCode} - ${p.buildingName || ''} ${p.floorNumber || ''}层 ${p.houseType || ''} ${p.buildingArea || ''}㎡`" 
              :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产权关系" required>
          <el-radio-group v-model="propertyRightType">
            <el-radio :value="1">完全产权</el-radio>
            <el-radio :value="2">共有产权</el-radio>
            <el-radio :value="3">使用权</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="产权比例" v-if="propertyRightType === 2" required>
          <el-input-number v-model="propertyRightRatio" :min="1" :max="100" style="width: 100px;" />
          <span style="margin-left: 10px;">%（所有共有人比例之和需等于100%）</span>
        </el-form-item>
        <el-form-item label="产权证号">
          <el-input v-model="propertyCertNo" placeholder="请输入产权证号（可选）" />
        </el-form-item>
        <el-form-item label="取得方式">
          <el-select v-model="acquireType" placeholder="请选择" style="width: 100%;">
            <el-option label="购买" :value="1" />
            <el-option label="继承" :value="2" />
            <el-option label="赠与" :value="3" />
            <el-option label="拆迁" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="取得日期">
          <el-date-picker v-model="acquireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker v-model="moveInDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="是否自住">
          <el-radio-group v-model="isSelfOccupy">
            <el-radio :value="1">是（自住）</el-radio>
            <el-radio :value="0">否（出租或空置）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="购买价格">
          <el-input-number v-model="purchasePrice" :min="0" :precision="2" style="width: 150px;" />
          <span style="margin-left: 10px;">万元（可选）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBind">确定关联</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete, Promotion, Link, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const owner = ref({})
const propertyList = ref([])
const auditLogs = ref([])

const auditDialogVisible = ref(false)
const auditResult = ref(2)
const auditRemark = ref('')

const bindDialogVisible = ref(false)
const unboundPropertyList = ref([])
const selectedPropertyId = ref(null)
const propertyRightType = ref(1)
const propertyRightRatio = ref(100)
const propertyCertNo = ref('')
const acquireType = ref(1)
const acquireDate = ref(null)
const moveInDate = ref(null)
const isSelfOccupy = ref(1)
const purchasePrice = ref(null)

const hasEditPermission = computed(() => userStore.hasPermission('park:owner:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:owner:delete'))
const hasAuditPermission = computed(() => userStore.hasPermission('park:owner:audit'))
const hasBindPropertyPermission = computed(() => userStore.hasPermission('park:owner:bindProperty'))
const hasUnbindPropertyPermission = computed(() => userStore.hasPermission('park:owner:unbindProperty'))

onMounted(() => {
  loadDetail()
})

function loadDetail() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  api.parkOwner.getById(id).then(res => {
    owner.value = res.data || {}
    propertyList.value = res.data.propertyList || []
    auditLogs.value = res.data.auditLogs || []
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

function getOwnerTypeText(type) {
  const map = { 1: '个人业主', 2: '企业业主', 3: '投资业主' }
  return map[type] || '-'
}

function getOwnerTypeTagType(type) {
  const map = { 1: 'primary', 2: 'warning', 3: 'success' }
  return map[type] || ''
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

function handleBack() {
  router.back()
}

function handleEdit() {
  router.push(`/park/owner/edit/${owner.value.id}`)
}

function handleDelete() {
  ElMessageBox.confirm(`确定要删除业主 "${owner.value.name || owner.value.ownerCode}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkOwner.delete(owner.value.id).then(() => {
      ElMessage.success('删除成功')
      router.back()
    })
  }).catch(() => {})
}

function handleAudit() {
  auditResult.value = 2
  auditRemark.value = ''
  auditDialogVisible.value = true
}

function confirmAudit() {
  if (auditResult.value === 3 && !auditRemark.value.trim()) {
    ElMessage.warning('拒绝时请填写审核意见（作为拒绝原因）')
    return
  }
  if (auditResult.value === 2 && !auditRemark.value.trim()) {
    auditRemark.value = '审核通过'
  }
  api.parkOwner.audit({
    ownerId: owner.value.id,
    auditResult: auditResult.value,
    auditOpinion: auditRemark.value,
    rejectReason: auditResult.value === 3 ? auditRemark.value : null
  }).then(() => {
    ElMessage.success('审核完成')
    auditDialogVisible.value = false
    loadDetail()
  })
}

function handleBindProperty() {
  selectedPropertyId.value = null
  propertyRightType.value = 1
  propertyRightRatio.value = 100
  propertyCertNo.value = ''
  acquireType.value = 1
  acquireDate.value = null
  moveInDate.value = null
  isSelfOccupy.value = 1
  purchasePrice.value = null
  api.property.list({ pageNum: 1, pageSize: 500 }).then(res => {
    unboundPropertyList.value = res.data.list || []
    bindDialogVisible.value = true
  })
}

function handleBindClose() {
  unboundPropertyList.value = []
}

function confirmBind() {
  if (!selectedPropertyId.value) {
    ElMessage.warning('请选择要关联的房产')
    return
  }
  const params = {
    ownerId: owner.value.id,
    propertyId: selectedPropertyId.value,
    propertyRightType: propertyRightType.value,
    propertyRightRatio: propertyRightType.value === 2 ? propertyRightRatio.value : 100,
    propertyCertNo: propertyCertNo.value || null,
    acquireType: acquireType.value,
    acquireDate: acquireDate.value || null,
    moveInDate: moveInDate.value || null,
    isSelfOccupy: isSelfOccupy.value,
    purchasePrice: purchasePrice.value || null
  }
  api.parkOwner.bindProperty(params).then(() => {
    ElMessage.success('关联成功')
    bindDialogVisible.value = false
    loadDetail()
  })
}

function handleUnbindProperty(row) {
  ElMessageBox.confirm(`确定要解除与房产 "${row.propertyCode}" 的关联吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkOwner.unbindProperty({
      relationId: row.id,
      id: row.id,
      propertyId: row.id
    }).then(() => {
      ElMessage.success('解除关联成功')
      loadDetail()
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.owner-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  .owner-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .owner-title {
      display: flex;
      align-items: center;
      gap: 16px;

      .owner-info {
        h1 {
          margin: 0 0 8px 0;
          font-size: 22px;
          font-weight: bold;
        }

        .owner-tags {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }

    .owner-actions {
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

    .remark-content {
      padding: 10px;
      line-height: 1.8;
      color: #606266;
      min-height: 80px;
    }

    .log-card {
      :deep(h4) {
        margin: 0 0 10px 0;
      }

      :deep(p) {
        margin: 6px 0;
        color: #606266;
      }

      .reason {
        color: #E6A23C;
      }

      .operator {
        color: #909399;
        font-size: 12px;
      }
    }
  }
}
</style>
