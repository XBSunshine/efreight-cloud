package com.efreight.common.core.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author lc
 * @date 2020/12/2 17:26
 *
 */
public class SignUtil {

    private SignUtil(){
        throw new Error("Don't init Sign Util.");
    }

    /**
     * 授权信息
     * appId: secret
     */
    private final static HashMap<String, String> licenses = new HashMap<>(4);

    /**
     * 时间间隔(毫秒) 8秒
     */
    public final static Integer TIME_INTERVAL =  8 * 1000;

    static {
        //微信小程序调用
        licenses.put("wx-oct20m12d04", "ezapp8wlv3on4dwte0noson6bmaugvyy");
    }

    /**
     * 生成md5签名字符串
     * @param request
     * @return
     */
    public static String signMD5(HttpServletRequest request){
        String appId = request.getParameter("appId"),
                timeStamp = request.getParameter("ts"),
                sign = request.getParameter("sign"),
                uri = request.getRequestURI();

        Assert.hasLength(appId, "Invalid appId.");
        Assert.hasLength(timeStamp, "Invalid timeStamp.");
        Assert.hasLength(sign, "Invalid signature");

        String secret = licenses.get(appId);
        Assert.hasText(secret, "Invalid signature.");

        Enumeration enumeration = request.getParameterNames();
        ArrayList<String> names = new ArrayList<>();
        while (enumeration.hasMoreElements()){
            String key = enumeration.nextElement().toString();
            if("sign".equals(key)){
                continue;
            }
            names.add(key + "=" + Optional.ofNullable(request.getParameter(key)).orElse(""));
        }
        names.add("secret="+secret);

        Collections.sort(names);
        StringBuilder builder = new StringBuilder();
        builder.append(uri);
        builder.append("?");
        names.forEach((key)->{
            builder.append(key);
            builder.append("&");
        });
        builder.deleteCharAt(builder.length()-1);
        String data = builder.toString().toLowerCase();
        return DigestUtils.md5Hex(data);
    }

    /**
     * 验证签名是否在有效期内
     * @param request
     */
    public static void validateTimeInterval(HttpServletRequest request){
        String ts = request.getParameter("ts");
        Assert.hasLength(ts, "Invalid timeStamp.");
        long nowTime = new Date().getTime();
        if(nowTime - Long.valueOf(ts) > TIME_INTERVAL || nowTime - Long.valueOf(ts) < 0){
            throw new IllegalStateException("Signature expired.");
        }
    }

    /**
     * 接口鉴权验证
     * @param request
     */
    public static void validateSign(HttpServletRequest request){
//         validateTimeInterval(request);
        String sign = signMD5(request);
        Assert.notNull(sign, "Generate signature error.");
        if(!sign.equals(request.getParameter("sign"))){
            throw new RuntimeException("Invalid signature.");
        }
    }

}
