package com.efreight.afbase.utils;

import cn.hutool.core.util.StrUtil;
import com.efreight.afbase.entity.Letters;
import com.efreight.afbase.entity.OperationPlanPrintTag;
import com.efreight.afbase.entity.OrderLetters;
import com.efreight.common.security.util.SecurityUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
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
public class PDFUtils {


    public static String filePath = FilePathUtils.filePath;
    public static String simhei = filePath + "/PDFtemplate/simhei.ttf";
    // 微软雅黑
    public static String yaheiPath = filePath + "/PDFtemplate/YaHei.ttf";

    //分页中英文标记-中文
    public static final String PAGE_CH = "ch";
    //分页中英文标记-英文
    public static final String PAGE_EN = "en";


    /**
     * 操作计划打印标签
     * 2019年11月12日14:51:05
     * sunshine
     *
     * @param list
     */
    @SneakyThrows
    public static void printAllTag(List<OperationPlanPrintTag> list) {

        //新建临时保存路径的路径数组
        List<String> PDFPathList = new ArrayList<>();

        for (OperationPlanPrintTag operationPlanPrintTag : list) {

            // 模板路径
            String templatePath = filePath + "/PDFtemplate/MAWB_HAWB_100x80.pdf";
            String savePath = filePath + "/PDFtemplate/temp/printTag";

            //得到文件保存的名称
            String saveFilename = makeFileName(operationPlanPrintTag.getAwbNumber() + "$" + operationPlanPrintTag.getSequence() + ".pdf");
            //得到文件的保存目录
            String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;
            PDFPathList.add(newPDFPath);

            Map<String, String> valueData = new HashMap<>();

            valueData.put("txtAWBBarCode", "*" + operationPlanPrintTag.getAwbNumber() + "*");
            valueData.put("txtHAWBBarCode", StrUtil.isBlank(operationPlanPrintTag.getHawbNumber()) ? "" : "*" + operationPlanPrintTag.getHawbNumber() + "*");
            valueData.put("txt01", StrUtil.isBlank(operationPlanPrintTag.getCarrierCode()) ? "" : operationPlanPrintTag.getCarrierCode());
            valueData.put("txt02", operationPlanPrintTag.getAwbNumber());
            valueData.put("txt03", operationPlanPrintTag.getPiecesHawb() == null ? "" : operationPlanPrintTag.getPieceNumber() + "/" + operationPlanPrintTag.getPiecesHawb());
            valueData.put("txt04", operationPlanPrintTag.getSequence());
            valueData.put("txt05", StrUtil.isBlank(operationPlanPrintTag.getDeparture()) ? "" : operationPlanPrintTag.getDeparture());
            valueData.put("txt06", StrUtil.isBlank(operationPlanPrintTag.getDestinatonAwb()) ? "" : operationPlanPrintTag.getDestinatonAwb());
            valueData.put("txt07", operationPlanPrintTag.getPiecesAwb() == null ? "" : operationPlanPrintTag.getPiecesAwb().toString());
            valueData.put("txt08", StrUtil.isBlank(operationPlanPrintTag.getHawbNumber()) ? "" : operationPlanPrintTag.getHawbNumber());
            valueData.put("txt09", StrUtil.isBlank(operationPlanPrintTag.getDestinationHawb()) ? "" : operationPlanPrintTag.getDestinationHawb());

            //填充每个PDF
            loadPDF2(templatePath, newPDFPath, valueData, false, false);
        }
        loadAllPDF1(PDFPathList);
    }

