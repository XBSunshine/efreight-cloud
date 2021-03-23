package com.efreight.hrs.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.UserBaseVO;
import com.efreight.common.security.vo.UserInfo;
import com.efreight.common.security.vo.UserVo;
import com.efreight.hrs.dao.UserMapper;
import com.efreight.hrs.entity.*;
import com.efreight.hrs.service.*;
import com.efreight.hrs.utils.HttpConnectionUtil;
import com.efreight.hrs.utils.PassGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final MailSendService mailSendService;
    private final BlacklistService blacklistService;
    private final DeptService deptService;
    private final UserDeptService userDeptService;
    private final LogService logService;
    private final CacheManager cacheManager;
    private final OrgService orgService;
    private final UserPageSetService userPageSetService;

    @Value("${dnspath}")
    private String dnspath;

    /**
     * 新建用户
     *
     * @param userVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    // @CacheEvict(value = "user_details",)
    @Caching(evict = {@CacheEvict(value = "user_details", key = "T(String).valueOf(0).concat('|').concat(#userVo.loginName==null?'':#userVo.loginName).concat('|').concat(#userVo.orgId==null?'':#userVo.orgId)"),
            @CacheEvict(value = "user_details", key = "T(String).valueOf(1).concat('|').concat(#userVo.phoneNumber).concat('|').concat(#userVo.orgId==null?'':#userVo.orgId)"),
            @CacheEvict(value = "user_details", key = "T(String).valueOf(2).concat('|').concat(#userVo.userEmail).concat('|').concat(#userVo.orgId==null?'':#userVo.orgId)")})

    public void saveUser(UserVo userVo) {

        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.eq("dept_id", userVo.getDeptId()).eq("org_id", SecurityUtils.getUser().getOrgId());
        Dept dept = deptService.getDeptByID(userVo.getDeptId());
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }

        //验证选择的角色是否失效
        List<Integer> roleIds = userVo.getRoleIds();
        if (roleIds != null && roleIds.size() > 0) {
            for (int i = 0; i < roleIds.size(); i++) {
                Role role = roleService.getRoleByID1(roleIds.get(i));
                if (role.getRoleStatus() == false) {
                    throw new RuntimeException(role.getRoleName() + " 角色已失效，不可修改！");
                }
            }
        }


        // 新建用户
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        //手机号 如果录入了，则不能重复（不分签约公司 ，不含管理员）
        /*if(user.getPhoneNumber()!=null && !"".equals(user.getPhoneNumber())){
            Integer count = baseMapper.countByPhoneNumber(user.getPhoneNumber());
            if(count>0){
                throw new RuntimeException("手机号不可重复");
            }
        }*/
        //验证区号+手机号唯一
        LambdaQueryWrapper<User> userWrapperForPhone = Wrappers.<User>lambdaQuery();
        userWrapperForPhone.eq(User::getIsadmin, 0).eq(User::getInternationalCountryCode, user.getInternationalCountryCode()).eq(User::getPhoneNumber, user.getPhoneNumber());
        User oneForPhone = baseMapper.selectOne(userWrapperForPhone);
        if (oneForPhone != null) {
            throw new RuntimeException("手机号已存在");
        }
        //邮箱不可重复
        if (user.getUserEmail() != null && !"".equals(user.getUserEmail())) {
            Integer count = baseMapper.countByUserEmail1(user.getUserEmail());
            if (count > 0) {
                throw new RuntimeException("邮箱不可重复");
            }
        }
        user.setIsadmin(false);
        // user.setPassWord(ENCODER.encode(user.getPassWord()));
        final String passWord = PassGenerator.getPassword(8);
        if (StrUtil.isNotBlank(user.getUserEmail())) {
            user.setPassWord(ENCODER.encode(passWord));
        }
        user.setCreateTime(LocalDateTime.now());
        user.setCreatorId(SecurityUtils.getUser().getId());
        user.setOrgId(SecurityUtils.getUser().getOrgId());
        user.setLoginName(user.getUserEmail());
        user.setJobNumber(user.getUserEmail());
        user.setUserStatus(true);

        baseMapper.insert(user);

        // 添加用户部门表hrs_user_dept
        UserDept userDept = new UserDept();
        userDept.setUserId(user.getUserId());
        userDept.setDeptId(userVo.getDeptId());
        userDept.setOrgId(SecurityUtils.getUser().getOrgId());
        userDept.setCreateTime(LocalDateTime.now());
        userDept.setCreatorId(SecurityUtils.getUser().getId());
        userDept.setIsMain(true);
        userDept.setJobPosition(userVo.getJobPosition());
        userDeptService.saveUserDept(userDept);


        //新建角色
        if (StrUtil.isNotBlank(user.getUserEmail())) {
            List<UserRole> userRoleList = userVo.getRoleIds().stream().map(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRole.setCreatorId(SecurityUtils.getUser().getId());
                userRole.setOrgId(SecurityUtils.getUser().getOrgId());
                userRole.setDeptId(SecurityUtils.getUser().getDeptId());
                return userRole;
            }).collect(Collectors.toList());
            userRoleService.saveBatch(userRoleList);
        }
        //密码发送邮件给客户
        if (StrUtil.isNotBlank(user.getUserEmail())) {
            Integer orgId = SecurityUtils.getUser().getOrgId();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //根据org_id查询企业编码和企业名称
                        Org org = orgService.getOrgByID(orgId);
                        String content = "欢迎使用生态云管理系统 <br>" +
                                /*"企业编码：" + org.getOrgCode() + "<br>" +*/
                                "企业名称：" + org.getOrgName() + "<br>" +
                                "登录账号：" + user.getUserEmail() + "<br>" +
                                "登录密码：" + passWord + "<br>";
                        UserVo superAdmin = new UserVo();
                        BeanUtils.copyProperties(getById(1), superAdmin);
                        mailSendService.sendHtmlMailNewForHrs(false, new String[]{userVo.getUserEmail()}, null, null, "生态云系统账号开通通知", content, null, superAdmin);
                    } catch (Exception e) {
                        log.info(user.getUserEmail() + "您的密码是：" + passWord + e.getMessage());
                    }

                }
            }).start();
        }
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("新建用户：" + user.getUserName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("新建用户成功，日志添加失败");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserPageSet(UserPageSetVo userPageSetVo) {
        String pageName = userPageSetVo.getPageName();
        List<UserPageSet> multipleSelection = userPageSetVo.getMultipleSelection();
        //首先根据org_id，user_id，permission_name从hrs_user_page_set删除当前页面的设置记录
        UserPageSet userPageSet = new UserPageSet();
        userPageSet.setPageName(pageName);
        userPageSet.setOrgId(SecurityUtils.getUser().getOrgId());
        userPageSet.setUserId(SecurityUtils.getUser().getId());
        userPageSetService.removeUserPageSet(userPageSet);
        //批量保存到hrs_user_page_set
        if (multipleSelection != null && multipleSelection.size() > 0) {
            for (int i = 0; i < multipleSelection.size(); i++) {
                multipleSelection.get(i).setOrgId(SecurityUtils.getUser().getOrgId());
                multipleSelection.get(i).setUserId(SecurityUtils.getUser().getId());
                multipleSelection.get(i).setPageName(pageName);
                multipleSelection.get(i).setCreateTime(LocalDateTime.now());
                multipleSelection.get(i).setEditTime(LocalDateTime.now());
                multipleSelection.get(i).setFieldNo(multipleSelection.get(i).getIndex());
            }
            userPageSetService.saveBatch(multipleSelection);
        }
    }

    @Override
    public List<UserPageSet> getUserPageSet(String pageName) {
        return userPageSetService.listByMap(pageName);
    }

    @Override
    public User getByOrgId(Integer orgId) {
        User user = new User();
        user = baseMapper.selectByOrgId(orgId);
        return user;
    }

    /**
     * 修改用户
     *
     * @param userVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {@CacheEvict(value = "user_details", key = "T(String).valueOf(0).concat('|').concat(#userVo.loginName==null?'':#userVo.loginName).concat('|').concat(#userVo.orgId==null?'':#userVo.orgId)"),
            @CacheEvict(value = "user_details", key = "T(String).valueOf(1).concat('|').concat(#userVo.phoneNumber).concat('|').concat(#userVo.orgId==null?'':#userVo.orgId)"),
            @CacheEvict(value = "user_details", key = "T(String).valueOf(2).concat('|').concat(#userVo.userEmail).concat('|').concat(#userVo.orgId==null?'':#userVo.orgId)")})

    public Boolean updateUser(UserVo userVo) {
        if (ifBlackList(userVo.getUserId().toString())) {
            throw new RuntimeException("该用户已列入黑名单，不可修改！");
        }
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userVo.getUserId()).eq("org_id", SecurityUtils.getUser().getOrgId());
        User userResult = baseMapper.selectOne(queryWrapper);
        if (userResult.getLeaveDate() != null) {
            throw new RuntimeException("该用户已经离职，不可修改！");
        }
        //验证选择的角色是否失效
        List<Integer> roleIds = userVo.getRoleIds();
        if (roleIds != null && roleIds.size() > 0) {
            for (int i = 0; i < roleIds.size(); i++) {
                Role role = roleService.getRoleByID1(roleIds.get(i));
                if (role.getRoleStatus() == false) {
                    throw new RuntimeException(role.getRoleName() + " 角色已失效，不可修改！");
                }
            }
        }

        //更新主部门
        if (!userVo.getDeptId().equals(userResult.getDeptId()) || !userVo.getJobPosition().equals(userResult.getJobPosition())) {
            UserDept userDept = new UserDept();
            userDept.setDeptId(userVo.getDeptId());
            userDept.setOrgId(SecurityUtils.getUser().getOrgId());
            userDept.setUserId(userVo.getUserId());
            userDept.setJobPosition(userVo.getJobPosition());
            userDeptService.updateByUserIdAndDeptId(userDept);
        }
        //更新用户
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        //手机号 如果录入了，则不能重复（不分签约公司 ，不含管理员）
        /*if(user.getPhoneNumber()!=null && !"".equals(user.getPhoneNumber())){
            Integer count = baseMapper.countByPhoneNumber1(user.getPhoneNumber(),user.getUserId());
            if(count>0){
                throw new RuntimeException("手机号不可重复");
            }
        }*/
        //验证区号+手机号唯一
        LambdaQueryWrapper<User> userWrapperForPhone = Wrappers.<User>lambdaQuery();
        userWrapperForPhone.eq(User::getIsadmin, 0).eq(User::getInternationalCountryCode, user.getInternationalCountryCode()).eq(User::getPhoneNumber, user.getPhoneNumber()).ne(User::getUserId, user.getUserId());
        User oneForPhone = baseMapper.selectOne(userWrapperForPhone);
        if (oneForPhone != null) {
            throw new RuntimeException("手机号已存在");
        }
        //邮箱不可重复
        if (user.getUserEmail() != null && !"".equals(user.getUserEmail())) {
            Integer count = baseMapper.countByUserEmail2(user.getUserEmail(), user.getUserId());
            if (count > 0) {
                throw new RuntimeException("邮箱不可重复");
            }

        }
        user.setIsadmin(false);
        user.setEditTime(LocalDateTime.now());
        user.setEditorId(SecurityUtils.getUser().getId());
        user.setLoginName(user.getUserEmail());
        user.setJobNumber(user.getUserEmail());
        baseMapper.updateById(user);

        //更新角色
        userRoleService.removeRoleByUserId(user.getUserId());
        List<UserRole> userRoleList = userVo.getRoleIds().stream().map(roleId -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            userRole.setCreatorId(SecurityUtils.getUser().getId());
            userRole.setOrgId(SecurityUtils.getUser().getOrgId());
            userRole.setDeptId(SecurityUtils.getUser().getDeptId());
            return userRole;
        }).collect(Collectors.toList());

        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("修改用户：" + user.getUserName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改用户成功，日志添加失败");
        }
        return userRoleService.saveBatch(userRoleList);

    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean editPersonal(UserVo userVo) {
        if (ifBlackList(userVo.getUserId().toString())) {
            throw new RuntimeException("该用户已列入黑名单，不可修改！");
        }
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userVo.getUserId()).eq("org_id", SecurityUtils.getUser().getOrgId());
        User userResult = baseMapper.selectOne(queryWrapper);
        if (userResult.getLeaveDate() != null) {
            throw new RuntimeException("该用户已经离职，不可修改！");
        }
        //更新用户
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        //验证区号+手机号唯一
        //管理员 修改 校验 isadmin=1 的 区号+手机号 是否重复（不含自己）；
        //普通用户 修改 校验 isadmin=0 的 区号+手机号 是否重复（不含自己）；
        if (user.getPhoneNumber() != null && !"".equals(user.getPhoneNumber())) {
            LambdaQueryWrapper<User> userWrapperForPhone = Wrappers.<User>lambdaQuery();
            if ("admin".equals(userVo.getLoginRole())) {
                userWrapperForPhone.eq(User::getIsadmin, 1).eq(User::getInternationalCountryCode, user.getInternationalCountryCode()).eq(User::getPhoneNumber, user.getPhoneNumber()).ne(User::getUserId, user.getUserId());
            } else {
                userWrapperForPhone.eq(User::getIsadmin, 0).eq(User::getInternationalCountryCode, user.getInternationalCountryCode()).eq(User::getPhoneNumber, user.getPhoneNumber()).ne(User::getUserId, user.getUserId());
            }
            User oneForPhone = baseMapper.selectOne(userWrapperForPhone);
            if (oneForPhone != null) {
                throw new RuntimeException("手机号已存在");
            }
        }
        //邮箱不可重复
        //管理员 修改 校验 isadmin=1 的 邮箱是否重复（不含自己）；
        //普通用户 修改 校验 isadmin=0 的 邮箱是否重复（不含自己）；
        if (user.getUserEmail() != null && !"".equals(user.getUserEmail())) {
            Integer count = 0;
            if ("admin".equals(userVo.getLoginRole())) {
                count = baseMapper.countByUserEmail(user.getUserEmail(), user.getUserId());
            } else {
                count = baseMapper.countByUserEmail2(user.getUserEmail(), user.getUserId());
            }
            if (count > 0) {
                throw new RuntimeException("邮箱不可重复");
            }
        }
        user.setEditTime(LocalDateTime.now());
        user.setEditorId(SecurityUtils.getUser().getId());
        baseMapper.updateById(user);
        //设置默认抄送人
        //先根据签约公司Id和当前操作人Id删除默认抄送人记录
        baseMapper.removeUserMailCc(SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getId());
        if (userVo.getOrderTrackCcUser() != null && userVo.getOrderTrackCcUser().size() > 0) {
            List<Integer> orderTrackCcUser = userVo.getOrderTrackCcUser();
            for (int i = 0; i < orderTrackCcUser.size(); i++) {
                UserMailCc userMailCcOrderTrack = new UserMailCc();
                userMailCcOrderTrack.setOrgId(SecurityUtils.getUser().getOrgId());
                userMailCcOrderTrack.setUserId(SecurityUtils.getUser().getId());
                userMailCcOrderTrack.setPermissionName("订单跟踪码");
                userMailCcOrderTrack.setUserIdCc(orderTrackCcUser.get(i));
                userMailCcOrderTrack.setCreateTime(LocalDateTime.now());
                baseMapper.insetUserMailCc(userMailCcOrderTrack);
            }
        }
        if (userVo.getSendGoodsNotifyCcUser() != null && userVo.getSendGoodsNotifyCcUser().size() > 0) {
            List<Integer> sendGoodsNotifyCcUser = userVo.getSendGoodsNotifyCcUser();
            for (int i = 0; i < sendGoodsNotifyCcUser.size(); i++) {
                UserMailCc userMailCcSendGoodsNotify = new UserMailCc();
                userMailCcSendGoodsNotify.setOrgId(SecurityUtils.getUser().getOrgId());
                userMailCcSendGoodsNotify.setUserId(SecurityUtils.getUser().getId());
                userMailCcSendGoodsNotify.setPermissionName("送货通知码");
                userMailCcSendGoodsNotify.setUserIdCc(sendGoodsNotifyCcUser.get(i));
                userMailCcSendGoodsNotify.setCreateTime(LocalDateTime.now());
                baseMapper.insetUserMailCc(userMailCcSendGoodsNotify);
            }
        }
        if (userVo.getSendBillCcUser() != null && userVo.getSendBillCcUser().size() > 0) {
            List<Integer> sendBillCcUser = userVo.getSendBillCcUser();
            for (int i = 0; i < sendBillCcUser.size(); i++) {
                UserMailCc userMailCcSendBill = new UserMailCc();
                userMailCcSendBill.setOrgId(SecurityUtils.getUser().getOrgId());
                userMailCcSendBill.setUserId(SecurityUtils.getUser().getId());
                userMailCcSendBill.setPermissionName("发送账单");
                userMailCcSendBill.setUserIdCc(sendBillCcUser.get(i));
                userMailCcSendBill.setCreateTime(LocalDateTime.now());
                baseMapper.insetUserMailCc(userMailCcSendBill);
            }
        }
        if (userVo.getSendInventoryCcUser() != null && userVo.getSendInventoryCcUser().size() > 0) {
            List<Integer> sendInventoryCcUser = userVo.getSendInventoryCcUser();
            for (int i = 0; i < sendInventoryCcUser.size(); i++) {
                UserMailCc userMailCcSendInventory = new UserMailCc();
                userMailCcSendInventory.setOrgId(SecurityUtils.getUser().getOrgId());
                userMailCcSendInventory.setUserId(SecurityUtils.getUser().getId());
                userMailCcSendInventory.setPermissionName("发送清单");
                userMailCcSendInventory.setUserIdCc(sendInventoryCcUser.get(i));
                userMailCcSendInventory.setCreateTime(LocalDateTime.now());
                baseMapper.insetUserMailCc(userMailCcSendInventory);
            }
        }


        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("个人信息");
            logBean.setOpInfo("修改个人信息");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改个人信息成功，日志添加失败");
        }
        return true;

    }

    @Override
    public IPage<User> getUserPage(Page page, UserVo userVo) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

       /* if (userVo.getUserStatus() != null) {
            queryWrapper.eq("user_status", userVo.getUserStatus());
        }*/
        if (userVo.getUserName() != null && !"".equals(userVo.getUserName())) {
//            queryWrapper.like("user_name", userVo.getUserName());
            String keys = userVo.getUserName();
            queryWrapper.and(wrapper -> wrapper.like("login_name", keys).or().like("job_number", keys).or().like("user_name", keys).or().like("user_ename", keys));

        }
        if (userVo.getEmploymentType() != null && !"".equals(userVo.getEmploymentType())) {
            queryWrapper.eq("employment_type", userVo.getEmploymentType());
        }
        if (userVo.getUserEmail() != null && !"".equals(userVo.getUserEmail())) {
            queryWrapper.like("user_email", userVo.getUserEmail());
        }
        if (userVo.getPhoneNumber() != null && !"".equals(userVo.getPhoneNumber())) {
            queryWrapper.like("phone_number", userVo.getPhoneNumber());
        }

        if (userVo.getUserBirthdayStart() != null && !"".equals(userVo.getUserBirthdayStart())) {
            queryWrapper.ge("user_birthday", userVo.getUserBirthdayStart());
        }
        if (userVo.getUserBirthdayEnd() != null && !"".equals(userVo.getUserBirthdayEnd())) {
            queryWrapper.le("user_birthday", userVo.getUserBirthdayEnd());
        }
        if (userVo.getHireDateStart() != null && !"".equals(userVo.getHireDateStart())) {
            queryWrapper.ge("hire_date", userVo.getHireDateStart());
        }
        if (userVo.getHireDateEnd() != null && !"".equals(userVo.getHireDateEnd())) {
            queryWrapper.le("hire_date", userVo.getHireDateEnd());
        }
        if (userVo.getLeaveDateStart() != null && !"".equals(userVo.getLeaveDateStart())) {
            queryWrapper.ge("leave_date", userVo.getLeaveDateStart());
        }
        if (userVo.getLeaveDateEnd() != null && !"".equals(userVo.getLeaveDateEnd())) {
            queryWrapper.le("leave_date", userVo.getLeaveDateEnd());
        }
        if (userVo.getBlacklistDateStart() != null && !"".equals(userVo.getBlacklistDateStart())) {
            queryWrapper.ge("blacklist_date", userVo.getBlacklistDateStart());
        }
        if (userVo.getBlacklistDateEnd() != null && !"".equals(userVo.getBlacklistDateEnd())) {
            queryWrapper.le("blacklist_date", userVo.getBlacklistDateEnd());
        }
        if (userVo.getEditTimeStart() != null && !"".equals(userVo.getEditTimeStart())) {
            queryWrapper.ge("edit_time", userVo.getEditTimeStart());
        }
        if (userVo.getEditTimeEnd() != null && !"".equals(userVo.getEditTimeEnd())) {
            queryWrapper.le("edit_time", userVo.getEditTimeEnd());
        }
        queryWrapper.eq("isadmin", false);
        queryWrapper.eq("org_Id", SecurityUtils.getUser().getOrgId());
        queryWrapper.orderByDesc("create_time");
        if (userVo.getDeptCode() != null && !"".equals(userVo.getDeptCode())) {
            queryWrapper.inSql("dept_Id", "SELECT dept_id FROM hrs_dept WHERE org_id=" + SecurityUtils.getUser().getOrgId() + " AND dept_code LIKE CONCAT(" + userVo.getDeptCode() + ",'%')");
        }
        if (userVo.getIfBlack() != null && userVo.getIfBlack().booleanValue() == true) {
            queryWrapper.isNotNull("blacklist_date");
        }
        if (userVo.getIfBlack() != null && userVo.getIfBlack().booleanValue() == false) {
            queryWrapper.isNull("blacklist_date");
        }
        if (userVo.getIfLeave() != null && userVo.getIfLeave().booleanValue() == true) {
            queryWrapper.isNotNull("leave_date");
        }
        if (userVo.getIfLeave() != null && userVo.getIfLeave().booleanValue() == false) {
            queryWrapper.isNull("leave_date");
        }
        IPage<User> iPage = baseMapper.selectPage(page, queryWrapper);
        iPage.getRecords().stream().forEach(user -> {
            //一、获取角色
            HashMap<String, Object> userRoleParams = new HashMap<>();
            userRoleParams.put("user_id", user.getUserId());
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
            //二、获取部门、岗位
            Dept dept = deptService.getDeptByID(user.getDeptId());
            StringBuilder deptBuff = new StringBuilder();
            if (dept != null) {
                if (dept.getFullName().indexOf("/") != -1) {
                    deptBuff.append(dept.getFullName().substring(dept.getFullName().indexOf("/") + 1, dept.getFullName().length()));
                } else {
                    deptBuff.append(dept.getFullName());
                }
//                deptBuff.append(dept.getDeptName());
            }
            StringBuilder jobBuff = new StringBuilder(user.getJobPosition());
            HashMap<String, Object> userDeptParams = new HashMap<>();
            userDeptParams.put("user_id", user.getUserId());
            userDeptParams.put("org_id", SecurityUtils.getUser().getOrgId());
            userDeptParams.put("isMain", "0");
            Collection<UserDept> userDepts = userDeptService.listByMap(userDeptParams);
            userDepts.stream().forEach(userDept -> {
                Dept deptOther = deptService.getDeptByID(userDept.getDeptId());
                if (deptOther != null) {
                    if (deptOther.getFullName().indexOf("/") != -1) {
                        deptBuff.append(",").append(deptOther.getFullName().substring(deptOther.getFullName().indexOf("/") + 1, deptOther.getFullName().length()));
                    } else {
                        deptBuff.append(",").append(deptOther.getFullName());
                    }
//                    deptBuff.append(",").append(deptOther.getDeptName());
                } else {
                    deptBuff.append(",");
                }
                jobBuff.append(",").append(userDept.getJobPosition());

            });

            //三、将部门、岗位和角色赋值给当前用户

            user.setDeptName(deptBuff.toString());
            user.setJobPosition(jobBuff.toString());
            user.setRoleName(roleName);
        });

        return iPage;
    }

    @Override
    public UserVo getUserByID(Integer userId) {
        UserVo uv = new UserVo();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_Id", userId);
        BeanUtils.copyProperties(baseMapper.selectOne(queryWrapper), uv);
        List<Integer> roleIds = roleService.listRolesByUserId(userId).stream().map(Role::getRoleId)
                .collect(Collectors.toList());
        uv.setRoleIds(roleIds);
        //ui.setRoles(ArrayUtil.toArray(roleIds, Integer.class));
        //根据签约公司ID和操作人id默认抄送人信息
        List<Integer> orderTrackCcUser = baseMapper.getUserIdCc(SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getId(), "订单跟踪码");
        List<Integer> sendGoodsNotifyCcUser = baseMapper.getUserIdCc(SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getId(), "送货通知码");
        List<Integer> sendBillCcUser = baseMapper.getUserIdCc(SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getId(), "发送账单");
        List<Integer> sendInventoryCcUser = baseMapper.getUserIdCc(SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getId(), "发送清单");
        uv.setOrderTrackCcUser(orderTrackCcUser);
        uv.setSendGoodsNotifyCcUser(sendGoodsNotifyCcUser);
        uv.setSendBillCcUser(sendBillCcUser);
        uv.setSendInventoryCcUser(sendInventoryCcUser);
        return uv;
    }

    @Override
    public UserInfo getUserInfo(User user) {
        UserVo userVo = new UserVo();
        UserInfo ui = new UserInfo();
        BeanUtils.copyProperties(user, userVo);
        ui.setUserVo(userVo);
        List<Integer> roleIds = roleService.listRolesByUserId(userVo.getUserId()).stream().map(Role::getRoleId)
                .collect(Collectors.toList());
        ui.setRoles(ArrayUtil.toArray(roleIds, Integer.class));
        Set<String> permissions = new HashSet<>();
        roleIds.forEach(roleId -> {
            List<String> permissionList = permissionService.getPermissionByRoleID(roleId, userVo.getOrgId()).stream()
                    .filter(permission -> StringUtils.isNotEmpty(permission.getPermission()))
                    .map(Permission::getPermission).collect(Collectors.toList());
            permissions.addAll(permissionList);
        });
        ui.setPermissions(ArrayUtil.toArray(permissions, String.class));
        return ui;
    }

    /**
     * 删除用户
     *
     * @param user
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {@CacheEvict(value = "user_details", key = "T(String).valueOf(0).concat('|').concat(#user.loginName==null?'':#user.loginName).concat('|').concat(#user.orgId==null?'':#user.orgId)"),
            @CacheEvict(value = "user_details", key = "T(String).valueOf(1).concat('|').concat(#user.phoneNumber==null?'':#user.phoneNumber).concat('|').concat(#user.orgId==null?'':#user.orgId)"),
            @CacheEvict(value = "user_details", key = "T(String).valueOf(2).concat('|').concat(#user.userEmail==null?'':#user.userEmail).concat('|').concat(#user.orgId==null?'':#user.orgId)")})

    public Boolean removeUserById(User user) {
        //删除用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_Id", user.getUserId());
        User resultUser = baseMapper.selectById(user.getUserId());
        this.remove(queryWrapper);
        //删除角色
        userRoleService.removeRoleByUserId(user.getUserId());
        //删除用户部门
        QueryWrapper<UserDept> queryWrapperUserDept = Wrappers.query();
        queryWrapperUserDept.eq("user_id", user.getUserId()).eq("org_id", SecurityUtils.getUser().getOrgId());
        userDeptService.remove(queryWrapperUserDept);

        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("删除用户：" + resultUser.getUserName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("删除用户成功，日志添加失败");
        }
        return true;
    }

    /**
     * 修改-离职
     *
     * @param userId
     * @param leaveDate
     * @param leaveReason
     */
    @Override
    public void leave(String userId, String leaveDate, String leaveReason) {
        if (StrUtil.isBlank(userId)) {
            throw new RuntimeException("用户编码不能为空！");
        }
        if (StrUtil.isBlank(leaveDate)) {
            throw new RuntimeException("离职时间不能为空！");
        }
        if (ifBlackList(userId)) {
            throw new RuntimeException("该用户已列入黑名单，不可修改！");
        }

        User user = baseMapper.selectById(userId);
        //根据userId去prm_coop表查询是否有客商资料的负责人
        String transactorUser = baseMapper.getTransactorUserByUserId(userId);
        if (transactorUser != null && !"".equals(transactorUser)) {
            throw new RuntimeException(user.getUserName() + "是 客商资料（" + transactorUser + "）负责人，请确认！");
        }
//        if(user.getLeaveDate()!=null){
//            throw new RuntimeException("该用户已离职，不可再离职！");
//        }
        baseMapper.leave(userId, leaveDate, leaveReason, SecurityUtils.getUser().getOrgId());
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("离职");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("用户离职：" + user.getUserName() + ",离职原因：" + leaveReason);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户离职成功，日志添加失败");
        }
    }

    /**
     * 修改-复职
     *
     * @param userId
     */
    @Override
    public void resume(String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new RuntimeException("用户编码不能为空！");
        }
        if (ifBlackList(userId)) {
            throw new RuntimeException("该用户已列入黑名单，不可修改！");
        }
        User userCheck = getByOrgId(SecurityUtils.getUser().getOrgId());
        if (userCheck != null) {
            if (userCheck.getUserCount() >= userCheck.getOrgUserCount()) {
                throw new RuntimeException("用户数量已经达到最大值（" + userCheck.getOrgUserCount() + "),不允许复职");
            }
        }
        baseMapper.resume(userId, SecurityUtils.getUser().getOrgId());
        User user = baseMapper.selectById(userId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("复职");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("用户复职：" + user.getUserName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户复职成功，日志添加失败");
        }
    }

    /**
     * 修改-黑名单
     *
     * @param userId
     * @param blackDate
     * @param blackReason
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void black(String userId, String blackDate, String blackReason) {
        if (StrUtil.isBlank(userId)) {
            throw new RuntimeException("用户编码不能为空！");
        }
        if (StrUtil.isBlank(blackDate)) {
            throw new RuntimeException("黑名单时间不能为空！");
        }
        if (StrUtil.isBlank(blackReason)) {
            throw new RuntimeException("黑名单原因不能为空！");
        }
        if (ifBlackList(userId)) {
            throw new RuntimeException("该用户已列入黑名单，不可修改！");
        }

        //添加黑名单列表
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId).eq("org_id", SecurityUtils.getUser().getOrgId());
        User user = baseMapper.selectOne(queryWrapper);

        //根据userId去prm_coop表查询是否有客商资料的负责人
        String transactorUser = baseMapper.getTransactorUserByUserId(userId);
        if (transactorUser != null && !"".equals(transactorUser)) {
            throw new RuntimeException(user.getUserName() + "是 客商资料（" + transactorUser + "）负责人，请确认！");
        }

        //修改用户表--变成黑名单
        baseMapper.black(userId, blackDate, blackReason, SecurityUtils.getUser().getOrgId());

        Blacklist blacklist = new Blacklist();
//        blacklist.setBlacklistId(null);
        blacklist.setBlacklistDate(LocalDate.parse(blackDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay());
        blacklist.setBlacklistReason(blackReason);
        blacklist.setBlacklistStatus(true);
        blacklist.setCreateTime(LocalDateTime.now());
        blacklist.setCreatorId(SecurityUtils.getUser().getId());
        blacklist.setFromApp("hrs");
        blacklist.setOrgId(SecurityUtils.getUser().getOrgId().toString());
        blacklist.setIdNumber(user.getIdNumber());
        blacklist.setIdType(user.getIdType());
        blacklist.setPhoneNumber(user.getPhoneNumber());
        blacklist.setUserEmail(user.getUserEmail());
        blacklist.setUserEname(user.getUserEname());
        blacklist.setUserName(user.getUserName());
        blacklistService.saveBlackList(blacklist);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("黑名单");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("用户[" + user.getUserName() + "]添加黑名单，原因：" + blackReason);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户黑名单成功，日志添加失败");
        }

    }

    @Override
    public List<UserExcel> queryListForExcel(UserVo user) {
        user.setOrgId(SecurityUtils.getUser().getOrgId());
        if (user.getIfBlack() != null && !"".equals(user.getIfBlack()) && user.getIfBlack() == true) {
            user.setIfBlack1("1");
        } else if (user.getIfBlack() != null && !"".equals(user.getIfBlack()) && user.getIfBlack() == false) {
            user.setIfBlack1("0");
        }
        if (user.getIfLeave() != null && !"".equals(user.getIfLeave()) && user.getIfLeave() == true) {
            user.setIfLeave1("1");
        } else if (user.getIfLeave() != null && !"".equals(user.getIfLeave()) && user.getIfLeave() == false) {
            user.setIfLeave1("0");
        }
        return baseMapper.queryListForExcel(user);
    }

    @Override
    public List<UserAddressExcel> queryListForAddressExcel(UserVo user) {
        user.setOrgId(SecurityUtils.getUser().getOrgId());
        if (user.getIfBlack() != null && !"".equals(user.getIfBlack()) && user.getIfBlack() == true) {
            user.setIfBlack1("1");
        } else if (user.getIfBlack() != null && !"".equals(user.getIfBlack()) && user.getIfBlack() == false) {
            user.setIfBlack1("0");
        }
        if (user.getIfLeave() != null && !"".equals(user.getIfLeave()) && user.getIfLeave() == true) {
            user.setIfLeave1("1");
        } else if (user.getIfLeave() != null && !"".equals(user.getIfLeave()) && user.getIfLeave() == false) {
            user.setIfLeave1("0");
        }
        return baseMapper.queryListForAddressExcel(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassward(Integer userId) {
        if (ifBlackList(userId.toString())) {
            throw new RuntimeException("该用户已列入黑名单，不可重置密码！");
        }
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId).eq("org_id", SecurityUtils.getUser().getOrgId());
        User userResult = baseMapper.selectOne(queryWrapper);
        if (userResult.getLeaveDate() != null) {
            throw new RuntimeException("该用户已经离职，不可重置密码！");
        }
        //查询邮件
        User resultUser = baseMapper.selectById(userId);
        if (resultUser == null) {
            throw new RuntimeException("该管理员不存在");
        } else if (StrUtil.isBlank(resultUser.getUserEmail())) {
            throw new RuntimeException("该管理员未设置邮箱,请设置好邮箱再修改密码");
        }
        String passWord = PassGenerator.getPassword(8);
        User user = new User();
        user.setUserId(userId);
        user.setPassWord(ENCODER.encode(passWord));
        user.setPassWordVerification(new String(Base64Utils.encode((passWord).getBytes())));
        baseMapper.updateById(user);
        //清缓存
        try {
            cacheManager.getCache("user_details").clear();
        } catch (Exception e) {
            log.info("--------------redis清缓存失败------------------");
        }
        Integer orgId = SecurityUtils.getUser().getOrgId();
        try {
            //根据org_id查询企业编码和企业名称
            Org org = orgService.getOrgByID(orgId);
            String content = "欢迎使用生态云管理系统 <br>" +
                    /*"企业编码：" + org.getOrgCode() + "<br>" +*/
                    "企业名称：" + org.getOrgName() + "<br>" +
                    "登录账号：" + resultUser.getJobNumber() + "<br>" +
                    "登录密码：" + passWord + "<br>";
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(getById(1), userVo);
            mailSendService.sendHtmlMailNewForHrs(false, new String[]{resultUser.getUserEmail()}, null, null, "生态云系统密码重置通知", content, null, userVo);
        } catch (Exception e) {
            log.info("用户管理重置用户密码失败,原因：发送邮件失败," + e.getMessage());
            throw new RuntimeException("用户管理重置用户密码失败,原因：发送邮件失败," + e.getMessage());
        }

        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("重置密码");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("用户 [" + resultUser.getUserName() + "] 重置密码");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户重置密码密码成功，日志添加失败");
        }
    }

    @Override
    public void modifyPassward(Map<String, Object> map) {
        if (map.get("userId") == null) {
            throw new RuntimeException("缺少必要信息，修改失败");
        }
        Integer userId = Integer.parseInt(map.get("userId").toString());
        String oldPass = map.get("oldPassword").toString();
        String newPass = map.get("newPassword").toString();
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("未找到用户，修改失败");
        }
        if (!ENCODER.matches(oldPass, user.getPassWord())) {
            throw new RuntimeException("原密码不正确，修改失败");
        }

        //查询org
        LambdaQueryWrapper<Org> wrapperOrg = Wrappers.<Org>lambdaQuery();
        wrapperOrg.eq(Org::getOrgId, user.getOrgId());
        Org org = orgService.getOrgByID(user.getOrgId());
        User userUpdate = new User();
        userUpdate.setUserId(userId);
        userUpdate.setPassWord(ENCODER.encode(newPass));
        //针对个人用户 是 EF 过来的 需要同步更新一下校验码
