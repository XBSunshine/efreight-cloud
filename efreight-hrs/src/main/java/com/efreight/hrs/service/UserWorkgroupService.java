package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.UserWorkgroup;
import com.efreight.hrs.pojo.org.workgroup.UserListBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupExport;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupQuery;

import java.util.List;

/**
 * 工作组服务类
 * @author lc
 * @date 2020/10/15 14:06
 */
public interface UserWorkgroupService extends IService<UserWorkgroup> {

    /**
     * 保存用户组信息
     * @param userWorkgroupBean 数据信息
     */
    void save(UserWorkgroupBean userWorkgroupBean);

    /**
     * 修改用户组组信息
     * @param userWorkgroupBean 数据信息
     * @return
     */
    void update(UserWorkgroupBean userWorkgroupBean);

    /**
     * 查询用户组信息
     * @param query 查询条件
     * @return
     */
    IPage<UserWorkgroup> query(Page page, UserWorkgroupQuery query);

    /**
     * 导出数据查询
     * @param query 查询条件
     * @return
     */
    List<UserWorkgroupExport> exportQuery(UserWorkgroupQuery query);

    /**
     * 根据企业ID查询有效用户信息
     * @param orgId 企业ID
     * @return
     */
    List<UserListBean> findUser(Integer orgId);

    /**
     * 详情
     * @param workgroupId 数据ID
     * @return
     */
    UserWorkgroup detail(Integer workgroupId);

    /**
     * 工作组数据源查询
     * @param businessScope 业务范畴
     * @return
     */
    List<UserWorkgroup> selectWorkgroup(String businessScope);

    /**
     * 查询当前客服所在的工作组
     * @param servicerId 责任客服ID
     * @return
     */
    List<Integer> selectWorkgroupByServicerId(Integer servicerId);

    /**
     * 删除数据
     * @param workgroupId 数据ID
     */
    void deleteByWorkgroupId(Integer workgroupId);

}
