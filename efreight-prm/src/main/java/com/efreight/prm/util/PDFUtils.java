package com.efreight.prm.util;

import com.efreight.prm.entity.CoopBillEmail;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 根据模板生成pdf
 *
 * @author limr
 * @version 1.0.0
 * @date 2018年5月24日 上午11:19:08
 * @modify limr
 * @modify_date 2018年5月24日 上午11:19:08
 * @desc PDFUtils.java
 * @since 1.8
 */
@Transactional
public class PDFUtils {

    private static Logger logger = LoggerFactory.getLogger(PDFUtils.class);

    public static String pdfPath = LoginUtils.getRequest().getServletContext().getRealPath("/WEB-INF/download/printpdf/");

    public static String filePath = "/datadisk/html";

    public static String simhei = filePath + "/PDFtemplate/simhei.ttf";

    // 微软雅黑
    public static String yaheiPath = pdfPath + "msyh.ttf";


    public static String fillTemplate(CoopBillEmail coopBillEmail, List<CoopBillEmail> list, String parentPath) {
        // 模板路径
        //String templatePath="C:/Users/bxs/Documents/WeChat Files/wxid_h7jugpb9y65q22/FileStorage/File/2019-10/"+coopBillEmail.getPrintTemplate();
        String templatePath =parentPath+ "/PDFtemplate/"+coopBillEmail.getPrintTemplate();
        String savePath = parentPath + "/PDFtemplate/temp/generateBillTemp";
        //String savePath=PDFUtils.class.getResource("/").getPath()+"/PDFtemplate";
        //String templatePath ="C:/efreight/开发/prm/191112 PRM 翌飞账单/"+coopBillEmail.getPrintTemplate();
        //String savePath=PDFUtils.class.getResource("/").getPath()+"/PDFtemplate";
        //得到文件保存的名称
        String title=coopBillEmail.getMailTitle();
        String saveFilename = makeFileName(title + ".pdf");
        //得到文件的保存目录
        String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, String> valueData = new HashMap<>();
            valueData.put("txt_01", coopBillEmail.getTxt_01());//客商资料名称
            valueData.put("txt_02", coopBillEmail.getTxt_02());
            valueData.put("txt_03", coopBillEmail.getTxt_03());
            valueData.put("txt_04", coopBillEmail.getTxt_04());
            valueData.put("txt_05", coopBillEmail.getTxt_05());
            valueData.put("txt_06", coopBillEmail.getTxt_06());
            valueData.put("txt_07", coopBillEmail.getTxt_07());
            valueData.put("txt_08", coopBillEmail.getTxt_08());
            valueData.put("txt_09", coopBillEmail.getTxt_09());
            valueData.put("txt_10", coopBillEmail.getTxt_10());

            //明细
            for (int i = 0; i < list.size(); i++) {
                CoopBillEmail innerDetail = list.get(i);
                int index = (i + 1);
                if (index < 10) {
                    valueData.put("r_0" + index + "01", innerDetail.getR_01());
                    valueData.put("r_0" + index + "02", innerDetail.getR_02());
                    valueData.put("r_0" + index + "03", innerDetail.getR_03());
                    valueData.put("r_0" + index + "04", innerDetail.getR_04());
                    valueData.put("r_0" + index + "05", innerDetail.getR_05());
                    valueData.put("r_0" + index + "06", innerDetail.getR_06());
                    valueData.put("r_0" + index + "07", innerDetail.getR_07());
                    valueData.put("r_0" + index + "08", innerDetail.getR_08());
                    valueData.put("r_0" + index + "09", innerDetail.getR_09());
                } else {
                    valueData.put("r_" + index + "01", innerDetail.getR_01());
                    valueData.put("r_" + index + "02", innerDetail.getR_02());
                    valueData.put("r_" + index + "03", innerDetail.getR_03());
                    valueData.put("r_" + index + "04", innerDetail.getR_04());
                    valueData.put("r_" + index + "05", innerDetail.getR_05());
                    valueData.put("r_" + index + "06", innerDetail.getR_06());
                    valueData.put("r_" + index + "07", innerDetail.getR_07());
                    valueData.put("r_" + index + "08", innerDetail.getR_08());
                    valueData.put("r_" + index + "09", innerDetail.getR_09());
                }

            }

            //pdf填充数据以及下载
            loadPDF(templatePath, newPDFPath, valueData);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(1);
            throw new RuntimeException("IOException : " + e.getMessage());
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("DocumentException : " + e.getMessage());
        }
        return newPDFPath.replace(parentPath,"");
    }

    /**
     * 打印账单
     *
     * fjh
     * @param
     * @throws DocumentException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws BadPdfFormatException
     */
    public static void printBill(CoopBillEmail coopBillEmail, List<CoopBillEmail> list, String parentPath) throws BadPdfFormatException, FileNotFoundException, IOException, DocumentException {

        //新建临时保存路径的路径数组
        List<String> PDFPathList = new ArrayList<>();


       // String title=coopBillEmail.getTxt_08()+" 账单("+coopBillEmail.getTxt_09()+")： "+coopBillEmail.getTxt_01();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-ddHH-mm-ss");
        String pdfName = df1.format(new Date());
        //String templateName = order.getLetterPdf();
        // 模板路径
        String templatePath =parentPath+ "/PDFtemplate/"+coopBillEmail.getPrintTemplate();
        String savePath = parentPath + "/PDFtemplate/temp/printBillTemp";
        //String templatePath ="C:/efreight/开发/prm/191112 PRM 翌飞账单/"+coopBillEmail.getPrintTemplate();
        //String savePath=PDFUtils.class.getResource("/").getPath()+"/PDFtemplate";

        //得到文件保存的名称
        String saveFilename = makeFileName(pdfName + ".pdf");
        //得到文件的保存目录
        String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;
        PDFPathList.add(newPDFPath);

       // Map<String, String> valueData  = new HashMap<>();
        //月份全部大写
        //String Etd = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> valueData = new HashMap<>();
        valueData.put("txt_01", coopBillEmail.getTxt_01());//客商资料名称
        valueData.put("txt_02", coopBillEmail.getTxt_02());
        valueData.put("txt_03", coopBillEmail.getTxt_03());
        valueData.put("txt_04", coopBillEmail.getTxt_04());
        valueData.put("txt_05", coopBillEmail.getTxt_05());
        valueData.put("txt_06", coopBillEmail.getTxt_06());
        valueData.put("txt_07", coopBillEmail.getTxt_07());
        valueData.put("txt_08", coopBillEmail.getTxt_08());
        valueData.put("txt_09", coopBillEmail.getTxt_09());
        valueData.put("txt_10", coopBillEmail.getTxt_10());

        //明细
        for (int i = 0; i < list.size(); i++) {
            CoopBillEmail innerDetail = list.get(i);
            int index = (i + 1);
            if (index < 10) {
                valueData.put("r_0" + index + "01", innerDetail.getR_01());
                valueData.put("r_0" + index + "02", innerDetail.getR_02());
                valueData.put("r_0" + index + "03", innerDetail.getR_03());
                valueData.put("r_0" + index + "04", innerDetail.getR_04());
                valueData.put("r_0" + index + "05", innerDetail.getR_05());
                valueData.put("r_0" + index + "06", innerDetail.getR_06());
                valueData.put("r_0" + index + "07", innerDetail.getR_07());
                valueData.put("r_0" + index + "08", innerDetail.getR_08());
                valueData.put("r_0" + index + "09", innerDetail.getR_09());
            } else {
                valueData.put("r_" + index + "01", innerDetail.getR_01());
                valueData.put("r_" + index + "02", innerDetail.getR_02());
                valueData.put("r_" + index + "03", innerDetail.getR_03());
                valueData.put("r_" + index + "04", innerDetail.getR_04());
                valueData.put("r_" + index + "05", innerDetail.getR_05());
                valueData.put("r_" + index + "06", innerDetail.getR_06());
                valueData.put("r_" + index + "07", innerDetail.getR_07());
                valueData.put("r_" + index + "08", innerDetail.getR_08());
                valueData.put("r_" + index + "09", innerDetail.getR_09());
            }
        }

        //填充每个PDF
        loadPDF2(templatePath, newPDFPath, valueData,false);
        //合并PDF
        loadAllPDF(PDFPathList);
    }

    public static File loadPDF2(String templatePath, String newPDFPath, Map<String, String> valueData,boolean flage )
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {
        File file = new File(newPDFPath);
        if(!file.exists()){
            file = new File(newPDFPath);
        }
        FileOutputStream out = new FileOutputStream(file);// 输出流
        PdfReader reader = new PdfReader(templatePath);// 读取pdf模板
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, bos);
        AcroFields form = stamper.getAcroFields();


        // 给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
        BaseFont bf=null;
        if(flage){
            bf = BaseFont.createFont(yaheiPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }else{
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
        }
//		BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);

        form.addSubstitutionFont(bf);
        String modTop ="";
        String modRight ="";
        String modBottom ="";
        String modLeft="";


        for (String key : valueData.keySet()) {
            form.setField(key,valueData.get(key));
            if(key.equals("modLeft")){
                modLeft = valueData.get(key);
            }else if(key.equals("modTop")){
                modTop = valueData.get(key);
            }
            else if(key.equals("modRight")){
                modRight = valueData.get(key);
            }
            else if(key.equals("modBottom")){
                modBottom = valueData.get(key);
            }
        }
        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true
        stamper.close();

        //Document doc = new Document();
        Document doc = new Document(new RectangleReadOnly(842.0F, 595.0F));
        //Document doc = new Document(PageSize.A4,50,50,30,20);


        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), 1);
        copy.addPage(importPage);
        doc.close();
        return file;
    }

    public static void loadAllPDF(List<String> PDFPathList)
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {

        //Document document = new Document();
        Document document = new Document(new RectangleReadOnly(842.0F, 595.0F));

        PdfWriter writer = PdfWriter.getInstance(document, LoginUtils.getResponse().getOutputStream());
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        int totalPages = 0;
        List<PdfReader> readers = new ArrayList<PdfReader>();
        for (String PDFPath : PDFPathList) {
            PdfReader pdfReader = new PdfReader(PDFPath);
            totalPages += pdfReader.getNumberOfPages();
            readers.add(pdfReader);
        }

        int pageOfCurrentReaderPDF = 0;
        Iterator<PdfReader> iteratorPDFReader = readers.iterator();

        // Loop through the PDF files and add to the output.
        while (iteratorPDFReader.hasNext()) {
            PdfReader pdfReader = iteratorPDFReader.next();

            // Create a new page in the target for each source page.
            while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                document.newPage();//创建新的一页
                pageOfCurrentReaderPDF++;
                PdfImportedPage page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                cb.addTemplate(page, 0, 0);
            }
            pageOfCurrentReaderPDF = 0;
        }
        HttpServletResponse response = LoginUtils.getResponse();
