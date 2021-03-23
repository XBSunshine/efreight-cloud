package com.efreight.afbase.controller;


import com.efreight.afbase.entity.CssCostFiles;
import com.efreight.afbase.service.CssCostFilesService;
import com.efreight.common.core.annotation.ResponseResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * CSS 成本对账：附件表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2021-03-10
 */
@RestController
@RequestMapping("/cssCostFiles")
@ResponseResult
@AllArgsConstructor
public class CssCostFilesController {

    private final CssCostFilesService cssCostFilesService;

    /**
     * 列表查询
     *
     * @param flag
     * @param id
     * @return
     */
    @GetMapping("/{flag}/{id}")
    public List<CssCostFiles> list(@PathVariable("flag") String flag, @PathVariable("id") Integer id) {
        return cssCostFilesService.getList(flag, id);
    }

    /**
     * 保存上传文件-single
     *
     * @param cssCostFiles
     */
    @PostMapping
    public void save(@RequestBody CssCostFiles cssCostFiles) {
        cssCostFilesService.insert(cssCostFiles);
    }

    /**
     * 保存上传文件-batch
     *
     * @param list
     */
    @PostMapping("/batch")
    public void save(@RequestBody List<CssCostFiles> list) {
        cssCostFilesService.insertBatch(list);
    }

    /**
     * 删除上传文件
     *
     * @param fileId
     */
    @DeleteMapping("/{fileId}")
    public void delete(@PathVariable("fileId") Integer fileId) {
        cssCostFilesService.delete(fileId);
    }

}

