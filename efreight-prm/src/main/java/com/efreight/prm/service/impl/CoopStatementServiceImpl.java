package com.efreight.prm.service.impl;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopStatementDao;
import com.efreight.prm.entity.statement.*;
import com.efreight.prm.service.CoopStatementService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lc
 * @date 2021/1/29 14:54
 */
@Service
public class CoopStatementServiceImpl implements CoopStatementService {

    @Resource
    private CoopStatementDao coopStatementDao;

    @Override
    public CoopStatementAggregate listCoopStatement(CoopStatementQuery query) {

        query.setOrgId(SecurityUtils.getUser().getOrgId());
        Optional.ofNullable(query.getOverdueInterval()).ifPresent((item)->{
            Integer[] tmp = new Integer[5];
            item.toArray(tmp);
            query.setIntervalAmount1(tmp[0]);
            query.setIntervalAmount2(tmp[1]);
            query.setIntervalAmount3(tmp[2]);
            query.setIntervalAmount4(tmp[3]);
            query.setIntervalAmount5(tmp[4]);
        });
        List<CoopStatementBean> coopStatementBeanList = this.coopStatementDao.listCoopStatement(query);

        DecimalFormat decimalFormat = new DecimalFormat("###,###.00");
        BigDecimal[] total = new BigDecimal[9];
        Arrays.fill(total, new BigDecimal(0));
        List<CoopStatementList> coopStatementLists = coopStatementBeanList.stream().map((item)->{
            CoopStatementList coopStatementList = new CoopStatementList();
            coopStatementList.transTo(item, decimalFormat);
            total[0] = total[0].add(item.getIntervalAmount1());
            total[1] = total[1].add(item.getIntervalAmount2());
            total[2] = total[2].add(item.getIntervalAmount3());
            total[3] = total[3].add(item.getIntervalAmount4());
            total[4] = total[4].add(item.getIntervalAmount5());
            total[5] = total[5].add(item.getIntervalAmount6());

            total[6] = total[6].add(item.getAmountReceived());
            total[7] = total[7].add(item.getAccountPeriodAmount());
            total[8] = total[8].add(item.getOverdueAmount());
            return coopStatementList;
        }).collect(Collectors.toList());

        CoopStatementAggregate aggregate = new CoopStatementAggregate();
        aggregate.setCoopStatementList(coopStatementLists);
        aggregate.setIntervalAmount1(formatBigDecimal(decimalFormat, total[0]));
        aggregate.setIntervalAmount2(formatBigDecimal(decimalFormat, total[1]));
        aggregate.setIntervalAmount3(formatBigDecimal(decimalFormat, total[2]));
        aggregate.setIntervalAmount4(formatBigDecimal(decimalFormat, total[3]));
        aggregate.setIntervalAmount5(formatBigDecimal(decimalFormat, total[4]));
        aggregate.setIntervalAmount6(formatBigDecimal(decimalFormat, total[5]));
        aggregate.setAmountReceived(formatBigDecimal(decimalFormat, total[6]));
        aggregate.setAccountPeriodAmount(formatBigDecimal(decimalFormat, total[7]));
        aggregate.setOverdueAmount(formatBigDecimal(decimalFormat, total[8]));
        return aggregate;
    }

    @Override
    public List<CoopStatementDetail> listDetailCoopStatement(Integer orgId, Integer coopId) {
        return this.coopStatementDao.listDetailCoopStatement(orgId, coopId);
    }


    private String formatBigDecimal(DecimalFormat decimalFormat, BigDecimal decimal){
        if(null == decimal || new BigDecimal(0).compareTo(decimal) == 0){
            return "0.00";
        }
        return decimalFormat.format(decimal.setScale(2, BigDecimal.ROUND_HALF_UP));
    }
}
