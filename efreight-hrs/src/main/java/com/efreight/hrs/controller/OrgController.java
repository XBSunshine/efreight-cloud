package com.efreight.hrs.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.*;
import com.efreight.hrs.service.*;
import com.qiniu.util.Auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/org")
@Slf4j
public class OrgController {
    private final OrgService orgService;
    private final OrgPermissionService orgPermissionService;
    private final SourceLoginOfEfService sourceLoginOfEfService;
    private final OrgCouldUserService orgCouldUserService;
    private final OrgOrderConfigService orgOrderConfigService;
    private final OrgServiceMealConfigService orgServiceMealConfigService;
    private final OrgBankConfigService orgBankConfigService;

    /**
     * 通过ID查询
     *
     * @param id ID
     * @return MessageInfo
     */
    @GetMapping("/{id}")
    public MessageInfo getById(@PathVariable Integer id) {
        Org org = orgService.getOrgByID(id);

        org.setOrderConfig(orgOrderConfigService.findByOrgId(id));
        org.setServiceMealConfigList(orgServiceMealConfigService.listByOrgId(id));
        org.setOrgBankConfigList(orgBankConfigService.findByOrgId(id));
        return MessageInfo.ok(org);
    }

    @GetMapping("/getUpToken")
    public MessageInfo getUpToken() {
        String accessKey = "G-lJUKv86VsufncgJ7s9gAI8h4GVwFHNDEnagYRu";
        String secretKey = "edW9yTbeuQntu8-5eeXndcLFef0LPKZ0v_6MnROJ";
        String bucket = "youcang";
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        System.out.println(upToken);
        return MessageInfo.ok(upToken);
    }

    /**
     * 添加
     *
     * @param org 实体
     * @return success/false
     */
//	@SysLog("添加机构")
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_org_add')")
    public MessageInfo save(@Valid @RequestBody Org org) {

        return MessageInfo.ok(orgService.saveOrg(org));
    }

    /**
     * 删除
     *
     * @param id ID
     * @return success/false
     */
    //@SysLog("删除机构")
    @DeleteMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('sys_org_del')")
    public MessageInfo removeById(@PathVariable Integer id) {
        return MessageInfo.ok(orgService.removeOrgById(id));
    }

    /**
     * 编辑
     *
     * @param org 实体
     * @return success/false
     */
//	@SysLog("编机构")
    @PutMapping
    @PreAuthorize("@pms.hasPermission('sys_org_edit')")
    public MessageInfo update(@Valid @RequestBody Org org) {
        org.setEditTime(LocalDateTime.now());
        if (org.getCoopId() != null && org.getCoopId().intValue() == 123456789) {
            org.setCoopId(null);
        }
        return MessageInfo.ok(orgService.updateOrg(org));
    }


    /**
     * 返回集合
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/page")
    public MessageInfo listDeptTrees(Page page, OrgCouldUser couldUser) {
        try {
            IPage<OrgCouldUser> pagee = orgCouldUserService.queryCouldUser(page, couldUser);
            return MessageInfo.ok(pagee);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 返回当前用户树形菜单集合
     *
     * @return 树形菜单
     */
    @GetMapping(value = "/user-tree")
    public MessageInfo listCurrentUserDeptTrees() {
        return MessageInfo.ok(orgService.listCurrentUserTrees());
    }

    /**
     * 保存签约客户的权限
     *
     * @return 树形菜单
     */
//    @PreAuthorize("@pms.hasPermission('sys_org_permission')")
    @PostMapping(value = "/org-permission")
    public MessageInfo SaveOrgPermission(@RequestBody Map<String, Object> para) {
        return MessageInfo.ok(orgPermissionService.saveOrgPermission(para));
    }

