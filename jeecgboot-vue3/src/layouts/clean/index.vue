<template>
  <Layout class="clean-layout">
    <LayoutContent />
  </Layout>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { Layout } from 'ant-design-vue';
  import LayoutContent from '../default/content/index.vue';

  export default defineComponent({
    name: 'CleanLayout',
    components: {
      Layout,
      LayoutContent,
    },
  });
</script>

<style lang="less" scoped>
  .clean-layout {
    width: 100%;
    min-height: 100%;
    background-color: #f0f2f5;
  }
</style>
