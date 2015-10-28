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
		
		if (CMain.IS_2016_VERSION){
            ((TextView) findViewById(R.id.xmlAboutCopyright)).setText(Html.fromHtml(getString(R.string.about_copyright2016)));
            ((TextView) findViewById(R.id.aboutAppRule)).setText(Html.fromHtml(getString(R.string.app_rule2016)));
        } else {
            ((TextView) findViewById(R.id.xmlAboutCopyright)).setText(R.string.about_copyright2015);
            ((TextView) findViewById(R.id.aboutAppRule)).setText(Html.fromHtml(getString(R.string.app_rule2015)));
        }


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
