package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.efreight.hrs.entity.doc.DocQuery;
import com.efreight.hrs.entity.doc.DocView;

import java.util.List;

/**
 * @author lc
 * @date 2021/1/21 17:19
 */
public interface DocService {

     /**
      * 数据查询
      * @param query
      * @return
      */
     IPage<DocView> pageDocView(DocQuery query);

     /**
      * 数据导出
      * @param query
      * @return
      */
     List<DocView> exportDocView(DocQuery query);
}
