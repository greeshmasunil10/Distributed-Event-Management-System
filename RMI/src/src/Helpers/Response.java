package Helpers;

import java.io.Serializable;

public class Response implements Serializable {

	private String message;
	private boolean result;

	public Response(String p_message, boolean p_success) {
		this.message= p_message;
		this.result=p_success;
	}
	public void setMessage(String msg) {
		this.message=msg;
	}
	public void setResult(boolean res) {
		this.result=res;
	}
	public String getMessage() {
		return this.message;
	}
	public boolean getResult() {
		return this.result;
	}
}
