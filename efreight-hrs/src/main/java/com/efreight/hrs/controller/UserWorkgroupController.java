package com.efreight.hrs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.UserWorkgroup;
import com.efreight.hrs.pojo.org.workgroup.UserListBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupExport;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupQuery;
import com.efreight.hrs.service.UserWorkgroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户工作组服务类
 * @author lc
 * @date 2020/10/15 17:38
 */

@RestController
@AllArgsConstructor
@RequestMapping("userWorkgroup")
@Slf4j
public class UserWorkgroupController {


    private final UserWorkgroupService userWorkgroupService;

    /**
     * 查询
     */
    @GetMapping("query")
    public MessageInfo query(Page page, UserWorkgroupQuery query){
        IPage<UserWorkgroup> result = userWorkgroupService.query(page, query);
        return MessageInfo.ok(result);
    }

    /**
     * 保存
     */
    @PostMapping("save")
    public MessageInfo save(@RequestBody  UserWorkgroupBean userWorkgroupBean){
        userWorkgroupService.save(userWorkgroupBean);
        return MessageInfo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("edit")
    public MessageInfo edit(@RequestBody UserWorkgroupBean userWorkgroupBean){
        userWorkgroupService.update(userWorkgroupBean);
        return MessageInfo.ok();
    }

    /**
     * 导出
     */
    @PostMapping("export")
    public void export(UserWorkgroupQuery query, HttpServletResponse response){
        List<UserWorkgroupExport> result = userWorkgroupService.exportQuery(query);
        ExportExcel<UserWorkgroupExport> ex = new ExportExcel<>();
        String[] headers = {"业务范畴", "工作组名称", "组备注", "姓名", "邮箱", "手机号", "部门", "岗位"};
        ex.exportExcel(response, "导出EXCEL", headers, result, "Export");
    }

    @GetMapping("listUser")
    public MessageInfo listUser(){
        Integer orgId = SecurityUtils.getUser().getOrgId();
        List<UserListBean> userListBeanList = userWorkgroupService.findUser(orgId);
        return MessageInfo.ok(userListBeanList);
    }

    @GetMapping("{workgroupId}")
    public MessageInfo detail(@PathVariable("workgroupId") Integer workgroupId){
        UserWorkgroup userWorkgroup = userWorkgroupService.detail(workgroupId);
        return MessageInfo.ok(userWorkgroup);
    }

    @GetMapping(value = "/selectWorkgroup")
    public MessageInfo selectWorkgroup(String businessScope) {
        return MessageInfo.ok(userWorkgroupService.selectWorkgroup(businessScope));
    }
    @DeleteMapping("{workgroupId}")
    public MessageInfo deleteWorkgroup(@PathVariable("workgroupId") Integer workgroupId){
        userWorkgroupService.deleteByWorkgroupId(workgroupId);
        return MessageInfo.ok();
    }

    @GetMapping(value = "/selectWorkgroupByServicerId")
    public MessageInfo selectWorkgroupByServicerId(Integer servicerId) {
        return MessageInfo.ok(userWorkgroupService.selectWorkgroupByServicerId(servicerId));
    }


}
