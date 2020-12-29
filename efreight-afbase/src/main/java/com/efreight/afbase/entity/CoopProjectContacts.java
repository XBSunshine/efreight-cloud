package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_coop_project_contacts")
public class CoopProjectContacts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库id
     */
    @TableId(value = "contacts_id", type = IdType.AUTO)
    private Integer contactsId;
    /**
     * 签约公司id
     */
    private Integer orgId;
    /**
     * 客户项目ID
     */
    private Integer projectId;
    /**
     * 联系人类型
     */
    private String contactsType;
    /**
     * 联系人姓名
     */
    private String contactsName;
    /**
     * 联系人手机号
     */
    private String phoneNumber;
    /**
     * 联系人邮箱
     */
    private String email;

    /**
     * 联系人部门
     */
    private String deptName;

    /**
     * 联系人职务
     */
    private String jobPosition;

    /**
     * 联系人座机
     */
    private String telNumber;

}
