package com.nm.camerafx.fragments;

import com.nm.camerafx.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ListViewFragment extends DialogFragment implements
		OnItemClickListener {

	public interface Communicator {
		public void message(String data, int position);
	}

	String[] listitems;
	
	private String title;
	ArrayAdapter<String> adapter;
	ListView mylist;
	Communicator communicator;

	public ListViewFragment(){
		super();
	}
	public ListViewFragment(String[] list, String title) {
		super();
		this.listitems = list;
		this.title = title;
	}


	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		if (activity instanceof Communicator) {
			communicator = (Communicator) getActivity();
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet MyListFragment.communicator");
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, listitems);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		 // getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		  //  getDialog().setCanceledOnTouchOutside(true);
		    
		View view = inflater.inflate(R.layout.listview_fragment, null, true);

		
		//getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		//((LinearLayout) view).setOrientation(LinearLayout.VERTICAL);

		mylist = (ListView) view.findViewById(R.id.list);
		mylist.setAdapter(adapter);
		mylist.setOnItemClickListener(this);

		return view;
	}
	
	@Override
	public void onResume(){

		
		
	    super.onResume();
	    Window window = getDialog().getWindow();
	   // window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	   window.setGravity(Gravity.LEFT);
	    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	   
	    window.setTitle(title);
	    
	    //getDialog().setTitle(title);
	    
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		dismiss();
		communicator.message(listitems[position], position);

	}

}
