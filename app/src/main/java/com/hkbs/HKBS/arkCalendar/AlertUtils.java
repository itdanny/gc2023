package com.hkbs.HKBS.arkCalendar;
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.CalendarAlerts;
import android.provider.CalendarContract.CalendarCache;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.hkbs.HKBS.CMain;
import com.hkbs.HKBS.R;
import com.hkbs.HKBS.arkUtil.MyUtil;

@SuppressLint("NewApi")
public class AlertUtils {
	public static final long SNOOZE_DELAY = 5 * 60 * 1000L;

    // We use one notification id for the expired events notification.  All
    // other notifications (the 'active' future/concurrent ones) use a unique ID.
    public static final int EXPIRED_GROUP_NOTIFICATION_ID = 0;

    public static final String EVENT_ID_KEY = "eventid";
    public static final String SHOW_EVENT_KEY = "showevent";
    public static final String EVENT_START_KEY = "eventstart";
    public static final String EVENT_END_KEY = "eventend";
    public static final String NOTIFICATION_ID_KEY = "notificationid";
    public static final String EVENT_IDS_KEY = "eventids";

    /**
     * Schedules an alarm intent with the system AlarmManager that will notify
     * listeners when a reminder should be fired. The provider will keep
     * scheduled reminders up to date but apps may use this to implement snooze
     * functionality without modifying the reminders table. Scheduled alarms
     * will generate an intent using {@link #ACTION_EVENT_REMINDER}.
     *
     * @param context A context for referencing system resources
     * @param manager The AlarmManager to use or null
     * @param alarmTime The time to fire the intent in UTC millis since epoch
     */
    public static void scheduleAlarm(Context context, AlarmManager manager, long alarmTime) {
        scheduleAlarmHelper(context, manager, alarmTime, false);
    }

    /**
     * Schedules the next alarm to silently refresh the notifications.  Note that if there
     * is a pending silent refresh alarm, it will be replaced with this one.
     */
    static void scheduleNextNotificationRefresh(Context context, AlarmManager manager,
            long alarmTime) {
        scheduleAlarmHelper(context, manager, alarmTime, true);
    }

    private static void scheduleAlarmHelper(Context context, AlarmManager manager, long alarmTime,
            boolean quietUpdate) {
        if (manager == null) {
            manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        int alarmType = AlarmManager.RTC_WAKEUP;
        Intent intent = new Intent(CalendarContract.ACTION_EVENT_REMINDER);
        intent.setClass(context, AlertReceiver.class);
        if (quietUpdate) {
            alarmType = AlarmManager.RTC;
        } else {
            // Set data field so we get a unique PendingIntent instance per alarm or else alarms
            // may be dropped.
            Uri.Builder builder = CalendarAlerts.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, alarmTime);
            intent.setData(builder.build());
        }

        intent.putExtra(CalendarContract.CalendarAlerts.ALARM_TIME, alarmTime);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        manager.set(alarmType, alarmTime, pi);
    }

    /**
     * Format the second line which shows time and location for single alert or the
     * number of events for multiple alerts
     *     1) Show time only for non-all day events
     *     2) No date for today
     *     3) Show "tomorrow" for tomorrow
     *     4) Show date for days beyond that
     */
    static String formatTimeLocation(Context context, long startMillis, boolean allDay,
            String location) {
        String tz = getTimeZone(context, null);
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

        if (eventDay < today || eventDay > today + 1) {
            flags |= DateUtils.FORMAT_SHOW_DATE;
        }

        StringBuilder sb = new StringBuilder(formatDateRange(context, startMillis,
                startMillis, flags));

        if (!allDay && !tz.contentEquals(Time.getCurrentTimezone())) {
            // Assumes time was set to the current tz
            time.set(startMillis);
            boolean isDST = time.isDst != 0;
            sb.append(" ").append(TimeZone.getTimeZone(tz).getDisplayName(
                    isDST, TimeZone.SHORT, Locale.getDefault()));
        }

        if (eventDay == today + 1) {
            // Tomorrow
            sb.append(", ");
            sb.append(context.getString(R.string.tomorrow));
        }

        String loc;
        if (location != null && !TextUtils.isEmpty(loc = location.trim())) {
            sb.append(", ");
            sb.append(loc);
        }
        return sb.toString();
    }
    public static Intent buildEventViewIntent(Context c, long eventId, long begin, long end) {
    	ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        // get the info from the currently running task
//    	List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE); 
//    	for (int i1 = 0; i1 < services.size(); i1++) { 
//    	    runningactivities.add(0,services.get(i1).topActivity.toString());  
//    	}
//    	if(runningactivities.contains("ComponentInfo{com.app/com.app.main.MyActivity}")==true){
//    	    Toast.makeText(getBaseContext(),"Activity is in foreground, active",1000).show(); 
//    	}
        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
        MyUtil.log("?", "CURRENT Activity ::"+ taskInfo.get(0).topActivity.getClassName());
        
    	//Intent intent = new Intent(c, EventActivity.class);
    	Intent intent = new Intent(c, CMain.class);
    	intent.addFlags(Api8.getTopActivityFlags());
    	
		intent.setData(ContentUris.withAppendedId(Uri.EMPTY, R.id.CALL_MAIN));//R.id.CALL_CALENDAR_OPEN 
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 999);
		intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, R.id.CALL_MAIN);		
		intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, String.valueOf(eventId));
