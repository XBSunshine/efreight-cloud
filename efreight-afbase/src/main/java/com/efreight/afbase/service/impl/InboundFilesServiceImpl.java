package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.InboundFiles;
import com.efreight.afbase.dao.InboundFilesMapper;
import com.efreight.afbase.service.InboundFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * AF 操作计划 操作出重表 照片文件 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-05
 */
@Service
public class InboundFilesServiceImpl extends ServiceImpl<InboundFilesMapper, InboundFiles> implements InboundFilesService {
    @Override
    public Integer saveInboundFile(InboundFiles inboundFiles) {
        if (StrUtil.isBlank(inboundFiles.getFileUrl())) {
            throw new RuntimeException("文件上传路径不能为空");
        }
        if (inboundFiles.getInboundId() == null) {
            throw new RuntimeException("出重id不能为空");
        }
        inboundFiles.setCreateTime(LocalDateTime.now());
        inboundFiles.setCreatorId(SecurityUtils.getUser().getId());
        inboundFiles.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        inboundFiles.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(inboundFiles);
        return inboundFiles.getFileId();
    }

    @Override
    public void delete(Integer fileId) {

        baseMapper.deleteById(fileId);
    }

    @Override
    public List<InboundFiles> getList(Integer inboundId) {
        LambdaQueryWrapper<InboundFiles> wrapper = Wrappers.<InboundFiles>lambdaQuery();
        wrapper.eq(InboundFiles::getInboundId, inboundId).eq(InboundFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        return baseMapper.selectList(wrapper);
    }
}
