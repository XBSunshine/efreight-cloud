package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CS 订单管理 订单附件
 * </p>
 *
 * @author qipm
 * @since 2020-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_order_files")
public class ScOrderFiles implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "order_file_id",type = IdType.AUTO)
    private Integer orderFileId;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 附件类型：图片、文件
     */
    private String fileType;

    /**
     * 附件文件名
     */
    private String fileName;

    /**
     * 附件地址
     */
    private String fileUrl;

    /**
     * 附件备注
     */
    private String fileRemark;

    private Integer creatorId;

    private String creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<OrderFiles> fileLists;

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String url;

    private Integer isDisplay;


}
