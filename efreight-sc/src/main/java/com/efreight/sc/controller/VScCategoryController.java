package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.view.VScCategory;
import com.efreight.sc.service.VScCategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.handler.MessageContext;
import java.util.List;

/**
 * <p>
 * VIEW 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@RestController
@RequestMapping("/vScCategory")
@AllArgsConstructor
@Slf4j
public class VScCategoryController {

    private final VScCategoryService vScCategoryService;

    /**
     * 通过类型查询参数
     * @param categoryName
     * @return
     */
    @GetMapping("/{categoryName}")
    public MessageInfo getListByCategoryName(@PathVariable("categoryName") String categoryName){
        try {
            List<VScCategory> list = vScCategoryService.getListByCategoryName(categoryName);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

