package com.nm.camerafx.camera;

import android.os.HandlerThread;
import android.util.Log;

public class CameraHandlerThread extends HandlerThread {

	private static String threadName;
	
	public CameraHandlerThread(String threadName) {
		super(threadName);
		this.threadName = threadName;
		
		// Log.d("CameraHandlerThread", "Thread: " + Thread.currentThread().getId());
	}
}
