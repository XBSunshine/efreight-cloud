package com.efreight.hrs.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.TomPublicNotice;
import com.efreight.hrs.service.TomPublicNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.efreight.hrs.service.RolePermissionService;
import com.efreight.hrs.service.RoleService;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
@RestController
@AllArgsConstructor
@RequestMapping("/notice")
@Slf4j
public class TomPublicNoticeController {

    private final TomPublicNoticeService tomPublicNoticeService;

    /**
     * 分页列表
     *
     * @param page
     * @param tomPublicNotice
     * @return
     */
    @GetMapping("/page")
    public MessageInfo getPage(Page<TomPublicNotice> page, TomPublicNotice tomPublicNotice) {
        try {
            return MessageInfo.ok(tomPublicNoticeService.getTomPublicNoticeListPage(page, tomPublicNotice));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param noticeId
     * @return
     */
    @GetMapping("/{noticeId}")
    public MessageInfo getOne(@PathVariable("noticeId") Integer noticeId) {
        try {
            return MessageInfo.ok(tomPublicNoticeService.getTomPublicNotice(noticeId));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新建
     *
     * @param tomPublicNotice
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_notice_add')")
    public MessageInfo save(@RequestBody TomPublicNotice tomPublicNotice) {
        try {
            tomPublicNoticeService.saveTomPublicNotice(tomPublicNotice);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param tomPublicNotice
     * @return
     */
    @PutMapping
    public MessageInfo update(@RequestBody TomPublicNotice tomPublicNotice) {
        try {
            tomPublicNoticeService.updateNoticeById(tomPublicNotice);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param noticeId
     * @return
     */
    @DeleteMapping("/{noticeId}")
    public MessageInfo delete(@PathVariable("noticeId") String noticeId) {
        try {
            tomPublicNoticeService.removeNoticeById(noticeId);
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
    @GetMapping
    public MessageInfo getListForHomePage() {
        try {
            List<TomPublicNotice> list = tomPublicNoticeService.getListForHomePage();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

