package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.AwbNumber;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.AwbNumberService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/awb")
@Slf4j
public class AwbNumberController {
	private final AwbNumberService service;
	private final AirportService airportService;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page,AwbNumber bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}
	/**
	 * 新增分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/selectpage")
	public MessageInfo getSelectListPage(Page page,AwbNumber bean) {
		return MessageInfo.ok(service.getSelectListPage(page,bean));
	}
	@GetMapping("/queryList")
	public MessageInfo queryList(AwbNumber bean) {
		return MessageInfo.ok(service.queryList(bean));
	}
	/**
	 * 选择通用
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectCategory")
	public MessageInfo selectCategory(String category) {
		//运单来源
		return MessageInfo.ok(service.selectCategory(category));
	}
	/**
	 * 选择通用业务范畴
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectCategory/{category}/{businessScope}")
	public MessageInfo selectCategory(@PathVariable String category,@PathVariable String businessScope) {
		//运单来源
		return MessageInfo.ok(service.selectCategory2(category,businessScope));
	}
	@GetMapping(value = "/selectCategoryPro/{category}/{departureStation}")
	public MessageInfo selectCategoryPro(@PathVariable String category,@PathVariable String departureStation) {
		return MessageInfo.ok(service.selectCategoryPro(category,departureStation));
	}
	/**
	 * 选择货站
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectWarehouse")
	public MessageInfo selectWarehouse(String warehouse) {
		//运单来源
		return MessageInfo.ok(service.selectWarehouse(warehouse));
	}
	/**
	 * 生成主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/getAwbList")
	public MessageInfo getAwbList(@RequestBody AwbNumber bean) {
		String departureStation = bean.getDepartureStation();
		if(StringUtils.hasText(departureStation)){
			Airport airport = airportService.getAirportCityNameENByApCode(departureStation.toUpperCase());
			if(null == airport){
				return MessageInfo.failed("始发港不存在！");
			}
		}
		//航司
		List<Map<String, Object>> list=service.selectCarrier(bean.getAwb3());
		if (list.size()==0) {
			return MessageInfo.failed(bean.getAwb3()+"不存在");
		}
		return MessageInfo.ok(getAwbList(bean.getAwb8(),bean.getAwbcount(),bean.getAwb3()));
	}
	private  ArrayList<Map<String, Object>> getAwbList(String StartNo, int count,String awb3) {
		ArrayList<Map<String, Object>> al = new ArrayList<Map<String, Object>>();
		String awbCode = "";
		String startAwbCode = StartNo.substring(0, StartNo.length() - 1);
		for (int i = 0; i < count; i++) {
			awbCode = String.valueOf(Integer.parseInt(startAwbCode) + i)
					+ String.valueOf((Integer.parseInt(startAwbCode) + i) % 7);
			if (awbCode.length() < 8) {
				awbCode = "00000000".substring(0, 8 - awbCode.length()) + awbCode;
			}
			if (awbCode.length()==8) {
//				System.out.println(awbCode);
				Map<String, Object> map=new HashMap<String, Object>();
				map.put("number",al.size()+1);
				map.put("awbNumber",awb3+"-"+awbCode);
				al.add(map);
			}
		}
		return al;
	}
	private  ArrayList<String> getAwbList2(String StartNo, int count,String awb3) {
		ArrayList<String> al = new ArrayList<String>();
		String awbCode = "";
		String startAwbCode = StartNo.substring(0, StartNo.length() - 1);
		for (int i = 0; i < count; i++) {
			awbCode = String.valueOf(Integer.parseInt(startAwbCode) + i)
					+ String.valueOf((Integer.parseInt(startAwbCode) + i) % 7);
			if (awbCode.length() < 8) {
				awbCode = "00000000".substring(0, 8 - awbCode.length()) + awbCode;
			}
			if (awbCode.length()==8) {
				System.out.println(awbCode);
				
				al.add(awb3+"-"+awbCode);
			}
		}
		return al;
	}
	
	/**
	 * 添加
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_base_awb_add')")
	public MessageInfo save(@Valid @RequestBody AwbNumber bean) {
		ArrayList<Map<String, Object>> al = getAwbList(bean.getAwb8(),bean.getAwbcount(),bean.getAwb3());
		return MessageInfo.ok(service.saveAwbNumber(bean,al));
	}
	
	/**
	 * 校验主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/checkAwbList")
	public MessageInfo checkAwbList(@RequestBody AwbNumber bean) {
		//航司
		List<Map<String, Object>> list=service.selectCarrier(bean.getAwb3());
		if (list.size()==0) {
			return MessageInfo.failed(bean.getAwb3()+"不存在");
		}
		ArrayList<String> al = getAwbList2(bean.getAwb8(),bean.getAwbcount(),bean.getAwb3());
		//一年以内存在的运单
		List<AwbNumber> oneList=service.selectOneYearAwbList(al);
		if (oneList.size()>0) {
			StringBuffer abwStr=new StringBuffer();
			for (int i = 0; i < oneList.size(); i++) {
				abwStr.append(oneList.get(i).getAwbNumber());
				abwStr.append(",");
			}
			return MessageInfo.failed(abwStr.toString()+"主单号已存在，不允许保存");
		}
		//两年以内存在的运单
		List<AwbNumber> twoList=service.selectTwoYearAwbList(al);
		if (twoList.size()>0) {
			StringBuffer abwStr=new StringBuffer();
			for (int i = 0; i < twoList.size(); i++) {
				abwStr.append(twoList.get(i).getAwbNumber());
				abwStr.append(",");
			}
			return MessageInfo.failed(abwStr.toString()+"主单号已存在，是否继续保存");
		}
		return MessageInfo.ok();
	}
	
	/**
	 * 预订主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/bookAwbList")
	public MessageInfo bookAwbList(@RequestBody AwbNumber bean) {
		return MessageInfo.ok(service.bookAwbList(bean));
	}
	/**
	 * 取消预订主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/cancelBook")
	public MessageInfo cancelBook(@RequestBody AwbNumber bean) {
		return MessageInfo.ok(service.cancelBook(bean));
	}
	/**
	 * 锁定主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/doLock")
	public MessageInfo doLock(@RequestBody AwbNumber bean) {
		return MessageInfo.ok(service.doLock(bean));
	}
	/**
	 * 取消锁定主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/doCancelLock")
	public MessageInfo doCancelLock(@RequestBody AwbNumber bean) {
		return MessageInfo.ok(service.doCancelLock(bean));
	}
	/**
	 * 删除主运单
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping(value = "/doDelete")
	@PreAuthorize("@pms.hasPermission('sys_base_awb_del')")
	public MessageInfo doDelete(@RequestBody AwbNumber bean) {
		return MessageInfo.ok(service.doDelete(bean));
	}
	/**
	 * 商户资料类型
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectVCategory")
	public MessageInfo selectVCategory(String category) {
		//运单来源
		return MessageInfo.ok(service.selectVCategory(category));
	}

	/**
	 * 主运单 - 日志
	 *
	 * @return list
	 */
	@GetMapping(value = "/awbLogPage")
	public MessageInfo awbLogPage(Page page, LogBean bean) {
		return MessageInfo.ok(service.awbLogPage(page,bean));
	}

	@PostMapping("/exportExcel")
	public void exportExcel(AwbNumber bean) {
		try {
			service.exportExcel(bean);
		} catch (Exception e) {
			log.info(e.getMessage());
		}
	}
}

