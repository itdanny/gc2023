package com.hkbs.HKBS;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hkbs.HKBS.arkUtil.MyGestureListener;
import com.hkbs.HKBS.arkUtil.MyUtil;
import com.hkbs.HKBS.util.SystemUiHider;

import org.arkist.share.AxAlarm;
import org.arkist.share.AxImageView;
import org.arkist.share.AxTextView;
import org.arkist.share.AxTools;

import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class CMain extends MyActivity {
    final static public boolean IS_2016_VERSION = true;
    final static public boolean is_2016DayShown(){
        if (mDisplayDay==null){
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.YEAR)>=2016;
        }
        int curYear = mDisplayDay.get(Calendar.YEAR);
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.YEAR)>=2016;
        return curYear>=2016;
    }
//    final static private boolean IS_2015_OR_LATER = true;
    final static public boolean DEBUG = true;

    final static private String TAG = CMain.class.getSimpleName();

    final static private int CALL_FROM_EXTERNAL_APP = -999;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

//    static public int mCalendarYear = 2015;
//    static private float _scaleDensity = 0;
    static private Calendar mDisplayDay;
    static public String mGoldVerse;
    //	private View mContentsView;
    private View mControlsView;
    private View mLeftRightPanel;
    private View mTitleView;
    static public String mScreenType = "";

//    private LinearLayout page1;
//    private LinearLayout page2;
    private Handler handler;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_W:
            case KeyEvent.KEYCODE_DPAD_UP:
                gotoPriorMonth();
                return true;
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                gotoPrevDay();
                return true;
            case KeyEvent.KEYCODE_X:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                gotoNextMonth();
                return true;
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                gotoNextDay();
                return true;
            case KeyEvent.KEYCODE_S:
                onClickToday(this);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private GestureDetector mGesture = new GestureDetector(getBaseContext(), new MyGestureListener(new MyGestureListener.Callback() {
        @Override
        public boolean onClick(MotionEvent e) { // !!! SetClicable to TRUE !!!
            if (DEBUG) MyUtil.log(TAG, "onClick day="+mDisplayDay.get(Calendar.DAY_OF_MONTH));
            setControlsVisibility(!isTitleShown);
            return true;
        }
        @Override public boolean onLongPress(MotionEvent e) {
            return false;
        }
        @Override public boolean onRight() {
            gotoNextDay();
            return true;
        }
        @Override public boolean onLeft() {
            gotoPrevDay();
            return true;
        }
    }));
    private View.OnTouchListener mViewOnTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGesture.onTouchEvent(event);
            return false;
        }
    };

    //private ViewAnimator mViewAnimator;
    private boolean isTitleShown = true;
    //private int mViewIndex = 1;
    private CustomViewPager mPager;
    private CustomViewAdapter mAdapter;
    public MyDailyBread mDailyBread;
    private View mRootView;

    private class CustomViewAdapter extends FragmentStatePagerAdapter {
        CMain mCMain;
        MyDailyBread mDailyBread;
        public CustomViewAdapter(FragmentManager fm, CMain cmain) {
            super(fm);
            mCMain=cmain;
            mDailyBread=cmain.mDailyBread;
        }

        @Override
        public Fragment getItem(int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis());
            calendar.add(Calendar.DAY_OF_MONTH, position);
            return DailyFragment.getInstance(CMain.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }
        @Override
        public int getCount() {
            return (int) mCMain.mDailyBread.getNbrOfValidDays();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        AxTools.init(CMain.this);
//        scaleDensity(getApplicationContext());
        MyUtil.initMyUtil(this);
        MyUtil.log(TAG, "StartApp3..............");

//		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		final List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
//	    for (int i = 0; i < recentTasks.size(); i++){
//	        MyUtil.log(TAG, "Application executed : " +recentTasks.get(i).baseActivity.toShortString()+ "\t\t ID: "+recentTasks.get(i).id+"");         
//	    }
//		
        setContentView(R.layout.activity_cmain);

        mRootView = (View) findViewById(R.id.xmlRoot);
        mRootView.setOnTouchListener(mViewOnTouch);

        AxImageView clickLeftView = (AxImageView) findViewById(R.id.xmlMainClickLeft);
        clickLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delayedHide();
                gotoPrevDay();
            }
        });
        AxImageView clickRightView = (AxImageView) findViewById(R.id.xmlMainClickRight);
        clickRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delayedHide();
                gotoNextDay();
            }
        });

        mDisplayDay = Calendar.getInstance();
