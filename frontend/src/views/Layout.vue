<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">智慧园区物业系统</span>
        <span v-else>园</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="/park" v-if="hasParkPermission">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>园区管理</span>
          </template>
          
          <el-menu-item index="/park/overview" v-if="hasPermission('park:overview:list')">
            <el-icon><HomeFilled /></el-icon>
            <span>园区概览</span>
          </el-menu-item>
          
          <el-menu-item index="/park/building" v-if="hasPermission('park:building:list')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>楼宇管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/floor" v-if="hasPermission('park:floor:list')">
            <el-icon><Grid /></el-icon>
            <span>楼层管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/dashboard" v-if="hasPermission('park:dashboard:list')">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计看板</span>
          </el-menu-item>
          
          <el-menu-item index="/park/property" v-if="hasPermission('park:property:list')">
            <el-icon><House /></el-icon>
            <span>房产管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/property/stats" v-if="hasPermission('park:property:stats')">
            <el-icon><DataLine /></el-icon>
            <span>房产统计</span>
          </el-menu-item>
          
          <el-menu-item index="/park/owner" v-if="hasPermission('park:owner:list')">
            <el-icon><UserFilled /></el-icon>
            <span>业主管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/owner/stats" v-if="hasPermission('park:owner:stats')">
            <el-icon><DataLine /></el-icon>
            <span>业主统计</span>
          </el-menu-item>
          
          <el-menu-item index="/park/parking" v-if="hasPermission('park:parking:list')">
            <el-icon><Van /></el-icon>
            <span>车位管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/parking/stats" v-if="hasPermission('park:parking:stats')">
            <el-icon><DataLine /></el-icon>
            <span>车位统计</span>
          </el-menu-item>
          
          <el-menu-item index="/park/tenant" v-if="hasPermission('park:tenant:list')">
            <el-icon><User /></el-icon>
            <span>租户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/contract" v-if="hasPermission('park:contract:list')">
            <el-icon><Document /></el-icon>
            <span>合约管理</span>
          </el-menu-item>
          
          <el-menu-item index="/park/tenant/stats" v-if="hasPermission('park:tenant:stats')">
            <el-icon><DataLine /></el-icon>
            <span>租户统计</span>
          </el-menu-item>
          
          <el-menu-item index="/park/transfer" v-if="hasPermission('park:transfer:list')">
            <el-icon><Switch /></el-icon>
            <span>房产转让</span>
          </el-menu-item>
          
          <el-menu-item index="/park/transfer/audit" v-if="hasPermission('park:transfer:audit')">
            <el-icon><CircleCheck /></el-icon>
            <span>转让审核</span>
          </el-menu-item>
          
          <el-menu-item index="/park/tag" v-if="hasPermission('park:tag:list')">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/system" v-if="hasSystemPermission">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          
          <el-menu-item index="/system/user" v-if="hasPermission('system:user:list')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/role" v-if="hasPermission('system:role:list')">
            <el-icon><Avatar /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/menu" v-if="hasPermission('system:menu:list')">
            <el-icon><Menu /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/dataPerm" v-if="hasPermission('system:dataPerm:list')">
            <el-icon><Lock /></el-icon>
            <span>数据权限</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="layout-main">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title && $route.path !== '/dashboard'">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="nickname">{{ userStore.user?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  HomeFilled, Setting, User, Avatar, Menu, Lock,
  Expand, Fold, ArrowDown, SwitchButton,
  OfficeBuilding, Grid, DataAnalysis, House, DataLine, UserFilled,
  Switch, CircleCheck, PriceTag, Van, Document
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const hasPermission = (permission) => {
  return userStore.hasPermission(permission)
}

const hasParkPermission = computed(() => {
  return hasPermission('park:overview:list') ||
         hasPermission('park:building:list') ||
         hasPermission('park:floor:list') ||
         hasPermission('park:dashboard:list') ||
         hasPermission('park:property:list') ||
         hasPermission('park:property:stats') ||
         hasPermission('park:owner:list') ||
         hasPermission('park:owner:stats') ||
         hasPermission('park:parking:list') ||
         hasPermission('park:parking:stats') ||
         hasPermission('park:tenant:list') ||
         hasPermission('park:contract:list') ||
         hasPermission('park:tenant:stats') ||
         hasPermission('park:transfer:list') ||
         hasPermission('park:transfer:audit') ||
         hasPermission('park:tag:list')
})

const hasSystemPermission = computed(() => {
  return hasPermission('system:user:list') ||
         hasPermission('system:role:list') ||
         hasPermission('system:menu:list') ||
         hasPermission('system:dataPerm:list')
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.nickname {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
