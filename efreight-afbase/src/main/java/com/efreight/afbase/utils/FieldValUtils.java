package com.efreight.afbase.utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class FieldValUtils {


	public static String getFieldValueByFieldName(String fieldName,Object object) {
	  try {
	  	Field field = object.getClass().getDeclaredField(fieldName);
	  	field.setAccessible(true);
	  	Object value = field.get(object);
	  	if (value == null) {
            return "";
        }else {
        	return value.toString();
        }
        // 判断值的类型后进行强制类型转换
//        if (value instanceof Integer) {
//        	return  (Integer) value;
//        } else if (value instanceof Long) {
//        	return  (Long) value;
//        }  else {
//        	return value;
//        }
	  } catch (Exception e) {
	  	e.printStackTrace();
	  	return null;
	  }
	}

}
