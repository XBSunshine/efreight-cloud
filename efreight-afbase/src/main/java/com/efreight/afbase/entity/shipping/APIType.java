package com.efreight.afbase.entity.shipping;

//配置类型
public interface APIType {
    //AE 舱单 - 主单 (跳转)
    String AE_CD_POST_MAWB = "AE_CD_POST_MAWB";
    //AE 制单 - 主单 (跳转)
    String AE_DZ_POST_MAWB = "AE_DZ_POST_MAWB";
    //AE 舱单 (直传)
    String AE_CD_AWB = "AE_CD_AWB";
    //AI 舱单 (直传)
    String AI_CD_AWB ="AI_CD_AWB";
    //AE 制单 - 主单 (直传)
    String AE_DZ_MAWB = "AE_DZ_MAWB";

    //标签制作
    String BQ_POST_MAWB = "BQ_POST_MAWB";
    //AE 预录入-ESD
    String AE_ESD_POST_MAWB = "AE_ESD_POST_MAWB";
    //AE 进口舱单 - 分单
    String AE_CD_IMP_HAWB = "AE_CD_IMP_HAWB";
    //AE 鉴定证书 - 南京
    String AE_IDF_POST_MAWB = "AE_IDF_POST_MAWB";

    String AE_DZ_AWB = "AE_DZ_AWB";
    //一站式2.0接口
    String ALL_WORK ="ALL_WORK";
}
