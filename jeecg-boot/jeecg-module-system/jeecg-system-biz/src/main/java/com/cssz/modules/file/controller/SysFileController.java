package com.cssz.modules.file.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cssz.modules.file.entity.SysAttachment;
import com.cssz.modules.file.service.ISysAttachmentService;
import com.cssz.util.UserUtil;
import org.jeecg.common.constant.SymbolConstant;

import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;

/**
 * @Description: 系统附件管理
 */
@Slf4j
@RestController
@RequestMapping("/sys/file")
@Tag(name = "系统附件管理")
public class SysFileController {

    @Autowired
    private ISysAttachmentService sysAttachmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value(value = "${jeecg.uploadType}")
    private String uploadType;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /** bizCode 中 tableName 和 fieldName 的校验正则：只允许字母、数字、下划线 */
    private static final Pattern BIZ_CODE_PART_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * 托管模式上传
     * POST /sys/file/upload
     * 参数：bizCode（必填）、file（文件）
     * 返回：file_id
     */
    @Operation(summary = "托管模式上传", description = "传入 bizCode 和文件，返回 file_id")
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result<?> result = new Result<>();
        String bizCode = request.getParameter("bizCode");
        if (oConvertUtils.isEmpty(bizCode)) {
            return Result.error("bizCode 不能为空");
        }

        // 校验 bizCode 格式，防止 SQL 注入
        String[] parts = bizCode.split("\\.");
        if (parts.length != 2 || !BIZ_CODE_PART_PATTERN.matcher(parts[0]).matches() || !BIZ_CODE_PART_PATTERN.matcher(parts[1]).matches()) {
            return Result.error("bizCode 格式不正确，应为 {table_name}.{field_name}，只允许字母、数字、下划线");
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

            result.setMessage(attachment.getId());
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
    @Operation(summary = "获取文件信息", description = "根据 file_id 获取文件详细信息")
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
    @Operation(summary = "获取文件 URL", description = "根据 file_id 获取文件访问 URL")
    @GetMapping(value = "/url")
    public Result<String> url(@RequestParam String id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null || attachment.getDeleteTime() != null) {
            return Result.error("文件不存在");
        }
        return Result.OK(attachment.getFilePath());
    }

    /**
     * 文件访问入口（托管模式）
     * GET /sys/file/view/{id}
     * 校验软删除状态后，重定向到实际文件地址
     */
    @Operation(summary = "文件访问入口", description = "校验软删除状态后重定向到实际文件地址")
    @GetMapping(value = "/view/{id}")
    public void view(@PathVariable String id, HttpServletResponse response) throws IOException {
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null || attachment.getDeleteTime() != null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String redirectUrl;
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(attachment.getStorageType())) {
            // local 存储：重定向到 CommonController 的静态文件访问接口
            redirectUrl = "/sys/common/static/" + attachment.getFilePath();
        } else {
            // minio/oss 存储：直接重定向到完整 URL
            redirectUrl = attachment.getFilePath();
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * 软删除附件
     * POST /sys/file/delete?id=xxx
     */
    @Operation(summary = "软删除附件", description = "软删除附件，不删除物理文件")
    @PostMapping(value = "/delete")
    public Result<?> delete(@RequestParam String id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null) {
            return Result.error("文件不存在");
        }
        attachment.setDeleteBy(UserUtil.getCurrentUsername());
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

    /**
     * 定时清理孤儿附件
     * 通过 bizCode 反查业务表，判断附件是否被引用
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOrphanFiles() {
        log.info("【孤儿附件清理】开始执行");
        Date threshold = DateUtil.offsetHour(new Date(), -24);

        // 查询所有未删除且超过24小时的附件，按 bizCode 分组
        LambdaQueryWrapper<SysAttachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(SysAttachment::getBizCode)
                .isNull(SysAttachment::getDeleteTime)
                .lt(SysAttachment::getCreateTime, threshold);
        List<SysAttachment> candidates = sysAttachmentService.list(queryWrapper);

        if (candidates.isEmpty()) {
            log.info("【孤儿附件清理】无待清理附件");
            return;
        }

        // 按 bizCode 分组处理
        Map<String, List<SysAttachment>> grouped = candidates.stream()
                .collect(Collectors.groupingBy(SysAttachment::getBizCode));

        List<String> orphanIds = new ArrayList<>();

        for (Map.Entry<String, List<SysAttachment>> entry : grouped.entrySet()) {
            String bizCode = entry.getKey();
            List<SysAttachment> attachments = entry.getValue();

            // 解析 bizCode: {table_name}.{field_name}
            String[] parts = bizCode.split("\\.");
            if (parts.length != 2) {
                continue;
            }
            String tableName = parts[0];
            String fieldName = parts[1];

            // 校验表名和字段名格式，防止 SQL 注入
            if (!BIZ_CODE_PART_PATTERN.matcher(tableName).matches() || !BIZ_CODE_PART_PATTERN.matcher(fieldName).matches()) {
                log.warn("【孤儿附件清理】bizCode 格式不合法，跳过：{}", bizCode);
                continue;
            }

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
            log.info("【孤儿附件清理】清理完成，共清理 {} 个孤儿附件", orphanIds.size());
        } else {
            log.info("【孤儿附件清理】清理完成，无孤儿附件");
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

        try {
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(storageType)) {
                // 本地存储：删除物理文件
                File file = new File(uploadpath + File.separator + filePath);
                if (file.exists()) {
                    file.delete();
                }
            } else if ("minio".equals(storageType)) {
                // MinIO 存储：调用 MinIO 客户端删除
                // TODO: 实现 MinIO 删除逻辑
                log.warn("【孤儿附件清理】MinIO 删除暂未实现，文件路径：{}", filePath);
            } else if ("alioss".equals(storageType)) {
                // 阿里云 OSS 存储：调用 OSS 客户端删除
                // TODO: 实现 OSS 删除逻辑
                log.warn("【孤儿附件清理】OSS 删除暂未实现，文件路径：{}", filePath);
            }
        } catch (Exception e) {
            log.error("【孤儿附件清理】删除文件失败：{}", filePath, e);
        }
    }
}
