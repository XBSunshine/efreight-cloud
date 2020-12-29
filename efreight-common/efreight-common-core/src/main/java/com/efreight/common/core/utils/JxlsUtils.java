package com.efreight.common.core.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jxls.common.Context;
import org.jxls.expression.JexlExpressionEvaluator;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通过jxls模板导出excel
 */
@UtilityClass
public class JxlsUtils {

    private String modelDownloadPath = "/datadisk/html/PDFtemplate/temp/jxls_model/";
    public String modelRootPath = "/datadisk/html/PDFtemplate/";

    @SneakyThrows
    public void exportExcel(InputStream is, OutputStream os, Map<String, Object> model) {
        Context context = new Context();
        if (model != null) {
            for (String key : model.keySet()) {
                context.putVar(key, model.get(key));
            }
        }
        JxlsHelper jxlsHelper = JxlsHelper.getInstance();
        Transformer transformer = jxlsHelper.createTransformer(is, os);
        jxlsHelper.processTemplate(context, transformer);
    }

    @SneakyThrows
    public void exportExcelToFile(String in, String out, Map<String, Object> model) {
        String path = out.substring(0, out.lastIndexOf("/"));
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        exportExcel(new FileInputStream(in), new FileOutputStream(out), model);
    }

    @SneakyThrows
    public void exportExcelToResponse(File in, Map<String, Object> model) {
        HttpServletResponse response = WebUtils.getResponse();
        String fileName = "Excel-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        String headStr = "attachment; filename=\"" + fileName + "\"";
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", headStr);
        OutputStream out = response.getOutputStream();
        exportExcel(new FileInputStream(in), out, model);
    }

    @SneakyThrows
    public void exportExcelWithUploadModel(String modelPath, Map<String, Object> model) {
        String path = modelDownloadPath + UUID.randomUUID().toString() + "/" + modelPath.substring(modelPath.lastIndexOf("/") + 1, modelPath.length());
        downloadFile(modelPath, path);
        exportExcelToResponse(new File(path), model);
    }

    @SneakyThrows
    public void exportExcelWithLocalModel(String modelPath, Map<String, Object> model) {
        exportExcelToResponse(new File(modelPath), model);
    }

    public void downloadFile(String fileURL, String fileName) {
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            String path = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SneakyThrows
    public void responseExcel(String filePath) {
        HttpServletResponse response = WebUtils.getResponse();
        String fileName = "Excel-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
        String headStr = "attachment; filename=\"" + fileName + "\"";
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", headStr);
        OutputStream out = response.getOutputStream();
        String path = filePath.substring(0, filePath.lastIndexOf("/"));
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileInputStream in = new FileInputStream(filePath);
        try {
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

}