    /**
     * 保存签约模板和权限
     *
     * @return
     */
    @PostMapping(value = "/saveSignTemplateAndPermission")
    public MessageInfo saveSignTemplateAndPermission(@RequestBody Map<String, Object> para) {
        try {
            return MessageInfo.ok(orgPermissionService.saveSignTemplateAndPermission(para));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    /**
     * 修改签约模板和权限
     *
     * @return
     */
    @PostMapping(value = "/editSignTemplateAndPermission")
    public MessageInfo editSignTemplateAndPermission(@RequestBody Map<String, Object> para) {
        try {
            return MessageInfo.ok(orgPermissionService.editSignTemplateAndPermission(para));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取所有签约客户列表
     *
     * @return
     */
    @GetMapping
    public MessageInfo getOrgList() {
        try {
            return MessageInfo.ok(orgService.listOrg());
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 编进页面查询模板列表
     *
     * @return
     */
    @GetMapping("getOrgList")
    public MessageInfo queryOrgList() {
        try {
            List<Org> list = orgService.queryModelOrg();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 重置密码
     *
     * @param userId
     * @return
     */
//    @PreAuthorize("@pms.hasPermission('sys_org_resetpass')")
    @PutMapping("/resetPassward/{userId}")
    public MessageInfo resetPassward(@PathVariable Integer userId) {
        try {
            orgService.resetPassward(userId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询类型为0（模板）的签约公司
     *
     * @return
     */
    @GetMapping("/queryModelOrg")
    public MessageInfo queryModelOrg() {
        try {
            List<Org> list = orgService.queryModelOrg();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/getSignTemplate")
    public MessageInfo getSignTemplate(Org org) {
        try {
            List<Org> list = orgService.getSignTemplate(org);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @RequestMapping(value = "/queryInterfaceList", method = RequestMethod.POST)
    public MessageInfo queryInterfaceList(Integer orgId) {
        try {
            List<OrgInterface> list = orgService.queryInterfaceList(orgId);
            return MessageInfo.ok(list);

        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/saveInterface")
    public MessageInfo saveInterface(@RequestBody OrgInterface orgInterface) {
        try {
            orgService.saveInterface(orgInterface);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    @PostMapping(value = "/editInterface")
    public MessageInfo editInterface(@RequestBody OrgInterface orgInterface) {
        try {
            orgService.editInterface(orgInterface);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }

    }

    @GetMapping(value = "/shippingBillConfig/{orgId}")
    public MessageInfo shippingBillConfig(@PathVariable("orgId") Integer orgId,
                                          @Param("apiType") String apiType) {
        try {
            OrgInterface orgInterface = orgService.getInterface(orgId, apiType);
            return MessageInfo.ok(orgInterface);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    //接口类型
    @GetMapping("/listCategory/{categoryName}")
    public MessageInfo listCategory(@PathVariable("categoryName") String categoryName) {
        try {
            List<AFVPRMCategory> categories = orgService.listCategory(categoryName);
            return MessageInfo.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 用于EF对接注册账户-- 只开放当前CTR
     *
     * @param phoneArea
     * @param phone
     * @param email
     * @return
     */
    @PostMapping(value = "/saveEfOrgUser")
    public MessageInfo saveEfOrgUser(String phoneArea, String phone, String email, String orgName, String passWordVerification, String userName,String orgFromRemark,String orgFromRemark2) {
        try {
            if (StringUtils.isEmpty(phoneArea)) {
                return MessageInfo.failed("手机区号为空");
            }
            if (StringUtils.isEmpty(phone)) {
                return MessageInfo.failed("手机号为空");
            }
            //        if(StringUtils.isEmpty(email)) {
            //        	return MessageInfo.failed("邮件为空");
            //        }
            if (StringUtils.isEmpty(orgName)) {
                return MessageInfo.failed("企业名称为空");
            }
            if (StringUtils.isEmpty(userName)) {
                return MessageInfo.failed("用户名称为空");
            }
            if (StringUtils.isEmpty(passWordVerification)) {
                return MessageInfo.failed("用户密码为空");
            }
            String str = "";

            if ("86".equals(phoneArea)) {//大陆
                phoneArea = "00" + phoneArea;
            } else {
                //香港852
                //澳门853
                //台湾886
                phoneArea = "0" + phoneArea;
            }
            str = orgService.saveEfOrgUser(phoneArea, phone, email, orgName, passWordVerification, userName,orgFromRemark,orgFromRemark2);
            if (str == null) {
                return MessageInfo.failed("系统操作异常");
            }
            if (str.contains("msg")) {
                return MessageInfo.failed(str.substring(str.indexOf("msg-"), str.length()));
            }
            Map map = new HashMap();
            map.put("orgId", str);
            map.put("loginName", StringUtils.isEmpty(email) ? (phoneArea + phone + "") : email);
            map.put("phoneArea", phoneArea);
            map.put("phone", phone);
            map.put("email", email);
            return MessageInfo.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通行证登录
     *
     * @param token
     * @return
     */
    @PostMapping(value = "/sourceLoginOfEf")
    public MessageInfo sourceLoginOfEf(String token) {
        try {
            Map map = sourceLoginOfEfService.getToken(token);
            return MessageInfo.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 云用户统计
     *
     * @return
     */
    @GetMapping(value = "/could/user")
    public MessageInfo seachCouldUser(Page page, OrgCouldUser couldUser) {
        try {
            IPage<OrgCouldUser> pagee = orgCouldUserService.queryCouldUser(page, couldUser);
            //清空orgId
            pagee.getRecords().stream().forEach((user) -> {
                user.setOrgId(null);
            });
            return MessageInfo.ok(pagee);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }

    }

    /**
     * 云用户统计导出
     */
    @PostMapping("/could/exportExcel")
    public void exportExcel(OrgCouldUser couldUser) {
        try {
            orgCouldUserService.exportExcelList(couldUser);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }

    /**
     * 公司配置
     *
     * @param org
     * @return
     */
    @PostMapping(value = "/configure")
    public MessageInfo orgConfigure(@Valid @RequestBody Org org) {
        try {
            orgService.orgConfigure(org);
            orgOrderConfigService.saveOrUpdate(org.getOrderConfig());
            orgBankConfigService.deleteAndSave(org.getOrgBankConfigList());
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 获取签约公司订单配置
     *
     * @param businessScope
     * @return
     */
    @GetMapping("/getOrgOrderConfig/{businessScope}")
    public MessageInfo getOrgOrderConfig( @PathVariable("businessScope") String businessScope) {
        try {
            OrgOrderConfig orgOrderConfig = orgOrderConfigService.getOrgOrderConfig(businessScope);
            return MessageInfo.ok(orgOrderConfig);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PutMapping("editIntended/{status}/{orgId}")
    public MessageInfo editIntended(@PathVariable("status") String status, @PathVariable("orgId") Integer orgId) {
        int result = 0;
        try {
            if ("enabled".equals(status)) {
                result = orgService.enabledIntendedUser(orgId);
            } else if ("disabled".equals(status)) {
                result = orgService.disabledIntendedUser(orgId);
            } else if ("unenabled".equals(status)) {
                result = orgService.unenabledIntendedUser(orgId);
            } else {
                return MessageInfo.failed("无效操作");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
        return MessageInfo.ok(result);
    }

    /**
     * 获取公司设置的锁账是否可见字段-<作废，可能有残余功能使用暂不删除>
     *
     * @return list
     */
    @GetMapping(value = "/getOrderFinanceLockView")
    public MessageInfo getOrderFinanceLockView(Org org) {
        return MessageInfo.ok(orgService.getOrderFinanceLockView(org));
    }

    /**
     * 获取公司设置的锁账是否可见字段 new 区分老板新开接口
     * 根据业务范畴+签约公司ID  查询出当前公司公共配置
     *
     * @return list
     */
    @GetMapping(value = "/getOrderFinanceLockView/{businessScope}")
    public MessageInfo getOrderFinanceLockViewNew(@PathVariable("businessScope") String businessScope) {
        Map map = orgService.getOrderFinanceLockViewNew(businessScope);
        boolean flag = true;
        if (map != null && map.containsKey("finance_lock_view")) {
            flag = (boolean) map.get("finance_lock_view");
        }
        return MessageInfo.ok(flag);
    }
    
    /**
     * 查询当前签约公司是否有子公司
     *
     * @return
     */
    @GetMapping(value = "/getOrgChild")
    public MessageInfo getOrgChild(Org org) {
        try {
        	org.setOrgId(SecurityUtils.getUser().getOrgId());
            return MessageInfo.ok(orgService.getOrgChild(org));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }

    }

    /**
     * 查询企业分公司配置信息
     * @return
     */
    @GetMapping("listSubOrg/{orgId}")
    public MessageInfo listSubOrg(@PathVariable("orgId") Integer orgId){
        List<Org> result = orgService.listSubOrg(orgId);
        return MessageInfo.ok(result);
    }

    /**
     * 删除分公司
     *
     * @param orgId ID
     * @return success/false
     */
    @DeleteMapping("/deleteSubOrg/{orgId}/{suborgCount}")
    public MessageInfo deleteSubOrg(@PathVariable Integer orgId,@PathVariable Integer suborgCount) {
        return MessageInfo.ok(orgService.deleteSubOrg(orgId,suborgCount));
    }

    /**
     * 选择子公司
     *
     *
     */
    @GetMapping(value = "/selectSubOrg")
    public MessageInfo selectSubOrg(Org org) {
        try {
            List<Org> list = orgService.selectSubOrg(org);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存分公司
     */
    @PostMapping("saveSubOrg")
    public MessageInfo saveSubOrg(@RequestBody SubOrgBean subOrgBean){
        orgService.saveSubOrg(subOrgBean);
        return MessageInfo.ok();
    }
}
