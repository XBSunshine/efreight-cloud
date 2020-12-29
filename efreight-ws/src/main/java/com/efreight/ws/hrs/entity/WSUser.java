package com.efreight.ws.hrs.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user")
public class WSUser implements Serializable {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;
    private String loginName;
    private String userEmail;
    private String phoneNumber;
    private String userName;
    private String userEname;
    private Boolean userStatus;

    /**
     * 格式为:userName userEmail 如:lc lc@efreght.cn
     */
    public String buildUserName(){
        return this.userName + " " + this.userEmail;
    }
}
