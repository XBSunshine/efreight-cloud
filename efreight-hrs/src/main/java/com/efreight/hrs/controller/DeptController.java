package com.efreight.hrs.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.service.DeptService;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.utils.MenuTreeDept;
import com.efreight.hrs.utils.TreeDeptUtil;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dept")
@Slf4j
public class DeptController {
	private final DeptService deptService;
	private final LogService logService;

	/**
	 * 通过ID查询
	 *
	 * @param id ID
	 * @return MessageInfo
	 */
	@GetMapping("/{id}")
	public MessageInfo getById(@PathVariable Integer id) {
		return MessageInfo.ok(deptService.getDeptByID(id));
	}

	/**
	 * 添加
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	// @SysLog("添加部门")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_dept_add')")
	public MessageInfo save(@Valid @RequestBody Dept dept) {
		//部门或子部门包含用户
//		List<Dept> list=deptService.selectDeptByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));
//		int deptNum=0;
//		for (int i = 0; i < list.size(); i++) {
//			deptNum=deptNum+list.get(i).getBudgetHc();
//		}
//		Dept user=deptService.getUserByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));		
//		if (dept.getBudgetHc()>user.getBudgetHc()-deptNum) {
//			return MessageInfo.failed("子部门预算人数超出预算,不能新建");
//		}
		//名称和简称是否重复
		List<Dept> allNamelist=deptService.checkDeptName(dept.getDeptCode(),dept.getDeptName());
		if (allNamelist.size()>0) {
			return MessageInfo.failed("部门名称已经存在,不能新建");
		}
		List<Dept> shortNamelist=deptService.checkDeptShortName(dept.getDeptCode(),dept.getShortName());
		if (shortNamelist.size()>0) {
			return MessageInfo.failed("部门简称已经存在,不能新建");
		}
		//判断上级是否有末级利润中心
//		int isHave=0;
//		if (dept.getIsFinalProfitunit()) {
//			for (int i = 0; i < dept.getDeptCode().length(); i+=3) {
//	    		String deptCode=dept.getDeptCode().substring(0, dept.getDeptCode().length()-3-i);
//	    		List<Dept> lList=deptService.getlList(deptCode);
//	    		if (lList.size()>0) {
//	    			isHave=1;
//	    			break;
//				}
//			}			
//		}
//		if (isHave==1) {
//			return MessageInfo.failed("部门上级已经存在末级利润中心,不能新建");
//		}
		Log logBean = new Log();
		logBean.setOpLevel("高");
		logBean.setOpType("新建");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("新建部门："+dept.getDeptCode()+" 部门名称："+dept.getDeptName());
		logService.doSave(logBean);
		return MessageInfo.ok(deptService.saveDept(dept));
	}

	/**
	 * 删除
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("删除部门")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_dept_del')")
	public MessageInfo removeById(@PathVariable Integer id) {
		return MessageInfo.ok(deptService.removeDeptById(id));
	}

	/**
	 * 编辑
	 *
	 * @param sysDept 实体
	 * @return success/false
	 */
	// @SysLog("编辑部门")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('sys_dept_edit')")
	public MessageInfo update(@Valid @RequestBody Dept dept) {
		//名称和简称是否重复
		List<Dept> allNamelist=deptService.checkDeptName(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3),dept.getDeptName());
		int a=0;
		if (dept.getDeptName().equals(dept.getDeptNameOld())) {
			a=1;
		}
		int b=0;
		if (dept.getShortName().equals(dept.getShortNameOld())) {
			b=1;
		}
		if (allNamelist.size()>a) {
			return MessageInfo.failed("部门名称已经存在,不能修改");
		}
		List<Dept> shortNamelist=deptService.checkDeptShortName(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3),dept.getShortName());
		if (shortNamelist.size()>b) {
			return MessageInfo.failed("部门简称已经存在,不能修改");
		}
		//部门或子部门包含用户
//		List<Dept> list=deptService.selectDeptByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));
//		int deptNum=0;
//		for (int i = 0; i < list.size(); i++) {
//			if (dept.getDeptId().intValue()==list.get(i).getDeptId().intValue()) {
//				continue;
//			}
//			deptNum=deptNum+list.get(i).getBudgetHc();
//		}
//		Dept user=deptService.getUserByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));		
//		if (user!=null && dept.getBudgetHc()>user.getBudgetHc()-deptNum) {
//			return MessageInfo.failed("子部门预算人数超出预算,不能修改");
//		}
		//校验子级部门预算个数
