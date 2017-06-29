package com.hkbs.HKBS;

import android.content.ContentValues;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.hkbs.HKBS.arkCalendar.MyCalendarLunar;
import com.hkbs.HKBS.arkUtil.MyUtil;

import org.arkist.share.AxDebug;
import org.arkist.share.AxTools;

import java.util.Calendar;

/**
 * Created by dchow on 28/10/15.
 */
public class DailyFragment extends Fragment {
    final static private String TAG = DailyFragment.class.getSimpleName();
    final static private boolean DEBUG = true && CMain.DEBUG;
    final static private String CHI_MONTHS[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    final static private String STD_LAYOUT = "standard";
    final static private String SMALL_LAYOUT = "small";
    final static private String SW600_LAYOUT = "sw600dp";

    private ViewGroup mRootView;
    private int mThisPageYear;
    private int mThisPageZeroBasedMonth;//Zero_base
    private int mThisPageDay;
    private Calendar mCalendar;
    private MyCalendarLunar mLunar;

    private int mTextColor;
    private boolean mIsHoliday;
    private String mScrType;
    private TextView mScreenTypeView;
    private ContentValues mContentValues;
// Row 1
    private TextView mEngYear;
    private TextView mEngMonthName;
// Row 2
    private TextView mBigDay;
    private TextView mHoliday1View;
    private TextView mHoliday2View;
    private TextView mHolyDay1View;
    private TextView mHolyDay2View;
    private String mHolidayText;
    private String mHolyDayText;
// Row 3
    private TextView mChiLeftWeather;
    private TextView mChiLeftYear;
    private TextView mChiMonthName;//一月

    private TextView mWeekDay;
    private ImageView mWeekDayImage;

    private TextView mChiLunarYear;//甲午年
    private TextView mChiLunarMonth;//十二月大
    private TextView mChiLunarDay;//初一
// Row 4
    private ImageView mGoldFrame;
    private TextView mGoldTextView;
    private TextView mGoldVerseView;
    private int mMaxCharsPerGoldLine;
    private int mGoldTextFontSize;
    private int mGoldVerseFontSize;
// Row 5
    private ImageView mWisdomIcon;
    private TextView mWisdomTextView;
    private TextView mWisdomVerseView;
    private int mWisdomTextFontSize;
    private int mWisdomVerseFontSize;
//    
    private Paint textPaint = new TextPaint();
    private Rect textBounds = new Rect();
    private int statusBarHeight=0;
    static public DailyFragment getInstance(int year, int month, int day){
        DailyFragment dailyFragment = new DailyFragment();
        dailyFragment.mThisPageYear = year;
        dailyFragment.mThisPageZeroBasedMonth = month;
        dailyFragment.mThisPageDay = day;
        dailyFragment.mCalendar = Calendar.getInstance();
        // DC 2016.09.16
        dailyFragment.mCalendar.set(year,month,day);
        dailyFragment.mLunar = new MyCalendarLunar(dailyFragment.mCalendar,MyApp.mIsSimplifiedChinese);
        return dailyFragment;
    }
    protected void onRefreshSettings(int year, int month, int day){
        mThisPageYear = year;
        mThisPageZeroBasedMonth = month;
        mThisPageDay = day;
        mCalendar = Calendar.getInstance();
        // DC 2016.09.16
        mCalendar.set(year, month, day);
        mLunar = new MyCalendarLunar(mCalendar,true);
        onRefreshScreen();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.activity_page1, container, false);
        // Crash on 4.3.1
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2 ||
//                    Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//                if (DEBUG) AxDebug.debug(this+" sdk="+Build.VERSION.SDK_INT);
//                mRootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//            }
//        }
//        onRefreshScreen();
        return mRootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        onRefreshScreen();
    }
    public void onRefreshScreen() {
        if (DEBUG) AxDebug.debug(this);
//        if (getActivity()!=null){
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    onRefreshScreenOnUIThread();
//                }
//            });
//        }
//    }
//    synchronized private void onRefreshScreenOnUIThread(){
        if (mRootView==null) return;
        if (mThisPageYear==0) {
            AxTools.runLater(500, new Runnable() {
                @Override
                public void run() {
                    onRefreshScreen();
                }
            });
            return;
        }
        try {
            mContentValues = new ContentValues(CMain.mDailyBread.getContentValues(mThisPageYear, mThisPageZeroBasedMonth, mThisPageDay));// get ContentValues from dailyBread file
        } catch (Exception e){
            mRootView.setVisibility(View.INVISIBLE);
            AxTools.toast(AxTools.MsgType.ERROR, getContext(),null,"找資料時出現問題 ("+ mThisPageYear +","+ (mThisPageZeroBasedMonth+1) +","+ mThisPageDay +") "+e.getMessage());
            return;
        }
        if (mContentValues==null) {
            mRootView.setVisibility(View.INVISIBLE);
            AxTools.toast(AxTools.MsgType.ERROR,getContext(),null,"找不到指定日期內容 ("+ mThisPageYear +","+ (mThisPageZeroBasedMonth+1) +","+ mThisPageDay +")");
            return;
        }
        mScreenTypeView = (TextView) mRootView.findViewById(R.id.xmlPage1ScreenType);
        // Row 1
        mEngYear = (TextView) mRootView.findViewById(R.id.xmlPage1EngYear);
        mEngMonthName = (TextView) mRootView.findViewById(R.id.xmlPage1EngMonthName);
        // Row 2
        mBigDay = (TextView) mRootView.findViewById(R.id.xmlPage1Day);
        mHoliday1View = (TextView) mRootView.findViewById(R.id.xmlPage1Holiday1);
        mHoliday2View = (TextView) mRootView.findViewById(R.id.xmlPage1Holiday2);
        mHolyDay1View = (TextView) mRootView.findViewById(R.id.xmlPage1HolyDay1);
        mHolyDay2View = (TextView) mRootView.findViewById(R.id.xmlPage1HolyDay2);
        // Row 3
        mWeekDay = (TextView) mRootView.findViewById(R.id.xmlPage1WeekDay);
        mWeekDayImage = (ImageView) mRootView.findViewById(R.id.xmlPage1WeekImage);
        mChiLunarDay = (TextView) mRootView.findViewById(R.id.xmlPage1ChiLunarDay);//初一
        mChiLunarMonth = (TextView) mRootView.findViewById(R.id.xmlPage1ChiLunarMonth);//十二月大
        mChiLunarYear = (TextView) mRootView.findViewById(R.id.xmlPage1ChiLunarYear);//甲午年
        mChiMonthName = (TextView) mRootView.findViewById(R.id.xmlPage1ChiMonthName);//一月(UpperMiddle)
        mChiLeftWeather = (TextView) mRootView.findViewById(R.id.xmlPage1ChiLeftWeather);
        mChiLeftYear = (TextView) mRootView.findViewById(R.id.xmlPage1ChiLeftYear);
        // Row 4
        mGoldFrame = (ImageView) mRootView.findViewById(R.id.xmlPage1ImageFrame);
        mGoldTextView = (TextView) mRootView.findViewById(R.id.xmlPage1GoldText);
        mGoldVerseView = (TextView) mRootView.findViewById(R.id.xmlPage1GoldVerse);
        // Row 5
        mWisdomIcon = (ImageView) mRootView.findViewById(R.id.xmlPage1ImageIcon);        
        mWisdomTextView = (TextView) mRootView.findViewById(R.id.xmlPage1BigText);
        mWisdomVerseView = (TextView) mRootView.findViewById(R.id.xmlPage1BigHint);
        
        //mWisdomTextView.setText("Date="+mThisPageYear+" "+mThisPageZeroBasedMonth+" "+mThisPageDay);
        mScrType="";
        if (mScreenTypeView!=null){
            mScrType=mScreenTypeView.getTag().toString();
            CMain.mScreenType=mScrType;
            if (!DEBUG) mScreenTypeView.setText("");
        }
        mHolidayText = MyHoliday.getHolidayRemark(mCalendar.getTime());
        mHolidayText = mHolidayText.replace("*","");
        mHolyDayText = MyHoliday.getHolyDayText(mCalendar.getTime());

        mIsHoliday = ((!mHolidayText.equals("")) & !mHolidayText.startsWith("#")) || mCalendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY;
        if (mHolidayText.startsWith("#")) {
            mHolidayText = mHolidayText.substring(1);
        }
        mTextColor = getResources().getColor(mIsHoliday ? R.color.holiday : R.color.weekday);        

        setRow1YearAndMonth();
        setRow2Day();
        setRow3ChineseYearMonthAndWeekday();
        setRow4GoldText();
        setRow5Wisdom();
        mRootView.setVisibility(View.VISIBLE);
        AxDebug.info(this, "Refresh Completed ("+(mThisPageZeroBasedMonth+1)+"."+mThisPageDay+")");
    }
    private void setRow1YearAndMonth(){
        mEngYear.setText(String.valueOf(mThisPageYear));
        mEngYear.setTextColor(mTextColor);

        mEngMonthName.setText(MyUtil.sdfEngMMMM.format(mCalendar.getTime()));
        mEngMonthName.setTextColor(mTextColor);

        mChiMonthName.setText(CHI_MONTHS[mCalendar.get(Calendar.MONTH)] + "月");
        mChiMonthName.setTextColor(mTextColor);
    }
    private void setRow2Day(){
        mBigDay.setText(String.valueOf(mThisPageDay));
        mBigDay.setTextColor(mTextColor);
        if (CMain.IS_2016_VERSION) {
            LinearLayout.LayoutParams monthParams = (LinearLayout.LayoutParams) mChiMonthName.getLayoutParams();
            float heightOfTopLine = Math.max(mEngMonthName.getTextSize(), mChiMonthName.getTextSize() + monthParams.topMargin + monthParams.bottomMargin);
            float heightOfWeekDayLine = (mChiLunarMonth.getTextSize() * 2) + heightOfTopLine;
            float daySpaceInPixel = (getAppHeight() * 20 / 38);
            daySpaceInPixel = daySpaceInPixel - (heightOfWeekDayLine * 1.15f);
            float dayRatioOfScreen = daySpaceInPixel / MyDailyBread.mAppHeight;
            int dayFontSize = setViewFontBySize(null, mBigDay, dayRatioOfScreen, 1, 1, 0.85f, 2);
            mBigDay.setTextSize(TypedValue.COMPLEX_UNIT_PX, dayFontSize);
        }

        if (MyDailyBread.mAppWidth==320 && MyDailyBread.mAppHeight==480){
            mHoliday1View.setTextSize(TypedValue.COMPLEX_UNIT_PX,mHoliday1View.getTextSize() * 0.85f);
            mHoliday2View.setTextSize(TypedValue.COMPLEX_UNIT_PX,mHoliday2View.getTextSize() * 0.85f);
        }
        final int maxCharacters=7;
        if (mHolidayText.equals("")){
            mHoliday1View.setVisibility(View.GONE);
            mHoliday2View.setVisibility(View.GONE);
        } else {
            mHoliday1View.setVisibility(View.VISIBLE);
            // Split Empty String will cause 1st one be empty; So we remove 1st and add back
            String str [] = mHolidayText.substring(1).split("");
            str[0] = mHolidayText.substring(0,1);
            //ok now.
            int remarkLength = Math.min(maxCharacters * 2, str.length);
            int prefixlength = Math.min(maxCharacters, str.length);
            String holidayRemark="";
            for (int i=0;i<prefixlength;i++){
                holidayRemark+=str[i]+(i==(prefixlength-1)?"":"\n");
            }
            mHoliday1View.setLines(prefixlength);
            mHoliday1View.setText(holidayRemark);
            mHoliday1View.setTextColor(mTextColor);
            if (remarkLength>maxCharacters){
                mHoliday2View.setVisibility(View.VISIBLE);
                if (remarkLength==maxCharacters+1){// Just one more character; Move last one to next line
                    holidayRemark="";
                    for (int i=0;i<prefixlength-1;i++){
                        holidayRemark+=str[i]+(i==(prefixlength-2)?"":"\n");
                    }
                    mHoliday1View.setLines(prefixlength-1);
                    mHoliday1View.setText(holidayRemark);
                    holidayRemark="";
                    for (int i=maxCharacters-1;i<remarkLength;i++){
                        holidayRemark+=str[i]+(i==(remarkLength-1)?"":"\n");
                    }
                    mHoliday2View.setLines(2);
                } else {
                    holidayRemark="";
                    for (int i=maxCharacters;i<remarkLength;i++){
                        holidayRemark+=str[i]+(i==(remarkLength-1)?"":"\n");
                    }
                    mHoliday2View.setLines(remarkLength-prefixlength);
                }
                mHoliday2View.setText(holidayRemark);
                mHoliday2View.setTextColor(mTextColor);
            } else {
                mHoliday2View.setVisibility(View.GONE);
            }
        }
/***********************************************************************
 *  HOLY DAY
 ************************************************************************/
        if (MyDailyBread.mAppWidth==320 && MyDailyBread.mAppHeight==480){
            mHolyDay1View.setTextSize(TypedValue.COMPLEX_UNIT_PX,mHolyDay1View.getTextSize() * 0.85f);
            mHolyDay2View.setTextSize(TypedValue.COMPLEX_UNIT_PX,mHolyDay2View.getTextSize() * 0.85f);
        }
        mHolyDay1View.setTextColor(mTextColor);
        mHolyDay2View.setTextColor(mTextColor);
        if ((CMain.IS_2017_VERSION && Calendar.getInstance().get(Calendar.YEAR)>=2017) ||
            (MyUtil.getPrefInt(MyUtil.PREF_HOLY_DAY,0)<=0 || mHolyDayText.equals(""))){
            mHolyDay1View.setVisibility(View.GONE);
            mHolyDay2View.setVisibility(View.GONE);
        } else {
            String holyDayLines [] = DailyFragment.getHolyDay2Lines(mHolyDayText);
            mHolyDay1View.setText(holyDayLines[0]);
            mHolyDay1View.setVisibility(View.VISIBLE);
            if (holyDayLines[1].equalsIgnoreCase("")){
                mHolyDay2View.setVisibility(View.GONE);
            } else {
                mHolyDay2View.setVisibility(View.VISIBLE);
                mHolyDay2View.setText(holyDayLines[1]);
            }
        }
    }
    static public String [] getHolyDay2Lines(String holyDayText){
        final int maxHolyDaysChars=9;
        String holyDayLines [] = new String[]{"",""};
        int splitIndex=holyDayText.indexOf("#");
        if (splitIndex>=0){
            for (int i=0;i<splitIndex;i++){
                holyDayLines[0]=holyDayLines[0]+holyDayText.substring(i,i+1)+(i==splitIndex-1?"":"\n");
            }
            for (int i=splitIndex+1;i<holyDayText.length();i++){
                holyDayLines[1]=holyDayLines[1]+holyDayText.substring(i,i+1)+(i==holyDayText.length()-1?"":"\n");
            }
        } else {
            holyDayLines = new String[]{"",""};
            if (holyDayText.length()>maxHolyDaysChars){
                for (int i=0;i<maxHolyDaysChars;i++){
                    holyDayLines[0]=holyDayLines[0]+holyDayText.substring(i,i+1)+(i==maxHolyDaysChars-1?"":"\n");
                }
                for (int i=maxHolyDaysChars;i<holyDayText.length();i++){
                    holyDayLines[1]=holyDayLines[1]+holyDayText.substring(i,i+1)+(i==holyDayText.length()-1?"":"\n");
                }
            } else {
                for (int i=0;i<holyDayText.length();i++){
                    holyDayLines[0]=holyDayLines[0]+holyDayText.substring(i,i+1)+(i==holyDayText.length()-1?"":"\n");
                }
            }
        }
        return holyDayLines;
    }
    private void setRow3ChineseYearMonthAndWeekday(){
        //if (CMain.IS_2016_VERSION) {
        if (mThisPageYear >=2016) {
            mWeekDayImage.setImageResource(mIsHoliday ? R.drawable.red_weekday_2016 : R.drawable.green_weekday_2016);
        } else {
            mWeekDayImage.setImageResource(mIsHoliday ? R.drawable.red_weekday_2015 : R.drawable.green_weekday_2015);
        }
        mWeekDay.setTextColor(getResources().getColor(R.color.white));

        // Prepare data
        final Calendar monthEndDate = (Calendar) mCalendar.clone();
        int nbrOfDaysTo30 = 30-mLunar.getDay(); // Chinese Day
        monthEndDate.add(Calendar.DAY_OF_MONTH, nbrOfDaysTo30);
        final MyCalendarLunar monthEndLunar = new MyCalendarLunar(monthEndDate,MyApp.mIsSimplifiedChinese);
        boolean isBigMonth = (monthEndLunar.getDay()==30)?true:false;
        // Settting
        mChiLunarMonth.setText(mLunar.toChineseMM() + (isBigMonth ? "大" : "小"));
        mChiLunarMonth.setTextColor(mTextColor);

        int theDay = mCalendar.get(Calendar.DAY_OF_WEEK);
        switch (theDay){
            case Calendar.SUNDAY: mWeekDay.setText("星期日 SUN"); break;
            case Calendar.MONDAY: mWeekDay.setText("星期一 MON"); break;
            case Calendar.TUESDAY: mWeekDay.setText("星期二 TUE"); break;
            case Calendar.WEDNESDAY: mWeekDay.setText("星期三 WED"); break;
            case Calendar.THURSDAY: mWeekDay.setText("星期四 THU"); break;
            case Calendar.FRIDAY: mWeekDay.setText("星期五 FRI"); break;
            case Calendar.SATURDAY: mWeekDay.setText("星期六 SAT"); break;
        }

        mChiLunarDay.setText(mLunar.toChineseDD() + "日");
        mChiLunarDay.setTextColor(mTextColor);

        mChiLunarYear.setText(mLunar.toChineseYY() + "年");
        mChiLunarYear.setTextColor(mTextColor);

        final String solarTerm=MyCalendarLunar.solar.getSolarTerm(mCalendar);

        mChiLeftYear.setText(mLunar.toChineseYY()+"年");
        mChiLeftYear.setTextColor(mTextColor);

        mChiLeftWeather.setText(solarTerm);
        mChiLeftWeather.setTextColor(mTextColor);

        if (solarTerm.equals("")){
            mChiLeftWeather.setVisibility(View.GONE);
            mChiLeftYear.setVisibility(View.GONE);
            mChiLunarYear.setVisibility(View.VISIBLE);
        } else {
            mChiLeftWeather.setVisibility(View.VISIBLE);
            mChiLeftYear.setVisibility(View.VISIBLE);
            mChiLunarYear.setVisibility(View.GONE);
        }
        
//        final ImageView pageImageFrameUpper = (ImageView) findViewById(getID(nbr, "ImageFrameUpper"));
//        final ImageView pageImageFrameLower = (ImageView) findViewById(getID(nbr, "ImageFrameLower"));

    }
    private void setRow4GoldText(){
        /***************************************************************
         GOLD FRAME
         ***************************************************************/

        //if (CMain.IS_2016_VERSION){
        if (mThisPageYear >=2016) {
            mGoldFrame.setImageDrawable(getResources().getDrawable(mIsHoliday?R.drawable.red_frame_2016:R.drawable.green_frame_2016));
            mGoldFrame.setVisibility(View.VISIBLE);
        } else {
            ImageView upperFrame = (ImageView) mRootView.findViewById(R.id.xmlPage1ImageFrameUpper);
            ImageView lowerFrame = (ImageView) mRootView.findViewById(R.id.xmlPage1ImageFrameLower);
            mGoldFrame.setImageDrawable(getResources().getDrawable(mIsHoliday?R.drawable.red_frame_2015:R.drawable.green_frame_2015));

            mGoldFrame.setVisibility(View.GONE);
            upperFrame.setImageDrawable(getResources().getDrawable(mIsHoliday ? R.drawable.red_frame_2015_upper : R.drawable.green_frame_2015_upper));
            lowerFrame.setImageDrawable(getResources().getDrawable(mIsHoliday ? R.drawable.red_frame_2015_upper : R.drawable.green_frame_2015_upper));
            upperFrame.setVisibility(View.VISIBLE);
            lowerFrame.setVisibility(View.VISIBLE);
        }

        /***************************************************************
            GOLD TEXT
         ***************************************************************/

        Calendar calendar = Calendar.getInstance();
        if (CMain.IS_2016_VERSION) {
            if (calendar.get(Calendar.YEAR) == mThisPageYear &&
                    calendar.get(Calendar.MONTH) == mThisPageZeroBasedMonth &&
                    calendar.get(Calendar.DAY_OF_MONTH) == mThisPageDay) {
                YoYo.with(Techniques.Bounce).duration(1000).playOn(mGoldTextView);
            }
        }

        String goldText = mContentValues.getAsString(MyDailyBread.wGoldText);
        final String goldLines [] = goldText.split("#");
        mMaxCharsPerGoldLine =0;
        for (int i=0;i<goldLines.length;i++){
            mMaxCharsPerGoldLine =Math.max(mMaxCharsPerGoldLine, goldLines[i].length());
        }
        // 2015.11.06 Min ; At least <X> characters; Make it not too large;
        // 2015.11.10 Min (12)-Max(21);
        mMaxCharsPerGoldLine = Math.max(12,mMaxCharsPerGoldLine);
        mGoldTextView.setTextColor(mTextColor);
        int goldFontSize;
        if (MyDailyBread.mCurrentYear>=2015){
            if (CMain.IS_2016_VERSION){
//                goldFontSize = getFontSizeByMaxCharacters(pageGoldText, 22);//Gold Text not more than 20; 26 should be small enough
//                goldTextFontSize=getFontSizeUponSize(pageGoldText, (float) 11 / 38, theText, 3, (float) 0.5);
//                pageGoldText.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldTextFontSize);
            } else {
                // 2015.09.16 To protect small device overflow, we don't allow 4 lines
                if (mMaxCharsPerGoldLine >= 22 && goldLines.length == 3) {//Relocate characters automatically (But, should be smaller size to occupy)
                    goldText = goldText.replace("#", "");
                    mMaxCharsPerGoldLine = (int) Math.ceil(goldText.length() / 4);
                    goldText = goldText.substring(0, mMaxCharsPerGoldLine) + "#" +
                            goldText.substring(mMaxCharsPerGoldLine, mMaxCharsPerGoldLine + mMaxCharsPerGoldLine) + "#" +
                            goldText.substring(mMaxCharsPerGoldLine + mMaxCharsPerGoldLine, mMaxCharsPerGoldLine + mMaxCharsPerGoldLine + mMaxCharsPerGoldLine) + "#" +
                            goldText.substring(mMaxCharsPerGoldLine + mMaxCharsPerGoldLine + mMaxCharsPerGoldLine);
                    if (DEBUG) {
                        Log.e(TAG, "OldText=" + mContentValues.getAsString(MyDailyBread.wGoldText));
                        Log.e(TAG, "NewText=" + goldText);
                    }
                    if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
                        goldFontSize = getFontSizeByText(mGoldTextView, goldText) - AxTools.dp2px(2);
                    } else {
                        goldFontSize = getFontSizeByText(mGoldTextView, goldText);
                    }
                } else {
                    goldFontSize = getFontSizeByText(mGoldTextView, goldText);
                }
                mGoldTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,goldFontSize);
            }
        } else {
            goldFontSize = getGoldFontSize(mGoldTextView, mContentValues.getAsString(MyDailyBread.wGoldSize));
            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)){
                mGoldTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize-AxTools.dp2px(3));
            } else {
                mGoldTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,goldFontSize);
            }
        }
        mGoldTextView.setText(goldText.replace("#", "\n"));
        if (CMain.IS_2016_VERSION){
            //if (MyDailyBread.mSpecialDevice==MyDailyBread.DEVICE_XIAOMI3) {
//            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
//                if (goldLines.length >= 4) {
//                    //mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 4, goldLines.length, 0.9f, mMaxCharsPerGoldLine);
//                    RelativeLayout.LayoutParams goldTextLP = (RelativeLayout.LayoutParams) mGoldTextView.getLayoutParams();
//                    goldTextLP.setMargins(goldTextLP.leftMargin, goldTextLP.topMargin - AxTools.dp2px(6), goldTextLP.rightMargin, goldTextLP.bottomMargin);
//                    mGoldTextView.setLayoutParams(goldTextLP);
//                }
//            }
                //(3 lines for text; 1 for verse;1 for top & bottom i.e. 3/5 = around 0.6)
//            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)){//Higher ratio to small screen
//                mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 3, goldLines.length, 0.65f, mMaxCharsPerGoldLine);
//            } else {
//                mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 3, goldLines.length, 0.6f, mMaxCharsPerGoldLine);
//            }
                //(4 lines for text; 1 for verse;1 for top & bottom i.e. 4/6 = around 0.66)
                if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {//Higher ratio to small screen
                    if (goldLines.length>=4) {
                        mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 4, goldLines.length, 0.8f, mMaxCharsPerGoldLine);
                    } else {
                        mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 4, goldLines.length, 0.7f, mMaxCharsPerGoldLine);
                    }
                } else {
                    mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 4, goldLines.length, 0.66f, mMaxCharsPerGoldLine);
                }

            mGoldTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mGoldTextFontSize);
        } else {
            mGoldTextFontSize = (int) mGoldTextView.getTextSize();
        }

        String goldAlign = mContentValues.getAsString(MyDailyBread.wGoldAlign);
        if (mThisPageYear <=2012 || mThisPageYear >=2016){
            goldAlign="C";
        }
        if (goldAlign.equalsIgnoreCase("L")){
            mGoldTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (goldAlign.equalsIgnoreCase("C")){
            mGoldTextView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        } else {
            mGoldTextView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        }

        /****************************************************************************************
         *  GOLD VERSE TEXT 金句經文出處
         ************************************************************************************/

        mGoldVerseView.setText(mContentValues.getAsString(MyDailyBread.wGoldVerse) + (mThisPageYear >= 2016 ? "；和合本" : "；和合本修訂版"));
        mGoldVerseView.setTextColor(mTextColor);
        // From HKBS, size change to less than Gold
        if (CMain.IS_2016_VERSION) {
            mGoldVerseFontSize=getFontSizeByMaxCharacters(mGoldTextView, 22);// Max. 22 per line; 4 lines
//            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
//                mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(mGoldVerseFontSize,(int)(mGoldTextFontSize*0.7)));
//            } else {
                mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(mGoldVerseFontSize, (int) (mGoldTextFontSize * 0.8)));
//            }
        } else {
            if (MyDailyBread.mCurrentYear >= 2015 & mMaxCharsPerGoldLine > 20) {
                // If size is too small, just a little bit different
                mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize - AxTools.dp2px(1));
            } else {
                if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
                    mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize - AxTools.dp2px(6));
                } else if (mScrType.equalsIgnoreCase(SW600_LAYOUT)) {
                    int suggestSize = (int) mChiLunarMonth.getTextSize();
                    suggestSize = Math.min(suggestSize, (int) (goldFontSize * 0.8)); // Use dp2px too less
                    mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, suggestSize);
                } else {
                    mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize - AxTools.dp2px(6));
                }
            }
            mGoldVerseFontSize=(int) mGoldVerseView.getTextSize();
        }
        if (mThisPageYear >=2010){
            mGoldVerseView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        }
        if (mThisPageYear >=2015){
            RelativeLayout.LayoutParams goldVerseLP = (RelativeLayout.LayoutParams) mGoldVerseView.getLayoutParams();
            if (mScrType.equalsIgnoreCase(STD_LAYOUT)) {
                if (CMain.IS_2016_VERSION) {
                    if (mThisPageYear >=2016) {
                        goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, AxTools.dp2px(8));
                    } else {
                        goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, AxTools.dp2px(16));
                    }
                } else {
                    goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, AxTools.dp2px(22));
                }
                mGoldVerseView.setLayoutParams(goldVerseLP);
            } else {
                if (CMain.IS_2016_VERSION && mThisPageYear >=2016) {
                    goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, AxTools.dp2px(8));
                    mGoldVerseView.setLayoutParams(goldVerseLP);
                }
            }

        }
    }
    private void setRow5Wisdom(){
        /****************************************************************************************
         *  WISDOM FRAME
         ************************************************************************************/


        //if (CMain.IS_2016_VERSION){
        if (mThisPageYear >=2016) {
            mWisdomIcon.setImageDrawable(getResources().getDrawable(mIsHoliday ? R.drawable.red_icon_2016 : R.drawable.green_icon_2016));
        } else {
            mWisdomIcon.setScaleType(ImageView.ScaleType.FIT_XY);
            if (mThisPageYear >= 2016) {
                mWisdomIcon.setImageDrawable(null);
            } else {
                mWisdomIcon.setImageDrawable(getResources().getDrawable(mIsHoliday ? R.drawable.red_icon_2015 : R.drawable.green_icon_2015));
            }
        }

        /****************************************************************************************
         *  WISDOM TEXT
         ************************************************************************************/

        mWisdomTextView.setTextColor(mTextColor);

        // Any Heading ":"
        String colonText = ":"; // English Style colon
        String wisdomText = mContentValues.getAsString(MyDailyBread.wBigText);
        int colonPos=-1;
        if (wisdomText.startsWith("#")) {
            wisdomText = wisdomText.substring(1);
        } else {
            wisdomText = wisdomText.replaceAll("：",":");
            colonPos = wisdomText.indexOf(":");
            int lineBreakPos = wisdomText.indexOf("#");
            if (lineBreakPos!=-1 && colonPos!=-1 && lineBreakPos<colonPos){
                colonPos = -1;
            }
        }
        String wisdomLines [] = wisdomText.split("#");
        int maxWisdomChars=0;
        for (int i=0;i<wisdomLines.length;i++){
            maxWisdomChars=Math.max(maxWisdomChars,wisdomLines[i].length());
        }
        maxWisdomChars=Math.max(16,maxWisdomChars);
        if (CMain.IS_2016_VERSION){// One Line change 2 or 3 lines upon length
            if (wisdomLines.length==1){
                if (wisdomLines [0].length()>=15) { // Change only for >15 characters
                    if (wisdomLines[0].length() <= 20) {
                        wisdomText = wisdomText.substring(0, 10) + "#" + wisdomText.substring(10);
                    } else if (wisdomLines[0].length() < 30) {
                        wisdomText = wisdomText.substring(0, 10) + "#" + wisdomText.substring(10,20)+"#"+wisdomText.substring(20);
                    }
                }
            }
            //wisdomTextSize=getFontSizeUponSize(pageWisdomText, (float) 7 / 38, wisdomText, 4,(float) 0.8);
        }
        if (colonPos>=0){
            wisdomText= "<small>"+wisdomText.substring(0, colonPos+1)+"</small>"+wisdomText.substring(colonPos+1).replace("#", "<br>");
            wisdomText=wisdomText+"<br>";
            mWisdomTextView.setText(Html.fromHtml(wisdomText));
        } else {
            mWisdomTextView.setText(wisdomText.replace("#", "\n"));
        }

        if (CMain.IS_2016_VERSION) {
            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)){// Provide more space ...
                float textRatio;
                if (TextUtils.isEmpty(mContentValues.getAsString(MyDailyBread.wSmallText))){
                    textRatio=0.95f;
                } else {
                    textRatio=0.8f;
                }
                mWisdomTextFontSize = setViewFontBySize(wisdomLines, mWisdomTextView, (float) 7 / 38, 4, wisdomLines.length, textRatio, maxWisdomChars);
                mWisdomTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(mWisdomTextFontSize,(int)(mGoldTextFontSize*0.95f)));
            } else {
                // 5 lines for all text lines; Space for Wisdom text is 4/5 = 0.8; Give some space for bottom margin
//                if (TextUtils.isEmpty(mContentValues.getAsString(MyDailyBread.wSmallText))) {
//                    mWisdomTextFontSize = setViewFontBySize(wisdomLines, mWisdomTextView, (float) 7 / 38, 4, wisdomLines.length, 0.8f, maxWisdomChars);
//                } else {
                    mWisdomTextFontSize = setViewFontBySize(wisdomLines, mWisdomTextView, (float) 7 / 38, 4, wisdomLines.length, 0.75f, maxWisdomChars);
//                }
                if (mMaxCharsPerGoldLine > 15){// Too small, nearly same size is ok
                    mWisdomTextFontSize=Math.min(mWisdomTextFontSize,(int) Math.floor(mGoldTextFontSize*0.95f));
                    mWisdomTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mWisdomTextFontSize);
                } else {
                    mWisdomTextFontSize=Math.min(mWisdomTextFontSize, (int) Math.floor (mGoldTextFontSize * 0.8f));
                    mWisdomTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mWisdomTextFontSize);
                }
            }
        } else {
            if (MyDailyBread.mCurrentYear >= 2015 & mMaxCharsPerGoldLine > 20) {
                // If size is too small, just a little bit different
                if (mScrType.equalsIgnoreCase(SW600_LAYOUT) || mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
                    mWisdomTextFontSize = (int) mChiLunarMonth.getTextSize();
                } else {
                    mWisdomTextFontSize = (int) (mGoldTextFontSize - AxTools.dp2px(2));
                }
            } else {
                if (mScrType.equalsIgnoreCase(SW600_LAYOUT) || mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
                    mWisdomTextFontSize = (int) mChiLunarMonth.getTextSize();
                } else {
                    mWisdomTextFontSize = (int) (mGoldTextFontSize - AxTools.dp2px(4));
                }
            }
            mWisdomTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mWisdomTextFontSize);
        }
