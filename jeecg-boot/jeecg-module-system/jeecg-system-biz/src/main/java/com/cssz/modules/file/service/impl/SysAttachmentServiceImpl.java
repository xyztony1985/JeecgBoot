package com.cssz.modules.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cssz.modules.file.entity.SysAttachment;
import com.cssz.modules.file.mapper.SysAttachmentMapper;
import com.cssz.modules.file.service.ISysAttachmentService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description: 系统附件 Service 实现
 */
@Slf4j
@Service
public class SysAttachmentServiceImpl extends ServiceImpl<SysAttachmentMapper, SysAttachment> implements ISysAttachmentService {
}
