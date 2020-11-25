package com.web.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WDEMSInterface {
	public String addEvent(String eventID, String eventType, int bookingCapacity) ;

	public String removeEvent(String eventID, String eventType) ;

	public String listEventAvailability(String eventType) ;

	public String bookEvent(String customerID, String eventID, String eventType) ;

	public String getBookingSchedule(String customerID) ;

	public String cancelEvent(String customerID, String eventID) ;

	public String swapEvent (String customerID, String newEventID,  String newEventType,  String oldEventID,  String oldEventType);
} // end interface
