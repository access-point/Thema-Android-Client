package creativedays.com.dilzas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import adapters.ColorsRecyclerAdapter;
import adapters.FabricsRecyclerAdapter;
import custom_listeners.ClickListener;
import custom_listeners.RecyclerTouchListener;
import objects.Fabric;
import objects.FabricColor;
import utilities.FabricsSingleton;
import utilities.recycle_view.ScaleInAnimatorAdapter;

public class CouchesActivity extends AppCompatActivity {

    RecyclerView colorsRecycler;
    RecyclerView fabricsRecycler;

    ArrayList<Fabric> fabrics;
    ImageView background;
    ImageView couch;

    Fabric selectedFabric;

    Animation fadeOut;
    Animation fadeIn;

    ProgressBar loader;

    ImageView camera;
    ImageView back;

    int selectedFabricInt = 0;

    String selectedCouch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(700);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(700);

        setContentView(R.layout.activity_couches);
        camera = (ImageView) findViewById(R.id.camera);
        loader = (ProgressBar) findViewById(R.id.loader);
        //blurView=(BlurView)findViewById(R.id.blurringview);
        background = (ImageView) findViewById(R.id.bg);
        couch = (ImageView) findViewById(R.id.couch);


        fabrics = FabricsSingleton.instance.getIndoorFabrics();
       // fabrics = (ArrayList<Fabric>) getIntent().getExtras().get("fabrics");

        final float radius = 2;
        final View decorView = getWindow().getDecorView();
        //Activity's root View. Can also be root View of your layout
        final View rootView = decorView.findViewById(android.R.id.content);
        //set background, if your root layout doesn't have one
        final Drawable windowBackground = decorView.getBackground();


        colorsRecycler = (RecyclerView) findViewById(R.id.colors);
        fabricsRecycler = (RecyclerView) findViewById(R.id.fabrics);
        back=(ImageView)findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        selectedFabric = fabrics.get(0);
        final ColorsRecyclerAdapter colorsAdapter = new ColorsRecyclerAdapter(selectedFabric,CouchesActivity.this);
        FabricsRecyclerAdapter fabricsRecyclerAdapter = new FabricsRecyclerAdapter(fabrics, this);


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        LinearLayoutManager layoutManager2
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        fabricsRecycler.setLayoutManager(layoutManager);
        fabricsRecycler.setItemAnimator(new DefaultItemAnimator());
        fabricsRecycler.setAdapter(fabricsRecyclerAdapter);

        colorsRecycler.setLayoutManager(layoutManager2);
        ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter(colorsAdapter, colorsRecycler);
        colorsRecycler.setAdapter(animatorAdapter);


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (ContextCompat.checkSelfPermission(CouchesActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(CouchesActivity.this,
                                Manifest.permission.CAMERA)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(CouchesActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    2);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                    else {
                        Intent intent = new Intent(CouchesActivity.this, CameraActivity.class);
                        intent.putExtra("picture", selectedCouch);
                        startActivity(intent);
                    }
                }
                else {
                    Intent intent = new Intent(CouchesActivity.this, CameraActivity.class);
                    intent.putExtra("picture", selectedCouch);
                    startActivity(intent);
                }
            }
        });


        fabricsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), fabricsRecycler, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedFabric = fabrics.get(position);
                selectedFabricInt = position;
                ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter(new ColorsRecyclerAdapter(selectedFabric,CouchesActivity.this), colorsRecycler);
                colorsRecycler.setAdapter(animatorAdapter);
                //colorsRecycler.findViewHolderForAdapterPosition(0).itemView.performClick();
                //colorsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        colorsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), colorsRecycler, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                loader.setVisibility(View.VISIBLE);
                FabricColor color = fabrics.get(selectedFabricInt).getFabricColors().get(position);
                selectedCouch = selectedFabric.getCouches().get(position);
                couch.startAnimation(fadeOut);
                couch.setVisibility(View.INVISIBLE);
                Glide.with(CouchesActivity.this)
                        .load(fabrics.get(selectedFabricInt).getCouches().get(position))
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                loader.setVisibility(View.GONE);
                                couch.startAnimation(fadeIn);
                                couch.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .dontAnimate()
                        .into(couch);

                Glide.with(CouchesActivity.this)
                        .load(color.getImgUrl())
                        .centerCrop()
                        .crossFade(300)
                        .into(background);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }
}
