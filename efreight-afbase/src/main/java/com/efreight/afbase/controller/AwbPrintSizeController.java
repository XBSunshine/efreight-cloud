package com.efreight.afbase.controller;


import com.efreight.afbase.service.AwbPrintSizeService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * AF 操作管理 运单制单 尺寸表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@RestController
@RequestMapping("/awbPrintSize")
@AllArgsConstructor
public class AwbPrintSizeController {

    private final AwbPrintSizeService awbPrintSizeService;

}