//        RelativeLayout.LayoutParams bigTextLP = (RelativeLayout.LayoutParams) mWisdomTextView.getLayoutParams();
        if (mThisPageYear >=2016) {
//            if (wisdomLines.length>=4){
//                mWisdomTextView.setGravity(Gravity.LEFT | Gravity.TOP);
//            } else {
                mWisdomTextView.setGravity(Gravity.LEFT | Gravity.TOP);
//            }
            //mWisdomTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//            int margin=Math.min(bigTextLP.leftMargin,bigTextLP.rightMargin);
//            bigTextLP.setMargins(margin,bigTextLP.topMargin,margin,bigTextLP.bottomMargin);
//            mWisdomTextView.setLayoutParams(bigTextLP);
        } else {
            mWisdomTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
/***********************************************************************************
 * 最後一行字 : pageHintText (SIZE IS STANDARD)
 ************************************************************************************/
        String wisdomVerseText=mContentValues.getAsString(MyDailyBread.wSmallText);
        if (TextUtils.isEmpty(wisdomVerseText)){
            mWisdomVerseView.setVisibility(View.GONE);
        } else {
            mWisdomVerseView.setVisibility(View.VISIBLE);
            String wisdomVerseLines [] = wisdomVerseText.split("#");
            int maxWisdomVerseChars=0;
            for (int i=0;i<wisdomVerseLines.length;i++){
                maxWisdomVerseChars=Math.max(maxWisdomVerseChars,wisdomVerseLines[i].length());
            }
            wisdomVerseText=wisdomVerseText.replace("#","\n");
            mWisdomVerseView.setText(wisdomVerseText);
            mWisdomVerseView.setTextColor(mTextColor);
            float referenceSize=0;// = getBigFontSize(pageWisdomText,"S");
            if (CMain.IS_2016_VERSION) {
                referenceSize = mWisdomTextFontSize;//mWisdomVerseView.getTextSize();
            } else {
                referenceSize = mWisdomTextView.getTextSize();
            }
            //mWisdomVerseFontSize = (int) (referenceSize - AxTools.dp2px(2));
            mWisdomVerseFontSize = (int) (referenceSize*0.8f);
            int wisdomVerseWidth=getDesiredWidth(mWisdomVerseView);
            mWisdomVerseFontSize = Math.min(mWisdomVerseFontSize, (int) Math.floor(wisdomVerseWidth / maxWisdomVerseChars));
            mWisdomVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mWisdomVerseFontSize);//DC: 2013.12.12
            if (DEBUG) {
                Log.w(TAG, "Device="+getString(R.string.widget_deviceType)+" screen=" + mScrType + " WisdomFontSize=" + mWisdomTextFontSize + " v="+mWisdomVerseFontSize + " height="+mWisdomTextView.getHeight()+" "+ mThisPageYear +"-"+(mThisPageZeroBasedMonth +1)+"-"+ mThisPageDay);
            }
        }
    }
    
    private int getAppHeight(){
        if (statusBarHeight==0) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }
        return MyUtil.heightPixels(getContext()) - statusBarHeight;
    }
    /*
    * @param fullScreenRatio  The ratio that textView occupy on screen
    * @param textRatio  The ratio that text occupy on textView
     */
    public int setViewFontBySize(String lines[], TextView textView, float fullScreenRatio, int maxLines, int nbrOfLines, float textRatio, int maxChars){
        int maxAllLinesHeight = (int) (getAppHeight() * fullScreenRatio * textRatio);
        // Either 3 lines (Gold) or 4 lines (Wisdom)
        if (maxLines==3) {
            switch (nbrOfLines) {
                case 1:
                    maxAllLinesHeight = maxAllLinesHeight * 2 / maxLines;
                    break;
                case 2:
                    if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {//Give more space since screen is small
                        maxAllLinesHeight = (int) (maxAllLinesHeight * maxLines / maxLines);
                    } else {
                        maxAllLinesHeight = (int) (maxAllLinesHeight * 2.5 / maxLines);
                    }
                    break;
                case 3:
                    maxAllLinesHeight = maxAllLinesHeight * 3 / maxLines;
                    break;
                default:
                    //maxHeight = maxHeight * maxLines / maxLines;
                    break;
            }
        } else if (maxLines==4){//Wisdom 4 Lines
            switch (nbrOfLines) {
                case 1:
                    maxAllLinesHeight = maxAllLinesHeight * 2 / maxLines; // Display One Row with Two row space
                    break;
                case 2:
                    maxAllLinesHeight = maxAllLinesHeight * 3  / maxLines; // Display Two Row with Three row space
                    break;
                case 3:
                    if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {//Give more space since screen is small
                        maxAllLinesHeight = (int) (maxAllLinesHeight * 4 / maxLines);
                    } else {
                        maxAllLinesHeight = (int) (maxAllLinesHeight * 3.5 / maxLines);
                    }
                    break;
                case 4:
                    maxAllLinesHeight = (int) (maxAllLinesHeight * 4  / maxLines);
                    break;
                default:
                    break;
            }
        }
        int maxWidth = getDesiredWidth(textView);
        //int textSize = nbrOfLines==1?(int)(maxWidth/2):200;// inital text size
        //int textSize = nbrOfLines==1?maxHeight:200;
        int textSize = (int) (maxAllLinesHeight / nbrOfLines);// First test figures
        String text = textView.getText().toString();
        textView.setLineSpacing(0,1.0f);
        textPaint=textView.getPaint();
        boolean isDigit=TextUtils.isDigitsOnly(text);
        while (getHeightOfMultiLineText(lines, textSize, maxWidth, nbrOfLines, isDigit) >= maxAllLinesHeight) {
            textSize--;
        }
        if (DEBUG) {
            Log.d(TAG, "Before fontSize=" + maxAllLinesHeight + " after=" + textSize + " lines=" + nbrOfLines + " width=" + maxWidth + " maxChars=" + maxChars + " maxAllLinesHeight=" + maxAllLinesHeight + " text=" + text);
        }
        if (textSize*maxChars > maxWidth ) {
            int sizeByWidth = (int) (Math.floor(maxWidth / maxChars));
            int sizeByHeight = (int) Math.floor(Math.min(textSize, maxAllLinesHeight / nbrOfLines));
            if (DEBUG) {
                Log.e(TAG, "Too large use width/maxChars=" + textSize + " byWidth=" +sizeByWidth+" byHeight"+sizeByHeight);
            }
            textSize = Math.min(sizeByHeight, sizeByWidth);
        }
        if (!(mScrType.equalsIgnoreCase(SMALL_LAYOUT) && maxLines==4)) {//Screen so small, not reduct siz
            textSize--;
        }
        return textSize;
    }
    private int getHeightOfMultiLineText(String lines [], int textSize, int maxWidth, int nbrOfLines, boolean isDigit) {
        textPaint.setTextSize(textSize);
//        int index = 0;
//        int lineCount = 0;
//        while (index < text.length()) {
//            index += textPaint.breakText(text, index, text.length(), true, maxWidth, null);
//            lineCount++;
//        }
        int lineCount=nbrOfLines+1;
//      int nbrOfCharText=Math.min(2, text.length());
//      textPaint.getTextBounds(text.substring(0,nbrOfCharText), 0, nbrOfCharText, textBounds);
        int textBoundsHeight=0;
        if (lines==null || isDigit){
            textPaint.getTextBounds("Yy", 0, 2, textBounds);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            return (int) (textBounds.height()-fontMetrics.descent);
        } else {
            double lineSpacing;
            textPaint.getTextBounds("測試", 0, 2, textBounds);
            textBoundsHeight = textBounds.height() * nbrOfLines;
//            Log.d(TAG,"textBoundsHeight="+textBoundsHeight+" fontSpacing="+textPaint.getFontSpacing());
//            lineSpacing = Math.max(0, ((lineCount - 1) * textPaint.getFontSpacing()));
//            return (int) Math.floor(lineSpacing + textBoundsHeight);
            // 2015.11.11
            // Already use setLineSacing(0,1.1f) before;
            // Another 0.25 is from http://stackoverflow.com/questions/16082359/how-to-auto-adjust-text-size-on-a-multi-line-textview-according-to-the-view-max
            return (int)(textBoundsHeight * 1.25f );
        }
    }
    private int getFontSizeByText(TextView textView, String str){
		String textLines [] = str.split("#");
		int maxChars=0;
		for (int i=0;i<textLines.length;i++){
			maxChars = Math.max(maxChars, textLines[i].length());
		}
//            Rect bounds = new Rect();
//            String text = new String(new char[maxChars]).replace("\0", "M");
//            Paint paint = textView.getPaint();
//            paint.getTextBounds(text, 0, text.length(), bounds);
//            int textHeight = bounds.bottom + bounds.height();

        // Control characters not too big; Value bigger Letter Smaller
        int fontSize;
            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
                maxChars = maxChars < 16 ? 16 : maxChars;
            } else if (mScrType.equalsIgnoreCase(SW600_LAYOUT)) {
                maxChars = maxChars < 17 ? 17 : maxChars;// 17 change to 19 since 小米Note cannot display
            } else {
                maxChars = maxChars < 18 ? 18 : maxChars;
            }
            fontSize = getFontSizeByMaxCharacters(textView, maxChars);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		return fontSize;
	}
