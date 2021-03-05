package com.efreight.hrs.controller;


import com.efreight.common.core.annotation.ResponseResult;
import com.efreight.hrs.entity.OrgBankConfig;
import com.efreight.hrs.service.OrgBankConfigService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * HRS 签约公司 服务套餐设置 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-29
 */
@RestController
@RequestMapping("/orgBankConfig")
@ResponseResult
@AllArgsConstructor
public class OrgBankConfigController {

    private final OrgBankConfigService orgBankConfigService;

    @GetMapping
    public List<OrgBankConfig> queryCurrOrgId(){
        return orgBankConfigService.queryCurrOrgId();
    }
}

