<template>
  <div class="parking-container">
    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">车位总数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value success">{{ stats.freeCount || 0 }}</div>
            <div class="stat-label">空闲车位</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value warning">{{ stats.rentedCount || 0 }}</div>
            <div class="stat-label">已出租车位</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ usageRate }}%</div>
            <div class="stat-label">使用率</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ vacancyRate }}%</div>
            <div class="stat-label">空置率</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value" style="color: #E6A23C;">{{ stats.estimatedMonthlyIncome || 0 }}</div>
            <div class="stat-label">月租金预估(元)</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" size="default">
        <el-form-item label="车位编号">
          <el-input v-model="queryForm.spaceCode" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属区域">
          <el-select v-model="queryForm.area" placeholder="全部" clearable style="width: 140px;">
            <el-option v-for="a in areaOptions" :key="a" :label="a" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item label="车位类型">
          <el-select v-model="queryForm.spaceType" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="t in spaceTypeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="产权归属">
          <el-select v-model="queryForm.propertyRight" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="p in propertyRightOptions" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="月租金">
          <el-input v-model="queryForm.minRent" placeholder="最低" style="width: 80px;" />
          <span style="margin: 0 4px;">-</span>
          <el-input v-model="queryForm.maxRent" placeholder="最高" style="width: 80px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>车位列表</span>
          <div class="header-buttons">
            <el-button v-if="hasAddPermission" type="primary" @click="$router.push('/park/parking/add')">
              <el-icon><Plus /></el-icon>新增车位
            </el-button>
            <el-button v-if="hasBatchAddPermission" type="success" @click="handleBatchCreate">
              <el-icon><DocumentAdd /></el-icon>批量创建
            </el-button>
            <el-button v-if="hasImportPermission" type="warning" @click="handleImportDialog">
              <el-icon><Upload /></el-icon>导入车位
            </el-button>
            <el-button v-if="hasExportPermission" type="info" @click="handleExport">
              <el-icon><Download /></el-icon>导出车位
            </el-button>
            <template v-if="selectedIds.length > 0">
              <el-divider direction="vertical" />
              <span style="color: #409EFF; font-size: 14px;">已选择 {{ selectedIds.length }} 个车位</span>
              <el-button v-if="hasChangeStatusPermission" type="warning" @click="handleBatchStatusDialog">
                <el-icon><Edit /></el-icon>批量改状态
              </el-button>
              <el-button v-if="hasDeletePermission" type="danger" @click="handleBatchDelete">
                <el-icon><Delete /></el-icon>批量删除
              </el-button>
              <el-button @click="clearSelection">
                <el-icon><Close /></el-icon>取消选择
              </el-button>
            </template>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="spaceCode" label="车位编号" width="130" sortable="custom">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.spaceCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="所属区域" width="130" sortable="custom" />
        <el-table-column prop="spaceType" label="车位类型" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.spaceType === '充电车位'" type="success" size="small">{{ row.spaceType }}</el-tag>
            <el-tag v-else-if="row.spaceType === '无障碍车位'" type="warning" size="small">{{ row.spaceType }}</el-tag>
            <span v-else>{{ row.spaceType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="尺寸(长×宽)" width="120">
          <template #default="{ row }">
            {{ row.length && row.width ? `${row.length}×${row.width}m` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="propertyRight" label="产权归属" width="100" />
        <el-table-column prop="ownerName" label="产权人" width="100">
          <template #default="{ row }">
            {{ row.ownerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="使用状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="monthlyRent" label="月租金(元)" width="110" sortable="custom">
          <template #default="{ row }">
            {{ row.monthlyRent || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="hasEditPermission" type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasChangeStatusPermission" type="warning" size="small" @click="handleChangeStatus(row)">改状态</el-button>
            <el-button v-if="hasDeletePermission" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[20, 50, 100, 200]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="statusDialogVisible" title="修改状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentSpace?.status)">{{ getStatusLabel(currentSpace?.status) }}</el-tag>
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

    <el-dialog v-model="batchStatusDialogVisible" title="批量修改状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="已选择">
          <span>{{ selectedIds.length }} 个车位</span>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="batchNewStatus" placeholder="请选择" style="width: 100%;">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更理由">
          <el-input v-model="batchChangeReason" type="textarea" :rows="3" placeholder="请输入变更理由（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchChangeStatus">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量创建车位" width="800px" @close="handleBatchClose">
      <el-steps :active="batchStep" finish-status="success" align-center>
        <el-step title="选择区域" />
        <el-step title="选择类型" />
        <el-step title="设置编号规则" />
        <el-step title="设置默认属性" />
        <el-step title="预览确认" />
      </el-steps>

      <div class="batch-step-content">
        <div v-if="batchStep === 0">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-form-item label="所属区域">
              <el-select v-model="batchForm.area" placeholder="请选择" style="width: 300px;">
                <el-option v-for="a in areaOptions" :key="a" :label="a" :value="a" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="batchStep === 1">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-form-item label="车位类型">
              <el-radio-group v-model="batchForm.spaceType">
                <el-radio v-for="t in spaceTypeOptions" :key="t" :value="t">{{ t }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="batchStep === 2">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-form-item label="编号前缀">
              <el-input v-model="batchForm.prefix" placeholder="如：A- 或 B1-" style="width: 300px;" />
            </el-form-item>
            <el-form-item label="起始号">
              <el-input-number v-model="batchForm.startNum" :min="1" :max="9999" style="width: 300px;" />
            </el-form-item>
            <el-form-item label="结束号">
              <el-input-number v-model="batchForm.endNum" :min="1" :max="9999" style="width: 300px;" />
            </el-form-item>
          </el-form>
        </div>

        <div v-if="batchStep === 3">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="长度(米)">
                  <el-input-number v-model="batchForm.length" :min="0" :precision="2" :step="0.5" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="宽度(米)">
                  <el-input-number v-model="batchForm.width" :min="0" :precision="2" :step="0.5" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权归属">
                  <el-select v-model="batchForm.propertyRight" placeholder="请选择" style="width: 100%;">
                    <el-option v-for="p in propertyRightOptions" :key="p" :label="p" :value="p" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="月租金(元)">
                  <el-input-number v-model="batchForm.monthlyRent" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="临时收费(元/小时)">
                  <el-input-number v-model="batchForm.hourlyFee" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="押金(元)">
                  <el-input-number v-model="batchForm.deposit" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div v-if="batchStep === 4">
          <div v-loading="batchPreviewLoading" style="margin-top: 20px;">
            <el-alert v-if="batchResult" :title="`创建完成：成功 ${batchResult.successCount} 个，失败 ${batchResult.failCount} 个`" :type="batchResult.failCount > 0 ? 'warning' : 'success'" show-icon style="margin-bottom: 16px;" />
            <el-table :data="batchPreviewList" border max-height="350" size="small">
              <el-table-column prop="spaceCode" label="车位编号" width="150" />
              <el-table-column prop="area" label="所属区域" width="140" />
              <el-table-column prop="spaceType" label="类型" width="120" />
              <el-table-column label="尺寸(长×宽)" width="140">
                <template #default="{ row }">
                  {{ row.length && row.width ? `${row.length}×${row.width}m` : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="propertyRight" label="产权归属" width="100" />
              <el-table-column prop="monthlyRent" label="月租金(元)" width="120" />
            </el-table>
            <el-table v-if="batchResult && batchResult.failList && batchResult.failList.length > 0" :data="batchResult.failList" border max-height="200" size="small" style="margin-top: 16px;">
              <el-table-column prop="rowNum" label="行号" width="80" />
              <el-table-column prop="spaceCode" label="车位编号" width="150" />
              <el-table-column prop="reason" label="失败原因" />
            </el-table>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button v-if="batchStep > 0 && !batchResult" @click="batchStep--">上一步</el-button>
        <el-button v-if="batchStep < 4 && !batchResult" type="primary" @click="handleBatchNext">下一步</el-button>
        <el-button v-if="batchStep === 4 && !batchResult" type="primary" :loading="batchCreating" @click="confirmBatchCreate">确认创建</el-button>
        <el-button v-if="batchResult" type="primary" @click="batchDialogVisible = false">完成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="导入车位" width="700px">
      <el-alert title="导入说明" type="info" show-icon :closable="false" style="margin-bottom: 16px;">
        <p>1. 请先下载模板，按照模板格式填写数据</p>
        <p>2. 车位编号必须唯一，带*号的为必填项</p>
        <p>3. 支持格式：.xlsx</p>
      </el-alert>
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleDownloadTemplate">
          <el-icon><Download /></el-icon>下载模板
        </el-button>
      </div>
      <el-upload
        ref="uploadRef"
        drag
        action=""
        :auto-upload="false"
        :show-file-list="true"
        :limit="1"
        accept=".xlsx"
        :on-change="handleFileChange">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx 格式文件</div>
        </template>
      </el-upload>
      <div v-if="importResult" style="margin-top: 16px;">
        <el-alert :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`" :type="importResult.failCount > 0 ? 'warning' : 'success'" show-icon />
        <el-table v-if="importResult.failList && importResult.failList.length > 0" :data="importResult.failList" border max-height="250" size="small" style="margin-top: 12px;">
          <el-table-column prop="rowNum" label="行号" width="80" />
          <el-table-column prop="spaceCode" label="车位编号" width="150" />
          <el-table-column prop="reason" label="失败原因" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!importFile" :loading="importLoading" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, DocumentAdd, Upload, Download, Edit, Delete, Close } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const hasAddPermission = computed(() => userStore.hasPermission('park:parking:add'))
const hasEditPermission = computed(() => userStore.hasPermission('park:parking:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:parking:delete'))
const hasBatchAddPermission = computed(() => userStore.hasPermission('park:parking:batchAdd'))
const hasImportPermission = computed(() => userStore.hasPermission('park:parking:import'))
const hasExportPermission = computed(() => userStore.hasPermission('park:parking:export'))
const hasChangeStatusPermission = computed(() => userStore.hasPermission('park:parking:changeStatus'))

const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const stats = ref({})
const selectedIds = ref([])
const selectedRows = ref([])

const areaOptions = ['地上露天A区', '地上访客区', '地下B1层', '地下B2层', '独立车库']
const spaceTypeOptions = ['标准车位', '大型车位', '微型车位', '充电车位', '无障碍车位']
const propertyRightOptions = ['开发商', '业主', '公共']
const statusOptions = [
  { label: '空闲', value: 1 },
  { label: '业主自用', value: 2 },
  { label: '已出租', value: 3 },
  { label: '临时占用', value: 4 },
  { label: '维护中', value: 5 },
  { label: '禁用', value: 6 }
]

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  spaceCode: '',
  area: '',
  spaceType: '',
  status: null,
  propertyRight: '',
  minRent: '',
  maxRent: '',
  sortField: '',
  sortOrder: ''
})

const usageRate = computed(() => (stats.value.usageRate || 0).toFixed(1))
const vacancyRate = computed(() => (stats.value.vacancyRate || 0).toFixed(1))

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

const loadList = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.spaceCode) delete params.spaceCode
    if (!params.area) delete params.area
    if (!params.spaceType) delete params.spaceType
    if (!params.status) delete params.status
    if (!params.propertyRight) delete params.propertyRight
    if (!params.minRent) delete params.minRent
    if (!params.maxRent) delete params.maxRent
    if (!params.sortField) delete params.sortField
    if (!params.sortOrder) delete params.sortOrder

    const res = await api.parkParking.list(params)
    if (res.code === 200) {
      total.value = res.data.total
      tableData.value = res.data.list
    }
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await api.parkParking.stats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {}
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadList()
}

const handleReset = () => {
  queryForm.spaceCode = ''
  queryForm.area = ''
  queryForm.spaceType = ''
  queryForm.status = null
  queryForm.propertyRight = ''
  queryForm.minRent = ''
  queryForm.maxRent = ''
  queryForm.sortField = ''
  queryForm.sortOrder = ''
  handleSearch()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
  selectedIds.value = rows.map(r => r.id)
}

const clearSelection = () => {
  selectedRows.value = []
  selectedIds.value = []
}

const handleSortChange = ({ prop, order }) => {
  if (order) {
    queryForm.sortField = prop
    queryForm.sortOrder = order === 'descending' ? 'desc' : 'asc'
  } else {
    queryForm.sortField = ''
    queryForm.sortOrder = ''
  }
  loadList()
}

const handleSizeChange = () => {
  queryForm.pageNum = 1
  loadList()
}

const handlePageChange = () => {
  loadList()
}

const handleView = (row) => {
  router.push(`/park/parking/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/park/parking/edit/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除车位【${row.spaceCode}】吗？`, '确认删除', { type: 'warning' })
    const res = await api.parkParking.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    }
  } catch (e) {}
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个车位吗？`, '确认删除', { type: 'warning' })
    const res = await api.parkParking.batchDelete(selectedIds.value)
    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      clearSelection()
      loadList()
      loadStats()
    }
  } catch (e) {}
}

const statusDialogVisible = ref(false)
const currentSpace = ref(null)
const newStatus = ref(null)
const changeReason = ref('')

const handleChangeStatus = (row) => {
  currentSpace.value = row
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
    const res = await api.parkParking.changeStatus(currentSpace.value.id, newStatus.value, changeReason.value)
    if (res.code === 200) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      loadList()
      loadStats()
    }
  } catch (e) {}
}

const batchStatusDialogVisible = ref(false)
const batchNewStatus = ref(null)
const batchChangeReason = ref('')

const handleBatchStatusDialog = () => {
  batchNewStatus.value = null
  batchChangeReason.value = ''
  batchStatusDialogVisible.value = true
}

const confirmBatchChangeStatus = async () => {
  if (!batchNewStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  try {
    const res = await api.parkParking.batchChangeStatus(selectedIds.value, batchNewStatus.value, batchChangeReason.value)
    if (res.code === 200) {
      ElMessage.success('批量状态修改成功')
      batchStatusDialogVisible.value = false
      clearSelection()
      loadList()
      loadStats()
    }
  } catch (e) {}
}

const batchDialogVisible = ref(false)
const batchStep = ref(0)
const batchPreviewLoading = ref(false)
const batchCreating = ref(false)
const batchPreviewList = ref([])
const batchResult = ref(null)
const batchForm = reactive({
  area: '',
  spaceType: '',
  prefix: '',
  startNum: 1,
  endNum: 10,
  length: 5.5,
  width: 2.5,
  propertyRight: '开发商',
  monthlyRent: 300,
  hourlyFee: 5,
  deposit: 500
})

const handleBatchCreate = () => {
  batchStep.value = 0
  batchPreviewList.value = []
  batchResult.value = null
  batchForm.area = ''
  batchForm.spaceType = ''
  batchForm.prefix = ''
  batchForm.startNum = 1
  batchForm.endNum = 10
  batchDialogVisible.value = true
}

const handleBatchClose = () => {
  batchDialogVisible.value = false
  loadList()
  loadStats()
}

const handleBatchNext = async () => {
  if (batchStep.value === 0 && !batchForm.area) {
    ElMessage.warning('请选择所属区域')
    return
  }
  if (batchStep.value === 1 && !batchForm.spaceType) {
    ElMessage.warning('请选择车位类型')
    return
  }
  if (batchStep.value === 2) {
    if (!batchForm.prefix) {
      ElMessage.warning('请输入编号前缀')
      return
    }
    if (batchForm.startNum > batchForm.endNum) {
      ElMessage.warning('起始号不能大于结束号')
      return
    }
  }
  if (batchStep.value === 3) {
    batchPreviewLoading.value = true
    try {
      const res = await api.parkParking.previewBatch({ ...batchForm })
      if (res.code === 200) {
        batchPreviewList.value = res.data
        batchStep.value++
      }
    } finally {
      batchPreviewLoading.value = false
    }
    return
  }
  batchStep.value++
}

const confirmBatchCreate = async () => {
  batchCreating.value = true
  try {
    const res = await api.parkParking.batchCreate({ ...batchForm })
    if (res.code === 200) {
      batchResult.value = res.data
      ElMessage.success(`批量创建完成`)
    }
  } finally {
    batchCreating.value = false
  }
}

const importDialogVisible = ref(false)
const importFile = ref(null)
const importLoading = ref(false)
const importResult = ref(null)

const handleImportDialog = () => {
  importFile.value = null
  importResult.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
  importResult.value = null
}

const handleDownloadTemplate = async () => {
  try {
    const res = await api.parkParking.getTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '车位导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {}
}

const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importLoading.value = true
  try {
    const res = await api.parkParking.importData(importFile.value)
    if (res.code === 200) {
      importResult.value = res.data
      ElMessage.success('导入完成')
      loadList()
      loadStats()
    }
  } finally {
    importLoading.value = false
  }
}

const handleExport = async () => {
  try {
    const params = {}
    if (queryForm.spaceCode) params.spaceCode = queryForm.spaceCode
    if (queryForm.area) params.area = queryForm.area
    if (queryForm.spaceType) params.spaceType = queryForm.spaceType
    if (queryForm.status) params.status = queryForm.status
    if (queryForm.propertyRight) params.propertyRight = queryForm.propertyRight
    if (queryForm.minRent) params.minRent = queryForm.minRent
    if (queryForm.maxRent) params.maxRent = queryForm.maxRent

    const res = await api.parkParking.export(params)
    if (res.code === 200 && res.data) {
      const exportData = res.data
      const headers = [
        '车位编号', '所属区域', '车位类型', '位置描述', '朝向',
        '长度(米)', '宽度(米)', '产权归属', '产权人', '产权证号',
        '购买价格', '购买日期', '使用状态', '月租金(元)', '临时收费(元/小时)',
        '押金(元)', '备注'
      ]
      const rows = exportData.map(item => [
        item.spaceCode, item.area, item.spaceType, item.locationDesc || '',
        item.orientation || '', item.length || '', item.width || '',
        item.propertyRight, item.ownerName || '', item.propertyCertNo || '',
        item.purchasePrice || '', item.purchaseDate || '',
        getStatusLabel(item.status), item.monthlyRent || 0,
        item.hourlyFee || 0, item.deposit || 0, item.remark || ''
      ])

      let csv = '\ufeff' + headers.join(',') + '\n'
      rows.forEach(row => {
        csv += row.map(cell => `"${cell}"`).join(',') + '\n'
      })
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `车位数据_${new Date().getTime()}.csv`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (e) {}
}

onMounted(() => {
  loadList()
  loadStats()
})
</script>

<style scoped lang="scss">
.parking-container {
  .stats-card {
    margin-bottom: 16px;
    .stat-item {
      text-align: center;
      padding: 10px 0;
    }
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      margin-bottom: 6px;
      &.primary { color: #409EFF; }
      &.success { color: #67C23A; }
      &.warning { color: #E6A23C; }
      &.info { color: #909399; }
    }
    .stat-label {
      font-size: 14px;
      color: #606266;
    }
  }
  .search-card {
    margin-bottom: 16px;
  }
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .header-buttons {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
  .batch-step-content {
    min-height: 300px;
  }
}
</style>
