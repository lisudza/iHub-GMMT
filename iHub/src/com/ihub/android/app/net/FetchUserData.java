package com.ihub.android.app.net;

import java.net.URI;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

public class FetchUserData {
	private XMLRPCClient client;
	private URI uri;
	
	public FetchUserData() {
		uri = URI.create("http://codediva.co.ke/ihub/");
		client = new XMLRPCClient(uri);
	}
	
	public Object sendDetailsToServer(String method, Object obj []) throws XMLRPCException {
		Object returnedObject =  client.callEx(method, obj);
		return returnedObject;
	}
	
}
