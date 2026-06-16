<template>
  <div class="tenant-form">
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
        <el-step title="选择租户类型" />
        <el-step :title="formData.tenantType === 1 ? '个人信息' : '企业信息'" />
      </el-steps>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="130px" class="form-content">
        <div v-if="activeStep === 0" class="step-content">
          <div class="type-selector">
            <div
              class="type-card"
              :class="{ active: formData.tenantType === 1 }"
              @click="selectTenantType(1)">
              <el-icon :size="48" class="type-icon"><User /></el-icon>
              <h3>个人租户</h3>
              <p>适用于个人承租</p>
            </div>
            <div
              class="type-card"
              :class="{ active: formData.tenantType === 2 }"
              @click="selectTenantType(2)">
              <el-icon :size="48" class="type-icon"><OfficeBuilding /></el-icon>
              <h3>企业租户</h3>
              <p>适用于公司、企业单位承租</p>
            </div>
          </div>
        </div>

        <div v-if="activeStep === 1" class="step-content">
          <div v-if="formData.tenantType === 1">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入姓名" maxlength="50" />
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
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="formData.idCard" placeholder="请输入18位身份证号" maxlength="18" />
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
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="备用电话">
                  <el-input v-model="formData.backupPhone" placeholder="请输入备用电话" clearable maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="电子邮箱">
                  <el-input v-model="formData.email" placeholder="请输入电子邮箱" clearable maxlength="100" />
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
                <el-form-item label="职业">
                  <el-input v-model="formData.occupation" placeholder="请输入职业" clearable maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="收入范围">
                  <el-select v-model="formData.incomeRange" placeholder="请选择收入范围" clearable style="width: 100%;">
                    <el-option label="5000元以下" :value="1" />
                    <el-option label="5000-10000元" :value="2" />
                    <el-option label="10000-20000元" :value="3" />
                    <el-option label="20000-50000元" :value="4" />
                    <el-option label="50000元以上" :value="5" />
                  </el-select>
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
                <el-form-item label="与联系人关系">
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
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="紧急联系电话">
                  <el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系电话" clearable maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div v-else>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业名称" prop="enterpriseName">
                  <el-input v-model="formData.enterpriseName" placeholder="请输入企业名称" maxlength="100" />
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
                <el-form-item label="联系人" prop="contactPerson">
                  <el-input v-model="formData.contactPerson" placeholder="请输入联系人姓名" maxlength="50" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系人职务">
                  <el-input v-model="formData.contactPosition" placeholder="如经理、行政专员等" clearable maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="formData.contactPhone" placeholder="请输入联系人电话" maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业联系电话" prop="phone">
                  <el-input v-model="formData.phone" placeholder="请输入企业联系电话" maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="备用电话">
                  <el-input v-model="formData.backupPhone" placeholder="请输入备用电话" clearable maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="电子邮箱">
                  <el-input v-model="formData.email" placeholder="请输入企业邮箱" clearable maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="企业地址" prop="enterpriseAddress">
              <el-input v-model="formData.enterpriseAddress" type="textarea" :rows="2" placeholder="请输入企业办公地址" maxlength="500" />
            </el-form-item>
            <el-form-item label="营业执照">
              <div class="upload-area">
                <el-upload
                  class="license-uploader"
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  list-type="picture-card"
                  :file-list="licenseFileList"
                  :on-success="handleUploadSuccess"
                  :on-remove="handleUploadRemove"
                  :on-preview="handleUploadPreview"
                  :limit="1"
                  accept="image/jpeg,image/png,image/jpg,application/pdf"
                >
                  <el-icon><Plus /></el-icon>
                  <template #tip>
                    <div class="el-upload__tip">
                      支持 jpg、png、pdf 格式，单文件不超过 10MB
                    </div>
                  </template>
                </el-upload>
                <div v-if="formData.businessLicenseUrl" class="preview-link">
                  <el-link :href="formData.businessLicenseUrl" target="_blank" type="primary">
                    <el-icon><View /></el-icon>查看已上传文件
                  </el-link>
                </div>
              </div>
            </el-form-item>
          </div>

          <el-form-item label="备注信息">
            <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息（可选）" maxlength="500" />
          </el-form-item>
        </div>
      </el-form>

      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 1" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 1" type="primary" @click="handleSubmit">{{ isEdit ? '保存修改' : '提交新增' }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, User, OfficeBuilding, Plus, View } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const isEdit = ref(false)
const activeStep = ref(0)
const formRef = ref(null)
const licenseFileList = ref([])

const pageTitle = computed(() => isEdit.value ? '编辑租户' : '新增租户')

const uploadAction = '/api/common/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const formData = reactive({
  id: null,
  tenantType: 1,
  name: '',
  gender: 1,
  idCard: '',
  birthDate: null,
  phone: '',
  backupPhone: '',
  email: '',
  workUnit: '',
  occupation: '',
  incomeRange: null,
  emergencyContact: '',
  emergencyRelation: null,
  emergencyPhone: '',
  remark: '',
  enterpriseName: '',
  enterpriseCreditCode: '',
  legalPersonName: '',
  contactPerson: '',
  contactPosition: '',
  contactPhone: '',
  enterpriseAddress: '',
  businessLicenseUrl: ''
})

const personalRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号码', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的18位身份证号', trigger: 'blur' }
  ],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ]
}

const enterpriseRules = {
  enterpriseName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  enterpriseCreditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { len: 18, message: '统一社会信用代码必须为18位', trigger: 'blur' }
  ],
  legalPersonName: [{ required: true, message: '请输入法定代表人姓名', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入企业联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  enterpriseAddress: [{ required: true, message: '请输入企业地址', trigger: 'blur' }]
}

const formRules = computed(() => {
  if (formData.tenantType === 1) {
    return personalRules
  } else {
    return enterpriseRules
  }
})

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    loadDetail()
  }
})

function loadDetail() {
  loading.value = true
  api.parkTenant.getById(route.params.id).then(res => {
    const data = res.data || {}
    Object.keys(formData).forEach(key => {
      if (data[key] !== undefined && data[key] !== null) {
        formData[key] = data[key]
      }
    })
    formData.id = data.id
    if (data.businessLicenseUrl) {
      licenseFileList.value = [{
        name: '营业执照',
        url: data.businessLicenseUrl
      }]
    }
    activeStep.value = 1
  }).finally(() => {
    loading.value = false
  })
}

function selectTenantType(type) {
  formData.tenantType = type
}

function prevStep() {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

async function nextStep() {
  if (activeStep.value < 1) {
    activeStep.value++
  }
}

function handleUploadSuccess(response, file, fileList) {
  if (response.code === 200 || response.code === 0) {
    const fileUrl = response.data?.url || response.data || response.url
    if (fileUrl) {
      formData.businessLicenseUrl = fileUrl
      ElMessage.success('上传成功')
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleUploadRemove(file, fileList) {
  formData.businessLicenseUrl = ''
}

function handleUploadPreview(file) {
  window.open(file.url, '_blank')
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请检查表单是否填写正确')
    return
  }

  const submitData = { ...formData }

  if (isEdit.value) {
    api.parkTenant.update(submitData).then(() => {
      ElMessage.success('修改成功')
      setTimeout(() => router.back(), 300)
    })
  } else {
    api.parkTenant.add(submitData).then(() => {
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
.tenant-form {
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

    .upload-area {
      display: flex;
      align-items: flex-start;
      gap: 20px;

      .license-uploader {
        :deep(.el-upload-list) {
          margin-top: 8px;
        }
      }

      .preview-link {
        margin-top: 8px;
      }
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
