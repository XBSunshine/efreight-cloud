package com.efreight.hrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.OrgOrderConfig;

import java.util.List;

/**
 * @author lc
 * @date 2020/7/28 16:35
 */
public  interface OrgOrderConfigService extends IService<OrgOrderConfig> {
    /**
     * 修改或添加企业订单配置信息
     * @param orgOrderConfigs 配置信息
     */
    void saveOrUpdate(List<OrgOrderConfig> orgOrderConfigs);

    /**
     * 根据企业ID和业务范畴来修改数据
     * @param orgOrderConfig 数据体
     * @return 影响行数
     */
    int updateByOrgIdAndBusinessCode(OrgOrderConfig orgOrderConfig);

    /**
     * 根据企业ID查询其相关的订单配置信息
     * @param orgId 企业ID
     * @return
     */
    List<OrgOrderConfig> findByOrgId(Integer orgId);

    OrgOrderConfig getOrgOrderConfig(String businessScope);
}
