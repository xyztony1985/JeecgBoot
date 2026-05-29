package com.cssz.util;

/**
 * CSSZ Base 工具类示例
 * 
 * 提供通用的工具方法，供其他项目使用
 */
public class CsszBaseUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private CsszBaseUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 判断字符串是否为空
     * 
     * @param str 待检查的字符串
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * 
     * @param str 待检查的字符串
     * @return true-不为空，false-为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 安全获取字符串，如果为null则返回默认值
     * 
     * @param str 原始字符串
     * @param defaultValue 默认值
     * @return 非空字符串或默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
}
