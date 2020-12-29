package com.efreight.prm.service;

import com.efreight.prm.entity.CoopAgreementSettlement;
import com.efreight.prm.entity.CoopAgreementSettlementExcel;
import com.efreight.prm.entity.FlightOptionsBean;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CoopAgreementSettlementService {
    Map<String,Object> findCoopAgreementSettlementListCriteria(CoopAgreementSettlement CoopAgreementSettlement, Integer currentPage, Integer pageSize);
    void deleteCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
    void createCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
    void createCoopAgreementSettlementGroup(CoopAgreementSettlement CoopAgreementSettlement);
    CoopAgreementSettlement findCoopAgreementSettlementCriteria(CoopAgreementSettlement CoopAgreementSettlement);
    void modifyCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);

    List<CoopAgreementSettlementExcel> queryListForExcel(CoopAgreementSettlement CoopAgreementSettlement);

    List<CoopAgreementSettlement> selectGoodStatusList(Date createTimeStart, Date createTimeEnd, Integer orgId);
    List<CoopAgreementSettlement> selectMonthBillList(Date createTimeStart, Date createTimeEnd, Integer orgId);
    List<CoopAgreementSettlement> selectYearBillList(Date createTimeStart, Date createTimeEnd, Integer orgId);
    void validCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
    void invalidCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
    void editCoopAgreementSettlementGroup(CoopAgreementSettlement CoopAgreementSettlement);
    List<FlightOptionsBean> selectFlightOptions();
    void validItCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
}
