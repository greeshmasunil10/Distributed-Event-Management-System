import java.io.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class DEMSClient {

	String userID, city, hostName, registryURL;
	String res;
	int RMIPort;
	DEMSInterface callingObj;
	Scanner sc;
	PrintWriter writer;
	
	public DEMSClient() {
		this.sc = new Scanner(System.in);
		this.hostName = "localhost";
	}

	public static void main(String args[]) throws IOException, NotBoundException {
		DEMSClient obj = new DEMSClient();
		obj.getID();
		obj.init();
	}

	private void getID() throws IOException {
		System.out.println("\nLOGIN***");
		System.out.print("Enter your ID:");
		
		this.userID = this.sc.nextLine();
		
		if (this.userID.contains("MTL")) {
			this.RMIPort = 1000;
			this.city = "montreal";
		} else if (this.userID.contains("OTW")) {
			this.RMIPort = 2000;
			this.city = "ottawa";
		} else if (this.userID.contains("TOR")) {
			this.RMIPort = 1099;
			this.city = "toronto";
		} else {
			System.out.println("Invalid ID");
			getID();
			return;
		}
		System.out.println("Port:" + this.RMIPort);
	}

	private void init() throws NotBoundException, IOException {
			this.registryURL = "rmi://" + this.hostName + ":" + this.RMIPort + "/" + this.city;
			this.callingObj = (DEMSInterface) Naming.lookup(this.registryURL);

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

	private void client() throws IOException, NotBoundException {
		boolean menu = true;
		while (menu) {
			System.out.print("\n\n1.Book Event \n2.Get Booking Schedule \n3.Cancel Event \n4.Log out \nEnter Choice:");
			String ch;
			ch = this.sc.nextLine();
			switch (ch) {
			case "1":
				bookEvent(true);
				break;
			case "2":
				getBookingSchedule();
				break;
			case "3":
				cancelEvent(true);
				break;
			case "4":
				menu = false;
				break;
			default:
				System.out.println("Invalid choice!");
			}
		}
		getID();
		init();
	}

	private void manager() throws NotBoundException, IOException {
		boolean menu = true;
		while (menu) {
			System.out.print(
					"\n\n1.Add Event \n2.Remove Event \n3.List Event Availablity \n4.Book Event \n5.Get Booking Schedule \n6.Cancel Event \n7.Log out \nEnter Choice:");
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
				getBookingSchedule();
				break;
			case "6":
				cancelEvent(false);
				break;
			case "7":
				menu = false;
				break;
			default:
				System.out.println("Invalid choice!");
			}
		}
		getID();
		init(); 
	}
	
	private void bookEvent(boolean check) {
		
		String eventID, eventType, customerID;
		System.out.println("\nEnter event details:");
		if(!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine(); // fix null
		}
		else {
			customerID = this.userID;
		}
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		
		try {
			this.res = this.callingObj.bookEvent(customerID, eventID, eventType);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		logOperation("bookEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

	private void cancelEvent(boolean check) {
		String eventID, customerID;
		System.out.println("\nEnter event details:");
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		if(!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine();
		}
		else {
			customerID = this.userID;
		}

		try {
			this.res = this.callingObj.cancelEvent(customerID, eventID);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		logOperation("cancelEvent", eventID, "NA", this.userID, this.res);
		System.out.println(this.res);
	}

	private void getBookingSchedule() {
		try {
			this.callingObj.getBookingSchedule(this.userID);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		logOperation("getBookingSchedule", "NA", "NA", this.userID, "Succeeded");
		System.out.println("Shown in the " + city + " server console!");
	}

	public void removeEvent() throws NotBoundException, IOException {
		String eventID, eventType;
		System.out.print("\nEvent ID to remove:");
		eventID = this.sc.nextLine();
		if(!eventID.substring(0,3).equals(this.userID.substring(0,3))) {
			System.out.println("Event Manager of this city cannot remove event in the another city");
			manager();
		}
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		try {
			this.res = this.callingObj.removeEvent(eventID, eventType);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		logOperation("removeEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

	public void addEvent() throws IOException, NotBoundException {
		String eventID, eventType;
		int bookingCapacity = 0;
		System.out.println("\nEnter event details:");
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		if(eventID.isEmpty()) {
			System.out.println("\nEvent ID cannot be empty!");
			addEvent();
			return;
		}
		if(!eventID.substring(0,3).equals(this.userID.substring(0,3))) {
			System.out.println("Event Manager of this city cannot add event in the another city");
			manager();
		}
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		System.out.print("Booking Capacity:");
		try {
			bookingCapacity = Integer.parseInt(this.sc.nextLine());
		} catch(NumberFormatException e) {
			System.out.println("Enter a number for the booking capacity.");
			addEvent();
			return;
		}
		try {
			this.res = this.callingObj.addEvent(eventID, eventType, bookingCapacity);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		logOperation("addEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}
	
// 	TODO: Get HashMap and print it
	public void listEventAvailability() {
		String msg="asdsfsdf";
		String eventType;
		System.out.print("\nEvent Type:");
		eventType = this.sc.nextLine();
		if(!eventType.equals("Conferences") || !eventType.equals("Seminars")
				|| !eventType.equals("Trade Shows")) {
			System.out.println("Invalid input!");
			 listEventAvailability();
			return;
		}
		try {
			msg=this.callingObj.listEventAvailability(eventType);
			System.out.println(msg);
			logOperation("listEventAvailability", "NA", eventType, this.userID, "Succeeded");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void logOperation(String name, String eventID, String eventType, String customerID, String status) {

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