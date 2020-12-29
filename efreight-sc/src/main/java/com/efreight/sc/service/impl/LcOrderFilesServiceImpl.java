package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcOrderFiles;
import com.efreight.sc.dao.LcOrderFilesMapper;
import com.efreight.sc.service.LcOrderFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * LC 订单管理 订单附件 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
public class LcOrderFilesServiceImpl extends ServiceImpl<LcOrderFilesMapper, LcOrderFiles> implements LcOrderFilesService {

    @Override
    public List<LcOrderFiles> getList(Integer orderId) {
        LambdaQueryWrapper<LcOrderFiles> lcOrderFilesWrapper = Wrappers.<LcOrderFiles>lambdaQuery();
        lcOrderFilesWrapper.eq(LcOrderFiles::getOrderId, orderId).eq(LcOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        List<LcOrderFiles> list = list(lcOrderFilesWrapper);
        return list;
    }

    @Override
    public void insert(LcOrderFiles lcOrderFiles) {
        lcOrderFiles.setCreateTime(LocalDateTime.now());
        lcOrderFiles.setCreatorId(SecurityUtils.getUser().getId());
        lcOrderFiles.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcOrderFiles.setOrgId(SecurityUtils.getUser().getOrgId());
        save(lcOrderFiles);
    }


    @Override
    public void modifty(LcOrderFiles lcOrderFiles) {
        updateById(lcOrderFiles);
    }

    @Override
    public void delete(Integer orderFileId) {
        removeById(orderFileId);
    }

    @Override
    public List<LcOrderFiles> getListByOrderFileIds(String orderFileIds) {
        if (StrUtil.isNotBlank(orderFileIds)) {
            LambdaQueryWrapper<LcOrderFiles> wrapper = Wrappers.<LcOrderFiles>lambdaQuery();
            wrapper.eq(LcOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).in(LcOrderFiles::getOrderFileId, orderFileIds.split(","));
            return list(wrapper);
        }
        return null;
    }
}
