package adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import creativedays.com.dilzas.R;
import objects.Fabric;
import objects.FabricColor;

/**
 * Created by Sergios on 25/9/2016.
 */

public class ColorsRecyclerAdapter extends RecyclerView.Adapter<ColorsRecyclerAdapter.MyViewHolder> {

    Fabric fabric;
    int lastPosition=0;
    View itemView;
    Activity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView img;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.color_txt);
            img = (ImageView) view.findViewById(R.id.color_img);
        }
    }

    public ColorsRecyclerAdapter(Fabric fabric, Activity activity) {
        this.fabric = fabric;
        this.activity=activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_recycler, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        FabricColor color = fabric.getFabricColors().get(position);
        holder.title.setText(color.getNameEn());
        Glide.with(activity)
                .load(color.getThumbURL())
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
                })
                .into(holder.img);
        //holder.img.setImageResource(color.getThumbId());
        //setFadeAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return fabric.getFabricColors().size();
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(600);
        view.startAnimation(anim);
    }

    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        clearAnimation();

    }

    public void clearAnimation()
    {
        itemView.clearAnimation();
    }
}
