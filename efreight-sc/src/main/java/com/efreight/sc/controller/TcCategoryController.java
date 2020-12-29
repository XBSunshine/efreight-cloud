package com.efreight.sc.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.view.TcCategory;
import com.efreight.sc.service.TcCategoryService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tcCategory")
@AllArgsConstructor
@Slf4j
public class TcCategoryController {
	
	private final TcCategoryService tcCategoryService;
	
	 /**
     * 通过类型查询参数
     * @param categoryName
     * @return
     */
    @GetMapping("/{categoryName}")
    public MessageInfo getListByCategoryName(@PathVariable("categoryName") String categoryName){
        try {
            List<TcCategory> list = tcCategoryService.getListByCategoryName(categoryName);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}