//		List<Dept> listChild=deptService.selectDeptByDeptCode(dept.getDeptCode());
//		if (listChild.size()>0) {
//			int deptNum2=0;
//			for (int i = 0; i < listChild.size(); i++) {
//				deptNum2=deptNum2+listChild.get(i).getBudgetHc();
//			}
//			if (dept.getBudgetHc()<deptNum2) {
//				return MessageInfo.failed("子部门预算人数超出预算,不能修改");
//			}
//		}
		//判断是否有末级利润中心
//		int isHave=0;
//		if (dept.getIsFinalProfitunit()) {
//			//上级
//			for (int i = 0; i < dept.getDeptCode().length(); i+=3) {
//	    		String deptCode=dept.getDeptCode().substring(0, dept.getDeptCode().length()-3-i);
//	    		List<Dept> lList=deptService.getlList(deptCode);
//	    		if (lList.size()>0) {
//	    			isHave=1;
//	    			break;
//				}
//			}
//			//下级
//			if (isHave==0) {
//				List<Dept> lCList=deptService.getlCList(dept.getDeptCode());
//				if (lCList.size()>0) {
//					isHave=2;
//				}
//			}
//			
//		}
//		if (isHave==1) {
//			return MessageInfo.failed("部门上级已经存在末级利润中心,不能修改");
//		}
//		if (isHave==2) {
//			return MessageInfo.failed("部门下级已经存在末级利润中心,不能修改");
//		}
		Log logBean = new Log();
		logBean.setOpLevel("高");
		logBean.setOpType("修改");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("修改部门："+dept.getDeptCode()+" 部门名称："+dept.getDeptName());
		logService.doSave(logBean);
		
		return MessageInfo.ok(deptService.updateDept(dept));
	}

	/**
	 * 返回树形集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public MessageInfo listDeptTrees() {
		return MessageInfo.ok(deptService.listTrees());
	}
	/**
	 * 返回树形集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/treeinfo")
	public MessageInfo listDeptTrees2(Dept dept) {
//		Set<Dept> all = new LinkedHashSet<>();
//		all.addAll(deptService.getDeptbyOrgid(dept));
//		System.out.println("all:"+all);
//		SecurityUtils.getRoles().forEach(roleId -> all.addAll(permissionService.getPermissionByRoleID(roleId)));
//		List<MenuTreeDept> menuTreeList = all.stream()
//				.filter(menuVo -> true).map(MenuTreeDept::new)
//				.sorted(Comparator.comparingInt(MenuTreeDept::getId)).collect(Collectors.toList());
		
		
		List<Dept> listd =  deptService.getDeptbyOrgid(dept);
//		System.out.println("listd"+listd);
		List<MenuTreeDept> menuTreeList = listd.stream()
				.filter(menuVo -> true).map(MenuTreeDept::new).collect(Collectors.toList());
//				.sorted(Comparator.comparingInt(MenuTreeDept::getId)).collect(Collectors.toList());
		
//		System.out.println(menuTreeList.size()+"   "+menuTreeList.toString());
		
		for (int i = 0; i < menuTreeList.size(); i++) {
			MenuTreeDept bean=menuTreeList.get(i);
			List<User> userList=deptService.queryUserList(bean.getDeptCode());
			if (userList.size()>0) {
				bean.setActualHc(userList.size());
			}else{
				bean.setActualHc(null);
			}
		}
		List<MenuTreeDept> a=TreeDeptUtil.buildByLoop(menuTreeList, -1);
		
		return MessageInfo.ok(a);
	}
	

	/**
	 * 返回树形集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/page")
	public MessageInfo page(Page page,Dept dept) {
		return MessageInfo.ok(deptService.getDeptPage(page, dept));
	}
	
	/**
	 * 返回第一级部门
	 *
	 * @return list
	 */
	@GetMapping(value = "/list")
	public MessageInfo getDeptList(Page page,Dept dept) {
		return MessageInfo.ok(deptService.getDeptList(page, dept));
	}
	/**
	 * 返回下级部门
	 *
	 * @return list
	 */
	@GetMapping(value = "/listc")
	public MessageInfo getDeptListChildren(Dept dept) {
		return MessageInfo.ok(deptService.getDeptListChildren(dept));
	}
	/**
	 * 返回部门
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectList")
	public MessageInfo selectList(Dept dept) {
		return MessageInfo.ok(deptService.selectList(dept));
	}
	/**
	 * 选择部门负责人
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectUser")
	public MessageInfo selectUser(Dept dept) {
		return MessageInfo.ok(deptService.selectUser(dept));
	}
	/**
	 * 查询个人信息设置的默认抄送人
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectOrderTrackCcUser")
	public MessageInfo selectOrderTrackCcUser(UserMailCc userMailCc) {
		return MessageInfo.ok(deptService.selectOrderTrackCcUser(userMailCc));
	}
	/**
	 * 查询个人信息设置的默认抄送人
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectOrderTrackCcUserId")
	public MessageInfo selectOrderTrackCcUserId(UserMailCc userMailCc) {
		return MessageInfo.ok(deptService.selectOrderTrackCcUserId(userMailCc));
	}
	/**
	 * 选择部门负责人
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectUserByCode")
	public MessageInfo selectUserByCode(Dept dept) {
		return MessageInfo.ok(deptService.selectUserByCode(dept));
	}
	/**
	 * 返回当前用户树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/user-tree")
	public MessageInfo listCurrentUserDeptTrees() {
		return MessageInfo.ok(deptService.listCurrentUserTrees());
	}
	
	/**
	 * 删除
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("删除部门")
	@PostMapping(value = "/deleteById")
	@PreAuthorize("@pms.hasPermission('sys_dept_del')")
	public MessageInfo deleteById(@RequestBody Dept dept) {
		List<User> list=deptService.selectUserByDeptId(dept.getDeptCode());
		if (list.size()>0) {
			return MessageInfo.failed("部门或子部门包含用户,不能删除");
		}
		Log logBean = new Log();
		logBean.setOpLevel("高");
		logBean.setOpType("删除");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("删除部门："+dept.getDeptCode()+" 部门名称："+dept.getDeptName());
		logService.doSave(logBean);
		return MessageInfo.ok(deptService.deleteById(dept.getDeptCode(),dept.getIsFinalProfitunit()));
	}
	/**
	 * 停用
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("停用部门")
	@PostMapping(value = "/stopById")
	//@PreAuthorize("@pms.hasPermission('sys_dept_stop')")
	public MessageInfo stopById(@RequestBody Dept dept) {
		List<User> list=deptService.selectUserByDeptId(dept.getDeptCode());
		if (list.size()>0) {
			return MessageInfo.failed("部门或子部门包含用户,不能停用");
		}
		return MessageInfo.ok(deptService.stopById(dept.getDeptCode(),dept.getIsFinalProfitunit()));
	}
	/**
	 * 启用
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("启用部门")
	@PostMapping(value = "/startById")
	//@PreAuthorize("@pms.hasPermission('sys_dept_start)")
	public MessageInfo startById(@RequestBody Dept dept) {
		//部门或子部门包含用户
		List<Dept> list=deptService.selectDeptByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));
		int deptNum=0;
		for (int i = 0; i < list.size(); i++) {
			deptNum=deptNum+list.get(i).getBudgetHc();
		}
		Dept user=deptService.getUserByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));		
		if (dept.getBudgetHc()>user.getBudgetHc()-deptNum) {
			return MessageInfo.failed("子部门预算人数超出预算,不能启用");
		}
		//判断上级是否有末级利润中心
		int isHave=0;
		if (dept.getIsFinalProfitunit()) {
			for (int i = 0; i < dept.getDeptCode().length(); i+=3) {
	    		String deptCode=dept.getDeptCode().substring(0, dept.getDeptCode().length()-3-i);
	    		List<Dept> lList=deptService.getlList(deptCode);
	    		if (lList.size()>0) {
	    			isHave=1;
	    			break;
				}
			}			
		}
		if (isHave==1) {
			return MessageInfo.failed("部门上级已经存在末级利润中心,不能启用");
		}
		return MessageInfo.ok(deptService.startById(dept.getDeptCode(),dept.getIsFinalProfitunit()));
	}
	/**
	 * 移动
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("移动部门")
	@PostMapping(value = "/moveById")
	@PreAuthorize("@pms.hasPermission('sys_dept_move')")
	public MessageInfo moveById(@RequestBody Dept dept) {
		//部门或子部门包含用户
//		//List<Dept> list=deptService.selectDeptByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));
//		List<Dept> list=deptService.selectDeptByDeptCode(dept.getDeptCode());
//		int deptNum=0;
//		for (int i = 0; i < list.size(); i++) {
//			deptNum=deptNum+list.get(i).getBudgetHc();
//		}
//		//Dept user=deptService.getUserByDeptCode(dept.getDeptCode().substring(0, dept.getDeptCode().length()-3));		
		Dept user=deptService.getUserByDeptCode(dept.getDeptCode());		
		Dept user2=deptService.getUserByDeptCode(dept.getDeptCodeSelect());		
//		if (user2.getBudgetHc()>user.getBudgetHc()-deptNum) {
//			return MessageInfo.failed("子部门预算人数超出预算,不能移动");
//		}
		//检验部门层级
		String maxDeptCode=deptService.getAllMaxDeptCode(dept.getDeptCodeSelect());
		int length=3+maxDeptCode.length()-dept.getDeptCodeSelect().length()+dept.getDeptCode().length();
		if (length>15) {
			return MessageInfo.failed("部门层级超出预算,不能移动");
		}
		//名称和简称是否重复
		List<Dept> allNamelist=deptService.checkDeptName(dept.getDeptCode(),user2.getDeptName());
		if (allNamelist.size()>0) {
			return MessageInfo.failed("部门名称已经存在,不能移动");
		}
		List<Dept> shortNamelist=deptService.checkDeptShortName(dept.getDeptCode(),user2.getShortName());
		if (shortNamelist.size()>0) {
			return MessageInfo.failed("部门简称已经存在,不能移动");
		}
		
		//判断上级是否有末级利润中心
//		int isHave=0;	
//		List<Dept> lCList=deptService.getlCList2(dept.getDeptCodeSelect());
//		if (lCList.size()>0) {
//			for (int i = 0; i < dept.getDeptCode().length(); i+=3) {
//	    		String deptCode=dept.getDeptCode().substring(0, dept.getDeptCode().length()-i);
//	    		List<Dept> lList=deptService.getlList(deptCode);
//	    		if (lList.size()>0) {
//	    			isHave=1;
//	    			break;
//				}
//			}
//		}
//		if (isHave==1) {
//			return MessageInfo.failed("部门上级已经存在末级利润中心,不能移动");
//		}
		Log logBean = new Log();
		logBean.setOpLevel("高");
		logBean.setOpType("移动");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("移动部门："+user2.getDeptCode()+" 部门名称："+user2.getDeptName());
		logService.doSave(logBean);
		return MessageInfo.ok(deptService.moveById(dept.getDeptCode(),dept.getDeptCodeSelect(),user2.getIsFinalProfitunit(),user2.getFullName(),user.getFullName()));
	}
	/**
	 * 合并
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("合并部门")
	@PostMapping(value = "/mergeById")
	@PreAuthorize("@pms.hasPermission('sys_dept_merge')")
	public MessageInfo mergeById(@RequestBody Dept dept) {
		//部门或子部门包含用户
//		List<User> list=deptService.selectUserByDeptId(dept.getDeptCode());
//		List<User> listMerge=deptService.selectUserByDeptId(dept.getDeptCodeSelect());	
//		if (listMerge.size()>dept.getBudgetHc()-list.size()) {
//			return MessageInfo.failed("子部门预算人数超出预算,不能合并");
//		}
		Log logBean = new Log();
		logBean.setOpLevel("高");
		logBean.setOpType("合并");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("合并部门："+dept.getDeptCode()+" 部门名称："+dept.getDeptName());
		logService.doSave(logBean);
		return MessageInfo.ok(deptService.mergeById(dept.getDeptCode(),dept.getDeptCodeSelect(),dept.getIsFinalProfitunit()));
	}
	/**
	 * 排序
	 *
	 * @param id ID
	 * @return success/false
	 */
	// @SysLog("排序部门")
	@PostMapping(value = "/sortById")
	@PreAuthorize("@pms.hasPermission('sys_dept_sort')")
	public MessageInfo sortById(@RequestBody Map map) {
		Log logBean = new Log();
		logBean.setOpLevel("高");
		logBean.setOpType("排序");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("排序部门："+map.get("deptCode").toString());
		logService.doSave(logBean);
		return MessageInfo.ok(deptService.sortById(map.get("deptCode").toString(),map.get("deptCode3").toString()));
	}
	/**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
	@PostMapping("/exportExcel")
	@PreAuthorize("@pms.hasPermission('sys_dept_export')")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") Dept bean) throws IOException {

        List<DeptExcel> list = deptService.queryListForExcel(bean);
        Log logBean = new Log();
		logBean.setOpLevel("低");
		logBean.setOpType("导出");
		logBean.setOpName("部门管理");
		logBean.setOpInfo("导出部门，大小为"+list.size());
		logService.doSave(logBean);
        //导出日志数据
        ExportExcel<DeptExcel> ex = new ExportExcel<DeptExcel>();
        String[] headers = {"部门名称", "部门编号","部门简称", "部门全称","负责人", "是否利润中心", "是否末端利润中心", "预算人数", "实际人数", "状态"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
    }
}
