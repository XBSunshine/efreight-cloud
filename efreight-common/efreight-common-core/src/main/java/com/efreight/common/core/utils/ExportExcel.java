package com.efreight.common.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

/**
 * 导出Excel固定列公共方法
 *
 * @author qipm
 */
public class ExportExcel<T> {
    /**
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上
     *
     * @param title    表格标题名
     * @param headers  表格属性列名数组
     * @param dataset  需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                 javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
     * @param /out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param /pattern
     */
    @SuppressWarnings("unchecked")
    public void exportExcel(HttpServletResponse response, String title, String[] headers, Collection<T> dataset, String filename) {
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


        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);

        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = (T) it.next();
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();
            for (short i = 0; i < fields.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);
                Field field = fields[i];
                String fieldName = field.getName();
                String getMethodName = "get"
                        + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                try {
                    Class tCls = t.getClass();
                    Method getMethod = tCls.getMethod(getMethodName, new Class[]{});
                    Object value = getMethod.invoke(t, new Object[]{});
                    if (value == null) {
                        value = "";
                    }
                    // 判断值的类型后进行强制类型转换
                    //String textValue = null;
                    if (value instanceof Integer) {
                        int intValue = (Integer) value;
                        cell.setCellValue(intValue);
                    } else if (value instanceof Long) {
                        long longValue = (Long) value;
                        cell.setCellValue(longValue);
                    } else if (value instanceof Date) {
                        SimpleDateFormat format = null;
                        if ("com.efreight.hrs.entity.UserExcel".equals(t.getClass().getName()) && ("userBirthday".equals(fieldName) || "hireDate".equals(fieldName))) {
                            format = new SimpleDateFormat("yyyy-MM-dd");
                        } else {
                            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        }
                        String str = format.format(value);
                        cell.setCellValue(str);
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }

                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } finally {
                    // 清理资源
                }
            }
        }
        for (short i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn((short) i); //调整列宽度
        }
        try {

            String fileName = "Excel-" + filename + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
            String headStr = "attachment; filename=\"" + fileName + "\"";
//            response.setContentType("APPLICATION/OCTET-STREAM");
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
        style.setWrapText(true);//自动换行
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
        //设置字体颜色
//        font.setColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
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
        style.setWrapText(true);//自动换行
        return style;

    }

    /**
     * 导出,可map
     * @param data
     * @param Excelcolumn
     * @param response
     * @param excelName
     * @return
     */
    public static boolean getExcel(List<Object[]> data, String[] Excelcolumn, HttpServletResponse response, String excelName){
        boolean flag = false;
        try{

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(excelName); //创建的excel的名称
            HSSFCellStyle style = disFont(wb);
            sheet.setDefaultColumnWidth(20);
            HSSFRow row =sheet.createRow(0);
            HSSFCell cell;
//        	cell.setEncoding(HSSFCell.ENCODING_UTF_16);
            for(int x=0;x<Excelcolumn.length;x++){  // 输出列
                cell = row.createCell(x);
                cell.setCellStyle(style);
                if(!CellType.STRING.equals(cell.getCellType())){
                    cell.setCellType(CellType.STRING);
                }
                row.setHeightInPoints((short)20);
                cell.setCellValue(new HSSFRichTextString(Excelcolumn[x]));
            }
            HSSFCellStyle contentStyle = disContentFont(wb);

            for (int i = 0; i < data.size(); i++) { //内容
                row = sheet.createRow(i+1);
                Object [] obj =(Object [])data.get(i);
                cell = row.createCell(0);
                cell.setCellStyle(contentStyle);
                cell.setCellValue(new HSSFRichTextString((i+1)+""));
                for(int j=0;j<obj.length;j++){
                    cell = row.createCell((j+1));
                    cell.setCellStyle(contentStyle);
                    cell.setCellValue(new HSSFRichTextString((obj[j]==null?"":obj[j])+""));
                }
            }
//			String fileName = new String(excelName.getBytes("gbk"),"ISO8859-1");

            response.setContentType("application/x-download;charset=utf-8");//下面三行是关键代码，处理乱码问题
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename="+new String(excelName.getBytes("gbk"), "ISO8859-1")+".xls");

            wb.write(response.getOutputStream());
            flag = true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            flag = false;
        }
        return flag;
    }
    /**
     * 设置导出excel的格式样式
     * @param wb
     * @return
     */
    private static HSSFCellStyle disFont(HSSFWorkbook wb){
        HSSFCellStyle style = disContentFont(wb);
        HSSFFont font = wb.createFont();

        font.setColor(HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }
    /**
     * 设置导出excel的格式样式
     * @param wb
     * @return
     */
    private static HSSFCellStyle disContentFont(HSSFWorkbook wb){
        HSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
//         style.setAlignment(HorizontalAlignment.CENTER);
//         style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }
}