package org.jeecg.modules.airag.llm.consts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Description: airag模型常量类
 * @Author: chenrui
 * @Date: 2025/2/12 17:35
 */
public class LLMConsts {


    /**
     * 正则表达式:是否是网页
     */
    public static final Pattern WEB_PATTERN = Pattern.compile("^(http|https)://.*");

    /**
     * 状态:启用
     */
    public static final String STATUS_ENABLE = "enable";
    /**
     * 状态:禁用
     */
    public static final String STATUS_DISABLE = "disable";


    /**
     * 模型类型:向量
     */
    public static final String MODEL_TYPE_EMBED = "EMBED";

    /**
     * 模型类型:聊天
     */
    public static final String MODEL_TYPE_LLM = "LLM";

    /**
     * 模型类型: 图像生成
     */
    public static final String MODEL_TYPE_IMAGE = "IMAGE";

    /**
     * 向量模型：默认维度
     */
    public static final Integer EMBED_MODEL_DEFAULT_DIMENSION = 1536;

    /**
     * 知识库:文档状态:草稿
     */
    public static final String KNOWLEDGE_DOC_STATUS_DRAFT = "draft";
    /**
     * 知识库:文档状态:构建中
     */
    public static final String KNOWLEDGE_DOC_STATUS_BUILDING = "building";
    /**
     * 知识库:文档状态:构建完成
     */
    public static final String KNOWLEDGE_DOC_STATUS_COMPLETE = "complete";
    /**
     * 知识库:文档状态:构建失败
     */
    public static final String KNOWLEDGE_DOC_STATUS_FAILED = "failed";

    /**
     * 知识库:文档类型:文本
     */
    public static final String KNOWLEDGE_DOC_TYPE_TEXT = "text";
    /**
     * 知识库:文档类型:文件
     */
    public static final String KNOWLEDGE_DOC_TYPE_FILE = "file";
    /**
     * 知识库:文档类型:网页
     */
    public static final String KNOWLEDGE_DOC_TYPE_WEB = "web";

    /**
     * 知识库:文档元数据:文件路径
     */
    public static final String KNOWLEDGE_DOC_METADATA_FILEPATH = "filePath";

    /**
     * 知识库:文档元数据:资源路径
     */
    public static final String KNOWLEDGE_DOC_METADATA_SOURCES_PATH = "sourcesPath";

    /**
     * 知识库:文档元数据:网页URL
     */
    public static final String KNOWLEDGE_DOC_METADATA_WEBSITE = "website";

    /**
     * DEEPSEEK推理模型
     */
    public static final String DEEPSEEK_REASONER = "deepseek-reasoner";

    /**
     * 知识库类型：知识库
     */
    public static final String KNOWLEDGE_TYPE_KNOWLEDGE = "knowledge";
    
    /**
     * 知识库类型：记忆库
     */
    public static final String KNOWLEDGE_TYPE_MEMORY = "memory";

    /**
     * 支持文件的后缀
     */
    public static final Set<String> CHAT_FILE_EXT_WHITELIST = new HashSet<>(Arrays.asList("txt", "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "md"));

    /**
     * 文件内容最大长度
     */
    public static final int CHAT_FILE_TEXT_MAX_LENGTH = 20000;

    /**
     * 上传文件对打数量
     */
    public static final int CHAT_FILE_MAX_COUNT = 3;

    /**
     * 知识库是否开启默认分段策略
     */
    public static final String ENABLE_SEGMENT = "enableSegment";

    /**
     * 文档分段策略：使用知识库默认分段策略
     */
    public static final String USE_KNOWLEDGE_DEFAULT = "useKnowledgeDefault";

    /**
     * 分段策略
     */
    public static final String SEGMENT_STRATEGY = "segmentStrategy";
    
    /**
     * 分段策略：auto 自动分段与清洗
     */
    public static final String SEGMENT_STRATEGY_AUTO = "auto";
    
    /**
     * 分段策略：custom 自定义
     */
    public static final String SEGMENT_STRATEGY_CUSTOM = "custom";

    /**
     * 分段长度
     */
    public static final String MAX_SEGMENT = "maxSegment";

    /**
     * 重叠率 0-90%
     */
    public static final String OVERLAP = "overlap";
    
    /**
     * 分段标识符(\\n:换行，\\n\\n:2个换行，。:中文句号，！:中文叹号，？:中文问号，. :英文句号，! :英文叹号，? :英文问号，custom:自定义)
     */
    public static final String SEPARATOR = "separator";
    
    /**
     * 分段标识符自定义
     */
    public static final String CUSTOM_SEPARATOR = "customSeparator";

    /**
     * 文本预处理规则（cleanSpaces：替换掉连续的空格、换行符和制表符，removeUrlsEmails：删除所有 URL 和电子邮箱地址）
     */
    public static final String TEXT_RULES = "textRules";

    /**
     * 替换掉连续的空格、换行符和制表符
     */
    public static final String TEXT_RULES_CLEAN_SPACES = "cleanSpaces";

    /**
     * 删除所有URL和电子邮箱地址
     */
    public static final String TEXT_RULES_REMOVE_URLS_EMAILS = "removeUrlsEmails";
}
