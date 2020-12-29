package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.AfAwbRoute;

import java.util.List;

public interface AfAwbRouteService extends IService<AfAwbRoute>{
	
	List<AfAwbRoute> queryAfAwbRoute(String awbNumberStr,String[] arrayAwbNumber); 
	
	String inputAfAwbRoute();

	/**
	 * 根据主单号查询数据
	 * @param awbNumber 主单号
	 * @return
	 */
	AfAwbRoute findByAwbNumber(String awbNumber);

	/**
	 * 插入一条数据，如果主单号已经存在则不插入
	 * @param afAwbRoute 实体类
	 * @return 数据ID
	 */
	Integer insert(AfAwbRoute afAwbRoute);


	/**
	 * 保存路由信息
	 * @param awbNumber
	 * @param hawNumber
	 * @param businessScope
	 * @return boolean 是否首次订阅
	 */
	boolean saveRouteInfo(String awbNumber, String hawNumber, String businessScope);

}
