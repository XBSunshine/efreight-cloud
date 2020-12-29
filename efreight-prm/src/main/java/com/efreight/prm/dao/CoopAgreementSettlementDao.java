package com.efreight.prm.dao;

import com.efreight.prm.entity.CoopAgreementSettlement;
import com.efreight.prm.entity.CoopAgreementSettlementDetail;
import com.efreight.prm.entity.CoopAgreementSettlementExcel;

import com.efreight.prm.entity.FlightOptionsBean;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CoopAgreementSettlementDao {
    List<CoopAgreementSettlement> findCoopAgreementSettlementListCriteria(CoopAgreementSettlement CoopAgreementSettlement);

    void deleteCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);

    void createCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);

    void createCoopAgreementSettlementGroup(CoopAgreementSettlement CoopAgreementSettlement);

    CoopAgreementSettlement findCoopAgreementSettlementCriteria(CoopAgreementSettlement CoopAgreementSettlement);

    void modifyCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);

    List<CoopAgreementSettlementExcel> queryListForExcel(CoopAgreementSettlement CoopAgreementSettlement);

    List<CoopAgreementSettlement> selectGoodStatusList(@Param("createTimeStart") Date createTimeStart, @Param("createTimeEnd") Date createTimeEnd, @Param("orgId") Integer orgId);
    List<CoopAgreementSettlement> selectMonthBillList(Map<String, Object> map);
    List<CoopAgreementSettlement> selectYearBillList(Map<String, Object> map);

    void insertBillConfirmContacts(CoopAgreementSettlement CoopAgreementSettlement);
    void deleteBillConfirmContacts(CoopAgreementSettlement CoopAgreementSettlement);
    List<Integer> findContactsIdList(CoopAgreementSettlement CoopAgreementSettlement);

    List<CoopAgreementSettlement> findCoopAgreementSettlementListCriteriaGroup(CoopAgreementSettlement CoopAgreementSettlement);
    List<CoopAgreementSettlementDetail> findCoopAgreementSettlementListCriteriaDetail(Map<String, Object> paramMap);
    void validCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
    void validCoopAgreementSettlementAll(CoopAgreementSettlement CoopAgreementSettlement);
    void invalidCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
    void invalidCoopAgreementSettlementAll(CoopAgreementSettlement CoopAgreementSettlement);
    void editCoopAgreementSettlementGroup(CoopAgreementSettlement CoopAgreementSettlement);
    List<FlightOptionsBean> selectFlightOptions();
    CoopAgreementSettlement getCoopAgreementSettlementDetailById(Integer settlementId);
    void validItCoopAgreementSettlement(CoopAgreementSettlement CoopAgreementSettlement);
}
