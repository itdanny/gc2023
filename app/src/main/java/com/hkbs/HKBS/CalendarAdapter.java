/*
 * Copyright 2011 Lauri Nevala.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hkbs.HKBS;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hkbs.HKBS.arkCalendar.MyCalendar;
import com.hkbs.HKBS.arkCalendar.MyCalendar.MyDayEvents;
import com.hkbs.HKBS.arkCalendar.MyCalendarLunar;
import com.hkbs.HKBS.arkUtil.MySquareView;
import com.hkbs.HKBS.arkUtil.MyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {
	static final public SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd", Locale.US);
	static final boolean IS_SHOW_LUNAR_DAY_ON_SUNDAY_ONLY = false;
	static final int FIRST_DAY_OF_WEEK = Calendar.SUNDAY;
	static final int GRID_TYPE_TITLE = 0;
	static final int GRID_TYPE_CELL = 1;
	// references to our items
	public MyDayEvents[] daysEvents;
	private MyDayEvents[] daysTitles;

	private Context actContext;
	private Calendar displayMonth;
	public Calendar selectedDate;
	private ArrayList<String> items;
	private int gridType;
	private String defaultLang;
	private Calendar today;
	
	//private Calendar today = Calendar.getInstance();
	static public Map<String, String> holidayMap = new HashMap<String, String>();
		//static public String holidayCityName = "";
		
	public CalendarAdapter(Context actContext, Calendar monthCalendar, int gridType) {
		defaultLang = MyUtil.getPrefStr(MyUtil.PREF_LANG, "HK");
		selectedDate = (Calendar) monthCalendar.clone();
		today = Calendar.getInstance();
		this.actContext = actContext;
		this.items = new ArrayList<String>();
		this.gridType = gridType;		
		refreshDays(monthCalendar);
		this.notifyDataSetChanged();
	}
	
	static public String getHolidayText(Context context, Calendar cal){
		String key = sdfDate.format(cal.getTime());
		if (holidayMap.containsKey(key)){
			//return holidayCity+" : "+holidayMap.get(key);
			return holidayMap.get(key);
		} else {
			return "";
		}
	}
	public void setSelectedDate(Calendar newDate) {
		selectedDate = (Calendar) newDate.clone();
		notifyDataSetChanged();
	}
	public void setItems(ArrayList<String> items) {
		for (int i = 0; i != items.size(); i++) {
			if (items.get(i).length() == 1) {
				items.set(i, "0" + items.get(i));
			}
		}
		this.items = items;
	}

	public int getCount() {
		return (this.gridType == GRID_TYPE_TITLE?daysTitles.length:Math.min(35, daysEvents.length));
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new view for each item referenced by the Adapter
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyDayEvents curDayEvents;
		if (this.gridType == GRID_TYPE_TITLE) {
			curDayEvents = daysTitles[position];			
		} else {
			curDayEvents = daysEvents[position];			
		}
		View v = convertView;
		TextView dayView;
		if (convertView == null) { // if it's not recycled, initialize some attributes
			LayoutInflater vi = (LayoutInflater) actContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.activity_calendar_adapter,null);
		}
		dayView = (TextView) v.findViewById(R.id.xmlDay);
		// disable empty days from the beginning
		v.setTag(R.id.TAG_POSITION, position);
		final ImageView icon = (ImageView) v.findViewById(R.id.date_icon);		
		final TextView txtTitle = (TextView) v.findViewById(R.id.xmlTitle);
		final MySquareView txtCell = (MySquareView) v.findViewById(R.id.xmlCell);
		txtTitle.setVisibility(curDayEvents.getType()==MyDayEvents.TYPE_TITLE ? View.VISIBLE : View.GONE);
		txtCell.setVisibility(curDayEvents.getType()==MyDayEvents.TYPE_TITLE ? View.GONE : View.VISIBLE);

		Calendar curCal;
		boolean isOtherMonthCell;
		if (this.gridType == GRID_TYPE_TITLE) {
			txtTitle.setText(curDayEvents.text);
			txtTitle.setClickable(false);
			txtTitle.setFocusable(false);
			txtTitle.setBackgroundResource(R.color.calendarTitleBkg);
			icon.setVisibility(View.GONE);
		} else {						
			curCal = curDayEvents.getCalendar();
			String output="<bold>"+String.valueOf(curCal.get(Calendar.DAY_OF_MONTH))+"</bold>";
			// show UNDERLINE on TODAY
            MyUtil.sdfYYYYMMDD.setTimeZone(curCal.getTimeZone());
			final boolean isToday = MyUtil.sdfYYYYMMDD.format(curCal.getTime()).equals(MyUtil.sdfYYYYMMDD.format(today.getTime()));
			if (isToday){
				output = "<u>"+output+"</u>";
			}
			// show CHINESE LUNAR term
			String term="";
			if (!defaultLang.equals(MyUtil.PREF_LANG_EN)){
				term = MyCalendarLunar.solar.getSolarTerm(curCal);
				term = term.equals("")?"":"<small><small>"+term+"</small></small>";
			}
			output = output + term;
			// Show Chinese Lunar day on Sunday
			String holiday = MyHoliday.getHolidayRemark(curCal.getTime());
			final boolean isHoliday = ((!holiday.equals("") && !holiday.startsWith("#")) || (curCal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY));
			if (!defaultLang.equals(MyUtil.PREF_LANG_EN)){				
				if (IS_SHOW_LUNAR_DAY_ON_SUNDAY_ONLY){
					if (curCal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
						output = output + "<br><small>("+(new MyCalendarLunar(curDayEvents.getCalendar())).toMMDD()+")</small>";
					}
				} else {
					MyCalendarLunar lunarDay = new MyCalendarLunar(curDayEvents.getCalendar());
////					if (lunarDay.getDay()==1){
////						output = output + "<br><font color=\"blue\"><small>"+lunarDay.toChineseMM()+"</small></font>";
////					} else if (lunarDay.getDay()==15){
////						output = output + "<br><font color=\"blue\"><small>"+lunarDay.toChineseDD()+"</small></font>";
////					} else {
//						output = output + "<br><font color=\"black\"><small><small>"+lunarDay.toChineseDD()+"</small></small></font>";
////					}
						if (lunarDay.getDay()==1){
							output = output + "<br><font color=\"black\"><small><small>"+lunarDay.toChineseMM()+"</small></small></font>";
						} else {
							output = output + "<br><font color=\"black\"><small><small>"+lunarDay.toChineseDD()+"</small></small></font>";
						}
				}
			}
			// show ICON on SELECTED DAY
			if (curCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
				curCal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
				curCal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {
				icon.setVisibility(View.VISIBLE);
			} else {
				icon.setVisibility(View.GONE);
			}
			// set COLOR (darken NON-CURRENT month day) 
			isOtherMonthCell = (curDayEvents.getType()!=MyDayEvents.TYPE_NONE);
			if (displayMonth.get(Calendar.MONTH)==today.get(Calendar.MONTH) &&
				displayMonth.get(Calendar.YEAR)==today.get(Calendar.YEAR) &&
				curCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
				curCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
				curCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
					txtCell.setBackgroundResource(R.color.calendarTodayBkg);					
			} else { 
				if (isOtherMonthCell) {
					txtCell.setBackgroundColor(actContext.getResources().getColor(R.color.calendarOffBkg));
				} else {
					txtCell.setBackgroundColor(actContext.getResources().getColor(R.color.calendarOnBkg));
				}
			}
			if (isOtherMonthCell) {
				if (isHoliday) {
					dayView.setTextColor(actContext.getResources().getColor(R.color.sundayTextInActive));
				} else {
					dayView.setTextColor(actContext.getResources().getColor(R.color.weekdayTextInActive));
				}
			} else {
				if (isHoliday) {
					dayView.setTextColor(actContext.getResources().getColor(R.color.sundayTextActive));
				} else {
					dayView.setTextColor(actContext.getResources().getColor(R.color.weekdayTextActive));
				}				
			}
			

			output = output + MyCalendar.getCounterString(curDayEvents.counter,false) + "<br><small>" + curDayEvents.text + "</small>";
			dayView.setText(Html.fromHtml(output));
			
			//if (!getHolidayText(actContext, curCal).equals("")){
			if (!holiday.equals("") && !holiday.startsWith("#")){
				if (isOtherMonthCell) {
					dayView.setTextColor(actContext.getResources().getColor(R.color.sundayTextInActive));
				} else {
					dayView.setTextColor(actContext.getResources().getColor(R.color.sundayTextActive));
				}
			}			
		}
		return v;
	}
//	static public Calendar getCalendarByTag(Calendar theMonth, String tag){
//		final String prefix = tag.substring(0, 1);
//		final int nbrOfMonthMove = prefix.equals("p")?-1:(prefix.equals("s")?1:0);
//		final String curDay=nbrOfMonthMove==0?tag:tag.substring(1);
//		final int setToDay = Integer.parseInt(curDay);
//		return getCalendar(theMonth, nbrOfMonthMove, setToDay);
//	}
	static public Calendar getCalendar(Calendar theMonth, int monthMove, int daySet){
		Calendar newMonth = (Calendar) theMonth.clone();
		if (monthMove<0){ // Prev Month
			newMonth.set(Calendar.DAY_OF_MONTH, 1);
			newMonth.add(Calendar.DATE, -1);			
		} else  if (monthMove>0) { // Next Month
			newMonth.set(Calendar.DAY_OF_MONTH, 27);
			newMonth.add(Calendar.DATE, 5);
			newMonth.set(Calendar.DAY_OF_MONTH, 1);			
		} 
		if (daySet>0){
			newMonth.set(Calendar.DAY_OF_MONTH, daySet);
		}
		return newMonth;
	}
	static public MyDayEvents [] getDaysArray(Calendar targetMth){
		MyDayEvents [] returnDays;		
		// Get Last Month Information
		Calendar lastMth = MyCalendar.getLastMonthEndDay(targetMth);
		Calendar nextMth = MyCalendar.getNextMonthFirstDay(targetMth);
		Calendar thisMth = (Calendar) targetMth.clone();
		thisMth.set(Calendar.DAY_OF_MONTH, 1);

		int nbrOfDaysInThisMth = thisMth.getActualMaximum(Calendar.DAY_OF_MONTH);
		int thisMthFirstDayOfWeek = thisMth.get(Calendar.DAY_OF_WEEK);// Sunday 1,Monday 2
	
		// Calculate extra days before/after current display month
		int extraPrefixDays = thisMthFirstDayOfWeek-1;// Sunday 1, Monday 2....
		int nbrOfDaysInMonthIncludingPrefix= nbrOfDaysInThisMth + extraPrefixDays;
		final double nbrOfWeeks = Math.ceil(nbrOfDaysInMonthIncludingPrefix / 7.0);
		final int extraSuffixDays = (int) ((nbrOfWeeks * 7) - nbrOfDaysInMonthIncludingPrefix);

		// figure size of the array based on extra days
		returnDays = new MyDayEvents[extraPrefixDays+nbrOfDaysInThisMth+extraSuffixDays];		
		for (int j=extraPrefixDays;j>0;j--){
			returnDays[j-1] = new MyDayEvents(MyDayEvents.TYPE_PREV_MTH, lastMth);						
			lastMth.add(Calendar.DATE, -1);
		}
		
		// populate days		
		for (int i = extraPrefixDays; i < returnDays.length; i++) {
			if (i < returnDays.length - extraSuffixDays) {
				returnDays[i] = new MyDayEvents(MyDayEvents.TYPE_NONE, thisMth);
				thisMth.add(Calendar.DATE, 1);				
			} else {
				returnDays[i] = new MyDayEvents(MyDayEvents.TYPE_NEXT_MTH, nextMth);
				nextMth.add(Calendar.DATE, 1);
			}
		}
		return returnDays;
	}
	public void refreshDays(Calendar newCalendar) {
		displayMonth = (Calendar) newCalendar.clone();
		if (this.gridType == GRID_TYPE_TITLE) {
			daysTitles = new MyDayEvents[7];
			int dayName = FIRST_DAY_OF_WEEK;// 1
			for (int i = 0; i < 7; i++) {
				daysTitles[i] = new MyDayEvents(MyDayEvents.TYPE_TITLE, getShortDayName(dayName));				
				dayName++;
				if (dayName > 6) {
					dayName = 0;
				}
			}
			return;
		}
		// clear items
		items.clear();
		daysEvents = MyCalendar.getEachDayEvents(actContext, displayMonth,false);
//		daysEvents = getDaysArray(displayMonth);
//		MyApp.log("?", ".......NbrOfEvents:"+daysEvents.length);
	}

	public static String getFullDayName(int day) {
		Calendar c = Calendar.getInstance();
		c.set(2013, 0, 5, 0, 0, 0); // 2013.1.5 is Saturday
		c.add(Calendar.DAY_OF_MONTH, day);
		return String.format("%tA", c);
	}

	public static String getShortDayName(int day) {
		Calendar c = Calendar.getInstance();
		c.set(2013, 0, 5, 0, 0, 0); // 2013.1.5 is Saturday
		c.add(Calendar.DAY_OF_MONTH, day);
		return String.format("%ta", c);
	}

}