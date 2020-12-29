package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.OrgOrderConfigMapper;
import com.efreight.hrs.entity.OrgOrderConfig;
import com.efreight.hrs.service.OrgOrderConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lc
 * @date 2020/7/28 16:38
 */
@Service
@Slf4j
public class OrgOrderConfigServiceImpl extends ServiceImpl<OrgOrderConfigMapper, OrgOrderConfig> implements OrgOrderConfigService {

    @Resource
    private OrgOrderConfigMapper orgOrderConfigMapper;

    @Override
    public void saveOrUpdate(List<OrgOrderConfig> orgOrderConfigs) {
        Integer orgId = SecurityUtils.getUser().getOrgId();
        orgOrderConfigs.stream().forEach((orgOrderConfig) -> {
            orgOrderConfig.setOrgId(orgId);
            int updateCount = updateByOrgIdAndBusinessCode(orgOrderConfig);
            if (updateCount == 0) {
                this.orgOrderConfigMapper.insert(orgOrderConfig);
            }
        });
    }

    @Override
    public int updateByOrgIdAndBusinessCode(OrgOrderConfig orgOrderConfig) {
        Assert.notNull(orgOrderConfig.getOrgId(), "非法参数:企业ID为空");
        Assert.hasLength(orgOrderConfig.getBusinessScope(), "非法参数：业务范畴为空");
        return this.orgOrderConfigMapper.updateWithOrgIdAndBusinessScope(orgOrderConfig);
    }

    @Override
    public List<OrgOrderConfig> findByOrgId(Integer orgId) {
        LambdaQueryWrapper<OrgOrderConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrgOrderConfig::getOrgId, orgId);
        return this.orgOrderConfigMapper.selectList(queryWrapper);
    }

    @Override
    public OrgOrderConfig getOrgOrderConfig(String businessScope) {
        LambdaQueryWrapper<OrgOrderConfig> wrapper = Wrappers.<OrgOrderConfig>lambdaQuery();
        wrapper.eq(OrgOrderConfig::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrgOrderConfig::getBusinessScope, businessScope);
        return getOne(wrapper);
    }
}
