package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HRS 签约公司 服务套餐设置
 * @author lc
 * @date 2020/11/16 13:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_org_service_meal_config")
public class OrgServiceMealConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "org_service_meal_config_id", type = IdType.AUTO)
    private Integer orgServiceMealConfigId;
    /**
     * 企业ID
     */
    private Integer orgId;
    /**
     * 服务类型
     */
    private String serviceType;
    /**
     * 服务周期
     */
    private String serviceCycle;
    /**
     * 套餐量
     */
    private Integer serviceNumberMax;
    /**
     * 已经用量
     */
    private Integer serviceNumberUsed;
    /**
     * 备注信息
     */
    private String remark;

    private Integer creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private Integer editorId;
    private String editorName;
    private LocalDateTime editTime;

    /**
     * 操作类型:
     *  null或空字符: 不做任何处理
     *  add:添加，
     *  edit:修改，
     *  delete:删除
     */
    @TableField(exist = false)
    private String op;
}
