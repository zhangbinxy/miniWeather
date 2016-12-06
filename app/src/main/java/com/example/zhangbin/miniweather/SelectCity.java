package com.example.zhangbin.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import cn.edu.pku.zhangbin.bean.City;
import cn.edu.pku.zhangbin.bean.TodayWeather;
import cn.edu.pku.zhangbin.bean.pku.ss.zhangbin.app.MyApplication;

/**
 * Created by zhangbin on 2016/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;

    private GoogleApiClient client;
    public String selectedID;
    public String nowCity;

    private TextView currentCity;//点击列表城市选项后页面上边城市名称要随之改变



    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.select_city);

        //setContentView(R.layout.select_city);//发现多余重复的布局设置！！！！！！！！！！！！

        mBackBtn = (ImageView) findViewById(R.id.title_back);   //后退图案
        mBackBtn.setOnClickListener(this); //点击事件

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        MyApplication myApplication = (MyApplication) getApplication();
        final List <String> data = new ArrayList<>();
        final List <String> cityID = new ArrayList<>();
        Iterator <City> it = myApplication.getmCityList().iterator();

        while(it.hasNext())
        {
            City tmp = it.next();
            String cityname1 = tmp.getCity();
            String cityid1 = tmp.getNumber();
            data.add(cityname1);
            cityID.add(cityid1);
        }

        //
        ListView mlistView = (ListView) findViewById(R.id.list_view);
        ArrayAdapter <String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data);
        mlistView.setAdapter(adapter2);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(SelectCity.this, "你单击了:"+i, Toast.LENGTH_SHORT).show();
                selectedID = cityID.get(i);
                  nowCity = data.get(i);

                currentCity = (TextView) findViewById(R.id.title_city_name);//更新城市列表上方城市名称
                currentCity.setText("当前城市:"+nowCity);//在bar上显示更新后的城市名称


                Intent intent = new Intent(SelectCity.this,MainActivity.class);
                intent.putExtra("cityCode",selectedID);
                Log.d("cityid",selectedID);
                setResult(RESULT_OK,intent);
                finish();







            }
        });
    }

       @Override
       public void onClick(View v){
           switch (v.getId()){
               case R.id.title_back:
                   Intent i = new Intent();
                   i.putExtra("cityCode","101010100");
                   setResult(RESULT_OK,i);
                   finish();
                   break;

               default:
                   break;
           }
       }


    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SelectCity Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}

