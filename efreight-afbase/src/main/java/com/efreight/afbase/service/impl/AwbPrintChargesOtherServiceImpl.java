package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.AwbPrintChargesOther;
import com.efreight.afbase.dao.AwbPrintChargesOtherMapper;
import com.efreight.afbase.service.AwbPrintChargesOtherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AF 操作管理 运单制单 杂费表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@Service
public class AwbPrintChargesOtherServiceImpl extends ServiceImpl<AwbPrintChargesOtherMapper, AwbPrintChargesOther> implements AwbPrintChargesOtherService {

    @Override
    public void deleteByAwbPrintId(String awbPrintId) {
        LambdaQueryWrapper<AwbPrintChargesOther> wrapper = Wrappers.<AwbPrintChargesOther>lambdaQuery();
        wrapper.eq(AwbPrintChargesOther::getAwbPrintId, awbPrintId).eq(AwbPrintChargesOther::getOrgId, SecurityUtils.getUser().getOrgId());
        baseMapper.delete(wrapper);
    }
}
