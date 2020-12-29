package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.VLcCategory;
import com.efreight.sc.service.VLcCategoryService;
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
 * @since 2020-07-22
 */
@RestController
@RequestMapping("/vLcCategory")
@AllArgsConstructor
@Slf4j
public class VLcCategoryController {

    private final VLcCategoryService vLcCategoryService;


    /**
     * lc参数列表查询
     * @param categoryName
     * @return
     */
    @GetMapping("/{categoryName}")
    private MessageInfo list(@PathVariable("categoryName") String categoryName) {
        try {
            List<VLcCategory> list = vLcCategoryService.getList(categoryName);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

