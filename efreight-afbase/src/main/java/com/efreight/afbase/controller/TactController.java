package com.efreight.afbase.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgentUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.afbase.entity.ShipperConsignee;
import com.efreight.afbase.entity.exportExcel.ShipperConsigneeExcel;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;
import com.efreight.afbase.entity.exportExcel.TactExcel;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.Tact;
import com.efreight.afbase.entity.TactInfo;
import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.CategoryService;
import com.efreight.afbase.service.TactService;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tact")
public class TactController {
	private final TactService service;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page,Tact bean) {
		//如果选择了私有数据或者 没有选择 默认查询 当前签约公司的数据，反之则查询ORG_ID为1的数据
		if("private".equals(bean.getDataSource())) {
			bean.setOrgId(SecurityUtils.getUser().getOrgId());
		}else if("ef".equals(bean.getDataSource())){
			bean.setOrgId(1);
		}else {
			bean.setDataSourceDef(SecurityUtils.getUser().getOrgId());
		}
		return MessageInfo.ok(service.getListPage(page,bean));
	}

	@GetMapping("/{tactId}")
	public MessageInfo tact(@PathVariable("tactId") Integer tactId){
		Tact tact = service.getById(tactId);
		return MessageInfo.ok(tact);
	}

	/**
	 * 删除信息
	 * @param tactId 数据ID
	 * @return
	 */
	@DeleteMapping("/{tactId}")
	@PreAuthorize("@pms.hasPermission('sys_base_tact_del')")
	public MessageInfo deleteTact(@PathVariable("tactId") Integer tactId){
		int result = service.deleteTactById(tactId);
		return MessageInfo.ok(result);
	}

	@PostMapping("/doSave")
	@PreAuthorize("@pms.hasPermission('sys_base_tact_add')")
	public MessageInfo doSave(@Valid @RequestBody Tact tact){
		int result = service.saveTack(tact);
		return MessageInfo.ok(result);
	}

	@PostMapping("/doUpdate")
	@PreAuthorize("@pms.hasPermission('sys_base_tact_edit')")
	public MessageInfo doUpdate(@Valid @RequestBody Tact tact){
		int result = service.updateTack(tact);
		return MessageInfo.ok(result);
	}
	
	/**
	 * 提供外部查询接口
	 * @param bean
	 * @return
	 */
	@GetMapping("/tactinfo")
	public MessageInfo getTactInfo(Tact bean) {
		try {
			List<TactInfo> list = new ArrayList<TactInfo>();
			if(StringUtils.isEmpty(bean.getAppid())) {
	    		return MessageInfo.failed("appid校验码为空");
			}else {
				//校验校验码是否有效
				boolean flag = service.checkAppid(bean.getAppid());
				if(!flag) {
					return MessageInfo.failed("appid校验码不存在");
				}
			}
	        if(StringUtils.isEmpty(bean.getDepartureStation())) {
	        	return MessageInfo.failed("始发港为空");
			}
	        if(StringUtils.isEmpty(bean.getArrivalStation())) {
	        	return MessageInfo.failed("目的港为空");
			}
	        if(StringUtils.isEmpty(bean.getFlightDate())) {
//	        	return MessageInfo.failed("航班日期为空");
			}else {
				bean.setCreateTimeBegin(bean.getFlightDate());
			}
	        bean.setOrgId(1);//默认查询为1的
			Page page = new Page();
			page.setCurrent(1);
			page.setSize(10000);
			IPage<Tact> pageTact = service.getListPage(page,bean);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			if(pageTact!=null&&pageTact.getRecords()!=null&&pageTact.getRecords().size()>0) {
				list = pageTact.getRecords().stream().map(a->{
					TactInfo b = new TactInfo();
					BeanUtils.copyProperties(a, b);
					if(a.getBeginDate()!=null) {
						b.setBeginDateStr(a.getBeginDate().format(formatter));
					}
					if(a.getEndDate()!=null) {
						b.setEndDateStr(a.getEndDate().format(formatter));
					}
					return b;
				}).collect(Collectors.toList());
			}
	    	return MessageInfo.ok(list);
		} catch (Exception e) {
			e.printStackTrace();
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 查询tact信息(AE制单使用)
	 *
	 *
	 *
	 */
	@GetMapping("/getTactForBillMake")
	public MessageInfo getTactForBillMake(Tact bean) {
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return MessageInfo.ok(service.getTactForBillMake(bean));
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
	public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") Tact bean) throws IOException {
		if("private".equals(bean.getDataSource())) {
			bean.setOrgId(SecurityUtils.getUser().getOrgId());
		}else if("ef".equals(bean.getDataSource())){
			bean.setOrgId(1);
		}else {
			bean.setOrgId(SecurityUtils.getUser().getOrgId());
		}
		List<TactExcel> list = service.queryListForExcel(bean);

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
				for (TactExcel tactExcel : list) {
					LinkedHashMap map = new LinkedHashMap();
					for (int j = 0; j < colunmStrs.length; j++) {
						map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], tactExcel));
					}
					listExcel.add(map);
				}
			}

			ExcelExportUtils u = new ExcelExportUtils();
			u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

		}
	}
}

