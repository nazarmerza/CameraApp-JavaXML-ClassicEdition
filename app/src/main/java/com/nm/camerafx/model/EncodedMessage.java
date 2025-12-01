package com.nm.camerafx.model;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;

public class EncodedMessage {
	
	public int trackIndex;
	public ByteBuffer buffer;
	public MediaCodec.BufferInfo bufferInfo;
	
	public EncodedMessage(int trackIndex, ByteBuffer byteBuf,
			BufferInfo bufferInfo) {
		super();
		this.trackIndex = trackIndex;
		this.buffer = byteBuf;
		this.bufferInfo = bufferInfo;
	}


}
