package com.hkbs.HKBS;

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
        if (CMain.is_2016DayShown()) {
            ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.alarm_2016);
            ((ImageView) findViewById(R.id.imageView2)).setImageResource(R.drawable.bkg_2_2016);
        } else {
            ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.alarm_2015);
            ((ImageView) findViewById(R.id.imageView2)).setImageResource(R.drawable.bkg_2_2015);
        }
        tp = findViewById(R.id.timePicker1);
        int hour = MyUtil.getPrefInt("AlarmHour", 9);
        int minute = MyUtil.getPrefInt("AlarmMin", 0);
        // DC 202212
//        tp.setCurrentHour(hour);
//        tp.setCurrentMinute(minute);
        tp.setHour(hour);
        tp.setMinute(minute);
        cb = findViewById(R.id.checkBox1);
        cb.setChecked(MyUtil.getPrefStr("AlarmOn", "Y").equals("Y"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // force the timepicker to loose focus and the typed value is available !
        tp.clearFocus();
        // re-read the values, in my case i put them in a Time object.
        AxAlarm.setDailyAlarm(AlarmActivity.this,
                cb.isChecked() ? AxAlarm.MODE.ON : AxAlarm.MODE.OFF,
                tp.getHour(),
                tp.getMinute(), MyGoldBroadcast.class);
    }
}