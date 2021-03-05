package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.OrgBankConfigMapper;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.OrgBankConfig;
import com.efreight.hrs.service.OrgBankConfigService;
import com.efreight.hrs.service.OrgService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lc
 * @date 2020/12/18 16:25
 */
@Service
@AllArgsConstructor
public class OrgBankConfigServiceImpl extends ServiceImpl<OrgBankConfigMapper, OrgBankConfig> implements OrgBankConfigService {

    private final OrgService orgService;

    @Override
    public List<OrgBankConfig> findByOrgId(Integer orgId) {
        Assert.notNull(orgId, "企业ID不能为空");
        LambdaQueryWrapper<OrgBankConfig> query = Wrappers.lambdaQuery();
        query.eq(OrgBankConfig::getOrgId, orgId);
        return this.baseMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAndSave(List<OrgBankConfig> orgBankConfigs) {
        Integer orgId = SecurityUtils.getUser().getOrgId();

        LambdaUpdateWrapper<OrgBankConfig> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(OrgBankConfig::getOrgId, orgId);
        this.baseMapper.delete(updateWrapper);

        if (orgBankConfigs.size() > 0) {
            List<OrgBankConfig> result = orgBankConfigs.stream().filter((item) -> StringUtils.isNotBlank(item.getTitleCn())).collect(Collectors.toList());
            result.stream().forEach((item) -> item.setOrgId(orgId));
            saveBatch(result);
        }
    }

    @Override
    public List<OrgBankConfig> queryCurrOrgId() {
        List<OrgBankConfig> list = this.findByOrgId(SecurityUtils.getUser().getOrgId());
        Org org = orgService.getById(SecurityUtils.getUser().getOrgId());
        if (org != null) {
            OrgBankConfig orgBankConfig = new OrgBankConfig();
            orgBankConfig.setOrgBankConfigId(-1);
            orgBankConfig.setTitleCn(org.getOrgName());
            list.add(0, orgBankConfig);
        }
        return list;
    }
}
