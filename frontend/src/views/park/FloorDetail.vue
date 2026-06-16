<template>
  <div class="floor-detail-container" v-loading="loading">
    <el-card class="header-card">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>返回
          </el-button>
          <span class="floor-title">{{ detail.floorNumber ? detail.floorNumber + ' 层' : '-' }}</span>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header-title">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="所属楼宇">{{ detail.buildingName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="楼层号">{{ detail.floorNumber || '-' }} 层</el-descriptions-item>
            <el-descriptions-item label="楼层类型">
              <el-tag :type="getFloorTypeTagType(detail.floorType)" size="small">
                {{ getFloorTypeLabel(detail.floorType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="楼层用途">{{ detail.floorPurpose || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房产数量">{{ detail.propertyCount || 0 }} 套</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detail.status)" size="small">
                {{ getStatusLabel(detail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注">
              {{ detail.remark || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="plan-card">
          <template #header>
            <div class="card-header-title">
              <el-icon><Picture /></el-icon>
              <span>楼层平面图</span>
            </div>
          </template>
          <div class="plan-content">
            <el-image
              v-if="detail.floorPlan"
              :src="detail.floorPlan"
              fit="contain"
              class="plan-image"
              :preview-src-list="[detail.floorPlan]"
            >
              <template #error>
                <div class="plan-placeholder">
                  <el-icon :size="64" color="#c0c4cc"><Picture /></el-icon>
                  <p>图片加载失败</p>
                </div>
              </template>
            </el-image>
            <div v-else class="plan-placeholder">
              <el-icon :size="64" color="#c0c4cc"><PictureFilled /></el-icon>
              <p>暂无平面图</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="property-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header-title">
          <el-icon><OfficeBuilding /></el-icon>
          <span>房产列表</span>
        </div>
      </template>
      <div class="property-placeholder">
        <el-icon :size="80" color="#e4e7ed"><Setting /></el-icon>
        <p class="placeholder-text">功能开发中...</p>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑楼层" width="500px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="楼层号" prop="floorNumber">
          <el-input-number v-model="editForm.floorNumber" :min="-10" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="楼层类型" prop="floorType">
          <el-radio-group v-model="editForm.floorType">
            <el-radio :label="1">住宅楼层</el-radio>
            <el-radio :label="2">商业楼层</el-radio>
            <el-radio :label="3">停车楼层</el-radio>
            <el-radio :label="4">设备楼层</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="楼层用途" prop="floorPurpose">
          <el-input v-model="editForm.floorPurpose" placeholder="请输入楼层用途" />
        </el-form-item>
        <el-form-item label="房产数量" prop="propertyCount">
          <el-input-number v-model="editForm.propertyCount" :min="0" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">改造中</el-radio>
            <el-radio :label="3">封闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  InfoFilled,
  Picture,
  PictureFilled,
  OfficeBuilding,
  Setting
} from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const editDialogVisible = ref(false)
const editFormRef = ref(null)

const floorId = computed(() => route.params.id)

const detail = ref({})

const editForm = reactive({
  id: null,
  buildingId: null,
  floorNumber: 1,
  floorType: 1,
  floorPurpose: '',
  propertyCount: 0,
  status: 1,
  remark: ''
})

const editRules = {
  floorNumber: [{ required: true, message: '请输入楼层号', trigger: 'blur' }],
  floorType: [{ required: true, message: '请选择楼层类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getFloorTypeLabel = (type) => {
  const typeMap = {
    1: '住宅楼层',
    2: '商业楼层',
    3: '停车楼层',
    4: '设备楼层'
  }
  return typeMap[type] || '未知'
}

const getFloorTypeTagType = (type) => {
  const typeMap = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: 'info'
  }
  return typeMap[type] || 'info'
}

const getStatusLabel = (status) => {
  const statusMap = {
    1: '正常',
    2: '改造中',
    3: '封闭'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return statusMap[status] || 'info'
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await api.parkFloor.getById(floorId.value)
    detail.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载楼层详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.back()
}

const handleEdit = () => {
  Object.assign(editForm, {
    id: detail.value.id,
    buildingId: detail.value.buildingId,
    floorNumber: detail.value.floorNumber || 1,
    floorType: detail.value.floorType || 1,
    floorPurpose: detail.value.floorPurpose || '',
    propertyCount: detail.value.propertyCount || 0,
    status: detail.value.status || 1,
    remark: detail.value.remark || ''
  })
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await api.parkFloor.update(editForm)
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      loadDetail()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.floor-detail-container {
  padding: 0;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.floor-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.info-card {
  height: 100%;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #303133;
}

.plan-card {
  height: 100%;
}

.plan-content {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-image {
  width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

.plan-placeholder {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
}

.plan-placeholder p {
  margin-top: 12px;
  font-size: 14px;
}

.property-card {
  min-height: 300px;
}

.property-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: #c0c4cc;
}

.placeholder-text {
  margin-top: 16px;
  font-size: 16px;
  color: #909399;
}
</style>
