package com.efreight.hrs.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.doc.DocQuery;
import com.efreight.hrs.entity.doc.DocView;
import com.efreight.hrs.service.DocService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 单证管理
 * @author lc
 * @date 2021/1/21 17:08
 */
@RestController
@RequestMapping("doc")
@AllArgsConstructor
@Slf4j
public class DocController {

    private final DocService docService;

    @GetMapping("page")
    public MessageInfo page(DocQuery query){
        query.setOrgId(SecurityUtils.getUser().getOrgId());
        try{
            IPage<DocView> docPage =  docService.pageDocView(query);
            return MessageInfo.ok(docPage);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出单证数据
     * @param response
     * @param query
     */
    @PostMapping("export")
    public void export(HttpServletResponse response, DocQuery query){
        query.setCurrent(null);
        query.setSize(null);
        query.setOrgId(SecurityUtils.getUser().getOrgId());
        List<DocView> list = docService.exportDocView(query);

        if (!StringUtils.isEmpty(query.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(query.getColumnStrs());
            ArrayList<String> headers = new ArrayList<>();
            ArrayList<String> colunmStrs = new ArrayList<>();

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers.add(job.getString("label"));
                    colunmStrs.add(job.getString("prop"));
                }
            }
            if(query.getColumnStrs().indexOf("单证名称") > -1){
                headers.add("单证地址");
                colunmStrs.add("fileUrl");
            }
            if (list != null && list.size() > 0) {
                for (DocView item : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.size(); j++) {
                        map.put(colunmStrs.get(j), FieldValUtils.getFieldValueByFieldName(colunmStrs.get(j), item));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers.toArray(new String[headers.size()]), listExcel, "Export");

        }
    }
}
