package com.nm.camerafx.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import com.nm.camerafx.model.CameraSize;
import com.nm.camerafx.utils.MathUtils;

public class CameraInfo2 {

	private static final String TAG = CameraInfo2.class.getSimpleName();

	private static final int MAX_WIDTH = 960;
	private static final int MAX_HEIGHT = 720;
	private static final int MAX_SIZE = MAX_WIDTH * MAX_HEIGHT;

	private static final double MAX_ASPECT_RATIO = 1.5;
	private static final double MIN_BUTTON_CONTAINER_RATIO = 0.15625;
	
	private static Camera camera;

	public CameraInfo2(){
		
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static void openCamera() {

		if (camera != null) return;
		
		try {
			camera = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}

	}

	public static void releaseCamera() {
		if (camera != null) {
			camera.release(); // release the camera for other applications
			camera = null;
		}
	}
	
	/** Check if this device has a camera */
	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}
	
	public static String getWhiteBalance(){
		return camera.getParameters().getWhiteBalance();
	}
	
	public static List<String> getSupportedWhiteBalance(){
		return camera.getParameters().getSupportedWhiteBalance();
	}


	public static int getExposureCompensation(){
		return camera.getParameters().getExposureCompensation();
	}
	
	public static List<Integer> getExposureCompensations(){
		if (camera == null) {
			return new ArrayList<Integer>();
		}
		int min = camera.getParameters().getMinExposureCompensation();
		int max = camera.getParameters().getMaxExposureCompensation();
		
		List<Integer> EV = new ArrayList<Integer>();
		for (int exposure = min; exposure <= max; exposure++) {
			EV.add(exposure);
		}
		
		return EV;
	}


	
	/**
	 * 
	 * @param screenLength screen size, longer side
	 * @param screenWidth screen size, shorter side
	 * @return
	 */
	public static List<CameraSize> getPreviewSizes2(int screenLength, int screenWidth) {

		if (camera == null)
			return null;

		List<Size> sizes = camera.getParameters().getSupportedPreviewSizes();
		List<CameraSize> allowedSizes = new ArrayList<CameraSize>();


		for (Size size : sizes) {

			double scaleFactor = screenWidth /  (double) MathUtils.getSmaller(size.width, size.height) ;
			int previewLength = (int) (MathUtils.getLarger(size.width, size.height) * scaleFactor);
			int diff = screenLength - previewLength;
			
			if (diff <=0 ) continue;
			
			double ratio = (double) diff / screenLength;
			if (diff < 100 || ratio < 0.15625) continue;
			

			if (size.width * size.height <= MAX_SIZE) {
				allowedSizes.add(new CameraSize(size.width, size.height));
			}
			
		}
		
		// sort sizes in ascending order
		Collections.sort(allowedSizes, new CameraSize());
		return allowedSizes;

	}
	
	public static List<CameraSize> getPreviewSizes() {
		// return camera.getParameters().getSupportedPreviewSizes();
		//openCamera();
		if (camera == null)
			return null;

		List<Size> sizes = camera.getParameters().getSupportedPreviewSizes();
		List<CameraSize> allowedSizes = new ArrayList<CameraSize>();

		int width;
		int height;
		double aspectRatio;

		for (Size size : sizes) {

			width = size.width;
			height = size.height;

			if (width * height <= MAX_SIZE) {
				if (width > height) {

					aspectRatio = (double) width / (double) height;
					aspectRatio = 0.0;
					
					if (aspectRatio < 1.4) {
						allowedSizes.add(new CameraSize(width, height));
					}
				}
			}

		}

		//releaseCamera();
		
		// sort sizes in ascending order
		Collections.sort(allowedSizes, new CameraSize());
		
/*		Collections.sort(allowedSizes, new Comparator<CameraSize>() {

			public int compare(final CameraSize a, final CameraSize b) {
				return a.width * a.height - b.width * b.height;
			}

		});*/

		return allowedSizes;

	}

}
