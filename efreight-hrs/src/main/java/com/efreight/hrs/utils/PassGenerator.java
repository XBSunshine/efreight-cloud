
package com.efreight.hrs.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zhanghw
 */
@Data
public class PassGenerator {
	public static String getPassword(int length) {  

        String val = "";  
        Random random = new Random();        
        int temp=65;
        //length为几位密码 
        for(int i = 0; i < length; i++) {
            String charOrNum = i % 2 == 0 ? "char" : "num";  
            //输出字母还是数字  
            if( "char".equalsIgnoreCase(charOrNum) ) {  
                //输出是大写字母还是小写字母  
                temp = temp == 97 ? 65 : 97;  
                val += (char)(random.nextInt(26) + temp);  
            } else if( "num".equalsIgnoreCase(charOrNum) ) {  
                val += String.valueOf(random.nextInt(10));  
            }  
        }  
        System.out.println("Generat Password: "+val);
        return val;  
    }
}
