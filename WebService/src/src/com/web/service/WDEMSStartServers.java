package com.web.service;

import com.servers.WDEMSMontrealServer;
import com.servers.WDEMSOttawaServer;
import com.servers.WDEMSTorontoServer;

public class WDEMSStartServers {

	public static void main(String args[]) {
		WDEMSTorontoServer.startServer();
		WDEMSMontrealServer.startServer();
		WDEMSOttawaServer.startServer();
	}
}