package com.hkbs.HKBS;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    interface CallBack {
        void clicked(MotionEvent motionEvent);
    }
    public boolean isScrolling=false;
    private float lastX;
    private float lastY;
    public CallBack callBack;
    public CustomViewPager(Context context)  {
        super(context);
    }
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction()==MotionEvent.ACTION_DOWN){
            lastX = ev.getX();
            lastY = ev.getY();
        } else if (ev.getAction()==MotionEvent.ACTION_UP){
            //Log.i("#", " anyCallBack="+(callBack==null?"No":"Yes"));
            if (Math.abs(lastX-ev.getX())<=10 && Math.abs(lastY-ev.getY())<=10 && callBack!=null) {
                callBack.clicked(ev);
                return true;
            }
        }
        return super.onTouchEvent(ev);
    }
}