private int getFontSizeByMaxCharacters(TextView textView, int maxCharacters){
    RelativeLayout.LayoutParams lpGold = (RelativeLayout.LayoutParams) textView.getLayoutParams();
    int fontSize = (int) Math.floor((MyDailyBread.mAppWidth -
            AxTools.dp2px(mRootView.getPaddingLeft()) - AxTools.dp2px(mRootView.getPaddingRight()) -
            AxTools.dp2px(lpGold.leftMargin) - AxTools.dp2px(lpGold.rightMargin) -
            AxTools.dp2px(textView.getPaddingLeft()) - AxTools.dp2px(textView.getPaddingRight()))
            /
            maxCharacters);
    // Only Note has problem
    if (android.os.Build.MODEL.equalsIgnoreCase("MID") && MyUtil.heightPixels(getContext())==1232 && MyUtil.widthPixels(getContext())==800){
        fontSize = (int) Math.floor(fontSize * 0.9);
    }
    if (DEBUG) MyUtil.log(TAG,
            "Chars:"+maxCharacters+
                    " WIDTH:"+ MyDailyBread.mAppWidth+
                    ","+AxTools.dp2px(lpGold.leftMargin)+
                    ","+AxTools.dp2px(lpGold.rightMargin)+
                    ","+fontSize);
    return fontSize;
}

