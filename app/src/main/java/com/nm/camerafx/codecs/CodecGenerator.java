package com.nm.camerafx.codecs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.nm.camerafx.exception.NoAudioEncoderFoundException;
import com.nm.camerafx.exception.NoCamcorderFound;
import com.nm.camerafx.exception.NoVideoCodecFoundException;
import com.nm.camerafx.model.CameraSize;

public class CodecGenerator {

	private static String TAG = CodecGenerator.class.getSimpleName();
	private static final boolean VERBOSE = false;

	private static final int MAX_CAMCORDER_QUALITY = CamcorderProfile.QUALITY_1080P;
	private static final int MIN_CAMCORDER_QUALITY = CamcorderProfile.QUALITY_LOW;

	private static final int MAX_RESOLUTION = 1080;
	private static final int MIN_RESOLUTION = 144;

	private static final int[] resolutions = new int[] {MIN_RESOLUTION, 240, 288, 480, 720, MAX_RESOLUTION, 999};


	private static final String VIDEO_MIME = "video/";
	private static final String AUDIO_MIME = "audio/";

	private static final String[] supportedVideoTypes = { "video/avc",
			"video/mp4v-es", "video/3gp" };

	private static int[] supportedColorFormats = {
			MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
			MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar };

	private VideoCodecInfo vci;
	private CamcorderProfile profile;
	private String audioMimeType;
	private CameraSize size;

	public CodecGenerator(CameraSize size) throws NoVideoCodecFoundException,
			NoCamcorderFound, NoAudioEncoderFoundException {
		//this.size = size;

		vci = getSupportedVideoCodecInfo();
		if (vci == null) {
			throw new NoVideoCodecFoundException("No video codec was found");
		}

		/*int smallerSide = getSmaller(size.width, size.height);
		int camcorderQuality = getCamcorderProfileQuality(smallerSide);

		if (camcorderQuality < 0
				|| !CamcorderProfile.hasProfile(camcorderQuality)) {
			throw new NoCamcorderFound("Camcorder was not found for this size");
		}


		profile = getCamcorderProfile();
		if (profile == null) {
			throw new NoCamcorderFound("Camcorder was not found for this size");
		}
		*/

		audioMimeType = getSupportedAudioCodecInfo();
		if (audioMimeType == null) {
			throw new NoAudioEncoderFoundException(
					"Audio encoder codec was not found.");
		}
	}

	public void setSize(CameraSize size) throws NoCamcorderFound {
		this.size = size;
		profile = getCamcorderProfile();
		if (profile == null) {
			throw new NoCamcorderFound("Camcorder was not found for this size");
		}
	}
	public MediaCodec createAudioEncoder() throws IOException {

		//CamcorderProfile profile = getCamcorderProfile();
		
		MediaFormat format = createAudioFormat(audioMimeType, profile);
		MediaCodec codec = createEncoder(format);
		return codec;
	}

