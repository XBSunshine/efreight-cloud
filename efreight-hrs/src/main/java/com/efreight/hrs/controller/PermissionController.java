package com.efreight.hrs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.constant.CommonConstants;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.Permission;
import com.efreight.hrs.service.PermissionService;
import com.efreight.hrs.utils.MenuTree;
import com.efreight.hrs.utils.TreeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/permission")
@Slf4j
public class PermissionController {
    private final PermissionService permissionService;

    /**
     * 返回当前用户的树形菜单集合
     *
     * @return 当前用户的树形菜单
     */
    @GetMapping
    public MessageInfo getUserMenu() {
        // 获取符合条件的菜单
        Set<Permission> all = new HashSet<>();
        SecurityUtils.getRoles().forEach(roleId -> all.addAll(permissionService.getPermissionByRoleID(roleId,SecurityUtils.getUser().getOrgId())));
        List<MenuTree> menuTreeList = all.stream()
                .filter(menuVo -> CommonConstants.MENU.equals(menuVo.getPermissionType()) || "1".equals(menuVo.getPermissionType())).map(MenuTree::new)
                .sorted(Comparator.comparingInt(MenuTree::getSort)).collect(Collectors.toList());
//		System.out.println(menuTreeList.size()+"   "+menuTreeList.toString());

        List<MenuTree> a = TreeUtil.buildByLoop(menuTreeList, -1);

//		
//		
//		ObjectMapper om=new ObjectMapper();
//		try {
//			String res=om.writeValueAsString(om);
//			System.out.println(res);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        return MessageInfo.ok(a);

    }

    @GetMapping(value = "/getAllPermissionAndHave")
    public MessageInfo getAllPermission() {
        // 获取除了超级管理员拥有的所有菜单
        Set<Permission> all = new HashSet<>();
        List<Integer> roleList=SecurityUtils.getRoles();
        String roles="";
        for (int i = 0; i < roleList.size(); i++) {
            if (roles.length()==0) {
                roles="'"+roleList.get(i)+"'";
            } else {
                roles=roles+",'"+roleList.get(i)+"'";
            }
        }
        all.addAll(permissionService.getAllPermissionByRoleID1(roles,SecurityUtils.getUser().getOrgId()));
        //SecurityUtils.getRoles().forEach(roleId -> all.addAll(permissionService.getAllPermissionByRoleID(roleId,SecurityUtils.getUser().getOrgId())));
        List<MenuTree> menuTreeList = all.stream()
                .filter(menuVo -> CommonConstants.MENU.equals(menuVo.getPermissionType()) || "1".equals(menuVo.getPermissionType())).map(MenuTree::new)
                .sorted(Comparator.comparingInt(MenuTree::getSort)).collect(Collectors.toList());
        List<MenuTree> a = TreeUtil.buildByLoop(menuTreeList, -1);
        /*MenuTree mt = new MenuTree();
        mt.setPath("/unPermission");
        a.add(mt);*/
        /*MenuTree mt1 = new MenuTree();
        mt1.setPath("/unDevelop");
        a.add(mt1);*/
        return MessageInfo.ok(a);

    }

    @GetMapping(value = "/getButtonInfo")
    public MessageInfo getButtonInfo() {
       
        List<String> buttonInfo = permissionService.getButtonInfo();

        return MessageInfo.ok(buttonInfo);

    }