//        mViewAnimator = (ViewAnimator) findViewById(R.id.xmlMainContents);

        mControlsView = findViewById(R.id.xmlMainControls);
        mLeftRightPanel = findViewById(R.id.xmlMainClickPanel);
        mTitleView = findViewById(R.id.xmlMainTitle);
        mDailyBread = MyDailyBread.getInstance(CMain.this);

        onCreateSetButtons();

        AxAlarm.setDailyAlarm(CMain.this, AxAlarm.MODE.SET_DEFAULT, 9, 0);
        AxAlarm.setDailyOnDateChange(CMain.this);

        CWidgetNormal.broadcastMe(CMain.this);

        mPager = (CustomViewPager) findViewById(R.id.pager);
        mAdapter = new CustomViewAdapter(getSupportFragmentManager(), CMain.this);
        mPager.setAdapter(mAdapter);
        mPager.callBack = new CustomViewPager.CallBack() {
            @Override
            public void clicked(MotionEvent motionEvent) {
                //onClickItem(motionEvent);
                if (DEBUG) MyUtil.log(TAG, "onClicked");
                setControlsVisibility(!isTitleShown);
                //mRootView.performClick();
            }
        };
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            boolean lastPageChange = false;
            boolean fistPageChange = false;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
                int lastIdx = mAdapter.getCount() - 1;
                if (lastPageChange && position == lastIdx){
                    Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
                } else if (fistPageChange && position==0){
                    Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onPageSelected(int i) {
                mDisplayDay = (Calendar) mDailyBread.getValidFrDate().clone();
                mDisplayDay.add(Calendar.DAY_OF_MONTH, i);
                Log.i(TAG, "onPageSelected day=" + mDisplayDay.get(Calendar.DAY_OF_MONTH));
                onRefreshPage(mDisplayDay, false);
                Log.i(TAG, "onPageSelected day=" + mDisplayDay.get(Calendar.DAY_OF_MONTH));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                int lastIdx = mAdapter.getCount() - 1;
                int curItem = mPager.getCurrentItem();
                if(curItem==lastIdx && state==ViewPager.SCROLL_STATE_DRAGGING)   lastPageChange = true;
                else lastPageChange = false;
                if(curItem==0 && state==ViewPager.SCROLL_STATE_DRAGGING)   fistPageChange = true;
                else fistPageChange = false;
                if (state==ViewPager.SCROLL_STATE_IDLE) {
                    mPager.isScrolling = false;
                } else {
                    mPager.isScrolling = true;
                }
            }
        });

        MyUtil.log(TAG, "StartApp3..............End");

        AxTools.runFollow(new Runnable() {
            @Override
            public void run() {
                onClickToday(CMain.this);
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void setControlsVisibility(boolean isVisible) {
        if (isTitleShown == isVisible) {
            return;
        }
        if (this.isFinishing()) return;
        if (mControlsView == null) mControlsView = findViewById(R.id.xmlMainControls);
        if (mTitleView == null) mTitleView = findViewById(R.id.xmlMainTitle);
        int mControlsHeight = 0;
        int mTitleHeight = 0;
        int mShortAnimTime = 0;
        isTitleShown = isVisible;
        AxTextView mainBtnPlan = (AxTextView) findViewById(R.id.mainBtnPlan);
        if (mainBtnPlan != null) {
            Intent intent = CMain.this.getPackageManager().getLaunchIntentForPackage("org.arkist.cnote");
            if (intent != null) { // Exist
                mainBtnPlan.setText("聖經行事曆");
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            // If the ViewPropertyAnimator API is available
            // (Honeycomb MR2 and later), use it to animate the
            // in-layout UI controls at the bottom of the
            // screen.
            if (mControlsHeight == 0) {
                mControlsHeight = mControlsView.getHeight();
            }
            if (mShortAnimTime == 0) {
                mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            }
            mControlsView.animate().translationY(isVisible ? 0 : mControlsHeight).setDuration(mShortAnimTime);

            if (mTitleHeight == 0) {
                mTitleHeight = mTitleView.getHeight();
            }
            if (mShortAnimTime == 0) {
                mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            }
            mTitleView.animate().translationY(isVisible ? 0 : -mTitleHeight).setDuration(mShortAnimTime);

        } else {
            // If the ViewPropertyAnimator APIs aren't
            // available, simply show or hide the in-layout UI
            // controls.
            mControlsView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            mTitleView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        mLeftRightPanel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (isVisible) {
            // Schedule a hide().
            delayedHide();
        }
    }

    //	private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
//		@Override
//		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//			Calendar checkDate = Calendar.getInstance();
//			checkDate.set(year, monthOfYear, dayOfMonth);
//			if (isWithinRange(checkDate)){
//				mDisplayDay = (Calendar) checkDate.clone();
//				onRefreshPage(mViewIndex);
//			}
//		}
//	};
    private Bitmap screenShot;

    private void saveScreenShot() {
        // Get bitmap
        View contentView = this.findViewById(android.R.id.content);

        int controlVisibility = mControlsView.getVisibility();
        mControlsView.setVisibility(View.GONE);
        int titleVisibility = mTitleView.getVisibility();
        mTitleView.setVisibility(View.GONE);
        View leftView = findViewById(R.id.xmlMainClickLeft);
        int leftVisibility = 0;
        if (leftView != null) {
            leftView.setVisibility(View.GONE);
            leftVisibility = leftView.getVisibility();
        }
        View rightView = findViewById(R.id.xmlMainClickRight);
        int rightVisibility = 0;
        if (rightView != null) {
            rightView.setVisibility(View.GONE);
            rightVisibility = rightView.getVisibility();
        }

        contentView.setDrawingCacheEnabled(true);
        //contentView.buildDrawingCache(true);
        screenShot = contentView.getDrawingCache();
        if (screenShot == null) {
            screenShot = takeSnapshot(contentView);
            if (screenShot == null) {
                screenShot = loadBitmapFromView(contentView);
            }
        }
        //contentView.setDrawingCacheEnabled(false);

        mControlsView.setVisibility(controlVisibility);
        mTitleView.setVisibility(titleVisibility);
        if (leftView != null) leftView.setVisibility(leftVisibility);
        if (rightView != null) rightView.setVisibility(rightVisibility);

        // Save bitmap
//		File imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
//	    FileOutputStream fos;
//	    try {
//	        fos = new FileOutputStream(imagePath);
//	        screenShot.compress(CompressFormat.PNG, 100, fos);
//	        fos.flush();
//	        fos.close();
//	    } catch (FileNotFoundException e) {
//	        Log.e("GREC", e.getMessage(), e);
//	    } catch (IOException e) {adb
//	        Log.e("GREC", e.getMessage(), e);
//	    }
    }

    private Bitmap bitmap;

    private Bitmap takeSnapshot(View v) {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
                                          /*c.drawBitmap(v.getDrawingCache(), 0, 0, null);
                                          v.destroyDrawingCache();*/
        v.draw(c);
        return bitmap;
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    private void onCreateSetButtons() {
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        AxTextView mainBtnCal = (AxTextView) findViewById(R.id.mainBtnCalendar);
        //mainBtnCal.setOnTouchListener(mDelayHideTouchListener);
        mainBtnCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSelectCalendar(CMain.this);
            }
        });

        AxTextView mainBtnPlan = (AxTextView) findViewById(R.id.mainBtnPlan);
        //mainBtnPlan.setOnTouchListener(mDelayHideTouchListener);
        mainBtnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickViewOthers(CMain.this);
            }
        });

        AxTextView mainBtnBible = (AxTextView) findViewById(R.id.mainBtnBible);
        if (CMain.is_2016DayShown()) {
            mainBtnBible.setText(R.string.main_support);
        } else {
            mainBtnBible.setText(R.string.main_bible);
        }
        //mainBtnBible.setOnTouchListener(mDelayHideTouchListener);
        mainBtnBible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickViewBible(CMain.this);
            }
        });

        AxTextView mainBtnShare = (AxTextView) findViewById(R.id.mainBtnShare);
        //mainBtnShare.setOnTouchListener(mDelayHideTouchListener);
        mainBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickShare(CMain.this);
            }
        });

        AxTextView mainBtnAlarm = (AxTextView) findViewById(R.id.mainBtnAlarm);
        //mainBtnAlarm.setOnTouchListener(mDelayHideTouchListener);
        mainBtnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAlarm(CMain.this);
            }
        });

        AxTextView mainBtnAbout = (AxTextView) findViewById(R.id.mainBtnAbout);
        //mainBtnAbout.setOnTouchListener(mDelayHideTouchListener);
        mainBtnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAbout(CMain.this);
            }
        });

        AxTextView mainBtnCopy = (AxTextView) findViewById(R.id.mainBtnCopy);
        //mainBtnSupport.setOnTouchListener(mDelayHideTouchListener);
        mainBtnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCopy(CMain.this);
            }
        });

        AxTextView mainBtnToday = (AxTextView) findViewById(R.id.mainBtnToday);
        //mainBtnToday.setOnTouchListener(mDelayHideTouchListener);
        mainBtnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickToday(CMain.this);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            delayedHide();
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            setControlsVisibility(false);
            //mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    public void delayedHide() {
        int delayMillis = AUTO_HIDE_DELAY_MILLIS;
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    public void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    AlertDialog alert;
    DialogInterface.OnClickListener shareListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // Text
                    shareText(CMain.this);
                    break;
                case 1: // IMage
                    shareImage(CMain.this);
                    break;
                default:
                    break;
            }
        }
    };
    DialogInterface.OnClickListener copyListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // Text
                    copyText(CMain.this);
                    break;
                case 1: // Image
                    copyImage(CMain.this);
                    break;
                default:
                    break;
            }
        }
    };

    private void popSelectTextImage(Context context, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("請選擇分享:");
        String[] options = {"文字", "圖像"};
        builder.setItems(options, listener);
        builder.setNegativeButton("取消", null);
        alert = builder.create();
        alert.show();
    }

    private void onClickCopy(Context context) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//	    builder.setTitle("請選擇分享:");
