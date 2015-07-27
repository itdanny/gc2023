package org.arkist.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

public class AxPopUpWindow {
	final static private String ITEM_KEY="item_key";
	
	/*
	 * 
	 *  mPopupText = (TextView) child.findViewById(R.id.xmlTabViewTitleText);
		mPopupTitle = (RelativeLayout) child.findViewById(R.id.xmlTabViewTitle);
		mPopupTitle.setOnClickListener(this);
		>> onClick: mPopupPageMenu.show();
	 * private String [] mItems;
	 * this.mPopupPageMenu = new MyPopUpWindow(activity, mItems, new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				doSetPageSelected(position);		
				mPopupPageMenu.mPopWindow.dismiss();
			}
		});
	 *
	 *
	 *private void doSetPageSelected(int pos){
		pos=pos<mItems.length?pos:mItems.length-1;
		int itemNbr;
		switch (mItems.length){
		case 1:
			itemNbr=0;
			break;
		case 2:	
			itemNbr=curIndex==0?1:0;
			break;
		default:
			itemNbr=pos;
			break;
		}	
		curIndex = itemNbr;
		setPageSelected(pos);
	}
	 *
	 */
	
	public PopupWindow mPopWindow;
	
	List<Map<String, String>> moreList;
	private Activity mActivity;	
	private ListView mListView;
	private int nbrOfVisibleListRows;
	private OnItemClickListener mOnItemClick;
	
	public AxPopUpWindow(Activity activity, String [] items, OnItemClickListener onItemClick){
		this.mOnItemClick=onItemClick;
		this.mActivity=activity;
		// Init data
		moreList = new ArrayList<Map<String, String>>();
		for (String item:items){
			Map<String, String> map = new HashMap<String, String>();
			map.put(ITEM_KEY, item);
			moreList.add(map);
		}
		nbrOfVisibleListRows=moreList.size();
		iniPopupWindow();
	}
	private void iniPopupWindow() {
		mListView = (ListView) mActivity.getLayoutInflater().inflate(R.layout.popupwindow_listview, null);
		mListView.setAdapter(new SimpleAdapter(mActivity, moreList,
				R.layout.popupwindow_listitem, new String[] { ITEM_KEY },
				new int[] { R.id.tv_list_item }));
		mListView.setOnItemClickListener(mOnItemClick);
		mListView.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
		
		int rowWidth=mListView.getMeasuredWidth();
		View view = null;
		FrameLayout fakeParent = new FrameLayout(mActivity);
		for (int i=0;i<mListView.getAdapter().getCount();i++){			
			view = mListView.getAdapter().getView(i, view, fakeParent);
			view.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
			rowWidth=Math.max(rowWidth, view.getMeasuredWidth());
		}
		//rowWidth = (int) (rowWidth * 1.05f); // Extra Width; Then extra Space on right; If use, should give left-right space
		
		mPopWindow = new PopupWindow(mListView, rowWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
		mPopWindow.setFocusable(true);
		mPopWindow.setOutsideTouchable(true);
		
		int popupHeight = (AxTools.dp2px(1) * (nbrOfVisibleListRows-1)); // divider
		popupHeight += (mListView.getMeasuredHeight() * nbrOfVisibleListRows); // cell
		int windowHeight = (int) (Resources.getSystem().getDisplayMetrics().heightPixels/2);
		if (popupHeight > windowHeight) mPopWindow.setHeight(windowHeight);
	}
	public void show(View referenceView){
		if (mPopWindow.isShowing()) {
			mPopWindow.dismiss();
		} else {
			// Set background style
			final int windowHalfHeight = (int) (Resources.getSystem().getDisplayMetrics().heightPixels/2); 
			int drawableID;
			int[] loc = new int[2];
			referenceView.getLocationInWindow(loc); // getLocationOnScreen is the Phone location
			if (loc[1] < windowHalfHeight){
				drawableID=R.drawable.popup_window_white_top;
			} else {
				drawableID=R.drawable.popup_window_white_bottom;
			}
			mPopWindow.setBackgroundDrawable(mActivity.getResources().getDrawable(drawableID));	// Without this setting, can't click other space to exit.
			// Now, display (align right with some margin)
			mPopWindow.showAsDropDown(referenceView);
		}	
	}
}
