package com.efreight.common.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
	/**
	 * 获取当前UTC时间
	 * 
	 * @return
	 */
	public static Date createUTCNowDateTime() {
		// 1、取得本地时间：
		final java.util.Calendar cal = java.util.Calendar.getInstance();
		// 2、取得时间偏移量：
		final int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		final int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return cal.getTime();
	}
	
	
	/**
	 * 获取当前UTC时间
	 * 
	 * @return
	 */
	public static Date createNowDateTime() {
		// 1、取得本地时间：
		final java.util.Calendar cal = java.util.Calendar.getInstance();
	
		return cal.getTime();
	}

	public static Date createUTCNowDateTimeByGiveTime(String nowTime) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 1、取得本地时间：
		final java.util.Calendar cal = java.util.Calendar.getInstance();
		if (nowTime != null && !"".equals(nowTime)) {
			cal.setTime(sdf.parse(nowTime));
		}
		// 2、取得时间偏移量：
		final int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		final int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return cal.getTime();
	}

	@SuppressWarnings("unlikely-arg-type")
	public static Date createUTCNowDateTimeByGiveTime(Date nowTime) throws Exception {
		// 1、取得本地时间：
		final java.util.Calendar cal = java.util.Calendar.getInstance();
		if (nowTime != null && !"".equals(nowTime)) {
			cal.setTime(nowTime);
		}
		// 2、取得时间偏移量：
		final int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		final int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return cal.getTime();
	}
}
