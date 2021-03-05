package com.efreight.prm.controller;

import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.CoopAgreementSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coopAgreementSettlement")
@Slf4j
public class CoopAgreementSettlementController {

    @Autowired
    private CoopAgreementSettlementService coopAgreementSettlementService;

    /**
     * 有条件查询结算协议列表
     *
     * @param coopAgreementSettlement
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/findCoopAgreementSettlementListCriteria")
    public Map<String, Object> findCoopAgreementSettlementListCriteria(CoopAgreementSettlement coopAgreementSettlement, Integer currentPage, Integer pageSize) {
        return coopAgreementSettlementService.findCoopAgreementSettlementListCriteria(coopAgreementSettlement, currentPage, pageSize);
    }

    /**
     * 删除一个结算协议
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/deleteCoopAgreementSettlement")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_del')")
    public void deleteParam(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.deleteCoopAgreementSettlement(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("删除失败，原因：" + e.getMessage());
        }
    }

    /**
     * 财务审核
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/validCoopAgreementSettlement")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_valid')")
    public void validCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.validCoopAgreementSettlement(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("删除失败，原因：" + e.getMessage());
        }
    }

    /**
     * IT审核
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/validItCoopAgreementSettlement")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_itinvalid')")
    public void validItCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.validItCoopAgreementSettlement(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("删除失败，原因：" + e.getMessage());
        }
    }

    /**
     * 设置为失效
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/invalidCoopAgreementSettlement")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_invalid')")
    public void invalidCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.invalidCoopAgreementSettlement(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("删除失败，原因：" + e.getMessage());
        }
    }


    /**
     * 创建一个结算协议
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/createCoopAgreementSettlement")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_addbilltemplate')")
    public void createParam(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.createCoopAgreementSettlement(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("创建失败，原因：" + e.getMessage());
        }
    }

    /**
     * 创建一个账单客户
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/createCoopAgreementSettlementGroup")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_addbillcustomer')")
    public void createCoopAgreementSettlementGroup(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.createCoopAgreementSettlementGroup(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("创建失败，原因：" + e.getMessage());
        }
    }

    /**
     * 编辑账单客户
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/editCoopAgreementSettlementGroup")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_edit')")
    public void editCoopAgreementSettlementGroup(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.editCoopAgreementSettlementGroup(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("创建失败，原因：" + e.getMessage());
        }
    }

    /**
     * 查询一个结算协议
     *
     * @param coopAgreementSettlement
     * @return
     */
    @RequestMapping("/findCoopAgreementSettlementCriteria")
    public CoopAgreementSettlement findCoopAgreementSettlementCriteria(CoopAgreementSettlement coopAgreementSettlement) {

        return coopAgreementSettlementService.findCoopAgreementSettlementCriteria(coopAgreementSettlement);
    }

    /**
     * 修改结算协议
     *
     * @param coopAgreementSettlement
     */
    @RequestMapping("/modifyCoopAgreementSettlement")
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_edit')")
    public void modifyCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        try {
            coopAgreementSettlementService.modifyCoopAgreementSettlement(coopAgreementSettlement);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("修改失败，原因：" + e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param request
     * @param response
     * @param bean
     * @throws IOException
     */
    @RequestMapping(value = "/exportExcel1", method = RequestMethod.POST)
    public void exportExcel1(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("bean") CoopAgreementSettlement bean) throws IOException {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("coopName", bean.getCoopName());
        paramMap.put("agreementId", bean.getAgreementId());
        paramMap.put("needEmail", bean.getNeedEmail());
        paramMap.put("orgId", SecurityUtils.getUser().getOrgId());

        //List<CoopAgreementSettlementExcel> list = coopAgreementSettlementService.queryListForExcel(paramMap);
        //导出日志数据
        ExportExcel<CoopAgreementSettlementExcel> ex = new ExportExcel<CoopAgreementSettlementExcel>();
        String[] headers = {"协议结算编号", "协议编号", "客商资料", "计费模式", "结算周期", "单价", "应收金额", "最小金额", "最大金额", "数据填充工作日"
                , "数据填充责任人", "账单确认工作日", "账单确认责任人", "客户对账联系人","是否发送明细","是否电子发票","是否专用发票","创建人","创建时间","创建人部门"};
        //ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
    }

    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param coopAgreementSettlement
     * @throws IOException
     */

    @RequestMapping(value = "/exportExcel", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_settlement_export')")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") CoopAgreementSettlement coopAgreementSettlement) throws IOException {
        List<CoopAgreementSettlementExcel> list = coopAgreementSettlementService.queryListForExcel(coopAgreementSettlement);

        ExportExcel<CoopAgreementSettlementExcel> ex = new ExportExcel<CoopAgreementSettlementExcel>();
        String[] headers = {"账单模板", "企业名称", "企业六字码", "口岸", "进出港", "航班性质", "收费项目", "IT编码", "收费标准" , "结算方式" , "结算周期" , "计费模式", "收费备注", "收费期限"
                , "数据填充人", "账单确认人", "账单审核人", "总部确认人", "模板状态" , "发送明细" , "首次收费月份", "是否需要IT审核"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");

    }

    /**
     * 查询有效的航班性质
     *
     * @param
     * @return
     */
    @GetMapping("/selectFlightOptions")
    public MessageInfo selectFlightOptions() {
        try {
            List<FlightOptionsBean> list = coopAgreementSettlementService.selectFlightOptions();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
}
