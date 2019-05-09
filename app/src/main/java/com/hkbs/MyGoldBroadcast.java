package com.hkbs;

import android.app.Notification;
import android.app.NotificationChannel;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hkbs.HKBS.AxAlarm;
import com.hkbs.HKBS.CMain;
import com.hkbs.HKBS.CWidgetBase;
import com.hkbs.HKBS.CWidgetLarge;
import com.hkbs.HKBS.CWidgetMiddle;
import com.hkbs.HKBS.CWidgetNormal;
import com.hkbs.HKBS.CWidgetSmall;
import com.hkbs.HKBS.CWidgetXLarge;
import com.hkbs.HKBS.MyDailyBread;
import com.hkbs.HKBS.R;
import com.hkbs.HKBS.arkUtil.MyUtil;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyGoldBroadcast extends BroadcastReceiver {
    final static private boolean DEBUG=true && CMain.DEBUG;
    final static private String TAG = MyGoldBroadcast.class.getSimpleName();
    final static public String URI_SCHEME = "CalendarWidget";
    static public boolean screenIsOn=true;
    static public boolean lastScreenState=true;
    static public boolean isStopUpdateWidget=false;
    static BroadcastReceiver mReceiver;
    
    static final public int REQUEST_WIDGET=2;
	static final public int NOTIFICATION_ID_TODAY_VERSE=1;
	
	public MyGoldBroadcast() {
	}
	@Override
	public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Alarm.broadcast");
        if (intent==null) {
			if (DEBUG) MyUtil.logError(TAG, "Alarm.Broadcast.onReceive....null");
			return;
		}
        if (DEBUG) MyUtil.log(TAG, "Alarm.Broadcast.onReceive....");
		String intentAction = intent.getAction();
		if (intentAction!=null){
			if (intentAction.equals(Intent.ACTION_SCREEN_OFF)) {
				screenIsOn = false;
	            isStopUpdateWidget = true;	            
			} else if (intentAction.equals(Intent.ACTION_SCREEN_ON)) {
	        	screenIsOn = true;
	            isStopUpdateWidget = false;	            
	            //MyGoldBroadcast.updateAllWidget(context);
			} else if ( intentAction.equals(Intent.ACTION_BOOT_COMPLETED) || 
						intentAction.equals(Intent.ACTION_PACKAGE_ADDED) || 
						intentAction.equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)){
				setReceiverOn(context);
                AxAlarm.setDailyAlarm(context, AxAlarm.MODE.SET_DEFAULT, 9, 0, MyGoldBroadcast.class);
				//MyGoldBroadcast.updateAllWidget(context);
			} else if ( intentAction.equals(Intent.ACTION_SHUTDOWN) || 
						intentAction.equals(Intent.ACTION_PACKAGE_REMOVED)){
				setReceiverOff(context);
			} else if (intentAction.equals(AxAlarm.MANIFEST_ACTION_DATE_CHANGE)) {
                if (DEBUG) Log.i(TAG, "Action Alarm DateChange");
                //MyGoldBroadcast.updateAllWidget(context);
            } else if (intentAction.equals(AxAlarm.MANIFEST_ACTION + AxAlarm.REQUEST_CODE_DATE_CHANGE)){
                if (DEBUG) Log.i(TAG, "Action Alarm DateChange");
                //MyGoldBroadcast.updateAllWidget(context);
            } else if (intentAction.equals(AxAlarm.MANIFEST_ACTION + AxAlarm.REQUEST_CODE_ALARM)){
                if (DEBUG) Log.i(TAG, "Action Alarm Alarm");
                //AxAlarm.setDailyAlarm(context, AxAlarm.MODE.SET_DEFAULT, 9, 0);
                doDailyGoldSentence(context);
			} else if (intentAction.equals(AxAlarm.MANIFEST_ACTION_ALARM)){
                if (DEBUG) Log.i(TAG, "Action Alarm Alarm");
                //AxAlarm.setDailyAlarm(context, AxAlarm.MODE.SET_DEFAULT, 9, 0);
				doDailyGoldSentence(context);
                AxAlarm.setDailyAlarm(context, AxAlarm.MODE.SET_DEFAULT, 9, 0, MyGoldBroadcast.class);
			} else {
                if (DEBUG) Log.i(TAG, "Others..."+intentAction);
				int requestCode = intent.getIntExtra(AxAlarm.EXTRA_BROADCAST_CODE, -1);
//				if (requestCode==AxAlarm.BROADTCAST_REQUEST_CODE){
//					doDailyGoldSentence(context);
//				} else 
				//if (requestCode==REQUEST_WIDGET){
					//MyGoldBroadcast.updateAllWidget(context);
				//}
			}
        }
        MyGoldBroadcast.updateAllWidget(context);
		lastScreenState=screenIsOn;
		
	}
	static public synchronized void setReceiverOn(Context context){
		if (mReceiver==null){
			mReceiver = new MyGoldBroadcast();
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
        updateAllWidgetByClass(context, CWidgetXLarge.class);
        updateAllWidgetByClass(context, CWidgetLarge.class);
        updateAllWidgetByClass(context, CWidgetSmall.class);
        updateAllWidgetByClass(context, CWidgetMiddle.class);
        updateAllWidgetByClass(context, CWidgetBase.class);
	}
    static private void updateAllWidgetByClass(Context context, Class widgetClass){
        AppWidgetManager widgetMgr = AppWidgetManager.getInstance(context);
        int [] widgets = widgetMgr.getAppWidgetIds(new ComponentName(context,widgetClass));
        if (widgets==null) {
            if (DEBUG) MyUtil.log(TAG, "updateAllWidget "+widgetClass.getSimpleName()+" NULL");
        } else if (widgets.length==0){
            if (DEBUG) MyUtil.log(TAG, "updateAllWidget "+widgetClass.getSimpleName()+" 0");
        } else if (widgets.length!=0){
            if (DEBUG) MyUtil.log(TAG, "updateAllWidget "+widgetClass.getSimpleName()+" "+widgets.length);
            for (int i = 0; i < widgets.length; i++) {
//                Intent intent = new Intent();
//                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgets[i]);
//                intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, REQUEST_WIDGET);
//                intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, "");
//                intent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/" + REQUEST_WIDGET + "/"), String.valueOf(widgets[i])));
//                context.sendBroadcast(intent);
                //2015.11.03
                Intent intent = new Intent(context,widgetClass);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                // since it seems the onUpdate() is only fired on that:
                int[] ids = {widgets[i]};
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
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
        try {
            if (DEBUG) System.out.println("Daily Reminder:Start");
            final Calendar today = Calendar.getInstance();
            int curYear = today.get(Calendar.YEAR);
            int curMonth = today.get(Calendar.MONTH);
            int curDay = today.get(Calendar.DAY_OF_MONTH);

            // get ContentValues from dailyBread file

            MyDailyBread mDailyBread = MyDailyBread.getInstance(context);
            ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
            String todayMsg;
            if (cv==null || mDailyBread==null){
                todayMsg = context.getString(R.string.broadcast_download);
            } else {
                // GOLD TEXT
                todayMsg = cv.getAsString(MyDailyBread.wGoldText);
                if (todayMsg == null || todayMsg.equals("")) {
                    todayMsg = context.getString(R.string.broadcast_remind_today);
                } else {
                    todayMsg = todayMsg.replace("#", "\n") + " [" + cv.getAsString(MyDailyBread.wGoldVerse) + "]";
                    todayMsg = context.getString(R.string.broadcast_remind) + todayMsg;
                }
            }
            sendNotification(context,
                    todayMsg,
                    NOTIFICATION_ID_TODAY_VERSE, NOTIFICATION_ID_TODAY_VERSE);
            if (DEBUG) System.out.println("Daily Reminder:Complete");
        } catch (Exception e){
            // Do nothing
        }
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
	//@SuppressLint("InlinedApi")
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
                (notificationID == NOTIFICATION_ID_TODAY_VERSE) ? PendingIntent.FLAG_UPDATE_CURRENT : 0);
		if (contentIntent!=null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String channelId = "BroadcastChannel" + String.valueOf(notificationID);//"my_channel_01";

                NotificationChannel channel = new NotificationChannel(channelId, pAppName, NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.createNotificationChannel(channel);
                Notification.Builder oreoBuilder = new Notification.Builder(context,channelId);
                Notification notificationPopup = oreoBuilder.setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("[" + pAppName + "]")
                        .setChannelId(channelId)
                        .setAutoCancel(false)
                        .setContentIntent(contentIntent)
                        .build();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(notificationNbr, notificationPopup);
            } else {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("[" + pAppName + "]")
                                .setContentText(message);
                builder.setAutoCancel(false);
                builder.setContentIntent(contentIntent);
                // Add as notification
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(notificationNbr, builder.build());
            }
        }
	}
}