package org.arkist.share;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class AxImageView extends ImageView {
	final static private boolean DEBUG = false;
	final static private String TAG = AxImageView.class.getSimpleName();
	final static private int EFFECT_TIME = 350;
	final static private int CLICK_TIME = 200;//Response Action is faster than effect
	private Drawable mMaskedDrawable;
	private Drawable mDrawable;
	private Bitmap mColorBitmap;
	
	final static int UNKNOWN = -999;
	private int mIconColor=UNKNOWN;
	private String mText="";
	private int mTextSize=14;
	private int mTextColor=UNKNOWN;
	private Paint mPaint=null;
	private Drawable mColorDrawable=null;
	private Style mStyle=Style.DEFAULT;
	private int mStyleOrdinal;
	public enum Style {DEFAULT, DARKER, BLUR, LIGHTER, INVERT, NONE};
	private Rect mTextBounds = new Rect();
	private OnClickListener otherClassAssignedOnClickListener=null;
	private boolean isJustCreated=true;
	private boolean mIsClickAssigned=false;
	private int mResourceId=-1;
	
	public AxImageView(Context context) {
		super(context);
		init(context, null, 0);
	}
	public AxImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public AxImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	@Override
    public void draw(Canvas canvas) {		
		super.draw(canvas);
		if (mText!=null && !mText.equals("")){
			if (mPaint==null){
				mPaint = new Paint();
		        mPaint.setAlpha(255);
		        mPaint.setTextAlign(Paint.Align.CENTER);
		        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		        mPaint.setTextSize(mTextSize);
		        if (mTextColor!=UNKNOWN || mIconColor!=UNKNOWN){
		        	mPaint.setColor(mTextColor==UNKNOWN?mIconColor:mTextColor);
		        }
			}
			if (mTextColor!=UNKNOWN){ 
				if (mPaint.getColor()!=mTextColor) mPaint.setColor(mTextColor);
			} else if (mIconColor!=UNKNOWN){
				if (mPaint.getColor()!=mIconColor) mPaint.setColor(mIconColor);
			}
			mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
			int textHeight = mTextBounds.bottom - mTextBounds.top;
			Rect bounds = new Rect(0,0,getWidth(),getHeight()); //getBounds();			
			canvas.drawText(mText, bounds.right / 2, (((float) bounds.bottom + textHeight + 1) / 2), mPaint);
		}
    }
	public void setTextAndTextSize(String text, int textSize, int textColor){
		mText=text;
		mTextSize=textSize;
		mTextColor=textColor;
		invalidate();
	}
	public String getText(){
		return mText;
	}
	public int getTextSize(){
		return mTextSize;
	}
	public int getTextColor(){
		return mTextColor;
	}
	private void setColorImageBitmap(){
		/*
         * DCv62: Some users report nullPointException on bitmap;
         * Add try/catch to protect; Additionally add declare-styleable icon_src 
         */        
		/*
		 * DCv62: Method 1 ... some device not work ... don't know why
		 */
//		int src_resource = this.attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
//      Bitmap bitmap = getDrawable(Resources.getSystem(),src_resource); 
//      this.mColorBitmap = AxGraphics.getColorBitmap(bitmap,mIconColor);
//      this.setImageBitmap(mColorBitmap);		
		/*
		 * DCv62: Method 2
		 */
		try {//DCv73 .... some device 2.2.2 has problem; use normal one
			if (mColorDrawable==null){
				mColorDrawable = getDrawable();
			}
			this.mMaskedDrawable=null;
			this.mColorBitmap = AxGraphics.getColorBitmap(AxGraphics.drawableToBitmap(mColorDrawable),mIconColor);
		    this.setImageBitmap(mColorBitmap);		    
		} catch (Exception e){
			AxDebug.error(this, "Error:"+e.getStackTrace().toString());
			this.mColorBitmap = null;
		}					
	}
//	private Bitmap getDrawable(Resources res, int id){
//    return BitmapFactory.decodeStream(res.openRawResource(id));
//	}
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context
                    .obtainStyledAttributes(attrs, R.styleable.AxImageView,
                            defStyle, 0);            
            mIconColor = a.getColor(R.styleable.AxImageView_icon_color, UNKNOWN);
            if (mIconColor!=UNKNOWN){
            	if (DEBUG) AxDebug.debug(this,"init");
            	mColorDrawable = a.getDrawable(R.styleable.AxImageView_icon_src);
            	setColorImageBitmap();	            
            }            
            mText = a.getString(R.styleable.AxImageView_icon_text);
            mTextSize = a.getDimensionPixelSize(R.styleable.AxImageView_icon_text_size, 14);
            mTextColor = a.getColor(R.styleable.AxImageView_icon_text_color, UNKNOWN);
            mStyleOrdinal = a.getInt(R.styleable.AxImageView_icon_style, 1);
            if (mStyleOrdinal<Style.values().length){
            	mStyle = Style.values()[mStyleOrdinal];
            }
            a.recycle();            
        }
		setOnClickListener(runLaterOnClickListener);		
	}
	public void clearCache(){
		mMaskedDrawable=null;
	}
	public void setIconColorResource(int color, int resourceID){		
		mIconColor=color;
		mColorDrawable = getContext().getResources().getDrawable(resourceID);
		if (DEBUG) AxDebug.debug(this,"setIconColorResource");
		setColorImageBitmap();
		invalidate();
	}
	public void setIconColor(int color){
		if (mIconColor==UNKNOWN || mIconColor!=color){ // Check whether color is changed
			mIconColor=color;
			if (DEBUG) AxDebug.debug(this,"setIconColor");
			setColorImageBitmap();
			invalidate();
		}
	}
	public int getIconColor(){
		return mIconColor;
	}
	public void setStyle(Style style){
		this.mStyle=style;
	}
	public void setToggleImage(int resId){
		mResourceId=resId;
		setImageResource(resId);
	}
	@Override
	public void setImageResource(int resId){ // Set src; Not background		
		clearCache();
		super.setImageResource(resId);
	}
	@Override
	public void setOnClickListener(OnClickListener onClickListener){
		if (isJustCreated){
			isJustCreated=false;
			super.setOnClickListener(runLaterOnClickListener);
		} else {
			super.setOnClickListener(runLaterOnClickListener);
			otherClassAssignedOnClickListener=onClickListener;
			mIsClickAssigned=onClickListener==null?false:true;
		}
	}
