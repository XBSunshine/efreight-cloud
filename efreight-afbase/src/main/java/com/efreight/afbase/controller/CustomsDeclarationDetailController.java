package com.efreight.afbase.controller;


import com.efreight.afbase.entity.CustomsDeclarationDetail;
import com.efreight.afbase.service.CustomsDeclarationDetailService;
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
 * AF 报关单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-07
 */
@RestController
@RequestMapping("/customsDeclarationDetail")
@AllArgsConstructor
@Slf4j
public class CustomsDeclarationDetailController {

    private final CustomsDeclarationDetailService customsDeclarationDetailService;

    /**
     * 通过报关单Id查询报关单明细
     * @param customsDeclarationId
     * @return
     */
    @GetMapping("/{customsDeclarationId}")
    public MessageInfo list(@PathVariable("customsDeclarationId") Integer customsDeclarationId) {
        try {
            List<CustomsDeclarationDetail> list = customsDeclarationDetailService.listByCustomsDeclarationId(customsDeclarationId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

