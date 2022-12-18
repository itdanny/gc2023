package com.hkbs.HKBS.arkCalendar;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/*
 * http://rritw.com/a/ITxinwen/hulianwang/20120712/185448.html
 * 在1582年10月15日之後實行格裏高利曆，規定每400年97閏，平均年長度为365.2425天。 
 * 在儒略曆中，能被4整除的年份为閏年，這一年有366天，其它年份为平年（365天）。 如900年和1236年为閏年，而750年和1429年为平年。
 * 格裏高利曆法也采用這一規則，但下列年份除外：不能被100整除的年份为平年，如1700年，1800年，1900年和2100年。其餘能被400整除的年份則为閏年，如1600年，2000年和2400年。 
 */
		
public class MyCalendarLunar {

	// public static void main(String[] args) throws ParseException {
	// Calendar today = Calendar.getInstance();
	// today.setTime(chineseDateFormat.parse("2003年1月1日"));
	// Lunar lunar = new Lunar(today);
	// System.out.println("北京时间：" + chineseDateFormat.format(today.getTime()) +
	// "　农历" + lunar);
	// }
	
	private int year;
	private int month;
	private int day;
	private boolean leap;

	final static String chineseNumber[] = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二" };
	final static private String TAG = MyCalendarLunar.class.getSimpleName();

	static SimpleDateFormat chineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
	static String solarTerms [];
	static String solarTerms_tc[] =
			{"立春", "雨水", "驚蟄", "春分",
					"清明", "穀雨", "立夏", "小滿", "芒種", "夏至", "小暑", "大暑", "立秋",
					"處暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"};
	static String solarTerms_sc[] =
			{"立春", "雨水", "惊蛰", "春分",
					"清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋",
					"处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"};

	final static long[] lunarInfo = new long[]
	{ 0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
	0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
	0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
	0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
	0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
	0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
	0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
	0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
	0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
	0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
	0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
	0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
	0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
	0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
	0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0 };
	// ====== 传回农历 y年的总天数

	final private static long getLunarInfoWithin150(int whichYear){
		whichYear = whichYear - 1900;
		return lunarInfo[whichYear%150];
	}
	final private static int yearDays(int y) {
		int i, sum = 348;
		for (i = 0x8000; i > 0x8; i >>= 1) {
			if ((getLunarInfoWithin150(y) & i) != 0)
				sum += 1;
		}
		return (sum + leapDays(y));
	}

	// ====== 传回农历 y年闰月的天数

	final private static int leapDays(int y) {
		if (leapMonth(y) != 0) {
			if ((getLunarInfoWithin150(y) & 0x10000) != 0)
				return 30;
			else
				return 29;
		} else
			return 0;
	}

	// ====== 传回农历 y年闰哪个月 1-12 , 没闰传回 0

	final private static int leapMonth(int y) {
		return (int) (getLunarInfoWithin150(y) & 0xf);
	}

	// ====== 传回农历 y年m月的总天数
	final private static int monthDays(int y, int m) {
		if ((getLunarInfoWithin150(y) & (0x10000 >> m)) == 0)
			return 29;
		else
			return 30;
	}

	// ====== 传回农历 y年的生肖
	final public String animalsYear() {
		final String[] Animals;
		if (mIsSimpleChinese) {
			Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
		}else{
			Animals = new String[]{"鼠", "牛", "虎", "兔", "龍", "蛇", "馬", "羊", "猴", "雞", "狗", "豬"};
		}
		return Animals[(year - 4) % 12];
	}

	// ====== 传入 月日的offset 传回干支, 0=甲子
	final private static String cyclicalm(int num) {
		final String[] Gan = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸" };
		final String[] Zhi = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥" };
		return (Gan[num % 10] + Zhi[num % 12]);
	}

	// ====== 传入 offset 传回干支, 0=甲子

	final public String cyclical() {
		int num = year - 1900 + 36;
		return (cyclicalm(num));
	}

