package com.efreight.prm.controller;

import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.prm.entity.budget.BudgetListBean;
import com.efreight.prm.entity.budget.BudgetQuery;
import com.efreight.prm.entity.budget.BudgetServiceBean;
import com.efreight.prm.service.impl.BudgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author lc
 * @date 2021/2/25 12:59
 */
@RestController
@RequestMapping("budget")
@Slf4j
public class BudgetController {

    @Resource
    private BudgetService budgetService;

    /**
     * 预算分析列表接口
     * @param budgetQuery
     * @return
     */
    @PostMapping("list")
    public MessageInfo list(@RequestBody  BudgetQuery budgetQuery) {
        try {
            List<BudgetListBean> budgetListBeanList = budgetService.queryList(budgetQuery);
            return MessageInfo.ok(budgetListBeanList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 预算分析-产品服务数据
     * @return
     */
    @GetMapping("service")
    public MessageInfo service() {
        try {
            List<BudgetServiceBean> serviceBeanList = budgetService.queryService();
            return MessageInfo.ok(serviceBeanList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("export")
    public void export(HttpServletResponse response, @RequestBody BudgetQuery budgetQuery){
        List<BudgetListBean> list = budgetService.queryList(budgetQuery);

        LinkedHashMap<String, String> table = tableInfo();
        Collection<String> column = table.values();

        List<LinkedHashMap> listExcel = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (BudgetListBean item : list) {
                LinkedHashMap map = new LinkedHashMap();
                column.stream().forEach((prop)->{
                    map.put(prop, FieldValUtils.getFieldValueByFieldName(prop, item));
                });
                listExcel.add(map);
            }
        }

       new ExcelExportUtils().exportExcelLinkListMap( response, "预算分析",
                table.keySet().toArray(new String[table.size()]), listExcel, "Export");

    }

    private LinkedHashMap<String, String> tableInfo(){
        LinkedHashMap<String, String> relation = new LinkedHashMap<>();
        relation.put("业务区域", "zoneName");
        relation.put("产品名称", "serviceName");
        relation.put("老业务-实际(万元)", "oldActuralCharge");
        relation.put("老业务-预算(万元)", "oldBudget");
        relation.put("老业务-完成率", "oldFillRate");
        relation.put("新业务-实际(万元)", "newActuralCharge");
        relation.put("新业务-预算(万元)", "newBudget");
        relation.put("新业务-完成率", "newFillRate");
        relation.put("合计-实际(万元)", "totalActuralCharge");
        relation.put("合计-预算(万元)", "totalBudget");
        relation.put("合计-完成率", "totalFillRate");
        relation.put("同期对比-同期实际(万元)", "samePeriod");
        relation.put("同期对比-增长率", "growthRate");
        return relation;
    }
}
