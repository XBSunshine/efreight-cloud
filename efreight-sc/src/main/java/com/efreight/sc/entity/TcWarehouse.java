package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * TC 基础信息 堆场仓库
 * </p>
 *
 * @author caiwd
 * @since 2020-07-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tc_warehouse")
public class TcWarehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 堆场/仓库 ID
     */
    @TableId(value = "warehouse_id", type = IdType.AUTO)
    private Integer warehouseId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 堆场/仓库 代码
     */
    private String warehouseCode;

    /**
     * 港口代码
     */
    private String apCode;

    /**
     * 堆场/仓库 中文名称
     */
    private String warehouseNameCn;

    /**
     * 堆场/仓库 英文名称
     */
    private String warehouseNameEn;

    /**
     * 堆场/仓库 位置 经度
     */
    private String warehouseLongitude;

    /**
     * 堆场/仓库 位置 纬度
     */
    private String warehouseLatitude;

    /**
     * 堆场/仓库 导航地址 关键字
     */
    private String warehouseAddressGps;

    /**
     * 是否生效   1:有效  0：删除
     */
    private Integer warehouseStatus;

    /**
     * 堆场类型：堆场、仓库
     */
    private String customsSupervision;

    /**
     * 海关编码
     */
    private String customsCode;

    /**
     * 接货人信息
     */
    private String warehouseContactRemark;

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
    private LocalDateTime createTime;

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
    private LocalDateTime editTime;

    @TableField(exist = false)
    private String operateName;
    @TableField(exist = false)
    private LocalDateTime operateTime;
    @TableField(exist = false)
    private String portNameEn;
    @TableField(exist = false)
    private String warehouseCodeCheck;
    @TableField(exist = false)
    private String warehouseNameCnCheck;


}
