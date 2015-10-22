package com.hkbs.HKBS;

public class CWidgetXLarge extends CWidgetBase {
	public CWidgetXLarge() {
	}
    @Override
    public String getClassTag(){
        return "x";
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_cwidget_xlarge;
    }
    @Override
    public String getLayoutTag(){
        return CWidgetXLarge.class.getSimpleName();
    }
    @Override
    public String getClassName(){
        return CWidgetXLarge.class.getName();
    }
}
