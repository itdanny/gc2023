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
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.hkbs.HKBS.arkCalendar.MyCalendarLunar;
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
    final static public boolean IS_2016_OR_LATER = true;
    final static private boolean IS_2015_OR_LATER =true;
	final static private boolean DEBUG=true;
	final static private String TAG = CMain.class.getSimpleName();
	final static private String CHI_MONTHS [] = {"一","二","三","四","五","六","七","八","九","十","十一","十二"};
    final static private String STD_LAYOUT = "standard";
    final static private String SMALL_LAYOUT = "small";
    final static private String SW600_LAYOUT = "sw600dp";
	
	final static private int CALL_FROM_EXTERNAL_APP=-999;
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    static public int mCalendarYear=2015;
	static private float _scaleDensity=0;
	static private Calendar mDisplayDay;
	static private String mGoldVerse;
	 
//	private View mContentsView;
	private View mControlsView;
	private View mLeftRightPanel;
	private View mTitleView;
	static public String mScreenType="";
	private LinearLayout page1;
	private LinearLayout page2;
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
                return super.onKeyDown(keyCode,event);
        }
    }

    private GestureDetector mGesture = new GestureDetector(getBaseContext(), new MyGestureListener(new MyGestureListener.Callback() {
		@Override public boolean onClick(MotionEvent e) { // !!! SetClicable to TRUE !!!
			//MyUtil.log(TAG,"mGesture.onClick");
//			if (!isTitleShown){
//				saveScreenShot();
//			}
			setControlsVisibility(!isTitleShown);
			return true;
		}
		@Override public boolean onLongPress(MotionEvent e) {return false;}
		@Override
		public boolean onRight() {
			//MyUtil.log(TAG,"mGesture.onRight");
			gotoNextDay();
			return true;
		}			
		@Override
		public boolean onLeft() {
			//MyUtil.log(TAG,"mGesture.onLeftx");
			gotoPrevDay();
			return true;
		}			
	}));
	private View.OnTouchListener mViewOnTouch = new View.OnTouchListener() {
		@Override public boolean onTouch(View v, MotionEvent event) {
			//MyUtil.log(TAG,"mViewOnTouch.onTouch");
			mGesture.onTouchEvent(event);
			return false;				
		}		
	};
	
	private ViewAnimator mViewAnimator;
	private boolean isTitleShown=true;
	private int mViewIndex=1;
	public MyDailyBread mDailyBread; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		AxTools.init(CMain.this);
		scaleDensity(getApplicationContext());
		MyUtil.initMyUtil(this);
		MyUtil.log(TAG,"StartApp3..............");
		
//		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		final List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
//	    for (int i = 0; i < recentTasks.size(); i++){
//	        MyUtil.log(TAG, "Application executed : " +recentTasks.get(i).baseActivity.toShortString()+ "\t\t ID: "+recentTasks.get(i).id+"");         
//	    }
//		
		setContentView(R.layout.activity_cmain);
		
		mDisplayDay = Calendar.getInstance();
