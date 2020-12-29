package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_warehouse_letter")
public class WarehouseLetter {

    private static final long serialVersionUID = 1L;

    @TableId(value = "warehouse_letter_id", type = IdType.AUTO)
    private Integer warehouseLetterId;

    //机场代码
    private String apCode;

    //模板
    private String shipperTemplateFile;

    //模板名称
    private String shipperTemplateName;
    private String showName;
    /**
     * 是有有效
     */
    private Integer isValid;
    /**
     * 创建人ID
     */
    private Integer creatorId;
    /**
     * 创建人
     */
    private String creatorName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人ID
     */
    private Integer editorId;
    /**
     * 修改人
     */
    private String editorName;
    /**
     * 修改时间
     */
    private Date editTime;

    private String shipperTemplateFileExcel;

    private String shipperTemplateNameExcel;

    /**
     * 航司模板
     */
    @TableField(exist = false)
    private List<WarehouseLetterAttachFile> warehouseLetterAttachFiles;

}
