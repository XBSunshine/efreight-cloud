package com.efreight.afbase.controller;


import com.efreight.afbase.entity.VCurrencyRate;
import com.efreight.afbase.service.VCurrencyRateService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * VIEW 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-09
 */
@RestController
@RequestMapping("/vCurrencyRate")
@AllArgsConstructor
@Slf4j
public class VCurrencyRateController {

    private final VCurrencyRateService vCurrencyRateService;

    /**
     * 币种视图
     * @return
     */
    @GetMapping
    public MessageInfo list(){
        try {
           List<VCurrencyRate> list = vCurrencyRateService.getList();
           return MessageInfo.ok(list);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 币种视图 根据currencyCode查询
     * @return
     */
    @GetMapping("/{currencyCode}")
    public MessageInfo getVCurrencyRateByCode(@PathVariable("currencyCode") String currencyCode){
        try {
           VCurrencyRate v = vCurrencyRateService.getVCurrencyRateByCode(currencyCode);
           return MessageInfo.ok(v);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

