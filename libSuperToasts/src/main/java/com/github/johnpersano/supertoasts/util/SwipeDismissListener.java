package com.github.johnpersano.supertoasts.util;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;

@SuppressLint("NewApi")
@SuppressWarnings("javadoc")
public class SwipeDismissListener implements View.OnTouchListener 
{
	
 // Cached ViewConfiguration and system-wide constant values
 private int mScaledTouchSlop;
 private int mMinFlingVelocity;
 private int mMaxFlingVelocity;
 private long mAnimationTime;

 // Fixed properties
 private View mView;
 private OnDismissCallback mCallback;
 private int mViewWidth = 1;

 // Transient properties
 private float mActionDownXCoordinate;
 private boolean isSwiping;
 private VelocityTracker mVelocityTracker;
 private float mTranslationX;


public interface OnDismissCallback {

     void onDismiss(View view);
     
 }


 public SwipeDismissListener(View view, OnDismissCallback callback) 
 {
	 
     final ViewConfiguration mViewConfiguration = ViewConfiguration
    		 .get(view.getContext());
     
     
     mScaledTouchSlop = mViewConfiguration.getScaledTouchSlop();
     mMinFlingVelocity = mViewConfiguration.getScaledMinimumFlingVelocity();
     mMaxFlingVelocity = mViewConfiguration.getScaledMaximumFlingVelocity();
     mAnimationTime = view.getContext().getResources()
    		 .getInteger(android.R.integer.config_shortAnimTime);
     
     mView = view;
     mCallback = callback;
     
 }

 @Override
 public boolean onTouch(View view, MotionEvent motionEvent) 
 {
	 
     motionEvent.offsetLocation(mTranslationX, 0);

     mViewWidth = mView.getWidth();

     switch (motionEvent.getActionMasked()) 
     {
     
     
         case MotionEvent.ACTION_DOWN: 
        	 
        	 mActionDownXCoordinate = motionEvent.getRawX();
             mVelocityTracker = VelocityTracker.obtain();
             mVelocityTracker.addMovement(motionEvent);
             view.onTouchEvent(motionEvent);
             
             return true;
                      

             
         case MotionEvent.ACTION_UP:
        	 
	             if (mVelocityTracker == null) 
	             {
	            	 
	                 break;
	             }

             float deltaXActionUp = motionEvent.getRawX() - mActionDownXCoordinate;
             
             mVelocityTracker.addMovement(motionEvent);
             
             mVelocityTracker.computeCurrentVelocity(1000);
             
             
             float velocityX = Math.abs(mVelocityTracker.getXVelocity());
             float velocityY = Math.abs(mVelocityTracker.getYVelocity());
             
             boolean dismiss = false;
             boolean dismissRight = false;
             
             
	             if (Math.abs(deltaXActionUp) > mViewWidth / 2) 
	             {
	            	 
	                 dismiss = true;
	                 dismissRight = deltaXActionUp > 0;
	                 
	             } 
             
	             else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX) 
	             {
	            	 
	                 dismiss = true;
	                 dismissRight = mVelocityTracker.getXVelocity() > 0;
	                 
	             }
	             
	             
	             if (dismiss) 
	             {
	            	//Dcv72 .... https://code.google.com/p/android/issues/detail?id=18803
	            	 AnimatorSet set = new AnimatorSet();
	            	 set.playTogether(
	            			 ObjectAnimator.ofFloat(mView, "alpha", 1f, 0f),
	            			 ObjectAnimator.ofFloat(mView, "translationX", dismissRight ? mViewWidth : -mViewWidth)
	            			 );
	            	 set.setDuration(mAnimationTime);
	            	 set.removeAllListeners();
	                 set.addListener(new Animator.AnimatorListener() {
	 					@Override public void onAnimationStart(Animator animation) {}
	 					@Override public void onAnimationRepeat(Animator animation) {}
	 					@Override public void onAnimationEnd(Animator animation) {
	 						performDismiss();
	 					}
	 					@Override public void onAnimationCancel(Animator animation) {}
	 				});
	                 set.start();
//	            	 mView.animate()
//	                         .translationX(dismissRight ? mViewWidth : -mViewWidth)
//	                         .alpha(0)
//	                         .setDuration(mAnimationTime)
//	                         .setListener(new AnimatorListenerAdapter(){
//	                             @Override
//	                             public void onAnimationEnd(Animator animation){
//	                                 performDismiss();
//	                             }
//	                         });
	             }
	             
	             else 
	             {
	            	 
	             // User has cancelled action
	            	 
		            //Dcv72 .... https://code.google.com/p/android/issues/detail?id=18803
	            	 AnimatorSet set = new AnimatorSet();
	            	 set.playTogether(
	            			 ObjectAnimator.ofFloat(mView, "alpha", 1f),
	            			 ObjectAnimator.ofFloat(mView, "translationX", 0)
	            			 );
	            	 set.setDuration(mAnimationTime);
	            	 set.removeAllListeners();
	            	 set.start();
	                 
//                 mView.animate()
//                         .translationX(0)
//                         .alpha(1)
//                         .setDuration(mAnimationTime)
//                         .setListener(null);
//                 
	             }
	             
	         mVelocityTracker.recycle();
             mTranslationX = 0;
             mActionDownXCoordinate = 0;
             isSwiping = false;
             
             break;
                      

             
         case MotionEvent.ACTION_MOVE: 
        
	             if (mVelocityTracker == null) 
	             {
	            	 
	                 break;
	                 
	             }

             mVelocityTracker.addMovement(motionEvent);
             
             float deltaXActionMove = motionEvent.getRawX() - mActionDownXCoordinate;
             
	             if (Math.abs(deltaXActionMove) > mScaledTouchSlop) 
	             {
	            	 
	            	 isSwiping = true;
	                 mView.getParent().requestDisallowInterceptTouchEvent(true);
	
	                 // Cancel listview's touch
	                 MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
	                 cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
	                     (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
	                 mView.onTouchEvent(cancelEvent);
	                 
	                 cancelEvent.recycle();
	                 
	             }

	             if (isSwiping) 
	             {
	            	 
	                 mTranslationX = deltaXActionMove;
	                 mView.setTranslationX(deltaXActionMove);
	                 mView.setAlpha(Math.max(0f, Math.min(1f,
	                         1f - 2f * Math.abs(deltaXActionMove) / mViewWidth)));
	                 
	                 return true;
	                 
	             }
             
             break;
         
     }
     
     return false;
 }

 private void performDismiss() 
 {

     final ViewGroup.LayoutParams lp = mView.getLayoutParams();
     final int originalHeight = mView.getHeight();

     ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1)
    		 .setDuration(mAnimationTime);

     animator.addListener(new AnimatorListenerAdapter()
     {
    	 
         @Override
         public void onAnimationEnd(Animator animation) 
         {
        	 
             mCallback.onDismiss(mView);
             
         }
         
     });

     animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() 
     {
    	 
         @Override
         public void onAnimationUpdate(ValueAnimator valueAnimator) 
         {
        	 
             lp.height = (Integer) valueAnimator.getAnimatedValue();
             mView.setLayoutParams(lp);
             
         }
         
     });

     animator.start();
     
 }
 
}
