package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.OrgMapper;
import com.efreight.hrs.dao.OrgPermissionMapper;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.OrgPermission;
import com.efreight.hrs.pojo.org.OrgQuery;
import com.efreight.hrs.pojo.org.OrgVO;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.OrgPermissionService;
import com.efreight.hrs.service.OrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
public class OrgPermissionServiceImpl extends ServiceImpl<OrgPermissionMapper, OrgPermission> implements OrgPermissionService {

    @Resource
    private  CacheManager cacheManager;
    @Resource
    private  LogService logService;
    @Resource
    private  OrgService orgService;
    @Resource
    private  OrgMapper orgMapper;


    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Boolean saveOrgPermission(Map<String, Object> para) {
        Integer orgId = Integer.parseInt(para.get("orgId").toString());
        String menuIds = para.get("permissionIds").toString().replaceAll("\\[", "").replaceAll("\\]", "");

        //存签约客户权限hrs_org_permission
        this.remove(Wrappers.<OrgPermission>query().lambda().eq(OrgPermission::getOrgId, orgId));//删除之前的权限
        if (StrUtil.isBlank(menuIds)) {
            return Boolean.TRUE;
        }
        List<OrgPermission> orgMenuList = Arrays.stream(menuIds.split(",")).map(menuId -> {
            OrgPermission orgMenu = new OrgPermission();
            orgMenu.setPermissionId(Integer.valueOf(menuId));
            orgMenu.setOrgId(orgId);
            orgMenu.setCreatorId(SecurityUtils.getUser().getId());
            orgMenu.setCreateTime(LocalDateTime.now());
            orgMenu.setPermissionStatus(true);
            return orgMenu;
        }).collect(Collectors.toList());
        // 清空userinfo
        System.out.println(orgMenuList.size());


        //存签约客户管理员角色权限hrs_role_permission
//		rolePermissionService.remove(Wrappers.<RolePermission>query().lambda().eq(RolePermission::getRoleId, roleId)
//				.eq(RolePermission::getOrgId, orgId));
//		if (StrUtil.isBlank(menuIds)) {
//			return Boolean.TRUE;
//		}
//		List<RolePermission> roleMenuList = Arrays.stream(menuIds.split(",")).map(menuId -> {
//			RolePermission roleMenu = new RolePermission();
//			roleMenu.setRoleId(roleId);
//			roleMenu.setPermissionId(Integer.valueOf(menuId));
//			roleMenu.setOrgId(orgId);
//			roleMenu.setCreateTime(LocalDateTime.now());
//			return roleMenu;
//		}).collect(Collectors.toList());
        // 清空userinfo
//		System.out.println(roleMenuList.size());
//		return rolePermissionService.saveBatch(roleMenuList);

        cacheManager.getCache("user_details").clear();
        this.saveBatch(orgMenuList);
        Org org = orgService.getOrgByID(orgId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("配置权限");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("给签约客户：" + org.getOrgName() + "配置权限");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("签约客户授权成功，日志添加失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Boolean saveSignTemplateAndPermission(Map<String, Object> para) {
        String orgName = para.get("orgName").toString();
        Integer orgUserCount = Integer.parseInt(para.get("orgUserCount").toString());
        String orgStatus = para.get("orgStatus").toString();
        //向hrs_org插入一条记录并返回主键org_id
        Org org = new Org();
        org.setOrgType(0);
        org.setOrgName(orgName);
        org.setOrgEname(orgName);
        if(orgStatus == "1" || "1".equals(orgStatus)){
            org.setOrgStatus(true);
        }else{
            org.setOrgStatus(false);
        }
        org.setOrgUserCount(orgUserCount);
        org.setCreateTime(LocalDateTime.now());
        org.setCreatorId(SecurityUtils.getUser().getId());
        orgMapper.insertOrg(org);
        Integer orgId = org.getOrgId();

        String menuIds = para.get("permissionIds").toString().replaceAll("\\[", "").replaceAll("\\]", "");

        //存签约模板权限hrs_org_permission
        this.remove(Wrappers.<OrgPermission>query().lambda().eq(OrgPermission::getOrgId, orgId));//删除之前的权限
        if (StrUtil.isBlank(menuIds)) {
            return Boolean.TRUE;
        }
        List<OrgPermission> orgMenuList = Arrays.stream(menuIds.split(",")).map(menuId -> {
            OrgPermission orgMenu = new OrgPermission();
            orgMenu.setPermissionId(Integer.valueOf(menuId));
            orgMenu.setOrgId(orgId);
            orgMenu.setCreatorId(SecurityUtils.getUser().getId());
            orgMenu.setCreateTime(LocalDateTime.now());
            orgMenu.setPermissionStatus(true);
            return orgMenu;
        }).collect(Collectors.toList());
        // 清空userinfo
        System.out.println(orgMenuList.size());

        cacheManager.getCache("user_details").clear();
        this.saveBatch(orgMenuList);
        Org org1 = orgService.getOrgByID(orgId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("配置权限");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("给签约客户：" + org1.getOrgName() + "配置权限");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("签约客户授权成功，日志添加失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Boolean editSignTemplateAndPermission(Map<String, Object> para) {
        String orgName = para.get("orgName").toString();
        Integer orgUserCount = Integer.parseInt(para.get("orgUserCount").toString());
        String orgStatus = para.get("orgStatus").toString();
        Integer orgId = Integer.parseInt(para.get("orgId").toString());
        //根据org_id修改hrs_org
        Org org = new Org();
        org.setOrgId(orgId);
        org.setOrgName(orgName);
        org.setOrgEname(orgName);
        if(orgStatus == "1" || "1".equals(orgStatus)){
            org.setOrgStatus(true);
        }else{
            org.setOrgStatus(false);
        }
        org.setOrgUserCount(orgUserCount);
        org.setEditTime(LocalDateTime.now());
        org.setEditorId(SecurityUtils.getUser().getId());
        orgMapper.updateById(org);

        String menuIds = para.get("permissionIds").toString().replaceAll("\\[", "").replaceAll("\\]", "");

        //存签约模板权限hrs_org_permission
        this.remove(Wrappers.<OrgPermission>query().lambda().eq(OrgPermission::getOrgId, orgId));//删除之前的权限
        if (StrUtil.isBlank(menuIds)) {
            return Boolean.TRUE;
        }
        List<OrgPermission> orgMenuList = Arrays.stream(menuIds.split(",")).map(menuId -> {
            OrgPermission orgMenu = new OrgPermission();
            orgMenu.setPermissionId(Integer.valueOf(menuId));
            orgMenu.setOrgId(orgId);
            orgMenu.setCreatorId(SecurityUtils.getUser().getId());
            orgMenu.setCreateTime(LocalDateTime.now());
            orgMenu.setPermissionStatus(true);
            return orgMenu;
        }).collect(Collectors.toList());
        // 清空userinfo
        System.out.println(orgMenuList.size());

        cacheManager.getCache("user_details").clear();
        this.saveBatch(orgMenuList);
        Org org1 = orgService.getOrgByID(orgId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("配置权限");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("给签约客户：" + org1.getOrgName() + "配置权限");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("签约客户授权成功，日志添加失败");
        }
        return true;
    }

    @Override
    public boolean save(Integer permissionId, Set<Integer> orgIdList) {
        Assert.notNull(permissionId, "非法参数");
        Assert.notEmpty(orgIdList, "非法参数");

        List<OrgPermission> batchList = new ArrayList<>();
        Integer creatorId = SecurityUtils.getUser().getId();
        orgIdList.stream().forEach((item)->{
            batchList.add(
                new OrgPermission()
                .setPermissionId(permissionId)
                .setOrgId(item)
                .setPermissionStatus(true)
                .setCreatorId(creatorId)
                .setCreateTime(LocalDateTime.now())
            );
        });
        return saveBatch(batchList);
    }

    @Override
    public boolean remove(Integer permissionId, Set<Integer> orgIdList) {
        Assert.notNull(permissionId, "非法参数");
        Assert.notEmpty(orgIdList, "非法参数");

        LambdaUpdateWrapper<OrgPermission> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(OrgPermission::getPermissionId, permissionId);
        updateWrapper.in(OrgPermission::getOrgId, orgIdList);
        return remove(updateWrapper);
    }

    @Override
    public IPage<OrgVO> getEqPermissionOrgVoPage(OrgQuery orgQuery) {
        return this.orgMapper.getEqPermissionOrgVoPage(new Page<>(orgQuery.getCurrent(), orgQuery.getSeize()), orgQuery);
    }

    @Override
    public IPage<OrgVO> getNePermissionOrgVoPage(OrgQuery orgQuery) {
        return this.orgMapper.getNePermissionOrgVoPage(new Page<>(orgQuery.getCurrent(), orgQuery.getSeize()), orgQuery);
    }

}