    /**
     * 返回树形菜单集合
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/tree")
    public MessageInfo getTree() {
    	QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
    	queryWrapper.orderByAsc("sort");
        return MessageInfo.ok(TreeUtil.buildTree(permissionService.list(queryWrapper), -1));
    }

    /**
     * 返回树形菜单集合
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/tree1ForSelectAll")
    public List tree1ForSelectAll() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("disabled", "false").orderByAsc("sort");
        return permissionService.list(queryWrapper).stream().map(Permission::getPermissionId).collect(Collectors.toList());
    }

    @GetMapping(value = "/tree1")
    public MessageInfo getTree1() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("disabled", "false").orderByAsc("sort");
        return MessageInfo.ok(TreeUtil.buildTree(permissionService.list(queryWrapper), -1));
    }

    /**
     * 返回树形菜单集合，超级管理员给签约客户分配权限查此方法
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/treec/{orgId}")
    public MessageInfo getTreec(@PathVariable Integer orgId) {
    	QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("disabled", "false").or().in("able_Orgid", orgId).orderByAsc("sort");
        return MessageInfo.ok(TreeUtil.buildTree(permissionService.list(queryWrapper), -1));
    }

    @GetMapping(value = "/treecForSelectAll/{orgId}")
    public List treecForSelectAll(@PathVariable Integer orgId) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("disabled", "false").or().in("able_Orgid", orgId).orderByAsc("sort");
        return permissionService.list(queryWrapper).stream().map(Permission::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 返回角色树形菜单集合
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/roletree")
    public MessageInfo getRoleTree() {
        return MessageInfo.ok(TreeUtil.buildTree(permissionService.getRoleTree(), -1));
    }

    /**
     * 返回角色树形菜单集合
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/roletreeForSelectAll")
    public List getRoleTreeForSelectAll() {
        return permissionService.getRoleTree().stream().map(Permission::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 返回角色的菜单集合
     *
     * @param roleId 角色ID
     * @return 属性集合
     */
    @GetMapping("/tree/{roleId}")
    public List getRoleTree(@PathVariable Integer roleId) {
        return permissionService.getPermissionByRoleID(roleId,SecurityUtils.getUser().getOrgId()).stream().map(Permission::getPermissionId).collect(Collectors.toList());
    }

    @GetMapping("/treeR/{roleId}")
    public List treeR(@PathVariable Integer roleId) {
        return permissionService.getPermissionByRoleID(roleId,SecurityUtils.getUser().getOrgId()).stream().map(Permission::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 返回签约客户的菜单集合
     *
     * @param orgId 签约客户orgId
     * @return 属性集合
     */
    @GetMapping("/org-tree/{orgId}")
    public List getOrgTree(@PathVariable Integer orgId) {
        return permissionService.getPermissionByOrgID(orgId).stream().map(Permission::getPermissionId).collect(Collectors.toList());
    }

    /**
     * 通过ID查询菜单的详细信息
     *
     * @param id 菜单ID
     * @return 菜单详细信息
     */
    @GetMapping("/{id}")
    public MessageInfo getById(@PathVariable Integer id) {
        return MessageInfo.ok(permissionService.getById(id));
    }

    /**
     * 新增菜单
     *
     * @param permission 菜单信息
     * @return success/false
     */
//	@SysLog("新增菜单")
    @PostMapping("/save")
    @PreAuthorize("@pms.hasPermission('sys_menu_add')")
    public MessageInfo save(@Valid @RequestBody Permission permission) {
        return MessageInfo.ok(permissionService.savePermission(permission));
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return success/false
     */
//	@SysLog("删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('sys_menu_del')")
    public MessageInfo removeById(@PathVariable Integer id) {
        return MessageInfo.ok(permissionService.removeById(id));
    }

    /**
     * 更新菜单
     *
     * @param permission
     * @return
     */
    //@SysLog("更新菜单")
    @PutMapping
    @PreAuthorize("@pms.hasPermission('sys_menu_edit')")
    public MessageInfo update(@Valid @RequestBody Permission permission) {
        return MessageInfo.ok(permissionService.updatePermission(permission));
    }

    /**
     * 查询所有权限
     *
     * @return
     */
    @GetMapping("/permissionList")
    public MessageInfo getPermissionAll() {
        try {
            List<Permission> list = permissionService.getPermissionAll();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 首页我的常用
     * @return
     */
    @GetMapping("/listForMyApplication")
    public MessageInfo getPermissionTreeForHomePage(){
        try {
            return MessageInfo.ok(TreeUtil.buildTree(permissionService.getPermissionTreeForHomePage(), -1));
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    @GetMapping("helpDoc/{permissionName}")
    public MessageInfo helpDoc(@PathVariable("permissionName") String permissionName){
        try{
            LambdaQueryWrapper<Permission>  lambdaQueryWrapper = Wrappers.lambdaQuery();
            lambdaQueryWrapper.eq(Permission::getPermissionName, permissionName);
            Permission permission = permissionService.getOne(lambdaQueryWrapper);
            Map<String, Object> help = null;
            if(null != permission){
                help = new HashMap<>();
                help.put("helpDocUrl", permission.getHelpDocumentUrl());
                help.put("helpDocName", permission.getHelpDocumentName());
            }
            return MessageInfo.ok(help);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}
