package com.hkbs.HKBS;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.hkbs.HKBS.arkUtil.MyUtil;

import org.arkist.share.AxAlarm;

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
//		try {
        if (CMain.is_2016DayShown()){
            ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.alarm_2016);
            ((ImageView) findViewById(R.id.imageView2)).setImageResource(R.drawable.bkg_2_2016);
        } else {
            ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.alarm_2015);
            ((ImageView) findViewById(R.id.imageView2)).setImageResource(R.drawable.bkg_2_2015);
        }

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

        Button textView1 = (Button) findViewById(R.id.xmlAlarmOtherSetting);
//        TextView textView1 = (TextView) findViewById(R.id.xmlAlarmOtherSetting);
//        String str = "其他設定";
//        SpannableString content = new SpannableString(str);
//        content.setSpan(new UnderlineSpan(), 0, str.length(), 0);
//        textView1.setText(content);
//        textView1.setTextColor(getResources().getColor(R.color.blue));
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmActivity.this, SupportActivity.class);
                //startActivityForResult(intent, MyUtil.REQUEST_SUPPORT);
                AlarmActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
            }
        });
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