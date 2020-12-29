package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.IoLog;
import com.efreight.sc.service.IoLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * IO 订单操作日志 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@RestController
@RequestMapping("/ioLog")
@AllArgsConstructor
@Slf4j
public class IoLogController {

    private final IoLogService ioLogService;

    /**
     * 日志保存
     *
     * @param ioLog
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody IoLog ioLog) {
        try {
            ioLogService.insert(ioLog);
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
     * @param ioLog
     * @return
     */
    @GetMapping
    public MessageInfo<IPage> page(Page page, IoLog ioLog) {
        try {
            IPage result = ioLogService.getPage(page, ioLog);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

