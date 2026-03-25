<template>
  <div class="notice-container">
    <el-card>
      <div class="mb-20">
        <el-button type="primary" icon="Plus" @click="handleOpenDialog()">发布公告</el-button>
      </div>

      <!-- Search -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="类型" width="100">
           <template #default="scope">
              <el-tag :type="scope.row.type === 1 ? '' : 'warning'">
                {{ scope.row.type === 1 ? '系统公告' : '活动公告' }}
              </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="目标" width="100">
             <template #default="scope">
              {{ getTargetText(scope.row.target) }}
           </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
           <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确认删除该公告吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

       <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

     <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
           <el-radio-group v-model="form.type">
             <el-radio :label="1">系统公告</el-radio>
             <el-radio :label="2">活动公告</el-radio>
           </el-radio-group>
        </el-form-item>
        <el-form-item label="目标" prop="target">
           <el-radio-group v-model="form.target">
             <el-radio :label="1">商户</el-radio>
             <el-radio :label="2">用户</el-radio>
             <el-radio :label="3">全部</el-radio>
           </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="5"
            placeholder="请输入公告内容"
           />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleSaveDraft">存为草稿</el-button>
          <el-button type="primary" @click="handlePublish">立即发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布公告')
const formRef = ref(null)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  status: null
})

const form = reactive({
  title: '',
  content: '',
  type: 1,
  target: 3
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/page', { params: queryParams })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
    queryParams.page = 1
    getList()
}

const resetQuery = () => {
    queryParams.status = null
    handleSearch()
}

const handleDelete = async (row) => {
    await request.delete(`/notice/${row.id}`)
    ElMessage.success('删除成功')
    getList()
}

const handleOpenDialog = () => {
    dialogTitle.value = '发布公告'
    form.title = ''
    form.content = ''
    form.type = 1
    form.target = 3
    dialogVisible.value = true
}

const handlePublish = () => {
    formRef.value.validate(async (valid) => {
        if(valid) {
            await request.post('/notice/publish', form)
            ElMessage.success('发布成功')
            dialogVisible.value = false
            getList()
        }
    })
}

const handleSaveDraft = () => {
     formRef.value.validate(async (valid) => {
        if(valid) {
            await request.post('/notice/draft', form)
            ElMessage.success('已保存为草稿')
            dialogVisible.value = false
            getList()
        }
    })
}

const getTargetText = (target) => {
    const map = { 1: '商户', 2: '用户', 3: '全部' }
    return map[target] || '未知'
}

onMounted(() => {
    getList()
})
</script>

<style scoped>
.mb-20 {
    margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
