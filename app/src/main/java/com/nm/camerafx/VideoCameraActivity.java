package com.nm.camerafx;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.nm.camerafx.assets.BitmapUtils;
import com.nm.camerafx.camera.CameraInfo2;
import com.nm.camerafx.camera.CameraView;
import com.nm.camerafx.codecs.CamcorderProfileInfo;
import com.nm.camerafx.codecs.CodecGenerator;
import com.nm.camerafx.exception.NoAudioEncoderFoundException;
import com.nm.camerafx.exception.NoCamcorderFound;
import com.nm.camerafx.exception.NoVideoCodecFoundException;
import com.nm.camerafx.fragments.ListViewFragment;
import com.nm.camerafx.fragments.ListViewFragment.Communicator;
import com.nm.camerafx.io.SingleMediaScanner;
import com.nm.camerafx.io.StorageManager;
import com.nm.camerafx.model.CameraSize;
import com.nm.camerafx.model.FilterType;
import com.nm.camerafx.model.RecordTimer;
import com.nm.camerafx.service.RecorderService;
import com.nm.camerafx.utils.MathUtils;
import com.nm.camerafx.view.ListViewAdapter;
import com.nm.camerafx.view.SwipeGestureListener;
import com.nm.camerafx.view.Thumbnail;
import com.nm.camerafx.view.ThumbnailGroups;
import com.nm.camerafx.view.ViewAnimator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;


