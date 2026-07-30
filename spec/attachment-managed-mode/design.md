# 附件统一管理改造方案

## 一、方案概述

在现有上传体系中增加 `bizCode` 参数，实现附件的集中管理。

- **旧方式（默认）**：不传 `bizCode`，业务表直接存储文件路径，完全兼容现有代码
- **新方式（托管模式）**：传入 `bizCode`，文件信息存入 `sys_attachment` 表，业务表只存 `file_id`；无需手动确认关联，定时任务通过 `bizCode` 反查业务表判断是否被引用

**托管模式路径规则：**
- 前端只需传入 `bizCode`，无需指定 `bizPath`
- 后端自动生成存储路径：`{bizCode}/{yyyy-MM}/{file}`
- 示例：`my_report.attachment/2026-07/xxx_1234567890.pdf`

**bizCode 命名规范：**
- 格式：`{table_name}.{field_name}`，与数据库表名和字段名完全对应
- 示例：`my_report.attachment`、`sys_user.avatar`、`contract.file`
- 前端直接传入字符串，无需工具函数

## 二、数据库设计

### 2.1 新建附件表 `sys_attachment`

```sql
CREATE TABLE IF NOT EXISTS `sys_attachment` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `file_name` varchar(200) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径/URL',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型（pdf/doc/xlsx/image等）',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `storage_type` varchar(20) NOT NULL COMMENT '存储方式（local/minio/alioss）',
  `biz_code` varchar(50) DEFAULT NULL COMMENT '业务标识，格式：{table_name}.{field_name}',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_by` varchar(32) DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `owner_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门编码',
  PRIMARY KEY (`id`),
  INDEX `idx_biz_code` (`biz_code`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统附件表';
```

**字段说明：**

| 字段 | 说明 |
|------|------|
| `file_name` | 用户上传时的原始文件名 |
| `file_path` | 存储路径（local 为相对路径，minio/oss 为完整 URL） |
| `file_size` | 文件大小，单位字节 |
| `file_type` | 文件扩展名（小写），如 `pdf`、`jpg` |
| `mime_type` | MIME 类型，如 `application/pdf`、`image/jpeg` |
| `storage_type` | 上传时的存储方式，便于后续迁移 |
| `biz_code` | 业务标识，格式 `{table_name}.{field_name}`（如 `my_report.attachment`），用于区分业务场景，也可用于识别孤儿附件 |
| `delete_by` / `delete_time` | 软删除标记，非空表示已删除 |

**孤儿附件识别逻辑：**

定时任务通过 `biz_code` 提取表名和字段名，反查业务表判断是否被引用：

```sql
-- 示例：查找 my_report.attachment 的孤儿附件
-- 1. 从 biz_code 提取表名和字段名
-- 2. 查询业务表中是否存在该 file_id 的引用
-- 3. 若不存在且超过24小时，则判定为孤儿附件

-- 伪 SQL 示例
SELECT a.* 
FROM sys_attachment a
WHERE a.biz_code = 'my_report.attachment'
  AND a.delete_time IS NULL
  AND a.create_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)
  AND NOT EXISTS (
    SELECT 1 FROM my_report r 
    WHERE FIND_IN_SET(a.id, r.attachment) > 0
  );
```

## 三、后端改造

### 3.1 新增实体类

**文件路径：** `jeecg-module-system/jeecg-system-biz/src/main/java/com/cssz/modules/file/entity/SysAttachment.java`

```java
@Data
@TableName("sys_attachment")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "系统附件")
public class SysAttachment extends CsEntity {
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    private String storageType;
    private String bizCode;
}
```

### 3.2 新增 Mapper/Service

**文件路径：**
- `jeecg-module-system/jeecg-system-biz/src/main/java/com/cssz/modules/file/mapper/SysAttachmentMapper.java`
- `jeecg-module-system/jeecg-system-biz/src/main/java/com/cssz/modules/file/service/ISysAttachmentService.java`
- `jeecg-module-system/jeecg-system-biz/src/main/java/com/cssz/modules/file/service/impl/SysAttachmentServiceImpl.java`

