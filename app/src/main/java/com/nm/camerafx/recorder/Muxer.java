package com.nm.camerafx.recorder;

import java.io.IOException;

import com.nm.camerafx.model.EncodedMessage;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;



public class Muxer {
	
	private static String TAG = Muxer.class.getSimpleName();
	
	private MediaMuxer muxer;
	private boolean muxerStarted = false;
	private int trackCount = 0;
	
	public Muxer(String filePath){
		try {
            muxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }
	}
	
	public int addTrack(MediaFormat format) {
		int trackIndex = muxer.addTrack(format);
		trackCount++;
		return trackIndex;
	}
	
	public void start() {
		muxer.start();
		muxerStarted = true;	
	}
	
	public void stop() {
		
        muxer.stop();
        muxer.release();
        muxer = null;
        muxerStarted = false;
        
        //Log.d(TAG, "MediaMuxer stopped.");
    }


	public void writeSampleData(EncodedMessage message){
		if (muxerStarted)  {
			muxer.writeSampleData(message.trackIndex, message.buffer, message.bufferInfo);
		}
	}
	


}