//	public void animateOnClick(){
//		try {
//			boolean isClickAssigned=mIsClickAssigned;
//			mIsClickAssigned=true;
//			OnClickListener temp = otherClassAssignedOnClickListener;
//			otherClassAssignedOnClickListener=null;
//			onClickListener.onClick(this);
//			mIsClickAssigned=isClickAssigned;
//			otherClassAssignedOnClickListener=temp;
//		} catch (Exception e){
//			Log.e(TAG, "Error on aminateOnClick:"+e.getMessage());
//		}
//	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mIsClickAssigned){
				mDrawable = AxImageView.this.getDrawable();
				if (mDrawable!=null){
					if (mMaskedDrawable==null){
						if (mIconColor==UNKNOWN || mColorBitmap==null){
							mMaskedDrawable=getMaskDrawable(mDrawable);
						} else {
							Drawable drawable = new BitmapDrawable(Resources.getSystem(),mColorBitmap);
							mMaskedDrawable=getMaskDrawable(drawable);							
						}
					}
					if (DEBUG) Log.e(TAG,"SET NEW NEW BITMAP........mColorBitmap==null=>"+(mColorBitmap==null?"Y":"N"));
					AxImageView.this.setImageDrawable(mMaskedDrawable);
					AxImageView.this.invalidate();
				} else {
					if (DEBUG) Log.e(TAG,"NO DRAWABLE ........");
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsClickAssigned){
				if (mDrawable!=null){//Sometimes, screen will refresh and make effect not clear
					final AxImageView imageView = AxImageView.this;
					AxTools.runLater(EFFECT_TIME, new Runnable() {
						@Override
						public void run() {
							if (DEBUG) Log.e(TAG,"RESTORE NEW NEW BITMAP........");
							if (mResourceId==-1){
								if (mIconColor==UNKNOWN || mColorBitmap==null){
									imageView.setImageDrawable(mDrawable);
								} else {
									imageView.setImageBitmap(mColorBitmap);
								}
							} else {
								imageView.setImageResource(mResourceId);
							}
						}
					});					
				}
			}
			break;
		}		
		return super.onTouchEvent(event);
	}
	private OnClickListener runLaterOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v){
			final OnClickListener runLaterOnClick = otherClassAssignedOnClickListener;
			final View runLaterView = v;
			if (mIsClickAssigned){
				AxTools.runLater(CLICK_TIME, new Runnable() {
					@Override
					public void run() {
						//try {
							runLaterOnClick.onClick(runLaterView);
						//} catch (Exception e){
						//	AxDebug.showError(this, "Unknown error on click later..."+e.getMessage());
						//}
					}
				});
			}
		}
	};
	private BitmapDrawable getMaskDrawable(Drawable drawable){
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		if (mStyle==Style.DARKER || mStyle==Style.DEFAULT){
			if (DEBUG) Log.e(TAG,"Darker/Default");
			// bitmap = getContrast(bitmap, 0.1);
			// bitmap = MyGraphics.getGreyScale(bitmap);
			bitmap = AxGraphics.getBrightness(bitmap, -80);
		}
		if (mStyle==Style.LIGHTER){
			if (DEBUG) Log.e(TAG,"Lighter");
			bitmap = AxGraphics.getBrightness(bitmap, +80);
		}
		if (mStyle==Style.INVERT){
			if (DEBUG) Log.e(TAG,"Invert");
			bitmap = AxGraphics.getInvert(bitmap);
		}
		BitmapDrawable bitmapDrawable;
	    if (mStyle==Style.BLUR || mStyle==Style.DEFAULT){
	    	if (DEBUG) Log.e(TAG,"Blur/Default");
	    	bitmapDrawable = new BitmapDrawable(Resources.getSystem(),AxGraphics.getHighlight(bitmap));
	    } else {
	    	if (DEBUG) Log.e(TAG,"Others");
	    	bitmapDrawable = new BitmapDrawable(Resources.getSystem(),bitmap);
	    }	    	
	    return bitmapDrawable;
	}
}
