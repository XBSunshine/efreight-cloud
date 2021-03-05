package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.feign.RemoteServiceToAF;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.common.security.vo.CurrencyRateVo;
import com.efreight.common.security.vo.UserVo;
import com.efreight.hrs.dao.*;
import com.efreight.hrs.entity.*;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.OrgPermissionService;
import com.efreight.hrs.service.OrgService;
import com.efreight.hrs.utils.PassGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
public class OrgServiceImpl extends ServiceImpl<OrgMapper, Org> implements OrgService {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @Autowired
    UserMapper userMapper;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    UserRoleMapper userRoleMapper;
    @Autowired
    DeptMapper deptMapper;
    @Autowired
    UserDeptMapper userDeptMapper;
    @Autowired
    MailSendService mailSendService;
    //    @Autowired
//    Cache cache;
    @Autowired
    RolePermissionMapper rolePermissionMapper;
    @Autowired
    PermissionMapper permissionMapper;
    @Autowired
    private LogService logService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private RemoteServiceToAF remoteServiceToAF;
    @Autowired
    private OrgPermissionService orgPermissionService;
    @Autowired
    private RemoteCoopService remoteCoopService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrg(Org org) {
        //校验管理员账户和邮箱

        LambdaQueryWrapper<User> userWrapper = Wrappers.<User>lambdaQuery();
        userWrapper.eq(User::getIsadmin, 1).eq(User::getLoginName, org.getAdminName());
        User one = userMapper.selectOne(userWrapper);
        if (one != null) {
            throw new RuntimeException("管理员账户已被使用");
        }
        LambdaQueryWrapper<User> userAminWrapper = Wrappers.<User>lambdaQuery();
        userAminWrapper.eq(User::getIsadmin, 1).eq(User::getInternationalCountryCode, org.getAdminInternationalCountryCode()).eq(User::getPhoneNumber, org.getAdminTel());
        User internationalCountryCode = userMapper.selectOne(userAminWrapper);
        if (internationalCountryCode != null) {
            throw new RuntimeException("手机号已存在");
        }
        LambdaQueryWrapper<User> userAminWrapperTwo = Wrappers.<User>lambdaQuery();
        userAminWrapperTwo.eq(User::getIsadmin, 0).eq(User::getInternationalCountryCode, org.getAdminInternationalCountryCode()).eq(User::getPhoneNumber, org.getAdminTel());
        User internationalCountryCodeTwo = userMapper.selectOne(userAminWrapperTwo);
        if (internationalCountryCodeTwo != null) {
            throw new RuntimeException("手机号已存在");
        }
        
        /*LambdaQueryWrapper<User> userWrapperForEmail = Wrappers.<User>lambdaQuery();
        userWrapperForEmail.eq(User::getIsadmin, 1).eq(User::getUserEmail, org.getAdminEmail());
        User oneForEmail = userMapper.selectOne(userWrapperForEmail);
        if (oneForEmail != null) {
            throw new RuntimeException("管理邮箱已被使用");
        }*/
        //管理员邮箱不可与其他签约公司管理员重复
        Integer count = baseMapper.countByEmail1(org.getAdminEmail());
        if(count>0){
            throw new RuntimeException("管理邮箱已被其他管理员使用");
        }
        //管理员邮箱不可与普通用户邮箱重复
        Integer count1 = baseMapper.countByEmail2(org.getAdminEmail());
        if(count1>0){
            throw new RuntimeException("邮箱已存在");
        }

        //保存org
        org.setCreateTime(LocalDateTime.now());
        org.setCreatorId(SecurityUtils.getUser().getId());
        baseMapper.insert(org);
        String passWord = PassGenerator.getPassword(8);
        Integer orgId = org.getOrgId();

        //保存清单模板
        baseMapper.insertStTemplate(org);

        //保存部门
        Dept dept = new Dept();
        dept.setDeptCode("111");
        dept.setDeptName(org.getOrgName());
        dept.setShortName(org.getShortName());
        dept.setFullName(org.getShortName());
        dept.setIsFinalProfitunit(false);
        dept.setIsProfitunit(false);
        dept.setBudgetHc(0);
        dept.setCreatorId(SecurityUtils.getUser().getId());
        dept.setCreateTime(LocalDateTime.now());
        dept.setOrgId(orgId);
        dept.setDeptStatus(true);
        deptMapper.insert(dept);
        int deptId = dept.getDeptId();

        //保存管理员
        User user = new User();
        user.setOrgId(orgId);
        user.setDeptId(deptId);
        user.setLoginName(org.getAdminName());
        user.setPassWord(ENCODER.encode(passWord));
        user.setUserEmail(org.getAdminEmail());
        user.setPhoneNumber(org.getAdminTel());
        user.setCreatorId(SecurityUtils.getUser().getId());
        user.setCreateTime(LocalDateTime.now());
        user.setUserStatus(true);
        user.setIsadmin(true);
        user.setJobNumber(org.getAdminEmail());
        user.setUserName("管理员");
        user.setUserEname("admin");
        user.setIdType("");
        user.setIdNumber("");
        user.setUserSex("男");
        user.setUserBirthday(LocalDateTime.now());
        user.setHireDate(LocalDateTime.now());
        user.setEmploymentType("全职");
        user.setJobPosition("管理员");
        user.setInternationalCountryCode(org.getAdminInternationalCountryCode());

        userMapper.insert(user);
        Integer admin_id = user.getUserId();

        //保存管理员和dept的关系表
        UserDept userDept = new UserDept();
        userDept.setUserId(admin_id);
        userDept.setDeptId(deptId);
        userDept.setJobPosition("管理员");
        userDept.setCreatorId(SecurityUtils.getUser().getId());
        userDept.setCreateTime(LocalDateTime.now());
        userDept.setIsMain(true);
        userDeptMapper.insert(userDept);

        //保存管理员role
        Role role = new Role();
        role.setOrgId(orgId);
        role.setRoleName("admin");
        role.setCreatorId(SecurityUtils.getUser().getId());
        role.setCreateTime(LocalDateTime.now());
        role.setRoleStatus(true);
        role.setIsadmin(true);
        roleMapper.insert(role);
        Integer role_id = role.getRoleId();

        //查询需要内置给管理员的权限
        List<Permission> adminDefaultPermissions = permissionMapper.selectAdPermission();
        //保存role的permission
        adminDefaultPermissions.forEach(menuId -> {
            RolePermission roleMenu = new RolePermission();
            roleMenu.setRoleId(role_id);
            roleMenu.setPermissionId(menuId.getPermissionId());
            roleMenu.setOrgId(orgId);
            roleMenu.setCreateTime(LocalDateTime.now());
            rolePermissionMapper.insert(roleMenu);
        });

        //保存管理员和role的关系表

        UserRole userRole = new UserRole();
        userRole.setUserId(admin_id);
        userRole.setRoleId(role_id);
        userRole.setCreatorId(SecurityUtils.getUser().getId());
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setOrgId(orgId);
        userRoleMapper.insert(userRole);


        //更新org 的 admin_id 和 role_id
        org.setAdminId(admin_id);
        org.setRoleId(role_id);
//        String orgCode = "000000".substring(0, 6 - orgId.toString().length()) + orgId.toString();
//        org.setOrgCode(orgCode);
        UpdateWrapper<Org> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("org_Id", org.getOrgId());
        baseMapper.update(org, updateWrapper);

        //保存签约公司权限
        LambdaQueryWrapper<OrgPermission> orgPermissionWrapper = Wrappers.<OrgPermission>lambdaQuery();
        orgPermissionWrapper.eq(OrgPermission::getOrgId,org.getOrgEditionId()).eq(OrgPermission::getPermissionStatus,1);
        List<OrgPermission> orgPermissions = orgPermissionService.list(orgPermissionWrapper);
        orgPermissions.stream().forEach(orgPermission -> {
            orgPermission.setOrgId(orgId);
        });
        orgPermissionService.saveBatch(orgPermissions);

        //添加汇率信息
//        insertCurrencyRate(orgId);
        //添加汇率该调存储过程
        insertCurrencyRateWithCallProcedure(orgId,null,null);

        //保存日志
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("新建签约客户：" + org.getOrgName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("新建签约客户成功，保存日志失败");
        }

        //密码发送邮件给客户
//        mailSendService.sendMailSimple("saas_helper@efreight.com.cn", new String[]{org.getAdminEmail()}, "账户密码", "您的密码是："+passWord);
        //密码发送邮件给客户
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String content = buildSendEmailContent(org, passWord);
                    mailSendService.sendMailSimple("saas_helper@efreight.com.cn", new String[]{org.getAdminEmail()}, "翌飞生态云系统启用通知", content);
                } catch (Exception e) {
                    log.info(org.getAdminName() + "您的企业编码是：" + org.getOrgCode() + "您的用户名是：" + org.getAdminName() + " 您的密码是：" + passWord + e.getMessage());
                }

            }
        }).start();*/
        return true;
    }

    private void insertCurrencyRate(Integer orgId) {
        CurrencyRateVo currencyRateVo = new CurrencyRateVo();
        currencyRateVo.setOrgId(orgId);
        currencyRateVo.setCurrencyCode("CNY");
        currencyRateVo.setCurrencyRate("1.0");
        currencyRateVo.setBeginDate("2000-01-01 00:00:00");

        EUserDetails userDetail = SecurityUtils.getUser();
        currencyRateVo.setCreatorId(userDetail.getId());
        currencyRateVo.setCreatorName("系统 " + userDetail.getUserEmail());
        currencyRateVo.setCreateTime(LocalDateTime.now());

        remoteServiceToAF.createCurrencyRate(currencyRateVo);
    }

    private void insertCurrencyRateWithCallProcedure(Integer orgId,String passWord,String passWordVerification) {
        baseMapper.insertCurrencyRateWithCallProcedure(orgId,passWord,passWordVerification);
    }

    @Override
    public IPage<Org> getOrgPage(Page page, Org org) {
//        QueryWrapper<Org> queryWrapper = new QueryWrapper<>();
//        if (StrUtil.isNotBlank(org.getOrgName())) {
//            queryWrapper.like("org_name", "%" + org.getOrgName() + "%");
//        }
//        if (StrUtil.isNotBlank(org.getOrgCode())) {
//            queryWrapper.like("org_code", "%" + org.getOrgCode() + "%");
//        }
//        if (StrUtil.isNotBlank(org.getOrgEname())) {
//            queryWrapper.like("org_ename", "%" + org.getOrgEname() + "%");
//        }
//        if (org.getOrgStatus() != null) {
//            queryWrapper.eq("org_status", org.getOrgStatus());
//        }
//        //倒序排列
//        queryWrapper.orderByDesc("org_id");
//        //
//        return baseMapper.selectPage(page, queryWrapper);
        return baseMapper.getDeptList(page, org.getOrgCode(), org.getOrgName(), org.getOrgEname(), org.getOrgStatus(),org.getOrgType(),org.getAdminEmail(),org.getDemandPersonId(),org.getAdminTel());
    }

    @Override
    public Org getOrgByID(Integer orgId) {
        QueryWrapper<Org> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_Id", orgId);
        Org org = baseMapper.selectOne(queryWrapper);
        //根据签约公司ID查询清单模板配置信息
        OrgTemplateConfig orgTemplateConfig = baseMapper.getStatementTemplateConfig(orgId);
        if(orgTemplateConfig != null){
            org.setStatementTemplateAeExcelCn(orgTemplateConfig.getStatementTemplateAeExcelCn());
            org.setStatementTemplateAeExcelEn(orgTemplateConfig.getStatementTemplateAeExcelEn());
            org.setStatementTemplateAiExcelCn(orgTemplateConfig.getStatementTemplateAiExcelCn());
            org.setStatementTemplateAiExcelEn(orgTemplateConfig.getStatementTemplateAiExcelEn());
            org.setStatementTemplateSeExcelCn(orgTemplateConfig.getStatementTemplateSeExcelCn());
            org.setStatementTemplateSeExcelEn(orgTemplateConfig.getStatementTemplateSeExcelEn());
            org.setStatementTemplateSiExcelCn(orgTemplateConfig.getStatementTemplateSiExcelCn());
            org.setStatementTemplateSiExcelEn(orgTemplateConfig.getStatementTemplateSiExcelEn());
            org.setStatementTemplateTeExcelCn(orgTemplateConfig.getStatementTemplateTeExcelCn());
            org.setStatementTemplateTeExcelEn(orgTemplateConfig.getStatementTemplateTeExcelEn());
            org.setStatementTemplateTiExcelCn(orgTemplateConfig.getStatementTemplateTiExcelCn());
            org.setStatementTemplateTiExcelEn(orgTemplateConfig.getStatementTemplateTiExcelEn());
            org.setStatementTemplateLcExcelCn(orgTemplateConfig.getStatementTemplateLcExcelCn());
            org.setStatementTemplateLcExcelEn(orgTemplateConfig.getStatementTemplateLcExcelEn());
            org.setStatementTemplateIoExcelCn(orgTemplateConfig.getStatementTemplateIoExcelCn());
            org.setStatementTemplateIoExcelEn(orgTemplateConfig.getStatementTemplateIoExcelEn());
        }
        int totalUser = userMapper.countByOrgId(orgId);
        org.setTotalUser(totalUser);
        return org;
    }

    @Override
    public Org getOneByCode(String orgCode) {
        return baseMapper.getOneByCode(orgCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOrg(Org org) {
       /* LambdaQueryWrapper<User> userWrapperForEmail = Wrappers.<User>lambdaQuery();
        userWrapperForEmail.eq(User::getIsadmin, 1).eq(User::getUserEmail, org.getAdminEmail()).ne(User::getUserId, org.getAdminId());
        User oneForEmail = userMapper.selectOne(userWrapperForEmail);
        if (oneForEmail != null) {
            throw new RuntimeException("管理邮箱已被使用");
        }*/
        //管理员邮箱不可重复
    	
    	LambdaQueryWrapper<User> userWrapperForPhone = Wrappers.<User>lambdaQuery();
    	userWrapperForPhone.eq(User::getIsadmin, 1).eq(User::getInternationalCountryCode, org.getAdminInternationalCountryCode()).eq(User::getPhoneNumber, org.getAdminTel()).ne(User::getUserId, org.getAdminId());
        User oneForPhone = userMapper.selectOne(userWrapperForPhone);
        if (oneForPhone != null) {
            throw new RuntimeException("手机号已存在");
        }
    	
        Integer count = baseMapper.countByEmail(org.getAdminEmail(),org.getAdminId());
        if(count>0){
            throw new RuntimeException("管理邮箱已被使用");
        }
        org.setEditTime(LocalDateTime.now());
        org.setEditorId(SecurityUtils.getUser().getId());
        UpdateWrapper<Org> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("org_Id", org.getOrgId());
        baseMapper.update(org, updateWrapper);
        //修改清单模板配置信息(先查询，没有则新增)
        OrgTemplateConfig orgTemplateConfig = baseMapper.getStatementTemplateConfig(org.getOrgId());
        if(orgTemplateConfig != null){
            baseMapper.updateOrgTemplateConfig(org);
        }else{
            baseMapper.insertStTemplate(org);
        }
        baseMapper.updateUser(SecurityUtils.getUser().getOrgId(), org.getAdminId(), org.getAdminEmail(), org.getAdminTel());
        //修改 对应签约公司 id 下的部门
        //部门名称、部门简称、显示名称（所有子集部门）
        if("need".equals(org.getUpdateDeptNameFlag())){
            baseMapper.updateDeptName(org);
            baseMapper.updateFullName(org);
        }
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("修改签约客户：" + org.getOrgName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改签约客户成功，保存日志失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeOrgById(Integer orgId) {
        QueryWrapper<Org> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_Id", orgId);
        Org org = baseMapper.selectOne(queryWrapper);
        Map columnMap = new HashMap();
        columnMap.put("org_Id", orgId);
        baseMapper.deleteByMap(columnMap);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("删除签约客户：" + orgId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改签约客户成功，保存日志失败");
        }
        return true;
    }

    @Override
    public List<Org> listTrees() {
        // TODO Auto-generated method stub
        return this.list();
    }

    @Override
    public List<Org> listCurrentUserTrees() {
        Integer orgId = SecurityUtils.getUser().getOrgId();

        QueryWrapper<Org> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_Id", orgId);
        Org org = baseMapper.selectOne(queryWrapper);
        QueryWrapper<Org> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.like("org_Code", org.getOrgCode() + "%");
        return baseMapper.selectList(queryWrapper1);
    }

    /**
     * 重置密码
     *
     * @param userId
     */
    @Override
    public void resetPassward(Integer userId) {
        //查询邮件
        User resultUser = userMapper.selectById(userId);
        if (resultUser == null) {
            throw new RuntimeException("该管理员不存在");
        } else if (StrUtil.isBlank(resultUser.getUserEmail())) {
            throw new RuntimeException("该管理员未设置邮箱,请设置好邮箱再重置密码");
        }
        String passWord = PassGenerator.getPassword(8);
        User user = new User();
        user.setUserId(userId);
        user.setPassWord(ENCODER.encode(passWord));
        user.setPassWordVerification(new String(Base64Utils.encode((passWord).getBytes())));
        userMapper.updateById(user);

        //清缓存
        try {
            cacheManager.getCache("user_details").clear();
        } catch (Exception e) {
            log.info("--------------redis清缓存失败------------------");
        }

        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("重置密码");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("签约客户管理员 [" + resultUser.getLoginName() + "] 重置密码");
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("签约客户重置密码成功，保存日志失败");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //根据用户ID查询企业信息
                    Org dbOrg = baseMapper.selectById(resultUser.getOrgId());
                    if (null == dbOrg) {
                        throw new RuntimeException("未查询到该用户企业信息！userID:" + resultUser.getUserId());
                    }
                    String content = buildSendEmailContent(dbOrg, passWord);
                    UserVo userVo = new UserVo();
                    BeanUtils.copyProperties(userMapper.selectById(1),userVo);
                    mailSendService.sendHtmlMailNewForHrs(false,new String[]{resultUser.getUserEmail()}, null,null, "翌飞生态云系统启用通知", content,null,userVo);
                } catch (Exception e) {
                    log.info("签约客户重置管理员" + resultUser.getUserName() + "密码为--[" + passWord + "]---,发送邮件失败,原因：" + e.getMessage());
                }
            }
        }).start();

    }


