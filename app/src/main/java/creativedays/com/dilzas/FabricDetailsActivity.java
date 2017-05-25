package creativedays.com.dilzas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;

import adapters.FabricColorsAdapter;
import objects.Fabric;

public class FabricDetailsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Fabric fabric;
    ListView colorsList;
    KenBurnsView ken;
    ImageView back;

    LinearLayout titlesLayout;
    TextView fabricTitle;
    TextView colorTitle;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fabric_details);
        ken=(KenBurnsView)findViewById(R.id.ken);
        back=(ImageView)findViewById(R.id.back);
        colorsList=(ListView)findViewById(R.id.colors_list);
        fabricTitle=(TextView)findViewById(R.id.fabric_title);
        colorTitle=(TextView)findViewById(R.id.color_title);
        progressBar=(ProgressBar)findViewById(R.id.fabric_progress);
        fabric= (Fabric) getIntent().getExtras().getSerializable("fabric");

        FabricColorsAdapter adapter=new FabricColorsAdapter(this,fabric.getFabricColors());
        colorsList.setAdapter(adapter);
        colorsList.setOnItemClickListener(this);
        fabricTitle.setText(fabric.getTitle());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        progressBar.setVisibility(View.VISIBLE);
        colorTitle.setText(fabric.getFabricColors().get(position).getNameEn());
        colorTitle.setVisibility(View.VISIBLE);
        Glide
                .with(this)
                .load(fabric.getFabricColors().get(position).getImgUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .centerCrop()
                .crossFade()
                .into(ken);
        //ken.setImageResource(fabric.getFabricColors().get(position).getColorImg());
        RandomTransitionGenerator generator = new RandomTransitionGenerator(19000, new LinearInterpolator());
        ken.setTransitionGenerator(generator);
    }
}
