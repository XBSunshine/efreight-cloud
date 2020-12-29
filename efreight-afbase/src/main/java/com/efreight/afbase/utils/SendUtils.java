package com.efreight.afbase.utils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;

/** 
 * Created by code machine
 * @author TransformerPlugin
 */
@Slf4j
public class SendUtils {

	public static String doSend(String url, HashMap<String, String> bodyMap) {
		String str="";
		// 创建默认的httpClient实例.    
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost(url);
		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();		
		for (String bodykey : bodyMap.keySet()) {
			formparams.add(new BasicNameValuePair(bodykey,bodyMap.get(bodykey)));
            
        }
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// 将返回的数据直接转成String
					str = EntityUtils.toString(entity, "UTF-8");
				}
			} catch (Exception e) {
				log.info(e.getMessage());
			} finally {
				response.close();
			}

		} catch (ClientProtocolException e) {
			log.info(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.info(e.getMessage());
		} catch (IOException e) {
			log.info(e.getMessage());
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {
				log.info(e.getMessage());
			}
		}
		return str;
	}

}