    /**
     * 操作计划打印标签 new (返回路径)
     *
     * @param list
     */
    @SneakyThrows
    public static String printAllTagNew(List<OperationPlanPrintTag> list) {

        //新建临时保存路径的路径数组
        List<String> PDFPathList = new ArrayList<>();

        for (OperationPlanPrintTag operationPlanPrintTag : list) {

            // 模板路径
            String templatePath = filePath + "/PDFtemplate/MAWB_HAWB_100x80.pdf";
            String savePath = filePath + "/PDFtemplate/temp/printTag";

            //得到文件保存的名称
            String saveFilename = makeFileName(operationPlanPrintTag.getAwbNumber() + "$" + operationPlanPrintTag.getSequence() + ".pdf");
            //得到文件的保存目录
            String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;
            PDFPathList.add(newPDFPath);

            Map<String, String> valueData = new HashMap<>();

            valueData.put("txtAWBBarCode", "*" + operationPlanPrintTag.getAwbNumber() + "*");
            valueData.put("txtHAWBBarCode", StrUtil.isBlank(operationPlanPrintTag.getHawbNumber()) ? "" : "*" + operationPlanPrintTag.getHawbNumber() + "*");
            valueData.put("txt01", StrUtil.isBlank(operationPlanPrintTag.getCarrierCode()) ? "" : operationPlanPrintTag.getCarrierCode());
            valueData.put("txt02", operationPlanPrintTag.getAwbNumber());
            valueData.put("txt03", operationPlanPrintTag.getPiecesHawb() == null ? "" : operationPlanPrintTag.getPieceNumber() + "/" + operationPlanPrintTag.getPiecesHawb());
            valueData.put("txt04", operationPlanPrintTag.getSequence());
            valueData.put("txt05", StrUtil.isBlank(operationPlanPrintTag.getDeparture()) ? "" : operationPlanPrintTag.getDeparture());
            valueData.put("txt06", StrUtil.isBlank(operationPlanPrintTag.getDestinatonAwb()) ? "" : operationPlanPrintTag.getDestinatonAwb());
            valueData.put("txt07", operationPlanPrintTag.getPiecesAwb() == null ? "" : operationPlanPrintTag.getPiecesAwb().toString());
            valueData.put("txt08", StrUtil.isBlank(operationPlanPrintTag.getHawbNumber()) ? "" : operationPlanPrintTag.getHawbNumber());
            valueData.put("txt09", StrUtil.isBlank(operationPlanPrintTag.getDestinationHawb()) ? "" : operationPlanPrintTag.getDestinationHawb());

            //填充每个PDF
            loadPDF2(templatePath, newPDFPath, valueData, false, false);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String pdfName = df.format(new Date());
        String lastFilePath = filePath+"/PDFtemplate/temp/DeliveryDocumentList-" +SecurityUtils.getUser().getOrgId()+ pdfName + ".pdf";
        //合并PDF
        loadAllPDFForFile(PDFPathList, lastFilePath, null);
        return lastFilePath.replace(filePath, "");
    }

    /**
     * 打印全部托书
     * 2018年6月29日18:57:05
     * fjh
     *
     * @param orderList
     * @throws IOException
     * @throws FileNotFoundException
     * @throws BadPdfFormatException
     */
    public static void printAllLetters(List<Letters> orderList) throws BadPdfFormatException, FileNotFoundException, IOException, DocumentException {

        //新建临时保存路径的路径数组
        List<String> PDFPathList = new ArrayList<>();

        for (Letters order : orderList) {

            String mwbId = order.getInput001();

            String templateName = order.getLetterPdf();
            // 模板路径
//            String templatePath = filePath + "/PDFtemplate/" + templateName + ".pdf";
            String templatePath = PDFUtils.filePath + "/PDFtemplate/temp/" + templateName.split("/")[templateName.split("/").length - 1];
            downloadFile(templateName, templatePath);

            String savePath = filePath + "/PDFtemplate/temp/opertionPlan/letter";

            //得到文件保存的名称
            String saveFilename = makeFileName(mwbId + ".pdf");
            //得到文件的保存目录
            String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;
            PDFPathList.add(newPDFPath);

            Map<String, String> valueData = new HashMap<>();
            //月份全部大写
            String Etd = "";
//            if (order.getEtd()!=null) {
//            	Etd = new SimpleDateFormat("MMM d", Locale.US).format(order.getEtd()).toUpperCase();
//            }
            valueData.put("Input001", order.getInput001());
            valueData.put("Input002", order.getInput002());
            valueData.put("Input003", order.getInput003());
            valueData.put("Input004", order.getInput004());
            valueData.put("Input005", order.getInput005());
            valueData.put("Input006", order.getInput006());
            valueData.put("Input007", order.getInput007());
            valueData.put("Input008", order.getInput008());//此处代表主单数量
            valueData.put("Input009", order.getInput009());
            valueData.put("Input010", order.getInput010());
            valueData.put("Input011", order.getInput011());
            valueData.put("Input012", order.getInput012());
            valueData.put("Input013", order.getInput013());
            valueData.put("Input014", order.getInput014());
            valueData.put("Input015", order.getInput015());
            valueData.put("Input016", order.getInput016());

            valueData.put("Input0071", order.getInput0071());
            valueData.put("Input0072", order.getInput0072());
            valueData.put("Input0073", order.getInput0073());
            valueData.put("Input0091", order.getInput0091());
            valueData.put("Input0092", order.getInput0092());
            valueData.put("Input017", order.getInput017());
            valueData.put("Input018", order.getInput018());
            valueData.put("Input0191", order.getInput0191());
            valueData.put("Input0192", order.getInput0192());
            valueData.put("Input0193", order.getInput0193());
            valueData.put("Input0194", order.getInput0194());
            valueData.put("Input019", order.getInput019());
            valueData.put("Input020", order.getInput020());
            valueData.put("Input021", order.getInput021());
            valueData.put("Input022", order.getInput022());
            valueData.put("Input023", order.getInput023());
            valueData.put("Input024", order.getInput024());
            valueData.put("Input025", order.getInput025());
            valueData.put("Input026", order.getInput026());
            valueData.put("Input027", order.getInput027());
            valueData.put("Input028", order.getInput028());
            valueData.put("Input029", order.getInput029());
            valueData.put("Input030", order.getInput030());

            //填充每个PDF
            loadPDF2(templatePath, newPDFPath, valueData, false, false);
        }

        //合并PDF
        loadAllPDF(PDFPathList);
    }

    public static boolean downloadFile(String fileURL, String fileName) {
        try {
            String path = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 合并pdf填充数据以及下载(直接返回流方式)
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BadPdfFormatException
     */
    public static void loadAllPDF(List<String> PDFPathList)
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {

        Document document = new Document();

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
        Document document = null;
        if(lastNewFilePath.contains("DeliveryDocumentList-")) {
        	document = new Document(new Rectangle(280, 220));
        }else {
        	document = new Document();
        }
        

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

    /**
     * 合并pdf填充数据以及下载
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BadPdfFormatException
     */
    public static void loadAllPDF1(List<String> PDFPathList)
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {

        Document document = new Document(new Rectangle(280, 220));

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
        document.close();
        HttpServletResponse response = LoginUtils.getResponse();
//		String pdfName = DateUtils.convert(new Date(), DateUtils.DATE_FORMAT).replace("-", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String pdfName = df.format(new Date());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=" + "DeliveryDocumentList-" + pdfName + ".pdf");
        writer.close();

    }

    /**
     * 打印预览
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

    /**
     * pdf填充数据以及下载
     *
     * @param templatePath 模板路径
     * @param newPDFPath   得到文件的保存目录+文件名
     * @param valueData    数据
     * @return
     * @throws FileNotFoundException
     * @throws IOException
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

    public static File loadPDF2(String templatePath, String newPDFPath, Map<String, String> valueData, boolean font, boolean ifSeal)
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
        BaseFont bf = null;
        if (font) {
            bf = BaseFont.createFont(yaheiPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } else {
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        }
//		BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);

        form.addSubstitutionFont(bf);
        String modTop = "";
        String modRight = "";
        String modBottom = "";
        String modLeft = "";


        for (String key : valueData.keySet()) {
            if (!"org_seal".equals(key)) {
                form.setField(key, valueData.get(key));
            }
            if (key.equals("modLeft")) {
                modLeft = valueData.get(key);
            } else if (key.equals("modTop")) {
                modTop = valueData.get(key);
            } else if (key.equals("modRight")) {
                modRight = valueData.get(key);
            } else if (key.equals("modBottom")) {
                modBottom = valueData.get(key);
            }
        }
        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true

        if (ifSeal) {

            int pageNo = form.getFieldPositions("org_seal").get(0).page;
            Rectangle signRect = form.getFieldPositions("org_seal").get(0).position;
            float x = signRect.getLeft();
            float y = signRect.getBottom();
            // 读图片
            String imageUrl = filePath + "/PDFtemplate/temp/img/orderLetter/" + valueData.get("org_seal").substring(valueData.get("org_seal").lastIndexOf("/") + 1);
            downloadFile(valueData.get("org_seal"), imageUrl);
            Image image = Image.getInstance(imageUrl);
            // 获取操作的页面
            PdfContentByte under = stamper.getOverContent(pageNo);
            // 根据域的大小缩放图片
            image.scaleToFit(signRect.getWidth(), signRect.getHeight());
            // 添加图片
            image.setAbsolutePosition(x, y);
            under.addImage(image);
        }

        stamper.close();

//        Document doc = new Document();
        Document doc = new Document();

        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        for(int i=1, len=reader.getNumberOfPages(); i<=len; i++){
            PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), i);
            copy.addPage(importPage);
        }
        doc.close();
        return file;
    }

    /* @Method: makeFileName
     * @Description: 生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
     * @param filename 文件的原始名称
     * @return uuid+"_"+文件的原始名称
     */
    public static String makeFileName(String filename) {  //2.jpg
        //为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
        return UUID.randomUUID().toString() + "_" + filename;
    }

    /**
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
     * 打印订航托书
     * 2018年6月29日18:57:05
     * fjh
     *
     * @param orderList
     * @throws IOException
     * @throws FileNotFoundException
     * @throws BadPdfFormatException
     */
    public static void printArderLetters(List<OrderLetters> orderList) throws BadPdfFormatException, FileNotFoundException, IOException, DocumentException {

        //新建临时保存路径的路径数组
        List<String> PDFPathList = new ArrayList<>();

        for (OrderLetters order : orderList) {

            String mwbId = order.getInput04();

            String templateName = order.getInput04();
            // 模板路径
            String templatePath = filePath + "/PDFtemplate/AE_Letter_Agent.pdf";
            String savePath = filePath + "/PDFtemplate/temp/printBillTemp";
            //String templatePath ="C:/Users/bxs/Documents/WeChat Files/wxid_h7jugpb9y65q22/FileStorage/File/2019-11/AE_Letter_Agent.pdf";
            //String savePath ="C:/xshell/PDFtemplate";

            //得到文件保存的名称
            String saveFilename = makeFileName(mwbId + ".pdf");
            //得到文件的保存目录
            String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;
            PDFPathList.add(newPDFPath);

            Map<String, String> valueData = new HashMap<>();
            //月份全部大写
            String Etd = "";
//            if (order.getEtd()!=null) {
//            	Etd = new SimpleDateFormat("MMM d", Locale.US).format(order.getEtd()).toUpperCase();
//            }
            valueData.put("Input01", order.getInput01());
            valueData.put("Input02", order.getInput02());
            valueData.put("Input03", order.getInput03());
            valueData.put("Input04", order.getInput04());
            valueData.put("Input05", order.getInput05());
            valueData.put("Input06", order.getInput06());
            valueData.put("Input07", order.getInput07());
            valueData.put("Input08", order.getInput08());
            valueData.put("Input09", order.getInput09());
            valueData.put("Input10", order.getInput10());
            valueData.put("Input11", order.getInput11());
            valueData.put("Input12", order.getInput12());
            valueData.put("Input13", order.getInput13());
            valueData.put("Input14", order.getInput14());
            valueData.put("Input15", order.getInput15());
            valueData.put("Input16", order.getInput16());
            valueData.put("Input17", order.getInput17());
            valueData.put("Input18", order.getInput18());
            valueData.put("Input19", order.getInput19());
            valueData.put("Input20", order.getInput20());
            valueData.put("org_seal", order.getOrgSeal());

            //填充每个PDF
            if (StrUtil.isNotBlank(order.getOrgSeal())) {
                loadPDF2(templatePath, newPDFPath, valueData, false, true);
            } else {
                loadPDF2(templatePath, newPDFPath, valueData, false, false);
            }
        }

        //合并PDF
        loadAllPDF(PDFPathList);
    }

}