//	    String[] options = { "文字", "圖像"};
//	    builder.setItems(options, copyListener);
//	    builder.setNegativeButton("取消", null);
//	    alert = builder.create();	    
//	    alert.show();		

        copyText(context);
    }

    private void onClickShare(Context context) {
        saveScreenShot();
        popSelectTextImage(context, shareListener);
    }

    private void copyText(Context context) {
        int curYear = mDisplayDay.get(Calendar.YEAR);
        int curMonth = mDisplayDay.get(Calendar.MONTH);
        int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
        ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
        final String verse = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ") +
                "[" + cv.getAsString(MyDailyBread.wGoldVerse) + " RCUV]";
        MyClipboardManager mgr = new MyClipboardManager();
        mgr.copyToClipboard(CMain.this, verse);
        Toast.makeText(getApplicationContext(), "已把金句放到剪貼薄", Toast.LENGTH_SHORT).show();
        //onClickSupport(CMain.this);
    }

    private void copyImage(Context context) {
        // Some Android device not support !!
    }

    static public Bitmap getThumbnail(Bitmap target, int expectedSize, boolean isByHeight) { // ignore isByHeight
        int width = target.getWidth();
        int height = target.getHeight();
        if (width <= expectedSize && height <= expectedSize) return target;
        Matrix matrix = new Matrix();
        float scale = ((float) expectedSize) / (height > width ? height : width);
        matrix.postScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(target, 0, 0, width, height, matrix, true);
        return result;
    }

    private void shareImage(Context context) {
        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, "title");
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        //Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,values);
        String fileName = "pic_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), fileName));
        //Uri uri = Uri.fromFile(new File(getFilesDir(),fileName));
        OutputStream outstream;
        int tryCounter = 1;
        try {
            outstream = getContentResolver().openOutputStream(uri);
            boolean anyError = false;
            try {
                screenShot.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            } catch (Exception e) {
                anyError = true;
            }
            if (anyError) {
                outstream.close();
                outstream = getContentResolver().openOutputStream(uri);
                tryCounter++;
                Bitmap smallBitmap = getThumbnail(screenShot, 640, true);
                if (!smallBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream)) {
                    tryCounter++;
                    smallBitmap = getThumbnail(screenShot, 320, true);
                    smallBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                }
            }
            outstream.flush();
            outstream.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error:" + tryCounter + " " + e.toString(), Toast.LENGTH_LONG).show();
            //System.err.println(e.toString());
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(android.content.Intent.EXTRA_SUBJECT, "「" +
                (IS_2016_VERSION ?context.getString(R.string.app_name_2016):context.getString(R.string.app_name)) +
                "」經文分享");
        share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "分享圖像"));

