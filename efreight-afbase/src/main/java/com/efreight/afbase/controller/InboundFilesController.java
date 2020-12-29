package com.efreight.afbase.controller;


import com.efreight.afbase.entity.InboundFiles;
import com.efreight.afbase.service.InboundFilesService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * AF 操作计划 操作出重表 照片文件 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-05
 */
@RestController
@RequestMapping("/inboundFiles")
@AllArgsConstructor
@Slf4j
public class InboundFilesController {

    private final InboundFilesService inboundFilesService;

    /**
     * 新建
     *
     * @param inboundFiles
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody InboundFiles inboundFiles) {
        try {
            Integer fileId = inboundFilesService.saveInboundFile(inboundFiles);
            return MessageInfo.ok(fileId);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileId
     * @return
     */
    @DeleteMapping("/{fileId}")
    public MessageInfo delete(@PathVariable("fileId") Integer fileId) {
        try {
            inboundFilesService.delete(fileId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询列表
     *
     * @param inboundId
     * @return
     */
    @GetMapping
    public MessageInfo list(Integer inboundId) {
        try {
            List<InboundFiles> list = inboundFilesService.getList(inboundId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

