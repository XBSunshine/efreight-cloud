package com.efreight.afbase.controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssFinancialExpenseReport;
import com.efreight.afbase.entity.CssPReportSettleExcel;
import com.efreight.afbase.service.CssFinancialExpenseReportService;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * CSS 财务费用报销 前端控制器
 * </p>
 *
 * @author caiwd
 * @since 2020-10-14
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("cssFinancialExpenseReport")
public class CssFinancialExpenseReportController {
	
	private final CssFinancialExpenseReportService service;
	
	
	/**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, CssFinancialExpenseReport bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

	 /**
	  * 获取当前用户部门相关信息
	  */
    @GetMapping("/deptInfo")
    public MessageInfo deptInfo() {
        return MessageInfo.ok(service.getDeptInfo());
    }
    
    /**
	  * 获取当前签约公司有效用户
	  */
   @GetMapping("/orguser")
   public MessageInfo orguser(CssFinancialExpenseReport bean) {
	   if("1".equals(bean.getAuditType())) {
		   return MessageInfo.ok(service.getOrguser());
	   }else {
		   List<Map> list = new ArrayList<>();
		   Map map = new HashMap();
		   map.put("code", SecurityUtils.getUser().getId());
		   map.put("name", SecurityUtils.getUser().getUserCname());
		   list.add(map);
		   return MessageInfo.ok(list);
	   }
   }
    
    /**
     * 信息查看
     */
    @GetMapping("/view/{expenseReportId}")
    public MessageInfo view(@PathVariable Integer expenseReportId) {
        return MessageInfo.ok(service.view(expenseReportId));
    }
    
    /**
     * 编辑
     * @param bean 实体
     */
    @PostMapping(value = "/modify")
    public MessageInfo modify(@Valid @RequestBody CssFinancialExpenseReport bean) {
        try {
            return MessageInfo.ok(service.modify(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 编辑状态 单独接口   以便后续做业务特殊处理
     * @param bean 实体
     */
    @PostMapping(value = "/modify/status")
    public MessageInfo modifyStatus(@Valid @RequestBody CssFinancialExpenseReport bean) {
        try {
            return MessageInfo.ok(service.modifyStatus(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 添加
     * @param bean 实体
     */
//    @PreAuthorize("@pms.hasPermission('')")
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody CssFinancialExpenseReport bean) {
        try {
            return MessageInfo.ok(service.doSave(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    @GetMapping("/subject")
    public MessageInfo subject() {
        return MessageInfo.ok(service.getSubject());
    }

    /**
     * 财务审核
     * @param bean 实体
     */
    @PostMapping(value = "/audit")
    public MessageInfo audit(@Valid @RequestBody CssFinancialExpenseReport bean) {
        try {
            return MessageInfo.ok(service.audit(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 付款
     * @param bean 实体
     */
    @PostMapping(value = "/payment")
    public MessageInfo payment(@Valid @RequestBody CssFinancialExpenseReport bean) {
        try {
            return MessageInfo.ok(service.payment(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 删除
     * @param bean 实体
     */
    @PostMapping(value = "/delete")
    public MessageInfo delete(@Valid @RequestBody CssFinancialExpenseReport bean) {
        try {
        	service.delete(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
  	 * 导出
  	 *
  	 * @param
  	 * @param response
  	 * @param bean
  	 * @throws IOException
  	 */
  	
  	@PostMapping(value = "/exportExcel")
  	public void exportExcel(HttpServletResponse response, CssFinancialExpenseReport bean) throws IOException{
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
  		 //结果集
  		Page page = new Page();
        page.setCurrent(1);
        page.setSize(1000000);
        IPage result = service.getListPage(page, bean);
        List<CssFinancialExpenseReport> list = result.getRecords();
        if (list != null && list.size() > 0) {
            for (CssFinancialExpenseReport excel : list) {
                LinkedHashMap map = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                      if("expenseFinancialAccountName".equals(colunmStrs[j])) {
                    	  if(excel.getExpenseFinancialAccountName()!=null) {
                    		  map.put(colunmStrs[j],FieldValUtils.getFieldValueByFieldName("expenseFinancialAccountCode", excel)+" "+FieldValUtils.getFieldValueByFieldName("expenseFinancialAccountName", excel));
                    	  }else {
                    		  map.put(colunmStrs[j],"");
                    	  }
                      }else if("bankFinancialAccountName".equals(colunmStrs[j])) {
                    	  if(excel.getBankFinancialAccountName()!=null) {
                    		  map.put(colunmStrs[j],FieldValUtils.getFieldValueByFieldName("bankFinancialAccountCode", excel)+" "+FieldValUtils.getFieldValueByFieldName("bankFinancialAccountName", excel));
                    	  }else {
                    		  map.put(colunmStrs[j],"");
                    	  }
                      }else {
                    	  map.put(colunmStrs[j],FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                      }
                }
                listExcel.add(map);
            }
        }
       ExcelExportUtils u = new ExcelExportUtils();
	   u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");
  	 }
  	
  	/**
     * 打印
     * @param bean 实体
     */
    @PostMapping(value = "/print")
    public void print(CssFinancialExpenseReport bean) {
        try {
        	service.print(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

