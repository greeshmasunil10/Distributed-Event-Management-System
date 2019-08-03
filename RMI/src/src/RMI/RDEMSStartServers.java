package RMI;

import java.rmi.RemoteException;

import Servers.RDEMSMontrealServer;
import Servers.RDEMSOttawaServer;
import Servers.RDEMSTorontoServer;

public class RDEMSStartServers {

	public static void main(String args[]) {
		try {
			RDEMSTorontoServer.startServer();
			RDEMSMontrealServer.startServer();
			RDEMSOttawaServer.startServer();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}