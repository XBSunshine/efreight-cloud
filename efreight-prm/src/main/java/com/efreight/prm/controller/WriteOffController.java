package com.efreight.prm.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.prm.entity.writeoff.WriteOffConfirm;
import com.efreight.prm.entity.writeoff.WriteOffInfo;
import com.efreight.prm.entity.writeoff.WriteOffList;
import com.efreight.prm.entity.writeoff.WriteOffQuery;
import com.efreight.prm.service.WriteOffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author lc
 * @date 2021/3/11 14:05
 * 发票核销服务
 */
@Slf4j
@RestController
@RequestMapping("writeOff")
public class WriteOffController {

    @Resource
    private WriteOffService writeOffService;
    /**
     * 核销信息
     * @param statementId
     * @return
     */
    @GetMapping("info/{id}")
    public MessageInfo info(@PathVariable("id")Integer statementId){
        try{
            WriteOffInfo writeOffInfo = this.writeOffService.writeOffInfo(statementId);
            return MessageInfo.ok(writeOffInfo);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 核销确认
     * @return
     */
    @PostMapping("confirm")
    public MessageInfo confirm(@RequestBody WriteOffConfirm params){
        try{
            int result = this.writeOffService.writeOffConfirm(params);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 核销单 查询
     * @param query
     * @return
     */
    @GetMapping("page")
    public MessageInfo page(WriteOffQuery query){
        try{
            IPage<WriteOffList> page = this.writeOffService.pageQuery(query);
            return MessageInfo.ok(page);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除
     * @param rowId
     * @return
     */
    @DeleteMapping("/delete/{rowId}/{statementRowUuid}")
    public MessageInfo delete(@PathVariable("rowId") String rowId, @PathVariable("statementRowUuid")String statementRowUuid){
        try{
            int result = this.writeOffService.deleteWriteOff(rowId, statementRowUuid);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出数据
     * @param response
     * @param query
     */
    @PostMapping("export")
    public void export(HttpServletResponse response, WriteOffQuery query){
        query.setCurrent(null);
        query.setSize(null);
        List<WriteOffList> list = this.writeOffService.listQuery(query);

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
            if (list != null && list.size() > 0) {
                for (WriteOffList item : list) {
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