//		mContentsView = findViewById(R.id.xmlMainContents);
		mViewAnimator = (ViewAnimator) findViewById(R.id.xmlMainContents);
		mControlsView = findViewById(R.id.xmlMainControls);
		mLeftRightPanel = findViewById(R.id.xmlMainClickPanel);
		mTitleView = findViewById(R.id.xmlMainTitle);
		mDailyBread = MyDailyBread.getInstance(CMain.this);
		
		onCreateSetButtons();
		
		AxAlarm.setDailyAlarm(CMain.this, AxAlarm.MODE.SET_DEFAULT, 9, 0);
		AxAlarm.setDailyOnDateChange(CMain.this);
		
		CWidgetNormal.broadcastMe(CMain.this);

        MyUtil.log(TAG, "StartApp3..............End");

	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void setControlsVisibility(boolean isVisible){
		if (isTitleShown==isVisible){
			return;
		}
		if (this.isFinishing()) return;
		if (mControlsView==null) mControlsView=findViewById(R.id.xmlMainControls);
		if (mTitleView==null) mTitleView=findViewById(R.id.xmlMainTitle);
		int mControlsHeight=0;
		int mTitleHeight=0;
		int mShortAnimTime=0;
		isTitleShown=isVisible;
		AxTextView mainBtnPlan = (AxTextView) findViewById(R.id.mainBtnPlan);
		if (mainBtnPlan!=null){
			Intent intent = CMain.this.getPackageManager().getLaunchIntentForPackage("org.arkist.cnote");
			if (intent != null){ // Exist
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
			delayedHide(AUTO_HIDE_DELAY_MILLIS);
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
	private void saveScreenShot(){
		// Get bitmap
		View contentView = this.findViewById(android.R.id.content);
		
		int controlVisibility = mControlsView.getVisibility();
		mControlsView.setVisibility(View.GONE);
		int titleVisibility = mTitleView.getVisibility();
		mTitleView.setVisibility(View.GONE);
		View leftView = findViewById(R.id.xmlMainClickLeft);
		int leftVisibility=0; 
		if (leftView!=null){
			leftView.setVisibility(View.GONE);
			leftVisibility = leftView.getVisibility();
		}
		View rightView = findViewById(R.id.xmlMainClickRight);
		int rightVisibility=0; 
		if (rightView!=null){
			rightView.setVisibility(View.GONE);
			rightVisibility = rightView.getVisibility();
		}
		
		contentView.setDrawingCacheEnabled(true);
        //contentView.buildDrawingCache(true);
		screenShot = contentView.getDrawingCache();
        if (screenShot==null){
            screenShot = takeSnapshot(contentView);
            if (screenShot==null){
                screenShot = loadBitmapFromView(contentView);
            }
        }
        //contentView.setDrawingCacheEnabled(false);
		
		mControlsView.setVisibility(controlVisibility);
		mTitleView.setVisibility(titleVisibility);
		if (leftView!=null) leftView.setVisibility(leftVisibility);
		if (rightView!=null) rightView.setVisibility(rightVisibility);
		
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
        if(bitmap!=null) {
            bitmap.recycle();
            bitmap=null;
        }
        bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
                                          /*c.drawBitmap(v.getDrawingCache(), 0, 0, null);
                                          v.destroyDrawingCache();*/
        v.draw(c);
        return bitmap;
    }
    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }
	private void onCreateSetButtons(){
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
			delayedHide(AUTO_HIDE_DELAY_MILLIS);
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
	private void delayedHide(int delayMillis) {
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
	private void popSelectTextImage(Context context, DialogInterface.OnClickListener listener){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("請選擇分享:");
	    String[] options = { "文字", "圖像"};
	    builder.setItems(options, listener);
	    builder.setNegativeButton("取消", null);
	    alert = builder.create();	    
	    alert.show();
	}
	private void onClickCopy(Context context){
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//	    builder.setTitle("請選擇分享:");
//	    String[] options = { "文字", "圖像"};
//	    builder.setItems(options, copyListener);
//	    builder.setNegativeButton("取消", null);
//	    alert = builder.create();	    
//	    alert.show();		
		
		copyText(context);
	}
	private void onClickShare(Context context){
		saveScreenShot();
		popSelectTextImage(context, shareListener);
	}
	private void copyText(Context context){
		int curYear = mDisplayDay.get(Calendar.YEAR);
		int curMonth = mDisplayDay.get(Calendar.MONTH);
		int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
		ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
		final String verse = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ") +
							"["+cv.getAsString(MyDailyBread.wGoldVerse)+" RCUV]";
		MyClipboardManager mgr = new MyClipboardManager();
		mgr.copyToClipboard(CMain.this, verse);
		Toast.makeText(getApplicationContext(), "已把金句放到剪貼薄", Toast.LENGTH_SHORT).show();
		//onClickSupport(CMain.this);
	}
	private void copyImage(Context context){
		// Some Android device not support !! 
	}
    static public Bitmap getThumbnail(Bitmap target, int expectedSize, boolean isByHeight){ // ignore isByHeight
        int width = target.getWidth();
        int height = target.getHeight();
        if (width<= expectedSize && height<= expectedSize) return target;
        Matrix matrix = new Matrix();
        float scale = ((float) expectedSize)/ (height>width?height:width);
        matrix.postScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(target, 0, 0, width, height, matrix, true);
        return result;
    }
	private void shareImage(Context context){
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, "title");
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		//Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,values);
        String fileName =  "pic_" + String.valueOf( System.currentTimeMillis() ) + ".jpg";
        Uri uri= Uri.fromFile(new File( Environment.getExternalStorageDirectory(),fileName));
        //Uri uri = Uri.fromFile(new File(getFilesDir(),fileName));
		OutputStream outstream;
        int tryCounter=1;
        try {
		    outstream = getContentResolver().openOutputStream(uri);
            boolean anyError=false;
            try {
                screenShot.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            } catch (Exception e) {
                anyError = true;
            }
            if (anyError){
                outstream.close();
                outstream = getContentResolver().openOutputStream(uri);
                tryCounter++;
                Bitmap smallBitmap = getThumbnail(screenShot, 640, true);
                if (!smallBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream)){
                    tryCounter++;
                    smallBitmap = getThumbnail(screenShot, 320, true);
                    smallBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                }
            }
            outstream.flush();
		    outstream.close();
		} catch (Exception e) {
            Toast.makeText(this,"Error:"+tryCounter+" "+e.toString(),Toast.LENGTH_LONG).show();
		    //System.err.println(e.toString());
		}
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		share.putExtra(android.content.Intent.EXTRA_SUBJECT, "全年好日曆經文分享");
		share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(share, "分享圖像"));
		
//		Intent share = new Intent(Intent.ACTION_SEND);
//		share.setType("image/jpeg");
//		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/screenshot.png"));
//		startActivity(Intent.createChooser(share, "分享圖像"));
	}
	private void shareText(Context context){	
		int curYear = mDisplayDay.get(Calendar.YEAR);
		int curMonth = mDisplayDay.get(Calendar.MONTH);
		int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);
		ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);
		final String verse = cv.getAsString(MyDailyBread.wGoldText).replace("#", " ") +
							"["+cv.getAsString(MyDailyBread.wGoldVerse)+"；和合本修訂版]";
		MyUtil.trackClick(context, "Share", "M");
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "全年好日曆經文分享");//getResources().getString(R.string.app_name)
		intent.putExtra(android.content.Intent.EXTRA_TEXT, verse);
		startActivity(Intent.createChooser(intent, getResources().getString(R.string.app_name)));
	}
	static final public String [] BOOKS_ENG = new String [] {
		"GEN","EXO","LEV","NUM",
        "DEU","JOS","JDG","RUT",
        "1SA","2SA","1KI","2KI",
        "1CH","2CH","EZR","NEH",
        "EST","JOB","PSA","PRO",
        "ECC","SNG","ISA","JER",
        "LAM","EZK","DAN","HOS",
        "JOL","AMO","OBA","JON",
        "MIC","NAM","HAB","ZEP",
        "HAG","ZEC","MAL","MAT",
        "MRK","LUK","JHN","ACT",
        "ROM","1CO","2CO","GAL",
        "EPH","PHP","COL","1TH",
        "2TH","1TI","2TI","TIT",
        "PHM","HEB","JAS","1PE",
        "2PE","1JN","2JN","3JN",
        "JUD","REV"
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

	static final public String [][] BOOKS_CHT = new String [][] {
		{"1","創世記","創"},
		{"2","出埃及記","出"},
		{"3","利未記","利"},
		{"4","民數記","民"},
		{"5","申命記","申"},
		{"6","約書亞記","書"},
		{"7","士師記","士"},
		{"8","路得記","得"},
		{"9","撒母耳記上","撒上"},
		{"10","撒母耳記下","撒下"},
		{"11","列王紀上","王上"},
		{"12","列王紀下","王下"},
		{"13","歷代志上","代上"},
		{"14","歷代志下","代下"},
		{"15","以斯拉記","拉"},
		{"16","尼希米記","尼"},
		{"17","以斯帖記","斯"},
		{"18","約伯記","伯"},
		{"19","詩篇","詩"},
		{"20","箴言","箴"},
		{"21","傳道書","傳"},
		{"22","雅歌","歌"},
		{"23","以賽亞書","賽"},
		{"24","耶利米書","耶"},
		{"25","耶利米哀歌","哀"},
		{"26","以西結書","結"},
		{"27","但以理書","但"},
		{"28","何西阿書","何"},
		{"29","約珥書","珥"},
		{"30","阿摩司書","摩"},
		{"31","俄巴底亞書","俄"},
		{"32","約拿書","拿"},
		{"33","彌迦書","彌"},
		{"34","那鴻書","鴻"},
		{"35","哈巴谷書","哈"},
		{"36","西番雅書","番"},
		{"37","哈該書","該"},
		{"38","撒迦利亞書","亞"},
		{"39","瑪拉基書","瑪"},
		{"40","馬太福音","太"},
		{"41","馬可福音","可"},
		{"42","路加福音","路"},
		{"43","約翰福音","約"},
		{"44","使徒行傳","徒"},
		{"45","羅馬書","羅"},
		{"46","哥林多前書","林前"},
		{"47","哥林多後書","林後"},
		{"48","加拉太書","加"},
		{"49","以弗所書","弗"},
		{"50","腓立比書","腓"},
		{"51","歌羅西書","西"},
		{"52","帖撒羅尼迦前書","帖前"},
		{"53","帖撒羅尼迦後書","帖後"},
		{"54","提摩太前書","提前"},
		{"55","提摩太後書","提後"},
		{"56","提多書","多"},
		{"57","腓利門書","門"},
		{"58","希伯來書","來"},
		{"59","雅各書","雅"},
		{"60","彼得前書","彼前"},
		{"61","彼得後書","彼後"},
		{"62","約翰一書","約一"},
		{"62","約翰二書","約二"},
		{"62","約翰三書","約三"},
		{"65","猶大書","猶"},
		{"66","啟示錄","啟"}
	};
	static String digits = "0123456789";
	static public Object[] getEBCV(String bcv){
		int digitFirstPos=0;
		for (int i=0;i<bcv.length();i++){
			if (digits.contains(bcv.substring(i, i+1))){
				digitFirstPos=i;
				break;
			}			
		}
		int bookNbr=0;
		String bookName = bcv.substring(0, digitFirstPos).trim();
		bookName.replace("一","壹");
		bookName.replace("二","貳");
		bookName.replace("三","參");
		String bookAbbrev="";
		for (int i=0;i<BOOKS_CHT.length;i++){
			if (bookName.equalsIgnoreCase(BOOKS_CHT[i][1])){
				bookAbbrev = BOOKS_CHT[i][2];
				bookNbr=i+1;
				break;
			}
		}
		if (bookAbbrev.equalsIgnoreCase("")){
			MyUtil.logError(TAG, "Cannot find:"+bookName);
			return null;
		}
		int digitStopPos=0;
		for (int i=digitFirstPos+1;i<bcv.length();i++){
			if (digits.contains(bcv.substring(i, i+1))){
				continue;
			} else {
				digitStopPos=i;
				break;
			}
		}
		if (digitStopPos==0){
			MyUtil.logError(TAG, "Cannot find digitStopPos");
			return null;
		}
		int chapter = Integer.valueOf(bcv.substring(digitFirstPos, digitStopPos));
		int secondDigitStartPos=0;
		for (int i=digitStopPos;i<bcv.length();i++){
			if (digits.contains(bcv.substring(i, i+1))){
				secondDigitStartPos=i;
				break;
			}		
		}
		int verse=0;
		if (secondDigitStartPos==0){
			// if cannot find then we suppose the first stop digit is for verse, not chapter
			//MyUtil.logError(TAG, "Cannot find secondDigitStartPos");
			//return null;
			verse = chapter;
			chapter = 1;			
		} else {
			int digitEndPos=0;
			for (int i=secondDigitStartPos+1;i<bcv.length();i++){
				if (digits.contains(bcv.substring(i, i+1))){			
					continue;
				} else {
					digitEndPos=i;
					break;
				}
			}
			if (digitEndPos==0){
				digitEndPos=bcv.length();
			}
			verse = Integer.valueOf(bcv.substring(secondDigitStartPos, digitEndPos));
		}
		Object [] eabcv = new Object [] {
				"tr ",
				bookAbbrev,
				Integer.valueOf(bookNbr),
				Integer.valueOf(chapter),
				Integer.valueOf(verse)
				};
		//String finalBCV = bookAbbrev+" "+chapter+":"+verse;
		return eabcv;//"tr "+finalBCV;
		//return "rc 創 5:2";
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (DEBUG) MyUtil.log(TAG,"onActivityResult:"+requestCode+","+resultCode);
		switch (requestCode) {
		case MyUtil.REQUEST_CALENDAR:
			if (resultCode==RESULT_OK) {	
				long newTime = intent.getLongExtra(MyUtil.EXTRA_SELECT_MILLSEC,0);
				if (DEBUG) MyUtil.log(TAG, "CMain:"+newTime);
				if (newTime!=0){
					Calendar newDate = Calendar.getInstance();
					newDate.setTimeInMillis(newTime);
					if (isWithinRange(newDate)){
						mDisplayDay.setTimeInMillis(newTime);
					}					
				}
			}
			overridePendingTransition(R.anim.zoom_enter,R.anim.zoom_exit);
			break;
		case MyUtil.REQUEST_ABOUT:
			overridePendingTransition(R.anim.zoom_enter,R.anim.zoom_exit);
			break;
		case MyUtil.REQUEST_SUPPORT:
			overridePendingTransition(R.anim.zoom_enter,R.anim.zoom_exit);
			break;
		}
	}
	private boolean isWithinRange(Calendar checkDate){
		if (checkDate.compareTo(mDailyBread.getValidToDate())>0 ||
			checkDate.compareTo(mDailyBread.getValidFrDate())<0 ){
			Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
		
	}
	@Override
	public void onStop(){
		super.onStop();
		cleanAnimation();
	}
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
        onRefreshPage(mViewIndex);
        MyUtil.log(TAG, "onResumeAfterOnRefreshPage");
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
	@Override
	protected void onPause() {
		super.onPause();
//		if (android.os.Build.VERSION.SDK_INT < 11) {
//			if (mBroadcast!=null){
//				unregisterReceiver(mBroadcast);
//			}
//		}
	}
	private void cleanAnimation(){
		mViewAnimator.setInAnimation(null);
		mViewAnimator.setOutAnimation(null);
	}
	private int getID(int nbr, String extension){
		int resultVal = getResources().getIdentifier("xmlPage"+nbr+extension, "id", "com.hkbs.HKBS");
		if (resultVal==0){
			MyUtil.logError(TAG, "Error on:" + "xmlPage" + nbr + extension);
		}
		return resultVal; 
	}
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
	private void gotoPrevDay(){
		if (mDisplayDay.compareTo(mDailyBread.getValidFrDate())<=0){
			Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
		} else {
			mDisplayDay.add(Calendar.DAY_OF_MONTH, -1);
			mViewIndex=mViewIndex==1?2:1;
			onRefreshPage(mViewIndex);
			MyGestureListener.flingInFromRight(getApplicationContext(), mViewAnimator);			
		}
	}
    private void gotoPriorMonth(){
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(mDisplayDay.getTimeInMillis());
        newCalendar.add(Calendar.MONTH,-1);
        if (newCalendar.compareTo(mDailyBread.getValidFrDate())<=0){
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
        } else {
            mDisplayDay.setTimeInMillis(newCalendar.getTimeInMillis());
            mViewIndex=mViewIndex==1?2:1;
            onRefreshPage(mViewIndex);
            MyGestureListener.flingInFromRight(getApplicationContext(), mViewAnimator);
        }
    }
	private void gotoNextDay(){
		if (mDisplayDay.compareTo(mDailyBread.getValidToDate())>=0){
			Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
		} else {
			mDisplayDay.add(Calendar.DAY_OF_MONTH, +1);
			mViewIndex=mViewIndex==1?2:1;
			onRefreshPage(mViewIndex);
			MyGestureListener.flingInFromLeft(getApplicationContext(), mViewAnimator);			
		}
	}
    private void gotoNextMonth(){
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(mDisplayDay.getTimeInMillis());
        newCalendar.add(Calendar.MONTH,+1);
        if (newCalendar.compareTo(mDailyBread.getValidToDate())>=0){
            Toast.makeText(getApplicationContext(), "超出支援顯示範圍", Toast.LENGTH_SHORT).show();
        } else {
            mDisplayDay.setTimeInMillis(newCalendar.getTimeInMillis());
            mViewIndex=mViewIndex==1?2:1;
            onRefreshPage(mViewIndex);
            MyGestureListener.flingInFromLeft(getApplicationContext(), mViewAnimator);
        }
    }
	private void onRefreshPage(int nbr){
		
		if (mDisplayDay.compareTo(mDailyBread.getValidToDate())>0){
			mDisplayDay.setTimeInMillis(mDailyBread.getValidToDate().getTimeInMillis());
		} else if (mDisplayDay.compareTo(mDailyBread.getValidFrDate())<0 ){
			mDisplayDay.setTimeInMillis(mDailyBread.getValidFrDate().getTimeInMillis());	
		}
		int curYear = mDisplayDay.get(Calendar.YEAR);
		int curMonth = mDisplayDay.get(Calendar.MONTH);
		int curDay = mDisplayDay.get(Calendar.DAY_OF_MONTH);

		final MyCalendarLunar lunar = new MyCalendarLunar(mDisplayDay);
		final Calendar monthEndDate = (Calendar) mDisplayDay.clone();
		String holiday = MyHoliday.getHolidayRemark(mDisplayDay.getTime());
		final boolean isHoliday = ((!holiday.equals("")) & !holiday.startsWith("#")) || mDisplayDay.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY;
		if (holiday.startsWith("#")){
			holiday=holiday.substring(1);
		}
		int textColor = getResources().getColor(isHoliday?R.color.holiday:R.color.weekday);
		
		int nbrOfDaysTo30 = 30-lunar.getDay(); // Chinese Day
		monthEndDate.add(Calendar.DAY_OF_MONTH, nbrOfDaysTo30);
		final MyCalendarLunar monthEndLunar = new MyCalendarLunar(monthEndDate);
		boolean isBigMonth = (monthEndLunar.getDay()==30)?true:false;

		TextView screenTypeView = (TextView) findViewById(getID(nbr, "ScreenType"));
		mScreenType="";
		if (screenTypeView!=null){
			mScreenType= screenTypeView.getTag().toString();
			if (!DEBUG) screenTypeView.setText("");
		}		
		
//		MyUtil.log(TAG, "Set On Touch "+MyUtil.sdfYYYYMMDDHHMM.format(Calendar.getInstance().getTime()));
//		final View view = (View) findViewById(R.id.xmlMainContents);
//		view.setOnTouchListener(mViewOnTouch);
		// Assign Touch Listener
		page1 = (LinearLayout) findViewById(R.id.xmlPage1);
		page2 = (LinearLayout) findViewById(R.id.xmlPage2);
		if (nbr==1) {
			page1.setOnTouchListener(mViewOnTouch);
			page2.setOnTouchListener(null);
		} else {
			page1.setOnTouchListener(null);
			page2.setOnTouchListener(mViewOnTouch);
		}
		
		AxImageView clickLeftView = (AxImageView) findViewById(R.id.xmlMainClickLeft);
		clickLeftView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
				gotoPrevDay();
			}
		});
		AxImageView clickRightView = (AxImageView) findViewById(R.id.xmlMainClickRight);
		clickRightView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
				gotoNextDay();
			}
		});
		
		// Assign Date Related TextView		
		final TextView pageDay = (TextView) findViewById(getID(nbr, "Day"));
		pageDay.setText(String.valueOf(curDay));
		pageDay.setTextColor(textColor);
