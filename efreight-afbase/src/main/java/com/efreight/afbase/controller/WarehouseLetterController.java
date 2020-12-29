package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.WarehouseLetter;
import com.efreight.afbase.service.WarehouseLetterService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;


@RestController
@AllArgsConstructor
@RequestMapping("/warehouseLetter")
public class WarehouseLetterController {

    private final WarehouseLetterService service;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, WarehouseLetter bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody WarehouseLetter bean) {      
        return MessageInfo.ok(service.doSave(bean));
    }

    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody WarehouseLetter bean) {
        return MessageInfo.ok(service.doUpdate(bean));
    }

    /**
     * 获取
     * @param warehouseLetterId
     * @return
     */
    @GetMapping("detail/{id}")
    public MessageInfo detail(@PathVariable("id")Integer warehouseLetterId){
        return MessageInfo.ok(service.getWarehouseLetter(warehouseLetterId));
    }


    
}

