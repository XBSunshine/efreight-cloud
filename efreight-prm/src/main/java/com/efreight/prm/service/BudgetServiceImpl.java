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

    @Override
    public List<BudgetListBean> queryList(BudgetQuery budgetQuery) {
        BigDecimal oneHundred = new BigDecimal(100);
        DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");

        String saleIdsStr = budgetQuery.getSaleIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        budgetQuery.setSaleIdsStr(saleIdsStr);

        List<BudgetListBean> list = budgetDao.budgetList(budgetQuery);
        List<BudgetListBean> eftList = new ArrayList<>();
        List<BudgetListBean> result = new ArrayList<>();
        for (BudgetListBean item : list) {
            fillRate(item, oneHundred, decimalFormat);
            if("EFT".equals(item.getZoneCode())){
                eftList.add(item);
            }else{
                result.add(item);
            }
        }
        result.addAll(eftList);

        Map<Integer, List<BudgetListBean>> map = result.stream().collect(Collectors.groupingBy(BudgetListBean::getServiceId, Collectors.toList()));

        List<BudgetListBean> aggregateList = new ArrayList<>();
        map.forEach((key, value)->{
            BudgetListBean budgetListBean = new BudgetListBean();
            budgetListBean.setZoneCode("aggregate");
            budgetListBean.setZoneName("合计");
            budgetListBean.setServiceId(key);
            budgetListBean.setServiceName(value.get(0).getServiceName());
            budgetListBean.setServiceCode(value.get(0).getServiceCode());

            BigDecimal[] aggregate = aggregate(value);
            budgetListBean.setOldActuralCharge(format(aggregate[0], decimalFormat));
            budgetListBean.setNewActuralCharge(format(aggregate[1], decimalFormat));
            budgetListBean.setTotalActuralCharge(format(aggregate[2], decimalFormat));
            budgetListBean.setOldBudget(format(aggregate[3], decimalFormat));
            budgetListBean.setNewBudget(format(aggregate[4], decimalFormat));
            budgetListBean.setTotalBudget(format(aggregate[5], decimalFormat));
            budgetListBean.setSamePeriod(format(aggregate[6], decimalFormat));
            //完成率
            budgetListBean.setOldFillRate(
                    BigDecimal.ZERO.compareTo(aggregate[3]) != 0 ?
                            format(aggregate[0].divide(aggregate[3], 4, BigDecimal.ROUND_HALF_UP).multiply(oneHundred), decimalFormat)+" %" : "0.00 %"
            );
            budgetListBean.setNewFillRate(
                    BigDecimal.ZERO.compareTo(aggregate[4]) != 0 ?
                            format(aggregate[1].divide(aggregate[4], 4, BigDecimal.ROUND_HALF_UP).multiply(oneHundred), decimalFormat)+" %" : "0.00 %");
            budgetListBean.setTotalFillRate(
                    BigDecimal.ZERO.compareTo(aggregate[5]) != 0 ?
                            format(aggregate[2].divide(aggregate[5], 4, BigDecimal.ROUND_HALF_UP).multiply(oneHundred), decimalFormat)+" %" : "0.00 %");
            //增长率
            budgetListBean.setGrowthRate(
                    BigDecimal.ZERO.compareTo(aggregate[6]) != 0 ?
                            format((aggregate[2].subtract(aggregate[6]).divide(aggregate[6], 4, BigDecimal.ROUND_HALF_UP)).multiply(oneHundred), decimalFormat)+" %" : "0.00 %");
            aggregateList.add(budgetListBean);
        });
        Collections.sort(aggregateList, Comparator.comparing(BudgetListBean::getServiceCode));
        result.addAll(aggregateList);
        return result;
    }

    private void fillRate(BudgetListBean item, BigDecimal oneHundred, DecimalFormat decimalFormat) {
        item.setOldFillRate(fillRate(item.getOldActuralCharge(), item.getOldBudget(), oneHundred, decimalFormat));
        item.setNewFillRate(fillRate(item.getNewActuralCharge(), item.getNewBudget(), oneHundred, decimalFormat));
        item.setTotalFillRate(fillRate(item.getTotalActuralCharge(), item.getTotalBudget(), oneHundred, decimalFormat));

        if("0.00".equals(item.getTotalActuralCharge()) || "0.00".equals(item.getSamePeriod())){
            item.setGrowthRate("0.00 %");
        }else{
            item.setGrowthRate(fillRate(
                    format(new BigDecimal(item.getTotalActuralCharge()).subtract(new BigDecimal(item.getSamePeriod())), decimalFormat),
                    item.getSamePeriod(),
                    oneHundred,
                    decimalFormat
            ));
        }
    }

    private String fillRate(String real, String budget, BigDecimal fixed, DecimalFormat decimalFormat){
        if("0.00".equals(real) || "0.00".equals(budget)){
            return "0.00 %";
        }
        real = real.replaceAll(",", "");
        budget = budget.replaceAll(",", "");
        return format(new BigDecimal(real).divide(new BigDecimal(budget), 4, BigDecimal.ROUND_HALF_UP).multiply(fixed), decimalFormat) + " %";
    }

    @Override
    public List<BudgetServiceBean> queryService() {
        return this.budgetDao.budgetService();
    }

    private BigDecimal[] aggregate(List<BudgetListBean> list){
        BigDecimal[] bigDecimals = new BigDecimal[7];
        Arrays.fill(bigDecimals, BigDecimal.ZERO);

        list.stream().forEach(item -> {
            item.setZoneName(zone.get(item.getZoneCode()));
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
        return bigDecimals;
    }

    private String format(BigDecimal decimal, DecimalFormat decimalFormat){
        return decimalFormat.format(decimal.setScale(2, BigDecimal.ROUND_HALF_UP));
    }
}
