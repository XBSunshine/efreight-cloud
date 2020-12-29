package com.efreight.sc.utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class FieldValUtils {


    public static String getFieldValueByFieldName(String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) {
                return "";
            } else {
                return value.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFieldValueByFieldName(String fieldName, Object value, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("字段" + fieldName + "设置失败");
        }
    }

}
