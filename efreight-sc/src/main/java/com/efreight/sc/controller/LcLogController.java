package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.LcLog;
import com.efreight.sc.service.LcLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * LC 订单操作日志 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@RestController
@RequestMapping("/lcLog")
@AllArgsConstructor
@Slf4j
public class LcLogController {

    private final LcLogService lcLogService;

    /**
     * 日志保存
     *
     * @param lcLog
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody LcLog lcLog) {
        try {
            lcLogService.insert(lcLog);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 日志分页查询
     *
     * @param page
     * @param lcLog
     * @return
     */
    @GetMapping
    public MessageInfo<IPage> page(Page page, LcLog lcLog) {
        try {
            IPage result = lcLogService.getPage(page, lcLog);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

