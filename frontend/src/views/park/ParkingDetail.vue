<template>
  <div class="parking-detail">
    <div class="detail-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <div v-loading="loading" class="detail-content">
      <div class="space-header">
        <div class="space-title">
          <h1>{{ space.spaceCode }}</h1>
          <el-tag :type="getStatusTagType(space.status)" size="large">
            {{ getStatusLabel(space.status) }}
          </el-tag>
          <el-tag v-if="space.spaceType === '充电车位'" type="success" size="large" style="margin-left: 8px;">
            <el-icon><Lightning /></el-icon>充电车位
          </el-tag>
          <el-tag v-if="space.spaceType === '无障碍车位'" type="warning" size="large" style="margin-left: 8px;">
            无障碍车位
          </el-tag>
        </div>
        <div class="space-actions">
          <el-button v-if="hasEditPermission" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑车位
          </el-button>
          <el-button v-if="hasChangeStatusPermission" type="warning" @click="handleChangeStatus">
            <el-icon><Promotion /></el-icon>变更状态
          </el-button>
          <el-button v-if="hasDeletePermission" type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>删除车位
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
              <el-descriptions-item label="车位编号">{{ space.spaceCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属区域">{{ space.area || '-' }}</el-descriptions-item>
              <el-descriptions-item label="车位类型">{{ space.spaceType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="使用状态">
                <el-tag :type="getStatusTagType(space.status)" size="small">
                  {{ getStatusLabel(space.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="尺寸(长×宽)">
                {{ space.length && space.width ? `${space.length}×${space.width}m` : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="朝向">{{ space.orientation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="位置描述" :span="2">{{ space.locationDesc || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">产权信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="产权归属">{{ space.propertyRight || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产权人">{{ space.ownerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产权证号" :span="2">{{ space.propertyCertNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="购买价格">{{ space.purchasePrice ? space.purchasePrice + ' 元' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="购买日期">{{ space.purchaseDate || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">收费信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="月租金">{{ space.monthlyRent || 0 }} 元/月</el-descriptions-item>
              <el-descriptions-item label="临时收费">{{ space.hourlyFee || 0 }} 元/小时</el-descriptions-item>
              <el-descriptions-item label="押金金额" :span="2">{{ space.deposit || 0 }} 元</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span class="card-title">使用情况</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getStatusTagType(space.status)" size="small">
                  {{ getStatusLabel(space.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="产权人">{{ space.ownerName || '待关联' }}</el-descriptions-item>
              <el-descriptions-item label="租户信息" :span="2">-</el-descriptions-item>
              <el-descriptions-item label="绑定车辆" :span="2">-</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <span class="card-title">备注信息</span>
        </template>
        <div class="remark-content">{{ space.remark || '暂无备注' }}</div>
      </el-card>

      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <span class="card-title">状态变更历史</span>
        </template>
        <el-timeline>
          <el-timeline-item v-for="(log, index) in statusLogs" :key="log.id" :timestamp="log.createTime" :type="index === 0 ? 'primary' : ''" placement="top">
            <el-card shadow="never" class="log-card">
              <h4>
                <el-tag size="small" :type="getStatusTagType(log.newStatus)">{{ getStatusLabel(log.newStatus) }}</el-tag>
              </h4>
              <p v-if="log.oldStatus">
                由 <el-tag size="small" :type="getStatusTagType(log.oldStatus)">{{ getStatusLabel(log.oldStatus) }}</el-tag> 变更
              </p>
              <p v-else>初始状态</p>
              <p v-if="log.changeReason" class="reason">变更原因：{{ log.changeReason }}</p>
              <p class="operator">操作人：{{ log.operatorName || '系统' }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div v-if="statusLogs.length === 0" class="empty-tip">暂无状态变更记录</div>
      </el-card>
    </div>

    <el-dialog v-model="statusDialogVisible" title="变更状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(space?.status)">{{ getStatusLabel(space?.status) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" placeholder="请选择" style="width: 100%;">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更理由">
          <el-input v-model="changeReason" type="textarea" :rows="3" placeholder="请输入变更理由（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Promotion, Delete, Lightning } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const hasEditPermission = computed(() => userStore.hasPermission('park:parking:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:parking:delete'))
const hasChangeStatusPermission = computed(() => userStore.hasPermission('park:parking:changeStatus'))

const loading = ref(false)
const space = ref({})
const statusLogs = ref([])

const statusOptions = [
  { label: '空闲', value: 1 },
  { label: '业主自用', value: 2 },
  { label: '已出租', value: 3 },
  { label: '临时占用', value: 4 },
  { label: '维护中', value: 5 },
  { label: '禁用', value: 6 }
]

const getStatusLabel = (status) => {
  const item = statusOptions.find(s => s.value === status)
  return item ? item.label : '-'
}

const getStatusTagType = (status) => {
  const types = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: '',
    5: 'info',
    6: 'danger'
  }
  return types[status] || 'info'
}

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await api.parkParking.getById(id)
    if (res.code === 200) {
      space.value = res.data
      statusLogs.value = res.data.statusLogs || []
    }
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/park/parking')
}

const handleEdit = () => {
  router.push(`/park/parking/edit/${space.value.id}`)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除车位【${space.value.spaceCode}】吗？`, '确认删除', { type: 'warning' })
    const res = await api.parkParking.delete(space.value.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.push('/park/parking')
    }
  } catch (e) {}
}

const statusDialogVisible = ref(false)
const newStatus = ref(null)
const changeReason = ref('')

const handleChangeStatus = () => {
  newStatus.value = null
  changeReason.value = ''
  statusDialogVisible.value = true
}

const confirmChangeStatus = async () => {
  if (!newStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  try {
    const res = await api.parkParking.changeStatus(space.value.id, newStatus.value, changeReason.value)
    if (res.code === 200) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      loadDetail()
    }
  } catch (e) {}
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.parking-detail {
  .detail-header {
    margin-bottom: 16px;
  }
  .space-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    .space-title {
      display: flex;
      align-items: center;
      h1 {
        margin: 0 12px 0 0;
        font-size: 24px;
        color: #303133;
      }
    }
    .space-actions {
      display: flex;
      gap: 8px;
    }
  }
  .info-card {
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
    .remark-content {
      min-height: 60px;
      color: #606266;
      line-height: 1.6;
    }
  }
  .log-card {
    h4 {
      margin: 0 0 8px 0;
      font-size: 14px;
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
}
</style>
