<template>
  <div class="tag-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标签管理</span>
          <el-button v-if="hasAddPermission" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增标签
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="tagName" label="标签名称" width="200">
          <template #default="{ row }">
            <el-tag :color="row.tagColor" style="color: #fff;">{{ row.tagName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tagColor" label="标签颜色" width="150">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <div :style="{ width: '24px', height: '24px', borderRadius: '4px', background: row.tagColor }"></div>
              <span>{{ row.tagColor }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="propertyCount" label="使用房产数" width="120" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasEditPermission" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasDeletePermission" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑标签' : '新增标签'" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="标签名称" required>
          <el-input v-model="formData.tagName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-color-picker v-model="formData.tagColor" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const formVisible = ref(false)
const isEdit = ref(false)
const formData = reactive({
  id: null,
  tagName: '',
  tagColor: '#409EFF',
  sortOrder: 0
})

const hasAddPermission = computed(() => userStore.hasPermission('park:tag:add'))
const hasEditPermission = computed(() => userStore.hasPermission('park:tag:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:tag:delete'))

const loadList = async () => {
  loading.value = true
  try {
    const res = await api.parkPropertyTag.list()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  formData.id = null
  formData.tagName = ''
  formData.tagColor = '#409EFF'
  formData.sortOrder = 0
  isEdit.value = false
  formVisible.value = true
}

const handleEdit = (row) => {
  formData.id = row.id
  formData.tagName = row.tagName
  formData.tagColor = row.tagColor
  formData.sortOrder = row.sortOrder
  isEdit.value = true
  formVisible.value = true
}

const handleSubmit = async () => {
  if (!formData.tagName) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    if (isEdit.value) {
      await api.parkPropertyTag.update(formData)
    } else {
      await api.parkPropertyTag.add(formData)
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    formVisible.value = false
    loadList()
  } catch {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该标签？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await api.parkPropertyTag.delete(row.id)
      ElMessage.success('删除成功')
      loadList()
    } catch {}
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.tag-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
