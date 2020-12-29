package com.efreight.prm.service.impl;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopContactsDao;
import com.efreight.prm.entity.CoopContactsBean;
import com.efreight.prm.service.CoopContactsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CoopContactsServiceImpl implements CoopContactsService {

    @Autowired
    private CoopContactsDao coopContactsDao;

    @Override
    public Map<String, Object> queryCoopContactsList(Integer currentPage, Integer pageSize, Map paramMap) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        if (currentPage == null || currentPage == 0)
            currentPage = 1;
        if (pageSize == null || pageSize == 0)
            pageSize = 10;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<CoopContactsBean> persons = coopContactsDao.queryCoopContactsList(paramMap);
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public void saveCoopContacts(CoopContactsBean CoopContactsBean) {
        coopContactsDao.saveCoopContacts(CoopContactsBean);
        //Integer CoopContacts_id = coopContactsDao.saveCoopContacts(CoopContactsBean);
        //CoopContacts_id=CoopContactsBean.getContacts_id();
        //return CoopContacts_id;
    }

    @Override
    public Integer saveCoopContacts1(CoopContactsBean CoopContactsBean) {
        Integer CoopContacts_id = coopContactsDao.saveCoopContacts1(CoopContactsBean);
        CoopContacts_id=CoopContactsBean.getContacts_id();
        return CoopContacts_id;
    }

    @Override
    public Integer modifyCoopContacts(CoopContactsBean CoopContactsBean) {
        Integer CoopContacts_id = coopContactsDao.modifyCoopContacts(CoopContactsBean);

        return CoopContacts_id;
    }

    @Override
    public CoopContactsBean viewCoopContacts(Map paramMap) {
        CoopContactsBean reCoopContacts = coopContactsDao.viewCoopContacts(paramMap);

        return reCoopContacts;
    }

    @Override
    public List<CoopContactsBean> queryContactsIsValidByCoopId(String coopId) {
        return coopContactsDao.queryContactsIsValidByCoopId(coopId, SecurityUtils.getUser().getOrgId());
    }

    @Override
    public List<CoopContactsBean> queryContactsIsValidByCoopId1(String coopId,Integer contactsId) {
        return coopContactsDao.queryContactsIsValidByCoopId1(coopId, contactsId,SecurityUtils.getUser().getOrgId());
    }
}
