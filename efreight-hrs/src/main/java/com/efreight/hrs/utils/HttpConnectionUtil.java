package com.efreight.hrs.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.alibaba.druid.util.StringUtils;

/**
 * http 请求
 * @author cwd
 *
 */
public class HttpConnectionUtil {
	 /**
    * post方式发送请求
    * 
    * @param url 地址
    * @param params 参数
    * @param contentType 参数请求文本类型
    * @return
    * @throws Exception
    */
   public static String sendByPost(String url, Map<String, String> paramMap,String contentType) throws Exception {

       String retStr = "";
       if (!StringUtils.isEmpty(url)&& paramMap != null) {
	       StringBuilder param = new StringBuilder();
	       for (Map.Entry<String, String> entry : paramMap.entrySet()) {
	           param.append(entry.getKey());
	           param.append("=");
	           param.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
	           param.append("&");
	       }
	       param.deleteCharAt(param.lastIndexOf("&"));
           URL urlObj = new URL(url);
           HttpURLConnection httpConn = (HttpURLConnection)(urlObj.openConnection());
           // 设置http连接属性
           httpConn.setRequestProperty("Content-Type", contentType);
           //设置跟踪Key
           httpConn.setRequestProperty("Track-Key", Thread.currentThread().getName());

           httpConn.setRequestMethod("POST");
           httpConn.setDoOutput(true);
           httpConn.setDoInput(true);
           //设置连接主机超时：2分钟 (不建议太长)
           httpConn.setConnectTimeout(2*60*1000);
           //设置从主机读取数据超时:2分钟(不建议太长)
           httpConn.setReadTimeout(2*60*1000);
           
           // 发送请求数据,并设置为utf-8编码
           OutputStream out = httpConn.getOutputStream();
           out.write(param.toString().getBytes("UTF-8"));
           out.close();

           // 接受响应数据
           InputStream isr = httpConn.getInputStream();
           ByteArrayOutputStream bao = new ByteArrayOutputStream();
           int b;
           while ((b = isr.read()) != -1) {
               bao.write(b);
           }
           isr.close();
           // 关闭http连接
           httpConn.disconnect();
           retStr = new String(bao.toByteArray(), "ISO-8859-1");
           retStr = new String(retStr.getBytes("ISO-8859-1"), "UTF-8");
       }
       return retStr;
   }
   
	 /**
    * get方式发送请求
    * 
    * @param url 地址
    * @return
    * @throws Exception
    */
   public static String sendByGet(String url) throws Exception {

       String retStr = "";

       if (!StringUtils.isEmpty(url)) {
           URL urlObj = new URL(url);
           HttpURLConnection httpConn = (HttpURLConnection)(urlObj.openConnection());
           httpConn.setRequestProperty("accept", "*/*");
           httpConn.setRequestProperty("connection", "Keep-Alive");
           httpConn.setRequestProperty("user-agent",
               "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
           httpConn.setRequestMethod("GET");
           httpConn.setDoOutput(true);
           httpConn.setDoInput(true);
           //设置连接主机超时：2分钟 (不建议太长)
           httpConn.setConnectTimeout(2*60*1000);
           //设置从主机读取数据超时:2分钟(不建议太长)
           httpConn.setReadTimeout(2*60*1000);
           httpConn.connect();
           // 接受响应数据
           InputStream isr = httpConn.getInputStream();
           ByteArrayOutputStream bao = new ByteArrayOutputStream();
           int b;
           while ((b = isr.read()) != -1) {
               bao.write(b);
           }
           isr.close();
           // 关闭http连接
           httpConn.disconnect();
           retStr = new String(bao.toByteArray(), "ISO-8859-1");
           retStr = new String(retStr.getBytes("ISO-8859-1"), "UTF-8");
       }
       return retStr;
   }

}