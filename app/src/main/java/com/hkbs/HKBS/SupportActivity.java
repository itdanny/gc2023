package com.hkbs.HKBS;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hkbs.HKBS.arkUtil.MyUtil;

import java.util.Calendar;

public class SupportActivity extends MyActivity implements OnClickListener {
	
	private Button btnTaiWan;
	private Button btnHongKong;
	private Button btnOther;
	
	public SupportActivity() {
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_support);
//		TextView hkbs = (TextView) findViewById(R.id.supportHkbsAddress);
//		hkbs.setText(Html.fromHtml(getString(R.string.about_hkbs_address)));
//		TextView arkist = (TextView) findViewById(R.id.supportArkistAddress);
//		arkist.setText(Html.fromHtml(getString(R.string.about_arkist_address)));
		
		btnTaiWan = (Button) findViewById(R.id.xmlSupportTaiWan);
		btnTaiWan.setOnClickListener(this);
		
		btnHongKong = (Button) findViewById(R.id.xmlSupportHongKong);
		btnHongKong.setOnClickListener(this);
		
		
		btnOther = (Button) findViewById(R.id.xmlSupportOther);
		btnOther.setOnClickListener(this);
		
		setCountryColor();
		
		TextView tvVersion = (TextView) findViewById(R.id.xmlSupportVersion);
		String appVersionName = "?";
		try {
			//appVersionName = String.valueOf(getPackageManager().getPackageInfo(getPackageName(),0).versionCode);
            appVersionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
		} catch (Exception e){
			//
		}
		MyDailyBread dailyBread = MyDailyBread.getInstance(SupportActivity.this);
//        setRealDeviceSizeInPixels();
//        DisplayMetrics dm = new DisplayMetrics();
//        if (Build.VERSION.SDK_INT >=17) {
//            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
//        } else {
//            getWindowManager().getDefaultDisplay().getMetrics(dm);
//        }
//        double x = Math.pow(mWidthPixels/dm.xdpi,2);
//        double y = Math.pow(mHeightPixels/dm.ydpi,2);
//        double screenInches = Math.sqrt(x + y);
        String body = "有效開始日："+dailyBread.getValidFrDate().get(Calendar.YEAR)+"."+
								(dailyBread.getValidFrDate().get(Calendar.MONTH)+1)+"."+
								dailyBread.getValidFrDate().get(Calendar.DAY_OF_MONTH)+"\n"+
					  "有效結束日："+dailyBread.getValidToDate().get(Calendar.YEAR)+"."+
								(dailyBread.getValidToDate().get(Calendar.MONTH)+1)+"."+
								dailyBread.getValidToDate().get(Calendar.DAY_OF_MONTH)+"\n"+
					  "牌子:"+android.os.Build.BRAND+"\n"+
					  "型號:"+android.os.Build.MODEL+"\n"+
					  "系統版本:"+android.os.Build.VERSION.RELEASE+"\n"+
					  "程式版本:"+appVersionName+"\n"+
					  "SDK:"+Build.VERSION.SDK_INT+"\n"+
					  "畫面:"+CMain.mScreenType+"\n"+
//                      "大小:"+String.format("%.2f",screenInches)+"\n"+
					  "密度:"+MyUtil.scaleDensity(SupportActivity.this)+"\n"+
					  "解析度:"+MyUtil.widthPixels(SupportActivity.this)+"x"+MyUtil.heightPixels(SupportActivity.this)+"\n";
		tvVersion.setText(body);

	}
    int mHeightPixels;
    int mWidthPixels;
    private void setRealDeviceSizeInPixels() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >=17) {
            display.getRealMetrics(displayMetrics);
        } else {
            display.getMetrics(displayMetrics);
        }

        // since SDK_INT = 1;
        mWidthPixels = displayMetrics.widthPixels;
        mHeightPixels = displayMetrics.heightPixels;

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception ignored) {
            }
        }

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        }
    }
	private void setCountryColor(){
		btnTaiWan.setTextColor(getResources().getColor(R.color.white));
		btnHongKong.setTextColor(getResources().getColor(R.color.white));
		btnOther.setTextColor(getResources().getColor(R.color.white));
		String country = MyUtil.getPrefStr(MyUtil.PREF_COUNTRY, "");
		if (country.equalsIgnoreCase("TW")){
			btnTaiWan.setTextColor(getResources().getColor(R.color.black));
		} else if (country.equalsIgnoreCase("HK")){
			btnHongKong.setTextColor(getResources().getColor(R.color.black));
		} else {
			btnOther.setTextColor(getResources().getColor(R.color.black));
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.xmlSupportTaiWan:
			MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "TW");
			setCountryColor();
			break;
		case R.id.xmlSupportHongKong:
			MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "HK");
			setCountryColor();
			break;
		case R.id.xmlSupportOther:
			MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "CN");
			setCountryColor();
			break;
		}
		
	}
	
}
