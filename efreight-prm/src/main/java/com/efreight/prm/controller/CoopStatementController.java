package com.efreight.prm.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.statement.CoopStatementAggregate;
import com.efreight.prm.entity.statement.CoopStatementDetail;
import com.efreight.prm.entity.statement.CoopStatementList;
import com.efreight.prm.entity.statement.CoopStatementQuery;
import com.efreight.prm.service.CoopStatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author lc
 * @date 2021/2/1 15:51
 */
@RequestMapping("coopStatement")
@RestController
@Slf4j
public class CoopStatementController {

    @Resource
    private CoopStatementService coopStatementService;

    @PostMapping("list")
    public MessageInfo list(@RequestBody CoopStatementQuery query){
        try{
            CoopStatementAggregate coopStatementAggregate = coopStatementService.listCoopStatement(query);
            return MessageInfo.ok(coopStatementAggregate);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("exportList")
    public void exportList(HttpServletResponse response, CoopStatementQuery query){

        CoopStatementAggregate coopStatementAggregate = coopStatementService.listCoopStatement(query);
        if (!StringUtils.isEmpty(query.getColumnStr())) {
            List<LinkedHashMap> listExcel = new ArrayList<>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(query.getColumnStr());
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

            List<CoopStatementList> list = coopStatementAggregate.getCoopStatementList();
            if (list != null && list.size() > 0) {
                int index = 1;
                for (CoopStatementList item : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    item.setNo(String.valueOf(index++));
                    for (int j = 0; j < colunmStrs.size(); j++) {
                        map.put(colunmStrs.get(j), FieldValUtils.getFieldValueByFieldName(colunmStrs.get(j), item));
                    }
                    listExcel.add(map);
                }
            }

            LinkedHashMap aggregate = new LinkedHashMap();
            CoopStatementList coopStatementBean = new CoopStatementList();
            coopStatementBean.setNo("合计");
            coopStatementBean.setAmountReceived(coopStatementAggregate.getAmountReceived());
            coopStatementBean.setAccountPeriodAmount(coopStatementAggregate.getAccountPeriodAmount());
            coopStatementBean.setOverdueAmount(coopStatementAggregate.getOverdueAmount());
            coopStatementBean.setIntervalAmount1(coopStatementAggregate.getIntervalAmount1());
            coopStatementBean.setIntervalAmount2(coopStatementAggregate.getIntervalAmount2());
            coopStatementBean.setIntervalAmount3(coopStatementAggregate.getIntervalAmount3());
            coopStatementBean.setIntervalAmount4(coopStatementAggregate.getIntervalAmount4());
            coopStatementBean.setIntervalAmount5(coopStatementAggregate.getIntervalAmount5());
            coopStatementBean.setIntervalAmount6(coopStatementAggregate.getIntervalAmount6());

            for (int j = 0; j < colunmStrs.size(); j++) {
                aggregate.put(colunmStrs.get(j), FieldValUtils.getFieldValueByFieldName(colunmStrs.get(j), coopStatementBean));
            }
            listExcel.add(aggregate);

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers.toArray(new String[headers.size()]), listExcel, "Export");

        }
    }

    @GetMapping("/detail/{coopId}")
    public MessageInfo detail(@PathVariable("coopId")Integer coopId){
        try{
            Integer orgId = SecurityUtils.getUser().getOrgId();
            List<CoopStatementDetail> coopStatementDetailList = coopStatementService.listDetailCoopStatement(orgId, coopId);
            return MessageInfo.ok(coopStatementDetailList);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("exportDetail/{coopId}")
    public void exportDetail(HttpServletResponse response, @PathVariable("coopId") Integer coopId, @RequestParam("columnStr") String columnStr){
        Integer orgId = SecurityUtils.getUser().getOrgId();

        if (!StringUtils.isEmpty(columnStr)) {
            List<LinkedHashMap> listExcel = new ArrayList<>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(columnStr);
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


            List<CoopStatementDetail> list = coopStatementService.listDetailCoopStatement(orgId, coopId);
            if (list != null && list.size() > 0) {
                int index = 1;
                for (CoopStatementDetail item : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.size(); j++) {
                        if("no".equals(colunmStrs.get(j))){
                            map.put(colunmStrs.get(j), index++);
                        }else{
                            map.put(colunmStrs.get(j), FieldValUtils.getFieldValueByFieldName(colunmStrs.get(j), item));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers.toArray(new String[headers.size()]), listExcel, "Export");

        }
    }
}
