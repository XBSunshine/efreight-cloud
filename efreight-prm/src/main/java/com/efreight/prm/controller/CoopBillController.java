package com.efreight.prm.controller;

import cn.hutool.core.util.StrUtil;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.CoopBillService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 账单
 */
@RestController
@RequestMapping("coopBill")
@AllArgsConstructor
@Slf4j
public class CoopBillController {
    private final CoopBillService coopBillService;

    /**
     * 分页查询
     *
     * @param
     * @param coopBill
     * @return
     */
    @GetMapping
    public MessageInfo page(Integer current, Integer size, CoopBill coopBill) {
        try {
            Map<String, Object> result = coopBillService.getPage(current, size, coopBill);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 分页查询已制作账单
     *
     * @param
     * @param coopBillStatement
     * @return
     */
    @GetMapping("/madeBill")
    public MessageInfo getMadeBillList(Integer current, Integer size, CoopBillStatement coopBillStatement) {
        try {
            Map<String, Object> result = coopBillService.getMadeBillList(current, size, coopBillStatement);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询已确认账单合计
     *
     * @param
     * @param coopBillStatement
     * @return
     */
    @GetMapping("/getTotalMadeBill")
    public MessageInfo getTotalMadeBill(CoopBillStatement coopBillStatement) {
        try {
            return MessageInfo.ok(coopBillService.getTotalMadeBill(coopBillStatement));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询单个账单
     *
     * @param billId
     * @return
     */
    @GetMapping("/view")
    public MessageInfo view(Integer billId) {
        try {
            CoopBill coopBill = coopBillService.getView(billId);
            return MessageInfo.ok(coopBill);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 生成账单
     *
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_add')")
    public MessageInfo save(CoopBill coopBill) {
        try {
            coopBillService.save(coopBill.getCreateTimeStart(), coopBill.getCreateTimeEnd());
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 生成账单
     *
     * @return
     */
    @PostMapping("/repairBill")
    public MessageInfo repairBill(CoopBill coopBill) {
        try {
            coopBillService.repairBill(coopBill.getCreateTimeStart());
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 制作账单
     *
     * @return
     */
    @PostMapping("/makingBill")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_makingbill')")
    public MessageInfo makingBill(CoopBillStatement coopBillStatement) {
        try {
            coopBillService.makingBill(coopBillStatement);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除账单
     *
     * @param statementId
     * @return
     */
    @DeleteMapping("/{statementId}")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_delete')")
    public MessageInfo delete(@PathVariable Integer statementId) {
        try {
            coopBillService.delete(statementId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 账单确认
     *
     * @param statementId
     * @return
     */
    @PutMapping("/sendBill/{statementId}")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_send')")
    public MessageInfo sendBill(@PathVariable Integer statementId) {
        try {
            coopBillService.sendBill(statementId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 打印账单
     * @param
     * @return
     */
    @PostMapping("/printBill")
//    @PreAuthorize("@pms.hasPermission('sys_coop_bill_printbill')")
    public MessageInfo printBill(HttpServletRequest request){
        return MessageInfo.ok(coopBillService.printBill(Integer.parseInt(request.getParameter("statement_id"))));
    }

    @PostMapping("/printBill1/{statementId}")
    public MessageInfo printOrderLetter1( @PathVariable Integer statementId){
        try{
            String url = coopBillService.printBill1(statementId);
            return MessageInfo.ok(url);
        }catch (Exception e){
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 账单核销
     *
     * @param statement_id
     * @return
     */
    @PutMapping("/verify/{statement_id}/{invoiceAmount}/{invoiceWriteoffDate}")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_verify')")
    public MessageInfo verify(@PathVariable Integer statement_id,@PathVariable Double invoiceAmount,@PathVariable String invoiceWriteoffDate) {
        try {
            coopBillService.verify(statement_id,invoiceAmount,invoiceWriteoffDate);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据填充
     *
     * @param billId
     * @return
     */
    @PutMapping("/fill/{billId}/{acturalCharge}")
    public MessageInfo fill(@PathVariable Integer billId, @PathVariable Double acturalCharge) {
        try {
            coopBillService.fill(billId, acturalCharge);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @RequestMapping(value = "/doFill", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_fill')")
	public MessageInfo doFill(@ModelAttribute("bean") CoopBill bean) {
    	try {
            coopBillService.doFill(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 开发票
     *
     * @param
     * @return
     */
    @RequestMapping("/invoice")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_invoice')")
    public MessageInfo invoice(CoopBillStatement coopBillStatement) {
        try {
            coopBillService.invoice(coopBillStatement.getInvoiceNumber(),coopBillStatement.getStatement_id(),coopBillStatement.getActuralCharge(),coopBillStatement.getInvoiceTitle(),coopBillStatement.getInvoiceType(),
                    coopBillStatement.getInvoiceRemark(),coopBillStatement.getExpressCompany(),coopBillStatement.getExpressNumber(),coopBillStatement.getInvoiceDate());
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/sendInvoiceEmail")
    public MessageInfo sendInvoiceEmail(@RequestBody SendInvoiceEmail sendInvoiceEmail){
        try{
            coopBillService.sendInvoiceEmail(sendInvoiceEmail);
            return MessageInfo.ok();
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 费用上传
     *
     * @param
     * @return
     * @throws IOException
     */
    @PostMapping("/importData")
    public MessageInfo importUserData(MultipartFile file) throws IOException {
        InputStream input = null;
        try {
            input = file.getInputStream();
            String fileAllName = file.getOriginalFilename();
            Workbook wb = null;
            if (fileAllName.endsWith(".xlsx")) {
                wb = new XSSFWorkbook(input);
            } else if (fileAllName.endsWith(".xls")) {
                wb = new HSSFWorkbook(input);
            } else {
                throw new RuntimeException("导入文件格式有误！");
            }
            //获得第一个表单
            Sheet sheet = wb.getSheetAt(0);
            int minRowIx = sheet.getFirstRowNum() + 1;
            int maxRowIx = sheet.getLastRowNum();
            List<BillFee> list = new ArrayList<BillFee>();
            DecimalFormat df = new DecimalFormat("#.##");
            for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) {
                Row row = sheet.getRow(rowIx);
                //费用名称
                String feeName = "";
                if (row.getCell(0).getCellType() != CellType.BLANK) {
                    if (row.getCell(0).getCellType() == CellType.STRING) {
                        feeName = row.getCell(0).getStringCellValue();
                    } else if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                        feeName = df.format(row.getCell(0).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(feeName)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行费用名称为空");
                }
                //费用类型

                String feeType = "";
                if (row.getCell(1).getCellType() != CellType.BLANK) {
                    if (row.getCell(1).getCellType() == CellType.STRING) {
                        feeType = row.getCell(1).getStringCellValue();
                    } else if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                        feeType = df.format(row.getCell(1).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(feeType)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行费用类型为空");
                }
                //单价
                String unitPrice = "";
                if (row.getCell(2).getCellType() != CellType.BLANK) {
                    if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                        unitPrice = df.format(row.getCell(2).getNumericCellValue());
                    } else {
                        throw new RuntimeException("第" + (rowIx + 1) + "行单价格式有误");
                    }
                }
                if (StrUtil.isBlank(unitPrice)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行单价为空");
                }
                //数量

                String amount = "";
                if (row.getCell(3).getCellType() != CellType.BLANK) {
                    if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                        amount = df.format(row.getCell(3).getNumericCellValue());
                    } else {
                        throw new RuntimeException("第" + (rowIx + 1) + "行数量格式有误");
                    }
                }

                if (StrUtil.isBlank(amount)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行数量为空");
                }
                //总价

                String sumCharge = "";
                if (row.getCell(4).getCellType() != CellType.BLANK) {
                    if (row.getCell(4).getCellType() == CellType.NUMERIC) {
                        sumCharge = df.format(row.getCell(4).getNumericCellValue());
                    } else {
                        throw new RuntimeException("第" + (rowIx + 1) + "行总价格式有误");
                    }
                }

                if (StrUtil.isBlank(sumCharge)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行总价为空");
                }
                BillFee billFee = new BillFee();
                billFee.setAmount(Double.parseDouble(amount));
                billFee.setFeeName(feeName);
                billFee.setFeeType(feeType);
                billFee.setSumCharge(Double.parseDouble(sumCharge));
                billFee.setUnitPrice(Double.parseDouble(unitPrice));
                list.add(billFee);
            }

            return MessageInfo.ok(list);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        } finally {
            //关闭
            input.close();
        }
    }

    /**
     * 下载模板
     * @param response
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        ArrayList<BillFee> list = new ArrayList<>();
        BillFee billFee = new BillFee();
        billFee.setSumCharge(3.456);
        billFee.setFeeType("毛重");
        billFee.setFeeName("拖车费");
        billFee.setAmount(2.88);
        billFee.setUnitPrice(1.2);
        list.add(billFee);
        ExportExcel<BillFee> ex = new ExportExcel<BillFee>();
        String[] headers = {"费用名称", "费用类型", "单价", "数量", "总价"};
        ex.exportExcel(response, "sheet1", headers, list, "Export");
    }

    /*@GetMapping("/searchUnmakeBill")
    public MessageInfo searchUnmakeBill(CoopBillGroup coopBillGroup) {
        try {
            List<CoopBillGroup> list = coopBillService.getUnmakeBillList(coopBillGroup);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }*/

    @GetMapping("/searchUnmakeBill")
    public MessageInfo searchUnmakeBill(CoopBillGroupMerge coopBillGroupMerge) {
        try {
            List<CoopBillGroup> list = coopBillService.getUnmakeBillList(coopBillGroupMerge);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/checkBillByStatementId/{statement_id}")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_checkbill')")
    public MessageInfo checkBillByStatementId(@PathVariable Integer statement_id) {
        try {
            List<CoopBill> list = coopBillService.checkBillByStatementId(statement_id);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/searchBillService")
    @PreAuthorize("@pms.hasPermission('sys_coop_service_add')")
    public MessageInfo searchBillService(BillServiceGroup billServiceGroup) {
        try {
            List<BillServiceGroup> list = coopBillService.searchBillService(billServiceGroup);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("/searchBillServiceSelect")
    public MessageInfo searchBillServiceSelect() {
        try {
            List<BillServiceGroup> list = coopBillService.searchBillServiceSelect();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @RequestMapping("/addService")
    @PreAuthorize("@pms.hasPermission('sys_coop_service_addservice')")
    public MessageInfo addService(BillServiceGroup billServiceGroup) {
        try {
            coopBillService.addService(billServiceGroup);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @RequestMapping("/isHaveServiceClass")
    public MessageInfo isHaveServiceClass(BillServiceGroup billServiceGroup) {
        try {
            Integer serviceCount = coopBillService.isHaveServiceClass(billServiceGroup);
            if(serviceCount>0){
                return MessageInfo.ok();
            }else{
                return MessageInfo.failed();
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @RequestMapping("/isHaveServiceProject")
    public MessageInfo isHaveServiceProject(BillServiceGroup billServiceGroup) {
        try {
            Integer serviceCount = coopBillService.isHaveServiceProject(billServiceGroup);
            if(serviceCount>0){
                return MessageInfo.ok();
            }else{
                return MessageInfo.failed();
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @RequestMapping("/editService")
    @PreAuthorize("@pms.hasPermission('sys_coop_service_edit')")
    public MessageInfo editService(BillServiceGroup billServiceGroup) {
        try {
            coopBillService.editService(billServiceGroup);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 有条件查询未确认账单
     *
     * @param coopUnConfirmBillDetail
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/searchUnConfirmBill")
    public Map<String, Object> searchUnConfirmBill(CoopUnConfirmBillDetail coopUnConfirmBillDetail, Integer currentPage, Integer pageSize) {
        return coopBillService.searchUnConfirmBill(coopUnConfirmBillDetail, currentPage, pageSize);
    }

    /**
     * 有条件查询账单明细
     *
     * @param coopUnConfirmBillDetail
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/searchUnConfirmBill_detail")
    public Map<String, Object> searchUnConfirmBill_detail(CoopUnConfirmBillDetail coopUnConfirmBillDetail, Integer currentPage, Integer pageSize) {
        return coopBillService.searchUnConfirmBill_detail(coopUnConfirmBillDetail, currentPage, pageSize);
    }

    /**
     * 有条件查询结算报表
     *
     * @param
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/searchCoopBillSettleList")
    public Map<String, Object> searchCoopBillSettleList(CoopBillSettle coopBillSettle,Integer currentPage, Integer pageSize) {
        return coopBillService.searchCoopBillSettleList(coopBillSettle,currentPage, pageSize);
    }

    @GetMapping("/getTotalSettle")
    public MessageInfo getTotalSettle(CoopBillSettle coopBillSettle) {
        return MessageInfo.ok(coopBillService.getTotalSettle(coopBillSettle));
    }

    /**
     * 查询收费项目 一级科目
     *
     * @param
     * @return
     */
    @GetMapping("/queryServiceIsValid")
    public MessageInfo queryServiceIsValid() {
        try {
            List<CoopServiceBean> list = coopBillService.queryServiceIsValid();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询收费项目 二级科目
     *
     * @param
     * @return
     */
    @GetMapping("/queryServiceTwoIsValid")
    public MessageInfo queryServiceTwoIsValid() {
        try {
            List<CoopServiceBean> list = coopBillService.queryServiceTwoIsValid();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @RequestMapping(value = "/exportSettleExcel", method = RequestMethod.POST)
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") CoopBillSettle bean) throws IOException {
        List<CoopBillSettleExcel> list = coopBillService.queryListForExcel(bean);
        ExportExcel<CoopBillSettleExcel> ex = new ExportExcel<CoopBillSettleExcel>();
        String[] headers = {"序号", "期间", "客户名称", "分组名称", "一级科目", "二级科目", "口岸", "结算周期/计费模式", "数量", "单价", "金额"
                , "业务区域", "账单状态", "销售负责人" , "账单责任人" , "协同销售人" , "客户确认时间" , "核销人" ,"核销日期" , "首次收费月份" , "生效日期" , "截止日期" , "IT编码"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
    }

    /**
     * 确认账单
     *
     * @return
     */
    @PostMapping("/confirmBill")
    public MessageInfo confirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        try {
            coopBillService.confirmBill(coopUnConfirmBillGroup);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 拒绝账单
     *
     * @return
     */
    @PostMapping("/refuseConfirmBill")
    public MessageInfo refuseConfirmBill(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        try {
            coopBillService.refuseConfirmBill(coopUnConfirmBillGroup);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/checkIfModify")
    public MessageInfo checkIfModify(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        try {
            String ifModify = coopBillService.checkIfModify(coopUnConfirmBillGroup);
            if("modify".equals(ifModify)){
                return MessageInfo.ok("modify");
            }else{
                return MessageInfo.ok("unmodify");
            }

        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/getRemarkByBillId/{billId}")
    public MessageInfo getRemarkByBillId(@PathVariable Integer billId) {
        try {
            String ifModify = coopBillService.getRemarkByBillId(billId);
            return MessageInfo.ok(ifModify);

        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据填充后更新prm_coop_statement表的应收金额
     *
     * @return
     */
    @PostMapping("/updateAmountReceivable")
    public MessageInfo updateAmountReceivable(CoopUnConfirmBillGroup coopUnConfirmBillGroup) {
        try {
            coopBillService.updateAmountReceivable(coopUnConfirmBillGroup);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 客户确认账单
     *
     * @param statementId
     * @return
     */
    @PostMapping("/customerConfirmBill/{statementId}/{settlementId}")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_customerconfirm')")
    public MessageInfo customerConfirmBill(@PathVariable Integer statementId,@PathVariable Integer settlementId) {
        try {
            coopBillService.customerConfirmBill(statementId,settlementId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @RequestMapping(value = "/exportMadeBillExcel", method = RequestMethod.POST)
    public void exportMadeBillExcel(HttpServletResponse response, @ModelAttribute("bean") CoopBillStatement bean) throws IOException {
        List<CoopBillMadeExcel> list = coopBillService.querymadeBillListForExcel(bean);
        ExportExcel<CoopBillMadeExcel> ex = new ExportExcel<CoopBillMadeExcel>();
        String[] headers = {"账单名称", "状态", "账单月份", "账单金额", "业务区域","销售确认人", "客户确认时间", "开票客户名称", "发票类型", "发票号", "开票人", "开票日期", "核销人"
                , "核销日期", "快递号", "发送账单", "电子发票接收邮箱"};
        ex.exportExcel(response, "导出EXCEL", headers, list, "Export");
    }

    /**
     * 手工账单
     *
     * @param coopManualBill
     */
    @RequestMapping("/saveManualBill")
    @PreAuthorize("@pms.hasPermission('sys_coop_bill_manual')")
    public MessageInfo saveManualBill(CoopManualBill coopManualBill) {
        try {
           Integer statementId =  coopBillService.saveManualBill(coopManualBill);
           return MessageInfo.ok(statementId);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException("创建失败，原因：" + e.getMessage());
        }
    }

    /**
     * 发送人工账单
     *
     * @param
     * @return
     */
    @RequestMapping("/sendManualBill")
    public MessageInfo sendManualBill(CoopManualBill coopManualBill) {
        try {
            coopBillService.sendManualBill(coopManualBill.getStatementId(),coopManualBill.getToUsers());
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


}
