package com.hkbs.HKBS;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.support.multidex.MultiDexApplication;

import org.arkist.share.AxDebug;
import org.arkist.share.AxTools;

import java.util.Locale;

/**
 * Created by dchow on 28/12/2016.
 */

public class MyApp extends MultiDexApplication {
    final static public String PREF_APP_LANG = "prefAppLang";
    final static public String PREF_APP_LANG_CN = "CN";
    final static public String PREF_APP_LANG_TW = "TW";
    private Locale mAppLocale;
    static public Context mNewLangContext;
    static public boolean mIsSimplifiedChinese;
    @Override
    public void onCreate() {
        super.onCreate();
        AxTools.init(getApplicationContext());
        updateConfig(getApplicationContext());
    }
    private void updateConfig(Context context){
        String lang = AxTools.getPrefStr(PREF_APP_LANG,"");
        Locale curLocale = Locale.getDefault();
        if (lang.equals("")){ // No Default Value
            if (curLocale.getCountry().equalsIgnoreCase(PREF_APP_LANG_CN)){
                lang=PREF_APP_LANG_CN;
            } else {
                lang= PREF_APP_LANG_TW;
            }
            AxTools.setPrefStr(PREF_APP_LANG,lang);
        }
        Locale newLocale;
        if (lang.equalsIgnoreCase(PREF_APP_LANG_CN)){
            mIsSimplifiedChinese = true;
            newLocale = Locale.SIMPLIFIED_CHINESE;
        } else {
            mIsSimplifiedChinese = false;
            newLocale = Locale.TRADITIONAL_CHINESE;
        }
        if (mAppLocale==null || mAppLocale != newLocale) {
            mAppLocale = newLocale;
            updateConfigLocale(context, context.getResources().getConfiguration(), mAppLocale);
        }
    }
    private void updateConfigLocale(Context context, Configuration newConfig, Locale locale){
        if (mAppLocale !=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newConfig.setLocales(new LocaleList(locale));
            } else {
                //noinspection deprecation
                newConfig.locale=locale;
            }
            Locale.setDefault(locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                newConfig.setLayoutDirection(locale);
            }
            AxDebug.info("#","Locale="+locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mNewLangContext = context.createConfigurationContext(newConfig);
            } else {
                //noinspection deprecation
                context.getResources().updateConfiguration(newConfig, context.getResources().getDisplayMetrics());
            }
        }
    }
}
