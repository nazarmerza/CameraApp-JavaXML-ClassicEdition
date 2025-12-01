package com.nm.camerafx.codecs;

import android.media.CamcorderProfile;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;

public class CamcorderProfileInfo {

	private static String TAG = CamcorderProfileInfo.class.getSimpleName();


	public static void printCamcorderInfo() {
		Log.i(TAG, "CamcorderProfile");
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
			Log.i(TAG, "Profile: " + "QUALITY_1080P");
			printProfileInfo(CamcorderProfile.QUALITY_1080P);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
			Log.i(TAG, "Profile: " + "QUALITY_480P");
			printProfileInfo(CamcorderProfile.QUALITY_480P);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
			Log.i(TAG, "Profile: " + "QUALITY_720P");
			printProfileInfo(CamcorderProfile.QUALITY_720P);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
			Log.i(TAG, "Profile: " + "QUALITY_CIF");
			printProfileInfo(CamcorderProfile.QUALITY_CIF);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
			Log.i(TAG, "Profile: " + "QUALITY_HIGH");
			printProfileInfo(CamcorderProfile.QUALITY_HIGH);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
			Log.i(TAG, "Profile: " + "QUALITY_LOW");
			printProfileInfo(CamcorderProfile.QUALITY_LOW);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF)) {
			Log.i(TAG, "Profile: " + "QUALITY_QCIF");
			printProfileInfo(CamcorderProfile.QUALITY_QCIF);
			
		} 
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
			Log.i(TAG, "Profile: " + "QUALITY_QVGA");
			printProfileInfo(CamcorderProfile.QUALITY_QVGA);
			
		}

	}

	private static void printProfileInfo(Integer quality) {
		CamcorderProfile profile = CamcorderProfile.get(quality);
		Log.i(	TAG,	"fileFormat	:" + 	profile.fileFormat	);
		Log.i(	TAG,	"quality	:"	+ profile.quality	);
		Log.i(	TAG,	"videoBitRate	:"	+ profile.videoBitRate	);
		Log.i(	TAG,	"videoCodec	:"	+ profile.videoCodec	);
		Log.i(	TAG,	"videoFrameHeight	:"	+ profile.videoFrameHeight	);
		Log.i(	TAG,	"videoFrameRate	:"	+ profile.videoFrameRate	);
		Log.i(	TAG,	"videoFrameWidth	:" +	profile.videoFrameWidth	);

	}
	
	private static String arrToString(int[] arr) {
		String str = "";
		for (int i = 0; i < arr.length; i++) {
			str = str +  arr[i] + ", ";
		}
		
		return str;
	}
	public static void getCodecCapabilities() {

		Log.i(TAG, "CodecCapabilities: ");
		int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
        	
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            
            if (!info.isEncoder()) {
                continue;
            }

            String[] types = info.getSupportedTypes();
           
            for (int j = 0; j < types.length; j++) {
            	MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(types[j]);
            	
            	//if (!types[j].startsWith("video/") )continue;
            	
            	Log.i(TAG, "Codec Type: " + types[j]);

            	for (int k = 0; k < caps.colorFormats.length; k++) {
            		
            		Log.i(TAG, "Color format : " + getColorFormatName(caps.colorFormats[k]));
            	}
            	
            }
        }

	}
	
	private static String getColorFormatName(int colorFormatConst) {
		
		String colorFormat = null;
		switch(colorFormatConst){
		
	
			
		case MediaCodecInfo.CodecCapabilities.COLOR_Format12bitRGB444 : colorFormat = "COLOR_Format12bitRGB444"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB1555 : colorFormat = "COLOR_Format16bitARGB1555"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB4444 : colorFormat = "COLOR_Format16bitARGB4444"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitBGR565 : colorFormat = "COLOR_Format16bitBGR565"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitRGB565 : colorFormat = "COLOR_Format16bitRGB565"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format18BitBGR666 : colorFormat = "COLOR_Format18BitBGR666"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format18bitARGB1665 : colorFormat = "COLOR_Format18bitARGB1665"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format18bitRGB666 : colorFormat = "COLOR_Format18bitRGB666"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format19bitARGB1666 : colorFormat = "COLOR_Format19bitARGB1666"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24BitABGR6666 : colorFormat = "COLOR_Format24BitABGR6666"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24BitARGB6666 : colorFormat = "COLOR_Format24BitARGB6666"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24bitARGB1887 : colorFormat = "COLOR_Format24bitARGB1887"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24bitBGR888 : colorFormat = "COLOR_Format24bitBGR888"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888 : colorFormat = "COLOR_Format24bitRGB888"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format25bitARGB1888 : colorFormat = "COLOR_Format25bitARGB1888"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888 : colorFormat = "COLOR_Format32bitARGB8888"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format32bitBGRA8888 : colorFormat = "COLOR_Format32bitBGRA8888"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format8bitRGB332 : colorFormat = "COLOR_Format8bitRGB332"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY : colorFormat = "COLOR_FormatCbYCrY"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatCrYCbY : colorFormat = "COLOR_FormatCrYCbY"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL16 : colorFormat = "COLOR_FormatL16"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL2 : colorFormat = "COLOR_FormatL2"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL24 : colorFormat = "COLOR_FormatL24"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL32 : colorFormat = "COLOR_FormatL32"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL4 : colorFormat = "COLOR_FormatL4"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL8 : colorFormat = "COLOR_FormatL8"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatMonochrome : colorFormat = "COLOR_FormatMonochrome"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer10bit : colorFormat = "COLOR_FormatRawBayer10bit"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bit : colorFormat = "COLOR_FormatRawBayer8bit"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bitcompressed : colorFormat = "COLOR_FormatRawBayer8bitcompressed"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface : colorFormat = "COLOR_FormatSurface"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYCbYCr : colorFormat = "COLOR_FormatYCbYCr"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYCrYCb : colorFormat = "COLOR_FormatYCrYCb"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411PackedPlanar : colorFormat = "COLOR_FormatYUV411PackedPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411Planar : colorFormat = "COLOR_FormatYUV411Planar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar : colorFormat = "COLOR_FormatYUV420PackedPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar : colorFormat = "COLOR_FormatYUV420PackedSemiPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar : colorFormat = "COLOR_FormatYUV420Planar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar : colorFormat = "COLOR_FormatYUV420SemiPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedPlanar : colorFormat = "COLOR_FormatYUV422PackedPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar : colorFormat = "COLOR_FormatYUV422PackedSemiPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar : colorFormat = "COLOR_FormatYUV422Planar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422SemiPlanar : colorFormat = "COLOR_FormatYUV422SemiPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV444Interleaved : colorFormat = "COLOR_FormatYUV444Interleaved"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar : colorFormat = "COLOR_QCOM_FormatYUV420SemiPlanar"; break;
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar : colorFormat = "COLOR_TI_FormatYUV420PackedSemiPlanar"; break;
		default:	break;
		}
		
		return colorFormat;
		
	}

}