	/** */
	/**
	 * 
	 * 传出y年m月d日对应的农历.
	 * 
	 * yearCyl3:农历年与1864的相差数 ?
	 * 
	 * monCyl4:从1900年1月31日以来,闰月数
	 * 
	 * dayCyl5:与1900年1月31日相差的天数,再加40 ?
	 * 
	 * @param cal
	 * 
	 * @return
	 */
	static private boolean mIsSimpleChinese;
    static private long mBaseDateTime=-1;

    //http://time4j.net/javadoc-en/net/time4j/calendar/ChineseCalendar.html
    // Use this version -> https://github.com/MenoData/Time4A
    // http://www.docjar.com/html/api/com/ibm/icu/util/ChineseCalendar.java.html
	public MyCalendarLunar(Calendar cal, boolean isSimpleChinese) {
		mIsSimpleChinese=isSimpleChinese;
		@SuppressWarnings("unused")
		int yearCyl, monCyl, dayCyl;
		int leapMonth = 0;
		//Date baseDate = null;
        if (mBaseDateTime==-1) {
            Calendar baseCalendar = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= 29) {
                baseCalendar.set(1900, 0, 30, 12, 0, 0);
            } else {
                baseCalendar.set(1900, 0, 31, 0, 0, 0);
            }
            baseCalendar.set(Calendar.MILLISECOND, 0);
            // DC 2021.06.22 Include Lunar Date in CSV Files, Adjustment is not required now
//            int adjVal = MyUtil.getPrefInt(MyUtil.PREF_LUNAR_ADJ,0);
//            if (adjVal!=0){
//                baseCalendar.add(Calendar.DATE,adjVal*-1);
//            }
            mBaseDateTime = baseCalendar.getTimeInMillis();
        }
//		try {
//			baseDate = chineseDateFormat.parse("1900年1月31日");
//		} catch (ParseException e) {
//			e.printStackTrace(); // To change body of catch statement use
//									// Options | File Templates.
//		}
        // 求出和1900年1月31日相差的天数
        // 2020.12.27 Wrong put 2 getTime().getTime()
        if (Build.VERSION.SDK_INT >= 29) {
            cal.set(Calendar.HOUR_OF_DAY, 12);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        long timeInLong = cal.getTimeInMillis();//cal.getTime().getTime();
        int offset = (int) ((timeInLong - mBaseDateTime) / 86400000L);
		// 求出和1900年1月31日相差的天数
		//int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
		dayCyl = offset + 40;
		monCyl = 14;

		// 用offset减去每农历年的天数
		// 计算当天是农历第几天
		// i最终结果是农历的年份
		// offset是当年的第几天

		int iYear, daysOfYear = 0;
		for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
			daysOfYear = yearDays(iYear);
			offset -= daysOfYear;
			monCyl += 12;
		}
		if (offset < 0) {
			offset += daysOfYear;
			iYear--;
			monCyl -= 12;
		}

		// 农历年份
		year = iYear;
		yearCyl = iYear - 1864;
		leapMonth = leapMonth(iYear); // 闰哪个月,1-12

		leap = false;
		// 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天

