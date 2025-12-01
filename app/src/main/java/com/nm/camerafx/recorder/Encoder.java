package com.nm.camerafx.recorder;

import android.media.MediaFormat;

public interface Encoder {

	// get codec-specific-data (CSD) format.
	// This format is available only after codec is fed some data
	public abstract MediaFormat getCsdMediaFormat();

	public abstract void setMuxer(Muxer muxer);

	public abstract void setTrackIndex(int index);

	public abstract void stop();

	public abstract boolean isStopped();

	void release();

}