package com.efreight.afbase.controller;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.afbase.entity.ShipperConsignee;
import com.efreight.afbase.entity.exportExcel.AfServiceTemplateExcel;
import com.efreight.afbase.entity.exportExcel.ShipperConsigneeExcel;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.AfServiceTemplate;
import com.efreight.afbase.entity.IncomeCostList;
import com.efreight.afbase.service.AfServiceTemplateService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * AF 基础信息 服务类别：收付模板设定 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2020-06-01
 */
@RestController
@AllArgsConstructor
@RequestMapping("/servicetemplate")
@Slf4j
public class AfServiceTemplateController {

	private final AfServiceTemplateService service;
	
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page,AfServiceTemplate bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}
	/**
     * 添加
     *
     * @param dept 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody AfServiceTemplate bean) {

        return MessageInfo.ok(service.doSave(bean));
    }
    /**
     * 修改
     *
     * @param dept 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doEdit")
    public MessageInfo doEdit(@Valid @RequestBody AfServiceTemplate bean) {
    	
    	return MessageInfo.ok(service.doEdit(bean));
    }
    @PostMapping(value = "/getView")
    public MessageInfo getView(@Valid @RequestBody AfServiceTemplate bean) {
        return MessageInfo.ok(service.getView(bean));
    }
    @PostMapping(value = "/getServicetemplate")
    public MessageInfo getServicetemplate(@Valid @RequestBody AfServiceTemplate bean) {
    	return MessageInfo.ok(service.getServicetemplate(bean));
    }
    /**
     * 删除
     *
     * @param dept 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doDelete")
    public MessageInfo doDelete(@Valid @RequestBody AfServiceTemplate bean) {

        return MessageInfo.ok(service.doDelete(bean));
    }

    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @RequestMapping(value = "/exportExcel", method = RequestMethod.POST)
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") AfServiceTemplate bean) throws IOException {

        List<AfServiceTemplateExcel> list = service.queryListForExcel(bean);
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
                    if ("AE".equals(bean.getBusinessScope()) || "SE".equals(bean.getBusinessScope())) {
                        if ("portCode".equals(job.getString("prop"))) {
                            headers[i] = "始发港";
                        }
                    }else if(!"AE".equals(bean.getBusinessScope()) && !"SE".equals(bean.getBusinessScope())){
                        if ("portCode".equals(job.getString("prop"))) {
                            headers[i] = "目的港";
                        }
                    }
                }
            }
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if (list != null && list.size() > 0) {
                for (AfServiceTemplateExcel afServiceTemplateExcel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("editTime".equals(colunmStrs[j])) {
                            if (afServiceTemplateExcel.getEditTime() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                map.put("editTime", formatter.format(afServiceTemplateExcel.getEditTime()));
                            } else {
                                map.put("editTime", "");
                            }
                        }else{
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], afServiceTemplateExcel));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
        /*String portCode = "";
        if("AE".equals(bean.getBusinessScope()) || "SE".equals(bean.getBusinessScope())){
            portCode = "始发港";
        }else{
            portCode = "目的港";
        }
        ExportExcel<AfServiceTemplateExcel> ex = new ExportExcel<AfServiceTemplateExcel>();
        String[] headers = {"业务范畴", "模板类型", portCode, "模板名称", "备注", "操作人", "操作日期"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");*/
    }
}

