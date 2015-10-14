package com.hkbs.HKBS.arkUtil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MyUtil {
	final static private String TAG = MyUtil.class.getSimpleName();	
	final static public boolean DEBUG_APP = true;
	
	final static private boolean IS_GET_VIBRATE_PERMISSION=false;
	
	final static public int DELAY_MILLIS = 50;  // Standard
	final static public int DELAY_MILLIS_HIDE_ZOOM = 1500;
	final static public int DELAY_MILLIS_SELECTION = 200;  // Standard
	
	final public static int REQUEST_CALENDAR = 1;
	final public static int REQUEST_ABOUT = 2;
	final public static int REQUEST_SUPPORT = 3;
	final public static int REQUEST_ALARM = 4;
	
	final static public String FIELD_Type = "Type";
	final static public String FIELD_Name = "Name";
	final static public String FIELD_Date = "Date";
	final static public String FIELD_Begin = "Begin";
	final static public String FIELD_End = "End";
	final static public String FIELD_Abbrev = "Abbrev"; // For BooksRecord Only
	final static public String FIELD_CreatedDate = "CreatedDate";
	final static public String FIELD_Comment = "Comment";
	final static public String FIELD_Content = "Content";
	final static public String FIELD_EventID = "EventID";
	final static public String FIELD_TagValue = "TagValue";
	final static public String FIELD_Code = "Code";
	
	final static public String EXTRA_TYPE = "extraType";
	final static public String EXTRA_TYPE_SELECT = "extraTypeSelect";
	final static public String EXTRA_DEFAULT_DATE = "extraDefaultDate";
	final static public String EXTRA_SELECT_MILLSEC = "extraSelectMillsec";
	
	final static public String PREF_COUNTRY = "prefCountry";
	
	final static public String PREF_LANG_EN = "EN";
	final static public String PREF_LANG = "prefLang"; // EN, HK, TW, CN
	final static public String PREF_HOLIDAY_CODE = "prefHolidayCode"; // hongkong, china
	final static public String PREF_ALERT = "prefAlert";
	final static public String PREF_ALERT_POPUP = "prefAlertPopup";
	final static public String PREF_ALERT_RINGTONE = "prefAlertRingTone";
	final static public String PREF_ALERT_VIBRATE_WHEN = "prefAlertVibrateWhen"; // Always, Silent, Never
	final static public String PREF_ALERT_NBR_OF_FIRES = "prefAlertNbrOfFires";
	
	final static public SimpleDateFormat sdfYYYYMM = new SimpleDateFormat("yyyy-MM",Locale.US);
	final static public SimpleDateFormat sdfYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
	final static public SimpleDateFormat sdfYYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
	final static public SimpleDateFormat sdfYYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss",Locale.US);
	final static public SimpleDateFormat sdfHHMM = new SimpleDateFormat("HH:mm",Locale.US);
	final static public SimpleDateFormat sdfMMDD = new SimpleDateFormat("MM/dd",Locale.US);
	final static public SimpleDateFormat sdfEEE = new SimpleDateFormat("EEE", Locale.UK);
	final static public SimpleDateFormat sdfEngEEEE = new SimpleDateFormat("EEEE", Locale.UK);
	final static public SimpleDateFormat sdfChiEEEE = new SimpleDateFormat("EEEE", Locale.CHINESE);
	final static public SimpleDateFormat sdfEngMMMM = new SimpleDateFormat("MMMM",Locale.US);
	final static public SimpleDateFormat sdfChiMMMM = new SimpleDateFormat("MMMM",Locale.CHINESE);
	static public SimpleDateFormat utcYMD = new SimpleDateFormat(MyUtil.sdfYYYYMMDD.toPattern(),Locale.US);
	static public SimpleDateFormat utcHM = new SimpleDateFormat(MyUtil.sdfHHMM.toPattern(),Locale.US);
	
	static private Context mContext;
	
	public MyUtil() {		
	}
	static public void initMyUtil(Context context){
		mContext=context;
		utcYMD.setTimeZone(TimeZone.getTimeZone("UTC"));
		utcHM.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	static public void logError(String tag, String msg){
    	Log.e(tag, msg);
    }
	static public void log(String tag, String msg){
    	Log.i(tag, msg);
    }
	static public void setPrefInt(String key, Integer value){
		//PreferenceManage 帮忙去创建和管理preference hierarchies, 里面有一个sharedpreference
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		settings.edit().putInt(key, value).commit();
	}
	static public Map<String,?> getPrefAll(){
		return PreferenceManager.getDefaultSharedPreferences(mContext).getAll();		
	}
	static public Integer getPrefInt(String key, Integer defaultValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		return settings.getInt(key, defaultValue);
	}
	static public void setPrefLong(String key, long value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		settings.edit().putLong(key, value).commit();
	}
	static public long getPrefLong(String key, long defaultValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		return settings.getLong(key, defaultValue);
	}
	static public void setPrefStr(String key, String value){
		//PreferenceManage 帮忙去创建和管理preference hierarchies, 里面有一个sharedpreference
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		settings.edit().putString(key, value).commit();
	}
	static public String getPrefStr(String key, String defaultValue){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		return settings.getString(key, defaultValue);
	}
	static public void vibrate(Context context){
		if (IS_GET_VIBRATE_PERMISSION){
	    	Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vib.vibrate(300);
		}
    }
	@SuppressLint("InlinedApi")
	static public void popDate(Context context, String yyyymmddStr, DatePickerDialog.OnDateSetListener listener){
		String[] curCal = yyyymmddStr.split(" ");
		curCal = curCal[0].split("-");
		DatePickerDialog dlg;
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			dlg = new DatePickerDialog(context,
					android.R.style.Theme_Holo_Light_Dialog_NoActionBar, listener, 
					Integer.parseInt(curCal[0]),
					Integer.parseInt(curCal[1])-1,
					Integer.parseInt(curCal[2]));
		} else {
			dlg = new DatePickerDialog(context,
					android.R.style.Theme_Dialog, listener, 
					Integer.parseInt(curCal[0]),
					Integer.parseInt(curCal[1])-1,
					Integer.parseInt(curCal[2]));
		}
		dlg.show();
	}	
	static public BitmapDrawable scaleDrawable(Drawable drawing, int boundBoxInDp){
		Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

	    // Get current dimensions
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.
	    float xScale = ((float) boundBoxInDp) / width;
	    float yScale = ((float) boundBoxInDp) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
//	    width = scaledBitmap.getWidth();
//	    height = scaledBitmap.getHeight();
	    
	    return result;
	}
	static public void scaleImage(ImageView view, int boundBoxInDp)	{
	    // Get the ImageView and its bitmap
	    Drawable drawing = view.getDrawable();
	    // Apply the scaled bitmap
	    BitmapDrawable result = scaleDrawable(drawing, boundBoxInDp);
	    view.setImageDrawable(result);

	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
	    params.width = result.getBounds().width();// width;
	    params.height = result.getBounds().height();// height;
	    view.setLayoutParams(params);
	}
//    static private void trackError(Context thisContext, String category, String label, String desc){
//    	Context context = thisContext;//MyApp.getInstance().getApplicationContext();
//    	EasyTracker easyTracker = EasyTracker.getInstance(context);
//        if (easyTracker!=null){
//        	String appVersionName = "";
//    		try {
//    			appVersionName = "v"+context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName+"_";    			
//    		} catch (Exception e){
//    			//
//    		}
//    		String stackTrace = Log.getStackTraceString(new Exception());
//    		MyUtil.logError(TAG, "TrackError:"+label+" "+desc+" "+stackTrace);
//        	easyTracker.send(MapBuilder
//      		      .createEvent(appVersionName+category, // Category
//      		                   label+"_"+desc, // Action
//      		                   stackTrace, 	// Label
//      		                   null) 			// Value
//      		      .build()
//      		  );
//        }
//    }
    static public void trackClick(Context thisContext, String label, String fromWhere){
    	Context context = thisContext;//MyApp.getInstance().getApplicationContext();
    	EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker!=null){
        	// MyApplog(TAG, "TrackClick:"+fromWhere+"_"+label);
        	easyTracker.send(MapBuilder
        		      .createEvent("tClick", // Category
        		                   "tFrom_"+fromWhere, // Action
        		                   "tBtn_"+label, // Label
        		                   null) // Value
        		      .build()
        		  );

        }
    }
    static public String getCurCountry(){
    	String country = MyUtil.getPrefStr(PREF_COUNTRY, "");
    	if (!country.equals("")) return country;
    	try {
	        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	        final String simCountry = tm.getSimCountryIso();
	        if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
	        	country = simCountry;// TW; TWN   HK; HKG  CN;CHN;
	        } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
	            String networkCountry = tm.getNetworkCountryIso();
	            if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
	            	country=networkCountry;
	            } 
	        }
	    } catch (Exception e) { 
	    	//
	    }
	    if (country.equals("")){
	    	Locale curLocale = Locale.getDefault();
	    	country = curLocale.getCountry(); // HK; TW; CN
	    }
	    country=country.toUpperCase(Locale.US);
	    MyUtil.log(TAG, "Detect Country:"+country);
	    MyUtil.setPrefStr(PREF_COUNTRY, country);
	    return country;
    }
    
    static private float _scaleDensity=0;
    public static float scaleDensity(Context context){
    	if (_scaleDensity==0){
	    	final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    	final DisplayMetrics dm = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >=17) {
                wm.getDefaultDisplay().getRealMetrics(dm);
            } else {
                wm.getDefaultDisplay().getMetrics(dm);
            }
	        return dm.scaledDensity;
    	} else {
    		return _scaleDensity;
    	}
    }
    public static double diagnol(Context context){
    	final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	final DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >=17) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }
    	final float heightInches = dm.heightPixels /dm.ydpi;
        final float widthInches = dm.widthPixels /dm.xdpi;
        return Math.sqrt((heightInches*heightInches)+(widthInches*widthInches));
    }
    public static boolean isBigScreen(Context context){
    	return MyUtil.diagnol(context)>6.5f; 
    }
    public static int heightPixels(Context context){
    	final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	final DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >=17) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }
    public static int widthPixels(Context context){
    	final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	final DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >=17) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }
}
