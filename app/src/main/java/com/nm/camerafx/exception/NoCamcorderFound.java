package com.nm.camerafx.exception;

public class NoCamcorderFound extends Exception {

	public static final String message = "No Camcorder was found.";
	
	public NoCamcorderFound() {
		// TODO Auto-generated constructor stub
	}

	public NoCamcorderFound(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public NoCamcorderFound(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public NoCamcorderFound(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
