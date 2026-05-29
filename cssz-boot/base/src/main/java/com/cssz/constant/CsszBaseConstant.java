package com.cssz.constant;

/**
 * CSSZ Base 常量定义
 * 
 * 此基础包提供通用的常量、工具类等，供其他项目引用使用
 */
public class CsszBaseConstant {

    /**
     * 成功状态码
     */
    public static final String SUCCESS_CODE = "200";

    /**
     * 失败状态码
     */
    public static final String ERROR_CODE = "500";

    /**
     * 默认字符编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 私有构造函数，防止实例化
     */
    private CsszBaseConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
