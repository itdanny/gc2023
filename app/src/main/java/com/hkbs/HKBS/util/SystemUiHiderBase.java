package com.hkbs.HKBS.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

/**
 * A base implementation of {@link SystemUiHider}. Uses APIs available in all
 * API levels to show and hide the status bar.
 */
public class SystemUiHiderBase extends SystemUiHider {
	/**
	 * Whether or not the system UI is currently visible. This is a cached value
	 * from calls to {@link #hide()} and {@link #show()}.
	 */
	private boolean mVisible = true;

//	/**
//	 * Constructor not intended to be called by clients. Use
//	 * {@link SystemUiHider#getInstance} to obtain an instance.
//	 */
	protected SystemUiHiderBase(Activity activity, View anchorView, int flags) {
		super(activity, anchorView, flags);
	}

	@Override
	public void setup() {
		if ((mFlags & FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES) == 0) {
			mActivity.getWindow()
					.setFlags(
							WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
									| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
							WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
									| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	@Override
	public boolean isVisible() {
		return mVisible;
	}

	@RequiresApi(api = Build.VERSION_CODES.R)
    @Override
	public void hide() {
		if ((mFlags & FLAG_FULLSCREEN) != 0) {
            mActivity.getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
//			mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mOnVisibilityChangeListener.onVisibilityChange(false);
		mVisible = false;
	}

	@RequiresApi(api = Build.VERSION_CODES.R)
    @Override
	public void show() {
		if ((mFlags & FLAG_FULLSCREEN) != 0) {
            mActivity.getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
//			mActivity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mOnVisibilityChangeListener.onVisibilityChange(true);
		mVisible = true;
	}
}
