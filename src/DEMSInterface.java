import java.rmi.*;
import java.util.HashMap;

public interface DEMSInterface extends Remote {
	public String addEvent(String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	public String removeEvent(String eventID, String eventType) throws java.rmi.RemoteException;

	public String listEventAvailability(String eventType) throws java.rmi.RemoteException;

	public String bookEvent(String customerID, String eventID, String eventType) throws java.rmi.RemoteException;

	public void getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	public String cancelEvent(String customerID, String eventID) throws java.rmi.RemoteException;

} // end interface