### 3.3 新增 SysFileController

**文件路径：** `jeecg-module-system/jeecg-system-biz/src/main/java/com/cssz/modules/file/controller/SysFileController.java`（新建）

**路由前缀：** `/sys/file`

将附件管理相关接口独立为 Controller，职责清晰，路由简洁。

```java
@Slf4j
@RestController
@RequestMapping("/sys/file")
@Api(tags = "系统附件管理")
public class SysFileController {

    @Autowired
    private ISysAttachmentService sysAttachmentService;

    @Value(value = "${jeecg.uploadType}")
    private String uploadType;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * 托管模式上传
     * POST /sys/file/upload
     * 参数：bizCode（必填）、file（文件）
     * 返回：fileId、fileName、fileSize
     */
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result<?> result = new Result<>();
        String bizCode = request.getParameter("bizCode");
        if (oConvertUtils.isEmpty(bizCode)) {
            return Result.error("bizCode 不能为空");
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        String bizPath = bizCode + "/" + DateUtil.format(new Date(), "yyyy-MM");

        SsrfFileTypeFilter.checkUploadFileType(file, bizPath);

        String savePath;
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            savePath = this.uploadLocal(file, bizPath);
        } else {
            savePath = CommonUtils.upload(file, bizPath, uploadType);
        }

        if (oConvertUtils.isNotEmpty(savePath)) {
            SysAttachment attachment = new SysAttachment();
            attachment.setFileName(CommonUtils.getFileName(file.getOriginalFilename()));
            attachment.setFilePath(savePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(getFileExtension(file.getOriginalFilename()));
            attachment.setMimeType(file.getContentType());
            attachment.setStorageType(uploadType);
            attachment.setBizCode(bizCode);
            sysAttachmentService.save(attachment);

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", attachment.getId());
            fileInfo.put("fileName", file.getOriginalFilename());
            fileInfo.put("fileSize", file.getSize());
            result.setResult(fileInfo);
            result.setSuccess(true);
        } else {
            result.setMessage("上传失败！");
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 获取文件信息
     * GET /sys/file/info?id=xxx
     */
    @GetMapping(value = "/info")
    public Result<SysAttachment> info(@RequestParam String id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null || attachment.getDeleteTime() != null) {
            return Result.error("文件不存在");
        }
        return Result.OK(attachment);
    }

    /**
     * 获取文件访问 URL
     * GET /sys/file/url?id=xxx
     */
    @GetMapping(value = "/url")
    public Result<String> url(@RequestParam String id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null || attachment.getDeleteTime() != null) {
            return Result.error("文件不存在");
        }
        return Result.OK(attachment.getFilePath());
    }

    /**
     * 软删除附件
     * POST /sys/file/delete?id=xxx
     */
    @PostMapping(value = "/delete")
    public Result<?> delete(@RequestParam String id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null) {
            return Result.error("文件不存在");
        }
        attachment.setDeleteBy(LoginUserUtils.getLoginUser().getUsername());
        attachment.setDeleteTime(new Date());
        sysAttachmentService.updateById(attachment);
        return Result.OK();
    }

    /**
     * 本地文件上传
     */
    private String uploadLocal(MultipartFile mf, String bizPath) throws IOException {
        String ctxPath = uploadpath;
        String fileName = null;
        File file = new File(ctxPath + File.separator + bizPath + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        String orgName = mf.getOriginalFilename();
        orgName = CommonUtils.getFileName(orgName);
        if (orgName.indexOf(SymbolConstant.SPOT) != -1) {
            fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."));
        } else {
            fileName = orgName + "_" + System.currentTimeMillis();
        }
        String savePath = file.getPath() + File.separator + fileName;
        File savefile = new File(savePath);
        FileCopyUtils.copy(mf.getBytes(), savefile);
        String dbpath = bizPath + File.separator + fileName;
        if (dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
            dbpath = dbpath.replace(SymbolConstant.DOUBLE_BACKSLASH, SymbolConstant.SINGLE_SLASH);
        }
        return dbpath;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
```

