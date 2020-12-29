package com.efreight.hrs.service.impl;

import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.RolePermission;
import com.efreight.hrs.dao.RolePermissionMapper;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.RolePermissionService;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Slf4j
@Service
@AllArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>
        implements RolePermissionService {
    private final CacheManager cacheManager;
    private final LogService logService;

    @Override
    public Boolean saveRolePermission(Integer orgId, Integer roleId, String menuIds) {
        this.remove(Wrappers.<RolePermission>query().lambda().eq(RolePermission::getRoleId, roleId)
                .eq(RolePermission::getOrgId, orgId));
        if (StrUtil.isBlank(menuIds)) {
            return Boolean.TRUE;
        }
        List<RolePermission> roleMenuList = Arrays.stream(menuIds.split(",")).map(menuId -> {
            RolePermission roleMenu = new RolePermission();
            roleMenu.setRoleId(roleId);
            roleMenu.setPermissionId(Integer.valueOf(menuId));
            roleMenu.setOrgId(orgId);
            roleMenu.setCreateTime(LocalDateTime.now());
            return roleMenu;
        }).collect(Collectors.toList());
        // 清空userinfo
        cacheManager.getCache("user_details").clear();
        System.out.println(roleMenuList.size());
        this.saveBatch(roleMenuList);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("分配权限");
            logBean.setOpName("角色管理");
            logBean.setOpInfo("角色管理分配权限");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("角色管理分配权限成功，日志添加失败");
        }
        return true;


    }

}
