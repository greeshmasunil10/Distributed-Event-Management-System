package Model;

public class Messages {

	public static enum add {
		SUCCESS("Event is created"), CAP("Capacity Updated");
		public final String message;

		add(String message) {
			this.message = message;
		}
	}
	public static enum remove {
		SUCCESS("removed completely"), DOESNTEXIST("no such event");
		public final String message;
		
		remove(String message) {
			this.message = message;
		}
	}
	public static enum list {
		SUCCESS("Event is created"), CAP("Capacity Updated");
		public final String message;
		
		list(String message) {
			this.message = message;
		}
	}
	public static enum book {
		SUCCESS("Event is created"), CAP("Capacity Updated");
		public final String message;
		
		book(String message) {
			this.message = message;
		}
	}
	public static enum cancel {
		SUCCESS("Event is created"), CAP("Capacity Updated");
		public final String message;
		
		cancel(String message) {
			this.message = message;
		}
	}
	public static enum swap {
		SUCCESS("Event is created"), CAP("Capacity Updated");
		public final String message;
		
		swap(String message) {
			this.message = message;
		}
	}
	public static enum sched {
		SUCCESS("Event is created"), CAP("Capacity Updated");
		public final String message;
		
		sched(String message) {
			this.message = message;
		}
	}

	public static void main(String args[]) {
		System.out.println();
	}

}

