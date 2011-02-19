package com.ihub.android.app.net;

import java.net.URI;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import com.ihub.android.app.service.UpdateMembersInfoService;

public class FetchUserData {
	private XMLRPCClient client;
	private URI uri;
	
	public FetchUserData() {
		uri = URI.create(UpdateMembersInfoService.URL_STRING);
		client = new XMLRPCClient(uri);
	}
	
	public Object sendDetailsToServer(String method, Object obj []) throws XMLRPCException {
		Object returnedObject =  client.callEx(method, obj);
		return returnedObject;
	}
	
}
