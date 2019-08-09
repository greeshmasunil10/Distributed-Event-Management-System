package Servers;

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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import Model.Messages;
import Model.Ports;
import Model.Response;
import Model.Messages.add;
import Model.Messages.remove;
import Servers.DEMSMontrealServer;
import Servers.DEMSOttawaServer;
import Shared.DEMSInterfaceImpl;

public class DEMSTorontoServer {

	public static HashMap<String, HashMap<String, Integer>> torDb = new HashMap<>();
	public static HashMap<String, ArrayList<String>> torCustomerInfo = new HashMap<>();
	public static ArrayList<String> torManagers = new ArrayList<>();
	static String[] eventTypes = { "Conferences", "Seminars", "Trade Shows" };
	static PrintWriter writer;
	static String sample = "abc";
	public static DEMSInterfaceImpl ImpObj;

	public static void startServer() throws RemoteException {

//		System.out.println("Starting Toronto Server....");
		ImpObj = new DEMSInterfaceImpl();

//		Seminars, Conferences and Trade Shows keys are populated with dummy event data
		torManagers.addAll(Arrays.asList("TORM1221", "TORM1000", "TORM2222"));

		HashMap<String, Integer> dummyValsConf = new HashMap<>();
//		dummyValsConf.put("TORM100519", 5);
//		dummyValsConf.put("TORA100519", 10);
//		dummyValsConf.put("TORE100519", 15);
		torDb.put(eventTypes[0], dummyValsConf);

		HashMap<String, Integer> dummyValsSem = new HashMap<>();
		dummyValsSem.put("TORM112211", 10);
		dummyValsSem.put("TORM222222", 20);
		dummyValsSem.put("TORM332233", 30);
		dummyValsSem.put("TORM442244", 40);
		torDb.put(eventTypes[1], dummyValsSem);

		HashMap<String, Integer> dummyValsTS = new HashMap<>();
//		dummyValsTS.put("TORM120519", 5);
//		dummyValsTS.put("TORA120519", 10);
//		dummyValsTS.put("TORE120519", 15);
		torDb.put(eventTypes[2], dummyValsTS);
//		sample = "greeshma";
//		System.out.println("added:" + sample);

//		displayTorDbContents();
//		System.out.println("Toronto Server is ready.");

		String registryURL;
		try {
			int RMIPortNum = Ports.TORONTO_SERVER_PORT;
			startRegistry(RMIPortNum);
			DEMSInterfaceImpl exportedObj = new DEMSInterfaceImpl();
			registryURL = "rmi://localhost:"+RMIPortNum+"/toronto";
			Naming.rebind(registryURL, exportedObj);
//			System.out.println("Server registered.  Registry currently contains:");
			listRegistry(registryURL);
			System.out.println("Toronto Server is ready.");
		} catch (Exception re) {
			System.out.println("Exception in TorontoServer.main: " + re);
		}

		Runnable task2 = () -> {
			montrealListener();
		};
		Runnable task3 = () -> {
			ottawaListener();
		};

		Thread thread2 = new Thread(task2);
		Thread thread3 = new Thread(task3);
		thread2.start();
		thread3.start();
	}

	private static void startRegistry(int RMIPortNum) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(RMIPortNum);
			registry.list();
		} catch (RemoteException e) {

//			System.out.println("RMI registry scannot be located at port " + RMIPortNum);
			Registry registry = LocateRegistry.createRegistry(RMIPortNum);
//			System.out.println("RMI registry created at port " + RMIPortNum);
		}
	}

	public static HashMap<String, HashMap<String, Integer>> returnDb() {
		return torDb;
	}

	private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
//		System.out.println("Registry " + registryURL + " contains: ");
		String[] names = Naming.list(registryURL);
