package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * AF 订单管理 出口订单附件
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_files")
public class OrderFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单附件ID
     */
    @TableId(value = "order_file_id", type = IdType.AUTO)
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
