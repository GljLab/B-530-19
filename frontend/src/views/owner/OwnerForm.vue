<template>
  <div class="owner-form">
    <div class="form-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <el-card class="form-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="选择业主类型" />
        <el-step :title="formData.ownerType === 1 ? '个人信息' : '企业信息'" />
        <el-step title="联系与地址信息" />
        <el-step title="关联房产（可选）" />
      </el-steps>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="130px" class="form-content">
        <div v-if="activeStep === 0" class="step-content">
          <div class="type-selector">
            <div 
              class="type-card" 
              :class="{ active: formData.ownerType === 1 }"
              @click="selectOwnerType(1)">
              <el-icon :size="48" class="type-icon"><User /></el-icon>
              <h3>个人业主</h3>
              <p>适用于个人、家庭业主</p>
            </div>
            <div 
              class="type-card" 
              :class="{ active: formData.ownerType === 2 }"
              @click="selectOwnerType(2)">
              <el-icon :size="48" class="type-icon"><OfficeBuilding /></el-icon>
              <h3>企业业主</h3>
              <p>适用于公司、企业单位</p>
            </div>
          </div>
        </div>

        <div v-if="activeStep === 1" class="step-content">
          <div v-if="formData.ownerType === 1">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="业主姓名" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入业主姓名" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="formData.gender">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="证件号码" prop="idCard">
                  <el-input v-model="formData.idCard" placeholder="请输入身份证号码" maxlength="18" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="出生日期" prop="birthDate">
                  <el-date-picker 
                    v-model="formData.birthDate" 
                    type="date" 
                    value-format="YYYY-MM-DD"
                    placeholder="选择出生日期" 
                    style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="民族">
                  <el-input v-model="formData.nation" placeholder="请输入民族，如汉族" clearable maxlength="20" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="婚姻状况">
                  <el-select v-model="formData.maritalStatus" placeholder="请选择婚姻状况" clearable style="width: 100%;">
                    <el-option label="未婚" :value="1" />
                    <el-option label="已婚" :value="2" />
                    <el-option label="离异" :value="3" />
                    <el-option label="丧偶" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="职业">
                  <el-input v-model="formData.occupation" placeholder="请输入职业" clearable maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="工作单位">
                  <el-input v-model="formData.workUnit" placeholder="请输入工作单位" clearable maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="紧急联系人">
                  <el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人" clearable maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="紧急联系电话">
                  <el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系电话" clearable maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="与紧急联系人关系">
                  <el-select v-model="formData.emergencyRelation" placeholder="请选择关系" clearable style="width: 100%;">
                    <el-option label="配偶" :value="1" />
                    <el-option label="父母" :value="2" />
                    <el-option label="子女" :value="3" />
                    <el-option label="兄弟姐妹" :value="4" />
                    <el-option label="朋友" :value="5" />
                    <el-option label="同事" :value="6" />
                    <el-option label="其他" :value="99" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="家庭人口数">
                  <el-input-number v-model="formData.familyCount" :min="1" :max="20" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div v-else>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业名称" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入企业名称" maxlength="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="统一社会信用代码" prop="enterpriseCreditCode">
                  <el-input v-model="formData.enterpriseCreditCode" placeholder="请输入18位统一社会信用代码" maxlength="18" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="法定代表人" prop="legalPersonName">
                  <el-input v-model="formData.legalPersonName" placeholder="请输入法定代表人姓名" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="法人身份证号">
                  <el-input v-model="formData.legalPersonIdCard" placeholder="请输入法人身份证号" clearable maxlength="18" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业类型" prop="enterpriseType">
                  <el-select v-model="formData.enterpriseType" placeholder="请选择企业类型" clearable style="width: 100%;">
                    <el-option label="国有企业" :value="1" />
                    <el-option label="集体企业" :value="2" />
                    <el-option label="私营企业" :value="3" />
                    <el-option label="联营企业" :value="4" />
                    <el-option label="股份制企业" :value="5" />
                    <el-option label="外商投资企业" :value="6" />
                    <el-option label="有限责任公司" :value="7" />
                    <el-option label="股份有限公司" :value="8" />
                    <el-option label="个体工商户" :value="9" />
                    <el-option label="其他" :value="99" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="注册资本">
                  <el-input-number v-model="formData.registeredCapital" :min="0" :precision="2" style="width: 100%;" />
                  <div style="font-size: 12px; color: #909399; margin-top: 4px;">单位：万元</div>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系人">
                  <el-input v-model="formData.contactPerson" placeholder="请输入联系人姓名" clearable maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系人职务">
                  <el-input v-model="formData.contactPosition" placeholder="如经理、行政专员等" clearable maxlength="50" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系人电话">
                  <el-input v-model="formData.contactPhone" placeholder="请输入联系人手机号" clearable maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="行业">
                  <el-input v-model="formData.industry" placeholder="如房地产、科技、教育等" clearable maxlength="50" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="企业地址">
              <el-input v-model="formData.enterpriseAddress" type="textarea" :rows="2" placeholder="请输入企业实际办公/注册地址" maxlength="500" />
            </el-form-item>
          </div>
        </div>

        <div v-if="activeStep === 2" class="step-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="formData.phone" placeholder="请输入联系电话（必填）" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="备用手机">
                <el-input v-model="formData.backupPhone" placeholder="请输入备用手机（可选）" clearable maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电子邮箱">
                <el-input v-model="formData.email" placeholder="请输入电子邮箱（可选）" clearable maxlength="100" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="微信号">
                <el-input v-model="formData.wechat" placeholder="请输入微信号（可选）" clearable maxlength="50" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="户籍地址">
            <el-input v-model="formData.householdAddress" type="textarea" :rows="2" placeholder="请输入户籍/注册地址（可选）" maxlength="500" />
          </el-form-item>
          <el-form-item label="现居住地址（联系地址）" prop="currentAddress">
            <el-input v-model="formData.currentAddress" type="textarea" :rows="2" placeholder="请输入现居住/联系地址（必填）" maxlength="500" />
          </el-form-item>
          <el-form-item label="入住状态">
            <el-radio-group v-model="formData.occupancyStatus">
              <el-radio :value="1">已入住</el-radio>
              <el-radio :value="2">未入住</el-radio>
              <el-radio :value="3">出租中</el-radio>
              <el-radio :value="4">空置</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注信息">
            <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息（可选）" maxlength="500" />
          </el-form-item>
        </div>

        <div v-if="activeStep === 3" class="step-content">
          <div class="property-select-header">
            <span>选择关联房产（可在详情页后续管理）</span>
            <el-button type="primary" size="small" @click="handleAddProperty">
              <el-icon><Plus /></el-icon>添加房产
            </el-button>
          </div>
          <el-table :data="selectedProperties" border stripe style="margin-top: 15px;">
            <el-table-column prop="propertyCode" label="房产编号" width="130" />
            <el-table-column prop="buildingName" label="楼宇" width="120" />
            <el-table-column prop="floorNumber" label="楼层" width="80" />
            <el-table-column prop="buildingArea" label="建筑面积(㎡)" width="110" />
            <el-table-column prop="houseType" label="户型" width="90" />
            <el-table-column label="产权关系" width="130">
              <template #default="{ row }">
                <el-select v-model="row.propertyRightType" size="small" style="width: 100%;">
                  <el-option label="完全产权" :value="1" />
                  <el-option label="共有产权" :value="2" />
                  <el-option label="使用权" :value="3" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="产权比例" width="120">
              <template #default="{ row }">
                <el-input-number v-if="row.propertyRightType === 2" v-model="row.propertyRightRatio" :min="1" :max="100" size="small" />
                <span v-else style="color: #606266;">100%</span>
              </template>
            </el-table-column>
            <el-table-column label="是否自住" width="110">
              <template #default="{ row }">
                <el-radio-group v-model="row.isSelfOccupy" size="small">
                  <el-radio :value="1">自住</el-radio>
                  <el-radio :value="0">非自住</el-radio>
                </el-radio-group>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" size="small" text @click="handleRemoveProperty($index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="selectedProperties.length === 0" class="empty-tip">
            暂未关联房产，可点击上方"添加房产"按钮选择房产，或提交后在业主详情页管理
          </div>
        </div>
      </el-form>

      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 3" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 3" type="primary" @click="handleSubmit">{{ isEdit ? '保存修改' : '提交新增' }}</el-button>
      </div>
    </el-card>

    <el-dialog v-model="propertyDialogVisible" title="选择房产" width="900px">
      <el-form :inline="true" :model="propertyQuery" size="default">
        <el-form-item label="房产编号">
          <el-input v-model="propertyQuery.propertyCode" placeholder="请输入" clearable @keyup.enter="searchProperty" />
        </el-form-item>
        <el-form-item label="楼宇">
          <el-select v-model="propertyQuery.buildingId" placeholder="全部" clearable style="width: 140px;">
            <el-option v-for="b in buildingList" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchProperty">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </el-form-item>
      </el-form>
      <el-table 
        :data="propertyList" 
        border 
        stripe 
        height="360"
        @selection-change="handlePropertySelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="propertyCode" label="房产编号" width="120" />
        <el-table-column prop="buildingName" label="楼宇" width="110" />
        <el-table-column prop="floorNumber" label="楼层" width="70" />
        <el-table-column prop="houseNo" label="房号" width="80" />
        <el-table-column prop="buildingArea" label="建筑面积(㎡)" width="110" />
        <el-table-column prop="useArea" label="使用面积(㎡)" width="110" />
        <el-table-column prop="houseType" label="户型" width="80" />
        <el-table-column prop="roomCount" label="朝向" width="80" />
        <el-table-column label="可关联状态" width="100">
          <template #default="{ row }">
            <el-tag size="small">可关联</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="property-pagination"
        v-model:current-page="propertyQuery.pageNum"
        v-model:page-size="propertyQuery.pageSize"
        :page-sizes="[5, 10, 20]"
        :total="propertyTotal"
        layout="total, prev, pager, next"
        @current-change="searchProperty" />
      <template #footer>
        <el-button @click="propertyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddProperty">确定添加（{{ propertySelectedIds.length }}套）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, User, OfficeBuilding, Plus, Search } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const isEdit = ref(false)
