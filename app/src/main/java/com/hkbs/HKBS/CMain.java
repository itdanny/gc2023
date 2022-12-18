package com.hkbs.HKBS;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.hkbs.HKBS.arkUtil.MyPermission;
import com.hkbs.HKBS.arkUtil.MyUtil;
import com.hkbs.HKBS.util.SystemUiHider;

import org.arkist.share.AxImageView;
import org.arkist.share.AxTools;

import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import androidx.viewpager2.widget.ViewPager2;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class CMain extends MyActivity {
    final static public boolean IS_2016_VERSION = true;
    final static public boolean IS_2017_VERSION = true;

    static public boolean is_2016DayShown() {
        if (mDisplayDay == null) {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.YEAR) >= 2016;
        }
        int curYear = mDisplayDay.get(Calendar.YEAR);
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.YEAR)>=2016;
        return curYear >= 2016;
    }

    //    final static private boolean IS_2015_OR_LATER = true;
    final static public boolean DEBUG = true;
    final static public boolean DEBUG_LAYOUT = false;

    final static private String TAG = CMain.class.getSimpleName();

    final static private int CALL_FROM_EXTERNAL_APP = -999;
    private static final int AUTO_HIDE_DELAY_MILLIS = 1500;
    static public MyDailyBread mDailyBread;

    //    static public int mCalendarYear = 2015;
//    static private float _scaleDensity = 0;
    static private Calendar mDisplayDay;
    //	private View mContentsView;
    private View mControlsView;
    private View mLeftRightPanel;
    private View mTitleView;
    static public String mScreenType = "";

    //private ViewAnimator mViewAnimator;
    private boolean isTitleShown = false;
    private ViewPager2 mPager;
    private boolean isScrolling = false;
    private CustomViewAdapter mAdapter;

    private View mRootView;
    static int showOutOfBoundsIfAllowBeyondRange = 0; // Toggle to show system is out-of-bound
    private Handler handler;

    @Override
    protected void onSaveInstanceState(Bundle InstanceState) {
        super.onSaveInstanceState(InstanceState);
        InstanceState.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!MyPermission.getInstance().onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
                new MyPermission.Callback() {
                    @Override
                    public void onRequestResult(int requestCode, boolean success) {
                        switch (requestCode) {
                            case MyPermission.REQUEST_ACCESS_STORAGE:
                                if (success) {
                                    onClickShare(getBaseContext(), true);
                                }
                                break;
//                            case MyPermission.REQUEST_ACCESS_ALARM:
//                                if (success) {
//                                    AxAlarm.setDailyAlarm(CMain.this, AxAlarm.MODE.SET_DEFAULT, 9, 0, MyGoldBroadcast.class);
//                                    AxAlarm.setDailyOnDateChange(CMain.this);
//                                }
//                                break;
                        }
                    }
                })) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        //Execute your code here
        finish();
    }

    // DC 202212
    // Use Keyboard to control will disable onBackPressed

//

    //private GestureDetector mGesture;
    // private View.OnTouchListener mViewOnTouch;

    private class CustomViewAdapter extends FragmentStateAdapter {
        Calendar mCalendar;
        Context mContext;
        View.OnClickListener mOnClickListener;

        public CustomViewAdapter(FragmentActivity fa, View.OnClickListener onClickListener) {
            super(fa);
            mContext = getBaseContext();//context;
            mCalendar = Calendar.getInstance();
            mOnClickListener = onClickListener;
        }

        @Override
        public Fragment createFragment(int position) {
            mCalendar.setTimeInMillis(CMain.mDailyBread.getValidFrDate().getTimeInMillis());
            mCalendar.add(Calendar.DAY_OF_YEAR, position);
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            MyUtil.log(TAG, "Get Item:" + year + "," + month + "," + day);
            return DailyFragment.getInstance(year, month, day);
        }

        @Override
        public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            if (nbrOfItems == -1) {
                nbrOfItems = (int) CMain.mDailyBread.getNbrOfValidDays(mContext);
            }
            return nbrOfItems;
        }

        private int nbrOfItems = -1;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (MyApp.mNewLangContext != null) {
            super.attachBaseContext(MyApp.mNewLangContext);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "*** Start " + getPackageName() + " ***");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDailyBread = MyDailyBread.getInstance(this);
        mDisplayDay = MyDailyBread.getInstance(getApplicationContext()).index2date(getApplicationContext(), 0);
        mDisplayDay = MyDailyBread.getValidCalendar(this, mDisplayDay);
        AxTools.init(CMain.this);
        MyUtil.initMyUtil(this);
        if (DEBUG) MyUtil.log(TAG, "StartApp3..............");

        setContentView(R.layout.activity_cmain);
        mRootView = findViewById(R.id.xmlRoot);
