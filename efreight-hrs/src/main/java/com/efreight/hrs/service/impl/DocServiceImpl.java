package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.dao.DocMapper;
import com.efreight.hrs.entity.doc.DocQuery;
import com.efreight.hrs.entity.doc.DocView;
import com.efreight.hrs.service.DocService;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lc
 * @date 2021/1/21 17:20
 */
@Service
@Log
public class DocServiceImpl implements DocService {

    @Resource
    private DocMapper docMapper;

    @Override
    public IPage<DocView> pageDocView(DocQuery query){
        Assert.hasLength(query.getBusinessScope(), "请选择业务范畴");
        List<DocView> docViews = docMapper.listDocView(query);
        Page<DocView> page = new Page<>(query.getCurrent(), query.getSize(), query.getTotal());
        page.setRecords(docViews);
        return page;
    }

    @Override
    public List<DocView> exportDocView(DocQuery query) {
        return docMapper.listDocView(query);
    }

}
