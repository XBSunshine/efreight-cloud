package com.efreight.sc.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.TcProduct;
import com.efreight.sc.service.TcProductService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tcProduct")
@AllArgsConstructor
@Slf4j
public class TcProductController {
	
	 private final TcProductService tcProductService;
	 
	 
	 @GetMapping("/page")
     public MessageInfo gePageList(Page page, TcProduct bean) {
        try {
            IPage result = tcProductService.gePageList(page, bean);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
     }
	 
	 @PostMapping("/save")
     public MessageInfo saveProduct(@RequestBody TcProduct bean){
        try {
        	tcProductService.saveProduct(bean);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	 
	 @PostMapping("/modify")
     public MessageInfo modifyProduct(@RequestBody TcProduct bean){
        try {
        	tcProductService.modifyProduct(bean);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	 
	 @DeleteMapping("/{productId}")
	 public MessageInfo delete(@PathVariable("productId") Integer productId){
	    try{
	        tcProductService.deleteById(productId);
	        return MessageInfo.ok();
	    }catch (Exception e){
	        e.printStackTrace();
	        return MessageInfo.failed(e.getMessage());
	    }
	}
	@GetMapping("/view/{productId}")
    public MessageInfo view(@PathVariable("productId") Integer productId){
        try {
            TcProduct bean = tcProductService.view(productId);
            return MessageInfo.ok(bean);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}
