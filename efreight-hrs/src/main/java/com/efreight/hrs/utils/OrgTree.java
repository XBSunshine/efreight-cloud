package com.efreight.hrs.utils;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrgTree extends TreeNode {
	private String name;
}
