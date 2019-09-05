package creativedays.com.dilzas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.ArrayList;

import adapters.CouchPartAdapter;
import adapters.CreatorColorsRecyclerAdapter;
import adapters.CreatorFabricsRecyclerAdapter;
import custom_listeners.ClickListener;
import custom_listeners.RecyclerTouchListener;
import custom_views.CustomViewPager;
import objects.Fabric;
import objects.FabricColor;
import utilities.FabricsSingleton;
import utilities.recycle_view.ScaleInAnimatorAdapter;

public class SofaCreatorActivity extends AppCompatActivity {
    CustomViewPager bodyPager;
    CustomViewPager pillowsPager;

    CouchPartAdapter bodyAdapter;
    CouchPartAdapter pillowsAdapter;

    RecyclerView pillowFabricsRecycler;
    RecyclerView pillowColorsRecycler;
    RecyclerView bodyFabricsRecycler;
    RecyclerView bodyColorsRecycler;

    LinearLayout toolBox;
    LinearLayout bodySettings;
    LinearLayout pillowSettings;


    ImageView menu;
    ImageView camera;
    ImageView back;

    TextView bodyTab;
    TextView pillowsTab;

    ArrayList<Fabric>fabrics;

    Fabric selectedBodyFabric;
    Fabric selectedPillowsFabric;

    CreatorColorsRecyclerAdapter bodyColorsAdapter;
    CreatorFabricsRecyclerAdapter bodyFabricsRecyclerAdapter;

    CreatorColorsRecyclerAdapter pillowsColorsAdapter;
    CreatorFabricsRecyclerAdapter pillowFabricsRecyclerAdapter;

    int selectedBodyFabricInt=0;
    int selectedPillowFabricInt=0;

    Animation toRight;
    Animation fromRight;

    boolean menuVisible=true;

    FrameLayout pagersLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        fabrics = FabricsSingleton.instance.getIndoorFabrics();
        //fabrics = (ArrayList<Fabric>) getIntent().getExtras().get("fabrics");

        toRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        toRight.setDuration(300);

        fromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        fromRight.setDuration(300);

        selectedBodyFabric=fabrics.get(0);
        selectedPillowsFabric=fabrics.get(0);

        bodyAdapter=new CouchPartAdapter(getSupportFragmentManager(),selectedBodyFabric.getCouchBodys());
        pillowsAdapter=new CouchPartAdapter(getSupportFragmentManager(),selectedPillowsFabric.getCouchPillows());


        setContentView(R.layout.activity_sofa_creator);

        camera=(ImageView)findViewById(R.id.camera);
        pagersLayout=(FrameLayout)findViewById(R.id.pagers_layout);
        back=(ImageView)findViewById(R.id.back);

        bodyPager=(CustomViewPager)findViewById(R.id.body_pager);
        pillowsPager=(CustomViewPager)findViewById(R.id.pilows_pager);

        bodyPager.setAdapter(bodyAdapter);
        pillowsPager.setAdapter(pillowsAdapter);

        pillowsPager.setPagingEnabled(false);

        bodyFabricsRecycler=(RecyclerView)findViewById(R.id.body_fabrics);
        bodyColorsRecycler=(RecyclerView)findViewById(R.id.body_colors);
        pillowFabricsRecycler=(RecyclerView)findViewById(R.id.pillows_fabrics);
        pillowColorsRecycler=(RecyclerView)findViewById(R.id.pillows_colors);

        toolBox=(LinearLayout)findViewById(R.id.toolbox);
        bodySettings=(LinearLayout) findViewById(R.id.body_settings);
        pillowSettings=(LinearLayout)findViewById(R.id.pillows_settings);

        menu=(ImageView)findViewById(R.id.menu);

        bodyTab=(TextView)findViewById(R.id.body_tab);
        pillowsTab=(TextView)findViewById(R.id.pillows_tab);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bodyColorsAdapter = new CreatorColorsRecyclerAdapter(selectedBodyFabric,SofaCreatorActivity.this);
        bodyFabricsRecyclerAdapter = new CreatorFabricsRecyclerAdapter(fabrics,this);

        pillowsColorsAdapter = new CreatorColorsRecyclerAdapter(selectedPillowsFabric,SofaCreatorActivity.this);
        pillowFabricsRecyclerAdapter = new CreatorFabricsRecyclerAdapter(fabrics,this);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        LinearLayoutManager layoutManager2
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        LinearLayoutManager layoutManager3
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        LinearLayoutManager layoutManager4
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        bodyFabricsRecycler.setLayoutManager(layoutManager);
        bodyFabricsRecycler.setItemAnimator(new DefaultItemAnimator());

        bodyColorsRecycler.setLayoutManager(layoutManager2);
        final ScaleInAnimatorAdapter bodyAnimatorAdapter = new ScaleInAnimatorAdapter(bodyColorsAdapter, bodyColorsRecycler);
        bodyColorsRecycler.setAdapter(bodyAnimatorAdapter);
        bodyFabricsRecycler.setAdapter(bodyFabricsRecyclerAdapter);

        pillowFabricsRecycler.setLayoutManager(layoutManager3);
        pillowFabricsRecycler.setItemAnimator(new DefaultItemAnimator());

