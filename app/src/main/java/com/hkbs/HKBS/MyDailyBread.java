package com.hkbs.HKBS;

import android.content.ContentValues;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.hkbs.HKBS.arkUtil.MyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MyDailyBread {
	static private boolean IS_CURRENT_YEAR_2016_ONLY = false;
	static private boolean IS_CHECK_FUTURE_CHARS_ONLY = false;// PLEASE SET IT TO [[[false]]] for release
	static private boolean IS_CHECK_VALID_VERSE = false;
	static private boolean IS_CHECK_FIELD_VALUES = false; // MUST CHECK; DONOT SET TO false
	
	final static private boolean DEBUG=true;
	final static private String TAG = MyDailyBread.class.getSimpleName();
	
	static private MyDailyBread myDailyBread;
	static public int mAppWidth;
	static public int mAppHeight;
	
	static public int mGoldSizeL=0;
	static public int mGoldSizeM=0;
	static public int mGoldSizeS=0;
	static public int mHintSizeL=0;
	static public int mHintSizeS=0;
	
	static public int mGoldLineL=0;
	static public int mGoldLineM=0;
	static public int mGoldLineS=0;
	static public int mHintLineL=0;
	static public int mHintLineS=0;
	
	static public int mMaxGold_L_characters=0;
	static public int mMaxGold_M_characters=0;
	static public int mMaxGold_S_characters=0;
	static public int mMaxHint_L_characters=0;
	static public int mMaxHint_S_characters=0;
	
	private Context mContext;
	private List<ContentValues> mValueList;
	private Map<String, Integer> mMap;
	private Calendar validFrDate=null;
	private Calendar validToDate=null;
	//private ContentValues mEmptyContentValues;
	
	//GYear,GMonth,GDay,GoldText,GoldVerse,GoldFrame,GoldAlign,GoldSize,BigText,BigAlign,BigSize,SmallText
	final static String wGYear = "GYear";
	final static String wGMonth = "GMonth";
	final static String wGDay = "GDay";
	final static String wGoldText = "GoldText";
	final static String wGoldVerse = "GoldVerse";
	final static String wGoldFrame = "GoldFrame";
	final static String wGoldAlign = "GoldAlign";
	final static String wGoldSize = "GoldSize";
	final static String wBigText = "BigText";
	final static String wBigAlign = "BigAlign";
	final static String wBigSize = "BigSize";
	final static String wSmallText = "SmallText";
	
//	static public MyDailyBread getInstance(Activity act){
//		final Context context = act.getBaseContext();
	static public MyDailyBread getInstance(Context context){			
		// Step 1: Default Font Size (before init myDailyBread class) 
//	    mGoldSizeL = context.getResources().getDimensionPixelOffset(R.dimen.text_gold_size_l);
//	    mGoldSizeM = context.getResources().getDimensionPixelOffset(R.dimen.text_gold_size_m);
//	    mGoldSizeS = context.getResources().getDimensionPixelOffset(R.dimen.text_gold_size_s);
//	    mHintSizeL = context.getResources().getDimensionPixelOffset(R.dimen.text_hint_size_l);
//	    mHintSizeS = context.getResources().getDimensionPixelOffset(R.dimen.text_hint_size_s);		
		// Step 2: App Width & Height (Calculate before init myDailyBread class
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    final DisplayMetrics dm = new DisplayMetrics();
	    wm.getDefaultDisplay().getMetrics(dm);
	    mAppHeight = dm.heightPixels;
	    mAppWidth = dm.widthPixels;
		if (myDailyBread==null){
			myDailyBread = new MyDailyBread(context);
		}
	    return myDailyBread;
	}
	private void setValidRange(){
		if (IS_CURRENT_YEAR_2016_ONLY){
			validFrDate = Calendar.getInstance();
			validFrDate.set(2014, 10, 1, 23, 59, 59); // 11-01
			validToDate = Calendar.getInstance();
			validToDate.set(2016, 11, 31, 0, 0, 0); // 2013-12-1
		}
	}
	public Calendar getValidFrDate(){
		setValidRange();
		return validFrDate;
	}
	public Calendar getValidToDate(){
		setValidRange();
		return validToDate;
	}
	public MyDailyBread(Context context) {
		mContext=context;
		readFileFromAsset();
	}
	private void readFileFromAsset(){
		try {
			Calendar today = Calendar.getInstance();
			final InputStreamReader iReader = new InputStreamReader(mContext.getAssets().open("dailyBread.csv"), "UTF-8"); 
			final BufferedReader bReader = new BufferedReader(iReader); 
		    // do reading, usually loop until end of file reading 
		    String line = bReader.readLine();
		    ContentValues cvGoldFrame=null; 
    		ContentValues cvGoldAlign=null;
    		ContentValues cvGoldSize=null;
    		ContentValues cvBigAlign=null;
    		ContentValues cvBigSize=null;

    		String maxGold_L_date="";
    		String maxGold_M_date="";
    		String maxGold_S_date="";
    		String maxHint_L_date="";
    		String maxHint_S_date="";
    		String maxGold_L_str="";
    		String maxGold_M_str="";
    		String maxGold_S_str="";
    		String maxHint_L_str="";
    		String maxHint_S_str="";
    		int lastYear=0;
    		int lastMonth=0;
    		int lastDay=0;
		    // 1st Line Header
		    if (line!=null){
		    	String [] titles = line.split(",");
//		    	mEmptyContentValues = new ContentValues(titles.length);
//		    	for (int k=0;k<titles.length;k++){
//		    		mEmptyContentValues.put(titles[k], "");
//		    	}
		    	mValueList = new ArrayList<ContentValues>();
		    	mMap = new HashMap<String, Integer>();
//		    	validFrDate = Calendar.getInstance();
//		    	validFrDate.set(2013, 0, 1, 0, 0, 0);
//		    	validToDate = Calendar.getInstance();
//		    	validToDate.set(2013, 0, 1, 59, 59, 59);		    			    	
		    	line = bReader.readLine();
		    	int counter=0;
		    	if (IS_CHECK_FIELD_VALUES){
		    		cvGoldFrame = new ContentValues(); 
		    		cvGoldAlign = new ContentValues();
		    		cvGoldSize = new ContentValues();
		    		cvBigAlign = new ContentValues();
		    		cvBigSize = new ContentValues();
		    	}
			    while (line != null) {
			    	line.replace("\"", "");
			    	String [] fields = line.split(",");
			    	if (fields.length!=titles.length){
			    		MyUtil.logError("#","NbrOfFields [,] Wrong:"+line);
			    		for (int j=0;j<fields.length;j++){
			    			MyUtil.logError("#", "Field["+j+"]"+fields[j]);
			    		}
			    	} else {
			    		// Assign to mValueList
				    	ContentValues cv = new ContentValues(titles.length);
				    	for (int i=0;i<titles.length;i++){
				    		cv.put(titles[i],fields[i]);
				    	}
				    	if (cv.getAsString(wSmallText).equals(".")){
				    		cv.put(wSmallText, "");
				    	}
				    	lastYear=cv.getAsInteger(wGYear);
				    	lastMonth=cv.getAsInteger(wGMonth)-1;
				    	lastDay=cv.getAsInteger(wGDay);
				    	mValueList.add(mValueList.size(), cv);
				    	mMap.put(getDayString(lastYear,lastMonth,lastDay), counter);
				    	counter++;
				    	// Assign From/To Date				    	
				    	final Calendar newDate = Calendar.getInstance(); 
				    	newDate.set(Calendar.YEAR, lastYear);
				    	newDate.set(Calendar.MONTH, lastMonth);
				    	newDate.set(Calendar.DAY_OF_MONTH, lastDay);
				    	if (validFrDate==null){
				    		validFrDate = Calendar.getInstance();
				    		validFrDate.set(Calendar.YEAR, lastYear);
				    		validFrDate.set(Calendar.MONTH, lastMonth);
				    		validFrDate.set(Calendar.DAY_OF_MONTH, lastDay);
				    		validFrDate.set(Calendar.HOUR_OF_DAY, 23);
				    		validFrDate.set(Calendar.MINUTE, 59);
				    		validFrDate.set(Calendar.SECOND, 59);
				    	}
				    	if (newDate.compareTo(validFrDate)<0){
				    		validFrDate = (Calendar) newDate.clone();
				    		validFrDate.set(Calendar.HOUR_OF_DAY, 23);
				    		validFrDate.set(Calendar.MINUTE, 59);
				    		validFrDate.set(Calendar.SECOND, 59);
				    	}
				    	if (validToDate==null){
				    		validToDate = Calendar.getInstance();
				    		validToDate.set(Calendar.YEAR, lastYear);
				    		validToDate.set(Calendar.MONTH, lastMonth);
				    		validToDate.set(Calendar.DAY_OF_MONTH, lastDay);
				    		validToDate.set(Calendar.HOUR_OF_DAY, 0);
				    		validToDate.set(Calendar.MINUTE, 0);
				    		validFrDate.set(Calendar.SECOND, 0);
				    	}
				    	if (newDate.compareTo(validToDate)>0){
				    		validToDate = (Calendar) newDate.clone();
				    		validToDate.set(Calendar.HOUR_OF_DAY,0);
				    		validToDate.set(Calendar.MINUTE, 0);
				    		validToDate.set(Calendar.SECOND, 0);
				    	}				    	
				    	// Check Value
				    	if (IS_CHECK_VALID_VERSE){
				    		if (CMain.getEBCV(cv.getAsString(wGoldVerse))==null){
				    			MyUtil.logError(TAG, newDate.get(Calendar.YEAR)+"-"+newDate.get(Calendar.MONTH)+"-"+newDate.get(Calendar.DAY_OF_MONTH));
				    		}
				    	}
				    	if (IS_CHECK_FIELD_VALUES){
				    		// Assign Unique Value to Register
				    		if (!cvGoldFrame.containsKey(cv.getAsString(wGoldFrame))){
				    			cvGoldFrame.put(cv.getAsString(wGoldFrame), "");
				    		}
				    		if (!cvGoldAlign.containsKey(cv.getAsString(wGoldAlign))){
				    			cvGoldAlign.put(cv.getAsString(wGoldAlign), "");
				    		}
				    		if (!cvGoldSize.containsKey(cv.getAsString(wGoldSize))){
				    			cvGoldSize.put(cv.getAsString(wGoldSize), "");
				    		}
				    		if (!cvBigAlign.containsKey(cv.getAsString(wBigAlign))){
				    			cvBigAlign.put(cv.getAsString(wBigAlign), "");
				    		}
				    		if (!cvBigSize.containsKey(cv.getAsString(wBigSize))){
				    			cvBigSize.put(cv.getAsString(wBigSize), "");
				    		}				    		
				    	}
				    	String [] goldLines = cv.getAsString(wGoldText).split("#");
				    	for (int i=0;i<goldLines.length;i++){
				    		if (IS_CHECK_FUTURE_CHARS_ONLY){
				    			if (newDate.get(Calendar.YEAR)<today.get(Calendar.YEAR)){
				    				continue;
				    			} else if (newDate.get(Calendar.YEAR)==today.get(Calendar.YEAR)){
				    				if (newDate.get(Calendar.MONTH)!=Calendar.DECEMBER && newDate.get(Calendar.MONTH)!=Calendar.NOVEMBER){
				    					continue;
				    				}
				    			} 
				    		}
				    		if (cv.getAsString(wGoldSize).equalsIgnoreCase("L")){				    			
				    			if (goldLines[i].length()>mMaxGold_L_characters){
				    				maxGold_L_str = goldLines[i];
				    				maxGold_L_date = getDayString(lastYear, lastMonth, lastDay);
				    			}
				    			mGoldLineL = Math.max(mGoldLineL, goldLines.length);
				    			mMaxGold_L_characters=Math.max(mMaxGold_L_characters,goldLines[i].length());	
				    		} else if (cv.getAsString(wGoldSize).equalsIgnoreCase("M")){
				    			if (goldLines[i].length()>mMaxGold_M_characters){
				    				maxGold_M_str = goldLines[i];
				    				maxGold_M_date = getDayString(lastYear, lastMonth, lastDay);
				    			}
				    			mGoldLineM = Math.max(mGoldLineM, goldLines.length);
				    			mMaxGold_M_characters=Math.max(mMaxGold_M_characters,goldLines[i].length());
				    		} else {
				    			if (goldLines[i].length()>mMaxGold_S_characters){
				    				maxGold_S_str = goldLines[i];
				    				maxGold_S_date = getDayString(lastYear, lastMonth, lastDay);
				    			}
				    			mGoldLineS = Math.max(mGoldLineS, goldLines.length);
				    			mMaxGold_S_characters=Math.max(mMaxGold_S_characters,goldLines[i].length());
				    		}				    		
				    	}
				    	String [] hintLines = cv.getAsString(wBigText).split("#");
				    	for (int i=0;i<hintLines.length;i++){
				    		if (IS_CHECK_FUTURE_CHARS_ONLY){
				    			if (newDate.get(Calendar.YEAR)<today.get(Calendar.YEAR)){
				    				continue;
				    			} else if (newDate.get(Calendar.YEAR)==today.get(Calendar.YEAR)){
				    				if (newDate.get(Calendar.MONTH)!=Calendar.DECEMBER && newDate.get(Calendar.MONTH)!=Calendar.NOVEMBER){
				    					continue;
				    				}
				    			} 
				    		}
				    		if (cv.getAsString(wBigSize).equalsIgnoreCase("L")){
				    			if (hintLines[i].indexOf("Mama")==-1){//Special character in csv
					    			if (hintLines[i].length()>mMaxHint_L_characters){
					    				maxHint_L_str = hintLines[i];
					    				maxHint_L_date = getDayString(lastYear, lastMonth, lastDay);
					    			}
					    			mHintLineL = Math.max(mHintLineL, goldLines.length);
					    			mMaxHint_L_characters=Math.max(mMaxHint_L_characters,hintLines[i].length());
				    			}
				    		} else {
				    			if (hintLines[i].indexOf("Quot")==-1){//Special character in csv
					    			if (hintLines[i].length()>mMaxHint_S_characters){
					    				maxHint_S_str = hintLines[i];
					    				maxHint_S_date = getDayString(lastYear, lastMonth, lastDay);
					    			}
					    			mHintLineS = Math.max(mHintLineS, goldLines.length);
					    			mMaxHint_S_characters=Math.max(mMaxHint_S_characters,hintLines[i].length());
				    			}
				    		}				    		
				    	}
			    	}
			    	// Next Line
			    	line = bReader.readLine();	
			    	
			    }
			}
		    bReader.close();
		    if (DEBUG) MyUtil.log(TAG, getDayString(validFrDate)+" "+getDayString(validToDate));
		    //Toast.makeText(mContext, "資料庫顯示範圍:"+getDayString(validFrDate)+" 至 "+getDayString(validToDate), Toast.LENGTH_LONG).show();
		    if (IS_CHECK_FIELD_VALUES){
		    	printContentValues(wGoldFrame, cvGoldFrame);// L,S ... S for ??? 大L/小S
		    	printContentValues(wGoldAlign, cvGoldAlign);// L,C (Left,Center)
		    	printContentValues(wGoldSize, cvGoldSize);// L,M,S (Large, Middle, Small)
		    	printContentValues(wBigAlign, cvBigAlign);// L,S,C ... S for ???
		    	printContentValues(wBigSize, cvBigSize);// L,S (Large, Small)
		    }		    	
		    	MyUtil.log(TAG,"Remeber to clear space !"+(IS_CHECK_FUTURE_CHARS_ONLY?"<Check Furture Only>":"<Check All>"));
		    	MyUtil.log(TAG, "GoldSize L maxCharacters:"+mMaxGold_L_characters+" lines:"+mGoldLineL+" "+maxGold_L_date+" "+maxGold_L_str);
		    	MyUtil.log(TAG, "GoldSize M maxCharacters:"+mMaxGold_M_characters+" lines:"+mGoldLineM+" "+maxGold_M_date+" "+maxGold_M_str);
		    	MyUtil.log(TAG, "GoldSize S maxCharacters:"+mMaxGold_S_characters+" lines:"+mGoldLineS+" "+maxGold_S_date+" "+maxGold_S_str);
		    	MyUtil.log(TAG, "HintSize L maxCharacters:"+mMaxHint_L_characters+" lines:"+mHintLineL+" "+maxHint_L_date+" "+maxHint_L_str);
		    	MyUtil.log(TAG, "HintSize S maxCharacters:"+mMaxHint_S_characters+" lines:"+mHintLineS+" "+maxHint_S_date+" "+maxHint_S_str);
		    	/*
		    	 * 2013.09.08
		    	 * 2013.06.05
		    	 * 2013.07.18
		    	 *   12.02.14
		    	 * 2013.12.01
		    	 */
//		    	final int usableWidth = (int) (mAppWidth * 0.9);
//		    	mGoldSizeL = (int) Math.floor(usableWidth / maxGold_L_characters);
//		    	mGoldSizeM = (int) Math.floor(usableWidth / maxGold_M_characters);
//		    	mGoldSizeS = (int) Math.floor(usableWidth / maxGold_S_characters);
//		    	mHintSizeL = (int) Math.floor(usableWidth / maxHint_L_characters);
//		    	mHintSizeS = (int) Math.floor(usableWidth / maxHint_S_characters);
//		    	MyUtil.log(TAG,"AppWidth:"+mAppWidth);
//		    	MyUtil.log(TAG,"AppHeight:"+mAppHeight);		    	
//		    	MyUtil.log(TAG,"GoldSize L px:"+mGoldSizeL);// 24 chars 2014-07-08
//		    	MyUtil.log(TAG,"GoldSize M px:"+mGoldSizeM);// 20 chars 2014-05-28
//		    	MyUtil.log(TAG,"GoldSize S px:"+mGoldSizeS);// 25 chars 2014-09-10 
//		    	MyUtil.log(TAG,"HintSize L px:"+mHintSizeL);// 26 chars 2014-04-15
//		    	MyUtil.log(TAG,"HintSize S px:"+mHintSizeS);// 24 chars 2014-05-08		    	
		    
		} catch (IOException e) {
		    //log the exception
		}
	}
	static public String getDayString(int year, int month, int day){
		return year+"-"+(month+1)+"-"+day;
	}
	static public String getDayString(String year, String month, String day){
		return Integer.valueOf(year)+"-"+(Integer.valueOf(month)+1)+"-"+Integer.valueOf(day);
	}
	static public String getDayString(Calendar calendar){
		return calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
	}
	private void printContentValues(String str, ContentValues vals)	{
	   Set<Entry<String, Object>> s=vals.valueSet();
	   Iterator<?> itr = s.iterator();
	   MyUtil.log("#", "ContentValue["+str+"]"+vals.size());
	   while(itr.hasNext()){
	        Map.Entry<String, String> me = (Map.Entry<String, String>) itr.next(); 
	        String key = me.getKey().toString();
	        Object value =  me.getValue();
	        MyUtil.log(TAG,"Key["+key+"] values["  + (String)(value == null?null:value.toString())+"]");
	   }
	}
	public ContentValues getContentValues(int year, int month, int day){
		try {
			int keyNbr = mMap.get(getDayString(year, month, day));
			return mValueList.get(keyNbr);
		} catch (Exception e){
			return null;
		}
	}
	public String getValues(int year, int month, int day, String keyWord){
		int keyNbr = mMap.get(getDayString(year, month, day));
		ContentValues cv = mValueList.get(keyNbr);
		if (cv==null){
			return "";
		} else {
			return cv.getAsString(keyWord);
		}
	}	
}