package com.nm.camerafx.recorder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;
import android.util.Log;

import com.nm.camerafx.model.RawMessage;

public class AudioRecorder implements Runnable {
	// Logging
	public static final String TAG = AudioRecorder.class.getSimpleName();
	final boolean VERBOSE = false;


	
	// FramesPool holds a pool of already allocated audio buffer memory 
	/*private final LinkedBlockingQueue<RawMessage> framesPool 
	 		= new LinkedBlockingQueue<RawMessage>(FRAMES_POOL_SIZE); */
	private BlockingQueue<RawMessage> queue = null;
	 
	private long frameCount = 0;

	private AudioRecord audioReader;
	// AudioRecord parameters
	public static final int SAMPLE_RATE = 44100;
	public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int FRAMES_PER_BUFFER = 24; // 1 sec @ 1024
													// samples/frame (aac)
	// public static long US_PER_FRAME = 0;
	public int SAMPLES_PER_FRAME = 2048; // codec-specific

	private int bufferSize;
	//private byte[] data = new byte[SAMPLES_PER_FRAME];
	private int readResult;
	private RawMessage audioMessage;

	private boolean stopRequestReceived = false;
	private boolean isStopped = false;
	
	
	private final int FRAMES_POOL_SIZE = 20;
	private byte[][] framesPool = new byte[FRAMES_POOL_SIZE][];

	public AudioRecorder(BlockingQueue<RawMessage> queue) {
		this.queue = queue;
		prepare();
	}

	private void prepare() {

		int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				CHANNEL_CONFIG, AUDIO_FORMAT);
		bufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;

		// Ensure buffer is adequately sized for the AudioRecord
		// object to initialize
		if (bufferSize < minBufferSize) {
			bufferSize = ((minBufferSize / SAMPLES_PER_FRAME) + 1)
					* SAMPLES_PER_FRAME * 2;
		}
		
		 // create some audio buffers and pool them
/*		 for (int i = 0; i < FRAMES_POOL_SIZE; ++i) {
			 byte[] dummyBuffer = new byte[SAMPLES_PER_FRAME];
			 framesPool.add(new RawMessage(dummyBuffer, 0));
		 }*/
		
		 for (int i = 0; i < FRAMES_POOL_SIZE; ++i) {
			 framesPool[i] = new byte[SAMPLES_PER_FRAME];
		 }
		 
		 
		audioReader = new AudioRecord(MediaRecorder.AudioSource.MIC, // source
				SAMPLE_RATE, // sample rate, hz
				CHANNEL_CONFIG, // channels
				AUDIO_FORMAT, // audio format
				bufferSize); // buffer size (bytes)

	}

	
	public void stop() {
		stopRequestReceived = true;
	}

	public boolean isStopped() {
		return isStopped;
	}


	@Override
	public void run() {

		long ptsNs;
		audioReader.startRecording();
		boolean enqueueReslut = false;
		
		int framePoolIndex = 0;

		while (!stopRequestReceived) {

			// read a frame
			//byte[] data = new byte[SAMPLES_PER_FRAME];
			
			if (framePoolIndex >= FRAMES_POOL_SIZE) {
				framePoolIndex = 0;
			}
			byte[] data = framesPool[framePoolIndex];
			readResult = audioReader.read(data, 0, SAMPLES_PER_FRAME);

			ptsNs = System.nanoTime();
			frameCount++;

			if (VERBOSE) Log.d(TAG, data.length + "byte read");

			if (readResult == AudioRecord.ERROR_BAD_VALUE
					|| readResult == AudioRecord.ERROR_INVALID_OPERATION) {
				Log.e(TAG, "Read error");
				
			} else {
				// normal buffer, enque
				audioMessage = new RawMessage(data, ptsNs);
				enqueueReslut = queue.offer(audioMessage);
				framePoolIndex++;
				
				if (VERBOSE) {
					if (!enqueueReslut) Log.i(TAG, "Audio queue is full: frame count = " + frameCount );
				}
			}

		}
		if (audioReader != null) {
			// audioReader.setRecordPositionUpdateListener(null);
			audioReader.release();
			audioReader = null;
			if (VERBOSE) Log.i(TAG, "Audio Recorder stopped");
		}
		
		isStopped = true;

	}



}
