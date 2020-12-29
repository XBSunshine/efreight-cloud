package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.OrgServiceMealConfigMapper;
import com.efreight.hrs.entity.OrgServiceMealConfig;
import com.efreight.hrs.service.OrgServiceMealConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lc
 * @date 2020/11/16 15:08
 */
@Service
@Slf4j
public class OrgServiceMealConfigServiceImpl extends ServiceImpl<OrgServiceMealConfigMapper, OrgServiceMealConfig> implements OrgServiceMealConfigService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(List<OrgServiceMealConfig> orgServiceMealConfigs) {
        Assert.notEmpty(orgServiceMealConfigs, "Not found dataset.");
        Map<String, List<OrgServiceMealConfig>> relationMap = orgServiceMealConfigs.stream()
                .filter((item)-> StringUtils.isNotBlank(item.getOp()))
                .collect(Collectors.groupingBy(OrgServiceMealConfig::getOp));

        EUserDetails loginUser = SecurityUtils.getUser();
        Optional.ofNullable(relationMap.get("delete")).ifPresent((list)->{
            Set<Integer> ids = list.stream().map(OrgServiceMealConfig::getOrgServiceMealConfigId).collect(Collectors.toSet());
            removeByIds(ids);
        });

        Optional.ofNullable(relationMap.get("edit")).ifPresent(item -> {
            item.stream().forEach((ele)->{
                //不修改使用量
                ele.setServiceNumberUsed(null);
                ele.setEditorId(loginUser.getId());
                ele.setEditorName(loginUser.buildOptName());
                ele.setEditTime(LocalDateTime.now());
            });
            updateBatchById(item);
        });

        Optional.ofNullable(relationMap.get("add")).ifPresent(item -> {
            item.stream().forEach((ele)->{
                if(null == ele.getOrgId()){
                    throw new IllegalArgumentException("企业ID不能为空");
                }
                ele.setCreatorId(loginUser.getId());
                ele.setCreatorName(loginUser.buildOptName());
                ele.setCreateTime(LocalDateTime.now());
            });
            saveBatch(item);
        });

    }

    @Override
    public List<OrgServiceMealConfig> listByOrgId(Integer orgId) {
        Assert.notNull(orgId, "企业ID不能为空");
        LambdaQueryWrapper<OrgServiceMealConfig> queryChainWrapper = Wrappers.lambdaQuery();
        queryChainWrapper.eq(OrgServiceMealConfig::getOrgId, orgId);
        return this.baseMapper.selectList(queryChainWrapper);
    }

    @Override
    public OrgServiceMealConfig additionalService(Integer orgId, String serviceType) {
        Assert.notNull(orgId, "企业ID不能为空");
        Assert.hasLength(serviceType, "附加服务类型未指定");

        LambdaQueryWrapper<OrgServiceMealConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrgServiceMealConfig::getOrgId, orgId);
        queryWrapper.eq(OrgServiceMealConfig::getServiceType, serviceType);
        OrgServiceMealConfig orgServiceMealConfig = this.baseMapper.selectOne(queryWrapper);

        Assert.notNull(orgServiceMealConfig, "附加服务不存在");
        return orgServiceMealConfig;
    }

}
