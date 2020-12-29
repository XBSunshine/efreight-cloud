package com.efreight.afbase.controller;


import com.efreight.afbase.entity.view.VAfCategory;
import com.efreight.afbase.service.VAfCategoryService;
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
 * VIEW 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-28
 */
@RestController
@RequestMapping("/vAfCategory")
@AllArgsConstructor
@Slf4j
public class VAfCategoryController {

    private final VAfCategoryService vAfCategoryService;

    /**
     * 查询参数列表
     * @param categoryName
     * @return
     */
    @GetMapping("/{categoryName}")
    public MessageInfo list(@PathVariable("categoryName") String categoryName){
        try {
            List<VAfCategory> list = vAfCategoryService.getList(categoryName);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @GetMapping("sc/{categoryName}")
    public MessageInfo sclist(@PathVariable("categoryName") String categoryName){
    	try {
    		List<VAfCategory> list = vAfCategoryService.getscList(categoryName);
    		return MessageInfo.ok(list);
    	}catch (Exception e){
    		log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }
}

