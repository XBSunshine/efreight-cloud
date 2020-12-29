package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.service.LogService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("log")
@AllArgsConstructor
@Slf4j
public class LogController {

    private final LogService logService;

    /**
     * 日志列表分页查询
     * @param page
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, LogBean logBean){
        try {
            IPage<LogBean> result = logService.getPage(page,logBean);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存日志
     * @param logBean
     * @return
     */
    @PostMapping
    public MessageInfo save(LogBean logBean){
        try {
            logService.saveLog(logBean);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}
