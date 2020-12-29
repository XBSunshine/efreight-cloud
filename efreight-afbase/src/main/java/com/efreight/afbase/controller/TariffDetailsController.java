package com.efreight.afbase.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.TariffDetails;
import com.efreight.afbase.entity.TariffDetailsCIQ;
import com.efreight.afbase.service.TariffDetailsService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * AF 关税税则 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2020-05-20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tariff")
@Slf4j
public class TariffDetailsController {

    private final TariffDetailsService service;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, TariffDetails bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

    @GetMapping("/getCIQ")
    public MessageInfo getCIQ(TariffDetailsCIQ bean) {
        try {
            List<TariffDetailsCIQ> list = service.getCIQ(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询10条列表
     *
     * @param productName
     * @return
     */
    @GetMapping("/{productName}")
    public MessageInfo list(@PathVariable("productName") String productName) {
        try {
            List<TariffDetails> list = service.getList(productName);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过productCode查询单个税则
     * @param productCode
     * @return
     */
    @GetMapping("/view/{productCode}")
    public MessageInfo view(@PathVariable("productCode") String productCode){
        try {
            TariffDetails tariffDetails = service.view(productCode);
            return MessageInfo.ok(tariffDetails);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

