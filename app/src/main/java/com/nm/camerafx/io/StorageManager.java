package com.nm.camerafx.io;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.nm.camerafx.VideoCameraActivity;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StorageManager {
	
	private static final String TAG = StorageManager.class.getSimpleName();
	
	private static final String ALBUM_NAME = "CameraFX NM";
	
	private static final String CAMERA_DIR = "/dcim/";
	//public static final int MEDIA_TYPE_IMAGE = 1;
	//public static final int MEDIA_TYPE_VIDEO = 2;
	
	private Context context;

	
	//private static File mediaFile = null;
	

	public static File getOutputMediaFile(int type){
		
	
		
		 // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.i("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", 
	    		java.util.Locale.getDefault()).format(new Date());
	    
	    File mediaFile;
	    if (type == VideoCameraActivity.MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == VideoCameraActivity.MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	
	public static String getOutputMediaFilePath(int type){
		
		
		
		 // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), ALBUM_NAME);
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.i(ALBUM_NAME, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", 
	    		java.util.Locale.getDefault()).format(new Date());
	    
	    File mediaFile;
	    if (type == VideoCameraActivity.MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == VideoCameraActivity.MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile.getAbsolutePath();
	}
	
	public static Uri addImageToGallery(Context context, byte[] data, String title, String description) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, title);
		values.put(MediaStore.Images.Media.DESCRIPTION, description);
		values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

		Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		try {
			if (uri != null) {
				OutputStream os = context.getContentResolver().openOutputStream(uri);
				if (os != null) {
					os.write(data);
					os.close();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error writing to output stream", e);
		}

	    return uri;
	}
}
