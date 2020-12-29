
package com.efreight.prm.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanghw
 */
@Data
public class TreeNodeCoop {
	protected int id;
	protected int parentId;
	protected String  code;
	protected List<TreeNodeCoop> children = new ArrayList<TreeNodeCoop>();

	public void add(TreeNodeCoop node) {
		children.add(node);
	}
}
