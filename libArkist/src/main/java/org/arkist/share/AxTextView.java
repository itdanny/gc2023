package org.arkist.share;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class AxTextView extends TextView {
	//static final private String TAG=MyTextView.class.getSimpleName();
	
	private boolean mIsClickAssigned=false;
//	private OnClickListener otherClassAssignedOnClickListener=null;
//	private boolean isJustCreated=true;
	
	public AxTextView(Context context) {
		super(context);
//		setOnClickListener(onClickListener);
	}
	public AxTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		setOnClickListener(onClickListener);
	}
	public AxTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		setOnClickListener(onClickListener);
	}
	private int mColor=0;
	private Style mStyle=Style.DEFAULT;
	public void setStyle(Style style){
		this.mStyle=style;
	}	
//	private ColorStateList getNewColorState(int normalColor, int pressedColor){
//		int[][] states = new int[][] {
//				new int[] { android.R.attr.color}, // color
//				new int[] { android.R.attr.state_focused}, // focus
//			    new int[] { android.R.attr.state_enabled}, // enabled
//			    new int[] { android.R.attr.state_pressed}  // pressed
//		};
//		int[] colors = new int[] {
//				normalColor,
//				normalColor,
//				normalColor,
//				pressedColor,
//		};
//		return new ColorStateList(states, colors);
//	}
	public enum Style {NONE, 
		DEFAULT,
		
		TEXT_MASK,
		TEXT_SAME,
		TEXT_GREEN,
		TEXT_GRAY,
		
		BLUR_MASK,
		BLUR_SAME,
		BLUR_GREEN,
		BLUR_GRAY
		}; // MyTextView.Style.values()[i]
		
//	@Override
//	public void setOnClickListener(OnClickListener onClickListener){
//		if (isJustCreated){
//			isJustCreated=false;
//			super.setOnClickListener(onClickListener);
//		} else {		
//			otherClassAssignedOnClickListener=onClickListener;
//			mIsClickAssigned=onClickListener==null?false:true;
//		}
//	}
//	private OnClickListener onClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			// Caller may change textcolor e.g. white to yellow...do it first...cancel not like AxImageView
//			if (otherClassAssignedOnClickListener!=null){
//				otherClassAssignedOnClickListener.onClick(v);
//			}			
//			if (mIsClickAssigned){
//				boolean anyPressedColor=true;
//				int pressedColor=-1;//None;
//				boolean anyBlurColor=true;
//				int blurColor=-1;
//				mColor=AxTextView.this.getCurrentTextColor();
//				if (mStyle==Style.TEXT_MASK){
//					pressedColor=AxGraphics.getGrayMaskColor(mColor);				
//				} else if (mStyle==Style.TEXT_SAME || mStyle==Style.DEFAULT){
//					pressedColor=mColor;
//				} else if (mStyle==Style.TEXT_GREEN){
//					pressedColor=Color.parseColor("#59fa4b");
//				} else if (mStyle==Style.TEXT_GRAY){
//					pressedColor=Color.parseColor("#919191");
//				} else {
//					anyPressedColor=false;
//				}
//			
//				if (mStyle==Style.BLUR_MASK){
//					blurColor=AxGraphics.getGrayMaskColor(mColor);
//				} else if (mStyle==Style.BLUR_SAME || mStyle==Style.DEFAULT){
//					blurColor=mColor;
//				} else if (mStyle==Style.BLUR_GREEN){
//					blurColor=Color.parseColor("#59fa4b");
//				} else if (mStyle==Style.BLUR_GRAY){
//					blurColor=Color.parseColor("#919191");
//				} else {
//					anyBlurColor=false;
//				}
//				
//				if(anyPressedColor){
//					AxTextView.this.setTextColor(pressedColor);
//				}
//				if(anyBlurColor){
//					AxTextView.this.setShadowLayer(25, 3, 3, blurColor);
//				}
//				
//			}
//			if (mIsClickAssigned){
//				final int restoreColor = mColor;
//				AxTools.runLater(600, new Runnable() {
//					@Override
//					public void run() {
//						AxTextView.this.setShadowLayer(0, 0, 0, Color.parseColor("#000000"));	
//						AxTextView.this.setTextColor(restoreColor);									
//					}
//
//
//				});	
//			}
//		}
//	};
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mIsClickAssigned){
				mColor=this.getCurrentTextColor();
				boolean anyPressedColor=true;
				int pressedColor=-1;//None;
				boolean anyBlurColor=true;
				int blurColor=-1;
				
				if (mStyle==Style.TEXT_MASK){
					pressedColor=AxGraphics.getGrayMaskColor(mColor);				
				} else if (mStyle==Style.TEXT_SAME || mStyle==Style.DEFAULT){
					pressedColor=mColor;
				} else if (mStyle==Style.TEXT_GREEN){
					pressedColor=Color.parseColor("#59fa4b");
				} else if (mStyle==Style.TEXT_GRAY){
					pressedColor=Color.parseColor("#919191");
				} else {
					anyPressedColor=false;
				}
			
				if (mStyle==Style.BLUR_MASK){
					blurColor=AxGraphics.getGrayMaskColor(mColor);
				} else if (mStyle==Style.BLUR_SAME || mStyle==Style.DEFAULT){
					blurColor=mColor;
				} else if (mStyle==Style.BLUR_GREEN){
					blurColor=Color.parseColor("#59fa4b");
				} else if (mStyle==Style.BLUR_GRAY){
					blurColor=Color.parseColor("#919191");
				} else {
					anyBlurColor=false;
				}
				if(anyPressedColor){
					AxTextView.this.setTextColor(pressedColor);
				}
				if(anyBlurColor){
					AxTextView.this.setShadowLayer(25, 3, 3, blurColor);
				}	
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsClickAssigned){
				this.setShadowLayer(0, 0, 0, Color.parseColor("#000000"));	
				this.setTextColor(mColor);
			}
			break;
		}		
		return super.onTouchEvent(event);
	}
	
}
