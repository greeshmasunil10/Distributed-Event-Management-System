package com.web.service;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import Helpers.ThreadTestCases;


public class WDEMSClient {

	static String userID;
	String city;
	static String res;
	Scanner sc;
	static PrintWriter writer;
	static WDEMSInterface serverobj;


	public WDEMSClient() {
		this.sc = new Scanner(System.in);
	}

	public static void main(String args[]) throws IOException, NotBoundException, InvalidName {
		WDEMSClient obj = new WDEMSClient();
		
		obj.getID();
//		ThreadTestCases t= new ThreadTestCases();
//		t.startThreads(serverobj);
//		obj.sc.nextLine();	
		obj.init();
	}
	
	

	private void getID() throws IOException, InvalidName {
//		String sample="greeshma";
//		System.out.println(sample.substring(3,5));
		System.out.println("\nLOGIN***");
		System.out.print("Enter your ID:");

		this.userID = this.sc.nextLine();
		// -ORBInitialPort 1050 -ORBInitialHost localhost
//		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);


//		int c=intObj.add(5, 3);
		URL addURL=new URL("http://localhost:8081/addition?wsdl");
			if (this.userID.contains("MTL")) {
				 addURL = new URL("http://localhost:8081/addition?wsdl");
				this.city = "montreal";
			} else if (this.userID.contains("OTW")) {
				addURL = new URL("http://localhost:8083/addition?wsdl");
//				serverobj = (CorbaServer) CorbaServerHelper.narrow(ncRef.resolve_str("OTW"));
//			 serverobj= new DEMSCorbaServerImpl("OTW");
				this.city = "ottawa";
			} else if (this.userID.contains("TOR")) {
				addURL = new URL("http://localhost:8082/addition?wsdl");
//			 serverobj= new DEMSCorbaServerImpl("TOR");
				this.city = "toronto";
			} else {
				System.out.println("Invalid ID");
				getID();
				return;
			}
		QName addQName = new QName("http://service.web.com/", "WDEMSInterfaceImplService");
		
		Service serviceObj = Service.create(addURL, addQName);
		serverobj = serviceObj.getPort(WDEMSInterface.class);

	}

	private void init() throws NotBoundException, IOException, InvalidName {
		if (this.userID.charAt(3) == 'M') {
			System.out.print("\nYou are logged in Manager:" + city);
			manager();

		} else if (this.userID.charAt(3) == 'C') {
			System.out.print("\nYou are logged in as client:" + city);
			client();

		} else {
			System.out.println("Invalid ID");
			getID();
			init();
			return;
		}
	}

	private void client() throws IOException, NotBoundException, InvalidName {
		boolean menu = true;
		while (menu) {
			System.out.print(
					"\n\n1.Book Event \n2.Get Booking Schedule \n3.Cancel Event \n4.Swap Events\n5.Log out \nEnter Choice:");
			String ch;
			ch = this.sc.nextLine();
			switch (ch) {
			case "1":
				bookEvent(true);
				break;
			case "2":
				getBookingSchedule(true);
				break;
			case "3":
				cancelEvent(true);
				break;
			case "4":
				swapEvent(true);
				break;
			case "5":
				menu = false;
				break;
			default:
				System.out.println("Invalid choice!");
			}
		}
		getID();
		init();
	}

	private void manager() throws NotBoundException, IOException, InvalidName {
		boolean menu = true;
		while (menu) {
			System.out.print(
					"\n\n1.Add Event \n2.Remove Event \n3.List Event Availablity \n4.Book Event \n5.Get Booking Schedule \n6.Cancel Event \n7.Swap Event \n8.Log out \nEnter Choice:");
			String ch;
			ch = this.sc.nextLine();
			switch (ch) {
			case "1":
				addEvent();
				break;
			case "2":
				removeEvent();
				break;
			case "3":
				listEventAvailability();
				break;
			case "4":
				bookEvent(false);
				break;
			case "5":
				getBookingSchedule(false);
				break;
			case "6":
				cancelEvent(false);
				break;
			case "7":
				swapEvent(false);
				break;
			case "8":
				menu = false;
				break;
			default:
				System.out.println("Invalid choice!");
			}
		}
		getID();
		init();
	}

