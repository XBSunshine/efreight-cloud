package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.AwbPrintSize;
import com.efreight.afbase.dao.AwbPrintSizeMapper;
import com.efreight.afbase.service.AwbPrintSizeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AF 操作管理 运单制单 尺寸表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@Service
public class AwbPrintSizeServiceImpl extends ServiceImpl<AwbPrintSizeMapper, AwbPrintSize> implements AwbPrintSizeService {

    @Override
    public void deleteByAwbPrintId(String awbPrintId) {
        LambdaQueryWrapper<AwbPrintSize> wrapper = Wrappers.<AwbPrintSize>lambdaQuery();
        wrapper.eq(AwbPrintSize::getAwbPrintId,awbPrintId).eq(AwbPrintSize::getOrgId, SecurityUtils.getUser().getOrgId());
        baseMapper.delete(wrapper);
    }
}
