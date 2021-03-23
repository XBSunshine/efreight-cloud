package com.efreight.prm.dao;

import com.efreight.prm.entity.writeoff.CoopStatementWriteOff;
import com.efreight.prm.entity.writeoff.WriteOffInfo;
import com.efreight.prm.entity.writeoff.WriteOffList;
import com.efreight.prm.entity.writeoff.WriteOffQuery;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lc
 * @date 2021/3/11 14:07
 */
public interface WriteOffDao  {

    WriteOffInfo writeOffInfo(@Param("statementId") Integer statementId);

    /**
     * 获取 核销金额
     * @param statementId 账单ID
     * @return
     */
    BigDecimal amountWrittenOff(@Param("statementId") Integer statementId);

    /**
     * 每天中最大的核销单号
     * @return
     */
    String maxSerialNumber(Integer orgId);

    /**
     * 数据插入
     * @param writeOff
     * @return
     */
    int insert(CoopStatementWriteOff writeOff);

    /**
     * 查询数据
     * @param query
     * @return
     */
    List<WriteOffList> queryList(WriteOffQuery query);

    /**
     * 统计总数
     * @param query
     * @return
     */
    int countQueryList(WriteOffQuery query);

    /**
     * 数据查询
     * @param rowId
     * @return
     */
    CoopStatementWriteOff findByRowId(String rowId);

    /**
     * 删除数据
     * @param rowId
     * @return
     */
    int deleteByRowId(String rowId);
}