//	/**
//	 * 构建部门树
//	 *
//	 * @param depts 部门
//	 * @return
//	 */
//	private List<OrgTree> getDeptTree(List<Org> orgs) {
//		List<OrgTree> treeList = orgs.stream()
//			.filter(org -> !org.getOrgId().equals(org.getParentId()))
//			.map(dept -> {
//				OrgTree node = new OrgTree();
//				node.setId(dept.getOrgId());
//				//node.setParentId(dept.getParentId());
//				node.setName(dept.getOrgName());
//				return node;
//			}).collect(Collectors.toList());
//		return TreeUtil.buildByLoop(treeList, 0);
//	}

    private String buildSendEmailContent(Org org, String password) {
        StringBuilder builder = new StringBuilder();
        builder.append("翌飞生态云系统启用通知");
        builder.append("<br />");
        builder.append("企业编码：");
        builder.append(org.getOrgCode());
        builder.append("<br />");
        builder.append("企业名称：");
        builder.append(org.getOrgName());
        builder.append("<br />");
        builder.append("管理员账号：");
        builder.append(org.getAdminName());
        builder.append("<br />");
        builder.append("管理员密码：");
        builder.append(password);
        return builder.toString();
    }


    @Override
    public List<Org> queryModelOrg() {
        LambdaQueryWrapper<Org> wrapper = Wrappers.<Org>lambdaQuery();
        wrapper.eq(Org::getOrgType, 0).eq(Org::getOrgStatus, true);
        return  baseMapper.selectList(wrapper);
    }

    @Override
    public List<Org> listOrg() {
        LambdaQueryWrapper<Org> wrapper = Wrappers.<Org>lambdaQuery();
        wrapper.ne(Org::getOrgType, 0).eq(Org::getOrgStatus, true);
        return  baseMapper.selectList(wrapper);
    }

    @Override
    public List<Org> getSignTemplate(Org org) {
        QueryWrapper<Org> queryWrapper = new QueryWrapper<>();
        if (org.getOrgStatus() != null) {
            queryWrapper.eq("org_status", org.getOrgStatus());
        }
        if (org.getOrgName() != null && !"".equals(org.getOrgName())) {
            queryWrapper.like("org_name", org.getOrgName());
        }
        queryWrapper.eq("org_type", 0);
        return  baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<OrgInterface> queryInterfaceList(Integer orgId) {
        return  baseMapper.queryInterfaceList(orgId);
    }

    @Override
    public void saveInterface(OrgInterface orgInterface) {
        orgInterface.setCreatorId(SecurityUtils.getUser().getId());
        orgInterface.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        orgInterface.setCreateTime(LocalDateTime.now());
        baseMapper.saveInterface(orgInterface);
    }

    @Override
    public void editInterface(OrgInterface orgInterface) {
        orgInterface.setEditorId(SecurityUtils.getUser().getId());
        orgInterface.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        orgInterface.setEditTime(LocalDateTime.now());
        baseMapper.editInterface(orgInterface);
    }

    @Override
    public OrgInterface getInterface(Integer orgId, String apiType) {
        OrgInterface orgInterface = null;
        if(null != orgId){
            orgInterface = this.baseMapper.getInterface(orgId, apiType);
        }
        return orgInterface;
    }

    @Override
    public List<AFVPRMCategory> listCategory(String categoryName) {
        if(StringUtils.isEmpty(categoryName)){
            return Collections.emptyList();
        }
        return this.baseMapper.listCategory(categoryName);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveEfOrgUser(String phoneArea,String phone,String email,String orgName,String passWordVerification,String userName,String orgFromRemark,String orgFromRemark2) {
        //校验管理员账户和邮箱
    	String str = "";
        LambdaQueryWrapper<User> userWrapper = Wrappers.<User>lambdaQuery();
        userWrapper.eq(User::getIsadmin, 1).eq(User::getLoginName, phoneArea+"-"+phone);
        User one = userMapper.selectOne(userWrapper);
        if (one != null) {
        	 str = "msg-账户已被使用";
             throw new RuntimeException("账户已被使用");
        }
        LambdaQueryWrapper<User> userAminWrapper = Wrappers.<User>lambdaQuery();
        userAminWrapper.eq(User::getIsadmin, 1).eq(User::getInternationalCountryCode, phoneArea).eq(User::getPhoneNumber, phone);
        User internationalCountryCode = userMapper.selectOne(userAminWrapper);
        if (internationalCountryCode != null) {
        	str = "msg-手机号已存在";
           throw new RuntimeException("手机号已存在");
        }
        LambdaQueryWrapper<User> userAminWrapperTwo = Wrappers.<User>lambdaQuery();
        userAminWrapperTwo.eq(User::getIsadmin, 0).eq(User::getInternationalCountryCode, phoneArea).eq(User::getPhoneNumber, phone);
        User internationalCountryCodeTwo = userMapper.selectOne(userAminWrapperTwo);
        if (internationalCountryCodeTwo != null) {
        	str = "msg-手机号已存在";
            throw new RuntimeException("手机号已存在");
        }
        
        if(email!=null&&!"".equals(email)) {
        	 //管理员邮箱不可与其他签约公司管理员重复
            Integer count = baseMapper.countByEmail1(email);
            if(count>0){
            	str = "msg-邮箱已存在";
                throw new RuntimeException("邮箱已存在");
            }
            //管理员邮箱不可与普通用户邮箱重复
            Integer count1 = baseMapper.countByEmail2(email);
            if(count1>0){
            	str = "msg-邮箱已存在";
                throw new RuntimeException("邮箱已存在");
            }
        }else {
        	email=null;
        }
        
        //校验企业名称
        LambdaQueryWrapper<Org> orgWrapperTwo = Wrappers.<Org>lambdaQuery();
        orgWrapperTwo.eq(Org::getOrgName, orgName);
        Org orgTwo = baseMapper.selectOne(orgWrapperTwo);
        if (orgTwo != null) {
        	str = "msg-企业名称已存在";
            throw new RuntimeException("企业名称已存在");
        }
        
        Org org = new Org();
        //保存org
        org.setCreateTime(LocalDateTime.now());
        //org.setCreatorId();
        org.setOrgType(2);//个人用户
        org.setCoopId(null);//这个渠道来的不需要 使用客商资料
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        org.setStopDate(LocalDateTime.parse("2099-12-31 23:59:59", df));//时效时间设置
        org.setOrgUserCount(1);
        LambdaQueryWrapper<Org> wrapperEditionId = Wrappers.<Org>lambdaQuery();
        wrapperEditionId.eq(Org::getOrgType, 0).eq(Org::getOrgStatus, true).eq(Org::getOrgName, "体验版");
        Org orgEditionId = baseMapper.selectOne(wrapperEditionId);
        org.setOrgEditionId(orgEditionId.getOrgId());//体验版
        org.setAdminEmail(email);
        org.setAdminTel(phone);
        org.setAdminName("@"+phoneArea+phone+"");
        org.setRcEmail(email);
        org.setOrgName(orgName);
        org.setOrgStatus(true);
        org.setShortName(orgName);
        org.setOrgEname(orgName);
        org.setShortEname(orgName);
        org.setAdminInternationalCountryCode(phoneArea);
        org.setOrgFromRemark(orgFromRemark);
        org.setOrgFromRemark2(orgFromRemark2);
        baseMapper.insert(org);
        String passWord = PassGenerator.getPassword(8);
        
        Integer orgId = org.getOrgId(); // 过会检验一下

        //保存部门
        Dept dept = new Dept();
        dept.setDeptCode("111");
        dept.setDeptName(org.getOrgName());
        dept.setShortName(org.getShortName());
        dept.setFullName(org.getShortName());
        dept.setIsFinalProfitunit(false);
        dept.setIsProfitunit(false);
        dept.setBudgetHc(0);
        dept.setCreatorId(orgId);
        dept.setCreateTime(LocalDateTime.now());
        dept.setOrgId(orgId);
        dept.setDeptStatus(true);
        deptMapper.insert(dept);
        int deptId = dept.getDeptId();

        //保存管理员
        User user = new User();
        user.setOrgId(orgId);
        user.setDeptId(deptId);
        user.setLoginName(org.getAdminName());
        user.setPassWordVerification(new String(Base64Utils.encode((passWord).getBytes())));
        user.setPassWord(ENCODER.encode(passWord));
        user.setUserEmail(email);
        user.setPhoneNumber(org.getAdminTel());
        user.setCreatorId(orgId);
        user.setCreateTime(LocalDateTime.now());
        user.setUserStatus(true);
        user.setIsadmin(true);
        user.setJobNumber(email!=null?org.getAdminEmail():org.getAdminName());
        user.setUserName(userName);
        user.setUserEname(userName);
//        user.setUserName("管理员");
//        user.setUserEname("admin");
        user.setIdType("");
        user.setIdNumber("");
        user.setUserSex("男");
        user.setUserBirthday(LocalDateTime.now());
        user.setHireDate(LocalDateTime.now());
        user.setEmploymentType("全职");
        user.setJobPosition("管理员");
        user.setInternationalCountryCode(org.getAdminInternationalCountryCode());

        userMapper.insert(user);
        Integer admin_id = user.getUserId();

        //保存管理员和dept的关系表
        UserDept userDept = new UserDept();
        userDept.setUserId(admin_id);
        userDept.setDeptId(deptId);
        userDept.setJobPosition("管理员");
        userDept.setCreatorId(orgId);
        userDept.setCreateTime(LocalDateTime.now());
        userDept.setIsMain(true);
        userDeptMapper.insert(userDept);

        //保存管理员role
        Role role = new Role();
        role.setOrgId(orgId);
        role.setRoleName("admin");
        role.setCreatorId(orgId);
        role.setCreateTime(LocalDateTime.now());
        role.setRoleStatus(true);
        role.setIsadmin(true);
        roleMapper.insert(role);
        Integer role_id = role.getRoleId();

        //查询需要内置给管理员的权限
        List<Permission> adminDefaultPermissions = permissionMapper.selectAdPermission();
        //保存role的permission
        adminDefaultPermissions.forEach(menuId -> {
            RolePermission roleMenu = new RolePermission();
            roleMenu.setRoleId(role_id);
            roleMenu.setPermissionId(menuId.getPermissionId());
            roleMenu.setOrgId(orgId);
            roleMenu.setCreateTime(LocalDateTime.now());
            rolePermissionMapper.insert(roleMenu);
        });

        //保存管理员和role的关系表

        UserRole userRole = new UserRole();
        userRole.setUserId(admin_id);
        userRole.setRoleId(role_id);
        userRole.setCreatorId(orgId);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setOrgId(orgId);
        userRoleMapper.insert(userRole);


        //更新org 的 admin_id 和 role_id
        org.setAdminId(admin_id);
        org.setRoleId(role_id);
        UpdateWrapper<Org> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("org_Id", orgId);
        baseMapper.update(org, updateWrapper);

        //保存签约公司权限
        LambdaQueryWrapper<OrgPermission> orgPermissionWrapper = Wrappers.<OrgPermission>lambdaQuery();
        orgPermissionWrapper.eq(OrgPermission::getOrgId,org.getOrgEditionId()).eq(OrgPermission::getPermissionStatus,1);
        List<OrgPermission> orgPermissions = orgPermissionService.list(orgPermissionWrapper);
        orgPermissions.stream().forEach(orgPermission -> {
            orgPermission.setOrgId(orgId);
        });
        orgPermissionService.saveBatch(orgPermissions);

        //添加汇率该调存储过程
        insertCurrencyRateWithCallProcedure(orgId,ENCODER.encode(passWordVerification),new String(Base64Utils.encode((passWordVerification).getBytes())));

        //调用完存储过程 更新普通用户 校验密码
//        LambdaQueryWrapper<User> userWrapperP = Wrappers.<User>lambdaQuery();
//        userWrapperP.eq(User::getIsadmin, 0).eq(User::getInternationalCountryCode, phoneArea).eq(User::getPhoneNumber, phone).eq(User::getOrgId, orgId);
//        User userAminWrapperPInfo= userMapper.selectOne(userWrapperP);
//        //密码跟存储过程保持一致
//        userAminWrapperPInfo.setPassWordVerification(new String(Base64Utils.encode(("12345678").getBytes())));
//        userMapper.update(userAminWrapperPInfo, userWrapperP);
        //更新签约公司 org_code
        LambdaQueryWrapper<Org> wrapperUpdata = Wrappers.<Org>lambdaQuery();
        wrapperUpdata.eq(Org::getOrgId, orgId);
        org.setOrgCode("REGIST-"+orgId);
//        org.setOrgName(org.getOrgCode());
        baseMapper.update(org, updateWrapper);
        //保存日志
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("签约客户");
            logBean.setOpInfo("新建签约客户：" + org.getOrgName());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("新建签约客户成功，保存日志失败");
        }
        str = orgId+"";
        return str;
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean orgConfigure(Org org) {
		try {
			org.setEditTime(LocalDateTime.now());
	        org.setEditorId(SecurityUtils.getUser().getId());
	        UpdateWrapper<Org> updateWrapper = new UpdateWrapper<>();
	        updateWrapper.eq("org_Id", org.getOrgId());
	        baseMapper.update(org, updateWrapper);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return true;
	}

    @Override
    public int enabledIntendedUser(Integer orgId) {
        Assert.notNull(orgId, "非法参数");
        return this.baseMapper.updateIntendedUser(orgId, 1);
    }

    @Override
    public int disabledIntendedUser(Integer orgId) {
        Assert.notNull(orgId, "非法参数");
        return this.baseMapper.updateIntendedUser(orgId, null);
    }

    @Override
    public int unenabledIntendedUser(Integer orgId) {
        Assert.notNull(orgId, "非法参数");
        return this.baseMapper.updateIntendedUser(orgId, 0);
    }

    @Override
    public Boolean getOrderFinanceLockView(Org org) {
        org.setOrgId(SecurityUtils.getUser().getOrgId());
        return this.baseMapper.getOrderFinanceLockView(org);
    }

	@Override
	public Map getOrderFinanceLockViewNew(String businessScope) {
		return this.baseMapper.getOrderFinanceLockViewNew(SecurityUtils.getUser().getOrgId(),businessScope);
	}

	@Override
	public List<Org> getOrgChild(Org org) {
		if(StrUtil.isNotEmpty(org.getCheckJt())) {
			Map map = baseMapper.getGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			org.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			org.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
		}
		return baseMapper.getOrgChild(org);
	}
    @Override
    public List<Org> listSubOrg(Integer orgId) {
        Assert.notNull(orgId, "企业ID不能为空");
        LambdaQueryWrapper<Org> queryChainWrapper = Wrappers.lambdaQuery();
        queryChainWrapper.eq(Org::getGroupId, orgId);
        queryChainWrapper.ne(Org::getOrgId, orgId);
        queryChainWrapper.orderByAsc(Org::getOrgName);
        return this.baseMapper.selectList(queryChainWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSubOrg(Integer orgId,Integer suborgCount) {
        try {
            Org org = baseMapper.selectById(orgId);
            //删除时，需要判断 如果 没有子公司，则 父公司的 group_id  也改为 NULL
            if(suborgCount == 1){
                Org parentOrg = baseMapper.selectById(org.getGroupId());
                //更新父公司
                parentOrg.setGroupId(null);
                UpdateWrapper<Org> updateWrapperP = new UpdateWrapper<>();
                updateWrapperP.eq("org_Id", parentOrg.getOrgId());
                baseMapper.update(parentOrg, updateWrapperP);
            }
            //更新子公司
            org.setGroupId(null);
            UpdateWrapper<Org> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("org_Id", orgId);
            baseMapper.update(org, updateWrapper);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return true;
    }

    @Override
    public List<Org> selectSubOrg(Org org) {
        LambdaQueryWrapper<Org> queryChainWrapper = Wrappers.lambdaQuery();
        if(!"".equals(org.getOrgName()) && org.getOrgName() != null){
            queryChainWrapper.and(i -> i.like(Org::getOrgCode, org.getOrgName()).or().like(Org::getOrgName, org.getOrgName()));
        }
        queryChainWrapper.eq(Org::getOrgType, 1);
        queryChainWrapper.and(true, i -> i.eq(Org::getGroupId, 0).or().isNull(Org::getGroupId));
        queryChainWrapper.ne(Org::getOrgId, org.getOrgId());
        queryChainWrapper.eq(Org::getOrgStatus, true);
        queryChainWrapper.orderByAsc(Org::getOrgName);
        return this.baseMapper.selectList(queryChainWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSubOrg(SubOrgBean subOrgBean) {

        Assert.notNull(subOrgBean.getOrgId(), "父签约公司ID不能为空");

        Org parentOrg = baseMapper.selectById(subOrgBean.getOrgId());//父签约公司信息
        //将父公司 的 group_id 也写为 父公司 org_id(group_id为空再更新)
        if(parentOrg.getGroupId() == null){
            parentOrg.setGroupId(subOrgBean.getOrgId());
            baseMapper.updateById(parentOrg);
        }
        List<Org> selectionSubOrgs = subOrgBean.getSelectionSubOrgs();
        selectionSubOrgs.forEach(subOrg -> {
            Org org = baseMapper.selectById(subOrg.getOrgId());
            if(org.getGroupId() != null && org.getGroupId() != 0){
                throw new RuntimeException("数据有变化，请重新查询选择");
            }
            org.setGroupId(subOrgBean.getOrgId());
            baseMapper.updateById(org);
            Integer coopIdP = null;
            Integer coopIdS = null;
            //插入prm_coop表，A签约公司 新增分公司 B
            //1.先插入A,A的一些信息为B的信息
            //1.1先判断是否在coop表中存在
            MessageInfo<CoopVo> coop = remoteCoopService.getCoopCountByCode(subOrgBean.getOrgId(),org.getOrgCode());
            MessageInfo<CoopVo> coop1 = remoteCoopService.getCoopCountByName(subOrgBean.getOrgId(),org.getOrgName());
            if(coop.getData() == null && coop1.getData() == null){//不存在
                //1.2插入A
                CoopVo coopVo = new CoopVo();
                coopVo.setCoop_code(org.getOrgCode());
                coopVo.setCoop_name(org.getOrgName());
                coopVo.setCoop_ename(org.getOrgName());
                coopVo.setShort_name(org.getOrgName());
                coopVo.setShort_ename(org.getOrgName());
                coopVo.setCreator_id(SecurityUtils.getUser().getId());
                coopVo.setCreate_time(new Date());
                coopVo.setOrg_id(subOrgBean.getOrgId());
                coopVo.setDept_id(SecurityUtils.getUser().getDeptId());
                coopVo.setCoop_status(1);
                coopVo.setGroup_type("file");
                coopVo.setCoop_type("互为代理");
                coopVo.setIs_internal(1);
                coopVo.setIs_share(1);
                coopVo.setCoop_org_id(org.getOrgId());
                coopVo.setBind_time(new Date());
                coopVo.setBinder("sys");
                coopVo.setBinder_id(0);
                MessageInfo<Integer> coopId = remoteCoopService.remoteSaveCoop(coopVo);
                coopIdP = coopId.getData();
            }

            //2.插入B,B的一些信息为A的信息
            //2.1先判断是否在coop表中存在
            MessageInfo<CoopVo> coop2 = remoteCoopService.getCoopCountByCode(org.getOrgId(),parentOrg.getOrgCode());
            MessageInfo<CoopVo> coop3 = remoteCoopService.getCoopCountByName(org.getOrgId(),parentOrg.getOrgName());
            if(coop2.getData() == null && coop3.getData() == null){//不存在
                //2.2插入B
                CoopVo coopVo1 = new CoopVo();
                coopVo1.setCoop_code(parentOrg.getOrgCode());
                coopVo1.setCoop_name(parentOrg.getOrgName());
                coopVo1.setCoop_ename(parentOrg.getOrgName());
                coopVo1.setShort_name(parentOrg.getOrgName());
                coopVo1.setShort_ename(parentOrg.getOrgName());
                coopVo1.setCreator_id(SecurityUtils.getUser().getId());
                coopVo1.setCreate_time(new Date());
                coopVo1.setOrg_id(org.getOrgId());
                coopVo1.setDept_id(SecurityUtils.getUser().getDeptId());
                coopVo1.setCoop_status(1);
                coopVo1.setGroup_type("file");
                coopVo1.setCoop_type("互为代理");
                coopVo1.setIs_internal(1);
                coopVo1.setIs_share(1);
                coopVo1.setCoop_org_id(parentOrg.getOrgId());
                coopVo1.setBind_time(new Date());
                coopVo1.setBinder("sys");
                coopVo1.setBinder_id(0);
                coopVo1.setCoop_org_coop_id(coopIdP);
                MessageInfo<Integer> coopId1 = remoteCoopService.remoteSaveCoop(coopVo1);
                coopIdS = coopId1.getData();
            }
            //3.更新A的coop_org_coop_id为B的coop_id
            baseMapper.updateCoopById(coopIdP,coopIdS);
        });
    }

}
