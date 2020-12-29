package com.efreight.afbase.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.service.CarrierService;
import com.efreight.common.security.util.MessageInfo;
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

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/carrier")
@Slf4j
public class CarrierController {

    private final CarrierService carrierService;

    @GetMapping
    public MessageInfo page(Page page, Carrier carrier) {
        try {
            IPage<Carrier> ipage = carrierService.queryPage(page, carrier);
            return MessageInfo.ok(ipage);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }

    }

    @GetMapping("/view/{id}")
    public MessageInfo view(@PathVariable Integer id) {
        try {
            Carrier carrier = carrierService.queryOne(id);
            return MessageInfo.ok(carrier);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("/check3Code")
    public MessageInfo checkCode(@RequestBody Carrier bean) {
//		try{
        return MessageInfo.ok(carrierService.isHaved1(bean.getCarrierPrefix()));
//		}catch (Exception e){
//			log.info(e.getMessage());
//			return MessageInfo.failed(e.getMessage());
//		}
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doSave")
    @PreAuthorize("@pms.hasPermission('sys_base_carrier_add')")
    public MessageInfo addCarrier(@Valid @RequestBody Carrier bean) throws ParseException {
        List<Carrier> list = carrierService.isHaved(bean.getCarrierCode());
        if (list.size() != 0) {
            return MessageInfo.failed("航司两字码: " + bean.getCarrierCode() + " 已存在");
        }
        List<Carrier> list1 = carrierService.isHaved1(bean.getCarrierPrefix());
        if (list1.size() != 0) {
            return MessageInfo.failed("航司三字码: " + bean.getCarrierPrefix() + " 已存在");
        }
        return MessageInfo.ok(carrierService.addCarrier(bean));
    }

    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    @PreAuthorize("@pms.hasPermission('sys_base_carrier_edit')")
    public MessageInfo doUpdate(@Valid @RequestBody Carrier bean) throws ParseException {
        if (bean.getCarrierCode() != bean.getCarrierCode1() && !bean.getCarrierCode().equals(bean.getCarrierCode1())) {//更改了航司两字码
            List<Carrier> list = carrierService.isHaved(bean.getCarrierCode());
            if (list.size() != 0) {
                return MessageInfo.failed("航司两字码: " + bean.getCarrierCode() + " 已存在");
            }
        }
        if (bean.getCarrierPrefix() != bean.getCarrierPrefix1() && !bean.getCarrierPrefix().equals(bean.getCarrierPrefix1())) {//更改了航司三字码
            List<Carrier> list1 = carrierService.isHaved1(bean.getCarrierPrefix());
            if (list1.size() != 0) {
                return MessageInfo.failed("航司三字码: " + bean.getCarrierPrefix() + " 已存在");
            }
        }
        return MessageInfo.ok(carrierService.doUpdate(bean));
    }

    /**
     * 删除
     *
     * @param carrierId
     * @return
     */
    @DeleteMapping("/{carrierId}")
    @PreAuthorize("@pms.hasPermission('sys_base_carrier_del')")
    public MessageInfo delete(@PathVariable("carrierId") String carrierId) {
        try {
            carrierService.removeCarrierById(carrierId);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据导入
     *
     * @param
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    @ResponseBody
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
            List<Carrier> list = new ArrayList<Carrier>();
            DecimalFormat df = new DecimalFormat("#");

            for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) {

                Row row = sheet.getRow(rowIx);
                //航司二字码
                String carrierCode = "";
                if (row.getCell(0).getCellType() != CellType.BLANK) {
                    if (row.getCell(0).getCellType() == CellType.STRING) {
                        carrierCode = row.getCell(0).getStringCellValue();
                    } else if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                        carrierCode = df.format(row.getCell(0).getNumericCellValue());
                    }
                }

                if (StrUtil.isBlank(carrierCode)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行航司二字码为空");
                }
                //中文名称
                String carrierName = "";
                if (row.getCell(1).getCellType() != CellType.BLANK) {
                    if (row.getCell(1).getCellType() == CellType.STRING) {
                        carrierName = row.getCell(1).getStringCellValue();
                    } else if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                        carrierName = df.format(row.getCell(1).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(carrierName)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行中文名称为空");
                }
                //英文名称
                String carrierEname = "";
                if (row.getCell(2).getCellType() != CellType.BLANK) {
                    if (row.getCell(2).getCellType() == CellType.STRING) {
                        carrierEname = row.getCell(2).getStringCellValue();
                    } else if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                        carrierEname = df.format(row.getCell(2).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(carrierEname)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行英文名称为空");
                }
                //运单前缀
                String carrierPrefix = "";
                if (row.getCell(3).getCellType() != CellType.BLANK) {
                    if (row.getCell(3).getCellType() == CellType.STRING) {
                        carrierPrefix = row.getCell(3).getStringCellValue();
                    } else if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                        carrierPrefix = df.format(row.getCell(3).getNumericCellValue());
                    }
                }
                if (StrUtil.isBlank(carrierPrefix)) {
                    throw new RuntimeException("第" + (rowIx + 1) + "行运单前缀为空");
                }
                Carrier bean = new Carrier();
                bean.setCarrierCode(carrierCode);
                bean.setCarrierPrefix(carrierPrefix);
                list.add(bean);
            }
            carrierService.importData(list);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        } finally {
            //关闭
            input.close();
        }
    }

    /**
     * 获取航司
     *
     * @return
     */
    @GetMapping("/getCarrierList")
    public MessageInfo getCarrierList() {
        try {
            List<Carrier> list = carrierService.getCarrierList();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("search/{searchKey}")
    public MessageInfo search(@PathVariable("searchKey")String searchKey){
        return MessageInfo.ok(carrierService.search(searchKey));
    }
}