private int getGoldFontSize(TextView textView, String size){
    if (size.equalsIgnoreCase("L")){
        if (MyDailyBread.mGoldSizeL==0){
            MyDailyBread.mGoldSizeL = getFontSizeByMaxCharacters(textView,MyDailyBread.mMaxGold_L_characters);
        }
        return MyDailyBread.mGoldSizeL;
    } else if (size.equalsIgnoreCase("M")){
        if (MyDailyBread.mGoldSizeM==0){
            MyDailyBread.mGoldSizeM = getFontSizeByMaxCharacters(textView,MyDailyBread.mMaxGold_M_characters);
        }
        return MyDailyBread.mGoldSizeM;
    } else {
        if (MyDailyBread.mGoldSizeS==0){
            MyDailyBread.mGoldSizeS = getFontSizeByMaxCharacters(textView,MyDailyBread.mMaxGold_S_characters);
        }
        return MyDailyBread.mGoldSizeS;
    }
}
    private int getBigFontSize(TextView textView, String size){
        if (MyDailyBread.mHintSizeL==0){
            MyDailyBread.mHintSizeL = getFontSizeByMaxCharacters(textView,MyDailyBread.mMaxHint_L_characters);
        }
        if (MyDailyBread.mHintSizeS==0){
            MyDailyBread.mHintSizeS = getFontSizeByMaxCharacters(textView,MyDailyBread.mMaxHint_S_characters);
        }
        if (size.equalsIgnoreCase("L")){
            return MyDailyBread.mHintSizeL;
        } else if (size.equalsIgnoreCase("S")){
            return MyDailyBread.mHintSizeS;
        } else {
            return (MyDailyBread.mHintSizeS+MyDailyBread.mHintSizeL) / 2;
        }
    }
    public int getDesiredWidth(TextView textView){
        RelativeLayout.LayoutParams textViewParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        int usableWidth = (int) Math.floor((MyDailyBread.mAppWidth -
                AxTools.dp2px(mRootView.getPaddingLeft()) - AxTools.dp2px(mRootView.getPaddingRight()) -
                AxTools.dp2px(textViewParams.leftMargin) - AxTools.dp2px(textViewParams.rightMargin) -
                AxTools.dp2px(textView.getPaddingLeft()) - AxTools.dp2px(textView.getPaddingRight())));
        return usableWidth;
    }
    //    private int getFontSizeUponSize(TextView textView, float portion, String theText, int maxLines, float textRatioOfTextView){
