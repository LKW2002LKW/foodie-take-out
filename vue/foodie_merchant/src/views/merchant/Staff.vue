<template>
  <div class="staff-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">商户员工</h1>
        <p class="page-subtitle">管理门店内所有员工账号及权限</p>
      </div>
      <div class="header-action">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增员工</el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
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

    <!-- 列表区域 -->
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialog.visible"
        :title="dialog.title"
        width="500px"
        :close-on-click-modal="false"
        @close="handleDialogClose"
    >
        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
            <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="请输入系统登录用户名" :disabled="form.id !== undefined"/>
            </el-form-item>
             <el-form-item 
                label="密码" 
                prop="password" 
                v-if="form.id === undefined"
            >
                <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password/>
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
             <el-form-item label="状态" prop="status" v-if="form.id !== undefined">
                <el-radio-group v-model="form.status">
                    <el-radio :label="1">正常</el-radio>
                    <el-radio :label="0">禁用</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialog.visible = false">取消</el-button>
                <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog
        v-model="pwdDialog.visible"
        title="重置密码"
        width="400px"
    >
        <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
             <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password/>
            </el-form-item>
        </el-form>
         <template #footer>
            <span class="dialog-footer">
                <el-button @click="pwdDialog.visible = false">取消</el-button>
                <el-button type="primary" @click="submitResetPwd" :loading="pwdSubmitting">确定</el-button>
            </span>
        </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Refresh } from '@element-plus/icons-vue';
import employeeApi from '@/api/employee';

// List View Data
const loading = ref(false);
const employeeList = ref([]);
const total = ref(0);
const queryParams = reactive({
    page: 1,
    pageSize: 10,
    keyword: '',
    role: undefined,
    status: undefined
});

// Main Dialog Data
const dialog = reactive({
    visible: false,
    title: '新增员工'
});
const submitting = ref(false);
const formRef = ref(null);
const form = reactive({
    id: undefined,
    username: '',
    password: '',
    name: '',
    phone: '',
    idCard: '',
    role: 3,
    status: 1
});

// Password Dialog Data
const pwdDialog = reactive({
    visible: false
});
const pwdSubmitting = ref(false);
const pwdFormRef = ref(null);
const pwdForm = reactive({
    id: undefined,
    newPassword: ''
});

// Validation Rules
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    name: [
        { required: true, message: '请输入姓名', trigger: 'blur' }
    ],
    phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    role: [
        { required: true, message: '请选择角色', trigger: 'change' }
    ]
};

const pwdRules = {
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ]
};

// Lifecycle
onMounted(() => {
    getList();
});

// Methods: List
const getList = async () => {
    loading.value = true;
    try {
        const res = await employeeApi.getEmployeePage(queryParams);
        if (res && res.data) {
            employeeList.value = res.data.list || [];
            total.value = res.data.total || 0;
        }
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const handleQuery = () => {
    queryParams.page = 1;
    getList();
};

const resetQuery = () => {
    queryParams.keyword = '';
    queryParams.role = undefined;
    queryParams.status = undefined;
    handleQuery();
};

const handleSizeChange = (val) => {
    queryParams.pageSize = val;
    getList();
};

const handleCurrentChange = (val) => {
    queryParams.page = val;
    getList();
};

const getRoleTagType = (role) => {
    switch(role) {
        case 1: return 'danger';
        case 2: return 'warning';
        case 3: return 'info';
        default: return '';
    }
};

// Methods: Add/Edit
const handleAdd = () => {
    dialog.title = '新增员工';
    dialog.visible = true;
    // Reset Form
    resetForm();
};

const handleEdit = async (row) => {
    dialog.title = '编辑员工';
    dialog.visible = true;
    resetForm();
    // Fetch latest details
    try {
        const res = await employeeApi.getEmployeeDetail(row.id);
        if (res.data) {
             Object.assign(form, res.data);
             // Ensure status is number
             form.status = res.data.status;
        }
    } catch (e) {
        console.error(e);
        ElMessage.error('获取员工详情失败');
    }
};

const resetForm = () => {
    form.id = undefined;
    form.username = '';
    form.password = '';
    form.name = '';
    form.phone = '';
    form.idCard = '';
    form.role = 3;
    form.status = 1;
    if (formRef.value) formRef.value.clearValidate();
};

const handleDialogClose = () => {
    if (formRef.value) formRef.value.clearValidate();
};

const submitForm = () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            submitting.value = true;
            try {
                if (form.id) {
                    await employeeApi.updateEmployee(form);
                    ElMessage.success('更新成功');
                } else {
                    await employeeApi.addEmployee(form);
                    ElMessage.success('新增成功');
                }
                dialog.visible = false;
                getList();
            } catch (e) {
                console.error(e);
            } finally {
                submitting.value = false;
            }
        }
    });
};

// Methods: Status & Delete
const handleStatusChange = async (row) => {
    const newStatus = row.status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '启用' : '禁用';
    
    try {
        await ElMessageBox.confirm(`确认要${actionText}员工"${row.name}"吗?`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });
        
        await employeeApi.updateStatus(row.id, newStatus);
        ElMessage.success(`${actionText}成功`);
        getList();
    } catch (e) {
        if (e !== 'cancel') console.error(e);
    }
};

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm(`确认要删除员工"${row.name}"吗? 此操作不可恢复。`, '警告', {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'error'
        });
        
        await employeeApi.deleteEmployee(row.id);
        ElMessage.success('删除成功');
        getList();
    } catch (e) {
        if (e !== 'cancel') console.error(e);
    }
};

// Methods: Reset Password
const handleResetPwd = (row) => {
    pwdDialog.visible = true;
    pwdForm.id = row.id;
    pwdForm.newPassword = '';
    if (pwdFormRef.value) pwdFormRef.value.clearValidate();
};

const submitResetPwd = () => {
    pwdFormRef.value.validate(async (valid) => {
        if (valid) {
            pwdSubmitting.value = true;
            try {
                await employeeApi.resetPassword(pwdForm);
                ElMessage.success('密码重置成功');
                pwdDialog.visible = false;
            } catch (e) {
                console.error(e);
            } finally {
                pwdSubmitting.value = false;
            }
        }
    });
};

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
