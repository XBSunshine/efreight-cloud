package com.efreight.hrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.OrgBankConfig;

import java.util.List;

/**
 * @author lc
 * @date 2020/12/18 16:25
 */
public interface OrgBankConfigService extends IService<OrgBankConfig> {
    /**
     * 根据企业ID查询银行信息
     * @param orgId 企业ID
     * @return
     */
    List<OrgBankConfig> findByOrgId(Integer orgId);

    /**
     * 删除并保存数据
     * @param orgBankConfigs
     */
    void deleteAndSave(List<OrgBankConfig> orgBankConfigs);

    List<OrgBankConfig> queryCurrOrgId();
}
