package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.IoOrderFiles;
import com.efreight.sc.dao.IoOrderFilesMapper;
import com.efreight.sc.entity.IoOrderFiles;
import com.efreight.sc.service.IoOrderFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * IO 订单管理 其他业务订单附件 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Service
public class IoOrderFilesServiceImpl extends ServiceImpl<IoOrderFilesMapper, IoOrderFiles> implements IoOrderFilesService {

    @Override
    public List<IoOrderFiles> getList(Integer orderId) {
        LambdaQueryWrapper<IoOrderFiles> ioOrderFilesWrapper = Wrappers.<IoOrderFiles>lambdaQuery();
        ioOrderFilesWrapper.eq(IoOrderFiles::getOrderId, orderId).eq(IoOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        List<IoOrderFiles> list = list(ioOrderFilesWrapper);
        return list;
    }

    @Override
    public void insert(IoOrderFiles ioOrderFiles) {
        ioOrderFiles.setCreateTime(LocalDateTime.now());
        ioOrderFiles.setCreatorId(SecurityUtils.getUser().getId());
        ioOrderFiles.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioOrderFiles.setOrgId(SecurityUtils.getUser().getOrgId());
        save(ioOrderFiles);
    }


    @Override
    public void modifty(IoOrderFiles ioOrderFiles) {
        updateById(ioOrderFiles);
    }

    @Override
    public void delete(Integer orderFileId) {
        removeById(orderFileId);
    }

    @Override
    public List<IoOrderFiles> getListByOrderFileIds(String orderFileIds) {
        if (StrUtil.isNotBlank(orderFileIds)) {
            LambdaQueryWrapper<IoOrderFiles> wrapper = Wrappers.<IoOrderFiles>lambdaQuery();
            wrapper.eq(IoOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).in(IoOrderFiles::getOrderFileId, orderFileIds.split(","));
            return list(wrapper);
        }
        return null;
    }
}
