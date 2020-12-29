package com.efreight.afbase.entity.cargo.track;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/12/3 14:24
 */
@Data
public class CargoTrackQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    enum Type {
        AIR_I("AI"),
        AIR_E("AE");
        String name;
        Type(String name){
            this.name = name;
        }
    }

    /**
     * 手机号
     */
    private String phone;
    /**
     * 主单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;
    /**
     * 类型
     */
    private String type;


    /**
     * 访问IP
     */
    private String ip;

    /**
     * 业务域
     * @return
     */
    public String businessScope(){
        return Type.valueOf(this.type).name;
    }

    public void validate(){
        Assert.hasText(this.phone, "手机号不能为空");
        Assert.hasText(this.awbNumber, "主单号不能为空");
        Assert.hasText(this.type, "查询类型不能为空");
        Assert.notNull(Type.valueOf(this.type.toUpperCase()), "查询类型不存在");
    }
}
