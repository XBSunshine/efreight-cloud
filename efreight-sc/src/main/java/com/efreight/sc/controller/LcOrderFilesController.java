package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.LcOrderFiles;
import com.efreight.sc.service.LcOrderFilesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * LC 订单管理 订单附件 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@RestController
@RequestMapping("/lcOrderFiles")
@AllArgsConstructor
@Slf4j
public class LcOrderFilesController {

    private final LcOrderFilesService lcOrderFilesService;

    /**
     * 通过orderId查询订单附件列表
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<LcOrderFiles>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<LcOrderFiles> result = lcOrderFilesService.getList(orderId);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 附件保存
     *
     * @param lcOrderFiles
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody LcOrderFiles lcOrderFiles) {
        try {
            lcOrderFilesService.insert(lcOrderFiles);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 设置是否显示
     *
     * @param lcOrderFiles
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody LcOrderFiles lcOrderFiles) {
        try {
            lcOrderFilesService.modifty(lcOrderFiles);
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
            lcOrderFilesService.delete(orderFileId);
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
            List<LcOrderFiles> list = lcOrderFilesService.getListByOrderFileIds(orderFileIds);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

