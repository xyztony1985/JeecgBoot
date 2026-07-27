import type { AppRouteRecordRaw } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

export const AI_ROUTE: AppRouteRecordRaw = {
  path: '',
  name: 'ai-parent',
  component: LAYOUT,
  meta: {
    title: 'ai',
  },
  children: [
    {
      path: '/ai',
      name: 'ai',
      component: () => import('/@/views/dashboard/ai/index.vue'),
      meta: {
        title: 'AI助手',
      },
    },
  ],
};

/**
 * 开发模式：页面浏览器路由（基础路由，无需登录即可访问）
 * 不使用 LAYOUT，作为独立页面显示
 */
export const DEV_PAGES_ROUTE: AppRouteRecordRaw = {
  path: '/dev/pages',
  name: 'dev-pages-browser',
  component: () => import('/@/views/dev/DevPages.vue'),
  meta: {
    title: '页面浏览器',
    ignoreAuth: true, // 无需登录即可访问浏览器页面
  },
};

export const staticRoutesList = [AI_ROUTE];
