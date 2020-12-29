package com.efreight.afbase.controller;


import com.efreight.afbase.entity.PrmCategoryEciq;
import com.efreight.afbase.service.PrmCategoryEciqService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-10
 */
@RestController
@RequestMapping("/prmCategoryEciq")
@AllArgsConstructor
@Slf4j
public class PrmCategoryEciqController {

    private final PrmCategoryEciqService prmCategoryEciqService;

    /**
     * 查询境内区域
     * @return
     */
    @GetMapping
    public MessageInfo list() {
        try {
            List<PrmCategoryEciq> list = prmCategoryEciqService.list();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

