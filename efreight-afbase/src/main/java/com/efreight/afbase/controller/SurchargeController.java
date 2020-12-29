package com.efreight.afbase.controller;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.exportExcel.AfServiceTemplateExcel;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;
import com.efreight.afbase.service.SurchargeService;
import com.efreight.afbase.service.TactService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
@RequestMapping("/surcharge")
public class SurchargeController {
	private final SurchargeService service;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page, Surcharge bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}

	/**
	 * 获取始发港，目的港
	 *
	 * @return
	 */
	@GetMapping("/getDepartureStationList")
	public MessageInfo getDepartureStationList() {
		try {
			List<Airport> list = service.getDepartureStationList();
			return MessageInfo.ok(list);
		} catch (Exception e) {
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 目的国
	 *
	 * @return
	 */
	@GetMapping("/getNationCodesList")
	public MessageInfo getNationCodesList() {
		try {
			List<Nation> list = service.getNationCodesList();
			return MessageInfo.ok(list);
		} catch (Exception e) {
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 新增
	 *
	 * @return
	 */
	@PostMapping("/doSave")
	public MessageInfo doSave(@RequestBody Surcharge surcharge){
		int result = service.saveSurcharge(surcharge);
		return MessageInfo.ok(result);
	}

	/**
	 * 删除
	 *
	 * @return
	 */
	@DeleteMapping("/{surchargeId}")
	public MessageInfo deleteSurcharge(@PathVariable("surchargeId") Integer surchargeId){
		int result = service.deleteSurchargeById(surchargeId);
		return MessageInfo.ok(result);
	}

	@GetMapping("/{surchargeId}")
	public MessageInfo getSurchargeInfo(@PathVariable("surchargeId") Integer surchargeId){
		Surcharge surcharge = service.getById(surchargeId);
		if(surcharge != null){
			if (surcharge.getDepartureStation()!=null && !"".equals(surcharge.getDepartureStation())) {
				surcharge.setDepartureStations(Arrays.asList(surcharge.getDepartureStation().split(",")));
			}
			if (surcharge.getArrivalStation()!=null && !"".equals(surcharge.getArrivalStation())) {
				surcharge.setArrivalStations(Arrays.asList(surcharge.getArrivalStation().split(",")));
			}
			if (surcharge.getArrivalNationCode()!=null && !"".equals(surcharge.getArrivalNationCode())) {
				surcharge.setArrivalNationCodes(Arrays.asList(surcharge.getArrivalNationCode().split(",")));
			}
		}
		return MessageInfo.ok(surcharge);
	}

	@PostMapping("/doUpdate")
	public MessageInfo doUpdate(@Valid @RequestBody Surcharge surcharge){
		int result = service.updateSurcharge(surcharge);
		return MessageInfo.ok(result);
	}

	/**
	 * 获取附加费
	 *
	 * @return
	 */
	@GetMapping("/getSurchargeForBillMake")
	public MessageInfo getSurchargeForBillMake(Surcharge surcharge) {
		try {
			List<Surcharge> list = service.getSurchargeForBillMake(surcharge);
			return MessageInfo.ok(list);
		} catch (Exception e) {
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
	public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") Surcharge bean) throws IOException {

		List<SurchargeExcel> list = service.queryListForExcel(bean);
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
				for (SurchargeExcel surchargeExcel : list) {
					LinkedHashMap map = new LinkedHashMap();
					for (int j = 0; j < colunmStrs.length; j++) {
						map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], surchargeExcel));
					}
					listExcel.add(map);
				}
			}

			ExcelExportUtils u = new ExcelExportUtils();
			u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

		}
	}

}

