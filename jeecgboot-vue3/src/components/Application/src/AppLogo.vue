<!--
 * @Author: Jeecg
 * @Description: logo component
-->
<template>
  <div class="anticon" :class="getAppLogoClass" @click="goHome">
    <img :src="tenantLogo || '../../../assets/images/logo.png'" alt="logo" />
    <div class="ml-2 truncate md:opacity-100" :class="getTitleClass" v-show="showTitle">
      {{ tenantName || shortTitle }}
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, unref, ref, onMounted } from 'vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useGo } from '/@/hooks/web/usePage';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useUserStore } from '/@/store/modules/user';
  import { getTenantById } from '/@/views/system/tenant/tenant.api';
  import { getTenantId } from '/@/utils/auth';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';

  const props = defineProps({
    /**
     * The theme of the current parent component
     */
    theme: { type: String, validator: (v: string) => ['light', 'dark'].includes(v) },
    /**
     * Whether to show title
     */
    showTitle: { type: Boolean, default: true },
    /**
     * The title is also displayed when the menu is collapsed
     */
    alwaysShowTitle: { type: Boolean },
  });

  const { prefixCls } = useDesign('app-logo');
  const { getCollapsedShowTitle } = useMenuSetting();
  const userStore = useUserStore();
  const { title, shortTitle } = useGlobSetting();
  // tenant dynamic
  const tenantLogo = ref('');
  const tenantName = ref('');
  
  const go = useGo();

  const getAppLogoClass = computed(() => [prefixCls, props.theme, { 'collapsed-show-title': unref(getCollapsedShowTitle) }]);

  const getTitleClass = computed(() => [
    `${prefixCls}__title`,
    {
      'xs:opacity-0': !props.alwaysShowTitle,
    },
  ]);

  // init tenant info (logo / company name)
  onMounted(async () => {
    try {
      const tId = getTenantId();
      if (tId) {
        const res: any = await getTenantById({ id: tId });
        if (res) {
          if (res.companyLogo) {
            tenantLogo.value = getFileAccessHttpUrl(res.companyLogo);
          }
          if (res.name) {
            tenantName.value = res.name;
            // update document title so browser tab shows tenant company name
            try {
              document.title = res.name;
            } catch (e) {}
          }
        }
      }
    } catch (err) {
      // ignore
    }
  });

  function goHome() {
    go(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
  }
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-app-logo';

  .@{prefix-cls} {
    display: flex;
    align-items: center;
    padding-left: 7px;
    cursor: pointer;
    transition: all 0.2s ease;
    //左侧菜单模式和左侧菜单混合模式加渐变背景色
    &.jeecg-layout-mix-sider-logo,&.jeecg-layout-menu-logo{
      background:@sider-logo-bg-color;
    }
    // &.light {
    //   border-bottom: 1px solid @border-color-base;
    // }

    &.collapsed-show-title {
      padding-left: 20px;
    }

    &.light &__title {
      color: @primary-color;
    }

    &.dark &__title {
      color: @white;
    }

    &__title {
      font-size: 18px;
      font-weight: 600;
      transition: all 0.5s;
      line-height: normal;
    }
  }
</style>
