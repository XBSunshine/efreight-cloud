package com.efreight.afbase.controller;


import com.efreight.afbase.entity.view.VCurrencyRate;
import com.efreight.afbase.service.CurrencyService;
import com.efreight.common.security.util.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.service.CategoryService;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * <p>
 * 币种 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    /**
     * 获取当前org的币种
     * @return
     */
    @GetMapping
    public MessageInfo getCurrentListByOrgId(){
        try {
            List<VCurrencyRate> result = currencyService.getCurrentListByOrgId();
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

