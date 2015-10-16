package com.hkbs.HKBS;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hkbs.HKBS.arkUtil.MyUtil;

import org.arkist.share.AxAlarm;

import java.util.Calendar;

public class MyBroadcast extends BroadcastReceiver {
    final static private String TAG = MyBroadcast.class.getSimpleName();
    final static public String URI_SCHEME = "CalendarWidget";
    static public boolean screenIsOn=true;
    static public boolean lastScreenState=true;
    static public boolean isStopUpdateWidget=false;
    static BroadcastReceiver mReceiver;
    
    static final public int REQUEST_WIDGET=2;
//	static final public int REQUEST_RECEIVER=5;
	
	static public final int NOTIFICATION_ID_TODAY_VERSE=1;
	
//	static private final String INTENT_ACTION_ALARM="org.arkist.cnote.ALARM";
//	static private final String INTENT_ACTION_NEWS="org.arkist.cnote.NEWS";
//	static public final String INTENT_ACTION_KILL="org.arkist.cnote.KILL";
//	static public final String INTENT_ACTION_TYPE="AnyStringHere";
	
	public MyBroadcast() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent==null) {
			MyUtil.logError(TAG, "onReceive....null");
			return;
		}
		MyUtil.log(TAG, "Broadcast.onReceive....");
		String intentAction = intent.getAction();
		if (intentAction!=null){
			if (intentAction.equals(Intent.ACTION_SCREEN_OFF)) {
				screenIsOn = false;
	            isStopUpdateWidget = true;	            
			} else if (intentAction.equals(Intent.ACTION_SCREEN_ON)) {
	        	screenIsOn = true;
	            isStopUpdateWidget = false;	            
	            //MyBroadcast.updateAllWidget(context);
			} else if ( intentAction.equals(Intent.ACTION_BOOT_COMPLETED) || 
						intentAction.equals(Intent.ACTION_PACKAGE_ADDED) || 
						intentAction.equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)){
				setReceiverOn(context);
				//MyBroadcast.updateAllWidget(context);
			} else if ( intentAction.equals(Intent.ACTION_SHUTDOWN) || 
						intentAction.equals(Intent.ACTION_PACKAGE_REMOVED)){
				setReceiverOff(context);			
			} else if (intentAction.equals(AxAlarm.MANIFEST_ACTION_DATE_CHANGE)){
				Log.i(TAG, "Action Alarm DateChange");
				//MyBroadcast.updateAllWidget(context);
			} else if (intentAction.equals(AxAlarm.MANIFEST_ACTION_ALARM)){
				Log.i(TAG, "Action Alarm Alarm");
				doDailyGoldSentence(context);
			} else {
				Log.i(TAG, "Others..."+intentAction);
				int requestCode = intent.getIntExtra(AxAlarm.EXTRA_BROADCAST_CODE, -1);
//				if (requestCode==AxAlarm.BROADTCAST_REQUEST_CODE){
//					doDailyGoldSentence(context);
//				} else 
				//if (requestCode==REQUEST_WIDGET){
					//MyBroadcast.updateAllWidget(context);
				//}
			}
        }
        MyBroadcast.updateAllWidget(context);
		lastScreenState=screenIsOn;
		
	}
	static public synchronized void setReceiverOn(Context context){
		if (mReceiver==null){
			mReceiver = new MyBroadcast();
            final IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
            context.getApplicationContext().registerReceiver(mReceiver, screenFilter);
		}
	}
	static public void setReceiverOff(Context context){
		if (mReceiver!=null) {
			context.unregisterReceiver(mReceiver);
			mReceiver=null;
		}
	}
	static public void updateAllWidget(Context context){
		updateAllWidgetByClass(context, CWidgetNormal.class);
        updateAllWidgetByClass(context, CWidgetLarge.class);
        updateAllWidgetByClass(context, CWidgetSmall.class);
        updateAllWidgetByClass(context, CWidgetBase.class);
	}
    static private void updateAllWidgetByClass(Context context, Class widgetClass){
        AppWidgetManager widgetMgr = AppWidgetManager.getInstance(context);
        int [] widgets = widgetMgr.getAppWidgetIds(new ComponentName(context,widgetClass));
        if (widgets==null) {
            MyUtil.log(TAG, "updateAllWidget "+widgetClass.getSimpleName()+" NULL");
        } else if (widgets.length==0){
            MyUtil.log(TAG, "updateAllWidget "+widgetClass.getSimpleName()+" 0");
        } else if (widgets.length!=0){
            MyUtil.log(TAG, "updateAllWidget "+widgetClass.getSimpleName()+" "+widgets.length);
            for (int i = 0; i < widgets.length; i++) {
                Intent intent = new Intent();
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                //intent.setData(ContentUris.withAppendedId(Uri.EMPTY, buttonID));
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgets[i]);
                intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, REQUEST_WIDGET);
                intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, "");
                // below statment should be different on every call so that it won't reuse and can't broadcase wrong variables
                //intentCounter++;
                intent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/" + REQUEST_WIDGET + "/"), String.valueOf(widgets[i])));
                context.sendBroadcast(intent);
            }
        }
    }
	/*
	 * 
	 * 
	 * 
	 *      DAILY ALARM
	 * 
	 * 
	 * 
	 */
    private void doDailyGoldSentence(Context context){
		final Calendar today = Calendar.getInstance();		
		int curYear = today.get(Calendar.YEAR);
		int curMonth = today.get(Calendar.MONTH);
		int curDay = today.get(Calendar.DAY_OF_MONTH);
		
		// get ContentValues from dailyBread file
		MyDailyBread mDailyBread = MyDailyBread.getInstance(context);
		ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);

		// GOLD TEXT
		String todayMsg = cv.getAsString(MyDailyBread.wGoldText);
		if (todayMsg==null || todayMsg.equals("")){			
			todayMsg = "提醒：請細讀今天金句"; 
		} else {
			todayMsg = todayMsg.replace("#", "\n")+" ["+cv.getAsString(MyDailyBread.wGoldVerse)+"]";
			todayMsg = "金句提醒:"+todayMsg;
		}
	    sendNotification(context, 
	    		todayMsg,	    		
	    		NOTIFICATION_ID_TODAY_VERSE,NOTIFICATION_ID_TODAY_VERSE);	    	
		System.out.println("Daily Reminder:Complete");
	}
	static private void sendNotification(Context context, String message,int notificationID, int notificationNbr){
		//if (android.os.Build.VERSION.SDK_INT >= 11) {
			api11sendNotification(context, message, notificationID, notificationNbr);
		//} else {
		//	api8sendNotification(context, message, notificationNbr);
		//}
		// Kill existing task (Perform like Intent.FLAG_ACTIVITY_CLEAR_TASK)
		//context.sendBroadcast(new Intent(INTENT_ACTION_KILL));
	}
	@SuppressLint("InlinedApi")
	static private void api11sendNotification(Context context, String message,int notificationID, int notificationNbr){
		Resources res = context.getResources();
	    String pAppName = res.getString(R.string.app_name);
		
		Intent newIntent = new Intent(context, CMain.class);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
					   		   Intent.FLAG_ACTIVITY_CLEAR_TASK |
					   		   Intent.FLAG_ACTIVITY_NEW_TASK);// Clean activities in task and start NEW			
		} else {
			newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
							   Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 
				notificationNbr, 
				newIntent,
				(notificationID==NOTIFICATION_ID_TODAY_VERSE)?PendingIntent.FLAG_UPDATE_CURRENT:0);
		
		NotificationCompat.Builder builder =  
	            new NotificationCompat.Builder(context)  
	            .setSmallIcon(R.drawable.ic_launcher)  
	            .setContentTitle("["+pAppName+"]")  
	            .setContentText(message);  
//		builder.setAutoCancel((notificationID==NOTIFICATION_ID_TODAY_VERSE)?true:false);
//		builder.setAutoCancel((notificationID==NOTIFICATION_ID_TODAY_VERSE)?false:true);
		builder.setAutoCancel(false);
		builder.setContentIntent(contentIntent);  
	    
	    // Add as notification  
	    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
	    manager.notify(notificationNbr, builder.build());	    
	}
}
