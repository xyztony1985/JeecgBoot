package com.cssz.util;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;

/**
 * 用户工具类
 *
 * 提供获取当前登录用户相关的方法
 */
public class UserUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private UserUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录的 LoginUser 对象，如果未登录则返回 null
     */
    public static LoginUser getCurrentUser() {
        try {
            return (LoginUser) SecurityUtils.getSubject().getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户的ID，如果未登录则返回 null
     */
    public static String getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户的用户名，如果未登录则返回 null
     */
    public static String getCurrentUsername() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户真实姓名
     *
     * @return 当前登录用户的真实姓名，如果未登录则返回 null
     */
    public static String getCurrentRealname() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getRealname() : null;
    }

    /**
     * 获取当前登录用户部门编码
     *
     * @return 当前登录用户的部门编码，如果未登录则返回 null
     */
    public static String getCurrentOrgCode() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getOrgCode() : null;
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isLogin() {
        return getCurrentUser() != null;
    }
}
