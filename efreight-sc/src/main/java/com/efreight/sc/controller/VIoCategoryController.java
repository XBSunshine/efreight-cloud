package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.VIoCategory;
import com.efreight.sc.service.VIoCategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * VIEW 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
@RestController
@RequestMapping("/vIoCategory")
@AllArgsConstructor
@Slf4j
public class VIoCategoryController {

    private final VIoCategoryService vIoCategoryService;

    /**
     * lc参数列表查询
     * @param categoryName
     * @return
     */
    @GetMapping("/{categoryName}")
    private MessageInfo list(@PathVariable("categoryName") String categoryName) {
        try {
            List<VIoCategory> list = vIoCategoryService.getList(categoryName);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

