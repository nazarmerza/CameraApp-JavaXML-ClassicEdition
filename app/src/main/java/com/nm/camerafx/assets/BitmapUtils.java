package com.nm.camerafx.assets;

import java.io.File;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.util.Log;

public  class BitmapUtils {
	private static final String TAG = BitmapUtils.class.getSimpleName();
	private static final boolean VERBOSE = false;
	
	
	
	/*
	 * Decode bitmap with the original size
	 */
	public static Bitmap decode(InputStream istr){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options = setOptions(options);
		return BitmapFactory.decodeStream(istr);
		
	}
	
	/*
	 *  Load and resize bitmap EXACTLY as the given size
	 */
	public static Bitmap decodeResize(InputStream istr,  int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options = setOptions(options);
		//options.outWidth = reqWidth;
		//options.outHeight = reqHeight;
		

		Bitmap bitmap = BitmapFactory.decodeStream(istr, null, options);
		bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
		return bitmap;
	}
	
	
	
	/* Methods for reading from file system */
	public static Bitmap decode(File file) {
		return null;
	}
	
	/*
	 * Load and rescale image, with size closest to the given width and height
	 */
	public static Bitmap decodeRescale(File file, int reqWidth, int reqHeight) {
		
		// find bitmap dimensions
		BitmapFactory.Options options = getOptions(file);
		if (VERBOSE) Log.d(TAG, "original size: width = " + options.outWidth + " height = " + options.outHeight);
		
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// now set options for loading
		options = setOptions(options);
		return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		
	}
	
	public static int LONG_960 = 960;
	public static int SHORT_720 = 720;
	
	public static Bitmap decodeResize(File file, int reqWidth, int reqHeight) {
		// find bitmap dimensions
		BitmapFactory.Options options = getOptions(file);
		if (VERBOSE) Log.d(TAG, "original size: width = " + options.outWidth + " height = " + options.outHeight);
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;
		
		
		double aspectRatio = (imageWidth > imageHeight) ? imageWidth / imageHeight : imageHeight / imageWidth;
		if (imageWidth > SHORT_720) {
			imageHeight = (int) (((float) SHORT_720 / (float)imageWidth) * imageHeight);
			imageWidth = SHORT_720;
			
		}
		
		
		// now set options for loading
		options = setOptions(options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeFile(file.getAbsolutePath(), options), 
				imageWidth, imageHeight, false);
	
		
	}
	
	/* Private methods */
	
	
	
	 private static BitmapFactory.Options getOptions(File file) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		return options;
	 }
	 
	 private static BitmapFactory.Options getOptions(InputStream istr) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(istr, null, options);
		return options;
	 }
	 
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		
		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	
	private static BitmapFactory.Options setOptions(BitmapFactory.Options options) {
		//options.inSampleSize = 4;
		//options.inMutable = true;
		options.inDither = false;
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[32 * 1024];
		return options;
	}
	
	


}
