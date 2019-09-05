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
import utilities.FabricsSingleton;

import static creativedays.com.dilzas.R.string.outdoor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
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

    ArrayList<Fabric> indoorFabrics;
    ArrayList<Fabric> outdoorFabrics;
    ArrayList<Fabric> outdoorTemp;
    ArrayList<Fabric> allFabrics;
    ArrayList<Fabric> curtainFabrics;

    ArrayList<NewsObject> news;

    ArrayList<FabricType> fabricTypes;


    FrameLayout splash;
    ImageView blackLogo;
    ImageView redLogo;
    ImageView removeSplash;

    ProgressBar splashProgress;

    Typeface bookAntiqua;

    String initJson = "";

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        bookAntiqua = Typeface.createFromAsset(getAssets(), "fonts/bkant.ttf");
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        ken = findViewById(R.id.ken);
        newsTxt = findViewById(R.id.news);
        fabricsTxt = findViewById(R.id.fabrics);
        coversTxt = findViewById(R.id.covers);
        sofaCreatorTxt = findViewById(R.id.sofa_creator);
        sunbedsTxt = findViewById(R.id.sunbeds);
        curtainsTxt = findViewById(R.id.curtains);
        social = findViewById(R.id.social_media);

        camera1 = findViewById(R.id.camera1);
        camera2 = findViewById(R.id.camera2);
        camera3 = findViewById(R.id.camera3);
        camera4 = findViewById(R.id.camera4);

        newsTxt.setTypeface(bookAntiqua);
        fabricsTxt.setTypeface(bookAntiqua);
        coversTxt.setTypeface(bookAntiqua);
        sunbedsTxt.setTypeface(bookAntiqua);
        sofaCreatorTxt.setTypeface(bookAntiqua);
        curtainsTxt.setTypeface(bookAntiqua);
        social.setTypeface(bookAntiqua);


        splash = findViewById(R.id.splash);
        splashProgress = findViewById(R.id.splash_progress);
        blackLogo = findViewById(R.id.black);
        redLogo = findViewById(R.id.red);
        removeSplash = findViewById(R.id.remove_splash);

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

    public void clearCache()
    {
        if (!prefs.getBoolean("clear1", false))
        {
            prefs.edit().putBoolean("clear1", true).commit();
            AsyncTask.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    Glide.get(MainActivity.this).clearDiskCache();
                }
            });
        }

    }


    public void getInitJson()
    {
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

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
                final String cachedResponse = prefs.getString("json", "");
                initJson = cachedResponse;

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!cachedResponse.equals(""))
                        {
                            parseData(cachedResponse);
                        }
                        else
                        {

                           //Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
                            //finish();
                            showContinue();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseData = response.body().string();
                responseData.replace("\"" + "gr" + "\"", "\"" + "el" + "\"");
                if (!response.isSuccessful())
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
                            //finish();
                            showContinue();
                        }
                    });

                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                initJson = responseData;
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("json", responseData);
                                editor.commit();
                                parseData(responseData);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });
    }

    public void fadeCameras()
    {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(2000);
        camera1.startAnimation(fadeIn);
        camera2.startAnimation(fadeIn);
        camera3.startAnimation(fadeIn);
        camera4.startAnimation(fadeIn);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        fadeCameras();
    }

    public void parseData(String data)
    {

        try
        {
            JSONObject root = new JSONObject(data);
            JSONArray news = root.getJSONArray("articles");

            parseNews(news);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        showContinue();
    }

    public void parseNews(JSONArray newsArray)
    {
        news = new ArrayList<>();
        try
        {
            for (int i = 0; i < newsArray.length(); i++)
            {
                JSONObject item = newsArray.getJSONObject(i);
                NewsObject newsObject = new NewsObject();
                newsObject.setText(item.getString("text"));
                newsObject.setTitle(item.getString("title"));
                newsObject.setDate(item.getString("updated_at"));
                newsObject.setImgURL(item.getJSONObject("image").getString("url"));

                news.add(newsObject);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void startSplashAnimation()
    {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1500);
        blackLogo.setVisibility(View.VISIBLE);
        fadeIn.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

                Animation fadeIn2 = new AlphaAnimation(0, 1);
                fadeIn2.setDuration(2000);
                redLogo.setVisibility(View.VISIBLE);
                fadeIn2.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        redLogo.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {

                    }
                });
                redLogo.startAnimation(fadeIn2);

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        blackLogo.startAnimation(fadeIn);
    }

    public void showContinue()
    {
        splashProgress.setVisibility(View.GONE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(3000);
        removeSplash.startAnimation(fadeIn);
        removeSplash.setVisibility(View.VISIBLE);
        removeSplash.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setDuration(500);
                splash.startAnimation(fadeOut);
                splash.setVisibility(View.GONE);
                fadeCameras();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        Intent intent;
        switch (id)
        {
            case R.id.news:
                if (!initJson.equals(""))
                {
                    if (news.size() > 0)
                    {
                        intent = new Intent(this, NewsActivity.class);
                        intent.putExtra("news", news);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, getString(R.string.no_news), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, getString(R.string.no_news), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fabrics:
                FabricTypeActivity.fabricTypes = fabricTypes;
                intent = new Intent(this, FabricTypeActivity.class);
                startActivity(intent);
                break;
            case R.id.covers:
                intent = new Intent(this, CouchesActivity.class);
                startActivity(intent);
                break;
            case R.id.sofa_creator:
                intent = new Intent(this, SofaCreatorActivity.class);
                startActivity(intent);
                break;
            case R.id.sunbeds:
                intent = new Intent(this, CouchesActivity.class);
                startActivity(intent);
                break;
            case R.id.curtains:
                intent = new Intent(this, CurtainsActivity.class);
                startActivity(intent);
                break;
            case R.id.social_media:
                intent = new Intent(this, SocialMediaActivity.class);
                startActivity(intent);
                break;
        }
    }


    public void initData()
    {
        indoorFabrics = new ArrayList<>();
        outdoorFabrics = new ArrayList<>();
        outdoorTemp = new ArrayList<>();
        allFabrics = new ArrayList<>();
        curtainFabrics = new ArrayList<>();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(Constants.CONTENT_JSON_URL)
                .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseData = response.body().string();
                try
                {
                    JSONObject jsonRoot = new JSONObject(responseData);
                    JSONArray json_couches = jsonRoot.getJSONArray("couches");
                    JSONArray json_curtains = jsonRoot.getJSONArray("curtains");
                    JSONArray json_sunbeds = jsonRoot.getJSONArray("sunbeds");

                    for (int i = 0; i < json_couches.length(); i++)
                    {
                        JSONObject tmp = json_couches.getJSONObject(i);
                        String fabric_code = "";
                        if (tmp.has("fabric_code"))
                        {
                            fabric_code = tmp.getString("fabric_code") + "/";
                        }

                        Fabric e = loadCouch(tmp.getString("fabric_name"), fabric_code, tmp.getJSONArray("fabric_list"));
                        allFabrics.add(e);
                        indoorFabrics.add(e);
                    }
                    for (int i = 0; i < json_curtains.length(); i++)
                    {
                        JSONObject tmp = json_curtains.getJSONObject(i);

                        String fabric_code = "";
                        if (tmp.has("fabric_code"))
                        {
                            fabric_code = tmp.getString("fabric_code") + "/";
                        }

                        Fabric e = loadCurtain(tmp.getString("fabric_name"), fabric_code, tmp.getJSONArray("fabric_list"));
                        curtainFabrics.add(e);
                    }
                    for (int i = 0; i < json_sunbeds.length(); i++)
                    {
                        JSONObject tmp = json_sunbeds.getJSONObject(i);

                        String fabric_code = "";
                        if (tmp.has("fabric_code"))
                        {
                            fabric_code = tmp.getString("fabric_code") + "/";
                        }

                        Fabric e = loadSunbed(tmp.getString("fabric_name"), fabric_code, tmp.getJSONArray("fabric_list"));
                        outdoorFabrics.add(e);
                        outdoorTemp.add(e);
                    }

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                fabricTypes = new ArrayList<>();
                FabricType indoorFabricType = new FabricType();
                //indoorFabricType.setFabrics(indoorFabrics);
                indoorFabricType.setName(getString(R.string.indoor));
                indoorFabricType.setImgId(R.drawable.indoors);

                FabricType outdoorFabricType = new FabricType();
                //outdoorFabricType.setFabrics(outdoorTemp);
                outdoorFabricType.setName(getString(outdoor));
                outdoorFabricType.setImgId(R.drawable.outdoors);

                FabricType curtainFabricType = new FabricType();
                //curtainFabricType.setFabrics(curtainFabrics);
                curtainFabricType.setName(getString(R.string.curtains_lower));
                curtainFabricType.setImgId(R.drawable.curtains);

                fabricTypes.add(indoorFabricType);
                fabricTypes.add(outdoorFabricType);
                fabricTypes.add(curtainFabricType);

                FabricsSingleton.instance.setIndoorFabrics(indoorFabrics);
                FabricsSingleton.instance.setOutdoorFabrics(outdoorFabrics);
                FabricsSingleton.instance.setCurtainFabrics(curtainFabrics);
            }
        });


    }

    public Fabric loadCurtain(String fbric, String code, JSONArray fabric_array) throws JSONException
    {
        ArrayList<FabricColor> fColors = new ArrayList<>();
        ArrayList<String> curtains = new ArrayList<>();

        for (int i = 0; i < fabric_array.length(); i++)
        {
            JSONObject tmp = fabric_array.getJSONObject(i);

            String filename = tmp.getString("fname");
            String fcolor = tmp.getString("color");

            String thumb = filename.replace("curtain", "thumbnail_fabric");
            String fabric = filename.replace("curtain", "fabric");

            fColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL + thumb + ".jpg", Constants.FABRICS_BASE_URL + fbric + "/" + fabric + ".jpg", "Color "+ code + fcolor, "Χρώμα " + code + fcolor));
            curtains.add(Constants.CURTAINS_BASE_URL + fbric + "/" + filename + ".png");
        }

       // int resourceId = getApplicationContext().getResources().getIdentifier(fbric, "drawable", MainActivity.this.getPackageName());
        Fabric fb = new Fabric(0, fbric.replace('_',' ').toUpperCase(), fColors);
        fb.setCurtains(curtains);

        return fb;
    }

    public Fabric loadSunbed(String fbric, String code, JSONArray fabric_array) throws JSONException
    {
        ArrayList<FabricColor> fColors = new ArrayList<>();
        ArrayList<String> sunbeds = new ArrayList<>();
        for (int i = 0; i < fabric_array.length(); i++)
        {
            JSONObject tmp = fabric_array.getJSONObject(i);
            String filename = tmp.getString("fname");
            String fcolor = tmp.getString("color");

            String thumb = filename.replace("sunbed", "thumbnail_fabric");
            String fabric = filename.replace("sunbed", "fabric");

            fColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL + thumb + ".jpg", Constants.FABRICS_BASE_URL + fbric + "/" + fabric + ".jpg", "Color " + code + fcolor, "Χρώμα " + code + fcolor));
            sunbeds.add(Constants.SUNBEDS_BASE_URL + fbric + "/" + filename + ".png");
        }

        //int resourceId = getApplicationContext().getResources().getIdentifier(fbric, "drawable", MainActivity.this.getPackageName());
        Fabric fb = new Fabric(0, fbric.replace('_',' ').toUpperCase(), fColors);

        fb.setCouches(sunbeds);

        return fb;
    }

    public Fabric loadCouch(String fbric, String code, JSONArray fabric_array) throws JSONException
    {

        ArrayList<String> couches = new ArrayList<>();
        ArrayList<String> couch_bodies = new ArrayList<>();
        ArrayList<String> couch_pillows = new ArrayList<>();
        ArrayList<FabricColor> fColors = new ArrayList<>();

        for (int i = 0; i < fabric_array.length(); i++)
        {
            JSONObject tmp = fabric_array.getJSONObject(i);
            String filename = tmp.getString("fname");
            String fcolor = tmp.getString("color");

            String thumb = filename.replace("couch", "thumbnail_fabric");
            String fabric = filename.replace("couch", "fabric");
            String couch_body = filename.replace("couch", "couch_body");
            String couch_pillow = filename.replace("couch", "couch_pillow");

            fColors.add(new FabricColor(Constants.THUMBNAILS_BASE_URL + thumb + ".jpg", Constants.FABRICS_BASE_URL + fbric + "/" + fabric + ".jpg", "Color " + code + fcolor, "Χρώμα " + code + fcolor));
            couches.add(Constants.COUCHES_BASE_URL + fbric + "/" + filename + ".png");
            couch_bodies.add(Constants.COUCHES_BODY_URL + fbric + "/" + couch_body + ".png");
            couch_pillows.add(Constants.COUCHES_PILLOWS_URL + fbric + "/" + couch_pillow + ".png");
        }

        //int resourceId = getApplicationContext().getResources().getIdentifier(fbric, "drawable", MainActivity.this.getPackageName());
        Fabric fb = new Fabric(0, fbric.replace('_',' ').toUpperCase(), fColors);
        fb.setCouches(couches);
        fb.setCouchBodys(couch_bodies);
        fb.setCouchPillows(couch_pillows);

        return fb;
    }


}
