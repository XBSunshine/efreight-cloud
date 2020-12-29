package com.efreight.prm.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

import com.efreight.prm.entity.Coop;


/**
 * @author wangxx
 */


@UtilityClass
public class TreeCoopUtil {
	/**
	 * 两层循环实现建树
	 *
	 * @param treeNodes 传入的树节点列表
	 * @return
	 */
	public <T extends TreeNodeCoop> List<T> buildByLoop(List<T> treeNodes, Object root) {

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
	public <T extends TreeNodeCoop> List<T> buildByRecursive(List<T> treeNodes, Object root) {
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
	public <T extends TreeNodeCoop> T findChildren(T treeNode, List<T> treeNodes) {
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
	public List<MenuTreeCoop> buildTree(List<Coop> menus, int root) {
		List<MenuTreeCoop> trees = new ArrayList<>();
		MenuTreeCoop node;
		for (Coop menu : menus) {
			node = new MenuTreeCoop();
			node.setId(menu.getCoop_id());
			node.setCoop_id(menu.getCoop_id());
			node.setCoop_code(menu.getCoop_code());
			node.setCoop_id(menu.getCoop_id());
			node.setCoop_code(menu.getCoop_code());
			node.setCoop_type(menu.getCoop_type());
			node.setCoop_mnemonic(menu.getCoop_mnemonic());
			node.setCoop_name(menu.getCoop_name());
			node.setShort_name(menu.getShort_name());
			node.setCoop_ename(menu.getCoop_ename());
			node.setShort_ename(menu.getShort_ename());
			node.setSocial_credit_code(menu.getSocial_credit_code());
			node.setBank_name(menu.getBank_name());
			node.setBank_number(menu.getBank_number());
			node.setPhone_number(menu.getPhone_number());
			node.setCoop_address(menu.getCoop_address());
			node.setCoop_remark(menu.getCoop_remark());
			node.setLock_date(menu.getLock_date());
			node.setLock_reason(menu.getLock_reason());
			node.setBlacklist_date(menu.getBlacklist_date());
			node.setBlacklist_reason(menu.getBlacklist_reason());
			node.setWhitelist_date(menu.getWhitelist_date());
			node.setWhitelist_reason(menu.getWhitelist_reason());
			node.setCreator_id(menu.getCreator_id());
			node.setCreate_time(menu.getCreate_time());
			node.setEditor_id(menu.getEditor_id());
			node.setEdit_time(menu.getEdit_time());
			node.setOrg_id(menu.getOrg_id());
			node.setDept_id(menu.getDept_id());
			node.setCoop_status(menu.getCoop_status());
			node.setWhite_valid(menu.getWhite_valid());
			node.setBlack_valid(menu.getBlack_valid());
			node.setCredit_level(menu.getCredit_level());
			node.setContacts_name(menu.getContacts_name());
			node.setGroup_type(menu.getGroup_type());

			trees.add(node);
		}
		return TreeCoopUtil.buildByLoop(trees, root);
	}
}