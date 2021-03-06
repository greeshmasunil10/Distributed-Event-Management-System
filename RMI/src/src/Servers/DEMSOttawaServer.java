package Servers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

import javax.xml.ws.Endpoint;

import Model.Ports;
import Model.Response;
import Servers.DEMSMontrealServer;
import Servers.DEMSTorontoServer;
import Shared.DEMSInterfaceImpl;

public class DEMSOttawaServer {
	private static HashMap<String, HashMap<String, Integer>> otwDb = new HashMap<>();
	static HashMap<String, ArrayList<String>> otwCustomerInfo = new HashMap<>();
	static String[] eventTypes = { "Conferences", "Seminars", "Trade Shows" };
	static PrintWriter writer;
	public static DEMSInterfaceImpl ImpObj;

	public static void main(String args[]) {

	}

	public static HashMap<String, HashMap<String, Integer>> returnDb() {
		return otwDb;
	}

	public static void startServer() throws RemoteException {

//		System.out.println("Starting Ottawa Server...");
		ImpObj = new DEMSInterfaceImpl();

//		Seminars, Conferences and Trade Shows keys are populated with dummy event data
		HashMap<String, Integer> dummyValsConf = new HashMap<>();
//		dummyValsConf.put("OTWM100519", 5);
//		dummyValsConf.put("OTWA100519", 10);
//		dummyValsConf.put("OTWE100519", 15);
		otwDb.put(eventTypes[0], dummyValsConf);

		HashMap<String, Integer> dummyValsSem = new HashMap<>();
		dummyValsSem.put("OTWM112211", 5);
		dummyValsSem.put("OTWM222222", 10);
		dummyValsSem.put("OTWM332233", 15);
		dummyValsSem.put("OTWM442244", 15);
		otwDb.put(eventTypes[1], dummyValsSem);

		HashMap<String, Integer> dummyValsTS = new HashMap<>();
//		dummyValsTS.put("OTWM120519", 5);
//		dummyValsTS.put("OTWA120519", 10);
//		dummyValsTS.put("OTWE120519", 15);
		otwDb.put(eventTypes[2], dummyValsTS);

//		displayotwDbContents();

		String portNum, registryURL;
		try {
//			System.out.println("Enter the RMIregistry port number:");
//			portNum = (br.readLine()).trim();
//			int RMIPortNum = Integer.parseInt(portNum);
			int RMIPortNum = Ports.OTTAWA_SERVER_PORT;
			startRegistry(RMIPortNum);
			DEMSInterfaceImpl exportedObj = new DEMSInterfaceImpl();
			registryURL = "rmi://localhost:"+RMIPortNum+"/ottawa";
			Naming.rebind(registryURL, exportedObj);
//			System.out.println("Server registered.  Registry currently contains:");
			listRegistry(registryURL);
			System.out.println("Ottawa Server ready.");
		} catch (Exception re) {
			System.out.println("Exception in Ottawa Server.main: " + re);
		}

		Runnable task2 = () -> {
			torontoListener();
		};
		Runnable task3 = () -> {
			montrealListener();
		};

		Thread thread2 = new Thread(task2);
		Thread thread3 = new Thread(task3);
		thread2.start();
		thread3.start();
	}

	/*
	 * This method starts a RMI registry on the local host, if it does not already
	 * exists at the specified port number.
	 */
	private static void startRegistry(int RMIPortNum) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(RMIPortNum);
			registry.list(); // This call will throw an exception
								// if the registry does not already exist
		} catch (RemoteException e) {
			// No valid registry at that port.
//			System.out.println("RMI registry cannot be located at port " + RMIPortNum);
			Registry registry = LocateRegistry.createRegistry(RMIPortNum);
//			System.out.println(/**/ "RMI registry created at port " + RMIPortNum);
		}
	}

	/*
	 * This method lists the names registered with a Registry object
	 */
	private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
//		System.out.println("Registry " + registryURL + " contains: ");
		String[] names = Naming.list(registryURL);
