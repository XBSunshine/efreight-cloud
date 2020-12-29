
package com.efreight.common.security.constant;

/**
 * 
 * @author zhanghw
 *
 */
public interface CommonConstants {
	/**
	 * 删除
	 */
	String STATUS_DEL = "1";
	/**
	 * 正常
	 */
	String STATUS_NORMAL = "0";

	/**
	 * 锁定
	 */
	String STATUS_LOCK = "9";

	/**
	 * 菜单
	 */
	String MENU = "0";

	/**
	 * 编码
	 */
	String UTF8 = "UTF-8";

	/**
	 * JSON 资源
	 */
	String CONTENT_TYPE = "application/json; charset=utf-8";

	/**
	 * 前端工程名
	 */
	String FRONT_END_PROJECT = "efreight-ui";

	/**
	 * 后端工程名
	 */
	String BACK_END_PROJECT = "efreight";

	/**
	 * 成功标记
	 */
	Integer SUCCESS = 0;
	/**
	 * 失败标记
	 */
	Integer FAIL = 1;

	/**
	 * 验证码前缀
	 */
	String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY_";

	/**
	 * 业务域名
	 */
	interface BUSINESS_SCOPE {
		/**
		 * 空运出口
		 */
		String AE = "AE";
		/**
		 * 空运进口
		 */
		String AI = "AI";
		/**
		 * 海运出口
		 */
		String SE = "SE";
		/**
		 * 海运进口
		 */
		String SI = "SI";
	}

	/**
	 * 舱单进出口标识
	 */
	enum TRACK_MANIFEST_FLAG{
		IMPORT("I", "进口"),
		EXPORT("E", "出口");

		private String value;
		private String name;

		TRACK_MANIFEST_FLAG(String value, String name){
			this.value = value;
			this.name = name;
		}

		public String value(){
			return this.value;
		}
	}

	/**
	 * 企业附加服务类型
	 */
	interface ORG_ADDITIONAL_SERVICE_TYPE {
		/**
		 * 空运进出品服务
		 */
		String TRACK_AE_AI = "TRACK_AE_AI";
	}
}
