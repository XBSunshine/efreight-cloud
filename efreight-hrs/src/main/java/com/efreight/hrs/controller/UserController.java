package com.efreight.hrs.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.annotation.Inner;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.UserBaseVO;
import com.efreight.common.security.vo.UserInfo;
import com.efreight.common.security.vo.UserVo;
import com.efreight.hrs.entity.*;
import com.efreight.hrs.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final OrgService orgService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final LogService logService;
    private final UserRoleService userRoleService;


    /**
     * 获取当前用户全部信息
     *
     * @return 用户信息
     */
    @GetMapping(value = {"/info"})
    public MessageInfo<UserInfo> info() {
        String username = SecurityUtils.getUser().getUsername();
        User user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getLoginName, username));
        if (user == null) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo(String.format("用户信息为空 %s", username));
            mi.setData(new UserInfo());
            return mi;
        }
        UserInfo ui = userService.getUserInfo(user);
        ui.getUserVo().setPassWord(null);
        return MessageInfo.ok(ui);
    }

    /**
     * 获取指定用户全部信息
     *
     * @return 用户信息
     */
    @Inner
    @GetMapping("/info/{username}")
    public MessageInfo<UserInfo> info(@PathVariable String username) {
        String[] users = username.split("\\|");
        //1.查看签约客户状态
        Org org = orgService.getOneByCode(users[2]);
        if (org == null) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo("未找到您的签约客户 " + users[2]);
            mi.setData(new UserInfo());
            return mi;
        }
        //改为按照失效日期判断
//    	if(!org.getOrgStatus()){
        if (LocalDateTime.now().isAfter(org.getStopDate())) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo("您的签约已经失效 请联系工作人员。");
            mi.setData(new UserInfo());
            return mi;
        }
        if (org.getBlackValid() != null && org.getBlackValid() == 1) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo("您的公司已经进入黑名单 请联系工作人员。");
            mi.setData(new UserInfo());
            return mi;
        }
        if (org.getCoopStatus() != null && org.getCoopStatus() == 0) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo("您的公司已被锁定 请联系工作人员。");
            mi.setData(new UserInfo());
            return mi;
        }

        User user = null;
        if ("0".equals(users[0])) {
            user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getLoginName, users[1]).eq(User::getOrgId, org.getOrgId()).eq(User::getUserStatus, 1));
        } else if ("1".equals(users[0])) {
            user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getPhoneNumber, users[1]).eq(User::getOrgId, org.getOrgId()).eq(User::getUserStatus, 1));
        } else if ("2".equals(users[0])) {
            //user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getUserEmail, users[1]).eq(User::getOrgId, org.getOrgId()).eq(User::getUserStatus, 1).eq(User::getIsadmin, 0));
            user = userService.getUserInfoByUserEmail(users[1], org.getOrgId());
        }
//        if ("0".equals(users[0])) {
//            user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getLoginName, users[1]).eq(User::getOrgId, users[2]).eq(User::getUserStatus, 1).exists("SELECT org_id FROM hrs_org WHERE org_id="+users[2]+" AND org_status=1"));
//        } else if ("1".equals(users[0])) {
//            user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getPhoneNumber, users[1]).eq(User::getOrgId, users[2]).eq(User::getUserStatus, 1).exists("SELECT org_id FROM hrs_org WHERE org_id="+users[2]+" AND org_status=1"));
//        } else if ("2".equals(users[0])) {
//            user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getUserEmail, users[1]).eq(User::getOrgId, users[2]).eq(User::getUserStatus, 1).exists("SELECT org_id FROM hrs_org WHERE org_id="+users[2]+" AND org_status=1"));
//        }

        if (user == null) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo(String.format("未找到您的用户信息 %s", users[1]));
            mi.setData(new UserInfo());
            return mi;
        }
