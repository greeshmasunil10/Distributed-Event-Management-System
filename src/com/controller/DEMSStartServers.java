package com.controller;

import com.servers.DEMSMontrealServer;
import com.servers.DEMSOttawaServer;
import com.servers.DEMSTorontoServer;

public class DEMSStartServers {

	public static void main(String args[]) {
		DEMSTorontoServer.startServer();
		DEMSMontrealServer.startServer();
		DEMSOttawaServer.startServer();
	}
}