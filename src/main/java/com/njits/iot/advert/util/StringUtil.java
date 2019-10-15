package com.njits.iot.advert.util;

/**
 * 字符串工具类
 * @author xue
 */
public class StringUtil {

	public StringUtil() {
		// Do nothing
	}

	/**
	 * 是否是空值
	 */
	public static boolean isEmpty(String str) {
		return null == str || "".equals(str.trim());
	}

	/**
	 * 是否是非空值
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 对象转成字符串 null或者"null"都转成 ""
	 */
	public static String toString(Object obj) {
		String str = "";
		if (obj == null) {
			return "";
		} else {
			str = obj.toString();
			if (("").equals(str) || "null".equals(str)) {
				return "";
			} else {
				return str;
			}
		}
	}

	/**
	 * 判断对象是否为空
	 */
	public static boolean isNullEmpty(Object o) {
		return  (o == null || "".equals(o.toString().trim()) || "null".equalsIgnoreCase(o.toString().trim()));
	}
	
	/**
	 * 检查字符串是否是空白
	 */
	public static boolean isBlank(String str) {
		int length;

		if ((str == null) || ((length = str.length()) == 0)) {
			return true;
		}

		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 拼接字符串
	 */
	public static String connectString(String... values){
	    StringBuilder sb = new StringBuilder();
		for(String value : values){
			sb.append(value);
		}
		return sb.toString();
	}
	
	/**
	 * 根据起始位置复制字符串
	 * @param str
	 * @param startPosition
	 * @param endPosition
	 * @return
	 */
	public static String copyString(String str, int startPosition, int endPosition){
		return str.subSequence(startPosition, endPosition).toString();
	}
}
