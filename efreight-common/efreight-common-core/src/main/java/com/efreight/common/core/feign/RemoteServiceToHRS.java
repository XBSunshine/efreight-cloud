package com.efreight.common.core.feign;

import com.efreight.common.remoteVo.OrgTemplateConfig;
import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.constant.ServiceNameConstants;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "remoteServiceToHRS", value = ServiceNameConstants.HRS_SERVICE)
public interface RemoteServiceToHRS {

    @GetMapping("/org/{id}")
    MessageInfo<OrgVo> getByOrgId(@PathVariable("id") Integer orgId);

    @GetMapping("/org/shippingBillConfig/{orgId}")
    MessageInfo<OrgInterfaceVo> getShippingBillConfig(@PathVariable("orgId") Integer orgId, @RequestParam("apiType") String apiType);

    /**
     * 通过签约公司Id获取签约公司模板
     *
     * @param orgId
     * @return
     */
    @GetMapping("/orgTemplateConfig/{orgId}")
    MessageInfo<OrgTemplateConfig> getOrgTemlateByOrgId(@PathVariable("orgId") Integer orgId);

    /**
     * 企业附加服务剩余量
     * @param orgId
     * @return
     */
    @GetMapping("/orgServiceMealConfig/{orgId}/{serviceType}")
    MessageInfo<OrgServiceMealConfigVo> getAdditionalServicesRemaining(@PathVariable("orgId") Integer orgId, @PathVariable("serviceType") String serviceType) ;

    /**
     * 添加hrs日志
     * @param logVo
     * @param from
     * @return
     */
    @PostMapping("/log/record")
    MessageInfo recordLog(@RequestBody LogVo logVo, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 根据手机号查询用户信息
     * @param phone
     * @return
     */
    @GetMapping("/user/phone/{phone}")
    MessageInfo<UserBaseVO> findUserByPhone(@PathVariable("phone") String phone,  @RequestParam("internationalCountryCode")String internationalCountryCode,
                                            @RequestHeader(SecurityConstants.FROM) String from);
}