//        System.out.println("----------"+message);
//        if(!"".equals(message)){
//        	MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
//            mi.setCode(400);
//            mi.setMessageInfo(String.format(message, null));
//            mi.setData(new UserInfo());
//            return mi;
//        }
        UserVo userVo = new UserVo();
        UserInfo ui = new UserInfo();
        BeanUtils.copyProperties(user, userVo);
        ui.setUserVo(userVo);
        List<Integer> roleIds = roleService.listRolesByUserId(userVo.getUserId()).stream().map(Role::getRoleId)
                .collect(Collectors.toList());
        if (roleIds == null || roleIds.size() == 0) {
            MessageInfo<UserInfo> mi = new MessageInfo<UserInfo>();
            mi.setCode(400);
            mi.setMessageInfo("您的用户没有角色权限,请联系管理员");
            mi.setData(new UserInfo());
            return mi;
        }
        ui.setRoles(ArrayUtil.toArray(roleIds, Integer.class));
        Set<String> permissions = new HashSet<>();
        roleIds.forEach(roleId -> {
            List<String> permissionList = permissionService.getPermissionByRoleID(roleId, userVo.getOrgId()).stream()
                    .filter(permission -> StringUtils.isNotEmpty(permission.getPermission()))
                    .map(Permission::getPermission).collect(Collectors.toList());
            permissions.addAll(permissionList);
        });
        ui.setPermissions(ArrayUtil.toArray(permissions, String.class));

        return MessageInfo.ok(ui);
    }

    /**
     * 通过ID查询用户信息
     *
     * @param id ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public MessageInfo user(@PathVariable Integer id) {
        return MessageInfo.ok(userService.getUserByID(id));
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return
     */
    @GetMapping("/details/{username}")
    public MessageInfo user(@PathVariable String username) {
        User condition = new User();
        condition.setUserName(username);
        return MessageInfo.ok(userService.getOne(new QueryWrapper<>(condition)));
    }

    /**
     * 删除用户信息
     *
     * @param id ID
     *           MessageInfo
     */
