package com.efreight.afbase.controller;


import com.efreight.afbase.entity.TariffUnit;
import com.efreight.afbase.service.TariffUnitService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * AF 关税税则：单位 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-09
 */
@RestController
@RequestMapping("/tariffUnit")
@AllArgsConstructor
@Slf4j
public class TariffUnitController {

    private final TariffUnitService tariffUnitService;

    /**
     * 列表查询
     *
     * @return
     */
    @GetMapping
    public MessageInfo list() {
        try {
            List<TariffUnit> list = tariffUnitService.list();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

