package com.hkbs.HKBS;

import android.content.ContentValues;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.hkbs.HKBS.arkUtil.MyUtil;

import org.arkist.share.AxDebug;
import org.arkist.share.AxTools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MyDailyBread {
    // 2016.03.16 Change to 4 lines
    // 2016.04.15 / 19
    // 2016.05.10 / 14 / 20 / 22
    // 2016.06.03 / 17
    static public boolean allowBeyondRange = true;
    static public int beyondFrYear = 2015;
    static public int beyondToYear = 2063;

    static public int mCurrentYear = 2015;//Valid Range will be from last SEPT
    static public boolean IS_TEST_2016_STANDARD = false && CMain.DEBUG;
    static public boolean IS_TEST_2016_HOLIDAY = false && IS_TEST_2016_STANDARD && CMain.DEBUG;
    static public boolean IS_TEST_2016_YEAR = false && IS_TEST_2016_STANDARD && CMain.DEBUG;
    static private boolean IS_CURRENT_YEAR_ONLY = false;
    static private boolean IS_CHECK_FUTURE_CHARS_ONLY = false && CMain.DEBUG;// PLEASE SET IT TO [[[false]]] for release
    static private boolean IS_CHECK_VALID_VERSE = true && CMain.DEBUG;
    static private boolean IS_CHECK_FIELD_VALUES = false;
    static private boolean IS_CHECK_IF_LESS_THAN_4_LINES = true;

    final static private boolean DEBUG = true && CMain.DEBUG;
    final static private String TAG = MyDailyBread.class.getSimpleName();

    static private MyDailyBread myDailyBread;
    static public int mAppWidth;
    static public int mAppHeight;
//    static public final int DEVICE_NORMAL = 0;
//    static public final int DEVICE_XIAOMI3 = 1;
//    static public int mSpecialDevice = DEVICE_NORMAL;

    static public int mGoldSizeL = 0;
    static public int mGoldSizeM = 0;
    static public int mGoldSizeS = 0;
    static public int mHintSizeL = 0;
    static public int mHintSizeS = 0;

    static public String mGold_L_LastDate[] = {"", "", "", "", "", ""};
    static public String mGold_M_LastDate[] = {"", "", "", "", "", ""};
    static public String mGold_S_LastDate[] = {"", "", "", "", "", ""};
    static public String mHint_L_LastDate[] = {"", "", "", "", "", ""};
    static public String mHint_S_LastDate[] = {"", "", "", "", "", ""};

    static public int mGold_L_NbrOfRecords[] = {0, 0, 0, 0, 0, 0};
    static public int mGold_M_NbrOfRecords[] = {0, 0, 0, 0, 0, 0};
    static public int mGold_S_NbrOfRecords[] = {0, 0, 0, 0, 0, 0};
    static public int mHint_L_NbrOfRecords[] = {0, 0, 0, 0, 0, 0};
    static public int mHint_S_NbrOfRecords[] = {0, 0, 0, 0, 0, 0};

    static public int mMaxGold_L_characters = 0;
    static public int mMaxGold_M_characters = 0;
    static public int mMaxGold_S_characters = 0;
    static public int mMaxHint_L_characters = 0;
    static public int mMaxHint_S_characters = 0;

    static public int mMinGold_L_characters = 999;
    static public int mMinGold_M_characters = 999;
    static public int mMinGold_S_characters = 999;
    static public int mMinHint_L_characters = 999;
    static public int mMinHint_S_characters = 999;

    private Context mContext;
    private List<ContentValues> mValueList;
    private Map<String, Integer> mMap;
    private Calendar validFrDate = null;
    private Calendar validToDate = null;

    //GYear,GMonth,GDay,GoldText,GoldVerse,GoldFrame,GoldAlign,GoldSize,BigText,BigAlign,BigSize,SmallText
    final static public String wGYear = "GYear";
    final static public String wGMonth = "GMonth";
    final static public String wGDay = "GDay";
    final static public String wGoldText = "GoldText";
    final static public String wGoldVerse = "GoldVerse";
    final static public String wGoldFrame = "GoldFrame";
    final static public String wGoldAlign = "GoldAlign";
    final static public String wGoldSize = "GoldSize";
    final static public String wBigText = "BigText";
    final static public String wBigAlign = "BigAlign";
    final static public String wBigSize = "BigSize";
    final static public String wSmallText = "SmallText";
    final static public String wLunarYear = "LunarYear";
    final static public String wLunarAnimal = "LunarAnimal";
    final static public String wLunarMonth = "LunarMonth";
    final static public String wLunarBigSmall = "LunarBigSmall";
    final static public String wLunarDay = "LunarDay";
    final static public String wLunarSeason = "LunarSeason";

    //	static public MyDailyBread getInstance(Activity act){
//		final Context context = act.getBaseContext();
    static public MyDailyBread getInstance(Context context) {
        // Step 1: Default Font Size (before init myDailyBread class)
//	    mGoldSizeL = context.getResources().getDimensionPixelOffset(R.dimen.text_gold_size_l);
//	    mGoldSizeM = context.getResources().getDimensionPixelOffset(R.dimen.text_gold_size_m);
//	    mGoldSizeS = context.getResources().getDimensionPixelOffset(R.dimen.text_gold_size_s);
//	    mHintSizeL = context.getResources().getDimensionPixelOffset(R.dimen.text_hint_size_l);
//	    mHintSizeS = context.getResources().getDimensionPixelOffset(R.dimen.text_hint_size_s);		
        // Step 2: App Width & Height (Calculate before init myDailyBread class
        // DC 20212
//        DisplayMetrics dm = new DisplayMetrics();
//        getDisplay(context).getMetrics(dm);

//        if (DEBUG) {
//            if (MyUtil.widthPixels(context)==1080 &&
//                MyUtil.heightPixels(context)==1920 &&
//                MyUtil.scaleDensity(context)==3){
//                mSpecialDevice=DEVICE_XIAOMI3;
//            }
//        } else {
//            if (android.os.Build.BRAND.equalsIgnoreCase("Xiaomi") &&
//                android.os.Build.MODEL.contains("MI") &&
//                android.os.Build.MODEL.contains("3W") &&
//                MyUtil.widthPixels(context)==1080 &&
//                MyUtil.heightPixels(context)==1920 &&
//                MyUtil.scaleDensity(context)==3){
//                mSpecialDevice=DEVICE_XIAOMI3;
//            }
//        }

        DisplayMetrics dm = MyUtil.getDisplayMetrics(context);
        mAppHeight = dm.heightPixels;
        mAppWidth = dm.widthPixels;
        if (myDailyBread == null) {
            myDailyBread = new MyDailyBread(context);
        }
        return myDailyBread;
    }

    private void setValidRange() {
        if (validFrDate == null || validToDate == null) {
            validFrDate = Calendar.getInstance();
            //validFrDate.set(mCurrentYear-1,  0, 1, 23, 59, 59); // 1-1
            validToDate = Calendar.getInstance();
            if (IS_CURRENT_YEAR_ONLY) {
                if (CMain.IS_2016_VERSION) {
                    validFrDate.set(2015, 0, 1, 0, 0, 0); // 1-1
                    validToDate.set(2016, 5, 30, 23, 59, 59); // Custom; May be by season
                } else {
                    validFrDate.set(mCurrentYear - 1, 0, 1, 0, 0, 0); // 1-1
                    validToDate.set(mCurrentYear, 11, 31, 23, 59, 59); // 2016-12-21
                }
            } else {
                if (CMain.IS_2016_VERSION) {
                    //validFrDate.set(2011, 9, 1, 0, 0, 0); // 1-1
                    validFrDate.set(2015, 0, 1, 0, 0, 0); // 1-1
                    validToDate.set(2016, 11, 31, 23, 59, 59); // Custom; May be by season
                } else {
                    validFrDate.set(mCurrentYear - 1, 0, 1, 0, 0, 0); // 1-1
                    validToDate.set(mCurrentYear, 11, 31, 23, 59, 59); // 2016-12-21
                }
            }
        }
        Calendar curCalendar = Calendar.getInstance();
        validFrDate.setTimeZone(curCalendar.getTimeZone());
        validToDate.setTimeZone(curCalendar.getTimeZone());
    }


    //    public static int date2index(Calendar startCal, Calendar endCal) {
//        // Create copies so we don't update the original calendars.
//
//        Calendar start = Calendar.getInstance();
//        start.setTimeZone(startCal.getTimeZone());
//        start.setTimeInMillis(startCal.getTimeInMillis());
//
//        Calendar end = Calendar.getInstance();
//        end.setTimeZone(endCal.getTimeZone());
//        end.setTimeInMillis(endCal.getTimeInMillis());
//
//        if (DEBUG) AxDebug.debug(TAG, getDayHourString(start));
//        if (DEBUG) AxDebug.debug(TAG, getDayHourString(end));
//        // Set the copies to be at midnight, but keep the day information.
//
////        start.set(Calendar.HOUR_OF_DAY, 0);
////        start.set(Calendar.MINUTE, 0);
////        start.set(Calendar.SECOND, 0);
////        start.set(Calendar.MILLISECOND, 0);
////
////        end.set(Calendar.HOUR_OF_DAY, 0);
////        end.set(Calendar.MINUTE, 0);
////        end.set(Calendar.SECOND, 0);
////        end.set(Calendar.MILLISECOND, 0);
//
//        // At this point, each calendar is set to midnight on
//        // their respective days. Now use TimeUnit.MILLISECONDS to
//        // compute the number of full days between the two of them.
////        return (int) TimeUnit.MILLISECONDS.toDays(
////                Math.abs(end.getTimeInMillis() - start.getTimeInMillis()));
//        float nbrOfDays=((end.getTimeInMillis() - start.getTimeInMillis()) / (24*60*60*1000));
//        int resultVal = (int) nbrOfDays;
//        if (DEBUG) AxDebug.debug(TAG,"nbrOfDays="+nbrOfDays+"("+resultVal);
//        return resultVal;
//    }
    public long getNbrOfValidDays(Context context) {
        setValidRange();
        if (allowBeyondRange) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(validToDate.getTimeInMillis());
            calendar.set(Calendar.YEAR, beyondToYear);
            return date2index(context, calendar) + 1;//Include last day
        } else {
            return date2index(context, validToDate) + 1;//Include last day
        }
    }

    public static Calendar index2date(Context context, int nbrOfDays) {
        //i = 1;
        Calendar startCal = (Calendar) MyDailyBread.getInstance(context).getValidFrDate().clone();
        // 2018.11.01 DC
//        startCal.set(Calendar.HOUR_OF_DAY, 12);
//        startCal.set(Calendar.MINUTE, 0);
//        startCal.set(Calendar.SECOND, 0);
//        startCal.set(Calendar.MILLISECOND, 0);
        startCal = getSignedAddInDays(startCal, nbrOfDays);
        String newCal = getDayHourString(startCal);
        if (DEBUG) AxDebug.debug(TAG, "CalDate: " + nbrOfDays + " -> " + newCal);
        int result = date2index(context, startCal);
        if (DEBUG) AxDebug.debug(TAG, "CalDate: " + getDayHourString(startCal) + " -> " + result);
        if (!allowBeyondRange && startCal.compareTo(MyDailyBread.getInstance(context).getValidToDate()) > 0) {
            startCal = (Calendar) MyDailyBread.getInstance(context).getValidToDate().clone();
        }
        return startCal;
    }

    static public Calendar getValidCalendar(Context context, Calendar calendar) {
        if (!MyDailyBread.allowBeyondRange && calendar.compareTo(MyDailyBread.getInstance(context).getValidToDate()) > 0) {
            calendar = (Calendar) MyDailyBread.getInstance(context).getValidToDate().clone();
        }
        return calendar;
    }

    public static int date2index(Context context, Calendar end) {
        // DC 202212
        return getDayDiff(MyDailyBread.getInstance(context).getValidFrDate().getTimeInMillis(), end.getTimeInMillis());
        //return calcDaysDiff(MyDailyBread.getInstance(context).getValidFrDate(),end);
    }

    // DC 202212
    static public int getDayDiff(long start, long end) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        long t = (23 - hour) * 3600000 + (59 - min) * 60000;
        t = start + t;
        int diff = 0;
        if (end > t) diff = (int) ((end - t) / TimeUnit.DAYS.toMillis(1)) + 1;
        return diff;
    }

    //    public static int calcDaysDiff(Calendar date1, Calendar date2) {
