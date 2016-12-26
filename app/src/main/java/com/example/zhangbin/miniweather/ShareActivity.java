package com.example.zhangbin.miniweather;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by zhangbin on 2016/12/26.
 */

public class ShareActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.share_present);
    }
}
