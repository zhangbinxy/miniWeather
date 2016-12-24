package com.example.zhangbin.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
//    public String nowCity;

    private TextView currentCity;//点击列表城市选项后页面上边城市名称要随之改变

    private EditText editText;//接收输入城市搜索的字符

    int pyindex2;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.select_city);//设置布局


        mBackBtn = (ImageView) findViewById(R.id.title_back);   //后退图案
        mBackBtn.setOnClickListener(this); //为后退按钮设置点击事件监听器

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        MyApplication myApplication = (MyApplication) getApplication();

        final List<String> data = new ArrayList<>();//存放城市名称
        final List<String> data2 = new ArrayList<>();//存放城市名称，与上面一起完成搜索城市时候的配合
        final List<String> cityID = new ArrayList<>();//存放城市的代号
        final List<String> cityID2 = new ArrayList<>();//存放城市代号，配合完成搜索
        final List<String> cityPY = new ArrayList<>();//存放城市的拼音，全拼

        Iterator<City> it = myApplication.getmCityList().iterator();

        while (it.hasNext()) {//从原来整个database中筛选城市代号、城市名和城市拼音
            City tmp = it.next();
            String cityname1 = tmp.getCity();
            String cityid1 = tmp.getNumber();
            String cityAllPY = tmp.getAllPY();//retrieve the pinyin item from database
            String cityname2 = tmp.getCity();
            data.add(cityname1);
            data2.add(cityname2);
            cityID.add(cityid1);
            cityID2.add(cityid1);
            cityPY.add(cityAllPY.toLowerCase());//insert that item into a list called cityPY，拼音变成小写
        }

        //将数据库中的城市显示到列表中，需要利用适配器adapter
        ListView mlistView = (ListView) findViewById(R.id.list_view);
        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        mlistView.setAdapter(adapter1);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {//设置list view的点击监听
                Toast.makeText(SelectCity.this, "您选择的城市是:" + data.get(i), Toast.LENGTH_SHORT).show();
                selectedID = cityID2.get(i);
                Log.d("it's my test",i+"");
//                nowCity = data.get(i);
//                Log.d("it's my test",data.get(i));

//                currentCity = (TextView) findViewById(R.id.title_city_name);//更新城市列表上方城市名称
//                currentCity.setText("当前城市:" + nowCity);//在bar上显示更新后的城市名称


                Intent intent = new Intent(SelectCity.this, MainActivity.class);//用intent传递选择城市的号码，在点击list view的item后，直接返回主界面并更新选择城市的天气信息
                intent.putExtra("cityCode", selectedID);
                Log.d("cityid", selectedID);
                setResult(RESULT_OK, intent);
                finish();
            }

        });

        //实现输入城市名称的查找
        editText = (EditText) findViewById(R.id.search_edit);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.clear();
                cityID2.clear();
                int pyindex=0;
                //汉字搜索
//                for (String str:data2){
//                    if (str.indexOf(editText.getText().toString())!=-1){
//                        data.add(str);
//                        cityID2.add(cityID.get(pyindex));
//                    }
//                    pyindex++;
//                }
                //拼音搜索
                for (String str1:cityPY){
                    if (str1.indexOf(editText.getText().toString())!=-1){
                        data.add(data2.get(pyindex));
                        cityID2.add(cityID.get(pyindex));
                    }
                    pyindex++;
                }
                adapter1.notifyDataSetChanged();
            }
        });

    }



       @Override
       public void onClick(View v) {        //对于选择城市时有无操作进行区分
           Intent i = new Intent();
           switch (v.getId()) {
               case R.id.title_back:
                   if (selectedID == null) {
                       SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                       selectedID = sp.getString("cityCode", "101010100");
                   }
                   i.putExtra("cityCode", selectedID);//如果点击返回按钮，则硬性规定返回北京的天气
                   setResult(RESULT_OK, i);
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

