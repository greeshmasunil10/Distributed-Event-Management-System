package com.web.service;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.servers.WDEMSMontrealServer;
import com.servers.WDEMSOttawaServer;
import com.servers.WDEMSTorontoServer;


@WebService(endpointInterface = "com.web.service.WDEMSInterface")

@SOAPBinding(style = SOAPBinding.Style.RPC)
public class WDEMSInterfaceImpl implements WDEMSInterface {

	public WDEMSInterfaceImpl() {
		super();
	}

	@Override
	public synchronized String addEvent(String eventID, String eventType, int bookingCapacity)  {
		String res = "Invalid input";
		if (isValid(eventID, eventType, bookingCapacity)) {
			if (eventID.contains("TOR")) {
				res = WDEMSTorontoServer.addEvent(eventID, eventType, bookingCapacity);
				WDEMSTorontoServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = WDEMSMontrealServer.addEvent(eventID, eventType, bookingCapacity);
				WDEMSMontrealServer.logOperation("addEvent", eventID, eventType, "NA","NA",res);
			} else if (eventID.contains("OTW")) {
				res = WDEMSOttawaServer.addEvent(eventID, eventType, bookingCapacity);
				WDEMSOttawaServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized String removeEvent(String eventID, String eventType)  {
		String res = "Invalid input";
		if(isValid(eventID, eventType)) {
			if (eventID.contains("TOR")) {
				res = WDEMSTorontoServer.removeEvent(eventID, eventType);
				WDEMSTorontoServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = WDEMSMontrealServer.removeEvent(eventID, eventType);
				WDEMSMontrealServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("OTW")) {
				res = WDEMSOttawaServer.removeEvent(eventID, eventType);
				WDEMSOttawaServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized String listEventAvailability(String eventType)  {
			String msg=WDEMSTorontoServer.dispEventAvailability(eventType);
			WDEMSTorontoServer.logOperation("bookEvent", "NA", eventType,"NA","NA", "Succeeded");
	return msg;
	}

	@Override
	public synchronized String bookEvent(String customerID, String eventID, String eventType)  {
		String res = "Invalid input";
		if( !isValid(customerID))
			return("Invalid Customer ID!");
		if(isValid(eventID, eventType)) {
			if(customerID.contains("TOR")) {
				res = WDEMSTorontoServer.bookEvent(customerID, eventID, eventType);
				WDEMSTorontoServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("OTW")) {
				res = WDEMSOttawaServer.bookEvent(customerID, eventID, eventType);
				WDEMSOttawaServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("MTL")) {
				res = WDEMSMontrealServer.bookEvent(customerID, eventID, eventType);
				WDEMSMontrealServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public synchronized String getBookingSchedule(String customerID) {
		String msg="";
		if(customerID.contains("TOR")) {
			msg+=WDEMSTorontoServer.getBookingSchedule(customerID);
			WDEMSTorontoServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else if(customerID.contains("OTW")) {
			msg+=WDEMSOttawaServer.getBookingSchedule(customerID);
			WDEMSOttawaServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		}
		else{
			msg+=WDEMSMontrealServer.getBookingSchedule(customerID);
			WDEMSMontrealServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			
		}
		return msg;
	}

	@Override
	public synchronized String cancelEvent(String customerID, String eventID)  {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			res = WDEMSTorontoServer.cancelEvent(customerID, eventID);
			WDEMSTorontoServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res = WDEMSOttawaServer.cancelEvent(customerID, eventID);
			WDEMSOttawaServer.logOperation("cancelEvent",eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res = WDEMSMontrealServer.cancelEvent(customerID, eventID);
			WDEMSMontrealServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		return res;
	}
	
	@Override
	public synchronized String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType)  {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			 res=WDEMSTorontoServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			 WDEMSOttawaServer.logOperation("swapEvent",newEventID, oldEventID,customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res=WDEMSOttawaServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			WDEMSOttawaServer.logOperation("swapEvent",newEventID, oldEventID,customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res=WDEMSMontrealServer.swapEvent( customerID,  newEventID,  newEventType,  oldEventID, oldEventType);
			WDEMSMontrealServer.logOperation("swapEvent", newEventID, oldEventID,customerID,"NA", res);
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
