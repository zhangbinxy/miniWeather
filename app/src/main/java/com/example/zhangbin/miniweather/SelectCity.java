package com.example.zhangbin.miniweather;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by zhangbin on 2016/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                finish();
                break;
            default:
                break;
        }
    }
}
