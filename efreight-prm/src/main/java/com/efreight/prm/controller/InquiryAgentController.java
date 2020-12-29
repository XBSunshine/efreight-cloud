package com.efreight.prm.controller;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.prm.entity.InquiryAgentExcel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.InquiryAgent;
import com.efreight.prm.service.InquiryAgentService;
import com.efreight.prm.util.MessageInfo;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * PRM 询盘代理 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2020-05-18
 */
@RestController
@RequestMapping("/inquiryAgent")
public class InquiryAgentController {


    @Autowired
    private InquiryAgentService service;

    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public MessageInfo queryList(Integer currentPage, Integer pageSize, @ModelAttribute("bean") InquiryAgent bean) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("inquiryAgentName", bean.getInquiryAgentName());
            paramMap.put("inquiryAgentNameShort", bean.getInquiryAgentNameShort());
            paramMap.put("departureStation", bean.getDepartureStation());
            paramMap.put("arrivalStation", bean.getArrivalStation());
            paramMap.put("carrierCode", bean.getCarrierCode());
            paramMap.put("contractType", bean.getContractType());
            paramMap.put("isValid", bean.getIsValid());
            paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
            dataMap = service.queryList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 获取询价代理列表页面(询价单编辑页面查询使用)
     *
     * @param currentPage
     * @param pageSize
     * @param bean
     * @return
     */
    @RequestMapping(value = "/getInquiryAgentList", method = RequestMethod.POST)
    public MessageInfo getInquiryAgentList(Integer currentPage, Integer pageSize, @ModelAttribute("bean") InquiryAgent bean) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("inquiryAgentName", bean.getInquiryAgentName());
            paramMap.put("departureStation", bean.getDepartureStation());
            paramMap.put("arrivalStation", bean.getArrivalStation());
            paramMap.put("carrierCodes", bean.getCarrierCode());
            paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
            dataMap = service.getInquiryAgentList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /*
     * 增加
     */
    @RequestMapping(value = "/doSave", method = RequestMethod.POST)
    public MessageInfo doSave(@ModelAttribute("bean") InquiryAgent bean) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            bean.setCreatorId(SecurityUtils.getUser().getId());
            bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            bean.setCreateTime(new Date());
            bean.setOrgId(SecurityUtils.getUser().getOrgId());
            int agreement_id = service.doSave(bean);


            //保存日志
//			LogBean logBean = new LogBean();
//			logBean.setOp_level("高");
//			logBean.setOp_type("新建");
//			logBean.setOp_name("客商资料协议");
//			logBean.setOp_info("新建客商资料协议："+bean.getSerial_number()+" 客商资料："+bean.getCoop_name());
//			logService.doSave(logBean);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo viewCoop(@ModelAttribute("bean") InquiryAgent paramBean) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        InquiryAgent bean = new InquiryAgent();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("inquiryId", paramBean.getInquiryId());
            paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
            bean = service.queryById(paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(bean, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /*
     * 修改
     */
    @RequestMapping(value = "/doEdit", method = RequestMethod.POST)
    public MessageInfo doEdit(@ModelAttribute("bean") InquiryAgent newbean) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            //查询原协议
//			CoopAgreementBean bean=service.queryByID(newbean.getAgreement_id());
            newbean.setEditorId(SecurityUtils.getUser().getId());
            newbean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            newbean.setEditTime(new Date());

            service.doEdit(newbean);

            //保存日志
//			LogBean logBean = new LogBean();
//			logBean.setOp_level("高");
//			logBean.setOp_type("修改");
//			logBean.setOp_name("客商资料协议");
//			logBean.setOp_info("修改客商资料协议："+bean.getSerial_number()+" 客商资料："+newbean.getCoop_name());
//			logService.doSave(logBean);
        } catch (Exception e) {
            message = e.getMessage();
            System.out.println("We got unexpected:" + e.getMessage());
            code = 400;
        }

        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /*
     * 选择优势航司
     */
    @RequestMapping(value = "/selectCarrierCode", method = RequestMethod.POST)
    public MessageInfo selectCarrierCode() {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            dataMap = service.selectCarrierCode(paramMap);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /*
     * 选择始发港/目的港
     */
    @RequestMapping(value = "/selectAirport", method = RequestMethod.POST)
    public MessageInfo selectAirport() {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            dataMap = service.selectAirport(paramMap);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /*
     * 选择目的国
     */
    @RequestMapping(value = "/selectNation", method = RequestMethod.POST)
    public MessageInfo selectNation() {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            dataMap = service.selectNation(paramMap);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /*
     * 选择联系人
     */
    @RequestMapping(value = "/selectContacts", method = RequestMethod.POST)
    public MessageInfo selectContacts(@ModelAttribute("bean") InquiryAgent bean) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("orgId", SecurityUtils.getUser().getOrgId());
            paramMap.put("coopId", bean.getInquiryAgentId());
            dataMap = service.selectContacts(paramMap);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        MessageInfo messageInfo = new MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") InquiryAgent bean) throws IOException {
        //自定义字段
        List<InquiryAgent> list = service.exportExcel(bean);
        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if (list != null && list.size() > 0) {
                for (InquiryAgent inquiryAgent : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], inquiryAgent));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        } else {
            //默认查询导出
            List<InquiryAgentExcel> excelList = list.stream().map(inquiryAgent -> {
                InquiryAgentExcel inquiryAgentExcel = new InquiryAgentExcel();
                BeanUtils.copyProperties(inquiryAgent, inquiryAgentExcel);
                return inquiryAgentExcel;
            }).collect(Collectors.toList());
            //导出日志数据
            ExportExcel<InquiryAgentExcel> ex = new ExportExcel<InquiryAgentExcel>();
            String[] headers = {"客商代码", "询盘代理", "简称", "优势航司", "签约类型", "始发港", "目的港", "目的国", "航线", "订舱联系人", "备注", "90天内发盘票数"};
            ex.exportExcel(response, "导出EXCEL", headers, excelList, "Export");
        }
    }

}

