package com.nm.camerafx.model;

public class RawMessage {

	
	public byte[] data;
	public long ptsNs;
	
	
	public RawMessage(byte[] data, long ptsNs) {
		this.data = data;
		this.ptsNs = ptsNs;
	}

}