//		pageDay.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) Math.floor(MyUtil.widthPixels(CMain.this)/3));
//		pageDay.setOnTouchListener(mDelayHideTouchListener);
//		pageDay.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {				
//				MyUtil.log(TAG, "click");
//				MyUtil.popDate(CMain.this,MyDailyBread.getDayString(mDisplayDay), dateListener);
//			}
//		});
		final int maxCharacters=7;
		final TextView pageHoliday1 = (TextView) findViewById(getID(nbr, "Holiday1"));
		final TextView pageHoliday2 = (TextView) findViewById(getID(nbr, "Holiday2"));
		if (holiday.equals("")){
			pageHoliday1.setVisibility(View.GONE);
			pageHoliday2.setVisibility(View.GONE);
		} else {
			pageHoliday1.setVisibility(View.VISIBLE);
			// Split Empty String will cause 1st one be empty; So we remove 1st and add back		
			String str [] = holiday.substring(1).split(""); 
			str[0] = holiday.substring(0,1);
			//ok now.
			int remarkLength = Math.min(maxCharacters*2,str.length);
			int prefixlength = Math.min(maxCharacters, str.length);
			String holidayRemark="";
			for (int i=0;i<prefixlength;i++){
				holidayRemark+=str[i]+(i==(prefixlength-1)?"":"\n");
			}
			pageHoliday1.setLines(prefixlength);
			pageHoliday1.setText(holidayRemark);
			pageHoliday1.setTextColor(textColor);		
			if (remarkLength>maxCharacters){
				pageHoliday2.setVisibility(View.VISIBLE);
				if (remarkLength==maxCharacters+1){// Just one more character; Move last one to next line
					holidayRemark="";
					for (int i=0;i<prefixlength-1;i++){
						holidayRemark+=str[i]+(i==(prefixlength-2)?"":"\n");
					}
					pageHoliday1.setLines(prefixlength-1);
					pageHoliday1.setText(holidayRemark);
					holidayRemark="";
					for (int i=maxCharacters-1;i<remarkLength;i++){
						holidayRemark+=str[i]+(i==(remarkLength-1)?"":"\n");
					}
					pageHoliday2.setLines(2);
				} else {			
					holidayRemark="";
					for (int i=maxCharacters;i<remarkLength;i++){
						holidayRemark+=str[i]+(i==(remarkLength-1)?"":"\n");
					}
					pageHoliday2.setLines(remarkLength-prefixlength);
				}
				pageHoliday2.setText(holidayRemark);
				pageHoliday2.setTextColor(textColor);				
			} else {
				pageHoliday2.setVisibility(View.GONE);
			}
		}
		
		
		final TextView pageEngYear = (TextView) findViewById(getID(nbr, "EngYear"));
		pageEngYear.setText(String.valueOf(curYear));
		pageEngYear.setTextColor(textColor);
		
		final TextView pageEngMonthName = (TextView) findViewById(getID(nbr, "EngMonthName"));
		pageEngMonthName.setText(MyUtil.sdfEngMMMM.format(mDisplayDay.getTime()));
		pageEngMonthName.setTextColor(textColor);
		
		final TextView pageChiMonthName = (TextView) findViewById(getID(nbr, "ChiMonthName"));
		//pageChiMonthName.setText(MyUtil.sdfChiMMMM.format(mDisplayDay.getTime()));
		pageChiMonthName.setText(CHI_MONTHS[mDisplayDay.get(Calendar.MONTH)]+"月");
		pageChiMonthName.setTextColor(textColor);
				
		final TextView pageWeekDay = (TextView) findViewById(getID(nbr, "WeekDay"));
