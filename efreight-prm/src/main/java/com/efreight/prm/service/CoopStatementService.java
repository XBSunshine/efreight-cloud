package com.efreight.prm.service;

import com.efreight.prm.entity.statement.CoopStatementAggregate;
import com.efreight.prm.entity.statement.CoopStatementDetail;
import com.efreight.prm.entity.statement.CoopStatementQuery;

import java.util.List;

/**
 * @author lc
 * @date 2021/1/29 14:54
 */
public interface CoopStatementService {

    /**
     *
     * 应收账龄列表
     * @param query
     * @return
     */
    CoopStatementAggregate listCoopStatement(CoopStatementQuery query);

    /**
     * 应收账龄详情
     * @param orgId
     * @param coopId
     * @return
     */
    List<CoopStatementDetail> listDetailCoopStatement(Integer orgId, Integer coopId);
}
