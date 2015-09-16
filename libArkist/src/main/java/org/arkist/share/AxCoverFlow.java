package org.arkist.share;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;
import at.technikum.mti.fancycoverflow.ecogallery.EcoGalleryAdapterView;
/*
 * 	private AxCoverFlow coverFlow;
	private void showTabmenu(){
		String [] items = MainActivity.mTabString;	
		int [] images = new int [] {
				R.drawable.img_listen_bible,
				R.drawable.img_read_bible,
				R.drawable.img_calendar,
				R.drawable.img_to_do_list,
				R.drawable.img_note};
		int currentIndex = AxTools.getPrefInt(MyConst.PREF_TabIndex, 0);
		coverFlow = new AxCoverFlow(this, currentIndex, items, images, onItemClick, onClick);
		coverFlow.getWindow().getAttributes().windowAnimations = R.style.AxDialogAnimation; 
		coverFlow.show();		
	}
	final View.OnClickListener onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			System.exit(0);
		}
	};
	final OnItemClickListener onItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			View child = coverFlow.getChildAt(position);
//			if (child!=null){
//				AxImageView image = (AxImageView) child.findViewById(R.id.xmlCoverFlowItemImage);
//				if (image!=null){
//					Log.e(TAG,"FIND IT >>>>>>>>>>>>>>>>>>>>>>>>>>>");
//					image.setStyle(Style.BLUR);
//					image.animateOnClick();
//				}
//			}
			coverFlow.dismiss();
			mPager.setCurrentItem(position);
		}
	};
 */
public class AxCoverFlow extends Dialog {
/*
 *
 * 	private String [] mItems;
	private int [] mImages;
		this.mCoverFlow = new MyCoverFlow(mActivity, mItems, mImages, new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Integer position = (Integer) view.getTag();
				doSetPageSelected(position);
				mCoverFlow.dismiss();
            }
		}); 
		if (mItems.length==2){
			imageView.setVisibility(View.INVISIBLE);
		} else {
			imageView.setVisibility(View.VISIBLE);
		}

 */
	private Activity mActivity;
	private String[] mItems;
	private int[] mImages;
	private CoverFlowAdapter coverFlowAdapter;
	private FancyCoverFlow coverFlow;
	public AxCoverFlow(Activity activity, int startIndex, String[] items, int[] images, EcoGalleryAdapterView.OnItemClickListener onItemClick, android.view.View.OnClickListener onClick) {
		super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

		this.mActivity = activity;
		this.mItems = items;
		this.mImages = images;

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		this.getWindow().setBackgroundDrawableResource(R.color.coverFlow_Background);

		View contentView = (View) activity.getLayoutInflater().inflate(R.layout.coverflow, null);
		AxImageView imageView = (AxImageView) contentView.findViewById(R.id.xmlCoverFlowQuit);
		imageView.setOnClickListener(onClick);

		setContentView(contentView);
		setCanceledOnTouchOutside(false);
		setCancelable(true);

		coverFlow = (FancyCoverFlow) findViewById(R.id.fancyCoverFlow);
		coverFlowAdapter = new CoverFlowAdapter();
		coverFlow.setAdapter(coverFlowAdapter);
		coverFlow.setOnItemClickListener(onItemClick);
		coverFlow.setReflectionEnabled(true);
		coverFlow.setReflectionRatio(0.3f);
		coverFlow.setReflectionGap(0);
		coverFlow.setSelection(startIndex);
	}
	public View getChildAt(int index){
		return coverFlow.getChildAt(index);
	}
	public CoverFlowAdapter getAdapter(){
		return coverFlowAdapter;
	}
	public class CoverFlowAdapter extends FancyCoverFlowAdapter {
		public CoverFlowAdapter() {
		}

		@Override
		public int getCount() {
			return mItems.length;
		}

		public int getItemImage(int i) {
			if (mImages != null && i < mImages.length) {
				return mImages[i];
			} else {
				return 0;
			}
		}

		public String getItemString(int i) {
			if (mItems != null && i < mItems.length) {
				return mItems[i];
			} else {
				return "";
			}
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
			// int viewHeight=0;
			if (reuseableView != null) {
				viewGroup = (ViewGroup) reuseableView;
				// viewHeight=viewGroup.getHeight();
			} else {
				viewGroup = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.coverflow_item, null);
				// viewGroup.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
				// viewHeight=viewGroup.getMeasuredHeight();
			}
			viewGroup.setTag((Integer) i);

			// int boxWidth = (int) (tools.screenWidth() * 0.4);//720->300
			// int boxHeight = (int) (tools.screenHeight() * 0.5);//1280->600

			int boxWidth = AxTools.getScreenWidth();
			int boxHeight = AxTools.getScreenHeight();
			if (boxWidth < boxHeight) {
				boxWidth = (int) (boxWidth * 0.6);// 720->300
				boxHeight = (int) (boxHeight * 0.4);// 1280->600
			} else {
				boxWidth = (int) (boxWidth * 0.4);// 720->300
				boxHeight = (int) (boxHeight * 0.7);// 1280->600
			}

			TextView textView = (TextView) viewGroup.findViewById(R.id.xmlCoverFlowItemText);
			textView.setText(this.getItemString(i));

			ImageView imageView = (ImageView) viewGroup.findViewById(R.id.xmlCoverFlowItemImage);
			int imageID = this.getItemImage(i);
			if (imageID == 0) {
				imageView.setVisibility(View.GONE);
				// ViewGroup.LayoutParams textLP = (ViewGroup.LayoutParams)
				// textView.getLayoutParams();
				// textLP.height = boxHeight;
				// textLP.width = boxWidth;
				// textView.setLayoutParams(textLP);
			} else {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(imageID);
				ViewGroup.LayoutParams imageLP = (ViewGroup.LayoutParams) imageView.getLayoutParams();
				imageLP.width = (int) (Math.max(48, boxHeight * 0.2));
				imageLP.height = imageLP.width;
				imageView.setLayoutParams(imageLP);
			}

			// FancyCoverFlow.LayoutParams lp = new
			// FancyCoverFlow.LayoutParams(boxWidth,
			// boxHeight+mActivity.getResources().getDimensionPixelSize(R.dimen.text_medium)+16);
			// // 16 is padding
			FancyCoverFlow.LayoutParams lp = new FancyCoverFlow.LayoutParams(boxWidth, boxHeight); // 16
																									// is
																									// padding
			viewGroup.setLayoutParams(lp);
			return viewGroup;
		}

		@Override
		public Object getItem(int position) {
			return getItemString(position);
		}
	}
}
