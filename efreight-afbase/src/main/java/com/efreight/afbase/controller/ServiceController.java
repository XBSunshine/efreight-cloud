package com.efreight.afbase.controller;

import com.efreight.afbase.entity.Service;
import com.efreight.afbase.entity.VPrmCategory;
import com.efreight.afbase.entity.VPrmCategoryTree;
import com.efreight.afbase.service.ServiceService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service")
@Slf4j
@AllArgsConstructor
public class ServiceController {
    private final ServiceService serviceService;

    /**
     * 服务类别列表查询
     *
     * @param businessScope
     * @return
     */
    @GetMapping
    public MessageInfo list(String businessScope) {
        try {
            List<VPrmCategoryTree> list = serviceService.getList(businessScope);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @GetMapping("/treeList")
    public MessageInfo treeList(String businessScope) {
    	try {
    		List<VPrmCategoryTree> list = serviceService.treeList(businessScope);
    		return MessageInfo.ok(list);
    	} catch (Exception e) {
    		log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }
    @GetMapping("/queryList")
    public MessageInfo queryList(String businessScope) {
    	try {
    		List<Service> list = serviceService.queryList(businessScope);
    		return MessageInfo.ok(list);
    	} catch (Exception e) {
    		log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }
    
    @GetMapping("/queryListAE")
    public MessageInfo queryListAE(String businessScope,Integer orderId) {
    	try {
    		return MessageInfo.ok(serviceService.queryListAE(businessScope,orderId));
    	} catch (Exception e) {
    		log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }

    @GetMapping("/queryListForVL/{businessScope}")
    public MessageInfo queryListForVL(@PathVariable("businessScope") String businessScope) {
        try {
            List<Service> list = serviceService.queryList(businessScope);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 起始页面
     * @return
     */
    @GetMapping("/start")
    public MessageInfo startPage(){
        try {
            List<VPrmCategoryTree> list = serviceService.startPage();
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 服务类别新建
     *
     * @param service
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_base_service_add')")
    public MessageInfo save(@RequestBody Service service) {
        try {
            serviceService.save(service);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 服务类别编辑
     *
     * @param service
     * @return
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('sys_base_service_edit')")
    public MessageInfo edit(@RequestBody Service service) {
        try {
            serviceService.edit(service);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param serviceId
     * @return
     */
    @GetMapping("/detail")
    public MessageInfo get(Integer serviceId) {
        try {
            Service service = serviceService.getById(serviceId);
            return MessageInfo.ok(service);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 业务范畴查询
     * @return
     */
    @GetMapping("businessScope")
    public MessageInfo businessScope(){
        try{
            List<VPrmCategory> categoryList = serviceService.businessScope();
            return MessageInfo.ok(categoryList);
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据数据ID删除服务
     * @param serviceId
     * @return
     */
    @DeleteMapping("delete/{id}")
    public MessageInfo delete(@PathVariable("id") Integer serviceId){
        try{
            int result = serviceService.delete(serviceId);
            return MessageInfo.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
}
