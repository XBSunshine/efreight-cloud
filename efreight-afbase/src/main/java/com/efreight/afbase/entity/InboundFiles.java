package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 操作计划 操作出重表 照片文件
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_inbound_files")
public class InboundFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 出重文件ID
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Integer fileId;

    /**
     * 出重ID
     */
    private Integer inboundId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 照片文件名
     */
    private String fileName;

    /**
     * 照片文件路径
     */
    private String fileUrl;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;


}
