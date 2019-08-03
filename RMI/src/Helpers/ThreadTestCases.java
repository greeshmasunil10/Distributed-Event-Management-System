package Helpers;

import java.util.Scanner;

import com.web.service.DEMSInterface;

public class ThreadTestCases {
	static Scanner sc = new Scanner(System.in);

	public ThreadTestCases() {
		// TODO Auto-generated constructor stub
	}

	
	public  void startThreads(DEMSInterface serverobj) {
		threads t = new threads();
		synchronized (t) {

			Runnable task1 = () -> {
				t.bookthread(serverobj);
			};
			Runnable task2 = () -> {
				t.swapthread1(serverobj);
			};
			Runnable task3 = () -> {
				t.swapthread2(serverobj);
			};
			Runnable task4 = () -> {
				t.schedthread(serverobj);
			};

//		synchronized(serverobj);
			Thread thread1 = new Thread(task1);
			Thread thread2 = new Thread(task2);
			Thread thread3 = new Thread(task3);
			Thread thread4 = new Thread(task4);
//		sc.nextLine();
//		sc.nextLine();
//		sc.nextLine();
//		sc.nextLine();
//			thread1.start();
			thread2.start();
			thread3.start();
			thread4.start();
		}
	}
class threads{
	private synchronized void bookthread(DEMSInterface serverobj) {
		System.out.println("\nThread booked starting....");
		String res = serverobj.bookEvent("TORC1234", "TORA110519", "Seminars");
		System.out.println(res);
	}

	private synchronized void swapthread1(DEMSInterface serverobj) {
		System.out.println("\nThread 1 starting..");
		serverobj.bookEvent("TORC1234", "TORA110519", "Seminars");

		String res = serverobj.swapEvent("TORC1234", "TORE110519", "Seminars", "TORA110519", "Seminars");
		System.out.println(res + " by thread 1!");
	}

	private synchronized void swapthread2(DEMSInterface serverobj) {
		System.out.println("\nThread 2 starting..");
		String res = serverobj.swapEvent("TORC1234", "TORM110519", "Seminars", "TORE110519", "Seminars");
		System.out.println(res + " by thread 2!");

	}

	private synchronized void schedthread(DEMSInterface serverobj) {
		String res = serverobj.getBookingSchedule("TORC1234");
		System.out.println(res);
	}
}

}
