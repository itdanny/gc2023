package com.hkbs.HKBS.arkUtil;

import android.os.Handler;

public class MyRunLater {
	public interface Callback {
		public void ready();
	}
	public MyRunLater(int delay, Callback callback) {
		myRun(delay, callback);
	}
	public MyRunLater(Callback callback) {
		myRun(MyUtil.DELAY_MILLIS, callback);
	}
	private void myRun(int delay, Callback callback){
		final Callback runLaterCallback = callback;
		final int runLaterDelay = delay;
		Handler handler = new Handler();
		if (delay==0){
			handler.post(new Runnable() {
				@Override
				public void run() {
					runLaterCallback.ready();										
				}
			});
		} else {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					runLaterCallback.ready();										
				}
			},runLaterDelay);
		}
	}
}
