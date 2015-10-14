package com.hkbs.HKBS;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class CWidgetLarge extends AppWidgetProvider {
	final static private boolean DEBUG = true;
	final static private String TAG = CWidgetLarge.class.getSimpleName();
	final static private String CHI_MONTHS [] = {"一","二","三","四","五","六","七","八","九","十","十一","十二"};

	public CWidgetLarge() {

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
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);	
		MyUtil.log(TAG, "widget.onReceive"+((intent!=null && intent.getAction()!=null)?intent.getAction():""));
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

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CWidgetLarge.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);

        AxAlarm.setDailyOnDateChange(context);

	}
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		MyUtil.log(TAG, "widget.onUpdate");
		for (int i=0; i<appWidgetIds.length; i++) {
			ReceiveRef recRef = new ReceiveRef(context, appWidgetIds[i]); 
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
    	if (DEBUG) Log.i(TAG,"doUpdateAppWidgetNow");
    	AppWidgetManager appMgr = AppWidgetManager.getInstance(context);
    	try {
	    	if (widgetID==0){
	    		ComponentName cname = new ComponentName(context, CWidgetLarge.class);
	    		appMgr.updateAppWidget(cname, views);
	    		if (DEBUG) Log.i(TAG,"updateAppWidget All");
	    	} else {
	    		appMgr.updateAppWidget(widgetID, views);
	    	}
    	} catch (Exception e){
    		Log.e(TAG, "Update Widget Error !");
    		Log.e(TAG,e.getMessage());
    	}
	}
	private void onRefresh(ReceiveRef recRef){
		MyUtil.log(TAG, "widget.onRefresh");
		MyUtil.initMyUtil(recRef.context);
		MyDailyBread mDailyBread = MyDailyBread.getInstance(recRef.context);
		Intent intent = new Intent(recRef.context, CMain.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(recRef.context, 
        		0, intent, PendingIntent.FLAG_UPDATE_CURRENT);//PendingIntent.FLAG_UPDATE_CURRENT
		recRef.views.setOnClickPendingIntent(R.id.xmlPage1Middle, pendingIntent);
		
		int nbr = 1;
		Context context = recRef.context;
		Calendar mDisplayDay = Calendar.getInstance();
		// Protect Function
		if (mDisplayDay.compareTo(mDailyBread.getValidToDate())>0){
			mDisplayDay.setTimeInMillis(mDailyBread.getValidToDate().getTimeInMillis());
		} else if (mDisplayDay.compareTo(mDailyBread.getValidFrDate())<0 ){
			mDisplayDay.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis());	
		}
//		mDisplayDay.set(Calendar.YEAR, 2014);
//		mDisplayDay.set(Calendar.MONTH, 11);
//		mDisplayDay.set(Calendar.DAY_OF_MONTH, 25);
		int curYear = mDisplayDay.get(Calendar.YEAR);
		int curMonth = mDisplayDay.get(Calendar.MONTH);
		int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
		
		final MyCalendarLunar lunar = new MyCalendarLunar(mDisplayDay);
		final Calendar monthEndDate = (Calendar) mDisplayDay.clone();
		String holiday = MyHoliday.getHolidayRemark(mDisplayDay.getTime());
		final boolean isHoliday = (!holiday.equals("") && !holiday.startsWith("#")) || mDisplayDay.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY;
		int textColor = recRef.context.getResources().getColor(isHoliday?R.color.holiday:R.color.weekday);
		
		if (holiday.startsWith("#")){
			holiday=holiday.substring(1);
		}
		
		int nbrOfDaysTo30 = 30-lunar.getDay(); // Chinese Day
		monthEndDate.add(Calendar.DAY_OF_MONTH, nbrOfDaysTo30);
		final MyCalendarLunar monthEndLunar = new MyCalendarLunar(monthEndDate);
		boolean isBigMonth = (monthEndLunar.getDay()==30)?true:false;
		
		// Assign Date Related TextView		
		recRef.views.setTextViewText(getID(context, nbr, "Day"), String.valueOf(curDay));
		recRef.views.setTextColor(getID(context, nbr, "Day"), textColor);
		
		
		final int maxCharacters=7;
		if (holiday.equals("")){
			recRef.views.setViewVisibility(getID(context, nbr, "Holiday1"), View.GONE);
			recRef.views.setViewVisibility(getID(context, nbr, "Holiday2"), View.GONE);		
		} else {
			recRef.views.setViewVisibility(getID(context, nbr, "Holiday1"), View.VISIBLE);			
			// Split Empty String will cause 1st one be empty; So we remove 1st and add back		
			String str [] = holiday.substring(1).split(""); 
			str[0] = holiday.substring(0,1);
			//ok now.
			int remarkLength = Math.min(maxCharacters*2,str.length);
			int prefixlength = Math.min(maxCharacters, str.length);
			String holidayRemark="";
			for (int i=0;i<prefixlength;i++){
				holidayRemark+=str[i]+(i==(prefixlength-1)?"":"\n");
			}
			//pageHoliday1.setLines(prefixlength);
			recRef.views.setTextViewText(getID(context, nbr, "Holiday1"), holidayRemark);
			recRef.views.setTextColor(getID(context, nbr, "Holiday1"), textColor);
			if (remarkLength>maxCharacters){
				recRef.views.setViewVisibility(getID(context, nbr, "Holiday2"), View.VISIBLE);
				holidayRemark="";
				for (int i=maxCharacters;i<remarkLength;i++){
					holidayRemark+=str[i]+(i==(remarkLength-1)?"":"\n");
				}
				//pageHoliday2.setLines(remarkLength-prefixlength);
				recRef.views.setTextViewText(getID(context, nbr, "Holiday2"), holidayRemark);
				recRef.views.setTextColor(getID(context, nbr, "Holiday2"), textColor);								
			} else {
				recRef.views.setViewVisibility(getID(context, nbr, "Holiday2"), View.GONE);				
			}
		}
		
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
		
		recRef.views.setImageViewResource(getID(context, nbr, "WeekImage"), isHoliday?R.drawable.red_weekday_2015:R.drawable.green_weekday_2015);

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
		
		recRef.views.setImageViewResource(getID(context, nbr, "ImageFrame"), isHoliday?R.drawable.red_frame_2015:R.drawable.green_frame_2015);
		recRef.views.setImageViewResource(getID(context, nbr, "ImageIcon"), isHoliday?R.drawable.red_icon_2015:R.drawable.green_icon_2015);

		// get ContentValues from dailyBread file
		ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);

		// GOLD TEXT
		recRef.views.setTextViewText(getID(context, nbr, "GoldVerse"),cv.getAsString(MyDailyBread.wGoldVerse)+":和合本修訂版");
		recRef.views.setTextColor(getID(context, nbr, "GoldVerse"), textColor);
		
		String mGoldText = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ");
		if (mGoldText.length()>=12){
			mGoldText = mGoldText.substring(0, 12)+"...\n(按此觀看更多)";
		} else {
			mGoldText = mGoldText.substring(0, mGoldText.length());
		}
		recRef.views.setTextViewText(getID(context, nbr, "GoldText"),mGoldText);
		recRef.views.setTextColor(getID(context, nbr, "GoldText"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "BigText"),cv.getAsString(MyDailyBread.wBigText).replace("#", "\n"));
		recRef.views.setTextColor(getID(context, nbr, "BigText"), textColor);
		
		recRef.views.setTextViewText(getID(context, nbr, "BigHint"),cv.getAsString(MyDailyBread.wSmallText));
		recRef.views.setTextColor(getID(context, nbr, "BigHint"), textColor);
		
		// All completed ..... show
		recRef.views.setViewVisibility(R.id.xmlWidgetLoading, View.GONE);
		recRef.views.setViewVisibility(R.id.xmlPage1, View.VISIBLE);
	}
	private int getID(Context context, int nbr, String extension){
		int resultVal = context.getResources().getIdentifier("xmlPage"+nbr+extension, "id", "com.hkbs.HKBS");
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
		
//		public ReceiveRef(Context context, Intent intent){
//			this.context = context;
//			this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget);
//			this.appMgr = AppWidgetManager.getInstance(context);
//			this.intent = intent;
//			this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
//			this.widgetAction = intent.getIntExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS,0);
//			if (DEBUG) Log.i(TAG,"onReceiveAction:"+this.widgetAction+","+this.widgetID);
//		}
		public ReceiveRef(Context context, int widgetID) {
            this.context = context;
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            int screen_type=(int) context.getResources().getDimension(R.dimen.screen_type);
            // 240 -> 216 ; 320 -> 339
            int smallSize= AxTools.dp2px(180);//widget size is 180
            int standardSize=AxTools.dp2px(240);//widget size is 240
            int sw600Size=AxTools.dp2px(360);//AxTools.dp2px(360);//widget size is 360
            if (DEBUG) Log.i(TAG,"screenType:"+screen_type+" "+metrics.widthPixels+" "+smallSize+" "+standardSize+" "+sw600Size);
            //if (android.os.Build.MODEL.equalsIgnoreCase("SM-A8000")){
            //if (MyUtil.scaleDensity(context)==3 && MyUtil.widthPixels(context)==1080 && MyUtil.heightPixels(context)==1920){
                this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget_large);
            //} else {
            //    this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget);
           // }
//            if (screen_type==0 || screen_type==2){//small & large (use its default in folder)
//                this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget);
//            } else {
//                if (metrics.widthPixels >= sw600Size){//sw600Size){
//                    if (DEBUG) Log.i(TAG,"Widget Standard Big");
//                    this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget_large);
//                } else {
//                    if (DEBUG) Log.i(TAG,"Widget Standard Small");
//                    this.views = new RemoteViews(context.getPackageName(), R.layout.activity_cwidget);
//                }
//            }

			this.appMgr = AppWidgetManager.getInstance(context);
			this.widgetID = widgetID;
			this.intent = null;
			this.widgetAction = 0;
			if (DEBUG) Log.i(TAG,"onUpdateAction:"+this.widgetID);
		}
	}
	// It will auto update for every 30 minutes. NO need currently.
	//-> Call MyBroadcast -> Call CWidget onReceive -> Call CWidget onUpdate
	// All Activities can have receiver but we centrallized all receive in MyBroadcast and use Filter to control.
	// This app won't answer call individually
	static public void broadcastMe(Context context){
		MyUtil.log(TAG, "broadcastme.");
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
