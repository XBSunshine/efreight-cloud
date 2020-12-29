package com.efreight.common.core.constant;

public enum Constant {

    //是否被@ResponseResult标记
    RESPONSE_RESULT_ANN("RESPONSE_RESULT_ANN",true);

    private String value;

    private Boolean isExist;

    private Constant(String value, Boolean isExist) {
        this.value = value;
        this.isExist = isExist;
    }
    private Constant(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public Boolean getIsExist() {
        return isExist;
    }

}
