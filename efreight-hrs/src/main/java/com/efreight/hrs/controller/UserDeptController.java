package com.efreight.hrs.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.UserDept;
import com.efreight.hrs.service.UserDeptService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
@RestController
@AllArgsConstructor
@RequestMapping("/userDept")
@Slf4j
public class UserDeptController {
    private final UserDeptService userDeptService;

    /**
     * 根据用户id查询所有负责部门
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public MessageInfo getByUserId(@PathVariable("userId") String userId) {
        try {
            List<UserDept> userDeptList = userDeptService.getByUserId(userId);
            return MessageInfo.ok(userDeptList);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 根据用户id查询所有兼职部门
     * @param userId
     * @return
     */
    @GetMapping("/partTimeJob/{userId}")
    public MessageInfo getPartTimeJobByUserId(@PathVariable("userId") String userId) {
        try {
            List<UserDept> userDeptList = userDeptService.getPartTimeJobByUserId(userId);
            return MessageInfo.ok(userDeptList);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_user_parttime_save')")
    public MessageInfo save(@RequestBody Map<String,Object> param) {
        try {
            userDeptService.save(Integer.parseInt(param.get("userId").toString()), Integer.parseInt(param.get("deptId").toString()), param.get("jobPosition").toString());
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{deptId}")
    @PreAuthorize("@pms.hasPermission('sys_user_parttime_del')")
    public MessageInfo delete(@PathVariable("userId") Integer userId, @PathVariable("deptId") Integer deptId) {
        try {
            userDeptService.delete(userId, deptId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

