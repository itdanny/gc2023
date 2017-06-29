package com.hkbs.HKBS.arkCalendar;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.hkbs.HKBS.CalendarAdapter;
import com.hkbs.HKBS.MyApp;
import com.hkbs.HKBS.MyHoliday;
import com.hkbs.HKBS.arkUtil.MyUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@SuppressLint("InlinedApi")
public class MyCalendar {
	final static private boolean DEBUG = MyUtil.DEBUG_APP && false;
	final static private String TAG = MyCalendar.class.getSimpleName();
	final static public int REPEAT_END_YEAR=2030;
	final static public int REPEAT_END_MONTH=11;//i.e.12
	final static public int REPEAT_END_DAY=31;
	final static public int REPEAT_END_HH=23;
	final static public int REPEAT_END_MM=59;
	//final static private String FIELD_SEPERATOR = ""+(char) 1;①②③④⑤⑥⑦⑧⑨⑩⊕⊙⁇
	final static private String PREFIX_SYMBOL_WHITE = "①②③④⑤⑥⑦⑧⑨⑩⊕"; //☺⑪⓵⊕⊙⁇⊕
	final static public String PREFIX_SYMBOL_BLACK = "➊➋❸➍➎➏➐➑➒➓⊕"; //☺⑪⓵⊕⊙⁇⊕❶❷❸❹❺❻❼❽❾❿
	final static private int CELL_EVENT_LENGTH = 4;
	final static public String FIELD_SEPERATOR = "… "; // Extra space for wrap line
	
//	final static public String API8_EVENT_TITLE = "title";
//	final static public String API8_EVENT_ALLDAY = "allDay";
	
	public MyCalendar() {

	}
	static public class MyCalendarList {
		public String name;
		public String id;
		public boolean selected;
		public String ownerAccount;
		public int sync_events;
		public MyCalendarList(String _name, String _id, boolean selected, String ownerAccount, int sync_events) {
			this.name = _name;
			this.id = _id;
			this.selected = selected;
			this.ownerAccount = ownerAccount;
			this.sync_events = sync_events;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	static public final int INSTANCE_BEGIN=0;
	static public final int INSTANCE_END=1;
	static public final int INSTANCE_TITLE=2;
	static public final int INSTANCE_ALL_DAY=3;
	static public final int INSTANCE_EVENT_ID=4;
	static private final String[] INSTANCE_FIELDS = {
		Api8.BEGIN,
		Api8.END,
		Api8.TITLE,
		Api8.ALL_DAY,
		Api8.EVENT_ID
      };
	static private final String[] REMINDER_FIELDS = {
		Api8._ID,
		Api8.EVENT_ID,
		Api8.MINUTES,
		Api8.METHOD		
      };
	static private final String[] EVENTS_FIELDS_14 = {
		CalendarContract.Events._ID,
		CalendarContract.Events.DTSTART, 
		CalendarContract.Events.DTEND,
		CalendarContract.Events.TITLE,
		CalendarContract.Events.RRULE,
		CalendarContract.Events.ALL_DAY,
		CalendarContract.Events.CALENDAR_ID,
		CalendarContract.Events.DESCRIPTION,
		CalendarContract.Events.HAS_ALARM,
		CalendarContract.Events.EVENT_LOCATION,
		CalendarContract.Events.EVENT_TIMEZONE,
		CalendarContract.Events.ACCESS_LEVEL, // Default(0), Confiential(1), Private(2), Public (3)
		CalendarContract.Events.DURATION,
		CalendarContract.Events.LAST_DATE,
		CalendarContract.Events.RDATE,
		CalendarContract.Events.EXDATE
//		CalendarContract.Events.EXRULE,
//		CalendarContract.Events.ORIGINAL_ALL_DAY,
//		CalendarContract.Events.ORIGINAL_ID,
//		CalendarContract.Events.ORIGINAL_INSTANCE_TIME,
//		CalendarContract.Events.ORIGINAL_SYNC_ID
		};
		// Events.AVAILABILITY // Busy (0), Free (1), Tentative (2)
	static private final String[] EVENTS_FIELDS_8 = {
		Api8._ID,
		Api8.DTSTART, 
		Api8.DTEND,
		Api8.TITLE,
		Api8.RRULE,
		Api8.ALL_DAY,
		Api8.CALENDAR_ID,
		Api8.DESCRIPTION,
		Api8.HAS_ALARM,
		Api8.EVENT_LOCATION,
		Api8.EVENT_TIMEZONE,
		Api8.VISIBILITY, // default (0), confidential (1), private (2) public (3)
		Api8.DURATION,
		Api8.LAST_DATE,
		Api8.RDATE,
		Api8.EXDATE
//		Api8.EXRULE,
//		Api8.ORIGINAL_ALL_DAY,   
//		Api8.ORIGINAL_ID,
//		Api8.ORIGINAL_INSTANCE_TIME, // For recurring's single event (All except this one...then this one can't recur again)
//		Api8.ORIGINAL_SYNC_ID
		};
		// "eventStatus" // tentative (0), confirmed (1) or canceled (2):	
	static private final int CALENDAR_ID=0;
	static private final int CALENDAR_DISPLAY_NAME=1;
	static private final int CALENDAR_VISIBLE=2;
	static private final int CALENDAR_OWNER_ACCOUNT=3;
	static private final int CALENDAR_SYNC_EVENTS=4;
	static private final String[] CALENDAR_FIELDS_14 = {
		CalendarContract.Calendars._ID,
		CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, 
		CalendarContract.Calendars.VISIBLE,
		CalendarContract.Calendars.OWNER_ACCOUNT,
		CalendarContract.Calendars.SYNC_EVENTS
		};
	static private final String[] CALENDAR_FIELDS_8 = { 
			"_id","displayName","selected","ownerAccount","sync_events"};
	@SuppressLint("NewApi")
	static public Uri getInstanceUri() {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			return CalendarContract.Instances.CONTENT_URI;
		} else {
			return Uri.parse(Api8.URI_INSTANCES);
		}
	}
	@SuppressLint("NewApi")
	static public Uri getReminderUri() {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			return CalendarContract.Reminders.CONTENT_URI;
		} else {
			return Uri.parse(Api8.URI_REMINDERS);
		}
	}
	@SuppressLint("NewApi")
	static public Uri getEventUri() {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			return CalendarContract.Events.CONTENT_URI;
		} else {
			return Uri.parse(Api8.URI_EVENTS);
		}
	}
	@SuppressLint("NewApi")
	static public Uri getCalendarUri() {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			return CalendarContract.Calendars.CONTENT_URI;
		} else {
			return Uri.parse(Api8.URI_CALENDARS);
		}
	}
