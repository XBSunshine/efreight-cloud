package com.efreight.afbase.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.afbase.entity.CssPReportCustomerContribution;
import com.efreight.afbase.service.CssPReportCustomerContributionService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/customerContribution")
@AllArgsConstructor
@Slf4j
public class CssPReportCustomerContributionController {
	
	private final CssPReportCustomerContributionService service;
	
	  @GetMapping("/af")
	  public MessageInfo getAfList(CssPReportCustomerContribution bean) {
        try {
            List<Map> list = service.getAfList(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
	  }
	  
	  @GetMapping("/af/detail")
	  public MessageInfo getAfListDetail(CssPReportCustomerContribution bean) {
        try {
            List<Map> list = service.getAfListDetail(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
	  }

	  @GetMapping("/sc")
	  public MessageInfo getScList(CssPReportCustomerContribution bean) {
        try {
            List<Map> list = service.getScList(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
	  }
	  @GetMapping("/sc/detail")
	  public MessageInfo getScListDetail(CssPReportCustomerContribution bean) {
        try {
            List<Map> list = service.getScListDetail(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
	  }
	  
    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @PostMapping(value = "/exportExcel")
	public void exportExcel(HttpServletResponse response, CssPReportCustomerContribution bean) throws IOException{
    	 List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
         String[] colunmStrs = null;
         String[] headers = null;
         
         if (!StringUtils.isEmpty(bean.getColumnStrs())) {
        	 //转json为数组
             JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
             int num = jsonArr.size();
             headers = new String[num];
             colunmStrs = new String[num];
             //生成表头跟字段
             if (jsonArr != null && jsonArr.size() > 0) {
                 for (int i = 0; i < jsonArr.size(); i++) {
                     JSONObject job = jsonArr.getJSONObject(i);
                     headers[i] = job.getString("label");
                     colunmStrs[i] = job.getString("prop");
                     if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())||bean.getBusinessScope().startsWith("L")||bean.getBusinessScope().startsWith("I")) {
                         if ("plan_charge_weight_count".equals(job.getString("prop"))) {
                        	 headers[i] = "计重";
                         }
                         if ("plan_charge_weight_count_per".equals(job.getString("prop"))) {
                        	 headers[i] = "计重占比";
                         }
                         if ("plan_charge_weight_count_year".equals(job.getString("prop"))) {
                        	 headers[i] = "年度合作计重";
                         }
                     }
                     if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())||bean.getBusinessScope().startsWith("T")) {
                    	 if ("plan_charge_weight_count".equals(job.getString("prop"))) {
                        	  if("整箱".equals(bean.getContainerMethod())) {
                        		  headers[i] = "箱量";
                        	  }else {
                        		  headers[i] = "计费吨";
                        	  }
                    		 
                         }
                         if ("plan_charge_weight_count_per".equals(job.getString("prop"))) {
                        	 if("整箱".equals(bean.getContainerMethod())) {
                       		      headers[i] = "箱量占比";
                       	     }else {
                       		      headers[i] = "计费吨占比";
                       	     }
                         }
                         if ("plan_charge_weight_count_year".equals(job.getString("prop"))) {
                        	 if("整箱".equals(bean.getContainerMethod())) {
                          		  headers[i] = "年度合作箱量";
                          	 }else {
                          		  headers[i] = "年度合作计费吨";
                          	 }
                         }
                     }
                 }
             }
         }
         //结果集
         List<Map> ListMap = null;
         
         if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
        	 ListMap = service.getAfList(bean);
         }else {
        	 ListMap = service.getScList(bean);
         }
         if(ListMap!=null) {
        	 for(Map map:ListMap) {
        		 LinkedHashMap mapLink = new LinkedHashMap();
        		 for (int j = 0; j < colunmStrs.length; j++) {
        			 if("gross_profit_margin".equals(colunmStrs[j])) {
        				 if("0".equals(map.get("gross_profit_count").toString())||map.get(colunmStrs[j])==null) {
        					 mapLink.put("gross_profit_margin", "0.00%");
        				 }else {
        					 mapLink.put("gross_profit_margin", map.get(colunmStrs[j])+"%");
        				 }
        			 }else if("order_count_per".equals(colunmStrs[j])||"plan_charge_weight_count_per".equals(colunmStrs[j])
    						 ||"income_functional_amount_count_per".equals(colunmStrs[j])||"gross_profit_count_per".equals(colunmStrs[j])){
        					 if(map.get(colunmStrs[j])==null) {
        						 mapLink.put(colunmStrs[j],"0.00%");
            				 }else {
            					 mapLink.put(colunmStrs[j], map.get(colunmStrs[j])+"%");
            				 }
        			}else if("plan_charge_weight_count".equals(colunmStrs[j])||"income_functional_amount_count".equals(colunmStrs[j])
        						 ||"gross_profit_count".equals(colunmStrs[j])||"unit_gross_profit".equals(colunmStrs[j])
        						 ||"plan_charge_weight_count_year".equals(colunmStrs[j])||"gross_profit_year".equals(colunmStrs[j])||"order_count_year".equals(colunmStrs[j])) {
        				mapLink.put(colunmStrs[j], FormatUtils.formatWithQWF(new BigDecimal(map.get(colunmStrs[j])!=null?map.get(colunmStrs[j]).toString():"0"), 2));
	    			}else {
	    				mapLink.put(colunmStrs[j], map.get(colunmStrs[j]));
	    		    }
        			 
        		 }
        		 listExcel.add(mapLink);
        	 }
        	 ExcelExportUtils u = new ExcelExportUtils();
             u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
         }
    }
    
	  @GetMapping("/customer/detail")
	  public MessageInfo getCustomerDetail(CssPReportCustomerContribution bean) {
      try {
          Map list = service.getCustomerDetail(bean);
          return MessageInfo.ok(list);
      } catch (Exception e) {
          log.info(e.getMessage());
          return MessageInfo.failed(e.getMessage());
      }
	  }
	  
	  /**
	 * 导出ExcelDetail
	 *
	 * @param
	 * @param response
	 * @param bean
	 * @throws IOException
	 */
	
	@PostMapping(value = "/exportExcelDetail")
	public void exportExcelDetail(HttpServletResponse response, CssPReportCustomerContribution bean) throws IOException{
		 List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
	     String[] colunmStrs = null;
	     String[] headers = null;
	     
	     if (!StringUtils.isEmpty(bean.getColumnStrs())) {
	    	 //转json为数组
	         JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
	         int num = jsonArr.size();
	         int numP = num;
	         if(bean.isShowConstituteFlag()) {
	        	 numP = numP+24;
	         }
	         headers = new String[numP];
	         colunmStrs = new String[numP];
	         //生成表头跟字段
	         if (jsonArr != null && jsonArr.size() > 0) {
	             for (int i = 0; i < jsonArr.size(); i++) {
	                 JSONObject job = jsonArr.getJSONObject(i);
	                 headers[i] = job.getString("label");
	                 colunmStrs[i] = job.getString("prop");
	             }
	         }
	         if(bean.isShowConstituteFlag()) {
	         //毛利构成 表头
	         headers[num] = "干线收入";colunmStrs[num] = "main_routing_income";
	         headers[num+1] = "干线成本";colunmStrs[num+1] = "main_routing_cost";
	         headers[num+2] = "干线毛利";colunmStrs[num+2] = "main_routing";
	         headers[num+3] = "支线收入";colunmStrs[num+3] = "feeder_income";
	         headers[num+4] = "支线成本";colunmStrs[num+4] = "feeder_cost";
	         headers[num+5] = "支线毛利";colunmStrs[num+5] = "feeder";
	         headers[num+6] = "操作收入";colunmStrs[num+6] = "operation_income";
	         headers[num+7] = "操作成本";colunmStrs[num+7] = "operation_cost";
	         headers[num+8] = "操作毛利";colunmStrs[num+8] = "operation";
	         headers[num+9] = "包装收入";colunmStrs[num+9] = "packaging_income";
	         headers[num+10] = "包装成本";colunmStrs[num+10] = "packaging_cost";
	         headers[num+11] = "包装毛利";colunmStrs[num+11] = "packaging";
	         headers[num+12] = "仓储收入";colunmStrs[num+12] = "storage_income";
	         headers[num+13] = "仓储成本";colunmStrs[num+13] = "storage_cost";
	         headers[num+14] = "仓储毛利";colunmStrs[num+14] = "storage";
	         headers[num+15] = "快递收入";colunmStrs[num+15] = "postage_income";
	         headers[num+16] = "快递成本";colunmStrs[num+16] = "postage_cost";
	         headers[num+17] = "快递毛利";colunmStrs[num+17] = "postage";
	         headers[num+18] = "关检收入";colunmStrs[num+18] = "clearance_income";
	         headers[num+19] = "关检成本";colunmStrs[num+19] = "clearance_cost";
	         headers[num+20] = "关检毛利";colunmStrs[num+20] = "clearance";
	         headers[num+21] = "数据收入";colunmStrs[num+21] = "exchange_income";
	         headers[num+22] = "数据成本";colunmStrs[num+22] = "exchange_cost";
	         headers[num+23] = "数据毛利";colunmStrs[num+23] = "exchange";
	       }
	     }
	     //结果集
	     List<Map> ListMap = null;
	     
	     if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
	    	 ListMap = service.getAfListDetail(bean);
	     }else{
	    	 ListMap = service.getScListDetail(bean);
	     }
	     if(ListMap!=null) {
	    	 for(Map map:ListMap) {
	    		 LinkedHashMap mapLink = new LinkedHashMap();
	    		 for (int j = 0; j < colunmStrs.length; j++) {
	    			 if("gross_profit_margin".equals(colunmStrs[j])) {
	    				 if(Double.valueOf(map.get("gross_profit").toString())==0) {
	    					 mapLink.put("gross_profit_margin", "0.00%");
	    				 }else {
	    					 mapLink.put("gross_profit_margin", map.get(colunmStrs[j])+"%");
	    				 }
	    			 }else {
	    				 if("sales_name".equals(colunmStrs[j])||"servicer_name".equals(colunmStrs[j])) {
	    					 mapLink.put(colunmStrs[j], map.get(colunmStrs[j]).toString().split(" ")[0]);
	    				 }else if("unit_cost_amount".equals(colunmStrs[j])||"unit_gross_profit".equals(colunmStrs[j])){
	    					 if(Double.valueOf(map.get("plan_charge_weight").toString())==0) {
	    						 mapLink.put(colunmStrs[j], "0.00");
	    					 }else {
	    						 mapLink.put(colunmStrs[j], map.get(colunmStrs[j]));
	    					 }
	    				 }else if("income_functional_amount_count".equals(colunmStrs[j])||"gross_profit".equals(colunmStrs[j])
	    						 ||"functional_amount_no_writeoff".equals(colunmStrs[j])||"cost_functional_amount_count".equals(colunmStrs[j])){
	    					 mapLink.put(colunmStrs[j], FormatUtils.formatWithQWF(new BigDecimal(map.get(colunmStrs[j])!=null?map.get(colunmStrs[j]).toString():"0"), 2));
	    				 }else {
	    					 mapLink.put(colunmStrs[j], map.get(colunmStrs[j]));
	    				 }
	    			 }
	    		 }
	    		 listExcel.add(mapLink);
	    	 }
	    	 ExcelExportUtils u = new ExcelExportUtils();
	         u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
		   }
	 }
	
	  /**
	 * 导出航线分析表 统计标签
	 *
	 * @param
	 * @param response
	 * @param bean
	 * @throws IOException
	 */
	
	@PostMapping(value = "/exportExcelCount")
	public void exportExcelCount(HttpServletResponse response, CssPReportCustomerContribution bean) throws IOException{
		 List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
		 //结果集
	     String countName = "航线";
	     String countJzName = "计重(吨)";
	     Map list = service.getCustomerDetail(bean);
	     List<Map> ListMap = (List<Map>) list.get("mapCount");
	     if (bean.getBusinessScope().startsWith("A")||bean.getBusinessScope().startsWith("S")) {
	    	 countName = "航线";
	    	 if(bean.getBusinessScope().startsWith("S")) {
	    		 if("整箱".equals(bean.getContainerMethod())) {
	    			 countJzName = "箱量(TEU)";
	    		 }else {
	    			 countJzName = "计费(吨)";
	    		 }
	    	 }
	     }else{
	    	 countName = "目的地";
	    	 if(bean.getBusinessScope().startsWith("T")&&"整箱".equals(bean.getContainerMethod())) {
	    		 countJzName = "箱量(TEU)";
	    	 }
	     }
	     String[] colunmStrs = new String[15];
	     String[] headers = new String[15];
         //统计
         headers[0] = countName;colunmStrs[0] = "arr";
         headers[1] = "票数";colunmStrs[1] = "piao";
         headers[2] = "票数(同期)";colunmStrs[2] = "piaoTwo";
         headers[3] = countJzName;colunmStrs[3] = "weight";
         headers[4] = countJzName+"(同期)";colunmStrs[4] = "weightTwo";
         headers[5] = "收入(万元)";colunmStrs[5] = "shouru";
         headers[6] = "收入(万元)(同期)";colunmStrs[6] = "shouruTwo";
         headers[7] = "成本(万元)";colunmStrs[7] = "chengben";
         headers[8] = "成本(万元)(同期)";colunmStrs[8] = "chengbenTwo";
         headers[9] = "毛利(万元)";colunmStrs[9] = "maoli";
         headers[10] = "毛利(万元)(同期)";colunmStrs[10] = "maoliTwo";
         headers[11] = "单位毛利";colunmStrs[11] = "danweimaoli";
         headers[12] = "单位毛利(同期)";colunmStrs[12] = "danweimaoliTwo";
         headers[13] = "毛利率";colunmStrs[13] = "maolilv";
         headers[14] = "毛利率(同期)";colunmStrs[14] = "maolilvTwo";
	    
	     if(ListMap!=null) {
	    	 for(Map map:ListMap) {
	    		 LinkedHashMap mapLink = new LinkedHashMap();
	    		 for (int j = 0; j < colunmStrs.length; j++) {
//	    			 FormatUtils.formatWithQWF(new BigDecimal(map.get(colunmStrs[j])!=null?map.get(colunmStrs[j]).toString():"0"), 2)
	    			 if("maolilv".equals(colunmStrs[j])||"maolilvTwo".equals(colunmStrs[j])) {
	    				 mapLink.put(colunmStrs[j], map.get(colunmStrs[j])+"%");
	    			 }else {
	    				 mapLink.put(colunmStrs[j], map.get(colunmStrs[j]));
	    			 }
	    			 
	    		 }
	    		 listExcel.add(mapLink);
	    	 }
	    	 ExcelExportUtils u = new ExcelExportUtils();
	         u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
		   }
	 }
}
