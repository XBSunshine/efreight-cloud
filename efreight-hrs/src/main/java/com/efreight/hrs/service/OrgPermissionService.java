package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.OrgPermission;
import com.efreight.hrs.pojo.org.OrgQuery;
import com.efreight.hrs.pojo.org.OrgVO;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface OrgPermissionService extends IService<OrgPermission> {
	public Boolean saveOrgPermission(Map<String, Object> para) ;

	public Boolean saveSignTemplateAndPermission(Map<String, Object> para) ;

	public Boolean editSignTemplateAndPermission(Map<String, Object> para) ;

	/**
	 * 添加权限到某些企业
	 * @param orgIdList 企业ID集合
	 * @param permissionId 权限ID
	 * @return
	 */
	boolean save(Integer permissionId, Set<Integer> orgIdList);

	/**
	 * 移除权限从某些企业上
	 * @param orgIdList 企业ID集合
	 * @param permissionId 权限ID
	 * @return
	 */
	boolean remove(Integer permissionId, Set<Integer> orgIdList );

	/**
	 * 查询含有某个权限的企业数据
	 * @param orgQuery 查询条件
	 * @return
	 */
	IPage<OrgVO> getEqPermissionOrgVoPage(OrgQuery orgQuery);

	/**
	 * 查询不含有某个权限的企业数据
	 * @param orgQuery 查询条件
	 * @return
	 */
	IPage<OrgVO> getNePermissionOrgVoPage(OrgQuery orgQuery);
}
