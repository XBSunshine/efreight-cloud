package com.efreight.afbase.controller;


import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.service.CargoGoodsnamesService;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author qipm
 * @since 2020-12-17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/goodsName")
@Slf4j
public class CargoGoodsnamesController {
	private final CargoGoodsnamesService service;
	
	@GetMapping("querylist")
    public MessageInfo querylist(CargoGoodsnames bean) {
        try {
        	List<CargoGoodsnames> result = service.querylist(bean);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	@PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody CargoGoodsnames bean) {
        try {
            return MessageInfo.ok(service.doSave(bean));
        } catch (Exception e) {
        	log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	@PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody CargoGoodsnames bean) {
        try {
            return MessageInfo.ok(service.doUpdate(bean));
        } catch (Exception e) {
        	log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	@PostMapping(value = "/doDelete")
    public MessageInfo doDelete(@Valid @RequestBody CargoGoodsnames bean) {
        try {
            return MessageInfo.ok(service.doDelete(bean));
        } catch (Exception e) {
        	log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	@PostMapping("/downloadTemplate")
    public MessageInfo downloadTemplate(){
        try{
            String url = service.downloadTemplate();
            return MessageInfo.ok(url);
        }catch (Exception e){
            return MessageInfo.failed(e.getMessage());
        }
    }
	@PostMapping(value = "/importData")
    public MessageInfo importCoopData(MultipartFile file) throws IOException {
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
            List<CargoGoodsnames> list = new ArrayList<CargoGoodsnames>();
            DecimalFormat df = new DecimalFormat("#");
            boolean isHaveErroe = false;//是否有错误标志，控制页面确认导入是否可用
            for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) {

                Row row = sheet.getRow(rowIx);
                if (isEmptyRow(row)) {
                    continue;
                }
                String errorMessage = "";
                String coopCodeErrorFlag = "1";
                String coopTypeErrorFlag = "1";
                String coopNameErrorFlag = "1";
                String coopENameErrorFlag = "1";
               
                //中文品名
                String goodsCnnames = "";
                if(row.getCell(0) !=null && !"".equals(row.getCell(0))){
                    if (row.getCell(0).getCellType() != CellType.BLANK) {
                        if (row.getCell(0).getCellType() == CellType.STRING) {
                        	goodsCnnames = row.getCell(0).getStringCellValue();
                        } else if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                        	goodsCnnames = df.format(row.getCell(0).getNumericCellValue());
                        }
                    }
                }
             
                //英文品名
                String goodsEnnames = "";
                if(row.getCell(1) !=null && !"".equals(row.getCell(1))){
                    if (row.getCell(1).getCellType() != CellType.BLANK) {
                        if (row.getCell(1).getCellType() == CellType.STRING) {
                        	goodsEnnames = row.getCell(1).getStringCellValue();
                        } else if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                        	goodsEnnames = df.format(row.getCell(1).getNumericCellValue());
                        }
                    }
                }
                //件数
                String quantity = "";
//                try {
	                if(row.getCell(2) !=null && !"".equals(row.getCell(2))){
	                    if (row.getCell(2).getCellType() != CellType.BLANK) {
	                        if (row.getCell(2).getCellType() == CellType.STRING) {
	                        	quantity = row.getCell(2).getStringCellValue();
	                        } else if (row.getCell(2).getCellType() == CellType.NUMERIC) {
	                        	quantity = df.format(row.getCell(2).getNumericCellValue());
	                        }
	                    }
	                }
//                }catch (Exception e) {
//                }
                //货物类型
                String cargoType = "";
                if(row.getCell(3) !=null && !"".equals(row.getCell(3))){
                    if (row.getCell(3).getCellType() != CellType.BLANK) {
                        if (row.getCell(3).getCellType() == CellType.STRING) {
                        	cargoType = row.getCell(3).getStringCellValue();
                        } else if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                        	cargoType = df.format(row.getCell(3).getNumericCellValue());
                        }
                    }
                }
              //货物类型
                String reportIssueNo = "";
                if(row.getCell(4) !=null && !"".equals(row.getCell(4))){
                    if (row.getCell(4).getCellType() != CellType.BLANK) {
                        if (row.getCell(4).getCellType() == CellType.STRING) {
                        	reportIssueNo = row.getCell(4).getStringCellValue();
                        } else if (row.getCell(4).getCellType() == CellType.NUMERIC) {
                        	reportIssueNo = df.format(row.getCell(4).getNumericCellValue());
                        }
                    }
                }
               
               
                CargoGoodsnames bean = new CargoGoodsnames();
                bean.setGoodsCnnames(goodsCnnames);
                bean.setGoodsEnnames(goodsEnnames);
                bean.setCargoType(cargoType);
                bean.setReportIssueNo(reportIssueNo);
                if (StringUtils.isNotBlank(quantity)) {
                	bean.setQuantity(Integer.valueOf(quantity));
				}

//                bean.setErrorMessage(errorMessage);
                list.add(bean);
            }
            //单个文件数据的最大行数为2000
//            if(list != null && list.size()>2000){
//                throw new RuntimeException("单个文件数据的最大行数不能超过2000");
//            }

            if(isHaveErroe == true){
                return MessageInfo.ok(list,"haveError");
            }else{
                return MessageInfo.ok(list,"haveNoError");
            }
        } catch (Exception e) {
        	log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        } finally {
            //关闭
            input.close();
        }
    }
	@PostMapping(value = "/submitImport")
    public MessageInfo submitImport(@RequestBody List<CargoGoodsnames> data){
        if( data.size()>0){
        	for (int i = 0; i < data.size(); i++) {
    			data.get(i).setOrgId(SecurityUtils.getUser().getOrgId());
    			data.get(i).setCreateTime(LocalDateTime.now());
    			data.get(i).setCreatorId(SecurityUtils.getUser().getId());
    			data.get(i).setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
    		}
        	service.saveBatch(data);
        	 return MessageInfo.ok(data,"haveNoError");
        }else{
            throw new RuntimeException("列表无数据");
            //return com.efreight.common.core.utils.MessageInfo.ok();
        }
    }
	public boolean isEmptyRow(Row row) {
        if (row == null || row.toString().isEmpty()) {
            return true;
        } else {
            Iterator<Cell> it = row.iterator();
            boolean isEmpty = true;
            while (it.hasNext()) {
                Cell cell = it.next();
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }
}

