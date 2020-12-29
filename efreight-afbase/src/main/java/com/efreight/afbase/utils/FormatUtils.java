package com.efreight.afbase.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@UtilityClass
public class FormatUtils {
    public String formatWith2AndQFW(BigDecimal number) {
        if(number == null){
            return "";
        }else{
            String numberStr = new DecimalFormat("###,###.00").format(number.setScale(2, BigDecimal.ROUND_HALF_UP));
            if (numberStr.split("\\.")[0].equals("")) {
                numberStr = "0." + numberStr.split("\\.")[1];
            } else if (numberStr.split("\\.")[0].equals("-")) {
                numberStr = "-0." + numberStr.split("\\.")[1];
            }
            return numberStr;
        }
    }
}