//		Intent share = new Intent(Intent.ACTION_SEND);
//		share.setType("image/jpeg");
//		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/screenshot.png"));
//		startActivity(Intent.createChooser(share, "分享圖像"));
    }

    private void shareText(Context context) {
        int curYear = mDisplayDay.get(Calendar.YEAR);
        int curMonth = mDisplayDay.get(Calendar.MONTH);
        int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
        ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
        final String verse = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ") +
                "[" + cv.getAsString(MyDailyBread.wGoldVerse) +
                (is_2016DayShown()?";和合本]":"；和合本修訂版]");
        MyUtil.trackClick(context, "Share", "M");
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "「" +
                (IS_2016_VERSION ?context.getString(R.string.app_name_2016):context.getString(R.string.app_name)) +
                "」經文分享");//getResources().getString(R.string.app_name)
        intent.putExtra(android.content.Intent.EXTRA_TEXT, verse);
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.app_name)));
    }

    static final public String[] BOOKS_ENG = new String[]{
            "GEN", "EXO", "LEV", "NUM",
            "DEU", "JOS", "JDG", "RUT",
            "1SA", "2SA", "1KI", "2KI",
            "1CH", "2CH", "EZR", "NEH",
            "EST", "JOB", "PSA", "PRO",
            "ECC", "SNG", "ISA", "JER",
            "LAM", "EZK", "DAN", "HOS",
            "JOL", "AMO", "OBA", "JON",
            "MIC", "NAM", "HAB", "ZEP",
            "HAG", "ZEC", "MAL", "MAT",
            "MRK", "LUK", "JHN", "ACT",
            "ROM", "1CO", "2CO", "GAL",
            "EPH", "PHP", "COL", "1TH",
            "2TH", "1TI", "2TI", "TIT",
            "PHM", "HEB", "JAS", "1PE",
            "2PE", "1JN", "2JN", "3JN",
            "JUD", "REV"
    };