//        mViewOnTouch = new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (mGesture == null) {
//                    mGesture = new GestureDetector(getApplicationContext(), new MyGestureListener(new MyGestureListener.Callback() {
//                        @Override
//                        public boolean onClick(MotionEvent e) { // !!! SetClicable to TRUE !!!
//                            if (DEBUG)
//                                MyUtil.log(TAG, "onClick day=" + mDisplayDay.get(Calendar.DAY_OF_MONTH));
//                            setControlsVisibility(!isTitleShown);
//                            return true;
//                        }
//
//                        @Override
//                        public boolean onLongPress(MotionEvent e) {
//                            if (DEBUG)
//                                MyUtil.log(TAG, "onClick day=" + mDisplayDay.get(Calendar.DAY_OF_MONTH));
//                            setControlsVisibility(!isTitleShown);
//                            //setControlsVisibility(true);
//                            return true;
//                        }
//
//                        @Override
//                        public boolean onRight() {
//                            gotoNextDay();
//                            return true;
//                        }
//
//                        @Override
//                        public boolean onLeft() {
//                            gotoPrevDay();
//                            return true;
//                        }
//                    }));
//                }
//                mGesture.onTouchEvent(event);
//                return false;
//            }
//        };
//        mRootView.setOnTouchListener(mViewOnTouch);

        AxImageView clickLeftView = findViewById(R.id.xmlMainClickLeft);
        clickLeftView.setOnClickListener(v -> {
            delayedHide();
            gotoPrevDay();
        });
        AxImageView clickRightView = findViewById(R.id.xmlMainClickRight);
        clickRightView.setOnClickListener(v -> {
            delayedHide();
            gotoNextDay();
        });

        mDisplayDay = Calendar.getInstance();
        mDisplayDay = MyDailyBread.getValidCalendar(this, mDisplayDay);
        mControlsView = findViewById(R.id.xmlMainControls);
        mLeftRightPanel = findViewById(R.id.xmlMainClickPanel);
        mTitleView = findViewById(R.id.xmlMainTitle);

        onCreateSetButtons();

        mPager = findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(1);
        // DC 202212 mAdapter = new CustomViewAdapter(getSupportFragmentManager(), CMain.this);
        mAdapter = new CustomViewAdapter(CMain.this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setControlsVisibility(true);
            }
        });
        mPager.setAdapter(mAdapter);

//        View myTopView = mPager.getChildAt(mPager.getCurrentItem());
//        ArrayList<View> allViewsWithinMyTopView = getAllChildren(myTopView);
//        for (View child : allViewsWithinMyTopView) {
//            child.setClickable(true);
//            child.setOnClickListener(v->setControlsVisibility(true));
//        }
//        mPager.getChildAt(mPager.getCurrentItem()).setClickable(true);
//        mPager.getChildAt(mPager.getCurrentItem()).setOnClickListener(v -> setControlsVisibility(true));
//        mPager.getChildAt(mPager.getCurrentItem()).setOnTouchListener((v, event) -> {
//          //  switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    v.setTag(1);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    v.setTag(0);
//                    break;
//                case MotionEvent.ACTION_UP:
//                    if ((int) v.getTag() == 1) {
//                        v.setTag(0);
//                        setControlsVisibility(true);
//                        return true;
//                    }
//                    v.setTag(0);
//                    break;
//            }
//            return false;
//        });
        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            boolean lastPageChange = false;
            boolean fistPageChange = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                int lastIdx = mAdapter.getItemCount() - 1;
                if (lastPageChange && position == lastIdx) {
                    Calendar calendar = (Calendar) mDailyBread.getValidToDate().clone();
                    calendar.add(Calendar.MONTH, 1);
                    MyDailyBread.showOutOfBounds(getApplicationContext(), calendar);
                } else if (fistPageChange && position == 0) {
                    MyDailyBread.showOutOfBounds(getApplicationContext(), mDailyBread.getValidFrDate());
                }
                isScrolling = false;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mDisplayDay = MyDailyBread.getInstance(getApplicationContext()).index2date(getApplicationContext(), position);
                mDisplayDay = MyDailyBread.getValidCalendar(CMain.this, mDisplayDay);
//                mDisplayDay = (Calendar) mDailyBread.getValidFrDate().clone();
//                mDisplayDay.add(Calendar.DAY_OF_MONTH, i);
                if (DEBUG)
                    Log.i(TAG, "onPageSelected day=" + mDisplayDay.get(Calendar.DAY_OF_MONTH) + " beg");
                onRefreshPage(mDisplayDay, false, false);
                if (DEBUG)
                    Log.i(TAG, "onPageSelected day=" + mDisplayDay.get(Calendar.DAY_OF_MONTH) + " end");
                isScrolling = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                int lastIdx = mAdapter.getItemCount() - 1;
                int curItem = mPager.getCurrentItem();
                if (curItem == lastIdx && state == ViewPager2.SCROLL_STATE_DRAGGING)
                    lastPageChange = true;
                else lastPageChange = false;
                if (curItem == 0 && state == ViewPager2.SCROLL_STATE_DRAGGING)
                    fistPageChange = true;
                else fistPageChange = false;
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    isScrolling = false;
                } else {
                    isScrolling = true;
                }
            }
        });
        if (DEBUG) MyUtil.log(TAG, "StartApp3..............End");

        AxTools.runFollow(new Runnable() {
            @Override
            public void run() {
                onClickToday(CMain.this);
            }
        });

        //checkUpdateVersion();

    }

    //    private ArrayList<View> getAllChildren(View v) {