**文件访问接口 `/sys/file/view/{id}`**：托管模式下的文件访问统一通过此入口，校验软删除状态后直接返回文件流或重定向到云存储地址。CommonController 的 `view()` 保持不变，旧模式仍走 `/sys/common/static/**`，零侵入。

```java
/**
 * 文件访问入口（托管模式）
 * GET /sys/file/view/{id}
 * 
 * 校验软删除状态后：
 * - local 存储：直接读取文件流返回（减少一次重定向）
 * - minio/oss 存储：重定向到完整 URL
 */
@GetMapping(value = "/view/{id}")
public void view(@PathVariable String id, HttpServletResponse response) throws IOException {
    SysAttachment attachment = sysAttachmentService.getById(id);
    if (attachment == null || attachment.getDeleteTime() != null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
    }

    if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(attachment.getStorageType())) {
        // local 存储：直接读取文件流返回
        String filePath = attachment.getFilePath();
        // 安全检查：过滤路径遍历
        filePath = filePath.replace("..", "").replace("../", "");
        SsrfFileTypeFilter.checkDownloadFileType(filePath);

        String fullPath = uploadpath + File.separator + filePath;
        File file = new File(fullPath);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.warn("文件[{}]不存在..", filePath);
            return;
        }
        // 设置响应头，使用原始文件名
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + new String(attachment.getFileName().getBytes("UTF-8"), "iso-8859-1"));
        // 流式返回文件
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();
        }
    } else {
        // minio/oss 存储：重定向到完整 URL
        response.sendRedirect(attachment.getFilePath());
    }
}
```

**路由对照表：**

| 路由 | 方法 | 说明 |
|------|------|------|
| `POST /sys/file/upload` | upload | 托管模式上传，返回 fileId、fileName、fileSize |
| `GET /sys/file/info` | info | 根据 file_id 获取文件信息 |
| `GET /sys/file/url` | url | 根据 file_id 获取文件访问 URL |
| `GET /sys/file/view/{id}` | view | 文件访问入口，local 直接返回文件流，minio/oss 重定向 |
| `POST /sys/file/delete` | delete | 软删除附件 |

**前端使用方式：**

```typescript
// 托管模式预览图片（推荐，直接使用 view 接口）
<img :src="`/sys/file/view/${fileId}`" />

// 或获取文件信息后使用（适用于下载等需要真实 URL 的场景）
const info = await getFileInfo(fileId);
const url = info.filePath; // local 存储为相对路径，minio/oss 为完整 URL
```

### 3.4 定时清理孤儿文件

**放置位置**：`SysAttachmentServiceImpl` 中（或独立的定时任务类 `SysAttachmentCleanTask`）

**核心逻辑**：通过 `biz_code` 提取表名和字段名，反查业务表判断附件是否被引用。

