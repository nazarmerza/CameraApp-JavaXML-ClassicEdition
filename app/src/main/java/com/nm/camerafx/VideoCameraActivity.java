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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.exifinterface.media.ExifInterface;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoCameraActivity extends AppCompatActivity implements OnClickListener,
        OnItemClickListener, Communicator, OnSharedPreferenceChangeListener {

    static {
        System.loadLibrary("livecamera");
    }

    private native void setMap(int filterType);

    private static final String TAG = VideoCameraActivity.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private static final int BUTTON_VIBRATION_MS = 20;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private int cameraMode = MEDIA_TYPE_IMAGE;

    private int dialogType = -1;
    private final static int EXPOSURE_DIALOG = 1;
    private final static int RESOLUTION_DIALOG = 2;
    private final static int WHITE_BALANCE_DIALOG = 3;

    // screen orientation
    public static int mOrientation = -1;

    // Shared preferences keys
    private SharedPreferences prefs;

    private static final String KEY_LAST_SAVED_FILE_PATH = "LAST_OUTPUT_FILE_PATH";
    private static final String KEY_RES_WIDTH = "SAVED_RES_WIDTH";
    private static final String KEY_RES_HEIGHT = "SAVED_RES_HEIGHT";

    private static int currentCameraId = -1;

    private View listviewContainer;
    private ListView filtersListView = null;

    /*
     * Recording
     */
    private boolean isRecording = false;
    private RecorderService recorderService;

    private CodecGenerator cg = null;

    private String lastSavedFilePath;
    private Uri lastSavedFileUri;
    private String currentFilePath;

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
    private OrientationEventListener myOrientationEventListener;
    private GestureDetectorCompat mDetector;

    /*
     * Views, UI elements
     */
    private ListViewAdapter adapter = null;
    List<Thumbnail> list = null;
    private int listViewItemPosition = -1;
    private int listViewIndex = -1;
    private int listViewTopIndex = -1;

    // Preview
    private ViewGroup previewContainer;
    private CameraView cameraView;

    // buttons
    private View overlaybuttonsContainer;
    private Button recordBtn;
    private ImageView videoThumb;
    private ImageView videoPhotoSwitch;
    private ImageButton prefsButton;
    private TextView timerTextView;

    private TextView resolutionTextView;
    private TextView exposureTextView;
    private TextView whiteBalanceTextView;

    private int screenWidth;
    private int screenLength;

    private RecordTimer timer;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {
                // no-op
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (VERBOSE)
            Log.d(TAG, "onCreate() ");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_videocamera_portrait);

        requestPermissions();

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

        setMap(FilterType.PLATINUM);
        initCodecs();

        recorderService = new RecorderService();

        mDetector = new GestureDetectorCompat(this, new SwipeGestureListener(filtersListView));

        initOrientationChangeListener();
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.shutter_sound))) {
            shutterSoundOn = sharedPreferences.getBoolean(getString(R.string.shutter_sound), true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (VERBOSE) {
            Log.d(TAG, "onResume()");
            CamcorderProfileInfo.printCamcorderInfo();
            CamcorderProfileInfo.getCodecCapabilities();
        }

        if (currentCameraId == -1) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        initPreviewUi(currentCameraId);

        // Refresh thumbnail robustly (if nothing stored, query newest from MediaStore)
        refreshThumbnail(lastSavedFileUri);

        ViewAnimator.setLength(listviewContainer);
        ViewAnimator.delayedHide(filtersListView, ViewAnimator.VIEW_ANIMATION_DELAY_MS);

        timer = new RecordTimer(timerTextView);

        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }
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
                ViewAnimator.delayedHide(filtersListView, ViewAnimator.STAY_VISIBLE_DURATION_MS);
                return false;
            }
        });

        if (listViewIndex > 0) {
            filtersListView.setSelectionFromTop(listViewIndex, listViewTopIndex);
        }
    }

    private void setCameraSize() {
        CameraInfo2.openCamera();

        sizes = CameraInfo2.getPreviewSizes2(screenLength, screenWidth);
        exposures = CameraInfo2.getExposureCompensations();
        whiteBlances = CameraInfo2.getSupportedWhiteBalance();

        currentExposure = CameraInfo2.getExposureCompensation();
        currentWhiteBalance = CameraInfo2.getWhiteBalance();

        CameraInfo2.releaseCamera();

        if (sizes == null || sizes.isEmpty()) {
            Toast.makeText(this, "This device is not supported.",
                    Toast.LENGTH_LONG).show();
            showAppExitDialog();
            return;
        }

        currentCameraSize = sizes.get(0);

        for (CameraSize size : sizes) {
            if (size.width * size.height <= (DEFAULT_WIDTH * DEFAULT_HEIGHT)) {
                currentCameraSize = size;
            }
        }

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
        } catch (NoVideoCodecFoundException | NoCamcorderFound | NoAudioEncoderFoundException e) {
            Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage(), e);
            showAppExitDialog();
        }
    }

    private void initPreviewUi(int cameraId) {
        if (listviewContainer.getParent() != null) {
            ((ViewGroup) listviewContainer.getParent()).removeView(listviewContainer);
        }

        if (cameraView == null) {
            cameraView = new CameraView(getApplicationContext(), this, cameraId,
                    currentCameraSize, cg.yuvPlanesLayout());
            cameraView.setMode(cameraMode);
            cameraView.setExposure(currentExposure);
            cameraView.setWhiteBalance(currentWhiteBalance);

            cameraView.setZOrderOnTop(true);
            SurfaceHolder sfhTrackHolder = cameraView.getHolder();
            sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
            previewContainer.addView(cameraView);
        }

        previewContainer.addView(listviewContainer);
        overlaybuttonsContainer.bringToFront();
        timerTextView.bringToFront();

        resolutionTextView.setText(currentCameraSize.height + "p");
        exposureTextView.setText("EV\n" + currentExposure);
        whiteBalanceTextView.setText(currentWhiteBalance.substring(0, 4));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (isRecording) {
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
                if (lastSavedFileUri != null) {
                    launchMedia(lastSavedFileUri);
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
        } else {
            cameraMode = MEDIA_TYPE_IMAGE;
            videoPhotoSwitch.setImageResource(R.drawable.ic_camera);
        }
        cameraView.setMode(cameraMode);
    }

    private void shakeButton(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(BUTTON_VIBRATION_MS);
    }

    private void onVideoModeStart() {
        shakeButton(recordBtn);
        timer.startTimer();

        myOrientationEventListener.disable();

        overlaybuttonsContainer.setVisibility(View.GONE);
        filtersListView.setVisibility(View.GONE);

        timerTextView.setVisibility(View.VISIBLE);
        recordBtn.setBackgroundResource(R.drawable.record_btn_pressed);

        currentFilePath = StorageManager.getOutputMediaFilePath(MEDIA_TYPE_VIDEO);

        try {
            cg.setSize(currentCameraSize);
        } catch (NoCamcorderFound e) {
            Log.e(TAG, e.getMessage(), e);
        }
        recorderService.start(cameraView, cg, currentFilePath);
    }

    private void onVideoModeStop() {
        shakeButton(recordBtn);
        timer.stopTimer();

        myOrientationEventListener.enable();

        overlaybuttonsContainer.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        recordBtn.setBackgroundResource(R.drawable.round_btn_selector_green);

        try {
            recorderService.stop();

            if (currentFilePath != null) {
                // Use MediaScanner to get the content URI
                MediaScannerConnection.scanFile(
                        getApplicationContext(),
                        new String[]{currentFilePath},
                        new String[]{"video/mp4"},
                        (path, uri) -> {
                            lastSavedFileUri = uri;
                            runOnUiThread(() -> refreshThumbnail(uri));
                            currentFilePath = null; // Clear path after handling
                        }
                );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onTakePicture() {
        try {
            // takePicture now returns a content URI
            Uri imageUri = cameraView.takePicture();
            lastSavedFileUri = imageUri;
            refreshThumbnail(imageUri);

        } catch (IOException e) {
            Toast.makeText(this, "Problem occurred during taking picture.",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage(), e);
        }

        if (shutterSoundOn) {
            shutterSoundEffect();
        }
    }

    private void shutterSoundEffect() {
        SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        int shutterSound = soundPool.load(this, R.raw.camera_click, 0);
        soundPool.play(shutterSound, 0.5f, 0.5f, 0, 0, 1);
    }

    private void refreshThumbnail(Uri mediaUri) {
        if (mediaUri == null) {
            return; // Nothing to refresh
        }
        new Thread(() -> {
            try {
                final Bitmap bm = getContentResolver().loadThumbnail(mediaUri, new Size(200, 200), null);
                runOnUiThread(() -> videoThumb.setImageBitmap(bm));
            } catch (IOException e) {
                Log.e(TAG, "Failed to load thumbnail", e);
            }
        }).start();
    }

    private void launchMedia(Uri mediaUri) {
        String mimeType = getContentResolver().getType(mediaUri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(mediaUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app can handle this file.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listViewItemPosition = position;
        adapter.setSelectedIndex(listViewItemPosition);

        listViewIndex = filtersListView.getFirstVisiblePosition();
        View v = filtersListView.getChildAt(0);
        listViewTopIndex = (v == null) ? 0 : v.getTop();

        if (cameraView != null) {
            cameraView.stopPreview();
            setFilter(view.getId());
            cameraView.startPreview();
        }
    }

    private void setFilter(int id) {
        int filterType = FilterType.getFilterType(id);
        setMap(filterType);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

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
        int rotationAngle = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 0 : -90;

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

    private void showExposureSelectionDialog() {
        dialogType = EXPOSURE_DIALOG;
        int size = exposures.size();
        if (size <= 0) return;

        DecimalFormat format = new DecimalFormat("#.#");
        String[] items = new String[exposures.size()];
        for (int i = 0; i < size; i++) {
            items[i] = format.format(exposures.get(i));
        }
        showDialog(items, "Exposure");
    }

    private void showWhiteBalanceSelectionDialog() {
        dialogType = WHITE_BALANCE_DIALOG;
        showDialog(whiteBlances.toArray(new String[0]), "White Balance");
    }

    private void showResolutionSelectionDialog() {
        dialogType = RESOLUTION_DIALOG;
        String[] items = new String[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
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
        if (prefs != null) {
            prefs.edit().putInt(KEY_RES_WIDTH, currentCameraSize.width).apply();
            prefs.edit().putInt(KEY_RES_HEIGHT, currentCameraSize.height).apply();
        }
        recreateCamera();
    }

    private void recreateCamera() {
        if (cameraView != null) {
            cameraView.surfaceDestroyed(cameraView.getHolder());
            cameraView.getHolder().removeCallback(cameraView);
            cameraView = null;
            initPreviewUi(currentCameraId);
        }
    }

    @Override
    public void message(String data, int position) {
        if (dialogType == EXPOSURE_DIALOG) {
            currentExposure = exposures.get(position);
            onExposureSelected();
        } else if (dialogType == RESOLUTION_DIALOG) {
            currentCameraSize = sizes.get(position);
            onResolutionSelected();
        } else if (dialogType == WHITE_BALANCE_DIALOG) {
            currentWhiteBalance = whiteBlances.get(position);
            onWhiteBalanceSelected();
        }
    }

    private void showAppExitDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.device_not_supported)
                .setMessage(R.string.app_exit)
                .setPositiveButton(R.string.yes, (dialog, which) -> VideoCameraActivity.this.finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
