
package com.efreight.hrs.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanghw
 */
@Data
public class TreeNodeDept {
	protected int id;
	protected int parentId;
	protected String  code;
	protected List<TreeNodeDept> children = new ArrayList<TreeNodeDept>();

	public void add(TreeNodeDept node) {
		children.add(node);
	}
}
