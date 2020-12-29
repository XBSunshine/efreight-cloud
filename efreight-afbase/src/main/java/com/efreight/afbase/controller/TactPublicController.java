package com.efreight.afbase.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.TactPublic;
import com.efreight.afbase.service.TactPublicService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * tact公布价
 *
 * @author limr
 * @since 20201211
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tactPublic")
public class TactPublicController {
	private final TactPublicService service;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@PostMapping("/page")
	public MessageInfo getListPage(Page page,TactPublic bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}

	@GetMapping("/{tactId}")
	public MessageInfo tact(@PathVariable("tactId") Integer tactId){
		TactPublic tact = service.getById(tactId);
		return MessageInfo.ok(tact);
	}

	/**
	 * 删除信息
	 * @param tactId 数据ID
	 * @return
	 */
	@DeleteMapping("/deleteTact/{tactId}")
	@PreAuthorize("@pms.hasPermission('sys_base_tact_public_del')")
	public MessageInfo deleteTact(@PathVariable("tactId") Integer tactId){
		service.deleteTactById(tactId);
		return MessageInfo.ok(null);
	}

	@PostMapping("/doSave")
	@PreAuthorize("@pms.hasPermission('sys_base_tact_public_add')")
	public MessageInfo doSave(@Valid @RequestBody TactPublic tact){
		int result = service.saveTack(tact);
		return MessageInfo.ok(result);
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
	public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") TactPublic bean) throws IOException {

		List<TactPublic> list = service.queryListForExcel(bean);

		if (StringUtils.isNotEmpty(bean.getColumnStrs())) {
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
					if("beginDate".equals(job.getString("prop"))){
						colunmStrs[i] = "beginDateStr";
					}else if("endDate".equals(job.getString("prop"))){
						colunmStrs[i] = "endDateStr";
					}else{
						colunmStrs[i] = job.getString("prop");
					}
				}
			}
			//遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
			if (list != null && list.size() > 0) {
				for (TactPublic tactExcel : list) {
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

