<template>
  <div class="floor-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="building-list-card">
          <template #header>
            <div class="card-header">
              <span>楼宇列表</span>
            </div>
          </template>
          <div v-loading="buildingLoading" class="building-list">
            <div
              v-for="item in buildingList"
              :key="item.id"
              class="building-item"
              :class="{ active: selectedBuildingId === item.id }"
              @click="handleSelectBuilding(item)"
            >
              <div class="building-name">{{ item.buildingName }}</div>
              <el-tag :type="getBuildingTypeTagType(item.buildingType)" size="small" class="building-tag">
                {{ getBuildingTypeLabel(item.buildingType) }}
              </el-tag>
            </div>
            <el-empty v-if="!buildingLoading && buildingList.length === 0" description="暂无楼宇" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card class="floor-table-card">
          <template #header>
            <div class="card-header">
              <span>
                {{ selectedBuilding ? selectedBuilding.buildingName + ' - ' : '' }}楼层列表
              </span>
              <el-button type="primary" :disabled="!selectedBuildingId" @click="handleAdd">
                <el-icon><Plus /></el-icon>新增楼层
              </el-button>
            </div>
          </template>

          <el-table :data="floorList" v-loading="floorLoading" border stripe>
            <el-table-column prop="floorNumber" label="楼层号" width="100" align="center" />
            <el-table-column label="楼层类型" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getFloorTypeTagType(row.floorType)" size="small">
                  {{ getFloorTypeLabel(row.floorType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="floorPurpose" label="楼层用途" min-width="150" show-overflow-tooltip />
            <el-table-column prop="propertyCount" label="房产数量" width="100" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
                <el-button type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!floorLoading && floorList.length === 0" description="暂无楼层数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="楼层号" prop="floorNumber">
          <el-input-number v-model="form.floorNumber" :min="-10" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="楼层类型" prop="floorType">
          <el-radio-group v-model="form.floorType">
            <el-radio :label="1">住宅楼层</el-radio>
            <el-radio :label="2">商业楼层</el-radio>
            <el-radio :label="3">停车楼层</el-radio>
            <el-radio :label="4">设备楼层</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="楼层用途" prop="floorPurpose">
          <el-input v-model="form.floorPurpose" placeholder="请输入楼层用途" />
        </el-form-item>
        <el-form-item label="房产数量" prop="propertyCount">
          <el-input-number v-model="form.propertyCount" :min="0" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">改造中</el-radio>
            <el-radio :label="3">封闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const buildingLoading = ref(false)
const floorLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const buildingList = ref([])
const floorList = ref([])
const selectedBuildingId = ref(null)
const selectedBuilding = ref(null)
const formRef = ref(null)

const form = reactive({
  id: null,
  buildingId: null,
  floorNumber: 1,
  floorType: 1,
  floorPurpose: '',
  propertyCount: 0,
  status: 1,
  remark: ''
})

const dialogTitle = computed(() => form.id ? '编辑楼层' : '新增楼层')

const rules = {
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

const loadBuildingList = async () => {
  buildingLoading.value = true
  try {
    const res = await api.parkBuilding.listAll()
    buildingList.value = res.data || []
    if (buildingList.value.length > 0 && !selectedBuildingId.value) {
      handleSelectBuilding(buildingList.value[0])
    }
  } catch (error) {
    ElMessage.error('加载楼宇列表失败')
  } finally {
    buildingLoading.value = false
  }
}

const loadFloorList = async () => {
  if (!selectedBuildingId.value) {
    floorList.value = []
    return
  }
  floorLoading.value = true
  try {
    const res = await api.parkFloor.listByBuilding(selectedBuildingId.value)
    floorList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载楼层列表失败')
  } finally {
    floorLoading.value = false
  }
}

const handleSelectBuilding = (building) => {
  selectedBuildingId.value = building.id
  selectedBuilding.value = building
  loadFloorList()
}

const resetForm = () => {
  form.id = null
  form.buildingId = null
  form.floorNumber = 1
  form.floorType = 1
  form.floorPurpose = ''
  form.propertyCount = 0
  form.status = 1
  form.remark = ''
}

const handleAdd = () => {
  resetForm()
  form.buildingId = selectedBuildingId.value
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  try {
    const res = await api.parkFloor.getById(row.id)
    Object.assign(form, {
      id: res.data.id,
      buildingId: res.data.buildingId,
      floorNumber: res.data.floorNumber,
      floorType: res.data.floorType,
      floorPurpose: res.data.floorPurpose || '',
      propertyCount: res.data.propertyCount || 0,
      status: res.data.status,
      remark: res.data.remark || ''
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载楼层信息失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await api.parkFloor.update(form)
        ElMessage.success('修改成功')
      } else {
        await api.parkFloor.add(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadFloorList()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleView = (row) => {
  router.push(`/park/floor/detail/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除楼层 "${row.floorNumber} 层" 吗？`, '提示', { type: 'warning' })
    await api.parkFloor.delete(row.id)
    ElMessage.success('删除成功')
    loadFloorList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadBuildingList()
})
</script>

<style scoped>
.floor-container {
  padding: 0;
}

.building-list-card {
  height: calc(100vh - 160px);
}

.building-list {
  max-height: calc(100vh - 230px);
  overflow-y: auto;
}

.building-item {
  padding: 12px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.building-item:hover {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

.building-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.building-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
}

.building-tag {
  margin-top: 4px;
}

.floor-table-card .card-header,
.building-list-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.floor-table-card {
  min-height: 400px;
}
</style>
