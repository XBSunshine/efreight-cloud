package com.efreight.hrs.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.OrgTemplateConfig;
import com.efreight.hrs.service.OrgTemplateConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * HRS 签约公司表：模板配置表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-14
 */
@RestController
@RequestMapping("/orgTemplateConfig")
@AllArgsConstructor
@Slf4j
public class OrgTemplateConfigController {

    private final OrgTemplateConfigService orgTemplateConfigService;

    /**
     * 通过签约公司Id获取签约公司模板
     *
     * @param orgId
     * @return
     */
    @GetMapping("/{orgId}")
    public MessageInfo getOrgTemlateByOrgId(@PathVariable("orgId") Integer orgId) {
        try {
            OrgTemplateConfig orgTemplateConfig = orgTemplateConfigService.getOrgTemlateByOrgId(orgId);
            return MessageInfo.ok(orgTemplateConfig);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