public class VideoCameraActivity extends AppCompatActivity implements OnClickListener,
		OnItemClickListener, Communicator, OnSharedPreferenceChangeListener {

	static {
		System.loadLibrary("livecamera");
	}

	private native void setMap(int filterType);

	private static String TAG = VideoCameraActivity.class.getSimpleName();
	private static boolean VERBOSE = false;

	private static final int BUTTON_VIBRATION_MS = 20;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private int cameraMode = MEDIA_TYPE_IMAGE;

	private int dialogType = -1;
	private final static int EXPOSURE_DIALOG = 1;
	private final static int RESOLUTION_DIALOG = 2;
	private final static int WHITE_BALANCE_DIALOG = 3;
	
	// screen orientation
	private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
	private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
	public static int mOrientation = -1;
	


	// Shared preferences keys
	private SharedPreferences prefs;
	private ListPreference sceneModes;
	
	private static final String KEY_LAST_SAVED_FILE_PATH = "LAST_OUTPUT_FILE_PATH";
	private static final String KEY_RES_WIDTH = "SAVED_RES_WIDTH";
	private static final String KEY_RES_HEIGHT = "SAVED_RES_HEIGHT";
	private static final String KEY_EXPOSURE = "key_exposure";
	private static final String KEY_WHITE_BALANCE = "key_white_balance";

	private static int currentCameraId = -1;

	private View listviewContainer;
	private ListView filtersListView = null;
	private View selectedListItem = null; // Reference to currently selected
										// listiview item



	/*
	 * Recording 
	 */
	private boolean isRecording = false;
	private RecorderService recorderService;

	private CodecGenerator cg = null;
	private int colorFormat;

	private String lastSavedFilePath;
	private String currentFilePath;

	/*
	 * Camera parameters 
	 */
	CameraInfo2 cameraInfo;

	// available camera parameters
	private List<CameraSize> sizes;
	private List<Integer> exposures;
	private List<String> whiteBlances;

	// current camera parameters
	private static int currentExposure;
	
	public static CameraSize currentCameraSize = null;
	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;
	
	public static String currentWhiteBalance;

	/*
	 * Options
	 */
	private static boolean shutterSoundOn = true;

	/*
	 * Action detectors 
	 */
	// Listens for screen orientation change, used to rotate buttons/views
	private OrientationEventListener myOrientationEventListener;

	// Detects swipe gesture. Used to show/hide filters listview
	private GestureDetectorCompat mDetector;
	


	/*
	 * Views, UI elements
	 */
	
	private ListViewAdapter adapter = null;
	List<Thumbnail> list = null;
	private int listViewItemPosition = -1;
	private int listViewIndex = -1;
	private int listViewTopIndex = -1;
	
	private static final double RECORD_BUTTON_SCALE = 120.0 /160.0 ;
	private static final double SWITCH_BUTTON_SCALE = 70.0 / 160.0 ;
	
	// Preview
	private ViewGroup previewContainer;
	private CameraView cameraView;

	// buttons
	private View overlaybuttonsContainer;
	private View buttonsContainer;
	private Button recordBtn;
	private ImageView videoThumb;
	private ImageView videoPhotoSwitch;
	private ImageButton prefsButton;
	private ImageButton resolutionButton;
	// private ImageButton expositionBarBtn;
	private TextView timerTextView;

	private TextView resolutionTextView;
	private TextView exposureTextView;
	private TextView whiteBalanceTextView;
	
	private int screenWidth;
	private int screenLength;
	
	private RecordTimer timer;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					// Permission is granted. Continue the action or workflow in your
					// app.
				} else {
					// Explain to the user that the feature is unavailable because the
					// feature requires a permission that the user has denied. At the
					// same time, respect the user's decision. Don't link to system
					// settings in an effort to convince the user to change their
					// decision.
				}
			});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (VERBOSE)
			Log.d(TAG, "onCreate() ");

		// set activity as full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_videocamera_landscape);

		if (ContextCompat.checkSelfPermission(
				this, Manifest.permission.CAMERA) !=
				PackageManager.PERMISSION_GRANTED) {
			requestPermissionLauncher.launch(Manifest.permission.CAMERA);
		}
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		screenWidth = MathUtils.getSmaller(size.y, size.x);
		screenLength = MathUtils.getLarger(size.y, size.x);

		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		lastSavedFilePath = prefs.getString(KEY_LAST_SAVED_FILE_PATH, null);
		shutterSoundOn = prefs.getBoolean(getString(R.string.shutter_sound), true);

		setCameraSize();

		initListView();
		initButtons();



		previewContainer = (ViewGroup) findViewById(R.id.preview_container);

		// setCameraSize();
		setMap(FilterType.PLATINUM);
		initCodecs();

		recorderService = new RecorderService();

		
		// Gesture detection
		mDetector = new GestureDetectorCompat(this, new SwipeGestureListener(filtersListView));
		
		
		initOrientationChangeListener();


	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		if (key.equals(getString(R.string.shutter_sound))) {
			shutterSoundOn = sharedPreferences.getBoolean(
					getString(R.string.shutter_sound), true);
		}

	}



	@Override
	public void onStart() {
		if (VERBOSE)
			Log.d(TAG, "onStart()");
		super.onStart();
	}

	@Override
	public void onRestart() {
		if (VERBOSE)
			Log.d(TAG, "onRestart()");
		super.onRestart();
	}

	@Override
	public void onResume() {

		if (VERBOSE){
			Log.d(TAG, "onResume()");
			CamcorderProfileInfo.printCamcorderInfo();
			CamcorderProfileInfo.getCodecCapabilities();
		}


		if (currentCameraId == -1) {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		initPreviewUi(currentCameraId);

		if (lastSavedFilePath != null) {
			setThumbnailResource(lastSavedFilePath);
		}

		ViewAnimator.setLength(listviewContainer);
		ViewAnimator.delayedHide(filtersListView, ViewAnimator.VIEW_ANIMATION_DELAY_MS);
		
		timer = new RecordTimer(timerTextView);
		
		//prefs.registerOnSharedPreferenceChangeListener(this);
		if (prefs != null) {
			prefs.unregisterOnSharedPreferenceChangeListener(this);
		}
		
		
		super.onResume();
	}

	@Override
	public void onPause() {
		if (VERBOSE) Log.d(TAG, "onPause()");
		
		//prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	private void initButtons() {

		overlaybuttonsContainer = findViewById(R.id.overlay_buttons_layout);
		recordBtn = (Button) findViewById(R.id.recrod_btn);
		recordBtn.setOnClickListener(this);

		videoThumb = (ImageView) findViewById(R.id.video_thumbnail);
		videoThumb.setOnClickListener(this);

		videoPhotoSwitch = (ImageView) findViewById(R.id.video_photo_switch);
		videoPhotoSwitch.setOnClickListener(this);

		prefsButton = (ImageButton) findViewById(R.id.prefs);
		prefsButton.setOnClickListener(this);

		resolutionTextView = (TextView) findViewById(R.id.resolution_text);
		resolutionTextView.setOnClickListener(this);

		exposureTextView = (TextView) findViewById(R.id.exposure);
		exposureTextView.setOnClickListener(this);

		whiteBalanceTextView = (TextView) findViewById(R.id.white_balance);
		whiteBalanceTextView.setOnClickListener(this);

		timerTextView = (TextView) findViewById(R.id.timer);

	}



	private void initListView() {

		listviewContainer = findViewById(R.id.listview_container);

		if (filtersListView != null) {
			((ListViewAdapter) filtersListView.getAdapter()).clearData();
		}

		filtersListView = (ListView) findViewById(R.id.filters_listview);
		filtersListView.setOnItemClickListener(this);
		filtersListView.setSaveEnabled(false);

		list = ThumbnailGroups.getFiltersMetalic();
		adapter = new ListViewAdapter(this, list, mOrientation);
		adapter.setSelectedIndex(listViewItemPosition);

		filtersListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		filtersListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ViewAnimator.delayedHide(filtersListView,ViewAnimator.STAY_VISIBLE_DURATION_MS);
				return false;
			}
		});

		
		if (listViewIndex > 0) {
			filtersListView.setSelectionFromTop(listViewIndex, listViewTopIndex);
		}

	}



	private void setCameraSize() {

		// must call open camera before making any inquiry
		CameraInfo2.openCamera();

		// Get lists of parameter values/ranges
		sizes = CameraInfo2.getPreviewSizes2(screenLength, screenWidth);
		//sizes = CameraInfo2.getPreviewSizes();
		exposures = CameraInfo2.getExposureCompensations();
		whiteBlances = CameraInfo2.getSupportedWhiteBalance();

		// Get default parameters
		currentExposure = CameraInfo2.getExposureCompensation();
		currentWhiteBalance = CameraInfo2.getWhiteBalance();

		// must must release camera
		CameraInfo2.releaseCamera();

		if (sizes == null || sizes.size() <= 0) {
			Toast.makeText(this, "This device is not supported.",
					Toast.LENGTH_LONG).show();
			showAppExitDialog();
		}

		currentCameraSize = new CameraSize(sizes.get(0).width,
				sizes.get(0).height);

		
		int i = 0;
		for (CameraSize size : sizes) {
			if (size.width * size.height <= (DEFAULT_WIDTH * DEFAULT_HEIGHT)) {
				currentCameraSize = new CameraSize(size.width, size.height);
				;
			}
			
		}

		// if the user already has a prefered resolutin, use that one.
		int savedWidth = prefs.getInt(KEY_RES_WIDTH, -1);
		int savedHeight = prefs.getInt(KEY_RES_HEIGHT, -1);
		if (savedWidth > 0 && savedHeight > 0) {

			currentCameraSize.width = savedWidth;
			currentCameraSize.height = savedHeight;
		}

	}

	private void initCodecs() {

		try {
			cg = new CodecGenerator(currentCameraSize);
		} catch (NoVideoCodecFoundException e) {
			Toast.makeText(this, "This device is not supported.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, e.getMessage(), e);
			showAppExitDialog();

		} catch (NoCamcorderFound e) {
			Toast.makeText(this, "This device is not supported.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, e.getMessage(), e);
			showAppExitDialog();

		} catch (NoAudioEncoderFoundException e) {
			Toast.makeText(this, "This device is not supported.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, e.getMessage(), e);
			showAppExitDialog();

		} finally {

		}

	}

	private void initPreviewUi(int cameraId) {

		((ViewGroup) listviewContainer.getParent())
				.removeView(listviewContainer);

		// Construct a CameraPreview instance, and add it to view
		if (cameraView == null) {

			cameraView = new CameraView(getApplicationContext(), cameraId,
					currentCameraSize, cg.yuvPlanesLayout());
			// cameraView.setCameraId(cameraId);
			// cameraView.setPrviewSize(selectedCameraSize);

			// Camera camera = cameraView.getCamera();
			cameraView.setMode(cameraMode);
			cameraView.setExposure(currentExposure);
			cameraView.setWhiteBalance(currentWhiteBalance);

			cameraView.setZOrderOnTop(true); // necessary
			SurfaceHolder sfhTrackHolder = cameraView.getHolder();
			sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
			previewContainer.addView(cameraView);
		}

		previewContainer.addView(listviewContainer);
		overlaybuttonsContainer.bringToFront();
		timerTextView.bringToFront();

		resolutionTextView.setText(currentCameraSize.height + "p");
		exposureTextView.setText("EV\n" + Integer.toString(currentExposure));
		String wbTemp = currentWhiteBalance.substring(0, 4);
		whiteBalanceTextView.setText(currentWhiteBalance.substring(0, 4));
		
		sizeScreenElements();
		


	}
	
	

	
	private void sizeScreenElements(){
				

		
		// find ratio based on the shorter sides
		double scaleFactor = (double) screenWidth
				/ MathUtils.getSmaller(currentCameraSize.height, currentCameraSize.width);
		
		// now scale the longer side of preview 
		int previewLength = (int) (MathUtils.getLarger(currentCameraSize.height, currentCameraSize.width) * scaleFactor);		
		int diff = screenLength - previewLength;
		
		
		buttonsContainer = findViewById(R.id.buttons_layout);
		LinearLayout.LayoutParams params = (LayoutParams) buttonsContainer.getLayoutParams();
		params.width = diff;	// reset layout width
		buttonsContainer.setLayoutParams(params);
				
		recordBtn.setLayoutParams(new LinearLayout.LayoutParams( (int)(diff * RECORD_BUTTON_SCALE), (int) (diff * RECORD_BUTTON_SCALE)));
		videoPhotoSwitch.setLayoutParams(new LinearLayout.LayoutParams( (int)(diff * SWITCH_BUTTON_SCALE), (int) (diff * SWITCH_BUTTON_SCALE)));
		videoThumb.setLayoutParams(new LinearLayout.LayoutParams( (int)(diff * SWITCH_BUTTON_SCALE), (int) (diff * SWITCH_BUTTON_SCALE)));
		

	}
	
	

	
	/*
	private void resizeView(View view, double scaleFactor){
		LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
		params.width = (int) (params.width * scaleFactor);
		params.height = (int) (params.height * scaleFactor);
		view.setLayoutParams(params);
	}
	
	private double getSizeScale(int w, int h, int W, int H) {

		double ar1 = (double) W / (double) w;
		double ar2 = (double) H / (double) h;

		if (ar1 < ar2) {
			return ar1;
		}

		return ar2;

	}
*/
	/*******************************************************************/
	// Dialogs
	/*******************************************************************/
	

	@Override
	public void message(String data, int position) {
		if (dialogType == EXPOSURE_DIALOG) {
			int exposure = exposures.get(position);
			currentExposure = exposure;
			onExposureSelected();

		} else if (dialogType == RESOLUTION_DIALOG) {
			// set camera size
			currentCameraSize = sizes.get(position);
			onResolutionSelected();

		} else if (dialogType == WHITE_BALANCE_DIALOG) {

			currentWhiteBalance = whiteBlances.get(position);
			onWhiteBalanceSelected();
		}
	}

	private void showExposureSelectionDialog() {
		dialogType = EXPOSURE_DIALOG;
		int size = exposures.size();
		if (size <= 0)
			return;

		DecimalFormat format = new DecimalFormat("#.#");
		int value;

		String[] items = new String[exposures.size()];
		for (int i = 0; i < size; i++) {
			value = exposures.get(i);

			items[i] = format.format(value).toString();
			;
		}

		showDialog(items, "Exposure");
	}

	private void showWhiteBalanceSelectionDialog() {
		dialogType = WHITE_BALANCE_DIALOG;
		showDialog(whiteBlances.toArray(new String[whiteBlances.size()]),
				"White Balance");
	}

	private void showResolutionSelectionDialog() {
		dialogType = RESOLUTION_DIALOG;
		String[] items = new String[sizes.size()];
		for (int i = 0; i < sizes.size(); i++) {
			//items[i] = sizes.get(i).height + "p";
			items[i] = sizes.get(i).height + "x" + sizes.get(i).width;
		}

		showDialog(items, "Resolution");

	}

	public void showDialog(String[] list, String title) {
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		ListViewFragment newFragment = new ListViewFragment(list, title);
	

		newFragment.show(ft, "dialog");
	}

	private void onWhiteBalanceSelected() {
		recreateCamera();
	}

	private void onExposureSelected() {
		recreateCamera();
	}

	private void onResolutionSelected() {
		prefs.edit().putInt(KEY_RES_WIDTH, currentCameraSize.width).apply();
		prefs.edit().putInt(KEY_RES_HEIGHT, currentCameraSize.height).apply();

		recreateCamera();

	}

	private void recreateCamera() {
		if (cameraView != null) {
			// cameraView.setCameraId(currentCameraId);
			cameraView.surfaceDestroyed(cameraView.getHolder());
			cameraView.getHolder().removeCallback(cameraView);
			cameraView = null;
			initPreviewUi(currentCameraId);
		}
	}

	
	private void showAppExitDialog() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.device_not_supported)
				.setMessage(R.string.app_exit)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// Stop the activity
								VideoCameraActivity.this.finish();
							}

						}).setNegativeButton(R.string.no, null).show();

	}

	/*******************************************************************/
	// Handle button click
	/*******************************************************************/

	@Override
	public void onClick(View v) {
		//if (true) return;
		
		int id = v.getId();

		if (isRecording) {

			// if recording, only stop action is valid. Ignore all the rest.
			if (id == R.id.recrod_btn) {
				onVideoModeStop();
				isRecording = false;
			}

		} else {
			if (id == R.id.recrod_btn) {
				if (cameraMode == MEDIA_TYPE_IMAGE) {
					myOrientationEventListener.enable();
					onTakePicture();
				} else if (cameraMode == MEDIA_TYPE_VIDEO) {
					onVideoModeStart();
					isRecording = true;
				}
			} else if (id == R.id.video_thumbnail) {
				// String filePath = recorderService.getCurrentOutputFilePath();
				if (lastSavedFilePath != null) {
					launchVideo(lastSavedFilePath);
				} else {
					// TODO
				}
			} else if (id == R.id.video_photo_switch) {
				onVideoPhotSwitch(id);
			} else if (id == R.id.prefs) {
				prefs.registerOnSharedPreferenceChangeListener(this);
				startActivity(new Intent(this, PrefsActivity.class));
			} else if (id == R.id.resolution_text) {
				showResolutionSelectionDialog();
			} else if (id == R.id.exposure) {
				showExposureSelectionDialog();
			} else if (id == R.id.white_balance) {
				showWhiteBalanceSelectionDialog();
			}
		}

	}
	

	
	// switch camera mode between photo and video
	private void onVideoPhotSwitch(int id) {

		if (cameraMode == MEDIA_TYPE_IMAGE) {
			cameraMode = MEDIA_TYPE_VIDEO;

			videoPhotoSwitch.setImageResource(R.drawable.ic_video);

		} else if (cameraMode == MEDIA_TYPE_VIDEO) {
			cameraMode = MEDIA_TYPE_IMAGE;
			videoPhotoSwitch.setImageResource(R.drawable.ic_camera);
		}
		cameraView.setMode(cameraMode);
	}

	private void shakeButton(View view){
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
		vibe.vibrate(BUTTON_VIBRATION_MS);
		
	}
	private void onVideoModeStart() {
		
		shakeButton(recordBtn);
		timer.startTimer();
		
		myOrientationEventListener.disable();

		// hide all buttons on the preview
		overlaybuttonsContainer.setVisibility(View.GONE);
		filtersListView.setVisibility(View.GONE);

		// unhide timer
		timerTextView.setVisibility(View.VISIBLE);
		recordBtn.setBackgroundResource(R.drawable.record_btn_pressed);

		currentFilePath = StorageManager
				.getOutputMediaFilePath(MEDIA_TYPE_VIDEO);

		try {
			cg.setSize(currentCameraSize);
		} catch (NoCamcorderFound e) {

			Log.e(TAG, e.getMessage(), e);
			e.printStackTrace();
		}
		recorderService.start(cameraView, cg, currentFilePath);
		//recordBtn.setText("STOP");
		
		
		// isRecording = true;
	}

	private void onVideoModeStop() {
		shakeButton(recordBtn);
		timer.stopTimer();
		
		myOrientationEventListener.enable();
		

		// hide all buttons on the preview
		overlaybuttonsContainer.setVisibility(View.VISIBLE);
		//filtersListView.setVisibility(View.VISIBLE);

		// unhide timer
		timerTextView.setVisibility(View.GONE);
		recordBtn.setBackgroundResource(R.drawable.round_btn_selector_green);

		try {
			recorderService.stop();
			// String filePath =
			// recorderService.getCurrentOutputFilePath();
			if (currentFilePath != null) {
				lastSavedFilePath = currentFilePath;

				prefs.edit()
						.putString(KEY_LAST_SAVED_FILE_PATH, lastSavedFilePath)
						.apply();
				;
				setThumbnailResource(lastSavedFilePath);
				currentFilePath = null;

			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private void onTakePicture() {
		currentFilePath = StorageManager.getOutputMediaFilePath(MEDIA_TYPE_IMAGE);
		if (currentFilePath == null) {
			
			return;
		}

		try {
			currentFilePath = cameraView.takePicture(currentFilePath);
			new SingleMediaScanner(getApplicationContext(), new File(currentFilePath));


		} catch (IOException e) {
			Toast.makeText(this, "Problem occured during taking picture.",
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, e.getMessage(), e);
		}
		if (shutterSoundOn) {
			shutterSoundEffect();
		}
		lastSavedFilePath = currentFilePath;
		prefs.edit().putString(KEY_LAST_SAVED_FILE_PATH, lastSavedFilePath).apply();
		setThumbnailResource(currentFilePath);
		


		currentFilePath = null;
	}

	private void shutterSoundEffect() {
		SoundPool soundPool = new SoundPool(1,
				AudioManager.STREAM_NOTIFICATION, 0);
		int shutterSound = soundPool.load(this, R.raw.camera_click, 0);
		soundPool.play(shutterSound, 0.5f, 0.5f, 0, 0, 1);
	}

	private void onFrontBackCamera() {

		// swap the id of the camera to be used
		if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}

		if (cameraView != null) {
			// cameraView.setCameraId(currentCameraId);
			cameraView.surfaceDestroyed(cameraView.getHolder());
			cameraView.getHolder().removeCallback(cameraView);
			cameraView = null;
			initPreviewUi(currentCameraId);
		}

	}




	private void setThumbnailResource(String filePath) {

		Bitmap bm = null;
		if (filePath == null) {
			videoThumb.setImageResource(R.color.mint2);
			return;
		} else if (filePath.endsWith(".mp4")) {
			
			
			bm = ThumbnailUtils.createVideoThumbnail(filePath,
					MediaStore.Video.Thumbnails.MICRO_KIND);
	           Matrix matrix = new Matrix();
	            matrix.postRotate(90);

	            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	            
			videoThumb.setImageBitmap(bm);
			//videoThumb.setRotation(90);
		} else {

			bm = BitmapUtils.decodeRescale(new File(filePath), 100, 100);
			videoThumb.setImageBitmap(bm);
		}

		
		
	}

	private void launchVideo(String filePath) {

		String type = null;
		if (filePath.endsWith(".mp4")) {
			type = "video/*";
		} else {
			type = "image/*";
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
		startActivity(intent);

	}



	/**
	 * Receives click action on a listview item, selecting a filter
	 */

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		listViewItemPosition = position;
		adapter.setSelectedIndex(listViewItemPosition);
		
		//highlightListViewItem(view);
		
		selectedListItem = view;

		// save index and top position
		listViewIndex = filtersListView.getFirstVisiblePosition();
		View v = filtersListView.getChildAt(0);
		listViewTopIndex = (v == null) ? 0 : v.getTop();

		// set map
		if (cameraView != null) {
			cameraView.stopPreview();
			setFilter(view.getId());
			cameraView.startPreview();				
		}



	}


/*
	private void highlightListViewItem(View view) {
		
		
		ImageView imageView;

		// unselect previously selected item
		if (selectedListItem != null) {
			selectedListItem.findViewById(R.id.thumbnail_image)
					.setBackgroundResource(R.drawable.bg_listview_item);	
		}

		imageView = (ImageView) view.findViewById(R.id.thumbnail_image);
		imageView.setBackgroundResource(R.drawable.bg_listview_item_selected);
		

		selectedListItem = view;

		// save index and top position
		listViewIndex = filtersListView.getFirstVisiblePosition();
		View v = filtersListView.getChildAt(0);
		listViewTopIndex = (v == null) ? 0 : v.getTop();

	}
*/
	private void setFilter(int id) {
		int filterType = FilterType.getFilterType(id);
		setMap(filterType);

	}

	/***********************************************************************
	 * methods handling sliding (showing/hiding) of listview
	 ***********************************************************************/
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}


	/* Orientation change listener */
	private void initOrientationChangeListener() {

		myOrientationEventListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL * 100) {

			@Override
			public void onOrientationChanged(int orientation) {

				int lastOrientation = mOrientation;

				if (orientation >= 315 || orientation < 45) {
					
					mOrientation = Configuration.ORIENTATION_PORTRAIT;
				} else if (orientation < 315 && orientation >= 225) {
					
					mOrientation = Configuration.ORIENTATION_LANDSCAPE;
				} else if (orientation < 225 && orientation >= 135) {

					
					mOrientation = Configuration.ORIENTATION_PORTRAIT;
				} else { 
					// orientation <135 && orientation > 45
					
					mOrientation = Configuration.ORIENTATION_LANDSCAPE;
				}

				if (lastOrientation != mOrientation) {
					
					onRotation(mOrientation);
				}

			}
		};

		if (myOrientationEventListener.canDetectOrientation()) {

			myOrientationEventListener.enable();

		} else {

			myOrientationEventListener.disable();

		}
	}

	private void onRotation(int orientation) {

		int rotationAngle = 0;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

		} else {
			rotationAngle = -90;
		}

		recordBtn.animate().rotation(rotationAngle);
		videoThumb.animate().rotation(rotationAngle);
		videoPhotoSwitch.animate().rotation(rotationAngle);
		
		prefsButton.animate().rotation(rotationAngle);
		resolutionTextView.animate().rotation(rotationAngle);
		exposureTextView.animate().rotation(rotationAngle);
		timerTextView.animate().rotation(rotationAngle);
		whiteBalanceTextView.animate().rotation(rotationAngle);

		initListView();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (VERBOSE)
			Log.d(TAG, "onConfigurationChanged()");
		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}

	// ///////////////////////
	/*private Timer timer;
	
	private int seconds = 0;
	private long startTime = 0;

	private void stopTimer() {
		if (timerHandler != null) {
			timerHandler.removeMessages(MESSAGE_TOKEN);
			timerHandler = null;
		}

	}

	private int MESSAGE_TOKEN = 1;

	protected void startTimer() {
		
		if (timerHandler != null) {
			timerHandler.removeMessages(MESSAGE_TOKEN);
		}
		// merHandler.postDelayed(timerTask, 1000);
		startTime = System.currentTimeMillis();

		Message msg = timerHandler.obtainMessage();
		msg.what = MESSAGE_TOKEN;
		timerHandler.sendMessageDelayed(msg, 1000);

	};

	private Handler timerHandler = new Handler() {
		public void handleMessage(Message msg) {

			updateTimerView();

			// this is the textview
			msg = timerHandler.obtainMessage();
			msg.what = MESSAGE_TOKEN;
			timerHandler.sendMessageDelayed(msg, 1000);
		}
	};

	private void updateTimerView() {
		long seconds = (System.currentTimeMillis() - startTime) / 1000;
		String result = String.format("%02d:%02d", seconds / 60, seconds % 60);
		timerTextView.setText(result); // this is the textview
	}
*/
}
