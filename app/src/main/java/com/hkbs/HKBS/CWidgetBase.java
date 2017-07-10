package com.hkbs.HKBS;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.hkbs.HKBS.arkCalendar.MyCalendarLunar;
import com.hkbs.HKBS.arkUtil.MyUtil;

import org.arkist.share.AxAlarm;
import org.arkist.share.AxTools;

import java.util.Calendar;

public class CWidgetBase extends AppWidgetProvider {
	final static private boolean DEBUG = true && CMain.DEBUG;
	final static private String TAG = CWidgetBase.class.getSimpleName();
	final static private String CHI_MONTHS [] = {"一","二","三","四","五","六","七","八","九","十","十一","十二"};
    public boolean isLarger=false;
	public CWidgetBase() {

	}
    public String getClassTag(){
        return "";
    }
    public String getLayoutTag(){
        return "";
    }
    public int getLayoutId(){
        return R.layout.activity_cwidget;
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
	@Override
	public void onReceive(final Context context, Intent intent) {
		super.onReceive(context, intent);
        //if (intent !=null && intent.getAction()==Intent.ACTION_TIME_CHANGED) return;
		// Whatever receive; just do it.
		
////		int actionID = intent.getIntExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS,0);
////		switch (actionID){
////			case MyBroadcast.REQUEST_WIDGET:
//
//			    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//			    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CWidget.class.getName());
//			    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
//			    onUpdate(context, appWidgetManager, appWidgetIds);
//
////				break;
////		}
//		AxAlarm.setDailyOnDateChange(context);
        MyUtil.log(getLayoutTag(), "widget.onReceive "+((intent!=null && intent.getAction()!=null)?intent.getAction():""));
        refreshAll(context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AxAlarm.setDailyOnDateChange(context);
            }
        });
        thread.start();
	}
    public String getClassName(){
        return CWidgetBase.class.getName();
    }
    private void refreshAll(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClassName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        MyUtil.log(getLayoutTag(), "RefreshAll "+appWidgetIds.length+ "("+context.getPackageName()+")");
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
    @Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		MyUtil.log(getLayoutTag(), "widget.onUpdate nbrOfWidgets="+appWidgetIds.length);
		for (int i=0; i<appWidgetIds.length; i++) {
			ReceiveRef recRef = new ReceiveRef(context, appWidgetIds[i], getLayoutId());
			onRefresh(recRef); // No Intent
			doUpdateAppWidgetNow(context, recRef.views, recRef.widgetID);
	    }
//        final int N = appWidgetIds.length;
//        for (int i = 0; i < N; i++) {
//            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
//        }
	}
