package com.efreight.common.core.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class FieldValUtils {


    public String getFieldValueByFieldName(String fieldName, Object object) {
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

    public void setFieldValueByFieldName(String fieldName, Object value, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("字段" + fieldName + "设置失败");
        }
    }
}