//        //checks if the start date is later then the end date - gives 0 if it is
//        if (DEBUG){
//            if (getDayHourString(date1).contentEquals(getDayHourString(date2))){
//                AxDebug.debug(TAG, "Equal");
//            }
//        }
//        boolean exchangeDate=false;
//        if (date1.get(Calendar.YEAR) >= date2.get(Calendar.YEAR)) {
//            if (date1.get(Calendar.DAY_OF_YEAR) >= date2.get(Calendar.DAY_OF_YEAR)) {
//                exchangeDate=true;
//            }
//        }
//        Calendar startCal = (Calendar) (exchangeDate?date2.clone():date1.clone());
//        startCal.set(Calendar.HOUR_OF_DAY, 12);
//        startCal.set(Calendar.MINUTE, 0);
//        startCal.set(Calendar.SECOND, 0);
//        startCal.set(Calendar.MILLISECOND, 0);
//
//        Calendar endCal = (Calendar) (exchangeDate?date1.clone():date2.clone());
//        endCal.set(Calendar.HOUR_OF_DAY, 12);
//        endCal.set(Calendar.MINUTE, 0);
//        endCal.set(Calendar.SECOND, 0);
//        endCal.set(Calendar.MILLISECOND, 0);
////        endCal.set(Calendar.HOUR_OF_DAY, 23);
////        endCal.set(Calendar.MINUTE, 59);
////        endCal.set(Calendar.SECOND, 59);
////        endCal.set(Calendar.MILLISECOND, 0);
//
////      endCal.add(Calendar.SECOND, 1);
//
//        Calendar dateCpy = (Calendar) startCal.clone();
////        Date d1 = startCal.getTime();
////        Date d2 = endCal.getTime();
//        //checks if there is a daylight saving change between the two dates
////        boolean isDate1Summer = TimeZone.getDefault().inDaylightTime(d1);
////        boolean isDate2Summer = TimeZone.getDefault().inDaylightTime(d2);
////        int offset = 0;
//        //check if there as been a change in winter/summer time and adds/reduces an hour
////        if (isDate1Summer && !isDate2Summer) {
////            offset = 1;
////        }
////        if (!isDate1Summer && isDate2Summer) {
////            offset = -1;
////        }
//
//        //int diffAux = calcDaysDiffAux(dateCpy, endCal);//Exclude last day
//        //int diffAux = (int) ((endCal.getTimeInMillis() - dateCpy.getTimeInMillis()) / (24*60*60*1000));
//
//        // DC 202212
//        //int diffAux = getUnsignedDiffInDays(endCal.getTime(),dateCpy.getTime());
//int diffAux = getD
//        int diffAux = getUnsignedDiffInDays(endCal.getTime(),dateCpy.getTime());
//
////        int fullDay = checkFullDay(dateCpy, endCal, offset);
//        //if (DEBUG) AxDebug.debug(TAG,"nbrOfDays Difference (Exclude last day)="+diffAux+"("+fullDay+")");
////        return diffAux+fullDay;
//        if (DEBUG) AxDebug.debug(TAG,"CalDate: "+getDayHourString(startCal)+" -> "+getDayHourString(endCal)+" has "+diffAux+" days(ex. last day)");
//        return diffAux;
//    }
    //http://stackoverflow.com/questions/3838527/android-java-date-difference-in-days
    private final static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
