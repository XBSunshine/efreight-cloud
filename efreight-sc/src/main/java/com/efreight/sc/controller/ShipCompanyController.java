package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.ShipCompany;
import com.efreight.sc.service.ShipCompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 船司表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@RestController
@RequestMapping("/shipCompany")
@AllArgsConstructor
@Slf4j
public class ShipCompanyController {


    private final ShipCompanyService shipCompanyService;

    /**
     * 船公司查询分页
     * @param page
     * @param shipCompany
     * @return
     */
    @GetMapping
    public MessageInfo getPage(Page page, ShipCompany shipCompany) {
        try {
            IPage result = shipCompanyService.getPage(page, shipCompany);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("save")
    @PreAuthorize("@pms.hasPermission('sc_ship_company_save')")
    public MessageInfo save(@RequestBody ShipCompany shipCompany){
        try {
            int result = shipCompanyService.saveShipCompany(shipCompany);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @PostMapping("edit")
    @PreAuthorize("@pms.hasPermission('sc_ship_company_edit')")
    public MessageInfo edit(@RequestBody ShipCompany shipCompany){
        try {
            int result = shipCompanyService.editShipCompany(shipCompany);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

