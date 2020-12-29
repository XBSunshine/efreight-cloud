package com.efreight.hrs.service.impl;

import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.Role;
import com.efreight.hrs.entity.RolePermission;
import com.efreight.hrs.entity.User;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.RoleMapper;
import com.efreight.hrs.dao.RolePermissionMapper;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.RolePermissionService;
import com.efreight.hrs.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
@Slf4j
@Service
@AllArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    private RolePermissionMapper rolePermissionMapper;
    private final LogService logService;
    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRole(Role role) {
        role.setIsadmin(false);
        role.setCreateTime(LocalDateTime.now());
        role.setCreatorId(SecurityUtils.getUser().getId());
        role.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(role);
        //保存权限
        try {
            rolePermissionService.saveRolePermission(SecurityUtils.getUser().getOrgId(),role.getRoleId(),role.getPermissionIds());
        } catch (Exception e) {
            log.info("新建角色成功，保存权限失败");
        }
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("角色管理");
            logBean.setOpInfo("新建角色" + role.getRoleName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("新建角色成功，日志添加失败");
        }
        return true;
    }

    @Override
    public IPage<Role> getRolePage(Page page, Role role) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        if (role.getRoleStatus() != null) {
            queryWrapper.eq("role_Status", role.getRoleStatus());
        }
        if (role.getRoleName() != null && !"".equals(role.getRoleName())) {
            queryWrapper.like("role_name", role.getRoleName());
        }
        queryWrapper.eq("isadmin", false);
        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.orderByDesc("role_id");
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Role getRoleByID(Integer roleId) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_Id", roleId);
        queryWrapper.eq("role_Status", 1);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Role getRoleByID1(Integer roleId) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_Id", roleId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean updateRoleById(Role role) {
        role.setIsadmin(false);
        role.setEditorId(SecurityUtils.getUser().getId());
        role.setEditTime(LocalDateTime.now());
        baseMapper.updateById(role);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("角色管理");
            logBean.setOpInfo("修改角色" + role.getRoleName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改角色成功，日志添加失败");
        }
        return true;
    }

    /**
     * 通过角色ID，删除角色,并清空角色菜单缓存
     *
     * @param id
     * @return
     */
    @Override
    @CacheEvict(value = "menu_details", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeRoleById(Integer id) {
        rolePermissionMapper.delete(Wrappers.<RolePermission>update().lambda().eq(RolePermission::getRoleId, id));
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("角色管理");
            logBean.setOpInfo("删除角色" + id);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("删除角色成功，日志添加失败");
        }
        return this.removeById(id);
    }

    @Override
    public List<Role> listRolesByUserId(Integer userId) {
        // TODO Auto-generated method stub
        return baseMapper.listRolesByUserId(userId);
    }

    @Override
    public IPage<Role> page(IPage<Role> page) {
        QueryWrapper<Role> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.eq("isadmin", false);
        roleQueryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        return super.page(page, roleQueryWrapper);
    }

    @Override
    public List<Role> list() {
        QueryWrapper<Role> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.eq("isadmin", false);
        roleQueryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        roleQueryWrapper.eq("role_status", 1);
        return super.list(roleQueryWrapper);
    }
}
