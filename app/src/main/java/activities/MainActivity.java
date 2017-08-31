package activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import creativedays.com.dilzas.CouchesActivity;
import creativedays.com.dilzas.CurtainsActivity;
import creativedays.com.dilzas.FabricTypeActivity;
import creativedays.com.dilzas.NewsActivity;
import creativedays.com.dilzas.R;
import creativedays.com.dilzas.SocialMediaActivity;
import creativedays.com.dilzas.SofaCreatorActivity;
import objects.Fabric;
import objects.FabricColor;
import objects.FabricType;
import objects.NewsObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utilities.Constants;

import static creativedays.com.dilzas.R.string.outdoor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    KenBurnsView ken;
    TextView newsTxt;
    TextView fabricsTxt;
    TextView coversTxt;
    TextView sofaCreatorTxt;
    TextView sunbedsTxt;
    TextView curtainsTxt;
    TextView social;

    ImageView camera1;
    ImageView camera2;
    ImageView camera3;
    ImageView camera4;

    ArrayList <Fabric> indoorFabrics;
    ArrayList <Fabric>outdoorFabrics;
    ArrayList <Fabric> outdoorTemp;
    ArrayList<Fabric> allFabrics;
    ArrayList <Fabric> curtainFabrics;

    ArrayList <NewsObject> news;

    ArrayList<FabricType> fabricTypes;


    FrameLayout splash;
    ImageView blackLogo;
    ImageView redLogo;
    ImageView removeSplash;

    ProgressBar splashProgress;

    Typeface bookAntiqua;

    String initJson="";

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        bookAntiqua= Typeface.createFromAsset(getAssets(),"fonts/bkant.ttf");
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        ken = (KenBurnsView) findViewById(R.id.ken);
        newsTxt=(TextView)findViewById(R.id.news);
        fabricsTxt=(TextView)findViewById(R.id.fabrics);
        coversTxt=(TextView)findViewById(R.id.covers);
        sofaCreatorTxt=(TextView)findViewById(R.id.sofa_creator);
        sunbedsTxt=(TextView)findViewById(R.id.sunbeds);
        curtainsTxt=(TextView)findViewById(R.id.curtains);
        social=(TextView)findViewById(R.id.social_media);

        camera1=(ImageView)findViewById(R.id.camera1);
        camera2=(ImageView)findViewById(R.id.camera2);
        camera3=(ImageView)findViewById(R.id.camera3);
        camera4=(ImageView)findViewById(R.id.camera4);

        newsTxt.setTypeface(bookAntiqua);
        fabricsTxt.setTypeface(bookAntiqua);
        coversTxt.setTypeface(bookAntiqua);
        sunbedsTxt.setTypeface(bookAntiqua);
        sofaCreatorTxt.setTypeface(bookAntiqua);
        curtainsTxt.setTypeface(bookAntiqua);
        social.setTypeface(bookAntiqua);




        splash=(FrameLayout)findViewById(R.id.splash);
        splashProgress=(ProgressBar)findViewById(R.id.splash_progress);
        blackLogo=(ImageView)findViewById(R.id.black);
        redLogo=(ImageView)findViewById(R.id.red);
        removeSplash=(ImageView)findViewById(R.id.remove_splash);

        newsTxt.setOnClickListener(this);
        fabricsTxt.setOnClickListener(this);
        coversTxt.setOnClickListener(this);
        sofaCreatorTxt.setOnClickListener(this);
        sunbedsTxt.setOnClickListener(this);
        curtainsTxt.setOnClickListener(this);
        social.setOnClickListener(this);

        RandomTransitionGenerator generator = new RandomTransitionGenerator(19000, new LinearInterpolator());
        ken.setTransitionGenerator(generator);

        clearCache();
        initData();
        getInitJson();
    }

    public void clearCache () {
        if (!prefs.getBoolean("clear1",false)) {
            prefs.edit().putBoolean("clear1",true).commit();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Glide.get(MainActivity.this).clearDiskCache();
                }
            });
        }

    }


    public void getInitJson() {
        startSplashAnimation();
        String url;
        url = Constants.BASE_URL + Constants.INIT + Locale.getDefault().getLanguage();
        //String url = Constants.BASE_URL + Constants.INIT+"el";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final String cachedResponse = prefs.getString("json", "");
                initJson = cachedResponse;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!cachedResponse.equals("")) {
                            parseData(cachedResponse);
                        } else {

                            Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
                            //finish();
                            showContinue();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                responseData.replace("\"" + "gr" + "\"", "\"" + "el" + "\"");
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
                            //finish();
                            showContinue();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                initJson = responseData;
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("json", responseData);
                                editor.commit();
                                parseData(responseData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });
    }

    public void fadeCameras() {
        Animation fadeIn=new AlphaAnimation(0,1);
        fadeIn.setDuration(2000);
        camera1.startAnimation(fadeIn);
        camera2.startAnimation(fadeIn);
        camera3.startAnimation(fadeIn);
        camera4.startAnimation(fadeIn);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fadeCameras();
    }

    public void parseData (String data) {

        try {
            JSONObject root=new JSONObject(data);
            JSONArray news=root.getJSONArray("articles");

            parseNews(news);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        showContinue();
    }

    public void parseNews (JSONArray newsArray) {
        news=new ArrayList<>();
        try {
            for (int i=0; i<newsArray.length(); i++) {
                JSONObject item=newsArray.getJSONObject(i);
                NewsObject newsObject=new NewsObject();
                newsObject.setText(item.getString("text"));
                newsObject.setTitle(item.getString("title"));
                newsObject.setDate(item.getString("updated_at"));
                newsObject.setImgURL(item.getJSONObject("image").getString("url"));

                news.add(newsObject);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startSplashAnimation() {
        Animation fadeIn=new AlphaAnimation(0,1);
        fadeIn.setDuration(1500);
        blackLogo.setVisibility(View.VISIBLE);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Animation fadeIn2=new AlphaAnimation(0,1);
                fadeIn2.setDuration(2000);
                redLogo.setVisibility(View.VISIBLE);
                fadeIn2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        redLogo.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                redLogo.startAnimation(fadeIn2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        blackLogo.startAnimation(fadeIn);
    }

    public void showContinue () {
        splashProgress.setVisibility(View.GONE);
        Animation fadeIn=new AlphaAnimation(0,1);
        fadeIn.setDuration(3000);
        removeSplash.startAnimation(fadeIn);
        removeSplash.setVisibility(View.VISIBLE);
        removeSplash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation fadeOut=new AlphaAnimation(1,0);
                fadeOut.setDuration(500);
                splash.startAnimation(fadeOut);
                splash.setVisibility(View.GONE);
                fadeCameras();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        Intent intent;
        switch (id) {
            case R.id.news:
                if (!initJson.equals("")) {
                    if (news.size()>0) {
                        intent = new Intent(this, NewsActivity.class);
                        intent.putExtra("news", news);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MainActivity.this, getString(R.string.no_news), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_news), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fabrics:
                intent=new Intent(this,FabricTypeActivity.class);
                intent.putExtra("fabric_types", fabricTypes);
                startActivity(intent);
                break;
            case R.id.covers:
                intent=new Intent(this,CouchesActivity.class);
                intent.putExtra("fabrics", indoorFabrics);
                startActivity(intent);
                break;
            case R.id.sofa_creator:
                intent=new Intent(this,SofaCreatorActivity.class);
                intent.putExtra("fabrics", indoorFabrics);
                startActivity(intent);
                break;
            case R.id.sunbeds:
                intent=new Intent(this,CouchesActivity.class);
                intent.putExtra("fabrics", outdoorFabrics);
                startActivity(intent);
                break;
            case R.id.curtains:
                intent=new Intent(this,CurtainsActivity.class);
                intent.putExtra("fabrics", curtainFabrics);
                startActivity(intent);
                break;
            case R.id.social_media:
                intent=new Intent(this,SocialMediaActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void initData () {
        indoorFabrics =new ArrayList<>();
        outdoorFabrics=new ArrayList<>();
        outdoorTemp=new ArrayList<>();
        allFabrics=new ArrayList<>();
        curtainFabrics=new ArrayList<>();

        ArrayList<FabricColor> aegeanColors=new ArrayList<>();
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_06.jpg", Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_06.jpg","Color 06","Χρώμα 06"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_07.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_07.jpg","Color 07","Χρώμα 07"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_201.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_201.jpg","Color 201","Χρώμα 201"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_408.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_408.jpg","Color 408","Χρώμα 408"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_303.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_303.jpg","Color 303","Χρώμα 303"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_204.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_204.jpg","Color 204","Χρώμα 204"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_05.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_05.jpg","Color 05","Χρώμα 05"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_12.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_12.jpg","Color 12","Χρώμα 12"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_03.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_03.jpg","Color 03","Χρώμα 03"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_20.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_20.jpg","Color 20","Χρώμα 20"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_401.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_401.jpg","Color 401","Χρώμα 401"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_210.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_210.jpg","Color 210","Χρώμα 210"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_302.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_302.jpg","Color 302","Χρώμα 302"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_304.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_304.jpg","Color 304","Χρώμα 304"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_402.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_402.jpg","Color 402","Χρώμα 402"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_24.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_24.jpg","Color 24","Χρώμα 24"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_301.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_301.jpg","Color 301","Χρώμα 301"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_407.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_407.jpg","Color 407","Χρώμα 407"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_04.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_04.jpg","Color 04","Χρώμα 04"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_209.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_209.jpg","Color 209","Χρώμα 04"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_08.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_08.jpg","Color 08","Χρώμα 08"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_404.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_404.jpg","Color 404","Χρώμα 404"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_202.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_202.jpg","Color 202","Χρώμα 202"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_203.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_203.jpg","Color 203","Χρώμα 203"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_10.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_10.jpg","Color 10","Χρώμα 10"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_206.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_206.jpg","Color 206","Χρώμα 206"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_02.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_02.jpg","Color 02","Χρώμα 02"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_405.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_405.jpg","Color 02","Χρώμα 405"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_208.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_208.jpg","Color 208","Χρώμα 208"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_01.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_01.jpg","Color 01","Χρώμα 01"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_207.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_207.jpg","Color 207","Χρώμα 207"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_406.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_406.jpg","Color 406","Χρώμα 406"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_11.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_11.jpg","Color 11","Χρώμα 11"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_205.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_205.jpg","Color 205","Χρώμα 205"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_21.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_21.jpg","Color 21","Χρώμα 21"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_403.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_403.jpg","Color 403","Χρώμα 403"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_19.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_19.jpg","Color 19","Χρώμα 19"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_17.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_17.jpg","Color 17","Χρώμα 17"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_22.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_22.jpg","Color 22","Χρώμα 22"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_18.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_18.jpg","Color 18","Χρώμα 18"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_09.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_09.jpg","Color 09","Χρώμα 09"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_16.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_16.jpg","Color 16","Χρώμα 16"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_409.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_409.jpg","Color 409","Χρώμα 409"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_410.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_410.jpg","Color 410","Χρώμα 410"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_25.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_25.jpg","Color 25","Χρώμα 25"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_23.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_23.jpg","Color 23","Χρώμα 23"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_14.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_14.jpg","Color 14","Χρώμα 14"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_15.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_15.jpg","Color 15","Χρώμα 15"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_13.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_13.jpg","Color 13","Χρώμα 13"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_101.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_101.jpg","Color 101","Χρώμα 101"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_104.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_104.jpg","Color 104","Χρώμα 104"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_2130.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_2130.jpg","Color 2130","Χρώμα 2130"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_109.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_109.jpg","Color 109","Χρώμα 109"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_102.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_102.jpg","Color 102","Χρώμα 102"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_103.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_103.jpg","Color 103","Χρώμα 103"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_106.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_106.jpg","Color 103","Χρώμα 106"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_108.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_108.jpg","Color 108","Χρώμα 108"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_107.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_107.jpg","Color 107","Χρώμα 107"));
        aegeanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_aegean_color_105.jpg",Constants.FABRICS_BASE_URL+"aegean/fabric_aegean_color_105.jpg","Color 105","Χρώμα 105"));


        ArrayList<String> aegeanSunbeds=new ArrayList<>();
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_06.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_07.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_201.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_408.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_303.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_204.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_05.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_12.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_03.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_20.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_401.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_210.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_302.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_304.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_402.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_24.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_301.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_407.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_04.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_209.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_08.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_404.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_202.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_203.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_10.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_206.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_02.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_405.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_208.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_01.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_207.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_406.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_11.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_205.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_21.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_403.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_19.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_17.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_22.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_18.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_09.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_16.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_409.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_410.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_25.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_23.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_14.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_15.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_13.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_101.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_104.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_2130.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_109.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_102.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_103.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_106.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_108.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_107.png");
        aegeanSunbeds.add(Constants.SUNBEDS_BASE_URL+"aegean/sunbed_aegean_color_105.png");


        Fabric aegean=new Fabric(R.drawable.aegean,"AEGEAN",aegeanColors);
        aegean.setCouches(aegeanSunbeds);


        ArrayList<FabricColor> atlantisColors=new ArrayList<>();
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_01.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_01.jpg","Color 01","Χρώμα 01"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_08.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_08.jpg","Color 08","Χρώμα 08"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_06.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_06.jpg","Color 06","Χρώμα 06"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_04.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_04.jpg","Color 04","Χρώμα 04"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_13.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_13.jpg","Color 13","Χρώμα 13"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_23.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_23.jpg","Color 23","Χρώμα 23"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_11.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_11.jpg","Color 11","Χρώμα 11"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_16.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_16.jpg","Color 16","Χρώμα 16"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_15.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_15.jpg","Color 15","Χρώμα 15"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_17.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_17.jpg","Color 16","Χρώμα 17"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_19.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_19.jpg","Color 19","Χρώμα 19"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_09.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_09.jpg","Color 09","Χρώμα 09"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_07.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_07.jpg","Color 07","Χρώμα 07"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_02.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_02.jpg","Color 02","Χρώμα 02"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_22.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_22.jpg","Color 22","Χρώμα 22"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_24.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_24.jpg","Color 24","Χρώμα 24"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_14.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_14.jpg","Color 14","Χρώμα 14"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_18.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_18.jpg","Color 18","Χρώμα 18"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_12.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_12.jpg","Color 12","Χρώμα 12"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_03.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_03.jpg","Color 03","Χρώμα 03"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_10.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_10.jpg","Color 10","Χρώμα 10"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_21.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_21.jpg","Color 21","Χρώμα 21"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_05.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_05.jpg","Color 05","Χρώμα 05"));
        atlantisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_atlantis_color_20.jpg", Constants.FABRICS_BASE_URL+"atlantis/fabric_atlantis_color_20.jpg","Color 20","Χρώμα 20"));


        ArrayList<String> atlantisCouches=new ArrayList<>();
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_01.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_08.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_06.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_04.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_13.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_23.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_11.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_16.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_15.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_17.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_19.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_09.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_07.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_02.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_22.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_24.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_14.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_18.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_12.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_03.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_10.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_21.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_05.png");
        atlantisCouches.add(Constants.COUCHES_BASE_URL+"atlantis/couch_atlantis_color_20.png");


        ArrayList<String> atlantisCouchBodys=new ArrayList<>();


        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_01.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_08.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_06.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_04.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_13.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_23.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_11.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_16.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_15.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_17.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_19.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_09.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_07.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_02.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_22.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_24.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_14.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_18.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_12.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_03.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_10.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_21.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_05.png");
        atlantisCouchBodys.add(Constants.COUCHES_BODY_URL+"atlantis/couch_body_atlantis_color_20.png");
        

        ArrayList<String> atlantisCouchPillows=new ArrayList<>();

        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_01.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_08.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_06.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_04.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_13.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_23.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_11.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_16.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_15.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_17.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_19.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_09.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_07.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_02.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_22.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_24.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_14.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_18.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_12.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_03.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_10.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_21.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_05.png");
        atlantisCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"atlantis/couch_pillow_atlantis_color_20.png");
        

        Fabric atlantis=new Fabric(R.drawable.atlantis,"ATLANTIS",atlantisColors);
        atlantis.setCouchPillows(atlantisCouchPillows);
        atlantis.setCouchBodys(atlantisCouchBodys);
        atlantis.setCouches(atlantisCouches);

        ArrayList<FabricColor> belvedereColors=new ArrayList<>();
        

        belvedereColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_belvedere_color_05.jpg", Constants.FABRICS_BASE_URL+"belvedere/fabric_belvedere_color_05.jpg","Color 05","Χρώμα 05"));
        belvedereColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_belvedere_color_01.jpg", Constants.FABRICS_BASE_URL+"belvedere/fabric_belvedere_color_01.jpg","Color 01","Χρώμα 01"));
        belvedereColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_belvedere_color_02.jpg", Constants.FABRICS_BASE_URL+"belvedere/fabric_belvedere_color_02.jpg","Color 02","Χρώμα 02"));
        belvedereColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_belvedere_color_04.jpg", Constants.FABRICS_BASE_URL+"belvedere/fabric_belvedere_color_04.jpg","Color 04","Χρώμα 04"));
        belvedereColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_belvedere_color_03.jpg", Constants.FABRICS_BASE_URL+"belvedere/fabric_belvedere_color_03.jpg","Color 03","Χρώμα 03"));
        
        

        ArrayList<String> belvedereCouches=new ArrayList<>();
        belvedereCouches.add(Constants.COUCHES_BASE_URL+"belvedere/couch_belvedere_color_05.png");
        belvedereCouches.add(Constants.COUCHES_BASE_URL+"belvedere/couch_belvedere_color_01.png");
        belvedereCouches.add(Constants.COUCHES_BASE_URL+"belvedere/couch_belvedere_color_02.png");
        belvedereCouches.add(Constants.COUCHES_BASE_URL+"belvedere/couch_belvedere_color_04.png");
        belvedereCouches.add(Constants.COUCHES_BASE_URL+"belvedere/couch_belvedere_color_03.png");

        ArrayList<String> belvedereCouchBodys=new ArrayList<>();
        belvedereCouchBodys.add(Constants.COUCHES_BODY_URL+"belvedere/couch_body_belvedere_color_05.png");
        belvedereCouchBodys.add(Constants.COUCHES_BODY_URL+"belvedere/couch_body_belvedere_color_01.png");
        belvedereCouchBodys.add(Constants.COUCHES_BODY_URL+"belvedere/couch_body_belvedere_color_02.png");
        belvedereCouchBodys.add(Constants.COUCHES_BODY_URL+"belvedere/couch_body_belvedere_color_04.png");
        belvedereCouchBodys.add(Constants.COUCHES_BODY_URL+"belvedere/couch_body_belvedere_color_03.png");

        ArrayList<String> belvedereCouchPillows=new ArrayList<>();
        belvedereCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"belvedere/couch_pillow_belvedere_color_05.png");
        belvedereCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"belvedere/couch_pillow_belvedere_color_01.png");
        belvedereCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"belvedere/couch_pillow_belvedere_color_02.png");
        belvedereCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"belvedere/couch_pillow_belvedere_color_04.png");
        belvedereCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"belvedere/couch_pillow_belvedere_color_03.png");
        
        

        Fabric belvedere=new Fabric(R.drawable.belvedere,"BELVEDERE",belvedereColors);
        belvedere.setCouches(belvedereCouches);
        belvedere.setCouchBodys(belvedereCouchBodys);
        belvedere.setCouchPillows(belvedereCouchPillows);

        ArrayList<FabricColor> calmColors=new ArrayList<>();
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_02.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_02.jpg","Color 02","Χρώμα 02"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_11.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_11.jpg","Color 11","Χρώμα 11"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_07.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_07.jpg","Color 07","Χρώμα 07"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_13.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_13.jpg","Color 13","Χρώμα 13"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_14.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_14.jpg","Color 14","Χρώμα 14"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_05.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_05.jpg","Color 05","Χρώμα 05"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_06.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_06.jpg","Color 06","Χρώμα 06"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_15.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_15.jpg","Color 15","Χρώμα 15"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_08.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_08.jpg","Color 08","Χρώμα 08"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_01.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_01.jpg","Color 01","Χρώμα 01"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_04.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_04.jpg","Color 04","Χρώμα 04"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_03.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_03.jpg","Color 03","Χρώμα 03"));
        calmColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_calm_color_12.jpg", Constants.FABRICS_BASE_URL+"calm/fabric_calm_color_12.jpg","Color 12","Χρώμα 12"));


        ArrayList<String> calmCouches=new ArrayList<>();
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_02.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_11.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_07.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_13.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_14.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_05.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_06.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_15.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_08.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_01.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_04.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_03.png");
        calmCouches.add(Constants.COUCHES_BASE_URL+"calm/couch_calm_color_12.png");

        ArrayList<String> calmCouchBodys=new ArrayList<>();
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_02.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_11.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_07.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_13.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_14.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_05.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_06.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_15.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_08.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_01.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_04.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_03.png");
        calmCouchBodys.add(Constants.COUCHES_BODY_URL+"calm/couch_body_calm_color_12.png");

        ArrayList<String> calmCouchPillows=new ArrayList<>();
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_02.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_11.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_07.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_13.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_14.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_05.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_06.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_15.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_08.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_01.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_04.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_03.png");
        calmCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"calm/couch_pillow_calm_color_12.png");


        Fabric calm=new Fabric(R.drawable.calm,"CALM",calmColors);
        calm.setCouches(calmCouches);
        calm.setCouchBodys(calmCouchBodys);
        calm.setCouchPillows(calmCouchPillows);

        ArrayList<FabricColor> elementColors=new ArrayList<>();
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_01.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_01.jpg","Color 01","Χρώμα 01"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_02.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_02.jpg","Color 02","Χρώμα 02"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_03.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_03.jpg","Color 03","Χρώμα 03"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_05.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_05.jpg","Color 05","Χρώμα 05"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_15.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_15.jpg","Color 15","Χρώμα 15"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_14.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_14.jpg","Color 14","Χρώμα 14"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_13.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_13.jpg","Color 13","Χρώμα 13"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_12.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_12.jpg","Color 12","Χρώμα 12"));
        elementColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_element_color_11.jpg", Constants.FABRICS_BASE_URL+"element/fabric_element_color_11.jpg","Color 11","Χρώμα 11"));


        ArrayList<String> elementCouches=new ArrayList<>();
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_01.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_02.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_03.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_05.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_15.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_14.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_13.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_12.png");
        elementCouches.add(Constants.COUCHES_BASE_URL+"element/couch_element_color_11.png");
        Fabric element=new Fabric(R.drawable.element,"ELEMENT",elementColors);
        element.setCouches(elementCouches);
        
        
        ArrayList <String> elementCouchBodys=new ArrayList<>();
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_01.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_02.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_03.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_05.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_15.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_14.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_13.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_12.png");
        elementCouchBodys.add(Constants.COUCHES_BODY_URL+"element/couch_body_element_color_11.png");
        element.setCouchBodys(elementCouchBodys);
        

        ArrayList<String> elementCouchPillows=new ArrayList<>();
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_01.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_02.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_03.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_05.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_15.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_14.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_13.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_12.png");
        elementCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"element/couch_pillow_element_color_11.png");
        element.setCouchPillows(elementCouchPillows);
        

        ArrayList<String> elementCurtains=new ArrayList<>();
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_01.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_02.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_03.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_05.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_15.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_14.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_13.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_12.png");
        elementCurtains.add(Constants.CURTAINS_BASE_URL+"element/curtain_element_color_11.png");
        element.setCurtains(elementCurtains);



        ArrayList<FabricColor> elysseColors=new ArrayList<>();
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_91.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_91.jpg","4738-91","4738-91"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_85.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_85.jpg","4738-85","4738-85"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_90.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_90.jpg","4738-90","4738-90"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_112.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_112.jpg","4738-112","4738-112"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_83.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_83.jpg","4738-83","4738-83"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_67.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_67.jpg","4738-67","4738-67"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_376.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_376.jpg","4738-376","4738-376"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_55.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_55.jpg","4738-55","4738-55"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_68.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_68.jpg","4738-68","4738-68"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_139.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_139.jpg","4738-139","4738-139"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_100.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_100.jpg","4738-100","4738-100"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_48.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_48.jpg","4738-48","4738-48"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_251.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_251.jpg","4738-251","4738-251"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_09.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_09.jpg","4738-09","4738-09"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_113.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_113.jpg","4738-113","4738-113"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_45.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_45.jpg","4738-45","4738-45"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_35.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_35.jpg","4738-35","4738-35"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_69.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_69.jpg","4738-69","4738-69"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_375.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_375.jpg","4738-375","4738-375"));
        elysseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elysse_color_4738_125.jpg", Constants.FABRICS_BASE_URL+"elysse/fabric_elysse_color_4738_125.jpg","4738-125","4738-125"));

        ArrayList<String> elysseCouches=new ArrayList<>();
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_91.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_85.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_90.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_112.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_83.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_67.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_376.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_55.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_68.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_139.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_100.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_48.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_251.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_09.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_113.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_45.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_35.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_69.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_375.png");
        elysseCouches.add(Constants.COUCHES_BASE_URL+"elysse/couch_elysse_color_4738_125.png");



        Fabric elysse=new Fabric(R.drawable.elysse,"ELYSSE",elysseColors);
        elysse.setCouches(elysseCouches);


        ArrayList<String> elysseCouchBodys=new ArrayList<>();
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_91.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_85.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_90.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_112.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_83.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_67.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_376.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_55.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_68.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_139.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_100.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_48.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_251.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_09.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_113.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_45.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_35.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_69.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_375.png");
        elysseCouchBodys.add(Constants.COUCHES_BODY_URL+"elysse/couch_body_elysse_color_4738_125.png");
        elysse.setCouchBodys(elysseCouchBodys);

        ArrayList<String> elysseCouchPillows=new ArrayList<>();
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_91.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_85.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_90.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_112.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_83.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_67.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_376.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_55.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_68.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_139.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_100.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_48.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_251.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_09.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_113.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_45.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_35.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_69.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_375.png");
        elysseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"elysse/couch_pillow_elysse_color_4738_125.png");
        elysse.setCouchPillows(elysseCouchPillows);



        ArrayList<FabricColor> famousColors=new ArrayList<>();
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_01.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_01.jpg","Color 01","Χρώμα 01"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_02.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_02.jpg","Color 02","Χρώμα 02"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_03.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_03.jpg","Color 03","Χρώμα 03"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_05.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_05.jpg","Color 05","Χρώμα 05"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_11.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_11.jpg","Color 11","Χρώμα 11"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_12.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_12.jpg","Color 12","Χρώμα 12"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_13.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_13.jpg","Color 13","Χρώμα 13"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_14.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_14.jpg","Color 14","Χρώμα 14"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_16.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_16.jpg","Color 16","Χρώμα 16"));
        famousColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_famous_color_15.jpg", Constants.FABRICS_BASE_URL+"famous/fabric_famous_color_15.jpg","Color 15","Χρώμα 15"));

        ArrayList<String> famousCouches=new ArrayList<>();
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_01.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_02.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_03.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_05.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_11.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_12.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_13.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_14.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_16.png");
        famousCouches.add(Constants.COUCHES_BASE_URL+"famous/couch_famous_color_15.png");


        Fabric famous=new Fabric(R.drawable.famous,"FAMOUS",famousColors);
        famous.setCouches(famousCouches);

        ArrayList<String> famousCouchBodys=new ArrayList<>();
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_01.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_02.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_03.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_05.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_11.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_12.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_13.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_14.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_16.png");
        famousCouchBodys.add(Constants.COUCHES_BODY_URL+"famous/couch_body_famous_color_15.png");
        famous.setCouchBodys(famousCouchBodys);


        ArrayList<String> famousCouchPillows=new ArrayList<>();
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_01.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_02.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_03.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_05.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_11.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_12.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_13.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_14.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_16.png");
        famousCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"famous/couch_pillow_famous_color_15.png");
        famous.setCouchPillows(famousCouchPillows);

        ArrayList<FabricColor> giftColors=new ArrayList<>();
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_95.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_95.jpg","Color 95","Χρώμα 95"));
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_97.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_97.jpg","Color 97","Χρώμα 97"));
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_96.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_96.jpg","Color 96","Χρώμα 96"));
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_98.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_98.jpg","Color 98","Χρώμα 98"));
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_50.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_50.jpg","Color 50","Χρώμα 50"));
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_53.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_53.jpg","Color 53","Χρώμα 53"));
        giftColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gift_color_47.jpg", Constants.FABRICS_BASE_URL+"gift/fabric_gift_color_47.jpg","Color 47","Χρώμα 47"));


        ArrayList<String> giftCouches=new ArrayList<>();
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_95.png");
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_97.png");
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_96.png");
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_98.png");
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_50.png");
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_53.png");
        giftCouches.add(Constants.COUCHES_BASE_URL+"gift/couch_gift_color_47.png");

        Fabric gift=new Fabric(R.drawable.gift,"GIFT",giftColors);
        gift.setCouches(giftCouches);

        ArrayList<String> giftCouchBodys=new ArrayList<>();
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_95.png");
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_97.png");
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_96.png");
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_98.png");
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_50.png");
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_53.png");
        giftCouchBodys.add(Constants.COUCHES_BODY_URL+"gift/couch_body_gift_color_47.png");
        gift.setCouchBodys(giftCouchBodys);

        ArrayList<String> giftCouchPillows=new ArrayList<>();
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_95.png");
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_97.png");
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_96.png");
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_98.png");
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_50.png");
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_53.png");
        giftCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"gift/couch_pillow_gift_color_47.png");
        gift.setCouchPillows(giftCouchPillows);
        

        ArrayList<FabricColor> illusionColors=new ArrayList<>();
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_33.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_33.jpg","Color 33","Χρώμα 33"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_03.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_03.jpg","Color 03","Χρώμα 03"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_13.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_13.jpg","Color 13","Χρώμα 13"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_32.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_32.jpg","Color 32","Χρώμα 32"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_02.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_02.jpg","Color 02","Χρώμα 02"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_12.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_12.jpg","Color 12","Χρώμα 12"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_35.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_35.jpg","Color 35","Χρώμα 35"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_05.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_05.jpg","Color 05","Χρώμα 05"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_15.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_15.jpg","Color 15","Χρώμα 15"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_31.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_31.jpg","Color 31","Χρώμα 31"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_01.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_01.jpg","Color 01","Χρώμα 01"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_11.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_11.jpg","Color 11","Χρώμα 11"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_34.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_34.jpg","Color 34","Χρώμα 34"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_04.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_04.jpg","Color 04","Χρώμα 04"));
        illusionColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_illusion_color_14.jpg", Constants.FABRICS_BASE_URL+"illusion/fabric_illusion_color_14.jpg","Color 14","Χρώμα 14"));


        ArrayList<String> illusionCouches=new ArrayList<>();
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_33.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_03.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_13.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_32.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_02.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_12.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_35.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_05.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_15.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_31.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_01.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_11.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_34.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_04.png");
        illusionCouches.add(Constants.COUCHES_BASE_URL+"illusion/couch_illusion_color_14.png");

        Fabric illusion=new Fabric(R.drawable.illusion,"ILLUSION",illusionColors);
        illusion.setCouches(illusionCouches);

        ArrayList<String> illusionCouchBodys=new ArrayList<>();
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_33.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_03.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_13.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_32.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_02.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_12.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_35.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_05.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_15.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_31.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_01.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_11.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_34.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_04.png");
        illusionCouchBodys.add(Constants.COUCHES_BODY_URL+"illusion/couch_body_illusion_color_14.png");
        illusion.setCouchBodys(illusionCouchBodys);

        ArrayList<String> illusionCouchPillows=new ArrayList<>();
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_33.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_03.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_13.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_32.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_02.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_12.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_35.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_05.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_15.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_31.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_01.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_11.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_34.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_04.png");
        illusionCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"illusion/couch_pillow_illusion_color_14.png");
        illusion.setCouchPillows(illusionCouchPillows);
        

        ArrayList<FabricColor> localColors=new ArrayList<>();
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_02.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_02.jpg","Color 02","Χρώμα 02"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_08.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_08.jpg","Color 08","Χρώμα 08"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_20.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_20.jpg","Color 20","Χρώμα 20"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_16.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_16.jpg","Color 16","Χρώμα 16"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_14.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_14.jpg","Color 14","Χρώμα 14"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_06.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_06.jpg","Color 06","Χρώμα 06"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_04.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_04.jpg","Color 04","Χρώμα 04"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_12.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_12.jpg","Color 12","Χρώμα 12"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_22.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_22.jpg","Color 22","Χρώμα 22"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_10.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_10.jpg","Color 10","Χρώμα 10"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_18.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_18.jpg","Color 18","Χρώμα 18"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_01.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_01.jpg","Color 01","Χρώμα 01"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_07.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_07.jpg","Color 07","Χρώμα 07"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_19.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_19.jpg","Color 19","Χρώμα 19"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_15.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_15.jpg","Color 15","Χρώμα 15"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_13.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_13.jpg","Color 13","Χρώμα 13"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_05.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_05.jpg","Color 05","Χρώμα 05"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_03.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_03.jpg","Color 03","Χρώμα 03"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_11.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_11.jpg","Color 11","Χρώμα 11"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_21.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_21.jpg","Color 21","Χρώμα 21"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_17.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_17.jpg","Color 17","Χρώμα 17"));
        localColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_local_color_09.jpg", Constants.FABRICS_BASE_URL+"local/fabric_local_color_09.jpg","Color 09","Χρώμα 09"));

        ArrayList<String> localCouches=new ArrayList<>();
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_02.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_08.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_20.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_16.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_14.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_06.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_04.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_12.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_22.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_10.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_18.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_01.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_07.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_19.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_15.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_13.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_05.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_03.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_11.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_21.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_17.png");
        localCouches.add(Constants.COUCHES_BASE_URL+"local/couch_local_color_09.png");


        Fabric local=new Fabric(R.drawable.local,"LOCAL",localColors);
        local.setCouches(localCouches);

        ArrayList<String> localCouchBodys=new ArrayList<>();
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_02.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_08.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_20.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_16.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_14.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_06.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_04.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_12.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_22.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_10.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_18.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_01.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_07.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_19.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_15.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_13.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_05.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_03.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_11.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_21.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_17.png");
        localCouchBodys.add(Constants.COUCHES_BODY_URL+"local/couch_body_local_color_09.png");
        
        local.setCouchBodys(localCouchBodys);

        ArrayList<String> localCouchPillows=new ArrayList<>();
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_02.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_08.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_20.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_16.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_14.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_06.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_04.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_12.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_22.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_10.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_18.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_01.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_07.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_19.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_15.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_13.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_05.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_03.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_11.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_21.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_17.png");
        localCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"local/couch_pillow_local_color_09.png");

        local.setCouchPillows(localCouchPillows);
        

        ArrayList<FabricColor> majesticColors=new ArrayList<>();
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7489.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7489.jpg","MG 7489","MG 7489"));
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7491.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7491.jpg","MG 7491","MG 7491"));
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7493.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7493.jpg","MG 7493","MG 7493"));
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7495.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7495.jpg","MG 7495","MG 7495"));
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7496.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7496.jpg","MG 7496","MG 7496"));
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7498.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7498.jpg","MG 7498","MG 7498"));
        majesticColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_majestic_color_mg_7500.jpg", Constants.FABRICS_BASE_URL+"majestic/fabric_majestic_color_MG_7500.jpg","MG 7500","MG 7500"));

        ArrayList<String> majesticCouches=new ArrayList<>();
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7489.png");
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7491.png");
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7493.png");
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7495.png");
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7496.png");
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7498.png");
        majesticCouches.add(Constants.COUCHES_BASE_URL+"majestic/couch_majestic_color_MG_7500.png");

        Fabric majestic=new Fabric(R.drawable.majestic,"MAJESTIC",majesticColors);
        majestic.setCouches(majesticCouches);

        ArrayList<String> majesticCouchBodys=new ArrayList<>();
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7489.png");
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7491.png");
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7493.png");
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7495.png");
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7496.png");
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7498.png");
        majesticCouchBodys.add(Constants.COUCHES_BODY_URL+"majestic/couch_body_majestic_color_MG_7500.png");
        
        majestic.setCouchBodys(majesticCouchBodys);

        ArrayList<String> majesticCouchPillows=new ArrayList<>();
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7489.png");
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7491.png");
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7493.png");
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7495.png");
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7496.png");
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7498.png");
        majesticCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"majestic/couch_pillow_majestic_color_MG_7500.png");

        majestic.setCouchPillows(majesticCouchPillows);



        ArrayList<FabricColor> memoryColors=new ArrayList<>();
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_01.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_01.jpg","Color 01","Χρώμα 01"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_02.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_02.jpg","Color 02","Χρώμα 02"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_04.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_04.jpg","Color 04","Χρώμα 04"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_05.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_05.jpg","Color 05","Χρώμα 05"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_06.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_06.jpg","Color 06","Χρώμα 06"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_26.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_26.jpg","Color 26","Χρώμα 26"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_16.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_16.jpg","Color 16","Χρώμα 16"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_21.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_21.jpg","Color 21","Χρώμα 21"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_11.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_11.jpg","Color 11","Χρώμα 11"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_12.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_12.jpg","Color 12","Χρώμα 12"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_22.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_22.jpg","Color 22","Χρώμα 22"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_14.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_14.jpg","Color 14","Χρώμα 14"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_24.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_24.jpg","Color 24","Χρώμα 24"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_15.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_15.jpg","Color 15","Χρώμα 15"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_25.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_25.jpg","Color 25","Χρώμα 25"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_23.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_23.jpg","Color 23","Χρώμα 23"));
        memoryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_memory_color_13.jpg", Constants.FABRICS_BASE_URL+"memory/fabric_memory_color_13.jpg","Color 13","Χρώμα 13"));


        ArrayList<String> memoryCouches=new ArrayList<>();
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_01.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_02.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_04.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_05.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_06.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_26.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_16.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_21.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_11.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_12.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_22.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_14.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_24.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_15.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_25.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_23.png");
        memoryCouches.add(Constants.COUCHES_BASE_URL+"memory/couch_memory_color_13.png");



        Fabric memory=new Fabric(R.drawable.memory,"MEMORY",memoryColors);
        memory.setCouches(memoryCouches);


        ArrayList<String> memoryCouchBodys=new ArrayList<>();
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_01.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_02.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_04.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_05.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_06.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_26.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_16.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_21.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_11.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_12.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_22.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_14.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_24.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_15.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_25.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_23.png");
        memoryCouchBodys.add(Constants.COUCHES_BODY_URL+"memory/couch_body_memory_color_13.png");
        memory.setCouchBodys(memoryCouchBodys);

        ArrayList<String> memoryCouchPillows=new ArrayList<>();
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_01.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_02.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_04.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_05.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_06.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_26.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_16.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_21.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_11.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_12.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_22.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_14.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_24.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_15.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_25.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_23.png");
        memoryCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"memory/couch_pillow_memory_color_13.png");
        memory.setCouchPillows(memoryCouchPillows);


        ArrayList<FabricColor> orlandoColors=new ArrayList<>();
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_01.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_01.jpg","Color 01","Χρώμα 01"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_02.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_02.jpg","Color 02","Χρώμα 02"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_03.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_03.jpg","Color 03","Χρώμα 03"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_04.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_04.jpg","Color 04","Χρώμα 04"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_05.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_05.jpg","Color 05","Χρώμα 05"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_11.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_11.jpg","Color 11","Χρώμα 11"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_12.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_12.jpg","Color 12","Χρώμα 12"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_13.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_13.jpg","Color 13","Χρώμα 13"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_14.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_14.jpg","Color 14","Χρώμα 14"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_15.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_15.jpg","Color 15","Χρώμα 15"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_21.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_21.jpg","Color 21","Χρώμα 21"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_22.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_22.jpg","Color 22","Χρώμα 22"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_23.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_23.jpg","Color 23","Χρώμα 23"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_24.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_24.jpg","Color 24","Χρώμα 24"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_25.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_25.jpg","Color 25","Χρώμα 25"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_31.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_31.jpg","Color 31","Χρώμα 31"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_32.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_32.jpg","Color 32","Χρώμα 32"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_33.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_33.jpg","Color 33","Χρώμα 33"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_34.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_34.jpg","Color 34","Χρώμα 34"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_35.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_35.jpg","Color 35","Χρώμα 35"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_51.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_51.jpg","Color 51","Χρώμα 51"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_52.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_52.jpg","Color 52","Χρώμα 52"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_53.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_53.jpg","Color 53","Χρώμα 53"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_54.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_54.jpg","Color 54","Χρώμα 54"));
        orlandoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_orlando_color_55.jpg", Constants.FABRICS_BASE_URL+"orlando/fabric_orlando_color_55.jpg","Color 55","Χρώμα 55"));


        Fabric orlando=new Fabric(R.drawable.orlando,"ORLANDO",orlandoColors);

        ArrayList<String> orlandoCouches=new ArrayList<>();
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_01.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_02.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_03.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_04.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_05.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_11.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_12.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_13.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_14.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_15.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_21.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_22.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_23.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_24.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_25.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_31.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_32.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_33.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_34.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_35.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_51.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_52.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_53.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_54.png");
        orlandoCouches.add(Constants.COUCHES_BASE_URL+"orlando/couch_orlando_color_55.png");
        orlando.setCouches(orlandoCouches);


        ArrayList<String> orlandoCouchBodys=new ArrayList<>();
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_01.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_02.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_03.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_04.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_05.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_11.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_12.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_13.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_14.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_15.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_21.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_22.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_23.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_24.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_25.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_31.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_32.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_33.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_34.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_35.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_51.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_52.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_53.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_54.png");
        orlandoCouchBodys.add(Constants.COUCHES_BODY_URL+"orlando/couch_body_orlando_color_55.png");
        orlando.setCouchBodys(orlandoCouchBodys);

        ArrayList<String> orlandoCouchPillows=new ArrayList<>();
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_01.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_02.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_03.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_04.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_05.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_11.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_12.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_13.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_14.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_15.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_21.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_22.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_23.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_24.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_25.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_31.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_32.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_33.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_34.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_35.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_51.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_52.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_53.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_54.png");
        orlandoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"orlando/couch_pillow_orlando_color_55.png");
        orlando.setCouchPillows(orlandoCouchPillows);


        ArrayList<FabricColor> oceanColors=new ArrayList<>();
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_01.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_01.jpg","Color 01","Χρώμα 01"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_02.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_02.jpg","Color 02","Χρώμα 02"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_03.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_03.jpg","Color 03","Χρώμα 03"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_04.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_04.jpg","Color 04","Χρώμα 04"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_05.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_05.jpg","Color 05","Χρώμα 05"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_06.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_06.jpg","Color 06","Χρώμα 06"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_07.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_07.jpg","Color 07","Χρώμα 07"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_08.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_08.jpg","Color 08","Χρώμα 08"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_09.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_09.jpg","Color 09","Χρώμα 09"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_10.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_10.jpg","Color 10","Χρώμα 10"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_12.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_12.jpg","Color 12","Χρώμα 12"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_14.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_14.jpg","Color 14","Χρώμα 14"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_15.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_15.jpg","Color 15","Χρώμα 15"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_16.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_16.jpg","Color 16","Χρώμα 16"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_19.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_19.jpg","Color 19","Χρώμα 19"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_20.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_20.jpg","Color 20","Χρώμα 20"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_21.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_21.jpg","Color 21","Χρώμα 21"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_23.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_23.jpg","Color 23","Χρώμα 23"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_25.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_25.jpg","Color 25","Χρώμα 25"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_26.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_26.jpg","Color 26","Χρώμα 26"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_27.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_27.jpg","Color 27","Χρώμα 27"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_28.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_28.jpg","Color 28","Χρώμα 28"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_29.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_29.jpg","Color 29","Χρώμα 29"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_30.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_30.jpg","Color 30","Χρώμα 30"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_31.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_31.jpg","Color 31","Χρώμα 31"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_33.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_33.jpg","Color 33","Χρώμα 33"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_34.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_34.jpg","Color 34","Χρώμα 34"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_35.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_35.jpg","Color 35","Χρώμα 35"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_36.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_36.jpg","Color 36","Χρώμα 36"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_37.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_37.jpg","Color 37","Χρώμα 37"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_38.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_38.jpg","Color 38","Χρώμα 38"));
        oceanColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ocean_color_39.jpg", Constants.FABRICS_BASE_URL+"ocean/fabric_ocean_color_39.jpg","Color 39","Χρώμα 39"));

        Fabric ocean=new Fabric(R.drawable.ocean,"OCEAN",oceanColors);

        ArrayList<String> oceanCouches=new ArrayList<>();
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_01.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_02.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_03.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_04.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_05.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_06.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_07.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_08.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_09.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_10.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_12.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_14.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_15.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_16.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_19.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_20.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_21.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_23.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_25.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_26.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_27.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_28.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_29.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_30.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_31.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_33.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_34.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_35.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_36.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_37.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_38.png");
        oceanCouches.add(Constants.SUNBEDS_BASE_URL+"ocean/sunbed_ocean_color_39.png");

        ocean.setCouches(oceanCouches);



        ArrayList<FabricColor> senseColors=new ArrayList<>();
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_21.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_21.jpg","Color 21","Χρώμα 21"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_22.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_22.jpg","Color 22","Χρώμα 22"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_23.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_23.jpg","Color 23","Χρώμα 23"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_24.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_24.jpg","Color 24","Χρώμα 24"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_25.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_25.jpg","Color 25","Χρώμα 25"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_26.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_26.jpg","Color 21","Χρώμα 26"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_27.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_27.jpg","Color 22","Χρώμα 27"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_28.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_28.jpg","Color 23","Χρώμα 28"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_29.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_29.jpg","Color 24","Χρώμα 29"));
        senseColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sense_color_30.jpg", Constants.FABRICS_BASE_URL+"sense/fabric_sense_color_30.jpg","Color 25","Χρώμα 30"));



        Fabric sense=new Fabric(R.drawable.sense,"SENSE",senseColors);

        ArrayList<String> senseCouches=new ArrayList<>();
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_21.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_22.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_23.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_24.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_25.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_26.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_27.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_28.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_29.png");
        senseCouches.add(Constants.COUCHES_BASE_URL+"sense/couch_sense_color_30.png");

        sense.setCouches(senseCouches);

        ArrayList<String> senseCouchBodys=new ArrayList<>();
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_21.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_22.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_23.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_24.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_25.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_26.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_27.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_28.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_29.png");
        senseCouchBodys.add(Constants.COUCHES_BODY_URL+"sense/couch_body_sense_color_30.png");
        sense.setCouchBodys(senseCouchBodys);

        ArrayList<String> senseCouchPillows=new ArrayList<>();
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_21.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_22.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_23.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_24.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_25.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_26.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_27.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_28.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_29.png");
        senseCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"sense/couch_pillow_sense_color_30.png");
        sense.setCouchPillows(senseCouchPillows);


        ArrayList<FabricColor> viennaColors=new ArrayList<>();
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_01.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_01.jpg","Color 01","Χρώμα 01"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_02.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_02.jpg","Color 02","Χρώμα 02"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_03.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_03.jpg","Color 03","Χρώμα 03"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_05.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_05.jpg","Color 05","Χρώμα 05"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_06.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_06.jpg","Color 06","Χρώμα 06"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_07.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_07.jpg","Color 07","Χρώμα 07"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_08.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_08.jpg","Color 08","Χρώμα 08"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_09.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_09.jpg","Color 09","Χρώμα 09"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_11.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_11.jpg","Color 11","Χρώμα 11"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_12.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_12.jpg","Color 12","Χρώμα 12"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_13.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_13.jpg","Color 13","Χρώμα 13"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_15.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_15.jpg","Color 15","Χρώμα 15"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_16.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_16.jpg","Color 16","Χρώμα 16"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_17.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_17.jpg","Color 17","Χρώμα 17"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_18.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_18.jpg","Color 18","Χρώμα 18"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_19.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_19.jpg","Color 19","Χρώμα 19"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_32.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_32.jpg","Color 32","Χρώμα 32"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_41.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_41.jpg","Color 41","Χρώμα 41"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_42.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_42.jpg","Color 42","Χρώμα 42"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_43.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_43.jpg","Color 43","Χρώμα 43"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_44.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_44.jpg","Color 44","Χρώμα 44"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_45.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_45.jpg","Color 45","Χρώμα 45"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_46.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_46.jpg","Color 46","Χρώμα 46"));
        viennaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vienna_color_49.jpg", Constants.FABRICS_BASE_URL+"vienna/fabric_vienna_color_49.jpg","Color 49","Χρώμα 49"));





        Fabric vienna=new Fabric(R.drawable.vienna,"VIENNA",viennaColors);

        ArrayList<String> viennaCouches=new ArrayList<>();
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_01.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_02.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_03.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_05.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_06.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_07.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_08.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_09.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_11.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_12.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_13.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_15.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_16.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_17.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_18.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_19.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_32.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_41.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_42.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_43.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_44.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_45.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_46.png");
        viennaCouches.add(Constants.COUCHES_BASE_URL+"vienna/couch_vienna_color_49.png");



        vienna.setCouches(viennaCouches);

        ArrayList<String> viennaCouchBodys=new ArrayList<>();
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_01.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_02.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_03.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_05.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_06.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_07.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_08.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_09.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_11.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_12.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_13.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_15.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_16.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_17.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_18.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_19.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_32.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_41.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_42.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_43.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_44.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_45.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_46.png");
        viennaCouchBodys.add(Constants.COUCHES_BODY_URL+"vienna/couch_body_vienna_color_49.png");

        vienna.setCouchBodys(viennaCouchBodys);

        ArrayList<String> viennaCouchPillows=new ArrayList<>();
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_01.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_02.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_03.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_05.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_06.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_07.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_08.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_09.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_11.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_12.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_13.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_15.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_16.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_17.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_18.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_19.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_32.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_41.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_42.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_43.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_44.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_45.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_46.png");
        viennaCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"vienna/couch_pillow_vienna_color_49.png");
        vienna.setCouchPillows(viennaCouchPillows);




        ArrayList<FabricColor> cuervoColors=new ArrayList<>();

        cuervoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cuervo_color_01.jpg", Constants.FABRICS_BASE_URL+"cuervo/fabric_cuervo_color_01.jpg","Color 01","Χρώμα 01"));
        cuervoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cuervo_color_02.jpg", Constants.FABRICS_BASE_URL+"cuervo/fabric_cuervo_color_02.jpg","Color 02","Χρώμα 02"));
        cuervoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cuervo_color_03.jpg", Constants.FABRICS_BASE_URL+"cuervo/fabric_cuervo_color_03.jpg","Color 03","Χρώμα 03"));
        cuervoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cuervo_color_04.jpg", Constants.FABRICS_BASE_URL+"cuervo/fabric_cuervo_color_04.jpg","Color 04","Χρώμα 04"));

        Fabric cuervo=new Fabric(R.drawable.cuervo,"CUERVO",cuervoColors);

        ArrayList<String> cuervoCouches=new ArrayList<>();
        cuervoCouches.add(Constants.COUCHES_BASE_URL+"cuervo/couch_cuervo_color_01.png");
        cuervoCouches.add(Constants.COUCHES_BASE_URL+"cuervo/couch_cuervo_color_02.png");
        cuervoCouches.add(Constants.COUCHES_BASE_URL+"cuervo/couch_cuervo_color_03.png");
        cuervoCouches.add(Constants.COUCHES_BASE_URL+"cuervo/couch_cuervo_color_04.png");

        cuervo.setCouches(cuervoCouches);




        ArrayList <String> cuervoCouchBodys=new ArrayList<>();
        cuervoCouchBodys.add(Constants.COUCHES_BODY_URL+"cuervo/couch_body_cuervo_color_01.png");
        cuervoCouchBodys.add(Constants.COUCHES_BODY_URL+"cuervo/couch_body_cuervo_color_02.png");
        cuervoCouchBodys.add(Constants.COUCHES_BODY_URL+"cuervo/couch_body_cuervo_color_03.png");
        cuervoCouchBodys.add(Constants.COUCHES_BODY_URL+"cuervo/couch_body_cuervo_color_04.png");

        cuervo.setCouchBodys(cuervoCouchBodys);

        ArrayList<String> cuervoCouchPillows=new ArrayList<>();
        cuervoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cuervo/couch_pillow_cuervo_color_01.png");
        cuervoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cuervo/couch_pillow_cuervo_color_02.png");
        cuervoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cuervo/couch_pillow_cuervo_color_03.png");
        cuervoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cuervo/couch_pillow_cuervo_color_04.png");

        cuervo.setCouchPillows(cuervoCouchPillows);

        ArrayList<FabricColor> macaoColors=new ArrayList<>();
        macaoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_macao_color_101.jpg", Constants.FABRICS_BASE_URL+"macao/fabric_macao_color_101.jpg","Color 101","Χρώμα 101"));
        macaoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_macao_color_102.jpg", Constants.FABRICS_BASE_URL+"macao/fabric_macao_color_102.jpg","Color 102","Χρώμα 102"));
        macaoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_macao_color_103.jpg", Constants.FABRICS_BASE_URL+"macao/fabric_macao_color_103.jpg","Color 103","Χρώμα 103"));
        macaoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_macao_color_104.jpg", Constants.FABRICS_BASE_URL+"macao/fabric_macao_color_104.jpg","Color 104","Χρώμα 104"));
        macaoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_macao_color_105.jpg", Constants.FABRICS_BASE_URL+"macao/fabric_macao_color_105.jpg","Color 105","Χρώμα 105"));
        macaoColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_macao_color_106.jpg", Constants.FABRICS_BASE_URL+"macao/fabric_macao_color_106.jpg","Color 106","Χρώμα 106"));

        Fabric macao=new Fabric(R.drawable.macao,"MACAO",macaoColors);

        ArrayList<String> macaoCouches=new ArrayList<>();
        macaoCouches.add(Constants.COUCHES_BASE_URL+"macao/couch_macao_color_101.png");
        macaoCouches.add(Constants.COUCHES_BASE_URL+"macao/couch_macao_color_102.png");
        macaoCouches.add(Constants.COUCHES_BASE_URL+"macao/couch_macao_color_103.png");
        macaoCouches.add(Constants.COUCHES_BASE_URL+"macao/couch_macao_color_104.png");
        macaoCouches.add(Constants.COUCHES_BASE_URL+"macao/couch_macao_color_105.png");
        macaoCouches.add(Constants.COUCHES_BASE_URL+"macao/couch_macao_color_106.png");
        
        macao.setCouches(macaoCouches);

        ArrayList<String> macaoCouchBodys=new ArrayList<>();
        macaoCouchBodys.add(Constants.COUCHES_BODY_URL+"macao/couch_body_macao_color_101.png");
        macaoCouchBodys.add(Constants.COUCHES_BODY_URL+"macao/couch_body_macao_color_102.png");
        macaoCouchBodys.add(Constants.COUCHES_BODY_URL+"macao/couch_body_macao_color_103.png");
        macaoCouchBodys.add(Constants.COUCHES_BODY_URL+"macao/couch_body_macao_color_104.png");
        macaoCouchBodys.add(Constants.COUCHES_BODY_URL+"macao/couch_body_macao_color_105.png");
        macaoCouchBodys.add(Constants.COUCHES_BODY_URL+"macao/couch_body_macao_color_106.png");
        macao.setCouchBodys(macaoCouchBodys);

        ArrayList<String> macaoCouchPillows=new ArrayList<>();
        macaoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"macao/couch_pillow_macao_color_101.png");
        macaoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"macao/couch_pillow_macao_color_102.png");
        macaoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"macao/couch_pillow_macao_color_103.png");
        macaoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"macao/couch_pillow_macao_color_104.png");
        macaoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"macao/couch_pillow_macao_color_105.png");
        macaoCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"macao/couch_pillow_macao_color_106.png");

        macao.setCouchPillows(macaoCouchPillows);

        ArrayList<FabricColor> tenerifeColors=new ArrayList<>();
        tenerifeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tenerife_color_101.jpg", Constants.FABRICS_BASE_URL+"tenerife/fabric_tenerife_color_101.jpg","Color 101","Χρώμα 101"));
        tenerifeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tenerife_color_102.jpg", Constants.FABRICS_BASE_URL+"tenerife/fabric_tenerife_color_102.jpg","Color 102","Χρώμα 102"));
        tenerifeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tenerife_color_103.jpg", Constants.FABRICS_BASE_URL+"tenerife/fabric_tenerife_color_103.jpg","Color 103","Χρώμα 103"));
        tenerifeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tenerife_color_104.jpg", Constants.FABRICS_BASE_URL+"tenerife/fabric_tenerife_color_104.jpg","Color 104","Χρώμα 104"));
        tenerifeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tenerife_color_105.jpg", Constants.FABRICS_BASE_URL+"tenerife/fabric_tenerife_color_105.jpg","Color 105","Χρώμα 105"));
        tenerifeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tenerife_color_106.jpg", Constants.FABRICS_BASE_URL+"tenerife/fabric_tenerife_color_106.jpg","Color 106","Χρώμα 106"));

        Fabric tenerife=new Fabric(R.drawable.tenerife,"TENERIFE",tenerifeColors);

        ArrayList<String> tenerifeCouches=new ArrayList<>();
        tenerifeCouches.add(Constants.COUCHES_BASE_URL+"tenerife/couch_tenerife_color_101.png");
        tenerifeCouches.add(Constants.COUCHES_BASE_URL+"tenerife/couch_tenerife_color_102.png");
        tenerifeCouches.add(Constants.COUCHES_BASE_URL+"tenerife/couch_tenerife_color_103.png");
        tenerifeCouches.add(Constants.COUCHES_BASE_URL+"tenerife/couch_tenerife_color_104.png");
        tenerifeCouches.add(Constants.COUCHES_BASE_URL+"tenerife/couch_tenerife_color_105.png");
        tenerifeCouches.add(Constants.COUCHES_BASE_URL+"tenerife/couch_tenerife_color_106.png");
        
        tenerife.setCouches(tenerifeCouches);

        ArrayList<String> tenerifeCouchBodys=new ArrayList<>();
        tenerifeCouchBodys.add(Constants.COUCHES_BODY_URL+"tenerife/couch_body_tenerife_color_101.png");
        tenerifeCouchBodys.add(Constants.COUCHES_BODY_URL+"tenerife/couch_body_tenerife_color_102.png");
        tenerifeCouchBodys.add(Constants.COUCHES_BODY_URL+"tenerife/couch_body_tenerife_color_103.png");
        tenerifeCouchBodys.add(Constants.COUCHES_BODY_URL+"tenerife/couch_body_tenerife_color_104.png");
        tenerifeCouchBodys.add(Constants.COUCHES_BODY_URL+"tenerife/couch_body_tenerife_color_105.png");
        tenerifeCouchBodys.add(Constants.COUCHES_BODY_URL+"tenerife/couch_body_tenerife_color_106.png");
        
        tenerife.setCouchBodys(tenerifeCouchBodys);

        ArrayList<String> tenerifeCouchPillows=new ArrayList<>();
        tenerifeCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"tenerife/couch_pillow_tenerife_color_101.png");
        tenerifeCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"tenerife/couch_pillow_tenerife_color_102.png");
        tenerifeCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"tenerife/couch_pillow_tenerife_color_103.png");
        tenerifeCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"tenerife/couch_pillow_tenerife_color_104.png");
        tenerifeCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"tenerife/couch_pillow_tenerife_color_105.png");
        tenerifeCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"tenerife/couch_pillow_tenerife_color_106.png");

        tenerife.setCouchPillows(tenerifeCouchPillows);

        ArrayList<FabricColor> utopiaColors=new ArrayList<>();
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_01.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_01.jpg","Color 01","Χρώμα 01"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_02.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_02.jpg","Color 02","Χρώμα 02"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_03.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_03.jpg","Color 03","Χρώμα 03"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_101.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_101.jpg","Color 101","Χρώμα 101"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_102.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_102.jpg","Color 102","Χρώμα 102"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_103.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_103.jpg","Color 103","Χρώμα 103"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_201.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_201.jpg","Color 201","Χρώμα 201"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_202.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_202.jpg","Color 202","Χρώμα 202"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_203.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_203.jpg","Color 203","Χρώμα 203"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_301.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_301.jpg","Color 301","Χρώμα 301"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_302.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_302.jpg","Color 302","Χρώμα 302"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_303.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_303.jpg","Color 303","Χρώμα 303"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_304.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_304.jpg","Color 304","Χρώμα 304"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_401.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_401.jpg","Color 401","Χρώμα 401"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_402.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_402.jpg","Color 402","Χρώμα 402"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_403.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_403.jpg","Color 403","Χρώμα 403"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_404.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_404.jpg","Color 404","Χρώμα 404"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_501.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_501.jpg","Color 501","Χρώμα 501"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_502.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_502.jpg","Color 502","Χρώμα 502"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_503.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_503.jpg","Color 503","Χρώμα 503"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_601.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_601.jpg","Color 601","Χρώμα 601"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_602.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_602.jpg","Color 602","Χρώμα 602"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_603.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_603.jpg","Color 603","Χρώμα 603"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_604.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_604.jpg","Color 604","Χρώμα 604"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_701.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_701.jpg","Color 701","Χρώμα 701"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_702.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_702.jpg","Color 702","Χρώμα 702"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_801.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_801.jpg","Color 801","Χρώμα 801"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_802.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_802.jpg","Color 802","Χρώμα 802"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_901.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_901.jpg","Color 901","Χρώμα 901"));
        utopiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_utopia_color_902.jpg", Constants.FABRICS_BASE_URL+"utopia/fabric_utopia_color_902.jpg","Color 902","Χρώμα 902"));

        Fabric utopia=new Fabric(R.drawable.utopia,"UTOPIA",utopiaColors);

        ArrayList<String> utopiaCouches=new ArrayList<>();
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_01.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_02.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_03.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_101.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_102.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_103.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_201.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_202.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_203.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_301.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_302.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_303.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_304.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_401.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_402.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_403.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_404.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_501.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_502.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_503.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_601.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_602.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_603.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_604.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_701.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_702.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_801.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_802.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_901.png");
        utopiaCouches.add(Constants.COUCHES_BASE_URL+"utopia/couch_utopia_color_902.png");
        
        utopia.setCouches(utopiaCouches);
        
        ArrayList<String> utopiaBodys=new ArrayList<>();

        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_01.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_02.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_03.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_101.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_102.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_103.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_201.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_202.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_203.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_301.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_302.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_303.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_304.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_401.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_402.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_403.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_404.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_501.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_502.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_503.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_601.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_602.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_603.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_604.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_701.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_702.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_801.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_802.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_901.png");
        utopiaBodys.add(Constants.COUCHES_BODY_URL+"utopia/couch_body_utopia_color_902.png");
        
        utopia.setCouchBodys(utopiaBodys);

        ArrayList<String> utopiaPillows=new ArrayList<>();
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_01.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_02.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_03.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_101.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_102.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_103.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_201.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_202.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_203.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_301.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_302.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_303.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_304.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_401.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_402.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_403.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_404.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_501.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_502.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_503.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_601.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_602.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_603.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_604.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_701.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_702.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_801.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_802.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_901.png");
        utopiaPillows.add(Constants.COUCHES_PILLOWS_URL+"utopia/couch_pillow_utopia_color_902.png");
        
        utopia.setCouchPillows(utopiaPillows);

        ArrayList<FabricColor> theronColors=new ArrayList<>();
        theronColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_theron_color_krem.jpg", Constants.FABRICS_BASE_URL+"theron/fabric_theron_color_krem.jpg","Krem","Krem"));
        theronColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_theron_color_nohut.jpg", Constants.FABRICS_BASE_URL+"theron/fabric_theron_color_nohut.jpg","Nohut","Nohut"));
        theronColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_theron_color_pudra.jpg", Constants.FABRICS_BASE_URL+"theron/fabric_theron_color_pudra.jpg","Pudra","Pudra"));

        Fabric theron=new Fabric(R.drawable.theron,"THERON",theronColors);

        ArrayList<String> theronCouches=new ArrayList<>();
        theronCouches.add(Constants.COUCHES_BASE_URL+"theron/couch_theron_color_krem.png");
        theronCouches.add(Constants.COUCHES_BASE_URL+"theron/couch_theron_color_nohut.png");
        theronCouches.add(Constants.COUCHES_BASE_URL+"theron/couch_theron_color_pudra.png");

        theron.setCouches(theronCouches);

        ArrayList<String> theronCouchBodys=new ArrayList<>();
        theronCouchBodys.add(Constants.COUCHES_BODY_URL+"theron/couch_body_theron_color_krem.png");
        theronCouchBodys.add(Constants.COUCHES_BODY_URL+"theron/couch_body_theron_color_nohut.png");
        theronCouchBodys.add(Constants.COUCHES_BODY_URL+"theron/couch_body_theron_color_pudra.png");

        theron.setCouchBodys(theronCouchBodys);

        ArrayList<String> theronCouchPillows=new ArrayList<>();
        theronCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"theron/couch_pillow_theron_color_krem.png");
        theronCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"theron/couch_pillow_theron_color_nohut.png");
        theronCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"theron/couch_pillow_theron_color_pudra.png");

        theron.setCouchPillows(theronCouchPillows);

        ArrayList<FabricColor> cantuColors=new ArrayList<>();
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_01.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_01.jpg","Color 01","Χρώμα 01"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_02.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_02.jpg","Color 02","Χρώμα 02"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_03.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_03.jpg","Color 03","Χρώμα 03"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_04.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_04.jpg","Color 04","Χρώμα 04"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_05.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_05.jpg","Color 05","Χρώμα 05"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_06.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_06.jpg","Color 06","Χρώμα 06"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_07.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_07.jpg","Color 07","Χρώμα 07"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_08.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_08.jpg","Color 08","Χρώμα 08"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_09.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_09.jpg","Color 09","Χρώμα 09"));
        cantuColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_cantu_color_10.jpg", Constants.FABRICS_BASE_URL+"cantu/fabric_cantu_color_10.jpg","Color 10","Χρώμα 10"));
        
        Fabric cantu=new Fabric(R.drawable.cantu,"CANTU",cantuColors);
        
        ArrayList<String> cantuCouches=new ArrayList<>();
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_01.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_02.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_03.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_04.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_05.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_06.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_07.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_08.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_09.png");
        cantuCouches.add(Constants.COUCHES_BASE_URL+"cantu/couch_cantu_color_10.png");
        
        cantu.setCouches(calmCouches);

        ArrayList<String> cantuCouchBodys=new ArrayList<>();
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_01.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_02.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_03.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_04.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_05.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_06.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_07.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_08.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_09.png");
        cantuCouchBodys.add(Constants.COUCHES_BODY_URL+"cantu/couch_body_cantu_color_10.png");
        
        cantu.setCouchBodys(cantuCouchBodys);

        ArrayList<String> cantuCouchPillows=new ArrayList<>();
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_01.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_02.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_03.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_04.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_05.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_06.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_07.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_08.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_09.png");
        cantuCouchPillows.add(Constants.COUCHES_PILLOWS_URL+"cantu/couch_pillow_cantu_color_10.png");

        cantu.setCouchPillows(cantuCouchPillows);












        //Here the curtain fabrics are starting
        ArrayList<FabricColor> monikaColors=new ArrayList<>();
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140041.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140041.jpg","Color 14004/1","Χρώμα 14004/1"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140042.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140042.jpg","Color 14004/2","Χρώμα 14004/2"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140043.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140043.jpg","Color 14004/3","Χρώμα 14004/3"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140044.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140044.jpg","Color 14004/4","Χρώμα 14004/4"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140045.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140045.jpg","Color 14004/5","Χρώμα 14004/5"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140051.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140051.jpg","Color 14005/1","Χρώμα 14005/1"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140052.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140052.jpg","Color 14005/2","Χρώμα 14005/2"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140053.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140053.jpg","Color 14005/3","Χρώμα 14005/3"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140054.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140054.jpg","Color 14005/4","Χρώμα 14005/4"));
        monikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_monika_color_140055.jpg", Constants.FABRICS_BASE_URL+"monika/fabric_monika_color_140055.jpg","Color 14005/5","Χρώμα 14005/5"));

        Fabric monika=new Fabric(R.drawable.monika,"MONIKA",monikaColors);
        ArrayList<String> monikaCurtains=new ArrayList<>();
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140041.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140042.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140043.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140044.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140045.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140051.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140052.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140053.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140054.png");
        monikaCurtains.add(Constants.CURTAINS_BASE_URL+"monika/curtain_monika_color_140055.png");
        monika.setCurtains(monikaCurtains);

        ArrayList<FabricColor> afroditiColors=new ArrayList<>();
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_1a.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_1A.jpg","Color 14001/1A","Χρώμα 14001/1A"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_1b.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_1B.jpg","Color 14001/1B","Χρώμα 14001/1B"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_2a.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_2A.jpg","Color 14001/2A","Χρώμα 14001/2A"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_2b.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_2B.jpg","Color 14001/2B","Χρώμα 14001/2B"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_3a.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_3A.jpg","Color 14001/3A","Χρώμα 14001/3A"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_3b.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_3B.jpg","Color 14001/3B","Χρώμα 14001/3B"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_4a.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_4A.jpg","Color 14001/4A","Χρώμα 14001/4A"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_4b.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_4B.jpg","Color 14001/4B","Χρώμα 14001/4B"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_5a.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_5A.jpg","Color 14001/5A","Χρώμα 14001/5A"));
        afroditiColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_afroditi_color_5b.jpg", Constants.FABRICS_BASE_URL+"afroditi/fabric_afroditi_color_5B.jpg","Color 14001/5B","Χρώμα 14001/5B"));

        Fabric afroditi=new Fabric(R.drawable.afroditi,"AFRODITI",afroditiColors);

        ArrayList<String> afroditiCurtains=new ArrayList<>();
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_1A.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_1B.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_2A.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_2B.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_3A.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_3B.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_4A.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_4B.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_5A.png");
        afroditiCurtains.add(Constants.CURTAINS_BASE_URL+"afroditi/curtain_afroditi_color_5B.png");

        afroditi.setCurtains(afroditiCurtains);


        ArrayList<FabricColor> antoinetteColors=new ArrayList<>();
        antoinetteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_antoinette_color_01.jpg", Constants.FABRICS_BASE_URL+"antoinette/fabric_antoinette_color_01.jpg","Color 25001/01","Χρώμα 25001/01"));
        antoinetteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_antoinette_color_02.jpg", Constants.FABRICS_BASE_URL+"antoinette/fabric_antoinette_color_02.jpg","Color 25001/02","Χρώμα 25001/02"));
        antoinetteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_antoinette_color_03.jpg", Constants.FABRICS_BASE_URL+"antoinette/fabric_antoinette_color_03.jpg","Color 25001/03","Χρώμα 25001/03"));
        Fabric antoinette=new Fabric(R.drawable.antoinette,"ANTOINETTE",antoinetteColors);

        ArrayList<String> antoinetteCurtains=new ArrayList<>();
        antoinetteCurtains.add(Constants.CURTAINS_BASE_URL+"antoinette/curtain_antoinette_color_01.png");
        antoinetteCurtains.add(Constants.CURTAINS_BASE_URL+"antoinette/curtain_antoinette_color_02.png");
        antoinetteCurtains.add(Constants.CURTAINS_BASE_URL+"antoinette/curtain_antoinette_color_03.png");

        antoinette.setCurtains(antoinetteCurtains);

        ArrayList<FabricColor> barbaraColors=new ArrayList<>();
        barbaraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_barbara_color_01.jpg", Constants.FABRICS_BASE_URL+"barbara/fabric_barbara_color_01.jpg","Color 12002/01","Χρώμα 12002/01"));
        barbaraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_barbara_color_02.jpg", Constants.FABRICS_BASE_URL+"barbara/fabric_barbara_color_02.jpg","Color 12002/02","Χρώμα 12002/02"));
        barbaraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_barbara_color_03.jpg", Constants.FABRICS_BASE_URL+"barbara/fabric_barbara_color_03.jpg","Color 12002/03","Χρώμα 12002/03"));
        barbaraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_barbara_color_04.jpg", Constants.FABRICS_BASE_URL+"barbara/fabric_barbara_color_04.jpg","Color 12002/04","Χρώμα 12002/04"));

        Fabric barbara=new Fabric(R.drawable.barbara,"BARBARA",barbaraColors);
        ArrayList<String> barbaraCurtains=new ArrayList<>();
        barbaraCurtains.add(Constants.CURTAINS_BASE_URL+"barbara/curtain_barbara_color_01.png");
        barbaraCurtains.add(Constants.CURTAINS_BASE_URL+"barbara/curtain_barbara_color_02.png");
        barbaraCurtains.add(Constants.CURTAINS_BASE_URL+"barbara/curtain_barbara_color_03.png");
        barbaraCurtains.add(Constants.CURTAINS_BASE_URL+"barbara/curtain_barbara_color_04.png");

        barbara.setCurtains(barbaraCurtains);

        ArrayList<FabricColor> brigitteColors=new ArrayList<>();
        brigitteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brigitte_color_01.jpg", Constants.FABRICS_BASE_URL+"brigitte/fabric_brigitte_color_01.jpg","Color 20010/01","Χρώμα 20010/01"));
        brigitteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brigitte_color_02.jpg", Constants.FABRICS_BASE_URL+"brigitte/fabric_brigitte_color_02.jpg","Color 20010/02","Χρώμα 20010/02"));
        brigitteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brigitte_color_03.jpg", Constants.FABRICS_BASE_URL+"brigitte/fabric_brigitte_color_03.jpg","Color 20010/03","Χρώμα 20010/03"));
        brigitteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brigitte_color_04.jpg", Constants.FABRICS_BASE_URL+"brigitte/fabric_brigitte_color_04.jpg","Color 20010/04","Χρώμα 20010/04"));
        brigitteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brigitte_color_05.jpg", Constants.FABRICS_BASE_URL+"brigitte/fabric_brigitte_color_05.jpg","Color 20010/05","Χρώμα 20010/05"));
        brigitteColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brigitte_color_06.jpg", Constants.FABRICS_BASE_URL+"brigitte/fabric_brigitte_color_06.jpg","Color 20010/06","Χρώμα 20010/06"));

        Fabric brigitte=new Fabric(R.drawable.brigitte,"BRIGITTE",brigitteColors);
        ArrayList<String> brigitteCurtains=new ArrayList<>();
        brigitteCurtains.add(Constants.CURTAINS_BASE_URL+"brigitte/curtain_brigitte_color_01.png");
        brigitteCurtains.add(Constants.CURTAINS_BASE_URL+"brigitte/curtain_brigitte_color_02.png");
        brigitteCurtains.add(Constants.CURTAINS_BASE_URL+"brigitte/curtain_brigitte_color_03.png");
        brigitteCurtains.add(Constants.CURTAINS_BASE_URL+"brigitte/curtain_brigitte_color_04.png");
        brigitteCurtains.add(Constants.CURTAINS_BASE_URL+"brigitte/curtain_brigitte_color_05.png");
        brigitteCurtains.add(Constants.CURTAINS_BASE_URL+"brigitte/curtain_brigitte_color_06.png");
        brigitte.setCurtains(brigitteCurtains);



        ArrayList<FabricColor> electraColors=new ArrayList<>();
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130081.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130081.jpg","Color 13008/1","Χρώμα 13008/1"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130082.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130082.jpg","Color 13008/2","Χρώμα 13008/2"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130083.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130083.jpg","Color 13008/3","Χρώμα 13008/3"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130084.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130084.jpg","Color 13008/4","Χρώμα 13008/4"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130085.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130085.jpg","Color 13008/5","Χρώμα 13008/5"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130091.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130091.jpg","Color 13009/1","Χρώμα 13009/1"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130092.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130092.jpg","Color 13009/2","Χρώμα 13009/2"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130093.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130093.jpg","Color 13009/3","Χρώμα 13009/3"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130094.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130094.jpg","Color 13009/4","Χρώμα 13009/4"));
        electraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_electra_color_130095.jpg", Constants.FABRICS_BASE_URL+"electra/fabric_electra_color_130095.jpg","Color 13009/5","Χρώμα 13009/5"));

        Fabric electra=new Fabric(R.drawable.electra,"ELECTRA",electraColors);
        ArrayList<String> electraCurtains=new ArrayList<>();
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130081.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130082.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130083.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130084.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130085.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130091.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130092.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130093.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130094.png");
        electraCurtains.add(Constants.CURTAINS_BASE_URL+"electra/curtain_electra_color_130095.png");
        electra.setCurtains(electraCurtains);

        ArrayList<FabricColor> elisabethColors=new ArrayList<>();
        elisabethColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elisabeth_color_130021.jpg", Constants.FABRICS_BASE_URL+"elisabeth/fabric_elisabeth_color_130021.jpg","Color 13002/1","Χρώμα 13002/1"));
        elisabethColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elisabeth_color_130022.jpg", Constants.FABRICS_BASE_URL+"elisabeth/fabric_elisabeth_color_130022.jpg","Color 13002/2","Χρώμα 13002/2"));
        elisabethColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elisabeth_color_130023.jpg", Constants.FABRICS_BASE_URL+"elisabeth/fabric_elisabeth_color_130023.jpg","Color 13002/3","Χρώμα 13002/3"));
        elisabethColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elisabeth_color_130031.jpg", Constants.FABRICS_BASE_URL+"elisabeth/fabric_elisabeth_color_130031.jpg","Color 13003/1","Χρώμα 13003/1"));
        elisabethColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elisabeth_color_130032.jpg", Constants.FABRICS_BASE_URL+"elisabeth/fabric_elisabeth_color_130032.jpg","Color 13003/2","Χρώμα 13003/2"));
        elisabethColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_elisabeth_color_130033.jpg", Constants.FABRICS_BASE_URL+"elisabeth/fabric_elisabeth_color_130033.jpg","Color 13003/3","Χρώμα 13003/3"));

        Fabric elisabeth=new Fabric(R.drawable.elisabeth,"ELISABETH",elisabethColors);

        ArrayList<String> elisabethCurtains=new ArrayList<>();
        elisabethCurtains.add(Constants.CURTAINS_BASE_URL+"elisabeth/curtain_elisabeth_color_130021.png");
        elisabethCurtains.add(Constants.CURTAINS_BASE_URL+"elisabeth/curtain_elisabeth_color_130022.png");
        elisabethCurtains.add(Constants.CURTAINS_BASE_URL+"elisabeth/curtain_elisabeth_color_130023.png");
        elisabethCurtains.add(Constants.CURTAINS_BASE_URL+"elisabeth/curtain_elisabeth_color_130031.png");
        elisabethCurtains.add(Constants.CURTAINS_BASE_URL+"elisabeth/curtain_elisabeth_color_130032.png");
        elisabethCurtains.add(Constants.CURTAINS_BASE_URL+"elisabeth/curtain_elisabeth_color_130033.png");

        elisabeth.setCurtains(elisabethCurtains);


        ArrayList<FabricColor> emilyColors=new ArrayList<>();
        emilyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_emily_color_01.jpg", Constants.FABRICS_BASE_URL+"emily/fabric_emily_color_01.jpg","Color 14003/01","Χρώμα 14003/01"));
        emilyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_emily_color_02.jpg", Constants.FABRICS_BASE_URL+"emily/fabric_emily_color_02.jpg","Color 14003/02","Χρώμα 14003/02"));
        emilyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_emily_color_03.jpg", Constants.FABRICS_BASE_URL+"emily/fabric_emily_color_03.jpg","Color 14003/03","Χρώμα 14003/03"));
        emilyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_emily_color_04.jpg", Constants.FABRICS_BASE_URL+"emily/fabric_emily_color_04.jpg","Color 14003/04","Χρώμα 14003/04"));

        Fabric emily=new Fabric(R.drawable.emily,"EMILY",emilyColors);

        ArrayList<String> emilyCurtains=new ArrayList<>();
        emilyCurtains.add(Constants.CURTAINS_BASE_URL+"emily/curtain_emily_color_01.png");
        emilyCurtains.add(Constants.CURTAINS_BASE_URL+"emily/curtain_emily_color_02.png");
        emilyCurtains.add(Constants.CURTAINS_BASE_URL+"emily/curtain_emily_color_03.png");
        emilyCurtains.add(Constants.CURTAINS_BASE_URL+"emily/curtain_emily_color_04.png");

        emily.setCurtains(emilyCurtains);

        ArrayList<FabricColor> estellaColors=new ArrayList<>();
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200012.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200012.jpg","Color 20001/2","Χρώμα 20001/2"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200021.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200021.jpg","Color 20002/1","Χρώμα 20002/1"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200022.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200022.jpg","Color 20002/2","Χρώμα 20002/2"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200023.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200023.jpg","Color 20002/3","Χρώμα 20002/3"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200024.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200024.jpg","Color 20002/4","Χρώμα 20002/4"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200025.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200025.jpg","Color 20002/5","Χρώμα 20002/5"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200026.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200026.jpg","Color 20002/6","Χρώμα 20002/6"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200031.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200031.jpg","Color 20003/1","Χρώμα 20003/1"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200032.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200032.jpg","Color 20003/2","Χρώμα 20003/2"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200033.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200033.jpg","Color 20003/3","Χρώμα 20003/3"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200034.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200034.jpg","Color 20003/4","Χρώμα 20003/4"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200035.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200035.jpg","Color 20003/5","Χρώμα 20003/5"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200036.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200036.jpg","Color 20003/6","Χρώμα 20003/6"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200041.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200041.jpg","Color 20004/1","Χρώμα 20004/1"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200042.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200042.jpg","Color 20004/2","Χρώμα 20004/2"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200043.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200043.jpg","Color 20004/3","Χρώμα 20004/3"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200044.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200044.jpg","Color 20004/4","Χρώμα 20004/4"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200045.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200045.jpg","Color 20004/5","Χρώμα 20004/5"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200046.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200046.jpg","Color 20004/6","Χρώμα 20004/6"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200051.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200051.jpg","Color 20005/1","Χρώμα 20005/1"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200052.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200052.jpg","Color 20005/2","Χρώμα 20005/2"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200053.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200053.jpg","Color 20005/3","Χρώμα 20005/3"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200054.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200054.jpg","Color 20005/4","Χρώμα 20005/4"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200055.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200055.jpg","Color 20005/5","Χρώμα 20005/5"));
        estellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_estella_color_200056.jpg", Constants.FABRICS_BASE_URL+"estella/fabric_estella_color_200056.jpg","Color 20005/6","Χρώμα 20005/6"));

        Fabric estella=new Fabric(R.drawable.estella,"ESTELLA",estellaColors);

        ArrayList<String> estellaCurtains=new ArrayList<>();
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200012.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200021.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200022.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200023.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200024.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200025.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200026.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200031.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200032.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200033.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200034.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200035.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200036.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200041.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200042.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200043.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200044.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200045.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200046.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200051.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200052.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200053.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200054.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200055.png");
        estellaCurtains.add(Constants.CURTAINS_BASE_URL+"estella/curtain_estella_color_200056.png");

        estella.setCurtains(estellaCurtains);



        ArrayList<FabricColor> eugeniaColors=new ArrayList<>();
        eugeniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eugenia_color_01.jpg", Constants.FABRICS_BASE_URL+"eugenia/fabric_eugenia_color_01.jpg","Color 15003/01","Χρώμα 15003/01"));
        eugeniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eugenia_color_02.jpg", Constants.FABRICS_BASE_URL+"eugenia/fabric_eugenia_color_02.jpg","Color 15003/02","Χρώμα 15003/02"));
        eugeniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eugenia_color_03.jpg", Constants.FABRICS_BASE_URL+"eugenia/fabric_eugenia_color_03.jpg","Color 15003/03","Χρώμα 15003/03"));
        eugeniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eugenia_color_04.jpg", Constants.FABRICS_BASE_URL+"eugenia/fabric_eugenia_color_04.jpg","Color 15003/04","Χρώμα 15003/04"));
        eugeniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eugenia_color_05.jpg", Constants.FABRICS_BASE_URL+"eugenia/fabric_eugenia_color_05.jpg","Color 15003/05","Χρώμα 15003/05"));

        Fabric eugenia=new Fabric(R.drawable.eugenia,"EUGENIA",eugeniaColors);

        ArrayList<String> eugeniaCurtains=new ArrayList<>();
        eugeniaCurtains.add(Constants.CURTAINS_BASE_URL+"eugenia/curtain_eugenia_color_01.png");
        eugeniaCurtains.add(Constants.CURTAINS_BASE_URL+"eugenia/curtain_eugenia_color_02.png");
        eugeniaCurtains.add(Constants.CURTAINS_BASE_URL+"eugenia/curtain_eugenia_color_03.png");
        eugeniaCurtains.add(Constants.CURTAINS_BASE_URL+"eugenia/curtain_eugenia_color_04.png");
        eugeniaCurtains.add(Constants.CURTAINS_BASE_URL+"eugenia/curtain_eugenia_color_05.png");

        eugenia.setCurtains(eugeniaCurtains);

        ArrayList<FabricColor> feliciaColors=new ArrayList<>();
        feliciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_felicia_color_01.jpg", Constants.FABRICS_BASE_URL+"felicia/fabric_felicia_color_01.jpg","Color 12004/01","Χρώμα 12004/01"));
        feliciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_felicia_color_02.jpg", Constants.FABRICS_BASE_URL+"felicia/fabric_felicia_color_02.jpg","Color 12004/02","Χρώμα 12004/02"));
        feliciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_felicia_color_03.jpg", Constants.FABRICS_BASE_URL+"felicia/fabric_felicia_color_03.jpg","Color 12004/03","Χρώμα 12004/03"));
        feliciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_felicia_color_04.jpg", Constants.FABRICS_BASE_URL+"felicia/fabric_felicia_color_04.jpg","Color 12004/04","Χρώμα 12004/04"));

        Fabric felicia=new Fabric(R.drawable.felicia,"FELICIA",feliciaColors);

        ArrayList<String> feliciaCurtains=new ArrayList<>();
        feliciaCurtains.add(Constants.CURTAINS_BASE_URL+"felicia/curtain_felicia_color_01.png");
        feliciaCurtains.add(Constants.CURTAINS_BASE_URL+"felicia/curtain_felicia_color_02.png");
        feliciaCurtains.add(Constants.CURTAINS_BASE_URL+"felicia/curtain_felicia_color_03.png");
        feliciaCurtains.add(Constants.CURTAINS_BASE_URL+"felicia/curtain_felicia_color_04.png");
        felicia.setCurtains(feliciaCurtains);


        ArrayList<FabricColor> irisColors=new ArrayList<>();
        irisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_iris_color_01.jpg", Constants.FABRICS_BASE_URL+"iris/fabric_iris_color_01.jpg","Color 11002/01","Χρώμα 11002/01"));
        irisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_iris_color_02.jpg", Constants.FABRICS_BASE_URL+"iris/fabric_iris_color_02.jpg","Color 11002/02","Χρώμα 11002/02"));
        irisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_iris_color_03.jpg", Constants.FABRICS_BASE_URL+"iris/fabric_iris_color_03.jpg","Color 11002/03","Χρώμα 11002/03"));
        irisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_iris_color_04.jpg", Constants.FABRICS_BASE_URL+"iris/fabric_iris_color_04.jpg","Color 11002/04","Χρώμα 11002/04"));
        irisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_iris_color_05.jpg", Constants.FABRICS_BASE_URL+"iris/fabric_iris_color_05.jpg","Color 11002/05","Χρώμα 11002/05"));

        Fabric iris=new Fabric(R.drawable.iris,"IRIS",irisColors);

        ArrayList<String> irisCurtains=new ArrayList<>();
        irisCurtains.add(Constants.CURTAINS_BASE_URL+"iris/curtain_iris_color_01.png");
        irisCurtains.add(Constants.CURTAINS_BASE_URL+"iris/curtain_iris_color_02.png");
        irisCurtains.add(Constants.CURTAINS_BASE_URL+"iris/curtain_iris_color_03.png");
        irisCurtains.add(Constants.CURTAINS_BASE_URL+"iris/curtain_iris_color_04.png");
        irisCurtains.add(Constants.CURTAINS_BASE_URL+"iris/curtain_iris_color_05.png");
        iris.setCurtains(irisCurtains);


        ArrayList<FabricColor> kassandraColors=new ArrayList<>();
        kassandraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kassandra_color_01.jpg", Constants.FABRICS_BASE_URL+"kassandra/fabric_kassandra_color_01.jpg","Color 11001/01","Χρώμα 11001/01"));
        kassandraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kassandra_color_02.jpg", Constants.FABRICS_BASE_URL+"kassandra/fabric_kassandra_color_02.jpg","Color 11001/02","Χρώμα 11001/02"));
        kassandraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kassandra_color_03.jpg", Constants.FABRICS_BASE_URL+"kassandra/fabric_kassandra_color_03.jpg","Color 11001/03","Χρώμα 11001/03"));
        kassandraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kassandra_color_04.jpg", Constants.FABRICS_BASE_URL+"kassandra/fabric_kassandra_color_04.jpg","Color 11001/04","Χρώμα 11001/04"));
        kassandraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kassandra_color_05.jpg", Constants.FABRICS_BASE_URL+"kassandra/fabric_kassandra_color_05.jpg","Color 11001/05","Χρώμα 11001/05"));

        Fabric kassandra=new Fabric(R.drawable.kassandra,"KASSANDRA",kassandraColors);

        ArrayList<String> kassandraCurtains=new ArrayList<>();
        kassandraCurtains.add(Constants.CURTAINS_BASE_URL+"kassandra/curtain_kassandra_color_01.png");
        kassandraCurtains.add(Constants.CURTAINS_BASE_URL+"kassandra/curtain_kassandra_color_02.png");
        kassandraCurtains.add(Constants.CURTAINS_BASE_URL+"kassandra/curtain_kassandra_color_03.png");
        kassandraCurtains.add(Constants.CURTAINS_BASE_URL+"kassandra/curtain_kassandra_color_04.png");
        kassandraCurtains.add(Constants.CURTAINS_BASE_URL+"kassandra/curtain_kassandra_color_05.png");
        kassandra.setCurtains(kassandraCurtains);


        ArrayList<FabricColor> lauraColors=new ArrayList<>();
        lauraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_laura_color_230011.jpg", Constants.FABRICS_BASE_URL+"laura/fabric_laura_color_230011.jpg","Color 23001/1","Χρώμα 23001/1"));
        lauraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_laura_color_230012.jpg", Constants.FABRICS_BASE_URL+"laura/fabric_laura_color_230012.jpg","Color 23001/2","Χρώμα 23001/2"));
        lauraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_laura_color_230021.jpg", Constants.FABRICS_BASE_URL+"laura/fabric_laura_color_230021.jpg","Color 23002/1","Χρώμα 23002/1"));
        lauraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_laura_color_230022.jpg", Constants.FABRICS_BASE_URL+"laura/fabric_laura_color_230022.jpg","Color 23002/2","Χρώμα 23002/2"));
        lauraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_laura_color_230031.jpg", Constants.FABRICS_BASE_URL+"laura/fabric_laura_color_230031.jpg","Color 23003/1","Χρώμα 23003/1"));
        lauraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_laura_color_230032.jpg", Constants.FABRICS_BASE_URL+"laura/fabric_laura_color_230032.jpg","Color 23003/2","Χρώμα 23003/2"));

        Fabric laura=new Fabric(R.drawable.laura,"LAURA",lauraColors);

        ArrayList<String> lauraCurtains=new ArrayList<>();
        lauraCurtains.add(Constants.CURTAINS_BASE_URL+"laura/curtain_laura_color_230011.png");
        lauraCurtains.add(Constants.CURTAINS_BASE_URL+"laura/curtain_laura_color_230012.png");
        lauraCurtains.add(Constants.CURTAINS_BASE_URL+"laura/curtain_laura_color_230021.png");
        lauraCurtains.add(Constants.CURTAINS_BASE_URL+"laura/curtain_laura_color_230022.png");
        lauraCurtains.add(Constants.CURTAINS_BASE_URL+"laura/curtain_laura_color_230031.png");
        lauraCurtains.add(Constants.CURTAINS_BASE_URL+"laura/curtain_laura_color_230032.png");
        laura.setCurtains(lauraCurtains);

        ArrayList<FabricColor> louisaColors=new ArrayList<>();
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_01.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_01.jpg","Color 11003/01","Χρώμα 11003/01"));
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_02.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_02.jpg","Color 11003/02","Χρώμα 11003/02"));
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_03.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_03.jpg","Color 11003/03","Χρώμα 11003/03"));
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_04.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_04.jpg","Color 11003/04","Χρώμα 11003/04"));
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_05.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_05.jpg","Color 11003/05","Χρώμα 11003/05"));
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_06.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_06.jpg","Color 11003/06","Χρώμα 11003/06"));
        louisaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_louisa_color_07.jpg", Constants.FABRICS_BASE_URL+"louisa/fabric_louisa_color_07.jpg","Color 11003/07","Χρώμα 11003/07"));

        Fabric louisa=new Fabric(R.drawable.louisa,"LOUISA",louisaColors);

        ArrayList<String> louisaCurtains=new ArrayList<>();
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_01.png");
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_02.png");
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_03.png");
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_04.png");
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_05.png");
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_06.png");
        louisaCurtains.add(Constants.CURTAINS_BASE_URL+"louisa/curtain_louisa_color_07.png");
        louisa.setCurtains(louisaCurtains);


        ArrayList<FabricColor> mayaColors=new ArrayList<>();
        mayaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_maya_color_01.jpg", Constants.FABRICS_BASE_URL+"maya/fabric_maya_color_01.jpg","Color 20006/01","Χρώμα 20006/01"));
        mayaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_maya_color_02.jpg", Constants.FABRICS_BASE_URL+"maya/fabric_maya_color_02.jpg","Color 20006/02","Χρώμα 20006/02"));
        mayaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_maya_color_03.jpg", Constants.FABRICS_BASE_URL+"maya/fabric_maya_color_03.jpg","Color 20006/03","Χρώμα 20006/03"));
        mayaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_maya_color_04.jpg", Constants.FABRICS_BASE_URL+"maya/fabric_maya_color_04.jpg","Color 20006/04","Χρώμα 20006/04"));
        mayaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_maya_color_05.jpg", Constants.FABRICS_BASE_URL+"maya/fabric_maya_color_05.jpg","Color 20006/05","Χρώμα 20006/05"));
        mayaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_maya_color_06.jpg", Constants.FABRICS_BASE_URL+"maya/fabric_maya_color_06.jpg","Color 20006/06","Χρώμα 20006/06"));

        Fabric maya=new Fabric(R.drawable.maya,"maya",mayaColors);

        ArrayList<String> mayaCurtains=new ArrayList<>();
        mayaCurtains.add(Constants.CURTAINS_BASE_URL+"maya/curtain_maya_color_01.png");
        mayaCurtains.add(Constants.CURTAINS_BASE_URL+"maya/curtain_maya_color_02.png");
        mayaCurtains.add(Constants.CURTAINS_BASE_URL+"maya/curtain_maya_color_03.png");
        mayaCurtains.add(Constants.CURTAINS_BASE_URL+"maya/curtain_maya_color_04.png");
        mayaCurtains.add(Constants.CURTAINS_BASE_URL+"maya/curtain_maya_color_05.png");
        mayaCurtains.add(Constants.CURTAINS_BASE_URL+"maya/curtain_maya_color_06.png");
        maya.setCurtains(mayaCurtains);

        ArrayList<FabricColor> melinaColors=new ArrayList<>();
        melinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melina_color_01.jpg", Constants.FABRICS_BASE_URL+"melina/fabric_melina_color_01.jpg","Color 13006/01","Χρώμα 13006/01"));
        melinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melina_color_02.jpg", Constants.FABRICS_BASE_URL+"melina/fabric_melina_color_02.jpg","Color 13006/02","Χρώμα 13006/02"));
        melinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melina_color_03.jpg", Constants.FABRICS_BASE_URL+"melina/fabric_melina_color_03.jpg","Color 13006/03","Χρώμα 13006/03"));

        Fabric melina=new Fabric(R.drawable.melina,"MELINA",melinaColors);

        ArrayList<String> melinaCurtains=new ArrayList<>();
        melinaCurtains.add(Constants.CURTAINS_BASE_URL+"melina/curtain_melina_color_01.png");
        melinaCurtains.add(Constants.CURTAINS_BASE_URL+"melina/curtain_melina_color_02.png");
        melinaCurtains.add(Constants.CURTAINS_BASE_URL+"melina/curtain_melina_color_03.png");
        melina.setCurtains(melinaCurtains);

        ArrayList<FabricColor> patriciaColors=new ArrayList<>();
        patriciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_patricia_color_01.jpg", Constants.FABRICS_BASE_URL+"patricia/fabric_patricia_color_01.jpg","Color 12005/01","Χρώμα 12005/01"));
        patriciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_patricia_color_02.jpg", Constants.FABRICS_BASE_URL+"patricia/fabric_patricia_color_02.jpg","Color 12005/02","Χρώμα 12005/02"));
        patriciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_patricia_color_03.jpg", Constants.FABRICS_BASE_URL+"patricia/fabric_patricia_color_03.jpg","Color 12005/03","Χρώμα 12005/03"));
        patriciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_patricia_color_04.jpg", Constants.FABRICS_BASE_URL+"patricia/fabric_patricia_color_04.jpg","Color 12005/04","Χρώμα 12005/04"));
        patriciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_patricia_color_05.jpg", Constants.FABRICS_BASE_URL+"patricia/fabric_patricia_color_05.jpg","Color 12005/05","Χρώμα 12005/05"));

        Fabric patricia=new Fabric(R.drawable.patricia,"PATRICIA",patriciaColors);

        ArrayList<String> patriciaCurtains=new ArrayList<>();
        patriciaCurtains.add(Constants.CURTAINS_BASE_URL+"patricia/curtain_patricia_color_01.png");
        patriciaCurtains.add(Constants.CURTAINS_BASE_URL+"patricia/curtain_patricia_color_02.png");
        patriciaCurtains.add(Constants.CURTAINS_BASE_URL+"patricia/curtain_patricia_color_03.png");
        patriciaCurtains.add(Constants.CURTAINS_BASE_URL+"patricia/curtain_patricia_color_04.png");
        patriciaCurtains.add(Constants.CURTAINS_BASE_URL+"patricia/curtain_patricia_color_05.png");
        patricia.setCurtains(patriciaCurtains);

        ArrayList<FabricColor> valentinaColors=new ArrayList<>();
        valentinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_valentina_color_01.jpg", Constants.FABRICS_BASE_URL+"valentina/fabric_valentina_color_01.jpg","Color 12006/01","Χρώμα 12006/01"));
        valentinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_valentina_color_02.jpg", Constants.FABRICS_BASE_URL+"valentina/fabric_valentina_color_02.jpg","Color 12006/02","Χρώμα 12006/02"));
        valentinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_valentina_color_03.jpg", Constants.FABRICS_BASE_URL+"valentina/fabric_valentina_color_03.jpg","Color 12006/03","Χρώμα 12006/03"));
        valentinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_valentina_color_04.jpg", Constants.FABRICS_BASE_URL+"valentina/fabric_valentina_color_04.jpg","Color 12006/04","Χρώμα 12006/04"));
        valentinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_valentina_color_05.jpg", Constants.FABRICS_BASE_URL+"valentina/fabric_valentina_color_05.jpg","Color 12006/05","Χρώμα 12006/05"));

        Fabric valentina=new Fabric(R.drawable.valentina,"VALENTINA",valentinaColors);

        ArrayList<String> valentinaCurtains=new ArrayList<>();
        valentinaCurtains.add(Constants.CURTAINS_BASE_URL+"valentina/curtain_valentina_color_01.png");
        valentinaCurtains.add(Constants.CURTAINS_BASE_URL+"valentina/curtain_valentina_color_02.png");
        valentinaCurtains.add(Constants.CURTAINS_BASE_URL+"valentina/curtain_valentina_color_03.png");
        valentinaCurtains.add(Constants.CURTAINS_BASE_URL+"valentina/curtain_valentina_color_04.png");
        valentinaCurtains.add(Constants.CURTAINS_BASE_URL+"valentina/curtain_valentina_color_05.png");
        valentina.setCurtains(valentinaCurtains);


        ArrayList<FabricColor> victoriaColors=new ArrayList<>();
        victoriaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_victoria_color_01.jpg", Constants.FABRICS_BASE_URL+"victoria/fabric_victoria_color_01.jpg","Color 12007/01","Χρώμα 12007/01"));
        victoriaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_victoria_color_02.jpg", Constants.FABRICS_BASE_URL+"victoria/fabric_victoria_color_02.jpg","Color 12007/02","Χρώμα 12007/02"));
        victoriaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_victoria_color_03.jpg", Constants.FABRICS_BASE_URL+"victoria/fabric_victoria_color_03.jpg","Color 12007/03","Χρώμα 12007/03"));
        victoriaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_victoria_color_04.jpg", Constants.FABRICS_BASE_URL+"victoria/fabric_victoria_color_04.jpg","Color 12007/04","Χρώμα 12007/04"));

        Fabric victoria=new Fabric(R.drawable.victoria,"VICTORIA",victoriaColors);

        ArrayList<String> victoriaCurtains=new ArrayList<>();
        victoriaCurtains.add(Constants.CURTAINS_BASE_URL+"victoria/curtain_victoria_color_01.png");
        victoriaCurtains.add(Constants.CURTAINS_BASE_URL+"victoria/curtain_victoria_color_02.png");
        victoriaCurtains.add(Constants.CURTAINS_BASE_URL+"victoria/curtain_victoria_color_03.png");
        victoriaCurtains.add(Constants.CURTAINS_BASE_URL+"victoria/curtain_victoria_color_04.png");
        victoria.setCurtains(victoriaCurtains);

        ArrayList<FabricColor> vivianColors=new ArrayList<>();
        vivianColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vivian_color_01.jpg", Constants.FABRICS_BASE_URL+"vivian/fabric_vivian_color_01.jpg","Color 15001/01","Χρώμα 15001/01"));
        vivianColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vivian_color_02.jpg", Constants.FABRICS_BASE_URL+"vivian/fabric_vivian_color_02.jpg","Color 15001/02","Χρώμα 15001/02"));
        vivianColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_vivian_color_03.jpg", Constants.FABRICS_BASE_URL+"vivian/fabric_vivian_color_03.jpg","Color 15001/03","Χρώμα 15001/03"));

        Fabric vivian=new Fabric(R.drawable.vivian,"VIVIAN",vivianColors);

        ArrayList<String> vivianCurtains=new ArrayList<>();
        vivianCurtains.add(Constants.CURTAINS_BASE_URL+"vivian/curtain_vivian_color_01.png");
        vivianCurtains.add(Constants.CURTAINS_BASE_URL+"vivian/curtain_vivian_color_02.png");
        vivianCurtains.add(Constants.CURTAINS_BASE_URL+"vivian/curtain_vivian_color_03.png");
        vivian.setCurtains(vivianCurtains);

        ArrayList<FabricColor> andriannaColors=new ArrayList<>();
        andriannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_andrianna_color_01.jpg", Constants.FABRICS_BASE_URL+"andrianna/fabric_andrianna_color_01.jpg","Color 22002/01","Χρώμα 22002/01"));
        andriannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_andrianna_color_02.jpg", Constants.FABRICS_BASE_URL+"andrianna/fabric_andrianna_color_02.jpg","Color 22002/02","Χρώμα 22002/02"));
        andriannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_andrianna_color_03.jpg", Constants.FABRICS_BASE_URL+"andrianna/fabric_andrianna_color_03.jpg","Color 22002/03","Χρώμα 22002/03"));
        andriannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_andrianna_color_04.jpg", Constants.FABRICS_BASE_URL+"andrianna/fabric_andrianna_color_04.jpg","Color 22002/04","Χρώμα 22002/04"));

        Fabric andrianna=new Fabric(R.drawable.andrianna,"ANDRIANNA",andriannaColors);

        ArrayList<String> andriannaCurtains=new ArrayList<>();
        andriannaCurtains.add(Constants.CURTAINS_BASE_URL+"andrianna/curtain_andrianna_color_01.png");
        andriannaCurtains.add(Constants.CURTAINS_BASE_URL+"andrianna/curtain_andrianna_color_02.png");
        andriannaCurtains.add(Constants.CURTAINS_BASE_URL+"andrianna/curtain_andrianna_color_03.png");
        andriannaCurtains.add(Constants.CURTAINS_BASE_URL+"andrianna/curtain_andrianna_color_04.png");
        andrianna.setCurtains(andriannaCurtains);

        ArrayList<FabricColor> beatriceColors=new ArrayList<>();
        beatriceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_beatrice_color_01.jpg", Constants.FABRICS_BASE_URL+"beatrice/fabric_beatrice_color_01.jpg","Color 22005/01","Χρώμα 22005/01"));
        beatriceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_beatrice_color_02.jpg", Constants.FABRICS_BASE_URL+"beatrice/fabric_beatrice_color_02.jpg","Color 22005/02","Χρώμα 22005/02"));
        beatriceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_beatrice_color_03.jpg", Constants.FABRICS_BASE_URL+"beatrice/fabric_beatrice_color_03.jpg","Color 22005/03","Χρώμα 22005/03"));
        beatriceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_beatrice_color_04.jpg", Constants.FABRICS_BASE_URL+"beatrice/fabric_beatrice_color_04.jpg","Color 22005/04","Χρώμα 22005/04"));

        Fabric beatrice=new Fabric(R.drawable.beatrice,"BEATRICE",beatriceColors);

        ArrayList<String> beatriceCurtains=new ArrayList<>();
        beatriceCurtains.add(Constants.CURTAINS_BASE_URL+"beatrice/curtain_beatrice_color_01.png");
        beatriceCurtains.add(Constants.CURTAINS_BASE_URL+"beatrice/curtain_beatrice_color_02.png");
        beatriceCurtains.add(Constants.CURTAINS_BASE_URL+"beatrice/curtain_beatrice_color_03.png");
        beatriceCurtains.add(Constants.CURTAINS_BASE_URL+"beatrice/curtain_beatrice_color_04.png");
        beatrice.setCurtains(beatriceCurtains);


        ArrayList<FabricColor> daliaColors=new ArrayList<>();
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270011.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270011.jpg","Color 27001/1","Χρώμα 27001/1"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270012.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270012.jpg","Color 27001/2","Χρώμα 27001/2"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270021.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270021.jpg","Color 27002/1","Χρώμα 27002/1"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270022.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270022.jpg","Color 27002/2","Χρώμα 27002/2"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270031.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270031.jpg","Color 27003/1","Χρώμα 27003/1"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270032.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270032.jpg","Color 27003/2","Χρώμα 27003/2"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270041.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270041.jpg","Color 27004/1","Χρώμα 27004/1"));
        daliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dalia_color_270042.jpg", Constants.FABRICS_BASE_URL+"dalia/fabric_dalia_color_270042.jpg","Color 27004/2","Χρώμα 27004/2"));

        Fabric dalia=new Fabric(R.drawable.dalia,"DALIA",daliaColors);

        ArrayList<String> daliaCurtains=new ArrayList<>();
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270011.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270012.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270021.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270022.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270031.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270032.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270041.png");
        daliaCurtains.add(Constants.CURTAINS_BASE_URL+"dalia/curtain_dalia_color_270042.png");
        dalia.setCurtains(daliaCurtains);

        ArrayList<FabricColor> franceskaColors=new ArrayList<>();
        franceskaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_franceska_color_01.jpg", Constants.FABRICS_BASE_URL+"franceska/fabric_franceska_color_01.jpg","Color 22003/01","Χρώμα 22003/01"));
        franceskaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_franceska_color_02.jpg", Constants.FABRICS_BASE_URL+"franceska/fabric_franceska_color_02.jpg","Color 22003/02","Χρώμα 22003/02"));
        franceskaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_franceska_color_03.jpg", Constants.FABRICS_BASE_URL+"franceska/fabric_franceska_color_03.jpg","Color 22003/03","Χρώμα 22003/03"));
        franceskaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_franceska_color_04.jpg", Constants.FABRICS_BASE_URL+"franceska/fabric_franceska_color_04.jpg","Color 22003/04","Χρώμα 22003/04"));

        Fabric franceska=new Fabric(R.drawable.franceska,"FRANCESKA",franceskaColors);

        ArrayList<String> franceskaCurtains=new ArrayList<>();
        franceskaCurtains.add(Constants.CURTAINS_BASE_URL+"franceska/curtain_franceska_color_01.png");
        franceskaCurtains.add(Constants.CURTAINS_BASE_URL+"franceska/curtain_franceska_color_02.png");
        franceskaCurtains.add(Constants.CURTAINS_BASE_URL+"franceska/curtain_franceska_color_03.png");
        franceskaCurtains.add(Constants.CURTAINS_BASE_URL+"franceska/curtain_franceska_color_04.png");
        franceska.setCurtains(franceskaCurtains);

        ArrayList<FabricColor> inkasColors=new ArrayList<>();
        inkasColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_inkas_color_01.jpg", Constants.FABRICS_BASE_URL+"inkas/fabric_inkas_color_01.jpg","Color 13010/01","Χρώμα 13010/01"));
        inkasColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_inkas_color_02.jpg", Constants.FABRICS_BASE_URL+"inkas/fabric_inkas_color_02.jpg","Color 13010/02","Χρώμα 13010/02"));
        inkasColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_inkas_color_03.jpg", Constants.FABRICS_BASE_URL+"inkas/fabric_inkas_color_03.jpg","Color 13010/03","Χρώμα 13010/03"));
        inkasColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_inkas_color_04.jpg", Constants.FABRICS_BASE_URL+"inkas/fabric_inkas_color_04.jpg","Color 13010/04","Χρώμα 13010/04"));

        Fabric inkas=new Fabric(R.drawable.inkas,"INCAS",inkasColors);

        ArrayList<String> inkasCurtains=new ArrayList<>();
        inkasCurtains.add(Constants.CURTAINS_BASE_URL+"inkas/curtain_inkas_color_01.png");
        inkasCurtains.add(Constants.CURTAINS_BASE_URL+"inkas/curtain_inkas_color_02.png");
        inkasCurtains.add(Constants.CURTAINS_BASE_URL+"inkas/curtain_inkas_color_03.png");
        inkasCurtains.add(Constants.CURTAINS_BASE_URL+"inkas/curtain_inkas_color_04.png");
        inkas.setCurtains(inkasCurtains);

        ArrayList<FabricColor> mariettaColors=new ArrayList<>();
        mariettaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_marietta_color_01.jpg", Constants.FABRICS_BASE_URL+"marietta/fabric_marietta_color_01.jpg","Color 22006/01","Χρώμα 22006/01"));
        mariettaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_marietta_color_02.jpg", Constants.FABRICS_BASE_URL+"marietta/fabric_marietta_color_02.jpg","Color 22006/02","Χρώμα 22006/02"));
        mariettaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_marietta_color_03.jpg", Constants.FABRICS_BASE_URL+"marietta/fabric_marietta_color_03.jpg","Color 22006/03","Χρώμα 22006/03"));
        mariettaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_marietta_color_04.jpg", Constants.FABRICS_BASE_URL+"marietta/fabric_marietta_color_04.jpg","Color 22006/04","Χρώμα 22006/04"));
        mariettaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_marietta_color_05.jpg", Constants.FABRICS_BASE_URL+"marietta/fabric_marietta_color_05.jpg","Color 22006/05","Χρώμα 22006/05"));

        Fabric marietta=new Fabric(R.drawable.marietta,"MARIETTA",mariettaColors);

        ArrayList<String> mariettaCurtains=new ArrayList<>();
        mariettaCurtains.add(Constants.CURTAINS_BASE_URL+"marietta/curtain_marietta_color_01.png");
        mariettaCurtains.add(Constants.CURTAINS_BASE_URL+"marietta/curtain_marietta_color_02.png");
        mariettaCurtains.add(Constants.CURTAINS_BASE_URL+"marietta/curtain_marietta_color_03.png");
        mariettaCurtains.add(Constants.CURTAINS_BASE_URL+"marietta/curtain_marietta_color_04.png");
        mariettaCurtains.add(Constants.CURTAINS_BASE_URL+"marietta/curtain_marietta_color_05.png");

        marietta.setCurtains(mariettaCurtains);

        ArrayList<FabricColor> nefeliColors=new ArrayList<>();
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_01.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_01.jpg","Color 15005/01","Χρώμα 15005/01"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_02.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_02.jpg","Color 15005/02","Χρώμα 15005/02"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_03.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_03.jpg","Color 15005/03","Χρώμα 15005/03"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_04.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_04.jpg","Color 15005/04","Χρώμα 15005/04"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_05.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_05.jpg","Color 15005/05","Χρώμα 15005/05"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_06.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_06.jpg","Color 15005/06","Χρώμα 15005/06"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_07.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_07.jpg","Color 15005/07","Χρώμα 15005/07"));
        nefeliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nefeli_color_08.jpg", Constants.FABRICS_BASE_URL+"nefeli/fabric_nefeli_color_08.jpg","Color 15005/08","Χρώμα 15005/08"));

        Fabric nefeli=new Fabric(R.drawable.nefeli,"NEFELI",nefeliColors);

        ArrayList<String> nefeliCurtains=new ArrayList<>();
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_01.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_02.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_03.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_04.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_05.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_06.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_07.png");
        nefeliCurtains.add(Constants.CURTAINS_BASE_URL+"nefeli/curtain_nefeli_color_08.png");
        nefeli.setCurtains(nefeliCurtains);

        ArrayList<FabricColor> roxanneColors=new ArrayList<>();
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_01.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_01.jpg","Color 11011/01","Χρώμα 11011/01"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_02.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_02.jpg","Color 11011/02","Χρώμα 11011/02"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_03.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_03.jpg","Color 11011/03","Χρώμα 11011/03"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_04.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_04.jpg","Color 11011/04","Χρώμα 11011/04"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_05.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_05.jpg","Color 11011/05","Χρώμα 11011/05"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_06.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_06.jpg","Color 11011/06","Χρώμα 11011/06"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_07.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_07.jpg","Color 11011/07","Χρώμα 11011/07"));
        roxanneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_roxanne_color_08.jpg", Constants.FABRICS_BASE_URL+"roxanne/fabric_roxanne_color_08.jpg","Color 11011/08","Χρώμα 11011/08"));

        Fabric roxanne=new Fabric(R.drawable.roxanne,"ROXANNE",roxanneColors);

        ArrayList<String> roxanneCurtains=new ArrayList<>();
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_01.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_02.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_03.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_04.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_05.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_06.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_07.png");
        roxanneCurtains.add(Constants.CURTAINS_BASE_URL+"roxanne/curtain_roxanne_color_08.png");
        roxanne.setCurtains(roxanneCurtains);


        //latest curtains

        ArrayList<FabricColor> adeleColors=new ArrayList<>();
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_01.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_01.jpg","Color 12013/01","Χρώμα 12013/01"));
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_02.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_02.jpg","Color 12013/02","Χρώμα 12013/02"));
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_03.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_03.jpg","Color 12013/03","Χρώμα 12013/03"));
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_04.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_04.jpg","Color 12013/04","Χρώμα 12013/04"));
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_05.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_05.jpg","Color 12013/05","Χρώμα 12013/05"));
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_06.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_06.jpg","Color 12013/06","Χρώμα 12013/06"));
        adeleColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_adele_color_07.jpg", Constants.FABRICS_BASE_URL+"adele/fabric_adele_color_07.jpg","Color 12013/07","Χρώμα 12013/07"));

        Fabric adele=new Fabric(R.drawable.adele,"ADELE",adeleColors);

        ArrayList<String> adeleCurtains=new ArrayList<>();
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_01.png");
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_02.png");
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_03.png");
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_04.png");
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_05.png");
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_06.png");
        adeleCurtains.add(Constants.CURTAINS_BASE_URL+"adele/curtain_adele_color_07.png");
        adele.setCurtains(adeleCurtains);

        ArrayList<FabricColor> alkistisColors=new ArrayList<>();
        alkistisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alkistis_color_01.jpg", Constants.FABRICS_BASE_URL+"alkistis/fabric_alkistis_color_01.jpg","Color 11013/01","Χρώμα 11013/01"));
        alkistisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alkistis_color_02.jpg", Constants.FABRICS_BASE_URL+"alkistis/fabric_alkistis_color_02.jpg","Color 11013/02","Χρώμα 11013/02"));
        alkistisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alkistis_color_03.jpg", Constants.FABRICS_BASE_URL+"alkistis/fabric_alkistis_color_03.jpg","Color 11013/03","Χρώμα 11013/03"));
        alkistisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alkistis_color_04.jpg", Constants.FABRICS_BASE_URL+"alkistis/fabric_alkistis_color_04.jpg","Color 11013/04","Χρώμα 11013/04"));
        alkistisColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alkistis_color_05.jpg", Constants.FABRICS_BASE_URL+"alkistis/fabric_alkistis_color_05.jpg","Color 11013/05","Χρώμα 11013/05"));

        Fabric alkistis=new Fabric(R.drawable.alkistis,"ALKISTIS",alkistisColors);

        ArrayList<String> alkistisCurtains=new ArrayList<>();
        alkistisCurtains.add(Constants.CURTAINS_BASE_URL+"alkistis/curtain_alkistis_color_01.png");
        alkistisCurtains.add(Constants.CURTAINS_BASE_URL+"alkistis/curtain_alkistis_color_02.png");
        alkistisCurtains.add(Constants.CURTAINS_BASE_URL+"alkistis/curtain_alkistis_color_03.png");
        alkistisCurtains.add(Constants.CURTAINS_BASE_URL+"alkistis/curtain_alkistis_color_04.png");
        alkistisCurtains.add(Constants.CURTAINS_BASE_URL+"alkistis/curtain_alkistis_color_05.png");

        alkistis.setCurtains(alkistisCurtains);


        ArrayList<FabricColor> ariadniColors=new ArrayList<>();
        ariadniColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariadni_color_01.jpg", Constants.FABRICS_BASE_URL+"ariadni/fabric_ariadni_color_01.jpg","Color 26001/01","Χρώμα 26001/01"));
        ariadniColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariadni_color_02.jpg", Constants.FABRICS_BASE_URL+"ariadni/fabric_ariadni_color_02.jpg","Color 26001/02","Χρώμα 26001/02"));
        ariadniColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariadni_color_03.jpg", Constants.FABRICS_BASE_URL+"ariadni/fabric_ariadni_color_03.jpg","Color 26001/03","Χρώμα 26001/03"));

        Fabric ariadni=new Fabric(R.drawable.ariadni,"Ariadni",ariadniColors);

        ArrayList<String> ariadniCurtains=new ArrayList<>();
        ariadniCurtains.add(Constants.CURTAINS_BASE_URL+"ariadni/curtain_ariadni_color_01.png");
        ariadniCurtains.add(Constants.CURTAINS_BASE_URL+"ariadni/curtain_ariadni_color_02.png");
        ariadniCurtains.add(Constants.CURTAINS_BASE_URL+"ariadni/curtain_ariadni_color_03.png");

        ariadni.setCurtains(ariadniCurtains);


        ArrayList<FabricColor> arielColors=new ArrayList<>();
        arielColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariel_color_01.jpg", Constants.FABRICS_BASE_URL+"ariel/fabric_ariel_color_01.jpg","Color 12010/01","Χρώμα 12010/01"));
        arielColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariel_color_02.jpg", Constants.FABRICS_BASE_URL+"ariel/fabric_ariel_color_02.jpg","Color 12010/02","Χρώμα 12010/02"));
        arielColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariel_color_03.jpg", Constants.FABRICS_BASE_URL+"ariel/fabric_ariel_color_03.jpg","Color 12010/03","Χρώμα 12010/03"));
        arielColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariel_color_04.jpg", Constants.FABRICS_BASE_URL+"ariel/fabric_ariel_color_04.jpg","Color 12010/04","Χρώμα 12010/04"));
        arielColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariel_color_05.jpg", Constants.FABRICS_BASE_URL+"ariel/fabric_ariel_color_05.jpg","Color 12010/05","Χρώμα 12010/05"));
        arielColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ariel_color_06.jpg", Constants.FABRICS_BASE_URL+"ariel/fabric_ariel_color_06.jpg","Color 12010/06","Χρώμα 12010/06"));

        Fabric ariel=new Fabric(R.drawable.ariel,"Ariel",arielColors);

        ArrayList<String> arielCurtains=new ArrayList<>();
        arielCurtains.add(Constants.CURTAINS_BASE_URL+"ariel/curtain_ariel_color_01.png");
        arielCurtains.add(Constants.CURTAINS_BASE_URL+"ariel/curtain_ariel_color_02.png");
        arielCurtains.add(Constants.CURTAINS_BASE_URL+"ariel/curtain_ariel_color_03.png");
        arielCurtains.add(Constants.CURTAINS_BASE_URL+"ariel/curtain_ariel_color_04.png");
        arielCurtains.add(Constants.CURTAINS_BASE_URL+"ariel/curtain_ariel_color_05.png");
        arielCurtains.add(Constants.CURTAINS_BASE_URL+"ariel/curtain_ariel_color_06.png");
        ariel.setCurtains(arielCurtains);

        ArrayList<FabricColor> claudiaColors=new ArrayList<>();
        claudiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_claudia_color_01.jpg", Constants.FABRICS_BASE_URL+"claudia/fabric_claudia_color_01.jpg","Color 11012/01","Χρώμα 11012/01"));
        claudiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_claudia_color_02.jpg", Constants.FABRICS_BASE_URL+"claudia/fabric_claudia_color_02.jpg","Color 11012/02","Χρώμα 11012/02"));
        claudiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_claudia_color_03.jpg", Constants.FABRICS_BASE_URL+"claudia/fabric_claudia_color_03.jpg","Color 11012/03","Χρώμα 11012/03"));
        claudiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_claudia_color_04.jpg", Constants.FABRICS_BASE_URL+"claudia/fabric_claudia_color_04.jpg","Color 11012/04","Χρώμα 11012/04"));
        claudiaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_claudia_color_05.jpg", Constants.FABRICS_BASE_URL+"claudia/fabric_claudia_color_05.jpg","Color 11012/05","Χρώμα 11012/05"));

        Fabric claudia=new Fabric(R.drawable.claudia,"Claudia",claudiaColors);

        ArrayList<String> claudiaCurtains=new ArrayList<>();
        claudiaCurtains.add(Constants.CURTAINS_BASE_URL+"claudia/curtain_claudia_color_01.png");
        claudiaCurtains.add(Constants.CURTAINS_BASE_URL+"claudia/curtain_claudia_color_02.png");
        claudiaCurtains.add(Constants.CURTAINS_BASE_URL+"claudia/curtain_claudia_color_03.png");
        claudiaCurtains.add(Constants.CURTAINS_BASE_URL+"claudia/curtain_claudia_color_04.png");
        claudiaCurtains.add(Constants.CURTAINS_BASE_URL+"claudia/curtain_claudia_color_05.png");
        claudia.setCurtains(claudiaCurtains);

        ArrayList<FabricColor> gabrielaColors=new ArrayList<>();
        gabrielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gabriela_color_01.jpg", Constants.FABRICS_BASE_URL+"gabriela/fabric_gabriela_color_01.jpg","Color 16006/01","Χρώμα 16006/01"));
        gabrielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gabriela_color_02.jpg", Constants.FABRICS_BASE_URL+"gabriela/fabric_gabriela_color_02.jpg","Color 16006/02","Χρώμα 16006/02"));
        gabrielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gabriela_color_03.jpg", Constants.FABRICS_BASE_URL+"gabriela/fabric_gabriela_color_03.jpg","Color 16006/03","Χρώμα 16006/03"));
        gabrielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gabriela_color_04.jpg", Constants.FABRICS_BASE_URL+"gabriela/fabric_gabriela_color_04.jpg","Color 16006/04","Χρώμα 16006/04"));
        gabrielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gabriela_color_05.jpg", Constants.FABRICS_BASE_URL+"gabriela/fabric_gabriela_color_05.jpg","Color 16006/05","Χρώμα 16006/05"));
        gabrielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_gabriela_color_06.jpg", Constants.FABRICS_BASE_URL+"gabriela/fabric_gabriela_color_06.jpg","Color 16006/06","Χρώμα 16006/06"));

        Fabric gabriela=new Fabric(R.drawable.gabriela,"Gabriela",gabrielaColors);

        ArrayList<String> gabrielaCurtains=new ArrayList<>();
        gabrielaCurtains.add(Constants.CURTAINS_BASE_URL+"gabriela/curtain_gabriela_color_01.png");
        gabrielaCurtains.add(Constants.CURTAINS_BASE_URL+"gabriela/curtain_gabriela_color_02.png");
        gabrielaCurtains.add(Constants.CURTAINS_BASE_URL+"gabriela/curtain_gabriela_color_03.png");
        gabrielaCurtains.add(Constants.CURTAINS_BASE_URL+"gabriela/curtain_gabriela_color_04.png");
        gabrielaCurtains.add(Constants.CURTAINS_BASE_URL+"gabriela/curtain_gabriela_color_05.png");
        gabrielaCurtains.add(Constants.CURTAINS_BASE_URL+"gabriela/curtain_gabriela_color_06.png");
        gabriela.setCurtains(gabrielaCurtains);

        ArrayList<FabricColor> giovannaColors=new ArrayList<>();
        giovannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_giovanna_color_01.jpg", Constants.FABRICS_BASE_URL+"giovanna/fabric_giovanna_color_01.jpg","Color 12011/01","Χρώμα 12011/01"));
        giovannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_giovanna_color_02.jpg", Constants.FABRICS_BASE_URL+"giovanna/fabric_giovanna_color_02.jpg","Color 12011/02","Χρώμα 12011/02"));
        giovannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_giovanna_color_03.jpg", Constants.FABRICS_BASE_URL+"giovanna/fabric_giovanna_color_03.jpg","Color 12011/03","Χρώμα 12011/03"));
        giovannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_giovanna_color_04.jpg", Constants.FABRICS_BASE_URL+"giovanna/fabric_giovanna_color_04.jpg","Color 12011/04","Χρώμα 12011/04"));


        Fabric giovanna=new Fabric(R.drawable.giovanna,"Giovanna",giovannaColors);

        ArrayList<String> giovannaCurtains=new ArrayList<>();
        giovannaCurtains.add(Constants.CURTAINS_BASE_URL+"giovanna/curtain_giovanna_color_01.png");
        giovannaCurtains.add(Constants.CURTAINS_BASE_URL+"giovanna/curtain_giovanna_color_02.png");
        giovannaCurtains.add(Constants.CURTAINS_BASE_URL+"giovanna/curtain_giovanna_color_03.png");
        giovannaCurtains.add(Constants.CURTAINS_BASE_URL+"giovanna/curtain_giovanna_color_04.png");
        giovanna.setCurtains(giovannaCurtains);

        ArrayList<FabricColor> matildaColors=new ArrayList<>();
        matildaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_matilda_color_01.jpg", Constants.FABRICS_BASE_URL+"matilda/fabric_matilda_color_01.jpg","Color 11009/01","Χρώμα 11009/01"));
        matildaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_matilda_color_02.jpg", Constants.FABRICS_BASE_URL+"matilda/fabric_matilda_color_02.jpg","Color 11009/02","Χρώμα 11009/02"));
        matildaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_matilda_color_03.jpg", Constants.FABRICS_BASE_URL+"matilda/fabric_matilda_color_03.jpg","Color 11009/03","Χρώμα 11009/03"));
        matildaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_matilda_color_04.jpg", Constants.FABRICS_BASE_URL+"matilda/fabric_matilda_color_04.jpg","Color 11009/04","Χρώμα 11009/04"));
        matildaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_matilda_color_05.jpg", Constants.FABRICS_BASE_URL+"matilda/fabric_matilda_color_05.jpg","Color 11009/05","Χρώμα 11009/05"));


        Fabric matilda=new Fabric(R.drawable.matilda,"Matilda",matildaColors);

        ArrayList<String> matildaCurtains=new ArrayList<>();
        matildaCurtains.add(Constants.CURTAINS_BASE_URL+"matilda/curtain_matilda_color_01.png");
        matildaCurtains.add(Constants.CURTAINS_BASE_URL+"matilda/curtain_matilda_color_02.png");
        matildaCurtains.add(Constants.CURTAINS_BASE_URL+"matilda/curtain_matilda_color_03.png");
        matildaCurtains.add(Constants.CURTAINS_BASE_URL+"matilda/curtain_matilda_color_04.png");
        matildaCurtains.add(Constants.CURTAINS_BASE_URL+"matilda/curtain_matilda_color_05.png");

        matilda.setCurtains(matildaCurtains);

        ArrayList<FabricColor> ornelaColors=new ArrayList<>();
        ornelaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ornela_color_01.jpg", Constants.FABRICS_BASE_URL+"ornela/fabric_ornela_color_01.jpg","Color 11010/01","Χρώμα 11010/01"));
        ornelaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ornela_color_02.jpg", Constants.FABRICS_BASE_URL+"ornela/fabric_ornela_color_02.jpg","Color 11010/02","Χρώμα 11010/02"));
        ornelaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ornela_color_03.jpg", Constants.FABRICS_BASE_URL+"ornela/fabric_ornela_color_03.jpg","Color 11010/03","Χρώμα 11010/03"));
        ornelaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ornela_color_04.jpg", Constants.FABRICS_BASE_URL+"ornela/fabric_ornela_color_04.jpg","Color 11010/04","Χρώμα 11010/04"));
        ornelaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_ornela_color_05.jpg", Constants.FABRICS_BASE_URL+"ornela/fabric_ornela_color_05.jpg","Color 11010/05","Χρώμα 11010/05"));

        Fabric ornela=new Fabric(R.drawable.ornela,"Ornela",ornelaColors);

        ArrayList<String> ornelaCurtains=new ArrayList<>();
        ornelaCurtains.add(Constants.CURTAINS_BASE_URL+"ornela/curtain_ornela_color_01.png");
        ornelaCurtains.add(Constants.CURTAINS_BASE_URL+"ornela/curtain_ornela_color_02.png");
        ornelaCurtains.add(Constants.CURTAINS_BASE_URL+"ornela/curtain_ornela_color_03.png");
        ornelaCurtains.add(Constants.CURTAINS_BASE_URL+"ornela/curtain_ornela_color_04.png");
        ornelaCurtains.add(Constants.CURTAINS_BASE_URL+"ornela/curtain_ornela_color_05.png");
        ornela.setCurtains(ornelaCurtains);

        ArrayList<FabricColor> sandyColors=new ArrayList<>();
        sandyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sandy_color_01.jpg", Constants.FABRICS_BASE_URL+"sandy/fabric_sandy_color_01.jpg","Color 16002/01","Χρώμα 16002/01"));
        sandyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sandy_color_02.jpg", Constants.FABRICS_BASE_URL+"sandy/fabric_sandy_color_02.jpg","Color 16002/02","Χρώμα 16002/02"));
        sandyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_sandy_color_03.jpg", Constants.FABRICS_BASE_URL+"sandy/fabric_sandy_color_03.jpg","Color 16002/03","Χρώμα 16002/03"));

        Fabric sandy=new Fabric(R.drawable.sandy,"Sandy",sandyColors);

        ArrayList<String> sandyCurtains=new ArrayList<>();
        sandyCurtains.add(Constants.CURTAINS_BASE_URL+"sandy/curtain_sandy_color_01.png");
        sandyCurtains.add(Constants.CURTAINS_BASE_URL+"sandy/curtain_sandy_color_02.png");
        sandyCurtains.add(Constants.CURTAINS_BASE_URL+"sandy/curtain_sandy_color_03.png");
        sandy.setCurtains(sandyCurtains);

        ArrayList<FabricColor> aliceColors=new ArrayList<>();
        aliceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alice_color_01.jpg", Constants.FABRICS_BASE_URL+"alice/fabric_alice_color_01.jpg","Color 11005/01","Χρώμα 11005/01"));
        aliceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alice_color_02.jpg", Constants.FABRICS_BASE_URL+"alice/fabric_alice_color_02.jpg","Color 11005/02","Χρώμα 11005/02"));
        aliceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alice_color_03.jpg", Constants.FABRICS_BASE_URL+"alice/fabric_alice_color_03.jpg","Color 11005/03","Χρώμα 11005/03"));
        aliceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alice_color_04.jpg", Constants.FABRICS_BASE_URL+"alice/fabric_alice_color_04.jpg","Color 11005/04","Χρώμα 11005/04"));
        aliceColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_alice_color_05.jpg", Constants.FABRICS_BASE_URL+"alice/fabric_alice_color_05.jpg","Color 11005/05","Χρώμα 11005/05"));

        Fabric alice=new Fabric(R.drawable.alice,"Alice",aliceColors);

        ArrayList<String> aliceCurtains=new ArrayList<>();
        aliceCurtains.add(Constants.CURTAINS_BASE_URL+"alice/curtain_alice_color_01.png");
        aliceCurtains.add(Constants.CURTAINS_BASE_URL+"alice/curtain_alice_color_02.png");
        aliceCurtains.add(Constants.CURTAINS_BASE_URL+"alice/curtain_alice_color_03.png");
        aliceCurtains.add(Constants.CURTAINS_BASE_URL+"alice/curtain_alice_color_04.png");
        aliceCurtains.add(Constants.CURTAINS_BASE_URL+"alice/curtain_alice_color_05.png");
        alice.setCurtains(aliceCurtains);

        ArrayList<FabricColor> annaColors=new ArrayList<>();
        annaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_anna_color_01.jpg", Constants.FABRICS_BASE_URL+"anna/fabric_anna_color_01.jpg","Color 14002/01","Χρώμα 14002/01"));
        annaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_anna_color_02.jpg", Constants.FABRICS_BASE_URL+"anna/fabric_anna_color_02.jpg","Color 14002/02","Χρώμα 14002/02"));
        annaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_anna_color_03.jpg", Constants.FABRICS_BASE_URL+"anna/fabric_anna_color_03.jpg","Color 14002/03","Χρώμα 14002/03"));
        annaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_anna_color_04.jpg", Constants.FABRICS_BASE_URL+"anna/fabric_anna_color_04.jpg","Color 14002/04","Χρώμα 14002/04"));
        annaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_anna_color_05.jpg", Constants.FABRICS_BASE_URL+"anna/fabric_anna_color_05.jpg","Color 14002/05","Χρώμα 14002/05"));

        Fabric anna=new Fabric(R.drawable.anna,"Anna",annaColors);

        ArrayList<String> annaCurtains=new ArrayList<>();
        annaCurtains.add(Constants.CURTAINS_BASE_URL+"anna/curtain_anna_color_01.png");
        annaCurtains.add(Constants.CURTAINS_BASE_URL+"anna/curtain_anna_color_02.png");
        annaCurtains.add(Constants.CURTAINS_BASE_URL+"anna/curtain_anna_color_03.png");
        annaCurtains.add(Constants.CURTAINS_BASE_URL+"anna/curtain_anna_color_04.png");
        annaCurtains.add(Constants.CURTAINS_BASE_URL+"anna/curtain_anna_color_05.png");
        anna.setCurtains(annaCurtains);



        ArrayList<FabricColor> armoniaColors=new ArrayList<>();
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_01.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_01.jpg","Color 11007/01","Χρώμα 11007/01"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_02.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_02.jpg","Color 11007/02","Χρώμα 11007/02"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_03.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_03.jpg","Color 11007/03","Χρώμα 11007/03"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_04.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_04.jpg","Color 11007/04","Χρώμα 11007/04"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_05.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_05.jpg","Color 11007/05","Χρώμα 11007/05"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_06.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_06.jpg","Color 11007/06","Χρώμα 11007/06"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_07.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_07.jpg","Color 11007/07","Χρώμα 11007/07"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_08.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_08.jpg","Color 11007/08","Χρώμα 11007/08"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_09.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_09.jpg","Color 11007/09","Χρώμα 11007/09"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_10.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_10.jpg","Color 11007/10","Χρώμα 11007/10"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_11.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_11.jpg","Color 11007/11","Χρώμα 11007/11"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_12.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_12.jpg","Color 11007/12","Χρώμα 11007/12"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_13.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_13.jpg","Color 11007/13","Χρώμα 11007/13"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_14.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_14.jpg","Color 11007/14","Χρώμα 11007/14"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_15.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_15.jpg","Color 11007/15","Χρώμα 11007/15"));
        armoniaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_armonia_color_16.jpg", Constants.FABRICS_BASE_URL+"armonia/fabric_armonia_color_16.jpg","Color 11007/16","Χρώμα 11007/16"));

        Fabric armonia=new Fabric(R.drawable.armonia,"ARMONIA",armoniaColors);

        ArrayList<String> armoniaCurtains=new ArrayList<>();
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_01.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_02.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_03.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_04.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_05.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_06.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_07.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_08.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_09.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_10.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_11.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_12.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_13.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_14.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_15.png");
        armoniaCurtains.add(Constants.CURTAINS_BASE_URL+"armonia/curtain_armonia_color_16.png");
        armonia.setCurtains(armoniaCurtains);

        ArrayList<FabricColor> athenaColors=new ArrayList<>();
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_01.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_01.jpg","Color 14006/01","Χρώμα 14006/01"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_02.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_02.jpg","Color 14006/02","Χρώμα 14006/02"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_03.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_03.jpg","Color 14006/03","Χρώμα 14006/03"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_04.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_04.jpg","Color 14006/04","Χρώμα 14006/04"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_05.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_05.jpg","Color 14006/05","Χρώμα 14006/05"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_06.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_06.jpg","Color 14006/06","Χρώμα 14006/06"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_07.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_07.jpg","Color 14006/07","Χρώμα 14006/07"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_08.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_08.jpg","Color 14006/08","Χρώμα 14006/08"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_09.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_09.jpg","Color 14006/09","Χρώμα 14006/09"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_10.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_10.jpg","Color 14006/10","Χρώμα 14006/10"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_11.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_11.jpg","Color 14006/11","Χρώμα 14006/11"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_12.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_12.jpg","Color 14006/12","Χρώμα 14006/12"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_13.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_13.jpg","Color 14006/13","Χρώμα 14006/13"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_14.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_14.jpg","Color 14006/14","Χρώμα 14006/14"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_15.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_15.jpg","Color 14006/15","Χρώμα 14006/15"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_16.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_16.jpg","Color 14006/16","Χρώμα 14006/16"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_17.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_17.jpg","Color 14006/17","Χρώμα 14006/17"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_18.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_18.jpg","Color 14006/18","Χρώμα 14006/18"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_19.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_19.jpg","Color 14006/19","Χρώμα 14006/19"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_20.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_20.jpg","Color 14006/20","Χρώμα 14006/20"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_21.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_21.jpg","Color 14006/21","Χρώμα 14006/21"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_22.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_22.jpg","Color 14006/22","Χρώμα 14006/22"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_23.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_23.jpg","Color 14006/23","Χρώμα 14006/23"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_24.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_24.jpg","Color 14006/24","Χρώμα 14006/24"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_25.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_25.jpg","Color 14006/25","Χρώμα 14006/25"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_26.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_26.jpg","Color 14006/26","Χρώμα 14006/26"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_27.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_27.jpg","Color 14006/27","Χρώμα 14006/27"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_28.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_28.jpg","Color 14006/28","Χρώμα 14006/28"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_29.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_29.jpg","Color 14006/29","Χρώμα 14006/29"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_30.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_30.jpg","Color 14006/30","Χρώμα 14006/30"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_31.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_31.jpg","Color 14006/31","Χρώμα 14006/31"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_32.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_32.jpg","Color 14006/32","Χρώμα 14006/32"));
        athenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_athena_color_33.jpg", Constants.FABRICS_BASE_URL+"athena/fabric_athena_color_33.jpg","Color 14006/33","Χρώμα 14006/33"));


        Fabric athena=new Fabric(R.drawable.athena,"ATHENA",athenaColors);

        ArrayList<String> athenaCurtains=new ArrayList<>();
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_01.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_02.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_03.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_04.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_05.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_06.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_07.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_08.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_09.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_10.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_11.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_12.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_13.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_14.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_15.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_16.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_17.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_18.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_19.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_20.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_21.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_22.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_23.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_24.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_25.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_26.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_27.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_28.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_29.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_30.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_31.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_32.png");
        athenaCurtains.add(Constants.CURTAINS_BASE_URL+"athena/curtain_athena_color_33.png");
        athena.setCurtains(athenaCurtains);


        ArrayList<FabricColor> eleonoraColors=new ArrayList<>();
        eleonoraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eleonora_color_01.jpg", Constants.FABRICS_BASE_URL+"eleonora/fabric_eleonora_color_01.jpg","Color 13001/01","Χρώμα 13001/01"));
        eleonoraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eleonora_color_02.jpg", Constants.FABRICS_BASE_URL+"eleonora/fabric_eleonora_color_02.jpg","Color 13001/02","Χρώμα 13001/02"));
        eleonoraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eleonora_color_03.jpg", Constants.FABRICS_BASE_URL+"eleonora/fabric_eleonora_color_03.jpg","Color 13001/03","Χρώμα 13001/03"));
        eleonoraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eleonora_color_04.jpg", Constants.FABRICS_BASE_URL+"eleonora/fabric_eleonora_color_04.jpg","Color 13001/04","Χρώμα 13001/04"));
        eleonoraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eleonora_color_05.jpg", Constants.FABRICS_BASE_URL+"eleonora/fabric_eleonora_color_05.jpg","Color 13001/05","Χρώμα 13001/05"));
        eleonoraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_eleonora_color_06.jpg", Constants.FABRICS_BASE_URL+"eleonora/fabric_eleonora_color_06.jpg","Color 13001/06","Χρώμα 13001/06"));


        Fabric eleonora=new Fabric(R.drawable.eleonora,"ELEONORA",eleonoraColors);

        ArrayList<String> eleonoraCurtains=new ArrayList<>();
        eleonoraCurtains.add(Constants.CURTAINS_BASE_URL+"eleonora/curtain_eleonora_color_01.png");
        eleonoraCurtains.add(Constants.CURTAINS_BASE_URL+"eleonora/curtain_eleonora_color_02.png");
        eleonoraCurtains.add(Constants.CURTAINS_BASE_URL+"eleonora/curtain_eleonora_color_03.png");
        eleonoraCurtains.add(Constants.CURTAINS_BASE_URL+"eleonora/curtain_eleonora_color_04.png");
        eleonoraCurtains.add(Constants.CURTAINS_BASE_URL+"eleonora/curtain_eleonora_color_05.png");
        eleonoraCurtains.add(Constants.CURTAINS_BASE_URL+"eleonora/curtain_eleonora_color_06.png");
        eleonora.setCurtains(eleonoraCurtains);

        ArrayList<FabricColor> erikaColors=new ArrayList<>();
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_01.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_01.jpg","Color 20008/01","Χρώμα 20008/01"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_02.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_02.jpg","Color 20008/02","Χρώμα 20008/02"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_03.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_03.jpg","Color 20008/03","Χρώμα 20008/03"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_04.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_04.jpg","Color 20008/04","Χρώμα 20008/04"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_05.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_05.jpg","Color 20008/05","Χρώμα 20008/05"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_06.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_06.jpg","Color 20008/06","Χρώμα 20008/06"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_07.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_07.jpg","Color 20008/07","Χρώμα 20008/07"));
        erikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erika_color_08.jpg", Constants.FABRICS_BASE_URL+"erika/fabric_erika_color_08.jpg","Color 20008/08","Χρώμα 20008/08"));


        Fabric erika=new Fabric(R.drawable.erika,"ERIKA",erikaColors);

        ArrayList<String> erikaCurtains=new ArrayList<>();
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_01.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_02.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_03.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_04.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_05.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_06.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_07.png");
        erikaCurtains.add(Constants.CURTAINS_BASE_URL+"erika/curtain_erika_color_08.png");
        erika.setCurtains(erikaCurtains);

        ArrayList<FabricColor> floraColors=new ArrayList<>();
        floraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_flora_color_01.jpg", Constants.FABRICS_BASE_URL+"flora/fabric_flora_color_01.jpg","Color 15004/01","Χρώμα 15004/01"));
        floraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_flora_color_02.jpg", Constants.FABRICS_BASE_URL+"flora/fabric_flora_color_02.jpg","Color 15004/02","Χρώμα 15004/02"));
        floraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_flora_color_03.jpg", Constants.FABRICS_BASE_URL+"flora/fabric_flora_color_03.jpg","Color 15004/03","Χρώμα 15004/03"));

        Fabric flora=new Fabric(R.drawable.flora,"FLORA",floraColors);

        ArrayList<String> floraCurtains=new ArrayList<>();
        floraCurtains.add(Constants.CURTAINS_BASE_URL+"flora/curtain_flora_color_01.png");
        floraCurtains.add(Constants.CURTAINS_BASE_URL+"flora/curtain_flora_color_02.png");
        floraCurtains.add(Constants.CURTAINS_BASE_URL+"flora/curtain_flora_color_03.png");
        flora.setCurtains(floraCurtains);


        ArrayList<FabricColor> irinaColors=new ArrayList<>();
        irinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_irina_color_01.jpg", Constants.FABRICS_BASE_URL+"irina/fabric_irina_color_01.jpg","Color 12003/01","Χρώμα 12003/01"));
        irinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_irina_color_02.jpg", Constants.FABRICS_BASE_URL+"irina/fabric_irina_color_02.jpg","Color 12003/02","Χρώμα 12003/02"));
        irinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_irina_color_03.jpg", Constants.FABRICS_BASE_URL+"irina/fabric_irina_color_03.jpg","Color 12003/03","Χρώμα 12003/03"));
        irinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_irina_color_04.jpg", Constants.FABRICS_BASE_URL+"irina/fabric_irina_color_04.jpg","Color 12003/04","Χρώμα 12003/04"));
        irinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_irina_color_05.jpg", Constants.FABRICS_BASE_URL+"irina/fabric_irina_color_05.jpg","Color 12003/05","Χρώμα 12003/05"));

        Fabric irina=new Fabric(R.drawable.irina,"IRINA",irinaColors);

        ArrayList<String> irinaCurtains=new ArrayList<>();
        irinaCurtains.add(Constants.CURTAINS_BASE_URL+"irina/curtain_irina_color_01.png");
        irinaCurtains.add(Constants.CURTAINS_BASE_URL+"irina/curtain_irina_color_02.png");
        irinaCurtains.add(Constants.CURTAINS_BASE_URL+"irina/curtain_irina_color_03.png");
        irinaCurtains.add(Constants.CURTAINS_BASE_URL+"irina/curtain_irina_color_04.png");
        irinaCurtains.add(Constants.CURTAINS_BASE_URL+"irina/curtain_irina_color_05.png");
        irina.setCurtains(irinaCurtains);


        ArrayList<FabricColor> isabellaColors=new ArrayList<>();
        isabellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_isabella_color_01.jpg", Constants.FABRICS_BASE_URL+"isabella/fabric_isabella_color_01.jpg","Color 11006/01","Χρώμα 11006/01"));
        isabellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_isabella_color_02.jpg", Constants.FABRICS_BASE_URL+"isabella/fabric_isabella_color_02.jpg","Color 11006/02","Χρώμα 11006/02"));
        isabellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_isabella_color_03.jpg", Constants.FABRICS_BASE_URL+"isabella/fabric_isabella_color_03.jpg","Color 11006/03","Χρώμα 11006/03"));
        isabellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_isabella_color_04.jpg", Constants.FABRICS_BASE_URL+"isabella/fabric_isabella_color_04.jpg","Color 11006/04","Χρώμα 11006/04"));
        isabellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_isabella_color_05.jpg", Constants.FABRICS_BASE_URL+"isabella/fabric_isabella_color_05.jpg","Color 11006/05","Χρώμα 11006/05"));
        isabellaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_isabella_color_06.jpg", Constants.FABRICS_BASE_URL+"isabella/fabric_isabella_color_06.jpg","Color 11006/06","Χρώμα 11006/06"));

        Fabric isabella=new Fabric(R.drawable.isabella,"ISABELLA",isabellaColors);

        ArrayList<String> isabellaCurtains=new ArrayList<>();
        isabellaCurtains.add(Constants.CURTAINS_BASE_URL+"isabella/curtain_isabella_color_01.png");
        isabellaCurtains.add(Constants.CURTAINS_BASE_URL+"isabella/curtain_isabella_color_02.png");
        isabellaCurtains.add(Constants.CURTAINS_BASE_URL+"isabella/curtain_isabella_color_03.png");
        isabellaCurtains.add(Constants.CURTAINS_BASE_URL+"isabella/curtain_isabella_color_04.png");
        isabellaCurtains.add(Constants.CURTAINS_BASE_URL+"isabella/curtain_isabella_color_05.png");
        isabellaCurtains.add(Constants.CURTAINS_BASE_URL+"isabella/curtain_isabella_color_06.png");
        isabella.setCurtains(isabellaCurtains);

        ArrayList<FabricColor> julietColors=new ArrayList<>();
        julietColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_juliet_color_01.jpg", Constants.FABRICS_BASE_URL+"juliet/fabric_juliet_color_01.jpg","Color 22001/01","Χρώμα 22001/01"));
        julietColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_juliet_color_02.jpg", Constants.FABRICS_BASE_URL+"juliet/fabric_juliet_color_02.jpg","Color 22001/02","Χρώμα 22001/02"));
        julietColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_juliet_color_03.jpg", Constants.FABRICS_BASE_URL+"juliet/fabric_juliet_color_03.jpg","Color 22001/03","Χρώμα 22001/03"));
        julietColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_juliet_color_04.jpg", Constants.FABRICS_BASE_URL+"juliet/fabric_juliet_color_04.jpg","Color 22001/04","Χρώμα 22001/04"));

        Fabric juliet=new Fabric(R.drawable.juliet,"JULIET",julietColors);

        ArrayList<String> julietCurtains=new ArrayList<>();
        julietCurtains.add(Constants.CURTAINS_BASE_URL+"juliet/curtain_juliet_color_01.png");
        julietCurtains.add(Constants.CURTAINS_BASE_URL+"juliet/curtain_juliet_color_02.png");
        julietCurtains.add(Constants.CURTAINS_BASE_URL+"juliet/curtain_juliet_color_03.png");
        julietCurtains.add(Constants.CURTAINS_BASE_URL+"juliet/curtain_juliet_color_04.png");

        juliet.setCurtains(julietCurtains);


        ArrayList<FabricColor> korinaColors=new ArrayList<>();
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_01.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_01.jpg","Color 11004/01","Χρώμα 11004/01"));
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_02.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_02.jpg","Color 11004/02","Χρώμα 11004/02"));
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_03.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_03.jpg","Color 11004/03","Χρώμα 11004/03"));
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_04.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_04.jpg","Color 11004/04","Χρώμα 11004/04"));
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_05.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_05.jpg","Color 11004/05","Χρώμα 11004/05"));
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_06.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_06.jpg","Color 11004/06","Χρώμα 11004/06"));
        korinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_korina_color_07.jpg", Constants.FABRICS_BASE_URL+"korina/fabric_korina_color_07.jpg","Color 11004/07","Χρώμα 11004/07"));

        Fabric korina=new Fabric(R.drawable.korina,"KORINA",korinaColors);

        ArrayList<String> korinaCurtains=new ArrayList<>();
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_01.png");
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_02.png");
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_03.png");
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_04.png");
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_05.png");
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_06.png");
        korinaCurtains.add(Constants.CURTAINS_BASE_URL+"korina/curtain_korina_color_07.png");
        korina.setCurtains(korinaCurtains);

        ArrayList<FabricColor> kristinaColors=new ArrayList<>();
        kristinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kristina_color_01.jpg", Constants.FABRICS_BASE_URL+"kristina/fabric_kristina_color_01.jpg","Color 15002/01","Χρώμα 15002/01"));
        kristinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kristina_color_02.jpg", Constants.FABRICS_BASE_URL+"kristina/fabric_kristina_color_02.jpg","Color 15002/02","Χρώμα 15002/02"));
        kristinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kristina_color_03.jpg", Constants.FABRICS_BASE_URL+"kristina/fabric_kristina_color_03.jpg","Color 15002/03","Χρώμα 15002/03"));

        Fabric kristina=new Fabric(R.drawable.kristina,"KRISTINA",kristinaColors);

        ArrayList<String> kristinaCurtains=new ArrayList<>();
        kristinaCurtains.add(Constants.CURTAINS_BASE_URL+"kristina/curtain_kristina_color_01.png");
        kristinaCurtains.add(Constants.CURTAINS_BASE_URL+"kristina/curtain_kristina_color_02.png");
        kristinaCurtains.add(Constants.CURTAINS_BASE_URL+"kristina/curtain_kristina_color_03.png");

        kristina.setCurtains(kristinaCurtains);


        ArrayList<FabricColor> lindaColors=new ArrayList<>();
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_01.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_01.jpg","Color 11008/01","Χρώμα 11008/01"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_02.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_02.jpg","Color 11008/02","Χρώμα 11008/02"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_03.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_03.jpg","Color 11008/03","Χρώμα 11008/03"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_04.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_04.jpg","Color 11008/04","Χρώμα 11008/04"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_05.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_05.jpg","Color 11008/05","Χρώμα 11008/05"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_06.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_06.jpg","Color 11008/06","Χρώμα 11008/06"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_07.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_07.jpg","Color 11008/07","Χρώμα 11008/07"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_08.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_08.jpg","Color 11008/08","Χρώμα 11008/08"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_09.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_09.jpg","Color 11008/09","Χρώμα 11008/09"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_10.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_10.jpg","Color 11008/10","Χρώμα 11008/10"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_11.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_11.jpg","Color 11008/11","Χρώμα 11008/11"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_12.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_12.jpg","Color 11008/12","Χρώμα 11008/12"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_13.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_13.jpg","Color 11008/13","Χρώμα 11008/13"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_14.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_14.jpg","Color 11008/14","Χρώμα 11008/14"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_15.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_15.jpg","Color 11008/15","Χρώμα 11008/15"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_16.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_16.jpg","Color 11008/16","Χρώμα 11008/16"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_17.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_17.jpg","Color 11008/17","Χρώμα 11008/17"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_18.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_18.jpg","Color 11008/18","Χρώμα 11008/18"));
        lindaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_linda_color_19.jpg", Constants.FABRICS_BASE_URL+"linda/fabric_linda_color_19.jpg","Color 11008/19","Χρώμα 11008/19"));

        Fabric linda=new Fabric(R.drawable.linda,"LINDA",lindaColors);

        ArrayList<String> lindaCurtains=new ArrayList<>();
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_01.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_02.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_03.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_04.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_05.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_06.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_07.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_08.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_09.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_10.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_11.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_12.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_13.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_14.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_15.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_16.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_17.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_18.png");
        lindaCurtains.add(Constants.CURTAINS_BASE_URL+"linda/curtain_linda_color_19.png");
        linda.setCurtains(lindaCurtains);


        ArrayList<FabricColor> melanieColors=new ArrayList<>();
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_01.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_01.jpg","Color 20007/01","Χρώμα 20007/01"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_02.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_02.jpg","Color 20007/02","Χρώμα 20007/02"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_03.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_03.jpg","Color 20007/03","Χρώμα 20007/03"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_04.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_04.jpg","Color 20007/04","Χρώμα 20007/04"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_05.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_05.jpg","Color 20007/05","Χρώμα 20007/05"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_06.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_06.jpg","Color 20007/06","Χρώμα 20007/06"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_07.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_07.jpg","Color 20007/07","Χρώμα 20007/07"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_08.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_08.jpg","Color 20007/08","Χρώμα 20007/08"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_09.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_09.jpg","Color 20007/09","Χρώμα 20007/09"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_10.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_10.jpg","Color 20007/10","Χρώμα 20007/10"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_11.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_11.jpg","Color 20007/11","Χρώμα 20007/11"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_12.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_12.jpg","Color 20007/12","Χρώμα 20007/12"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_13.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_13.jpg","Color 20007/13","Χρώμα 20007/13"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_14.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_14.jpg","Color 20007/14","Χρώμα 20007/14"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_15.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_15.jpg","Color 20007/15","Χρώμα 20007/15"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_16.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_16.jpg","Color 20007/16","Χρώμα 20007/16"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_17.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_17.jpg","Color 20007/17","Χρώμα 20007/17"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_18.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_18.jpg","Color 20007/18","Χρώμα 20007/18"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_19.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_19.jpg","Color 20007/19","Χρώμα 20007/19"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_20.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_20.jpg","Color 20007/20","Χρώμα 20007/20"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_21.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_21.jpg","Color 20007/21","Χρώμα 20007/21"));
        melanieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_melanie_color_22.jpg", Constants.FABRICS_BASE_URL+"melanie/fabric_melanie_color_22.jpg","Color 20007/22","Χρώμα 20007/22"));


        Fabric melanie=new Fabric(R.drawable.melanie,"MELANIE",melanieColors);

        ArrayList<String> melanieCurtains=new ArrayList<>();
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_01.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_02.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_03.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_04.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_05.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_06.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_07.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_08.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_09.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_10.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_11.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_12.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_13.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_14.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_15.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_16.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_17.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_18.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_19.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_20.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_21.png");
        melanieCurtains.add(Constants.CURTAINS_BASE_URL+"melanie/curtain_melanie_color_22.png");
        melanie.setCurtains(melanieCurtains);


        ArrayList<FabricColor> nafsikaColors=new ArrayList<>();
        nafsikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nafsika_color_01.jpg", Constants.FABRICS_BASE_URL+"nafsika/fabric_nafsika_color_01.jpg","Color 12008/01","Χρώμα 12008/01"));
        nafsikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nafsika_color_02.jpg", Constants.FABRICS_BASE_URL+"nafsika/fabric_nafsika_color_02.jpg","Color 12008/02","Χρώμα 12008/02"));
        nafsikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nafsika_color_03.jpg", Constants.FABRICS_BASE_URL+"nafsika/fabric_nafsika_color_03.jpg","Color 12008/03","Χρώμα 12008/03"));
        nafsikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nafsika_color_04.jpg", Constants.FABRICS_BASE_URL+"nafsika/fabric_nafsika_color_04.jpg","Color 12008/04","Χρώμα 12008/04"));
        nafsikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nafsika_color_05.jpg", Constants.FABRICS_BASE_URL+"nafsika/fabric_nafsika_color_05.jpg","Color 12008/05","Χρώμα 12008/05"));
        nafsikaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nafsika_color_06.jpg", Constants.FABRICS_BASE_URL+"nafsika/fabric_nafsika_color_06.jpg","Color 12008/06","Χρώμα 12008/06"));

        Fabric nafsika=new Fabric(R.drawable.nafsika,"NAFSIKA",nafsikaColors);

        ArrayList<String> nafsikaCurtains=new ArrayList<>();
        nafsikaCurtains.add(Constants.CURTAINS_BASE_URL+"nafsika/curtain_nafsika_color_01.png");
        nafsikaCurtains.add(Constants.CURTAINS_BASE_URL+"nafsika/curtain_nafsika_color_02.png");
        nafsikaCurtains.add(Constants.CURTAINS_BASE_URL+"nafsika/curtain_nafsika_color_03.png");
        nafsikaCurtains.add(Constants.CURTAINS_BASE_URL+"nafsika/curtain_nafsika_color_04.png");
        nafsikaCurtains.add(Constants.CURTAINS_BASE_URL+"nafsika/curtain_nafsika_color_05.png");
        nafsikaCurtains.add(Constants.CURTAINS_BASE_URL+"nafsika/curtain_nafsika_color_06.png");

        nafsika.setCurtains(nafsikaCurtains);


        ArrayList<FabricColor> noraColors=new ArrayList<>();
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_01.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_01.jpg","Color 12001/01","Χρώμα 12001/01"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_02.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_02.jpg","Color 12001/02","Χρώμα 12001/02"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_03.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_03.jpg","Color 12001/03","Χρώμα 12001/03"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_04.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_04.jpg","Color 12001/04","Χρώμα 12001/04"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_05.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_05.jpg","Color 12001/05","Χρώμα 12001/05"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_06.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_06.jpg","Color 12001/06","Χρώμα 12001/06"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_07.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_07.jpg","Color 12001/07","Χρώμα 12001/07"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_08.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_08.jpg","Color 12001/08","Χρώμα 12001/08"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_09.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_09.jpg","Color 12001/09","Χρώμα 12001/09"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_10.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_10.jpg","Color 12001/10","Χρώμα 12001/10"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_11.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_11.jpg","Color 12001/11","Χρώμα 12001/11"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_12.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_12.jpg","Color 12001/12","Χρώμα 12001/12"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_13.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_13.jpg","Color 12001/13","Χρώμα 12001/13"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_14.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_14.jpg","Color 12001/14","Χρώμα 12001/14"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_15.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_15.jpg","Color 12001/15","Χρώμα 12001/15"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_16.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_16.jpg","Color 12001/16","Χρώμα 12001/16"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_17.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_17.jpg","Color 12001/17","Χρώμα 12001/17"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_18.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_18.jpg","Color 12001/18","Χρώμα 12001/18"));
        noraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nora_color_19.jpg", Constants.FABRICS_BASE_URL+"nora/fabric_nora_color_19.jpg","Color 12001/19","Χρώμα 12001/19"));



        Fabric nora=new Fabric(R.drawable.nora,"NORA",noraColors);

        ArrayList<String> noraCurtains=new ArrayList<>();
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_01.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_02.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_03.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_04.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_05.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_06.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_07.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_08.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_09.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_10.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_11.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_12.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_13.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_14.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_15.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_16.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_17.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_18.png");
        noraCurtains.add(Constants.CURTAINS_BASE_URL+"nora/curtain_nora_color_19.png");

        nora.setCurtains(noraCurtains);



        ArrayList<FabricColor> zizelColors=new ArrayList<>();
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_01.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_01.jpg","Color 13007/01","Χρώμα 13007/01"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_02.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_02.jpg","Color 13007/02","Χρώμα 13007/02"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_03.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_03.jpg","Color 13007/03","Χρώμα 13007/03"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_04.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_04.jpg","Color 13007/04","Χρώμα 13007/04"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_05.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_05.jpg","Color 13007/05","Χρώμα 13007/05"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_06.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_06.jpg","Color 13007/06","Χρώμα 13007/06"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_07.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_07.jpg","Color 13007/07","Χρώμα 13007/07"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_08.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_08.jpg","Color 13007/08","Χρώμα 13007/08"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_09.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_09.jpg","Color 13007/09","Χρώμα 13007/09"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_10.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_10.jpg","Color 13007/10","Χρώμα 13007/10"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_11.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_11.jpg","Color 13007/11","Χρώμα 13007/11"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_12.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_12.jpg","Color 13007/12","Χρώμα 13007/12"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_13.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_13.jpg","Color 13007/13","Χρώμα 13007/13"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_14.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_14.jpg","Color 13007/14","Χρώμα 13007/14"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_15.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_15.jpg","Color 13007/15","Χρώμα 13007/15"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_16.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_16.jpg","Color 13007/16","Χρώμα 13007/16"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_17.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_17.jpg","Color 13007/17","Χρώμα 13007/17"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_18.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_18.jpg","Color 13007/18","Χρώμα 13007/18"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_19.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_19.jpg","Color 13007/19","Χρώμα 13007/19"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_20.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_20.jpg","Color 13007/20","Χρώμα 13007/20"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_21.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_21.jpg","Color 13007/21","Χρώμα 13007/21"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_22.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_22.jpg","Color 13007/22","Χρώμα 13007/22"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_23.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_23.jpg","Color 13007/23","Χρώμα 13007/23"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_24.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_24.jpg","Color 13007/24","Χρώμα 13007/24"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_25.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_25.jpg","Color 13007/25","Χρώμα 13007/25"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_26.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_26.jpg","Color 13007/26","Χρώμα 13007/26"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_27.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_27.jpg","Color 13007/27","Χρώμα 13007/27"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_28.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_28.jpg","Color 13007/28","Χρώμα 13007/28"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_29.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_29.jpg","Color 13007/29","Χρώμα 13007/29"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_30.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_30.jpg","Color 13007/30","Χρώμα 13007/30"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_31.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_31.jpg","Color 13007/31","Χρώμα 13007/31"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_32.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_32.jpg","Color 13007/32","Χρώμα 13007/32"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_33.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_33.jpg","Color 13007/33","Χρώμα 13007/33"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_34.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_34.jpg","Color 13007/34","Χρώμα 13007/34"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_35.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_35.jpg","Color 13007/35","Χρώμα 13007/35"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_36.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_36.jpg","Color 13007/36","Χρώμα 13007/36"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_37.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_37.jpg","Color 13007/37","Χρώμα 13007/37"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_38.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_38.jpg","Color 13007/38","Χρώμα 13007/38"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_39.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_39.jpg","Color 13007/39","Χρώμα 13007/39"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_40.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_40.jpg","Color 13007/40","Χρώμα 13007/40"));
        zizelColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_zizel_color_41.jpg", Constants.FABRICS_BASE_URL+"zizel/fabric_zizel_color_41.jpg","Color 13007/41","Χρώμα 13007/41"));


        Fabric zizel=new Fabric(R.drawable.zizel,"ZIZEL",zizelColors);

        ArrayList<String> zizelCurtains=new ArrayList<>();
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_01.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_02.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_03.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_04.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_05.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_06.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_07.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_08.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_09.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_10.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_11.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_12.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_13.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_14.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_15.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_16.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_17.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_18.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_19.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_20.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_21.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_22.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_23.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_24.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_25.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_26.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_27.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_28.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_29.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_30.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_31.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_32.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_33.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_34.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_35.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_36.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_37.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_38.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_39.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_40.png");
        zizelCurtains.add(Constants.CURTAINS_BASE_URL+"zizel/curtain_zizel_color_41.png");
        zizel.setCurtains(zizelCurtains);

        ArrayList<FabricColor> brendaColors=new ArrayList<>();
        brendaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brenda_color_01.jpg", Constants.FABRICS_BASE_URL+"brenda/fabric_brenda_color_01.jpg","Color 30001/01","Χρώμα 30001/01"));
        brendaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brenda_color_02.jpg", Constants.FABRICS_BASE_URL+"brenda/fabric_brenda_color_02.jpg","Color 30001/02","Χρώμα 30001/02"));
        brendaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brenda_color_03.jpg", Constants.FABRICS_BASE_URL+"brenda/fabric_brenda_color_03.jpg","Color 30001/03","Χρώμα 30001/03"));
        brendaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brenda_color_04.jpg", Constants.FABRICS_BASE_URL+"brenda/fabric_brenda_color_04.jpg","Color 30001/04","Χρώμα 30001/04"));
        brendaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brenda_color_05.jpg", Constants.FABRICS_BASE_URL+"brenda/fabric_brenda_color_05.jpg","Color 30001/05","Χρώμα 30001/05"));
        brendaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_brenda_color_06.jpg", Constants.FABRICS_BASE_URL+"brenda/fabric_brenda_color_06.jpg","Color 30001/06","Χρώμα 30001/06"));

        Fabric brenda=new Fabric(R.drawable.brenda,"BRENDA",brendaColors);

        ArrayList<String> brendaCurtains=new ArrayList<>();
        brendaCurtains.add(Constants.CURTAINS_BASE_URL+"brenda/curtain_brenda_color_01.png");
        brendaCurtains.add(Constants.CURTAINS_BASE_URL+"brenda/curtain_brenda_color_02.png");
        brendaCurtains.add(Constants.CURTAINS_BASE_URL+"brenda/curtain_brenda_color_03.png");
        brendaCurtains.add(Constants.CURTAINS_BASE_URL+"brenda/curtain_brenda_color_04.png");
        brendaCurtains.add(Constants.CURTAINS_BASE_URL+"brenda/curtain_brenda_color_05.png");
        brendaCurtains.add(Constants.CURTAINS_BASE_URL+"brenda/curtain_brenda_color_06.png");

        brenda.setCurtains(brendaCurtains);

        ArrayList<FabricColor> dorothyColors=new ArrayList<>();
        dorothyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dorothy_color_01.jpg", Constants.FABRICS_BASE_URL+"dorothy/fabric_dorothy_color_01.jpg","Color 16007/01","Χρώμα 16007/01"));
        dorothyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dorothy_color_02.jpg", Constants.FABRICS_BASE_URL+"dorothy/fabric_dorothy_color_02.jpg","Color 16007/02","Χρώμα 16007/02"));
        dorothyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dorothy_color_03.jpg", Constants.FABRICS_BASE_URL+"dorothy/fabric_dorothy_color_03.jpg","Color 16007/03","Χρώμα 16007/03"));
        dorothyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dorothy_color_04.jpg", Constants.FABRICS_BASE_URL+"dorothy/fabric_dorothy_color_04.jpg","Color 16007/04","Χρώμα 16007/04"));
        dorothyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dorothy_color_05.jpg", Constants.FABRICS_BASE_URL+"dorothy/fabric_dorothy_color_05.jpg","Color 16007/05","Χρώμα 16007/05"));
        dorothyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dorothy_color_06.jpg", Constants.FABRICS_BASE_URL+"dorothy/fabric_dorothy_color_06.jpg","Color 16007/06","Χρώμα 16007/06"));

        Fabric dorothy=new Fabric(R.drawable.dorothy,"DOROTHY",dorothyColors);

        ArrayList<String> dorothyCurtains=new ArrayList<>();
        dorothyCurtains.add(Constants.CURTAINS_BASE_URL+"dorothy/curtain_dorothy_color_01.png");
        dorothyCurtains.add(Constants.CURTAINS_BASE_URL+"dorothy/curtain_dorothy_color_02.png");
        dorothyCurtains.add(Constants.CURTAINS_BASE_URL+"dorothy/curtain_dorothy_color_03.png");
        dorothyCurtains.add(Constants.CURTAINS_BASE_URL+"dorothy/curtain_dorothy_color_04.png");
        dorothyCurtains.add(Constants.CURTAINS_BASE_URL+"dorothy/curtain_dorothy_color_05.png");
        dorothyCurtains.add(Constants.CURTAINS_BASE_URL+"dorothy/curtain_dorothy_color_06.png");

        dorothy.setCurtains(dorothyCurtains);

        ArrayList<FabricColor> erofiliColors=new ArrayList<>();
        erofiliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erofili_color_01.jpg", Constants.FABRICS_BASE_URL+"erofili/fabric_erofili_color_01.jpg","Color 15008/01","Χρώμα 15008/01"));
        erofiliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erofili_color_02.jpg", Constants.FABRICS_BASE_URL+"erofili/fabric_erofili_color_02.jpg","Color 15008/02","Χρώμα 15008/02"));
        erofiliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erofili_color_03.jpg", Constants.FABRICS_BASE_URL+"erofili/fabric_erofili_color_03.jpg","Color 15008/03","Χρώμα 15008/03"));
        erofiliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erofili_color_04.jpg", Constants.FABRICS_BASE_URL+"erofili/fabric_erofili_color_04.jpg","Color 15008/04","Χρώμα 15008/04"));
        erofiliColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_erofili_color_05.jpg", Constants.FABRICS_BASE_URL+"erofili/fabric_erofili_color_05.jpg","Color 15008/05","Χρώμα 15008/05"));

        Fabric erofili=new Fabric(R.drawable.erofili,"EROFILI",erofiliColors);

        ArrayList<String> erofiliCurtains=new ArrayList<>();
        erofiliCurtains.add(Constants.CURTAINS_BASE_URL+"erofili/curtain_erofili_color_01.png");
        erofiliCurtains.add(Constants.CURTAINS_BASE_URL+"erofili/curtain_erofili_color_02.png");
        erofiliCurtains.add(Constants.CURTAINS_BASE_URL+"erofili/curtain_erofili_color_03.png");
        erofiliCurtains.add(Constants.CURTAINS_BASE_URL+"erofili/curtain_erofili_color_04.png");
        erofiliCurtains.add(Constants.CURTAINS_BASE_URL+"erofili/curtain_erofili_color_05.png");

        erofili.setCurtains(erofiliCurtains);

        ArrayList<FabricColor> jenniferColors=new ArrayList<>();
        jenniferColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_jennifer_color_01.jpg", Constants.FABRICS_BASE_URL+"jennifer/fabric_jennifer_color_01.jpg","Color 30000/01","Χρώμα 30000/01"));
        jenniferColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_jennifer_color_02.jpg", Constants.FABRICS_BASE_URL+"jennifer/fabric_jennifer_color_02.jpg","Color 30000/02","Χρώμα 30000/02"));
        jenniferColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_jennifer_color_03.jpg", Constants.FABRICS_BASE_URL+"jennifer/fabric_jennifer_color_03.jpg","Color 30000/03","Χρώμα 30000/03"));
        jenniferColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_jennifer_color_04.jpg", Constants.FABRICS_BASE_URL+"jennifer/fabric_jennifer_color_04.jpg","Color 30000/04","Χρώμα 30000/04"));
        jenniferColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_jennifer_color_05.jpg", Constants.FABRICS_BASE_URL+"jennifer/fabric_jennifer_color_05.jpg","Color 30000/05","Χρώμα 30000/05"));
        jenniferColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_jennifer_color_06.jpg", Constants.FABRICS_BASE_URL+"jennifer/fabric_jennifer_color_06.jpg","Color 30000/06","Χρώμα 30000/06"));

        Fabric jennifer=new Fabric(R.drawable.jennifer,"JENNIFER",jenniferColors);

        ArrayList<String> jenniferCurtains=new ArrayList<>();
        jenniferCurtains.add(Constants.CURTAINS_BASE_URL+"jennifer/curtain_jennifer_color_01.png");
        jenniferCurtains.add(Constants.CURTAINS_BASE_URL+"jennifer/curtain_jennifer_color_02.png");
        jenniferCurtains.add(Constants.CURTAINS_BASE_URL+"jennifer/curtain_jennifer_color_03.png");
        jenniferCurtains.add(Constants.CURTAINS_BASE_URL+"jennifer/curtain_jennifer_color_04.png");
        jenniferCurtains.add(Constants.CURTAINS_BASE_URL+"jennifer/curtain_jennifer_color_05.png");
        jenniferCurtains.add(Constants.CURTAINS_BASE_URL+"jennifer/curtain_jennifer_color_06.png");

        jennifer.setCurtains(jenniferCurtains);

        ArrayList<FabricColor> joannaColors=new ArrayList<>();
        joannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joanna_color_01.jpg", Constants.FABRICS_BASE_URL+"joanna/fabric_joanna_color_01.jpg","Color 13013/01","Χρώμα 13013/01"));
        joannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joanna_color_02.jpg", Constants.FABRICS_BASE_URL+"joanna/fabric_joanna_color_02.jpg","Color 13013/02","Χρώμα 13013/02"));
        joannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joanna_color_03.jpg", Constants.FABRICS_BASE_URL+"joanna/fabric_joanna_color_03.jpg","Color 13013/03","Χρώμα 13013/03"));
        joannaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joanna_color_04.jpg", Constants.FABRICS_BASE_URL+"joanna/fabric_joanna_color_04.jpg","Color 13013/04","Χρώμα 13013/04"));


        Fabric joanna=new Fabric(R.drawable.joanna,"JOANNA",joannaColors);

        ArrayList<String> joannaCurtains=new ArrayList<>();
        joannaCurtains.add(Constants.CURTAINS_BASE_URL+"joanna/curtain_joanna_color_01.png");
        joannaCurtains.add(Constants.CURTAINS_BASE_URL+"joanna/curtain_joanna_color_02.png");
        joannaCurtains.add(Constants.CURTAINS_BASE_URL+"joanna/curtain_joanna_color_03.png");
        joannaCurtains.add(Constants.CURTAINS_BASE_URL+"joanna/curtain_joanna_color_04.png");

        joanna.setCurtains(joannaCurtains);

        ArrayList<FabricColor> charlizeColors=new ArrayList<>();
        charlizeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_charlize_color_01.jpg", Constants.FABRICS_BASE_URL+"charlize/fabric_charlize_color_01.jpg","Color 28000/01","Χρώμα 28000/01"));
        charlizeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_charlize_color_02.jpg", Constants.FABRICS_BASE_URL+"charlize/fabric_charlize_color_02.jpg","Color 28000/02","Χρώμα 28000/02"));
        charlizeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_charlize_color_03.jpg", Constants.FABRICS_BASE_URL+"charlize/fabric_charlize_color_03.jpg","Color 28000/03","Χρώμα 28000/03"));

        Fabric charlize=new Fabric(R.drawable.charlize,"CHARLIZE",charlizeColors);

        ArrayList<String> charlizeCurtains=new ArrayList<>();
        charlizeCurtains.add(Constants.CURTAINS_BASE_URL+"charlize/curtain_charlize_color_01.png");
        charlizeCurtains.add(Constants.CURTAINS_BASE_URL+"charlize/curtain_charlize_color_02.png");
        charlizeCurtains.add(Constants.CURTAINS_BASE_URL+"charlize/curtain_charlize_color_03.png");

        charlize.setCurtains(charlizeCurtains);

        ArrayList<FabricColor> kellyColors=new ArrayList<>();
        kellyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kelly_color_01.jpg", Constants.FABRICS_BASE_URL+"kelly/fabric_kelly_color_01.jpg","Color 15009/01","Χρώμα 15009/01"));
        kellyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kelly_color_02.jpg", Constants.FABRICS_BASE_URL+"kelly/fabric_kelly_color_02.jpg","Color 15009/02","Χρώμα 15009/02"));
        kellyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kelly_color_03.jpg", Constants.FABRICS_BASE_URL+"kelly/fabric_kelly_color_03.jpg","Color 15009/03","Χρώμα 15009/03"));
        kellyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kelly_color_04.jpg", Constants.FABRICS_BASE_URL+"kelly/fabric_kelly_color_04.jpg","Color 15009/04","Χρώμα 15009/04"));
        kellyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kelly_color_05.jpg", Constants.FABRICS_BASE_URL+"kelly/fabric_kelly_color_05.jpg","Color 15009/05","Χρώμα 15009/05"));
        kellyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_kelly_color_06.jpg", Constants.FABRICS_BASE_URL+"kelly/fabric_kelly_color_06.jpg","Color 15009/06","Χρώμα 15009/06"));

        Fabric kelly=new Fabric(R.drawable.kelly,"KELLY",kellyColors);

        ArrayList<String> kellyCurtains=new ArrayList<>();
        kellyCurtains.add(Constants.CURTAINS_BASE_URL+"kelly/curtain_kelly_color_01.png");
        kellyCurtains.add(Constants.CURTAINS_BASE_URL+"kelly/curtain_kelly_color_02.png");
        kellyCurtains.add(Constants.CURTAINS_BASE_URL+"kelly/curtain_kelly_color_03.png");
        kellyCurtains.add(Constants.CURTAINS_BASE_URL+"kelly/curtain_kelly_color_04.png");
        kellyCurtains.add(Constants.CURTAINS_BASE_URL+"kelly/curtain_kelly_color_05.png");
        kellyCurtains.add(Constants.CURTAINS_BASE_URL+"kelly/curtain_kelly_color_06.png");

        kelly.setCurtains(kellyCurtains);


        ArrayList<FabricColor> loraColors=new ArrayList<>();
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_01.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_01.jpg","Color 20009/01","Χρώμα 20009/01"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_02.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_02.jpg","Color 20009/02","Χρώμα 20009/02"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_03.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_03.jpg","Color 20009/03","Χρώμα 20009/03"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_04.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_04.jpg","Color 20009/04","Χρώμα 20009/04"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_05.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_05.jpg","Color 20009/05","Χρώμα 20009/05"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_06.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_06.jpg","Color 20009/06","Χρώμα 20009/06"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_07.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_07.jpg","Color 20009/07","Χρώμα 20009/07"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_08.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_08.jpg","Color 20009/08","Χρώμα 20009/08"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_09.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_09.jpg","Color 20009/09","Χρώμα 20009/09"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_10.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_10.jpg","Color 20009/10","Χρώμα 20009/10"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_11.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_11.jpg","Color 20009/11","Χρώμα 20009/11"));
        loraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lora_color_12.jpg", Constants.FABRICS_BASE_URL+"lora/fabric_lora_color_12.jpg","Color 20009/12","Χρώμα 20009/12"));




        Fabric lora=new Fabric(R.drawable.lora,"LORA",loraColors);

        ArrayList<String> loraCurtains=new ArrayList<>();
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_01.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_02.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_03.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_04.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_05.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_06.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_07.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_08.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_09.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_10.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_11.png");
        loraCurtains.add(Constants.CURTAINS_BASE_URL+"lora/curtain_lora_color_12.png");

        lora.setCurtains(loraCurtains);


        ArrayList<FabricColor> luciaColors=new ArrayList<>();
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_01.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_01.jpg","Color 15007/01","Χρώμα 15007/01"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_02.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_02.jpg","Color 15007/02","Χρώμα 15007/02"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_03.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_03.jpg","Color 15007/03","Χρώμα 15007/03"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_04.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_04.jpg","Color 15007/04","Χρώμα 15007/04"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_05.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_05.jpg","Color 15007/05","Χρώμα 15007/05"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_06.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_06.jpg","Color 15007/06","Χρώμα 15007/06"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_07.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_07.jpg","Color 15007/07","Χρώμα 15007/07"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_08.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_08.jpg","Color 15007/08","Χρώμα 15007/08"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_09.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_09.jpg","Color 15007/09","Χρώμα 15007/09"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_10.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_10.jpg","Color 15007/10","Χρώμα 15007/10"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_11.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_11.jpg","Color 15007/11","Χρώμα 15007/11"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_12.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_12.jpg","Color 15007/12","Χρώμα 15007/12"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_13.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_13.jpg","Color 15007/13","Χρώμα 15007/13"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_14.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_14.jpg","Color 15007/14","Χρώμα 15007/14"));
        luciaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lucia_color_15.jpg", Constants.FABRICS_BASE_URL+"lucia/fabric_lucia_color_15.jpg","Color 15007/15","Χρώμα 15007/15"));


        Fabric lucia=new Fabric(R.drawable.lucia,"LUCIA",luciaColors);

        ArrayList<String> luciaCurtains=new ArrayList<>();
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_01.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_02.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_03.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_04.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_05.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_06.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_07.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_08.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_09.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_10.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_11.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_12.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_13.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_14.png");
        luciaCurtains.add(Constants.CURTAINS_BASE_URL+"lucia/curtain_lucia_color_15.png");

        lucia.setCurtains(luciaCurtains);


        ArrayList<FabricColor> natalyColors=new ArrayList<>();
        natalyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nataly_color_01.jpg", Constants.FABRICS_BASE_URL+"nataly/fabric_nataly_color_01.jpg","Color 16005/01","Χρώμα 16005/01"));
        natalyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nataly_color_02.jpg", Constants.FABRICS_BASE_URL+"nataly/fabric_nataly_color_02.jpg","Color 16005/02","Χρώμα 16005/02"));
        natalyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nataly_color_03.jpg", Constants.FABRICS_BASE_URL+"nataly/fabric_nataly_color_03.jpg","Color 16005/03","Χρώμα 16005/03"));
        natalyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nataly_color_04.jpg", Constants.FABRICS_BASE_URL+"nataly/fabric_nataly_color_04.jpg","Color 16005/04","Χρώμα 16005/04"));
        natalyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_nataly_color_05.jpg", Constants.FABRICS_BASE_URL+"nataly/fabric_nataly_color_05.jpg","Color 16005/05","Χρώμα 16005/05"));

        Fabric nataly=new Fabric(R.drawable.nataly,"NATALY",natalyColors);

        ArrayList<String> natalyCurtains=new ArrayList<>();
        natalyCurtains.add(Constants.CURTAINS_BASE_URL+"nataly/curtain_nataly_color_01.png");
        natalyCurtains.add(Constants.CURTAINS_BASE_URL+"nataly/curtain_nataly_color_02.png");
        natalyCurtains.add(Constants.CURTAINS_BASE_URL+"nataly/curtain_nataly_color_03.png");
        natalyCurtains.add(Constants.CURTAINS_BASE_URL+"nataly/curtain_nataly_color_04.png");
        natalyCurtains.add(Constants.CURTAINS_BASE_URL+"nataly/curtain_nataly_color_05.png");
        
        nataly.setCurtains(natalyCurtains);


        ArrayList<FabricColor> penelopeColors=new ArrayList<>();
        penelopeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_penelope_color_01.jpg", Constants.FABRICS_BASE_URL+"penelope/fabric_penelope_color_01.jpg","Color 16008/01","Χρώμα 16008/01"));
        penelopeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_penelope_color_02.jpg", Constants.FABRICS_BASE_URL+"penelope/fabric_penelope_color_02.jpg","Color 16008/02","Χρώμα 16008/02"));
        penelopeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_penelope_color_03.jpg", Constants.FABRICS_BASE_URL+"penelope/fabric_penelope_color_03.jpg","Color 16008/03","Χρώμα 16008/03"));
        penelopeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_penelope_color_04.jpg", Constants.FABRICS_BASE_URL+"penelope/fabric_penelope_color_04.jpg","Color 16008/04","Χρώμα 16008/04"));
        penelopeColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_penelope_color_05.jpg", Constants.FABRICS_BASE_URL+"penelope/fabric_penelope_color_05.jpg","Color 16008/05","Χρώμα 16008/05"));

        Fabric penelope=new Fabric(R.drawable.penelope,"PENELOPE",penelopeColors);

        ArrayList<String> penelopeCurtains=new ArrayList<>();
        penelopeCurtains.add(Constants.CURTAINS_BASE_URL+"penelope/curtain_penelope_color_01.png");
        penelopeCurtains.add(Constants.CURTAINS_BASE_URL+"penelope/curtain_penelope_color_02.png");
        penelopeCurtains.add(Constants.CURTAINS_BASE_URL+"penelope/curtain_penelope_color_03.png");
        penelopeCurtains.add(Constants.CURTAINS_BASE_URL+"penelope/curtain_penelope_color_04.png");
        penelopeCurtains.add(Constants.CURTAINS_BASE_URL+"penelope/curtain_penelope_color_05.png");

        penelope.setCurtains(penelopeCurtains);


        ArrayList<FabricColor> silvieColors=new ArrayList<>();
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_01.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_01.jpg","Color 28002/01","Χρώμα 28002/01"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_02.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_02.jpg","Color 28002/02","Χρώμα 28002/02"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_03.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_03.jpg","Color 28002/03","Χρώμα 28002/03"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_04.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_04.jpg","Color 28002/04","Χρώμα 28002/04"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_05.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_05.jpg","Color 28002/05","Χρώμα 28002/05"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_06.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_06.jpg","Color 28002/06","Χρώμα 28002/06"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_07.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_07.jpg","Color 28002/07","Χρώμα 28002/07"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_08.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_08.jpg","Color 28002/08","Χρώμα 28002/08"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_09.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_09.jpg","Color 28002/09","Χρώμα 28002/09"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_10.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_10.jpg","Color 28002/10","Χρώμα 28002/10"));
        silvieColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_silvie_color_11.jpg", Constants.FABRICS_BASE_URL+"silvie/fabric_silvie_color_11.jpg","Color 28002/11","Χρώμα 28002/11"));



        Fabric silvie=new Fabric(R.drawable.silvie,"SILVIE",silvieColors);

        ArrayList<String> silvieCurtains=new ArrayList<>();
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_01.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_02.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_03.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_04.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_05.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_06.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_07.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_08.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_09.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_10.png");
        silvieCurtains.add(Constants.CURTAINS_BASE_URL+"silvie/curtain_silvie_color_11.png");


        silvie.setCurtains(silvieCurtains);


        ArrayList<FabricColor> tammyColors=new ArrayList<>();
        tammyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tammy_color_01.jpg", Constants.FABRICS_BASE_URL+"tammy/fabric_tammy_color_01.jpg","Color 18004/01","Χρώμα 18004/01"));
        tammyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tammy_color_02.jpg", Constants.FABRICS_BASE_URL+"tammy/fabric_tammy_color_02.jpg","Color 18004/02","Χρώμα 18004/02"));
        tammyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tammy_color_03.jpg", Constants.FABRICS_BASE_URL+"tammy/fabric_tammy_color_03.jpg","Color 18004/03","Χρώμα 18004/03"));
        tammyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tammy_color_04.jpg", Constants.FABRICS_BASE_URL+"tammy/fabric_tammy_color_04.jpg","Color 18004/04","Χρώμα 18004/04"));
        tammyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_tammy_color_05.jpg", Constants.FABRICS_BASE_URL+"tammy/fabric_tammy_color_05.jpg","Color 18004/05","Χρώμα 18004/05"));

        Fabric tammy=new Fabric(R.drawable.tammy,"TAMMY",tammyColors);

        ArrayList<String> tammyCurtains=new ArrayList<>();
        tammyCurtains.add(Constants.CURTAINS_BASE_URL+"tammy/curtain_tammy_color_01.png");
        tammyCurtains.add(Constants.CURTAINS_BASE_URL+"tammy/curtain_tammy_color_02.png");
        tammyCurtains.add(Constants.CURTAINS_BASE_URL+"tammy/curtain_tammy_color_03.png");
        tammyCurtains.add(Constants.CURTAINS_BASE_URL+"tammy/curtain_tammy_color_04.png");
        tammyCurtains.add(Constants.CURTAINS_BASE_URL+"tammy/curtain_tammy_color_05.png");

        tammy.setCurtains(tammyCurtains);


        ArrayList<FabricColor> yolandaColors=new ArrayList<>();
        yolandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_yolanda_color_01.jpg", Constants.FABRICS_BASE_URL+"yolanda/fabric_yolanda_color_01.jpg","Color 30002/01","Χρώμα 30002/01"));
        yolandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_yolanda_color_02.jpg", Constants.FABRICS_BASE_URL+"yolanda/fabric_yolanda_color_02.jpg","Color 30002/02","Χρώμα 30002/02"));
        yolandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_yolanda_color_03.jpg", Constants.FABRICS_BASE_URL+"yolanda/fabric_yolanda_color_03.jpg","Color 30002/03","Χρώμα 30002/03"));
        yolandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_yolanda_color_04.jpg", Constants.FABRICS_BASE_URL+"yolanda/fabric_yolanda_color_04.jpg","Color 30002/04","Χρώμα 30002/04"));
        yolandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_yolanda_color_05.jpg", Constants.FABRICS_BASE_URL+"yolanda/fabric_yolanda_color_05.jpg","Color 30002/05","Χρώμα 30002/05"));
        yolandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_yolanda_color_06.jpg", Constants.FABRICS_BASE_URL+"yolanda/fabric_yolanda_color_06.jpg","Color 30002/06","Χρώμα 30002/06"));

        Fabric yolanda=new Fabric(R.drawable.yolanda,"YOLANDA",yolandaColors);

        ArrayList<String> yolandaCurtains=new ArrayList<>();
        yolandaCurtains.add(Constants.CURTAINS_BASE_URL+"yolanda/curtain_yolanda_color_01.png");
        yolandaCurtains.add(Constants.CURTAINS_BASE_URL+"yolanda/curtain_yolanda_color_02.png");
        yolandaCurtains.add(Constants.CURTAINS_BASE_URL+"yolanda/curtain_yolanda_color_03.png");
        yolandaCurtains.add(Constants.CURTAINS_BASE_URL+"yolanda/curtain_yolanda_color_04.png");
        yolandaCurtains.add(Constants.CURTAINS_BASE_URL+"yolanda/curtain_yolanda_color_05.png");
        yolandaCurtains.add(Constants.CURTAINS_BASE_URL+"yolanda/curtain_yolanda_color_06.png");

        yolanda.setCurtains(yolandaCurtains);


        ArrayList<FabricColor> angelinaColors=new ArrayList<>();
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_01.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_01.jpg","Color 12012/01","Χρώμα 12012/01"));
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_02.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_02.jpg","Color 12012/02","Χρώμα 12012/02"));
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_03.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_03.jpg","Color 12012/03","Χρώμα 12012/03"));
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_04.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_04.jpg","Color 12012/04","Χρώμα 12012/04"));
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_05.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_05.jpg","Color 12012/05","Χρώμα 12012/05"));
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_07.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_07.jpg","Color 12012/07","Χρώμα 12012/07"));
        angelinaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_angelina_color_08.jpg", Constants.FABRICS_BASE_URL+"angelina/fabric_angelina_color_08.jpg","Color 12012/08","Χρώμα 12012/08"));





        Fabric angelina=new Fabric(R.drawable.angelina,"ANGELINA",angelinaColors);

        ArrayList<String> angelinaCurtains=new ArrayList<>();
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_01.png");
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_02.png");
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_03.png");
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_04.png");
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_05.png");
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_07.png");
        angelinaCurtains.add(Constants.CURTAINS_BASE_URL+"angelina/curtain_angelina_color_08.png");


        angelina.setCurtains(angelinaCurtains);


        ArrayList<FabricColor> carlaColors=new ArrayList<>();
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_01.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_01.jpg","Color 13011/01","Χρώμα 13011/01"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_02.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_02.jpg","Color 13011/02","Χρώμα 13011/02"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_03.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_03.jpg","Color 13011/03","Χρώμα 13011/03"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_04.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_04.jpg","Color 13011/04","Χρώμα 13011/04"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_05.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_05.jpg","Color 13011/05","Χρώμα 13011/05"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_06.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_06.jpg","Color 13011/06","Χρώμα 13011/06"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_07.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_07.jpg","Color 13011/07","Χρώμα 13011/07"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_08.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_08.jpg","Color 13011/08","Χρώμα 13011/08"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_09.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_09.jpg","Color 13011/09","Χρώμα 13011/09"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_10.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_10.jpg","Color 13011/10","Χρώμα 13011/10"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_11.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_11.jpg","Color 13011/11","Χρώμα 13011/11"));
        carlaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_carla_color_12.jpg", Constants.FABRICS_BASE_URL+"carla/fabric_carla_color_12.jpg","Color 13011/12","Χρώμα 13011/12"));

        Fabric carla=new Fabric(R.drawable.carla,"CARLA",carlaColors);

        ArrayList<String> carlaCurtains=new ArrayList<>();
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_01.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_02.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_03.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_04.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_05.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_06.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_07.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_08.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_09.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_10.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_11.png");
        carlaCurtains.add(Constants.CURTAINS_BASE_URL+"carla/curtain_carla_color_12.png");

        carla.setCurtains(carlaCurtains);

        ArrayList<FabricColor> daphneColors=new ArrayList<>();
        daphneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daphne_color_01.jpg", Constants.FABRICS_BASE_URL+"daphne/fabric_daphne_color_01.jpg","Color 15006/01","Χρώμα 15006/01"));
        daphneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daphne_color_02.jpg", Constants.FABRICS_BASE_URL+"daphne/fabric_daphne_color_02.jpg","Color 15006/02","Χρώμα 15006/02"));
        daphneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daphne_color_03.jpg", Constants.FABRICS_BASE_URL+"daphne/fabric_daphne_color_03.jpg","Color 15006/03","Χρώμα 15006/03"));
        daphneColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daphne_color_04.jpg", Constants.FABRICS_BASE_URL+"daphne/fabric_daphne_color_04.jpg","Color 15006/04","Χρώμα 15006/04"));

        
        Fabric daphne=new Fabric(R.drawable.daphne,"DAPHNE",daphneColors);

        ArrayList<String> daphneCurtains=new ArrayList<>();
        daphneCurtains.add(Constants.CURTAINS_BASE_URL+"daphne/curtain_daphne_color_01.png");
        daphneCurtains.add(Constants.CURTAINS_BASE_URL+"daphne/curtain_daphne_color_02.png");
        daphneCurtains.add(Constants.CURTAINS_BASE_URL+"daphne/curtain_daphne_color_03.png");
        daphneCurtains.add(Constants.CURTAINS_BASE_URL+"daphne/curtain_daphne_color_04.png");

        daphne.setCurtains(daphneCurtains);


        ArrayList<FabricColor> joyColors=new ArrayList<>();
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_01.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_01.jpg","Color 16004/01","Χρώμα 16004/01"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_02.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_02.jpg","Color 16004/02","Χρώμα 16004/02"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_03.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_03.jpg","Color 16004/03","Χρώμα 16004/03"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_04.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_04.jpg","Color 16004/04","Χρώμα 16004/04"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_05.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_05.jpg","Color 16004/05","Χρώμα 16004/05"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_06.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_06.jpg","Color 16004/06","Χρώμα 16004/06"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_07.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_07.jpg","Color 16004/07","Χρώμα 16004/07"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_08.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_08.jpg","Color 16004/08","Χρώμα 16004/08"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_09.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_09.jpg","Color 16004/09","Χρώμα 16004/09"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_10.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_10.jpg","Color 16004/10","Χρώμα 16004/10"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_11.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_11.jpg","Color 16004/11","Χρώμα 16004/11"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_12.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_12.jpg","Color 16004/12","Χρώμα 16004/12"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_13.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_13.jpg","Color 16004/13","Χρώμα 16004/13"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_14.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_14.jpg","Color 16004/14","Χρώμα 16004/14"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_15.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_15.jpg","Color 16004/15","Χρώμα 16004/15"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_16.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_16.jpg","Color 16004/16","Χρώμα 16004/16"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_17.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_17.jpg","Color 16004/17","Χρώμα 16004/17"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_18.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_18.jpg","Color 16004/18","Χρώμα 16004/18"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_19.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_19.jpg","Color 16004/19","Χρώμα 16004/19"));
        joyColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_joy_color_20.jpg", Constants.FABRICS_BASE_URL+"joy/fabric_joy_color_20.jpg","Color 16004/20","Χρώμα 16004/20"));

        Fabric joy=new Fabric(R.drawable.joy,"JOY",joyColors);

        ArrayList<String> joyCurtains=new ArrayList<>();
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_01.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_02.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_03.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_04.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_05.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_06.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_07.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_08.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_09.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_10.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_11.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_12.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_13.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_14.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_15.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_16.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_17.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_18.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_19.png");
        joyCurtains.add(Constants.CURTAINS_BASE_URL+"joy/curtain_joy_color_20.png");

        joy.setCurtains(joyCurtains);


        ArrayList<FabricColor> rosemaryColors=new ArrayList<>();
        rosemaryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_rosemary_color_01.jpg", Constants.FABRICS_BASE_URL+"rosemary/fabric_rosemary_color_01.jpg","Color 16003/01","Χρώμα 16003/01"));
        rosemaryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_rosemary_color_02.jpg", Constants.FABRICS_BASE_URL+"rosemary/fabric_rosemary_color_02.jpg","Color 16003/02","Χρώμα 16003/02"));
        rosemaryColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_rosemary_color_03.jpg", Constants.FABRICS_BASE_URL+"rosemary/fabric_rosemary_color_03.jpg","Color 16003/03","Χρώμα 16003/03"));


        Fabric rosemary=new Fabric(R.drawable.rosemary,"ROSEMARY",rosemaryColors);

        ArrayList<String> rosemaryCurtains=new ArrayList<>();
        rosemaryCurtains.add(Constants.CURTAINS_BASE_URL+"rosemary/curtain_rosemary_color_01.png");
        rosemaryCurtains.add(Constants.CURTAINS_BASE_URL+"rosemary/curtain_rosemary_color_02.png");
        rosemaryCurtains.add(Constants.CURTAINS_BASE_URL+"rosemary/curtain_rosemary_color_03.png");


        rosemary.setCurtains(rosemaryCurtains);


        ArrayList<FabricColor> ameliaColors=new ArrayList<>();
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_01.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_01.jpg","Color 11014/01","Χρώμα 11014/01"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_02.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_02.jpg","Color 11014/02","Χρώμα 11014/02"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_03.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_03.jpg","Color 11014/03","Χρώμα 11014/03"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_04.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_04.jpg","Color 11014/04","Χρώμα 11014/04"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_05.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_05.jpg","Color 11014/05","Χρώμα 11014/05"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_06.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_06.jpg","Color 11014/06","Χρώμα 11014/06"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_07.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_07.jpg","Color 11014/07","Χρώμα 11014/07"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_08.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_08.jpg","Color 11014/08","Χρώμα 11014/08"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_09.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_09.jpg","Color 11014/09","Χρώμα 11014/09"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_10.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_10.jpg","Color 11014/10","Χρώμα 11014/10"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_11.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_11.jpg","Color 11014/11","Χρώμα 11014/11"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_12.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_12.jpg","Color 11014/12","Χρώμα 11014/12"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_13.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_13.jpg","Color 11014/13","Χρώμα 11014/13"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_14.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_14.jpg","Color 11014/14","Χρώμα 11014/14"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_15.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_15.jpg","Color 11014/15","Χρώμα 11014/15"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_16.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_16.jpg","Color 11014/16","Χρώμα 11014/16"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_17.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_17.jpg","Color 11014/17","Χρώμα 11014/17"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_18.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_18.jpg","Color 11014/18","Χρώμα 11014/18"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_19.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_19.jpg","Color 11014/19","Χρώμα 11014/19"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_20.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_20.jpg","Color 11014/20","Χρώμα 11014/20"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_21.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_21.jpg","Color 11014/21","Χρώμα 11014/21"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_22.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_22.jpg","Color 11014/22","Χρώμα 11014/22"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_23.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_23.jpg","Color 11014/23","Χρώμα 11014/23"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_24.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_24.jpg","Color 11014/24","Χρώμα 11014/24"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_25.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_25.jpg","Color 11014/25","Χρώμα 11014/25"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_26.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_26.jpg","Color 11014/26","Χρώμα 11014/26"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_27.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_27.jpg","Color 11014/27","Χρώμα 11014/27"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_28.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_28.jpg","Color 11014/28","Χρώμα 11014/28"));
        ameliaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_amelia_color_29.jpg", Constants.FABRICS_BASE_URL+"amelia/fabric_amelia_color_29.jpg","Color 11014/29","Χρώμα 11014/29"));

        Fabric amelia=new Fabric(R.drawable.amelia,"AMELIA",ameliaColors);

        ArrayList<String> ameliaCurtains=new ArrayList<>();
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_01.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_02.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_03.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_04.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_05.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_06.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_07.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_08.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_09.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_10.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_11.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_12.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_13.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_14.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_15.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_16.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_17.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_18.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_19.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_20.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_21.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_22.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_23.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_24.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_25.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_26.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_27.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_28.png");
        ameliaCurtains.add(Constants.CURTAINS_BASE_URL+"amelia/curtain_amelia_color_29.png");

        amelia.setCurtains(ameliaCurtains);

        ArrayList<FabricColor> deboraColors=new ArrayList<>();
        deboraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_debora_color_01.jpg", Constants.FABRICS_BASE_URL+"debora/fabric_debora_color_01.jpg","Color 11015/01","Χρώμα 11015/01"));
        deboraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_debora_color_02.jpg", Constants.FABRICS_BASE_URL+"debora/fabric_debora_color_02.jpg","Color 11015/02","Χρώμα 11015/02"));
        deboraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_debora_color_03.jpg", Constants.FABRICS_BASE_URL+"debora/fabric_debora_color_03.jpg","Color 11015/03","Χρώμα 11015/03"));
        deboraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_debora_color_04.jpg", Constants.FABRICS_BASE_URL+"debora/fabric_debora_color_04.jpg","Color 11015/04","Χρώμα 11015/04"));
        deboraColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_debora_color_05.jpg", Constants.FABRICS_BASE_URL+"debora/fabric_debora_color_05.jpg","Color 11015/05","Χρώμα 11015/05"));

        Fabric debora=new Fabric(R.drawable.debora,"DEBORA",deboraColors);

        ArrayList<String> deboraCurtains=new ArrayList<>();
        deboraCurtains.add(Constants.CURTAINS_BASE_URL+"debora/curtain_debora_color_01.png");
        deboraCurtains.add(Constants.CURTAINS_BASE_URL+"debora/curtain_debora_color_02.png");
        deboraCurtains.add(Constants.CURTAINS_BASE_URL+"debora/curtain_debora_color_03.png");
        deboraCurtains.add(Constants.CURTAINS_BASE_URL+"debora/curtain_debora_color_04.png");
        deboraCurtains.add(Constants.CURTAINS_BASE_URL+"debora/curtain_debora_color_05.png");


        debora.setCurtains(deboraCurtains);


        ArrayList<FabricColor> mirandaColors=new ArrayList<>();
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_01.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_01.jpg","Color 20011/01","Χρώμα 20011/01"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_02.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_02.jpg","Color 20011/02","Χρώμα 20011/02"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_03.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_03.jpg","Color 20011/03","Χρώμα 20011/03"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_04.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_04.jpg","Color 20011/04","Χρώμα 20011/04"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_05.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_05.jpg","Color 20011/05","Χρώμα 20011/05"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_06.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_06.jpg","Color 20011/06","Χρώμα 20011/06"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_07.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_07.jpg","Color 20011/07","Χρώμα 20011/07"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_08.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_08.jpg","Color 20011/08","Χρώμα 20011/08"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_09.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_09.jpg","Color 20011/09","Χρώμα 20011/09"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_10.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_10.jpg","Color 20011/10","Χρώμα 20011/10"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_11.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_11.jpg","Color 20011/11","Χρώμα 20011/11"));
        mirandaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_miranda_color_12.jpg", Constants.FABRICS_BASE_URL+"miranda/fabric_miranda_color_12.jpg","Color 20011/12","Χρώμα 20011/12"));

        Fabric miranda=new Fabric(R.drawable.miranda,"MIRANDA",mirandaColors);

        ArrayList<String> mirandaCurtains=new ArrayList<>();
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_01.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_02.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_03.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_04.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_05.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_06.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_07.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_08.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_09.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_10.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_11.png");
        mirandaCurtains.add(Constants.CURTAINS_BASE_URL+"miranda/curtain_miranda_color_12.png");


        miranda.setCurtains(mirandaCurtains);

        ArrayList<FabricColor> helenaColors=new ArrayList<>();
        helenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_helena_color_01.jpg", Constants.FABRICS_BASE_URL+"helena/fabric_helena_color_01.jpg","Color 29002/01","Χρώμα 29002/01"));
        helenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_helena_color_02.jpg", Constants.FABRICS_BASE_URL+"helena/fabric_helena_color_02.jpg","Color 29002/02","Χρώμα 29002/02"));
        helenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_helena_color_03.jpg", Constants.FABRICS_BASE_URL+"helena/fabric_helena_color_03.jpg","Color 29002/03","Χρώμα 29002/03"));
        helenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_helena_color_06.jpg", Constants.FABRICS_BASE_URL+"helena/fabric_helena_color_06.jpg","Color 29002/06","Χρώμα 29002/06"));
        helenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_helena_color_07.jpg", Constants.FABRICS_BASE_URL+"helena/fabric_helena_color_07.jpg","Color 29002/07","Χρώμα 29002/07"));

        Fabric helena=new Fabric(R.drawable.helena,"HELENA",helenaColors);

        ArrayList<String> helenaCurtains=new ArrayList<>();
        helenaCurtains.add(Constants.CURTAINS_BASE_URL+"helena/curtain_helena_color_01.png");
        helenaCurtains.add(Constants.CURTAINS_BASE_URL+"helena/curtain_helena_color_02.png");
        helenaCurtains.add(Constants.CURTAINS_BASE_URL+"helena/curtain_helena_color_03.png");
        helenaCurtains.add(Constants.CURTAINS_BASE_URL+"helena/curtain_helena_color_06.png");
        helenaCurtains.add(Constants.CURTAINS_BASE_URL+"helena/curtain_helena_color_07.png");

        helena.setCurtains(helenaCurtains);


        ArrayList<FabricColor> lorenaColors=new ArrayList<>();
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_01.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_01.jpg","Color 29000/01","Χρώμα 29000/01"));
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_02.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_02.jpg","Color 29000/02","Χρώμα 29000/02"));
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_03.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_03.jpg","Color 29000/03","Χρώμα 29000/03"));
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_04.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_04.jpg","Color 29000/04","Χρώμα 29000/04"));
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_05.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_05.jpg","Color 29000/05","Χρώμα 29000/05"));
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_06.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_06.jpg","Color 29000/06","Χρώμα 29000/06"));
        lorenaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_lorena_color_07.jpg", Constants.FABRICS_BASE_URL+"lorena/fabric_lorena_color_07.jpg","Color 29000/07","Χρώμα 29000/07"));

        Fabric lorena=new Fabric(R.drawable.lorena,"LORENA",lorenaColors);

        ArrayList<String> lorenaCurtains=new ArrayList<>();
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_01.png");
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_02.png");
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_03.png");
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_04.png");
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_05.png");
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_06.png");
        lorenaCurtains.add(Constants.CURTAINS_BASE_URL+"lorena/curtain_lorena_color_07.png");



        lorena.setCurtains(lorenaCurtains);

        ArrayList<FabricColor> monaColors=new ArrayList<>();
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_01.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_01.jpg","Color 29001/01","Χρώμα 29001/01"));
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_02.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_02.jpg","Color 29001/02","Χρώμα 29001/02"));
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_03.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_03.jpg","Color 29001/03","Χρώμα 29001/03"));
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_04.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_04.jpg","Color 29001/04","Χρώμα 29001/04"));
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_05.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_05.jpg","Color 29001/05","Χρώμα 29001/05"));
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_06.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_06.jpg","Color 29001/06","Χρώμα 29001/06"));
        monaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_mona_color_07.jpg", Constants.FABRICS_BASE_URL+"mona/fabric_mona_color_07.jpg","Color 29001/07","Χρώμα 29001/07"));

        Fabric mona=new Fabric(R.drawable.mona,"MONA",monaColors);

        ArrayList<String> monaCurtains=new ArrayList<>();
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_01.png");
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_02.png");
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_03.png");
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_04.png");
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_05.png");
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_06.png");
        monaCurtains.add(Constants.CURTAINS_BASE_URL+"mona/curtain_mona_color_07.png");
        
        mona.setCurtains(monaCurtains);

        ArrayList<FabricColor> danielaColors=new ArrayList<>();
        danielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daniela_color_01.jpg", Constants.FABRICS_BASE_URL+"daniela/fabric_daniela_color_01.jpg","Color 22004/01","Χρώμα 22004/01"));
        danielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daniela_color_02.jpg", Constants.FABRICS_BASE_URL+"daniela/fabric_daniela_color_02.jpg","Color 22004/02","Χρώμα 22004/02"));
        danielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daniela_color_03.jpg", Constants.FABRICS_BASE_URL+"daniela/fabric_daniela_color_03.jpg","Color 22004/03","Χρώμα 22004/03"));
        danielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daniela_color_04.jpg", Constants.FABRICS_BASE_URL+"daniela/fabric_daniela_color_04.jpg","Color 22004/04","Χρώμα 22004/04"));
        danielaColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_daniela_color_05.jpg", Constants.FABRICS_BASE_URL+"daniela/fabric_daniela_color_05.jpg","Color 22004/05","Χρώμα 22004/05"));

        Fabric daniela=new Fabric(R.drawable.daniela,"DANIELA",danielaColors);

        ArrayList<String> danielaCurtains=new ArrayList<>();
        danielaCurtains.add(Constants.CURTAINS_BASE_URL+"daniela/curtain_daniela_color_01.png");
        danielaCurtains.add(Constants.CURTAINS_BASE_URL+"daniela/curtain_daniela_color_02.png");
        danielaCurtains.add(Constants.CURTAINS_BASE_URL+"daniela/curtain_daniela_color_03.png");
        danielaCurtains.add(Constants.CURTAINS_BASE_URL+"daniela/curtain_daniela_color_04.png");
        danielaCurtains.add(Constants.CURTAINS_BASE_URL+"daniela/curtain_daniela_color_05.png");
        
        daniela.setCurtains(danielaCurtains);

        ArrayList<FabricColor> doloresColors=new ArrayList<>();
        doloresColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dolores_color_01.jpg", Constants.FABRICS_BASE_URL+"dolores/fabric_dolores_color_01.jpg","Color 1510/01","Χρώμα 1510/01"));
        doloresColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dolores_color_02.jpg", Constants.FABRICS_BASE_URL+"dolores/fabric_dolores_color_02.jpg","Color 1510/02","Χρώμα 1510/02"));
        doloresColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dolores_color_03.jpg", Constants.FABRICS_BASE_URL+"dolores/fabric_dolores_color_03.jpg","Color 1510/03","Χρώμα 1510/03"));
        doloresColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dolores_color_04.jpg", Constants.FABRICS_BASE_URL+"dolores/fabric_dolores_color_04.jpg","Color 1510/04","Χρώμα 1510/04"));
        doloresColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dolores_color_05.jpg", Constants.FABRICS_BASE_URL+"dolores/fabric_dolores_color_05.jpg","Color 1510/05","Χρώμα 1510/05"));
        doloresColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL+"thumbnail_fabric_dolores_color_06.jpg", Constants.FABRICS_BASE_URL+"dolores/fabric_dolores_color_06.jpg","Color 1510/06","Χρώμα 1510/06"));

        Fabric dolores=new Fabric(R.drawable.dolores,"DOLORES",doloresColors);

        ArrayList<String> doloresCurtains=new ArrayList<>();
        doloresCurtains.add(Constants.CURTAINS_BASE_URL+"dolores/curtain_dolores_color_01.png");
        doloresCurtains.add(Constants.CURTAINS_BASE_URL+"dolores/curtain_dolores_color_02.png");
        doloresCurtains.add(Constants.CURTAINS_BASE_URL+"dolores/curtain_dolores_color_03.png");
        doloresCurtains.add(Constants.CURTAINS_BASE_URL+"dolores/curtain_dolores_color_04.png");
        doloresCurtains.add(Constants.CURTAINS_BASE_URL+"dolores/curtain_dolores_color_05.png");
        doloresCurtains.add(Constants.CURTAINS_BASE_URL+"dolores/curtain_dolores_color_06.png");

        dolores.setCurtains(doloresCurtains);



        indoorFabrics.add(atlantis);
        indoorFabrics.add(belvedere);
        indoorFabrics.add(calm);
        indoorFabrics.add(element);
        indoorFabrics.add(elysse);
        indoorFabrics.add(famous);
        indoorFabrics.add(gift);
        indoorFabrics.add(illusion);
        indoorFabrics.add(local);
        indoorFabrics.add(majestic);
        indoorFabrics.add(memory);
        indoorFabrics.add(orlando);
        indoorFabrics.add(sense);
        indoorFabrics.add(vienna);
        indoorFabrics.add(cuervo);
        indoorFabrics.add(macao);
        indoorFabrics.add(tenerife);
        indoorFabrics.add(utopia);
        indoorFabrics.add(theron);
        indoorFabrics.add(cantu);

        allFabrics.add(atlantis);
        allFabrics.add(belvedere);
        allFabrics.add(calm);
        allFabrics.add(element);
        allFabrics.add(elysse);
        allFabrics.add(famous);
        allFabrics.add(gift);
        allFabrics.add(illusion);
        allFabrics.add(local);
        allFabrics.add(majestic);
        allFabrics.add(memory);
        allFabrics.add(orlando);
        allFabrics.add(sense);
        allFabrics.add(vienna);
        allFabrics.add(aegean);
        allFabrics.add(ocean);



        outdoorFabrics.add(aegean);
        outdoorFabrics.add(ocean);

        outdoorTemp.add(aegean);
        outdoorTemp.add(ocean);
        outdoorTemp.add(tenerife);
        outdoorTemp.add(utopia);
        outdoorTemp.add(macao);
        outdoorTemp.add(cuervo);



        curtainFabrics.add(monika);
        curtainFabrics.add(afroditi);
        curtainFabrics.add(antoinette);
        curtainFabrics.add(barbara);
        curtainFabrics.add(brigitte);
        curtainFabrics.add(electra);
        curtainFabrics.add(elisabeth);
        curtainFabrics.add(emily);
        curtainFabrics.add(estella);
        curtainFabrics.add(eugenia);
        curtainFabrics.add(felicia);
        curtainFabrics.add(iris);
        curtainFabrics.add(kassandra);
        curtainFabrics.add(laura);
        curtainFabrics.add(louisa);
        curtainFabrics.add(maya);
        curtainFabrics.add(melina);
        curtainFabrics.add(patricia);
        curtainFabrics.add(victoria);
        curtainFabrics.add(vivian);
        curtainFabrics.add(andrianna);
        curtainFabrics.add(beatrice);
        curtainFabrics.add(dalia);
        curtainFabrics.add(franceska);
        curtainFabrics.add(inkas);
        curtainFabrics.add(marietta);
        curtainFabrics.add(nefeli);
        curtainFabrics.add(roxanne);
        curtainFabrics.add(element);
        curtainFabrics.add(adele);
        curtainFabrics.add(alkistis);
        curtainFabrics.add(ariadni);
        curtainFabrics.add(ariel);
        curtainFabrics.add(claudia);
        curtainFabrics.add(gabriela);
        curtainFabrics.add(giovanna);
        curtainFabrics.add(matilda);
        curtainFabrics.add(ornela);
        curtainFabrics.add(sandy);
        curtainFabrics.add(alice);
        curtainFabrics.add(anna);
        curtainFabrics.add(armonia);
        curtainFabrics.add(athena);
        curtainFabrics.add(eleonora);
        curtainFabrics.add(erika);
        curtainFabrics.add(flora);
        curtainFabrics.add(irina);
        curtainFabrics.add(isabella);
        curtainFabrics.add(juliet);
        curtainFabrics.add(korina);
        curtainFabrics.add(kristina);
        curtainFabrics.add(linda);
        curtainFabrics.add(melanie);
        curtainFabrics.add(nafsika);
        curtainFabrics.add(nora);
        curtainFabrics.add(zizel);
        curtainFabrics.add(brenda);
        curtainFabrics.add(dorothy);
        curtainFabrics.add(erofili);
        curtainFabrics.add(jennifer);
        curtainFabrics.add(joanna);
        curtainFabrics.add(charlize);
        curtainFabrics.add(kelly);
        curtainFabrics.add(lora);
        curtainFabrics.add(lucia);
        curtainFabrics.add(nataly);
        curtainFabrics.add(penelope);
        curtainFabrics.add(silvie);
        curtainFabrics.add(tammy);
        curtainFabrics.add(yolanda);
        curtainFabrics.add(angelina);
        curtainFabrics.add(carla);
        curtainFabrics.add(daphne);
        curtainFabrics.add(joy);
        curtainFabrics.add(rosemary);
        curtainFabrics.add(amelia);
        curtainFabrics.add(debora);
        curtainFabrics.add(miranda);
        curtainFabrics.add(helena);
        curtainFabrics.add(lorena);
        curtainFabrics.add(mona);
        curtainFabrics.add(daniela);
        curtainFabrics.add(dolores);




        fabricTypes=new ArrayList<>();
        FabricType indoorFabricType=new FabricType();
        indoorFabricType.setFabrics(indoorFabrics);
        indoorFabricType.setName(getString(R.string.indoor));
        indoorFabricType.setImgId(R.drawable.indoors);

        FabricType outdoorFabricType=new FabricType();
        outdoorFabricType.setFabrics(outdoorTemp);
        outdoorFabricType.setName(getString(outdoor));
        outdoorFabricType.setImgId(R.drawable.outdoors);

        FabricType curtainFabricType=new FabricType();
        curtainFabricType.setFabrics(curtainFabrics);
        curtainFabricType.setName(getString(R.string.curtains_lower));
        curtainFabricType.setImgId(R.drawable.curtains);

        fabricTypes.add(indoorFabricType);
        fabricTypes.add(outdoorFabricType);
        fabricTypes.add(curtainFabricType);

    }


}
