<template>
  <p>检测当前 OAuth2 环境为：{{ env.name }}</p>
  <p>{{ info }}</p>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { getOAuth2Env, sysOAuth2Callback, sysOAuth2Login } from '/@/views/sys/login/useLogin';
  import { useRouter } from 'vue-router';
  import { PageEnum } from '/@/enums/pageEnum';
  import { router } from '/@/router';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getAuthCache, getTenantId, getToken } from "/@/utils/auth";
  import { requestAuthCode } from 'dingtalk-jsapi';
  import { defHttp } from '/@/utils/http/axios';
  import { OAUTH2_THIRD_LOGIN_TENANT_ID } from "/@/enums/cacheEnum";

  let info = ref("");
  const env = getOAuth2Env();
  const { currentRoute } = useRouter();
  const route = currentRoute.value;
  if (env.isOAuth2) {
    doOAuth2Login();
  }
  else {
    router.replace({ path: PageEnum.BASE_LOGIN, query: route.query });
  }


  /**
   * 进行OAuth2登录操作
   */
  function doOAuth2Login() {
    console.log(route.query);
    // 判断是否携带了Token，是就说明登录成功
    if (route.query.oauth2LoginToken) {
      let token = route.query.oauth2LoginToken;
      //执行登录操作
      thirdLogin({ token, thirdType: route.query.thirdType,tenantId: getTenantId });
    } else if (env.wxwork) {
      sysOAuth2Login('wechat_enterprise');
    } else if (env.dingtalk) {
      //新版钉钉登录
      dingdingLogin();
    } else if (env.feishu) {
      sysOAuth2Login('feishu');
    } else {
      info.value = "当前环境未适配！";
    }
  }

  /**
   * 第三方登录
   * @param params
   */
  function thirdLogin(params) {
    const userStore = useUserStore();
    const { notification } = useMessage();
    const { t } = useI18n();
    userStore.ThirdLogin(params).then((res) => {
      if (res && res.userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
          duration: 3,
        });
      } else {
        notification.error({
          message: t('sys.login.errorTip'),
          description: ((res.response || {}).data || {}).message || res.message || t('sys.login.networkExceptionMsg'),
          duration: 4,
        });
      }
    });
  }

  /**
   * 钉钉登录
   */
  function dingdingLogin() {
    //先获取钉钉的企业id，如果没有配置 还是走原来的逻辑，走原来的逻辑 需要判断存不存在token，存在token直接去首页
    let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID) || 0;
    let url = `/sys/thirdLogin/get/corpId/clientId?tenantId=${tenantId}`;
    // 代码逻辑说明: 不要使用getAction online里面的，要用defHttp---
    defHttp.get({ url:url },{ isTransformResponse: false }).then((res) => {
        if (res.success) {
          if(res.result && res.result.corpId && res.result.clientId){
            requestAuthCode({ corpId: res.result.corpId, clientId: res.result.clientId }).then((res) => {
              let { code } = res;
              sysOAuth2Callback(code);
            });
          }else{
            toOldAuthLogin();
          }
        } else {
          toOldAuthLogin();
        }
      }).catch((err) => {
        toOldAuthLogin();
      });
  }
  
  /**
   * 旧版钉钉登录
   */
  function toOldAuthLogin() {
    let token = getToken();
    if (token) {
      router.replace({ path: PageEnum.BASE_HOME });
    } else {
      sysOAuth2Login('dingtalk');
    }
  }
</script>