```java
/**
 * 定时清理孤儿附件
 * 通过 bizCode 反查业务表，判断附件是否被引用
 */
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
public void cleanOrphanFiles() {
    Date threshold = DateUtil.offsetHour(new Date(), -24);
    
    // 查询所有未删除且超过24小时的附件，按 bizCode 分组
    List<SysAttachment> candidates = sysAttachmentService.lambdaQuery()
        .isNotNull(SysAttachment::getBizCode)
        .isNull(SysAttachment::getDeleteTime)
        .lt(SysAttachment::getCreateTime, threshold)
        .list();
    
    // 按 bizCode 分组处理
    Map<String, List<SysAttachment>> grouped = candidates.stream()
        .collect(Collectors.groupingBy(SysAttachment::getBizCode));
    
    List<String> orphanIds = new ArrayList<>();
    
    for (Map.Entry<String, List<SysAttachment>> entry : grouped.entrySet()) {
        String bizCode = entry.getKey();
        List<SysAttachment> attachments = entry.getValue();
        
        // 解析 bizCode: {table_name}.{field_name}
        String[] parts = bizCode.split("\\.");
        if (parts.length != 2) continue;
        String tableName = parts[0];
        String fieldName = parts[1];
        
        // 提取所有 file_id
        List<String> fileIds = attachments.stream()
            .map(SysAttachment::getId)
            .collect(Collectors.toList());
        
        // 动态查询业务表，判断哪些 file_id 被引用
        List<String> referencedIds = findReferencedFileIds(tableName, fieldName, fileIds);
        
        // 未被引用的即为孤儿
        for (SysAttachment attachment : attachments) {
            if (!referencedIds.contains(attachment.getId())) {
                orphanIds.add(attachment.getId());
                deleteStorageFile(attachment);
            }
        }
    }
    
    // 批量物理删除孤儿记录
    if (!orphanIds.isEmpty()) {
        sysAttachmentService.removeByIds(orphanIds);
    }
}

/**
 * 查询业务表，返回被引用的 file_id 列表
 * 使用 JdbcTemplate 执行动态 SQL
 */
private List<String> findReferencedFileIds(String tableName, String fieldName, List<String> fileIds) {
    if (fileIds == null || fileIds.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 构建动态 SQL：支持逗号分隔的 file_id 字段
    // 业务表字段可能存储单个 file_id 或逗号分隔的多个 file_id
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT DISTINCT ").append(fieldName).append(" FROM ").append(tableName);
    sql.append(" WHERE ");
    
    // 条件1：字段值等于某个 file_id（单值场景）
    sql.append(fieldName).append(" IN (");
    sql.append(fileIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")));
    sql.append(")");
    
    // 条件2：字段包含某个 file_id（逗号分隔场景）
    for (String fileId : fileIds) {
        sql.append(" OR FIND_IN_SET('").append(fileId).append("', ").append(fieldName).append(") > 0");
    }
    
    // 使用 JdbcTemplate 执行原生 SQL
    List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());
    
    // 提取所有被引用的 file_id
    Set<String> referencedIds = new HashSet<>();
    for (Map<String, Object> row : results) {
        Object value = row.get(fieldName);
        if (value != null) {
            String strValue = value.toString();
            // 处理逗号分隔的情况
            if (strValue.contains(",")) {
                for (String id : strValue.split(",")) {
                    if (fileIds.contains(id.trim())) {
                        referencedIds.add(id.trim());
                    }
                }
            } else if (fileIds.contains(strValue)) {
                referencedIds.add(strValue);
            }
        }
    }
    
    return new ArrayList<>(referencedIds);
}

/**
 * 删除存储介质中的文件
 * 根据 storageType 选择不同的删除方式
 */
private void deleteStorageFile(SysAttachment attachment) {
    String storageType = attachment.getStorageType();
    String filePath = attachment.getFilePath();
    
    if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(storageType)) {
        // 本地存储：删除物理文件
        File file = new File(uploadpath + File.separator + filePath);
        if (file.exists()) {
            file.delete();
        }
    } else if ("minio".equals(storageType)) {
        // MinIO 存储：调用 MinIO 客户端删除
        // TODO: 实现 MinIO 删除逻辑
    } else if ("alioss".equals(storageType)) {
        // 阿里云 OSS 存储：调用 OSS 客户端删除
        // TODO: 实现 OSS 删除逻辑
    }
}
```

## 四、前端改造

### 4.1 新建 CsUpload 组件

**文件路径：** `jeecgboot-vue3/src/components/cssz/CsUpload/CsUpload.vue`（新建）

仿照 JUpload 组件，开发纯净的全新附件上传统一管理组件，仅支持托管模式。

**核心特性：**
- `bizCode` 为必填参数，格式：`{table_name}.{field_name}`
- 固定使用 `/sys/file/upload` 上传接口
- 返回值为逗号分隔的 `file_id`
- 预览通过 `/sys/file/view/{id}` 接口
- 支持图片模式和文件模式
- 支持最大上传数量限制
- 支持删除确认

