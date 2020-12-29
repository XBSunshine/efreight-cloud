package com.efreight.afbase.controller;


import com.efreight.afbase.entity.OrderInquiryQuotation;
import com.efreight.afbase.service.OrderInquiryQuotationService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * AF 询价：报价单明细 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-28
 */
@RestController
@RequestMapping("/orderInquiryQuotation")
@AllArgsConstructor
@Slf4j
public class OrderInquiryQuotationController {

    private final OrderInquiryQuotationService orderInquiryQuotationService;


    /**
     * 暂存报价方案
     * @param orderInquiryQuotations
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody List<OrderInquiryQuotation> orderInquiryQuotations){
        try {
            orderInquiryQuotationService.modify(orderInquiryQuotations);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