//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//
//        CharSequence widgetText = context.getString(R.string.appwidget_text);
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//    }
	private void doUpdateAppWidgetNow(Context context, RemoteViews views, int widgetID){
    	if (DEBUG) Log.i(getLayoutTag(),"doUpdateAppWidgetNow");
    	AppWidgetManager appMgr = AppWidgetManager.getInstance(context);
    	try {
	    	if (widgetID==0){
	    		ComponentName cname = new ComponentName(context, getClassName());
	    		appMgr.updateAppWidget(cname, views);
	    		if (DEBUG) Log.i(getLayoutTag(),"updateAppWidget All");
	    	} else {
	    		appMgr.updateAppWidget(widgetID, views);
	    	}
    	} catch (Exception e){
    		Log.e(TAG, "Update Widget Error !");
    		Log.e(TAG,e.getMessage());
    	}
	}
	private void onRefresh(ReceiveRef recRef){
		MyUtil.log(getLayoutTag(), "widget.onRefresh");
		MyUtil.initMyUtil(recRef.context);
		MyDailyBread mDailyBread = MyDailyBread.getInstance(recRef.context);
		Intent intent = new Intent(recRef.context, CMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent pendingIntent = PendingIntent.getActivity(recRef.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (pendingIntent!=null) {
            recRef.views.setOnClickPendingIntent(R.id.xmlPage1Middle, pendingIntent);
        }

        Intent broadcastIntent=new Intent(recRef.context, JustBroadcast.class);
        final PendingIntent broadcastPendingIntent = PendingIntent.getActivity(recRef.context,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);//PendingIntent.FLAG_UPDATE_CURRENT
        if (broadcastPendingIntent!=null) {
            recRef.views.setOnClickPendingIntent(R.id.xmlPage1, broadcastPendingIntent);
        }
        //recRef.views.setOnClickPendingIntent(R.id.xmlPage2, broadcastPendingIntent);
		
		int nbr = 1;
		Context context = recRef.context;
		Calendar mDisplayDay = Calendar.getInstance();
        // DC 2016.09.16 One More Day Or One Less Day for different Regions
		// Protect Function
		if (!MyDailyBread.allowBeyondRange && mDisplayDay.compareTo(mDailyBread.getValidToDate())>=0){
			mDisplayDay.setTimeInMillis(mDailyBread.getValidToDate().getTimeInMillis()-24*60*60*1000);
		} else if (mDisplayDay.compareTo(mDailyBread.getValidFrDate())<=0 ){
			mDisplayDay.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis()+24*60*60*1000);
		}
//		mDisplayDay.set(Calendar.YEAR, 2014);
//		mDisplayDay.set(Calendar.MONTH, 11);
//		mDisplayDay.set(Calendar.DAY_OF_MONTH, 25);
		int curYear = mDisplayDay.get(Calendar.YEAR);
		int curMonth = mDisplayDay.get(Calendar.MONTH);
		int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
		
		final MyCalendarLunar lunar = new MyCalendarLunar(mDisplayDay,MyApp.mIsSimplifiedChinese);
		final Calendar monthEndDate = (Calendar) mDisplayDay.clone();
		String holiday = MyHoliday.getHolidayRemark(mDisplayDay.getTime());
        holiday = holiday.replace("*","");
		final boolean isHoliday = (!holiday.equals("") && !holiday.startsWith("#")) || mDisplayDay.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY;
		int textColor = recRef.context.getResources().getColor(isHoliday?R.color.holiday:R.color.weekday);
		if (holiday.startsWith("#")){
			holiday=holiday.substring(1);
		}
		int nbrOfDaysTo30 = 30-lunar.getDay(); // Chinese Day
		monthEndDate.add(Calendar.DAY_OF_MONTH, nbrOfDaysTo30);
		final MyCalendarLunar monthEndLunar = new MyCalendarLunar(monthEndDate,MyApp.mIsSimplifiedChinese);
		boolean isBigMonth = (monthEndLunar.getDay()==30)?true:false;

        /***********************************************************************
         *  HOLIDAY
         ************************************************************************/
        final int holidayMaxChars=7;
        recRef.views.setViewVisibility(R.id.xmlPage1, View.VISIBLE);
		if (TextUtils.isEmpty(holiday)){
			recRef.views.setViewVisibility(getID(context, nbr, "Holiday1"), View.GONE);
			recRef.views.setViewVisibility(getID(context, nbr, "Holiday2"), View.GONE);		
		} else {
			recRef.views.setViewVisibility(getID(context, nbr, "Holiday1"), View.VISIBLE);			
			// Split Empty String will cause 1st one be empty; So we remove 1st and add back		
			String str [] = holiday.substring(1).split(""); 
			str[0] = holiday.substring(0,1);
			//ok now.
			int remarkLength = Math.min(holidayMaxChars*2,str.length);
			int prefixlength = Math.min(holidayMaxChars, str.length);
			String holidayRemark="";
			for (int i=0;i<prefixlength;i++){
				holidayRemark+=str[i]+(i==(prefixlength-1)?"":"\n");
			}
			//pageHoliday1.setLines(prefixlength);
			recRef.views.setTextViewText(getID(context, nbr, "Holiday1"), holidayRemark);
			recRef.views.setTextColor(getID(context, nbr, "Holiday1"), textColor);
			if (remarkLength>holidayMaxChars){
				recRef.views.setViewVisibility(getID(context, nbr, "Holiday2"), View.VISIBLE);
				holidayRemark="";
				for (int i=holidayMaxChars;i<remarkLength;i++){
					holidayRemark+=str[i]+(i==(remarkLength-1)?"":"\n");
				}
				//pageHoliday2.setLines(remarkLength-prefixlength);
				recRef.views.setTextViewText(getID(context, nbr, "Holiday2"), holidayRemark);
				recRef.views.setTextColor(getID(context, nbr, "Holiday2"), textColor);								
			} else {
				recRef.views.setViewVisibility(getID(context, nbr, "Holiday2"), View.GONE);				
			}
		}
        /***********************************************************************
         *  HOLY DAY
         ************************************************************************/
        if ((CMain.IS_2017_VERSION && Calendar.getInstance().get(Calendar.YEAR)>=2017) ||
            MyUtil.getPrefInt(MyUtil.PREF_HOLY_DAY,-1)<=0) {
            recRef.views.setViewVisibility(getID(context, nbr, "HolyDay1"), View.GONE);
            recRef.views.setViewVisibility(getID(context, nbr, "HolyDay2"), View.GONE);
        } else {
            String holyDay = MyHoliday.getHolyDayText(mDisplayDay.getTime());
            if (TextUtils.isEmpty(holyDay)) {
                recRef.views.setViewVisibility(getID(context, nbr, "HolyDay1"), View.GONE);
                recRef.views.setViewVisibility(getID(context, nbr, "HolyDay2"), View.GONE);
            } else {
                recRef.views.setViewVisibility(getID(context, nbr, "HolyDay1"), View.VISIBLE);
                recRef.views.setTextColor(getID(context, nbr, "HolyDay1"), textColor);
                String holyDayLines[] = DailyFragment.getHolyDay2Lines(holyDay);
                recRef.views.setTextViewText(getID(context, nbr, "HolyDay1"), holyDayLines[0]);
                if (TextUtils.isEmpty(holyDayLines[1])) {
                    recRef.views.setViewVisibility(getID(context, nbr, "HolyDay2"), View.GONE);
                } else {
                    recRef.views.setViewVisibility(getID(context, nbr, "HolyDay2"), View.VISIBLE);
                    recRef.views.setTextColor(getID(context, nbr, "HolyDay2"), textColor);
                    recRef.views.setTextViewText(getID(context, nbr, "HolyDay2"), holyDayLines[1]);
                }
            }
        }
        /***********************************************************************
         *  DAY
         ************************************************************************/
        recRef.views.setTextViewText(getID(context, nbr, "Day"), String.valueOf(curDay));
        recRef.views.setTextColor(getID(context, nbr, "Day"), textColor);

        recRef.views.setTextViewText(getID(context, nbr, "EngYear"), String.valueOf(curYear));
		recRef.views.setTextColor(getID(context, nbr, "EngYear"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "EngMonthName"), MyUtil.sdfEngMMMM.format(mDisplayDay.getTime()));
		recRef.views.setTextColor(getID(context, nbr, "EngMonthName"), textColor);
		
		//recRef.views.setTextViewText(getID(context, nbr, "ChiMonthName"), MyUtil.sdfChiMMMM.format(mDisplayDay.getTime()));
		recRef.views.setTextViewText(getID(context, nbr, "ChiMonthName"), CHI_MONTHS[mDisplayDay.get(Calendar.MONTH)]+"月");
		recRef.views.setTextColor(getID(context, nbr, "ChiMonthName"), textColor);
		
//		recRef.views.setTextViewText(getID(context, nbr, "WeekDay"), 
//								MyUtil.sdfChiEEEE.format(mDisplayDay.getTime())+" "+
//								MyUtil.sdfEEE.format(mDisplayDay.getTime()).toUpperCase());
		int theDay = mDisplayDay.get(Calendar.DAY_OF_WEEK);
		switch (theDay){
		case Calendar.SUNDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期日 SUN"); break;
		case Calendar.MONDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期一 MON"); break;
		case Calendar.TUESDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期二 TUE"); break;
		case Calendar.WEDNESDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期三 WED"); break;
		case Calendar.THURSDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期四 THU"); break;
		case Calendar.FRIDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期五 FRI"); break;
		case Calendar.SATURDAY: recRef.views.setTextViewText(getID(context, nbr, "WeekDay"),"星期六 SAT"); break;		
		}

        recRef.views.setTextColor(getID(context, nbr, "WeekDay"), context.getResources().getColor(R.color.white));
        if (curYear>=2016) {
            //recRef.views.setTextColor(getID(context, nbr, "WeekDay"), textColor);
            recRef.views.setImageViewResource(getID(context, nbr, "WeekImage"), isHoliday ? R.drawable.red_weekday_2016 : R.drawable.green_weekday_2016);
        } else {
            recRef.views.setImageViewResource(getID(context, nbr, "WeekImage"), isHoliday?R.drawable.red_weekday_2015:R.drawable.green_weekday_2015);
        }

		recRef.views.setTextViewText(getID(context, nbr, "ChiLunarDay"), lunar.toChineseDD()+"日");
		recRef.views.setTextColor(getID(context, nbr, "ChiLunarDay"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "ChiLunarMonth"),lunar.toChineseMM()+(isBigMonth?"大":"小"));
		recRef.views.setTextColor(getID(context, nbr, "ChiLunarMonth"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "ChiLunarYear"),lunar.toChineseYY()+"年");
		recRef.views.setTextColor(getID(context, nbr, "ChiLunarYear"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "ChiLeftYear"),lunar.toChineseYY()+"年");
		recRef.views.setTextColor(getID(context, nbr, "ChiLeftYear"), textColor);
		
		String lunarTerm = MyCalendarLunar.solar.getSolarTerm(mDisplayDay);
		recRef.views.setTextViewText(getID(context, nbr, "ChiLeftWeather"),lunarTerm);
		recRef.views.setTextColor(getID(context, nbr, "ChiLeftWeather"), textColor);		
		if (lunarTerm.equals("")){
			recRef.views.setViewVisibility(getID(context, nbr, "ChiLunarYear"), View.VISIBLE);
			recRef.views.setViewVisibility(getID(context, nbr, "ChiLeftYear"), View.GONE);
			recRef.views.setViewVisibility(getID(context, nbr, "ChiLeftWeather"), View.GONE);
		} else {
			recRef.views.setViewVisibility(getID(context, nbr, "ChiLunarYear"), View.GONE);
			recRef.views.setViewVisibility(getID(context, nbr, "ChiLeftYear"), View.VISIBLE);
			recRef.views.setViewVisibility(getID(context, nbr, "ChiLeftWeather"), View.VISIBLE);
		}
        float layoutWidthInPixels=0;
		if (CMain.IS_2016_VERSION) {
            if (curYear>=2016  || MyDailyBread.IS_TEST_2016_YEAR) {
                String dimenIdStr = context.getString(R.string.widget_layoutType) + getClassTag() + "_width";
                //Log.e(TAG,"package name="+context.getPackageName());
                int dimenId = context.getResources().getIdentifier(dimenIdStr, "dimen", context.getPackageName());// "com.hkbs.HKBS"

                try {
                    layoutWidthInPixels = context.getResources().getDimension(dimenId);
                } catch (Exception e1) {
                    try {
                        dimenIdStr = "v" + getClassTag() + "_width";
                        dimenId = context.getResources().getIdentifier(dimenIdStr, "dimen", context.getPackageName());// "com.hkbs.HKBS"
                        layoutWidthInPixels = context.getResources().getDimension(dimenId);
                    } catch (Exception e2) {
                        Log.e(TAG, "Cannot find dimen resource = " + dimenId + " " + dimenIdStr);
                    }
                }
                // For Widget only since some layout is very small
                // Scale down image caused image quality bad
                if (layoutWidthInPixels < 650) {
                    recRef.views.setImageViewResource(getID(context, nbr, "ImageFrame"), isHoliday || MyDailyBread.IS_TEST_2016_HOLIDAY ? R.drawable.red_frame_2016_26 : R.drawable.green_frame_2016_26);
                } else {
                    recRef.views.setImageViewResource(getID(context, nbr, "ImageFrame"), isHoliday || MyDailyBread.IS_TEST_2016_HOLIDAY ? R.drawable.red_frame_2016 : R.drawable.green_frame_2016);
                }
                recRef.views.setViewVisibility(getID(context, nbr, "ImageFrameUpper"), View.GONE);
                recRef.views.setViewVisibility(getID(context, nbr, "ImageFrameLower"), View.GONE);
                recRef.views.setViewVisibility(getID(context, nbr, "ImageFrame"), View.VISIBLE);
            } else {
                recRef.views.setImageViewResource(getID(context, nbr, "ImageFrameUpper"), isHoliday ? R.drawable.red_frame_2015_upper : R.drawable.green_frame_2015_upper);
                recRef.views.setImageViewResource(getID(context, nbr, "ImageFrameLower"), isHoliday ? R.drawable.red_frame_2015_upper : R.drawable.green_frame_2015_upper);
                recRef.views.setViewVisibility(getID(context, nbr, "ImageFrameUpper"), View.VISIBLE);
                recRef.views.setViewVisibility(getID(context, nbr, "ImageFrameLower"), View.VISIBLE);
                recRef.views.setViewVisibility(getID(context, nbr, "ImageFrame"), View.GONE);

            }
        } else {
            recRef.views.setImageViewResource(getID(context, nbr, "ImageFrame"), isHoliday ? R.drawable.red_frame_2015 : R.drawable.green_frame_2015);
            recRef.views.setViewVisibility(getID(context, nbr, "ImageFrameUpper"), View.GONE);
            recRef.views.setViewVisibility(getID(context, nbr, "ImageFrameLower"), View.GONE);
            recRef.views.setViewVisibility(getID(context, nbr, "ImageFrame"), View.VISIBLE);
        }
        if (curYear>=2016) {
            recRef.views.setImageViewResource(getID(context, nbr, "ImageIcon"), isHoliday ? R.drawable.red_icon_2016 : R.drawable.green_icon_2016);
        } else {
            recRef.views.setImageViewResource(getID(context, nbr, "ImageIcon"), isHoliday?R.drawable.red_icon_2015:R.drawable.green_icon_2015);
        }
		// get ContentValues from dailyBread file
		ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
/**********************************************************
 GOLD TEXT
 *********************************************************
 */
		recRef.views.setTextViewText(getID(context, nbr, "GoldVerse"), cv.getAsString(MyDailyBread.wGoldVerse) + (curYear >= 2016 ? ";和合本" : "；和合本修訂版"));
		recRef.views.setTextColor(getID(context, nbr, "GoldVerse"), textColor);
		
		String mGoldText;
        mGoldText = cv.getAsString(MyDailyBread.wGoldText).replace("#", "\n");
        mGoldText = mGoldText.substring(0, mGoldText.length());
		recRef.views.setTextViewText(getID(context, nbr, "GoldText"), mGoldText);
		recRef.views.setTextColor(getID(context, nbr, "GoldText"), textColor);

        if (cv.getAsString(MyDailyBread.wGoldText).split("#").length>=4){
            recRef.views.setViewVisibility(getID(context, nbr, "GoldVerse"), View.GONE);
        }

/**********************************************************
 WISDOM TEXT
 *********************************************************
 */
		recRef.views.setTextViewText(getID(context, nbr, "BigText"), cv.getAsString(MyDailyBread.wBigText).replace("#", "\n"));
		recRef.views.setTextColor(getID(context, nbr, "BigText"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "BigHint"), cv.getAsString(MyDailyBread.wSmallText));
		recRef.views.setTextColor(getID(context, nbr, "BigHint"), textColor);

        // All completed ..... show
		recRef.views.setViewVisibility(R.id.xmlWidgetLoading, View.GONE);
		recRef.views.setViewVisibility(R.id.xmlPage1, View.VISIBLE);

/**********************************************************
 DEBUG ONLY - SHOW LAYOUT SIZE
 *********************************************************
 */
        if (CMain.DEBUG_LAYOUT){
            String appVersionName = "?";
            try {
                appVersionName = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            } catch (Exception e){
                //
            }
            appVersionName="v"+appVersionName+"."+context.getString(R.string.widget_deviceType)+getClassTag()+" "+AxTools.getScreenWidth()+":"+layoutWidthInPixels;
            Log.w(TAG, appVersionName);
            recRef.views.setTextViewText(R.id.xmlWidgetVersion, appVersionName);
            recRef.views.setTextColor(R.id.xmlWidgetVersion, textColor);
            recRef.views.setViewVisibility(R.id.xmlWidgetVersion, View.VISIBLE);
        } else {
            recRef.views.setViewVisibility(R.id.xmlWidgetVersion, View.GONE);
        }
        MyUtil.log(getLayoutTag(), "widget.onRefresh.Finish");
	}
	private int getID(Context context, int nbr, String extension){
		int resultVal = context.getResources().getIdentifier("xmlPage"+nbr+extension, "id",context.getPackageName());
		if (resultVal==0){
			MyUtil.logError(TAG, "Error on:"+"xmlPage"+nbr+extension);
		}
		return resultVal; 
	}
	static class ReceiveRef{
		public Context context;
		public Intent intent;
		public RemoteViews views;
		public int widgetID;
		public int widgetAction;
		public AppWidgetManager appMgr;
        private int mLayoutId;
		
//		public ReceiveRef(Context context, Intent intent){
//			this.context = context;
//			this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget);
//			this.appMgr = AppWidgetManager.getInstance(context);
//			this.intent = intent;
//			this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
//			this.widgetAction = intent.getIntExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS,0);
//			if (DEBUG) Log.i(TAG,"onReceiveAction:"+this.widgetAction+","+this.widgetID);
//		}
		public ReceiveRef(Context context, int widgetID, int layoutId) {
            this.mLayoutId=layoutId;
            this.context = context;
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            int screen_type=(int) context.getResources().getDimension(R.dimen.screen_type);
            // 240 -> 216 ; 320 -> 339
            int smallSize= AxTools.dp2px(180);//widget size is 180
            int standardSize=AxTools.dp2px(240);//widget size is 240
            int sw600Size=AxTools.dp2px(360);//AxTools.dp2px(360);//widget size is 360
            int largeHeight=AxTools.dp2px(480);//AxTools.dp2px(360);//widget size is 360
            if (DEBUG) Log.i(TAG,"screenType:"+screen_type+" "+metrics.widthPixels+" "+smallSize+" "+standardSize+" "+sw600Size+" "+largeHeight);
            //if (android.os.Build.MODEL.equalsIgnoreCase("SM-A8000")){
//            if (MyUtil.scaleDensity(context)==3 && MyUtil.widthPixels(context)==1080 && MyUtil.heightPixels(context)==1920){
//                this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget_large);
//            } else {
            this.views = new RemoteViews(context.getPackageName(), mLayoutId);
//            }
			this.appMgr = AppWidgetManager.getInstance(context);
			this.widgetID = widgetID;
			this.intent = null;
			this.widgetAction = 0;
			if (DEBUG) Log.i(TAG,"onUpdateActionWidgetId="+this.widgetID);
		}
	}
	// It will auto update for every 30 minutes. NO need currently.
	//-> Call MyBroadcast -> Call CWidget onReceive -> Call CWidget onUpdate
	// All Activities can have receiver but we centrallized all receive in MyBroadcast and use Filter to control.
	// This app won't answer call individually
	static public void broadcastMe(Context context){
		if (DEBUG) MyUtil.log(TAG, "broadcastme.");
		Intent intent=new Intent(context,MyBroadcast.class);
		intent.setAction("com.hkbs.HKBS.WidgetUpdate");
		//intent.setData(ContentUris.withAppendedId(Uri.EMPTY, buttonID));
		intent.putExtra(AxAlarm.EXTRA_BROADCAST_CODE, MyBroadcast.REQUEST_WIDGET);
        // below statment should be different on every call so that it won't reuse and can't broadcase wrong variables
		//intentCounter++;
		intent.setData(Uri.withAppendedPath(Uri.parse(MyBroadcast.URI_SCHEME+ "://widget/"+MyBroadcast.REQUEST_WIDGET+"/"),"1"));
		context.sendBroadcast(intent);
	}
}
