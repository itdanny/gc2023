package com.hkbs.HKBS;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hkbs.HKBS.arkUtil.MyUtil;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

public class AboutActivity extends MyActivity {

	public AboutActivity() {
		
	}

    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    // ...
                }
            }
    );

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		
		if (CMain.IS_2016_VERSION){
            findViewById(R.id.xmlAboutCopyright).setVisibility(View.GONE);
            //((TextView) findViewById(R.id.xmlAboutCopyright)).setText(Html.fromHtml(getString(R.string.about_copyright2016)));
            ((TextView) findViewById(R.id.aboutAppRule)).setText(HtmlCompat.fromHtml(getString(R.string.app_rule2016), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            ((TextView) findViewById(R.id.xmlAboutCopyright)).setText(R.string.about_copyright2015);
            ((TextView) findViewById(R.id.aboutAppRule)).setText(HtmlCompat.fromHtml(getString(R.string.app_rule2015), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }


		Button button = (Button) findViewById(R.id.mainBtnSupport);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyUtil.trackClick(AboutActivity.this, "Support", "M");
				Intent intent = new Intent(AboutActivity.this, SupportActivity.class);
                startActivityForResult.launch(intent);
				overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			}
		});

		
	}

}
