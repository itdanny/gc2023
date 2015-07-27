package org.arkist.share;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast.Type;
import com.github.johnpersano.supertoasts.util.OnDismissListener;

public class AxTools {
	static private Context mContext;// Application, Service and Broadcast should assign this value
	static public SharedPreferences mPreferences;
	static public enum MsgType {INFO, DETAIL, ERROR, EXPLAIN, PROGRSS};
	static public enum TextSize {BASIC, SMALLER, LARGER};
	
	public AxTools() {
	}
	static public Context getContext(){
		return mContext;
	}
	static public void init(Context context){
		mContext=context;
		mPreferences=PreferenceManager.getDefaultSharedPreferences(context);
	}
	static private List<Resources> resourceList = new ArrayList<Resources>();
	static public void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        	view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            	unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
        view=null;
    }
	static public Resources getResourceByLocale(Context context, Locale locale){
		for (Resources resource : resourceList){
			if (resource.getConfiguration().locale==locale){
				return resource;				
			}
		}
		Resources standardResources = context.getResources();
		AssetManager assets = standardResources.getAssets();
		DisplayMetrics metrics = standardResources.getDisplayMetrics();
		Configuration config = new Configuration(standardResources.getConfiguration());
		config.locale = locale;
		Resources defaultResources = new Resources(assets, metrics, config);
		resourceList.add(defaultResources);
		return defaultResources;
	}
	/*
	 * To get resource dependent width & height:
	 * final WindowManager wm = (WindowManager) getAppContext().getSystemService(Context.WINDOW_SERVICE);
	 * final DisplayMetrics dm = new DisplayMetrics();
	 * wm.getDefaultDisplay().getMetrics(dm);
	 * return dm.heightPixels;
	 */
	public static int getScreenWidth(){// Not App; Not Device but Screen (Rotate may change)
		return (int) Resources.getSystem().getDisplayMetrics().widthPixels;
	}
	public static int getScreenHeight(){
		return (int) Resources.getSystem().getDisplayMetrics().heightPixels;
	}
	public static int dp2px(float dp){
		return (int) (dp * Resources.getSystem().getDisplayMetrics().scaledDensity+0.5f);
	}
	public static float px2dp(int px){
	    return (float) (px / Resources.getSystem().getDisplayMetrics().scaledDensity);
	}
	public static float px2in(int px) {
		return (float) (px / Resources.getSystem().getDisplayMetrics().scaledDensity);
	}	
	public static int px2sp(int px) {
	    return (int) (px / Resources.getSystem().getDisplayMetrics().scaledDensity);
	}
	
