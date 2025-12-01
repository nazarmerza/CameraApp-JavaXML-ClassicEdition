package com.nm.camerafx.view;

import java.util.List;



import com.nm.camerafx.R;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {
	private Context context;

	// private Integer[] resourceIds;
	private List<Thumbnail> thumbnails;

	private ImageView imageView;
	private TextView textView;

	private int orientation;
	private int selectedIndex = -1;

	public ListViewAdapter(Context context, List<Thumbnail> thumbnails,
			int orientation) {
		this.context = context;
		this.thumbnails = thumbnails;
		this.orientation = orientation;
	}

	
	public void clearData() {
		thumbnails.clear();
		this.notifyDataSetChanged();
	}
	
    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        notifyDataSetChanged();
    }
	
	@Override
	public int getCount() {
		return thumbnails.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// Override this method according to your need
	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {

		int layoutId;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			layoutId = R.layout.thumbnail_layout_rotated;
			
		} else {
			layoutId = R.layout.thumbnail_layout;
		}

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		
		//if (view == null) {
			view = inflater.inflate(layoutId, null);
		//}
		
		Thumbnail thumbnail = thumbnails.get(index);
		imageView = (ImageView) view.findViewById(R.id.thumbnail_image);
		imageView.setImageResource(thumbnail.getResourceId());
		imageView.setTag("image_view");
		
		if (selectedIndex != -1 && index == selectedIndex) {
			imageView.setBackgroundResource(R.drawable.bg_listview_item_selected);
		}

		view.setId(thumbnail.getResourceId());

		textView = (TextView) view.findViewById(R.id.thumbnail_text);
		textView.setText(thumbnail.getName());
		// textView.setVisibility(View.GONE);

		return view;

	}


}