		int iMonth, daysOfMonth = 0;
		for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
			// 闰月
			if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
				--iMonth;
				leap = true;
				daysOfMonth = leapDays(year);
			} else
				daysOfMonth = monthDays(year, iMonth);

			offset -= daysOfMonth;

			// 解除闰月
			if (leap && iMonth == (leapMonth + 1))
				leap = false;
			if (!leap)
				monCyl++;
		}

		// offset为0时，并且刚才计算的月份是闰月，要校正
		if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
			if (leap) {
				leap = false;
			} else {
				leap = true;
				--iMonth;
				--monCyl;
			}
		}
		// offset小于0时，也要校正
		if (offset < 0) {
			offset += daysOfMonth;
			--iMonth;
			--monCyl;
		}
		month = iMonth;
		day = offset + 1;
	}
	private static String getChinaDayString(int day) {
		String chineseTen[] = { "初", "十", "廿", "卅" };
		int n = day % 10 == 0 ? 9 : day % 10 - 1;
		if (day > 30) return "";
		if (day == 10) return "初十";
		if (day == 20) return "二十";
		if (day == 30) return "三十";			
		return chineseTen[day / 10] + chineseNumber[n];
	}
	public String toString() {
		return year + "年" + (leap ? (mIsSimpleChinese?"闰":"閏") : "") + chineseNumber[month - 1] + "月" + getChinaDayString(day);
	}
	public int getMonth(){
		return month;
	}
	public int getDay(){
		return day;
	}
	public String toMMDD(){
		return month + "/" + day;
	}
	public String toChineseMMDD(){
		return (leap ? (mIsSimpleChinese?"闰":"閏") : "") + chineseNumber[month - 1] + "月" + getChinaDayString(day);
	}
	public String toChineseMM(){
		return (leap ? (mIsSimpleChinese?"闰":"閏") : "") + chineseNumber[month - 1] + "月";
	}
	public String toChineseDD(){
		return getChinaDayString(day);
	}
	public String toChineseYY(){
		int num = year - 1900 + 36;
		return (cyclicalm(num));		
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	static public class solar { 
		private static final  double D = 0.2422; 
		private final static Map<String,Integer[]> INCREASE_OFFSETMAP = new HashMap<String, Integer[]>();//+1偏移
		private final static Map<String,Integer[]> DECREASE_OFFSETMAP = new HashMap<String, Integer[]>();//-1偏移

		/**24节气**/ 
		private static enum SolarTermsEnum { 
		LICHUN,//--立春 
		YUSHUI,//--雨水 
		JINGZHE,//--惊蛰 
		CHUNFEN,//春分 
		QINGMING,//清明 
		GUYU,//谷雨 
		LIXIA,//立夏 
		XIAOMAN,//小满 
		MANGZHONG,//芒种 
		XIAZHI,//夏至 
		XIAOSHU,//小暑 
		DASHU,//大暑 
		LIQIU,//立秋 
		CHUSHU,//处暑 
		BAILU,//白露 
		QIUFEN,//秋分 
		HANLU,//寒露 
		SHUANGJIANG,//霜降 
		LIDONG,//立冬 
		XIAOXUE,//小雪 
		DAXUE,//大雪 
		DONGZHI,//冬至 
		XIAOHAN,//小寒 
		DAHAN;//大寒 
		} 

		static { 
		DECREASE_OFFSETMAP.put(SolarTermsEnum.YUSHUI.name(), new Integer[]{2026});//雨水 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.CHUNFEN.name(), new Integer[]{2084});//春分 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOMAN.name(), new Integer[]{2008});//小满 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.MANGZHONG.name(), new Integer[]{1902});//芒种
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAZHI.name(), new Integer[]{1928});//夏至 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOSHU.name(), new Integer[]{1925,2016});//小暑
		INCREASE_OFFSETMAP.put(SolarTermsEnum.DASHU.name(), new Integer[]{1922});//大暑 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.LIQIU.name(), new Integer[]{2002});//立秋 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.BAILU.name(), new Integer[]{1927});//白露 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.QIUFEN.name(), new Integer[]{1942});//秋分 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.SHUANGJIANG.name(), new Integer[]{2089});//霜降
		INCREASE_OFFSETMAP.put(SolarTermsEnum.LIDONG.name(), new Integer[]{2089});//立冬 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOXUE.name(), new Integer[]{1978});//小雪 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.DAXUE.name(), new Integer[]{1954});//大雪 
		DECREASE_OFFSETMAP.put(SolarTermsEnum.DONGZHI.name(), new Integer[]{1918,2021});//冬至

		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOHAN.name(), new Integer[]{1982});//小寒 
		DECREASE_OFFSETMAP.put(SolarTermsEnum.XIAOHAN.name(), new Integer[]{2019});//小寒 

		INCREASE_OFFSETMAP.put(SolarTermsEnum.DAHAN.name(), new Integer[]{2082});//大寒 
		INCREASE_OFFSETMAP.put(SolarTermsEnum.DAHAN.name(), new Integer[]{2012});//大寒 // Add by DC 
		} 

		//定义一个二维数组，第一维数组存储的是20世纪的节气C值，第二维数组存储的是21世纪的节气C值,0到23个，依次代表立春、雨水...大寒节气的C值 
		private static final double[][] CENTURY_ARRAY = 
		{{4.6295,19.4599,6.3826,21.4155,5.59,20.888,6.318,21.86,6.5,22.2,7.928,23.65,8.35,
		23.95,8.44,23.822,9.098,24.218,8.218,23.08,7.9,22.6,6.11,20.84} 
		,{3.87,18.73,5.63,20.646,4.81,20.1,5.52,21.04,5.678,21.37,7.108,22.83, 
		7.5,23.13,7.646,23.042,8.318,23.438,7.438,22.36,7.18,21.94,5.4055,20.12}}; 

		
		/** 
		* 
		* @param year 年份 
		* @param solarName 节气的名称 
		* @return 返回节气是相应月份的第几天 
		*/ 
		private static int getSolarTermNum(int year,String solarName){
			solarName = solarName.trim().toUpperCase();
			int ordinal = SolarTermsEnum.valueOf(solarName).ordinal();
			double centuryValue = 0;//节气的世纪值，每个节气的每个世纪值都不同 
			int centuryIndex = -1; 
			if(year>=1901 && year<=2000){//20世纪 
				centuryIndex = 0; 
			} else if(year>=2001 && year <= 2100){//21世纪 
				centuryIndex = 1; 
			} else { 
				throw new RuntimeException("不支持此年份："+year+"，目前只支持1901年到2100年的时间范围"); 
			} 
			centuryValue = CENTURY_ARRAY[centuryIndex][ordinal]; 
			int dateNum = 0; 
			/** 
			* 计算 num =[Y*D+C]-L这是传说中的寿星通用公式 
			* 公式解读：年数的后2位乘0.2422加C(即：centuryValue)取整数后，减闰年数 
			*/ 
			int y = year%100;//步骤1:取年分的后两位数 
			if(year%4 == 0 && year%100 !=0 || year%400 ==0){//闰年 
				// DC: The following calculation makes difference with HK Weather calculation variance 
				if(ordinal == SolarTermsEnum.XIAOHAN.ordinal() || ordinal == SolarTermsEnum.DAHAN.ordinal()
				|| ordinal == SolarTermsEnum.LICHUN.ordinal() || ordinal == SolarTermsEnum.YUSHUI.ordinal()){
					//注意：凡闰年3月1日前闰年数要减一，即：L=[(Y-1)/4],因为小寒、大寒、立春、雨水这两个节气都小于3月1日,所以 y = y-1 
					y = y-1;//步骤2 
				} 
			} 
			dateNum = (int)(y*D+centuryValue)-(int)(y/4);//步骤3，使用公式[Y*D+C]-L计算 
			dateNum += specialYearOffset(year,solarName);//步骤4，加上特殊的年分的节气偏移量 
			return dateNum; 
		} 

		/** 
		* 特例,特殊的年分的节气偏移量,由于公式并不完善，所以算出的个别节气的第几天数并不准确，在此返回其偏移量 
		* @param year 年份 
		* @param name 节气名称 
		* @return 返回其偏移量 
		*/ 
		public static int specialYearOffset(int year,String name) { 
			int offset = 0; 
			offset += getOffset(DECREASE_OFFSETMAP,year,name,-1); 
			offset += getOffset(INCREASE_OFFSETMAP,year,name,1); 
			return offset; 
		} 

		public static int getOffset(Map<String,Integer[]> map,int year,String name,int offset){
			int off = 0; 
			Integer[] years = map.get(name); 
			if(null != years){ 
				for(int i:years){ 
					if(i == year){ 
						off = offset; 
						break; 
					} 
				} 
			} 
			return off; 
		} 
		
		public static String getSolarTerm(Calendar cal){
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);// Zero-Index
			int day = cal.get(Calendar.DAY_OF_MONTH);
			// Get 2 solar term of specific month
			int seqNbr;
			if (month==0){ // Jan
				seqNbr = 22;
			} else {
				seqNbr = (month-1) * 2;
			}
			solarTerms = mIsSimpleChinese?solarTerms_sc:solarTerms_tc;
			String term = SolarTermsEnum.values()[seqNbr].name();
			int termDay = getSolarTermNum(year,term);
			if (termDay==day){
				term = solarTerms[seqNbr];
				
			} else {
				term = SolarTermsEnum.values()[seqNbr+1].name();
				termDay = getSolarTermNum(year,term);
				if (termDay==day){
					term = solarTerms[seqNbr+1];					
				} else {
					term="";
				}
			}			
			//MyApp.log(TAG,year+"/"+(month+1)+"/"+day+","+term);
			return term;
		}