//	static protected Uri baseUri = Uri.parse(Api8.URI_CALENDARS);
//	static protected Map<String, Account> accounts;
//	static private Uri createCompleteUri(String name, String type){
//		Uri.Builder b = baseUri.buildUpon();
//		b.appendQueryParameter("caller_is_syncadapter", "true");
//		b.appendQueryParameter("account_name", "local");//name//"local"
//		b.appendQueryParameter("account_type", "LOCAL");//"type
//		Uri calUri = b.build();
//		if (DEBUG) MyApp.log(TAG, calUri.toString());
//		return calUri;
//	}
//	static private boolean checkExistingCalendars(Context context) {
//		Cursor res = context.getContentResolver().query(baseUri, null, null, null, null);
//		if (res!=null && res.getCount() > 0) {
//			return false;
//		} else {
//			return true;
//		}
//	}
//	static private void createLocalCalendar(Context context){
//		if (DEBUG) MyApp.log(TAG, "Create Local Calendar...");
//		if (!checkExistingCalendars(context)){
//			if (DEBUG) MyApp.log(TAG,"Please empty all calendar data");
//			return;
//		}
//		accounts = new HashMap<String, Account>();
//		Account[] readAccounts = AccountManager.get(context).getAccounts();
//		Account account=null;
//		for (Account a:readAccounts) {
//			accounts.put(a.name, a);
//			if (account==null) account = a; //a.type = "com.google" a.name="chow.danny@gmail.com"
//			if (DEBUG) MyApp.log(TAG, a.name+","+a.type+","+a.toString());
//		}
//		if (account==null){
//			if (DEBUG) MyApp.log(TAG, "No accounts found");
//			return;
//		} else {
//			//showDialog("No accounts found", "You need to set up a (Google) account.");
//			Uri calUri = createCompleteUri(account.name, account.type);
//			ContentValues vals = new ContentValues();
//			vals.put("_id", 1);
//			vals.put("_sync_account",account.name);// account.name
//			vals.put("_sync_account_type", account.type);//account.type 
//			vals.put("name", account.name);
//			vals.put("displayName", account.name);
//			vals.put("color", 14417920);
//			vals.put("access_level", 700);
//			vals.put("selected", 1);
//			vals.put("ownerAccount", account.name);
//			vals.put("sync_events", 1);
//			vals.put("timezone", "GMT");
//			vals.put("hidden", 0);
//			context.getContentResolver().insert(calUri, vals);
//			if (DEBUG) MyApp.log(TAG, "Sync Account is created");
//		}
//	}
	static public MyCalendarList[] getCalendars(Context context) {
		Uri l_calendars = getCalendarUri();
		Cursor cursor = context.getContentResolver().query(l_calendars, 
				(android.os.Build.VERSION.SDK_INT >= 14)?CALENDAR_FIELDS_14:CALENDAR_FIELDS_8, 
				null, null, null); // all calendars
		MyCalendarList m_calendars[];
		if (cursor==null){
			//createLocalCalendar(context);
			m_calendars = new MyCalendarList[0];
		} else if (cursor.getCount()==0){
			m_calendars = new MyCalendarList[0];
			cursor.close();
		} else {
			m_calendars = new MyCalendarList[cursor.getCount()];
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				String id = cursor.getString(CALENDAR_ID);
				String displayName = cursor.getString(CALENDAR_DISPLAY_NAME);
				boolean selected = !cursor.getString(CALENDAR_VISIBLE).equals("0");
				//Log.i("cal", id + "," + displayName + "," + selected + "," + cursor.getString(2)+","+cursor.getString(3));
				m_calendars[i] = new MyCalendarList(displayName, id, selected, 
						cursor.getString(CALENDAR_OWNER_ACCOUNT), 
						cursor.getInt(CALENDAR_SYNC_EVENTS));
				cursor.moveToNext();
			}
			cursor.close();
		} 		
		return m_calendars;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */	
