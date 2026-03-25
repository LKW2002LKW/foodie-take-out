<template>
  <div class="review-page">
     <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">评价管理</h1>
        <p class="page-subtitle">查看及回复顾客评价</p>
      </div>
    </div>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="评分">
            <el-select v-model="queryParams.rating" placeholder="全部评分" clearable style="width: 150px" @change="handleQuery">
                <el-option label="5星" :value="5" />
                <el-option label="4星" :value="4" />
                <el-option label="3星" :value="3" />
                <el-option label="2星" :value="2" />
                <el-option label="1星" :value="1" />
            </el-select>
        </el-form-item>
        <el-form-item label="回复状态">
            <el-select v-model="queryParams.replied" placeholder="全部" clearable style="width: 120px" @change="handleQuery">
                <el-option label="已回复" :value="1" />
                <el-option label="未回复" :value="0" />
            </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="hover" class="list-card">
       <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部评价" name="all" />
        <el-tab-pane label="差评 (<=2星)" name="bad" />
      </el-tabs>

      <div v-loading="loading" class="review-list">
          <div v-if="reviewList.length === 0" class="empty-block">暂无评价数据</div>
          
          <div v-for="item in reviewList" :key="item.id" class="review-item">
              <div class="user-info">
                  <span class="username">{{ item.userName }}</span>
                  <span class="order-time">{{ item.createTime }}</span>
              </div>
              
              <div class="rating-row">
                   <el-rate v-model="item.rating" disabled show-score text-color="#ff9900" />
                   <span class="order-no">订单号: {{ item.orderNumber }}</span>
              </div>

              <div class="content text-truncate-multiline">
                  {{ item.content || '此用户没有填写评价。' }}
              </div>

               <div v-if="item.images" class="image-list">
                  <!-- Split by comma if multiple images string -->
                  <el-image 
                    v-for="(img, index) in (item.images ? item.images.split(',') : [])" 
                    :key="index"
                    :src="img" 
                    :preview-src-list="item.images.split(',')"
                    fit="cover"
                    class="review-img"
                    preview-teleported
                  />
              </div>

              <div v-if="item.merchantReply" class="merchant-reply">
                  <div class="reply-title">商家回复 ({{ item.replyTime }}) :</div>
                  <div class="reply-content">{{ item.merchantReply }}</div>
              </div>

              <div class="action-bar" v-if="!item.merchantReply">
                  <el-button type="primary" link icon="ChatDotRound" @click="handleOpenReply(item)">回复</el-button>
              </div>
          </div>
      </div>

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

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyVisible" title="回复评价" width="500px">
        <el-form :model="replyForm" ref="replyFormRef">
            <el-form-item 
              prop="content" 
              :rules="[{ required: true, message: '请输入回复内容', trigger: 'blur' }]"
            >
                <el-input 
                    v-model="replyForm.content" 
                    type="textarea" 
                    :rows="4" 
                    placeholder="请输入回复内容，注意礼貌用语..." 
                    maxlength="200"
                    show-word-limit
                />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="replyVisible = false">取消</el-button>
                <el-button type="primary" @click="submitReply" :loading="submitting">确定</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Refresh, ChatDotRound } from '@element-plus/icons-vue';
import reviewApi from '@/api/review';

// State
const loading = ref(false);
const reviewList = ref([]);
const total = ref(0);
const dateRange = ref([]);
const activeTab = ref('all');

const queryParams = reactive({
    page: 1,
    pageSize: 10,
    rating: undefined,
    replied: undefined,
    beginTime: undefined,
    endTime: undefined
});

// Reply State
const replyVisible = ref(false);
const submitting = ref(false);
const replyForm = reactive({
    id: undefined,
    content: ''
});
const replyFormRef = ref(null);

// Lifecycle
onMounted(() => {
    getList();
});

// Methods
const getList = async () => {
    loading.value = true;
    try {
        let res;
        if (activeTab.value === 'bad') {
             res = await reviewApi.getBadReviewPage({
                 page: queryParams.page,
                 pageSize: queryParams.pageSize
             });
        } else {
             res = await reviewApi.getReviewPage(queryParams);
        }
       
        if (res.data) {
            reviewList.value = res.data.records || [];
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
    queryParams.rating = undefined;
    queryParams.replied = undefined;
    queryParams.beginTime = undefined;
    queryParams.endTime = undefined;
    dateRange.value = [];
    handleQuery();
};

const handleDateChange = (val) => {
    if (val && val.length === 2) {
        queryParams.beginTime = val[0];
        queryParams.endTime = val[1];
    } else {
         queryParams.beginTime = undefined;
        queryParams.endTime = undefined;
    }
};

const handleTabChange = () => {
    queryParams.page = 1;
    getList();
};


const handleSizeChange = (val) => {
    queryParams.pageSize = val;
    getList();
};

const handleCurrentChange = (val) => {
    queryParams.page = val;
    getList();
};

// Actions
const handleOpenReply = (item) => {
    replyForm.id = item.id;
    replyForm.content = '';
    replyVisible.value = true;
};

const submitReply = () => {
    replyFormRef.value.validate(async (valid) => {
        if (valid) {
            submitting.value = true;
            try {
                await reviewApi.replyReview({
                    id: replyForm.id,
                    merchantReply: replyForm.content
                });
                ElMessage.success('回复成功');
                replyVisible.value = false;
                getList();
            } catch (e) {
                console.error(e);
            } finally {
                submitting.value = false;
            }
        }
    });
};

</script>

<style scoped>
.review-page { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px 0; }
.page-subtitle { color: #909399; font-size: 14px; margin: 0; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.list-card { border-radius: 8px; }

/* Review Item Styles */
.review-list {
    min-height: 200px;
}
.empty-block {
    text-align: center;
    color: #909399;
    padding: 40px 0;
}
.review-item {
    padding: 24px 0;
    border-bottom: 1px solid #ebeef5;
}
.review-item:last-child {
    border-bottom: none;
}
.user-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    color: #606266;
    font-size: 14px;
}
.username { font-weight: bold; color: #303133; }
.rating-row {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
}
.order-no {
    margin-left: 15px;
    font-size: 12px;
    color: #909399;
}
.content {
    color: #303133;
    font-size: 15px;
    line-height: 1.6;
    margin-bottom: 12px;
}
.image-list {
    margin-bottom: 12px;
}
.review-img {
    width: 100px;
    height: 100px;
    border-radius: 6px;
    margin-right: 8px;
    border: 1px solid #e4e7ed;
}
.merchant-reply {
    background-color: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    font-size: 14px;
    color: #5e6d82;
    margin-top: 10px;
}
.reply-title { font-weight: bold; margin-bottom: 4px; }
.action-bar {
    margin-top: 10px;
    text-align: right;
}

.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
