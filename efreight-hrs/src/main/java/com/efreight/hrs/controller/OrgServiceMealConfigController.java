package com.efreight.hrs.controller;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.OrgServiceMealConfig;
import com.efreight.hrs.service.OrgServiceMealConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 附加服务
 * @author lc
 * @date 2020/11/17 11:34
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/orgServiceMealConfig")
public class OrgServiceMealConfigController {

    private final OrgServiceMealConfigService orgServiceMealConfigService;

    /**
     * 查询企业附加服务配置信息
     * @return
     */
    @GetMapping("list/{orgId}")
    public MessageInfo list(@PathVariable("orgId") Integer orgId){
        List<OrgServiceMealConfig> result = orgServiceMealConfigService.listByOrgId(orgId);
        return MessageInfo.ok(result);
    }

    /**
     * 处理附加服务(添加，修改，删除)
     * @param mealConfigs
     * @return
     */
    @PostMapping("process")
    public MessageInfo process(@RequestBody List<OrgServiceMealConfig> mealConfigs){
        orgServiceMealConfigService.process(mealConfigs);
        return MessageInfo.ok();
    }

    /**
     * 企业附加服务剩余量(内部)
     * @param orgId
     * @return
     */
    @GetMapping("remaining/{orgId}/{serviceType}")
    public MessageInfo<OrgServiceMealConfig> remaining(@PathVariable("orgId")Integer orgId, @PathVariable("serviceType")String serviceType){
        OrgServiceMealConfig orgServiceMealConfig = orgServiceMealConfigService.additionalService(orgId, serviceType);
        return MessageInfo.ok(orgServiceMealConfig);
    }


}
