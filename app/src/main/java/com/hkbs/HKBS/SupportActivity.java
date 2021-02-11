package com.hkbs.HKBS;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hkbs.HKBS.arkUtil.MyAlert;
import com.hkbs.HKBS.arkUtil.MyUtil;

import org.arkist.share.AxDebug;

import java.util.Calendar;

public class SupportActivity extends MyActivity implements OnClickListener {

    private Button btnGetSupport;
	private Button btnTaiWan;
	private Button btnHongKong;
	private Button btnOther;
    private Button btnHolyDayOn;
    private Button btnHolyDayOff;
    private Button btnLangTradition;
    private Button btnLangSimpified;
    private Button btnAdjIncrease;
    private Button btnAdjDecrease;
    private TextView txtAdjust;

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

        btnLangTradition = (Button) findViewById(R.id.xmlLangTraditional);
        btnLangTradition.setOnClickListener(this);
        btnLangSimpified = (Button) findViewById(R.id.xmlLangSimplified);
        btnLangSimpified.setOnClickListener(this);

        btnTaiWan = (Button) findViewById(R.id.xmlSupportTaiWan);
		btnTaiWan.setOnClickListener(this);
		btnHongKong = (Button) findViewById(R.id.xmlSupportHongKong);
		btnHongKong.setOnClickListener(this);

        btnAdjIncrease = (Button) findViewById(R.id.xmlSupportAdjIncrease);
        btnAdjIncrease.setOnClickListener(this);
        btnAdjDecrease = (Button) findViewById(R.id.xmlSupportAdjDecrease);
        btnAdjDecrease.setOnClickListener(this);
        txtAdjust = (TextView) findViewById(R.id.xmlSupportAdjValue);
        txtAdjust.setText(String.valueOf(MyUtil.getPrefInt(MyUtil.PREF_LUNAR_ADJ,0)));

        ((TextView) findViewById(R.id.xmlSupportHK_Holiday_term)).setMovementMethod(LinkMovementMethod.getInstance());

		btnOther = (Button) findViewById(R.id.xmlSupportOther);
		btnOther.setOnClickListener(this);

        setCountryColor();
        setLangColor();

        btnHolyDayOn = (Button) findViewById(R.id.xmlSupportHolyDayOn);
        btnHolyDayOn.setOnClickListener(this);

        btnHolyDayOff = (Button) findViewById(R.id.xmlSupportHolyDayOff);
        btnHolyDayOff.setOnClickListener(this);

        setHolyDayColor();