const activeStep = ref(0)
const formRef = ref(null)

const pageTitle = computed(() => isEdit.value ? '编辑业主' : '新增业主')

const formData = reactive({
  id: null,
  ownerType: 1,
  name: '',
  gender: 1,
  idCard: '',
  birthDate: null,
  nation: '',
  maritalStatus: null,
  familyCount: 1,
  occupation: '',
  workUnit: '',
  emergencyContact: '',
  emergencyPhone: '',
  emergencyRelation: null,
  occupancyStatus: 2,
  enterpriseCreditCode: '',
  enterpriseType: null,
  registeredCapital: null,
  legalPersonName: '',
  legalPersonIdCard: '',
  contactPerson: '',
  contactPosition: '',
  contactPhone: '',
  industry: '',
  enterpriseAddress: '',
  phone: '',
  backupPhone: '',
  email: '',
  wechat: '',
  householdAddress: '',
  currentAddress: '',
  remark: ''
})

const personalRules = {
  name: [{ required: true, message: '请输入业主姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号码', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的18位身份证号', trigger: 'blur' }
  ],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }]
}

const enterpriseRules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  enterpriseCreditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { len: 18, message: '统一社会信用代码必须为18位', trigger: 'blur' }
  ],
  legalPersonName: [{ required: true, message: '请输入法定代表人姓名', trigger: 'blur' }]
}

