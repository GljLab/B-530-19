<template>
  <div class="building-detail-container" v-loading="loading">
    <el-card class="header-card">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>返回
          </el-button>
          <span class="building-name">{{ detail.buildingName || '-' }}</span>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="content-row">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header-title">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="楼宇名称">{{ detail.buildingName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="楼宇编号">{{ detail.buildingCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="楼宇类型">
              <el-tag :type="getBuildingTypeTagType(detail.buildingType)" size="small">
                {{ getBuildingTypeLabel(detail.buildingType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="子类型">{{ detail.buildingSubType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地上楼层">{{ detail.totalFloors || 0 }} 层</el-descriptions-item>
            <el-descriptions-item label="地下楼层">{{ detail.undergroundFloors || 0 }} 层</el-descriptions-item>
            <el-descriptions-item label="建筑结构">{{ detail.structureType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="建成年份">{{ detail.builtYear || '-' }}</el-descriptions-item>
            <el-descriptions-item label="客梯数量">{{ detail.passengerElevators || 0 }} 部</el-descriptions-item>
            <el-descriptions-item label="货梯数量">{{ detail.freightElevators || 0 }} 部</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detail.status)" size="small">
                {{ getStatusLabel(detail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="楼宇简介" :span="2">
              {{ detail.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="images-card">
          <template #header>
            <div class="card-header-title">
              <el-icon><Picture /></el-icon>
              <span>楼宇图片</span>
            </div>
          </template>
          <el-carousel v-if="imageList.length > 0" height="400px" indicator-position="outside">
            <el-carousel-item v-for="(img, index) in imageList" :key="index">
              <el-image :src="img" fit="contain" class="carousel-image" :preview-src-list="imageList" :initial-index="index" />
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="暂无图片" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stats-card">
          <template #header>
            <div class="card-header-title">
              <el-icon><DataAnalysis /></el-icon>
              <span>统计数据</span>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ totalFloors }}</div>
              <div class="stat-label">总楼层数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value primary">{{ residentialCount }}</div>
              <div class="stat-label">住宅楼层</div>
            </div>
            <div class="stat-item">
              <div class="stat-value success">{{ commercialCount }}</div>
              <div class="stat-label">商业楼层</div>
            </div>
            <div class="stat-item">
              <div class="stat-value warning">{{ parkingCount }}</div>
              <div class="stat-label">停车楼层</div>
            </div>
          </div>
        </el-card>

        <el-card class="floor-list-card">
          <template #header>
            <div class="card-header">
              <div class="card-header-title">
                <el-icon><Grid /></el-icon>
                <span>楼层列表</span>
              </div>
              <el-button type="primary" size="small" @click="handleAddFloor">
                <el-icon><Plus /></el-icon>新增楼层
              </el-button>
            </div>
          </template>
          <div class="floor-list" v-loading="floorLoading">
            <div
              v-for="floor in floorList"
              :key="floor.id"
              class="floor-item"
              @click="handleViewFloor(floor)"
            >
              <div class="floor-number">{{ floor.floorNumber }}层</div>
              <div class="floor-info">
                <el-tag :type="getFloorTypeTagType(floor.floorType)" size="small">
                  {{ getFloorTypeLabel(floor.floorType) }}
                </el-tag>
                <span class="floor-purpose">{{ floor.floorPurpose || '-' }}</span>
              </div>
              <div class="floor-status">
                <el-tag :type="getFloorStatusTagType(floor.status)" size="small">
                  {{ getFloorStatusLabel(floor.status) }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="!floorLoading && floorList.length === 0" description="暂无楼层数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="editDialogVisible" title="编辑楼宇" width="600px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="楼宇名称" prop="buildingName">
          <el-input v-model="editForm.buildingName" placeholder="请输入楼宇名称" />
        </el-form-item>
        <el-form-item label="楼宇编号" prop="buildingCode">
          <el-input v-model="editForm.buildingCode" placeholder="请输入楼宇编号" />
        </el-form-item>
        <el-form-item label="楼宇类型" prop="buildingType">
          <el-radio-group v-model="editForm.buildingType">
            <el-radio :label="1">住宅楼</el-radio>
            <el-radio :label="2">商业楼</el-radio>
            <el-radio :label="3">综合楼</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地上楼层" prop="totalFloors">
          <el-input-number v-model="editForm.totalFloors" :min="0" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="地下楼层" prop="undergroundFloors">
          <el-input-number v-model="editForm.undergroundFloors" :min="0" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="客梯数量" prop="passengerElevators">
          <el-input-number v-model="editForm.passengerElevators" :min="0" :max="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="货梯数量" prop="freightElevators">
          <el-input-number v-model="editForm.freightElevators" :min="0" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常使用</el-radio>
            <el-radio :label="2">装修中</el-radio>
            <el-radio :label="3">待交付</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="楼宇简介">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入楼宇简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="floorDialogVisible" title="新增楼层" width="500px" destroy-on-close>
      <el-form ref="floorFormRef" :model="floorForm" :rules="floorRules" label-width="100px">
        <el-form-item label="楼层号" prop="floorNumber">
          <el-input-number v-model="floorForm.floorNumber" :min="-10" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="楼层类型" prop="floorType">
          <el-radio-group v-model="floorForm.floorType">
            <el-radio :label="1">住宅楼层</el-radio>
            <el-radio :label="2">商业楼层</el-radio>
            <el-radio :label="3">停车楼层</el-radio>
            <el-radio :label="4">设备楼层</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="楼层用途" prop="floorPurpose">
          <el-input v-model="floorForm.floorPurpose" placeholder="请输入楼层用途" />
        </el-form-item>
        <el-form-item label="房产数量" prop="propertyCount">
          <el-input-number v-model="floorForm.propertyCount" :min="0" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="floorForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">改造中</el-radio>
            <el-radio :label="3">封闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="floorForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="floorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFloorSubmit" :loading="floorSubmitLoading">确定</el-button>
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
  DataAnalysis,
  Grid,
  Plus
} from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const floorLoading = ref(false)
const submitLoading = ref(false)
const floorSubmitLoading = ref(false)
const editDialogVisible = ref(false)
const floorDialogVisible = ref(false)
const editFormRef = ref(null)
const floorFormRef = ref(null)

const buildingId = computed(() => route.params.id)

const detail = ref({})
const floorList = ref([])

const imageList = computed(() => {
  if (detail.value.images && detail.value.images.length > 0) {
    return detail.value.images.map(img => img.imageUrl || img)
  }
  if (detail.value.mainImage) {
    return [detail.value.mainImage]
  }
  return []
})

const totalFloors = computed(() => {
  return (detail.value.totalFloors || 0) + (detail.value.undergroundFloors || 0)
})

const residentialCount = computed(() => {
  return floorList.value.filter(f => f.floorType === 1).length
})

const commercialCount = computed(() => {
  return floorList.value.filter(f => f.floorType === 2).length
})

const parkingCount = computed(() => {
  return floorList.value.filter(f => f.floorType === 3).length
})

const editForm = reactive({
  id: null,
  buildingName: '',
  buildingCode: '',
  buildingType: 1,
  totalFloors: 0,
  undergroundFloors: 0,
  passengerElevators: 0,
  freightElevators: 0,
  status: 1,
  description: ''
})

const editRules = {
  buildingName: [{ required: true, message: '请输入楼宇名称', trigger: 'blur' }],
  buildingCode: [{ required: true, message: '请输入楼宇编号', trigger: 'blur' }],
  buildingType: [{ required: true, message: '请选择楼宇类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const floorForm = reactive({
  id: null,
  buildingId: null,
  floorNumber: 1,
  floorType: 1,
  floorPurpose: '',
  propertyCount: 0,
  status: 1,
  remark: ''
})

const floorRules = {
  floorNumber: [{ required: true, message: '请输入楼层号', trigger: 'blur' }],
  floorType: [{ required: true, message: '请选择楼层类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getBuildingTypeLabel = (type) => {
  const typeMap = {
    1: '住宅楼',
    2: '商业楼',
    3: '综合楼'
  }
  return typeMap[type] || '未知'
}

const getBuildingTypeTagType = (type) => {
  const typeMap = {
    1: 'success',
    2: 'primary',
    3: 'warning'
  }
  return typeMap[type] || 'info'
}

const getStatusLabel = (status) => {
  const statusMap = {
    1: '正常使用',
    2: '装修中',
    3: '待交付'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    1: 'success',
    2: 'warning',
    3: 'info'
  }
  return statusMap[status] || 'info'
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

const getFloorStatusLabel = (status) => {
  const statusMap = {
    1: '正常',
    2: '改造中',
    3: '封闭'
  }
  return statusMap[status] || '未知'
}

const getFloorStatusTagType = (status) => {
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
    const res = await api.parkBuilding.getDetail(buildingId.value)
    detail.value = res.data || {}
  } catch (error) {
    ElMessage.error('加载楼宇详情失败')
  } finally {
    loading.value = false
  }
}

const loadFloorList = async () => {
  floorLoading.value = true
  try {
    const res = await api.parkFloor.listByBuilding(buildingId.value)
    floorList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载楼层列表失败')
  } finally {
    floorLoading.value = false
  }
}

const handleBack = () => {
  router.push('/park/building')
}

const handleEdit = () => {
  Object.assign(editForm, {
    id: detail.value.id,
    buildingName: detail.value.buildingName || '',
    buildingCode: detail.value.buildingCode || '',
    buildingType: detail.value.buildingType || 1,
    totalFloors: detail.value.totalFloors || 0,
    undergroundFloors: detail.value.undergroundFloors || 0,
    passengerElevators: detail.value.passengerElevators || 0,
    freightElevators: detail.value.freightElevators || 0,
    status: detail.value.status || 1,
    description: detail.value.description || ''
  })
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await api.parkBuilding.update(editForm)
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

const handleAddFloor = () => {
  Object.assign(floorForm, {
    id: null,
    buildingId: buildingId.value,
    floorNumber: 1,
    floorType: 1,
    floorPurpose: '',
    propertyCount: 0,
    status: 1,
    remark: ''
  })
  floorDialogVisible.value = true
}

const handleFloorSubmit = async () => {
  if (!floorFormRef.value) return
  await floorFormRef.value.validate(async (valid) => {
    if (!valid) return
    floorSubmitLoading.value = true
    try {
      await api.parkFloor.add(floorForm)
      ElMessage.success('新增成功')
      floorDialogVisible.value = false
      loadFloorList()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      floorSubmitLoading.value = false
    }
  })
}

const handleViewFloor = (floor) => {
  router.push(`/park/floor/detail/${floor.id}`)
}

onMounted(() => {
  loadDetail()
  loadFloorList()
})
</script>

<style scoped>
.building-detail-container {
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

.building-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.content-row {
  align-items: flex-start;
}

.info-card {
  margin-bottom: 20px;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #303133;
}

.images-card {
  margin-bottom: 20px;
}

.carousel-image {
  width: 100%;
  height: 400px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.floor-list-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.floor-list {
  max-height: 500px;
  overflow-y: auto;
}

.floor-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.floor-item:hover {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

.floor-number {
  width: 60px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.floor-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.floor-purpose {
  font-size: 12px;
  color: #909399;
}

.floor-status {
  width: 70px;
  text-align: right;
}
</style>
