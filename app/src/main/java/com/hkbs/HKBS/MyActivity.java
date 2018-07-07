package com.hkbs.HKBS;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

//import com.google.analytics.tracking.android.EasyTracker;
import com.hkbs.HKBS.arkUtil.MyUtil;

public class MyActivity extends FragmentActivity {

	public MyActivity() {
		
	}
	@Override
	  protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        MyUtil.initMyUtil(getBaseContext());
	}
	@Override
	  public void onStart() {
	    super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    //EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
}
