<template>
  <div class="park-overview">
    <el-card v-loading="loading" class="overview-card">
      <template #header>
        <div class="park-header">
          <div class="park-title">
            <h2>{{ parkInfo.parkName || '智慧园区' }}</h2>
            <el-tag :type="parkTypeTagType" effect="dark" class="park-type-tag">
              {{ parkTypeLabel }}
            </el-tag>
          </div>
          <div class="park-actions">
            <el-button type="primary" @click="handleEdit" v-if="canEdit">
              <el-icon><Edit /></el-icon>编辑园区
            </el-button>
          </div>
        </div>
      </template>

      <div class="park-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="地址">{{ parkInfo.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物业公司">{{ parkInfo.propertyCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ parkInfo.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客服热线">{{ parkInfo.serviceHotline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="建成时间">{{ parkInfo.builtTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="占地面积">{{ parkInfo.landArea ? parkInfo.landArea + ' ㎡' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="建筑面积">{{ parkInfo.buildingArea ? parkInfo.buildingArea + ' ㎡' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="绿化率">{{ parkInfo.greenRate ? parkInfo.greenRate + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="园区简介" :span="2">{{ parkInfo.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-carousel v-if="parkImages.length > 0" class="park-carousel" height="360px" :interval="4000" arrow="always">
        <el-carousel-item v-for="(img, index) in parkImages" :key="index">
          <el-image :src="img.imageUrl" :alt="img.imageName || `园区图片${index + 1}`" class="carousel-image" fit="cover" :preview-src-list="allImageUrls" :initial-index="index" :preview-teleported="true" />
        </el-carousel-item>
      </el-carousel>
      <el-empty v-else description="暂无图片" class="empty-carousel" />

      <div class="stats-section">
        <h3 class="section-title">园区统计</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="stat-card stat-card-building">
              <div class="stat-icon">
                <el-icon :size="40"><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statsData.totalBuildings || 0 }}</div>
                <div class="stat-label">总楼宇数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-card stat-card-floor">
              <div class="stat-icon">
                <el-icon :size="40"><Grid /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statsData.totalFloors || 0 }}</div>
                <div class="stat-label">总楼层数</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="facilities-section">
        <h3 class="section-title">配套设施</h3>
        <div v-if="facilityList.length > 0" class="facility-grid">
          <div v-for="item in facilityList" :key="item.id" class="facility-item">
            <div class="facility-icon">
              <el-icon :size="32"><component :is="getFacilityIcon(item.facilityType) || 'SetUp'" /></el-icon>
            </div>
            <div class="facility-name">{{ item.facilityName }}</div>
            <div class="facility-location">{{ item.location || '' }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无配套设施" />
      </div>

      <div class="quick-entry-section">
        <h3 class="section-title">快捷入口</h3>
        <div class="quick-entry-grid">
          <el-button type="primary" size="large" @click="goToBuilding">
            <el-icon style="margin-right: 8px"><OfficeBuilding /></el-icon>
            楼宇管理
          </el-button>
          <el-button type="success" size="large" @click="goToFloor">
            <el-icon style="margin-right: 8px"><Grid /></el-icon>
            楼层管理
          </el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑园区信息" width="700px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="园区名称" prop="parkName">
          <el-input v-model="editForm.parkName" placeholder="请输入园区名称" />
        </el-form-item>
        <el-form-item label="园区类型" prop="parkType">
          <el-radio-group v-model="editForm.parkType">
            <el-radio :label="1">住宅小区</el-radio>
            <el-radio :label="2">商业写字楼</el-radio>
            <el-radio :label="3">产业园区</el-radio>
            <el-radio :label="4">综合体</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model="editForm.longitude" placeholder="请输入经度" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model="editForm.latitude" placeholder="请输入纬度" />
        </el-form-item>
        <el-form-item label="物业公司">
          <el-input v-model="editForm.propertyCompany" placeholder="请输入物业公司名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="客服热线">
          <el-input v-model="editForm.serviceHotline" placeholder="请输入客服热线" />
        </el-form-item>
        <el-form-item label="建成时间">
          <el-date-picker v-model="editForm.builtTime" type="date" placeholder="请选择建成时间" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="占地面积">
          <el-input-number v-model="editForm.landArea" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="建筑面积">
          <el-input-number v-model="editForm.buildingArea" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="绿化率(%)">
          <el-input-number v-model="editForm.greenRate" :min="0" :max="100" :precision="1" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="园区简介">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入园区简介" />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, Grid, SetUp, Edit } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const editDialogVisible = ref(false)
const editFormRef = ref(null)

const parkInfo = reactive({
  id: null,
  parkName: '',
  parkType: 1,
  address: '',
  longitude: null,
  latitude: null,
  propertyCompany: '',
  contactPhone: '',
  serviceHotline: '',
  builtTime: '',
  landArea: null,
  buildingArea: null,
  greenRate: null,
  description: '',
  mainImage: ''
})

const editForm = reactive({
  id: null,
  parkName: '',
  parkType: 1,
  address: '',
  longitude: null,
  latitude: null,
  propertyCompany: '',
  contactPhone: '',
  serviceHotline: '',
  builtTime: '',
  landArea: null,
  buildingArea: null,
  greenRate: null,
  description: ''
})

const editRules = {
  parkName: [{ required: true, message: '请输入园区名称', trigger: 'blur' }],
  parkType: [{ required: true, message: '请选择园区类型', trigger: 'change' }]
}

const parkImages = ref([])
const facilityList = ref([])

const statsData = reactive({
  totalBuildings: 0,
  totalFloors: 0
})

const canEdit = computed(() => {
  return userStore.hasPermission('park:overview:edit')
})

const allImageUrls = computed(() => {
  return parkImages.value.map(img => img.imageUrl)
})

const parkTypeLabel = computed(() => {
  const typeMap = {
    1: '住宅小区',
    2: '商业写字楼',
    3: '产业园区',
    4: '综合体'
  }
  return typeMap[parkInfo.parkType] || '未知'
})

const parkTypeTagType = computed(() => {
  const typeMap = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: 'danger'
  }
  return typeMap[parkInfo.parkType] || 'info'
})