const commonRules = {
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  currentAddress: [{ required: true, message: '请输入现居住/联系地址', trigger: 'blur' }]
}

const formRules = computed(() => {
  if (formData.ownerType === 1) {
    return { ...personalRules, ...commonRules }
  } else {
    return { ...enterpriseRules, ...commonRules }
  }
})

const selectedProperties = ref([])
const propertyDialogVisible = ref(false)
const propertyList = ref([])
const propertyTotal = ref(0)
const propertySelectedIds = ref([])
const buildingList = ref([])

const propertyQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  propertyCode: '',
  buildingId: null
})

onMounted(() => {
  loadBuildingList()
  if (route.params.id) {
    isEdit.value = true
    loadDetail()
  }
})

function loadBuildingList() {
  api.parkBuilding.listAll({ parkId: 1 }).then(res => {
    buildingList.value = res.data || []
  }).catch(() => {})
}

function loadDetail() {
  loading.value = true
  api.parkOwner.getById(route.params.id).then(res => {
    const data = res.data || {}
    Object.keys(formData).forEach(key => {
      if (data[key] !== undefined && data[key] !== null) {
        formData[key] = data[key]
      }
    })
    formData.id = data.id
    selectedProperties.value = (data.propertyList || []).map(p => ({
      id: p.propertyId || p.id,
      propertyId: p.propertyId,
      relationId: p.id,
      propertyCode: p.propertyCode,
      buildingName: p.buildingName,
      floorNumber: p.floorNumber,
      buildingArea: p.buildingArea,
      houseType: p.houseType,
      propertyRightType: p.propertyRightType || 1,
      propertyRightRatio: p.propertyRightRatio || 100,
      isSelfOccupy: p.isSelfOccupy !== undefined ? p.isSelfOccupy : 1
    }))
    if (data.idCard) {
      activeStep.value = 2
    } else if (data.ownerType) {
      activeStep.value = 1
    }
  }).finally(() => {
    loading.value = false
  })
}

function selectOwnerType(type) {
  formData.ownerType = type
}

