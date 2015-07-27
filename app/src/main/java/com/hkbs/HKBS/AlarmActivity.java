package com.hkbs.HKBS;

import org.arkist.share.AxAlarm;

import android.os.Bundle;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.hkbs.HKBS.arkUtil.MyUtil;

public class AlarmActivity extends MyActivity {
//	final static private String TAG = AlarmActivity.class.getSimpleName();
	
	TimePicker tp;
	CheckBox cb;
	public AlarmActivity() {
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_alarm);
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);	
//		try {
			imageView.setImageResource(R.drawable.alarm_2015);
//		} catch (Exception e){
//			BitmapFactory.Options options=new BitmapFactory.Options();
//		    options.inSampleSize = 4;
//		    Bitmap preview_bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.alarm_2015,options);
//		    imageView.setImageBitmap(preview_bitmap);
//		}
			
		tp = (TimePicker) findViewById(R.id.timePicker1);
		int hour = MyUtil.getPrefInt("AlarmHour",9);
		int minute = MyUtil.getPrefInt("AlarmMin",0);		
		tp.setCurrentHour(hour);
		tp.setCurrentMinute(minute);		
		
		cb = (CheckBox) findViewById(R.id.checkBox1);
		if (MyUtil.getPrefStr("AlarmOn", "Y").equals("Y")){
			cb.setChecked(true);
		} else {
			cb.setChecked(false);
		}
	}
	@Override
	public void onPause(){
		super.onPause();
		// force the timepicker to loose focus and the typed value is available !
		tp.clearFocus();
		// re-read the values, in my case i put them in a Time object.
		AxAlarm.setDailyAlarm(AlarmActivity.this,
				cb.isChecked()?AxAlarm.MODE.ON:AxAlarm.MODE.OFF,
				tp.getCurrentHour(),
				tp.getCurrentMinute());
	}	

}