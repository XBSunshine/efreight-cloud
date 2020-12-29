package com.efreight.common.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;

public class ExcelExportUtils {
	
	
	/**
    *
    * @param title    表格标题名
    * @param headers  表格属性列名数组
    * @param dataset  需要显示的数据集合,List<LinkedHashMap>
    * @param /out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
    * @param /pattern
    */
   @SuppressWarnings("unchecked")
   public void exportExcelLinkListMap(HttpServletResponse response, String title, String[] headers, List<LinkedHashMap> dataList, String filename) {
       // 声明一个工作薄
       HSSFWorkbook workbook = new HSSFWorkbook();
       // 生成一个表格
       HSSFSheet sheet = workbook.createSheet(title);
       // 设置表格默认列宽度为15个字节
       //sheet.setDefaultColumnWidth((short) 15);
       // 生成一个样式
       HSSFCellStyle style = this.getColumnTopStyle(workbook);//获取列头样式对象
       // 生成并设置另一个样式
       HSSFCellStyle style2 = this.getStyle(workbook);
       style2.setWrapText(true);


       // 产生表格标题行
       HSSFRow row = sheet.createRow(0);
       for (short i = 0; i < headers.length; i++) {
           HSSFCell cell = row.createCell(i);
           cell.setCellStyle(style);
           HSSFRichTextString text = new HSSFRichTextString(headers[i]);
           cell.setCellValue(text);

       }

       // 遍历集合数据，产生数据行
       if(dataList!=null&&dataList.size()>0) {
    	   for(int i=0;i<dataList.size();i++) {
    		   row = sheet.createRow(i+1);
    		   LinkedHashMap map = dataList.get(i);
    		   Iterator it = map.entrySet().iterator();
    		   int j = 0;
    		   while(it.hasNext()) {
    			   Map.Entry entity = (Entry) it.next(); 
    			   //目前map 没有泛型，所有 默认为String 后续有需要 可以增加类型强转
    			   HSSFCell cell = row.createCell(j);
                   cell.setCellStyle(style2);
                   if(entity.getValue()!=null) {
                	   cell.setCellValue(String.valueOf(entity.getValue()));
                   }else {
                	   cell.setCellValue("");
                   }
                   j++;
    		   }
    	   }
       }
       for (short i = 0; i < headers.length; i++) {
           sheet.autoSizeColumn((short) i); //调整列宽度
       }
       try {

           String fileName = "Excel-" + filename + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
           String headStr = "attachment; filename=\"" + fileName + "\"";
//           response.setContentType("APPLICATION/OCTET-STREAM");
           response.setContentType("application/vnd.ms-excel;charset=UTF-8");
           response.setCharacterEncoding("UTF-8");
           response.setHeader("Content-Disposition", headStr);
           OutputStream out = response.getOutputStream();
           workbook.write(out);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    public void exportExcelLinkListMapForXlsx(HttpServletResponse response, String title, String[] headers, List<LinkedHashMap> dataList, String filename) {
        // 声明一个工作薄
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 生成一个表格
        XSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        //sheet.setDefaultColumnWidth((short) 15);
        // 生成一个样式
        XSSFCellStyle style = this.getColumnTopStyleForXlsx(workbook);//获取列头样式对象
        // 生成并设置另一个样式
        XSSFCellStyle style2 = this.getStyleForXlsx(workbook);


        // 产生表格标题行
        XSSFRow row = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            XSSFRichTextString text = new XSSFRichTextString(headers[i]);
            cell.setCellValue(text);

        }

        // 遍历集合数据，产生数据行
        if(dataList!=null&&dataList.size()>0) {
            for(int i=0;i<dataList.size();i++) {
                row = sheet.createRow(i+1);
                LinkedHashMap map = dataList.get(i);
                Iterator it = map.entrySet().iterator();
                int j = 0;
                while(it.hasNext()) {
                    Map.Entry entity = (Entry) it.next();
                    //目前map 没有泛型，所有 默认为String 后续有需要 可以增加类型强转
                    XSSFCell cell = row.createCell(j);
                    cell.setCellStyle(style2);
                    if(entity.getValue()!=null) {
                        cell.setCellValue(String.valueOf(entity.getValue()));
                    }else {
                        cell.setCellValue("");
                    }
                    j++;
                }
            }
        }
        for (short i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn((short) i); //调整列宽度
        }
        try {

            String fileName = "Excel-" + filename + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
            String headStr = "attachment; filename=\"" + fileName + "\"";
//           response.setContentType("APPLICATION/OCTET-STREAM");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", headStr);
            OutputStream out = response.getOutputStream();
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
   private HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {

       // 设置字体
       HSSFFont font = workbook.createFont();
       //设置字体名字
       font.setFontName("Courier New");
       //设置字体大小
       font.setFontHeightInPoints((short) 11);
       //字体加粗
       font.setBold(true);
       //设置样式;
       HSSFCellStyle style = workbook.createCellStyle();
       //设置底边框;
       style.setBorderBottom(BorderStyle.THIN);
       //设置底边框颜色;
       style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
       //设置左边框;
       style.setBorderLeft(BorderStyle.THIN);
       //设置左边框颜色;
       style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
       //设置右边框;
       style.setBorderRight(BorderStyle.THIN);
       //设置右边框颜色;
       style.setRightBorderColor(IndexedColors.BLACK.getIndex());
       //设置顶边框;
       style.setBorderTop(BorderStyle.THIN);
       //设置顶边框颜色;
       style.setTopBorderColor(IndexedColors.BLACK.getIndex());
       //在样式用应用设置的字体;
       style.setFont(font);
       //设置自动换行;
       style.setWrapText(false);
       //设置水平对齐的样式为居中对齐;
       style.setAlignment(HorizontalAlignment.CENTER);
       //设置垂直对齐的样式为居中对齐;
       style.setVerticalAlignment(VerticalAlignment.CENTER);
       return style;

   }

   /*
    * 列数据信息单元格样式
    */
   private HSSFCellStyle getStyle(HSSFWorkbook workbook) {
       // 设置字体
       HSSFFont font = workbook.createFont();
       //设置字体大小
       //font.setFontHeightInPoints((short)10);
       //字体加粗
       //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
       //设置字体名字
       font.setFontName("Courier New");
       //设置样式;
       HSSFCellStyle style = workbook.createCellStyle();
       //设置底边框;
       style.setBorderBottom(BorderStyle.THIN);
       //设置底边框颜色;
       style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
       //设置左边框;
       style.setBorderLeft(BorderStyle.THIN);
       //设置左边框颜色;
       style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
       //设置右边框;
       style.setBorderRight(BorderStyle.THIN);
       //设置右边框颜色;
       style.setRightBorderColor(IndexedColors.BLACK.getIndex());
       //设置顶边框;
       style.setBorderTop(BorderStyle.THIN);
       //设置顶边框颜色;
       style.setTopBorderColor(IndexedColors.BLACK.getIndex());
       //在样式用应用设置的字体;
       style.setFont(font);
       //设置自动换行;
       style.setWrapText(false);
       //设置水平对齐的样式为居中对齐;
       style.setAlignment(HorizontalAlignment.CENTER);
       //设置垂直对齐的样式为居中对齐;
       style.setVerticalAlignment(VerticalAlignment.CENTER);

       return style;

   }

    private XSSFCellStyle getColumnTopStyleForXlsx(XSSFWorkbook workbook) {

        // 设置字体
        XSSFFont font = workbook.createFont();
        //设置字体名字
        font.setFontName("Courier New");
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBold(true);
        //设置样式;
        XSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;

    }

    /*
     * 列数据信息单元格样式
     */
    private XSSFCellStyle getStyleForXlsx(XSSFWorkbook workbook) {
        // 设置字体
        XSSFFont font = workbook.createFont();
        //设置字体大小
        //font.setFontHeightInPoints((short)10);
        //字体加粗
        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        XSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;

    }

}
