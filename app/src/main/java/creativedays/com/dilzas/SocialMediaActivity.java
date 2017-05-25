package creativedays.com.dilzas;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import adapters.SocialAdapter;
import custom_views.FadeyTextView;
import objects.SocialNetwork;

public class SocialMediaActivity extends AppCompatActivity {
    KenBurnsView ken;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    ArrayList<Integer> fabrics;
    String [] names;

    ImageView back;

    int index=0;

    ViewPager pager;
    FadeyTextView socialTxt;
    ArrayList<SocialNetwork> socialNetworks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_social_media);
        ken=(KenBurnsView)findViewById(R.id.ken);
        pager=(ViewPager)findViewById(R.id.social_viewpager);
        socialTxt=(FadeyTextView)findViewById(R.id.social_txt);
        back=(ImageView)findViewById(R.id.back);
        fabrics=new ArrayList<>();
        fabrics.add(R.drawable.aegean);
        fabrics.add(R.drawable.atlantis);
        fabrics.add(R.drawable.ocean);
        fabrics.add(R.drawable.calm);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        RandomTransitionGenerator generator = new RandomTransitionGenerator(19000, new LinearInterpolator());
        ken.setTransitionGenerator(generator);

        initData();

        //socialTxt.setTypeface(Typeface.SERIF);
        //socialTxt.setAnimateType(HTextViewType.SCALE);
        //socialTxt.animateText(names[0]);
        socialTxt.setText(names[0]);
        SocialAdapter adapter=new SocialAdapter(getSupportFragmentManager(),socialNetworks);
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                //socialTxt.setTypeface(Typeface.SERIF);
                //socialTxt.setAnimateType(HTextViewType.SCALE);
                //socialTxt.animateText(names[position]);
                socialTxt.setText(names[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public int getImage () {
        int image=fabrics.get(index);

        if (index==fabrics.size()-1) {
            index=0;
        }
        else {
            index++;
        }

        return image;

    }

    public void initData () {
        socialNetworks=new ArrayList<>();
        socialNetworks.add(new SocialNetwork("FACEBOOK",R.drawable.facebook));
        socialNetworks.add(new SocialNetwork("TWITTER",R.drawable.twitter));
        socialNetworks.add(new SocialNetwork("INSTAGRAM",R.drawable.instagram));
        socialNetworks.add(new SocialNetwork("YOUTUBE",R.drawable.youtube));
        socialNetworks.add(new SocialNetwork("WEBSITE",R.drawable.internet));

        names=new String [5];

        names[0]="FACEBOOK";
        names[1]="TWITTER";
        names[2]="INSTAGRAM";
        names[3]="YOUTUBE";
        names[4]="WEBSITE";
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, 5000); //
    }


    public void initializeTimerTask() {

        timerTask = new TimerTask() {

            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        Animation fadeout=new AlphaAnimation(2,0);
                        fadeout.setDuration(1000);
                        fadeout.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                RandomTransitionGenerator generator = new RandomTransitionGenerator(19000, new LinearInterpolator());
                                ken.setTransitionGenerator(generator);
                                ken.setImageResource(getImage());
                                Animation fadeIn=new AlphaAnimation(0,1);
                                fadeIn.setDuration(1000);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        ken.startAnimation(fadeout);
                    }
                });
            }
        };
    }
}
