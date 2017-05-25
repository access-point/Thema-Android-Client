package adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import creativedays.com.dilzas.R;
import objects.FabricColor;


/**
 * Created by sergios on 18/7/2016.
 */
public class FabricColorsAdapter extends BaseAdapter {
    ArrayList<FabricColor>colors;
    Activity activity;

    public FabricColorsAdapter(Activity activity, ArrayList<FabricColor>colors) {
        this.activity=activity;
        this.colors=colors;
    }


    @Override
    public int getCount() {
        return colors.size();
    }

    @Override
    public Object getItem(int position) {
        return colors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



/*    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=activity.getLayoutInflater().inflate(R.layout.item_fabric,null);
        Fabric item=fabrics.get(position);
        ImageView pic= (ImageView) view.findViewById(R.id.fabric_img);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        pic.setColorFilter(filter);
        pic.setImageResource(item.getImageId());

        TextView name=(TextView)view.findViewById(R.id.fabric_title);



        name.setText(item.getTitle());
        return view;
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FabricColor item=colors.get(position);
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.item_color, parent, false);

            final TextView text = (TextView) convertView.findViewById(R.id.color_txt);
            ImageView img=(ImageView) convertView.findViewById(R.id.color_img);

            text.setText(item.getNameEn());
            Glide.with(activity)
                    .load(item.getThumbURL())
                    .fitCenter()
                    .placeholder(R.drawable.viewholder)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            text.setVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(img)
            ;

        } else {
            final TextView text = (TextView) convertView.findViewById(R.id.color_txt);
            ImageView img=(ImageView) convertView.findViewById(R.id.color_img);

            text.setText(item.getNameEn());
            Glide.with(activity)
                    .load(item.getThumbURL())
                    .fitCenter()
                    .placeholder(R.drawable.viewholder)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    text.setVisibility(View.VISIBLE);
                    return false;
                }
            }).into(img);
        }



        return convertView;
    }


}