	private MediaFormat createAudioFormat(String mimeType,
			CamcorderProfile profile) {

		MediaFormat mediaFormat = new MediaFormat();
		mediaFormat.setString(MediaFormat.KEY_MIME, mimeType);
		mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
				MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE,
				profile.audioSampleRate);
		mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, profile.audioBitRate);
		mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);
		return mediaFormat;
	}

	public MediaCodec createVideoEncoder() throws IOException {

		//CamcorderProfile profile = getCamcorderProfile(size);
		
		MediaFormat videoFormat = createVideoFormat(vci.mimeType, vci.colorFormat, size, profile);

		MediaCodec codec = createEncoder(videoFormat);

		return codec;

	}
	
	private MediaFormat createVideoFormat(String type, int colorFormat,
			CameraSize size, CamcorderProfile profile) {

		MediaFormat mediaFormat = MediaFormat.createVideoFormat(type,
				size.width, size.height);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, profile.videoBitRate);
		
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
				profile.videoFrameRate);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		return mediaFormat;
	}

	private MediaCodec createEncoder(MediaFormat format) throws IOException {
		String mime = format.getString(MediaFormat.KEY_MIME);
		MediaCodec codec = MediaCodec.createEncoderByType(mime);
		codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		return codec;
	}



	private VideoCodecInfo getSupportedVideoCodecInfo() {

		if (VERBOSE) Log.d(TAG, "CodecCapabilities: ");
		int numCodecs = MediaCodecList.getCodecCount();
		VideoCodecInfo vci = null;

		for (int i = 0; i < numCodecs; i++) {

			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

			if (info.isEncoder()) {

				String[] types = info.getSupportedTypes();

				for (int j = 0; j < types.length; j++) {

					if (types[j].startsWith("video/")) {

						if (VERBOSE) Log.d(TAG, "Codec Type: " + types[j]);

						MediaCodecInfo.CodecCapabilities caps = info
								.getCapabilitiesForType(types[j]);

						for (int k = 0; k < caps.colorFormats.length; k++) {
							if (isRecognizedFormat(caps.colorFormats[k])) {

								vci = new VideoCodecInfo(types[j],
										caps.colorFormats[k]);
								return vci;
							}
						}
					}

				}

			} // if encoder

		}

		return vci;

	}

	private String getSupportedAudioCodecInfo() {

		if (VERBOSE) Log.d(TAG, "CodecCapabilities: ");
		int numCodecs = MediaCodecList.getCodecCount();
		String audioMimeType = null;

		for (int i = 0; i < numCodecs; i++) {

			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

			if (info.isEncoder()) {

				String[] types = info.getSupportedTypes();

				for (int j = 0; j < types.length; j++) {

					if (types[j].startsWith("audio/")) {
						audioMimeType = types[j];
						return audioMimeType;

					}

				}

			} // if encoder

		}

		return audioMimeType;

	}

	/**
	 * Returns true if this is a color format that this test code understands
	 * (i.e. we know how to read and generate frames in this format).
	 */
	private boolean isRecognizedFormat(int colorFormat) {
		switch (colorFormat) {
		// these are the formats we know how to handle for this test
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:

		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Returns true if the specified color format is semi-planar YUV. Throws an
	 * exception if the color format is not recognized (e.g. not YUV).
	 */

	private static int PLANAR = 1;
	private static int SEMI_PLANAR = 2;

	public int yuvPlanesLayout() {

		switch (vci.colorFormat) {
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			return PLANAR;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			return SEMI_PLANAR;
		default:
			throw new RuntimeException("unknown format " + vci.colorFormat);
		}
	}

	

	
	private CamcorderProfile getCamcorderProfile(){
		int dim = getSmaller(size.width, size.height);
		if (dim > MAX_RESOLUTION) {
			dim = MAX_RESOLUTION;
		} else if (dim < MIN_RESOLUTION) {
			dim = MIN_RESOLUTION;
		}
		
		CamcorderProfile profile = null;
		List<Integer> reducedResolutions = getResolutionsWithCamcorder();
		
		for (int i = 0; i < reducedResolutions.size() - 1; i++) {
			if (dim >= reducedResolutions.get(i) && dim < reducedResolutions.get(i + 1)) {
				int quality = getExactCamcorderQuality(reducedResolutions.get(i));
				if (CamcorderProfile.hasProfile(quality)){
					profile = CamcorderProfile.get(quality);
					break;
				}
				
			}
		}
		if (profile == null) {
			// exact profile was not match, find the nearest/lowest
			for (int i = 0; i < reducedResolutions.size(); i++) {

				int quality = getExactCamcorderQuality(reducedResolutions.get(i));
					if (CamcorderProfile.hasProfile(quality)){
						profile = CamcorderProfile.get(quality);
						break;
					}
					
				}
			
			
		}
				
		return profile;
		
	}

	private List<Integer> getResolutionsWithCamcorder(){
		List<Integer> reducedResolutions = new ArrayList<Integer>();
		for (int i = 0; i < resolutions.length; i++) {

			int quality = getExactCamcorderQuality(resolutions[i]);
			if (CamcorderProfile.hasProfile(quality)){
					reducedResolutions.add(resolutions[i]);
				}
			}
		return reducedResolutions;
	}

	/**
	 * Get a camcorder quality level, based on the provided size/dimension
	 * 
	 * @param shortSide
	 * @return
	 */

	private int getExactCamcorderQuality(int shortSide) {
		int camcorderQuality = -1;
		switch (shortSide) {

		case 1080:
			camcorderQuality = CamcorderProfile.QUALITY_1080P;
			break; // Quality level corresponding to the 1080p (1920 x 1080)
					// resolution.

		case 720:
			camcorderQuality = CamcorderProfile.QUALITY_720P;
			break; // Quality level corresponding to the 720p (1280 x 720)
					// resolution.
		case 480:
			camcorderQuality = CamcorderProfile.QUALITY_480P;
			break; // Quality level corresponding to the 480p (720 x 480)
					// resolution.
		case 288:

			camcorderQuality = CamcorderProfile.QUALITY_CIF;
			break; // Quality level corresponding to the cif (352 x 288)
					// resolution.

		case 240:
			camcorderQuality = CamcorderProfile.QUALITY_QVGA;
			break; // Quality level corresponding to the QVGA (320x240)
					// resolution.
		case 144:
			camcorderQuality = CamcorderProfile.QUALITY_QCIF;
			break; // Quality level corresponding to the qcif (176 x 144)
					// resolution.

		}

		if (camcorderQuality < 0) {
			if (shortSide > MAX_RESOLUTION) {
				camcorderQuality = MAX_CAMCORDER_QUALITY;
			} else if (shortSide < MIN_RESOLUTION) {
				camcorderQuality = MIN_CAMCORDER_QUALITY;
			}
		}

		return camcorderQuality;

	}
	
	private int getSmaller(int width, int height) {
		return width > height ? height : width;
	}

	private static class VideoCodecInfo {
		private String mimeType;
		private int colorFormat;

		public VideoCodecInfo(String mimeType, int colorFormat) {
			super();
			this.mimeType = mimeType;
			this.colorFormat = colorFormat;
		}

		public String getMimeType() {
			return mimeType;
		}

		public int getColorFormat() {
			return colorFormat;
		}
	}

}