//        if (org != null && !user.getIsadmin() && org.getOrgType().intValue() == 2) {
//            userUpdate.setPassWordVerification(new String(Base64Utils.encode((newPass).getBytes())));
//        }
        //新需求  全放开
        userUpdate.setPassWordVerification(new String(Base64Utils.encode((newPass).getBytes())));
        baseMapper.updateById(userUpdate);
        //清缓存
        try {
            cacheManager.getCache("user_details").clear();
        } catch (Exception e) {
            log.info("--------------redis清缓存失败------------------");
        }
//        System.out.println(dnspath);
        try {
            //完成插入后 如果是签约用户类型 通知通行证同步更新密码
            if (org != null && !user.getIsadmin() && org.getOrgType().intValue() == 2) {
                String url = "";
                if (dnspath.contains("tom.efreight.cn")) {
                    url = "http://usr.yctop.com/saas/update/pw";
                } else {
                    url = "http://uc.yctop.com/saas/update/pw";
                }
                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("phone", user.getPhoneNumber());
                switch (user.getInternationalCountryCode()) {
                    case "0086":
                        paramMap.put("area", "86");
                        break;
                    case "0852":
                        paramMap.put("area", "852");
                        break;
                    case "0853":
                        paramMap.put("area", "853");
                        break;
                    case "0886":
                        paramMap.put("area", "886");
                        break;
                    default:
                        paramMap.put("area", "86");
                        break;
                }
                paramMap.put("pw", newPass);
                String str = HttpConnectionUtil.sendByPost(url, paramMap, "application/x-www-form-urlencoded");
                log.info(user.getLoginName() + "签约用户修改密码通知通行证结果：" + str);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("签约用户:" + user.getLoginName() + "修改密码通知通行证失败，异常信息：" + e.getMessage());
        }
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改密码");
            logBean.setOpName("用户管理");
            logBean.setOpInfo("用户 [" + user.getUserName() + "] 修改密码");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("用户修改密码成功，日志添加失败");
        }
    }

    private boolean ifBlackList(String userId) {
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId).eq("org_id", SecurityUtils.getUser().getOrgId());
        User user = baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getBlacklistDate() == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<UserVo> searchLoginNameAndOrgCode(UserVo user) {
        return baseMapper.searchLoginNameAndOrgCode(user);
    }

    @Override
    public List<UserVo> searchLoginNameAndOrgCode1(UserVo user) {
        return baseMapper.searchLoginNameAndOrgCode1(user);
    }

    @Override
    public User getUserInfoByUserEmail(String userEmail, Integer orgId) {
        return baseMapper.getUserInfoByUserEmail(userEmail, orgId);
    }

    @Override
    public List<Map> searchUserByOrg(String orgCode) {
        return baseMapper.searchUserByOrg(orgCode);
    }

    @Override
    public List<UserVo> searchLoginNameAndOrgCode3(UserVo user) {
        return baseMapper.searchLoginNameAndOrgCode3(user);
    }

    @Override
    public User queryAdminByOrgId(Integer orgId) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        wrapper.eq(User::getOrgId, orgId).eq(User::getIsadmin, true).ne(User::getUserId, 1);
        return getOne(wrapper);
    }

    @Override
    public Boolean checkEmail(Map<String, Object> param) {
        return mailSendService.checkEmail(param);
    }

    @Override
    public User getUserAboutKeepDecimalPlaces() {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        wrapper.select(User::getUserId, User::getOrderAeDigitsWeight, User::getOrderAeDigitsVolume, User::getOrderAeDigitsChargeWeight, User::getOrderAiDigitsWeight, User::getOrderAiDigitsVolume, User::getOrderAiDigitsChargeWeight, User::getOrderSeDigitsWeight, User::getOrderSeDigitsVolume, User::getOrderSeDigitsChargeWeight, User::getOrderSiDigitsWeight, User::getOrderSiDigitsVolume, User::getOrderSiDigitsChargeWeight,
                User::getOrderTeDigitsWeight, User::getOrderTeDigitsVolume, User::getOrderTeDigitsChargeWeight,
                User::getOrderTiDigitsWeight, User::getOrderTiDigitsVolume, User::getOrderTiDigitsChargeWeight,
                User::getOrderIoDigitsWeight, User::getOrderIoDigitsVolume, User::getOrderIoDigitsChargeWeight,
                User::getOrderLcDigitsWeight, User::getOrderLcDigitsVolume, User::getOrderLcDigitsChargeWeight)
                .eq(User::getUserId, SecurityUtils.getUser().getId());
        return getOne(wrapper);
    }

    @Override
    public List<Integer> getUserWorkgroupDetail(Integer userId) {
        return baseMapper.getUserWorkgroupDetail(userId);
    }

    @Override
    public UserBaseVO findByUserPhone(String phone, String internationalCountryCode) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getPhoneNumber, phone);
        wrapper.eq(User::getIsadmin, false);
        wrapper.eq(User::getUserStatus, true);
        if (StringUtils.isNotBlank(internationalCountryCode)) {
            wrapper.eq(User::getInternationalCountryCode, internationalCountryCode);
        }

        User dbUser = this.baseMapper.selectOne(wrapper);
        UserBaseVO userBaseVO = null;
        if (null != dbUser) {
            userBaseVO = new UserBaseVO();
            userBaseVO.setUserId(dbUser.getUserId());
            userBaseVO.setOrgId(dbUser.getOrgId());
        }
        return userBaseVO;
    }
}
