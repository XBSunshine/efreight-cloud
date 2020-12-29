package com.efreight.afbase.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.view.SendProductEmail;
import com.efreight.afbase.service.AfProductService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * af 产品相关控制器
 * @author cwd
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/afProduct")
@Slf4j
public class AfProductController {
	private final AfProductService afProductService;
	
	/*
	 * 发送产品邮件
	 */
	@PostMapping(value="sendProductEmail")
	public MessageInfo sendProductEmail(@Valid @RequestBody SendProductEmail sendProductEmail) {
		try {
			boolean flag = afProductService.sendProductEmail(sendProductEmail);
			if(flag) {
				return MessageInfo.ok();
			}else {
				return MessageInfo.failed("发送失败");
			}
		} catch (Exception e) {
		   log.info(e.getMessage());
           return MessageInfo.failed(e.getMessage());
		}
	}

}
