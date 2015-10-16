package com.hkbs.HKBS;

public class CWidgetSmall extends CWidgetBase {
    public CWidgetSmall() {
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_cwidget_small;
    }
    @Override
    public String getLayoutTag(){
        return CWidgetSmall.class.getSimpleName();
    }
    @Override
    public String getClassName(){
        return CWidgetSmall.class.getName();
    }
}