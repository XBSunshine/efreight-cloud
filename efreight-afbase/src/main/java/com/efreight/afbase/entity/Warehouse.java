package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_warehouse")
public class Warehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库id
     */
    @TableId(value = "warehouse_id", type = IdType.AUTO)
    private Integer warehouseId;
    /**
     * 签约公司id
     */
    private Integer orgId;
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     * 仓库代码
     */
    private String warehouseCode;
    /**
     * 机场代码
     */
    private String apCode;
    /**
     * 仓库名称
     */
    private String warehouseNameCn;

    /**
     * 仓库英文名称
     */
    private String warehouseNameEn;

    /**
     * 经度
     */
    private String warehouseLongitude;

    /**
     * 纬度
     */
    private String warehouseLatitude;

    /**
     * 导航地址
     */
    private String warehouseAddressGps;
    /**
     * 是否生效
     */
    private Integer warehouseStatus;
    /**
     * 监管等级
     */
    private String customsSupervision;
    /**
     * 海关编码
     */
    private String customsCode;
    private transient String isChange;
    /**
     * 操作代理供应商
     */
    private String agentCoopId;
    /**
     * 送货通知模板
     */
    private String deliverTemplate;
    /**
     * 交货托书模板
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private Integer shipperTemplate;

    @TableField(exist = false)
    private String shipperTemplateName;
    /**
     * 标签模板 主单
     */
    private String labelTempalteAwb;
    /**
     * 标签模板 分单
     */
    private String labelTempalteHwb;
    /**
     * 接货联系人备注
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

    /**
     * 交货人信息-身份证号
     */
    private String delivererContactRemark;
    /**
     * 交货人信息：姓名
     */
    private String delivererContactName;
    /**
     * 交货人信息：电话
     */
    private String delivererContactPhoneNumber;

}