	private void swapEvent(boolean check) {
		String oldEventID, oldEventType, customerID, newEventID, newEventType;
		System.out.println("\nEnter event details:");
		if (!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine(); // fix null
		} else {
			customerID = this.userID;
		}
		customerID = this.userID;
		System.out.print("Old Event ID:");
		oldEventID = this.sc.nextLine();
		System.out.print("Old Event Type:");
		oldEventType = this.sc.nextLine();
		System.out.print("New Event ID:");
		newEventID = this.sc.nextLine();
		System.out.print("New Event Type:");
		newEventType = this.sc.nextLine();

//		if(!oldEventType.equals(null)
//				&& !oldEventType.equals(null)
//				&& !newEventID.equals(null)
//				&& !newEventType.equals(null)) {
//			System.out.println("Empty Entries! Enter Again:");
//			swapEvent(check);
//			return;
//		}

		if (!(oldEventID.contains("TOR") || oldEventID.contains("MTL") || oldEventID.contains("OTW")
				|| oldEventType.contains("Seminars") || oldEventType.contains("Trade Shows")
				|| oldEventType.contains("Conferences") || newEventID.contains("TOR") || newEventID.contains("MTL")
				|| newEventID.contains("OTW") || newEventType.contains("Seminars")
				|| newEventType.contains("Trade Shows") || newEventType.contains("Conferences"))) {
			System.out.println("Invalid Entries! Enter Again:");
//			swapEvent(check);
			return;
		}

		if (!oldEventID.equalsIgnoreCase(newEventID))
			this.res = this.serverobj.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
		else {
			System.out.println("Old and new Events cannot be the same! Enter Again:");
//			swapEvent(check);
			return;
		}

		logOperation("Swap Event", oldEventID, oldEventType, this.userID, this.res);
		System.out.println(this.res);
	}

	private void bookEvent(boolean check) {

		String eventID, eventType, customerID;
		System.out.println("\nEnter event details:");
		if (!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine(); // fix null
		} else {
			customerID = this.userID;
		}
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();

		this.res = this.serverobj.bookEvent(customerID, eventID, eventType);

		logOperation("bookEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

	private void cancelEvent(boolean check) {
		String eventID, customerID;
		System.out.println("\nEnter event details:");
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		if (!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine();
		} else {
			customerID = this.userID;
		}

		res = "not entered";
		this.res = this.serverobj.cancelEvent(customerID, eventID);
		logOperation("cancelEvent", eventID, "NA", this.userID, this.res);
		System.out.println(this.res);
	}

	private void getBookingSchedule(boolean check) {
		String customerID = "";
		if (!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine(); // fix null
		} else {
			customerID = this.userID;
		}
		String msg = "Unsuccessfull";
		msg = this.serverobj.getBookingSchedule(customerID);
		logOperation("getBookingSchedule", "NA", "NA", this.userID, "Succeeded");
		System.out.println(msg);
//		System.out.println("Shown in the " + city + " server console!");
	}

	public void removeEvent() throws NotBoundException, IOException, InvalidName {
		String eventID, eventType;
		System.out.print("\nEvent ID to remove:");
		eventID = this.sc.nextLine();
		if (!eventID.substring(0, 3).equals(this.userID.substring(0, 3))) {
			System.out.println("Event Manager of this city cannot remove event in the another city");
			manager();
		}
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		this.res = this.serverobj.removeEvent(eventID, eventType);
		logOperation("removeEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

	public void addEvent() throws IOException, NotBoundException, InvalidName {
		String eventID, eventType;
		int bookingCapacity = 0;
		System.out.println("\nEnter event details:");
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		if (eventID.isEmpty()) {
			System.out.println("\nEvent ID cannot be empty!");
			addEvent();
			return;
		}
		if (!eventID.substring(0, 3).equals(this.userID.substring(0, 3))) {
			if(!eventID.substring(0, 3).equals("TOR")
					&&!eventID.substring(0, 3).equals("MTL")&&
					!eventID.substring(0, 3).equals("OTW")) {
				System.out.println("invalid event id!");
			}else {
			System.out.println("Event Manager of this city cannot add event in the another city");
			}manager();
		}
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		System.out.print("Booking Capacity:");
		try {
			bookingCapacity = Integer.parseInt(this.sc.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Enter a number for the booking capacity.");
			addEvent();
			return;
		}
		this.res = this.serverobj.addEvent(eventID, eventType, bookingCapacity);
		logOperation("addEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

// 	TODO: Get HashMap and print it
	public void listEventAvailability() {
		String msg = "asdsfsdf";
		String eventType;
		System.out.print("\nEvent Type:");
		eventType = this.sc.nextLine();
		if (!(eventType.equals("Conferences") || eventType.equals("Seminars") || eventType.equals("Trade Shows"))) {
			System.out.println("Invalid input!");
			System.out.println(eventType + "i am here");
			listEventAvailability();
			return;
		}
		msg = this.serverobj.listEventAvailability(eventType);
		System.out.println(msg);
		logOperation("listEventAvailability", "NA", eventType, this.userID, "Succeeded");
	}

	public static void logOperation(String name, String eventID, String eventType, String customerID, String status) {

		try {
			FileWriter fw = new FileWriter("ClientLogs.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String log = "\n" + name + "Performed.\nTime: " + timeStamp + "\nCustomerID: " + customerID + "\nEventID: "
				+ eventID + "\nEventType: " + eventType + "\nStatus: " + status;
		writer.println(log);
		writer.close();
	}

}