```typescript
// Props 定义
const props = defineProps({
  value: propTypes.string.def(''),           // 绑定值，逗号分隔的 file_id
  text: propTypes.string.def('上传'),         // 按钮文字
  fileType: propTypes.string.def('all'),     // 上传类型：all/image/file
  bizCode: propTypes.string.require,         // 业务标识（必填）
  maxCount: propTypes.number.def(0),         // 最大上传数
  buttonVisible: propTypes.bool.def(true),   // 显示按钮
  multiple: propTypes.bool.def(true),        // 允许多文件
  mover: propTypes.bool.def(true),           // 显示移动按钮
  download: propTypes.bool.def(true),        // 显示下载按钮
  removeConfirm: propTypes.bool.def(false),  // 删除确认
  beforeUpload: propTypes.func,              // 上传前校验
  disabled: propTypes.bool.def(false),       // 禁用
  replaceLastOne: propTypes.bool.def(false), // 替换最后一个
});

// 上传参数
const bindProps = computed(() => {
  const bind: any = Object.assign({}, props, unref(attrs));
  bind.name = 'file';
  bind.listType = isImageMode.value ? 'picture-card' : 'text';
  // 固定使用托管模式上传接口
  bind.action = uploadManagedUrl;
  bind.data = {
    bizCode: props.bizCode,
    ...bind.data,
  };
  // ...
});

// 上传成功处理
function onFileChange(info) {
  if (info.file.status === 'done') {
    if (info.file.response.success) {
      // 从 response.result 获取文件信息
      const result = info.file.response.result;
      file.fileId = result.fileId;
      file.url = `${apiUrl}/sys/file/view/${result.fileId}`;
      file.name = file.name || result.fileName;
    }
  }
}

// 返回值：逗号分隔的 file_id
function handleFileIdChange() {
  let fileIdList: string[] = [];
  for (const item of fileList.value) {
    if (item.status === 'done') {
      fileIdList.push(item.fileId);
    }
  }
  emitValue(fileIdList.join(','));
}
```

### 4.3 其他涉及组件（暂不改造）

以下组件涉及文件上传/预览，后续按需适配：

| 组件 | 文件路径 | 说明 |
|------|----------|------|
| JImageUpload | `src/components/Form/src/jeecg/components/JImageUpload/JImageUpload.vue` | 图片上传，逻辑与 JUpload 类似，使用 Options API |
| JUploadDrag | `src/components/Form/src/jeecg/components/JUpload/JUploadDrag.vue` | 拖拽上传 |

### 4.4 前端文件预览工具

**保持 `getFileAccessHttpUrl()` 不变**（同步，零侵入）。移除 `resolveFileUrl` 的魔法数字检测，改为让调用方自行明确文件类型：

**文件路径：** `jeecgboot-vue3/src/utils/common/compUtils.ts`

```typescript
// 保持原有同步函数不变
export const getFileAccessHttpUrl = (fileUrl, prefix = 'http') => {
  // 原有逻辑不变
};
```

### 4.5 前端 API 新增

**文件路径：** `jeecgboot-vue3/src/api/common/api.ts`

```typescript
/**
 * 根据 file_id 获取文件信息
 */
export const getFileInfo = (id: string) => {
  return defHttp.get({ url: '/sys/file/info', params: { id } });
};
```

## 五、使用示例

### 5.1 旧方式（默认，完全兼容）

```vue
<template>
  <JUpload v-model:value="formModel.files" bizPath="report/doc" />
</template>

<script setup>
const formModel = reactive({
  files: '' // 存储: "upload/doc/xxx_123.pdf,upload/doc/yyy_456.pdf"
});
</script>
```

### 5.2 新方式（托管模式，使用 CsUpload）

```vue
<template>
  <!-- 托管模式：使用 CsUpload 组件，只需传 bizCode，不需要 bizPath -->
  <!-- 后端自动生成存储路径：my_report.attachment/2026-07/xxx.pdf -->
  <CsUpload v-model:value="formModel.attachment" :bizCode="bizCode" />
</template>

<script setup>
import { CsUpload } from '/@/components/cssz/CsUpload';

// bizCode 格式：表名.字段名
const bizCode = 'my_report.attachment';

const formModel = reactive({
  attachment: '' // 存储: "1812345678901234567,1812345678901234568" (file_id)
});

// 业务提交
async function handleSubmit() {
  await defHttp.post({ url: '/myReport/add', params: formModel });
  // 无需手动确认关联，定时任务会自动清理未引用的孤儿附件
}
</script>
```

