package com.hkbs.HKBS.arkCalendar;
/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * limitations under the License.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;

import com.hkbs.HKBS.CMain;
import com.hkbs.HKBS.R;
import com.hkbs.HKBS.arkCalendar.AlertService.NotificationWrapper;
import com.hkbs.HKBS.arkUtil.MyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Receives android.intent.action.EVENT_REMINDER intents and handles
 * event reminders.  The intent URI specifies an alert id in the
 * CalendarAlerts database table.  This class also receives the
 * BOOT_COMPLETED intent so that it can add a status bar notification
 * if there are Calendar event alarms that have not been dismissed.
 * It also receives the TIME_CHANGED action so that it can fire off
 * snoozed alarms that have become ready.  The real work is done in
 * the AlertService class.
 *
 * To trigger this code after pushing the apk to device:
 * adb shell am broadcast -a "android.intent.action.EVENT_REMINDER"
 *    -n "com.android.calendar/.alerts.AlertReceiver"
 */
@SuppressLint("NewApi")
public class AlertReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = true;
    private static final String TAG = "AlertReceiver";

    private static final String DELETE_ALL_ACTION = "org.arkist.calendar.DELETEALL";
    private static final String MAIL_ACTION = "org.arkist.calendar.MAIL";
    private static final String EXTRA_EVENT_ID = "eventid";

    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    private static final Pattern mBlankLinePattern = Pattern.compile("^\\s*$[\n\r]",
            Pattern.MULTILINE);

    public static final String ACTION_DISMISS_OLD_REMINDERS = "removeOldReminders";
    private static final int NOTIFICATION_DIGEST_MAX_LENGTH = 3;
    private static final String DELETE_ACTION = "delete";

    //private static Handler sAsyncHandler;
    static {
        HandlerThread thr = new HandlerThread("AlertReceiver async");
        thr.start();
        //sAsyncHandler = new Handler(thr.getLooper());
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DEBUG) MyUtil.log(TAG, "onReceive: a=" + intent.getAction() + " " + intent.toString());
        if (android.os.Build.VERSION.SDK_INT < AlertService.NEW_API_VERSION) {
            if (DELETE_ACTION.equals(intent.getAction())) {

                /* The user has clicked the "Clear All Notifications"
                 * buttons so dismiss all Calendar alerts.
                 */
                // TODO Grab a wake lock here?
                Intent serviceIntent = new Intent(context, DismissAllAlarmsServiceAPI8.class);
                context.startService(serviceIntent);
            } else {
                Intent i = new Intent();
                i.setClass(context, AlertService.class);
                i.putExtras(intent);
                i.putExtra("action", intent.getAction());
                Uri uri = intent.getData();

                // This intent might be a BOOT_COMPLETED so it might not have a Uri.
                if (uri != null) {
                    i.putExtra("uri", uri.toString());
                }
                beginStartingServiceAPI8(context, i);
            }

        } else {
            if (DELETE_ALL_ACTION.equals(intent.getAction())) {
	            /* The user has clicked the "Clear All Notifications"
	             * buttons so dismiss all Calendar alerts.
	             */
                // TODO Grab a wake lock here?
                Intent serviceIntent = new Intent(context, DismissAlarmsService.class);
                context.startService(serviceIntent);
            } else if (MAIL_ACTION.equals(intent.getAction())) {
                // Close the notification shade.
                Intent closeNotificationShadeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(closeNotificationShadeIntent);

                // Now start the email intent.
                final long eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
                if (eventId != -1) {
                    Intent i = new Intent(context, QuickResponseActivity.class);
                    i.putExtra(QuickResponseActivity.EXTRA_EVENT_ID, eventId);
                    //DC:MW
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            } else { // onReceive...startService...onCreate...startCommand...updateAlertNotification....
                Intent i = new Intent();
                i.setClass(context, AlertService.class); // <<<--------
                i.putExtras(intent);
                i.putExtra("action", intent.getAction());
                Uri uri = intent.getData();

                // This intent might be a BOOT_COMPLETED so it might not have a Uri.
                if (uri != null) {
                    i.putExtra("uri", uri.toString());
                }
                beginStartingService(context, i);
            }
        }
    }

    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (DEBUG) MyUtil.log(TAG, "beginStartingService");
            if (mStartingService == null) {
                PowerManager pm =
                        (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "gc2015:StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (DEBUG) MyUtil.log(TAG, "finishStartingService");
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                }
            }
        }
    }

    private static PendingIntent createClickEventIntent(Context context, long eventId,
                                                        long startMillis, long endMillis, int notificationId) {
        if (DEBUG) MyUtil.log(TAG, "createClickEventIntent");
        return createDismissAlarmsIntent(context, eventId, startMillis, endMillis, notificationId,
                "org.arkist.calendar.CLICK", true);
    }

    private static PendingIntent createDeleteEventIntent(Context context, long eventId,
                                                         long startMillis, long endMillis, int notificationId) {
        if (DEBUG) MyUtil.log(TAG, "createDeketeEventIntent");
        return createDismissAlarmsIntent(context, eventId, startMillis, endMillis, notificationId,
                "org.arkist.calendar.DELETE", false);
    }

    private static PendingIntent createDismissAlarmsIntent(Context context, long eventId,
                                                           long startMillis, long endMillis, int notificationId, String action,
                                                           boolean showEvent) {
        if (DEBUG) MyUtil.log(TAG, "createDismissAlarmsIntent");
        Intent intent = new Intent();
        intent.setClass(context, DismissAlarmsService.class);
        intent.putExtra(AlertUtils.EVENT_ID_KEY, eventId);
        intent.putExtra(AlertUtils.EVENT_START_KEY, startMillis);
        intent.putExtra(AlertUtils.EVENT_END_KEY, endMillis);
        intent.putExtra(AlertUtils.SHOW_EVENT_KEY, showEvent);
        intent.putExtra(AlertUtils.NOTIFICATION_ID_KEY, notificationId);

        // Must set a field that affects Intent.filterEquals so that the resulting
        // PendingIntent will be a unique instance (the 'extras' don't achieve this).
        // This must be unique for the click event across all reminders (so using
        // event ID + startTime should be unique).  This also must be unique from
        // the delete event (which also uses DismissAlarmsService).
        Uri.Builder builder = Events.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, eventId);
        ContentUris.appendId(builder, startMillis);
        intent.setData(builder.build());
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createSnoozeIntent(Context context, long eventId,
                                                    long startMillis, long endMillis, int notificationId) {
        if (DEBUG) MyUtil.log(TAG, "createSnoozeIntent");
        Intent intent = new Intent();
        intent.setClass(context, SnoozeAlarmsService.class);
        intent.putExtra(AlertUtils.EVENT_ID_KEY, eventId);
        intent.putExtra(AlertUtils.EVENT_START_KEY, startMillis);
        intent.putExtra(AlertUtils.EVENT_END_KEY, endMillis);
        intent.putExtra(AlertUtils.NOTIFICATION_ID_KEY, notificationId);

        Uri.Builder builder = Events.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, eventId);
        ContentUris.appendId(builder, startMillis);
        intent.setData(builder.build());
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("InlinedApi")
    private static PendingIntent createAlertActivityIntent(Context context) {
        if (DEBUG) MyUtil.log(TAG, "createAlertIntent");
        Intent clickIntent = new Intent();
        clickIntent.setClass(context, CMain.class);
        //DC:MW 
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, clickIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static NotificationWrapper makeBasicNotification(Context context, String title,
                                                            String summaryText, long startMillis, long endMillis, long eventId,
                                                            int notificationId, boolean doPopup) {
        if (DEBUG) MyUtil.log(TAG, "makeBasicNotification");
        Notification n = makeBasicNotificationBuilder(context, title, summaryText, startMillis,
                endMillis, eventId, notificationId, doPopup, false, false).build();

        return new NotificationWrapper(n, notificationId, eventId, startMillis, endMillis, doPopup);
    }

    private static Notification.Builder makeBasicNotificationBuilder(Context context, String title,
                                                                     String summaryText, long startMillis, long endMillis, long eventId,
                                                                     int notificationId, boolean doPopup, boolean highPriority, boolean addActionButtons) {
        if (DEBUG) MyUtil.log(TAG, "makeBasicNotificationBuilder");
        Resources resources = context.getResources();
        if (title == null || title.length() == 0) {
            title = resources.getString(R.string.no_title_label);
        }

        // Create an intent triggered by clicking on the status icon, that dismisses the
        // notification and shows the event.
        PendingIntent clickIntent = createClickEventIntent(context, eventId, startMillis, endMillis, notificationId);

        // Create a delete intent triggered by dismissing the notification.
        PendingIntent deleteIntent = createDeleteEventIntent(context, eventId, startMillis, endMillis, notificationId);

        // Create the base notification.
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(summaryText);
        //notificationBuilder.setSmallIcon(R.drawable.stat_notify_calendar);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setContentIntent(clickIntent);
        notificationBuilder.setDeleteIntent(deleteIntent);
        if (addActionButtons) {
            // Create a snooze button.  TODO: change snooze to 10 minutes.
            if (DEBUG) MyUtil.log(TAG, "AddSnoozeIntent");
            PendingIntent snoozeIntent = createSnoozeIntent(context, eventId, startMillis,
                    endMillis, notificationId);
            notificationBuilder.addAction(R.drawable.ic_alarm_holo_dark,
                    resources.getString(R.string.snooze_label), snoozeIntent);

            // Create an email button.
            PendingIntent emailIntent = createBroadcastMailIntent(context, eventId, title);
            if (emailIntent != null) {
                notificationBuilder.addAction(R.drawable.ic_menu_email_holo_dark,
                        resources.getString(R.string.email_guests_label), emailIntent);
            }

        }
        if (doPopup) {
            PendingIntent pendingIntent = createAlertActivityIntent(context);
            if (pendingIntent != null) {
                notificationBuilder.setFullScreenIntent(pendingIntent, true);
            }
        }
        // Turn off timestamp.
        notificationBuilder.setWhen(0);
        // Setting to a higher priority will encourage notification manager to expand the
        // notification.
        if (highPriority) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        } else {
            notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        return notificationBuilder;
    }

    /**
     * Creates an expanding notification.  The initial expanded state is decided by
     * the notification manager based on the priority.
     */
    public static NotificationWrapper makeExpandingNotification(Context context, String title,
                                                                String summaryText, String description, long startMillis, long endMillis, long eventId,
                                                                int notificationId, boolean doPopup, boolean highPriority) {

        Notification.Builder basicBuilder = makeBasicNotificationBuilder(context, title,
                summaryText, startMillis, endMillis, eventId, notificationId,
                doPopup, highPriority, true);

        // Create an expanded notification
        Notification.BigTextStyle expandedBuilder = new Notification.BigTextStyle(
                basicBuilder);
        if (description != null) {
            description = mBlankLinePattern.matcher(description).replaceAll("");
            description = description.trim();
        }
        CharSequence text;
        if (TextUtils.isEmpty(description)) {
            text = summaryText;
        } else {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append(summaryText);
            stringBuilder.append("\n\n");
            stringBuilder.setSpan(new RelativeSizeSpan(0.5f), summaryText.length(),
                    stringBuilder.length(), 0);
            stringBuilder.append(description);
            text = stringBuilder;
        }
        expandedBuilder.bigText(text);

        return new NotificationWrapper(expandedBuilder.build(), notificationId, eventId,
                startMillis, endMillis, doPopup);
    }

    /**
     * Creates an expanding digest notification for expired events.
     */
    public static NotificationWrapper makeDigestNotification(Context context,
                                                             ArrayList<AlertService.NotificationInfo> notificationInfos, String digestTitle,
                                                             boolean expandable) {
        if (notificationInfos == null || notificationInfos.size() < 1) {
            return null;
        }

        Resources res = context.getResources();
        int numEvents = notificationInfos.size();
        long[] eventIds = new long[notificationInfos.size()];
        for (int i = 0; i < notificationInfos.size(); i++) {
            eventIds[i] = notificationInfos.get(i).eventId;
        }

        // Create an intent triggered by clicking on the status icon that shows the alerts list.
        PendingIntent pendingClickIntent = createAlertActivityIntent(context);

        // Create an intent triggered by dismissing the digest notification that clears all
        // expired events.
        Intent deleteIntent = new Intent();
        deleteIntent.setClass(context, DismissAlarmsService.class);
        deleteIntent.setAction(DELETE_ALL_ACTION);
        deleteIntent.putExtra(AlertUtils.EVENT_IDS_KEY, eventIds);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(context, 0, deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (digestTitle == null || digestTitle.length() == 0) {
            digestTitle = res.getString(R.string.no_title_label);
        }

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setContentText(digestTitle);
        notificationBuilder.setSmallIcon(R.drawable.stat_notify_calendar_multiple);
        notificationBuilder.setContentIntent(pendingClickIntent);
        notificationBuilder.setDeleteIntent(pendingDeleteIntent);
        String nEventsStr = res.getQuantityString(R.plurals.Nevents, numEvents, numEvents);
        notificationBuilder.setContentTitle(nEventsStr);

        // Set to min priority to encourage the notification manager to collapse it.
        notificationBuilder.setPriority(Notification.PRIORITY_MIN);

        Notification n;

        if (expandable) {
            // Multiple reminders.  Combine into an expanded digest notification.
            Notification.InboxStyle expandedBuilder = new Notification.InboxStyle(
                    notificationBuilder);
            int i = 0;
            for (AlertService.NotificationInfo info : notificationInfos) {
                if (i < NOTIFICATION_DIGEST_MAX_LENGTH) {
                    String name = info.eventName;
                    if (TextUtils.isEmpty(name)) {
                        name = context.getResources().getString(R.string.no_title_label);
                    }
                    String timeLocation = AlertUtils.formatTimeLocation(context, info.startMillis,
                            info.allDay, info.location);

                    TextAppearanceSpan primaryTextSpan = new TextAppearanceSpan(context,
                            R.style.NotificationPrimaryText);
                    TextAppearanceSpan secondaryTextSpan = new TextAppearanceSpan(context,
                            R.style.NotificationSecondaryText);

                    // Event title in bold.
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                    stringBuilder.append(name);
                    stringBuilder.setSpan(primaryTextSpan, 0, stringBuilder.length(), 0);
                    stringBuilder.append("  ");

                    // Followed by time and location.
                    int secondaryIndex = stringBuilder.length();
                    stringBuilder.append(timeLocation);
                    stringBuilder.setSpan(secondaryTextSpan, secondaryIndex, stringBuilder.length(),
                            0);
                    expandedBuilder.addLine(stringBuilder);
                    i++;
                } else {
                    break;
                }
            }

            // If there are too many to display, add "+X missed events" for the last line.
            int remaining = numEvents - i;
            if (remaining > 0) {
                String nMoreEventsStr = res.getQuantityString(R.plurals.N_remaining_events,
                        remaining, remaining);
                // TODO: Add highlighting and icon to this last entry once framework allows it.
                expandedBuilder.setSummaryText(nMoreEventsStr);
            }

            // Remove the title in the expanded form (redundant with the listed items).
            expandedBuilder.setBigContentTitle("");

            n = expandedBuilder.build();
        } else {
            n = notificationBuilder.build();
        }

        NotificationWrapper nw = new NotificationWrapper(n);
        if (AlertService.DEBUG) {
            for (AlertService.NotificationInfo info : notificationInfos) {
                nw.add(new NotificationWrapper(null, 0, info.eventId, info.startMillis,
                        info.endMillis, false));
            }
        }
        return nw;
    }

    private static final String[] ATTENDEES_PROJECTION = new String[]{
            Attendees.ATTENDEE_EMAIL,           // 0
            Attendees.ATTENDEE_STATUS,          // 1
    };
    private static final int ATTENDEES_INDEX_EMAIL = 0;
    private static final int ATTENDEES_INDEX_STATUS = 1;
    private static final String ATTENDEES_WHERE = Attendees.EVENT_ID + "=?";
    private static final String ATTENDEES_SORT_ORDER = Attendees.ATTENDEE_NAME + " ASC, "
            + Attendees.ATTENDEE_EMAIL + " ASC";

    private static final String[] EVENT_PROJECTION = new String[]{
            Calendars.OWNER_ACCOUNT, // 0
            Calendars.ACCOUNT_NAME,  // 1
            Events.TITLE,            // 2
    };
    private static final int EVENT_INDEX_OWNER_ACCOUNT = 0;
    private static final int EVENT_INDEX_ACCOUNT_NAME = 1;
    private static final int EVENT_INDEX_TITLE = 2;

    private static Cursor getEventCursor(Context context, long eventId) {
        return context.getContentResolver().query(
                ContentUris.withAppendedId(Events.CONTENT_URI, eventId), EVENT_PROJECTION,
                null, null, null);
    }

    private static Cursor getAttendeesCursor(Context context, long eventId) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return context.getContentResolver().query(Attendees.CONTENT_URI,
                ATTENDEES_PROJECTION, ATTENDEES_WHERE, new String[]{Long.toString(eventId)},
                ATTENDEES_SORT_ORDER);
    }

    /**
     * Creates a broadcast pending intent that fires to AlertReceiver when the email button
     * is clicked.
     */
    private static PendingIntent createBroadcastMailIntent(Context context, long eventId,
            String eventTitle) {
        // Query for viewer account.
        String syncAccount = null;
        Cursor eventCursor = getEventCursor(context, eventId);
        try {
            if (eventCursor != null && eventCursor.moveToFirst()) {
                syncAccount = eventCursor.getString(EVENT_INDEX_ACCOUNT_NAME);
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }

        // Query attendees to see if there are any to email.
        Cursor attendeesCursor = getAttendeesCursor(context, eventId);
        try {
            if (attendeesCursor != null && attendeesCursor.moveToFirst()) {
                do {
                    String email = attendeesCursor.getString(ATTENDEES_INDEX_EMAIL);
                    if (isEmailableFrom(email, syncAccount)) {
                        // Send intent back to ourself first for a couple reasons:
                        // 1) Workaround issue where clicking action button in notification does
                        //    not automatically close the notification shade.
                        // 2) Attendees list in email will always be up to date.
                        Intent broadcastIntent = new Intent(MAIL_ACTION);
                        broadcastIntent.setClass(context, AlertReceiver.class);
                        broadcastIntent.putExtra(EXTRA_EVENT_ID, eventId);
                        return PendingIntent.getBroadcast(context,
                                Long.valueOf(eventId).hashCode(), broadcastIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                    }
                } while (attendeesCursor.moveToNext());
            }
            return null;

        } finally {
            if (attendeesCursor != null) {
                attendeesCursor.close();
            }
        }
    }

    /**
     * Creates an Intent for emailing the attendees of the event.  Returns null if there
     * are no emailable attendees.
     */
    @SuppressLint("InlinedApi")
    static Intent createEmailIntent(Context context, long eventId, String body) {
        // TODO: Refactor to move query part into Utils.createEmailAttendeeIntent, to
        // be shared with EventInfoFragment.

        // Query for the owner account(s).
        String ownerAccount = null;
        String syncAccount = null;
        String eventTitle = null;
        Cursor eventCursor = getEventCursor(context, eventId);
        try {
            if (eventCursor != null && eventCursor.moveToFirst()) {
                ownerAccount = eventCursor.getString(EVENT_INDEX_OWNER_ACCOUNT);
                syncAccount = eventCursor.getString(EVENT_INDEX_ACCOUNT_NAME);
                eventTitle = eventCursor.getString(EVENT_INDEX_TITLE);
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
        if (TextUtils.isEmpty(eventTitle)) {
            eventTitle = context.getResources().getString(R.string.no_title_label);
        }

        // Query for the attendees.
        List<String> toEmails = new ArrayList<String>();
        List<String> ccEmails = new ArrayList<String>();
        Cursor attendeesCursor = getAttendeesCursor(context, eventId);
        try {
            if (attendeesCursor != null && attendeesCursor.moveToFirst()) {
                do {
                    int status = attendeesCursor.getInt(ATTENDEES_INDEX_STATUS);
                    String email = attendeesCursor.getString(ATTENDEES_INDEX_EMAIL);
                    switch(status) {
                        case Attendees.ATTENDEE_STATUS_DECLINED:
                            addIfEmailable(ccEmails, email, syncAccount);
                            break;
                        default:
                            addIfEmailable(toEmails, email, syncAccount);
                    }
                } while (attendeesCursor.moveToNext());
            }
        } finally {
            if (attendeesCursor != null) {
                attendeesCursor.close();
            }
        }

        Intent intent = null;
        if (ownerAccount != null && (toEmails.size() > 0 || ccEmails.size() > 0)) {
            intent = createEmailAttendeesIntent(context.getResources(), eventTitle, body,
                    toEmails, ccEmails, ownerAccount);
        }

        if (intent == null) {
            return null;
        }
        else {
        	//DC:MW 
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return intent;
        }
    }

    private static void addIfEmailable(List<String> emailList, String email, String syncAccount) {
        if (isEmailableFrom(email, syncAccount)) {
            emailList.add(email);
        }
    }
    /**
     * Returns true if:
     *   (1) the email is not a resource like a conference room or another calendar.
     *       Catch most of these by filtering out suffix calendar.google.com.
     *   (2) the email is not equal to the sync account to prevent mailing himself.
     */
    public static boolean isEmailableFrom(String email, String syncAccountName) {
        return isValidEmail(email) && !email.equals(syncAccountName);
    }
    /**
     * Example fake email addresses used as attendee emails are resources like conference rooms,
     * or another calendar, etc.  These all end in "calendar.google.com".
     */
    static final String MACHINE_GENERATED_ADDRESS = "calendar.google.com";
    public static boolean isValidEmail(String email) {
        return email != null && !email.endsWith(MACHINE_GENERATED_ADDRESS);
    }
    /**
     * Create an intent for emailing attendees of an event.
     *
     * @param resources The resources for translating strings.
     * @param eventTitle The title of the event to use as the email subject.
     * @param body The default text for the email body.
     * @param toEmails The list of emails for the 'to' line.
     * @param ccEmails The list of emails for the 'cc' line.
     * @param ownerAccount The owner account to use as the email sender.
     */
    public static Intent createEmailAttendeesIntent(Resources resources, String eventTitle,
            String body, List<String> toEmails, List<String> ccEmails, String ownerAccount) {
        List<String> toList = toEmails;
        List<String> ccList = ccEmails;
        if (toEmails.size() <= 0) {
            if (ccEmails.size() <= 0) {
                // TODO: Return a SEND intent if no one to email to, to at least populate
                // a draft email with the subject (and no recipients).
                throw new IllegalArgumentException("Both toEmails and ccEmails are empty.");
            }

            // Email app does not work with no "to" recipient.  Move all 'cc' to 'to'
            // in this case.
            toList = ccEmails;
            ccList = null;
        }

        // Use the event title as the email subject (prepended with 'Re: ').
        String subject = null;
        if (eventTitle != null) {
            subject = resources.getString(R.string.email_subject_prefix) + eventTitle;
        }

        // Use the SENDTO intent with a 'mailto' URI, because using SEND will cause
        // the picker to show apps like text messaging, which does not make sense
        // for email addresses.  We put all data in the URI instead of using the extra
        // Intent fields (ie. EXTRA_CC, etc) because some email apps might not handle
        // those (though gmail does).
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("mailto");

        // We will append the first email to the 'mailto' field later (because the
        // current state of the Email app requires it).  Add the remaining 'to' values
        // here.  When the email codebase is updated, we can simplify this.
        if (toList.size() > 1) {
            for (int i = 1; i < toList.size(); i++) {
                // The Email app requires repeated parameter settings instead of
                // a single comma-separated list.
                uriBuilder.appendQueryParameter("to", toList.get(i));
            }
        }

        // Add the subject parameter.
        if (subject != null) {
            uriBuilder.appendQueryParameter("subject", subject);
        }

        // Add the subject parameter.
        if (body != null) {
            uriBuilder.appendQueryParameter("body", body);
        }

        // Add the cc parameters.
        if (ccList != null && ccList.size() > 0) {
            for (String email : ccList) {
                uriBuilder.appendQueryParameter("cc", email);
            }
        }

        // Insert the first email after 'mailto:' in the URI manually since Uri.Builder
        // doesn't seem to have a way to do this.
        String uri = uriBuilder.toString();
        if (uri.startsWith("mailto:")) {
            StringBuilder builder = new StringBuilder(uri);
            builder.insert(7, Uri.encode(toList.get(0)));
            uri = builder.toString();
        }

        // Start the email intent.  Email from the account of the calendar owner in case there
        // are multiple email accounts.
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO, Uri.parse(uri));
        emailIntent.putExtra("fromAccountString", ownerAccount);
        return Intent.createChooser(emailIntent, resources.getString(R.string.email_picker_label));
    }
/*
 * 
 * 
 * 
 * ******************** API 8
 * 
 * 
 * 
 * 
 */
    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingServiceAPI8(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "gc2015:StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingServiceAPI8(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                }
            }
        }
    }

    public static Notification makeNewAlertNotificationAPI8(Context context, String title,
            String location, int numReminders, long eventID) {
        Resources res = context.getResources();

        // Create an intent triggered by clicking on the status icon.
//        Intent clickIntent = new Intent();
//        clickIntent.setClass(context, CalendarActivity.class);
//        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent clickIntent = AlertUtils.buildEventViewIntent(context, eventID, 0, 0);
        
        // Create an intent triggered by clicking on the "Clear All Notifications" button
        Intent deleteIntent = new Intent();
        deleteIntent.setClass(context, AlertReceiver.class);
        deleteIntent.setAction(DELETE_ACTION);

        if (title == null || title.length() == 0) {
            title = res.getString(R.string.no_title_label);
        }

        String helperString;
        if (numReminders > 1) {
            String format;
            if (numReminders == 2) {
                format = res.getString(R.string.alert_missed_events_single);
            } else {
                format = res.getString(R.string.alert_missed_events_multiple);
            }
            helperString = String.format(format, numReminders - 1);
        } else {
            helperString = location;
        }



//        Notification notification = new Notification(R.drawable.ic_launcher,null,System.currentTimeMillis());
////        notification.setLatestEventInfo(context,title,helperString,
////				                PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT));
//        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);
//        notification.setLatestEventInfo(context,title,helperString, pendingIntent);
//        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        // DC 2015.12.06 Change Compiler caused error so change it to below

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, clickIntent, 0);
        Notification.Builder builder = new Notification.Builder(context);

        builder.setAutoCancel(false);
        //builder.setTicker("this is ticker text");
        builder.setContentTitle(title);
        builder.setContentText(helperString);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        //builder.setSubText("This is subtext...");   //API level 16
        builder.setNumber(100);
        builder.build();

        Notification notification = builder.getNotification();

        return notification;
    }
}
