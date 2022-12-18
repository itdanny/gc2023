package com.hkbs.HKBS.arkCalendar;

import net.time4j.PlainDate;
import net.time4j.calendar.ChineseCalendar;
import net.time4j.calendar.SolarTerm;
import net.time4j.format.NumberSystem;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// **********
// https://itw01.com/UBF9UEI.html
// **********
public class MyChineseCalendar {
    private ChineseCalendar mCC;
    private Locale mLocale;
    private Calendar mCal;
    private boolean mIsSimpleChinese;
    static int mThisYear = -1;
    static List<ChineseCalendar> mThisYearList;
    static int mLastYear = -1;
    static List<ChineseCalendar> mLastYearList;
    static String[] chineseNumber = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
//    static String solarTerms_tc[] =
//            {"立春", "雨水", "驚蟄", "春分",
//                    "清明", "穀雨", "立夏", "小滿", "芒種", "夏至", "小暑", "大暑", "立秋",
//                    "處暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"};
//    static String solarTerms_sc[] =
//            {"立春", "雨水", "惊蛰", "春分",
//                    "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋",
//                    "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"};

    public MyChineseCalendar(Calendar cal, boolean isSimpleChinese) {
        mIsSimpleChinese = isSimpleChinese;
        mLocale = isSimpleChinese ? Locale.CHINA : Locale.TRADITIONAL_CHINESE;
        init(cal);
//        test(2022, 12, 18);
//        test(2022, 12, 22);
//        test(2022, 12, 25);
//        test(2022, 12, 31);
//        test(2023, 1, 1);
//        test(2023, 1, 5);
//        test(2023, 1, 20);
//        test(2023, 1, 21);
//        test(2023, 1, 22);
//        test(2023, 3, 21);
//        test(2023, 3, 22);
//        test(2023, 12, 22);
//        test(2023, 12, 31);
//        System.out.println("Completed");
    }

    private void test(int calYear, int calMonth, int calDay) {
        init(calYear, calMonth, calDay);
        System.out.println(calYear + "/" + calMonth + "/" + calDay + " " + ccYear() + " " + ccAnimal() + " " + ccLeap() + " " + ccMonth() + "月 " + ccMonthBigSmall() + " " + ccDay() + " " + ccSolar());
    }

    private void init(int calYear, int calMonth, int calDay) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, calYear);
        cal.set(Calendar.MONTH, calMonth - 1); // Calendar Month Index start from ZERO
        cal.set(Calendar.DAY_OF_MONTH, calDay);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        init(cal);
    }

    private void init(Calendar calendar) {
        mCal = calendar;
        PlainDate plainDate = PlainDate.of(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1, mCal.get(Calendar.DAY_OF_MONTH));
        mCC = plainDate.transform(ChineseCalendar.class);
    }

    public String ccSolar() {//大寒
        //return mCC.getSexagesimalDay().getDisplayName(mLocale);//庚辰
        ChineseCalendar cc;
        List<ChineseCalendar> ccList;
        if (mCal.get(Calendar.YEAR) != mThisYear) {
            mThisYear = mCal.get(Calendar.YEAR);
            mThisYearList = SolarTerm.list(mCal.get(Calendar.YEAR), ChineseCalendar.class);
        }
        ccList = mThisYearList;
        for (int i = 0; i < ccList.size(); i++) {
            cc = ccList.get(i);
            if (cc.getYear() == mCC.getYear() && cc.getMonth() == mCC.getMonth() && cc.getDayOfMonth() == mCC.getDayOfMonth()) {
                return ccList.get(i).getSolarTerm().getDisplayName(mLocale);
            }
        }
        if (mCal.get(Calendar.YEAR) - 1 != mLastYear) {
            mLastYear = mCal.get(Calendar.YEAR) - 1;
            mLastYearList = SolarTerm.list(mCal.get(Calendar.YEAR) - 1, ChineseCalendar.class);
        }
        ccList = mLastYearList;
        for (int i = 0; i < ccList.size(); i++) {
            cc = ccList.get(i);
            if (cc.getYear() == mCC.getYear() && cc.getMonth() == mCC.getMonth() && cc.getDayOfMonth() == mCC.getDayOfMonth()) {
                return ccList.get(i).getSolarTerm().getDisplayName(mLocale);
            }
        }
        return "";
    }

    //        Moment moment = TemporalType.MILLIS_SINCE_UNIX.translate(mCC.getDaysSinceEpochUTC() * 86400000);
//        return SolarTerm.of(moment).getDisplayName(mLocale);
    public int year() {  // Chinese Year number; Not Used
        return mCal.get(Calendar.YEAR);
    }

    public int month() {  // Chinese Year number; Not Used
        return mCal.get(Calendar.MONTH) + 1;
    }

    public Calendar calendar() {
        return mCal;
    }

    public String ccMonthBigSmall() {  // Chinese
        return mCC.lengthOfMonth() == 30 ? "大" : "小";
        // return mCC.getMaximum(ChineseCalendar.DAY_OF_MONTH) == 30 ? "大" : "小";
    }

    public String ccAnimal() {//虎
        return mCC.getYear().getZodiac(mLocale);
    }

    public String ccYear() {// 壬寅
        return mCC.getYear().getDisplayName(mLocale);
//        int num = year - 1900 + 36;
//        return (cyclicalm(num));
    }

    public String ccLeap() {//十二月
        return (mCC.getMonth().isLeap() ? (mIsSimpleChinese ? "闰" : "閏") : "");
    }

    public String ccMonth() {//十二月
        return chineseNumber[mCC.getMonth().getNumber() - 1];
    }

    public String ccArabicMonthWithLeap() {//十二月
        return mCC.getMonth().getDisplayName(mLocale, NumberSystem.ARABIC);
    }

    public int day() {
        return mCal.get(Calendar.DAY_OF_MONTH);
    }

    public String monthDay() { // 12/18
        return (mCal.get(Calendar.MONTH) + 1) + "/" + mCal.get(Calendar.DAY_OF_MONTH);
    }

    public String ccDay() { // 二十
        int day = mCC.getDayOfMonth();
        String ccDay = "";
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30) {
            ccDay = "";
        } else if (day == 10) {
            ccDay = "初十";
        } else if (day == 20) {
            ccDay = "二十";
        } else if (day == 30) {
            ccDay = "三十";
        } else {
            String[] chineseTen = {"初", "十", "廿", "卅"};

            ccDay = chineseTen[day / 10] + chineseNumber[n];
        }
        return ccDay;
    }
}
