<template>
  <div class="parking-form">
    <div class="form-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h2>{{ formTitle }}</h2>
    </div>

    <el-card v-loading="loading">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" style="max-width: 900px;">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="车位编号" prop="spaceCode">
                  <el-input v-model="formData.spaceCode" :disabled="isEdit" placeholder="如：A-001、B1-088" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="使用状态" prop="status" v-if="!isEdit">
                  <el-select v-model="formData.status" style="width: 100%;">
                    <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所属区域" prop="area">
                  <el-select v-model="formData.area" placeholder="请选择" style="width: 100%;" :disabled="isEdit">
                    <el-option v-for="a in areaOptions" :key="a" :label="a" :value="a" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="车位类型" prop="spaceType">
                  <el-radio-group v-model="formData.spaceType" :disabled="isEdit">
                    <el-radio v-for="t in spaceTypeOptions" :key="t" :value="t">{{ t }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="长度(米)" prop="length">
                  <el-input-number v-model="formData.length" :min="0" :precision="2" :step="0.5" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="宽度(米)" prop="width">
                  <el-input-number v-model="formData.width" :min="0" :precision="2" :step="0.5" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="朝向" prop="orientation">
                  <el-select v-model="formData.orientation" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="室内" value="室内" />
                    <el-option label="室外" value="室外" />
                    <el-option label="向阳" value="向阳" />
                    <el-option label="背阴" value="背阴" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="位置描述" prop="locationDesc">
              <el-input v-model="formData.locationDesc" type="textarea" :rows="2" placeholder="请输入位置描述" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="产权信息" name="right">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权归属" prop="propertyRight">
                  <el-select v-model="formData.propertyRight" placeholder="请选择" style="width: 100%;" @change="handlePropertyRightChange">
                    <el-option v-for="p in propertyRightOptions" :key="p" :label="p" :value="p" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="产权人" prop="ownerId">
                  <el-select v-model="formData.ownerId" placeholder="请选择产权人（可选）" clearable filterable style="width: 100%;">
                    <el-option v-for="o in ownerList" :key="o.id" :label="o.name" :value="o.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权证号" prop="propertyCertNo">
                  <el-input v-model="formData.propertyCertNo" :disabled="!isManager && isEdit" placeholder="请输入产权证号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="购买日期" prop="purchaseDate">
                  <el-date-picker v-model="formData.purchaseDate" type="date" placeholder="选择日期" style="width: 100%;" :disabled="!isManager && isEdit" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="购买价格(元)" prop="purchasePrice">
              <el-input-number v-model="formData.purchasePrice" :min="0" :precision="2" style="width: 50%;" :disabled="!isManager && isEdit" />
            </el-form-item>
            <el-alert v-if="formData.propertyRight === '业主'" title="提示：产权归属为业主时，购买价格和购买日期为必填项" type="info" show-icon :closable="false" />
          </el-tab-pane>

          <el-tab-pane label="收费信息" name="fee">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="月租金(元)" prop="monthlyRent">
                  <el-input-number v-model="formData.monthlyRent" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="临时收费(元/小时)" prop="hourlyFee">
                  <el-input-number v-model="formData.hourlyFee" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="押金(元)" prop="deposit">
                  <el-input-number v-model="formData.deposit" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="备注信息" name="remark">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" :rows="6" placeholder="请输入备注信息" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            <el-icon><Check /></el-icon>提交
          </el-button>
          <el-button @click="handleBack">
            <el-icon><Close /></el-icon>取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isEdit = computed(() => !!route.params.id)
const formTitle = computed(() => isEdit.value ? '编辑车位' : '新增车位')
const isManager = computed(() => userStore.hasPermission('park:parking:delete'))

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('basic')
const ownerList = ref([])

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

const formData = reactive({
  id: null,
  spaceCode: '',
  area: '',
  spaceType: '',
  locationDesc: '',
  orientation: '',
  length: 5.5,
  width: 2.5,
  propertyRight: '开发商',
  ownerId: null,
  propertyCertNo: '',
  purchasePrice: null,
  purchaseDate: null,
  status: 1,
  monthlyRent: 0,
  hourlyFee: 0,
  deposit: 0,
  remark: ''
})

const formRules = {
  spaceCode: [
    { required: true, message: '请输入车位编号', trigger: 'blur' }
  ],
  area: [
    { required: true, message: '请选择所属区域', trigger: 'change' }
  ],
  spaceType: [
    { required: true, message: '请选择车位类型', trigger: 'change' }
  ],
  propertyRight: [
    { required: true, message: '请选择产权归属', trigger: 'change' }
  ]
}

const handlePropertyRightChange = () => {}

const loadOwnerList = async () => {
  try {
    const res = await api.parkOwner.list({ pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      ownerList.value = res.data.list || []
    }
  } catch (e) {}
}

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await api.parkParking.getById(id)
    if (res.code === 200) {
      const data = res.data
      Object.assign(formData, {
        id: data.id,
        spaceCode: data.spaceCode,
        area: data.area,
        spaceType: data.spaceType,
        locationDesc: data.locationDesc,
        orientation: data.orientation,
        length: data.length,
        width: data.width,
        propertyRight: data.propertyRight,
        ownerId: data.ownerId,
        propertyCertNo: data.propertyCertNo,
        purchasePrice: data.purchasePrice,
        purchaseDate: data.purchaseDate,
        status: data.status,
        monthlyRent: data.monthlyRent,
        hourlyFee: data.hourlyFee,
        deposit: data.deposit,
        remark: data.remark
      })
    }
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (formData.propertyRight === '业主') {
    if (!formData.purchasePrice) {
      ElMessage.warning('产权归属为业主时，购买价格必填')
      return
    }
    if (!formData.purchaseDate) {
      ElMessage.warning('产权归属为业主时，购买日期必填')
      return
    }
  }

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await api.parkParking.update(formData)
    } else {
      res = await api.parkParking.add(formData)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      router.push('/park/parking')
    }
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.push('/park/parking')
}

onMounted(() => {
  loadOwnerList()
  if (isEdit.value) {
    loadDetail()
  }
})
</script>

<style scoped lang="scss">
.parking-form {
  .form-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    h2 {
      margin: 0;
      font-size: 20px;
      color: #303133;
    }
  }
}
</style>
