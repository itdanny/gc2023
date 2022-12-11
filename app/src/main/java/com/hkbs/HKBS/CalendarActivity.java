package com.hkbs.HKBS;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.hkbs.HKBS.arkCalendar.MyCalendar.MyDayEvents;
import com.hkbs.HKBS.arkUtil.MyGestureListener;
import com.hkbs.HKBS.arkUtil.MyRunLater;
import com.hkbs.HKBS.arkUtil.MyUtil;

import java.text.ParseException;
import java.util.Calendar;

import androidx.core.text.HtmlCompat;

public class CalendarActivity extends MyActivity {
    final static private boolean DEBUG = true;

    public Calendar monthToShow;
    private TextView calendarMonth;
    private TextView calendarTitle;
    private ImageView calendarIcon;
    private GestureDetector gDetector;
    private GridView[] gridViewList = new GridView[2];
    private CalendarAdapter[] gridAdapterList = new CalendarAdapter[2];
    private int gridViewIndex;
    private ViewAnimator viewAnimator;

    private Calendar lastClickTimeStamp = Calendar.getInstance();
    private String defaultDate;
    private boolean isCallFromSelectAction = false;
    private Intent callerIntent;

    public CalendarActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_calendar);

        callerIntent = getIntent();
        monthToShow = Calendar.getInstance();

        String mode = callerIntent.getStringExtra(MyUtil.EXTRA_TYPE);
        isCallFromSelectAction = (mode != null && mode.equals(MyUtil.EXTRA_TYPE_SELECT));
        isCallFromSelectAction = true;//<----FORCE TO TRUE; ONLY PURPOSE
        defaultDate = callerIntent.getStringExtra(MyUtil.EXTRA_DEFAULT_DATE);

        if (defaultDate != null) {
            try {
                if (defaultDate.length() > MyUtil.sdfYYYYMMDD.toPattern().length()) {
                    monthToShow.setTime(MyUtil.sdfYYYYMMDDHHMM.parse(defaultDate));
                } else {
                    monthToShow.setTime(MyUtil.sdfYYYYMMDD.parse(defaultDate));
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        setResult(RESULT_CANCELED);
        initSetup();
    }

    private void initSetup() {
        calendarIcon = (ImageView) findViewById(R.id.xmlCalendarIcon);
        calendarTitle = (TextView) findViewById(R.id.xmlCalendarTitle);
        gDetector = new GestureDetector(CalendarActivity.this, new MyGestureListener(new MyGestureListener.Callback() {
            @Override
            public boolean onLeft() {
                gotoPrevMth();
                return true;
            }

            @Override
            public boolean onRight() {
                gotoNextMth();
                return true;
            }

            @Override
            public boolean onClick(MotionEvent e) {
                GridView grid = gridViewList[gridViewIndex];
                if (grid == null) return false;
                int firstPos = grid.getFirstVisiblePosition();
                View view = grid.getChildAt(0);
                if (view == null) return false;
                int invisibleHeight = (firstPos / 7) * view.getHeight();
                int newX = (int) (e.getX());
                int newY = (int) (e.getY() - invisibleHeight);
                final int pos = grid.pointToPosition(newX, newY);
                if (pos >= 0) {
                    doOnItemClick(grid.getChildAt(pos));
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onLongPress(MotionEvent e) {
                return false;
            }
        }));

        final TextView prevYear = (TextView) findViewById(R.id.xmlPrevYear);
        prevYear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPrevYear();
            }
        });

        final TextView today = (TextView) findViewById(R.id.xmlToday);
        //today.setText(MyUtil.sdfMMM.format(Calendar.getInstance().getTime()));
        today.setText(R.string.WordToday);
        today.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoToday();
            }
        });

        final TextView prevMth = (TextView) findViewById(R.id.xmlPrevMth);
        prevMth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPrevMth(0);
            }
        });

        final TextView nextYear = (TextView) findViewById(R.id.xmlNextYear);
        nextYear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextYear();
            }
        });

        final TextView nextMth = (TextView) findViewById(R.id.xmlNextMth);
        nextMth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextMth(0);
            }
        });

        viewAnimator = findViewById(R.id.xmlReminderViewAnimator);
        GridView gridviewTitle = findViewById(R.id.xmlGridViewTitle);
        gridviewTitle.setAdapter(new CalendarAdapter(CalendarActivity.this, monthToShow, CalendarAdapter.GRID_TYPE_TITLE));
        calendarMonth = findViewById(R.id.xmlCalendarMonth);
        calendarMonth.setText(MyUtil.sdfYYYYMM.format(monthToShow.getTime()));
        calendarMonth.setOnClickListener(v -> MyUtil.popDate(CalendarActivity.this, MyDailyBread.getDayString(monthToShow), dateListener));
        if (isCallFromSelectAction) {
            Toast.makeText(CalendarActivity.this, R.string.MsgCalendarChangeEventDate, Toast.LENGTH_LONG).show();
        }
        new MyRunLater(() -> {
            CalendarActivity.this.initGridView(0);
            calendarIcon.setVisibility(View.VISIBLE);
            if (!isCallFromSelectAction) calendarTitle.setPressed(true);
            CalendarActivity.this.setEventPanel(monthToShow);
            new MyRunLater(() -> {
                initGridView(1);
                if (!isCallFromSelectAction) {
                    new MyRunLater(MyUtil.DELAY_MILLIS_HIDE_ZOOM, new MyRunLater.Callback() {
                        @Override
                        public void ready() {
                            calendarTitle.setPressed(false);
                        }
                    });
                }
            });
        }
        );
    }

    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar checkDate = Calendar.getInstance();
            checkDate.set(year, monthOfYear, dayOfMonth);
            if (isWithinRange(checkDate)) {
                if (changeIndexAndSetSelectedDate()) {
                    int moveStyle = checkDate.compareTo(monthToShow);
                    monthToShow = (Calendar) checkDate.clone();
                    refreshCalendar(monthToShow);
                    if (moveStyle > 0) {
                        MyGestureListener.slideInFromRight(CalendarActivity.this, viewAnimator);
                    } else {
                        MyGestureListener.slideInFromLeft(CalendarActivity.this, viewAnimator);
                    }
                }
            }
        }
    };

    private void initGridView(int index) {
        gridAdapterList[index] = new CalendarAdapter(CalendarActivity.this, monthToShow, CalendarAdapter.GRID_TYPE_CELL);
        gridAdapterList[index].setSelectedDate(monthToShow);
        gridViewList[index] = (GridView) viewAnimator.getChildAt(index);
        gridViewList[index].setTag(index);
        gridViewList[index].setVerticalScrollBarEnabled(false);
        gridViewList[index].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gDetector.onTouchEvent(event);
                return false;
            }
        });
        gridViewList[index].setAdapter(gridAdapterList[index]);

    }

    private void gotoToday() {
        monthToShow = Calendar.getInstance();
        //eventDate.setText(sdfDate.format(month.getTime()));
        //gridAdapterList[gridViewIndex].setSelectedDate(month);
        refreshCalendar(monthToShow);
        setEventPanel(monthToShow);
    }

    private boolean changeIndexAndSetSelectedDate() {
        //gridViewList[gridViewIndex].setSelector(R.drawable.gridview_selector_calendar);
        try {
            Calendar oldSelectedDate = gridAdapterList[gridViewIndex].selectedDate;
            gridViewIndex = gridViewIndex == 0 ? 1 : 0;
            gridAdapterList[gridViewIndex].setSelectedDate(oldSelectedDate);
            //gridViewList[gridViewIndex].setSelector(R.drawable.gridview_selector_calendar);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void gotoPrevYear() {
        Calendar checkDate = (Calendar) monthToShow.clone();
        checkDate.add(Calendar.YEAR, -1);
        if (isWithinRange(checkDate)) {
            if (changeIndexAndSetSelectedDate()) {
                monthToShow.add(Calendar.YEAR, -1);
                refreshCalendar(monthToShow);
                MyGestureListener.slideInFromRight(CalendarActivity.this, viewAnimator);
            }
        }
    }

    private boolean isWithinRange(Calendar checkDate) {
        Calendar validFrDate = Calendar.getInstance();
        validFrDate.set(1910, 0, 1, 0, 0, 0); // Month is one less
        Calendar validToDate = Calendar.getInstance();
        validToDate.set(2030, 11, 31, 59, 59, 59);
//		MyUtil.log(TAG, MyDailyBread.getDayString(checkDate)+" Vs "+MyDailyBread.getDayString(validToDate)+" "+checkDate.compareTo(validToDate));
//		MyUtil.log(TAG, MyDailyBread.getDayString(checkDate)+" Vs "+MyDailyBread.getDayString(validFrDate)+" "+checkDate.compareTo(validFrDate));
        if (checkDate.compareTo(validToDate) > 0 ||
                checkDate.compareTo(validFrDate) < 0) {
            MyDailyBread.showOutOfBounds(this, checkDate);
            return false;
        } else {
            return true;
        }
    }

    private void gotoNextYear() {
        Calendar checkDate = (Calendar) monthToShow.clone();
        checkDate.add(Calendar.YEAR, 1);
        if (isWithinRange(checkDate)) {
            if (changeIndexAndSetSelectedDate()) {
                monthToShow.add(Calendar.YEAR, 1);
                refreshCalendar(monthToShow);
                MyGestureListener.slideInFromLeft(CalendarActivity.this, viewAnimator);
            }
        }
    }

    private void gotoPrevMth() {
        gotoPrevMth(0);
    }

    static public Calendar getPrevMonth(Calendar oldMonth, int newDay) {
        Calendar newMonth = (Calendar) oldMonth.clone();
        newMonth.set(Calendar.DAY_OF_MONTH, 1); // Ensure day is 1; Otherwise, calculation will be wrong if 30 on Feb
        if ((int) newMonth.get(Calendar.MONTH) == (int) newMonth.getActualMinimum(Calendar.MONTH)) {
            newMonth.set((newMonth.get(Calendar.YEAR) - 1), newMonth.getActualMaximum(Calendar.MONTH), 1);
        } else {
            newMonth.set(Calendar.MONTH, newMonth.get(Calendar.MONTH) - 1);
        }
        if (newDay > 0) {
            newMonth.set(Calendar.DAY_OF_MONTH, newDay);
//			gridAdapterList[gridViewIndex].setSelectedDate(month);
//			onSelectedDateChanged();
        }
        return newMonth;
    }

    private void gotoPrevMth(int newDay) {
        Calendar checkDate = (Calendar) monthToShow.clone();
        checkDate = getPrevMonth(checkDate, newDay);
        if (isWithinRange(checkDate)) {
            if (changeIndexAndSetSelectedDate()) {
                monthToShow = getPrevMonth(monthToShow, newDay);
                refreshCalendar(monthToShow);
                MyGestureListener.slideInFromRight(CalendarActivity.this, viewAnimator);
            }
        }
    }

    private void gotoNextMth() {
        gotoNextMth(0);
    }

    static public Calendar getNextMonth(Calendar oldMonth, int newDay) {
        Calendar newMonth = (Calendar) oldMonth.clone();
        newMonth.set(Calendar.DAY_OF_MONTH, 1); // Ensure day is 1; Otherwise, calculation will be wrong if 30 on Feb
        if ((int) newMonth.get(Calendar.MONTH) == (int) newMonth.getActualMaximum(Calendar.MONTH)) {
            newMonth.set((newMonth.get(Calendar.YEAR) + 1), newMonth.getActualMinimum(Calendar.MONTH), 1);
        } else {
            newMonth.set(Calendar.MONTH, newMonth.get(Calendar.MONTH) + 1);
        }
        if (newDay > 0) {
            newMonth.set(Calendar.DAY_OF_MONTH, newDay);
//			gridAdapterList[gridViewIndex].setSelectedDate(month);
//			onSelectedDateChanged();
        }
        return newMonth;
    }

    private void gotoNextMth(int newDay) {
        Calendar checkDate = (Calendar) monthToShow.clone();
        checkDate = getNextMonth(checkDate, newDay);
//		MyUtil.log(TAG, "checkDate:"+MyDailyBread.getDayString(checkDate));
        if (isWithinRange(checkDate)) {
            if (changeIndexAndSetSelectedDate()) {
                monthToShow = getNextMonth(monthToShow, newDay);
                refreshCalendar(monthToShow);
                MyGestureListener.slideInFromLeft(CalendarActivity.this, viewAnimator);
            }
        }
    }

    public void refreshCalendar(Calendar newCalendar) {
        gridAdapterList[gridViewIndex].refreshDays(newCalendar);
        gridAdapterList[gridViewIndex].notifyDataSetChanged();
        calendarMonth.setText(MyUtil.sdfYYYYMM.format(monthToShow.getTime()));
        setEventPanel(null);
    }
//	static private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);

    private void setEventPanel(Calendar curDay) {
        if (curDay == null) {
            calendarTitle.setVisibility(View.GONE);
            calendarTitle.setText("");
            return;
        }
        calendarTitle.setVisibility(View.VISIBLE);
//    	calendarTitle.setText(Html.fromHtml(MyUtil.sdfMMDD.format(curDay.getTime())+" "+
//				CalendarAdapter.getHolidayText(CalendarActivity.this, curDay)));
        String holidayText = MyHoliday.getHolidayRemark(curDay.getTime());
        if (holidayText.startsWith("#")) {
            holidayText = holidayText.substring(1);
        }
        calendarTitle.setText(HtmlCompat.fromHtml(MyUtil.sdfMMDD.format(curDay.getTime()) + " " +
                holidayText, HtmlCompat.FROM_HTML_MODE_LEGACY));

    }

    //	@Override
//	public void onResume(){
//		super.onResume();		
//		if (MyUtil.autoGoKey==MyUtil.AUTO_GO_EVENT){
//			MyUtil.autoGoKey=MyUtil.AUTO_GO_NONE;
//			final int msgID = MyUtil.getPrefInt(MyUtil.PREF_NOTEPAD_MSGID, -1);
//			Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
//			intent.putExtra(MyUtil.EXTRA_TYPE, MyUtil.EXTRA_TYPE_EDIT);
//			intent.putExtra(MyUtil.EXTRA_KEY,msgID);
//			startActivity(intent);
//			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);			
//		}
//	}
    private void refreshByDate(Calendar cal) {
        monthToShow = (Calendar) cal.clone();
        gridAdapterList[gridViewIndex].setSelectedDate(monthToShow);
        refreshCalendar(monthToShow);
        setEventPanel(monthToShow);
    }

    private Calendar oldSelectedDate = null;

    private void doOnItemClick(View v) {
        if (v == null) return;
        TextView date = (TextView) v.findViewById(R.id.xmlDay);
        if (date instanceof TextView) {
            final int index = Integer.parseInt(v.getTag(R.id.TAG_POSITION).toString());
            final MyDayEvents dayEvents = gridAdapterList[gridViewIndex].daysEvents[index];
            gridAdapterList[gridViewIndex].setSelectedDate(dayEvents.getCalendar());
            new MyRunLater(MyUtil.DELAY_MILLIS_SELECTION, new MyRunLater.Callback() {
                @Override
                public void ready() {
                    setEventPanel(dayEvents.getCalendar());
                }
            });
            if (oldSelectedDate != null && oldSelectedDate.compareTo(dayEvents.getCalendar()) == 0) { // Click TWICE
                Long diff = Calendar.getInstance().getTimeInMillis() - lastClickTimeStamp.getTimeInMillis();
                if (DEBUG) MyUtil.log("?", "TimeDifference:" + diff);
                if (diff <= 1500) {
                    if (isCallFromSelectAction) {
                        MyUtil.vibrate(CalendarActivity.this);
                        gridAdapterList[gridViewIndex].setSelectedDate(dayEvents.getCalendar());
                        onSelect(dayEvents.getCalendar());
                    }
                }
            }
            oldSelectedDate = dayEvents.getCalendar();
            lastClickTimeStamp = Calendar.getInstance();
        }
    }

    private void onSelect(Calendar date) {
        callerIntent.putExtra(MyUtil.EXTRA_SELECT_MILLSEC, date.getTimeInMillis());
        setResult(RESULT_OK, callerIntent);
        finish();
    }


}
