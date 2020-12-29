/*
 *
 * Author:zhanghw
 */

package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Currency;
import com.efreight.afbase.entity.CurrencyRate;
import com.efreight.afbase.service.CurrencyRateService;
import com.efreight.common.security.util.MessageInfo;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 汇率维护
 *
 * @author zhanghw
 * @date 2019-08-30 17:42:34
 */
@RestController
@AllArgsConstructor
@RequestMapping("/currencyrate")
@Api(value = "currencyrate", tags = "currencyrate管理")
@Slf4j
public class CurrencyRateController {

    private final CurrencyRateService currencyRateService;

    /**
     * 分页查询
     *
     * @param page         分页对象
     * @param currencyRate 汇率维护
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, CurrencyRate currencyRate) {
        try {
            return MessageInfo.ok(currencyRateService.getPage(page, currencyRate));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    /**
     * 通过id查询汇率维护
     *
     * @param crId id
     * @return MessageInfo
     */
    @GetMapping("/{crId}")
    public MessageInfo getById(@PathVariable("crId") Integer crId) {
        return MessageInfo.ok(currencyRateService.getById(crId));
    }

    /**
     * 通过code查询最后一次汇率维护
     *
     * @param
     * @return MessageInfo
     */
    @GetMapping("/currencyCode/{currencyCode}")
    public MessageInfo getByCurrencyCode(@PathVariable("currencyCode") String currencyCode) {
        try {
            CurrencyRate currencyRate = currencyRateService.lastOperation(currencyCode);
            return MessageInfo.ok(currencyRate);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增汇率维护
     *
     * @param currencyRate 汇率维护
     * @return R
     */
    // @SysLog("新增汇率维护" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_base_currencyrate_add')" )
    public MessageInfo save(@RequestBody CurrencyRate currencyRate) {
        try {
            currencyRateService.insert(currencyRate);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过code查询币种是否存在
     *
     * @param
     * @return MessageInfo
     */
    @GetMapping("/getCurrencyByCode/{currencyCode}")
    public MessageInfo getCurrencyByCode(@PathVariable("currencyCode") String currencyCode) {
        try {
            Currency currency = currencyRateService.getCurrencyByCode(currencyCode);
            return MessageInfo.ok(currency);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/remoteSave")
    public MessageInfo remoteCallSave(@RequestBody CurrencyRate currencyRate) {
        try{
            currencyRateService.remoteInsert(currencyRate);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


}