//	static final public String [][] BOOKS_ENG = new String [][] {
//		{"1","Genesis","Gen"},
//		{"2","Exodus","Exo"},
//		{"3","Leviticus","Lev"},
//		{"4","Numbers","Num"},
//		{"5","Deuteronomy","Deu"},
//		{"6","Joshua","Jos"},
//		{"7","Judges","Jug"},
//		{"8","Ruth","Rut"},
//		{"9","1 Samuel","1Sa"},
//		{"10","2 Samuel","2Sa"},
//		{"11","1Kings","1Ki"},
//		{"12","2Kings","2Ki"},
//		{"13","1 Chronicles","1Ch"},
//		{"14","2 Chronicles","2Ch"},
//		{"15","Ezra","Ezr"},
//		{"16","Nehemiah","Neh"},
//		{"17","Esther","Est"},
//		{"18","Job","Job"},
//		{"19","Psalms","Psm"},
//		{"20","Proverbs","Pro"},
//		{"21","Ecclesiastes","Ecc"},
//		{"22","Song of Songs","Son"},
//		{"23","Isaiah","Isa"},
//		{"24","Jeremiah","Jer"},
//		{"25","Lamentations","Lam"},
//		{"26","Ezekiel","Eze"},
//		{"27","Daniel","Dan"},
//		{"28","Hosea","Hos"},
//		{"29","Joel","Joe"},
//		{"30","Amos","Amo"},
//		{"31","Obadiah","Oba"},
//		{"32","Jonah","Jon"},
//		{"33","Micah","Mic"},
//		{"34","Nahum","Nah"},
//		{"35","Habakkuk","Hab"},
//		{"36","Zephaniah","Zep"},
//		{"37","Haggai","Hag"},
//		{"38","Zechariah","Zec"},
//		{"39","Malachi","Mal"},
//		{"40","Matthew","Mat"},
//		{"41","Mark","Mak"},
//		{"42","Luke","Luk"},
//		{"43","John","Jhn"},
//		{"44","Acts","Act"},
//		{"45","Romans","Rom"},
//		{"46","1 Corinthians","1Co"},
//		{"47","2 Corinthians","2Co"},
//		{"48","Galatians","Gal"},
//		{"49","Ephesians","Eph"},
//		{"50","Philippians","Phl"},
//		{"51","Colossians","Col"},
//		{"52","1 Thessalonians","1Ts"},
//		{"53","2 Thessalonians","2Ts"},
//		{"54","1 Timothy","1Ti"},
//		{"55","2 Timothy","2Ti"},
//		{"56","Titus","Tit"},
//		{"57","Philemon","Phm"},
//		{"58","Hebrews","Heb"},
//		{"59","James","Jas"},
//		{"60","1 Peter","1Pe"},
//		{"61","2 Peter","2Pe"},
//		{"62","1 John","1Jn"},
//		{"63","2 John","2Jn"},
//		{"64","3 John","3Jn"},
//		{"65","Jude","Jud"},
//		{"66","Revelation","Rev"}
//	};

    static final public String[][] BOOKS_CHT = new String[][]{
            {"1", "創世記", "創"},
            {"2", "出埃及記", "出"},
            {"3", "利未記", "利"},
            {"4", "民數記", "民"},
            {"5", "申命記", "申"},
            {"6", "約書亞記", "書"},
            {"7", "士師記", "士"},
            {"8", "路得記", "得"},
            {"9", "撒母耳記上", "撒上"},
            {"10", "撒母耳記下", "撒下"},
            {"11", "列王紀上", "王上"},
            {"12", "列王紀下", "王下"},
            {"13", "歷代志上", "代上"},
            {"14", "歷代志下", "代下"},
            {"15", "以斯拉記", "拉"},
            {"16", "尼希米記", "尼"},
            {"17", "以斯帖記", "斯"},
            {"18", "約伯記", "伯"},
            {"19", "詩篇", "詩"},
            {"20", "箴言", "箴"},
            {"21", "傳道書", "傳"},
            {"22", "雅歌", "歌"},
            {"23", "以賽亞書", "賽"},
            {"24", "耶利米書", "耶"},
            {"25", "耶利米哀歌", "哀"},
            {"26", "以西結書", "結"},
            {"27", "但以理書", "但"},
            {"28", "何西阿書", "何"},
            {"29", "約珥書", "珥"},
            {"30", "阿摩司書", "摩"},
            {"31", "俄巴底亞書", "俄"},
            {"32", "約拿書", "拿"},
            {"33", "彌迦書", "彌"},
            {"34", "那鴻書", "鴻"},
            {"35", "哈巴谷書", "哈"},
            {"36", "西番雅書", "番"},
            {"37", "哈該書", "該"},
            {"38", "撒迦利亞書", "亞"},
            {"39", "瑪拉基書", "瑪"},
            {"40", "馬太福音", "太"},
            {"41", "馬可福音", "可"},
            {"42", "路加福音", "路"},
            {"43", "約翰福音", "約"},
            {"44", "使徒行傳", "徒"},
            {"45", "羅馬書", "羅"},
            {"46", "哥林多前書", "林前"},
            {"47", "哥林多後書", "林後"},
            {"48", "加拉太書", "加"},
            {"49", "以弗所書", "弗"},
            {"50", "腓立比書", "腓"},
            {"51", "歌羅西書", "西"},
            {"52", "帖撒羅尼迦前書", "帖前"},
            {"53", "帖撒羅尼迦後書", "帖後"},
            {"54", "提摩太前書", "提前"},
            {"55", "提摩太後書", "提後"},
            {"56", "提多書", "多"},
            {"57", "腓利門書", "門"},
            {"58", "希伯來書", "來"},
            {"59", "雅各書", "雅"},
            {"60", "彼得前書", "彼前"},
            {"61", "彼得後書", "彼後"},
            {"62", "約翰一書", "約一"},
            {"62", "約翰二書", "約二"},
            {"62", "約翰三書", "約三"},
            {"65", "猶大書", "猶"},
            {"66", "啟示錄", "啟"}
    };
    static String digits = "0123456789";

    static public Object[] getEBCV(String bcv) {
        int digitFirstPos = 0;
        for (int i = 0; i < bcv.length(); i++) {
            if (digits.contains(bcv.substring(i, i + 1))) {
                digitFirstPos = i;
                break;
            }
        }
        int bookNbr = 0;
        String bookName = bcv.substring(0, digitFirstPos).trim();
        bookName.replace("一", "壹");
        bookName.replace("二", "貳");
        bookName.replace("三", "參");
        String bookAbbrev = "";
        for (int i = 0; i < BOOKS_CHT.length; i++) {
            if (bookName.equalsIgnoreCase(BOOKS_CHT[i][1])) {
                bookAbbrev = BOOKS_CHT[i][2];
                bookNbr = i + 1;
                break;
            }
        }
        if (bookAbbrev.equalsIgnoreCase("")) {
            MyUtil.logError(TAG, "Cannot find:" + bookName);
            return null;
        }
        int digitStopPos = 0;
        for (int i = digitFirstPos + 1; i < bcv.length(); i++) {
            if (digits.contains(bcv.substring(i, i + 1))) {
                continue;
            } else {
                digitStopPos = i;
                break;
            }
        }
        if (digitStopPos == 0) {
            MyUtil.logError(TAG, "Cannot find digitStopPos");
            return null;
        }
        int chapter = Integer.valueOf(bcv.substring(digitFirstPos, digitStopPos));
        int secondDigitStartPos = 0;
        for (int i = digitStopPos; i < bcv.length(); i++) {
            if (digits.contains(bcv.substring(i, i + 1))) {
                secondDigitStartPos = i;
                break;
            }
        }
        int verse = 0;
        if (secondDigitStartPos == 0) {
            // if cannot find then we suppose the first stop digit is for verse, not chapter
            //MyUtil.logError(TAG, "Cannot find secondDigitStartPos");
            //return null;
            verse = chapter;
            chapter = 1;
        } else {
            int digitEndPos = 0;
            for (int i = secondDigitStartPos + 1; i < bcv.length(); i++) {
                if (digits.contains(bcv.substring(i, i + 1))) {
                    continue;
                } else {
                    digitEndPos = i;
                    break;
                }
            }
            if (digitEndPos == 0) {
                digitEndPos = bcv.length();
            }
            verse = Integer.valueOf(bcv.substring(secondDigitStartPos, digitEndPos));
        }
        Object[] eabcv;
        if (CMain.is_2016DayShown()) {
            eabcv = new Object[]{
                    "tc ",
                    bookAbbrev,
                    Integer.valueOf(bookNbr),
                    Integer.valueOf(chapter),
                    Integer.valueOf(verse)
            };
        } else {
            eabcv = new Object[]{
                    "tr ",
                    bookAbbrev,
                    Integer.valueOf(bookNbr),
                    Integer.valueOf(chapter),
                    Integer.valueOf(verse)
            };
        }
        ;
        //String finalBCV = bookAbbrev+" "+chapter+":"+verse;
        return eabcv;//"tr "+finalBCV;
        //return "rc 創 5:2";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (DEBUG) MyUtil.log(TAG, "onActivityResult:" + requestCode + "," + resultCode);
        switch (requestCode) {
            case MyUtil.REQUEST_CALENDAR:
                if (resultCode == RESULT_OK) {
                    long newTime = intent.getLongExtra(MyUtil.EXTRA_SELECT_MILLSEC, 0);
                    if (DEBUG) MyUtil.log(TAG, "CMain:" + newTime);
                    if (newTime != 0) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.setTimeInMillis(newTime);
                        if (isWithinRange(newDate)) {
                            mDisplayDay.setTimeInMillis(newTime);
                        }
                        onRefreshPage(mDisplayDay,false);
                    }
                }
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                break;
            case MyUtil.REQUEST_ABOUT:
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                break;
            case MyUtil.REQUEST_SUPPORT:
                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                break;
        }
    }

    private boolean isWithinRange(Calendar checkDate) {
        if (checkDate.compareTo(mDailyBread.getValidToDate()) > 0 ||
                checkDate.compareTo(mDailyBread.getValidFrDate()) < 0) {
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        cleanAnimation();
//    }

    // For testing only since we want broadcast onReceive event app stopped
//	static private MyBroadcast mBroadcast; 
    @Override
    protected void onResume() {
        super.onResume();
//		if (android.os.Build.VERSION.SDK_INT < 11) {
//			mBroadcast = new MyBroadcast();
//			registerReceiver(mBroadcast, new IntentFilter());
//		}
        MyUtil.log(TAG, "onResume");
//        onClickToday(this);
//        MyUtil.log(TAG, "onResumeAfterOnRefreshPage");
        String defaultCountry = MyUtil.getPrefStr(MyUtil.PREF_COUNTRY, "");
        if (TextUtils.isEmpty(defaultCountry)) {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CMain.this);
                    CharSequence items[] = {"台灣", "香港", "其他"};
                    alertBuilder.setTitle("請選擇日曆地區：");
                    alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "TW");
                                    break;
                                case 1:
                                    MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "HK");
                                    break;
                                default:
                                    MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "CN");
                                    break;
                            }
                        }
                    });
                    alertBuilder.show();
                }
            }, 1000);
        }
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
////		if (android.os.Build.VERSION.SDK_INT < 11) {
////			if (mBroadcast!=null){
////				unregisterReceiver(mBroadcast);
////			}
////		}
//    }

