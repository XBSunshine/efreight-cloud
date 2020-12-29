package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CustomsDeclaration;
import com.efreight.afbase.service.CustomsDeclarationService;
import com.efreight.common.core.annotation.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * AF 报关单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-07
 */
@RestController
@RequestMapping("/customsDeclaration")
@AllArgsConstructor
@Slf4j
@ResponseResult
public class CustomsDeclarationController {

    private final CustomsDeclarationService customsDeclarationService;

    /**
     * 分页查询
     *
     * @param page
     * @param customsDeclaration
     * @return
     */
    @GetMapping
    public IPage page(Page page, CustomsDeclaration customsDeclaration) {
        return customsDeclarationService.getPage(page, customsDeclaration);
    }

    /**
     * 总计
     *
     * @param customsDeclaration
     * @return
     */
    @GetMapping("/total")
    public CustomsDeclaration total(CustomsDeclaration customsDeclaration) {
        return customsDeclarationService.total(customsDeclaration);
    }

    /**
     * 查看详情
     *
     * @param customsDeclarationId
     * @return
     */
    @GetMapping("/view/{customsDeclarationId}")
    public CustomsDeclaration view(@PathVariable("customsDeclarationId") Integer customsDeclarationId) {
        return customsDeclarationService.view(customsDeclarationId);
    }

    /**
     * 新增
     *
     * @param customsDeclaration
     * @return
     */
    @PostMapping
    public Integer save(@RequestBody CustomsDeclaration customsDeclaration) {
        return customsDeclarationService.insert(customsDeclaration);
    }

    /**
     * 编辑
     *
     * @param customsDeclaration
     * @return
     */
    @PutMapping
    public void modify(@RequestBody CustomsDeclaration customsDeclaration) {
        customsDeclarationService.modify(customsDeclaration);
    }

    /**
     * 删除
     *
     * @param customsDeclarationId
     * @return
     */
    @DeleteMapping("/{customsDeclarationId}")
    public void delete(@PathVariable("customsDeclarationId") Integer customsDeclarationId) {
        customsDeclarationService.delete(customsDeclarationId);
    }

    /**
     * 导出
     *
     * @param customsDeclarationId
     * @return
     */
    @PostMapping("/exportExcel/{customsDeclarationId}")
    public void exportExcel(@PathVariable("customsDeclarationId") Integer customsDeclarationId) {
        customsDeclarationService.exportExcel(customsDeclarationId);
    }
}

