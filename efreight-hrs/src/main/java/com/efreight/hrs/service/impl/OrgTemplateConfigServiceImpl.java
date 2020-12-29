package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.hrs.entity.OrgTemplateConfig;
import com.efreight.hrs.dao.OrgTemplateConfigMapper;
import com.efreight.hrs.service.OrgTemplateConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * HRS 签约公司表：模板配置表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-14
 */
@Service
public class OrgTemplateConfigServiceImpl extends ServiceImpl<OrgTemplateConfigMapper, OrgTemplateConfig> implements OrgTemplateConfigService {

    @Override
    public OrgTemplateConfig getOrgTemlateByOrgId(Integer orgId) {
        LambdaQueryWrapper<OrgTemplateConfig> wrapper = Wrappers.<OrgTemplateConfig>lambdaQuery();
        wrapper.eq(OrgTemplateConfig::getOrgId, orgId);
        return getOne(wrapper);
    }
}
