package com.efreight.afbase.controller;



import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfOrderStorageMns;
import com.efreight.afbase.service.AfOrderStorageMnsService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author qipm
 * @since 2020-10-26
 */
@RestController
@AllArgsConstructor
@RequestMapping("/mnsLog")
public class AfOrderStorageMnsController {
	 private final AfOrderStorageMnsService service;
	@PostMapping(value = "/queryList")
    public MessageInfo queryList(AfOrderStorageMns bean) {
        try {
            return MessageInfo.ok(service.queryList(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
}

