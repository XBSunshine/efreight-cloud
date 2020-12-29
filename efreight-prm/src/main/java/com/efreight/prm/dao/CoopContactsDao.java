package com.efreight.prm.dao;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopContactsBean;
import org.apache.ibatis.annotations.Param;


public interface CoopContactsDao {

    //查询列表
    List<CoopContactsBean> queryCoopContactsList(Map<String, Object> paramMap);

    //插入
    void saveCoopContacts(CoopContactsBean bean);

    //插入
    Integer saveCoopContacts1(CoopContactsBean bean);

    //修改
    Integer modifyCoopContacts(CoopContactsBean bean);

    //查看
    CoopContactsBean viewCoopContacts(Map paramMap);

    List<CoopContactsBean> queryContactsIsValidByCoopId(@Param("coopId") String coopId, @Param("orgId") Integer orgId);

    List<CoopContactsBean> queryContactsIsValidByCoopId1(@Param("coopId") String coopId, @Param("contactsId") Integer contactsId,@Param("orgId") Integer orgId);
}
