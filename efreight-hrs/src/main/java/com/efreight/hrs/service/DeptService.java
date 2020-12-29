package com.efreight.hrs.service;

import com.efreight.hrs.entity.*;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface DeptService extends IService<Dept> {
	Boolean saveDept(Dept dept);

	IPage<Dept> getDeptPage(Page page, Dept Dept);
	
	IPage<Dept> getDeptList(Page page, Dept Dept);
	List<Dept> getDeptListChildren(Dept Dept);
	List<Dept> selectList(Dept Dept);
	List<User> selectUserByDeptId(String deptCode);
	List<Dept> selectDeptByDeptCode(String deptCode);
	List<Dept> getlList(String deptCode);
	List<Dept> getlCList(String deptCode);
	List<Dept> getlCList2(String deptCode);
	List<Dept> checkDeptName(String deptCode,String deptName);
	List<Dept> checkDeptShortName(String deptCode,String deptShortName);
	List<Map<String, Object>> selectUser(Dept dept);
	List<Map<String, Object>> selectUserByCode(Dept dept);

	Dept getDeptByID(Integer deptId);
	Dept getUserByDeptCode(String deptCode);
	String getAllMaxDeptCode(String deptCode);

	Boolean updateDept(Dept dept);
	
	Boolean removeDeptById(Integer orgId);
	Boolean stopById(String deptCode,Boolean isFinalProfitunit);
	Boolean deleteById(String deptCode,Boolean isFinalProfitunit);
	Boolean startById(String deptCode,Boolean isFinalProfitunit);
	Boolean sortById(String deptCode,String deptCode3);
	Boolean mergeById(String deptCode,String deptCodeSelect,Boolean isFinalProfitunit);
	Boolean moveById(String deptCode,String deptCodeSelect,Boolean isFinalProfitunit,String oldFullName,String newFullName);

	List<Dept> listTrees();

	List<Dept> listCurrentUserTrees();
	
	List<Dept> getDeptbyOrgid(Dept Dept);
	
	List<DeptExcel> queryListForExcel(Dept bean);

	List<User> queryUserList(String deptCode);

	List<String> selectOrderTrackCcUser(UserMailCc userMailCc);

	List<Integer> selectOrderTrackCcUserId(UserMailCc userMailCc);
}
