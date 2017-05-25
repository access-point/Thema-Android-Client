package creativedays.com.dilzas;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import adapters.NewsAdapter;
import objects.NewsObject;
import utilities.Constants;

public class NewsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Typeface ba;
    ListView newsList;
    ArrayList<NewsObject> news;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_news);
        news= (ArrayList<NewsObject>) getIntent().getExtras().getSerializable("news");
        back=(ImageView)findViewById(R.id.back);
        newsList=(ListView)findViewById(R.id.news_list);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //initData();
        NewsAdapter adapter=new NewsAdapter(this,news);
        newsList.setAdapter(adapter);

        newsList.setOnItemClickListener(this);
    }






    public void initData () {
        news=new ArrayList<>();
        NewsObject newsItem=new NewsObject();
        newsItem.setTitle("Welcome to Thema");
        newsItem.setText(getString(R.string.lorem));
        newsItem.setImgURL("http://thema.com.gr/images/ap-smart-layerslider/thema-slider/sea%20view1a1920x900.jpg");
        newsItem.setDate("13/11/2016");

        NewsObject newsItem2=new NewsObject();
        newsItem2.setTitle("The new elements colors are here");
        newsItem2.setText(getString(R.string.lorem));
        newsItem2.setImgURL(Constants.FABRICS_BASE_URL+"element/fabric_element_color_02.jpg");
        newsItem2.setDate("13/11/2016");

        NewsObject newsItem3=new NewsObject();
        newsItem3.setTitle("We are going to Heimtextil");
        newsItem3.setText(getString(R.string.lorem));
        newsItem3.setImgURL("http://www.dilzas.gr/images/dilzas/exhibition/008.jpg");
        newsItem3.setDate("13/11/2016");










        news.add(newsItem);
        news.add(newsItem2);
        news.add(newsItem3);
        news.add(newsItem);
        news.add(newsItem2);
        news.add(newsItem3);
        news.add(newsItem);
        news.add(newsItem2);
        news.add(newsItem3);
        news.add(newsItem);
        news.add(newsItem2);
        news.add(newsItem3);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(this,NewsDetailsActivity.class);
        intent.putExtra("news",news.get(position));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> pair1 = Pair.create(view.findViewById(R.id.picture), getString(R.string.activity_image_trans));
            Pair<View, String> pair2 = Pair.create(view.findViewById(R.id.news_title), getString(R.string.activity_text_trans));
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, pair1, pair2);
            startActivity(intent, options.toBundle());
        }
        else {
            startActivity(intent);
        }
    }
}
