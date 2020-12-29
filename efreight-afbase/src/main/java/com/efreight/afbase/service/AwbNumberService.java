package com.efreight.afbase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.AwbNumber;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.LogBean;


public interface AwbNumberService extends IService<AwbNumber> {

	IPage<AwbNumber> getListPage(Page page, AwbNumber bean);
	IPage<AwbNumber> getSelectListPage(Page page, AwbNumber bean);
	List<AwbNumber> queryList(AwbNumber bean);
	List<Map<String, Object>> selectCategory(String category);
	List<Map<String, Object>> selectCategory2(String category,String businessScope);
	List<Map<String, Object>> selectCategoryPro(String category,String departureStation);
	List<Map<String, Object>> selectVCategory(String category);
	List<Map<String, Object>> selectWarehouse(String warehouse);
	List<Map<String, Object>> selectCarrier(String awb3);
	Boolean saveAwbNumber(AwbNumber bean, ArrayList<Map<String, Object>> al);
	Boolean bookAwbList(AwbNumber bean);
	Boolean cancelBook(AwbNumber bean);
	Boolean doLock(AwbNumber bean);
	Boolean doCancelLock(AwbNumber bean);
	Boolean doDelete(AwbNumber bean);
	
	List<AwbNumber> selectOneYearAwbList(ArrayList<String> al);
	List<AwbNumber> selectTwoYearAwbList(ArrayList<String> al);

	IPage<LogBean> awbLogPage(Page page, LogBean bean);

	void exportExcel(AwbNumber income);
	
}
