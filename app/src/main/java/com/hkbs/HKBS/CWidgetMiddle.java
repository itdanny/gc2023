package com.hkbs.HKBS;

public class CWidgetMiddle extends CWidgetBase {
	public CWidgetMiddle() {
	}
    @Override
    public String getClassTag(){
        return "m";
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_cwidget_middle;
    }
    @Override
    public String getLayoutTag(){
        return CWidgetMiddle.class.getSimpleName();
    }
    @Override
    public String getClassName(){
        return CWidgetMiddle.class.getName();
    }
}