const getFacilityIcon = (iconName) => {
  if (!iconName) return null
  const iconMap = {
    '停车场': 'Van',
    '健身房': 'Cpu',
    '游泳池': 'Cpu',
    '超市': 'Cpu',
    '幼儿园': 'Cpu',
    '物业服务中心': 'Cpu'
  }
  return iconMap[iconName] || null
}

const loadParkInfo = async () => {
  try {
    const res = await api.parkInfo.getInfo()
    if (res.data) {
      Object.assign(parkInfo, res.data)
    }
  } catch (error) {
    console.error('加载园区信息失败', error)
  }
}

const loadFacilities = async () => {
  try {
    const res = await api.parkInfo.getFacilities(1)
    facilityList.value = res.data || []
  } catch (error) {
    console.error('加载配套设施失败', error)
  }
}

const loadStats = async () => {
  try {
    const res = await api.parkStats.overview()
    if (res.data) {
      Object.assign(statsData, res.data)
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadParkImages = async () => {
  try {
    const res = await api.parkInfo.getImages(1)
    parkImages.value = res.data || []
  } catch (error) {
    console.error('加载园区图片失败', error)
  }
}

const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadParkInfo(),
      loadFacilities(),
      loadStats(),
      loadParkImages()
    ])
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  Object.assign(editForm, {
    id: parkInfo.id,
    parkName: parkInfo.parkName || '',
    parkType: parkInfo.parkType || 1,
    address: parkInfo.address || '',
    longitude: parkInfo.longitude || null,
    latitude: parkInfo.latitude || null,
    propertyCompany: parkInfo.propertyCompany || '',
    contactPhone: parkInfo.contactPhone || '',
    serviceHotline: parkInfo.serviceHotline || '',
    builtTime: parkInfo.builtTime || '',
    landArea: parkInfo.landArea || null,
    buildingArea: parkInfo.buildingArea || null,
    greenRate: parkInfo.greenRate || null,
    description: parkInfo.description || ''
  })
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await api.parkInfo.update(editForm)
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadParkInfo()
    } catch (error) {
      ElMessage.error(error.message || '保存失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const goToBuilding = () => {
  router.push('/park/building')
}

const goToFloor = () => {
  router.push('/park/floor')
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.park-overview {
  padding: 0;
}

.overview-card {
  border-radius: 8px;
}

.park-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.park-header .park-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.park-title h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.park-type-tag {
  margin-left: 8px;
}

.park-actions {
  display: flex;
  gap: 10px;
}

.park-info {
  margin-bottom: 24px;
}

.park-carousel {
  margin-bottom: 32px;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.empty-carousel {
  height: 200px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.stats-section {
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 8px;
  color: #fff;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card-building {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-floor {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon {
  margin-right: 20px;
  opacity: 0.9;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.facilities-section {
  margin-bottom: 32px;
}

.facility-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 20px;
}

.facility-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  border-radius: 8px;
  background-color: #f5f7fa;
  transition: all 0.3s ease;
  cursor: pointer;
}

.facility-item:hover {
  background-color: #ecf5ff;
  transform: translateY(-2px);
}

.facility-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #fff;
  color: #409eff;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.facility-name {
  font-size: 13px;
  color: #606266;
  text-align: center;
  font-weight: 500;
}

.facility-location {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.quick-entry-section {
  margin-bottom: 16px;
}

.quick-entry-grid {
  display: flex;
  gap: 16px;
}

.quick-entry-grid .el-button {
  flex: 1;
  padding: 20px;
  font-size: 16px;
  border-radius: 8px;
}
</style>
