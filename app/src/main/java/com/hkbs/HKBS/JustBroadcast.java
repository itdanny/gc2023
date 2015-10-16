package com.hkbs.HKBS;

import android.os.Bundle;

/**
 * Created by dchow on 16/10/15.
 */
public class JustBroadcast extends MyActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        CWidgetBase.broadcastMe(JustBroadcast.this);
        finish();
    }
}
