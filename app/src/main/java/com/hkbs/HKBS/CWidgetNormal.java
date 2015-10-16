package com.hkbs.HKBS;
public class CWidgetNormal extends CWidgetBase {
	public CWidgetNormal() {
	}
    @Override
    public int getLayoutId() {
        return R.layout.activity_cwidget;
    }
    @Override
    public String getLayoutTag(){
        return CWidgetNormal.class.getSimpleName();
    }
    @Override
    public String getClassName(){
        return CWidgetNormal.class.getName();
    }
}