//
//        if (!(v instanceof ViewGroup)) {
//            ArrayList<View> viewArrayList = new ArrayList<View>();
//            viewArrayList.add(v);
//            return viewArrayList;
//        }
//
//        ArrayList<View> result = new ArrayList<View>();
//
//        ViewGroup vg = (ViewGroup) v;
//        for (int i = 0; i < vg.getChildCount(); i++) {
//
//            View child = vg.getChildAt(i);
//
//            ArrayList<View> viewArrayList = new ArrayList<View>();
//            viewArrayList.add(v);
//            viewArrayList.addAll(getAllChildren(child));
//
//            result.addAll(viewArrayList);
//        }
//        return result;
//    }
    // @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void setControlsVisibility(boolean isVisible) {
        if (this.isFinishing()) return;
        if (isVisible && isTitleShown) {
            isVisible = false;
        }
        onCreateSetButtons();

        if (mControlsView == null) mControlsView = findViewById(R.id.xmlMainControls);
        if (mTitleView == null) mTitleView = findViewById(R.id.xmlMainTitle);
        int mControlsHeight = 0;
        int mTitleHeight = 0;
        int mShortAnimTime = 0;
        isTitleShown = isVisible;

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
// DC 202212
//        contentView.setDrawingCacheEnabled(true);
//        screenShot = contentView.getDrawingCache();
        screenShot = MyUtil.getBitmapFromView(contentView);
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
        Button mainBtnCal = (Button) findViewById(R.id.mainBtnCalendar);
        mainBtnCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSelectCalendar(CMain.this);
            }
        });

        Button btnOnLineBible = (Button) findViewById(R.id.mainBtnOnlineBible);
        if (CMain.is_2016DayShown()) {
            btnOnLineBible.setText(R.string.main_support);
            btnOnLineBible.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickSupport(CMain.this);
                }
            });
        } else {
            btnOnLineBible.setText(R.string.main_bible);
            btnOnLineBible.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOnLineBible(CMain.this);
                }
            });
        }

        Button mainBtnPlan = (Button) findViewById(R.id.mainBtnOfflineBible);
        mainBtnPlan.setText(R.string.main_arkist);
        mainBtnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOfflineBible(CMain.this);
            }
        });
        if (mainBtnPlan != null) {
            Intent intent = CMain.this.getPackageManager().getLaunchIntentForPackage("org.arkist.cnote");
            if (intent != null) { // Exist
                mainBtnPlan.setText(R.string.BibleApp);
            }
        }

        Button mainBtnShare = findViewById(R.id.mainBtnShare);
        mainBtnShare.setOnClickListener(v -> onClickShare(CMain.this, false));

        Button mainBtnAlarm = findViewById(R.id.mainBtnAlarm);
        mainBtnAlarm.setOnClickListener(v -> onClickAlarm(CMain.this));

        Button mainBtnAbout = findViewById(R.id.mainBtnAbout);
        mainBtnAbout.setOnClickListener(v -> onClickAbout(CMain.this));

        Button mainBtnCopy = findViewById(R.id.mainBtnCopy);
        mainBtnCopy.setOnClickListener(v -> onClickCopy(CMain.this));

        Button mainBtnToday = findViewById(R.id.mainBtnToday);
        mainBtnToday.setOnClickListener(v -> onClickToday(CMain.this));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //       delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            delayedHide();
