package com.efreight.prm.service;

import com.efreight.prm.dao.BudgetDao;
import com.efreight.prm.entity.budget.BudgetListBean;
import com.efreight.prm.entity.budget.BudgetQuery;
import com.efreight.prm.entity.budget.BudgetServiceBean;
import com.efreight.prm.service.impl.BudgetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lc
 * @date 2021/2/25 13:01
 */
@Service
public class BudgetServiceImpl implements BudgetService {

    @Resource
    private BudgetDao budgetDao;

    private static HashMap<String, String> zone = new HashMap<>();
    static {
        zone.put("BJS", "华北");
        zone.put("CAN", "华南");
        zone.put("SHA", "华东");
        zone.put("XIY", "西北");
        zone.put("EFT", "总部");
        zone.put("aggregate", "合计");
    }
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,##0.00");

    @Override
    public List<BudgetListBean> queryList(BudgetQuery budgetQuery) {
        String saleIdsStr = budgetQuery.getSaleIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        budgetQuery.setSaleIdsStr(saleIdsStr);

        List<BudgetListBean> list = budgetDao.budgetList(budgetQuery);

        //根据服务ID来添加合计数据
        aggregateByServiceId(list);

        //根据区域代码来添加合计数据
        List<BudgetListBean> aggregateByZoneCodeList = aggregateByZoneCode(list);

        //将总部数据后移
        List<BudgetListBean> eftList = new ArrayList<>();
        List<BudgetListBean> result = new ArrayList<>();
        for (BudgetListBean item : aggregateByZoneCodeList) {
            item.setZoneName(zone.get(item.getZoneCode()));
            fillRate(item);
            if("EFT".equals(item.getZoneCode())){
                eftList.add(item);
            }else{
                result.add(item);
            }
        }
        result.addAll(eftList);

        return result;
    }
    private void aggregateByServiceId(List<BudgetListBean> list){
        Map<Integer, List<BudgetListBean>> map = list.stream().collect(Collectors.groupingBy(BudgetListBean::getServiceId, Collectors.toList()));
        List<BudgetListBean> aggregateList = new ArrayList<>();
        map.forEach((key, value)->{
            BudgetListBean budgetListBean = buildTotal(value);
            budgetListBean.setZoneCode("aggregate");
            budgetListBean.setZoneName("合计");
            budgetListBean.setServiceId(key);
            budgetListBean.setServiceName(value.get(0).getServiceName());
            budgetListBean.setServiceCode(value.get(0).getServiceCode());
            aggregateList.add(budgetListBean);
        });
        Collections.sort(aggregateList, Comparator.comparing(BudgetListBean::getServiceCode));
        list.addAll(aggregateList);
    }

    // 根据业务区域进行合计
    private List<BudgetListBean> aggregateByZoneCode(List<BudgetListBean> list) {
        Map<String, List<BudgetListBean>> map = list.stream().collect(Collectors.groupingBy(BudgetListBean::getZoneCode, Collectors.toList()));
        List<BudgetListBean> result = new ArrayList<>();

        map.forEach((key, values)->{
            BudgetListBean total = buildTotal(values);
            total.setZoneCode(key);
            total.setZoneName(zone.get(key));
            total.setServiceName("汇总");
            result.addAll(values);
            result.add(total);
        });
        return result;
    }

    private BudgetListBean buildTotal(List<BudgetListBean> budgetListBeanList){
        String[] aggregate = aggregate(budgetListBeanList);

        BudgetListBean budgetListBean = new BudgetListBean();
        budgetListBean.setOldActuralCharge(aggregate[0]);
        budgetListBean.setNewActuralCharge(aggregate[1]);
        budgetListBean.setTotalActuralCharge(aggregate[2]);
        budgetListBean.setOldBudget(aggregate[3]);
        budgetListBean.setNewBudget(aggregate[4]);
        budgetListBean.setTotalBudget(aggregate[5]);
        budgetListBean.setSamePeriod(aggregate[6]);
        fillRate(budgetListBean);
        return budgetListBean;
    }