//    private void cleanAnimation() {
//        mViewAnimator.setInAnimation(null);
//        mViewAnimator.setOutAnimation(null);
//    }

//    private int getID(int nbr, String extension) {
//        int resultVal = getResources().getIdentifier("xmlPage" + nbr + extension, "id", CMain.this.getPackageName());
//        if (resultVal == 0) {
//            MyUtil.logError(TAG, "Error on:" + "xmlPage" + nbr + extension);
//        }
//        return resultVal;
//    }

    //	private void onRefresh(){
//		//Toast.makeText(getApplicationContext(), "View:"+mViewIndex+" "+MyUtil.sdfYYYYMMDD.format(mDisplayDay.getTime()), Toast.LENGTH_SHORT).show();
//		if (mViewIndex==1){
//			onRefreshPage(1);
//			mViewIndex=2;
//		} else {
//			onRefreshPage(2);
//			mViewIndex=1;
//		}
//	}
    public void gotoPrevDay() {
        if (mPager.isScrolling) return;
        int result = MyDailyBread.calendarDaysBetween(mDailyBread.getValidFrDate(),mDisplayDay);
        Log.i(TAG, "gotoPrevDay day="+mDisplayDay.get(Calendar.DAY_OF_MONTH)+" ("+result+")");
        if (result <= 0) {
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
        } else {
            mDisplayDay.add(Calendar.DAY_OF_MONTH, -1);
//            mViewIndex = mViewIndex == 1 ? 2 : 1;
//            onRefreshPage(mViewIndex);
            onRefreshPage(mDisplayDay, true);
//            MyGestureListener.flingInFromRight(getApplicationContext(), mViewAnimator);
        }
    }

    public void gotoPriorMonth() {
        if (mPager.isScrolling) return;
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(mDisplayDay.getTimeInMillis());
        newCalendar.add(Calendar.MONTH, -1);
        int result = MyDailyBread.calendarDaysBetween(mDailyBread.getValidFrDate(),newCalendar);
        if (result<= 0) {
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
        } else {
            mDisplayDay.setTimeInMillis(newCalendar.getTimeInMillis());
//            mViewIndex = mViewIndex == 1 ? 2 : 1;
//            onRefreshPage(mViewIndex);
            onRefreshPage(mDisplayDay, false);
//            MyGestureListener.flingInFromRight(getApplicationContext(), mViewAnimator);
        }
    }

    public void gotoNextDay() {
        if (mPager.isScrolling) return;
        int result = MyDailyBread.calendarDaysBetween(mDisplayDay, mDailyBread.getValidToDate());
        if (result <= 0) {
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
        } else {
            mDisplayDay.add(Calendar.DAY_OF_MONTH, +1);
//            mViewIndex = mViewIndex == 1 ? 2 : 1;
//            onRefreshPage(mViewIndex);
            onRefreshPage(mDisplayDay, true);
            //MyGestureListener.flingInFromLeft(getApplicationContext(), mViewAnimator);
        }
    }

    public  void gotoNextMonth() {
        if (mPager.isScrolling) return;
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(mDisplayDay.getTimeInMillis());
        newCalendar.add(Calendar.MONTH, +1);
        int result = MyDailyBread.calendarDaysBetween(newCalendar, mDailyBread.getValidToDate());
        if (result <= 0) {
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
        } else {
            mDisplayDay.setTimeInMillis(newCalendar.getTimeInMillis());
//            mViewIndex = mViewIndex == 1 ? 2 : 1;
//            onRefreshPage(mViewIndex);
            onRefreshPage(mDisplayDay, false);
//            MyGestureListener.flingInFromLeft(getApplicationContext(), mViewAnimator);
        }
    }
    private void onRefreshPage(Calendar calendar, boolean smooth) {
        int position = Math.abs(MyDailyBread.calendarDaysBetween(calendar, mDailyBread.getValidFrDate()));
        if (mPager.getCurrentItem()!=position) {
            mPager.setCurrentItem(position, smooth);
        }
    }
	private void onClickViewBible(Context context){
        if (CMain.is_2016DayShown()){
            MyUtil.trackClick(context, "Support", "M");
            Intent intent = new Intent(context, SupportActivity.class);
            startActivityForResult(intent, MyUtil.REQUEST_SUPPORT);
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
            return;
        }
		boolean isShownAdBanner = AxTools.getPrefBoolean("pref_showBanner", false);
		//isShownAdBanner=false;
		if (isShownAdBanner){
			onViewHKBSBible(context);
			return;
		}
		AxTools.setPrefBoolean("pref_showBanner", true);
		final ImageView img = (ImageView) findViewById(R.id.xmlAdBannerImage);		
//		try {
//			img.setImageResource(R.drawable.alarm_2015);
//		} catch (Exception e){
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 2;
			Bitmap preview_bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.banner,options);
			img.setImageBitmap(preview_bitmap);
