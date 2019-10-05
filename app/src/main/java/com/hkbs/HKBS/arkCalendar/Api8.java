package com.hkbs.HKBS.arkCalendar;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import com.hkbs.HKBS.CMain;
import com.hkbs.HKBS.R;
import com.hkbs.HKBS.arkUtil.MyUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
/*
 * Should not have CalendarContract (API-14) in this class
 */
public class Api8 {
    static final boolean DEBUG = MyUtil.DEBUG_APP && false;
    final static private String TAG = Api8.class.getSimpleName();
    
    public static class Api14{
    	public static final String ACCESS_LEVEL = "accessLevel";
    }
    
    public static final int METHOD_DEFAULT = 0;
    public static final int METHOD_ALERT = 1;
    public static final int METHOD_EMAIL = 2;
    public static final int METHOD_SMS = 3;
    public static final int METHOD_ALARM = 4;
    
    public static final String ORIGINAL_ID = "original_id";
    public static final String ORIGINAL_SYNC_ID = "original_sync_id";
    public static final String ORIGINAL_INSTANCE_TIME = "originalInstanceTime";
    public static final String ORIGINAL_ALL_DAY = "originalAllDay";
    
	public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String TITLE = "title";
    public static final String ALL_DAY = "allDay";
    public static final String EVENT_ID = "event_id";
    public static final String _ID = "_id";
    public static final String MINUTES = "minutes";
    public static final String METHOD = "method";
    public static final String DTSTART = "dtstart";
    public static final String DTEND = "dtend";
    public static final String EXDATE = "exdate";
    public static final String EXRULE = "exrule";
    public static final String DURATION = "duration";
    public static final String LAST_DATE = "lastDate";
    public static final String RDATE = "rdate";
    public static final String RRULE = "rrule";
    public static final String CALENDAR_ID = "calendar_id";
    public static final String DESCRIPTION = "description";
    public static final String HAS_ALARM = "hasAlarm";
    public static final String EVENT_LOCATION = "eventLocation";
    public static final String EVENT_TIMEZONE = "eventTimezone";
    public static final String VISIBILITY = "visibility";// default (0), confidential (1), private (2) public (3)
    public static final String ALARM_TIME = "alarmTime";
    public static final String SELF_ATTENDEE_STATUS = "selfAttendeeStatus";
    
    public static final String URI = "content://com.android.calendar";
    public static final String URI_REMINDERS = URI+"/reminders";
    public static final String URI_EVENTS = URI+"/events";
    public static final String URI_CALENDARS = URI+"/calendars";
    public static final String URI_INSTANCES = URI+"/instances/when";
    public static final String URI_CALENDAR_ALERTS = URI+"/calendar_alerts";
    public static final String ACTION_EVENT_REMINDER = "android.intent.action.EVENT_REMINDER";//android.provider.CalendarContract
    // CalendarAlert
    public static final String STATE = "state";
    public static final int STATE_SCHEDULED = 0; 
    public static final int STATE_FIRED = 1;
    public static final int STATE_DISMISSED = 2;
    public static final String RECEIVED_TIME = "receivedTime";
    public static final String NOTIFY_TIME = "notifyTime";
    public static final String ATTENDEE_STATUS = "attendeeStatus";
    // Attendees
    public static final int ATTENDEE_STATUS_NONE = 0; 
    public static final int ATTENDEE_STATUS_ACCEPTED = 1;
    public static final int ATTENDEE_STATUS_DECLINED = 2;
    public static final int ATTENDEE_STATUS_INVITED = 3;
    public static final int ATTENDEE_STATUS_TENTATIVE = 4;
    private static final String[] ALERT_PROJECTION_API8 = new String[] {
		  Api8._ID,                     // 0
		  Api8.EVENT_ID,                // 1
		  Api8.STATE,                   // 2
		  Api8.TITLE,                   // 3
		  Api8.EVENT_LOCATION,          // 4
		  Api8.SELF_ATTENDEE_STATUS,    // 5
		  Api8.ALL_DAY,                 // 6
		  Api8.ALARM_TIME,              // 7
		  Api8.MINUTES,                 // 8
		  Api8.BEGIN,                   // 9
		  Api8.END,                     // 10
		};
    public static final int ALERT_INDEX_ID = 0;
    public static final int ALERT_INDEX_EVENT_ID = 1;
    public static final int ALERT_INDEX_STATE = 2;
    public static final int ALERT_INDEX_TITLE = 3;
    public static final int ALERT_INDEX_EVENT_LOCATION = 4;
    public static final int ALERT_INDEX_SELF_ATTENDEE_STATUS = 5;
    public static final int ALERT_INDEX_ALL_DAY = 6;
    public static final int ALERT_INDEX_ALARM_TIME = 7;
    public static final int ALERT_INDEX_MINUTES = 8;
    public static final int ALERT_INDEX_BEGIN = 9;
    public static final int ALERT_INDEX_END = 10;
    public static final int ALERT_INDEX_DESCRIPTION = 11;