    private void fillRate(BudgetListBean item) {
        item.setOldFillRate(fillFinishRate(item.getOldActuralCharge(), item.getOldBudget()));
        item.setNewFillRate(fillFinishRate(item.getNewActuralCharge(), item.getNewBudget()));
        item.setTotalFillRate(fillFinishRate(item.getTotalActuralCharge(), item.getTotalBudget()));
        item.setGrowthRate(growthRate(item.getTotalActuralCharge(), item.getSamePeriod()));
    }

    //计算完成率
    private String fillFinishRate(String real, String budget){
        if("0.00".equals(real)){
            return "0.00 %";
        }
        if("0.00".equals(budget)){
            return "100.00 %";
        }
        real = real.replaceAll(",", "");
        budget = budget.replaceAll(",", "");
        return format(new BigDecimal(real).divide(new BigDecimal(budget), 4, BigDecimal.ROUND_HALF_UP).multiply(ONE_HUNDRED), DECIMAL_FORMAT) + " %";
    }

    //计算增长率
    private String growthRate(String real, String samePeriod){
        if("0.00".equals(real)){
            return "0.00".equals(samePeriod) ? "0.00 %" : "-100.00 %";
        }
        if("0.00".equals(samePeriod)){
            return "100.00 %";
        }
        return format(new BigDecimal(real).subtract(new BigDecimal(samePeriod)).divide(new BigDecimal(samePeriod), 4, BigDecimal.ROUND_HALF_UP).multiply(ONE_HUNDRED), DECIMAL_FORMAT) + " %";
    }

    @Override
    public List<BudgetServiceBean> queryService() {
        return this.budgetDao.budgetService();
    }

    //合计数据
    private String[] aggregate(List<BudgetListBean> list){
        BigDecimal[] bigDecimals = new BigDecimal[7];
        Arrays.fill(bigDecimals, BigDecimal.ZERO);

        list.stream().forEach(item -> {
            //实际-老业务
            bigDecimals[0] = bigDecimals[0].add(new BigDecimal(item.getOldActuralCharge().replaceAll(",", "")));
            //实际-新业务
            bigDecimals[1] = bigDecimals[1].add(new BigDecimal(item.getNewActuralCharge().replaceAll(",", "")));
            //实际-合计
            bigDecimals[2] = bigDecimals[2].add(new BigDecimal(item.getTotalActuralCharge().replaceAll(",", "")));
            //预算-老业务
            bigDecimals[3] = bigDecimals[3].add(new BigDecimal(item.getOldBudget().replaceAll(",", "")));
            //预算-新业务
            bigDecimals[4] = bigDecimals[4].add(new BigDecimal(item.getNewBudget().replaceAll(",", "")));
            //预算-合计
            bigDecimals[5] = bigDecimals[5].add(new BigDecimal(item.getTotalBudget().replaceAll(",", "")));
            //同期
            bigDecimals[6] = bigDecimals[6].add(new BigDecimal(item.getSamePeriod().replaceAll(",", "")));
        });

        String[] result = new String[7];
        result[0] = format(bigDecimals[0], DECIMAL_FORMAT);
        result[1] = format(bigDecimals[1], DECIMAL_FORMAT);
        result[2] = format(bigDecimals[2], DECIMAL_FORMAT);
        result[3] = format(bigDecimals[3], DECIMAL_FORMAT);
        result[4] = format(bigDecimals[4], DECIMAL_FORMAT);
        result[5] = format(bigDecimals[5], DECIMAL_FORMAT);
        result[6] = format(bigDecimals[6], DECIMAL_FORMAT);
        return result;
    }

    private String format(BigDecimal decimal, DecimalFormat decimalFormat){
        return decimalFormat.format(decimal.setScale(2, BigDecimal.ROUND_HALF_UP));
    }
}
