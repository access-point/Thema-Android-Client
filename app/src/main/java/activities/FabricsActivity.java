package activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.yayandroid.parallaxrecyclerview.ParallaxRecyclerView;

import java.util.ArrayList;

import adapters.FabricsAdapter;
import creativedays.com.dilzas.FabricDetailsActivity;
import creativedays.com.dilzas.R;
import custom_listeners.ClickListener;
import custom_listeners.RecyclerTouchListener;
import custom_views.PreCachingLayoutManager;
import objects.Fabric;
import utilities.FabricsSingleton;

import static creativedays.com.dilzas.R.string.outdoor;

public class FabricsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<Fabric> fabrics;
    ParallaxRecyclerView fabricsList;
    ImageView back;
    String fabricsName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fabrics);

        fabrics = FabricsSingleton.instance.getIndoorFabrics();
        fabricsName = (String) getIntent().getExtras().get("fabrics");

        if(fabricsName.equals(getString(R.string.indoor)))
        {
            fabrics = FabricsSingleton.instance.getIndoorFabrics();
        }
        else if(fabricsName.equals(getString(outdoor)))
        {
            fabrics = FabricsSingleton.instance.getOutdoorFabrics();
        }
        else if(fabricsName.equals(getString(R.string.curtains_lower)))
        {
            fabrics = FabricsSingleton.instance.getCurtainFabrics();
        }

        fabricsList=(ParallaxRecyclerView)findViewById(R.id.fabrics_listview);
        back=(ImageView)findViewById(R.id.back);
        FabricsAdapter adapter=new FabricsAdapter(this,fabrics, this);

        PreCachingLayoutManager manager=new PreCachingLayoutManager(this);
        fabricsList.setLayoutManager(manager);
        fabricsList.setHasFixedSize(true);
        fabricsList.setAdapter(adapter);

        fabricsList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), fabricsList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent=new Intent(FabricsActivity.this, FabricDetailsActivity.class);
                intent.putExtra("fabric",fabrics.get(position));
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }





    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
