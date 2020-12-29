package com.efreight.hrs.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.pojo.org.OrgQuery;
import com.efreight.hrs.pojo.org.OrgVO;
import com.efreight.hrs.service.OrgPermissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/org-permission")
public class OrgPermissionController {

    private final OrgPermissionService orgPermissionService;

    /**
     * 查询含有某个权限的相关的企业，
     * @param query 查询条件
     * @return
     */
    @GetMapping("/eqOrg")
    public MessageInfo eqOrg(OrgQuery query){
        try{
            IPage<OrgVO> orgVOIPage = orgPermissionService.getEqPermissionOrgVoPage(query);
            return MessageInfo.ok(orgVOIPage);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询不含有某个权限相关的企业，
     * @param query 查询条件
     * @return
     */
    @GetMapping("/neOrg")
    public MessageInfo neOrg(OrgQuery query){
        try{
            IPage<OrgVO> orgVOIPage = orgPermissionService.getNePermissionOrgVoPage(query);
            return MessageInfo.ok(orgVOIPage);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 添加某个权限到企业中
     * @param orgIdSet 企业ID命令
     * @param permissionId 权限ID
     * @return
     */
    @PostMapping("save/{permissionId}")
    public MessageInfo save(@PathVariable("permissionId")Integer permissionId, @RequestBody Set<Integer> orgIdSet){
        try{
            boolean result = orgPermissionService.save(permissionId, orgIdSet);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 移除企业下某个权限
     * @param orgIdSet 企业ID集合
     * @param permissionId 权限ID
     * @return
     */
    @PostMapping("remove/{permissionId}")
    public MessageInfo remove(@PathVariable("permissionId") Integer permissionId, @RequestBody Set<Integer> orgIdSet){
        try{
            boolean result = orgPermissionService.remove(permissionId, orgIdSet);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