        pillowColorsRecycler.setLayoutManager(layoutManager4);
        ScaleInAnimatorAdapter pillowAnimatorAdapter = new ScaleInAnimatorAdapter(pillowsColorsAdapter, pillowColorsRecycler);
        pillowColorsRecycler.setAdapter(pillowAnimatorAdapter);
        pillowFabricsRecycler.setAdapter(pillowFabricsRecyclerAdapter);




        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (ContextCompat.checkSelfPermission(SofaCreatorActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SofaCreatorActivity.this,
                                Manifest.permission.CAMERA)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(SofaCreatorActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    2);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                    else {
                        startCamera();
                    }
                }
                else {
                    startCamera();
                }
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuVisible) {
                    toolBox.startAnimation(toRight);
                    toolBox.setVisibility(View.GONE);
                    menuVisible=false;
                }
                else {
                    toolBox.startAnimation(fromRight);
                    toolBox.setVisibility(View.VISIBLE);
                    menuVisible=true;
                }
            }
        });

        bodyTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bodyTab.setTypeface(Typeface.DEFAULT_BOLD);
                pillowsTab.setTypeface(Typeface.DEFAULT);
                pillowsTab.setBackgroundColor(getResources().getColor(R.color.transparent));
                bodyTab.setBackgroundColor(getResources().getColor(R.color.selected_blue_trans));
                pillowsPager.setPagingEnabled(false);
                bodyPager.setPagingEnabled(true);

                Animation fadeOut=new AlphaAnimation(1,0);
                fadeOut.setDuration(200);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation fadeIn=new AlphaAnimation(0,1);
                        fadeIn.setDuration(200);
                        bodySettings.setVisibility(View.VISIBLE);
                        bodySettings.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                pillowSettings.startAnimation(fadeOut);
                pillowSettings.setVisibility(View.GONE);


            }
        });

        pillowsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pillowsTab.setTypeface(Typeface.DEFAULT_BOLD);
                bodyTab.setTypeface(Typeface.DEFAULT);
                bodyTab.setBackgroundColor(getResources().getColor(R.color.transparent));
                pillowsTab.setBackgroundColor(getResources().getColor(R.color.selected_blue_trans));
                pillowsPager.setPagingEnabled(true);
                bodyPager.setPagingEnabled(false);
                Animation fadeOut=new AlphaAnimation(1,0);
                fadeOut.setDuration(200);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation fadeIn=new AlphaAnimation(0,1);
                        fadeIn.setDuration(200);
                        pillowSettings.setVisibility(View.VISIBLE);
                        pillowSettings.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                bodySettings.startAnimation(fadeOut);
                bodySettings.setVisibility(View.GONE);
            }
        });


        bodyFabricsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), bodyFabricsRecycler, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedBodyFabric = fabrics.get(position);
                selectedBodyFabricInt = position;
                bodyColorsAdapter=new CreatorColorsRecyclerAdapter(selectedBodyFabric,SofaCreatorActivity.this);
                ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter(bodyColorsAdapter, bodyColorsRecycler);
                bodyColorsRecycler.setAdapter(animatorAdapter);

                bodyAdapter=new CouchPartAdapter(getSupportFragmentManager(),selectedBodyFabric.getCouchBodys());
                bodyPager.setAdapter(bodyAdapter);

                bodyFabricsRecyclerAdapter.selectPos(position);

                //colorsRecycler.findViewHolderForAdapterPosition(0).itemView.performClick();
                //colorsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        pillowFabricsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), pillowFabricsRecycler, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedPillowsFabric = fabrics.get(position);
                selectedPillowFabricInt = position;
                pillowsColorsAdapter=new CreatorColorsRecyclerAdapter(selectedPillowsFabric,SofaCreatorActivity.this);
                ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter(pillowsColorsAdapter, pillowColorsRecycler);
                pillowColorsRecycler.setAdapter(animatorAdapter);
                //colorsRecycler.findViewHolderForAdapterPosition(0).itemView.performClick();
                //colorsAdapter.notifyDataSetChanged();

                pillowsAdapter=new CouchPartAdapter(getSupportFragmentManager(),selectedPillowsFabric.getCouchPillows());
                pillowsPager.setAdapter(pillowsAdapter);

                pillowFabricsRecyclerAdapter.selectPos(position);

                System.out.println("thez position is "+position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        bodyColorsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), bodyColorsRecycler, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                FabricColor color = fabrics.get(selectedBodyFabricInt).getFabricColors().get(position);
                bodyPager.setCurrentItem(position);
                bodyColorsAdapter.selectPos(position);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        bodyPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bodyColorsAdapter.selectPos(position);
                bodyColorsRecycler.getLayoutManager().scrollToPosition(position);
        }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pillowColorsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), pillowColorsRecycler, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                FabricColor color = fabrics.get(selectedPillowFabricInt).getFabricColors().get(position);
                pillowsPager.setCurrentItem(position);
                pillowsColorsAdapter.selectPos(position);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        pillowsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pillowsColorsAdapter.selectPos(position);
                pillowColorsRecycler.getLayoutManager().scrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final int pos = 0;
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bodyFabricsRecycler.findViewHolderForAdapterPosition(pos).itemView.performClick();
            }
        },1);
        */

    }

    public void startCamera () {
        pagersLayout.setDrawingCacheEnabled(true);
        pagersLayout.buildDrawingCache();
        Bitmap bitmap = pagersLayout.getDrawingCache();


        Intent intent=new Intent(this,CameraActivityWithBitmap.class);
        //intent.putExtra("picture",bitmap.copy(bitmap.getConfig(),false));
        saveImage(this,bitmap,"image","png");

        pagersLayout.setDrawingCacheEnabled(false);
        startActivity(intent);
    }

    public void saveImage(Context context, Bitmap b, String name, String extension){
        name=name+"."+extension;
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
