<template>
  <div class="property-container">
    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">房产总数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value success">{{ stats.typeDistribution?.residential || 0 }}</div>
            <div class="stat-label">住宅</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value warning">{{ stats.typeDistribution?.commercial || 0 }}</div>
            <div class="stat-label">商业</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ occupancyRate }}%</div>
            <div class="stat-label">入住率</div>
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
            <div class="stat-value" style="color: #909399;">{{ stats.totalBuildingArea || 0 }}</div>
            <div class="stat-label">总建筑面积(㎡)</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" size="default">
        <el-form-item label="房产编号">
          <el-input v-model="queryForm.propertyCode" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="楼宇">
          <el-select v-model="queryForm.buildingId" placeholder="全部" clearable style="width: 140px;" @change="handleBuildingChange">
            <el-option v-for="b in buildingList" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="queryForm.floorId" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="f in floorList" :key="f.id" :label="f.floorNumber" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.propertyType" placeholder="全部" clearable style="width: 100px;">
            <el-option label="住宅" :value="1" />
            <el-option label="商业" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待售" :value="1" />
            <el-option label="已售未交付" :value="2" />
            <el-option label="业主自住" :value="3" />
            <el-option label="业主出租" :value="4" />
            <el-option label="空置" :value="5" />
            <el-option label="装修中" :value="6" />
            <el-option label="查封" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="户型">
          <el-select v-model="queryForm.houseType" placeholder="全部" clearable style="width: 100px;">
            <el-option label="一室" value="一室" />
            <el-option label="两室" value="两室" />
            <el-option label="三室" value="三室" />
            <el-option label="四室及以上" value="四室及以上" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积">
          <el-select v-model="queryForm.areaRange" placeholder="全部" clearable style="width: 120px;">
            <el-option label="50㎡以下" value="0-50" />
            <el-option label="50-80㎡" value="50-80" />
            <el-option label="80-120㎡" value="80-120" />
            <el-option label="120-150㎡" value="120-150" />
            <el-option label="150㎡以上" value="150-9999" />
          </el-select>
        </el-form-item>
        <el-form-item label="朝向">
          <el-select v-model="queryForm.orientation" placeholder="全部" clearable style="width: 100px;">
            <el-option label="东" value="东" />
            <el-option label="南" value="南" />
            <el-option label="西" value="西" />
            <el-option label="北" value="北" />
            <el-option label="东南" value="东南" />
            <el-option label="西南" value="西南" />
            <el-option label="东北" value="东北" />
            <el-option label="西北" value="西北" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="queryForm.tagIds" placeholder="全部" clearable multiple style="width: 200px;" collapse-tags collapse-tags-tooltip>
            <el-option v-for="t in tagList" :key="t.id" :label="t.tagName" :value="t.id" />
          </el-select>
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
          <span>房产列表</span>
          <div class="header-buttons">
            <el-button v-if="hasAddPermission" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增房产
            </el-button>
            <el-button v-if="hasBatchAddPermission" type="success" @click="handleBatchCreate">
              <el-icon><DocumentAdd /></el-icon>批量创建
            </el-button>
            <el-button v-if="hasImportPermission" type="warning" @click="handleImport">
              <el-icon><Upload /></el-icon>导入房产
            </el-button>
            <el-button v-if="hasExportPermission" type="info" @click="handleExport">
              <el-icon><Download /></el-icon>导出房产
            </el-button>
            <template v-if="selectedIds.length > 0">
              <el-divider direction="vertical" />
              <span style="color: #409EFF; font-size: 14px;">已选择 {{ selectedIds.length }} 个房产</span>
              <el-button v-if="hasBatchStatusPermission" type="warning" @click="handleBatchStatusDialog">
                <el-icon><Edit /></el-icon>批量改状态
              </el-button>
              <el-button v-if="hasBatchAttrPermission" type="warning" @click="handleBatchAttrDialog">
                <el-icon><Setting /></el-icon>批量改属性
              </el-button>
              <el-button v-if="hasComparePermission && selectedIds.length >= 2 && selectedIds.length <= 4" type="primary" @click="handleCompare">
                <el-icon><ScaleToOriginal /></el-icon>对比房产
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
        <el-table-column prop="propertyCode" label="房产编号" width="120" sortable="custom">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.propertyCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="buildingName" label="楼宇" width="120" />
        <el-table-column prop="floorNumber" label="楼层" width="80" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            {{ getPropertyTypeLabel(row.propertyType, row.propertySubType) }}
          </template>
        </el-table-column>
        <el-table-column prop="buildingArea" label="建筑面积(㎡)" width="120" sortable="custom">
          <template #default="{ row }">
            {{ row.buildingArea || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="houseType" label="户型" width="100">
          <template #default="{ row }">
            {{ row.houseType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="orientation" label="朝向" width="80">
          <template #default="{ row }">
            {{ row.orientation || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
            <el-icon v-if="row.isLongTermVacant === 1" style="color: #F56C6C; margin-left: 4px;" title="长期空置"><Warning /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <template v-if="row.tagList && row.tagList.length > 0">
              <el-tag v-for="(tag, idx) in row.tagList.slice(0, 3)" :key="tag.id" size="small" :color="tag.tagColor" style="color: #fff; margin-right: 4px;">{{ tag.tagName }}</el-tag>
              <el-tag v-if="row.tagList.length > 3" size="small" type="info">+{{ row.tagList.length - 3 }}</el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="propertyRightType" label="产权性质" width="100">
          <template #default="{ row }">
            {{ row.propertyRightType || '-' }}
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
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="formVisible" :title="formTitle" width="700px" @close="handleFormClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="房产编号" prop="propertyCode">
                  <el-input v-model="formData.propertyCode" :disabled="isEdit" placeholder="如：1-101" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="房产状态" prop="status" v-if="!isEdit">
                  <el-select v-model="formData.status" style="width: 100%;">
                    <el-option label="待售" :value="1" />
                    <el-option label="已售未交付" :value="2" />
                    <el-option label="业主自住" :value="3" />
                    <el-option label="业主出租" :value="4" />
                    <el-option label="空置" :value="5" />
                    <el-option label="装修中" :value="6" />
                    <el-option label="查封" :value="7" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="楼宇" prop="buildingId">
                  <el-select v-model="formData.buildingId" placeholder="请选择" style="width: 100%;" :disabled="isEdit" @change="handleFormBuildingChange">
                    <el-option v-for="b in buildingList" :key="b.id" :label="b.buildingName" :value="b.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="楼层" prop="floorId">
                  <el-select v-model="formData.floorId" placeholder="请选择" style="width: 100%;" :disabled="isEdit">
                    <el-option v-for="f in formFloorList" :key="f.id" :label="f.floorNumber" :value="f.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="房产类型" prop="propertyType">
                  <el-select v-model="formData.propertyType" placeholder="请选择" style="width: 100%;" :disabled="isEdit">
                    <el-option label="住宅" :value="1" />
                    <el-option label="商业" :value="2" />
                    <el-option label="其他" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="子类型" prop="propertySubType">
                  <el-select v-model="formData.propertySubType" placeholder="请选择" style="width: 100%;">
                    <el-option v-for="t in subTypeOptions" :key="t" :label="t" :value="t" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="房产属性" name="attr">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="建筑面积" prop="buildingArea">
                  <el-input-number v-model="formData.buildingArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="套内面积" prop="insideArea">
                  <el-input-number v-model="formData.insideArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="公摊面积" prop="sharedArea">
                  <el-input-number v-model="formData.sharedArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="户型" prop="houseType">
                  <el-select v-model="formData.houseType" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="一室" value="一室" />
                    <el-option label="两室" value="两室" />
                    <el-option label="三室" value="三室" />
                    <el-option label="四室及以上" value="四室及以上" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="朝向" prop="orientation">
                  <el-select v-model="formData.orientation" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="东" value="东" />
                    <el-option label="南" value="南" />
                    <el-option label="西" value="西" />
                    <el-option label="北" value="北" />
                    <el-option label="东南" value="东南" />
                    <el-option label="西南" value="西南" />
                    <el-option label="东北" value="东北" />
                    <el-option label="西北" value="西北" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="装修状况" prop="decorationStatus">
                  <el-select v-model="formData.decorationStatus" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="毛坯" value="毛坯" />
                    <el-option label="简装" value="简装" />
                    <el-option label="精装" value="精装" />
                    <el-option label="豪装" value="豪装" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="产权信息" name="right">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权性质" prop="propertyRightType">
                  <el-select v-model="formData.propertyRightType" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="商品房" value="商品房" />
                    <el-option label="经济适用房" value="经济适用房" />
                    <el-option label="公租房" value="公租房" />
                    <el-option label="商铺产权" value="商铺产权" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="产权年限" prop="propertyRightYears">
                  <el-select v-model="formData.propertyRightYears" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="40年" :value="40" />
                    <el-option label="50年" :value="50" />
                    <el-option label="70年" :value="70" />
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
                <el-form-item label="交付日期" prop="deliveryDate">
                  <el-date-picker v-model="formData.deliveryDate" type="date" placeholder="选择日期" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="其他信息" name="other">
            <el-form-item label="特殊标识">
              <el-checkbox-group v-model="formData.specialTagList">
                <el-checkbox label="学区房">学区房</el-checkbox>
                <el-checkbox label="人才房">人才房</el-checkbox>
                <el-checkbox label="拆迁房">拆迁房</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" :rows="4" placeholder="请输入备注" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="修改状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentProperty?.status)">{{ getStatusLabel(currentProperty?.status) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" placeholder="请选择" style="width: 100%;">
            <el-option label="待售" :value="1" />
            <el-option label="已售未交付" :value="2" />
            <el-option label="业主自住" :value="3" />
            <el-option label="业主出租" :value="4" />
            <el-option label="空置" :value="5" />
            <el-option label="装修中" :value="6" />
            <el-option label="查封" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="changeReason" type="textarea" :rows="3" placeholder="请输入变更原因（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeStatus">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量创建房产" width="800px" @close="handleBatchClose">
      <el-steps :active="batchStep" finish-status="success" align-center>
        <el-step title="选择楼宇楼层" />
        <el-step title="选择房产类型" />
        <el-step title="设置房号规则" />
        <el-step title="设置默认属性" />
        <el-step title="预览确认" />
      </el-steps>

      <div class="batch-step-content">
        <div v-if="batchStep === 0">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-form-item label="选择楼宇">
              <el-select v-model="batchForm.buildingId" placeholder="请选择" style="width: 300px;" @change="handleBatchBuildingChange">
                <el-option v-for="b in buildingList" :key="b.id" :label="b.buildingName" :value="b.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="选择楼层">
              <el-select v-model="batchForm.floorId" placeholder="请选择" style="width: 300px;">
                <el-option v-for="f in batchFloorList" :key="f.id" :label="f.floorNumber" :value="f.id" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="batchStep === 1">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-form-item label="房产类型">
              <el-radio-group v-model="batchForm.propertyType">
                <el-radio :value="1">住宅</el-radio>
                <el-radio :value="2">商业</el-radio>
                <el-radio :value="3">其他</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="子类型">
              <el-select v-model="batchForm.propertySubType" placeholder="请选择" style="width: 300px;">
                <el-option v-for="t in subTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="batchStep === 2">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-form-item label="房号前缀">
              <el-input v-model="batchForm.prefix" placeholder="如：1-2" style="width: 300px;" />
            </el-form-item>
            <el-form-item label="起始号">
              <el-input-number v-model="batchForm.startNum" :min="1" :max="999" style="width: 300px;" />
            </el-form-item>
            <el-form-item label="结束号">
              <el-input-number v-model="batchForm.endNum" :min="1" :max="999" style="width: 300px;" />
            </el-form-item>
          </el-form>
        </div>

        <div v-if="batchStep === 3">
          <el-form label-width="100px" style="margin-top: 30px;">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="建筑面积">
                  <el-input-number v-model="batchForm.buildingArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="套内面积">
                  <el-input-number v-model="batchForm.insideArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="公摊面积">
                  <el-input-number v-model="batchForm.sharedArea" :min="0" :precision="2" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="户型">
                  <el-select v-model="batchForm.houseType" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="一室" value="一室" />
                    <el-option label="两室" value="两室" />
                    <el-option label="三室" value="三室" />
                    <el-option label="四室及以上" value="四室及以上" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="朝向">
                  <el-select v-model="batchForm.orientation" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="东" value="东" />
                    <el-option label="南" value="南" />
                    <el-option label="西" value="西" />
                    <el-option label="北" value="北" />
                    <el-option label="东南" value="东南" />
                    <el-option label="西南" value="西南" />
                    <el-option label="东北" value="东北" />
                    <el-option label="西北" value="西北" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="装修状况">
                  <el-select v-model="batchForm.decorationStatus" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="毛坯" value="毛坯" />
                    <el-option label="简装" value="简装" />
                    <el-option label="精装" value="精装" />
                    <el-option label="豪装" value="豪装" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产权性质">
                  <el-select v-model="batchForm.propertyRightType" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="商品房" value="商品房" />
                    <el-option label="经济适用房" value="经济适用房" />
                    <el-option label="公租房" value="公租房" />
                    <el-option label="商铺产权" value="商铺产权" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="产权年限">
                  <el-select v-model="batchForm.propertyRightYears" placeholder="请选择" clearable style="width: 100%;">
                    <el-option label="40年" :value="40" />
                    <el-option label="50年" :value="50" />
                    <el-option label="70年" :value="70" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div v-if="batchStep === 4">
          <div style="margin: 20px 0;">
            <el-alert title="即将创建以下房产，请确认" type="info" show-icon />
          </div>
          <el-table :data="batchPreviewList" border stripe max-height="300">
            <el-table-column prop="propertyCode" label="房产编号" width="150" />
            <el-table-column prop="buildingName" label="楼宇" width="120" />
            <el-table-column prop="floorNumber" label="楼层" width="80" />
            <el-table-column prop="propertySubType" label="类型" width="100" />
            <el-table-column prop="buildingArea" label="建筑面积(㎡)" width="120" />
            <el-table-column prop="houseType" label="户型" width="80" />
            <el-table-column prop="orientation" label="朝向" width="80" />
            <el-table-column label="状态" width="80">
              <template #default>
                <el-tag type="warning" size="small">待售</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 10px; text-align: right; color: #606266;">
            共 {{ batchPreviewList.length }} 套房产
          </div>
          <div v-if="batchResult" style="margin-top: 20px;">
            <el-alert
              :title="'创建完成：成功 ' + batchResult.successCount + ' 套，失败 ' + batchResult.failCount + ' 套'"
              :type="batchResult.failCount > 0 ? 'warning' : 'success'"
              show-icon />
            <el-table v-if="batchResult.failList && batchResult.failList.length > 0" :data="batchResult.failList" style="margin-top: 10px;" size="small">
              <el-table-column prop="propertyCode" label="房产编号" width="150" />
              <el-table-column prop="reason" label="失败原因" />
            </el-table>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button v-if="batchStep > 0" @click="batchStep--">上一步</el-button>
        <el-button v-if="batchStep < 4" type="primary" @click="handleBatchNext">下一步</el-button>
        <el-button v-if="batchStep === 4 && !batchResult" type="primary" @click="confirmBatchCreate">确认创建</el-button>
        <el-button v-if="batchStep === 4 && batchResult" type="primary" @click="batchDialogVisible = false">完成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="导入房产" width="700px">
      <div class="import-content">
        <div style="margin-bottom: 20px;">
          <el-button type="primary" @click="downloadTemplate">
            <el-icon><Download /></el-icon>下载导入模板
          </el-button>
        </div>
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          :disabled="!!importResult"
          accept=".xlsx,.xls">
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 xlsx/xls 格式文件
            </div>
          </template>
        </el-upload>
        <div v-if="importResult" style="margin-top: 20px;">
          <el-alert
            :title="'导入完成：成功 ' + importResult.successCount + ' 条，失败 ' + importResult.failCount + ' 条'"
            :type="importResult.failCount > 0 ? 'warning' : 'success'"
            show-icon />
          <div v-if="importResult.failList && importResult.failList.length > 0" style="margin-top: 15px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
              <span style="font-weight: bold;">失败列表</span>
              <el-button size="small" type="primary" @click="downloadFailList">
                <el-icon><Download /></el-icon>下载失败数据
              </el-button>
            </div>
            <el-table :data="importResult.failList" border size="small" max-height="200">
              <el-table-column prop="rowNum" label="行号" width="80" />
              <el-table-column prop="propertyCode" label="房产编号" width="120" />
              <el-table-column prop="reason" label="失败原因" />
            </el-table>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">{{ importResult ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!importResult" type="primary" :loading="importLoading" @click="handleImportConfirm">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchStatusDialogVisible" title="批量修改状态" width="700px">
      <el-alert v-if="batchCheckResult.failItems && batchCheckResult.failItems.length > 0"
        :title="'有 ' + batchCheckResult.failItems.length + ' 个房产不符合条件'" type="warning" show-icon style="margin-bottom: 15px;" />
      <el-table :data="batchCheckResult.items" border size="small" max-height="250"
        :row-class-name="({row}) => row.canOperate === false ? 'warning-row' : ''">
        <el-table-column prop="propertyCode" label="房产编号" width="120" />
        <el-table-column prop="buildingName" label="楼宇" width="100" />
        <el-table-column prop="floorNumber" label="楼层" width="80" />
        <el-table-column label="当前状态" width="100">
          <template #default="{ row }">{{ getStatusLabel(row.currentStatus) }}</template>
        </el-table-column>
        <el-table-column label="是否可操作" width="100">
          <template #default="{ row }">
            <el-tag :type="row.canOperate !== false ? 'success' : 'danger'" size="small">
              {{ row.canOperate !== false ? '可以' : '不可' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="原因" />
      </el-table>
      <el-form label-width="100px" style="margin-top: 15px;">
        <el-form-item label="目标状态" required>
          <el-select v-model="batchStatusForm.targetStatus" placeholder="请选择" style="width: 200px;">
            <el-option label="待售" :value="1" />
            <el-option label="业主自住" :value="3" />
            <el-option label="业主出租" :value="4" />
            <el-option label="空置" :value="5" />
            <el-option label="装修中" :value="6" />
            <el-option label="查封" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作原因" required>
          <el-input v-model="batchStatusForm.operateReason" type="textarea" :rows="3" placeholder="请输入操作原因（必填，限500字）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="confirmBatchStatus">确定执行</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchAttrDialogVisible" title="批量修改属性" width="700px">
      <el-form label-width="100px">
        <el-form-item label="装修状况">
          <el-select v-model="batchAttrForm.targetDecorationStatus" placeholder="不修改" clearable style="width: 200px;">
            <el-option label="毛坯" value="毛坯" />
            <el-option label="简装" value="简装" />
            <el-option label="精装" value="精装" />
            <el-option label="豪装" value="豪装" />
          </el-select>
        </el-form-item>
        <el-form-item label="特殊标识">
          <el-select v-model="batchAttrForm.targetSpecialTags" placeholder="不修改" clearable style="width: 200px;">
            <el-option label="学区房" value="学区房" />
            <el-option label="人才房" value="人才房" />
            <el-option label="拆迁房" value="拆迁房" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作原因" required>
          <el-input v-model="batchAttrForm.operateReason" type="textarea" :rows="3" placeholder="请输入操作原因（必填，限500字）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchAttrDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="confirmBatchAttr">确定执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, DocumentAdd, Upload, Download, Edit, Delete, Picture,
  UploadFilled, Setting, ScaleToOriginal, Close, Warning
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const stats = ref({})
const buildingList = ref([])
const floorList = ref([])
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  propertyCode: '',
  buildingId: null,
  floorId: null,
  propertyType: null,
  status: null,
  houseType: '',
  orientation: '',
  areaRange: '',
  sortField: '',
  sortOrder: '',
  tagIds: []
})

const formVisible = ref(false)
const formTitle = ref('新增房产')
const isEdit = ref(false)
const activeTab = ref('basic')
const formRef = ref(null)
const formFloorList = ref([])

const formData = reactive({
  id: null,
  propertyCode: '',
  buildingId: null,
  floorId: null,
  propertyType: 1,
  propertySubType: '',
  buildingArea: null,
  insideArea: null,
  sharedArea: null,
  houseType: '',
  orientation: '',
  decorationStatus: '',
  propertyRightType: '',
  propertyRightYears: null,
  propertyCertNo: '',
  deliveryDate: null,
  status: 1,
  specialTagList: [],
  remark: ''
})

const formRules = {
  propertyCode: [{ required: true, message: '请输入房产编号', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择楼宇', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  propertyType: [{ required: true, message: '请选择房产类型', trigger: 'change' }]
}

const statusDialogVisible = ref(false)
const currentProperty = ref(null)
const newStatus = ref(null)
const changeReason = ref('')

const batchDialogVisible = ref(false)
const batchStep = ref(0)
const batchFloorList = ref([])
const batchPreviewList = ref([])
const batchResult = ref(null)

const batchForm = reactive({
  buildingId: null,
  floorId: null,
  propertyType: 1,
  propertySubType: '',
  prefix: '',
  startNum: 1,
  endNum: 10,
  buildingArea: null,
  insideArea: null,
  sharedArea: null,
  houseType: '两室',
  orientation: '南',
  decorationStatus: '毛坯',
  propertyRightType: '商品房',
  propertyRightYears: 70
})

const importDialogVisible = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const importLoading = ref(false)

const tagList = ref([])
const batchStatusDialogVisible = ref(false)
const batchAttrDialogVisible = ref(false)
const batchLoading = ref(false)
const batchCheckResult = reactive({ items: [], failItems: [] })
const batchStatusForm = reactive({ targetStatus: null, operateReason: '' })
const batchAttrForm = reactive({ targetDecorationStatus: '', targetSpecialTags: '', operateReason: '' })

const hasAddPermission = computed(() => userStore.hasPermission('park:property:add'))
const hasEditPermission = computed(() => userStore.hasPermission('park:property:edit'))
const hasDeletePermission = computed(() => userStore.hasPermission('park:property:delete'))
const hasBatchAddPermission = computed(() => userStore.hasPermission('park:property:batchAdd'))
const hasImportPermission = computed(() => userStore.hasPermission('park:property:import'))
const hasExportPermission = computed(() => userStore.hasPermission('park:property:export'))
const hasChangeStatusPermission = computed(() => userStore.hasPermission('park:property:changeStatus'))
const hasBatchStatusPermission = computed(() => userStore.hasPermission('park:property:batchStatus'))
const hasBatchAttrPermission = computed(() => userStore.hasPermission('park:property:batchAttr'))
const hasComparePermission = computed(() => userStore.hasPermission('park:property:compare'))
const isManager = computed(() => userStore.hasPermission('park:property:delete'))

const occupancyRate = computed(() => {
  return (stats.value.occupancyRate || 0).toFixed(1)
})

const vacancyRate = computed(() => {
  return (stats.value.vacancyRate || 0).toFixed(1)
})

const subTypeOptions = computed(() => {
  const type = batchForm.propertyType || formData.propertyType
  if (type === 1) return ['普通住宅', '公寓', '别墅', '复式', 'loft']
  if (type === 2) return ['写字间', '商铺', '底商']
  if (type === 3) return ['仓库', '设备间']
  return []
})

onMounted(() => {
  loadBuildingList()
  loadStats()
  loadList()
  loadTagList()
})

function loadBuildingList() {
  api.parkBuilding.listAll({ parkId: 1 }).then(res => {
    buildingList.value = res.data || []
  })
}

function loadFloors(buildingId) {
  if (!buildingId) {
    floorList.value = []
    return
  }
  api.parkFloor.listByBuilding(buildingId).then(res => {
    floorList.value = res.data || []
  })
}

function loadStats() {
  api.parkProperty.stats().then(res => {
    stats.value = res.data || {}
  })
}

function loadList() {
  loading.value = true
  if (queryForm.tagIds && queryForm.tagIds.length > 0) {
    api.parkPropertyTag.filter({
      tagIds: queryForm.tagIds.join(','),
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    }).then(res => {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }).finally(() => { loading.value = false })
    return
  }
  const params = {
    pageNum: queryForm.pageNum,
    pageSize: queryForm.pageSize,
    propertyCode: queryForm.propertyCode || undefined,
    buildingId: queryForm.buildingId || undefined,
    floorId: queryForm.floorId || undefined,
    propertyType: queryForm.propertyType || undefined,
    status: queryForm.status || undefined,
    houseType: queryForm.houseType || undefined,
    orientation: queryForm.orientation || undefined,
    sortField: queryForm.sortField || undefined,
    sortOrder: queryForm.sortOrder || undefined
  }

  if (queryForm.areaRange) {
    const [min, max] = queryForm.areaRange.split('-')
    params.minArea = min === '0' ? undefined : parseFloat(min)
    params.maxArea = max === '9999' ? undefined : parseFloat(max)
  }

  api.parkProperty.list(params).then(res => {
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleBuildingChange() {
  queryForm.floorId = null
  loadFloors(queryForm.buildingId)
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  queryForm.propertyCode = ''
  queryForm.buildingId = null
  queryForm.floorId = null
  queryForm.propertyType = null
  queryForm.status = null
  queryForm.houseType = ''
  queryForm.orientation = ''
  queryForm.areaRange = ''
  queryForm.tagIds = []
  floorList.value = []
  handleSearch()
}

function handleSortChange({ prop, order }) {
  if (order) {
    queryForm.sortField = prop
    queryForm.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryForm.sortField = ''
    queryForm.sortOrder = ''
  }
  loadList()
}

function handleSizeChange(size) {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadList()
}

function handlePageChange(page) {
  queryForm.pageNum = page
  loadList()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function getPropertyTypeLabel(type, subType) {
  if (subType) return subType
  const map = { 1: '住宅', 2: '商业', 3: '其他' }
  return map[type] || '-'
}

function getStatusLabel(status) {
  const map = {
    1: '待售',
    2: '已售未交付',
    3: '业主自住',
    4: '业主出租',
    5: '空置',
    6: '装修中',
    7: '查封'
  }
  return map[status] || '-'
}

function getStatusTagType(status) {
  const map = {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: '',
    5: 'info',
    6: 'warning',
    7: 'danger'
  }
  return map[status] || 'info'
}

function handleView(row) {
  router.push(`/park/property/detail/${row.id}`)
}

function handleAdd() {
  isEdit.value = false
  formTitle.value = '新增房产'
  activeTab.value = 'basic'
  formFloorList.value = []
  Object.assign(formData, {
    id: null,
    propertyCode: '',
    buildingId: null,
    floorId: null,
    propertyType: 1,
    propertySubType: '',
    buildingArea: null,
    insideArea: null,
    sharedArea: null,
    houseType: '',
    orientation: '',
    decorationStatus: '',
    propertyRightType: '',
    propertyRightYears: null,
    propertyCertNo: '',
    deliveryDate: null,
    status: 1,
    specialTagList: [],
    remark: ''
  })
  formVisible.value = true
}

function handleFormBuildingChange() {
  formData.floorId = null
  formFloorList.value = []
  if (formData.buildingId) {
    api.parkFloor.listByBuilding(formData.buildingId).then(res => {
      formFloorList.value = res.data || []
    })
  }
}

function handleEdit(row) {
  isEdit.value = true
  formTitle.value = '编辑房产'
  activeTab.value = 'basic'
  
  api.parkProperty.getById(row.id).then(res => {
    const data = res.data
    Object.assign(formData, data)
    formData.specialTagList = data.specialTagList || []
    
    if (data.buildingId) {
      api.parkFloor.listByBuilding(data.buildingId).then(res => {
        formFloorList.value = res.data || []
      })
    }
    
    formVisible.value = true
  })
}

function handleFormClose() {
  formRef.value?.resetFields()
}

function handleSubmit() {
  formRef.value?.validate(valid => {
    if (!valid) return
    
    const submitData = { ...formData }
    if (submitData.specialTagList && submitData.specialTagList.length > 0) {
      submitData.specialTags = submitData.specialTagList.join(',')
    }

    if (isEdit.value) {
      api.parkProperty.update(submitData).then(() => {
        ElMessage.success('修改成功')
        formVisible.value = false
        loadList()
        loadStats()
      })
    } else {
      api.parkProperty.add(submitData).then(() => {
        ElMessage.success('新增成功')
        formVisible.value = false
        loadList()
        loadStats()
      })
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除房产 "${row.propertyCode}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkProperty.delete(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 套房产吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    api.parkProperty.batchDelete(selectedIds.value).then(() => {
      ElMessage.success('批量删除成功')
      loadList()
      loadStats()
    })
  }).catch(() => {})
}

function handleChangeStatus(row) {
  currentProperty.value = row
  newStatus.value = null
  changeReason.value = ''
  statusDialogVisible.value = true
}

function confirmChangeStatus() {
  if (!newStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  api.parkProperty.changeStatus(currentProperty.value.id, newStatus.value, changeReason.value).then(() => {
    ElMessage.success('状态修改成功')
    statusDialogVisible.value = false
    loadList()
  })
}

function loadTagList() {
  api.parkPropertyTag.all().then(res => {
    tagList.value = res.data || []
  })
}

function handleBatchStatusDialog() {
  if (selectedIds.value.length > 100) {
    ElMessage.warning('单次最多选择100个房产')
    return
  }
  batchCheckResult.items = tableData.value.filter(r => selectedIds.value.includes(r.id)).map(r => ({
    propertyCode: r.propertyCode, buildingName: r.buildingName, floorNumber: r.floorNumber,
    currentStatus: r.status, canOperate: !(r.status === 4), failReason: r.status === 4 ? '该房产已出租且有租约' : ''
  }))
  batchCheckResult.failItems = batchCheckResult.items.filter(i => !i.canOperate)
  batchStatusForm.targetStatus = null
  batchStatusForm.operateReason = ''
  batchStatusDialogVisible.value = true
}

function handleBatchAttrDialog() {
  if (selectedIds.value.length > 100) {
    ElMessage.warning('单次最多选择100个房产')
    return
  }
  batchAttrForm.targetDecorationStatus = ''
  batchAttrForm.targetSpecialTags = ''
  batchAttrForm.operateReason = ''
  batchAttrDialogVisible.value = true
}

async function confirmBatchStatus() {
  if (!batchStatusForm.targetStatus) { ElMessage.warning('请选择目标状态'); return }
  if (!batchStatusForm.operateReason.trim()) { ElMessage.warning('请填写操作原因'); return }
  batchLoading.value = true
  try {
    const res = await api.parkPropertyBatch.changeStatus({ ids: selectedIds.value, targetStatus: batchStatusForm.targetStatus, operateReason: batchStatusForm.operateReason })
    const data = res.data || {}
    ElMessage.success(`批量操作完成：成功${data.successCount || 0}个，失败${data.failCount || 0}个`)
    batchStatusDialogVisible.value = false
    loadList()
    loadStats()
  } finally { batchLoading.value = false }
}

async function confirmBatchAttr() {
  if (!batchAttrForm.targetDecorationStatus && !batchAttrForm.targetSpecialTags) { ElMessage.warning('请至少修改一项属性'); return }
  if (!batchAttrForm.operateReason.trim()) { ElMessage.warning('请填写操作原因'); return }
  batchLoading.value = true
  try {
    const res = await api.parkPropertyBatch.changeAttribute({ ids: selectedIds.value, targetDecorationStatus: batchAttrForm.targetDecorationStatus, targetSpecialTags: batchAttrForm.targetSpecialTags, operateReason: batchAttrForm.operateReason })
    const data = res.data || {}
    ElMessage.success(`批量操作完成：成功${data.successCount || 0}个，失败${data.failCount || 0}个`)
    batchAttrDialogVisible.value = false
    loadList()
    loadStats()
  } finally { batchLoading.value = false }
}

function handleCompare() {
  const ids = selectedIds.value.join(',')
  router.push(`/park/property/compare?ids=${ids}`)
}

function clearSelection() {
  selectedIds.value = []
}

function handleBatchCreate() {
  batchStep.value = 0
  batchResult.value = null
  batchPreviewList.value = []
  batchFloorList.value = []
  Object.assign(batchForm, {
    buildingId: null,
    floorId: null,
    propertyType: 1,
    propertySubType: '',
    prefix: '',
    startNum: 1,
    endNum: 10,
    buildingArea: null,
    insideArea: null,
    sharedArea: null,
    houseType: '两室',
    orientation: '南',
    decorationStatus: '毛坯',
    propertyRightType: '商品房',
    propertyRightYears: 70
  })
  batchDialogVisible.value = true
}

function handleBatchBuildingChange() {
  batchForm.floorId = null
  batchFloorList.value = []
  if (batchForm.buildingId) {
    api.parkFloor.listByBuilding(batchForm.buildingId).then(res => {
      batchFloorList.value = res.data || []
    })
  }
}

function handleBatchNext() {
  if (batchStep.value === 0) {
    if (!batchForm.buildingId || !batchForm.floorId) {
      ElMessage.warning('请选择楼宇和楼层')
      return
    }
  } else if (batchStep.value === 1) {
    if (!batchForm.propertyType) {
      ElMessage.warning('请选择房产类型')
      return
    }
  } else if (batchStep.value === 2) {
    if (!batchForm.prefix) {
      ElMessage.warning('请输入房号前缀')
      return
    }
    if (batchForm.startNum >= batchForm.endNum) {
      ElMessage.warning('结束号必须大于起始号')
      return
    }
  } else if (batchStep.value === 3) {
    loadBatchPreview()
  }
  batchStep.value++
}

function loadBatchPreview() {
  api.parkProperty.previewBatch(batchForm).then(res => {
    batchPreviewList.value = res.data || []
  })
}

function confirmBatchCreate() {
  api.parkProperty.batchCreate(batchForm).then(res => {
    batchResult.value = res.data
    loadList()
    loadStats()
  })
}

function handleBatchClose() {
  batchResult.value = null
}

function handleImport() {
  importResult.value = null
  importFile.value = null
  importDialogVisible.value = true
}

function downloadTemplate() {
  api.parkProperty.getTemplate().then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '房产导入模板.xlsx'
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('模板下载成功')
  }).catch(() => {
    ElMessage.error('模板下载失败')
  })
}

function handleFileChange(file) {
  importFile.value = file.raw
  importResult.value = null
}

function handleImportConfirm() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  importLoading.value = true
  api.parkProperty.importData(importFile.value).then(res => {
    importResult.value = res.data
    loadList()
    loadStats()
    ElMessage.success('导入完成')
  }).finally(() => {
    importLoading.value = false
  })
}

function downloadFailList() {
  if (!importResult.value || !importResult.value.failList) return
  
  const headers = ['行号', '房产编号', '失败原因']
  const rows = importResult.value.failList.map(item => [
    item.rowNum || '',
    item.propertyCode || '',
    item.reason || ''
  ])
  
  const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `导入失败数据_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

function handleExport() {
  const params = {
    propertyCode: queryForm.propertyCode || undefined,
    buildingId: queryForm.buildingId || undefined,
    floorId: queryForm.floorId || undefined,
    propertyType: queryForm.propertyType || undefined,
    status: queryForm.status || undefined,
    houseType: queryForm.houseType || undefined,
    orientation: queryForm.orientation || undefined
  }
  
  if (queryForm.areaRange) {
    const [min, max] = queryForm.areaRange.split('-')
    params.minArea = min === '0' ? undefined : parseFloat(min)
    params.maxArea = max === '9999' ? undefined : parseFloat(max)
  }

  api.parkProperty.export(params).then(res => {
    const data = res.data || []
    exportToCSV(data)
  })
}

function exportToCSV(data) {
  const headers = ['房产编号', '楼宇', '楼层', '类型', '子类型', '建筑面积(㎡)', '套内面积(㎡)', '公摊面积(㎡)', 
                   '户型', '朝向', '装修状况', '产权性质', '产权年限', '产权证号', '交付日期', '状态', '备注']
  
  const statusMap = { 1: '待售', 2: '已售未交付', 3: '业主自住', 4: '业主出租', 5: '空置', 6: '装修中', 7: '查封' }
  
  const rows = data.map(item => [
    item.propertyCode,
    item.buildingName,
    item.floorNumber,
    item.propertyType === 1 ? '住宅' : item.propertyType === 2 ? '商业' : '其他',
    item.propertySubType || '',
    item.buildingArea || '',
    item.insideArea || '',
    item.sharedArea || '',
    item.houseType || '',
    item.orientation || '',
    item.decorationStatus || '',
    item.propertyRightType || '',
    item.propertyRightYears || '',
    item.propertyCertNo || '',
    item.deliveryDate || '',
    statusMap[item.status] || '',
    item.remark || ''
  ])
  
  const csvContent = [headers, ...rows].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `房产列表_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.property-container {
  padding: 20px;
}

.stats-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-item {
  text-align: center;
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 8px;
    
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
  margin-bottom: 20px;
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-buttons {
      display: flex;
      gap: 8px;
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}

.batch-step-content {
  min-height: 350px;
  padding: 20px 0;
}

.import-content {
  padding: 20px 0;
}

.building-image {
  width: 80px;
  height: 60px;
  border-radius: 4px;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 60px;
  background: #f5f7fa;
  color: #c0c4cc;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

:deep(.warning-row) {
  background-color: #fef0f0 !important;
}
</style>
