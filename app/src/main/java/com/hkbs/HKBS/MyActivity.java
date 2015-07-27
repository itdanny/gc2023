package com.hkbs.HKBS;

import com.google.analytics.tracking.android.EasyTracker;
import com.hkbs.HKBS.arkUtil.MyUtil;

import android.app.Activity;
import android.os.Bundle;

public class MyActivity extends Activity {

	public MyActivity() {
		
	}
	@Override
	  protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		MyUtil.initMyUtil(this);
	}
	@Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.	    
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
}
