package com.efreight.prm.service.impl;

import com.efreight.prm.dao.CoopScopeDao;
import com.efreight.prm.entity.CoopScopeBean;
import com.efreight.prm.service.CoopScopeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CoopScopeServiceImpl implements CoopScopeService {

    @Autowired
    private CoopScopeDao coopScopeDao;

    @Override
    public Map<String, Object> queryCoopScopeList(Integer currentPage, Integer pageSize, Map paramMap) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        if (currentPage == null || currentPage == 0)
            currentPage = 1;
        if (pageSize == null || pageSize == 0)
            pageSize = 10;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<CoopScopeBean> persons = coopScopeDao.queryCoopScopeList(paramMap);
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public Integer saveCoopScope(CoopScopeBean CoopScopeBean) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("coop_id", CoopScopeBean.getCoop_id());
        params.put("org_id", CoopScopeBean.getOrg_id());
        params.put("business_scope", CoopScopeBean.getBusiness_scope());
        List<CoopScopeBean> coopScopeBeans = coopScopeDao.queryCoopScopeList(params);
        if (coopScopeBeans != null && coopScopeBeans.size() != 0) {
            throw new RuntimeException("业务范畴" + CoopScopeBean.getBusiness_scope() + "已经存在不允许新建");
        }

        Integer CoopScope_id = coopScopeDao.saveCoopScope(CoopScopeBean);

        return CoopScope_id;
    }

    @Override
    public Integer modifyCoopScope(CoopScopeBean CoopScopeBean) {
        Integer CoopScope_id = coopScopeDao.modifyCoopScope(CoopScopeBean);

        return CoopScope_id;
    }

    @Override
    public CoopScopeBean viewCoopScope(Map paramMap) {
        CoopScopeBean reCoopScope = coopScopeDao.viewCoopScope(paramMap);

        return reCoopScope;
    }
}
