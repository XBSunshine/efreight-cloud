package com.efreight.hrs.controller;

import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.UserAccessRecord;
import com.efreight.hrs.service.UserAccessRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lc
 * @date 2020/5/12 15:02
 */
@RestController
@RequestMapping("/userAccessRecord")
@AllArgsConstructor
@Slf4j
public class UserAccessRecordController {

    private final UserAccessRecordService userAccessRecordService;

    @PostMapping("recordMenu")
    public MessageInfo recordMenu(@RequestParam("id")Integer menuId, @RequestParam("name") String menuName,
                                  @RequestParam("path") String menuPath){
        try{
            EUserDetails user = SecurityUtils.getUser();

            UserAccessRecord userAccessRecord = new UserAccessRecord();
            userAccessRecord.setUserId(user.getId());
            userAccessRecord.setOrgId(user.getOrgId());
            userAccessRecord.setPermissionId(menuId);
            userAccessRecord.setPermissionName(menuName);
            userAccessRecord.setPath(menuPath);
            userAccessRecordService.recordAccess(userAccessRecord);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
        return MessageInfo.ok();
    }

    @GetMapping("topAccess/{number}")
    public MessageInfo topAccess(@PathVariable("number") Integer number){
        try{
            List<UserAccessRecord> accessRecords = userAccessRecordService.topAccess(SecurityUtils.getUser().getId(), number);
            return MessageInfo.ok(accessRecords);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("topActiveIndex")
    public MessageInfo topActiveIndex(){
        try{
            List<Org> activeIndexRecords = userAccessRecordService.topActiveIndex();
            return MessageInfo.ok(activeIndexRecords);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }
}
