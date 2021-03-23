package com.efreight.common.core.utils;

import com.efreight.common.security.util.SecurityUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@UtilityClass
public class FormatUtils {
    public String formatWithQWF(BigDecimal number, Integer num) {
        if (number == null) {
            return null;
        }
        String numberStr = "";
        if (num == null || num < 1) {
            numberStr = new DecimalFormat("###,###.0").format(number.setScale(1, BigDecimal.ROUND_HALF_UP));
        } else {
            StringBuffer buffer = new StringBuffer("###,###.");
            for (int i = 0; i < num; i++) {
                buffer.append("0");
            }
            numberStr = new DecimalFormat(buffer.toString()).format(number.setScale(num, BigDecimal.ROUND_HALF_UP));
        }

        if (numberStr.split("\\.")[0].equals("")) {
            numberStr = "0." + numberStr.split("\\.")[1];
        } else if (numberStr.split("\\.")[0].equals("-")) {
            numberStr = "-0." + numberStr.split("\\.")[1];
        }
        return numberStr;
    }

    public String formatWithQWFNoBit(BigDecimal number) {
        if (number == null) {
            return null;
        }
        String numberStr = new DecimalFormat("###,###.###").format(number);
        if (numberStr.split("\\.")[0].equals("")) {
            numberStr = "0." + numberStr.split("\\.")[1];
        } else if (numberStr.split("\\.")[0].equals("-")) {
            numberStr = "-0." + numberStr.split("\\.")[1];
        }
        return numberStr;
    }

    /**
     * 格式创建人，
     *
     * @param creator 创建人，其格式为: 姓名+空格+邮箱(jone jone@163.com)
     * @return 创建人姓名
     */
    public static String formatCreator(String creator) {
        if (StringUtils.isNotBlank(creator)) {
            String[] arr = creator.split(" ");
            if (arr.length > 1) {
                creator = arr[0];
            }
        }
        return creator;
    }


    public void initDefaultInfo(Object object, boolean ifContainUpdate) {
        Class clazz = object.getClass();
        try {
            Method setCreatorId = clazz.getMethod("setCreatorId", Integer.class);
            Method setCreatorName = clazz.getMethod("setCreatorName", String.class);
            Method setCreateTime = clazz.getMethod("setCreateTime", LocalDateTime.class);
            setCreatorId.invoke(object, SecurityUtils.getUser().getId());
            setCreatorName.invoke(object, SecurityUtils.getUser().buildOptName());
            setCreateTime.invoke(object, LocalDateTime.now());
            if (ifContainUpdate) {
                Method setEditorId = clazz.getMethod("setEditorId", Integer.class);
                Method setEditorName = clazz.getMethod("setEditorName", String.class);
                Method setEditTime = clazz.getMethod("setEditTime", LocalDateTime.class);
                setEditorId.invoke(object, SecurityUtils.getUser().getId());
                setEditorName.invoke(object, SecurityUtils.getUser().buildOptName());
                setEditTime.invoke(object, LocalDateTime.now());
            }
            Method setOrgId = clazz.getMethod("setOrgId", Integer.class);
            setOrgId.invoke(object, SecurityUtils.getUser().getOrgId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
