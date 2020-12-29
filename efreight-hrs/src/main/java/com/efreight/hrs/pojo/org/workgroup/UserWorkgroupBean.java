package com.efreight.hrs.pojo.org.workgroup;

import com.efreight.hrs.entity.UserWorkgroup;
import com.efreight.hrs.entity.UserWorkgroupDetail;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lc
 * @date 2020/10/15 15:03
 */
@Data
public class UserWorkgroupBean {

    /**
     * 数据ID
     */
    private Integer workgroupId;
    /**
     * 业务域
     */
    private String businessScope;
    /**
     * 工作组名
     */
    private String workgroupName;
    /**
     * 工作组备注
     */
    private String workgroupRemark;

    /**
     * 企业ID
     */
    private Integer orgId;

    /**
     * 用户ID
     */
    private HashSet<Integer> userIds;

    public UserWorkgroup buildUserWorkgroup(){
        UserWorkgroup userWorkgroup = new UserWorkgroup();
        userWorkgroup.setBusinessScope(this.businessScope);
        userWorkgroup.setWorkgroupName(this.workgroupName);
        userWorkgroup.setWorkgroupRemark(this.workgroupRemark);
        userWorkgroup.setOrgId(this.orgId);
        return userWorkgroup;
    }

    public List<UserWorkgroupDetail> buildUserWorkDetail(Integer workgroupId){
        if(null == userIds || userIds.isEmpty()){
            return Collections.emptyList();
        }

       return userIds.stream().map((userId)->{
            UserWorkgroupDetail userWorkgroupDetail = new UserWorkgroupDetail();
            userWorkgroupDetail.setOrgId(this.orgId);
            userWorkgroupDetail.setUserId(userId);
            userWorkgroupDetail.setWorkgroupId(workgroupId);
            return userWorkgroupDetail;
        }).collect(Collectors.toList());
    }

}
