package com.hkbs.HKBS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.arkist.share.AxTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AxAlarm {
	public AxAlarm() {
	}
	static public enum MODE {ON, OFF, SET_DEFAULT};
	final static public String MANIFEST_ACTION = "AxAlarm_"; // For Manifest + Request Code
	final static public String MANIFEST_ACTION_ALARM = "AxAlarm_Alarm"; // For Manifest (Used in GoodCalendar)
	final static public String MANIFEST_ACTION_DATE_CHANGE = "AxAlarm_DateChange"; // For Manifest
	//final static public String MANIFEST_ACTION_NOTIFY_1 = "AxAlarm_Notify_1"; // For Manifest;
	
	final static public String EXTRA_BROADCAST_CODE = "extraBroadcastCode";
	final static public int REQUEST_CODE_ALARM = 11;
	final static public int REQUEST_CODE_DATE_CHANGE = 12;
//	final static private int REQUEST_CODE_NOTIFY_1 = 21;

    final static private boolean DEBUG = true;
	final static private String TAG = AxAlarm.class.getSimpleName(); 
	final static private long MILLSECOND_IN_DAYS = 24*60*60*1000;
	final static private SimpleDateFormat sdfYYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
	final static private String PREF_ALARM_ON = "AlarmOn";
	final static private String PREF_ALARM_HOUR = "AlarmHour";
	final static private String PREF_ALARM_MIN = "AlarmMin";

    //2017.07.03 ANR lock problem
    final static private Object lockObject = new Object();
	static public void setDailyOnDateChange(Context context){
        synchronized (lockObject) {
            Intent intent = new Intent();
            intent.setAction(MANIFEST_ACTION_DATE_CHANGE);
            PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE_DATE_CHANGE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //long startTime = timeToStartFromNow(0, 0);
            //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, MILLSECOND_IN_DAYS, pi);
            Calendar cal = hhmm2Calendar(0, 0);
            am.cancel(pi);
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), MILLSECOND_IN_DAYS, pi);
            if (DEBUG) Log.i(TAG, "Alarm.dateChange at " + sdfYYYYMMDDHHMM.format(cal.getTime()));
        }
	}
	/*
	 * No need specify the broadcast class i.e.
	 * Intent intent = new Intent(context, MyGoldBroadcast.class);
	 */	
	static public PendingIntent getPendingIntent(Context context, int requestCode, Class broadcastClass){
        if (DEBUG) Log.i(TAG, "Set:"+MANIFEST_ACTION+String.valueOf(requestCode));
		Intent intent = new Intent(context,broadcastClass);
    	intent.setAction(MANIFEST_ACTION+String.valueOf(requestCode));    	
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);//PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}
	/* 
	 * Old Method
	 */
	static public PendingIntent getPendingIntent(Context context, String action, int requestCode){
		Intent intent = new Intent();
    	intent.setAction(action);    	
    	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}
	static public boolean getIsAlarmOn(){
		return AxTools.getPrefStr(PREF_ALARM_ON, "X").equals("Y");
	}
	static public String getHH_MM(){
		return AxTools.padZero(AxTools.getPrefInt(PREF_ALARM_HOUR,9),2)+":"+
			   AxTools.padZero(AxTools.getPrefInt(PREF_ALARM_MIN,0),2);	
	}
	/*
	 * SET ALARM PROBLEM .... PREF_ALARM .... MUST SPECIFY FOR WHICH NOTIFICATION
	 */
