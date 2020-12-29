package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * TC 订单管理 订单附件
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tc_order_files")
public class TcOrderFiles implements Serializable {

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

    /**
     * 是否显示 1 是 0否
     */
    private Integer isDisplay;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;


}
