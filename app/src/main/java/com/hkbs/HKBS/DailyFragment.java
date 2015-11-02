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

    private CMain mCMain;
    private ViewGroup mRootView;
    private int mCurYear;
    private int mCurMonth;//Zero_base
    private int mCurDay;
    private Calendar mCalendar;
    private MyCalendarLunar mLunar;

    static public DailyFragment getInstance(CMain cmain, int year, int month, int day){
        DailyFragment dailyFragment = new DailyFragment();
        dailyFragment.mCMain = cmain;
        dailyFragment.mCurYear = year;
        dailyFragment.mCurMonth = month;
        dailyFragment.mCurDay = day;
        dailyFragment.mCalendar = Calendar.getInstance();
        dailyFragment.mCalendar.set(year, month, day, 0, 0, 0);
        dailyFragment.mLunar = new MyCalendarLunar(dailyFragment.mCalendar);
        return dailyFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.activity_page1, container, false);
        onRefreshScreen();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefreshScreen();
    }
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
    
    public void onRefreshScreen(){
        if (mRootView==null) return;
        mContentValues = mCMain.mDailyBread.getContentValues(mCurYear, mCurMonth, mCurDay);// get ContentValues from dailyBread file
        if (mContentValues==null) {
            Log.e(TAG, "Contens not found ("+mCurYear+","+mCurMonth+","+mCurDay+")");
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
        
        //mWisdomTextView.setText("Date="+mCurYear+" "+mCurMonth+" "+mCurDay);
        mScrType="";
        if (mScreenTypeView!=null){
            mScrType=mScreenTypeView.getTag().toString();
            mCMain.mScreenType=mScrType; 
            if (!DEBUG) mScreenTypeView.setText("");
        }
        mHolidayText = MyHoliday.getHolidayRemark(mCalendar.getTime());
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

    }
    private void setRow1YearAndMonth(){
        mEngYear.setText(String.valueOf(mCurYear));
        mEngYear.setTextColor(mTextColor);

        mEngMonthName.setText(MyUtil.sdfEngMMMM.format(mCalendar.getTime()));
        mEngMonthName.setTextColor(mTextColor);

        mChiMonthName.setText(CHI_MONTHS[mCalendar.get(Calendar.MONTH)] + "月");
        mChiMonthName.setTextColor(mTextColor);
    }
    private void setRow2Day(){
        mBigDay.setText(String.valueOf(mCurDay));
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

        final int maxHolyDaysChars=9;
        mHolyDay1View.setTextColor(mTextColor);
        mHolyDay2View.setTextColor(mTextColor);
        if (MyUtil.getPrefInt(MyUtil.PREF_HOLY_DAY,0)<=0 || mHolyDayText.equals("")){
            mHolyDay1View.setVisibility(View.GONE);
            mHolyDay2View.setVisibility(View.GONE);
        } else {
            String holyDayLines [] = new String[]{"",""};
            int splitIndex=mHolyDayText.indexOf("#");
            if (splitIndex>=0){
                for (int i=0;i<splitIndex;i++){
                    holyDayLines[0]=holyDayLines[0]+mHolyDayText.substring(i,i+1)+"\n";
                }
                for (int i=splitIndex+1;i<mHolyDayText.length();i++){
                    holyDayLines[1]=holyDayLines[1]+mHolyDayText.substring(i,i+1)+"\n";
                }
            } else {
                holyDayLines = new String[]{"",""};
                if (mHolyDayText.length()>maxHolyDaysChars){
                    for (int i=0;i<maxHolyDaysChars;i++){
                        holyDayLines[0]=holyDayLines[0]+mHolyDayText.substring(i,i+1)+"\n";
                    }
                    for (int i=maxHolyDaysChars;i<mHolyDayText.length();i++){
                        holyDayLines[1]=holyDayLines[1]+mHolyDayText.substring(i,i+1)+"\n";
                    }
                } else {
                    for (int i=0;i<mHolyDayText.length();i++){
                        holyDayLines[0]=holyDayLines[0]+mHolyDayText.substring(i,i+1)+"\n";
                    }
                }
            }
            mHolyDay1View.setLines(holyDayLines[0].length()/2);
            mHolyDay1View.setText(holyDayLines[0]);
            mHolyDay1View.setVisibility(View.VISIBLE);
            if (holyDayLines[1].equalsIgnoreCase("")){
                mHolyDay2View.setVisibility(View.GONE);
            } else {
                mHolyDay2View.setVisibility(View.VISIBLE);
                mHolyDay2View.setLines(holyDayLines[1].length()/2);
                mHolyDay2View.setText(holyDayLines[1]);
            }
        }
    }
    private void setRow3ChineseYearMonthAndWeekday(){
        // Prepare data
        final Calendar monthEndDate = (Calendar) mCalendar.clone();
        int nbrOfDaysTo30 = 30-mLunar.getDay(); // Chinese Day
        monthEndDate.add(Calendar.DAY_OF_MONTH, nbrOfDaysTo30);
        final MyCalendarLunar monthEndLunar = new MyCalendarLunar(monthEndDate);
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

        if (mCurYear>=2016) {
            mWeekDayImage.setImageResource(mIsHoliday ? R.drawable.red_weekday_2016 : R.drawable.green_weekday_2016);            
        } else {
            mWeekDayImage.setImageResource(mIsHoliday ? R.drawable.red_weekday_2015 : R.drawable.green_weekday_2015);
        }
        mWeekDay.setTextColor(getResources().getColor(R.color.white));

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
        if (mCurYear>=2016) {
            mGoldFrame.setImageDrawable(getResources().getDrawable(mIsHoliday?R.drawable.red_frame_2016:R.drawable.green_frame_2016));
            mGoldFrame.setVisibility(View.VISIBLE);
        } else {
            mGoldFrame.setImageDrawable(getResources().getDrawable(mIsHoliday?R.drawable.red_frame_2015:R.drawable.green_frame_2015));
            mGoldFrame.setVisibility(View.VISIBLE);
        }

        /***************************************************************
            GOLD TEXT
         ***************************************************************/

        Calendar calendar = Calendar.getInstance();
        if (CMain.IS_2016_VERSION) {
            if (calendar.get(Calendar.YEAR) == mCurYear &&
                    calendar.get(Calendar.MONTH) == mCurMonth &&
                    calendar.get(Calendar.DAY_OF_MONTH) == mCurDay) {
                YoYo.with(Techniques.Bounce).duration(1000).playOn(mGoldTextView);
            }
        }

        String goldText = mContentValues.getAsString(MyDailyBread.wGoldText);
        final String goldLines [] = goldText.split("#");
        mMaxCharsPerGoldLine =0;
        for (int i=0;i<goldLines.length;i++){
            mMaxCharsPerGoldLine =Math.max(mMaxCharsPerGoldLine, goldLines[i].length());
        }
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
            //(3 lines for text; 1 for verse;1 for top & bottom i.e. 3/5 = around 0.6)
            if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)){//Higher ratio to small screen
                mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 3, goldLines.length, 0.65f, mMaxCharsPerGoldLine);
            } else {
                mGoldTextFontSize = setViewFontBySize(goldLines, mGoldTextView, (float) 11 / 38, 3, goldLines.length, 0.6f, mMaxCharsPerGoldLine);
            }
            mGoldTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mGoldTextFontSize);
        } else {
            mGoldTextFontSize = (int) mGoldTextView.getTextSize();
        }

        String goldAlign = mContentValues.getAsString(MyDailyBread.wGoldAlign);
        if (mCurYear<=2012 || mCurYear>=2016){
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

        mGoldVerseView.setText(mContentValues.getAsString(MyDailyBread.wGoldVerse) + (mCurYear >= 2016 ? "" : "；和合本修訂版"));
        mGoldVerseView.setTextColor(mTextColor);
        // From HKBS, size change to less than Gold
        if (CMain.IS_2016_VERSION) {
            mGoldVerseFontSize=getFontSizeByMaxCharacters(mGoldTextView, 22);
            mGoldVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(mGoldVerseFontSize,(int)(mGoldTextFontSize*0.8)));
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
        if (mCurYear>=2010){
            mGoldVerseView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        if (mCurYear>=2015){
            if (mScrType.equalsIgnoreCase(STD_LAYOUT)) {
                RelativeLayout.LayoutParams goldVerseLP = (RelativeLayout.LayoutParams) mGoldVerseView.getLayoutParams();
                if (CMain.IS_2016_VERSION) {
                    goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, AxTools.dp2px(16));
                } else {
                    goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, AxTools.dp2px(22));
                }
                mGoldVerseView.setLayoutParams(goldVerseLP);
            }
        }
    }
    private void setRow5Wisdom(){
        if (mCurYear>=2016) {
            mWisdomIcon.setImageDrawable(getResources().getDrawable(mIsHoliday ? R.drawable.red_icon_2016 : R.drawable.green_icon_2016));
        } else {
            mWisdomIcon.setScaleType(ImageView.ScaleType.FIT_XY);
            if (mCurYear >= 2016) {
                mWisdomIcon.setImageDrawable(null);
            } else {
                mWisdomIcon.setImageDrawable(getResources().getDrawable(mIsHoliday ? R.drawable.red_icon_2015 : R.drawable.green_icon_2015));
            }
        }

        mWisdomTextView.setTextColor(mTextColor);

        // Any Heading ":"
        String colonText = ":"; // English Style colon
        String wisdomText = mContentValues.getAsString(MyDailyBread.wBigText);
        int colonPos=-1;
        if (wisdomText.startsWith("#")) {
            wisdomText = wisdomText.substring(1);
        } else {
            int englishColonPos = wisdomText.indexOf(":");
            int chineseColonPos = wisdomText.indexOf("：");
            if (englishColonPos < 0) {
                if (chineseColonPos >= 0) {
                    colonPos = chineseColonPos;
                } else {
                    colonPos = -1;
                }
            } else {
                if (chineseColonPos >= 0) {
                    colonPos = Math.min(englishColonPos, chineseColonPos);
                } else {
                    colonPos = englishColonPos;
                }
            }
        }
        String wisdomLines [] = wisdomText.split("#");
        int maxWisdomChars=0;
        for (int i=0;i<wisdomLines.length;i++){
            maxWisdomChars=Math.max(maxWisdomChars,wisdomLines[0].length());
        }
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
                mWisdomTextFontSize = setViewFontBySize(wisdomLines, mWisdomTextView, (float) 7 / 38, 4, wisdomLines.length, 0.8f, maxWisdomChars);
                if (mMaxCharsPerGoldLine > 15){// Too small, nearly same size is ok
                    mWisdomTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(mWisdomTextFontSize,(int)(mGoldTextFontSize*0.95f)));
                } else {
                    mWisdomTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(mWisdomTextFontSize, (int) (mGoldTextFontSize * 0.8f)));
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
        RelativeLayout.LayoutParams bigTextLP = (RelativeLayout.LayoutParams) mWisdomTextView.getLayoutParams();
        if (mCurYear>=2016) {
            mWisdomTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            int margin=Math.min(bigTextLP.leftMargin,bigTextLP.rightMargin);
            bigTextLP.setMargins(margin,bigTextLP.topMargin,margin,bigTextLP.bottomMargin);
            mWisdomTextView.setLayoutParams(bigTextLP);
        } else {
            mWisdomTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
        if (DEBUG) {
            Log.w(TAG, "Device="+getString(R.string.deviceType)+" screenType=" + mScrType + " Hint TextSize=" + mWisdomTextFontSize + " " + mWisdomTextView.getHeight()+" "+mCurYear+"-"+(mCurMonth+1)+"-"+mCurDay);
        }
/***********************************************************************************
 * 最後一行字 : pageHintText (SIZE IS STANDARD)
 ************************************************************************************/
        mWisdomVerseView.setText(mContentValues.getAsString(MyDailyBread.wSmallText));
        mWisdomVerseView.setTextColor(mTextColor);
        float referenceSize=0;// = getBigFontSize(pageWisdomText,"S");
        if (CMain.IS_2016_VERSION) {
            referenceSize = mWisdomVerseView.getTextSize();
        } else {
            referenceSize = mWisdomTextView.getTextSize();
        }
        if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {
            mWisdomVerseFontSize = (int) (referenceSize - AxTools.dp2px(2));
        } else if (mScrType.equalsIgnoreCase(STD_LAYOUT)) {
            mWisdomVerseFontSize = (int) (referenceSize - AxTools.dp2px(3));
        } else {
            mWisdomVerseFontSize = (int) (referenceSize - AxTools.dp2px(4));
        }
        mWisdomVerseView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mWisdomVerseFontSize);//DC: 2013.12.12
    }
    
    private int getAppHeight(){
        if (statusBarHeight==0) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }
        return MyUtil.heightPixels(mCMain) - statusBarHeight;
    }
    /*
    * @param fullScreenRatio  The ratio that textView occupy on screen
    * @param textRatio  The ratio that text occupy on textView
     */
    public int setViewFontBySize(String lines[], TextView textView, float fullScreenRatio, int maxLines, int nbrOfLines, float textRatio, int maxChars){
        int maxHeight = (int) (getAppHeight() * fullScreenRatio * textRatio);
        // Either 3 lines (Gold) or 4 lines (Wisdom)
        if (maxLines==3) {
            switch (nbrOfLines) {
                case 1:
                    maxHeight = maxHeight * 2 / maxLines;
                    break;
                case 2:
                    if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {//Give more space since screen is small
                        maxHeight = (int) (maxHeight * maxLines / maxLines);
                    } else {
                        maxHeight = (int) (maxHeight * 2.5 / maxLines);
                    }
                    break;
                default:
                    //maxHeight = maxHeight * maxLines / maxLines;
                    break;
            }
        } else if (maxLines==4){//Wisdom 4 Lines
            switch (nbrOfLines) {
                case 1:
                    maxHeight = maxHeight * 2 / maxLines; // Display One Row with Two row space
                    break;
                case 2:
                    maxHeight = maxHeight * 3  / maxLines; // Display Two Row with Three row space
                    break;
                case 3:
                    if (mScrType.equalsIgnoreCase(SMALL_LAYOUT)) {//Give more space since screen is small
                        maxHeight = (int) (maxHeight * 4 / maxLines);
                    } else {
                        maxHeight = (int) (maxHeight * 3.5 / maxLines);
                    }
                    break;
                default:
                    //maxHeight = (int) (maxHeight * 4  / maxLines);
                    break;
            }
        }
        int maxWidth = getDesiredWidth(textView);
        //int textSize = nbrOfLines==1?(int)(maxWidth/2):200;// inital text size
        //int textSize = nbrOfLines==1?maxHeight:200;
        //Log.e(TAG,"curDay="+mDisplayDay.get(Calendar.DAY_OF_MONTH));
        int textSize = maxHeight;
        String text = textView.getText().toString();
        textPaint=textView.getPaint();
        boolean isDigit=TextUtils.isDigitsOnly(text);
        while (getHeightOfMultiLineText(text, textSize, maxWidth, nbrOfLines, isDigit) >= maxHeight) {
            textSize--;
        }
        //Log.e(TAG,"After check by Height Font Size ="+textSize);
        if (textSize*maxChars > maxWidth ) {
            textSize = (int) (Math.floor(maxWidth / maxChars));
//            if (lines==null){
//                textPaint.setTextSize(textSize);
//                textPaint.getTextBounds(text, 0, text.length(), textBounds);
//                while (textBounds.width()>=maxWidth){
//                    textSize--;
//                    textPaint.setTextSize(textSize);
//                    textPaint.getTextBounds(text, 0, text.length(), textBounds);
//                }
//            } else {
//                for (int i=0;i<lines.length;i++){
//                    text = lines[i]+"M";
//                    textPaint.setTextSize(textSize);
//                    textPaint.getTextBounds(text, 0, text.length(), textBounds);
//                    while (textBounds.width()>=maxWidth){
//                        textSize--;
//                        textPaint.setTextSize(textSize);
//                        textPaint.getTextBounds(text, 0, text.length(), textBounds);
//                    }
//                }
        }
        if (!(mScrType.equalsIgnoreCase(SMALL_LAYOUT) && maxLines==4)) {//Screen so small, not reduct siz
            textSize--;
        }
        //Log.e(TAG,"After check by Width Font Size ="+textSize);
//        }
        return textSize;
    }
    private int getHeightOfMultiLineText(String text, int textSize, int maxWidth, int nbrOfLines, boolean isDigit) {
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
        textPaint.getTextBounds("Yy", 0, 2, textBounds);
        double lineSpacing;
        if (isDigit) {
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            return (int) (textBounds.height()-fontMetrics.descent);
        } else {
            // obtain space between lines
            lineSpacing = Math.max(0, ((lineCount - 1) * textBounds.height() * 0.25));
            return (int) Math.floor(lineSpacing + lineCount * textBounds.height());
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
    if (android.os.Build.MODEL.equalsIgnoreCase("MID") && MyUtil.heightPixels(mCMain)==1232 && MyUtil.widthPixels(mCMain)==800){
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