//        String textLines[] = theText.split("#");
//        // Middle Ratio 20:11:7 (Not yet rendering, cannot calculate other size)
//        int statusBarHeight = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if(resourceId>0){
//            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
//        }
//        int textUsableHeight = (int) ((MyUtil.heightPixels(CMain.this) - statusBarHeight) * portion * 0.8);
//        //int maxLinesForGoldText = 3;
//        if(textLines.length>maxLines){//Relocate characters automatically (But, should be smaller size to occupy)
//            if (maxLines==3) {
//                theText = theText.replace("#", "");
//                final int charPerLines = (int) Math.ceil(theText.length() / maxLines);
//                theText = theText.substring(0, charPerLines) + "#" +
//                        theText.substring(charPerLines, charPerLines + charPerLines) + "#" +
//                        theText.substring(charPerLines + charPerLines);
//                textLines = theText.split("#");
//            } else if (maxLines==4){
//                    theText = theText.replace("#", "");
//                    final int charPerLines = (int) Math.ceil(theText.length() / maxLines);
//                    theText = theText.substring(0, charPerLines) + "#" +
//                            theText.substring(charPerLines, charPerLines + charPerLines) + "#" +
//                            theText.substring(charPerLines + charPerLines, charPerLines + charPerLines + charPerLines) + "#" +
//                            theText.substring(charPerLines + charPerLines + charPerLines);
//                    textLines = theText.split("#");
//            }
//        }
//        int maxChars = 0;
//        for(int i = 0;i<textLines.length;i++){
//            maxChars = Math.max(maxChars, textLines[i].length());
//        }
//        //int newBigFontSize = getFontSizeByMaxCharacters(textView, maxChars);
//        RelativeLayout.LayoutParams lpGold = (RelativeLayout.LayoutParams) textView.getLayoutParams();
//        int usableWidth = (int) Math.floor((MyDailyBread.mAppWidth -
//                dp2px(page1.getPaddingLeft()) - dp2px(page1.getPaddingRight()) -
//                dp2px(lpGold.leftMargin) - dp2px(lpGold.rightMargin) -
//                dp2px(textView.getPaddingLeft()) - dp2px(textView.getPaddingRight())));
//        int maxFontSizeUponUsableWidth = (int) (usableWidth / maxChars);
//        int usableHeight = (int) (textUsableHeight * textRatioOfTextView); //0.9 for smaller
//        int maxFontSizeUponUsableHeight = (int) (usableHeight / maxLines);
//        return Math.min(maxFontSizeUponUsableHeight, maxFontSizeUponUsableWidth);
//    }
}
