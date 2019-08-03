package RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import RMI.RDEMSInterface;
import Servers.RDEMSMontrealServer;
import Servers.RDEMSOttawaServer;
import Servers.RDEMSTorontoServer;


public class RDEMSInterfaceImpl extends UnicastRemoteObject implements RDEMSInterface {

	public RDEMSInterfaceImpl() throws RemoteException {
		super();
	}

	@Override
	public synchronized String addEvent(String eventID, String eventType, int bookingCapacity) throws RemoteException  {
		String res = "Invalid input";
		if (isValid(eventID, eventType, bookingCapacity)) {
			if (eventID.contains("TOR")) {
				res = RDEMSTorontoServer.addEvent(eventID, eventType, bookingCapacity);
				RDEMSTorontoServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = RDEMSMontrealServer.addEvent(eventID, eventType, bookingCapacity);
				RDEMSMontrealServer.logOperation("addEvent", eventID, eventType, "NA","NA",res);
			} else if (eventID.contains("OTW")) {
				res = RDEMSOttawaServer.addEvent(eventID, eventType, bookingCapacity);
				RDEMSOttawaServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized String removeEvent(String eventID, String eventType) throws RemoteException  {
		String res = "Invalid input";
		if(isValid(eventID, eventType)) {
			if (eventID.contains("TOR")) {
				res = RDEMSTorontoServer.removeEvent(eventID, eventType);
				RDEMSTorontoServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = RDEMSMontrealServer.removeEvent(eventID, eventType);
				RDEMSMontrealServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("OTW")) {
				res = RDEMSOttawaServer.removeEvent(eventID, eventType);
				RDEMSOttawaServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized String listEventAvailability(String eventType) throws RemoteException  {
			String msg=RDEMSTorontoServer.dispEventAvailability(eventType);
			RDEMSTorontoServer.logOperation("bookEvent", "NA", eventType,"NA","NA", "Succeeded");
	return msg;
	}

	@Override
	public synchronized String bookEvent(String customerID, String eventID, String eventType) throws RemoteException  {
		String res = "Invalid input";
		if( !isValid(customerID))
			return("Invalid Customer ID!");
		if(isValid(eventID, eventType)) {
			if(customerID.contains("TOR")) {
				res = RDEMSTorontoServer.bookEvent(customerID, eventID, eventType);
				RDEMSTorontoServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("OTW")) {
				res = RDEMSOttawaServer.bookEvent(customerID, eventID, eventType);
				RDEMSOttawaServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("MTL")) {
				res = RDEMSMontrealServer.bookEvent(customerID, eventID, eventType);
				RDEMSMontrealServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized String getBookingSchedule(String customerID)throws RemoteException  {
		String msg="";
		if(customerID.contains("TOR")) {
			msg+=RDEMSTorontoServer.getBookingSchedule(customerID);
			RDEMSTorontoServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else if(customerID.contains("OTW")) {
			msg+=RDEMSOttawaServer.getBookingSchedule(customerID);
			RDEMSOttawaServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else{
			msg+=RDEMSMontrealServer.getBookingSchedule(customerID);
			RDEMSMontrealServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			
		}
		return msg;
	}

	@Override
	public synchronized String cancelEvent(String customerID, String eventID) throws RemoteException  {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			res = RDEMSTorontoServer.cancelEvent(customerID, eventID);
			RDEMSTorontoServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res = RDEMSOttawaServer.cancelEvent(customerID, eventID);
			RDEMSOttawaServer.logOperation("cancelEvent",eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res = RDEMSMontrealServer.cancelEvent(customerID, eventID);
			RDEMSMontrealServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		return res;
	}
	
	@Override
	public synchronized String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) throws RemoteException  {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			 res=RDEMSTorontoServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			 RDEMSOttawaServer.logOperation("swapEvent",newEventID, oldEventID,customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res=RDEMSOttawaServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			RDEMSOttawaServer.logOperation("swapEvent",newEventID, oldEventID,customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res=RDEMSMontrealServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			RDEMSMontrealServer.logOperation("swapEvent", newEventID, oldEventID,customerID,"NA", res);
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
