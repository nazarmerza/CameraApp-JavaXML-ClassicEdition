package com.nm.camerafx.fragments;

import com.nm.camerafx.R;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;


public class PrefsFragment extends PreferenceFragment {

	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	 
	  super.onCreate(savedInstanceState);
	 // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	  
	  // Load the preferences from an XML resource
	   addPreferencesFromResource(R.xml.preferences);
	  
	   //getView().setBackgroundColor(Color.WHITE);
	   
	        
	 }

	}