		TextView tvVersion = (TextView) findViewById(R.id.xmlSupportVersion);

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
                getString(R.string.infoBrand)+android.os.Build.BRAND+"\n"+
                getString(R.string.infoModel)+android.os.Build.MODEL+"\n"+
                getString(R.string.infoSysVersion)+android.os.Build.VERSION.RELEASE+"\n"+
                getString(R.string.infoAppVersion)+appVersionName+"\n"+
                getString(R.string.infoFrDate)+dailyBread.getValidFrDate().get(Calendar.YEAR)+"."+
                        (dailyBread.getValidFrDate().get(Calendar.MONTH)+1)+"."+
                        dailyBread.getValidFrDate().get(Calendar.DAY_OF_MONTH)+"\n"+
                getString(R.string.infoToDate)+dailyBread.getValidToDate().get(Calendar.YEAR)+"."+
                        (dailyBread.getValidToDate().get(Calendar.MONTH)+1)+"."+
                        dailyBread.getValidToDate().get(Calendar.DAY_OF_MONTH)+"\n"+
                "SDK:"+Build.VERSION.SDK_INT+"\n"+
                getString(R.string.infoScreenType)+CMain.mScreenType+"\n"+
//                      "大小:"+String.format("%.2f",screenInches)+"\n"+
                getString(R.string.infoDensity)+MyUtil.scaleDensity(SupportActivity.this)+"\n"+
                getString(R.string.infoResolution)+MyUtil.widthPixels(SupportActivity.this)+"x"+
                        MyUtil.heightPixels(SupportActivity.this)+"\n";
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
    private void setHolyDayColor(){
        if (CMain.IS_2017_VERSION && Calendar.getInstance().get(Calendar.YEAR)>=2017) {
            findViewById(R.id.xmlSupportHolyDayTitle).setVisibility(View.GONE);
            findViewById(R.id.xmlSupportHolyDayButtons).setVisibility(View.GONE);
        } else if (CMain.IS_2016_VERSION) {
            findViewById(R.id.xmlSupportHolyDayTitle).setVisibility(View.VISIBLE);
            findViewById(R.id.xmlSupportHolyDayButtons).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.xmlSupportHolyDayTitle).setVisibility(View.GONE);
            findViewById(R.id.xmlSupportHolyDayButtons).setVisibility(View.GONE);
        }
        int showHolyDay = MyUtil.getPrefInt(MyUtil.PREF_HOLY_DAY,0);
        if (showHolyDay==0){
            btnHolyDayOn.setTextColor(getResources().getColor(R.color.white));
            btnHolyDayOff.setTextColor(getResources().getColor(R.color.black));
        } else {
            btnHolyDayOn.setTextColor(getResources().getColor(R.color.black));
            btnHolyDayOff.setTextColor(getResources().getColor(R.color.white));
        }
    }
    private void setLangColor(){
        btnLangSimpified.setTextColor(getResources().getColor(R.color.white));
        btnLangTradition.setTextColor(getResources().getColor(R.color.white));
        String lang = MyUtil.getPrefStr(MyApp.PREF_APP_LANG, MyApp.PREF_APP_LANG_TW);
        if (lang.equalsIgnoreCase(MyApp.PREF_APP_LANG_CN)){
            btnLangSimpified.setTextColor(getResources().getColor(R.color.black));
        } else {
            btnLangTradition.setTextColor(getResources().getColor(R.color.black));
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
                if (android.os.Build.VERSION.SDK_INT<15) {
                    Toast.makeText(SupportActivity.this,R.string.support_contact, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse(getString(R.string.support_mail_to)));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@arkist.org"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, "\n"+getString(R.string.support_device_info)+"\n" + getAppInfo() + "\n");
                        startActivity(Intent.createChooser(intent, getString(R.string.support_mail_via)));
                    } catch (Exception e) {
                        Toast.makeText(SupportActivity.this,R.string.support_contact, Toast.LENGTH_LONG).show();
                    }
                }
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
        case R.id.xmlLangTraditional:
            if (!MyUtil.getPrefStr(MyApp.PREF_APP_LANG, MyApp.PREF_APP_LANG_TW).equalsIgnoreCase(MyApp.PREF_APP_LANG_TW)){
                MyUtil.setPrefStr(MyApp.PREF_APP_LANG, MyApp.PREF_APP_LANG_TW);
                setLangColor();
                MyAlert.show(SupportActivity.this, getString(R.string.support_restart), new MyAlert.OkListener() {
                            @Override
                            public void ready(MyAlert alert) {
                                restart(SupportActivity.this);
                            }
                        });
            }
            break;
        case R.id.xmlLangSimplified:
            if (!MyUtil.getPrefStr(MyApp.PREF_APP_LANG, MyApp.PREF_APP_LANG_CN).equalsIgnoreCase(MyApp.PREF_APP_LANG_CN)) {
                MyUtil.setPrefStr(MyApp.PREF_APP_LANG, MyApp.PREF_APP_LANG_CN);
                setLangColor();
                // If use simplified word, don't use traditional calendar
                    MyUtil.setPrefStr(MyUtil.PREF_COUNTRY, "CN");
                    setCountryColor();
                // Restart
                MyAlert.show(SupportActivity.this, getString(R.string.support_restart), new MyAlert.OkListener() {
                    @Override
                    public void ready(MyAlert alert) {
                        restart(SupportActivity.this);
                    }
                });
            }
            break;
        case R.id.xmlSupportHolyDayOn:
            MyUtil.setPrefInt(MyUtil.PREF_HOLY_DAY, 1);
            setHolyDayColor();
            break;
        case R.id.xmlSupportHolyDayOff:
            MyUtil.setPrefInt(MyUtil.PREF_HOLY_DAY, 0);
            setHolyDayColor();
            break;
        case R.id.xmlSupportAdjIncrease:
            int newIncrease = MyUtil.getPrefInt(MyUtil.PREF_LUNAR_ADJ,0)+1;
            MyUtil.setPrefInt(MyUtil.PREF_LUNAR_ADJ, newIncrease);
            txtAdjust.setText(String.valueOf(newIncrease));
            MyAlert.show(SupportActivity.this, getString(R.string.support_restart), new MyAlert.OkListener() {
                @Override
                public void ready(MyAlert alert) {
                    restart(SupportActivity.this);
                }
            });
            break;
        case R.id.xmlSupportAdjDecrease:
            int newDecrease = MyUtil.getPrefInt(MyUtil.PREF_LUNAR_ADJ,0)-1;
            MyUtil.setPrefInt(MyUtil.PREF_LUNAR_ADJ, newDecrease);
            txtAdjust.setText(String.valueOf(newDecrease));
            MyAlert.show(SupportActivity.this, getString(R.string.support_restart), new MyAlert.OkListener() {
                @Override
                public void ready(MyAlert alert) {
                    restart(SupportActivity.this);
                }
            });
            break;
		}
        CWidgetBase.broadcastMe(SupportActivity.this);
	}
    private void restart(Activity activity){
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        int mPendingIntentId = 99999;
        PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400, mPendingIntent);

        if (activity!=null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.finishAndRemoveTask();
                    activity.finishAffinity();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    activity.finishAffinity();
                } else {
                    if (!activity.isFinishing()) {
                        activity.finish();
                    }
                }
            } catch (Exception e){
                AxDebug.error(this,e.getMessage());
            }
        }

        ActivityManager am = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
        String currentPackageName = activity.getPackageName();
        am.killBackgroundProcesses(currentPackageName);

        Process.killProcess(Process.myPid());
    }
}
