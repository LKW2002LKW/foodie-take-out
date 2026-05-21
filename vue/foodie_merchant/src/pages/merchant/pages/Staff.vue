<template>
  <div class="staff-page">
    <!-- 员工管理页只负责视图渲染，新增、编辑与重置密码逻辑由 useStaffPage 托管。 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">商户员工</h1>
        <p class="page-subtitle">管理门店内所有员工账号及权限</p>
      </div>
      <div class="header-action">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增员工</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="姓名/用户名/手机号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" placeholder="全部角色" clearable style="width: 150px" @change="handleQuery">
            <el-option label="管理员" :value="1" />
            <el-option label="经理" :value="2" />
            <el-option label="普通员工" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px" @change="handleQuery">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="list-card">
      <el-table v-loading="loading" :data="employeeList" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">{{ scope.row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button
              link
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入系统登录用户名" :disabled="form.id !== undefined" />
        </el-form-item>
        <el-form-item v-if="form.id === undefined" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入员工真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="身份证" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号码" maxlength="18" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择员工角色" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="经理" :value="2" />
            <el-option label="普通员工" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.id !== undefined" label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDialog.visible" title="重置密码" width="400px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pwdDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="pwdSubmitting" @click="submitResetPwd">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useStaffPage } from '@/composables/business/useStaffPage'

const {
  dialog,
  employeeList,
  form,
  formRef,
  getRoleTagType,
  handleAdd,
  handleCurrentChange,
  handleDelete,
  handleDialogClose,
  handleEdit,
  handleQuery,
  handleResetPwd,
  handleSizeChange,
  handleStatusChange,
  loading,
  pwdDialog,
  pwdForm,
  pwdFormRef,
  pwdRules,
  pwdSubmitting,
  queryParams,
  resetQuery,
  rules,
  submitForm,
  submitResetPwd,
  submitting,
  total,
} = useStaffPage()
</script>

<style scoped>
.staff-page { padding: 0; }
.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title { font-size: 24px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px 0; }
.page-subtitle { color: #909399; font-size: 14px; margin: 0; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.list-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
