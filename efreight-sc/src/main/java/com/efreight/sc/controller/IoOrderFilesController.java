package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.IoOrderFiles;
import com.efreight.sc.service.IoOrderFilesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * IO 订单管理 其他业务订单附件 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@RestController
@RequestMapping("/ioOrderFiles")
@AllArgsConstructor
@Slf4j
public class IoOrderFilesController {

    private final IoOrderFilesService ioOrderFilesService;

    /**
     * 通过orderId查询订单附件列表
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<IoOrderFiles>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<IoOrderFiles> result = ioOrderFilesService.getList(orderId);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 附件保存
     *
     * @param ioOrderFiles
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody IoOrderFiles ioOrderFiles) {
        try {
            ioOrderFilesService.insert(ioOrderFiles);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 设置是否显示
     *
     * @param ioOrderFiles
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody IoOrderFiles ioOrderFiles) {
        try {
            ioOrderFilesService.modifty(ioOrderFiles);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除附件
     *
     * @param orderFileId
     * @return
     */
    @DeleteMapping("/{orderFileId}")
    public MessageInfo delete(@PathVariable("orderFileId") Integer orderFileId) {
        try {
            ioOrderFilesService.delete(orderFileId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过orderFileIds获取附件列表
     * @param orderFileIds
     * @return
     */
    @GetMapping("/getListByOrderFileIds/{orderFileIds}")
    public MessageInfo getListByOrderFileIds(@PathVariable("orderFileIds") String orderFileIds) {
        try {
            List<IoOrderFiles> list = ioOrderFilesService.getListByOrderFileIds(orderFileIds);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

