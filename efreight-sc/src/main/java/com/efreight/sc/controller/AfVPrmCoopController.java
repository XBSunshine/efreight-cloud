package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.sc.service.AfVPrmCoopService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * VIEW 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-09
 */
@RestController
@RequestMapping("/afVprmCoop")
@Slf4j
@AllArgsConstructor
public class AfVPrmCoopController {
    private final AfVPrmCoopService afVPrmCoopService;

    /**
     * 查询客户（优化版）list版
     *
     * @param afVPrmCoop
     * @return
     */
    @GetMapping("/list")
    public MessageInfo list(AfVPrmCoop afVPrmCoop) {
        try {
            List<AfVPrmCoop> list = afVPrmCoopService.getList(afVPrmCoop);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询客户（优化版）分页版
     *
     * @param afVPrmCoop
     * @return
     */
    @GetMapping
    public MessageInfo getPage(Page page,AfVPrmCoop afVPrmCoop) {
        try {
            IPage result = afVPrmCoopService.getPage(page,afVPrmCoop);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

