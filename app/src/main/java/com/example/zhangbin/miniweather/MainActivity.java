package com.example.zhangbin.miniweather;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zhangbin.bean.TodayWeather;
import cn.edu.pku.zhangbin.bean.pku.ss.zhangbin.Tomorrow1;
import util.NetUtil;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.Poi;

public class MainActivity extends Activity implements View.OnClickListener ,ViewPager.OnPageChangeListener {

    private static final int UPDATE_TODAY_WEATHER = 1;


    private ImageView mUpdateBtn,location;

    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, temperatureTvNow,climateTv,
            windTv, windTvOrient, city_name_Tv,tDay1,tDay2,tDay3,tDay4,tDay5,tDay6,tWen1,tWen2,tWen3,tWen4,tWen5,tWen6,tType1,
            tType2,tType3,tType4,tType5,tType6,tFeng1,tFeng2,tFeng3,tFeng4,tFeng5,tFeng6;
    private ImageView weatherImg, pmImg,tType1Img,tType2Img,tType3Img,tType4Img,tType5Img,tType6Img;


    private View medium1,medium2;//.....................................
    //更新按钮、点击旋转
    private ProgressBar progressBar;

    //六日天气的滑动界面对象
    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;


    private ImageView[] dots;
    private int[] ids = {R.id.iv1,R.id.iv2,R.id.iv3};//小圆点数组



