package Shared;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Helper.Response;
import Servers.DEMSMontrealServer;
import Servers.DEMSOttawaServer;
import Servers.DEMSTorontoServer;
import Shared.DEMSInterface;


public class DEMSInterfaceImpl extends UnicastRemoteObject implements DEMSInterface {

	public DEMSInterfaceImpl() throws RemoteException {
		super();
	}

	@Override
	public Response addEvent(String eventID, String eventType, int bookingCapacity)throws RemoteException   {
		Response res = new Response("Invalid input",false);
		if (isValid(eventID, eventType, bookingCapacity)) {
			if (eventID.contains("TOR")) {
				res = DEMSTorontoServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSTorontoServer.logOperation("addEvent", eventID, eventType,"NA","NA", res.getMessage());
			} else if (eventID.contains("MTL")) {
				res = DEMSMontrealServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSMontrealServer.logOperation("addEvent", eventID, eventType, "NA","NA",res.getMessage());
			} else if (eventID.contains("OTW")) {
				res = DEMSOttawaServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSOttawaServer.logOperation("addEvent", eventID, eventType,"NA","NA", res.getMessage());
			}
		}
		return res;
	}

	@Override
	public Response removeEvent(String eventID, String eventType) throws RemoteException  {
		Response res = new Response("Invalid input",false);
		if(isValid(eventID, eventType)) {
			if (eventID.contains("TOR")) {
				res = DEMSTorontoServer.removeEvent(eventID, eventType);
				DEMSTorontoServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res.getMessage());
			} else if (eventID.contains("MTL")) {
				res = DEMSMontrealServer.removeEvent(eventID, eventType);
				DEMSMontrealServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res.getMessage());
			} else if (eventID.contains("OTW")) {
				res = DEMSOttawaServer.removeEvent(eventID, eventType);
				DEMSOttawaServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res.getMessage());
			}
		}
		return res;
	}

	@Override
	public Response listEventAvailability(String eventType)throws RemoteException   {
		Response msg=DEMSOttawaServer.dispEventAvailability(eventType);
		DEMSOttawaServer.logOperation("bookEvent", "NA", eventType,"NA","NA", "Succeeded");
	return msg;
	}

	@Override
	public Response bookEvent(String customerID, String eventID, String eventType) throws RemoteException  {
		Response res = new Response("Invalid input",false);
		if( !isValid(customerID))
			return(new Response("Invalid Customer ID!",false));
		if(isValid(eventID, eventType)) {
			if(customerID.contains("TOR")) {
				res = DEMSTorontoServer.bookEvent(customerID, eventID, eventType);
				DEMSTorontoServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res.getMessage());
			}
			else if(customerID.contains("OTW")) {
				res = DEMSOttawaServer.bookEvent(customerID, eventID, eventType);
				DEMSOttawaServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res.getMessage());
			}
			else if(customerID.contains("MTL")) {
				res = DEMSMontrealServer.bookEvent(customerID, eventID, eventType);
				DEMSMontrealServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res.getMessage());
			}
		}
		return res;
	}

	@Override
	public synchronized Response getBookingSchedule(String customerID)throws RemoteException  {
		String msg="";
		Response r;
		boolean b1=true,b2=true,b3=true;
		if(customerID.contains("TOR")) {
			r=DEMSTorontoServer.getBookingSchedule(customerID);
			msg+=r.getMessage();
			b1=r.getResult();
			DEMSTorontoServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else if(customerID.contains("OTW")) {
			r=DEMSOttawaServer.getBookingSchedule(customerID);
			msg+=r.getMessage();
			b1=r.getResult();
			DEMSOttawaServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else{
			r=DEMSMontrealServer.getBookingSchedule(customerID);
			msg+=r.getMessage();
			b1=r.getResult();
			DEMSMontrealServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			
		}
		return new Response(msg,b1&&b2&&b3);
	}

	@Override
	public Response cancelEvent(String customerID, String eventID) throws RemoteException  {
		Response res = new Response("Invalid input",false);
		if(customerID.contains("TOR")) {
			res = DEMSTorontoServer.cancelEvent(customerID, eventID);
			DEMSTorontoServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res.getMessage());
		}
		else if(customerID.contains("OTW")) {
			res = DEMSOttawaServer.cancelEvent(customerID, eventID);
			DEMSOttawaServer.logOperation("cancelEvent",eventID, "NA",customerID,"NA", res.getMessage());
		}
		else if(customerID.contains("MTL")){
			res = DEMSMontrealServer.cancelEvent(customerID, eventID);
			DEMSMontrealServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res.getMessage());
		}
		return res;
	}
	
	@Override
	public Response swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) throws RemoteException  {
		Response res =  new Response("Invalid input",false);
		if(customerID.contains("TOR")) {
			 res=DEMSTorontoServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			 DEMSTorontoServer.logOperation("swapEvent", oldEventID, newEventID,customerID,"NA", res.getMessage());
		}
		else if(customerID.contains("OTW")) {
			res = DEMSOttawaServer.swapEvent(customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			DEMSOttawaServer.logOperation("swapEvent", oldEventID, newEventID,customerID,"NA", res.getMessage());
		}
		else if(customerID.contains("MTL")){
			res = DEMSMontrealServer.swapEvent(customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			DEMSMontrealServer.logOperation("swapEvent", oldEventID, newEventID,customerID,"NA", res.getMessage());
		}
		return res;
	}
	
	private boolean isValid(String eventID, String eventType, int bookingCapacity)throws RemoteException  {
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
