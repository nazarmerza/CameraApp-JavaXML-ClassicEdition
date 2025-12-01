package com.nm.camerafx.recorder;

import java.util.concurrent.BlockingQueue;

import com.nm.camerafx.model.RawMessage;

public interface Recorder {
	
	public void configure(BlockingQueue<RawMessage> queue);
	public void start();
	public void stop();
	public boolean isStopped();
	
	
}