//    private static long getDateToLong(Date date) {
//        return Date.UTC(date.getYear(), date.getMonth(), date.getDate(), 12, 0, 0);
//    }


    public static void showOutOfBounds(Context context, Calendar calendar) {
        String strDate = "";
//        try {
//            strDate="("+getDayString(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))+")";
//        } catch (Exception ignored){
//
//        }
        Calendar toDate = MyDailyBread.getInstance(context).getValidToDate();
        if (calendar.getTimeInMillis() > toDate.getTimeInMillis()) {
            String str = toDate.get(Calendar.YEAR) + "-" + (toDate.get(Calendar.MONTH) + 1) + "-" + toDate.get(Calendar.DAY_OF_MONTH);
            str = strDate + context.getString(R.string.out_of_range_download, str);
            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.out_of_range, Toast.LENGTH_SHORT).show();
        }
    }

    public static Calendar getSignedAddInDays(Calendar startDate, int nbrOfDays) {
        long beginMS = startDate.getTimeInMillis();// getDateToLong(startDate.getTime()); // Time in UTC
        long endMS = beginMS + nbrOfDays * MILLISECS_PER_DAY;// Time in UTC
        Calendar newCal = Calendar.getInstance();
        newCal.setTimeZone(TimeZone.getTimeZone("UTC"));
        newCal.setTimeInMillis(endMS);
        newCal.setTimeZone(startDate.getTimeZone());
        return newCal;
    }
