<template>
  <div class="contract-form">
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

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="130px" class="form-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="租户" prop="tenantId">
              <el-select v-model="formData.tenantId" placeholder="请选择租户" filterable style="width: 100%;">
                <el-option v-for="tenant in tenantOptions" :key="tenant.id" :label="tenant.name" :value="tenant.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房产" prop="propertyId">
              <el-select v-model="formData.propertyId" placeholder="请选择房产" filterable style="width: 100%;">
                <el-option v-for="property in propertyOptions" :key="property.id" :label="property.propertyCode" :value="property.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="租赁类型" prop="leaseType">
              <el-select v-model="formData.leaseType" placeholder="请选择租赁类型" style="width: 100%;">
                <el-option label="整租" :value="1" />
                <el-option label="合租" :value="2" />
                <el-option label="商铺" :value="3" />
                <el-option label="办公" :value="4" />
                <el-option label="仓库" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签约日期" prop="signDate">
              <el-date-picker
                v-model="formData.signDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择签约日期"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开始日期"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="formData.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束日期"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="月租金（元）" prop="monthlyRent">
              <el-input-number v-model="formData.monthlyRent" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金（元）" prop="deposit">
              <el-input-number v-model="formData.deposit" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="付款方式" prop="paymentMethod">
              <el-select v-model="formData.paymentMethod" placeholder="请选择付款方式" style="width: 100%;">
                <el-option label="月付" :value="1" />
                <el-option label="季付" :value="2" />
                <el-option label="半年付" :value="3" />
                <el-option label="年付" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ isEdit ? '保存修改' : '提交新增' }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tenantOptions = ref([])
const propertyOptions = ref([])

const pageTitle = computed(() => isEdit.value ? '编辑合同' : '新增合同')

const formData = reactive({
  id: null,
  tenantId: null,
  propertyId: null,
  leaseType: 1,
  signDate: '',
  startDate: '',
  endDate: '',
  monthlyRent: 0,
  deposit: 0,
  paymentMethod: 1,
  remark: ''
})

const formRules = {
  tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
  propertyId: [{ required: true, message: '请选择房产', trigger: 'change' }],
  leaseType: [{ required: true, message: '请选择租赁类型', trigger: 'change' }],
  signDate: [{ required: true, message: '请选择签约日期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  monthlyRent: [{ required: true, message: '请输入月租金', trigger: 'blur' }],
  deposit: [{ required: true, message: '请输入押金', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}

onMounted(() => {
  loadTenants()
  loadProperties()
  if (route.params.id) {
    isEdit.value = true
    loadDetail()
  }
})

function loadTenants() {
  api.parkTenant.list({ pageSize: 1000 }).then(res => {
    tenantOptions.value = res.data.list || []
  })
}

function loadProperties() {
  api.parkProperty.list({ pageSize: 1000 }).then(res => {
    propertyOptions.value = res.data.list || []
  })
}

function loadDetail() {
  loading.value = true
  api.parkLeaseContract.getById(route.params.id).then(res => {
    const data = res.data || {}
    Object.keys(formData).forEach(key => {
      if (data[key] !== undefined && data[key] !== null) {
        formData[key] = data[key]
      }
    })
    formData.id = data.id
  }).finally(() => {
    loading.value = false
  })
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请检查表单是否填写正确')
    return
  }

  if (new Date(formData.startDate) >= new Date(formData.endDate)) {
    ElMessage.warning('结束日期必须晚于开始日期')
    return
  }

  const submitData = { ...formData }

  if (isEdit.value) {
    api.parkLeaseContract.update(submitData).then(() => {
      ElMessage.success('修改成功')
      setTimeout(() => router.back(), 300)
    })
  } else {
    api.parkLeaseContract.add(submitData).then(() => {
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
.contract-form {
  padding: 20px;
}

.form-header {
  margin-bottom: 20px;
}

.form-card {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    font-size: 18px;
    font-weight: bold;
  }

  .form-content {
    padding: 10px 20px;
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
