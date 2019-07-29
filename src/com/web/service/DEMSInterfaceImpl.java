package com.web.service;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.servers.DEMSMontrealServer;
import com.servers.DEMSOttawaServer;
import com.servers.DEMSTorontoServer;


@WebService(endpointInterface = "com.web.service.DEMSInterface")

@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DEMSInterfaceImpl implements DEMSInterface {

	public DEMSInterfaceImpl() {
		super();
	}

	@Override
	public String addEvent(String eventID, String eventType, int bookingCapacity)  {
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
	public String removeEvent(String eventID, String eventType)  {
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
	public String listEventAvailability(String eventType)  {
			String msg=DEMSTorontoServer.dispEventAvailability(eventType);
			DEMSTorontoServer.logOperation("bookEvent", "NA", eventType,"NA","NA", "Succeeded");
	return msg;
	}

	@Override
	public String bookEvent(String customerID, String eventID, String eventType)  {
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
	public synchronized String getBookingSchedule(String customerID) {
		String msg="";
		if(customerID.contains("TOR")) {
			msg+=DEMSTorontoServer.getBookingSchedule(customerID);
			DEMSTorontoServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else if(customerID.contains("OTW")) {
			msg+=DEMSOttawaServer.getBookingSchedule(customerID);
			DEMSOttawaServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else{
			msg+=DEMSMontrealServer.getBookingSchedule(customerID);
			DEMSMontrealServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			
		}
		return msg;
	}

	@Override
	public String cancelEvent(String customerID, String eventID)  {
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
	
	@Override
	public String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType)  {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			 res=DEMSTorontoServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			 DEMSOttawaServer.logOperation("swapEvent",newEventID, oldEventID,customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res=DEMSMontrealServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			DEMSOttawaServer.logOperation("swapEvent",newEventID, oldEventID,customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res=DEMSOttawaServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			DEMSMontrealServer.logOperation("swapEvent", newEventID, oldEventID,customerID,"NA", res);
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
