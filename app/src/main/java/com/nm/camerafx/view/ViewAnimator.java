package com.nm.camerafx.view;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ViewAnimator {
	
	
	public static final int SLIDE_DURATION_MS = 400;
	public static final int VIEW_ANIMATION_DELAY_MS = 1000;
	public static final int STAY_VISIBLE_DURATION_MS = 5000; // 5 seconds
	
	
	private static View containerView;
	//private static int distance = 0;
	public static void setLength(View containerView) {
		ViewAnimator.containerView = containerView;
	
	}
	
	public static void animateView(View view, int visiblity, int duration) {
		
		// show
		int fromXDelta = containerView.getWidth() * -1;
		
		int toXDelta = 0;

		// hide
		if (visiblity != View.VISIBLE) {
			toXDelta = fromXDelta;
			fromXDelta = 0;
		}

		Animation animation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
		animation.setDuration(duration);
		// animation.setFillAfter(true);
		view.startAnimation(animation);
		view.setVisibility(visiblity);

	}

	private static View view;
	
	public static void delayedHide(View view, int delayMillis) {
		ViewAnimator.view = view;
		
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	private static Handler mHideHandler = new Handler();
	private static Runnable mHideRunnable = new Runnable() {

		@Override
		public void run() {

			animateView(view, View.GONE, SLIDE_DURATION_MS);
		}
	};

}
