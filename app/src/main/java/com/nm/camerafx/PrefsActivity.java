package com.nm.camerafx;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import com.nm.camerafx.fragments.PrefsFragment;

public class PrefsActivity extends Activity {

	
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  
	  super.onCreate(savedInstanceState);
	  if( VideoCameraActivity.mOrientation == Configuration.ORIENTATION_PORTRAIT) {
		  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	  } else {
		  
	  }
	  
	  getFragmentManager().beginTransaction().replace(android.R.id.content,
	                new PrefsFragment()).commit();
	 }

}
