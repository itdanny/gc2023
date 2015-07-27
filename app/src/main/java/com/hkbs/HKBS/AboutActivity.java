package com.hkbs.HKBS;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hkbs.HKBS.arkUtil.MyUtil;

public class AboutActivity extends MyActivity {

	public AboutActivity() {
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		
		TextView about = (TextView) findViewById(R.id.aboutAppRule);
		about.setText(Html.fromHtml(getString(R.string.app_rule)));
		
		Button button = (Button) findViewById(R.id.mainBtnSupport);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyUtil.trackClick(AboutActivity.this, "Support", "M");
				Intent intent = new Intent(AboutActivity.this, SupportActivity.class);
				startActivityForResult(intent, MyUtil.REQUEST_SUPPORT);
				overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			}
		});
		
	}

}
