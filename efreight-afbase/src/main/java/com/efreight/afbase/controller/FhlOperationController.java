package com.efreight.afbase.controller;



import javax.validation.Valid;

import com.efreight.afbase.entity.AfOrderStorageMns;
import com.efreight.afbase.entity.BranchLine;
import com.efreight.afbase.entity.FhlOperation;
import com.efreight.afbase.entity.OperationLook;
import com.efreight.afbase.service.FhlOperationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.util.MessageInfo;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/fhl")
public class FhlOperationController {

	@Autowired
	private FhlOperationService service;
	
	
	@RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public MessageInfo queryList(Integer currentPage, Integer pageSize,@ModelAttribute("bean") FhlOperation bean) {

        try {
            return MessageInfo.ok(service.queryList(currentPage, pageSize,bean));
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }


	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody FhlOperation bean) {
		try {
			return MessageInfo.ok(service.doSave(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
	@PostMapping(value = "/doDelete")
	public MessageInfo doDelete(@Valid @RequestBody FhlOperation bean) {
		try {
			return MessageInfo.ok(service.doDelete(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
	@PostMapping(value = "/doDeclare")
	public MessageInfo doDeclare(@Valid @RequestBody FhlOperation bean) {
		try {
			return MessageInfo.ok(service.doDeclare(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
	
	////
	@RequestMapping(value = "/queryLineList", method = RequestMethod.POST)
    public MessageInfo queryLineList(Integer currentPage, Integer pageSize,@ModelAttribute("bean") BranchLine bean) {
        try {
            return MessageInfo.ok(service.queryLineList(currentPage, pageSize,bean));
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
	@PostMapping(value = "/doMerge")
	public MessageInfo doMerge(@Valid @RequestBody BranchLine bean) {
		try {
			return MessageInfo.ok(service.doMerge(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
	@PostMapping(value = "/doReset")
	public MessageInfo doReset(@Valid @RequestBody BranchLine bean) {
		try {
			return MessageInfo.ok(service.doReset(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
	@PostMapping(value = "/doSplit")
	public MessageInfo doSplit(@Valid @RequestBody BranchLine bean) {
		try {
			return MessageInfo.ok(service.doSplit(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
	
	@PostMapping(value = "/queryLookList")
    public MessageInfo queryLookList(Integer currentPage, Integer pageSize,@ModelAttribute("bean") OperationLook bean) { 
        try {
            return MessageInfo.ok(service.queryLookList(currentPage, pageSize,bean));
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
	
	@PostMapping(value = "/queryLogList")
    public MessageInfo queryLogList(OperationLook bean) { 
        try {
            return MessageInfo.ok(service.queryLogList(bean));
        } catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
    }
	@PostMapping(value = "/queryStatus")
	public MessageInfo queryStatus(OperationLook bean) { 
		try {
			return MessageInfo.ok(service.queryStatus(bean));
		} catch (CheckedException e){
            log.error("异常："+ e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }catch (Exception e){
            log.error("系统出现异常："+ e.getMessage());
            return MessageInfo.failed("系统出现异常，请联系管理员！");
        }
	}
}

