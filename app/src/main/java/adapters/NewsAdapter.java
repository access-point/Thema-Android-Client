package adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import creativedays.com.dilzas.R;
import objects.NewsObject;


/**
 * Created by Sergios on 21/11/2016.
 */

public class NewsAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<NewsObject> news;

    public NewsAdapter(Activity activity, ArrayList<NewsObject> news) {
        this.activity=activity;
        this.news=news;

    }
    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public Object getItem(int position) {
        return news.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            View row=activity.getLayoutInflater().inflate(R.layout.item_news,null);
            TextView title= (TextView) row.findViewById(R.id.news_title);
            TextView date=(TextView)row.findViewById(R.id.date);
            ImageView picture=(ImageView)row.findViewById(R.id.picture);

            title.setText(news.get(position).getTitle());
            date.setText(news.get(position).getDate());
            Glide.with(activity).load(news.get(position).getImgURL()).dontAnimate().fitCenter().into(picture);

            return row;
        }
        else {
            TextView title= (TextView) convertView.findViewById(R.id.news_title);
            TextView date=(TextView)convertView.findViewById(R.id.date);
            ImageView picture=(ImageView)convertView.findViewById(R.id.picture);

            title.setText(news.get(position).getTitle());
            date.setText(news.get(position).getDate());
            Glide.with(activity).load(news.get(position).getImgURL()).dontAnimate().fitCenter().into(picture);
            return convertView;
        }
    }
}