//		String weekDayStr = MyUtil.sdfChiEEEE.format(mDisplayDay.getTime());
//		if (weekDayStr.length()<=2) {
//			weekDayStr = "";
//		} else {
//			weekDayStr = weekDayStr + " "; 
//		}
//		pageWeekDay.setText(weekDayStr+
//							MyUtil.sdfEEE.format(mDisplayDay.getTime()).toUpperCase());//sdfEngEEEE
		int theDay = mDisplayDay.get(Calendar.DAY_OF_WEEK);
		switch (theDay){
		case Calendar.SUNDAY: pageWeekDay.setText("星期日 SUN"); break;
		case Calendar.MONDAY: pageWeekDay.setText("星期一 MON"); break;
		case Calendar.TUESDAY: pageWeekDay.setText("星期二 TUE"); break;
		case Calendar.WEDNESDAY: pageWeekDay.setText("星期三 WED"); break;
		case Calendar.THURSDAY: pageWeekDay.setText("星期四 THU"); break;
		case Calendar.FRIDAY: pageWeekDay.setText("星期五 FRI"); break;
		case Calendar.SATURDAY: pageWeekDay.setText("星期六 SAT"); break;		
		}		

//		pageWeekDay.setBackgroundResource(isHoliday?R.drawable.round_corner_holiday:R.drawable.round_corner_weekday);
//		BitmapDrawable result = MyUtil.scaleDrawable(getResources().getDrawable(R.drawable.green_weekday_2015), pageWeekDay.getWidth());
//		pageWeekDay.setBackgroundDrawable(result);
		final ImageView pageWeekImage = (ImageView) findViewById(getID(nbr, "WeekImage"));
        if (IS_2016_OR_LATER) {
            pageWeekImage.setImageResource(isHoliday ? R.drawable.red_weekday_2016 : R.drawable.green_weekday_2016);
            //pageWeekDay.setTextColor(textColor);
        } else {
            pageWeekImage.setImageResource(isHoliday ? R.drawable.red_weekday_2015 : R.drawable.green_weekday_2015);
        }
        pageWeekDay.setTextColor(getResources().getColor(R.color.white));

		final TextView pageChiLunarDay = (TextView) findViewById(getID(nbr, "ChiLunarDay"));
		pageChiLunarDay.setText(lunar.toChineseDD()+"日");
		pageChiLunarDay.setTextColor(textColor);
		
		final TextView pageChiLunarMonth = (TextView) findViewById(getID(nbr, "ChiLunarMonth"));
		pageChiLunarMonth.setText(lunar.toChineseMM()+(isBigMonth?"大":"小"));
		pageChiLunarMonth.setTextColor(textColor);
		
		final TextView pageChiLunarYear = (TextView) findViewById(getID(nbr, "ChiLunarYear"));
		pageChiLunarYear.setText(lunar.toChineseYY()+"年");
		pageChiLunarYear.setTextColor(textColor);
		
		final String solarTerm=MyCalendarLunar.solar.getSolarTerm(mDisplayDay);
		
		final TextView pageChiLeftYear = (TextView) findViewById(getID(nbr, "ChiLeftYear"));
		pageChiLeftYear.setText(lunar.toChineseYY()+"年");
		pageChiLeftYear.setTextColor(textColor);
		
		final TextView pageChiLeftWeather = (TextView) findViewById(getID(nbr, "ChiLeftWeather"));
		pageChiLeftWeather.setText(solarTerm);
		pageChiLeftWeather.setTextColor(textColor);
		
		if (solarTerm.equals("")){
			pageChiLeftWeather.setVisibility(View.GONE);
			pageChiLeftYear.setVisibility(View.GONE);
			pageChiLunarYear.setVisibility(View.VISIBLE);
		} else {
			pageChiLeftWeather.setVisibility(View.VISIBLE);
			pageChiLeftYear.setVisibility(View.VISIBLE);
			pageChiLunarYear.setVisibility(View.GONE);			
		}
        final ImageView pageImageFrameUpper = (ImageView) findViewById(getID(nbr, "ImageFrameUpper"));
        final ImageView pageImageFrameLower = (ImageView) findViewById(getID(nbr, "ImageFrameLower"));
        final ImageView pageImageFrame = (ImageView) findViewById(getID(nbr, "ImageFrame"));
		if (IS_2016_OR_LATER) {
            pageImageFrameUpper.setImageDrawable(getResources().getDrawable(isHoliday ? R.drawable.red_frame_2016_upper : R.drawable.green_frame_2016_upper));
            pageImageFrameLower.setImageDrawable(getResources().getDrawable(isHoliday ? R.drawable.red_frame_2016_lower : R.drawable.green_frame_2016_lower));
            pageImageFrameUpper.setVisibility(View.VISIBLE);
            pageImageFrameLower.setVisibility(View.VISIBLE);
            pageImageFrame.setVisibility(View.GONE);
        } else {
            pageImageFrame.setImageDrawable(getResources().getDrawable(isHoliday?R.drawable.red_frame_2015:R.drawable.green_frame_2015));
            pageImageFrameUpper.setVisibility(View.GONE);
            pageImageFrameLower.setVisibility(View.GONE);
            pageImageFrame.setVisibility(View.VISIBLE);
        }

        final ImageView pageImageIcon = (ImageView) findViewById(getID(nbr, "ImageIcon"));
        if (CMain.IS_2016_OR_LATER) {
            pageImageIcon.setImageDrawable(getResources().getDrawable(isHoliday ? R.drawable.red_icon_2016 : R.drawable.green_icon_2016));
        } else {
            if (curYear >= 2016) {
                pageImageIcon.setImageDrawable(null);
            } else {
                pageImageIcon.setImageDrawable(getResources().getDrawable(isHoliday ? R.drawable.red_icon_2015 : R.drawable.green_icon_2015));
            }
        }

		// get ContentValues from dailyBread file
		ContentValues cv = mDailyBread.getContentValues(curYear, curMonth, curDay);

		/****************************************************************************************
         *  GOLD TEXT 金句
         ************************************************************************************/
		
		final TextView pageGoldText = (TextView) findViewById(getID(nbr, "GoldText"));
        String theText = cv.getAsString(MyDailyBread.wGoldText);
        final String text [] = theText.split("#");
        int charPerLines=0;
        for (int i=0;i<text.length;i++){
            charPerLines=Math.max(charPerLines,text[i].length());
        }
        pageGoldText.setTextColor(textColor);
		int goldFontSize;
		if (MyDailyBread.mCurrentYear>=2015){
            // 2015.09.16 To protect small device overflow, we don't allow 4 lines
            if (charPerLines>=22 && text.length==3){//Relocate characters automatically (But, should be smaller size to occupy)
                theText=theText.replace("#","");
                charPerLines = (int) Math.ceil(theText.length() / 4);
                theText = theText.substring(0,charPerLines)+"#"+
                          theText.substring(charPerLines,charPerLines+charPerLines)+"#"+
                          theText.substring(charPerLines+charPerLines,charPerLines+charPerLines+charPerLines)+"#"+
                          theText.substring(charPerLines+charPerLines+charPerLines);
                if (DEBUG) {
                    Log.e(TAG, "OldText=" + cv.getAsString(MyDailyBread.wGoldText));
                    Log.e(TAG, "NewText=" + theText);
                }
                if (mScreenType.equalsIgnoreCase(SMALL_LAYOUT)) {
                    goldFontSize = getFontSizeByText(pageGoldText, theText) - dp2px(2);
                } else {
                    goldFontSize = getFontSizeByText(pageGoldText, theText);
                }
            } else {
                goldFontSize = getFontSizeByText(pageGoldText, theText);
            }
            pageGoldText.setTextSize(TypedValue.COMPLEX_UNIT_PX,goldFontSize);
		} else {
			goldFontSize = getGoldFontSize(pageGoldText,cv.getAsString(MyDailyBread.wGoldSize));
			if (mScreenType.equalsIgnoreCase(SMALL_LAYOUT)){
				pageGoldText.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize-dp2px(3));
			} else {
				pageGoldText.setTextSize(TypedValue.COMPLEX_UNIT_PX,goldFontSize);
			}
		}
        pageGoldText.setText(theText.replace("#", "\n"));

        String goldAlign = cv.getAsString(MyDailyBread.wGoldAlign);
        if (curYear<=2012 || curYear>=2016){
            goldAlign="C";
        }
        if (goldAlign.equalsIgnoreCase("L")){
            pageGoldText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (goldAlign.equalsIgnoreCase("C")){
            pageGoldText.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        } else {
            pageGoldText.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        }

        /****************************************************************************************
         *  GOLD VERSE TEXT 金句經文出處
         ************************************************************************************/

		final TextView pageGoldVerse = (TextView) findViewById(getID(nbr, "GoldVerse"));
		pageGoldVerse.setText(cv.getAsString(MyDailyBread.wGoldVerse)+(curYear>=2016?"":"；和合本修訂版"));
		pageGoldVerse.setTextColor(textColor);
		// From HKBS, size change to less than Gold
        if (MyDailyBread.mCurrentYear>=2015 & charPerLines>20){
            // If size is too small, just a little bit different
            pageGoldVerse.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize - dp2px(1));
        } else {
            if (mScreenType.equalsIgnoreCase(SMALL_LAYOUT)) {
                pageGoldVerse.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize - dp2px(6));
            } else if (mScreenType.equalsIgnoreCase(SW600_LAYOUT)) {
                pageGoldVerse.setTextSize(TypedValue.COMPLEX_UNIT_PX, pageChiLunarMonth.getTextSize());
            } else {
                pageGoldVerse.setTextSize(TypedValue.COMPLEX_UNIT_PX, goldFontSize - dp2px(6));
            }
        }
        if (curYear>=2010){
            pageGoldVerse.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        if (curYear>=2015){
            if (mScreenType.equalsIgnoreCase(STD_LAYOUT)) {
                RelativeLayout.LayoutParams goldVerseLP = (RelativeLayout.LayoutParams) pageGoldVerse.getLayoutParams();
                goldVerseLP.setMargins(goldVerseLP.leftMargin, goldVerseLP.topMargin, goldVerseLP.rightMargin, dp2px(22));
                pageGoldVerse.setLayoutParams(goldVerseLP);
            }
        }
        mGoldVerse = cv.getAsString(MyDailyBread.wGoldVerse);

		/****************************************************************************************
		 *  HINT BIG TEXT (心靈雞湯）: pageBigText (1st line smaller); (SIZE IS SMALLER THAN GOLD TEXT)
		 ************************************************************************************/
		final TextView pageBigText = (TextView) findViewById(getID(nbr, "BigText"));
		// Any Heading ":"
		String colonText = ":"; // English Style colon
		String bigText = cv.getAsString(MyDailyBread.wBigText);
		int colonPos=0;
		int englishColonPos = bigText.indexOf(":");
		int chineseColonPos = bigText.indexOf("：");
		if (englishColonPos<0){
			if (chineseColonPos>=0){
				colonPos = chineseColonPos;
			}  else {
				colonPos = -1;
			}
		} else {
			if (chineseColonPos>=0){
				colonPos = Math.min(englishColonPos, chineseColonPos);
			} else {
				colonPos = englishColonPos;
			}			
		}
        String lines [] = bigText.split("#");
		if (colonPos>=0){
			bigText= "<small>"+bigText.substring(0, colonPos+1)+"</small>"+bigText.substring(colonPos+1).replace("#", "<br>");
			bigText=bigText+"<br>";
			pageBigText.setText(Html.fromHtml(bigText));
		} else {
			pageBigText.setText(bigText.replace("#", "\n"));
		}
        pageBigText.setTextColor(textColor);
		int bigTextSize;
        if (MyDailyBread.mCurrentYear>=2015 & charPerLines>20){
            // If size is too small, just a little bit different
            if (mScreenType.equalsIgnoreCase(SW600_LAYOUT) || mScreenType.equalsIgnoreCase(SMALL_LAYOUT)) {
                bigTextSize = (int) pageChiLunarMonth.getTextSize();
            } else {
                bigTextSize = (int) (pageGoldText.getTextSize() - dp2px(2));
            }
        } else {
            if (mScreenType.equalsIgnoreCase(SW600_LAYOUT) || mScreenType.equalsIgnoreCase(SMALL_LAYOUT)) {
                bigTextSize = (int) pageChiLunarMonth.getTextSize();
            } else {
                bigTextSize = (int) (pageGoldText.getTextSize() - dp2px(4));
            }
        }
		pageBigText.setTextSize(TypedValue.COMPLEX_UNIT_PX, bigTextSize);
        RelativeLayout.LayoutParams bigTextLP = (RelativeLayout.LayoutParams) pageBigText.getLayoutParams();
        if (curYear>=2016) {
            pageBigText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            int margin=Math.min(bigTextLP.leftMargin,bigTextLP.rightMargin);
            bigTextLP.setMargins(margin,bigTextLP.topMargin,margin,bigTextLP.bottomMargin);
            pageBigText.setLayoutParams(bigTextLP);
        } else {
            pageBigText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
        if (DEBUG) {
            Log.d(TAG, "screenType=" + mScreenType + " Hint TextSize=" + bigTextSize + " " + pageBigText.getHeight()+" "+curYear+"-"+(curMonth+1)+"-"+curDay);
        }
        /***********************************************************************************
         * 最後一行字 : pageHintText (SIZE IS STANDARD)
         ************************************************************************************/
		final TextView pageHintText = (TextView) findViewById(getID(nbr, "BigHint"));
            pageHintText.setText(cv.getAsString(MyDailyBread.wSmallText));
            pageHintText.setTextColor(textColor);
        // From HKBS, same size or little smaller
		int hintSize = getBigFontSize(pageBigText,"S");
		//Log.i(TAG,"Dialog:"+diagnol(CMain.this)+" Big Font Size:"+bigTextSize);		
		if (mScreenType.equalsIgnoreCase(SMALL_LAYOUT)){
			hintSize = (int) (pageBigText.getTextSize() - dp2px(2));
		} else if (mScreenType.equalsIgnoreCase(STD_LAYOUT)){
			hintSize = (int) (pageBigText.getTextSize() - dp2px(3));
		} else {
			hintSize = (int) (pageBigText.getTextSize() - dp2px(4));//hintSize=hintSize-dp2px(diagnol(CMain.this)>7.0?6:4);
		}
		pageHintText.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintSize);//DC: 2013.12.12
	}
	private int getFontSizeByText(TextView textView, String str){
		String textLines [] = str.split("#");
		int maxChars=0;
		for (int i=0;i<textLines.length;i++){
			maxChars = Math.max(maxChars, textLines[i].length());
		}
        // Control characters not too big; Value bigger Letter Smaller
		if (mScreenType.equalsIgnoreCase(SMALL_LAYOUT)){
			maxChars = maxChars < 16 ? 16 : maxChars;
		} else if (mScreenType.equalsIgnoreCase(SW600_LAYOUT)){
			maxChars = maxChars < 17 ? 17 : maxChars;// 17 change to 19 since 小米Note cannot display
		} else {
			maxChars = maxChars < 18 ? 18 : maxChars;
		}
		int fontSize = getFontSizeByMaxCharacters(textView,maxChars);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		return fontSize; 
	}
	private int getFontSizeByMaxCharacters(TextView textView, int maxCharacters){
		RelativeLayout.LayoutParams lpGold = (RelativeLayout.LayoutParams) textView.getLayoutParams();
		int fontSize = (int) Math.floor((MyDailyBread.mAppWidth - 
					 dp2px(page1.getPaddingLeft()) - dp2px(page1.getPaddingRight()) - 
			       	 dp2px(lpGold.leftMargin) - dp2px(lpGold.rightMargin) - 
			       	 dp2px(textView.getPaddingLeft()) - dp2px(textView.getPaddingRight())) 
			       	 /
			       	maxCharacters);
		// Only Note has problem
		if (android.os.Build.MODEL.equalsIgnoreCase("MID") && MyUtil.heightPixels(CMain.this)==1232 && MyUtil.widthPixels(CMain.this)==800){
			fontSize = (int) Math.floor(fontSize * 0.9); 
		}
		if (DEBUG) MyUtil.log(TAG,    		
    			"Chars:"+maxCharacters+
    			" WIDTH:"+ MyDailyBread.mAppWidth+
    			","+dp2px(lpGold.leftMargin)+
    			","+dp2px(lpGold.rightMargin)+    			
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
	public static int dp2px(float dpValue) {
		//int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
		return (int) (dpValue * _scaleDensity + 0.5f);
	}
    public static double diagnol(Context context){
    	final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	final DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
    	final float heightInches = dm.heightPixels /dm.ydpi;
        final float widthInches = dm.widthPixels /dm.xdpi;
        return Math.sqrt((heightInches*heightInches)+(widthInches*widthInches));
    }
	public static float scaleDensity(Context context){
    	if (_scaleDensity==0){
	    	final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    	final DisplayMetrics dm = new DisplayMetrics();
	        wm.getDefaultDisplay().getMetrics(dm);
	        _scaleDensity = dm.scaledDensity;
    	} 
    	return _scaleDensity;
    }
	private void onClickViewBible(Context context){
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
		Toast.makeText(context, "你將會離開「全年好日曆」並進入其他網站或程式 !", Toast.LENGTH_SHORT).show();
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
			String displayStr = MyUtil.sdfYYYYMMDD.format(mDisplayDay.getTime());			
			int slideDirection = newDateStr.compareTo(displayStr);
			if (slideDirection!=0){
				mDisplayDay.setTimeInMillis(newDate.getTimeInMillis());
				mViewIndex=mViewIndex==1?2:1;
				onRefreshPage(mViewIndex);
				if (slideDirection>0){				
					MyGestureListener.flingInFromLeft(getApplicationContext(), mViewAnimator);
				} else if (slideDirection<0){
					MyGestureListener.flingInFromRight(getApplicationContext(), mViewAnimator);
				}
			}
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
}
