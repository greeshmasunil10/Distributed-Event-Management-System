package com.servers;
import java.io.BufferedReader;
import javax.xml.ws.Endpoint;

import com.web.service.WDEMSInterfaceImpl;

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

import javax.xml.ws.Endpoint;

public class WDEMSMontrealServer {
	private static HashMap<String, HashMap<String, Integer>> mtlDb = new HashMap<>();
	private static HashMap<String, ArrayList<String>> mtlCustomerInfo = new HashMap<>();
	static String[] eventTypes = { "Conferences", "Seminars", "Trade Shows" };
	static PrintWriter writer;

	public static WDEMSInterfaceImpl ImpObj;

	public static void main(String args[]) {		
	}
	
	public static HashMap<String, HashMap<String, Integer>> returnDb(){
		return mtlDb;
	}
	
	public static void startServer() {
		System.out.println("Web Service Server Started...");
		ImpObj = new WDEMSInterfaceImpl();
		Endpoint endpoint = Endpoint.publish("http://localhost:8081/addition", ImpObj);
		
		
		
		HashMap<String, Integer> dummyValsConf = new HashMap<>();
//		dummyValsConf.put("MTLM100519", 5);
//		dummyValsConf.put("MTLA100519", 10);
//		dummyValsConf.put("MTLE100519", 15);
		mtlDb.put(eventTypes[0], dummyValsConf);

		HashMap<String, Integer> dummyValsSem = new HashMap<>();
//		dummyValsSem.put("MTLM112211", 5);
//		dummyValsSem.put("MTLA110519", 10);
//		dummyValsSem.put("MTLE110519", 15);
		mtlDb.put(eventTypes[1], dummyValsSem);

		HashMap<String, Integer> dummyValsTS = new HashMap<>();
//		dummyValsTS.put("MTLM120519", 5);
//		dummyValsTS.put("MTLA120519", 10);
//		dummyValsTS.put("MTLE120519", 15);
		mtlDb.put(eventTypes[2], dummyValsTS);

		displaymtlDbContents();

		
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
	
	public synchronized static String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) {
		String res = "Event could not be swapped!";
		HashMap<String, Integer> temp1 = (HashMap<String, Integer>) mtlDb.get(oldEventType).clone();
		HashMap<String, Integer> temp2 = (HashMap<String, Integer>) WDEMSOttawaServer.returnDb().get(oldEventType)
				.clone();
		HashMap<String, Integer> temp3 = (HashMap<String, Integer>) WDEMSTorontoServer.returnDb().get(oldEventType)
				.clone();
		if (temp1.containsKey(oldEventID) || temp2.containsKey(oldEventID) || temp3.containsKey(oldEventID)) {
			if (mtlCustomerInfo.containsKey(customerID) && mtlCustomerInfo.get(customerID).contains(oldEventID)) {
				String msg1 = bookEvent(customerID, newEventID, newEventType);
				if (!msg1.equals("Event successfully Booked")) {
//				res = "Event cannot be swapped";
					return msg1;
				}
				String msg2 = cancelEvent(customerID, oldEventID);
				if (!msg2.equals("Event successfully cancelled")) {
					res = "Event could not be swapped: " + msg2;
					cancelEvent(customerID, oldEventID);
					return res;
				}
				res = " Event succefully swapped";
				logOperation("Swap Performed", newEventID, newEventType, "NA", "NA", "Succeeded");
				displaymtlDbContents();
				displayCustomerInfo();
			} else {
				res = "Event could not be swapped since this event wasnt booked!";
			}
		} else {
			res = "Event could not be swapped since this event wasnt booked!";
		}
		return res;
	}

	public synchronized static String removeEvent(String eventID, String eventType) {
//		dummyVals always smtled in the DB so no need to check for an empty sub-HashMap for any eventType

		HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eventType).clone();

		if (!temp.containsKey(eventID)) {
			return "Event doesn't exist";
		}
		for (ArrayList<String> al : mtlCustomerInfo.values()) {
			if (al.contains(eventID)) {
				al.remove(eventID);
				if (al.isEmpty())
					mtlCustomerInfo.remove(al);
				logOperation("Deleted Event which has been booked!", eventID, eventType, "NA", "NA", "Succeeded");
			}
		}

		temp.remove(eventID);
		mtlDb.put(eventType, temp);
		displaymtlDbContents();
		displayCustomerInfo();
		return "Event successfully removed!";
	}

	public synchronized static String getBookingSchedule(String customerID) {
		String msg="";
		if (!mtlCustomerInfo.containsKey(customerID)) {
			System.out.println("No events to display!");
			return "No events to display!";
		}
//		TODO: Get events from other servers for this customerID.
		System.out.println(customerID + ":" + mtlCustomerInfo.get(customerID).toString());
		msg+= "\n" + customerID + ":" + mtlCustomerInfo.get(customerID).toString();
		return msg;
	}
	
	public synchronized static String bookEvent(String customerID, String eventID, String eventType) {
		if (mtlCustomerInfo.containsKey(customerID) && mtlCustomerInfo.get(customerID) != null
				&& mtlCustomerInfo.get(customerID).contains(eventID)) {
			return "This event was already booked!";
		}
		String month = eventID.substring(6, 8);
		if (mtlCustomerInfo.containsKey(customerID) && !eventID.contains("MTL")) {
			ArrayList<String> al = mtlCustomerInfo.get(customerID);
			int count = 0;
			for (String id : al) {

				if (!id.contains("MTL")) {
					String m = id.substring(6, 8);
					if (m.equals(month))
						count++;

				}
			}

			if (count >= 3) {
				return "customer cannot book more than 3 outside city events in the same month";
			}

		}
		System.out.println("greeshma: "+eventType);
		if (eventID.contains("MTL")) {
			
			System.out.println("greeshma: "+"enetered inside");
			HashMap<String, Integer> temp = (HashMap<String, Integer>) mtlDb.get(eventType).clone();
			System.out.println(temp);
			if (!temp.containsKey(eventID) ) {
				System.out.println("greeshma here");
				return "This event does not exist!";
			}
			else if(temp.get(eventID) == 0)
				return "Event capacity is zero";
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
		return "Event successfully Booked";
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
							return "Event successfully cancelled";
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
	
	public synchronized static String dispEventAvailability(String eventType) {
		System.out.println("Entered Montreal");
		String msg = "";
		System.out.println("\n------------EVENTS------------");
		msg += "\n------------EVENTS------------";
		System.out.println(eventType + ": ");
		msg += "\n" + eventType + ": ";
		HashMap<String, Integer> temp = mtlDb.getOrDefault(eventType, new HashMap<String, Integer>());
		
		for(String EventID:temp.keySet()) {
			msg+= "\n"+EventID + " "+ temp.get(EventID);
		}
	
		
//		System.out.println(temp.keySet().toString());
		
//		msg += "\n" + temp.keySet().toString();
		
//		System.out.println(temp.values());
		
//		msg += "\n" + temp.values();
		
		System.out.println("-----------------------------------");
		msg += "\n" + "-----------------------------------";
//		UDPclient("MTL", "MTL", eventType, "disp");
//		UDPclient("OTW", "OTW", eventType, "disp");
		return msg;
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
					String buff = list[3] + ":" + list[2] + " successfully cancelled by tor to mon for " + list[1];
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

	public static String ottawaListener() {
		DatagramSocket aSocket = null;
		String res="null";
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
					 res=bookEvent(list[1], list[2], list[3]);
					String buff = res;
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
		return res;
	}

	



} // end class