//    	Intent i = new Intent(Intent.ACTION_VIEW);
//        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
//        builder.appendEncodedPath("events/" + eventId);
//        i.setData(builder.build());
//        i.setClass(c, EventActivity.class);
//        i.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin);
//        i.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);
        return intent;
    }
    public static ContentValues makeContentValues(long eventId, long begin, long end,
            long alarmTime, int minutes) {
        ContentValues values = new ContentValues();
        values.put(CalendarAlerts.EVENT_ID, eventId);
        values.put(CalendarAlerts.BEGIN, begin);
        values.put(CalendarAlerts.END, end);
        values.put(CalendarAlerts.ALARM_TIME, alarmTime);
        long currentTime = System.currentTimeMillis();
        values.put(CalendarAlerts.CREATION_TIME, currentTime);
        values.put(CalendarAlerts.RECEIVED_TIME, 0);
        values.put(CalendarAlerts.NOTIFY_TIME, 0);
        values.put(CalendarAlerts.STATE, CalendarAlerts.STATE_SCHEDULED);
        values.put(CalendarAlerts.MINUTES, minutes);
        return values;
    }
    private static final TimeZoneUtils mTZUtils = new TimeZoneUtils();
    /**
     * Gets the time zone that Calendar should be displayed in This is a helper
     * method to get the appropriate time zone for Calendar. If this is the
     * first time this method has been called it will initiate an asynchronous
     * query to verify that the data in preferences is correct. The callback
     * supplied will only be called if this query returns a value other than
     * what is stored in preferences and should cause the calling activity to
     * refresh anything that depends on calling this method.
     *
     * @param context The calling activity
     * @param callback The runnable that should execute if a query returns new
     *            values
     * @return The string value representing the time zone Calendar should
     *         display
     */
    public static String getTimeZone(Context context, Runnable callback) {
        return mTZUtils.getTimeZone(context, callback);
    }
    /**
     * Formats a date or a time range according to the local conventions.
     *
     * This formats a date/time range using Calendar's time zone and the
     * local conventions for the region of the device.
     *
     * If the {@link DateUtils#FORMAT_UTC} flag is used it will pass in
     * the UTC time zone instead.
     *
     * @param context the context is required only if the time is shown
     * @param startMillis the start time in UTC milliseconds
     * @param endMillis the end time in UTC milliseconds
     * @param flags a bit mask of options See
     * {@link DateUtils#formatDateRange(Context, Formatter, long, long, int, String) formatDateRange}
     * @return a string containing the formatted date/time range.
     */
    private static StringBuilder mSB = new StringBuilder(50);
    private static Formatter mF = new Formatter(mSB, Locale.getDefault());
    static public String formatDateRange(Context context, long startMillis,
            long endMillis, int flags) {
        String date;
        String tz;
        if ((flags & DateUtils.FORMAT_UTC) != 0) {
            tz = Time.TIMEZONE_UTC;
        } else {
            tz = getTimeZone(context, null);
        }
        synchronized (mSB) {
            mSB.setLength(0);
            date = DateUtils.formatDateRange(context, mF, startMillis, endMillis, flags,
                    tz).toString();
        }
        return date;
    }
    /**
     * This class contains methods specific to reading and writing time zone
     * values.
     */
    public static class TimeZoneUtils {
        private static final String[] TIMEZONE_TYPE_ARGS = { CalendarCache.KEY_TIMEZONE_TYPE };
        private static final String[] TIMEZONE_INSTANCES_ARGS =
                { CalendarCache.KEY_TIMEZONE_INSTANCES };
        public static final String[] CALENDAR_CACHE_POJECTION = {
                CalendarCache.KEY, CalendarCache.VALUE
        };

        private static StringBuilder mSB = new StringBuilder(50);
        private static Formatter mF = new Formatter(mSB, Locale.getDefault());
        private volatile static boolean mFirstTZRequest = true;
        private volatile static boolean mTZQueryInProgress = false;

        private volatile static boolean mUseHomeTZ = false;
        private volatile static String mHomeTZ = Time.getCurrentTimezone();

        private static HashSet<Runnable> mTZCallbacks = new HashSet<Runnable>();
        private static int mToken = 1;
        private static AsyncTZHandler mHandler;

        /**
         * This is the key used for writing whether or not a home time zone should
         * be used in the Calendar app to the Calendar Preferences.
         */
        public static final String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
        /**
         * This is the key used for writing the time zone that should be used if
         * home time zones are enabled for the Calendar app.
         */
        public static final String KEY_HOME_TZ = "preferences_home_tz";

        /**
         * This is a helper class for handling the async queries and updates for the
         * time zone settings in Calendar.
         */
        private class AsyncTZHandler extends AsyncQueryHandler {
            public AsyncTZHandler(ContentResolver cr) {
                super(cr);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                synchronized (mTZCallbacks) {
                    if (cursor == null) {
                        mTZQueryInProgress = false;
                        mFirstTZRequest = true;
                        return;
                    }

                    boolean writePrefs = false;
                    // Check the values in the db
                    int keyColumn = cursor.getColumnIndexOrThrow(CalendarCache.KEY);
                    int valueColumn = cursor.getColumnIndexOrThrow(CalendarCache.VALUE);
                    while(cursor.moveToNext()) {
                        String key = cursor.getString(keyColumn);
                        String value = cursor.getString(valueColumn);
                        if (TextUtils.equals(key, CalendarCache.KEY_TIMEZONE_TYPE)) {
                            boolean useHomeTZ = !TextUtils.equals(
                                    value, CalendarCache.TIMEZONE_TYPE_AUTO);
                            if (useHomeTZ != mUseHomeTZ) {
                                writePrefs = true;
                                mUseHomeTZ = useHomeTZ;
                            }
                        } else if (TextUtils.equals(
                                key, CalendarCache.KEY_TIMEZONE_INSTANCES_PREVIOUS)) {
                            if (!TextUtils.isEmpty(value) && !TextUtils.equals(mHomeTZ, value)) {
                                writePrefs = true;
                                mHomeTZ = value;
                            }
                        }
                    }
                    cursor.close();
                    if (writePrefs) {
                    	MyUtil.setPrefStr(KEY_HOME_TZ_ENABLED, mUseHomeTZ?"Y":"N");
                    	MyUtil.setPrefStr(KEY_HOME_TZ, mHomeTZ);
                    }

                    mTZQueryInProgress = false;
                    for (Runnable callback : mTZCallbacks) {
                        if (callback != null) {
                            callback.run();
                        }
                    }
                    mTZCallbacks.clear();
                }
            }
        }

        /**
         * The name of the file where the shared prefs for Calendar are stored
         * must be provided. All activities within an app should provide the
         * same preferences name or behavior may become erratic.
         *
         * @param prefsName
         */
        public TimeZoneUtils() {            
        }

        /**
         * Formats a date or a time range according to the local conventions.
         *
         * This formats a date/time range using Calendar's time zone and the
         * local conventions for the region of the device.
         *
         * If the {@link DateUtils#FORMAT_UTC} flag is used it will pass in
         * the UTC time zone instead.
         *
         * @param context the context is required only if the time is shown
         * @param startMillis the start time in UTC milliseconds
         * @param endMillis the end time in UTC milliseconds
         * @param flags a bit mask of options See
         * {@link DateUtils#formatDateRange(Context, Formatter, long, long, int, String) formatDateRange}
         * @return a string containing the formatted date/time range.
         */
        public String formatDateRange(Context context, long startMillis,
                long endMillis, int flags) {
            String date;
            String tz;
            if ((flags & DateUtils.FORMAT_UTC) != 0) {
                tz = Time.TIMEZONE_UTC;
            } else {
                tz = getTimeZone(context, null);
            }
            synchronized (mSB) {
                mSB.setLength(0);
                date = DateUtils.formatDateRange(context, mF, startMillis, endMillis, flags,
                        tz).toString();
            }
            return date;
        }

        /**
         * Writes a new home time zone to the db.
         *
         * Updates the home time zone in the db asynchronously and updates
         * the local cache. Sending a time zone of
         * {@link CalendarCache#TIMEZONE_TYPE_AUTO} will cause it to be set
         * to the device's time zone. null or empty tz will be ignored.
         *
         * @param context The calling activity
         * @param timeZone The time zone to set Calendar to, or
         * {@link CalendarCache#TIMEZONE_TYPE_AUTO}
         */
        public void setTimeZone(Context context, String timeZone) {
            if (TextUtils.isEmpty(timeZone)) {
                Log.d("?", "Empty time zone, nothing to be done.");
                return;
            }
            boolean updatePrefs = false;
            synchronized (mTZCallbacks) {
            	if (CalendarCache.TIMEZONE_TYPE_AUTO.equals(timeZone)) {
                    if (mUseHomeTZ) {
                        updatePrefs = true;
                    }
                    mUseHomeTZ = false;
                } else {
                    if (!mUseHomeTZ || !TextUtils.equals(mHomeTZ, timeZone)) {
                        updatePrefs = true;
                    }
                    mUseHomeTZ = true;
                    mHomeTZ = timeZone;
                }
            }
            if (updatePrefs) {
                // Write the prefs
                MyUtil.setPrefStr(KEY_HOME_TZ_ENABLED, mUseHomeTZ?"Y":"N");
                MyUtil.setPrefStr(KEY_HOME_TZ, mHomeTZ);

                // Update the db
                ContentValues values = new ContentValues();
                if (mHandler != null) {
                    mHandler.cancelOperation(mToken);
                }

                mHandler = new AsyncTZHandler(context.getContentResolver());

                // skip 0 so query can use it
                if (++mToken == 0) {
                    mToken = 1;
                }

                // Write the use home tz setting
                values.put(CalendarCache.VALUE, mUseHomeTZ ? CalendarCache.TIMEZONE_TYPE_HOME
                        : CalendarCache.TIMEZONE_TYPE_AUTO);
                mHandler.startUpdate(mToken, null, CalendarCache.URI, values, "key=?",
                        TIMEZONE_TYPE_ARGS);

                // If using a home tz write it to the db
                if (mUseHomeTZ) {
                    ContentValues values2 = new ContentValues();
                    values2.put(CalendarCache.VALUE, mHomeTZ);
                    mHandler.startUpdate(mToken, null, CalendarCache.URI, values2,
                            "key=?", TIMEZONE_INSTANCES_ARGS);
                }
            }
        }

        /**
         * Gets the time zone that Calendar should be displayed in
         *
         * This is a helper method to get the appropriate time zone for Calendar. If this
         * is the first time this method has been called it will initiate an asynchronous
         * query to verify that the data in preferences is correct. The callback supplied
         * will only be called if this query returns a value other than what is stored in
         * preferences and should cause the calling activity to refresh anything that
         * depends on calling this method.
         *
         * @param context The calling activity
         * @param callback The runnable that should execute if a query returns new values
         * @return The string value representing the time zone Calendar should display
         */
        public String getTimeZone(Context context, Runnable callback) {
            synchronized (mTZCallbacks){
                if (mFirstTZRequest) {
                    mTZQueryInProgress = true;
                    mFirstTZRequest = false;

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    mUseHomeTZ = prefs.getBoolean(KEY_HOME_TZ_ENABLED, false);
                    mHomeTZ = prefs.getString(KEY_HOME_TZ, Time.getCurrentTimezone());

                    // When the async query returns it should synchronize on
                    // mTZCallbacks, update mUseHomeTZ, mHomeTZ, and the
                    // preferences, set mTZQueryInProgress to false, and call all
                    // the runnables in mTZCallbacks.
                    if (mHandler == null) {
                        mHandler = new AsyncTZHandler(context.getContentResolver());
                    }
                    mHandler.startQuery(0, context, CalendarCache.URI, CALENDAR_CACHE_POJECTION,
                            null, null, null);
                }
                if (mTZQueryInProgress) {
                    mTZCallbacks.add(callback);
                }
            }
            return mUseHomeTZ ? mHomeTZ : Time.getCurrentTimezone();
        }

        /**
         * Forces a query of the database to check for changes to the time zone.
         * This should be called if another app may have modified the db. If a
         * query is already in progress the callback will be added to the list
         * of callbacks to be called when it returns.
         *
         * @param context The calling activity
         * @param callback The runnable that should execute if a query returns
         *            new values
         */
        public void forceDBRequery(Context context, Runnable callback) {
            synchronized (mTZCallbacks){
                if (mTZQueryInProgress) {
                    mTZCallbacks.add(callback);
                    return;
                }
                mFirstTZRequest = true;
                getTimeZone(context, callback);
            }
        }
    }
}
