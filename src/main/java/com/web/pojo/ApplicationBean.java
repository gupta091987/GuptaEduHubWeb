package com.web.pojo;


public class ApplicationBean {
	
	
	private static ApplicationBean applicationBean = new ApplicationBean( );
	
	private String url;
	private String userName;
	private String password;
	public static ApplicationBean getInstance() {
		return applicationBean;
	}
	public static void setInstance(ApplicationBean applicationBean) {
		ApplicationBean.applicationBean = applicationBean;
		
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	}
