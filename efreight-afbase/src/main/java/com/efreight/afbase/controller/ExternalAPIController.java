package com.efreight.afbase.controller;


import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AwbPrint;
import com.efreight.afbase.entity.OrderInquiry;
import com.efreight.afbase.entity.OrderInquiryQuotation;
import com.efreight.afbase.entity.cargo.track.CargoTrack;
import com.efreight.afbase.entity.cargo.track.CargoTrackQuery;
import com.efreight.afbase.entity.procedure.AfPAwbPrintProcedure;
import com.efreight.afbase.entity.view.OrderDeliveryNotice;
import com.efreight.afbase.entity.view.OrderDeliveryNoticeCheck;
import com.efreight.afbase.entity.view.OrderTrack;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.AwbPrintService;
import com.efreight.afbase.service.OrderInquiryQuotationService;
import com.efreight.afbase.service.OrderInquiryService;
import com.efreight.common.core.utils.SignUtil;
import com.efreight.common.core.utils.WebUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 外部访问服务
 */
@RestController
@AllArgsConstructor
@RequestMapping("/external")
@Slf4j
public class ExternalAPIController {

    private final AfOrderService service;
    private final OrderInquiryService orderInquiryService;
    private final OrderInquiryQuotationService orderInquiryQuotationService;
    private final AwbPrintService awbPrintService;

    @PostMapping("/order/awbSubmit")
    public MessageInfo awbSubmit(@RequestParam("o") String orderUuid) {
        try {
            AfOrder afOrder = service.getOrderByUUID(orderUuid);
            String url = service.awbSubmit(orderUuid, afOrder.getOrgId());
            return MessageInfo.ok(url);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/order/orderTrack")
    public MessageInfo getOrderTrack(@RequestParam("o") String orderUUID) {
        try {
            //前两位为业务域
            if (StringUtils.isNotBlank(orderUUID)) {
                orderUUID = orderUUID.substring(2);
            }
            OrderTrack orderTrack = service.getOrderTrack(orderUUID);
            return MessageInfo.ok(orderTrack);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/order/orderDeliveryNotice")
    public MessageInfo orderDeliveryNotice(@RequestParam("o") String orderUuid,@RequestParam("flag") String flag) {
        try {
            OrderDeliveryNotice orderDeliveryNotice = service.getOrderDeliveryNotice(orderUuid,flag);
            return MessageInfo.ok(orderDeliveryNotice);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/order/cargoTrack")
    public MessageInfo cargoTrack(CargoTrackQuery cargoTrackQuery, HttpServletRequest request){
        try{
            SignUtil.validateSign(request);
            cargoTrackQuery.setIp(WebUtils.getIP(request));
            CargoTrack cargoTrack = service.cargoTrack(cargoTrackQuery);
            return MessageInfo.ok(cargoTrack);
        }catch (Exception e){
            String errorMsg = e.getMessage();
            log.warn("ErrorMsg:{} Ex-AIP-OCT:{}", errorMsg, Base64.encodeBase64String(request.getQueryString().getBytes(StandardCharsets.UTF_8)));

            CargoTrack cargoTrack = new CargoTrack();
            cargoTrack.setTotal(0);
            cargoTrack.setUsed(0);
            cargoTrack.setRouteTracks(Collections.emptyList());
            cargoTrack.setTrackManifest(Collections.emptyList());
            cargoTrack.setManifestList(Collections.emptyList());

            MessageInfo messageInfo;
            if(errorMsg.indexOf("您的空运订阅量为") > -1){
                Pattern pattern = Pattern.compile("(\\d+).*(\\d+)");
                Matcher matcher = pattern.matcher(e.getMessage());
                if(matcher.find()){
                    cargoTrack.setTotal(Integer.valueOf(matcher.group(1)));
                    cargoTrack.setUsed(Integer.valueOf(matcher.group(2)));
                }
                messageInfo = MessageInfo.restResult(cargoTrack, 1002, "套餐量已用完");
            }else if(errorMsg.indexOf("用户不存在") > -1){
                messageInfo = MessageInfo.restResult(cargoTrack, 1000, errorMsg);
            }else{
                messageInfo = MessageInfo.failed(cargoTrack, e.getMessage());
            }
            return messageInfo;
        }
    }

    /**
     * 查看询价单详情
     *
     * @param orderInquiryUuid
     * @return
     */
    @GetMapping("/orderInquiry/{orderInquiryUuid}")
    public MessageInfo view(@PathVariable("orderInquiryUuid") String orderInquiryUuid) {
        try {
            OrderInquiry orderInquiry = orderInquiryService.view(orderInquiryUuid);
            return MessageInfo.ok(orderInquiry);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 报价单新建
     *
     * @param orderInquiryQuotations
     * @return
     */
    @PostMapping("/orderInquiryQuotation")
    public MessageInfo saveInquiryQuotation(@RequestBody List<OrderInquiryQuotation> orderInquiryQuotations) {
        try {
            orderInquiryQuotationService.saveInquiryQuotation(orderInquiryQuotations);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 通过UUID获取AF订单详情
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/viewAFOrderByOrderUuid/{orderUuid}")
    public MessageInfo viewAFOrderByOrderUuid(@PathVariable("orderUuid") String orderUuid) {
        try {
            AfOrder order = service.getOrderByUUID(orderUuid);
            return MessageInfo.ok(order);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 检查订单时候可推送送货通知
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/checkOrderDeliveryNotice/{orderUuid}/{flag}")
    public MessageInfo checkOrderDeliveryNotice(@PathVariable("orderUuid") String orderUuid, @PathVariable("flag") String flag) {
        try {
            OrderDeliveryNoticeCheck result = service.checkOrderDeliveryNotice(orderUuid, flag);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看主单-通过orderUuid
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/viewByOrderUuid/{orderUuid}")
    public MessageInfo viewByOrderUuid(@PathVariable("orderUuid") String orderUuid) {
        try {
            AwbPrint awbPrint = awbPrintService.viewByOrderUuid(orderUuid);
            return MessageInfo.ok(awbPrint);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看分单-通过orderUuid
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/hawbListByOrderUuid/{orderUuid}")
    public MessageInfo hawbListByOrderUuid(@PathVariable("orderUuid") String orderUuid) {
        try {
            List<AwbPrint> list = awbPrintService.hawbListByOrderUuid(orderUuid);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 主单下载
     *
     * @param afPAwbPrintProcedure
     * @return
     */
    @PostMapping("/downloadAWB")
    public MessageInfo awbDownloadWithPDF(@RequestBody AfPAwbPrintProcedure afPAwbPrintProcedure) {
        try {
            return MessageInfo.ok(awbPrintService.awbDownloadWithPDF(afPAwbPrintProcedure));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分单下载
     *
     * @param afPAwbPrintProcedure
     * @param
     * @return
     */
    @PostMapping("/downloadHAWB")
    public MessageInfo hawbDownloadWithPDF(@RequestBody AfPAwbPrintProcedure afPAwbPrintProcedure) {
        try {
            return MessageInfo.ok(awbPrintService.hawbDownloadWithPDF(afPAwbPrintProcedure));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}
