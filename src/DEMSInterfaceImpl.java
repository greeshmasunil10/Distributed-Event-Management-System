import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class DEMSInterfaceImpl extends UnicastRemoteObject implements DEMSInterface {

	protected DEMSInterfaceImpl() throws RemoteException {
		super();
	}

	@Override
	public String addEvent(String eventID, String eventType, int bookingCapacity) throws RemoteException {
		String res = "Invalid input";
		if (isValid(eventID, eventType, bookingCapacity)) {
			if (eventID.contains("TOR")) {
				res = DEMSTorontoServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSTorontoServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = DEMSMontrealServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSMontrealServer.logOperation("addEvent", eventID, eventType, "NA","NA",res);
			} else if (eventID.contains("OTW")) {
				res = DEMSOttawaServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSOttawaServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public String removeEvent(String eventID, String eventType) throws RemoteException {
		String res = "Invalid input";
		if(isValid(eventID, eventType)) {
			if (eventID.contains("TOR")) {
				res = DEMSTorontoServer.removeEvent(eventID, eventType);
				DEMSTorontoServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = DEMSMontrealServer.removeEvent(eventID, eventType);
				DEMSMontrealServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("OTW")) {
				res = DEMSOttawaServer.removeEvent(eventID, eventType);
				DEMSOttawaServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public String listEventAvailability(String eventType) throws RemoteException {
			String msg=DEMSTorontoServer.dispEventAvailability(eventType);
			DEMSTorontoServer.logOperation("bookEvent", "NA", eventType,"NA","NA", "Succeeded");
	return msg;
	}

	@Override
	public String bookEvent(String customerID, String eventID, String eventType) throws RemoteException {
		String res = "Invalid input";
		if( !isValid(customerID))
			return("Invalid Customer ID!");
		if(isValid(eventID, eventType)) {
			if(customerID.contains("TOR")) {
				res = DEMSTorontoServer.bookEvent(customerID, eventID, eventType);
				DEMSTorontoServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("OTW")) {
				res = DEMSOttawaServer.bookEvent(customerID, eventID, eventType);
				DEMSOttawaServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("MTL")) {
				res = DEMSMontrealServer.bookEvent(customerID, eventID, eventType);
				DEMSMontrealServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized void getBookingSchedule(String customerID) throws RemoteException {
		if(customerID.contains("TOR")) {
			DEMSTorontoServer.getBookingSchedule(customerID);
			DEMSTorontoServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else if(customerID.contains("OTW")) {
			DEMSOttawaServer.getBookingSchedule(customerID);
			DEMSOttawaServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else{
			DEMSMontrealServer.getBookingSchedule(customerID);
			DEMSMontrealServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			
		}
	}

	@Override
	public String cancelEvent(String customerID, String eventID) throws RemoteException {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			res = DEMSTorontoServer.cancelEvent(customerID, eventID);
			DEMSTorontoServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res = DEMSOttawaServer.cancelEvent(customerID, eventID);
			DEMSOttawaServer.logOperation("cancelEvent",eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res = DEMSMontrealServer.cancelEvent(customerID, eventID);
			DEMSMontrealServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		return res;
	}
	
	
	private boolean isValid(String eventID, String eventType, int bookingCapacity) {
		return (eventType.equalsIgnoreCase("Conferences") || eventType.equalsIgnoreCase("Seminars")
				|| eventType.equalsIgnoreCase("Trade Shows")) 
				&& (eventID.length() == 10 && (eventID.substring(0, 3).equals("TOR") || eventID.substring(0, 3).equals("MTL")
						|| eventID.substring(0, 3).equals("OTW")) && (eventID.charAt(3) == 'M' || eventID.charAt(3) == 'A' || eventID.charAt(3) == 'E')) 
				&& (bookingCapacity > 0);
	}

	private boolean isValid( String eventID, String eventType) {
		return (eventType.equalsIgnoreCase("Conferences") || eventType.equalsIgnoreCase("Seminars")
				|| eventType.equalsIgnoreCase("Trade Shows")) 
				&& (eventID.length() == 10 && (eventID.substring(0, 3).equals("TOR") || eventID.substring(0, 3).equals("MTL")
						|| eventID.substring(0, 3).equals("OTW")) && (eventID.charAt(3) == 'M' || eventID.charAt(3) == 'A' || eventID.charAt(3) == 'E'));
	}
	private boolean isValid(String customerID) {
		return (!customerID.isEmpty() &&(customerID.substring(0, 3).equals("TOR") || customerID.substring(0, 3).equals("MTL")
						|| customerID.substring(0, 3).equals("OTW")) && customerID.charAt(3) == 'C' );
	}
	
}
