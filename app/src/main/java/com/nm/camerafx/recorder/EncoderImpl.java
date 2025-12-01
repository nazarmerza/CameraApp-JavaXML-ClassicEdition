package com.nm.camerafx.recorder;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.nm.camerafx.model.EncodedMessage;
import com.nm.camerafx.model.RawMessage;

public class EncoderImpl implements Runnable, Encoder {

	private final boolean VERBOSE = false;
	private String TAG;

	private BlockingQueue<RawMessage> inputQueue = null;

	//
	private MediaCodec codec = null;
	private MediaFormat csdFormat = null;
	private boolean csdFormatSet = false;
	private int trackIndex = -1;

	private Muxer muxer = null;
	// private boolean muxerStarted = false;

	private boolean eosReceived = false;
	private boolean eosSeen = false;

	private long inputFrameCounter = 0;
	// private boolean endOfStream = false;

	private int TIMEOUT_USEC = 10;

	private long startTime;

	public EncoderImpl(MediaCodec codec, BlockingQueue<RawMessage> inputQueue) {
		this.codec = codec;
		this.inputQueue = inputQueue;

		TAG = codec.getName();

	}

	// get codec-specific-data (CSD) format.
	// This format is available only after codec is fed some data
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nm.camerav5.recorder.Encoder#getCsdMediaFormat()
	 */
	@Override
	public MediaFormat getCsdMediaFormat() {

		// Generate some random data and fet it into codec
		// MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int ascPollCount = 0;
		byte[] aubuf = new byte[1000]; // Garbage data

		// feed some garbase data into encoder
		while (ascPollCount < 10) {
			RawMessage rawMessage = new RawMessage(aubuf, 0);

			// feed data into encoder
			enqueueData(rawMessage);
			dequeueData();
			// check codec output
			if (csdFormatSet) {
				break;
			}
		}

		// Remove garbage data from codec
		codec.flush();

		// set state back to initial
		inputFrameCounter = 0;
		eosReceived = false;
		eosSeen = false;

		return csdFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nm.camerav5.recorder.Encoder#setMuxer(com.nm.camerav5.recorder.Muxer)
	 */
	@Override
	public void setMuxer(Muxer muxer) {
		this.muxer = muxer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nm.camerav5.recorder.Encoder#setTrackIndex(int)
	 */
	@Override
	public void setTrackIndex(int index) {
		this.trackIndex = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nm.camerav5.recorder.Encoder#stop()
	 */
	@Override
	public void stop() {
		eosReceived = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nm.camerav5.recorder.Encoder#isStopped()
	 */
	@Override
	public boolean isStopped() {
		return eosSeen;
	}

	@Override
	public void release() {
		codec.stop();
		codec.release();
		codec = null;
	}

	@Override
	public void run() {

		RawMessage message;
		// loop until end of stream signal is received.
		// while (!eosReceived) {
		while (!eosReceived) {

			message = inputQueue.poll();
			if (message != null) {
				enqueueData(message);
				dequeueData();
			} else {
				if (VERBOSE) {
					Log.d(TAG, "input queue is empty.");
				}
			}

		}
		
		// wait until codec finishes all its frames
		while (!eosSeen);
		
		// now it is safe to stop codec
		if (codec != null) {
			codec.stop();
			codec.release();
			codec = null;
		}
	}



	private void enqueueData(RawMessage msg) {

		// RawMessage audioMessage = (RawMessage) msg.obj;
		byte[] input = msg.data;
		long ptsUs = calculatePtsUs(msg.ptsNs);

		ByteBuffer[] inputBuffers = codec.getInputBuffers();
		int inputBufferIndex = codec.dequeueInputBuffer(-1);

		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();

			if (eosReceived) {
				// encoder.signalEndOfInputStream();
				codec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
						MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				// endOfStream = true;
			} else {
				inputBuffer.put(input);
				codec.queueInputBuffer(inputBufferIndex, 0, input.length,
						ptsUs, 0);
			}
		}

		inputFrameCounter++;
	}

	private void dequeueData() {
		ByteBuffer[] outputBuffers = codec.getOutputBuffers();
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

		while (true) {

			int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo,
					TIMEOUT_USEC);

			if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
				if (!eosReceived) {
					// we are done
					eosSeen = true;
					break;
				} else {
					// we are looping until new
				}
			} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				// not expected for an encoder
				outputBuffers = codec.getOutputBuffers();

			} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

				// should happen before receiving buffers, and should only
				// happen once
				if (csdFormatSet) {
					//throw new RuntimeException("format changed twice");
                    continue; // format changed twice, ignore the second one
				}

				// now that we have the Magic Goodies, start the muxer
				csdFormat = codec.getOutputFormat();
				if (VERBOSE) Log.d(TAG, "encoder output format changed: " + csdFormat);

				csdFormatSet = true;
				break;

			} else if (outputBufferIndex < 0) {
				if (VERBOSE) Log.d(TAG,
						"unexpected result from encoder.dequeueOutputBuffer: "
								+ outputBufferIndex);
				// let's ignore it
			} else {

				// get encoded data
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

				if (outputBuffer == null) {
					throw new RuntimeException("encoderOutputBuffer "
							+ outputBufferIndex + " was null");
				}

				if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					// The codec config data was pulled out and fed to the
					// muxer
					// when we got
					// the INFO_OUTPUT_FORMAT_CHANGED status. Ignore it.
					if (VERBOSE)
						Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
					bufferInfo.size = 0;
				}

				if (bufferInfo.size != 0) {

					// adjust the ByteBuffer values to match BufferInfo (not
					// needed?)
					outputBuffer.position(bufferInfo.offset);
					outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
					if (csdFormatSet) {
						muxer.writeSampleData(new EncodedMessage(trackIndex,
								outputBuffer, bufferInfo));
	
					}
				}

				codec.releaseOutputBuffer(outputBufferIndex, false);

				if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (!eosReceived) {
						Log.e(TAG, "reached end of stream unexpectedly");
					} else {
						if (VERBOSE)
							Log.d(TAG, "end of stream reached");
					}
					break; // out of while
				}
			} // else
		}
	}

	private long calculatePtsUs(long ptsNs) {

		if (inputFrameCounter == 0) {
			startTime = ptsNs;
			return 0;

		} else {
			return (ptsNs - startTime) / 1000;
		}

	}

}
