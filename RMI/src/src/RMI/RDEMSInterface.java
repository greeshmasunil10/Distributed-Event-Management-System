package RMI;

import java.rmi.Remote;


public interface RDEMSInterface extends Remote {
	public String addEvent(String eventID, String eventType, int bookingCapacity)  throws java.rmi.RemoteException;;

	public String removeEvent(String eventID, String eventType)  throws java.rmi.RemoteException;;

	public String listEventAvailability(String eventType)  throws java.rmi.RemoteException;;

	public String bookEvent(String customerID, String eventID, String eventType) throws java.rmi.RemoteException; ;

	public String getBookingSchedule(String customerID)  throws java.rmi.RemoteException;;

	public String cancelEvent(String customerID, String eventID)  throws java.rmi.RemoteException;;

	public String swapEvent (String customerID, String newEventID,  String newEventType,  String oldEventID,  String oldEventType) throws java.rmi.RemoteException;;
} // end interface
