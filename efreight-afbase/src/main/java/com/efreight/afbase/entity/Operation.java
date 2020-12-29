package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_operation")
public class Operation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型id
     */
    private String opId;

    /**
     * 操作类型代码
     */
    private String opCode;

    /**
     * 操作类型名称
     */
    private String opName;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 流程阶段
     */
    private String processType;

    /**
     * 公开给外部用户
     */
    private Boolean isPublish;

    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 备注
     */
    private String remarks;


}