//	static public int dp2px(float dpValue) {
//		//int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
//		return (int) (dpValue * scaleDensity() + 0.5f);
//	}
//	static public int px2in(float pxValue) {
//		return (int) (pxValue / scaleDensity());
//	}
//    static public float scaleDensity(){
//    	final DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
//	    return dm.scaledDensity;    	
//    }
//	private void scaleImage(ImageView view, int boundBoxInDp)
//	{
//	    // Get the ImageView and its bitmap
//	    Drawable drawing = view.getDrawable();
//	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();
//
//	    // Get current dimensions
//	    int width = bitmap.getWidth();
//	    int height = bitmap.getHeight();
//
//	    // Determine how much to scale: the dimension requiring less scaling is
//	    // closer to the its side. This way the image always stays inside your
//	    // bounding box AND either x/y axis touches it.
//	    float xScale = ((float) boundBoxInDp) / width;
//	    float yScale = ((float) boundBoxInDp) / height;
//	    float scale = (xScale <= yScale) ? xScale : yScale;
//
//	    // Create a matrix for the scaling and add the scaling data
//	    Matrix matrix = new Matrix();
//	    matrix.postScale(scale, scale);
//
//	    // Create a new bitmap and convert it to a format understood by the ImageView
//	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//	    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
//	    width = scaledBitmap.getWidth();
//	    height = scaledBitmap.getHeight();
//
//	    // Apply the scaled bitmap
//	    view.setImageDrawable(result);
//
//	    // Now change ImageView's dimensions to match the scaled image
//	    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
//	    params.width = width;
//	    params.height = height;
//	    view.setLayoutParams(params);
//	}
//	private int dpToPx(int dp){
//	    float density = _activity.getResources().getDisplayMetrics().density;
//	    return Math.round((float)dp * density);
//	}
	static public void runFollow(Runnable runnable){
		Handler handler = new Handler();
		handler.post(runnable);
	}
	static public void runLater(Runnable runnable){
		Handler handler = new Handler();
		handler.postDelayed(runnable,100);
	}
	static public void runLater(long delayMillis, Runnable runnable){
		Handler handler = new Handler();
		handler.postDelayed(runnable,delayMillis);
	}
	static public Map<String,?> getPrefAll(){
		return mPreferences.getAll();		
	}
	static public void setPrefBoolean(String key, boolean bool){
		mPreferences.edit().putBoolean(key, bool).commit();
	}
	static public boolean getPrefBoolean(String key, boolean defaultValue){
		return mPreferences.getBoolean(key, defaultValue);
	}
	static public void setPrefInt(String key, Integer value){
		mPreferences.edit().putInt(key, value).commit();
	}
	static public Integer getPrefInt(String key, Integer defaultValue){
		return mPreferences.getInt(key, defaultValue);
	}
	static public void setPrefLong(String key, long value){
		mPreferences.edit().putLong(key, value).commit();
	}
	static public long getPrefLong(String key, long defaultValue){
		return mPreferences.getLong(key, defaultValue);
	}
	static public void setPrefStr(String key, String value){
		mPreferences.edit().putString(key, value).commit();
	}
	static public String getPrefStr(String key, String defaultValue){		
		return mPreferences.getString(key, defaultValue);
	}
	static public String padZero(long value, int nbrOfZero){
    	return String.format("%0"+nbrOfZero+"d", value);
    }
	static public int getDigits(String str){
		String checkDigits="";
		int checkDigit;
		for (int i=0;i<str.length();i++){
			checkDigit=str.codePointAt(i);
			if (checkDigit>=48 && checkDigit<=57){ 
				checkDigits+=str.charAt(i);
			} else {
				if (!checkDigits.equals("")) {
					break;
				}
			}
		}
		int result=0;
		try {
			result = Integer.valueOf(checkDigits); 
		} catch (Exception e){}
		return result;
	}
	static public void toast(MsgType msgType, Activity activity, int stringID){
		toast(msgType, activity, activity.getString(stringID));
	}
	static public void toast(MsgType msgType, Activity activity, CharSequence text){
		if (activity==null || activity.isFinishing() ){
			AxDebug.error("*", "Toast without context:"+text);
			//return null;
			return;
		}
		toast(msgType, activity, null, text);
	}
	static public void toast(MsgType msgType, Context context, View cardRootView, CharSequence text){
		// To use SuperCardToast:- LinearLayout with name card_container is required
		int bkgColor=0;
		int duration=2000;
		if (msgType==MsgType.ERROR){
			duration=2750; // Medium
			bkgColor=R.color.axMsgError;
		} else if (msgType==MsgType.INFO){
			duration=2000; // Short
			bkgColor=R.color.axMsgInfo;
		} else if (msgType==MsgType.DETAIL){
			duration=3500;// Long
			bkgColor=R.color.axMsgInfo;
		} else if (msgType==MsgType.EXPLAIN){
			duration=3500; // Long
			bkgColor=R.color.axMsgWarn;
		} else if (msgType==MsgType.PROGRSS){
			duration=2750; // Medium
			bkgColor=R.color.axMsgInfo;			
		}
		int gap = AxTools.dp2px(6);
		Toast customToast = new Toast(context);
		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(0, gap, 0, gap);
		textView.setText(text);
		textView.setTextSize(px2sp((int) AxTools.getSmallerTextSize()));
		textView.setTextColor(context.getResources().getColor(R.color.white));
		textView.setBackgroundResource(bkgColor);
		RelativeLayout relativeLayout = new RelativeLayout(context);
		relativeLayout.addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		customToast.setView(relativeLayout);
		customToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		customToast.setDuration(duration);
		customToast.show();
//		try {
//			SuperCardToast superToast = new SuperCardToast(context,cardRootView,msgType==MsgType.PROGRSS?Type.PROGRESS:Type.STANDARD);
//			if (msgType==MsgType.ERROR){
//				superToast.setDuration(2750); // Medium
//				superToast.setBackgroundResource(R.color.axMsgError);
//			} else if (msgType==MsgType.INFO){
//				superToast.setDuration(2000); // Short
//				superToast.setBackgroundResource(R.color.axMsgInfo);
//			} else if (msgType==MsgType.DETAIL){
//				superToast.setDuration(3500); // Long
//				superToast.setBackgroundResource(R.color.axMsgInfo);
//			} else if (msgType==MsgType.EXPLAIN){
//				superToast.setDuration(3500); // Long
//				superToast.setBackgroundResource(R.color.axMsgWarn);
//			} else if (msgType==MsgType.PROGRSS){
//				superToast.setDuration(2750); // Medium
//				superToast.setBackgroundResource(R.color.axMsgInfo);			
//			}
//			superToast.setTextSize(px2sp((int) AxTools.getSmallerTextSize()));
//			superToast.getTextView().setGravity(Gravity.CENTER);
//			superToast.setText(text);		
//			superToast.show();		
//			return;//superToast;
//		} catch (Exception e){
//			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
//			return;// null;
//		}
	}
	static public SuperCardToast toast(Activity activity, CharSequence text, OnClickListener onClickListener, OnDismissListener onDismissListener){
		if (activity==null || activity.isFinishing() ){
			AxDebug.error("*", "Toast without context:"+text);
			return null;
		}
		SuperCardToast superToast = new SuperCardToast(activity, Type.BUTTON);
		superToast.setDuration(3500); // Long
		//superToast.setBackgroundResource(R.color.msgInfo);
		superToast.setTextSize(px2sp(AxTools.getSmallerTextSize()));
		superToast.setText(text);
		superToast.getTextView().setGravity(Gravity.CENTER);
		superToast.setButtonOnClickListener(onClickListener);
		superToast.setOnDismissListener(onDismissListener);		
		superToast.show();
		return superToast;
	}
	static public void setTextViewSize(TextSize size, TextView view){
		view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size==TextSize.BASIC?AxTools.getBasicTextSize():(size==TextSize.LARGER?AxTools.getLargerTextSize():AxTools.getSmallerTextSize()));			
	}
	static public int getBasicTextSize(){
		return mContext.getResources().getDimensionPixelSize(R.dimen.ax_text_size_basic);
	}
	static public int getLargerTextSize(){
		return (int)(getBasicTextSize()*1.2);
	}
	static public int getSmallerTextSize(){
		return (int)(getBasicTextSize()*0.8);
	}


}
