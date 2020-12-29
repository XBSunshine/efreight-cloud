package com.efreight.afbase.controller;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.afbase.entity.procedure.CssWorkloadDetail;
import com.efreight.afbase.entity.procedure.CssWorkloadDetailExcel;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.efreight.afbase.entity.procedure.CssWorkload;
import com.efreight.afbase.service.CssWorkloadService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;

/**
 * 工作量统计
 * @author caiwd
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/workload")
@Slf4j
public class CssWorkloadController {
	
	private final CssWorkloadService cssWorkloadService;
	
	
    @GetMapping
    public MessageInfo getCssWorkloadList(CssWorkload cssWorkload) {
        try {
            List<CssWorkload> result = cssWorkloadService.getCssWorkloadList(cssWorkload);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    @GetMapping(value="/detial")
    public MessageInfo getCssWorkloadDetail(CssWorkload cssWorkload) {
        try {
            List<Map> result = cssWorkloadService.getCssWorkloadDetail(cssWorkload);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") CssWorkload bean) throws IOException {

        List<CssWorkload> list = cssWorkloadService.getCssWorkloadList(bean);

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
                for (CssWorkload cssWorkload : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssWorkload));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportDetailExcel")
    public void exportDetailExcel(HttpServletResponse response, @ModelAttribute("bean") CssWorkload bean) throws IOException {

        List<CssWorkloadDetail> list = cssWorkloadService.getCssWorkloadDetailForExcel(bean);
        List<CssWorkloadDetailExcel> cssWorkloadDetailExcelList = new ArrayList<CssWorkloadDetailExcel>();

        if (list != null && list.size() > 0) {
            for (CssWorkloadDetail cssWorkloadDetail : list) {
                //处理主运单号
                if("AI".equals(bean.getBusinessScope())){
                    if(cssWorkloadDetail.getHawbNumber() != null && !"".equals(cssWorkloadDetail.getHawbNumber())){
                        cssWorkloadDetail.setAwbNumber(cssWorkloadDetail.getAwbNumber()+"_"+cssWorkloadDetail.getHawbNumber());
                    }else if(cssWorkloadDetail.getAwbNumber() != null && !"".equals(cssWorkloadDetail.getAwbNumber())){
                        cssWorkloadDetail.setAwbNumber(cssWorkloadDetail.getAwbNumber());
                    }else{
                        cssWorkloadDetail.setAwbNumber("");
                    }
                }else if("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())){
                    if(cssWorkloadDetail.getHawbNumber() != null && !"".equals(cssWorkloadDetail.getHawbNumber())){
                        cssWorkloadDetail.setAwbNumber(cssWorkloadDetail.getMblNumber()+"_"+cssWorkloadDetail.getHawbNumber());
                    }else if(cssWorkloadDetail.getMblNumber() != null && !"".equals(cssWorkloadDetail.getMblNumber())){
                        cssWorkloadDetail.setAwbNumber(cssWorkloadDetail.getMblNumber());
                    }else{
                        cssWorkloadDetail.setAwbNumber("");
                    }
                }
                //处理船名/航次
                if("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())){
                    cssWorkloadDetail.setShipName(cssWorkloadDetail.getShipName()+"/"+cssWorkloadDetail.getShipVoyageNumber());
                }
                String jsonStr = JSONObject.toJSONString(cssWorkloadDetail);
                CssWorkloadDetailExcel cssWorkloadDetailExcel = JSONObject.toJavaObject(JSONObject.parseObject(jsonStr), CssWorkloadDetailExcel.class);
                cssWorkloadDetailExcelList.add(cssWorkloadDetailExcel);
            }
        }

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
            if (cssWorkloadDetailExcelList != null && cssWorkloadDetailExcelList.size() > 0) {
                for (CssWorkloadDetailExcel cssWorkloadDetailExcel : cssWorkloadDetailExcelList) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], cssWorkloadDetailExcel));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }
    }

}
