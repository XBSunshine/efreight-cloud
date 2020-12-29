package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfVPrmCategory;
import com.efreight.afbase.entity.Category;
import com.efreight.afbase.entity.CategoryTree;
import com.efreight.afbase.service.CategoryService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 参数表 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 分页查询-OLD
     *
     * @return 列表
     */
    @GetMapping(value = "/page")
    public MessageInfo getPage(Page page, Category category) {
        return MessageInfo.ok(categoryService.getPage(page, category));
    }

    /**
     * 列表查询-NEW
     *
     * @param category
     * @return
     */
    @GetMapping
    public MessageInfo getTree(Category category) {
        try {
            List<CategoryTree> result = categoryService.getTree(category);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据参数类型名称查询参数
     *
     * @param categoryName
     * @return
     */
    @GetMapping("/params")
    public MessageInfo getParams(String categoryName) {
        try {
            List<Category> params = categoryService.getParams(categoryName);
            return MessageInfo.ok(params);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新业务范畴查询
     *
     * @param categoryName
     * @return
     */
    @GetMapping("/paramsNew")
    public MessageInfo getParamsNew(String categoryName) {
        try {
            List<AfVPrmCategory> params = categoryService.getAfVPrmCategory(categoryName);
            return MessageInfo.ok(params);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过参数分类查询列表
     *
     * @param categoryType
     * @return
     */
    @GetMapping("/queryCategoryByCategoryType/{categoryType}")
    public MessageInfo queryCategoryByCategoryType(@PathVariable("categoryType") Integer categoryType) {
        try {
            List<AfVPrmCategory> params = categoryService.queryCategoryByCategoryType(categoryType);
            return MessageInfo.ok(params);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/find")
    public MessageInfo findCategory(String categoryName) {
        try {
            List<Category> params = categoryService.findCategory(categoryName);
            return MessageInfo.ok(params);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

