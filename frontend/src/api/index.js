import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return res
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          window.location.href = '/login'
        }).catch(() => {})
      }
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    let message = '请求失败'
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message = '登录已过期，请重新登录'
          localStorage.removeItem('token')
          ElMessageBox.confirm(message, '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            window.location.href = '/login'
          }).catch(() => {})
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = data?.message || '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败: ${status}`
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// API模块
const api = {
  // 认证相关
  auth: {
    login: (data) => request.post('/auth/login', data),
    register: (data) => request.post('/auth/register', data),
    getUserInfo: () => request.get('/auth/info'),
    logout: () => request.post('/auth/logout')
  },
  
  // 用户管理
  user: {
    list: (params) => request.get('/system/user/list', { params }),
    getById: (id) => request.get(`/system/user/${id}`),
    add: (data) => request.post('/system/user', data),
    update: (data) => request.put('/system/user', data),
    delete: (id) => request.delete(`/system/user/${id}`),
    updateStatus: (data) => request.put('/system/user/status', data),
    resetPassword: (data) => request.put('/system/user/resetPwd', data),
    changePassword: (data) => request.put('/system/user/profile/password', data),
    updateProfile: (data) => request.put('/system/user/profile', data)
  },
  
  // 角色管理
  role: {
    list: (params) => request.get('/system/role/list', { params }),
    listAll: () => request.get('/system/role/listAll'),
    getById: (id) => request.get(`/system/role/${id}`),
    add: (data) => request.post('/system/role', data),
    update: (data) => request.put('/system/role', data),
    delete: (id) => request.delete(`/system/role/${id}`),
    updateStatus: (data) => request.put('/system/role/status', data)
  },
  
  // 菜单管理
  menu: {
    tree: () => request.get('/system/menu/tree'),
    list: () => request.get('/system/menu/list'),
    selectTree: () => request.get('/system/menu/selectTree'),
    getById: (id) => request.get(`/system/menu/${id}`),
    add: (data) => request.post('/system/menu', data),
    update: (data) => request.put('/system/menu', data),
    delete: (id) => request.delete(`/system/menu/${id}`)
  },
  
  // 数据权限
  dataPerm: {
    list: (params) => request.get('/system/dataPermission/list', { params }),
    listAll: () => request.get('/system/dataPermission/all'),
    getByRoleId: (roleId) => request.get(`/system/dataPermission/role/${roleId}`),
    save: (data) => request.post('/system/dataPermission', data),
    deleteByRoleId: (roleId) => request.delete(`/system/dataPermission/role/${roleId}`),
    getScopeTypes: () => request.get('/system/dataPermission/scopeTypes')
  },

  // 园区信息
  parkInfo: {
    getInfo: () => request.get('/park/info'),
    getDetail: (id) => request.get(`/park/info/${id}`),
    update: (data) => request.put('/park/info', data),
    getFacilities: (parkId) => request.get(`/park/info/facility/${parkId}`),
    saveFacilities: (parkId, data) => request.put(`/park/info/facility/${parkId}`, data),
    getImages: (parkId) => request.get(`/park/info/image/${parkId}`),
    saveImages: (parkId, data) => request.put(`/park/info/image/${parkId}`, data)
  },

  // 楼宇管理
  parkBuilding: {
    list: (params) => request.get('/park/building/list', { params }),
    listAll: (params) => request.get('/park/building/listAll', { params }),
    getById: (id) => request.get(`/park/building/${id}`),
    getDetail: (id) => request.get(`/park/building/detail/${id}`),
    add: (data) => request.post('/park/building', data),
    update: (data) => request.put('/park/building', data),
    delete: (id) => request.delete(`/park/building/${id}`),
    getImages: (buildingId) => request.get(`/park/building/image/${buildingId}`),
    saveImages: (buildingId, data) => request.put(`/park/building/image/${buildingId}`, data)
  },

  // 楼层管理
  parkFloor: {
    listByBuilding: (buildingId) => request.get(`/park/floor/list/${buildingId}`),
    getById: (id) => request.get(`/park/floor/${id}`),
    add: (data) => request.post('/park/floor', data),
    update: (data) => request.put('/park/floor', data),
    delete: (id) => request.delete(`/park/floor/${id}`)
  },

  // 统计看板
  parkStats: {
    overview: () => request.get('/park/stats/overview'),
    buildingType: () => request.get('/park/stats/buildingType'),
    floorPurpose: () => request.get('/park/stats/floorPurpose'),
    buildingStatus: () => request.get('/park/stats/buildingStatus'),
    buildingList: () => request.get('/park/stats/buildingList')
  },

  // 房产管理
  parkProperty: {
    list: (params) => request.get('/park/property/list', { params }),
    getById: (id) => request.get(`/park/property/${id}`),
    add: (data) => request.post('/park/property', data),
    update: (data) => request.put('/park/property', data),
    delete: (id) => request.delete(`/park/property/${id}`),
    batchDelete: (ids) => request.delete('/park/property/batch', { data: ids }),
    changeStatus: (id, status, reason) => request.put('/park/property/status', null, { params: { id, status, reason } }),
    batchChangeStatus: (ids, status, reason) => request.put('/park/property/status/batch', ids, { params: { status, reason } }),
    getStatusLogs: (propertyId) => request.get(`/park/property/statusLog/${propertyId}`),
    previewBatch: (data) => request.post('/park/property/batch/preview', data),
    batchCreate: (data) => request.post('/park/property/batch', data),
    export: (params) => request.get('/park/property/export', { params }),
    stats: () => request.get('/park/property/stats'),
    getTemplate: () => request.get('/park/property/template', { responseType: 'blob' }),
    importData: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return request.post('/park/property/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    getDetail: (id) => request.get(`/park/property/${id}/detail`),
    compare: (ids) => request.post('/park/property/compare', ids)
  },

  parkOwner: {
    list: (params) => request.get('/park/owner/list', { params }),
    getById: (id) => request.get(`/park/owner/${id}`),
    add: (data) => request.post('/park/owner', data),
    update: (data) => request.put('/park/owner', data),
    delete: (id) => request.delete(`/park/owner/${id}`),
    audit: (data) => request.post('/park/owner/audit', data),
    bindProperty: (data) => request.post('/park/owner/bindProperty', data),
    unbindProperty: (data) => request.post('/park/owner/unbindProperty', data),
    stats: () => request.get('/park/owner/stats'),
    getTemplate: () => request.get('/park/owner/template', { responseType: 'blob' }),
    importData: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return request.post('/park/owner/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    export: (params) => request.get('/park/owner/export', { params }),
    getUnboundProperties: () => request.get('/park/property/list')
  },

  parkPropertyBatch: {
    changeStatus: (data) => request.put('/park/property/batch/status', data),
    changeAttribute: (data) => request.put('/park/property/batch/attribute', data),
    logList: (params) => request.get('/park/property/batch/log', { params }),
    logDetail: (id) => request.get(`/park/property/batch/log/${id}`),
    logItems: (batchNo) => request.get(`/park/property/batch/log/items/${batchNo}`)
  },

  parkPropertyTransfer: {
    apply: (data) => request.post('/park/property/transfer/apply', data),
    audit: (data) => request.post('/park/property/transfer/audit', data),
    list: (params) => request.get('/park/property/transfer/list', { params }),
    getById: (id) => request.get(`/park/property/transfer/${id}`),
    history: (propertyId) => request.get(`/park/property/transfer/history/${propertyId}`)
  },

  parkPropertyTag: {
    list: () => request.get('/park/property/tag/list'),
    all: () => request.get('/park/property/tag/all'),
    add: (data) => request.post('/park/property/tag', data),
    update: (data) => request.put('/park/property/tag', data),
    delete: (id) => request.delete(`/park/property/tag/${id}`),
    setPropertyTags: (data) => request.post('/park/property/tag/property', data),
    getPropertyTags: (propertyId) => request.get(`/park/property/tag/property/${propertyId}`),
    filter: (params) => request.get('/park/property/tag/filter', { params })
  },

  parkPropertyVacancy: {
    stats: () => request.get('/park/property/vacancy/stats'),
    longTermList: (params) => request.get('/park/property/vacancy/longTerm', { params }),
    recordReason: (data) => request.post('/park/property/vacancy/reason', data),
    exportList: () => request.get('/park/property/vacancy/export')
  },

  parkParking: {
    list: (params) => request.get('/park/parking/list', { params }),
    getById: (id) => request.get(`/park/parking/${id}`),
    add: (data) => request.post('/park/parking', data),
    update: (data) => request.put('/park/parking', data),
    delete: (id) => request.delete(`/park/parking/${id}`),
    batchDelete: (ids) => request.delete('/park/parking/batch', { data: ids }),
    changeStatus: (id, status, reason) => request.put('/park/parking/status', null, { params: { id, status, reason } }),
    batchChangeStatus: (ids, status, reason) => request.put('/park/parking/status/batch', ids, { params: { status, reason } }),
    getStatusLogs: (spaceId) => request.get(`/park/parking/statusLog/${spaceId}`),
    previewBatch: (data) => request.post('/park/parking/batch/preview', data),
    batchCreate: (data) => request.post('/park/parking/batch', data),
    export: (params) => request.get('/park/parking/export', { params }),
    stats: () => request.get('/park/parking/stats'),
    getTemplate: () => request.get('/park/parking/template', { responseType: 'blob' }),
    importData: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return request.post('/park/parking/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }
  },

  parkTenant: {
    list: (params) => request.get('/park/tenant/list', { params }),
    getById: (id) => request.get(`/park/tenant/${id}`),
    add: (data) => request.post('/park/tenant', data),
    update: (data) => request.put('/park/tenant', data),
    delete: (id) => request.delete(`/park/tenant/${id}`),
    setBlacklist: (data) => request.post('/park/tenant/blacklist', data),
    stats: () => request.get('/park/tenant/stats'),
    getTemplate: () => request.get('/park/tenant/template', { responseType: 'blob' }),
    importData: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return request.post('/park/tenant/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    export: (params) => request.get('/park/tenant/export', { params })
  },

  parkLeaseContract: {
    list: (params) => request.get('/park/lease-contract/list', { params }),
    getById: (id) => request.get(`/park/lease-contract/${id}`),
    add: (data) => request.post('/park/lease-contract', data),
    update: (data) => request.put('/park/lease-contract', data),
    audit: (data) => request.post('/park/lease-contract/audit', data),
    renew: (data) => request.post('/park/lease-contract/renew', data),
    terminate: (data) => request.post('/park/lease-contract/terminate', data),
    auditTermination: (data) => request.post('/park/lease-contract/audit-termination', data),
    checkExpired: () => request.post('/park/lease-contract/check-expired')
  }
}

export default api