    public Tomorrow1[] strings1 = new Tomorrow1[]{new Tomorrow1(),new Tomorrow1(),new Tomorrow1(),new Tomorrow1(),new Tomorrow1(),new Tomorrow1()};


    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    //定位功能
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    //change the background picture 随着今日天气变换背景图片
    LinearLayout linearLayout;
    Drawable drawable;
    Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.d(TAG, "MainActivity->Oncreate");
        setContentView(R.layout.weather_info);          //把布局设置在主界面上


        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        location = (ImageView) findViewById(R.id.title_location);//定位功能按钮，设置监听器
        location.setOnClickListener(this);


        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn); //跟新的按钮图片来源
        mUpdateBtn.setOnClickListener(this);                           //这个更新图标可以点击，设置监听器
      //  progressBar = (ProgressBar) findViewById(R.id.progressbar);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {     //检查网络连接状况
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK!", Toast.LENGTH_LONG).show();  //如果网络连接上，就弹出消息，2秒后自动消失
        } else {
            Log.d("myWeather", "网络挂了");                                             //如果网络没有连接上，弹出消息，2秒后自动消失
            Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager); //城市管理图片来源
        mCitySelect.setOnClickListener(this);                              //图片可点击，设置监听器

        pmImg = (ImageView) findViewById(R.id.pm2_5_img);//这个有什么用？？？？？？？？？？？？？？？？？
        //pmImg.setImageIcon();

        linearLayout = (LinearLayout) findViewById(R.id.citybackgroundpic);//找到要改变的布局
        resources = getResources();//得到所有图片资源



        initViews(); //初始化六日天气的页面

        initView(); //初始化主界面上的布局信息

        initDots();//忘记了在这里初始化，所以一直报错：空指针异常。解决之后好开心！！！！！！！六日天气滑动页面的圆点

        initLocation();//初始化定位功能

    }



    void initDots(){
        dots = new ImageView[views.size()];
        for(int j=0;j<views.size();j++){
            dots[j] = (ImageView)findViewById(ids[j]);
        }
    }



    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        medium1 = inflater.inflate(R.layout.page1,null);
        medium2 = inflater.inflate(R.layout.page2,null);
        views.add(medium1);
        views.add(medium2);
        views.add(inflater.inflate(R.layout.page3,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);


    }

   private void initView() {

        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        temperatureTvNow = (TextView) findViewById(R.id.temperatureNow);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        windTvOrient = (TextView) findViewById(R.id.windorient);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        temperatureTvNow.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        windTvOrient.setText("N/A");


       progressBar = (ProgressBar) findViewById(R.id.title_update_progress);

        tDay1 = (TextView) medium1.findViewById(R.id.textViewdate1);
        tDay2 = (TextView) medium1.findViewById(R.id.textViewdate2);
        tDay3 = (TextView) medium1.findViewById(R.id.textViewdate3);
        tDay4 = (TextView) medium2.findViewById(R.id.textViewdate4);
        tDay5 = (TextView) medium2.findViewById(R.id.textViewdate5);
        tDay6 = (TextView) medium2.findViewById(R.id.textViewdate6);

        tWen1 = (TextView) medium1.findViewById(R.id.textViewTemp1);
        tWen2 = (TextView) medium1.findViewById(R.id.textViewTemp2);
        tWen3 = (TextView) medium1.findViewById(R.id.textViewTemp3);
        tWen4 = (TextView) medium2.findViewById(R.id.textViewTemp4);
        tWen5 = (TextView) medium2.findViewById(R.id.textViewTemp5);
        tWen6 = (TextView) medium2.findViewById(R.id.textViewTemp6);

        tType1Img = (ImageView) medium1.findViewById(R.id.textViewWeather1);
        tType2Img = (ImageView) medium1.findViewById(R.id.textViewWeather2);
        tType3Img = (ImageView) medium1.findViewById(R.id.textViewWeather3);
        tType4Img = (ImageView) medium2.findViewById(R.id.textViewWeather4);
        tType5Img = (ImageView) medium2.findViewById(R.id.textViewWeather5);
        tType6Img = (ImageView) medium2.findViewById(R.id.textViewWeather6);

        tFeng1 = (TextView) medium1.findViewById(R.id.textViewFeng1);
        tFeng2 = (TextView) medium1.findViewById(R.id.textViewFeng2);
        tFeng3 = (TextView) medium1.findViewById(R.id.textViewFeng3);
        tFeng4 = (TextView) medium2.findViewById(R.id.textViewFeng4);
        tFeng5 = (TextView) medium2.findViewById(R.id.textViewFeng5);
        tFeng6 = (TextView) medium2.findViewById(R.id.textViewFeng6);

    }



    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;//天气信息初始为空

        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        int i=0;//这个i一定要在这里，之前设置成全局变量导致数组越界。主界面刷新后，城市数据保存在数组中，
        // 如果是全局变量，新选择城市后自动返回主界面时数据是不会自动清空的，也就是说，新数据没地方放了，
        // 造成溢出在错误。而这里声明，每次都要重新建立，所以不会溢出。

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //....标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("date")){
                                eventType = xmlPullParser.next();
                                strings1[i].setDate(xmlPullParser.getText());
                                Log.d("test",strings1[i].getDate());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high")) {
                                eventType = xmlPullParser.next();
                                strings1[i].setHigh(xmlPullParser.getText());
                                Log.d("test",strings1[i].getHigh());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")){
                                eventType = xmlPullParser.next();
                                strings1[i].setLow(xmlPullParser.getText());
                                Log.d("test",strings1[i].getLow());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&&(typeCount==2||typeCount==4||typeCount==6||typeCount==8)){
                                eventType = xmlPullParser.next();
                                typeCount++;
                                strings1[i].setType(xmlPullParser.getText());
                                Log.d("test",strings1[i].getType());

                            }else if (xmlPullParser.getName().equals("type")){
                                eventType = xmlPullParser.next();
                                typeCount++;

                            }else if (xmlPullParser.getName().equals("fengxiang")&&(fengxiangCount==3||fengxiangCount==5||fengxiangCount==7||fengxiangCount==9)){
                                eventType = xmlPullParser.next();
                                fengxiangCount++;
                                strings1[i].setFengxiang(xmlPullParser.getText());
                                Log.d("test",strings1[i].getFengxiang());

                            }else if (xmlPullParser.getName().equals("fengli")&&(fengliCount==3||fengliCount==5||fengliCount==7||fengliCount==9)){
                                eventType = xmlPullParser.next();
                                fengliCount++;
                                strings1[i].setFengli(xmlPullParser.getText());
                                Log.d("test",strings1[i].getFengli());
                                i++;
                            }
                            else if (xmlPullParser.getName().equals("fengxiang")){
                                eventType = xmlPullParser.next();
                                fengxiangCount++;
                            }
                            else if (xmlPullParser.getName().equals("fengli")){
                                eventType = xmlPullParser.next();
                                fengliCount++;
                            }
                            else if (xmlPullParser.getName().equals("error")){

                                try {
                                    Log.d("data","null");
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                        break;
                    //
                    case XmlPullParser.END_TAG:
                        break;

                }
                //
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    /**
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);

                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View view) { //重写



        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            //startActivity(i);
            startActivityForResult(i, 1);
        }

        if (view.getId() == R.id.title_update_btn) {



          //mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
            progressBar.setVisibility(View.VISIBLE);
            mUpdateBtn.setVisibility(View.INVISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);//私有信息
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");//主界面城市北京
            Log.d("myWeather", cityCode);

//在这里添加按钮旋转效果。。。


            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {    //网络状况检查
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);//请求城市代码


            } else {

                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }

        if (view.getId() == R.id.title_location){//点击定位按钮后，开启定位功能
            mLocationClient.start();
            Log.d("Location :","正在定位");
        }

        mLocationClient.stop();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//从激活的SelectCity活动接收返回数据
        if (requestCode==1 &&resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "选择城市代码为" + newCityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);

            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    void updateTodayWeather(TodayWeather todayWeather) {

        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        temperatureTvNow.setText("当前温度" + todayWeather.getWendu() + "摄氏度");
        humidityTv.setText("湿度:" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力: " + todayWeather.getFengli());
        windTvOrient.setText("风向: " + todayWeather.getFengxiang());



        //<!-- 更新空气质量适配图片 根据空气质量显示不同颜色提示 -->
        if (pmQualityTv.getText().equals("优")){

            pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            pmQualityTv.setTextColor(Color.BLUE);
        }
        if (pmQualityTv.getText().equals("良")){

            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            pmQualityTv.setTextColor(Color.GRAY);
        }
        if (pmQualityTv.getText().equals("轻度污染")){

            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            pmQualityTv.setTextColor(Color.YELLOW);
        }
        if (pmQualityTv.getText().equals("中度污染")){

            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            pmQualityTv.setTextColor(Color.DKGRAY);
        }
        if (pmQualityTv.getText().equals("严重污染")){

            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            pmQualityTv.setTextColor(Color.RED);
        }

        //更新今日天气适配图片
        switch (todayWeather.getType()){
            case "晴": weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);//更改显示在屏幕上部的今日天气图片
                drawable = resources.getDrawable(R.drawable.biz_qingtian);//获取与当天天气情况对应的图片
                linearLayout.setBackgroundDrawable(drawable);//显示到背景
                break;
            case "暴雪": weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                drawable = resources.getDrawable(R.drawable.biz_baoxue);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "暴雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                drawable = resources.getDrawable(R.drawable.biz_baoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "大暴雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                drawable = resources.getDrawable(R.drawable.biz_baoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "大雪": weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                drawable = resources.getDrawable(R.drawable.biz_baoxue);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "大雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                drawable = resources.getDrawable(R.drawable.biz_baoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "多云": weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                drawable = resources.getDrawable(R.drawable.biz_duoyun);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "雷阵雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                drawable = resources.getDrawable(R.drawable.biz_leizhenyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "雷阵雨冰雹": weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                drawable = resources.getDrawable(R.drawable.biz_bingbao);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "沙尘暴": weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                drawable = resources.getDrawable(R.drawable.biz_shachenbao);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "特大暴雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                drawable = resources.getDrawable(R.drawable.biz_baoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "雾": weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                drawable = resources.getDrawable(R.drawable.biz_wu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "小雪": weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                drawable = resources.getDrawable(R.drawable.biz_xiaoxue);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "小雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                drawable = resources.getDrawable(R.drawable.biz_xiaoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "阴": weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                drawable = resources.getDrawable(R.drawable.biz_yintian);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "雨加雪": weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                drawable = resources.getDrawable(R.drawable.biz_xiaoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "阵雪": weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                drawable = resources.getDrawable(R.drawable.biz_xue);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "阵雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                drawable = resources.getDrawable(R.drawable.biz_xiaoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "中雪": weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                drawable = resources.getDrawable(R.drawable.biz_zhongxue);
                linearLayout.setBackgroundDrawable(drawable);
                break;
            case "中雨": weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                drawable = resources.getDrawable(R.drawable.biz_baoyu);
                linearLayout.setBackgroundDrawable(drawable);
                break;


            default: try {
                Log.d("weatherType","null");
            }catch (Exception e){
                e.printStackTrace();
            }
                break;

        }


        tDay1.setText(strings1[0].getDate());
        tWen1.setText(strings1[0].getLow()+"~"+strings1[0].getHigh());
//        tType1.setText(strings1[0].getType());
        switch( strings1[0].getType()){                 //更新明日天气图片
            case "晴": tType1Img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "暴雪": tType1Img.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪": tType1Img.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云": tType1Img.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹": tType1Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "沙尘暴": tType1Img.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾": tType1Img.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪": tType1Img.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴": tType1Img.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨加雪": tType1Img.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪": tType1Img.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪": tType1Img.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨": tType1Img.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;


            default: try {
                Log.d("weatherType","null");
            }catch (Exception e){
                e.printStackTrace();
            }
                break;
        };

        tFeng1.setText(strings1[0].getFengli()+" "+strings1[0].getFengxiang());



        tDay2.setText(strings1[1].getDate());
        tWen2.setText(strings1[1].getLow()+"~"+strings1[1].getHigh());
//        tType2.setText(strings1[1].getType());
        switch( strings1[1].getType()){                 //更新后日天气图片
            case "晴": tType2Img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "暴雪": tType2Img.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪": tType2Img.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云": tType2Img.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹": tType2Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "沙尘暴": tType2Img.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾": tType2Img.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪": tType2Img.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴": tType2Img.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨加雪": tType2Img.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪": tType2Img.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪": tType2Img.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨": tType2Img.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;


            default: try {
                Log.d("weatherType","null");
            }catch (Exception e){
                e.printStackTrace();
            }
                break;
        };

        tFeng2.setText(strings1[1].getFengli()+" "+strings1[1].getFengxiang());

        tDay3.setText(strings1[2].getDate());
        tWen3.setText(strings1[2].getLow()+"~"+strings1[2].getHigh());
//        tType3.setText(strings1[2].getType());
        switch( strings1[2].getType()){                 //更新第三日天气图片
            case "晴": tType3Img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "暴雪": tType3Img.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪": tType3Img.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云": tType3Img.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹": tType3Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "沙尘暴": tType3Img.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾": tType3Img.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪": tType3Img.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴": tType3Img.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨加雪": tType3Img.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪": tType3Img.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪": tType3Img.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨": tType3Img.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;


            default: try {
                Log.d("weatherType","null");
            }catch (Exception e){
                e.printStackTrace();
            }
                break;
        };

        tFeng3.setText(strings1[2].getFengli()+" "+strings1[2].getFengxiang());

        tDay4.setText(strings1[3].getDate());
        tWen4.setText(strings1[3].getLow()+"~"+strings1[3].getHigh());
//        tType4.setImageResource(R);
        switch( strings1[3].getType()){             //更新第四日天气图片
            case "晴": tType4Img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "暴雪": tType4Img.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪": tType4Img.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云": tType4Img.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹": tType4Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "沙尘暴": tType4Img.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾": tType4Img.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪": tType4Img.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴": tType4Img.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨加雪": tType4Img.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪": tType4Img.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪": tType4Img.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨": tType4Img.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;


            default: try {
                Log.d("weatherType","null");
            }catch (Exception e){
                e.printStackTrace();
            }
                break;
        };
        tFeng4.setText(strings1[3].getFengli()+" "+strings1[3].getFengxiang());

/**        tDay5.setText(strings1[4].getDate());
        tWen5.setText(strings1[4].getLow()+"~"+strings1[4].getHigh());
        tType5.setText(strings1[4].getType());
        switch( strings1[4].getType()){                           //这里会产生空指针异常，因为没有数据传过来
            case "晴": tType5Img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "暴雪": tType5Img.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪": tType5Img.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云": tType5Img.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹": tType5Img.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "沙尘暴": tType5Img.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾": tType5Img.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪": tType5Img.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴": tType5Img.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨加雪": tType5Img.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪": tType5Img.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪": tType5Img.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨": tType5Img.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;


            default: try {
                Log.d("weatherType","null");
            }catch (Exception e){
                e.printStackTrace();
            }
                break;
        };

        tFeng5.setText(strings1[4].getFengli()+" "+strings1[4].getFengxiang());



       try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        */

        //更新按钮在数据更新后恢复正常
        progressBar.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.VISIBLE);

        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_SHORT).show();



    }

    //重写OnPageChangeListener方法
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int j) {
        for(int a =0;a<ids.length;a++){
            if(a == j){
                dots[a].setImageResource(R.drawable.page_indicator_focused);//选中该页，小圆点为选中式样
            }
            else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);//未选中该页，小圆点为未选中式样
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

//定位的初始化方法
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备//这里比文档中多了一个LocationClientOption的引用
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

}