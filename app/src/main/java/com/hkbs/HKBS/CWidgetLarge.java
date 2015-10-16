package com.hkbs.HKBS;

public class CWidgetLarge extends CWidgetBase {
	public CWidgetLarge() {
	}
    @Override
    public int getLayoutId() {
        return R.layout.activity_cwidget_large;
    }
    @Override
    public String getLayoutTag(){
        return CWidgetLarge.class.getSimpleName();
    }
    @Override
    public String getClassName(){
        return CWidgetLarge.class.getName();
    }
}
