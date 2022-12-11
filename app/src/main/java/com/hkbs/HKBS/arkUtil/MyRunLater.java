package com.hkbs.HKBS.arkUtil;

import android.os.Handler;
import android.os.Looper;

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
        // DC 202212
		if (delay==0){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    runLaterCallback.ready();
                }
            });
		} else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runLaterCallback.ready();
                }
            }, runLaterDelay);
		}
	}
}
