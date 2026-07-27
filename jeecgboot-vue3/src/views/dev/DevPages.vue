<template>
  <div class="dev-pages-container">
    <a-card title="页面浏览器" :bordered="false">
      <template #extra>
        <a-tag color="blue">共 {{ filteredPages.length }} 个页面</a-tag>
      </template>

      <a-input-search
        v-model:value="searchText"
        placeholder="搜索页面路径或文件名"
        style="width: 400px; margin-bottom: 16px"
        allow-clear
      />

      <a-table
        :columns="columns"
        :data-source="filteredPages"
        :pagination="{ pageSize: 50, showSizeChanger: true }"
        :scroll="{ y: 600 }"
        size="small"
        row-key="path"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openInNewTab(record.path)">
              新页面
            </a-button>
            <a-button type="link" size="small" @click="openDrawer(record.path)">
              抽屉
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerTitle"
      placement="right"
      width="80%"
      :destroy-on-close="true"
      :body-style="{ padding: 0, overflow: 'hidden' }"
      :header-style="{ padding: '8px 16px' }"
      class="preview-drawer"
    >
      <iframe
        v-if="drawerVisible"
        :src="drawerSrc"
        class="preview-iframe"
        frameborder="0"
      />
    </a-drawer>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { dynamicPages } from '/@/utils/dynamicPages';

  defineOptions({ name: 'dev-pages' });

  const searchText = ref('');
  const drawerVisible = ref(false);
  const drawerTitle = ref('');
  const drawerSrc = ref('');

  // 获取所有动态页面（直接使用 dynamicPages，避免重复扫描）
  const allPages = Object.keys(dynamicPages)
    .map((key) => {
      const path = key.replace(/^\.\.\/views\//, '').replace(/\.(vue|tsx)$/, '');
      const fileName = path.split('/').pop() || '';
      return {
        path: `/${path}`,
        fileName,
        fullPath: key,
      };
    })
    .filter((page) => {
      // 跳过 components 目录
      return !page.path.includes('/components/') && !page.path.startsWith('/components/');
    });

  const columns = [
    { title: '文件路径', dataIndex: 'path', key: 'path', width: 500 },
    { title: '文件名', dataIndex: 'fileName', key: 'fileName', width: 300 },
    { title: '打开方式', key: 'action', width: 150 },
  ];

  const filteredPages = computed(() => {
    if (!searchText.value) return allPages;
    const keyword = searchText.value.toLowerCase();
    return allPages.filter(
      (page) => page.path.toLowerCase().includes(keyword) || page.fileName.toLowerCase().includes(keyword),
    );
  });

  function openInNewTab(path: string) {
    window.open(path, '_blank');
  }

  function openDrawer(path: string) {
    drawerTitle.value = path;
    // 添加预览参数，让 LAYOUT 只渲染内容区域
    drawerSrc.value = `${path}?preview=true`;
    drawerVisible.value = true;
  }
</script>

<style lang="less" scoped>
  .dev-pages-container {
    padding: 16px;
  }

  .preview-iframe {
    width: 100%;
    height: calc(100vh - 48px);
    border: none;
  }
</style>