//		String pdfName = DateUtils.convert(new Date(), DateUtils.DATE_FORMAT).replace("-", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String pdfName = df.format(new Date());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=" + "DeliveryDocumentList-" + pdfName + ".pdf");
        document.close();
        writer.close();

    }

    /**
     * 公用打印预览
     *
     * @param file    文件
     * @param pdfName 下载的文件名
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void printPdf(File file, String pdfName) throws FileNotFoundException, IOException {
        HttpServletResponse response = LoginUtils.getResponse();
        FileInputStream in = new FileInputStream(file);
        long l = file.length();
        int k = 0;
        byte abyte0[] = new byte[65000];
        response.setContentType("application/pdf;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength((int) l);
        response.setHeader("Content-Disposition", "inline; filename=" + pdfName + ".pdf");
        OutputStream out = LoginUtils.getResponse().getOutputStream();

        while ((long) k < l) {
            int j;
            j = in.read(abyte0, 0, 65000);
            k += j;
            out.write(abyte0, 0, j);

        }

        out.flush();
        in.close();
        out.close();
    }

    //合并  pdfFilenames为文件路径数组，targetFileName为目标pdf路径
    public static void combinPdf(String[] pdfFilenames, String targetFilename)
            throws Exception {
        PdfReader reader = null;
        Document doc = new Document();
        PdfCopy pdfCopy = new PdfCopy(doc, new FileOutputStream(targetFilename));
        int pageCount = 0;
        doc.open();
        for (int i = 0; i < pdfFilenames.length; ++i) {
            System.out.println(pdfFilenames[i]);
            reader = new PdfReader(pdfFilenames[i]);
            pageCount = reader.getNumberOfPages();
            for (int j = 1; j <= pageCount; ++j) {
                pdfCopy.addPage(pdfCopy.getImportedPage(reader, j));
            }
        }
        doc.close();
    }

    /**
     * 公用 pdf填充数据以及下载
     *
     * @param templatePath 模板路径
     * @param newPDFPath   得到文件的保存目录+文件名
     * @param valueData    数据
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DocumentException
     * @throws BadPdfFormatException
     */
    public static File loadPDF(String templatePath, String newPDFPath, Map<String, String> valueData)
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {
        File file = new File(newPDFPath);
        if (!file.exists()) {
            file = new File(newPDFPath);
        }
        FileOutputStream out = new FileOutputStream(file);// 输出流
        PdfReader reader = new PdfReader(templatePath);// 读取pdf模板
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, bos);
        AcroFields form = stamper.getAcroFields();

        // 给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
        //BaseFont.AddToResourceSearch("iTextAsianCmaps.dll");
        BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        form.addSubstitutionFont(bf);


        for (String key : valueData.keySet()) {
            form.setField(key, valueData.get(key));
        }
        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true
        stamper.close();

        Document doc = new Document();
        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), 1);
        copy.addPage(importPage);
        doc.close();
        return file;
    }

    /**
     * 公用
     *
     * @param filename 文件的原始名称
     * @return uuid+"_"+文件的原始名称
     * @Method: makeFileName
     * @Description: 生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
     */
    public static String makeFileName(String filename) {  //2.jpg
        //为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
        //return UUID.randomUUID().toString() + "_" + filename;
        return filename;
    }

    /**
     * 公用
     * 为防止一个目录下面出现太多文件，要使用hash算法打散存储
     *
     * @param filename 文件名，要根据文件名生成存储目录
     * @param savePath 文件存储路径
     * @return 新的存储目录
     * @Method: makePath
     * @Description:
     */
    public static String makePath(String filename, String savePath) {
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        String hashDir = makeHashDir(filename);
        //构造新的保存目录
        String dir = savePath + hashDir;  //upload\2\3  upload\3\5
        //File既可以代表文件也可以代表目录
        File file = new File(dir);
        //如果目录不存在
        if (!file.exists()) {
            //创建目录
            file.mkdirs();
        }
        return dir;
    }

    public static String makeHashDir(String filename) {
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        int hashcode = filename.hashCode();
        int dir1 = hashcode & 0xf;  //0--15
        int dir2 = (hashcode & 0xf0) >> 4;  //0-15
        //构造新的保存目录
        String hashDir = "/" + dir1 + "/" + dir2;  //upload/2/3  upload/3/5
        return hashDir;
    }

    /**
     * null判断
     *
     * @param value
     */
    public static String checkNull(String value) {
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * 合并pdf填充数据以及下载(现在好返回下载好文件路径)
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BadPdfFormatException
     */
    public static void loadAllPDFForFile(List<String> PDFPathList, String lastNewFilePath, String langForPage)
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {
        String path = lastNewFilePath.substring(0, lastNewFilePath.lastIndexOf("/"));
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        Document document = new Document(new RectangleReadOnly(842.0F, 595.0F));

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(lastNewFilePath));
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        int totalPages = 0;
        List<PdfReader> readers = new ArrayList<PdfReader>();
        for (String PDFPath : PDFPathList) {
            PdfReader pdfReader = new PdfReader(PDFPath);
            totalPages += pdfReader.getNumberOfPages();
            readers.add(pdfReader);
        }

        int pageOfCurrentReaderPDF = 0;
        Iterator<PdfReader> iteratorPDFReader = readers.iterator();

        // Loop through the PDF files and add to the output.

        while (iteratorPDFReader.hasNext()) {
            PdfReader pdfReader = iteratorPDFReader.next();

            // Create a new page in the target for each source page.
            while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                document.newPage();//创建新的一页
                onEndPage(writer, document, totalPages, langForPage);
                pageOfCurrentReaderPDF++;
                PdfImportedPage page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                cb.addTemplate(page, 0, 0);
            }
            pageOfCurrentReaderPDF = 0;
        }
        document.close();
        writer.close();
    }

    //** 显示当前页码
    @SneakyThrows
    private static void onEndPage(PdfWriter writer, Document document, int totalPages, String langForPage) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        String text = "";
        if ("en".equals(langForPage)) {
            //Page 1 of 2 Pages
            text = "Page " + writer.getPageNumber() + " of " + totalPages + " Pages";
        } else if("ch".equals(langForPage)) {
            text = "第" + writer.getPageNumber() + "页,共" + totalPages + "页";
        }
        cb.beginText();

        cb.setFontAndSize(BaseFont.createFont(PDFUtils.simhei, BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 8);
        cb.setTextMatrix(272, 30);//定位“第x页,共” 在具体的页面调试时候需要更改这xy的坐标
        cb.showText(text);
        cb.endText();
        //** 创建以及固定显示总页数的位置
//        cb.addTemplate(writer.getDirectContent().createTemplate(100, 100), 283, 30);//定位“y页” 在具体的页面调试时候需要更改这xy的坐标

//            cb.saveState();
        cb.stroke();
        cb.restoreState();
        cb.closePath();
    }

}
