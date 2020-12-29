package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.UserPageSetMapper;
import com.efreight.hrs.dao.UserRoleMapper;
import com.efreight.hrs.entity.UserPageSet;
import com.efreight.hrs.entity.UserRole;
import com.efreight.hrs.service.UserPageSetService;
import com.efreight.hrs.service.UserRoleService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
public class UserPageSetServiceImpl extends ServiceImpl<UserPageSetMapper, UserPageSet> implements UserPageSetService {

	@Override
	public Boolean removeUserPageSet(UserPageSet userPageSet) {
		// TODO Auto-generated method stub
		Map<String,Object> queryWrapper = new HashMap<String,Object>();
		queryWrapper.put("org_id", userPageSet.getOrgId());
		queryWrapper.put("user_id", userPageSet.getUserId());
		queryWrapper.put("page_name", userPageSet.getPageName());
		baseMapper.deleteByMap(queryWrapper);
		return true;
	}

	@Override
	public List<UserPageSet> listByMap(String pageName) {
		// TODO Auto-generated method stub
		LambdaQueryWrapper<UserPageSet> queryWrapper = Wrappers.<UserPageSet>lambdaQuery();
		queryWrapper.eq(UserPageSet::getOrgId, SecurityUtils.getUser().getOrgId());
		queryWrapper.eq(UserPageSet::getUserId, SecurityUtils.getUser().getId());
		queryWrapper.eq(UserPageSet::getPageName, pageName);
		queryWrapper.orderByAsc(UserPageSet::getFieldNo);
		List<UserPageSet> list = baseMapper.selectList(queryWrapper);
		if(list != null && list.size()>0){
			for (int i = 0; i < list.size(); i++) {
				list.get(i).setIndex(list.get(i).getFieldNo());
			}
		}
		return list;
	}

}
