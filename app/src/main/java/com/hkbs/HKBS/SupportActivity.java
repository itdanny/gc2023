package com.hkbs.HKBS;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
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

    private Button btnGetSupport;
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

        if (CMain.IS_2016_VERSION){
            findViewById(R.id.supportTechnicalHeader).setVisibility(View.GONE);
            findViewById(R.id.supportContentHeader).setVisibility(View.GONE);
            findViewById(R.id.xmlSupportHKBS_Name).setVisibility(View.GONE);
            findViewById(R.id.xmlSupportHKBS_Addr).setVisibility(View.GONE);
        } else {
            findViewById(R.id.supportTechnicalHeader).setVisibility(View.VISIBLE);
            findViewById(R.id.supportContentHeader).setVisibility(View.VISIBLE);
            findViewById(R.id.xmlSupportHKBS_Name).setVisibility(View.VISIBLE);
            findViewById(R.id.xmlSupportHKBS_Addr).setVisibility(View.VISIBLE);
        }

        btnGetSupport = (Button) findViewById(R.id.supportGetSupport);
        btnGetSupport.setOnClickListener(this);

		btnTaiWan = (Button) findViewById(R.id.xmlSupportTaiWan);
		btnTaiWan.setOnClickListener(this);
		
		btnHongKong = (Button) findViewById(R.id.xmlSupportHongKong);
		btnHongKong.setOnClickListener(this);
		
		
		btnOther = (Button) findViewById(R.id.xmlSupportOther);
		btnOther.setOnClickListener(this);
		
		setCountryColor();
		
		TextView tvVersion = (TextView) findViewById(R.id.xmlSupportVersion);


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

		tvVersion.setText(getAppInfo());

	}
    int mHeightPixels;
    int mWidthPixels;
    private String getAppInfo(){
        MyDailyBread dailyBread = MyDailyBread.getInstance(SupportActivity.this);
        String appVersionName = "?";
        try {
            //appVersionName = String.valueOf(getPackageManager().getPackageInfo(getPackageName(),0).versionCode);
            appVersionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        } catch (Exception e){
            //
        }
        String body =
                "牌子:"+android.os.Build.BRAND+"\n"+
                "型號:"+android.os.Build.MODEL+"\n"+
                "系統版本:"+android.os.Build.VERSION.RELEASE+"\n"+
                "程式版本:"+appVersionName+"\n"+
                "有效開始日："+dailyBread.getValidFrDate().get(Calendar.YEAR)+"."+
                (dailyBread.getValidFrDate().get(Calendar.MONTH)+1)+"."+
                dailyBread.getValidFrDate().get(Calendar.DAY_OF_MONTH)+"\n"+
                "有效結束日："+dailyBread.getValidToDate().get(Calendar.YEAR)+"."+
                (dailyBread.getValidToDate().get(Calendar.MONTH)+1)+"."+
                dailyBread.getValidToDate().get(Calendar.DAY_OF_MONTH)+"\n"+
                "SDK:"+Build.VERSION.SDK_INT+"\n"+
                "畫面:"+CMain.mScreenType+"\n"+
//                      "大小:"+String.format("%.2f",screenInches)+"\n"+
                "密度:"+MyUtil.scaleDensity(SupportActivity.this)+"\n"+
                "解析度:"+MyUtil.widthPixels(SupportActivity.this)+"x"+MyUtil.heightPixels(SupportActivity.this)+"\n";
        return body;
    }
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
		switch (v.getId()) {
            case R.id.supportGetSupport:
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@arkist.org"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "「全年金句日曆」查詢");
            intent.putExtra(Intent.EXTRA_TEXT, "\n以下是正使用裝置資料:\n"+getAppInfo()+"\n");
            startActivity(Intent.createChooser(intent, "電郵 經..."));
            break;
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