//		for (int i = 0; i < names.length; i++)
//			System.out.println(names[i]);
	}

	public synchronized static Response swapEvent(String customerID, String newEventID, String newEventType,
			String oldEventID, String oldEventType) {
		Response res = new Response("Event could not be swapped!", false);
		HashMap<String, Integer> temp1 = (HashMap<String, Integer>) torDb.get(oldEventType).clone();
		HashMap<String, Integer> temp2 = (HashMap<String, Integer>) DEMSMontrealServer.returnDb().get(oldEventType)
				.clone();
		HashMap<String, Integer> temp3 = (HashMap<String, Integer>) DEMSOttawaServer.returnDb().get(oldEventType)
				.clone();
		if (temp1.containsKey(oldEventID) || temp2.containsKey(oldEventID) || temp3.containsKey(oldEventID)) {
			if (torCustomerInfo.containsKey(customerID) && torCustomerInfo.get(customerID).contains(oldEventID)) {
				Response msg1 = bookEvent(customerID, newEventID, newEventType);
				if (!msg1.getMessage().equals("Event successfully Booked")) {
//					res="Event cannot be swapped since customer cannot book more than 3 outside city events in the same month";
					if (!checkbooklimit(customerID, newEventID) && !checkbooklimit(customerID, oldEventID))
					{

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
						
					}
					else
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
				displayTorDbContents();
				displayCustomerInfo();
			} else {
				res = new Response("Event could not be swapped since this event wasnt booked!", false);
			}
		} else {
			res = new Response("Event could not be swapped since this event wasnt booked!", false);
		}

		return res;
	}
	public synchronized static boolean checkbooklimit(String customerID, String eventID) {
		if (customerID.contains("TOR")) {
			String month = eventID.substring(6, 8);
			ArrayList<String> al = DEMSTorontoServer.torCustomerInfo.get(customerID);
			int count = 0;
			for (String id : al) {

				if (!id.contains("TOR")) {
					String m = id.substring(6, 8);
					if (m.equals(month))
						count++;

				}
			}

			if (count >= 3) {
				return false;
			} else
				return true;
		} else if (customerID.contains("MTL")) {
			String month = eventID.substring(6, 8);
			ArrayList<String> al = DEMSMontrealServer.mtlCustomerInfo.get(customerID);
			int count = 0;
			for (String id : al) {

				if (!id.contains("MTL")) {
					String m = id.substring(6, 8);
					if (m.equals(month))
						count++;

				}
			}

			if (count >= 3) {
				return false;
			} else
				return true;
		} else {
			String month = eventID.substring(6, 8);
			ArrayList<String> al = DEMSOttawaServer.otwCustomerInfo.get(customerID);
			int count = 0;
			for (String id : al) {

				if (!id.contains("OTW")) {
					String m = id.substring(6, 8);
					if (m.equals(month))
						count++;

				}
			}

			if (count >= 3) {
				return false;
			} else
				return true;
		}

	}


	public synchronized static Response addEvent(String eventID, String eventType, int bookingCapacity) {
//		dummyVals always stored in the DB so no need to check for an empty sub-HashMap for any eventType
		HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eventType).clone();

		if (temp.containsKey(eventID)) {
			int newCap = temp.get(eventID) + bookingCapacity;
			temp.put(eventID, newCap);
		} else {
			temp.put(eventID, bookingCapacity);
		}

		torDb.put(eventType, temp);
		displayTorDbContents();
		displayCustomerInfo();
		return new Response(add.SUCCESS.message, true);
	}

	public synchronized static Response removeEvent(String eventID, String eventType) {
//		dummyVals always stored in the DB so no need to check for an empty sub-HashMap for any eventType
		HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eventType).clone();

		if (!temp.containsKey(eventID)) {
			return new Response(remove.DOESNTEXIST.message, false);
		}
		for (ArrayList<String> al : torCustomerInfo.values()) {
			if (al.contains(eventID)) {
				al.remove(eventID);
				if (al.isEmpty())
					torCustomerInfo.remove(al);
				logOperation("Deleted Event which has been booked!", eventID, eventType, "NA", "NA", "Succeeded");
			}
		}

		temp.remove(eventID);
		torDb.put(eventType, temp);
		displayTorDbContents();
		displayCustomerInfo();
		return new Response(remove.SUCCESS.message, true);
	}

	public synchronized static Response dispEventAvailability(String eventType) {
		String msg = "";
		HashMap<String, Integer> temp = torDb.getOrDefault(eventType, new HashMap<String, Integer>());
		msg += "TOR|";
		for (String EventID : temp.keySet()) {
			msg += EventID + ":" + temp.get(EventID) + ",";
		}
		msg += "||";
		return new Response(msg, true);
	}

	public synchronized static Response bookEvent(String customerID, String eventID, String eventType) {
		if (torCustomerInfo.containsKey(customerID) && torCustomerInfo.get(customerID) != null
				&& torCustomerInfo.get(customerID).contains(eventID)) {
			return new Response("This event was already booked!", false);
		}
		String month = eventID.substring(6, 8);
		if (torCustomerInfo.containsKey(customerID) && !eventID.contains("TOR")) {
			ArrayList<String> al = torCustomerInfo.get(customerID);
			int count = 0;
			for (String id : al) {

				if (!id.contains("TOR")) {
					String m = id.substring(6, 8);
					if (m.equals(month))
						count++;

				}
			}

			if (count >= 3) {
				return new Response("customer cannot book more than 3 outside city events in the same month", false);
			}

		}
		if (eventID.contains("TOR")) {
			System.out.println(eventTypes[0]);
			System.out.println(sample);
			HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eventType).clone();
			if (!temp.containsKey(eventID)) {
				return new Response("This event does not exist!", false);
			} else if (temp.get(eventID) == 0)
				return new Response("Event capacity is zero", false);
			temp.put(eventID, temp.get(eventID) - 1);
			torDb.put(eventType, temp);
			displayTorDbContents();
		} else if (eventID.contains("MTL")) {
			DEMSMontrealServer.bookEvent(customerID, eventID, eventType);
		} else if (eventID.contains("OTW")) {
			DEMSOttawaServer.bookEvent(customerID, eventID, eventType);
		}

