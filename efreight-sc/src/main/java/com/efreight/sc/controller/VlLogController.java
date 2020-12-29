package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.VlLog;
import com.efreight.sc.service.VlLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * VL 派車單操作日志 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@RestController
@RequestMapping("/vlLog")
@AllArgsConstructor
@Slf4j
public class VlLogController {

    private final VlLogService vlLogService;
    /**
     * 日志分页查询
     *
     * @param page
     * @param vlLog
     * @return
     */
    @GetMapping
    public MessageInfo<IPage> page(Page page, VlLog vlLog) {
        try {
            IPage result = vlLogService.getPage(page, vlLog);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

