package com.efreight.common.security.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class EUserDetails extends User {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @Getter
    @Setter
    private Integer id;
    /**
     * 部门ID
     */
    @Getter
    @Setter
    private Integer deptId;
    /**
     * 部门ID
     */
    @Getter
    @Setter
    private Integer orgId;
    @Getter
    @Setter
    private String userEmail;

    @Getter
    @Setter
    private String userCname;

    @Getter
    @Setter
    private String userEname;

    @Getter
    @Setter
    private String phoneNumber;

    @Getter
    @Setter
    private Boolean orderEditNewPage;

    @Getter
    @Setter
    private Boolean orderSaveClosePage;


    public EUserDetails(Integer id, Integer deptId, Integer orgId, String username, String password, String userEmail, String userCname, String userEname,boolean enabled,
                        boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
                        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.deptId = deptId;
        this.orgId = orgId;
        this.userCname = userCname;
        this.userEname = userEname;
        this.userEmail = userEmail;
    }

    /**
     * <p>
     * 构建操作者用户名 <br />
     * 格式为：用户名 邮箱。<br />
     * eg： 张三 zhangsan@163.com
     * </p>
     * @return
     */
    public String buildOptName(){
        return this.userCname + " " + this.userEmail;
    }

}
