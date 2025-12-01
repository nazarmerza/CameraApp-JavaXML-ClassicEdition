package com.nm.camerafx.codecs;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;

public class Capabilities {
	
	private static String TAG = Capabilities.class.getSimpleName();
	private static final boolean VERBOSE = false; // lots of logging
	
	
	
	private String arrToString(int[] arr) {
		String str = "";
		for (int i = 0; i < arr.length; i++) {
			str = str +  arr[i] + ", ";
		}
		
		return str;
	}
	
	private void setCodecCapabilities() {

		int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
        	
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }

            String[] types = info.getSupportedTypes();
            //CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
            for (int j = 0; j < types.length; j++) {
            	MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(types[j]);
            	//if (types[j] != "video/avc") continue;
            	Log.i(TAG, "Codec Type: " + types[j]);
            	Log.i(TAG, "Required Type: " + MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888);
            	Log.i(TAG, "Color format supported: " + arrToString(caps.colorFormats));
            	/*
	            for (CodecProfileLevel profileLevel : caps.profileLevels) {
	                if (profileLevel.profile == profile
	                        && profileLevel.level >= level) {
	                    return true;
	                }
	            }
	            */
            }
        }

	}

}
