package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * AF 托书
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Letters implements Serializable {

    private static final long serialVersionUID = 1L;

    private String letterPdf;
    private String input001;
    private String input002;
    private String input003;
    private String input004;
    private String input005;
    private String input006;
    private String input007;
    private String input008;
    private String input009;
    private String input010;
    private String input011;
    private String input012;
    private String input013;
    private String input014;
    private String input015;
    private String input016;
    //航班号
    private String Input0071;
    //航班日期
    private String Input0072;
    //第一承运人
    private String Input0073;
    //英文品名
    private String Input0091;
    //中文品名
    private String Input0092;
    //分单号
    private String Input017;
    //分单件数
    private String Input018;
    //货物性质：普通货物
    private String Input0191;
    //货物性质：特种货物
    private String Input0192;
    //货物性质：危险品
    private String Input0193;
    //货物性质：航空快件
    private String Input0194;
    //航空货物托运人
    private String Input020;
    //航空货运销售人
    private String Input021;
    //预留字段
    private String Input019;
    private String Input022;
    private String Input023;
    private String Input024;
    private String Input025;
    private String Input026;
    private String Input027;
    private String Input028;
    private String Input029;
    private String Input030;
    private String Input031;
    private String Input032;
    private String Input033;
    private String Input034;
    private String Input035;
    private String Input036;
    private String Input037;
    private String Input038;
    private String Input039;
    private String Input040;
    private String Input041;
    private String Input042;
    private String Input043;
    private String Input044;
    private String Input045;
    private String Input046;
    private String Input047;
    private String Input048;
    private String Input049;
    private String Input050;
    private String Input051;
    private String Input052;
    private String Input053;
    private String Input054;
    private String Input055;
    private String Input056;
    private String Input057;
    private String Input058;
    private String Input059;
    private String Input060;
    private String Input061;
    private String Input062;
    private String Input063;
    private String Input064;
    private String Input065;
    private String Input066;
    private String Input067;
    private String Input068;
    private String Input069;
    private String Input070;

    private String awbUUIds;
    
    private String businessScope;
    private String warehouseNameCn;

    private String letterExcel;
    private String securityNoteExcel;
    private String securityNotePdf;

    //以下字段为打印运单确认件字段
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
    private String hawbNumber;
}
