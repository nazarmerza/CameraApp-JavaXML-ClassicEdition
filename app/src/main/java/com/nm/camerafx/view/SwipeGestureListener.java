package com.nm.camerafx.view;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class SwipeGestureListener extends SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 50;
/*	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private int mShortAnimTime;
	private int mControlsHeight;
	private int mControlsWidth;*/
	
	
	//private Activity activity;
	private View view;
	
	
	public SwipeGestureListener(View view) {
		this.view = view;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

/*		if (mShortAnimTime == 0) {
			mShortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);
		}
		if (mControlsWidth == 0) {
			mControlsWidth = listviewContainer.getWidth();
		}*/

		if ((e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE) {
			// swiped towards the edge
			ViewAnimator.animateView(view, View.GONE, ViewAnimator.SLIDE_DURATION_MS);
			
		} else if ((e2.getX() - e1.getX()) > SWIPE_MIN_DISTANCE) {
			// swiped from the edge
			
			ViewAnimator.animateView(view, View.VISIBLE, ViewAnimator.SLIDE_DURATION_MS);	// make view visible
			ViewAnimator.delayedHide(view, ViewAnimator.STAY_VISIBLE_DURATION_MS);	// request hide after delay
		}

		return super.onFling(e1, e2, velocityX, velocityY);
	}

}