    public static final String ACTIVE_ALERTS_SELECTION = "(" + Api8.STATE + "=? OR "
            + Api8.STATE + "=?) AND " + Api8.ALARM_TIME + "<=";
    public static final String[] ACTIVE_ALERTS_SELECTION_ARGS = new String[] {
            Integer.toString(Api8.STATE_FIRED),
            Integer.toString(Api8.STATE_SCHEDULED)
    };
    public static final String ACTIVE_ALERTS_SORT = "begin DESC, end DESC";

	public Api8() {
		
	}
	@SuppressLint("InlinedApi")
	static public int getNewInstanceFlags(){
        return 	Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK;// For multiple window only
    }
	@SuppressLint("InlinedApi")
	static public int getTopActivityFlags(){
        return 	Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK;// Clean activities in task and start NEW
    }
	static public boolean updateAlertNotificationAPI8(Context context) {
        ContentResolver cr = context.getContentResolver();
        final long currentTime = System.currentTimeMillis();
        
        Cursor alertCursor = cr.query(Uri.parse(Api8.URI_CALENDAR_ALERTS), ALERT_PROJECTION_API8,
                (ACTIVE_ALERTS_SELECTION + currentTime), ACTIVE_ALERTS_SELECTION_ARGS,
                ACTIVE_ALERTS_SORT);
//        Cursor alertCursor = CalendarAlerts.query(cr, ALERT_PROJECTION, 
//        		(ACTIVE_ALERTS_SELECTION+ currentTime), ACTIVE_ALERTS_SELECTION_ARGS, 
//        		ACTIVE_ALERTS_SORT);

        if (alertCursor == null || alertCursor.getCount() == 0) {
            if (alertCursor != null) alertCursor.close();
            if (DEBUG) Log.d(TAG, "No fired or scheduled alerts");
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(0);
            return false;
        }

        if (DEBUG) Log.d(TAG, "alert count:" + alertCursor.getCount());
        
        long notificationEventID = 0;
        String notificationEventName = null;
        String notificationEventLocation = null;
        long notificationEventBegin = 0;
        int notificationEventStatus = 0;
        HashMap<Long, Long> eventIds = new HashMap<Long, Long>();
        int numReminders = 0;
        int numFired = 0;
        boolean notificationEventAllDay = true;
        try {
            while (alertCursor.moveToNext()) {
                final long alertId = alertCursor.getLong(ALERT_INDEX_ID);
                final long eventId = alertCursor.getLong(ALERT_INDEX_EVENT_ID);
                final int minutes = alertCursor.getInt(ALERT_INDEX_MINUTES);
                final String eventName = alertCursor.getString(ALERT_INDEX_TITLE);
                final String location = alertCursor.getString(ALERT_INDEX_EVENT_LOCATION);
                final boolean allDay = alertCursor.getInt(ALERT_INDEX_ALL_DAY) != 0;
                final int status = alertCursor.getInt(ALERT_INDEX_SELF_ATTENDEE_STATUS);
                final boolean declined = status == ATTENDEE_STATUS_DECLINED;
                final long beginTime = alertCursor.getLong(ALERT_INDEX_BEGIN);
                final long endTime = alertCursor.getLong(ALERT_INDEX_END);
                final Uri alertUri = ContentUris.withAppendedId(Uri.parse(Api8.URI_CALENDAR_ALERTS), alertId);
                final long alarmTime = alertCursor.getLong(ALERT_INDEX_ALARM_TIME);
                int state = alertCursor.getInt(ALERT_INDEX_STATE);

                if (DEBUG) MyUtil.log(TAG, "alarmTime:" + alarmTime + " alertId:" + alertId
                            + " eventId:" + eventId + " state: " + state + " minutes:" + minutes
                            + " declined:" + declined + " beginTime:" + beginTime
                            + " endTime:" + endTime);
                
                ContentValues values = new ContentValues();
                int newState = -1;

                // Uncomment for the behavior of clearing out alerts after the
                // events ended. b/1880369
                //
                // if (endTime < currentTime) {
                //     newState = CalendarAlerts.DISMISSED;
                // } else

                // Remove declined events and duplicate alerts for the same event
                if (!declined && eventIds.put(eventId, beginTime) == null) {
                    numReminders++;
                    if (state == Api8.STATE_SCHEDULED) {
                        newState = Api8.STATE_FIRED;
                        numFired++;

                        // Record the received time in the CalendarAlerts table.
                        // This is useful for finding bugs that cause alarms to be
                        // missed or delayed.
                        values.put(Api8.RECEIVED_TIME, currentTime);
                    }
                } else {
                    newState = Api8.STATE_DISMISSED;
                    if (DEBUG) {
                        if (!declined) Log.d(TAG, "dropping dup alert for event " + eventId);
                    }
                }

                // Update row if state changed
                if (newState != -1) {
                    values.put(Api8.STATE, newState);
                    state = newState;
                }

                if (state == Api8.STATE_FIRED) {
                    // Record the time posting to notification manager.
                    // This is used for debugging missed alarms.
                    values.put(Api8.NOTIFY_TIME, currentTime);
                }

                // Write row to if anything changed
                if (values.size() > 0) cr.update(alertUri, values, null, null);//<<---------------------

                if (state != Api8.STATE_FIRED) {
                    continue;
                }

                // Pick an Event title for the notification panel by the latest
                // alertTime and give prefer accepted events in case of ties.
                int newStatus;
                switch (status) {
                    case Api8.ATTENDEE_STATUS_ACCEPTED:
                        newStatus = 2;
                        break;
                    case Api8.ATTENDEE_STATUS_TENTATIVE:
                        newStatus = 1;
                        break;
                    default:
                        newStatus = 0;
                }

                // TODO Prioritize by "primary" calendar
                // Assumes alerts are sorted by begin time in reverse
                if (notificationEventName == null
                        || (notificationEventBegin <= beginTime &&
                                notificationEventStatus < newStatus)) {
                    notificationEventName = eventName;
                    notificationEventLocation = location;
                    notificationEventBegin = beginTime;
                    notificationEventStatus = newStatus;
                    notificationEventAllDay = allDay;
                    notificationEventID = eventId;
                }
            }
        } finally {
            if (alertCursor != null) {
                alertCursor.close();
            }
        }
        
//        SharedPreferences prefs = CalendarPreferenceActivity.getSharedPreferences(context);
//        String reminderType = prefs.getString(CalendarPreferenceActivity.KEY_ALERTS_TYPE,
//                CalendarPreferenceActivity.ALERT_TYPE_STATUS_BAR);
//
//        // TODO check for this before adding stuff to the alerts table.
//        if (reminderType.equals(CalendarPreferenceActivity.ALERT_TYPE_OFF)) {
//            if (DEBUG) {
//                Log.d(TAG, "alert preference is OFF");
//            }
//            return true;
//        }
        if (MyUtil.getPrefStr(MyUtil.PREF_ALERT, "Y").equals("N")){
        	if (DEBUG) MyUtil.log(TAG,"alert preference is OFF");
        	return true;
        }
        postNotificationAPI8(context, notificationEventName, notificationEventLocation,
                numReminders, numFired == 0,notificationEventBegin, notificationEventAllDay, notificationEventID 
                /* quiet update */);

        boolean isDoPopup = MyUtil.getPrefStr(MyUtil.PREF_ALERT_POPUP, "N").equals("Y");
        if (numFired > 0 && isDoPopup) {
            Intent alertIntent = new Intent();
            //alertIntent.setClass(context, AlertActivity.class);
            alertIntent.setClass(context, CMain.class);
            //DC:MW 
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alertIntent);
        }

