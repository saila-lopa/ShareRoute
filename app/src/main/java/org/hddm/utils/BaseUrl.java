package org.hddm.utils;

public class BaseUrl {
//	public static String BaseHTTP =  "http://www.imstarvin.ie/";

//	public static String BaseHTTP =  "http://shareroute.socialcubebd.com:8080";
	public static String BaseHTTP =  "http://formsworkflow5535.cloudapp.net/shareroute/public/";
//	public static String BaseHTTP =  "http://188.165.247.23/";
//	public static String BaseHTTP =  "http://192.168.0.111/";
//	public static String BaseHTTP =  "http://admin.foodnetbd.com/";
//	public static String BaseHTTP =  "http://192.168.0.130/";
//	public static String BaseHTTP =  "http://sfd.socialcubebd.com:8080/";
	//public static String ImageBaseUrl = "http://180.92.239.220:8080/img/logo/";
	//public static String BaseHTTP = "http://149.202.75.148/";
	public static String HTTP =  BaseHTTP + "service/shareroute/";
	//production service urls
	//public static String HTTP =  "http://92.222.190.101/service/delivery/";
	//public static String ImageBaseUrl = "http://92.222.190.101/img/logo/";
	/**
	 * @return the hTTP
	 */
	public static String makeHTTPURL(String param)
	{
		return HTTP + UrlUtils.encode(param);
	}

	/**
	 * @param hTTP
	 *            the hTTP to set
	 */
	public static void setHTTP(final String hTTP) {
		HTTP = hTTP;
	}
}
