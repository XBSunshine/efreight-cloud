package com.efreight.hrs.service;

import com.efreight.hrs.entity.OrgTemplateConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * HRS 签约公司表：模板配置表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-14
 */
public interface OrgTemplateConfigService extends IService<OrgTemplateConfig> {

    OrgTemplateConfig getOrgTemlateByOrgId(Integer orgId);
}
