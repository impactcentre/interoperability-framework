package eu.impact_project.iif.t2.client;

import org.jaxen.saxpath.Operator;

public class Wsdl {
	
	private String url;
	private String user;
	private String pass;
	
	public Wsdl(String url) {
		this.url = url;
		this.user = null;
		this.pass = null;
	}

	public Wsdl(String url, String user, String pass) {
		this.url = url;
		this.user = user;
		this.pass = pass;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public void setUser(String user)
	{
		this.user = user;
	}
	
	public void setPass(String pass)
	{
		this.pass = pass;
	}
	
	public String getUrl()
	{
		return this.url;
	}
	
	public String getUser()
	{
		return this.user;
	}
	
	public String getPass()
	{
		return this.pass;
	}
	
}
