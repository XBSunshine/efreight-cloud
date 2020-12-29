package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AwbSubscription;
import com.efreight.afbase.entity.view.Subscribe;
import com.efreight.afbase.entity.view.SubscribeVO;
import com.efreight.afbase.service.AwbSubscriptionService;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.WebUtils;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * AF 运单号 我的订阅 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-06-29
 */
@RestController
@RequestMapping("/awbSubscription")
@AllArgsConstructor
@Slf4j
public class AwbSubscriptionController {

    private final AwbSubscriptionService awbSubscriptionService;

    /**
     * 获取列表信息
     */
    @GetMapping("{businessScope}")
    public MessageInfo list(@PathVariable("businessScope") String businessScope) {
        try {
            List<AwbSubscription> list = awbSubscriptionService.getList(businessScope);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存订阅
     * @param subscribe
     * @return
     */
    @PostMapping("cargoTrackingSubscribe")
    public MessageInfo cargoTrackingSubscribe(@RequestBody Subscribe subscribe) {
        try {
            EUserDetails loginUser = SecurityUtils.getUser();

            subscribe.setOrgId(loginUser.getOrgId());
            subscribe.setUserId(loginUser.getId());
            subscribe.setCreateIp(WebUtils.getIP());

            boolean isFirst = awbSubscriptionService.cargoTrackingSubscribe(subscribe);
            return MessageInfo.ok(isFirst);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 检查主单是否在已开始订阅轨迹信息
     * @param awbNumber
     * @return
     */
    @GetMapping("/route/{awbNumber}")
    public MessageInfo getRoute(@PathVariable("awbNumber") String awbNumber) {
        try {
            Boolean ifExist = awbSubscriptionService.getRoute(awbNumber);
            return MessageInfo.ok(ifExist);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除历史订阅
     * @param awbSubscriptionId
     * @return
     */
    @DeleteMapping("/{awbSubscriptionId}")
    public MessageInfo deleteAwbSubscription(@PathVariable("awbSubscriptionId") Integer awbSubscriptionId) {
        try {
            awbSubscriptionService.deleteAwbSubscription(awbSubscriptionId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 订阅明细
     * @param page
     * @param date
     * @return
     */
    @GetMapping("/pageSubscribe")
    public MessageInfo  pageSubscribe(Page page, @RequestParam("date") String date){
        Integer orgId = SecurityUtils.getUser().getOrgId();
        IPage<SubscribeVO> subscribeVOIPage = awbSubscriptionService.pageSubscribe(page, orgId, date);
        return MessageInfo.ok(subscribeVOIPage);
    }

    /**
     * 导出订阅明细
     * @param date
     * @return
     */
    @PostMapping("/exportSubscribe")
    public void  exportSubscribe(@RequestParam("date") String date, HttpServletResponse response){
        Integer orgId = SecurityUtils.getUser().getOrgId();
        List<SubscribeVO> subscribeVOList = awbSubscriptionService.exportSubscribe(orgId, date);
        String[] headers = new String[] {"月份", "业务类型", "主运单号", "分运单号", "操作人", "操作时间", "IP地址"};
        ExportExcel<SubscribeVO> u = new ExportExcel<>();
        u.exportExcel(response, "导出EXCEL", headers, subscribeVOList, "Export");
    }
}