        return true;
    }
    private static String getDateRange(Context context, long startMillis, boolean allDay) {
        // Format the second line which shows time and location.
        //
        // 1) Show time only for non-all day events
        // 2) No date for today
        // 3) Show "tomorrow" for tomorrow
        // 4) Show date for days beyond that
        String tz = AlertUtils.getTimeZone(context, null);
        Time time = new Time(tz);
        time.setToNow();
        int today = Time.getJulianDay(time.toMillis(false), time.gmtoff);
        time.set(startMillis);
        int eventDay = Time.getJulianDay(time.toMillis(false), time.gmtoff);

        int flags = DateUtils.FORMAT_ABBREV_ALL;
        if (!allDay) {
            flags |= DateUtils.FORMAT_SHOW_TIME;
            if (DateFormat.is24HourFormat(context)) {
                flags |= DateUtils.FORMAT_24HOUR;
            }
        } else {
            flags |= DateUtils.FORMAT_UTC;
        }

        if (eventDay > today + 1) {
            flags |= DateUtils.FORMAT_SHOW_DATE;
        }
        String dateRange = AlertUtils.formatDateRange(context, startMillis,startMillis, flags);
        if (!allDay && !tz.contentEquals(Time.getCurrentTimezone())) {
            // Assumes time was set to the current tz
            time.set(startMillis);
            boolean isDST = time.isDst != 0;
            dateRange = dateRange+" "+TimeZone.getTimeZone(tz).getDisplayName(isDST, TimeZone.SHORT, Locale.getDefault());
        }

        if (eventDay == today + 1) {
            // Tomorrow
            dateRange = dateRange + ", ";
            dateRange = dateRange + context.getString(R.string.tomorrow);
        }
        return dateRange;
    }
    private static void postNotificationAPI8(Context context, 
            String eventName, String location, int numReminders, boolean quietUpdate,
            long startMillis, boolean allDay, long eventID) {
        if (DEBUG) {
            Log.d(TAG, "###### creating new alarm notification, numReminders: " + numReminders
                    + (quietUpdate ? " QUIET" : " loud"));
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (numReminders == 0) {
            nm.cancel(0);
            return;
        }
        String content = getDateRange(context, startMillis, allDay);
        String loc;
        if (location != null && !TextUtils.isEmpty(loc = location.trim())) {
        	content=content+", "+loc;
        }
        
        Notification notification = AlertReceiver.makeNewAlertNotificationAPI8(context, eventName, content, numReminders, eventID);
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        
        // Quietly update notification bar. Nothing new. Maybe something just got deleted.
        if (!quietUpdate) {
            // Flash ticker in status bar
            notification.tickerText = eventName;
            if (!TextUtils.isEmpty(location)) {
                notification.tickerText = eventName + " - " + location;
            }

            // Generate either a pop-up dialog, status bar notification, or
            // neither. Pop-up dialog and status bar notification may include a
            // sound, an alert, or both. A status bar notification also includes
            // a toast.
//
//            // Find out the circumstances under which to vibrate.
//            // Migrate from pre-Froyo boolean setting if necessary.
//            String vibrateWhen; // "always" or "silent" or "never"
//            if(prefs.contains(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE_WHEN))
//            {
//                // Look up Froyo setting
//                vibrateWhen =
//                    prefs.getString(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE_WHEN, null);
//            } else if(prefs.contains(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE)) {
//                // No Froyo setting. Migrate pre-Froyo setting to new Froyo-defined value.
//                boolean vibrate =
//                    prefs.getBoolean(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE, false);
//                vibrateWhen = vibrate ?
//                    context.getString(R.string.prefDefault_alerts_vibrate_true) :
//                    context.getString(R.string.prefDefault_alerts_vibrate_false);
//            } else {
//                // No setting. Use Froyo-defined default.
//                vibrateWhen = context.getString(R.string.prefDefault_alerts_vibrateWhen);
//            }
//            boolean vibrateAlways = vibrateWhen.equals("always");
//            boolean vibrateSilent = vibrateWhen.equals("silent");
//            AudioManager audioManager =
//                (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//            boolean nowSilent =
//                audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
//
//            // Possibly generate a vibration
//            if (vibrateAlways || (vibrateSilent && nowSilent)) {
//                notification.defaults |= Notification.DEFAULT_VIBRATE;
//            }
            boolean doVibrate = getVibrate(context);
            if (doVibrate){
            	 notification.defaults |= Notification.DEFAULT_VIBRATE;
            }
            	
//            // Possibly generate a sound. If 'Silent' is chosen, the ringtone
//            // string will be empty.
//            String reminderRingtone = prefs.getString(
//                    CalendarPreferenceActivity.KEY_ALERTS_RINGTONE, null);
//            notification.sound = TextUtils.isEmpty(reminderRingtone) ? null : Uri
//                    .parse(reminderRingtone);
            String reminderRingtone = MyUtil.getPrefStr(MyUtil.PREF_ALERT_RINGTONE, "");
            notification.sound = TextUtils.isEmpty(reminderRingtone) ? null : Uri.parse(reminderRingtone);
            
        }

        nm.notify(0, notification);
    }
    static public boolean getVibrate(Context context){
    	boolean doVibrate=false;
        String vibrateWhen = MyUtil.getPrefStr(MyUtil.PREF_ALERT_VIBRATE_WHEN, "A");
        if (vibrateWhen.equals("A")){//Always
        	doVibrate=true;
        } else if (!vibrateWhen.equals("S")){//Silent
        	doVibrate=false;
        } else { // 
        	AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
    	       doVibrate=true;
    	    } else {
    	    	doVibrate=false;
    	    }  	
        }
        return doVibrate;
    }
}
