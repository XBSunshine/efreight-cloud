package com.efreight.afbase.entity.shipping;

//配置类型
public class APIType {

    //AE 舱单 - 主单 (跳转)
    public static  final String AE_CD_POST_MAWB = "AE_CD_POST_MAWB";
    //AE 制单 - 主单 (跳转)
    public static  final String AE_DZ_POST_MAWB = "AE_DZ_POST_MAWB";
    //AE 舱单 (直传)
    public static  final String AE_CD_AWB = "AE_CD_AWB";
    //AI 舱单 (直传)
    public static  final  String AI_CD_AWB ="AI_CD_AWB";
    //AE 制单 - 主单 (直传)
    public static  final String AE_DZ_MAWB = "AE_DZ_MAWB";
    //标签制作
    public static  final String BQ_POST_MAWB = "BQ_POST_MAWB";
    //AE 预录入-ESD
    public static  final String AE_ESD_POST_MAWB = "AE_ESD_POST_MAWB";
    //AE 进口舱单 - 分单
    public static   final String AE_CD_IMP_HAWB = "AE_CD_IMP_HAWB";
    //AE 鉴定证书 - 南京
    public static   final String AE_IDF_POST_MAWB = "AE_IDF_POST_MAWB";

    public static  final String AE_DZ_AWB = "AE_DZ_AWB";
    //一站式2.0接口
    public static   final String ALL_WORK ="ALL_WORK";

   public static String getAPIType(String type){
        switch (type){
            case AE_CD_AWB :
                type= "出口舱单直传";
                break;
            case AE_IDF_POST_MAWB :
                type= "鉴定证书";
                break;
            case AE_DZ_MAWB :
                type= "电子运单直传";
                break;
            case AE_ESD_POST_MAWB:
                type="预录入";
                break;
            case AE_CD_IMP_HAWB:
                type= "南京货站接口";
                break;
            case ALL_WORK :
                type= "一站式2.0直传";
                break;
            default:
                type= "该功能";
        }
        return type;
    }
}