//		{

//			UDPclient(customerID, eventID, eventType, "book");
//		}
		ArrayList<String> temp1 = torCustomerInfo.get(customerID) == null ? (new ArrayList<>())
				: torCustomerInfo.get(customerID);
		temp1.add(eventID);
		torCustomerInfo.put(customerID, temp1);
		displayTorDbContents();
		displayCustomerInfo();
		return new Response("Event successfully Booked", false);

	}

	public synchronized static Response cancelEvent(String customerID, String eventID) {

		if (torCustomerInfo.containsKey(customerID) && torCustomerInfo.get(customerID) != null
				&& torCustomerInfo.get(customerID).contains(eventID)) {

			if (eventID.contains("TOR")) {
				for (String eType : torDb.keySet()) {
					for (String eID : torDb.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							System.out.println("entered");
							ArrayList<String> temp1 = torCustomerInfo.get(customerID);
							temp1.remove(eventID);
							if (temp1.size() == 0)
								torCustomerInfo.remove(customerID);
							else
								torCustomerInfo.put(customerID, temp1);
							HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							torDb.put(eType, temp);
							displayTorDbContents();
							return new Response("Event successfully cancelled", true);
						}
					}
				}
			} else {
				for (String eType : torDb.keySet()) {
					for (String eID : torDb.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							torDb.put(eType, temp);
						}
					}
				}
				UDPclient(customerID, eventID, "", "cancel");
				ArrayList<String> temp1 = torCustomerInfo.get(customerID);
				temp1.remove(eventID);
				displayCustomerInfo();
				return new Response("Event successfully cancelled", true);

			}
			return new Response("This event cannot be cancelled!", false);
		}
		return new Response("The event doesn't exist/ not in your schedule!", false);
	}

	public synchronized static Response getBookingSchedule(String customerID) {

		String msg="";
		if (!torCustomerInfo.containsKey(customerID)) {
			System.out.println("No events to display!");
			return new Response("No events to display!", false);
		}
//		TODO: Get events from other servers for this customerID.
//		System.out.println(customerID + ":" + torCustomerInfo.get(customerID).toString());
		ArrayList<String> al = torCustomerInfo.get(customerID);
		msg += "OTW|";
		for (String id : al) {
			if(id.contains("OTW")) {
				msg +=id+",";
			}
		}
		msg+="||";
		msg += "MTL|";
		for (String id : al) {
			if(id.contains("MTL")) {
				msg +=id+",";
			}
		}
		msg+="||";
		msg += "TOR|";
		for (String id : al) {
			if(id.contains("TOR")) {
				msg +=id+",";
			}
		}
		msg+="||";
		return new Response(msg, true);
	}

//	Helper Method to print contents of the modified DB after an operation done on it
	public static void displayTorDbContents() {
		System.out.println("\n------------DATABASE CONT.------------");
		for (String et : eventTypes) {
			System.out.println(et + ": ");
			HashMap<String, Integer> temp = torDb.getOrDefault(et, new HashMap<String, Integer>());
			System.out.println(temp.keySet().toString());
			System.out.println(temp.values());
		}
		System.out.println("-----------------------------------");
	}

	public static void displayCustomerInfo() {
		System.out.println("\n------------CLIENT INFO------------");
		for (String cID : torCustomerInfo.keySet()) {
			System.out.print(cID + ": ");
			System.out.println(torCustomerInfo.get(cID).toString());
		}
		System.out.println("-----------------------------------");
	}

//	TODO: Fix the logging
	public static void logOperation(String name, String eventID, String eventType, String customerID, String bookingCap,
			String status) {

		try {
			FileWriter fw = new FileWriter("TorontoLogs.txt", true);
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

	public static boolean ottawaListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(3002);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
//			System.out.println("Listener Server Started for Ottawa...");
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

	public static boolean montrealListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(3001);
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

}