### 5.3 预览文件

**托管模式（推荐）**：直接使用 `/sys/file/view` 接口，无需异步获取 URL，兼容所有存储方式。

```vue
<template>
  <!-- 托管模式：直接用 view 接口预览 -->
  <img :src="`/sys/file/view/${fileId}`" />
</template>
```

## 六、兼容性说明

| 场景 | 影响 |
|------|------|
| 现有业务代码 | 零影响，旧模式使用 JUpload，托管模式使用 CsUpload |
| JUpload / JImageUpload 组件 | 零影响，不修改现有组件 |
| CsUpload 组件 | 纯新增，仅支持托管模式，`bizCode` 为必填参数 |
| 后端上传接口 | 零影响，`bizCode` 参数可选 |
| `getFileAccessHttpUrl()` | 零影响，保持同步不变 |

## 七、文件清单与实施计划

### 阶段一：数据库准备

**实施步骤：**
1. 执行 SQL 创建 `sys_attachment` 表

**新增文件：**

| 文件 | 说明 |
|------|------|
| `db/V20260720_0__attachment_create_sys_attachment.sql` | 建表脚本 |

**风险与应对：**
- 无特殊风险

---

### 阶段二：前后端开发

**实施步骤：**

后端：
1. 新增 SysAttachment 实体类、Mapper、Service
2. 新增 SysFileController（upload/info/url/view/delete 接口）
3. 新增定时清理孤儿附件任务（通过 bizCode 反查业务表判断引用）

前端：
1. 新建 CsUpload 组件（仅支持托管模式），放置于 `jeecgboot-vue3/src/components/cssz/CsUpload/`
2. API 层新增 getFileInfo 方法

**新增文件：**

| 文件 | 说明 |
|------|------|
| `jeecg-module-system/.../com/cssz/modules/file/entity/SysAttachment.java` | 附件实体类 |
| `jeecg-module-system/.../com/cssz/modules/file/mapper/SysAttachmentMapper.java` | Mapper 接口 |
| `jeecg-module-system/.../com/cssz/modules/file/service/ISysAttachmentService.java` | Service 接口 |
| `jeecg-module-system/.../com/cssz/modules/file/service/impl/SysAttachmentServiceImpl.java` | Service 实现 |
| `jeecg-module-system/.../com/cssz/modules/file/controller/SysFileController.java` | 附件管理 Controller（路由前缀 `/sys/file`） |
| `jeecgboot-vue3/src/components/cssz/CsUpload/CsUpload.vue` | 托管模式上传组件 |
| `jeecgboot-vue3/src/components/cssz/CsUpload/index.ts` | 导出入口 |

**修改文件：**

| 文件 | 改动点 |
|------|--------|
| `api/common/api.ts` | 新增 getFileInfo 方法（路由 `/sys/file/info`） |

**风险与应对：**

| 风险 | 等级 | 应对 |
|------|------|------|
| 动态 SQL 注入 | 中 | bizCode 格式校验：只允许字母、数字、下划线，使用正则 `^[a-zA-Z0-9_]+$` |
| 孤儿文件堆积 | 低 | 定时清理任务每天凌晨 2 点执行，通过 bizCode 反查业务表判断引用 |
| 文件删除失败 | 低 | deleteStorageFile() 方法需处理异常，记录日志但不中断整个清理流程 |
| 现有代码受影响 | 低 | 不传 `bizCode` 即为旧模式，完全兼容 |
| 前端预览性能 | 低 | file_id 查询是单条记录，响应快 |

---

### 阶段三：测试与文档

**实施步骤：**
1. 测试：验证旧模式不受影响，新模式功能正常
2. 文档：更新 `spec/dev-guide/attachment-guide.md`

**风险与应对：**

| 风险 | 等级 | 应对 |
|------|------|------|
| 业务提交失败 | 低 | 附件无 status 字段，定时任务通过反查业务表判断是否被引用，未被引用则清理 |
