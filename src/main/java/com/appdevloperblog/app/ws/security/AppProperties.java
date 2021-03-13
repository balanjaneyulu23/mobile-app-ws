package com.appdevloperblog.app.ws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

	@Autowired
	private Environment environment;
	
	public String getToken() {
		return environment.getProperty("tokensecret");
	}
	
	public String getSignupUrl() {
		return environment.getProperty("signupurl");
	}

}
