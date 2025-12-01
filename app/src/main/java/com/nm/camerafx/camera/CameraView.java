package com.nm.camerafx.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nm.camerafx.VideoCameraActivity;
import com.nm.camerafx.io.StorageManager;
import com.nm.camerafx.model.CameraSize;
import com.nm.camerafx.model.RawMessage;
import com.nm.camerafx.recorder.Recorder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class CameraView extends SurfaceView implements Camera.PreviewCallback,
		SurfaceHolder.Callback, Recorder {
	
	/* Native */
	static {
		System.loadLibrary("livecamera");
	}

	public native void decodeyuv420(Bitmap pTarget, byte[] pSource, int format);
//	public native void decodeyuv420spv2(Bitmap pTarget, byte[] pSource);
//	public native void decode(Bitmap pTarget, byte[] pSource);
	

	private static final String TAG = CameraView.class.getSimpleName();
	//private static final boolean VERBOSE = false;
	private static final boolean DEBUG = false;

	private int mode;

	private Context context;

	private boolean isRecording = false;
	private boolean isTakingPicture = false;

	// camera parameters
	private int orientation = -1;
	private int exposure;
	private String whiteBalance;
	
	private Camera mCamera;
	private int cameraId;

	private byte[] callbackBuffer1;
	private byte[] callbackBuffer2;

	private Bitmap bitmap;
	//private Paint mPaint;

	//private Paint fpsPaint;
	private Rect src;
	private Rect dest;

	private CameraHandlerThread mCameraThread = null;
	private Handler mCameraHandler = null;
	private final Object lock = new Object();

	public int colorFormat;
	private CameraSize surfaceViewSize;
	private CameraSize cameraFrameSize;

	private BlockingQueue<RawMessage> queue = null;
	private SurfaceTexture surfaceTexture = new SurfaceTexture(10);
	private final Activity activity;

	public CameraView(Context context, Activity activity, int cameraId, CameraSize selectedSize,
			int colorFormat) {
		super(context);

		this.context = context;
		this.activity = activity;
		this.cameraId = cameraId;
		this.cameraFrameSize = selectedSize;
		this.colorFormat = colorFormat;

		getHolder().addCallback(this);
		setWillNotDraw(false);
		
	}

	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public void setExposure(int exposure){
		this.exposure = exposure;
	}
	

	public void setWhiteBalance(String whiteBalance) {
		this.whiteBalance = whiteBalance;
		
	}
	public Camera getCamera() {
		return mCamera;
	}

	/**
	 * Opens a camera. Camera is opened on a separate thread, in order to
	 * receive camera frames on this, separate, thread, instead of the main/ui
	 * thread.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if (DEBUG) Log.d(TAG, "surfaceCreated()");
		if (mCameraThread == null) {

			// create and start new thread
			mCameraThread = new CameraHandlerThread("CAMERA_THREAD_NAME");

			mCameraThread.start();
			mCameraHandler = new Handler(mCameraThread.getLooper());

			if (DEBUG) Log.d(TAG, "CameraHandlerThread created");

			mCameraHandler.post(new Runnable() {
				@Override
				public void run() {

					try {
						mCamera = Camera.open(cameraId);
						if (DEBUG) Log.d(TAG, "Camera opened on another thread.");

					} catch (Exception e) {

						Log.e(TAG,
								"! Camera is not available (in use or does not exist)");
					}
					synchronized (lock) {
						lock.notify();
						if (DEBUG) Log.d(TAG, "notify() called");
					}
				}
			});
		}

		if (mCamera == null) {
			try {
				synchronized (lock) {
					if (DEBUG) Log.d(TAG, "wait() being called");
					lock.wait();
				}

			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage(), e);

			}
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		if (DEBUG) Log.d(TAG, "surfaceChanged()");
		surfaceViewSize = new CameraSize(width, height);

		mCamera.stopPreview();

		if (DEBUG) Log.d(TAG, " Selected Resolution: w = " + cameraFrameSize.width
				+ ", h = " + cameraFrameSize.height);

		PixelFormat pixelFormat = new PixelFormat();
		PixelFormat.getPixelFormatInfo(mCamera.getParameters()
				.getPreviewFormat(), pixelFormat);
		int frameByteSize = cameraFrameSize.width * cameraFrameSize.height
				* pixelFormat.bitsPerPixel / 8;

		// byte buffer to capture raw camera snapshot
		callbackBuffer1 = new byte[frameByteSize];
		callbackBuffer2 = new byte[frameByteSize];

		// holds conversion result
		bitmap = Bitmap.createBitmap(cameraFrameSize.width,
				cameraFrameSize.height, Bitmap.Config.ARGB_8888);

		// calculate scaled preview size
		setPreviewSize(cameraFrameSize, width, height);

		setCameraParams();

		// ///////////////
		mCamera.startPreview();

	}

	/**
	 * Releases resources when surface is destroyed
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		if (DEBUG) Log.d(TAG, "surfaceDestroyed()");
		stopCamera();
	}

	public void stopCamera() {

		if (DEBUG) Log.d(TAG, "stopCamera()");
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
			callbackBuffer1 = null;
			bitmap = null;
		}

		if (mCameraThread != null) {

			if (DEBUG) Log.d(TAG, "HandlerThread:" + mCameraThread.getName());

			// stop sending frames to
			mCameraThread.quit();
			mCameraHandler.removeCallbacksAndMessages(null);
			mCameraThread.interrupt();
			try {
				mCameraThread.join();
			} catch (InterruptedException e) {
				Log.e(TAG, "Exception while inerrupting camera thread:", e);

			}
			mCameraThread = null;
			if (DEBUG) Log.d(TAG, "Camera thread stopped.");

		}
	}

	

	private void setCameraParams() {
		// set camera parameters
		Camera.Parameters params = mCamera.getParameters();

		android.hardware.Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		mCamera.setDisplayOrientation(result);

		params.setWhiteBalance(whiteBalance);
		params.setSceneMode(Parameters.SCENE_MODE_AUTO);
		
/*		int index = params.getMinExposureCompensation ();
		index = (params.getMaxExposureCompensation () - index) / 2;
		float step = params.getExposureCompensationStep();*/
		
		// to set maximum Exposure
        params.setExposureCompensation(exposure);
        params.setWhiteBalance(whiteBalance);
		
		params.setPreviewSize(cameraFrameSize.width, cameraFrameSize.height);
		params.setPreviewFormat(ImageFormat.NV21);
		// params.setPreviewFormat(ImageFormat.YV12);
		params.set("cam_mode", 1);
		// params.set("orientation", "landscape");
		// params.setRotation(90);
		mCamera.setParameters(params);

		// requestLayout();

		// attach byte buffer to camera
		try {
			mCamera.setPreviewTexture(surfaceTexture);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		mCamera.addCallbackBuffer(callbackBuffer1);
		mCamera.addCallbackBuffer(callbackBuffer2);
		mCamera.setPreviewCallbackWithBuffer(this);
	}

	public void stopPreview() {
		mCamera.stopPreview();

	}

	public void startPreview() {
		setCameraParams();
		mCamera.startPreview();

	}

	private void setPreviewSize(CameraSize actualFrameSize, int width, int height) {
		
		src = new Rect(0, 0, actualFrameSize.width, actualFrameSize.height);
		// now recalculate position values
		double scaleFactor = getSizeScale(actualFrameSize.width, actualFrameSize.height, width, height);
		
		int scaledWidth = (int) (actualFrameSize.width * scaleFactor);
		int sclaedHeight = (int) (actualFrameSize.height * scaleFactor);
		dest = new Rect(0, 0, scaledWidth, sclaedHeight);
		

	}

	// both sizes must have the same orientation
	private double getSizeScale(int w, int h, int W, int H) {

		double ar1 = (double) W / (double) w;
		double ar2 = (double) H / (double) h;

		if (ar1 < ar2) {
			return ar1;
		}

		return ar2;

	}


	//boolean test2 = true;

	@Override
	protected void onDraw(Canvas canvas) {

		if (mCamera != null) {
			canvas.drawBitmap(bitmap, src, dest, null);
			//canvas.drawBitmap(bitmap, 0, 0, mPaint);
			mCamera.addCallbackBuffer(callbackBuffer1);
		}
	}

	/**********************************************************************/
	// Taking picture

	/**
	 * @throws IOException
	 ********************************************************************/
	public String takePicture(String filePath) throws IOException {

		// taking picture 
		isTakingPicture = true;
		//while(takePictureSeenOnPreviewFrame != true ); //&& takePictureSeenOnDraw != true);

		if (DEBUG) Log.d(TAG, "takePicture() take picture set true");
		Bitmap picture = null;
		if (VideoCameraActivity.mOrientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            picture = bitmap.copy(bitmap.getConfig(), false);
            
            picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
		} else {
			picture = bitmap.copy(bitmap.getConfig(), false);
			
		}

		
		File file = new File(filePath);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		picture.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] data = bos.toByteArray();
		
		String fileName = file.getName();
		StorageManager.addImageToGallery(context.getApplicationContext(), data, fileName, "some description");
		
		isTakingPicture = false;

		

		//isTakingPicture = false;
		return filePath;

	}
	


	/**********************************************************************/
	// RECORDER
	/**********************************************************************/

	@Override
	public void configure(BlockingQueue<RawMessage> queue) {
		this.queue = queue;
	}

	@Override
	public void start() {

		isRecording = true;

	}

	@Override
	public void stop() {
		isRecording = false;

	}

	@Override
	public boolean isStopped() {
		return !isRecording;

	}

	boolean test1 = true;
	boolean display = true;

	@Override
	public void onPreviewFrame(byte[] pData, Camera pCamera) {
		// decode(mBackBuffer, pData);
		
		
		
		if (isTakingPicture) {
			//if (DEBUG) Log.d(TAG, "onPreviewFrame() take picture mode");
			//takePictureSeenOnPreviewFrame = true;
			postInvalidate();
		} else {
			//if (DEBUG) Log.d(TAG, "onPreviewFrame() working on frame");
			decodeyuv420(bitmap, pData, colorFormat);

			postInvalidate();

			//frameCounter++;
			if (mode == VideoCameraActivity.MEDIA_TYPE_VIDEO) {

				if (isRecording) {

					// get array byte from bitmap
					// setBitmapByteArray();
					long ptsNs = System.nanoTime();
					RawMessage rawMessage = new RawMessage(pData, ptsNs);
					boolean result = queue.offer(rawMessage);

				
						if (result == false) {
							if (DEBUG) Log.d(TAG, "onPreviewFrame: queue is full");
						}
					

				}
			}
		}
		
		

	}


/*
	private final double NANOS = 1000000000.0;
	private int frameCounter = 0;
	private int MAX_FRAME_COUNT = 100;
	private long lastTime = 0;

	private List<Long> times = new ArrayList<Long>();

	// Calculates and returns frames per second 
	private Double fps() {

		// initialize first time
		if (lastTime == 0) {
			lastTime = System.nanoTime();
		}

		double seconds = (System.nanoTime() - lastTime) / NANOS;
		Double fps = MAX_FRAME_COUNT / seconds;
		lastTime = System.nanoTime();

		return fps;
	}*/





}
