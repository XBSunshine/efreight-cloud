package com.efreight.afbase.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.AiOperationLookService;
import com.efreight.afbase.service.FhlOperationService;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.util.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URL;


@Slf4j
@RestController
@RequestMapping("/aiOperationLook")
public class AiOperationLookController {

    @Autowired
    private AiOperationLookService service;


    @PostMapping(value = "/queryLookList")
    @ResponseBody
    public MessageInfo queryLookList(Page page, AfOrder bean) {
        try {
            return MessageInfo.ok(service.queryLookList(page,bean));
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }

    @PostMapping(value = "/queryHAWBList")
    @ResponseBody
    public MessageInfo queryHAWBList(String awbNumber) {
        try {
            return MessageInfo.ok(service.queryHAWBList(awbNumber));
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }

    /** 
    * @Description: 分拨运抵申报 
    * @Author: shihongkai
    * @Date: 2021/1/15 
    */ 
    @PostMapping(value = "/distributionDeclare")
    public MessageInfo distributionDeclare(ImportLook importLook) {
        try {
            JSONObject jsonObject = service.distributionDeclare(importLook);
            if("01".equals(jsonObject.get("code"))){
                return MessageInfo.ok(null,jsonObject.getString("messageInfo"));
            }else {
                return MessageInfo.failed(jsonObject.getString("messageInfo"));
            }
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }

    /** 
    * @Description: 原始状态申报 
    * @Author: shihongkai
    * @Date: 2021/1/15 
    */ 
    @PostMapping(value = "/originalStateDeclare")
    public MessageInfo originalStateDeclare(ImportLook importLook) {
        try {
            JSONObject jsonObject = service.originalStateDeclare(importLook);
            if("01".equals(jsonObject.get("code"))){
                return MessageInfo.ok(null,jsonObject.getString("messageInfo"));
            }else {
                return MessageInfo.failed(jsonObject.getString("messageInfo"));
            }
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
    /** 
    * @Description:  理货状态申报
    * @Author: shihongkai
    * @Date: 2021/1/15
    */ 
    @PostMapping(value = "/tallyStateDeclare")
    public MessageInfo tallyStateDeclare(ImportLook importLook) {
        try {
            JSONObject jsonObject = service.tallyStateDeclare(importLook);
            if("01".equals(jsonObject.get("code"))){
                return MessageInfo.ok(null,jsonObject.getString("messageInfo"));
            }else {
                return MessageInfo.failed(jsonObject.getString("messageInfo"));
            }
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
}