//		public static String getSolarTerm(int year) {
//			StringBuffer sb = new StringBuffer();
//			sb.append("---").append(year);
//			if(year%4 == 0 && year%100 !=0 || year%400 ==0){//闰年
//				sb.append(mIsSimpleChinese?" 闰年":" 閏年");
//			} else {
//				sb.append(" 平年");
//			}
//			if (mIsSimpleChinese) {
//				sb.append("\n")
//						.append("小寒:1月").append(getSolarTermNum(year, SolarTermsEnum.XIAOHAN.name()))
//						.append("日,大寒:1月").append(getSolarTermNum(year, SolarTermsEnum.DAHAN.name()))
//						.append("立春：2月").append(getSolarTermNum(year, SolarTermsEnum.LICHUN.name()))
//						.append("日,雨水：2月").append(getSolarTermNum(year, SolarTermsEnum.YUSHUI.name()))
//						.append("日,惊蛰:3月").append(getSolarTermNum(year, SolarTermsEnum.JINGZHE.name()))
//						.append("日,春分:3月").append(getSolarTermNum(year, SolarTermsEnum.CHUNFEN.name()))
//						.append("日,清明:4月").append(getSolarTermNum(year, SolarTermsEnum.QINGMING.name()))
//						.append("日,谷雨:4月").append(getSolarTermNum(year, SolarTermsEnum.GUYU.name()))
//						.append("日,立夏:5月").append(getSolarTermNum(year, SolarTermsEnum.LIXIA.name()))
//						.append("日,小满:5月").append(getSolarTermNum(year, SolarTermsEnum.XIAOMAN.name()))
//						.append("日,芒种:6月").append(getSolarTermNum(year, SolarTermsEnum.MANGZHONG.name()))
//						.append("日,夏至:6月").append(getSolarTermNum(year, SolarTermsEnum.XIAZHI.name()))
//						.append("日,小暑:7月").append(getSolarTermNum(year, SolarTermsEnum.XIAOSHU.name()))
//						.append("日,大暑:7月").append(getSolarTermNum(year, SolarTermsEnum.DASHU.name()))
//						.append("日,\n立秋:8月").append(getSolarTermNum(year, SolarTermsEnum.LIQIU.name()))
//						.append("日,处暑:8月").append(getSolarTermNum(year, SolarTermsEnum.CHUSHU.name()))
//						.append("日,白露:9月").append(getSolarTermNum(year, SolarTermsEnum.BAILU.name()))
//						.append("日,秋分:9月").append(getSolarTermNum(year, SolarTermsEnum.QIUFEN.name()))
//						.append("日,寒露:10月").append(getSolarTermNum(year, SolarTermsEnum.HANLU.name()))
//						.append("日,霜降:10月").append(getSolarTermNum(year, SolarTermsEnum.SHUANGJIANG.name()))
//						.append("日,立冬:11月").append(getSolarTermNum(year, SolarTermsEnum.LIDONG.name()))
//						.append("日,小雪:11月").append(getSolarTermNum(year, SolarTermsEnum.XIAOXUE.name()))
//						.append("日,大雪:12月").append(getSolarTermNum(year, SolarTermsEnum.DAXUE.name()))
//						.append("日,冬至:12月").append(getSolarTermNum(year, SolarTermsEnum.DONGZHI.name()))
//						.append("日");
//			} else {
//				sb.append("\n")
//						.append("小寒:1月").append(getSolarTermNum(year,SolarTermsEnum.XIAOHAN.name()))
//						.append("日,大寒:1月").append(getSolarTermNum(year,SolarTermsEnum.DAHAN.name()))
//						.append("立春：2月").append(getSolarTermNum(year,SolarTermsEnum.LICHUN.name()))
//						.append("日,雨水：2月").append(getSolarTermNum(year,SolarTermsEnum.YUSHUI.name()))
//						.append("日,驚蟄:3月").append(getSolarTermNum(year,SolarTermsEnum.JINGZHE.name()))
//						.append("日,春分:3月").append(getSolarTermNum(year,SolarTermsEnum.CHUNFEN.name()))
//						.append("日,清明:4月").append(getSolarTermNum(year,SolarTermsEnum.QINGMING.name()))
//						.append("日,谷雨:4月").append(getSolarTermNum(year,SolarTermsEnum.GUYU.name()))
//						.append("日,立夏:5月").append(getSolarTermNum(year,SolarTermsEnum.LIXIA.name()))
//						.append("日,小滿:5月").append(getSolarTermNum(year,SolarTermsEnum.XIAOMAN.name()))
//						.append("日,芒種:6月").append(getSolarTermNum(year,SolarTermsEnum.MANGZHONG.name()))
//						.append("日,夏至:6月").append(getSolarTermNum(year,SolarTermsEnum.XIAZHI.name()))
//						.append("日,小暑:7月").append(getSolarTermNum(year,SolarTermsEnum.XIAOSHU.name()))
//						.append("日,大暑:7月").append(getSolarTermNum(year,SolarTermsEnum.DASHU.name()))
//						.append("日,\n立秋:8月").append(getSolarTermNum(year,SolarTermsEnum.LIQIU.name()))
//						.append("日,處暑:8月").append(getSolarTermNum(year,SolarTermsEnum.CHUSHU.name()))
//						.append("日,白露:9月").append(getSolarTermNum(year,SolarTermsEnum.BAILU.name()))
//						.append("日,秋分:9月").append(getSolarTermNum(year,SolarTermsEnum.QIUFEN.name()))
//						.append("日,寒露:10月").append(getSolarTermNum(year,SolarTermsEnum.HANLU.name()))
//						.append("日,霜降:10月").append(getSolarTermNum(year,SolarTermsEnum.SHUANGJIANG.name()))
//						.append("日,立冬:11月").append(getSolarTermNum(year,SolarTermsEnum.LIDONG.name()))
//						.append("日,小雪:11月").append(getSolarTermNum(year,SolarTermsEnum.XIAOXUE.name()))
//						.append("日,大雪:12月").append(getSolarTermNum(year,SolarTermsEnum.DAXUE.name()))
//						.append("日,冬至:12月").append(getSolarTermNum(year,SolarTermsEnum.DONGZHI.name()))
//						.append("日");
//			}
//		return sb.toString();
//		}

//		public static void main(String[] args) { 
//			for(int year=1912;year<2015;year++){ 
//				System.out.println(solarTermToString(year)); 
//			} 
//		} 
	} 
}