//	static public void setNotify(Context context, MODE mode, int setHour, int setMinute){
//		setAlarm(context, mode, setHour, setMinute, MANIFEST_ACTION_NOTIFY_1, REQUEST_CODE_NOTIFY_1);
//	}
//	public static void notifyCancel(Context c) {
//		offAlarm(c, MANIFEST_ACTION_NOTIFY_1, REQUEST_CODE_NOTIFY_1);
//	}
	static public void setDailyAlarm(Context context, MODE mode, int setHour, int setMinute, Class broadcastClass){
		//-- for Good Calendar --
        context = context.getApplicationContext();
		MODE finalMode = gc_setAlarm(context, mode, setHour, setMinute);
		alarmCancel(context);
		if (finalMode==MODE.ON){
            setHour=AxTools.getPrefInt(PREF_ALARM_HOUR,setHour);
            setMinute=AxTools.getPrefInt(PREF_ALARM_MIN,setMinute);
			//alarmOn(setHour, setMinute, MILLSECOND_IN_DAYS, context, getPendingIntent(context, MANIFEST_ACTION_ALARM, REQUEST_CODE_ALARM));
            alarmOn(setHour, setMinute, MILLSECOND_IN_DAYS, context, REQUEST_CODE_ALARM, broadcastClass);
		}
	}
	static public void alarmCancel(Context c) {
		//-- for Good Calendar --
		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
	    am.cancel(getPendingIntent(c, MANIFEST_ACTION_ALARM, REQUEST_CODE_ALARM));
	    if (DEBUG) Log.i(TAG, "Alarm.Cancel");
	}
	static private MODE gc_setAlarm(Context context, MODE mode, int setHour, int setMinute){
		//-- for Good Calendar --
		switch (mode){
		case SET_DEFAULT:
			if (AxTools.getPrefStr(PREF_ALARM_ON, "X").equals("X")){// Just Install; Not Yet Initialized
				AxTools.setPrefInt(PREF_ALARM_HOUR,setHour);
				AxTools.setPrefInt(PREF_ALARM_MIN,setMinute);
				AxTools.setPrefStr(PREF_ALARM_ON,"Y");
				mode=MODE.ON;
			} else {
				if (AxTools.getPrefStr(PREF_ALARM_ON,"Y").contentEquals("Y")){
					mode=MODE.ON;
					setHour=AxTools.getPrefInt(PREF_ALARM_HOUR,setHour);
					setMinute=AxTools.getPrefInt(PREF_ALARM_MIN,setMinute);
				} else {
					mode=MODE.OFF;
				}
			}
			break;
		case ON:
			AxTools.setPrefStr(PREF_ALARM_ON,"Y");
			AxTools.setPrefInt(PREF_ALARM_HOUR,setHour);
			AxTools.setPrefInt(PREF_ALARM_MIN,setMinute);			
			break;
		case OFF:
			AxTools.setPrefStr(PREF_ALARM_ON,"N");
			if (setHour!=-1) AxTools.setPrefInt(PREF_ALARM_HOUR,setHour);
			if (setMinute!=-1) AxTools.setPrefInt(PREF_ALARM_MIN,setMinute);
			break;
		}		 
		return mode;
    }
	static public void alarmOn(int hour, int minute, long repeatInterval, final Context c, int requestCode, Class broadcastClass) {
        PendingIntent pi = getPendingIntent(c, requestCode, broadcastClass);
		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
	    Calendar cal = hhmm2Calendar(hour,minute);
	    am.cancel(pi);
	    if (!(repeatInterval == -1)){
	    	am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), repeatInterval, pi);
	    } else {
	    	//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+50, pi);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } else if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP,  cal.getTimeInMillis(), pi);
            }
	    }
		if (DEBUG) Log.i(TAG, "Alarm.Set to "+sdfYYYYMMDDHHMM.format(cal.getTime())+" on "+sdfYYYYMMDDHHMM.format(Calendar.getInstance().getTime()));
	}
//	static public void alarmOff(Context c, int requestCode) {
//		alarmOff(c, getPendingIntent(c, requestCode));
//	}
	static private void alarmOff(Context c, PendingIntent pi) {		
		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
	    am.cancel(pi);
	    Log.i(TAG, "Alarm.Cancel");
	}
 
	static private Calendar hhmm2Calendar(int hh, int mm){
		Calendar today = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hh);
		cal.set(Calendar.MINUTE, mm);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		if (today.getTimeInMillis() > cal.getTimeInMillis()){//+delayMillSeconds
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return cal;
	}
	/*
     * to use elapsed realTime monotonic clock, and fire alarm at a specific time
     * we need to know the span between current time and the time of alarm.
     * then we can add this span to 'elapsedRealTime' to fire the alarm at that time
     * this way we can get alarms even when device is in sleep mood
    */