//		}
		    
		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.xmlAdBanner);
		layout.setVisibility(View.VISIBLE);
		final TextView text = (TextView) findViewById(R.id.xmlAdBannerText);
		text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				layout.setVisibility(View.GONE);
				onViewHKBSBible(CMain.this);
			}
		});
		final ImageView imgClose = (ImageView) findViewById(R.id.xmlAdBannerClose);
		imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				layout.setVisibility(View.GONE);
				onViewHKBSBible(CMain.this);
			}
		});				
	}
	private void onExitGoodCalendar(Context context){
		Toast.makeText(context, "你將會離開「"+(CMain.IS_2016_VERSION?context.getString(R.string.app_name_2016):context.getString(R.string.app_name))+"」並進入其他網站或程式 !", Toast.LENGTH_SHORT).show();
	}
	private void onViewHKBSBible(Context context){		
		MyUtil.trackClick(context, "BibleHKBS", "M");
		final Object eabcv[] = getEBCV(mGoldVerse);
		//final String prefix = "http://rcuv.hkbs.org.hk/bible_list.php?dowhat=&version=RCUV&bible=";
		final String prefix = "http://rcuv.hkbs.org.hk/RCUV_1/";
//		final String chapter = "&chapter=";
//		final String suffix = "&section=0";
		final int bookNbr = (Integer) eabcv[2];
		final int chapterNbr = (Integer) eabcv[3];
		final int verseNbr = (Integer) eabcv[4];
//		final String url = prefix+BOOKS_ENG[bookNbr-1]+chapter+chapterNbr+suffix;
		final String url = prefix+BOOKS_ENG[bookNbr-1]+"/"+chapterNbr+":"+verseNbr;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		onExitGoodCalendar(context);
		startActivity(intent);
	}
	private void onViewArkistBible(Context context){
		Intent intent = context.getPackageManager().getLaunchIntentForPackage("org.arkist.cnote");
		if (intent != null){
			MyUtil.trackClick(context, "BibleApp", "M");
			//intent.setComponent(new ComponentName("org.arkist.cnote","org.arkist.cnote.WebActivity"));
			final Object eabcv[] = getEBCV(mGoldVerse);
			final String finalEabcv = (String) eabcv[0]+ (String) eabcv[1]+" "+eabcv[3]+":"+eabcv[4]; 
			MyUtil.log(TAG, "bcv:"+finalEabcv);
			intent.setComponent(new ComponentName("org.arkist.cnote","org.arkist.cnote.KnockActivity"));
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK); // Disable bring existing task to foreground; Should used with New_Task
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // A New Task
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setData(ContentUris.withAppendedId(Uri.EMPTY, CALL_FROM_EXTERNAL_APP)); 
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, CALL_FROM_EXTERNAL_APP);
			intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, CALL_FROM_EXTERNAL_APP);		
			intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, finalEabcv);
			onExitGoodCalendar(context);
			context.startActivity(intent);
		} else {
			try {
				MyUtil.trackClick(context, "BiblePlayStore", "M");
				Toast.makeText(getApplicationContext(), "找尋閱讀聖經App", Toast.LENGTH_SHORT).show();
				Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
				downloadIntent.setData(Uri.parse("market://details?id=org.arkist.cnote"));
				onExitGoodCalendar(context);
				startActivity(downloadIntent);
			} catch (Exception e){
				MyUtil.trackClick(context, "BiblePlayStoreError", "M");
				Toast.makeText(getApplicationContext(), "找尋不到此機閱讀聖經App版本", Toast.LENGTH_SHORT).show();
			}
		}
	}
	private void onClickViewOthers(Context context){
		onViewArkistBible(context);
	}
