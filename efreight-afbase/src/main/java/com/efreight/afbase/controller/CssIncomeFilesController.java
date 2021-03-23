package com.efreight.afbase.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.CssIncomeFiles;
import com.efreight.afbase.service.CssIncomeFilesService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("cssIncomeFiles")
public class CssIncomeFilesController {
	
	private final CssIncomeFilesService cssIncomeFilesService;
	
	/**
     * 附件查询
     */
    @GetMapping("/{businessType}/{ids}")
    public MessageInfo fileList(@PathVariable String businessType,@PathVariable Integer ids) {
        return MessageInfo.ok(cssIncomeFilesService.fileList(businessType,ids));
    }
    
    @PostMapping(value = "/saveOrModify")
    public MessageInfo saveOrModify(@Valid @RequestBody CssIncomeFiles bean) {
        try {
            return MessageInfo.ok(cssIncomeFilesService.saveOrModify(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    @PostMapping(value = "/deleteFile")
    public MessageInfo deleteFile(@Valid @RequestBody CssIncomeFiles bean) {
        try {
            return MessageInfo.ok(cssIncomeFilesService.deleteFile(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

}
