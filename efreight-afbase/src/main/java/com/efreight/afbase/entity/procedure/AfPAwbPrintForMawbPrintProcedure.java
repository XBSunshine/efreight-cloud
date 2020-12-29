package com.efreight.afbase.entity.procedure;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfPAwbPrintForMawbPrintProcedure {


    private String txtAWBTop;
    private String txtAWBBottom;
    private String txtAWBPrefix;
    private String txtOriginCode;
    private String txtAWBSuffix;
    private String txtCarrierName;
    private String txtShipperName;
    private String txtConsigneeName;
    private String txtAgentName;
    private String txtAgentIataCode;
    private String txtAgentAccount;
    private String txtDeparture;
    private String txtTo1;
    private String txtFlight1Carr;
    private String txtTo2;
    private String txtBy2;
    private String txtTo3;
    private String txtBy3;
    private String txtDestination;
    private String txtAccountingInfoText;
    private String txtFlight2Carr;
    private String txtFlight3Carr;
    private String txtDefaultCurrCode;
    private String txtPCWtgPP;
    private String txtPCOthPP;
    private String txtHandlingInfoText;
    private String txtGoodsDesc1;
    private String txtGoodsVolume;
    private String txtRCPPcs1;
    private String txtTotalRcp;
    private String txtGrossWtg1;
    private String txtDefaultWgtCode1;
    private String txtChgWtg1;
    private String txtRateClass1;
    private String txtRateChgDis1;
    private String txtTotalChg1;
    private String txtGoodsSize;
    private String txtTotalWtgChgPP;
    private String txtTotalWtgChgCC;
    private String txtValChgPP;
    private String txtValChgCC;
    private String txtTaxChgPP;
    private String txtTaxChgCC;
    private String txtChgDueCarrPP;
    private String txtChgDueCarrCC;
    private String txtChgDueAgtPP;
    private String txtChgDueAgtCC;
    private String txtShipperRemark1;
    private String txtTotalPP;
    private String txtTotalCC;
    private String txtShipperRemark2;
    private String txtAWBBarCode;
    private String txtChgsCode;
    private String txtCVDCarriage;
    private String txtCVDCustom;
    private String txtCVDInsurance;
    private String txtItemNum1;
    private String txtCCR;
    private String txtCDC;
    private String txtChgDest;
    private String txtTotColl;
    private String txtSRIRefNum;
    private String txtSRI1;
    private String txtSPL;
    private String txtSCI;
    private String txtOtherCharges1;

}