function prevStep() {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

async function nextStep() {
  const fields = getStepFields()
  if (fields && fields.length > 0) {
    try {
      await formRef.value.validateField(fields)
    } catch {
      return
    }
  }
  if (activeStep.value < 3) {
    activeStep.value++
  }
}

function getStepFields() {
  if (activeStep.value === 0) {
    return null
  } else if (activeStep.value === 1) {
    if (formData.ownerType === 1) {
      return ['name', 'gender', 'idCard', 'birthDate']
    } else {
      return ['name', 'enterpriseCreditCode', 'legalPersonName']
    }
  } else if (activeStep.value === 2) {
    return ['phone', 'currentAddress']
  }
  return null
}

function handleAddProperty() {
  propertyQuery.pageNum = 1
  propertyQuery.propertyCode = ''
  propertyQuery.buildingId = null
  propertySelectedIds.value = []
  searchProperty()
  propertyDialogVisible.value = true
}

function searchProperty() {
  const params = {
    pageNum: propertyQuery.pageNum,
    pageSize: propertyQuery.pageSize,
    propertyCode: propertyQuery.propertyCode || undefined,
    buildingId: propertyQuery.buildingId || undefined
  }
  api.property.list(params).then(res => {
    propertyList.value = res.data.list || []
    propertyTotal.value = res.data.total || 0
  })
}

function handlePropertySelectionChange(selection) {
  propertySelectedIds.value = selection.map(item => item.id)
}

function confirmAddProperty() {
  if (propertySelectedIds.value.length === 0) {
    ElMessage.warning('请选择要关联的房产')
    return
  }
  const newProperties = propertyList.value
    .filter(p => propertySelectedIds.value.includes(p.id))
    .filter(p => !selectedProperties.value.find(sp => sp.id === p.id))
    .map(p => ({
      id: p.id,
      propertyId: p.id,
      propertyCode: p.propertyCode,
      buildingName: p.buildingName,
      floorNumber: p.floorNumber,
      buildingArea: p.buildingArea,
      houseType: p.houseType,
      propertyRightType: 1,
      propertyRightRatio: 100,
      isSelfOccupy: 1
    }))
  if (newProperties.length === 0) {
    ElMessage.info('选择的房产已添加')
    propertyDialogVisible.value = false
    return
  }
  selectedProperties.value.push(...newProperties)
  propertyDialogVisible.value = false
  ElMessage.success(`已添加 ${newProperties.length} 套房产`)
}

function handleRemoveProperty(index) {
  selectedProperties.value.splice(index, 1)
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请检查表单是否填写正确')
    return
  }

  const propertyListData = selectedProperties.value.map(p => ({
    propertyId: p.propertyId || p.id,
    propertyRightType: p.propertyRightType,
    propertyRightRatio: p.propertyRightType === 2 ? p.propertyRightRatio : 100,
    isSelfOccupy: p.isSelfOccupy
  }))

  const submitData = {
    ...formData,
    propertyList: propertyListData
  }

  if (isEdit.value) {
    api.parkOwner.update(submitData).then(() => {
      ElMessage.success('修改成功')
      setTimeout(() => router.back(), 300)
    })
  } else {
    api.parkOwner.add(submitData).then(() => {
      ElMessage.success('新增成功')
      setTimeout(() => router.back(), 300)
    })
  }
}

function handleBack() {
  ElMessageBox.confirm('确定要离开吗？未保存的数据将会丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.back()
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.owner-form {
  padding: 20px;
}

.form-header {
  margin-bottom: 20px;
}

.form-card {
  max-width: 1100px;
  margin: 0 auto;

  .card-header {
    font-size: 18px;
    font-weight: bold;
  }

  .form-content {
    padding: 30px 10px 10px 10px;
    min-height: 400px;

    .step-content {
      padding: 10px 20px;
    }

    .type-selector {
      display: flex;
      justify-content: center;
      gap: 40px;
      padding: 40px 0 60px 0;

      .type-card {
        width: 280px;
        padding: 40px 30px;
        border: 2px solid #e4e7ed;
        border-radius: 12px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #c6e2ff;
          background: #f5faff;
          transform: translateY(-4px);
        }

        &.active {
          border-color: #409eff;
          background: #ecf5ff;
          box-shadow: 0 6px 20px rgba(64, 158, 255, 0.2);
        }

        .type-icon {
          color: #909399;
          margin-bottom: 16px;
        }

        &.active .type-icon {
          color: #409eff;
        }

        h3 {
          margin: 0 0 8px 0;
          color: #303133;
        }

        p {
          margin: 0;
          color: #909399;
          font-size: 13px;
        }
      }
    }

    .property-select-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid #ebeef5;
      font-weight: 600;
      color: #606266;
    }

    .empty-tip {
      padding: 30px;
      text-align: center;
      color: #909399;
      background: #fafafa;
      border-radius: 4px;
      margin-top: 10px;
    }

    .property-pagination {
      margin-top: 15px;
      justify-content: flex-end;
    }
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    padding: 24px 0;
    border-top: 1px solid #ebeef5;
    margin-top: 20px;
  }
}
</style>
