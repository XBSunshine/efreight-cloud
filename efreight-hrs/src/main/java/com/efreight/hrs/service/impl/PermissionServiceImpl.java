package com.efreight.hrs.service.impl;

import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.Permission;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.PermissionMapper;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
@Slf4j
@AllArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final LogService logService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean savePermission(Permission permission) {
        permission.setCreateTime(LocalDateTime.now());
        permission.setCreatorId(SecurityUtils.getUser().getId());
        baseMapper.insert(permission);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("权限管理");
            logBean.setOpInfo("新建权限管理：" + permission.getPermissionName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("新建权限管理成功，保存日志失败");
        }
        return null;
    }

    @Override
    public IPage<Permission> getUserPage(Page page, Permission permission) {
        // TODO Auto-generated method stub
        return baseMapper.selectPage(page, null);
    }

    @Override
    public Permission getPermissionByID(Integer permissionId) {

        return baseMapper.selectByPermissionId(permissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermission(Permission permission) {
        UpdateWrapper<Permission> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("permission_Id", permission.getPermissionId());
        baseMapper.update(permission, updateWrapper);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("权限管理");
            logBean.setOpInfo("修改权限管理：" + permission.getPermissionId());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改权限管理成功，保存日志失败");
        }
        return null;
    }

    @Override
    public List<Permission> getPermissionByRoleID(Integer roleId,Integer orgId) {
        // TODO Auto-generated method stub
        //根据roleID查询其是否为管理员角色
        Integer isAdmin = baseMapper.getRoleByRoleId(roleId);
        if(isAdmin == 1){
            if(roleId == 1){//超级管理员
                return baseMapper.selectByPermissionRoleIdForAdmin(roleId);
            }else{
                return baseMapper.selectByPermissionRoleIdForAdmin1(roleId);
            }
        }else{
            return baseMapper.selectByPermissionRoleId(roleId,orgId);
        }
    }

    @Override
    public List<Permission> getAllPermissionByRoleID(Integer roleId,Integer orgId) {
        // TODO Auto-generated method stub
        //根据roleID查询其是否为管理员角色
        Integer isAdmin = baseMapper.getRoleByRoleId(roleId);
        if(isAdmin == 1){
            if(roleId == 1){//超级管理员
                return baseMapper.selectByPermissionRoleIdForAdmin(roleId);
            }else{
                return baseMapper.selectByPermissionRoleIdForAdmin1(roleId);
            }
        }else{
            return baseMapper.selectByAllPermissionRoleId(roleId,orgId);
        }
    }

    @Override
    public List<Permission> getAllPermissionByRoleID1(String roles,Integer orgId) {
        return baseMapper.selectByAllPermissionRoleId1(roles,orgId);
    }

    @Override
    public List<Permission> getRoleTree() {
        // TODO Auto-generated method stub
        return baseMapper.getRoleTree(SecurityUtils.getUser().getOrgId());
    }
    @Override
    public List<String> getButtonInfo() {
    	List<Integer> roleList=SecurityUtils.getRoles();
    	String roles="";
        String adminFlag="";
    	for (int i = 0; i < roleList.size(); i++) {
            //根据roleID查询其是否为管理员角色
            Integer isAdmin = baseMapper.getRoleByRoleId(roleList.get(i));
            if(isAdmin == 1){
                adminFlag = "1";
            }
			if (roles.length()==0) {
				roles="'"+roleList.get(i)+"'";
			} else {
				roles=roles+",'"+roleList.get(i)+"'";
			}
		}
        if(adminFlag == "1" || "1".equals(adminFlag)){
            if(roles == "1" || "1".equals(roles)){//超级管理员
                return baseMapper.getButtonInfoForAdmin(roles);
            }else{
                return baseMapper.getButtonInfoForAdmin1(roles);
            }
        }else{
            return baseMapper.getButtonInfo(roles,SecurityUtils.getUser().getOrgId());
        }
    }

    @Override
    public List<Permission> getPermissionByOrgID(Integer orgId) {
        // TODO Auto-generated method stub
        return baseMapper.selectByPermissionOrgId(orgId);
    }

    /**
     * 查询所有权限
     *
     * @return
     */
    @Override
    public List<Permission> getPermissionAll() {
        return baseMapper.selectList(Wrappers.emptyWrapper());
    }

    /**
     * 首页设置我的常用
     *
     * @return
     */
    @Override
    public List<Permission> getPermissionTreeForHomePage() {
        ArrayList<Permission> permissions = new ArrayList<>();
        SecurityUtils.getRoles().forEach(roleId -> permissions.addAll(baseMapper.getPermissionTreeForHomePage(SecurityUtils.getUser().getOrgId(), roleId)));
        List<Permission> list = permissions.stream().distinct().sorted(Comparator.comparingInt(Permission::getSort)).collect(Collectors.toList());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeById(Integer permissionId) {
        // TODO Auto-generated method stub
        Map columnMap = new HashMap();
        columnMap.put("permission_Id", permissionId);
        baseMapper.deleteByMap(columnMap);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("权限管理");
            logBean.setOpInfo("删除权限管理：" + permissionId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("删除权限管理成功，保存日志失败");
        }
        return true;
    }

}
