package creativedays.com.dilzas;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

import adapters.FabricTypeAdapter;
import custom_views.ZoomOutPageTransformer;
import objects.FabricType;

public class FabricTypeActivity extends AppCompatActivity {

    ArrayList<FabricType> fabricTypes;
    ViewPager fabricTypesPager;
    ImageView back;

    SmartTabLayout tabs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fabric_type);
        fabricTypes= (ArrayList<FabricType>) getIntent().getSerializableExtra("fabric_types");
        fabricTypesPager=(ViewPager)findViewById(R.id.fabric_type_viewpager);
        fabricTypesPager.setPageTransformer(false,new ZoomOutPageTransformer());
        tabs=(SmartTabLayout)findViewById(R.id.viewpagertab);


        FabricTypeAdapter adapter=new FabricTypeAdapter(getSupportFragmentManager(),fabricTypes);
        fabricTypesPager.setAdapter(adapter);

        tabs.setViewPager(fabricTypesPager);

        back=(ImageView)findViewById(R.id.back);




        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