//            return false;
//        }
//    };

    Handler mHideHandler = new Handler(Looper.getMainLooper());
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            setControlsVisibility(false);
            //mSystemUiHider.hide();
        }
    };

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
        builder.setTitle(R.string.main_select_share);
        String[] options = {getString(R.string.main_share_text), getString(R.string.main_share_image)};
        builder.setItems(options, listener);
        builder.setNegativeButton(getString(R.string.BtnCancel), null);
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

    private void onClickShare(Context context, boolean suppressRequestPermission) {
        if (!MyPermission.getInstance().checkPermission(
                CMain.this,
                MyPermission.REQUEST_ACCESS_STORAGE,
                suppressRequestPermission)) {
            return;
        }
        saveScreenShot();
        popSelectTextImage(context, shareListener);
    }

    private ContentValues getContentValues(Context context) {
        ContentValues cv;
        try {
            int curYear = mDisplayDay.get(Calendar.YEAR);
            int curMonth = mDisplayDay.get(Calendar.MONTH);
            int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
            cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
        } catch (Exception ignored) {
            Calendar calendar = MyDailyBread.getInstance(context).getValidToDate();
            cv = mDailyBread.getContentValues(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }
        return cv;
    }

    private String getContentValueGoldVerse() {
        int curYear = mDisplayDay.get(Calendar.YEAR);
        int curMonth = mDisplayDay.get(Calendar.MONTH);
        int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
        ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
        return cv.getAsString(MyDailyBread.wGoldVerse) + (curYear >= 2016 ? "" : "；和合本修訂版");
    }

    private void copyText(Context context) {
        try {
            ContentValues cv = getContentValues(context);
            if (cv == null) return;
            final String verse = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ") +
                    "[" + cv.getAsString(MyDailyBread.wGoldVerse) + "]";// + " RCUV]";
            MyClipboardManager mgr = new MyClipboardManager();
            mgr.copyToClipboard(CMain.this, verse);
            Toast.makeText(getApplicationContext(), R.string.main_copy_to_pasteboard, Toast.LENGTH_SHORT).show();
            //onClickSupport(CMain.this);
        } catch (Exception ignored) {
            Toast.makeText(getApplicationContext(), R.string.main_nothing_to_copy, Toast.LENGTH_SHORT).show();
        }
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
        File imageFile = new File(Environment.getExternalStorageDirectory(), fileName);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File filelocation = new File(getCacheDir(), fileName);
            uri = FileProvider.getUriForFile(context, "com.hkbs.HKBS.provider", filelocation);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//"com.hkbs.HKBS.provider"
            //uri = FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID + ".provider",imageFile);
            uri = FileProvider.getUriForFile(context, "com.hkbs.HKBS.provider", imageFile);
        } else {
            uri = Uri.fromFile(imageFile);
        }
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
            if (tryCounter == 1) {
                try {

                } catch (Exception e1) {
                    Toast.makeText(this, "Error11:" + tryCounter + " " + e.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Error:" + tryCounter + " " + e.toString(), Toast.LENGTH_LONG).show();
            }
            //System.err.println(e.toString());
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(android.content.Intent.EXTRA_SUBJECT, "「" +
                (IS_2016_VERSION ? context.getString(R.string.app_name) : context.getString(R.string.app_name_2015)) +
                "」經文分享");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(share, "分享圖像"));
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
        }
//		Intent share = new Intent(Intent.ACTION_SEND);
//		share.setType("image/jpeg");
//		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/screenshot.png"));
//		startActivity(Intent.createChooser(share, "分享圖像"));
    }

    private void shareText(Context context) {
        try {
            int curYear = mDisplayDay.get(Calendar.YEAR);
            int curMonth = mDisplayDay.get(Calendar.MONTH);
            int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
            ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
            final String verse = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ") +
                    "[" + cv.getAsString(MyDailyBread.wGoldVerse) +
                    (is_2016DayShown() ? ";和合本]" : "；和合本修訂版]");
            MyUtil.trackClick(context, "Share", "M");
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "「" +
                    (IS_2016_VERSION ? context.getString(R.string.app_name) : context.getString(R.string.app_name_2015)) +
                    "」" + getString(R.string.main_share_bible));//getResources().getString(R.string.app_name)
            intent.putExtra(android.content.Intent.EXTRA_TEXT, verse);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.app_name_2015)));
        } catch (Exception ignored) {

        }
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
            {"62", "約翰一書", "約一"}, //約翰壹書4章18節
            {"62", "約翰二書", "約二"},
            {"62", "約翰三書", "約三"},
            {"65", "猶大書", "猶"},
            {"66", "啟示錄", "啟"}
    };
    static final public String[][] BOOKS_CHS = new String[][]{
            {"1", "创世记", "创"},
            {"2", "出埃及记", "出"},
            {"3", "利未记", "利"},
            {"4", "民数记", "民"},
            {"5", "申命记", "申"},
            {"6", "约书亚记", "书"},
            {"7", "士师记", "士"},
            {"8", "路得记", "得"},
            {"9", "撒母耳记上", "撒上"},
            {"10", "撒母耳记下", "撒下"},
            {"11", "列王纪上", "王上"},
            {"12", "列王纪下", "王下"},
            {"13", "历代志上", "代上"},
            {"14", "历代志下", "代下"},
            {"15", "以斯拉记", "拉"},
            {"16", "尼希米记", "尼"},
            {"17", "以斯帖记", "斯"},
            {"18", "约伯记", "伯"},
            {"19", "诗篇", "诗"},
            {"20", "箴言", "箴"},
            {"21", "传道书", "传"},
            {"22", "雅歌", "歌"},
            {"23", "以赛亚书", "赛"},
            {"24", "耶利米书", "耶"},
            {"25", "耶利米哀歌", "哀"},
            {"26", "以西结书", "结"},
            {"27", "但以理书", "但"},
            {"28", "何西阿书", "何"},
            {"29", "约珥书", "珥"},
            {"30", "阿摩司书", "摩"},
            {"31", "俄巴底亚书", "俄"},
            {"32", "约拿书", "拿"},
            {"33", "弥迦书", "弥"},
            {"34", "那鸿书", "鸿"},
            {"35", "哈巴谷书", "哈"},
            {"36", "西番雅书", "番"},
            {"37", "哈该书", "该"},
            {"38", "撒迦利亚书", "亚"},
            {"39", "玛拉基书", "玛"},
            {"40", "马太福音", "太"},
            {"41", "马可福音", "可"},
            {"42", "路加福音", "路"},
            {"43", "约翰福音", "约"},
            {"44", "使徒行传", "徒"},
            {"45", "罗马书", "罗"},
            {"46", "哥林多前书", "林前"},
            {"47", "哥林多后书", "林后"},
            {"48", "加拉太书", "加"},
            {"49", "以弗所书", "弗"},
            {"50", "腓立比书", "腓"},
            {"51", "歌罗西书", "西"},
            {"52", "帖撒罗尼迦前书", "帖前"},
            {"53", "帖撒罗尼迦后书", "帖后"},
            {"54", "提摩太前书", "提前"},
            {"55", "提摩太后书", "提后"},
            {"56", "提多书", "多"},
            {"57", "腓利门书", "门"},
            {"58", "希伯来书", "来"},
            {"59", "雅各书", "雅"},
            {"60", "彼得前书", "彼前"},
            {"61", "彼得后书", "彼后"},
            {"62", "约翰一书", "约一"},//约翰壹书4章18节
            {"62", "约翰二书", "约二"},
            {"62", "约翰三书", "约三"},
            {"65", "犹大书", "犹"},
            {"66", "启示录", "启"}};
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
//        bookName.replace("一", "壹");
//        bookName.replace("二", "貳");
//        bookName.replace("三", "參");
        bookName = bookName.replace("壹", "一");//約翰壹書4章18節
        bookName = bookName.replace("貳", "二");
        bookName = bookName.replace("參", "三");
        String lang = AxTools.getPrefStr(MyApp.PREF_APP_LANG, "");
        String bookAbbrev = "";
        if (lang.equalsIgnoreCase(MyApp.PREF_APP_LANG_CN)) {
            for (int i = 0; i < BOOKS_CHS.length; i++) {
                if (bookName.equalsIgnoreCase(BOOKS_CHS[i][1])) {
                    bookAbbrev = BOOKS_CHS[i][2];
                    bookNbr = i + 1;
                    break;
                }
            }
        } else {
            for (int i = 0; i < BOOKS_CHT.length; i++) {
                if (bookName.equalsIgnoreCase(BOOKS_CHT[i][1])) {
                    bookAbbrev = BOOKS_CHT[i][2];
                    bookNbr = i + 1;
                    break;
                }
            }
        }
        if (bookAbbrev.equalsIgnoreCase("")) {
            MyUtil.logError(TAG, "Cannot find:" + bcv.substring(0, digitFirstPos).trim() + "(" + bookName + ")");
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

        //String finalBCV = bookAbbrev+" "+chapter+":"+verse;
        return eabcv;//"tr "+finalBCV;
        //return "rc 創 5:2";
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//        if (DEBUG) MyUtil.log(TAG, "onActivityResult:" + requestCode + "," + resultCode);
//        switch (requestCode) {
//            case MyUtil.REQUEST_CALENDAR:
//                if (resultCode == RESULT_OK) {
//                    long newTime = intent.getLongExtra(MyUtil.EXTRA_SELECT_MILLSEC, 0);
//                    if (DEBUG) MyUtil.log(TAG, "CMain:" + newTime);
//                    if (newTime != 0) {
//                        Calendar newDate = Calendar.getInstance();
//                        newDate.setTimeInMillis(newTime);
//                        if (isWithinRange(newDate, 0)) {
//                            mDisplayDay.setTimeInMillis(newTime);
//                        } else {
//                            if (newDate.compareTo(mDailyBread.getValidFrDate()) < 0){
//                                mDisplayDay.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis());
//                            } else {
//                                mDisplayDay.setTimeInMillis(mDailyBread.getValidToDate().getTimeInMillis());
//                            }
//                        }
//                        onRefreshPage(mDisplayDay,false, true);
//                    }
//                }
//                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//                break;
//            case MyUtil.REQUEST_ABOUT:
//                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//                break;
//            case MyUtil.REQUEST_SUPPORT:
//                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//                break;
//        }
//    }

    private boolean isWithinRange(Calendar checkDate, int nbrOfDates) {
        if (MyDailyBread.allowBeyondRange && checkDate.get(Calendar.YEAR) >= MyDailyBread.beyondFrYear && checkDate.get(Calendar.YEAR) <= MyDailyBread.beyondToYear) {
            return true;
        }
        Calendar frDate = (Calendar) mDailyBread.getValidFrDate().clone();
        frDate.set(Calendar.HOUR_OF_DAY, 0);
        frDate.set(Calendar.MINUTE, 0);
        frDate.set(Calendar.SECOND, 0);
        frDate.set(Calendar.MILLISECOND, 0);
        Calendar toDate = (Calendar) mDailyBread.getValidToDate().clone();
        toDate.set(Calendar.HOUR_OF_DAY, 23);
        toDate.set(Calendar.MINUTE, 59);
        toDate.set(Calendar.SECOND, 59);
        toDate.set(Calendar.MILLISECOND, 0);
        toDate.add(Calendar.SECOND, 1);
        Calendar tmpDate = (Calendar) checkDate.clone();
        tmpDate.set(Calendar.HOUR_OF_DAY, 11);
        tmpDate.set(Calendar.MINUTE, 59);
        tmpDate.set(Calendar.SECOND, 59);
        tmpDate.set(Calendar.MILLISECOND, 0);
        tmpDate.add(Calendar.DAY_OF_MONTH, nbrOfDates);
        if (tmpDate.compareTo(frDate) < 0 ||
                tmpDate.compareTo(toDate) >= 0) {
            MyDailyBread.showOutOfBounds(getApplicationContext(), tmpDate);
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
//	static private MyGoldBroadcast mBroadcast;
    @Override
    protected void onResume() {
        super.onResume();
        delayedHide();
//		if (android.os.Build.VERSION.SDK_INT < 11) {
//			mBroadcast = new MyGoldBroadcast();
//			registerReceiver(mBroadcast, new IntentFilter());
//		}
        if (DEBUG) MyUtil.log(TAG, "onResume");
//        onClickToday(this);
        //MyUtil.log(TAG, "onResumeAfterOnRefreshPage "+mPager.getCurrentItem());

        String defaultCountry = MyUtil.getPrefStr(MyUtil.PREF_COUNTRY, "");
        if (TextUtils.isEmpty(defaultCountry)) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CMain.this);
                        CharSequence items[] = {"台灣", "香港", "其他"};
                        alertBuilder.setTitle("請選擇日曆地區：");
                        // If cancel, then others;
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
                                //DailyFragment dailyFragment = (DailyFragment) mAdapter.getItem(mPager.getCurrentItem());
                                //dailyFragment.onRefreshScreen();
                                //mAdapter.notifyDataSetChanged();
                                mPager.setAdapter(null);
                                mPager.setAdapter(mAdapter);
                                onClickToday(CMain.this);
                                //askHolyDay();
                            }
                        });
                        if (CMain.isAppInForground(CMain.this) && !CMain.this.isFinishing()) {
                            alertBuilder.show();
                        }
                    } catch (Exception ignored) {

                    }
                }
            }, 1000);
        } else {
            //askHolyDay();
        }
        AxTools.runLater(500, new Runnable() {
            @Override
            public void run() {
                try {
                    if (CMain.isAppInForground(CMain.this) && !CMain.this.isFinishing()) {
                        // DC 202212 DailyFragment dailyFragment = (DailyFragment) mAdapter.getItemCount(mPager.getCurrentItem());
                        DailyFragment dailyFragment = (DailyFragment) mAdapter.createFragment(mPager.getCurrentItem());
                        dailyFragment.onRefreshScreen();
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void askHolyDay() {
        int showHolyDay = MyUtil.getPrefInt(MyUtil.PREF_HOLY_DAY, -1);
        if (showHolyDay == -1) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CMain.this);
                    CharSequence items[] = {"關閉", "開啟"};
                    alertBuilder.setTitle("顯示教會年曆：");
                    alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    MyUtil.setPrefInt(MyUtil.PREF_HOLY_DAY, 0);
                                    break;
                                case 1:
                                    MyUtil.setPrefInt(MyUtil.PREF_HOLY_DAY, 1);
                                    break;
                                default:
                                    break;
                            }
//                            DailyFragment dailyFragment = (DailyFragment) mAdapter.getItem(mPager.getCurrentItem());
//                            dailyFragment.onRefreshScreen();
//                            mAdapter.notifyDataSetChanged();
                            mPager.setAdapter(null);
                            mPager.setAdapter(mAdapter);
                            onClickToday(CMain.this);
                            askNotHKBS();
                        }
                    });
                    alertBuilder.show();
                }
            }, 1000);
        } else {
            askNotHKBS();
        }
    }

    private void askNotHKBS() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) <= 2015) {
            int showNotHkbs = MyUtil.getPrefInt(MyUtil.PREF_NOT_HKBS, -1);
            if (showNotHkbs == -1) {
                MyUtil.setPrefInt(MyUtil.PREF_NOT_HKBS, 0);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CMain.this);
                alertBuilder.setCancelable(false);
                alertBuilder.setTitle("2016年版提示:");
                alertBuilder.setMessage("2016年版金句使用公眾領域傳統和合本版權。" +
                        "2015年前金句由香港聖經公會提供並使用和合本修訂版。蒙香港聖經公會允許使用。詳情可看「關於我們」。\n" +
                        "如有任何查詢，歡迎電郵給我們info@arkist.org");
                alertBuilder.setPositiveButton("繼續", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertBuilder.show();
            }
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
        if (isScrolling) return;
        if (!MyDailyBread.allowBeyondRange && !isWithinRange(mDisplayDay, -1)) {
            MyDailyBread.showOutOfBounds(getApplicationContext(), mDisplayDay);
        } else {
            mDisplayDay.add(Calendar.DAY_OF_MONTH, -1);
            onRefreshPage(mDisplayDay, true, true);
        }
    }

    public void gotoPriorMonth() {
        if (isScrolling) return;
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(mDisplayDay.getTimeInMillis());
        newCalendar.add(Calendar.MONTH, -1);
        if (!MyDailyBread.allowBeyondRange && !isWithinRange(newCalendar, 0)) {
            MyDailyBread.showOutOfBounds(getApplicationContext(), newCalendar);
            newCalendar.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis());
        }
        mDisplayDay.setTimeInMillis(newCalendar.getTimeInMillis());
        onRefreshPage(mDisplayDay, false, true);
    }

    public void gotoNextDay() {
        if (isScrolling) return;
        if (!MyDailyBread.allowBeyondRange && !isWithinRange(mDisplayDay, 1)) {
            Calendar calendar = (Calendar) mDisplayDay.clone();
            calendar.add(Calendar.MONTH, 1);
            MyDailyBread.showOutOfBounds(getApplicationContext(), calendar);
        } else {
            mDisplayDay.add(Calendar.DAY_OF_MONTH, 1);
            onRefreshPage(mDisplayDay, true, true);
        }
    }

    public void gotoNextMonth() {
        if (isScrolling) return;
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(mDisplayDay.getTimeInMillis());
        newCalendar.add(Calendar.MONTH, +1);
        if (!MyDailyBread.allowBeyondRange && !isWithinRange(newCalendar, 0)) {
            MyDailyBread.showOutOfBounds(getApplicationContext(), newCalendar);
            newCalendar.setTimeInMillis(mDailyBread.getValidToDate().getTimeInMillis());
        }
        mDisplayDay.setTimeInMillis(newCalendar.getTimeInMillis());
        onRefreshPage(mDisplayDay, false, true);

    }

    private void onRefreshPage(Calendar calendar, final boolean smooth, boolean setPager) {
        final int position = MyDailyBread.date2index(getApplicationContext(), calendar);//Exclude last day
        if (setPager) {
            mPager.setCurrentItem(position, smooth);
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    int resultCode = result.getResultCode();
                    // There are no request codes
                    Intent data = result.getData();
                    if (data == null) return;
                    // theRequest
                    int requestCode = data.getIntExtra(MyUtil.EXTRA_REQUEST, -1);
                    switch (requestCode) {
                        case MyUtil.REQUEST_ALARM:
                            break;
                        case MyUtil.REQUEST_CALENDAR:
                            if (resultCode == RESULT_OK) {
                                long newTime = data.getLongExtra(MyUtil.EXTRA_SELECT_MILLSEC, 0);
                                if (DEBUG) MyUtil.log(TAG, "CMain:" + newTime);
                                if (newTime != 0) {
                                    Calendar newDate = Calendar.getInstance();
                                    newDate.setTimeInMillis(newTime);
                                    if (isWithinRange(newDate, 0)) {
                                        mDisplayDay.setTimeInMillis(newTime);
                                    } else {
                                        if (newDate.compareTo(mDailyBread.getValidFrDate()) < 0) {
                                            mDisplayDay.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis());
                                        } else {
                                            mDisplayDay.setTimeInMillis(mDailyBread.getValidToDate().getTimeInMillis());
                                        }
                                    }
                                    onRefreshPage(mDisplayDay, false, true);
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

            });

    private void onClickOnLineBible(Context context) {
        MyUtil.trackClick(context, "BibleOnLine", "M");
        final Object eabcv[] = getEBCV(getContentValueGoldVerse());
        //final String prefix = "http://rcuv.hkbs.org.hk/bible_list.php?dowhat=&version=RCUV&bible=";
        //final String prefix = "http://rcuv.hkbs.org.hk/RCUV_1/";
        String prefix;
        if (CMain.is_2016DayShown()) {
            prefix = "http://arkist.org/MyBibleMap/_php/getReadBibleUrl.php?editionNbr=2&";
        } else {
            prefix = "http://arkist.org/MyBibleMap/_php/getReadBibleUrl.php?editionNbr=1&";
        }
//		final String chapter = "&chapter=";
//		final String suffix = "&section=0";
        final int bookNbr = (Integer) eabcv[2];
        final int chapterNbr = (Integer) eabcv[3];
        final int verseNbr = (Integer) eabcv[4];
//		final String url = prefix+BOOKS_ENG[bookNbr-1]+chapter+chapterNbr+suffix;
//      final String url = prefix+BOOKS_ENG[bookNbr-1]+"/"+chapterNbr+":"+verseNbr;
        final String url = prefix + "bookNbr=" + bookNbr + "&chapterNbr=" + chapterNbr + "&verseNbr=" + verseNbr;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Log.e(TAG, "Call another app " + url);
        intent.setData(Uri.parse(url));
        //onExitGoodCalendar(context);
        startActivity(intent);
    }

    //	private void onClickViewHkbsBible(Context context){
//		boolean isShownAdBanner = AxTools.getPrefBoolean("pref_showBanner", false);
//		//isShownAdBanner=false;
//		if (isShownAdBanner){
//			onViewHKBSBible(context);
//			return;
//		}
//		AxTools.setPrefBoolean("pref_showBanner", true);
//		final ImageView img = (ImageView) findViewById(R.id.xmlAdBannerImage);
////		try {
////			img.setImageResource(R.drawable.alarm_2015);
////		} catch (Exception e){
//			BitmapFactory.Options options=new BitmapFactory.Options();
//			options.inSampleSize = 2;
//			Bitmap preview_bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.banner,options);
//			img.setImageBitmap(preview_bitmap);
////		}
//
//		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.xmlAdBanner);
//		layout.setVisibility(View.VISIBLE);
//		final TextView text = (TextView) findViewById(R.id.xmlAdBannerText);
//		text.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				layout.setVisibility(View.GONE);
//				onViewHKBSBible(CMain.this);
//			}
//		});
//		final ImageView imgClose = (ImageView) findViewById(R.id.xmlAdBannerClose);
//		imgClose.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				layout.setVisibility(View.GONE);
//				onViewHKBSBible(CMain.this);
//			}
//		});
//	}
//	private void onExitGoodCalendar(Context context){
//		Toast.makeText(context, "你將會離開「"+(CMain.IS_2016_VERSION?context.getString(R.string.app_name):context.getString(R.string.app_name_2015))+"」並進入其他網站或程式 !", Toast.LENGTH_SHORT).show();
//	}
//	private void onViewHKBSBible(Context context){
//		MyUtil.trackClick(context, "BibleHKBS", "M");
//		final Object eabcv[] = getEBCV(getContentValueGoldVerse());
//		//final String prefix = "http://rcuv.hkbs.org.hk/bible_list.php?dowhat=&version=RCUV&bible=";
//		final String prefix = "http://rcuv.hkbs.org.hk/RCUV_1/";
////		final String chapter = "&chapter=";
////		final String suffix = "&section=0";
//		final int bookNbr = (Integer) eabcv[2];
//		final int chapterNbr = (Integer) eabcv[3];
//		final int verseNbr = (Integer) eabcv[4];
////		final String url = prefix+BOOKS_ENG[bookNbr-1]+chapter+chapterNbr+suffix;
//		final String url = prefix+BOOKS_ENG[bookNbr-1]+"/"+chapterNbr+":"+verseNbr;
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setData(Uri.parse(url));
//		onExitGoodCalendar(context);
//		startActivity(intent);
//	}
    private void startBibleMap(Context context, Intent intent, ComponentName componentName) {
        final Object eabcv[] = getEBCV(getContentValueGoldVerse());
        final String finalEabcv = (String) eabcv[0] + (String) eabcv[1] + " " + eabcv[3] + ":" + eabcv[4];
        MyUtil.log(TAG, "bcv:" + finalEabcv);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK); // Disable bring existing task to foreground; Should used with New_Task
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // A New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setData(ContentUris.withAppendedId(Uri.EMPTY, CALL_FROM_EXTERNAL_APP));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, CALL_FROM_EXTERNAL_APP);
        intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, CALL_FROM_EXTERNAL_APP);
        intent.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, finalEabcv);
        //onExitGoodCalendar(context);
        context.startActivity(intent);
    }

    private void onClickOfflineBible(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("org.arkist.cnote");
        if (intent != null) {
            MyUtil.trackClick(context, "BibleApp", "M");
            //intent.setComponent(new ComponentName("org.arkist.cnote","org.arkist.cnote.WebActivity"));
            try {
                startBibleMap(context, intent, new ComponentName("org.arkist.cnote", "org.arkist.bx.BxActivity"));
            } catch (Exception e) {
                try {
                    startBibleMap(context, intent, new ComponentName("org.arkist.cnote", "org.arkist.cnote.KnockActivity"));
                } catch (Exception e2) {
                    Toast.makeText(getApplicationContext(), R.string.main_cannot_find_bible_app, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            try {
                MyUtil.trackClick(context, "BiblePlayStore", "M");
                Toast.makeText(getApplicationContext(), R.string.main_find_bible_app, Toast.LENGTH_SHORT).show();
                Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
                downloadIntent.setData(Uri.parse("market://details?id=org.arkist.cnote"));
                //onExitGoodCalendar(context);
                startActivity(downloadIntent);
            } catch (Exception e) {
                MyUtil.trackClick(context, "BiblePlayStoreError", "M");
                Toast.makeText(getApplicationContext(), R.string.main_cannot_find_bible_app, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onClickSelectCalendar(Context context) {
        MyUtil.trackClick(context, "Calendar", "M");
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra(MyUtil.EXTRA_REQUEST, MyUtil.REQUEST_CALENDAR);
        intent.putExtra(MyUtil.EXTRA_TYPE, MyUtil.EXTRA_TYPE_SELECT);
        intent.putExtra(MyUtil.EXTRA_DEFAULT_DATE, MyUtil.sdfYYYYMMDDHHMM.format(mDisplayDay.getTime()));
        resultLauncher.launch(intent);
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    private void onClickToday(Context context) {
        MyUtil.trackClick(context, "Today", "M");
        Calendar newDate = Calendar.getInstance();
        if (isWithinRange(newDate, 0)) {
            MyUtil.sdfYYYYMMDD.setTimeZone(newDate.getTimeZone());
            String newDateStr = MyUtil.sdfYYYYMMDD.format(newDate.getTime());
            mDisplayDay.setTimeZone(newDate.getTimeZone());
            mDisplayDay.setTimeInMillis(newDate.getTimeInMillis());
            onRefreshPage(mDisplayDay, false, true);
            Toast.makeText(context, newDateStr, Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickAbout(Context context) {
        MyUtil.trackClick(context, "About", "M");
        Intent intent = new Intent(this, AboutActivity.class);
        intent.putExtra(MyUtil.EXTRA_REQUEST, MyUtil.REQUEST_ABOUT);
        resultLauncher.launch(intent);
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    private void onClickSupport(Context context) {
        MyUtil.trackClick(context, "Support", "M");
        Intent intent = new Intent(this, SupportActivity.class);
        intent.putExtra(MyUtil.EXTRA_REQUEST, MyUtil.REQUEST_SUPPORT);
        resultLauncher.launch(intent);
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    private void onClickAlarm(Context context) {
        MyUtil.trackClick(context, "Alarm", "M");
        Intent intent = new Intent(this, AlarmActivity.class);
        intent.putExtra(MyUtil.EXTRA_REQUEST, MyUtil.REQUEST_ALARM);
        resultLauncher.launch(intent);
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

//    private void checkUpdateVersion(){
//        getCurrentVersion();
//    }
//    String currentVersion="", latestVersion="";
//    Dialog dialog;
//    private void getCurrentVersion(){
//        PackageManager pm = this.getPackageManager();
//        PackageInfo pInfo = null;
//        try {
//            pInfo =  pm.getPackageInfo(this.getPackageName(),0);
//
//        } catch (PackageManager.NameNotFoundException e1) {
//            e1.printStackTrace();
//        }
//        currentVersion = pInfo.versionName;
//        new GetLatestVersion().execute();
//
//    }
//
//    private class GetLatestVersion extends AsyncTask<String, String, JSONObject> {
//        //private ProgressDialog progressDialog;
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//        @Override
//        protected JSONObject doInBackground(String... params) {
//            try {
////It retrieves the latest version by scraping the content of current version from play store at runtime
//                String playUrl = "https://play.google.com/store/apps/details?id=" + getPackageName();
//                org.jsoup.nodes.Document doc = Jsoup.connect(playUrl).get();
//                latestVersion = doc.getElementsByAttributeValue("itemprop","softwareVersion").first().text();
//            }catch (Exception e){
//                latestVersion ="";
//                e.printStackTrace();
//            }
//            return new JSONObject();
//        }
//
//        @Override
//        protected void onPostExecute(JSONObject jsonObject) {
//            // May be 3.3.1 Vs 3.4
//            Log.e(TAG, "Version latest=" + latestVersion + " current=" + currentVersion);
//            float latestNbr=0;
//            try {
//                latestNbr = Float.valueOf(latestVersion);
//            } catch (Exception e){
//                //
//            }
//            float currentNbr=0;
//            try {
//                currentNbr = Float.valueOf(currentVersion);
//            } catch (Exception e){
//                //
//            }
//
//            if (latestNbr > currentNbr) {// Normal is latestNbr>currentNbr .. do update
//                boolean askBefore=AxTools.getPrefBoolean("AskUpdate"+latestVersion,false);
//                Log.e(TAG, "Find updated version "+currentNbr);
//                if (!askBefore) {
//                    showUpdateDialog();
//                }
//            } else {
//                Log.e(TAG,"No updated version ");
//            }
//            super.onPostExecute(jsonObject);
//        }
//    }

    //    private void showUpdateDialog(){
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("找到新的更新");
//        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
//                            ("market://details?id=" + CMain.this.getPackageName())));
//                } catch (Exception e){
//                    Toast.makeText(CMain.this,"找不到Google Play下載新的程式。", Toast.LENGTH_SHORT).show();
//                    Log.e(TAG,"Request update app but GooglePlay not find");
//                }
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //background.start();
//                AxTools.setPrefBoolean("AskUpdate"+latestVersion,true);
//            }
//        });
//        builder.setCancelable(false);
//        dialog = builder.show();
//    }
    public static boolean isAppInForground(Context context) {
        boolean isInForeground = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInForeground = true;
                    }
                }
            }
        }

        return isInForeground;
    }
}
