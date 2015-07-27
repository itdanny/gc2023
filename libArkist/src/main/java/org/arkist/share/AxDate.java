package org.arkist.share;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class AxDate {
	final static public String TAG = AxDate.class.getSimpleName();
	final static public SimpleDateFormat sdfYYYYMMDD = new SimpleDateFormat("yyyyMMdd",Locale.US);
	final static public SimpleDateFormat sdfMMM = new SimpleDateFormat("MMM",Locale.US);
	final static public SimpleDateFormat sdfEEE = new SimpleDateFormat("EEE",Locale.US);
	final static public SimpleDateFormat sdfYYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss",Locale.US);
	final static public SimpleDateFormat utcFormat = sdfYYYYMMDDHHMMSS;//new SimpleDateFormat(AxDate.sdfYYYYMMDDHHMMSS.toPattern(),Locale.US);
	
	final static public SimpleDateFormat sdf_YYYYMM = new SimpleDateFormat("yyyy-MM",Locale.US);
	final static public SimpleDateFormat sdf_YYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
	final static public SimpleDateFormat sdf_MMDD = new SimpleDateFormat("MM/dd",Locale.US);
	final static public SimpleDateFormat sdf_HHMM = new SimpleDateFormat("HH:mm",Locale.US);
	final static public SimpleDateFormat sdf_YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
	
	public AxDate() {}
	static public String getTodayYYYYMMDD(){
		return AxDate.sdfYYYYMMDD.format(new Date());
	}
	static public String getTimeIfToday(String yyyymmddhhss){
		yyyymmddhhss = utc2local(yyyymmddhhss);
	    final String today = sdfYYYYMMDDHHMMSS.format(new Date());		                
	    if (today.substring(0, 8).equals(yyyymmddhhss.substring(0, 8))){// If yyyymmdd is equal
	    	return yyyymmddhhss.substring(8, 10)+":"+yyyymmddhhss.substring(10, 12); // Only Time
	    } else {
	    	return yyyymmddhhss.substring(4, 6)+"-"+yyyymmddhhss.substring(6, 8); // Only Date
	    }
	}
	static public String utc2local(String yyyymmddhhmmss){
		// Change UTC date to LOCAL date
		Calendar utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			utcDate.setTime(utcFormat.parse(yyyymmddhhmmss));
			yyyymmddhhmmss = sdfYYYYMMDDHHMMSS.format(utcDate.getTime());
		} catch (ParseException e) {
			AxDebug.track(TAG, "time=", yyyymmddhhmmss, e.getMessage());			
		}	
		return yyyymmddhhmmss;
	}
	static public long utc2local(long milliseconds){
		Calendar utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar localDate = Calendar.getInstance();
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		utcDate.setTimeInMillis(milliseconds);
		try {
			localDate.setTime(sdfYYYYMMDDHHMMSS.parse(utcFormat.format(utcDate.getTime())));
		} catch (ParseException e) {
			AxDebug.track(TAG, "time=", String.valueOf(milliseconds), e.getMessage());
		}
		return localDate.getTimeInMillis();
	}
}