//	public static String getUtcDate(Calendar date){
//		return MyUtil.utcYMD.format(date.getTime())+" "+MyUtil.sdfEEE.format(date.getTime());
//	}
//	public static String getUtcTime(Calendar date){
//		return MyUtil.utcHM.format(date.getTime());
//	}
//	public static String getLocalDate(Calendar date){
//		return MyUtil.sdfYYYYMMDD.format(date.getTime())+" "+MyUtil.sdfEEE.format(date.getTime());
//	}
//	public static String getLocalTime(Calendar date){
//		return MyUtil.sdfHHMM.format(date.getTime());
//	}
//	public static long getUTCinMillis(Calendar date){
//		return date.getTimeInMillis()+date.getTimeZone().getOffset(date.getTimeInMillis());
//	}
	public static String day2RuleString(int day) {
        switch (day) {
        case Calendar.SUNDAY:	return "SU";
        case Calendar.MONDAY:   return "MO";
        case Calendar.TUESDAY:	return "TU";
        case Calendar.WEDNESDAY:return "WE";
        case Calendar.THURSDAY:	return "TH";
        case Calendar.FRIDAY:	return "FR";
        case Calendar.SATURDAY:	return "SA";
        default:	throw new IllegalArgumentException("bad day argument: " + day);
        }
    }
	static public class MyDayEvents{
		static public int TYPE_NONE=0;
		static public int TYPE_TITLE=1;
		static public int TYPE_NEXT_MTH=2;
		static public int TYPE_PREV_MTH=3;
		
		public int counter=0;
		public String text="";
		
		private int mType;
		private Calendar mCalendar;
		private long mCalendarMinInMillsec;
		private long mCalendarMaxInMillsec;
		
		public MyDayEvents(int type, Calendar calendar) {
			this.mType = type;
			setCalendar(calendar);
		}
		public MyDayEvents(int type, String text) { // For Calendar Title
			this.mType = type;
			this.text = text;
		}
		private void setCalendar(Calendar cal){
			this.mCalendar = (Calendar) cal.clone();
			this.mCalendarMinInMillsec = getFrIncludeDay(cal).getTimeInMillis();
			this.mCalendarMaxInMillsec = getToIncludeDay(cal).getTimeInMillis();
		}
		@Override public String toString() {return text;}
		
		public int getType(){return mType;}
		public Calendar getCalendar(){return mCalendar;}
		public long getMinInMillsec(){return mCalendarMinInMillsec;}
		public long getMaxInMillsec(){return mCalendarMaxInMillsec;}
		public String toYYYYMMDD(){return MyUtil.sdfYYYYMMDD.format(this.mCalendar.getTime());}
//		public String toYMDHM(){return MyUtil.sdfYYYYMMDDHHMM.format(this.mCalendar.getTime());}
//		public String toYMDHM_MIN(){return MyUtil.sdfYYYYMMDD.format(this.mCalendar.getTime())+" 00:00";}
//		public String toYMDHM_MAX(){return MyUtil.sdfYYYYMMDD.format(this.mCalendar.getTime())+" 23:59";}
	}
	static public Calendar getLastMonthEndDay(Calendar cal){
		Calendar lastMth = (Calendar) cal.clone();
		lastMth.set(Calendar.DAY_OF_MONTH, 1);
		lastMth.add(Calendar.DATE, -1);
		return lastMth;
	}
	static public Calendar getNextMonthFirstDay(Calendar cal){
		Calendar nextMth = (Calendar) cal.clone();
		nextMth.set(Calendar.DAY_OF_MONTH, 27);
		nextMth.add(Calendar.DATE, 5);
		nextMth.set(Calendar.DAY_OF_MONTH, 1);
		return nextMth;
	}
	static public Calendar getFrIncludeDay(Calendar cal){
		Calendar frCal = (Calendar) cal.clone();
    	frCal.set(Calendar.HOUR_OF_DAY, 0);
    	frCal.set(Calendar.MINUTE, 0);
    	frCal.set(Calendar.SECOND, 0);
    	frCal.set(Calendar.MILLISECOND, 0);
    	return frCal;
	}
	static public long getFrIncludeDay(long cal){
		Calendar frCal = Calendar.getInstance();
		frCal.setTimeInMillis(cal);
		return getFrIncludeDay(frCal).getTimeInMillis();		
	}
	static public Calendar getToIncludeDay(Calendar cal){
		Calendar toCal = (Calendar) cal.clone();
		toCal.set(Calendar.HOUR_OF_DAY, 0);
		toCal.set(Calendar.MINUTE, 0);
		toCal.set(Calendar.SECOND, 0);
		toCal.set(Calendar.MILLISECOND, 0);
		toCal.add(Calendar.DAY_OF_MONTH, 1);
		toCal.add(Calendar.MILLISECOND, -1);		
    	return toCal;
	}
	static public long getToIncludeDay(long cal){
		Calendar toCal = Calendar.getInstance();
		toCal.setTimeInMillis(cal);
		return getToIncludeDay(toCal).getTimeInMillis();
	}
	static public String getCounterString(int nbrOfEvents, boolean isBlack){
		String symbol = isBlack?PREFIX_SYMBOL_BLACK:PREFIX_SYMBOL_WHITE;
    	if (nbrOfEvents<=1){ // No need to display if only one
    		return " ";
    	} else if (nbrOfEvents<symbol.length()-1){
    		return symbol.substring(nbrOfEvents-1, nbrOfEvents);
    	} else {
    		return symbol.substring(symbol.length()-1,symbol.length());
    	}
    }
	static public boolean isOneEventSymbol(String str, boolean isBlack){
		String symbol = isBlack?PREFIX_SYMBOL_BLACK:PREFIX_SYMBOL_WHITE;
		return str.substring(0, 1).equals(symbol.substring(0, 1));
	}
	static public HashMap<String, String> getEventCursor(Context context, String eventID){
		Uri.Builder builder = MyCalendar.getEventUri().buildUpon();
		if (DEBUG) MyUtil.log(TAG,"SDK:"+android.os.Build.VERSION.SDK_INT);
		String [] events = (android.os.Build.VERSION.SDK_INT >= 14)?EVENTS_FIELDS_14:EVENTS_FIELDS_8;
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			Cursor cursor = context.getContentResolver().query(builder.build(),events,Api8._ID+"="+eventID, null, null);
			if (cursor.moveToFirst()){
				for (int i=0;i<events.length;i++){
					map.put(events[i], cursor.getString(i));		
				}
			}		
			cursor.close();
		} catch (Exception e){
			// Nothing
		}
    	return map;
	}
	static public Cursor getReminderCursor(Context context, String eventID){
		Uri.Builder builder = MyCalendar.getReminderUri().buildUpon();
		try{
			Cursor cursor = context.getContentResolver().query(builder.build(),REMINDER_FIELDS,  
					Api8.EVENT_ID+"="+eventID, null, null);
			return cursor;
		} catch (Exception e){
			return null;
		}
	}
	static public Cursor getInstanceCursor(Context context, Calendar frCal, Calendar toCal){
		return getInstanceCursor(context, frCal.getTimeInMillis(), toCal.getTimeInMillis());
	}
	static public Cursor getInstanceCursor(Context context, long frCal, long toCal){
		return getInstanceCursor(context, frCal, toCal, "");
	}
	static public Cursor getInstanceCursor(Context context, long frCal, long toCal, String curCalendarID){
		Uri.Builder builder = MyCalendar.getInstanceUri().buildUpon();
//		frCal = MyCalendar.getFrIncludeDay(frCal);
//		toCal = MyCalendar.getToIncludeDay(toCal);
    	ContentUris.appendId(builder, frCal);
    	ContentUris.appendId(builder, toCal);
    	String sqlStr = "";
    	MyCalendarList calList [] = MyCalendar.getCalendars(context);
    	if (curCalendarID.equals("")){
    		for (int i=0;i<calList.length;i++){
	    		if (calList[i].selected){
	    			if (sqlStr.equals("")){
	    				sqlStr = Api8.CALENDAR_ID+"="+calList[i].id+" ";
	    			} else {
	    				sqlStr = sqlStr + " OR "+Api8.CALENDAR_ID+"="+calList[i].id;
	    			}
	    		}
	    	}
    	} else {
	    	for (int i=0;i<calList.length;i++){
    			if (calList[i].id.equals(curCalendarID) && calList[i].selected){
    				sqlStr = Api8.CALENDAR_ID+"="+calList[i].id+" ";
    				break;
    			}
    		}
    	}
    	if (!sqlStr.equals("")){
    		sqlStr = "("+sqlStr+") AND ";
    	}
    	String frStr=String.valueOf(frCal);
    	String toStr=String.valueOf(toCal);
    	// 4 Scenario: 1) Fr/To InRange 2) Fr/To OutRange 3) Fr Out/To In 4) Fr In/To Out
    	sqlStr = sqlStr + 
    			"(("+Api8.BEGIN+">="+frStr+" AND "+Api8.BEGIN+"<="+toStr+" AND " +
    			Api8.END+">="+frStr+" AND "+Api8.END+"<="+toStr+") " + " OR " +
    			
			     "("+Api8.BEGIN+"<="+frStr+" AND "+Api8.END+">="+toStr+") "+" OR " +
			     
				"("+Api8.BEGIN+"<="+frStr+" AND "+
				Api8.END+">="+frStr+" AND "+Api8.END+"<="+toStr+") "+" OR " +
						
				"("+Api8.END+">="+toStr+" AND "+
				Api8.BEGIN+">="+frStr+" AND "+Api8.BEGIN+"<="+toStr+") "+" ) ";
    	try {
    		if (DEBUG) MyUtil.log(TAG,sqlStr);
	    	Cursor cursor = context.getContentResolver().query(builder.build(),INSTANCE_FIELDS,  
	    			sqlStr, null,  Api8.BEGIN + " ASC ");
	    	return cursor;
    	} catch (Exception err){
    		if (DEBUG) MyUtil.logError(TAG,"Query Error:"+err.getMessage());
    		return null;
    	}
	}
	static public int withinRange(long frDay, long toDay, boolean isAllDay, long rangeFrDay, long rangeToDay){
		// 4 Scenario: 1) Fr/To InRange 2) Fr/To OutRange 3) Fr Out/To In 4) Fr In/To Out
		
		// 2013-06-02: NO need to handle isAllDay ?
		if (isAllDay){ 
			frDay = MyCalendar.utc2local(frDay);
			toDay = MyCalendar.utc2local(toDay);
//			frDay = getFrIncludeDay(frDay);
//			toDay = getToIncludeDay(toDay);
		}
		if (frDay<rangeFrDay){
			if (toDay>rangeToDay){
				return 1; // OutRange (Include Day)
			} else if (toDay>rangeFrDay && toDay<=rangeToDay){
				return 2; // Fr Out / To In
			}
		} else if (frDay>=rangeFrDay && frDay<rangeToDay){
			if (toDay<=rangeToDay) {
				return 3; // In Range
			} else if (toDay>rangeToDay){
				return 4; // Fr In / To Out
			}
		} 
		return 0;
	}
	static Calendar utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));		
	static Calendar localDate = Calendar.getInstance();
	static SimpleDateFormat utcFormat = new SimpleDateFormat(MyUtil.sdfYYYYMMDDHHMMSS.toPattern(),Locale.US);
	//static SimpleDateFormat utcFormat = new SimpleDateFormat(MyApp.sdfYYYYMMDDHHMMSS.toPattern(),Locale.US);
	static public String utc2local(String yyyymmddhhmmss){
		// Change UTC date to LOCAL date
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			utcDate.setTime(utcFormat.parse(yyyymmddhhmmss));
			if (DEBUG) MyUtil.log(TAG, MyUtil.sdfYYYYMMDDHHMMSS.format(utcDate.getTime()));
			yyyymmddhhmmss = MyUtil.sdfYYYYMMDDHHMMSS.format(utcDate.getTime());
		} catch (ParseException e) {
			MyUtil.logError(TAG,"ERROR (utc2local)");			
		}	
		return yyyymmddhhmmss;
	}
	static public long utc2local(long milliseconds){
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		utcDate.setTimeInMillis(milliseconds);
		try {
			localDate.setTime(MyUtil.sdfYYYYMMDDHHMMSS.parse(utcFormat.format(utcDate.getTime())));
		} catch (ParseException e) {
			MyUtil.logError(TAG,"ERROR (utc2local)");
		}
		return localDate.getTimeInMillis();
	}
	static public Calendar getNextDay(Calendar calendar){
		Calendar nextDay = (Calendar) calendar.clone();
		nextDay.add(Calendar.DAY_OF_MONTH, 1);
		return nextDay;
	}
    static public MyDayEvents[] getEachDayEvents(Context context, Calendar today, boolean isGetEvents){
    	MyDayEvents [] dayEvents=CalendarAdapter.getDaysArray(today);
    	Calendar extraOneDay = Calendar.getInstance();
    	extraOneDay.setTimeInMillis(dayEvents[dayEvents.length-1].getMaxInMillsec());
    	extraOneDay.add(Calendar.DAY_OF_MONTH, 1);
    	if (!isGetEvents) return dayEvents;
    	Cursor cursor = getInstanceCursor(context, dayEvents[0].getMinInMillsec(), extraOneDay.getTimeInMillis());
    	if (cursor!=null){
    		if (cursor.getCount()>0 && cursor.moveToFirst()) {
	    		do {
	    			final String cursorTitle = cursor.getString(2);
	    			if (DEBUG) {
	    				Calendar cursorDate = Calendar.getInstance();
	    				cursorDate.setTimeInMillis(Long.parseLong(cursor.getString(0)));
	    				MyUtil.log(TAG, cursorTitle+" "+MyUtil.sdfYYYYMMDDHHMM.format(cursorDate.getTime()));
    				}
	    			// Find Days position
	    			int nbrOfDays=dayEvents.length;
	    			for (int j=0;j<nbrOfDays;j++){
	    				/*
	    				 * Change utc2local to local: Found that a record in moring 5:00 fill to wrong date
	    				 */
	    				final int result = withinRange(
	    						Long.parseLong(cursor.getString(MyCalendar.INSTANCE_BEGIN)), 
	    						Long.parseLong(cursor.getString(MyCalendar.INSTANCE_END)),
	    						!cursor.getString(MyCalendar.INSTANCE_ALL_DAY).equals("0"),
	    						dayEvents[j].getMinInMillsec(),
	    						dayEvents[j].getMaxInMillsec());
	    				if (result!=0){
	    					dayEvents[j].counter++;
							dayEvents[j].text = dayEvents[j].text + (dayEvents[j].text.equals("")?"":FIELD_SEPERATOR) +cursorTitle.substring(0, Math.min(CELL_EVENT_LENGTH,cursorTitle.length()));
	    				}
	    			}
	    		} while (cursor.moveToNext());
    		}
    		cursor.close();
    	} 
    	return dayEvents;
    }
    static final int NBR_OF_DAYS_SHOWN = 14;
    static final int FIRST_SECTION = 7;
    static public List<ContentValues> get14DayEvents(Context context, Calendar firstDay, String curCalendarID){
    	SimpleDateFormat sdfMMMEEEdd = new SimpleDateFormat("EEEMMMdd",Locale.US);
    	Calendar lastDay = (Calendar) firstDay.clone();
    	List<ContentValues> daysInRange = new ArrayList<ContentValues>();
    	List<ContentValues> eventsInRange = new ArrayList<ContentValues>();
    	String lunarTerm = "";
		String lunarDate = "";
		String defaultLang = MyUtil.getPrefStr(MyUtil.PREF_LANG, "HK");
    	for (int i=0;i<NBR_OF_DAYS_SHOWN;i++){
    		ContentValues eachDate = new ContentValues();
    		MyDayEvents dayEvent = new MyDayEvents(MyDayEvents.TYPE_NONE, lastDay);
    		eachDate.put(MyUtil.FIELD_Date, dayEvent.toYYYYMMDD());
    		eachDate.put(MyUtil.FIELD_Name, sdfMMMEEEdd.format(lastDay.getTime()));
    		eachDate.put(MyUtil.FIELD_Begin, dayEvent.getMinInMillsec()); // Not really begin, just keep a value
    		eachDate.put(MyUtil.FIELD_End, dayEvent.getMaxInMillsec());
    		// Lunar
    		lunarTerm="";
    		lunarDate="";
    		if (!defaultLang.equals(MyUtil.PREF_LANG_EN)) {
    			final MyCalendarLunar lunar = new MyCalendarLunar(lastDay, MyApp.mIsSimplifiedChinese);
    			lunarTerm = MyCalendarLunar.solar.getSolarTerm(lastDay);
    			lunarDate = lunar.toChineseMMDD();
    		} 
    		eachDate.put(MyUtil.FIELD_Abbrev, lunarTerm);
    		eachDate.put(MyUtil.FIELD_CreatedDate, lunarDate);
    		// Holiday
    		//final String holidayText = CalendarAdapter.getHolidayText(context, lastDay);
    		String holidayText = MyHoliday.getHolidayRemark(lastDay.getTime());
            holidayText= holidayText.replace("*","");
    		if (holidayText.startsWith("#")){
    			holidayText=holidayText.substring(1);
    		}    			
    		eachDate.put(MyUtil.FIELD_Comment, holidayText);
    		
    		daysInRange.add(eachDate);
    		lastDay.add(Calendar.DAY_OF_MONTH, 1);
    	}
    	MyDayEvents firstDayEvent = new MyDayEvents(MyDayEvents.TYPE_NONE, firstDay);
    	MyDayEvents lastDayEvent = new MyDayEvents(MyDayEvents.TYPE_NONE, lastDay);
    	Cursor cursor = getInstanceCursor(context, firstDayEvent.getMinInMillsec(), lastDayEvent.getMaxInMillsec(),curCalendarID);
    	if (cursor!=null){
    		if (cursor.getCount()>0 && cursor.moveToFirst()) {
	    		do {
	    			if (DEBUG) {
		    			final String cursorTitle = cursor.getString(MyCalendar.INSTANCE_TITLE);
	    				Calendar cursorDate = Calendar.getInstance();
	    				cursorDate.setTimeInMillis(Long.parseLong(cursor.getString(0)));
    					MyUtil.log(TAG, cursorTitle+" "+MyUtil.sdfYYYYMMDDHHMM.format(cursorDate.getTime()));
    				}
	    			// Find Days position
	    			long iBegin = Long.parseLong(cursor.getString(MyCalendar.INSTANCE_BEGIN));
    				long iEnd = Long.parseLong(cursor.getString(MyCalendar.INSTANCE_END));
    				boolean iAllDay = !cursor.getString(MyCalendar.INSTANCE_ALL_DAY).equals("0");
	    			for (int j=0;j<NBR_OF_DAYS_SHOWN;j++){
	    				ContentValues dayRecord = daysInRange.get(j);
	    				/*
	    				 * Change utc2local to local: Found that a record in moring 5:00 fill to wrong date
	    				 */
	    				final int result = withinRange(
	    						iBegin, iEnd, 
	    						iAllDay,
	    						dayRecord.getAsLong(MyUtil.FIELD_Begin),
	    						dayRecord.getAsLong(MyUtil.FIELD_End));     	
	    				if (result!=0){
	    					Calendar iBeginCal = Calendar.getInstance();
	    					iBeginCal.setTimeInMillis(iBegin);
	    					ContentValues record = new ContentValues();
	    					record.put(MyUtil.FIELD_Type,0);//Standard record
	    					record.put(MyUtil.FIELD_Date,dayRecord.getAsString(MyUtil.FIELD_Date));
	    					record.put(MyUtil.FIELD_Name,sdfMMMEEEdd.format(iBeginCal.getTime()));
	    					record.put(MyUtil.FIELD_Content,cursor.getString(MyCalendar.INSTANCE_TITLE));
	    					record.put(MyUtil.FIELD_Begin,iBegin);
	    					record.put(MyUtil.FIELD_End,iEnd);
	    					record.put(MyUtil.FIELD_EventID,Long.parseLong(cursor.getString(MyCalendar.INSTANCE_EVENT_ID)));	    					
	    					record.put(MyUtil.FIELD_TagValue,iAllDay);
	    					record.put(MyUtil.FIELD_Code,j<FIRST_SECTION?1:2);
	    					eventsInRange.add(record);
		    				//if (DEBUG) MyApp.log("#","Add record:"+sdfMMMEEEdd.format(iBeginCal.getTime()));
	    				}
	    			}
	    		} while (cursor.moveToNext());
    		}
    		cursor.close();
    	}
    	// Add Calendar if not exists
    	int dayIndex=0;
    	int eventIndex=0;
    	String lastEventDateStr = "";
    	while (dayIndex<NBR_OF_DAYS_SHOWN){
    		ContentValues dayInRange = daysInRange.get(dayIndex);
    		String dayIndexDate = dayInRange.getAsString(MyUtil.FIELD_Date);
    		int section = dayIndex<FIRST_SECTION?1:2;
//    		MyApp.log(TAG,"dayIndexDate:"+dayIndexDate);
    		if (eventIndex>=eventsInRange.size()){ // No more event record to analysis; just add
    			lastEventDateStr = "";
    			eventsInRange.add(eventIndex, getHeader(1,dayIndexDate,dayInRange,section));eventIndex++;//+1 for new record
    			dayIndex++;
    		} else {
	    		ContentValues eventInRange = eventsInRange.get(eventIndex);
	    		int result = dayIndexDate.compareTo(eventInRange.getAsString(MyUtil.FIELD_Date)); 
	    		if (result<0){  // Event record date is still large; just add
	    			lastEventDateStr = "";
	    			eventsInRange.add(eventIndex, getHeader(1,dayIndexDate,dayInRange,section));eventIndex++;	    			
	    			dayIndex++;//Not incremental to add TAIL
	    		} else {
	    			if (!eventInRange.getAsString(MyUtil.FIELD_Date).equals(lastEventDateStr)){
	    				lastEventDateStr = eventInRange.getAsString(MyUtil.FIELD_Date);
	    				eventsInRange.add(eventIndex,getHeader(1,lastEventDateStr,dayInRange,section));eventIndex++;
	    				dayIndex++;
	    			}
	    			eventIndex++;
	    		}
    		}
    	}
    	return eventsInRange;
    }
    static private ContentValues getHeader(int type, String date, ContentValues cv, int section){
    	ContentValues dayHeader = new ContentValues();
		dayHeader.put(MyUtil.FIELD_Type,type); // Header Record
		dayHeader.put(MyUtil.FIELD_Date,date);
		dayHeader.put(MyUtil.FIELD_Name,cv.getAsString(MyUtil.FIELD_Name));
		dayHeader.put(MyUtil.FIELD_Code,section);
		dayHeader.put(MyUtil.FIELD_Abbrev,cv.getAsString(MyUtil.FIELD_Abbrev)); // LunarTerm
		dayHeader.put(MyUtil.FIELD_CreatedDate,cv.getAsString(MyUtil.FIELD_CreatedDate)); // LunarDate
		dayHeader.put(MyUtil.FIELD_Comment, cv.getAsString(MyUtil.FIELD_Comment));
		return dayHeader;
    }
}