//    public static int getSignedDiffInDays(Date beginDate, Date endDate) {
//        long beginMS = getDateToLong(beginDate);
//        long endMS = getDateToLong(endDate);
//        long diff = (endMS - beginMS) / (MILLISECS_PER_DAY);
//        return (int)diff;
//    }
//    public static int getUnsignedDiffInDays(Date beginDate, Date endDate) {
//        return Math.abs(getSignedDiffInDays(beginDate, endDate));
//    }

    // check if there is a 24 hour diff between the 2 dates including the daylight saving offset
    public static int checkFullDay(Calendar day1, Calendar day2, int offset) {
        if (day1.get(Calendar.HOUR_OF_DAY) <= day2.get(Calendar.HOUR_OF_DAY) + offset) {
            return 0;
        }
        return -1;
    }

    // find the number of days between the 2 dates. check only the dates and not the hours
//    public static int calcDaysDiffAux(Calendar day1, Calendar day2) {
//        Calendar dayOne = (Calendar) day1.clone(),
//                 dayTwo = (Calendar) day2.clone();
//
//        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
//            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
//        } else {
//            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
//                //swap them
//                Calendar temp = dayOne;
//                dayOne = dayTwo;
//                dayTwo = temp;
//            }
//            int extraDays = 0;
//
//            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
//                dayOne.add(Calendar.YEAR, -1);
//                // getActualMaximum() important for leap years
//                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
//            }
//            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOne.get(Calendar.DAY_OF_YEAR);
//        }
//    }
    public Calendar getValidFrDate() {
        setValidRange();
        return validFrDate;
    }

    public Calendar getValidToDate() {
        setValidRange();
        return validToDate;
    }

    public MyDailyBread(Context context) {
        mContext = context;
        readFileFromAsset();
    }

    private void readFileFromAsset() {
         BufferedReader bReader = null;
         InputStreamReader iReader = null;
        String dummyLine = "";
        try {
            Calendar today = Calendar.getInstance();
            String lang = AxTools.getPrefStr(MyApp.PREF_APP_LANG, "");
            if (lang.equalsIgnoreCase(MyApp.PREF_APP_LANG_CN)) {
                iReader = new InputStreamReader(mContext.getAssets().open("dailyBread_sc.csv"), "UTF-8");
            } else {
                iReader = new InputStreamReader(mContext.getAssets().open("dailyBread.csv"), "UTF-8");
            }
            bReader = new BufferedReader(iReader);
            // do reading, usually loop until end of file reading
            String line = bReader.readLine();
            ContentValues cvGoldFrame = null;
            ContentValues cvGoldAlign = null;
            ContentValues cvGoldSize = null;
            ContentValues cvBigAlign = null;
            ContentValues cvBigSize = null;

            String maxGold_L_date = "";
            String maxGold_M_date = "";
            String maxGold_S_date = "";
            String maxHint_L_date = "";
            String maxHint_S_date = "";
            String minGold_L_date = "";
            String minGold_M_date = "";
            String minGold_S_date = "";
            String minHint_L_date = "";
            String minHint_S_date = "";
            String maxGold_L_str = "";
            String maxGold_M_str = "";
            String maxGold_S_str = "";
            String maxHint_L_str = "";
            String maxHint_S_str = "";
            int lastYear = 0;
            int lastMonth = 0;
            int lastDay = 0;
            int lunarDivider = 0;
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.HOUR_OF_DAY, 0);
            newDate.set(Calendar.MINUTE, 0);
            newDate.set(Calendar.SECOND, 0);
            // 1st Line Header
            if (line != null) {
                String[] titles = line.split(",");
                mValueList = new ArrayList<ContentValues>();
                mMap = new HashMap<String, Integer>();
                line = bReader.readLine();
                int counter = 0;
                if (IS_CHECK_FIELD_VALUES) {
                    cvGoldFrame = new ContentValues();
                    cvGoldAlign = new ContentValues();
                    cvGoldSize = new ContentValues();
                    cvBigAlign = new ContentValues();
                    cvBigSize = new ContentValues();
                }
                while (line != null) {
                    line = line.replace("\"", "");
                    dummyLine = line;
                    String[] fields = line.split(",");
                    // Check Valid Line
                    boolean validLine = true;
                    if (fields.length != titles.length) {
                        validLine = false;
                        MyUtil.logError("#", "NbrOfFields [,] Wrong:" + line);
                        for (int j = 0; j < fields.length; j++) {
                            MyUtil.logError("#", "Field[" + j + "]" + fields[j]);
                        }
                    }
                    ContentValues cv = null;
                    if (validLine) {
                        cv = new ContentValues();//ContentValues(titles.length)
                        // Assign to mValueList
                        for (int i = 0; i < titles.length; i++) {
                            cv.put(titles[i], fields[i]);
                        }
                        if (cv.getAsString(wSmallText).equals(".")) {
                            cv.put(wSmallText, "");
                        }
                        lastYear = cv.getAsInteger(wGYear);
                        lastMonth = cv.getAsInteger(wGMonth) - 1;
                        // Check Lunar Information is provided
                        lunarDivider = cv.getAsString(wGDay).indexOf("^");
                        if (lunarDivider == -1) {
                            lastDay = cv.getAsInteger(wGDay);
                        } else {
                            String pureLastDay = cv.getAsString(wGDay).substring(0, lunarDivider);
                            String[] lunars = cv.getAsString(wGDay).substring(lunarDivider).split("\\^", 8);
                            cv.put(wLunarYear, lunars[1]);
                            cv.put(wLunarAnimal, lunars[2]);
                            cv.put(wLunarMonth, lunars[3]);
                            cv.put(wLunarBigSmall, lunars[4]);
                            cv.put(wLunarDay, lunars[5]);
                            cv.put(wLunarSeason, lunars[6]);

                            cv.put(wGDay, pureLastDay);
                            lastDay = Integer.parseInt(pureLastDay);
                        }
                        if (IS_CURRENT_YEAR_ONLY) {
                            if (CMain.IS_2016_VERSION) {
                                if (lastYear < 2014) {
                                    validLine = false;
                                }
                            } else {
                                if (lastYear < mCurrentYear - 1) {
                                    validLine = false;
                                }
                            }
                        }
                    }
                    if (validLine) {
                        String strGoldText = cv.getAsString(wGoldText);
                        String strWisdomText = cv.getAsString(wBigText);
                        String strGoldVerse = cv.getAsString(wGoldVerse);
                        String strWisdomVerse = cv.getAsString(wSmallText);
                        if (IS_TEST_2016_STANDARD) {
                            if (today.get(Calendar.YEAR) == lastYear &&
                                    today.get(Calendar.MONTH) == lastMonth &&
                                    today.get(Calendar.DAY_OF_MONTH) == lastDay) {
                                // 21 chars
                                strGoldVerse = "?????????????????????2???3???";
                                // Gold Text 4 lines (21 chars)
                                strGoldText = "????????????????????????????????????????????????????????????.#" +
                                        "????????????????????????????????????????????????????????????.#" +
                                        "????????????????????????????????????????????????????????????.#" +
                                        "????????????????????????????????????????????????????????????.";
                                // Gold Text 2 lines (14 chars)
//                            strGoldText =   "??????????????????????????????????????????#" +
//                                            "??????????????????????????????????????????";
//                           3 lines (21 chars)
//                                    strGoldText = "????????????????????????????????????????????????????????????.#" +
//                                            "????????????????????????????????????????????????????????????.#" +
//                                            "????????????????????????????????????????????????????????????.";
//                           2 lines (13 chars)
//                                    strGoldText = "????????????????????????????????????.#" +
//                                                  "????????????????????????????????????.";
//                           1 Lines (4 chars)
//                                    strGoldText =   "????????????";

                                // Wisdom 4 lines (14 chars)
                                strWisdomText = "??????????????????????????????????????????#" +
                                        "??????????????????????????????????????????#" +
                                        "??????????????????????????????????????????#" +
                                        "??????????????????????????????????????????";
                                // Wisdom 4 lines (21 chars)
//                                strWisdomText = "????????????????????????????????????????????????????????????.#"+
//                                        "????????????????????????????????????????????????????????????.#"+
//                                        "????????????????????????????????????????????????????????????.#"+
//                                        "????????????????????????????????????????????????????????????.";
                                // Wisdom 2 lines (21 chars)
//                                strWisdomVerse = "???????????????????????????????????????????????????????????????."+
//                                        "???????????????????????????????????????????????????????????????.";
                                strWisdomVerse = "????????????????????????????????????????????????????????????.";
                                cv.put(wGoldText, strGoldText);
                                cv.put(wGoldVerse, strGoldVerse);
                                cv.put(wBigText, strWisdomText);
                                cv.put(wSmallText, strWisdomVerse);
                            }
                        }
                        // Now set contents
                        mValueList.add(mValueList.size(), cv);
                        final String mapDayString = getDayString(lastYear, lastMonth, lastDay);
                        mMap.put(mapDayString, counter);
                        counter++;
                        // Assign From/To Date
                        newDate.set(Calendar.YEAR, lastYear);
                        newDate.set(Calendar.MONTH, lastMonth);
                        newDate.set(Calendar.DAY_OF_MONTH, lastDay);
                        if (validFrDate == null) {
                            validFrDate = Calendar.getInstance();
                            validFrDate.set(Calendar.YEAR, lastYear);
                            validFrDate.set(Calendar.MONTH, lastMonth);
                            validFrDate.set(Calendar.DAY_OF_MONTH, lastDay);
                            validFrDate.set(Calendar.HOUR_OF_DAY, 0);//23
                            validFrDate.set(Calendar.MINUTE, 0);//59
                            validFrDate.set(Calendar.SECOND, 0);//59
                        }
                        if (newDate.compareTo(validFrDate) < 0) {
                            validFrDate = (Calendar) newDate.clone();
                            validFrDate.set(Calendar.HOUR_OF_DAY, 0);//23
                            validFrDate.set(Calendar.MINUTE, 0);//59
                            validFrDate.set(Calendar.SECOND, 0);//59
                        }
                        if (validToDate == null) {
                            validToDate = Calendar.getInstance();
                            validToDate.set(Calendar.YEAR, lastYear);
                            validToDate.set(Calendar.MONTH, lastMonth);
                            validToDate.set(Calendar.DAY_OF_MONTH, lastDay);
                            validToDate.set(Calendar.HOUR_OF_DAY, 23);
                            validToDate.set(Calendar.MINUTE, 59);
                            validToDate.set(Calendar.SECOND, 59);
                        }
                        if (newDate.compareTo(validToDate) > 0) {
                            validToDate = (Calendar) newDate.clone();
                            validToDate.set(Calendar.HOUR_OF_DAY, 23);
                            validToDate.set(Calendar.MINUTE, 59);
                            validToDate.set(Calendar.SECOND, 59);
                        }
                        // Check Value
                        if (IS_CHECK_VALID_VERSE) {
                            if (CMain.getEBCV(cv.getAsString(wGoldVerse)) == null) {
                                MyUtil.logError(TAG, "!!!!! " + newDate.get(Calendar.YEAR) + "-" + (newDate.get(Calendar.MONTH) + 1) + "-" + newDate.get(Calendar.DAY_OF_MONTH) + " !!!!");
                            }
                        }
                        if (IS_CHECK_FIELD_VALUES) {
                            // Assign Unique Value to Register
                            if (cvGoldFrame != null && !cvGoldFrame.containsKey(cv.getAsString(wGoldFrame))) {
                                cvGoldFrame.put(cv.getAsString(wGoldFrame), "");
                            }
                            if (cvGoldAlign != null && !cvGoldAlign.containsKey(cv.getAsString(wGoldAlign))) {
                                cvGoldAlign.put(cv.getAsString(wGoldAlign), "");
                            }
                            if (cvGoldSize != null && !cvGoldSize.containsKey(cv.getAsString(wGoldSize))) {
                                cvGoldSize.put(cv.getAsString(wGoldSize), "");
                            }
                            if (cvBigAlign != null && !cvBigAlign.containsKey(cv.getAsString(wBigAlign))) {
                                cvBigAlign.put(cv.getAsString(wBigAlign), "");
                            }
                            if (cvBigSize != null && !cvBigSize.containsKey(cv.getAsString(wBigSize))) {
                                cvBigSize.put(cv.getAsString(wBigSize), "");
                            }
                        }

                        String[] goldLines = strGoldText.split("#");
                        String[] hintLines;
                        if (strWisdomText.startsWith("#")) {
                            hintLines = strWisdomText.substring(1).split("#");
                        } else {
                            hintLines = strWisdomText.split("#");
                        }

                        if (IS_CHECK_IF_LESS_THAN_4_LINES || goldLines.length > 4) {
                            if (goldLines.length > 4) {
                                if (DEBUG)
                                    Log.e(TAG, "<TooBig> lines=" + goldLines.length + " chars=" + strGoldText.replace("#", "").length() + " at " + getDayString(lastYear, lastMonth, lastDay) + " " + strGoldText);
                            }
                            if (cv.getAsString(wGoldSize).equalsIgnoreCase("L")) {
                                mGold_L_NbrOfRecords[goldLines.length]++;
                                mGold_L_LastDate[goldLines.length] = getDayString(lastYear, lastMonth, lastDay);
                            } else if (cv.getAsString(wGoldSize).equalsIgnoreCase("M")) {
                                mGold_M_NbrOfRecords[goldLines.length]++;
                                mGold_M_LastDate[goldLines.length] = getDayString(lastYear, lastMonth, lastDay);
                            } else {
                                mGold_S_NbrOfRecords[goldLines.length]++;
                                mGold_S_LastDate[goldLines.length] = getDayString(lastYear, lastMonth, lastDay);
                            }
                            if (cv.getAsString(wBigSize).equalsIgnoreCase("L")) {
                                mHint_L_NbrOfRecords[hintLines.length]++;
                                mHint_L_LastDate[hintLines.length] = getDayString(lastYear, lastMonth, lastDay);
                            } else {
                                mHint_S_NbrOfRecords[hintLines.length]++;
                                mHint_S_LastDate[hintLines.length] = getDayString(lastYear, lastMonth, lastDay);
                            }
                        }

                        for (String goldLine : goldLines) {
                            if (IS_CHECK_FUTURE_CHARS_ONLY) {
                                if (newDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
                                    continue;
                                } else if (newDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                                    if (newDate.get(Calendar.MONTH) != Calendar.DECEMBER && newDate.get(Calendar.MONTH) != Calendar.NOVEMBER) {
                                        continue;
                                    }
                                }
                            }
                            if (cv.getAsString(wGoldSize).equalsIgnoreCase("L")) {
                                if (goldLine.length() > mMaxGold_L_characters) {
                                    maxGold_L_str = goldLine;
                                    maxGold_L_date = getDayString(lastYear, lastMonth, lastDay);
                                }
                                mMaxGold_L_characters = Math.max(mMaxGold_L_characters, goldLine.length());
                                if (goldLines.length == 1) {
                                    if (goldLine.length() < mMinGold_L_characters) {
                                        minGold_L_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMinGold_L_characters = Math.min(mMinGold_L_characters, goldLine.length());
                                }

                            } else if (cv.getAsString(wGoldSize).equalsIgnoreCase("M")) {
                                if (goldLine.length() > mMaxGold_M_characters) {
                                    maxGold_M_str = goldLine;
                                    maxGold_M_date = getDayString(lastYear, lastMonth, lastDay);
                                }
                                mMaxGold_M_characters = Math.max(mMaxGold_M_characters, goldLine.length());
                                if (goldLines.length == 1) {
                                    if (goldLine.length() < mMinGold_M_characters) {
                                        minGold_M_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMinGold_M_characters = Math.min(mMinGold_M_characters, goldLine.length());
                                }
                            } else {
                                if (goldLine.length() > mMaxGold_S_characters) {
                                    maxGold_S_str = goldLine;
                                    maxGold_S_date = getDayString(lastYear, lastMonth, lastDay);
                                }
                                mMaxGold_S_characters = Math.max(mMaxGold_S_characters, goldLine.length());
                                if (goldLines.length == 1) {
                                    if (goldLine.length() < mMinGold_S_characters) {
                                        minGold_S_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMinGold_S_characters = Math.min(mMinGold_S_characters, goldLine.length());
                                }
                            }
                        }

                        for (int i = 0; i < hintLines.length; i++) {
                            if (IS_CHECK_FUTURE_CHARS_ONLY) {
                                if (newDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
                                    continue;
                                } else if (newDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                                    if (newDate.get(Calendar.MONTH) != Calendar.DECEMBER && newDate.get(Calendar.MONTH) != Calendar.NOVEMBER) {
                                        continue;
                                    }
                                }
                            }
                            if (cv.getAsString(wBigSize).equalsIgnoreCase("L")) {
                                if (hintLines[i].indexOf("Mama") == -1) {//Special character in csv
                                    if (hintLines[i].length() > mMaxHint_L_characters) {
                                        maxHint_L_str = hintLines[i];
                                        maxHint_L_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMaxHint_L_characters = Math.max(mMaxHint_L_characters, hintLines[i].length());
                                    if (hintLines[i].length() < mMinHint_L_characters) {
                                        minHint_L_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMinHint_L_characters = Math.min(mMinHint_L_characters, hintLines[i].length());
                                }
                            } else {
                                if (hintLines[i].indexOf("Quot") == -1) {//Special character in csv
                                    if (hintLines[i].length() > mMaxHint_S_characters) {
                                        maxHint_S_str = hintLines[i];
                                        maxHint_S_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMaxHint_S_characters = Math.max(mMaxHint_S_characters, hintLines[i].length());
                                    if (hintLines[i].length() < mMinHint_S_characters) {
                                        minHint_S_date = getDayString(lastYear, lastMonth, lastDay);
                                    }
                                    mMinHint_S_characters = Math.min(mMinHint_S_characters, hintLines[i].length());
                                }
                            }
                        }
                    }
                    // Next Line
                    line = bReader.readLine();

                }
            }

            if (DEBUG) MyUtil.log(TAG, getDayString(validFrDate) + " " + getDayString(validToDate));
            //Toast.makeText(mContext, "?????????????????????:"+getDayString(validFrDate)+" ??? "+getDayString(validToDate), Toast.LENGTH_LONG).show();
            if (IS_CHECK_FIELD_VALUES) {
                printContentValues(wGoldFrame, cvGoldFrame);// L,S ... S for ??? ???L/???S
                printContentValues(wGoldAlign, cvGoldAlign);// L,C (Left,Center)
                printContentValues(wGoldSize, cvGoldSize);// L,M,S (Large, Middle, Small)
                printContentValues(wBigAlign, cvBigAlign);// L,S,C ... S for ???
                printContentValues(wBigSize, cvBigSize);// L,S (Large, Small)
            }
            if (DEBUG) {
                MyUtil.log(TAG, "Remember to clear space !" + (IS_CHECK_FUTURE_CHARS_ONLY ? "<Check Furture Only>" : "<Check All>"));
                MyUtil.log(TAG, "GoldSize L maxChar:" + mMaxGold_L_characters + " " + maxGold_L_date + " " + maxGold_L_str);
                MyUtil.log(TAG, "GoldSize M maxChar:" + mMaxGold_M_characters + " " + maxGold_M_date + " " + maxGold_M_str);
                MyUtil.log(TAG, "GoldSize S maxChar:" + mMaxGold_S_characters + " " + maxGold_S_date + " " + maxGold_S_str);
                MyUtil.log(TAG, "HintSize L maxChar:" + mMaxHint_L_characters + " " + maxHint_L_date + " " + maxHint_L_str);
                MyUtil.log(TAG, "HintSize S maxChar:" + mMaxHint_S_characters + " " + maxHint_S_date + " " + maxHint_S_str);
                MyUtil.log(TAG, "GoldSize L minChar:" + mMinGold_L_characters + " " + minGold_L_date);
                MyUtil.log(TAG, "GoldSize M minChar:" + mMinGold_M_characters + " " + minGold_M_date);
                MyUtil.log(TAG, "GoldSize S minChar:" + mMinGold_S_characters + " " + minGold_S_date);
                MyUtil.log(TAG, "HintSize L minChar:" + mMinHint_L_characters + " " + minHint_L_date);
                MyUtil.log(TAG, "HintSize S minChar:" + mMinHint_S_characters + " " + minHint_S_date);
                if (IS_CHECK_IF_LESS_THAN_4_LINES) {
                    for (int i = 1; i < mGold_L_NbrOfRecords.length; i++) {
                        if (i > 4) {
                            MyUtil.logError(TAG, "*** Below has " + i + " line(s) *** [Big size may overflow in small device");
                        } else {
                            MyUtil.logError(TAG, "*** Below has " + i + " line(s) ***");
                        }
                        MyUtil.log(TAG, "Gold L NbrOfRecords=" + mGold_L_NbrOfRecords[i] + " date=" + mGold_L_LastDate[i]);
                        MyUtil.log(TAG, "Gold M NbrOfRecords=" + mGold_M_NbrOfRecords[i] + " date=" + mGold_M_LastDate[i]);
                        MyUtil.log(TAG, "Gold S NbrOfRecords=" + mGold_S_NbrOfRecords[i] + " date=" + mGold_S_LastDate[i]);
                        MyUtil.log(TAG, "Hint L NbrOfRecords=" + mHint_L_NbrOfRecords[i] + " date=" + mHint_L_LastDate[i]);
                        MyUtil.log(TAG, "Hint S NbrOfRecords=" + mHint_S_NbrOfRecords[i] + " date=" + mHint_S_LastDate[i]);
                    }
                } else {
                    MyUtil.log(TAG, "*** Below has 5 line(s) *** [Big size may overflow in small device");
                    MyUtil.log(TAG, "Gold L NbrOfRecords=" + mGold_L_NbrOfRecords[4] + " date=" + mGold_L_LastDate[4]);
                    MyUtil.log(TAG, "Gold M NbrOfRecords=" + mGold_M_NbrOfRecords[4] + " date=" + mGold_M_LastDate[4]);
                    MyUtil.log(TAG, "Gold S NbrOfRecords=" + mGold_S_NbrOfRecords[4] + " date=" + mGold_S_LastDate[4]);
                    MyUtil.log(TAG, "Hint L NbrOfRecords=" + mHint_L_NbrOfRecords[4] + " date=" + mHint_L_LastDate[4]);
                    MyUtil.log(TAG, "Hint S NbrOfRecords=" + mHint_S_NbrOfRecords[4] + " date=" + mHint_S_LastDate[4]);
                }
            }

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

        } catch (Exception e1) {
            //log the exception
            Log.e(TAG, "Error:\n" + dummyLine);
            Log.e(TAG,"Action/DailyBread:"+ Objects.requireNonNull(e1.getMessage()));
        } finally {
            if (iReader !=null){
                try {
                    iReader.close();
                } catch (Exception e2) {
                    Log.e(TAG, "Action/iReader:"+ Objects.requireNonNull(e2.getMessage()));
                }
            }
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (Exception e2) {
                    Log.e(TAG, "Action/bReader:"+ Objects.requireNonNull(e2.getMessage()));
                }
            }
        }
    }

    static public String getDayString(int year, int month, int day) {
        return year + "-" + (month + 1) + "-" + day;
    }

    static public String getDayString(String year, String month, String day) {
        return Integer.valueOf(year) + "-" + (Integer.valueOf(month) + 1) + "-" + Integer.valueOf(day);
    }

    static public String getDayString(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    static public String getDayHourString(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
    }

    private void printContentValues(String str, ContentValues vals) {
        Set<Entry<String, Object>> s = vals.valueSet();
        Iterator<Entry<String, Object>> itr = s.iterator();
        MyUtil.log("#", "ContentValue[" + str + "]" + vals.size());
        while (itr.hasNext()) {
            Map.Entry<String, Object> me = (Map.Entry<String, Object>) itr.next();
            Object value = me.getValue();
            MyUtil.log(TAG, "Key[" + me.getKey().toString() + "] values[" + (String) (value == null ? null : value.toString()) + "]");
        }
    }

    public ContentValues getContentValues(int year, int month, int day) {
        int keyNbr = 0;
        try {
            keyNbr = mMap.get(getDayString(year, month, day));
            return mValueList.get(keyNbr);
        } catch (Exception e1) {
            boolean isError = false;
            if (MyDailyBread.allowBeyondRange && year >= beyondFrYear && year <= beyondToYear) {
                //
                //
//                Calendar lastDate = MyDailyBread.getInstance(mContext).getValidToDate();
//                Calendar getContentDate = (Calendar) lastDate.clone();
//                getContentDate.set(year, month, day);
//                if (getContentDate.compareTo(lastDate) > 0){
//                    MyDailyBread.showOutOfBounds(mContext,getContentDate);
//                }
                int baseYear = validFrDate.get(Calendar.YEAR);
                if ((year % 2) == 0) {
                    // number is even
                    year = baseYear;
                } else {
                    year = baseYear + 1;
                }
                boolean day29 = false;
                if (month == 1 && day == 29) {
                    day29 = true;
                }
                try {
                    if (day29) {
                        keyNbr = mMap.get(getDayString(year, 6, 15));
                    } else {
                        keyNbr = mMap.get(getDayString(year, month, day));
                    }
                    isError = false;
                } catch (Exception e2) {
                    MyUtil.logError(TAG, "Adjusted Value not find in array:" + year + "," + month + "," + day);
                    isError = true;
                }
            } else {
                MyUtil.logError(TAG, "Value not find in array:" + year + "," + month + "," + day);
                isError = true;
            }
            if (isError) {
                try {
                    Calendar calendar = MyDailyBread.getInstance(mContext).getValidToDate();
                    keyNbr = mMap.get(getDayString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
                    return mValueList.get(keyNbr);
                } catch (Exception e) {
                    MyUtil.logError(TAG, "Use last date value not find in array");
                    return null;
                }
            } else {
                return mValueList.get(keyNbr);
            }
        }
    }
//	public String getValues(int year, int month, int day, String keyWord){
//		int keyNbr = mMap.get(getDayString(year, month, day));
//		ContentValues cv = mValueList.get(keyNbr);
//		if (cv==null){
//			return "";
//		} else {
//			return cv.getAsString(keyWord);
//		}
//	}
}
