package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.VlOrderFiles;
import com.efreight.sc.service.VlOrderFilesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * TC 订单管理 订单附件 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@RestController
@RequestMapping("/vlOrderFiles")
@Slf4j
@AllArgsConstructor
public class VlOrderFilesController {
    private  final VlOrderFilesService vlOrderFilesService;

    /**
     * 通过orderId查询订单附件列表
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<VlOrderFiles>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<VlOrderFiles> result = vlOrderFilesService.getList(orderId);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 附件保存
     *
     * @param vlOrderFiles
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody VlOrderFiles vlOrderFiles) {
        try {
            vlOrderFilesService.insert(vlOrderFiles);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 设置是否显示
     *
     * @param vlOrderFiles
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody VlOrderFiles vlOrderFiles) {
        try {
            vlOrderFilesService.modifty(vlOrderFiles);
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
            vlOrderFilesService.delete(orderFileId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

