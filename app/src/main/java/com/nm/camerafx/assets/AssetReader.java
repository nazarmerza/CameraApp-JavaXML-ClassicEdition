package com.nm.camerafx.assets;


import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class AssetReader {
	
	private AssetManager assetManager;
	
	
	
	public AssetReader(Context context) {
		this.assetManager = context.getAssets();
	}
	
	public Bitmap decode(String fileName) {
		InputStream istr = getInputStream(fileName);
		return BitmapUtils.decode(istr);
	}

	
	public Bitmap decodeResized(String fileName, int reqWidth, int reqHeight) {
		InputStream istr = getInputStream(fileName);
		return BitmapUtils.decodeResize(istr, reqWidth, reqHeight);
	}
	
	private InputStream getInputStream(String fileName) {
		InputStream istr = null;
		try {
			istr = assetManager.open(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return istr;
	}

}
