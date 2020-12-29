package com.efreight.hrs.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

import com.efreight.hrs.entity.Dept;
import com.efreight.hrs.entity.Permission;

/**
 * @author zhanghw
 */


@UtilityClass
public class TreeDeptUtil {
	/**
	 * 两层循环实现建树
	 *
	 * @param treeNodes 传入的树节点列表
	 * @return
	 */
	public <T extends TreeNodeDept> List<T> buildByLoop(List<T> treeNodes, Object root) {

		List<T> trees = new ArrayList<>();
		
		for (T t1 : treeNodes) {//第一级
			if (t1.getCode().length()==3) {
				trees.add(t1);
			}
			for (T t2 : treeNodes) {//第二级
				if(t2.getCode().length()<=3)
					continue;
				if (t2.getCode().substring(0, 3).equals(t1.getCode())) {
					if (t1.getChildren() == null) {
						t1.setChildren(new ArrayList<>());
					}
					if(t2.getCode().length()==6){
						t1.add(t2);
						for (T t3 : treeNodes) {//第三级
							if(t3.getCode().length()<=6)
								continue;
							if (t3.getCode().substring(0, 6).equals(t2.getCode())) {
								if (t2.getChildren() == null) {
									t2.setChildren(new ArrayList<>());
								}
								if(t3.getCode().length()==9){
									t2.add(t3);
									for (T t4 : treeNodes) {//第四级
										if(t4.getCode().length()<=9)
											continue;
										if (t4.getCode().substring(0, 9).equals(t3.getCode())) {
											if (t3.getChildren() == null) {
												t3.setChildren(new ArrayList<>());
											}
											if(t4.getCode().length()==12){
												t3.add(t4);
												for (T t5 : treeNodes) {//第5级
													if(t5.getCode().length()<=12)
														continue;
													if (t5.getCode().substring(0, 12).equals(t4.getCode())) {
														if (t4.getChildren() == null) {
															t4.setChildren(new ArrayList<>());
														}
														if(t5.getCode().length()==15){
															t4.add(t5);
														}
													}
												}
											}
										}
									}
								
								}
							}
						}
					
					}
					
					
				}
			}
		}
		return trees;
	}

	/**
	 * 使用递归方法建树
	 *
	 * @param treeNodes
	 * @return
	 */
	public <T extends TreeNodeDept> List<T> buildByRecursive(List<T> treeNodes, Object root) {
		List<T> trees = new ArrayList<T>();
		for (T treeNode : treeNodes) {
			if (treeNode.getCode().length()==3) {
				trees.add(findChildren(treeNode, treeNodes));
			}
		}
		return trees;
	}

	/**
	 * 递归查找子节点
	 *
	 * @param treeNodes
	 * @return
	 */
	public <T extends TreeNodeDept> T findChildren(T treeNode, List<T> treeNodes) {
		for (T it : treeNodes) {
			if (treeNode.getId() == it.getParentId()) {
				if (treeNode.getChildren() == null) {
					treeNode.setChildren(new ArrayList<>());
				}
				treeNode.add(findChildren(it, treeNodes));
			}
		}
		return treeNode;
	}

	/**
	 * 通过sysMenu创建树形节点
	 *
	 * @param menus
	 * @param root
	 * @return
	 */
	public List<MenuTreeDept> buildTree(List<Dept> menus, int root) {
		List<MenuTreeDept> trees = new ArrayList<>();
		MenuTreeDept node;
		for (Dept menu : menus) {
			node = new MenuTreeDept();
			node.setId(menu.getDeptId());
			node.setDeptCode(menu.getDeptCode());
			node.setDeptName(menu.getDeptName());
			node.setShortName(menu.getShortName());
			node.setFullName(menu.getFullName());
			node.setManagerId(menu.getManagerId());
			node.setIsProfitunit(menu.getIsProfitunit());
			node.setIsFinalProfitunit(menu.getIsFinalProfitunit());
			node.setBudgetHc(menu.getBudgetHc());
			node.setCreatorId(menu.getCreatorId());
			node.setCreateTime(menu.getCreateTime());
			node.setEditorId(menu.getEditorId());
			node.setEditTime(menu.getEditTime());
			node.setStopDate(menu.getStopDate());
			node.setStopId(menu.getStopId());
			node.setOrgId(menu.getOrgId());
			node.setDeptStatus(menu.getDeptStatus());
			node.setManagerName(menu.getManagerName());
			node.setActualHc(menu.getActualHc());
			trees.add(node);
		}
		return TreeDeptUtil.buildByLoop(trees, root);
	}
}