//	private void onClickViewILoveTheBible(Context context){
//		Intent intent = context.getPackageManager().getLaunchIntentForPackage("hk.org.hkbs.ilovethebible");
//		if (intent != null){
//			MyUtil.trackClick(context, "PlanApp", "M");
//			context.startActivity(intent);
//		} else {
//			try {
//				MyUtil.trackClick(context, "PlanPlayStore", "M");
//				Toast.makeText(getApplicationContext(), "找尋讀經計劃App", Toast.LENGTH_SHORT).show();
//				Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
//				downloadIntent.setData(Uri.parse("market://details?id=hk.org.hkbs.ilovethebible"));
//				startActivity(downloadIntent);
//			} catch (Exception e){
//				MyUtil.trackClick(context, "PlanPlayStoreError", "M");
//				Toast.makeText(getApplicationContext(), "找尋不到此機讀經計劃App版本", Toast.LENGTH_SHORT).show();
//			}
//		}
//	}
	private void onClickSelectCalendar(Context context){
		MyUtil.trackClick(context, "Calendar", "M");
		Intent intent = new Intent(context, CalendarActivity.class);
		intent.putExtra(MyUtil.EXTRA_TYPE, MyUtil.EXTRA_TYPE_SELECT);
		intent.putExtra(MyUtil.EXTRA_DEFAULT_DATE, MyUtil.sdfYYYYMMDDHHMM.format(mDisplayDay.getTime()));
		startActivityForResult(intent, MyUtil.REQUEST_CALENDAR);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}
	private void onClickToday(Context context){
		MyUtil.trackClick(context, "Today", "M");
		Calendar newDate = Calendar.getInstance();
		if (isWithinRange(newDate)){
			String newDateStr = MyUtil.sdfYYYYMMDD.format(newDate.getTime());
//			String displayStr = MyUtil.sdfYYYYMMDD.format(mDisplayDay.getTime());
//			int slideDirection = newDateStr.compareTo(displayStr);
//			if (slideDirection!=0){
				mDisplayDay.setTimeInMillis(newDate.getTimeInMillis());
//				mViewIndex=mViewIndex==1?2:1;
//				onRefreshPage(mViewIndex);
                onRefreshPage(mDisplayDay,false);
//				if (slideDirection>0){
//					MyGestureListener.flingInFromLeft(getApplicationContext(), mViewAnimator);
//				} else if (slideDirection<0){
//					MyGestureListener.flingInFromRight(getApplicationContext(), mViewAnimator);
//				}
//			}
			Toast.makeText(context,newDateStr, Toast.LENGTH_SHORT).show();			 
		}
	}
	private void onClickAbout(Context context){
		MyUtil.trackClick(context, "About", "M");
		Intent intent = new Intent(context, AboutActivity.class);
		startActivityForResult(intent, MyUtil.REQUEST_ABOUT);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);		
	}
	private void onClickSupport(Context context){
		MyUtil.trackClick(context, "Support", "M");
		Intent intent = new Intent(context, SupportActivity.class);
		startActivityForResult(intent, MyUtil.REQUEST_SUPPORT);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}
	private void onClickAlarm(Context context){
		MyUtil.trackClick(context, "Alarm", "M");
		Intent intent = new Intent(context, AlarmActivity.class);
		startActivityForResult(intent, MyUtil.REQUEST_ALARM);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}
//    /**
//     * A paint that has utilities dealing with painting text.
//     * @author <a href="maillto:nospam">Ben Barkay</a>
//     * @version 10, Aug 2014
//     */
//    public class TextPaint extends android.text.TextPaint {
//        /**
//         * Constructs a new {@code TextPaint}.
//         */
//        public TextPaint() {
//            super();
//        }
//
//        /**
//         * Constructs a new {@code TextPaint} using the specified flags
//         * @param flags
//         */
//        public TextPaint(int flags) {
//            super(flags);
//        }
//
//        /**
//         * Creates a new {@code TextPaint} copying the specified {@code source} state.
//         * @param source The source paint to copy state from.
//         */
//        public TextPaint(Paint source) {
//            super(source);
//        }
//
//        // Some more utility methods...
//
//        /**
//         * Calibrates this paint's text-size to fit the specified text within the specified width.
//         * @param text      The text to calibrate for.
//         * @param boxWidth  The width of the space in which the text has to fit.
//         */
//        public void calibrateTextSize(String text, float boxWidth) {
//            calibrateTextSize(text, 0, Float.MAX_VALUE, boxWidth);
//        }
//
//        /**
//         * Calibrates this paint's text-size to fit the specified text within the specified width.
//         * @param text      The text to calibrate for.
//         * @param min       The minimum text size to use.
//         * @param max       The maximum text size to use.
//         * @param boxWidth  The width of the space in which the text has to fit.
//         */
//        public void calibrateTextSize(String text, float min, float max, float boxWidth) {
//            setTextSize(10);
//            setTextSize(Math.max(Math.min((boxWidth/measureText(text))*10, max), min));
//        }
//    }
//    /**
//     * Calibrates this paint's text-size to fit the specified text within the specified width.
//     * @param paint     The paint to calibrate.
//     * @param text      The text to calibrate for.
//     * @param min       The minimum text size to use.
//     * @param max       The maximum text size to use.
//     * @param boxWidth  The width of the space in which the text has to fit.
//     */
//    public static void calibrateTextSize(Paint paint, String text, float min, float max, float boxWidth) {
//        paint.setTextSize(10);
//        paint.setTextSize(Math.max(Math.min((boxWidth/paint.measureText(text))*10, max), min));
//    }


}
