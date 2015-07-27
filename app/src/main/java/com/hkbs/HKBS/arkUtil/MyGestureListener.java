package com.hkbs.HKBS.arkUtil;

import android.content.Context;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ViewAnimator;

import com.hkbs.HKBS.R;

public class MyGestureListener implements OnGestureListener{//extends SimpleOnGestureListener
	final static private boolean DEBUG=false;
	final static private String TAG = MyGestureListener.class.getSimpleName();	
	private Callback callback;
	private boolean islongPress=false;
	public interface Callback {
        public boolean onLeft();
        public boolean onRight();
        public boolean onClick(MotionEvent e);
        public boolean onLongPress(MotionEvent e);
    }
	
	public MyGestureListener(Callback callback) {
		this.callback = callback;
	}
	private static final int SWIPE_MIN_DISTANCE = 20;
	private static final int SWIPE_MAX_OFF_PATH = 200;// Y-Axis
	//private static final int SWIPE_THRESHOLD_VELOCITY = 200;	
	public int result=0;
	@Override
	public boolean onDown(MotionEvent e) {
		islongPress=false;
		return false;//		return super.onDown(e);
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (DEBUG) MyUtil.log(TAG,"onFling");
		if (e1==null || e2==null) return false;
		//if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH ) return false;
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE ){// && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
			return this.callback.onRight();
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE ){// && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
			return this.callback.onLeft();			
		}
		return false;
	}
	@Override public void onLongPress(MotionEvent e) {
		if (DEBUG) MyUtil.log(TAG,"onLongPress");
		islongPress=true;
		this.callback.onLongPress(e);
	}
	@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
	@Override public void onShowPress(MotionEvent e) {}
	@Override public boolean onSingleTapUp(MotionEvent e) {
		if (DEBUG) MyUtil.log(TAG,"onSingleTapUp");
		if (!islongPress){
			return this.callback.onClick(e);
		} else {
			return false;
		}
	}
	static public void slideInFromLeft(Context context, ViewAnimator va) { // Same As MyGridLayout
		// ListView outView = starListView[activeList];
		// ListView inView = starListView[activeList==0?1:0];
		va.setInAnimation(context,R.anim.slide_in_right);
		va.setOutAnimation(context,R.anim.slide_out_left);
		va.showNext();
	}
	static public void slideInFromRight(Context context, ViewAnimator va) {
		va.setInAnimation(context,R.anim.slide_in_left);
		va.setOutAnimation(context,R.anim.slide_out_right);
		va.showPrevious();
	}
	static public void flingInFromLeft(Context context, ViewAnimator va) { // Same As MyGridLayout
		// ListView outView = starListView[activeList];
		// ListView inView = starListView[activeList==0?1:0];
		va.setInAnimation(context,R.anim.fling_in_right);
		va.setOutAnimation(context,R.anim.fling_out_left);
		va.showNext();
	}
	static public void flingInFromRight(Context context, ViewAnimator va) {
		va.setInAnimation(context,R.anim.fling_in_left);
		va.setOutAnimation(context,R.anim.fling_out_right);
		va.showPrevious();
	}
}
