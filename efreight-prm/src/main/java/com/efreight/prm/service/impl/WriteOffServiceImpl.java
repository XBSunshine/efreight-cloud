package com.efreight.prm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopStatementDao;
import com.efreight.prm.dao.WriteOffDao;
import com.efreight.prm.entity.statement.CoopStatement;
import com.efreight.prm.entity.writeoff.*;
import com.efreight.prm.service.WriteOffService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author lc
 * @date 2021/3/11 14:06
 */
@Slf4j
@Service
public class WriteOffServiceImpl implements WriteOffService {

    @Resource
    private WriteOffDao writeOffDao;
    @Resource
    private CoopStatementDao coopStatementDao;

    @Override
    public WriteOffInfo writeOffInfo(Integer statementId) {
        return this.writeOffDao.writeOffInfo(statementId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int writeOffConfirm(WriteOffConfirm params) {
        params.check();
        Integer statementId = params.getStatementId();
        CoopStatement statement = coopStatementDao.selectById(params.getStatementId());
        Assert.notNull(statement, "数据不存在");
        Assert.isTrue(params.getRowId().equals(statement.getRowUuid()), "账单数据发生变化，请刷新后重试");
        BigDecimal unwrittenAmount = statement.getInvoiceAmount().subtract(Optional.ofNullable(statement.getInvoiceWriteoffAmount()).orElse(BigDecimal.ZERO));

        if("账单已核销".equals(statement.getStatementStatus()) && BigDecimal.ZERO.compareTo(unwrittenAmount) == 0){
            throw new RuntimeException("账单已经核销,请勿重复核销");
        }

        if(params.getAmount().compareTo(unwrittenAmount) > 0){
            log.warn("Exceeding the written-off amount. AmountWrittenOff:{}, UnwrittenAmount:{}", params.getAmount(), unwrittenAmount);
            throw new RuntimeException("超出未核销金额");
        }

        EUserDetails loginUser = SecurityUtils.getUser();
        //保存核销明细
        CoopStatementWriteOff writeOff = new CoopStatementWriteOff();
        writeOff.setStatementId(statementId);
        writeOff.setOrgId(loginUser.getOrgId());
        writeOff.setWriteOffNum(buildWriteOffNum(loginUser.getOrgId()));
        writeOff.setCoopId(statement.getCoopId());
        writeOff.setWriteOffDate(params.getDate());
        writeOff.setCurrency("CNY");
        writeOff.setAmountWriteOff(params.getAmount());
        writeOff.setWriteOffRemark(params.getRemark());
        writeOff.setCreatorId(loginUser.getId());
        writeOff.setCreateTime(new Date());
        writeOff.setCreatorName(loginUser.buildOptName());
        writeOff.setFinancialAccountName(params.getFinancialAccountName());
        writeOff.setFinancialAccountType(params.getFinancialAccountType());
        writeOff.setFinancialAccountCode(params.getFinancialAccountCode());
        writeOff.setRowUuid(UUID.randomUUID().toString());
        this.writeOffDao.insert(writeOff);
        //更新账单表
        BigDecimal amountWriteOff = writeOffDao.amountWrittenOff(statementId);
        if(amountWriteOff.compareTo(statement.getInvoiceAmount()) > 0){
            log.warn("The total written-off amount is greater than the invoice amount. STATEMENT_ID:{} AMOUNT_WRITTEN_OFF:{}", statementId, params.getAmount());
            throw new RuntimeException("总核销金额大于发票金额");
        }
        statement.setRowUuid(UUID.randomUUID().toString());
        statement.setInvoiceWriteoffAmount(amountWriteOff);
        if(StringUtils.isBlank(statement.getInvoiceWriteoffUserName())){
            statement.setInvoiceWriteoffUserName(loginUser.buildOptName());
            statement.setInvoiceWriteoffDate(new Date());
        }
        statement.setStatementStatus("账单已核销");
        int result = this.coopStatementDao.updateByRowUuid(statement, params.getRowId());
        if(result  != 1) {
            throw new RuntimeException("核销失败,请重新更新数据");
        }
        return result;
    }

    @Override
    public IPage<WriteOffList> pageQuery(WriteOffQuery query) {
        List<WriteOffList> writeOffLists = this.writeOffDao.queryList(query);
        Integer total = this.writeOffDao.countQueryList(query);
        IPage<WriteOffList> page = new Page<>(query.getCurrent(), query.getSize(), total);
        page.setRecords(writeOffLists);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWriteOff(String rowId, String statementRowId) {
        Assert.notNull(rowId, "数据ID不能为空");


        CoopStatementWriteOff writeOff = this.writeOffDao.findByRowId(rowId);
        Assert.notNull(writeOff, "核销数据发生变化，请刷新后重试");

        CoopStatement statement = coopStatementDao.selectById(writeOff.getStatementId());
        Assert.notNull(statement, "账单数据不存在");

        if(!statement.getRowUuid().equals(statementRowId)){
            throw new RuntimeException("账单数据发生变化，请刷新后重试");
        }

        int result = this.writeOffDao.deleteByRowId(rowId);
        if(result != 1){
            throw new RuntimeException("账单数据发生变化，请刷新后重试");
        }

        BigDecimal amountWriteOff = this.writeOffDao.amountWrittenOff(statement.getStatementId());

        String rowUuid = statement.getRowUuid();
        statement.setInvoiceWriteoffAmount(amountWriteOff);
        statement.setRowUuid(UUID.randomUUID().toString());
        if(BigDecimal.ZERO.compareTo(amountWriteOff) == 0){
            statement.setInvoiceWriteoffUserName(null);
            statement.setInvoiceWriteoffDate(null);
            statement.setInvoiceWriteoffAmount(null);
            statement.setStatementStatus("发票已开具");
        }
        result = this.coopStatementDao.updateByRowUuid(statement, rowUuid);
        if(0 == result){
            throw new RuntimeException("账单数据发生变化，请刷新后重试");
        }

        return result;
    }

    @Override
    public List<WriteOffList> listQuery(WriteOffQuery query) {
        return this.writeOffDao.queryList(query);
    }

    /**
     * @return
     */
    private String buildWriteOffNum(Integer orgId) {
        String serialNum = this.writeOffDao.maxSerialNumber(orgId);
        if(StringUtils.isBlank(serialNum)){
            serialNum = "0000";
        }else{
            Integer num = Integer.valueOf(serialNum) + 1;
            if(num > 9999){
                throw new RuntimeException("每天最多9999个核销单");
            }
            serialNum = String.format("%04d", num);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("EF-PW-");
        builder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")));
        builder.append(serialNum);
        return builder.toString();
    }
}