//		for (int i = 0; i < names.length; i++)
//			System.out.println(names[i]);
	}

	public synchronized static Response addEvent(String eventID, String eventType, int bookingCapacity) {
		HashMap<String, Integer> temp = (HashMap<String, Integer>) otwDb.get(eventType).clone();

		if (temp.containsKey(eventID)) {
			int newCap = temp.get(eventID) + bookingCapacity;
			temp.put(eventID, newCap);
		} else {
			temp.put(eventID, bookingCapacity);
		}
		temp.put(eventID, bookingCapacity);
		otwDb.put(eventType, temp);
		displayotwDbContents();
		displayCustomerInfo();
		return new Response("Event " + eventID + " Added!", true);
	}

	public synchronized static Response removeEvent(String eventID, String eventType) {
//		dummyVals always stored in the DB so no need to check for an empty sub-HashMap for any eventType
		HashMap<String, Integer> temp = (HashMap<String, Integer>) otwDb.get(eventType).clone();

		if (!temp.containsKey(eventID)) {
			return new Response("Event doesn't exist", false);
		}
		for (ArrayList<String> al : otwCustomerInfo.values()) {
			if (al.contains(eventID)) {
				al.remove(eventID);
				if (al.isEmpty())
					otwCustomerInfo.remove(al);
				logOperation("Deleted Event which has been booked!", eventID, eventType, "NA", "NA", "Succeeded");
			}
		}

		temp.remove(eventID);
		otwDb.put(eventType, temp);
		displayotwDbContents();
		displayCustomerInfo();
		return new Response(eventID + " successfully removed", true);
	}

	public synchronized static Response dispEventAvailability(String eventType) {
		String msg = "";
		HashMap<String, Integer> temp = otwDb.getOrDefault(eventType, new HashMap<String, Integer>());
		msg += "OTW|";
		for (String EventID : temp.keySet()) {
			msg += EventID + ":" + temp.get(EventID)+",";
		}
		msg+="||";
		msg += DEMSTorontoServer.dispEventAvailability(eventType).getMessage();
		msg += DEMSMontrealServer.dispEventAvailability(eventType).getMessage();
		return new Response(msg, true);
	}

	public synchronized static Response bookEvent(String customerID, String eventID, String eventType) {

		if (otwCustomerInfo.containsKey(customerID) && otwCustomerInfo.get(customerID) != null
				&& otwCustomerInfo.get(customerID).contains(eventID)) {
			return new Response("This event was already booked!", false);
		}
		String month = eventID.substring(6, 8);
		if (otwCustomerInfo.containsKey(customerID) && !eventID.contains("OTW")) {
			ArrayList<String> al = otwCustomerInfo.get(customerID);
			int count = 0;
			for (String id : al) {

				if (!id.contains("OTW")) {
					String m = id.substring(6, 8);
					if (m.equals(month))
						count++;

				}
			}

			if (count >= 3) {
				return new Response("customer cannot book more than 3 outside city events in the same month", false);
			}

		}
		if (eventID.contains("OTW")) {
			HashMap<String, Integer> temp = (HashMap<String, Integer>) otwDb.get(eventType).clone();
			if (!temp.containsKey(eventID)) {
				return new Response("This event does not exist!", false);
			} else if (temp.get(eventID) == 0)
				return new Response("Event capacity is zero", false);
			temp.put(eventID, temp.get(eventID) - 1);
			otwDb.put(eventType, temp);
			displayotwDbContents();
		} else {
			UDPclient(customerID, eventID, eventType, "book");
		}
		ArrayList<String> temp1 = otwCustomerInfo.get(customerID) == null ? (new ArrayList<>())
				: otwCustomerInfo.get(customerID);
		temp1.add(eventID);
		otwCustomerInfo.put(customerID, temp1);
		displayCustomerInfo();
		return new Response("Event successfully Booked", true);

	}

	public synchronized static Response swapEvent(String customerID, String newEventID, String newEventType,
			String oldEventID, String oldEventType) {
		Response res = new Response("Event could not be swapped!", false);
		HashMap<String, Integer> temp1 = (HashMap<String, Integer>) otwDb.get(oldEventType).clone();
		HashMap<String, Integer> temp2 = (HashMap<String, Integer>) DEMSMontrealServer.returnDb().get(oldEventType)
				.clone();
		HashMap<String, Integer> temp3 = (HashMap<String, Integer>) DEMSTorontoServer.returnDb().get(oldEventType)
				.clone();
		if (temp1.containsKey(oldEventID) || temp2.containsKey(oldEventID) || temp3.containsKey(oldEventID)) {
			if (otwCustomerInfo.containsKey(customerID) && otwCustomerInfo.get(customerID).contains(oldEventID)) {
				Response msg1 = bookEvent(customerID, newEventID, newEventType);
				if (!msg1.getMessage().equals("Event successfully Booked")) {
					if (!DEMSTorontoServer.checkbooklimit(customerID, newEventID)
							&& !DEMSTorontoServer.checkbooklimit(customerID, oldEventID)) {

						Response msg2 = cancelEvent(customerID, oldEventID);
						if (!msg2.getMessage().equals("Event successfully cancelled")) {
							res = new Response("Event could not be swapped: " + msg2.getMessage(), false);
							cancelEvent(customerID, newEventID);
							return res;
						}
						Response msg3 = bookEvent(customerID, newEventID, newEventType);
						if (msg3.getResult())
							return new Response("Event succesfully swapped", true);
						else {
							bookEvent(customerID, oldEventID, oldEventType);
							res = new Response("Event could not be swapped: " + msg3.getMessage(), false);
							return res;

						}
					} else
						return new Response("Event could not be swapped since:" + msg1.getMessage(), false);
				}
				Response msg2 = cancelEvent(customerID, oldEventID);
				if (!msg2.getMessage().equals("Event successfully cancelled")) {
					res = new Response("Event could not be swapped: " + msg2.getMessage(), false);
					cancelEvent(customerID, newEventID);
					return res;
				}

				res = new Response("Event succesfully swapped", true);
				logOperation("Swap Performed", newEventID, newEventType, "NA", "NA", "Succeeded");
				displayotwDbContents();
				displayCustomerInfo();
			} else {
				res = new Response("Event could not be swapped since this event wasnt booked!", false);
			}
		} else {
			res = new Response("Event could not be swapped since this event wasnt booked!", false);
		}

		return res;
	}

	public synchronized static Response cancelEvent(String customerID, String eventID) {

		if (otwCustomerInfo.containsKey(customerID) && otwCustomerInfo.get(customerID) != null
				&& otwCustomerInfo.get(customerID).contains(eventID)) {

			if (eventID.contains("OTW")) {
				if (otwCustomerInfo.containsKey(customerID) && otwCustomerInfo.get(customerID) != null
						&& otwCustomerInfo.get(customerID).contains(eventID)) {
					for (String eType : otwDb.keySet()) {
						for (String eID : otwDb.get(eType).keySet()) {
							if (eID.equalsIgnoreCase(eventID)) {
								System.out.println("entered");
								ArrayList<String> temp1 = otwCustomerInfo.get(customerID);
								temp1.remove(eventID);
								if (temp1.size() == 0)
									otwCustomerInfo.remove(customerID);
								else
									otwCustomerInfo.put(customerID, temp1);
								HashMap<String, Integer> temp = (HashMap<String, Integer>) otwDb.get(eType).clone();
								temp.put(eID, temp.get(eID) + 1);
								otwDb.put(eType, temp);
								displayotwDbContents();
								return new Response("Event successfully cancelled", true);
							}
						}
					}
				}
			} else {

				for (String eType : otwDb.keySet()) {
					for (String eID : otwDb.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							HashMap<String, Integer> temp = (HashMap<String, Integer>) otwDb.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							otwDb.put(eType, temp);
						}
					}
				}
				UDPclient(customerID, eventID, "", "cancel");
				ArrayList<String> temp1 = otwCustomerInfo.get(customerID);
				temp1.remove(eventID);
				displayCustomerInfo();
				return new Response("Event successfully cancelled", true);

			}
			return new Response("This event cannot be cancelled", false);
		}
		return new Response("The event doesn't exist", false);
	}

	public synchronized static Response getBookingSchedule(String customerID) {
		String msg = "";
		if (!otwCustomerInfo.containsKey(customerID)) {
			System.out.println("No events to display!");
			return new Response("No events to display!", false);
		}
		System.out.println(customerID + ":" + otwCustomerInfo.get(customerID).toString());
		msg += "\n" + customerID + ":" + otwCustomerInfo.get(customerID).toString();
		return new Response(msg, true);
	}

	public static void displayotwDbContents() {
		System.out.println("\n------------DATABASE CONT.------------");
		for (String et : eventTypes) {
			System.out.println(et + ": ");
			HashMap<String, Integer> temp = otwDb.getOrDefault(et, new HashMap<String, Integer>());
			System.out.println(temp.keySet().toString());
			System.out.println(temp.values());
		}
		System.out.println("-----------------------------------");
	}

	public static void displayCustomerInfo() {
		System.out.println("\n------------CLIENT INFO------------");
		for (String cID : otwCustomerInfo.keySet()) {
			System.out.print(cID + ": ");
			System.out.println(otwCustomerInfo.get(cID).toString());
		}
		System.out.println("-----------------------------------");
	}

	public static void logOperation(String name, String eventID, String eventType, String customerID, String bookingCap,
			String status) {
		try {
			FileWriter fw = new FileWriter("OttawaLogs.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String log = "\n" + name + "Performed.\nTime: " + timeStamp + "\nCustomerID: " + customerID + "\nEventID: "
				+ eventID + "\nEventType: " + eventType + "\nBooking Capacity: " + bookingCap + "\nStatus: " + status;
		writer.println(log);
		writer.close();
	}

	private static boolean UDPclient(String customerID, String eventID, String eventType, String action) {
		int serverPort;// agreed upon port
		if (eventID.contains("MTL")) {
			System.out.println("\n\nRequesting Montreal Server...");
			serverPort = 1001;
		} else if (eventID.contains("TOR")) {
			System.out.println("\n\nRequesting Toronto Server...");
			serverPort = 3001;
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
//			System.out.println("Request message sent from the client is : " + new String(request.getData()));

			byte[] buffer = new byte[1000];// to store the received data, it will be populated by what receive method
											// returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);// reply packet ready but not populated.

			// Client waits until the reply is
			// received-----------------------------------------------------------------------
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
			aSocket = new DatagramSocket(2001);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
//			System.out.println("Listener Server Started for Toronto...");
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
//					bookEvent(  list[1],   list[2],   list[3]);
					String buff = list[3] + ":" + list[2] + " successfully booked for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
																													// packet
																													// ready
				} else if (list[0].equals("cancel")) {
//					cancelEvent(  list[1],   list[2]);
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

	public static boolean montrealListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(2002);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
//			System.out.println("Listener Server Started for Montreal...");
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
				} else if (list[0].equals("disp")) {
					System.out.println("recieved sdispo");
					dispEventAvailability(list[3]);
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
}
