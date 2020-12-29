package com.efreight.afbase.controller;


import java.io.IOException;
import java.util.*;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.ShipperConsignee;
import com.efreight.afbase.entity.exportExcel.ShipperConsigneeExcel;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;
import com.efreight.afbase.service.ShipperConsigneeService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;

import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * AF 基础信息 收发货人 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-09
 */
@RestController
@RequestMapping("/shipperConsignee")
@AllArgsConstructor
@Slf4j
public class ShipperConsigneeController {

    private final ShipperConsigneeService shipperConsigneeService;

    /**
     * 分页列表查询
     * @param page
     * @param shipperConsignee
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, ShipperConsignee shipperConsignee){
        try {
          IPage<ShipperConsignee> result = shipperConsigneeService.selectToPage(page,shipperConsignee);
          return MessageInfo.ok(result);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @GetMapping("/page")
    public MessageInfo page2(Page page, ShipperConsignee shipperConsignee){
    	try {
    		IPage<ShipperConsignee> result = shipperConsigneeService.getPage2(page,shipperConsignee);
    		return MessageInfo.ok(result);
    	}catch (Exception e){
            log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }
    @GetMapping("/queryList")
    public MessageInfo queryList(ShipperConsignee shipperConsignee){
    	try {
    		List<ShipperConsignee> result = shipperConsigneeService.queryList(shipperConsignee);
    		return MessageInfo.ok(result);
    	}catch (Exception e){
    		log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }

    /**
     * 查看详情
     * @param scId
     * @return
     */
    @GetMapping("/view")
    @PreAuthorize("@pms.hasPermission('sys_shipperconsignee_view')")
    public MessageInfo view(Integer scId){
        try {
            ShipperConsignee shipperConsignee = shipperConsigneeService.view(scId);
            return MessageInfo.ok(shipperConsignee);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增
     * @param shipperConsignee
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_shipperconsignee_add')")
    public MessageInfo save(@RequestBody ShipperConsignee shipperConsignee){
        try {
            shipperConsigneeService.save(shipperConsignee);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 编辑
     * @param shipperConsignee
     * @return
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('sys_shipperconsignee_edit')")
    public MessageInfo modify(@RequestBody ShipperConsignee shipperConsignee){
        try {
            shipperConsigneeService.modify(shipperConsignee);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 作废
     * @param scId
     * @return
     */
    @PutMapping("/cancel/{scId}")
    @PreAuthorize("@pms.hasPermission('sys_shipperconsignee_cancel')")
    public MessageInfo cancel(@PathVariable("scId") Integer scId){
        try {
            shipperConsigneeService.cancel(scId);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据城市代码查询城市英文名称
     * @param cityCode
     * @return
     */
    @GetMapping("/searchCityName")
    public MessageInfo searchCityName(String cityCode) {
        try {
            String cityName = shipperConsigneeService.searchCityName(cityCode);
            return MessageInfo.ok(cityName);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据国家代码查询国家英文名称
     * @param nationCode
     * @return
     */
    @GetMapping("/searchNationalName")
    public MessageInfo searchNationalName(String nationCode) {
        try {
            String nationName = shipperConsigneeService.searchNationalName(nationCode);
            return MessageInfo.ok(nationName);
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

    @RequestMapping(value = "/exportExcel", method = RequestMethod.POST)
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") ShipperConsignee bean) throws IOException {

        List<ShipperConsigneeExcel> list = shipperConsigneeService.queryListForExcel(bean);
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
                for (ShipperConsigneeExcel shipperConsigneeExcel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], shipperConsigneeExcel));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }

    }
}

