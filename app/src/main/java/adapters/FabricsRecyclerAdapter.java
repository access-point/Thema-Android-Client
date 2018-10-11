package adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import creativedays.com.dilzas.R;
import objects.Fabric;
import utilities.Constants;

/**
 * Created by Sergios on 25/9/2016.
 */

public class FabricsRecyclerAdapter extends RecyclerView.Adapter<FabricsRecyclerAdapter.MyViewHolder> {

    ArrayList<Fabric> fabrics;
    Activity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView img;

        public MyViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.fabric_txt);
            img = (ImageView) view.findViewById(R.id.fabric_img);
        }
    }

    public FabricsRecyclerAdapter(ArrayList<Fabric> fabrics, Activity activity) {
        this.fabrics = fabrics;
        this.activity=activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fabric_small, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Fabric fabric = fabrics.get(position);
        holder.title.setVisibility(View.INVISIBLE);
        holder.title.setText(fabric.getTitle().toUpperCase());

       // holder.img.setImageResource(fabric.getImageId());

        String preview_url = Constants.PREVIEW_BASE_URL+fabric.getTitle().replace(' ','_').toLowerCase()+".jpg";
        Glide.with(activity)
                .load(preview_url)
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
                        holder.title.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(holder.img) ;
    }

    @Override
    public int getItemCount() {
        return fabrics.size();
    }



}
