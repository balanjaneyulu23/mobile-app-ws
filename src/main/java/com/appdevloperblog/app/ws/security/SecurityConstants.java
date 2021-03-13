package com.appdevloperblog.app.ws.security;

import com.appdevloperblog.app.ws.SpringApplicationContext;

public final class SecurityConstants {
	public static final long EXPIRATION_TIME = 864000000; //10 days in mill seconds
	public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000; //1 hour in mill seconds
	public static final String TOKEN_PREFIX = "Bearer";
	public static final String HEADER_STRING = "Authorization";
	public static final String SIGN_UP_URL = "/users";
	public static final String VERIFICATION_EMAIL_URL = "/users/email-verification";
	public static final String PASSWORD_RESET_REQUEST_URL = "/users/password-reset-request";
	public static final String PASSWORD_RESET_URL = "/users/password-reset";
	public static final String H2_CONSOLE = "/h2-console/**";
	

	public static String getTokenSecret() {
		AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("appProperties");
		return appProperties.getToken();
	}
	
	public static String getSignupUrl() {
		AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("appProperties");
		return appProperties.getSignupUrl();
	}
	
	private SecurityConstants(){};



}
