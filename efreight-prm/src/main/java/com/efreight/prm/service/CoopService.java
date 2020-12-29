package com.efreight.prm.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.prm.entity.BillServiceGroup;
import com.efreight.prm.entity.Coop;
import com.efreight.prm.entity.CoopExcel;


public interface CoopService {

	/**
	  * 根据条件查询
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryCoopList(Integer currentPage, Integer pageSize,Map paramMap);

	Map<String,Object> queryCoopListByPage(Integer currentPage, Integer pageSize,Map paramMap);
	
	/**
	  * 根据条件查询树表
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	List<Coop> getTreeList(Coop coop);
	
	
	/**
	  * 根据coop_code查询子公司
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryCoopChildList(Integer currentPage, Integer pageSize,Map paramMap);
	
	/**
	  * 插入新数据
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Integer saveCoop(Coop coop);
	
	/**
	  * 修改数据
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Integer modifyCoop(Coop coop);
	
	/**
	  * 查看单个数据
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Coop viewCoop(Map paramMap);

	/**
	  * 根据coop_code查询子公司
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryListForChoose(Map paramMap);

	/**
	 * 修改黑白名单
	 * @param blackwhiteValid
	 * @param coopId
	 * @param coopCode
	 */
	void modifyBlackWhiteValid(String coopId, String coopCode, String blackWhiteValid, String blackWhiteReason);

	/**
	 * 修改黑白名单
	 * @param blackwhiteValid
	 * @param coopId
	 * @param coopCode
	 */
	void modifyOutBlackWhiteValid(String coopId, String coopCode, String blackWhiteValid, String blackWhiteReason);

	/**
	 * 解锁、开锁
	 * @param
	 * @param
	 * @param coopId
	 * @param coopCode
	 */
	void lockOrUnlock(String coopId, String coopCode, String coopStatus, String lockReason);

	/**
	 * 导出excel
	 * @param bean
	 * @return
	 */
    List<CoopExcel> queryListForExcel(Map paramMap);
    
	/**
	 * 增加分组修改客商资料代码
	 * @param
	 * @param
	 * @param oriCoopCode
	 * @param afCoopCode
	 */
	void modifyCoopCode(String oriCoopCode, String afCoopCode);
	
	/**
	  * 查找和伙伴代码及其子集的代码
	  * @param 
	  * @return List
	  */
	List<Coop> queryCoopCodes(String CoopCode);
	
	/**
	  * 查看单个数据-根据coop_code
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Coop queryCoopCodeByCoop(String CoopCode);

	List<Coop> queryCoopList1(Map paramMap);

	Coop queryCoopCodeByCoopName(String CoopName);

	Coop queryCoopCodeByCoopName1(String CoopName,Integer coopId);

	Coop queryCoopCodeByShortName(String ShortName);

	Coop queryCoopCodeByShortName1(String ShortName,Integer coopId);

	Coop queryCoopCodeByCoopEName(String CoopEName);

	Coop queryCoopCodeByCoopEName1(String CoopEName,Integer coopId);

	Coop queryCoopCodeByShortEName(String ShortEName);

	Coop queryCoopCodeByShortEName1(String ShortEName,Integer coopId);

	Coop queryCoopCodeBySocialCreditCode(String SocialCreditCode);

	Integer isHaveSocialCreditCode(Coop coop);

    Map<String, Object> queryListForChooseForUpdate(Map<String, Object> paramMap);

	List<Coop> listByType(Map<String, Object> paramMap);

    List<Coop> listByCoopName(Map<String, Object> paramMap);

	void importData(List<Coop> data);

	String downloadTemplate();

	Coop getCoopCountByCode(String coopCode,Integer orgId);

	Coop getCoopCountByName(String coopName,Integer orgId);

	//插入新数据（签约公司绑定子公司时调用）
	Integer saveCoop1(Coop coop);

    List<Coop> selectPrmCoopsForAwb(Integer orgId, String bussinessScope);
}
