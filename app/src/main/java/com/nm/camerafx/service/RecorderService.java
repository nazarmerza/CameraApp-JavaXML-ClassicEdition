package com.nm.camerafx.service;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.nm.camerafx.codecs.CodecGenerator;
import com.nm.camerafx.model.RawMessage;
import com.nm.camerafx.recorder.AudioRecorder;
import com.nm.camerafx.recorder.EncoderImpl;
import com.nm.camerafx.recorder.Muxer;
import com.nm.camerafx.recorder.Recorder;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RecorderService {

	private static String TAG = RecorderService.class.getSimpleName();
	private static boolean DEBUG = false;
	private static boolean VERBOSE = false;

	
	public static final int AUDIO_QUEUE_SIZE = 3;
	public static final int VIDEO_QUEUE_SIZE = 3;
	
	private BlockingQueue<RawMessage> audioRawBufferQueue = new ArrayBlockingQueue<RawMessage>(AUDIO_QUEUE_SIZE);
	private BlockingQueue<RawMessage> vidoeRawBufferQueue = new ArrayBlockingQueue<RawMessage>(VIDEO_QUEUE_SIZE);
	
	MediaFormat videoFormat;

	private AudioRecorder audioRecorder;
	private Recorder videoRecorder;
	
	private EncoderImpl videoEncoder;
	private EncoderImpl audioEncoder;
	
	private Muxer muxer;

	private Thread videoEncoderThread;
	private Thread audioEncoderThread;
	private Thread audioRecorderThread;
	
	/*
	 * Sets up recording pipleline and starts recording.
	 * Pipleline must be initialized, starting from the last step, which is muxer.
	 * Such that, when a component in the pipleline is started, all subsequent components 
	 * must be ready to accept and process input. 
	 */
	
	//private String currentOutputFile = null;
	
	public RecorderService() {
		
	}
	
/*	public String getCurrentOutputFilePath() {
		return currentOutputFile;
	}*/
		
	public void start(Recorder vRecorder, CodecGenerator cg, String currentOutputFile) {

		//currentOutputFile = StorageManager.getOutputMediaFilePath(StorageManager.MEDIA_TYPE_VIDEO);
		
		muxer = new Muxer(currentOutputFile);
		
		/*
		 *  Video recording stream
		 */
		
		videoRecorder = vRecorder;
		videoRecorder.configure(vidoeRawBufferQueue);
		
		MediaCodec videoCodec = null;
		try {
			videoCodec = cg.createVideoEncoder();
		} catch (IOException e) {
			Log.e(TAG, "Unable to create video encoder", e);
			return;
		}
		videoCodec.start();
		videoEncoder = new EncoderImpl(videoCodec, vidoeRawBufferQueue);
		
		
		// the actual format coming out of codec might be different from what the codec
		// was created with. Get this actual format.
		videoFormat = videoEncoder.getCsdMediaFormat();
		int videoTrackIndex = muxer.addTrack(videoFormat);
		videoEncoder.setMuxer(muxer);
		videoEncoder.setTrackIndex(videoTrackIndex);
		
		/*
		 *  Audio recording stream
		 */
		
		
		// Producer: records audio frames
		audioRecorder = new AudioRecorder(audioRawBufferQueue);
		
		//MediaFormat audioFormat = getAudioFormat();
		MediaCodec audioCodec = null;
		try {
			audioCodec = cg.createAudioEncoder();
		} catch (IOException e) {
			Log.e(TAG, "Unable to create audio encoder", e);
			return;
		}
		audioCodec.start();
		
		// Consumer: consumes audio frames
		audioEncoder = new EncoderImpl(audioCodec, audioRawBufferQueue);
		MediaFormat audioFormat = audioEncoder.getCsdMediaFormat();
		int audioTrackIndex = muxer.addTrack(audioFormat);
		audioEncoder.setMuxer(muxer);
		audioEncoder.setTrackIndex(audioTrackIndex);
		

		
		// components in the pipleline MUST be started from the end, 
		// that is the last component must be started first. Else, a producer
		// may produce something, while the consumer is not yet available.
		
		muxer.start();
		
		videoEncoderThread = new Thread(videoEncoder);
		videoEncoderThread.start();
		
		audioEncoderThread = new Thread(audioEncoder);
		audioEncoderThread.start();
		
		videoRecorder.start();
		
		audioRecorderThread = new Thread(audioRecorder);
		audioRecorderThread.start();

	}
	
	/**
	 * Stops video recording.
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException {

		/* Recording components must be stopped in order: starting from the begging of the 
		 * recording pipeline. That is for each consumer-producer pair, producer must be stopped 
		 * before the consumer. Otherwise, the process may crash.
		 */
		videoRecorder.stop();
		audioRecorder.stop();
		
		
		audioRecorderThread.join();
		
		videoEncoder.stop();
		audioEncoder.stop();
		
		videoEncoderThread.join();
		audioEncoderThread.join();
		
		// stop muxer
		muxer.stop();
		
		videoRecorder = null;
		audioRecorder = null;
		videoEncoder = null;
		audioEncoder = null;
		muxer = null;

	}

}
