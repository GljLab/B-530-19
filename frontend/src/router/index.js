import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 静态路由
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User' }
      },
      {
        path: 'park/overview',
        name: 'ParkOverview',
        component: () => import('@/views/park/Overview.vue'),
        meta: { title: '园区概览', icon: 'HomeFilled', permission: 'park:overview:list' }
      },
      {
        path: 'park/building',
        name: 'ParkBuilding',
        component: () => import('@/views/park/Building.vue'),
        meta: { title: '楼宇管理', icon: 'OfficeBuilding', permission: 'park:building:list' }
      },
      {
        path: 'park/floor',
        name: 'ParkFloor',
        component: () => import('@/views/park/Floor.vue'),
        meta: { title: '楼层管理', icon: 'Grid', permission: 'park:floor:list' }
      },
      {
        path: 'park/dashboard',
        name: 'ParkDashboard',
        component: () => import('@/views/park/Dashboard.vue'),
        meta: { title: '统计看板', icon: 'DataAnalysis', permission: 'park:dashboard:list' }
      },
      {
        path: 'park/property',
        name: 'ParkProperty',
        component: () => import('@/views/park/PropertyList.vue'),
        meta: { title: '房产管理', icon: 'House', permission: 'park:property:list' }
      },
      {
        path: 'park/property/detail/:id',
        name: 'ParkPropertyDetail',
        component: () => import('@/views/park/PropertyDetail.vue'),
        meta: { title: '房产详情', icon: 'House', permission: 'park:property:list', hidden: true }
      },
      {
        path: 'park/property/stats',
        name: 'ParkPropertyStats',
        component: () => import('@/views/park/PropertyStats.vue'),
        meta: { title: '房产统计', icon: 'DataLine', permission: 'park:property:stats' }
      },
      {
        path: 'park/owner',
        name: 'ParkOwner',
        component: () => import('@/views/owner/OwnerList.vue'),
        meta: { title: '业主管理', icon: 'UserFilled', permission: 'park:owner:list' }
      },
      {
        path: 'park/owner/add',
        name: 'ParkOwnerAdd',
        component: () => import('@/views/owner/OwnerForm.vue'),
        meta: { title: '新增业主', icon: 'UserFilled', permission: 'park:owner:add', hidden: true }
      },
      {
        path: 'park/owner/edit/:id',
        name: 'ParkOwnerEdit',
        component: () => import('@/views/owner/OwnerForm.vue'),
        meta: { title: '编辑业主', icon: 'UserFilled', permission: 'park:owner:edit', hidden: true }
      },
      {
        path: 'park/owner/detail/:id',
        name: 'ParkOwnerDetail',
        component: () => import('@/views/owner/OwnerDetail.vue'),
        meta: { title: '业主详情', icon: 'UserFilled', permission: 'park:owner:list', hidden: true }
      },
      {
        path: 'park/owner/stats',
        name: 'ParkOwnerStats',
        component: () => import('@/views/owner/OwnerStats.vue'),
        meta: { title: '业主统计', icon: 'DataLine', permission: 'park:owner:stats' }
      },
      {
        path: 'park/transfer',
        name: 'ParkTransfer',
        component: () => import('@/views/park/TransferList.vue'),
        meta: { title: '房产转让', icon: 'Switch', permission: 'park:transfer:list' }
      },
      {
        path: 'park/transfer/audit',
        name: 'ParkTransferAudit',
        component: () => import('@/views/park/TransferAudit.vue'),
        meta: { title: '转让审核', icon: 'CircleCheck', permission: 'park:transfer:audit' }
      },
      {
        path: 'park/tag',
        name: 'ParkTag',
        component: () => import('@/views/park/TagManage.vue'),
        meta: { title: '标签管理', icon: 'PriceTag', permission: 'park:tag:list' }
      },
      {
        path: 'park/property/compare',
        name: 'ParkPropertyCompare',
        component: () => import('@/views/park/PropertyCompare.vue'),
        meta: { title: '房产对比', icon: 'ScaleToOriginal', permission: 'park:property:compare', hidden: true }
      },
      {
        path: 'park/building/detail/:id',
        name: 'ParkBuildingDetail',
        component: () => import('@/views/park/BuildingDetail.vue'),
        meta: { title: '楼宇详情', icon: 'OfficeBuilding', permission: 'park:building:list', hidden: true }
      },
      {
        path: 'park/floor/detail/:id',
        name: 'ParkFloorDetail',
        component: () => import('@/views/park/FloorDetail.vue'),
        meta: { title: '楼层详情', icon: 'Grid', permission: 'park:floor:list', hidden: true }
      },
      {
        path: 'park/parking',
        name: 'ParkParking',
        component: () => import('@/views/park/ParkingList.vue'),
        meta: { title: '车位管理', icon: 'Van', permission: 'park:parking:list' }
      },
      {
        path: 'park/parking/detail/:id',
        name: 'ParkParkingDetail',
        component: () => import('@/views/park/ParkingDetail.vue'),
        meta: { title: '车位详情', icon: 'Van', permission: 'park:parking:list', hidden: true }
      },
      {
        path: 'park/parking/add',
        name: 'ParkParkingAdd',
        component: () => import('@/views/park/ParkingForm.vue'),
        meta: { title: '新增车位', icon: 'Van', permission: 'park:parking:add', hidden: true }
      },
      {
        path: 'park/parking/edit/:id',
        name: 'ParkParkingEdit',
        component: () => import('@/views/park/ParkingForm.vue'),
        meta: { title: '编辑车位', icon: 'Van', permission: 'park:parking:edit', hidden: true }
      },
      {
        path: 'park/parking/stats',
        name: 'ParkParkingStats',
        component: () => import('@/views/park/ParkingStats.vue'),
        meta: { title: '车位统计', icon: 'DataLine', permission: 'park:parking:stats' }
      },
      {
        path: 'park/tenant',
        name: 'ParkTenant',
        component: () => import('@/views/tenant/TenantList.vue'),
        meta: { title: '租户管理', icon: 'User', permission: 'park:tenant:list' }
      },
      {
        path: 'park/tenant/add',
        name: 'ParkTenantAdd',
        component: () => import('@/views/tenant/TenantForm.vue'),
        meta: { title: '新增租户', icon: 'User', permission: 'park:tenant:add', hidden: true }
      },
      {
        path: 'park/tenant/edit/:id',
        name: 'ParkTenantEdit',
        component: () => import('@/views/tenant/TenantForm.vue'),
        meta: { title: '编辑租户', icon: 'User', permission: 'park:tenant:edit', hidden: true }
      },
      {
        path: 'park/tenant/detail/:id',
        name: 'ParkTenantDetail',
        component: () => import('@/views/tenant/TenantDetail.vue'),
        meta: { title: '租户详情', icon: 'User', permission: 'park:tenant:query', hidden: true }
      },
      {
        path: 'park/tenant/stats',
        name: 'ParkTenantStats',
        component: () => import('@/views/tenant/TenantStats.vue'),
        meta: { title: '租户统计', icon: 'DataLine', permission: 'park:tenant:stats' }
      },
      {
        path: 'park/contract',
        name: 'ParkLeaseContract',
        component: () => import('@/views/tenant/ContractList.vue'),
        meta: { title: '合约管理', icon: 'Document', permission: 'park:contract:list' }
      },
      {
        path: 'park/contract/add',
        name: 'ParkLeaseContractAdd',
        component: () => import('@/views/tenant/ContractForm.vue'),
        meta: { title: '新增合约', icon: 'Document', permission: 'park:contract:add', hidden: true }
      },
      {
        path: 'park/contract/edit/:id',
        name: 'ParkLeaseContractEdit',
        component: () => import('@/views/tenant/ContractForm.vue'),
        meta: { title: '编辑合约', icon: 'Document', permission: 'park:contract:edit', hidden: true }
      },
      {
        path: 'park/contract/detail/:id',
        name: 'ParkLeaseContractDetail',
        component: () => import('@/views/tenant/ContractDetail.vue'),
        meta: { title: '合约详情', icon: 'Document', permission: 'park:contract:query', hidden: true }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user:list' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Avatar', permission: 'system:role:list' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', permission: 'system:menu:list' }
      },
      {
        path: 'system/dataPerm',
        name: 'SystemDataPerm',
        component: () => import('@/views/system/DataPermission.vue'),
        meta: { title: '数据权限', icon: 'Lock', permission: 'system:dataPerm:list' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧园区物业管理系统` : '智慧园区物业管理系统'
  
  const userStore = useUserStore()
  const token = userStore.token
  
  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 需要认证的页面
  if (!token) {
    next('/login')
    return
  }
  
  // 检查用户信息是否已加载
  if (!userStore.user) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      userStore.logout()
      next('/login')
      return
    }
  }
  
  // 检查权限
  if (to.meta.permission) {
    const hasPermission = userStore.hasPermission(to.meta.permission)
    if (!hasPermission) {
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
