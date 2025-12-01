package com.nm.camerafx.model;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class RecordTimer {
	
	private int MESSAGE_TOKEN = 1;

	private long startTime = 0;
	private TextView view;
	private Handler timerHandler;
	
	public RecordTimer(TextView view){
		this.view = view;
		
	}

	public void stopTimer() {
		if (timerHandler != null) {
			timerHandler.removeMessages(MESSAGE_TOKEN);
			//timerHandler = null;
		} 

	}

	public void startTimer() {
		
		if (timerHandler != null) {
			timerHandler.removeMessages(MESSAGE_TOKEN);
		} else {
			//timerHandler = new TimerHandler();
			timerHandler = new Handler(new TimerHandlerCallback());
		}
		
		// merHandler.postDelayed(timerTask, 1000);
		startTime = System.currentTimeMillis();

		Message msg = timerHandler.obtainMessage();
		msg.what = MESSAGE_TOKEN;
		timerHandler.sendMessageDelayed(msg, 1000);

	};



	private  void updateTimerView() {
		long seconds = (System.currentTimeMillis() - startTime) / 1000;
		String result = String.format(java.util.Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
		view.setText(result); 
	}
	

	//class TimerHandler extends Handler {
			
		class TimerHandlerCallback implements Handler.Callback {
			
		@Override
		public boolean handleMessage(Message msg) {

			updateTimerView();

			msg = timerHandler.obtainMessage();
			msg.what = MESSAGE_TOKEN;
			timerHandler.sendMessageDelayed(msg, 1000);
			return true;
		}
	}

}
