import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class DEMSMontrealServer {
	private static HashMap<String, HashMap<String, Integer>> mtlDb = new HashMap<>();
	private static HashMap<String, ArrayList<String>> mtlCustomerInfo = new HashMap<>();
	static String[] eventTypes = { "Conferences", "Seminars", "Trade Shows" };
	static PrintWriter writer;

	public static void main(String args[]) {

		HashMap<String, Integer> dummyValsConf = new HashMap<>();
		dummyValsConf.put("MTLM100519", 5);
		dummyValsConf.put("MTLA100519", 10);
		dummyValsConf.put("MTLE100519", 15);
		mtlDb.put(eventTypes[0], dummyValsConf);

		HashMap<String, Integer> dummyValsSem = new HashMap<>();
		dummyValsSem.put("MTLM110519", 5);
		dummyValsSem.put("MTLA110519", 10);
		dummyValsSem.put("MTLE110519", 15);
		mtlDb.put(eventTypes[1], dummyValsSem);

		HashMap<String, Integer> dummyValsTS = new HashMap<>();
		dummyValsTS.put("MTLM120519", 5);
		dummyValsTS.put("MTLA120519", 10);
		dummyValsTS.put("MTLE120519", 15);
		mtlDb.put(eventTypes[2], dummyValsTS);

		displaymtlDbContents();

		String registryURL;
		try {
			int RMIPortNum, portNum;
			portNum = RMIPortNum = 1000;
			startRegistry(RMIPortNum);
			DEMSInterfaceImpl exportedObj = new DEMSInterfaceImpl();
			registryURL = "rmi://localhost:" + portNum + "/montreal";
			Naming.rebind(registryURL, exportedObj);
			System.out.println
			("Server registered.  Registry currently contains:");
			listRegistry(registryURL);
			System.out.println("Montreal Server ready.");
		}
		catch (Exception re) {
			System.out.println("Exception in MontrealServer.main: " + re);
		}
		
		Runnable task2 = () -> {
			torontoListener();
		};
		Runnable task3 = () -> {
			ottawaListener();
		};

		Thread thread2 = new Thread(task2);
		Thread thread3 = new Thread(task3);	
		thread2.start();
		thread3.start();

	}
	
	// This method starts a RMI registry on the local host, if it
	// does not already exists at the specified port number.
	private static void startRegistry(int RMIPortNum) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(RMIPortNum);
			registry.list(); 
		} catch (RemoteException e) {
			System.out.println
			("RMI registry cannot be located at port "/**/ + RMIPortNum);
			Registry registry = LocateRegistry.createRegistry(RMIPortNum);
			System.out.println(/**/ "RMI registry created at port " + RMIPortNum);
		}
	}

	// This method lists the names registered with a Registry object
	private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
		System.out.println("Registry " + registryURL + " contains: ");
		String[] names = Naming.list(registryURL);
		for (int i = 0; i < names.length; i++)
			System.out.println(names[i]);
	}

	public synchronized static String addEvent(String eventID, String eventType, int bookingCapacity) {
		HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eventType).clone();

		if (temp.containsKey(eventID)) {
			int newCap = temp.get(eventID)+ bookingCapacity;
			temp.put(eventID, newCap);
		}else {
			temp.put(eventID, bookingCapacity);
		}
		temp.put(eventID, bookingCapacity);
		mtlDb.put(eventType, temp);
		displaymtlDbContents();
		displayCustomerInfo();
		return "Added!";
	}

	public synchronized static String removeEvent(String eventID, String eventType) {
//		dummyVals always smtled in the DB so no need to check for an empty sub-HashMap for any eventType

		HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eventType).clone();

		if (!temp.containsKey(eventID)) {
			return "Event doesn't exist";
		}
		if(mtlCustomerInfo.values().contains(eventID)) {
			logOperation("Deleted Event which has been booked!", eventID, eventType,"NA","NA","Succeeded");
		}
		temp.remove(eventID);
		mtlDb.put(eventType, temp);
		displaymtlDbContents();
		displayCustomerInfo();
		return "Removed Event";
	}

	public synchronized static void getBookingSchedule(String customerID) {
		if (!mtlCustomerInfo.containsKey(customerID)) {
			System.out.println("No events to display!");
			return;
		}
//		TODO: Get events from other servers for this customerID.
		System.out.println(customerID + ":" + mtlCustomerInfo.get(customerID).toString());
	}
	
	public synchronized static String bookEvent(String customerID, String eventID, String eventType) {
		if (mtlCustomerInfo.containsKey(customerID) && mtlCustomerInfo.get(customerID) != null
				&& mtlCustomerInfo.get(customerID).contains(eventID)) {
			return "This event was already booked!";
		}
		if (eventID.contains("MTL")) {
			HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eventType).clone();
			if (!temp.containsKey(eventID) || temp.get(eventID) == 0) {
				return "This event does not exist!";
			}
			temp.put(eventID, temp.get(eventID) - 1);
			mtlDb.put(eventType, temp);
			displayCustomerInfo();
		} else {
			UDPclient(customerID, eventID, eventType, "book");
		}
		ArrayList<String> temp1 = mtlCustomerInfo.get(customerID) == null ? (new ArrayList<>())
				: mtlCustomerInfo.get(customerID);
		temp1.add(eventID);
		mtlCustomerInfo.put(customerID, temp1);
		displaymtlDbContents();
		displayCustomerInfo();
		return "Booked!";
	}

	public synchronized static String cancelEvent(String customerID, String eventID) {
		if (mtlCustomerInfo.containsKey(customerID) && mtlCustomerInfo.get(customerID) != null
				&& mtlCustomerInfo.get(customerID).contains(eventID)) {
			
		if (eventID.contains("MTL")) {
			if (mtlCustomerInfo.containsKey(customerID) && mtlCustomerInfo.get(customerID) != null
					&& mtlCustomerInfo.get(customerID).contains(eventID)) {
				for (String eType : mtlDb.keySet()) {
					for (String eID : mtlDb.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							System.out.println("entered");
							ArrayList<String> temp1 = mtlCustomerInfo.get(customerID);
							temp1.remove(eventID);
							if (temp1.size() == 0)
								mtlCustomerInfo.remove(customerID);
							else
								mtlCustomerInfo.put(customerID, temp1);
							HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							mtlDb.put(eType, temp);
							displaymtlDbContents();
							displayCustomerInfo();
							return "Cancelled";
						}
					}
				}
			}
		} else {
			for (String eType : mtlDb.keySet()) {
				for (String eID : mtlDb.get(eType).keySet()) {
					if (eID.equalsIgnoreCase(eventID)) {
						HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eType).clone();
						temp.put(eID, temp.get(eID) + 1);
						mtlDb.put(eType, temp);
					}
				}
			}
			UDPclient(customerID, eventID, "","cancel");
			ArrayList<String> temp1 = mtlCustomerInfo.get(customerID);
			temp1.remove(eventID);
			displaymtlDbContents();
			return "Cancelled";
		}
		return "This event cannot be cancelled";
		}
		return "Event doesn't exist";
	}
	
	public synchronized static void dispEventAvailability(String eventType) {
		System.out.println("\n------------EVENTS------------");
				System.out.println(eventType + ": ");
				HashMap<String, Integer> temp = mtlDb.getOrDefault(eventType, new HashMap<String, Integer>());
				System.out.println(temp.keySet().toString());
				System.out.println(temp.values());
		System.out.println("-----------------------------------");
	}
	

	public static void displaymtlDbContents() {
		System.out.println("\n------------DATABASE CONT.------------");
		for (String et : eventTypes) {
			System.out.println(et + ": ");
			HashMap<String, Integer> temp = mtlDb.getOrDefault(et, new HashMap<String, Integer>());
			System.out.println(temp.keySet().toString());
			System.out.println(temp.values());
		}
		System.out.println("-----------------------------------");
	}

	public static void displayCustomerInfo() {
		System.out.println("\n------------CLIENT INFO------------");
		for (String cID : mtlCustomerInfo.keySet()) {
			System.out.print(cID + ": ");
			System.out.println(mtlCustomerInfo.get(cID).toString());
		}
		System.out.println("-----------------------------------");
	}
	
	public static void logOperation(String name, String eventID, String eventType,String customerID, String bookingCap, String status) {
		
		try {
			FileWriter fw = new FileWriter("MontrealLogs.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String log = "\n"+name + "Performed.\nTime: " + timeStamp 
				+ "\nCustomerID: " + customerID+ "\nEventID: " 
				+ eventID +  "\nEventType: " + eventType +  "\nBooking Capacity: " + bookingCap
				+"\nStatus: "+status ;
		writer.println(log);
		writer.close();
	}

	private static boolean UDPclient(String customerID, String eventID, String eventType, String action) {
		int serverPort;
		if (eventID.contains("TOR")) {
			System.out.println("\n\nRequesting Toronto Server...");
			serverPort = 3001;
		} else if (eventID.contains("OTW")) {
			System.out.println("\n\nRequesting Ottawa Server...");
			serverPort = 2002;
		} else {
			return false;
		}
		String send = action + "," + customerID + "," + eventID + "," + eventType + ",";
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(); // reference of the original socket
			byte[] message = send.getBytes(); // message to be passed is stored in byte array

			InetAddress aHost = InetAddress.getByName("localhost"); // Host name is specified and the IP address of
																	// server host is calculated using DNS.

			DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);// request packet
																									// ready
			aSocket.send(request);// request sent out

			byte[] buffer = new byte[1000];// to store the received data, it will be populated by what receive method
											// returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);// reply packet ready but not populated.

			// Client waits until the reply is received
			aSocket.receive(reply);// reply received and will populate reply packet now.
			System.out.println("Reply received from the server is: " + new String(reply.getData()));// print reply
																									// message after
																									// converting it to
																									// a string from
																									// bytes
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();// now all resources used by the socket are returned to the OS, so that there is
								// no
								// resource leakage, therefore, close the socket after it's use is completed to
								// release resources.
		}
		return false;
	}

	public static boolean torontoListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(1001);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
			System.out.println("Listener Server Started for Toronto...");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				// Server waits for the request to come
				aSocket.receive(request);// request received

				System.out.println("Request received from client: " + new String(request.getData()));
				String message = new String(request.getData());

				String[] list = message.split(",");
				DatagramPacket reply = null;
				if (list[0].equals("book")) {
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					bookEvent(list[1], list[2], list[3]);
					String buff = list[3] + ":" + list[2] + " successfully booked for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				} else if (list[0].equals("cancel")) {
					cancelEvent(list[1], list[2]);
					String buff = list[3] + ":" + list[2] + " successfully cancelled for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				}else if (list[0].equals("disp")) {
					System.out.println("recieved sdispo");
					dispEventAvailability( list[3]);
					String buff = list[3] + ":" + list[2] + "Availability displayed for " + list[3];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				}
				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();

		}
		return false;
	}

	public static boolean ottawaListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(1002);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
			System.out.println("Listener Server Started for Ottawa...");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				// Server waits for the request to come
				aSocket.receive(request);// request received

				System.out.println("Request received from client: " + new String(request.getData()));
				String message = new String(request.getData());

				String[] list = message.split(",");
				DatagramPacket reply = null;
				if (list[0].equals("book")) {
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					bookEvent(list[1], list[2], list[3]);
					String buff = list[3] + ":" + list[2] + " successfully booked for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
																													// packet
																													// ready
				} else if (list[0].equals("cancel")) {
					cancelEvent(list[1], list[2]);
					String buff = list[3] + ":" + list[2] + " successfully cancelled for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
																													// packet
																													// ready

				}
				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();

		}
		return false;
	}

	



} // end class