//	@SysLog("删除用户信息")
    @DeleteMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('sys_user_del')")
    public MessageInfo userDel(@PathVariable Integer id) {
        User User = userService.getById(id);
        return MessageInfo.ok(userService.removeUserById(User));
    }

    /**
     * 添加用户
     *
     * @param
     * @return success/false
     */
    //@SysLog("添加用户")
    @PostMapping("/saveUser")
    @PreAuthorize("@pms.hasPermission('sys_user_add')")
    public MessageInfo user(@RequestBody UserVo user) {
        try {
            //如果当前签约公司员工数（不包含管理员，isadmin=0），不用区分是否离职，
            //如果数量<员工数量 则可以增加，否提提示：超过用户最大数，不允许增加新用户
            User user1 = userService.getByOrgId(SecurityUtils.getUser().getOrgId());
            if (user1 != null) {
                if (user1.getUserCount() >= user1.getOrgUserCount()) {
                    return MessageInfo.failed("用户数量已经达到最大值（" + user1.getOrgUserCount() + "),不允许新增用户");
                } else {
                    userService.saveUser(user);
                    return MessageInfo.ok();
                }
            }
            userService.saveUser(user);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     *
     * @param
     */
//	@SysLog("更新用户信息")
    @PutMapping("/edit")
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
    public MessageInfo updateUser(@Valid @RequestBody UserVo user) {
        try {
            return MessageInfo.ok(userService.updateUser(user));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 更新个人信息
     *
     * @param
     */
    @PutMapping("/editPersonal")
    public MessageInfo editPersonal(@Valid @RequestBody UserVo user) {
        try {
            return MessageInfo.ok(userService.editPersonal(user));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分页查询用户
     *
     * @param page 参数集
     * @param
     * @return 用户集合
     */
    @GetMapping("/getUserList")
    public MessageInfo getUserPage(Page page, UserVo user) {
        return MessageInfo.ok(userService.getUserPage(page, user));
    }

    /**
     * 离职
     *
     * @return
     */
    @PutMapping("/leave")
    @PreAuthorize("@pms.hasPermission('sys_user_leave')")
    public MessageInfo leave(@RequestBody Map<String, Object> param) {
        MessageInfo messageInfo = null;
        try {
            userService.leave(param.get("userId").toString(), param.get("leaveDate").toString(), param.get("leaveReason").toString());
            messageInfo = MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            messageInfo = MessageInfo.failed(e.getMessage());
        }
        return messageInfo;
    }

    /**
     * 复职
     *
     * @param userId
     * @return
     */
    @PutMapping("/resume/{userId}")
    @PreAuthorize("@pms.hasPermission('sys_user_resume')")
    public MessageInfo resume(@PathVariable("userId") String userId) {
        MessageInfo messageInfo = null;
        try {
            userService.resume(userId);
            messageInfo = MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            messageInfo = MessageInfo.failed(e.getMessage());
        }
        return messageInfo;
    }

    /**
     * 黑名单
     *
     * @return
     */
    @PutMapping("/black")
    @PreAuthorize("@pms.hasPermission('sys_user_black')")
    public MessageInfo black(@RequestBody Map<String, Object> param) {
        MessageInfo messageInfo = null;
        try {
            userService.black(param.get("userId").toString(), param.get("blacklistDate").toString(), param.get("blacklistReason").toString());
            messageInfo = MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            messageInfo = MessageInfo.failed(e.getMessage());
        }
        return messageInfo;
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    @PreAuthorize("@pms.hasPermission('sys_user_export')")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") UserVo bean) throws IOException {

        List<UserExcel> list = userService.queryListForExcel(bean);
        if(list != null && list.size()>0){
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setOrderId(i + 1);

                HashMap<String, Object> userRoleParams = new HashMap<>();
                userRoleParams.put("user_id", list.get(i).getUserId());
                userRoleParams.put("org_id", SecurityUtils.getUser().getOrgId());
                Collection<UserRole> userRoles = userRoleService.listByMap(userRoleParams);
                StringBuilder buffer = new StringBuilder();
                userRoles.stream().forEach(userRole -> {
                    Role role = roleService.getRoleByID(userRole.getRoleId());
                    if (role != null) {
                        buffer.append(role.getRoleName()).append(",");
                    }
                });
                String roleName = "";
                if (!StrUtil.isBlank(buffer.toString())) {
                    roleName = buffer.toString().substring(0, buffer.toString().length() - 1);
                }
                list.get(i).setRoleName(roleName);
            }
        }

        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            String[] headers = new String[jsonArr.size()+1];
            String[] colunmStrs = new String[jsonArr.size()+1];

            //生成表头跟字段
            headers[0] = "序号";
            colunmStrs[0] = "orderId";
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i+1] = job.getString("label");
                    colunmStrs[i+1] = job.getString("prop");
                }
            }
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if (list != null && list.size() > 0) {
                for (UserExcel userExcel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], userExcel));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }else{
            ExportExcel<UserExcelForAll> ex = new ExportExcel<UserExcelForAll>();
            List<UserExcelForAll> userList = new ArrayList<UserExcelForAll>();
            if(list != null && list.size() > 0){
                for (UserExcel userExcel : list) {
                    UserExcelForAll userExcelForAll = new UserExcelForAll();
                    BeanUtils.copyProperties(userExcel, userExcelForAll);
                    userList.add(userExcelForAll);
                }
            }
            String[] headers = {"序号", "用户名", "用户英文名", "邮箱", "电话号", "证件类型", "证件号", "性别", "生日"
                    , "入职日期", "劳务类型", "职位", "离职时间", "离职原因", "黑名单时间", "黑名单原因", "创建人", "创建时间", "部门", "状态"};
            ex.exportExcel(response, "导出EXCEL", headers, userList, "Export");
        }
        try {
            Log logBean = new Log();
            logBean.setOpLevel("低");
            logBean.setOpType("导出");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("导出用户，大小为" + list.size());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户导出成功，添加日志失败！");
        }

    }

    /**
     * 导出通讯录Excel
     *
     * @param response
     * @param bean
     * @throws IOException
     */
    @PostMapping("/exportAddressExcel")
    @PreAuthorize("@pms.hasPermission('sys_user_export_address')")
    public void exportAddressExcel(HttpServletResponse response, @ModelAttribute("bean") UserVo bean) throws IOException {

        List<UserAddressExcel> list = userService.queryListForAddressExcel(bean);
        //导出日志数据
        ExportExcel<UserAddressExcel> ex = new ExportExcel<UserAddressExcel>();
        String[] headers = {"部门", "姓名", "手机号码", "工作邮箱"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
        try {
            Log logBean = new Log();
            logBean.setOpLevel("低");
            logBean.setOpType("导出");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("导出通讯录，大小为" + list.size());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("通讯录导出成功，添加日志失败！");
        }

    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !"".equals(text))
                    setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !"".equals(text))
                    setValue(LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        });
        binder.registerCustomEditor(LocalTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !"".equals(text))
                    setValue(LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });
    }

    /**
     * 重置密码
     *
     * @param userId
     * @return
     */
    @PutMapping("/resetPassward/{userId}")
    @PreAuthorize("@pms.hasPermission('sys_user_resetpass')")
    public MessageInfo resetPassward(@PathVariable Integer userId) {
        try {
            userService.resetPassward(userId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param
     * @return
     */
    @PostMapping("/modifyPassward")
    public MessageInfo modifyPassward(@RequestBody Map<String, Object> map) {
        try {
            userService.modifyPassward(map);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/searchLoginNameAndOrgCode")
    public MessageInfo searchLoginNameAndOrgCode(UserVo bean) {
        try {
            List<UserVo> list = new ArrayList<>();
            if (bean.getUserEmail() != null && !"".equals(bean.getUserEmail())) {
                list = userService.searchLoginNameAndOrgCode(bean);
            } else {
                if (bean.getLoginName() != null && !"".equals(bean.getLoginName())) {
                    list = userService.searchLoginNameAndOrgCode1(bean);
                } else {
                    //手机号+区位码登录
                    list = userService.searchLoginNameAndOrgCode3(bean);
                }

            }
            //根据userId查询工作组信息
            if(list != null && list.size() > 0){
                List<Integer> userWorkgroupDetailList = userService.getUserWorkgroupDetail(list.get(0).getUserId());
                if(userWorkgroupDetailList != null && userWorkgroupDetailList.size() > 0){
                    list.get(0).setUserWorkgroupDetailList(userWorkgroupDetailList);
                }
            }

            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询指定公司下有效的成员
     *
     * @param
     * @return
     */
    @GetMapping("/searchUserByOrg")
    public MessageInfo searchUserByOrg() {
        try {
            List<Map> mapList = userService.searchUserByOrg("EFTBJS");
            return MessageInfo.ok(mapList);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取签约公司管理员
     *
     * @param orgId
     * @return
     */
    @GetMapping("/queryAdminByOrgId/{orgId}")
    public MessageInfo queryAdminByOrgId(@PathVariable("orgId") Integer orgId) {
        try {
            User user = userService.queryAdminByOrgId(orgId);
            return MessageInfo.ok(user);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 邮箱校验
     *
     * @param param
     * @return
     */
    @PostMapping("/checkEmail")
    public MessageInfo checkEmail(@RequestBody Map<String, Object> param) {
        try {
            return MessageInfo.ok(userService.checkEmail(param));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取用户设置保留小数位
     *
     * @return
     */
    @GetMapping("/getUserAboutKeepDecimalPlaces")
    public MessageInfo getUserAboutKeepDecimalPlaces() {
        try {
            User user = userService.getUserAboutKeepDecimalPlaces();
            return MessageInfo.ok(user);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存页面设置字段
     *
     * @param
     * @return success/false
     */
    @PostMapping("/saveUserPageSet")
    public MessageInfo saveUserPageSet(@RequestBody UserPageSetVo userPageSetVo) {
        try {
            userService.saveUserPageSet(userPageSetVo);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/getUserPageSet")
    public MessageInfo getUserPageSet(String pageName) {
        return MessageInfo.ok(userService.getUserPageSet(pageName));
    }

    @Inner
    @GetMapping("/phone/{phone}")
    public MessageInfo<UserBaseVO> findByUserPhone(@PathVariable("phone")String phone){
        UserBaseVO userBase = userService.findByUserPhone(phone);
        return MessageInfo.ok(userBase);
    }

}