//	static private long timeToStartFromNow(int hour, int minute){
//	Time nowTime = new Time();
//    nowTime.setToNow();
//    Time startTime = new Time(nowTime);
//    startTime.hour = hour;
//    startTime.minute = minute;
//    startTime.second = 0;
//    //get the span from current time to alarm time 'startTime'
//    Time elapsedTime = new Time();
//    elapsedTime.set(SystemClock.elapsedRealtime());
//    elapsedTime.second=0;
//    return elapsedTime.toMillis(true) + spanInMillis(nowTime, startTime);
//}	
	//final static private String URI_SCHEME = "MyGoldBroadcast";
//	final static private int REQUEST_ALARM=1;
//	final static public String API8_ACTION_ALARM = "Api8ActionAlarm";
//	final static public String EXTRA_BROADCAST_CODE = "extraBroadcastCode";
	
//	public static ArrayList<String> alarmIntens = new ArrayList<String>();
//	public static int alarms=0;

	
//long startTime = timeToStartFromNow(hour, minute);
//if (!(repeatInterval == -1)){
//am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, repeatInterval, pi);
//} else {
//am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, pi);
//}
	
	//static private int delayMillSeconds = 10*1000; // 30 seconds
//	public static long spanInMillis(Time startTime, Time endTime) {
//    long diff = endTime.toMillis(true) - startTime.toMillis(true);
//    if (diff >= 0) {
//        return diff;
//    } else { // endTime in past; add 1 day
//    	endTime.monthDay = endTime.monthDay + 1; 
//    	diff = endTime.toMillis(true) - startTime.toMillis(true);
//    	return diff;
//    	//return AlarmManager.INTERVAL_DAY - Math.abs(diff);
//    }	    	
//}
	
//	} else {
//	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//	Intent intent = new Intent(context, MyGoldBroadcast.class);// Wake up a broadcast
//	PendingIntent pendingIntent;
//	int requestCode=REQUEST_ALARM;
//	intent.setAction(String.valueOf(requestCode));
////	intent.setAction(AlarmActivity.API8_ACTION_ALARM);
//	intent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(requestCode)));
//	intent.putExtra(EXTRA_BROADCAST_CODE, requestCode); 
//	pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//	if (!MyUtil.getPrefStr("AlarmOn","Y").equals("Y")){
//		am.cancel(pendingIntent);//SAME  data, type, class, and categories			
//		return;
//	} else {
//		am.cancel(pendingIntent);//SAME  data, type, class, and categories
//		Calendar cal = hhmm2Calendar(MyUtil.getPrefInt("AlarmHour",9),
//									 MyUtil.getPrefInt("AlarmMin" ,0));
//		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),24*60*60*1000, pendingIntent);
//		//am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),1*60*1000, pendingIntent);
//		//am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delayMillSeconds,24*60*60*1000, pendingIntent);
//		// 	should use ELAPSED_REALTIME_WAKEUPorRTC_WAKEUP, otherwise, when the screen is off, the alarm won't be triggered
////				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delayMillSeconds, 24*60*60*1000, pendingIntent);
////			} else {
//		MyUtil.log(TAG, "Alarm.Set "+MyUtil.getPrefInt("AlarmHour",9)+":"+MyUtil.getPrefInt("AlarmMin",0));
//		MyUtil.log(TAG, MyUtil.sdfYYYYMMDDHHMM.format(Calendar.getInstance().getTime())+" Vs "+MyUtil.sdfYYYYMMDDHHMM.format(cal.getTime()));
//	}
//}

}
