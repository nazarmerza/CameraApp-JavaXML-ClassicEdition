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
import android.net.Uri;
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

    private int displayOrientationDegrees = 0; // 0/90/180/270 for drawing


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

	public int getDisplayOrientation() {
		return displayOrientationDegrees;
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
            case Surface.ROTATION_0:   degrees = 0;   break;
            case Surface.ROTATION_90:  degrees = 90;  break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // mirror compensation (kept for completeness)
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);

// store for our own Canvas draw rotation
        displayOrientationDegrees = result;

		params.setWhiteBalance(whiteBalance);
		params.setSceneMode(Parameters.SCENE_MODE_AUTO);
		
		// to set maximum Exposure
        params.setExposureCompensation(exposure);
        params.setWhiteBalance(whiteBalance);
		
		params.setPreviewSize(cameraFrameSize.width, cameraFrameSize.height);
		params.setPreviewFormat(ImageFormat.NV21);
		params.set("cam_mode", 1);
		mCamera.setParameters(params);

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
        // No-op - we now draw in onDraw, which handles all aspect ratios
	}

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCamera == null || bitmap == null) {
            return;
        }

        // We’ll draw the decoded bitmap rotated by displayOrientationDegrees
        final int viewW = getWidth();
        final int viewH = getHeight();
        final int bmpW  = bitmap.getWidth();
        final int bmpH  = bitmap.getHeight();

        final boolean rotate90or270 = (displayOrientationDegrees == 90 || displayOrientationDegrees == 270);

        // Effective dimensions on screen after rotation (the bounding box)
        final float targetW = rotate90or270 ? bmpH : bmpW;
        final float targetH = rotate90or270 ? bmpW : bmpH;

        // Scale to fit inside view while preserving aspect
        final float scale = Math.min(viewW / targetW, viewH / targetH);

        canvas.save();
        // Draw centered
        canvas.translate(viewW * 0.5f, viewH * 0.5f);
        // Apply rotation around center
        canvas.rotate(displayOrientationDegrees);
        // Scale image
        canvas.scale(scale, scale);
        // Draw bitmap centered at origin
        canvas.drawBitmap(bitmap, -bmpW * 0.5f, -bmpH * 0.5f, null);
        canvas.restore();

        // Re-queue buffer for next frame
        mCamera.addCallbackBuffer(callbackBuffer1);
    }


	/**********************************************************************/
	// Taking picture

	/**
	 * @throws IOException
	 ********************************************************************/
	public Uri takePicture() throws IOException {

		isTakingPicture = true;

		if (DEBUG) Log.d(TAG, "takePicture() take picture set true");
		Bitmap picture = null;
		if (displayOrientationDegrees != 0) {
			
            Matrix matrix = new Matrix();
            matrix.postRotate(displayOrientationDegrees);
            picture = bitmap.copy(bitmap.getConfig(), false);
            
            picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
		} else {
			picture = bitmap.copy(bitmap.getConfig(), false);
			
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		picture.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] data = bos.toByteArray();

		String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
		Uri imageUri = StorageManager.addImageToGallery(context.getApplicationContext(), data, fileName, "some description");
		
		isTakingPicture = false;

		return imageUri;

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

	@Override
	public void onPreviewFrame(byte[] pData, Camera pCamera) {
		if (isTakingPicture) {
			postInvalidate();
		} else {
			decodeyuv420(bitmap, pData, colorFormat);

			postInvalidate();

			if (mode == VideoCameraActivity.MEDIA_TYPE_VIDEO) {

				if (isRecording) {

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

}
