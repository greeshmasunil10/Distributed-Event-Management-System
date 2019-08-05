package Shared;

import java.rmi.RemoteException;

import Servers.DEMSMontrealServer;
import Servers.DEMSOttawaServer;
import Servers.DEMSTorontoServer;

public class DEMSStartServers {
	public static boolean startServers(){
		try {
			DEMSTorontoServer.startServer();
			DEMSMontrealServer.startServer();
			DEMSOttawaServer.startServer();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static void main(String args[]) {
		DEMSStartServers obj= new DEMSStartServers();
		obj.startServers();
	}
}