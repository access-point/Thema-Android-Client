package creativedays.com.dilzas;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import objects.NewsObject;

public class NewsDetailsActivity extends AppCompatActivity {
    NewsObject newsItem;
    ImageView picture;
    TextView title;
    TextView date;
    TextView text;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_news_details);
        newsItem= (NewsObject) getIntent().getExtras().getSerializable("news");

        title=(TextView)findViewById(R.id.title);
        date=(TextView)findViewById(R.id.date);
        text=(TextView)findViewById(R.id.text);
        picture=(ImageView)findViewById(R.id.picture);
        back=(ImageView)findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //text.setTypeface(ac);

        title.setText(newsItem.getTitle());
        date.setText(newsItem.getDate());
        text.setText(Html.fromHtml(newsItem.getText()));

        Glide.with(this).load(newsItem.getImgURL()).centerCrop().into(picture);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            picture.setTransitionName(getString(R.string.activity_image_trans));
            title.setTransitionName(getString(R.string.activity_text_trans));
        }

    }
}