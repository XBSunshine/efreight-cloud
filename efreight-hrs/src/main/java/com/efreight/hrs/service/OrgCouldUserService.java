package com.efreight.hrs.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.OrgCouldUser;

/**
 *  云用户统计
 * @author Administrator
 *
 */
public interface OrgCouldUserService extends IService<OrgCouldUser>{
	
	IPage<OrgCouldUser> queryCouldUser(Page page,OrgCouldUser user);
	
	void exportExcelList(OrgCouldUser user);

}
