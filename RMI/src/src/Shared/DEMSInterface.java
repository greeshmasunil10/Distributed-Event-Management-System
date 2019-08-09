package Shared;

import java.rmi.Remote;

import Model.Response;

public interface DEMSInterface extends Remote{
	public Response addEvent(String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	public Response removeEvent(String eventID, String eventType) throws java.rmi.RemoteException;

	public Response listEventAvailability(String eventType) throws java.rmi.RemoteException;

	public Response bookEvent(String customerID, String eventID, String eventType) throws java.rmi.RemoteException;

	public Response getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	public Response cancelEvent(String customerID, String eventID) throws java.rmi.RemoteException;

	public Response swapEvent (String customerID, String newEventID,  String newEventType,  String oldEventID,  String oldEventType)throws java.rmi.RemoteException;
} // end interface
