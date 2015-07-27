package com.hkbs.HKBS.arkUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class MySquareView extends RelativeLayout {
	static private RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(10, 10);
	public MySquareView(Context context) {		
		super(context);
	}

	public MySquareView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MySquareView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override // Do Nothing in onLayout as did in onMeasure but should include this routine 
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
       // super.layout(left, top, right, bottom);
    }     
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		//int newWidth=resolveSize(getMeasuredWidth(), widthMeasureSpec);
		int newWidth=MeasureSpec.getSize(widthMeasureSpec);
		int newHeight=(int)(newWidth*1.1);
		
		View child = getChildAt(0);
		lp.width=newWidth;
		lp.height=newHeight;
		child.setLayoutParams(lp);		
		child.layout(0, 0, newWidth, newHeight);
		
		child = getChildAt(1);
		RelativeLayout.LayoutParams iconLP = (RelativeLayout.LayoutParams) child.getLayoutParams();
		child.layout(iconLP.leftMargin, iconLP.topMargin,iconLP.leftMargin+child.getMeasuredWidth(), iconLP.topMargin+child.getMeasuredHeight());
		
		measureChildren(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY);
		setMeasuredDimension(newWidth, newHeight);

	}
}
