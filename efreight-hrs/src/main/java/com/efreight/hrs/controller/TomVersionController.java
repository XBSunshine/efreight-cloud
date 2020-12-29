package com.efreight.hrs.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.TomVersion;
import com.efreight.hrs.entity.TomVersionRecord;
import com.efreight.hrs.service.TomVersionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
@AllArgsConstructor
@RestController
@RequestMapping("/version")
@Slf4j
public class TomVersionController {

    private final TomVersionService tomVersionService;

    /**
     * 分页查询版本主表
     *
     * @param page
     * @param tomVersion
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, TomVersion tomVersion) {
        try {
            IPage<TomVersion> result = tomVersionService.queryListPage(page, tomVersion);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分页查询子记录表
     *
     * @param page
     * @param tomVersionRecord
     * @return
     */
    @GetMapping("/children")
    /*@PreAuthorize("@pms.hasPermission('sys_version_record')")*/
    public MessageInfo pageChildren(Page page, TomVersionRecord tomVersionRecord) {
        try {
            IPage<TomVersionRecord> result = tomVersionService.queryChildrenListPage(page, tomVersionRecord);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询单个版本主表
     *
     * @param versionId
     * @return
     */
    @GetMapping("/{versionId}")
    public MessageInfo view(@PathVariable("versionId") Integer versionId) {
        try {
            TomVersion tomVersion = tomVersionService.queryVersion(versionId);
            return MessageInfo.ok(tomVersion);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询单个记录表
     *
     * @param recordId
     * @return
     */
    @GetMapping("/child/{recordId}")
    public MessageInfo viewChild(@PathVariable("recordId") Integer recordId) {
        try {
            TomVersionRecord tomVersionRecord = tomVersionService.queryRecord(recordId);
            return MessageInfo.ok(tomVersionRecord);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 版本主表新建
     *
     * @param tomVersion
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_version_add')")
    public MessageInfo save(@RequestBody TomVersion tomVersion) {
        try {
            tomVersionService.saveVersion(tomVersion);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 子记录表新建
     *
     * @param tomVersionRecord
     * @return
     */
    @PostMapping("/child")
    public MessageInfo save(@RequestBody TomVersionRecord tomVersionRecord) {
        try {
            tomVersionService.saveRecord(tomVersionRecord);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 版本主表修改
     *
     * @param tomVersion
     * @return
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('sys_version_edit')")
    public MessageInfo update(@RequestBody TomVersion tomVersion) {
        try {
            tomVersionService.updateVersion(tomVersion);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 子记录表修改
     *
     * @param tomVersionRecord
     * @return
     */
    @PutMapping("/child")
    public MessageInfo update(@RequestBody TomVersionRecord tomVersionRecord) {
        try {
            tomVersionService.updateRecord(tomVersionRecord);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 版本主表删除
     *
     * @param versionId
     * @return
     */
    @DeleteMapping("/{versionId}")
    @PreAuthorize("@pms.hasPermission('sys_version_del')")
    public MessageInfo delete(@PathVariable("versionId") Integer versionId) {
        try {
            tomVersionService.deleteVersion(versionId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 子记录表删除
     *
     * @param recordId
     * @return
     */
    @DeleteMapping("/child/{recordId}")
    public MessageInfo deleteChild(@PathVariable("recordId") Integer recordId) {
        try {
            tomVersionService.deleteRecord(recordId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 首页展示
     * @return
     */
    @GetMapping("/list")
    public MessageInfo getListForHomePage() {
        try {
            List<TomVersion> list = tomVersionService.getListForHomePage();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 首页展示
     * @return
     */
    @GetMapping("/recordlist/{versionId}")
    public MessageInfo getRecordListByVersionId(@PathVariable Integer versionId) {
        try {
            List<TomVersionRecord> list = tomVersionService.getRecordListByVersionId(versionId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

