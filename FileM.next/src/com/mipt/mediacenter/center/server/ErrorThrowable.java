package com.mipt.mediacenter.center.server;
/**
 * 
 * @author fang
 *
 */
public class ErrorThrowable extends Throwable {

	private static final long serialVersionUID = 1L;
	
	private String message;

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}