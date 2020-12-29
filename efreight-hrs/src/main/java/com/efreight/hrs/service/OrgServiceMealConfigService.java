package com.efreight.hrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.OrgServiceMealConfig;

import java.util.List;

/**
 * @author lc
 * @date 2020/11/16 15:07
 */
public interface OrgServiceMealConfigService extends IService<OrgServiceMealConfig> {

    /**
     * 保存/修改/删除，根据实体类的op字段的值来判断操作
     * @param orgServiceMealConfigs
     */
    void process(List<OrgServiceMealConfig> orgServiceMealConfigs);

    /**
     * 根据企业ID 查询数据
     * @param orgId 企业ID
     * @return
     */
    List<OrgServiceMealConfig> listByOrgId(Integer orgId);

    /**
     * 企业某个附加服务的剩余量
     * @param orgId 企业ID
     * @param serviceType 附加服务标识
     * @return
     */
    OrgServiceMealConfig additionalService(Integer orgId, String serviceType);
}
