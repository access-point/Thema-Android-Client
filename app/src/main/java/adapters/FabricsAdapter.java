package adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.yayandroid.parallaxrecyclerview.ParallaxViewHolder;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import creativedays.com.dilzas.R;
import objects.Fabric;
import utilities.Constants;
import utilities.Tools;


/**
 * Created by sergios on 18/7/2016.
 */
public class FabricsAdapter extends RecyclerView.Adapter<FabricsAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    Activity activity;
    ArrayList<Fabric>fabrics;

    /*
    private int[] imageIds = new int[]{R.mipmap.test_image_1,
            R.mipmap.test_image_2, R.mipmap.test_image_3,
            R.mipmap.test_image_4, R.mipmap.test_image_5};
    */




    public FabricsAdapter(Context context, ArrayList<Fabric>fabrics, Activity activity) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.fabrics = fabrics;
        this.activity =activity;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            return new ViewHolder(inflater.inflate(R.layout.item_fabric, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {


       // viewHolder.getBackgroundImage().setImageResource(fabrics.get(position).getImageId());

        String preview_url = Constants.PREVIEW_BASE_URL+fabrics.get(position).getTitle().replace(' ','_').toLowerCase()+".jpg";

        Glide.with(activity).load(preview_url).asBitmap().into(new SimpleTarget<Bitmap>(400, 400) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(context.getResources(), resource);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.getBackgroundImage().setImageDrawable(drawable);
                }
            }
        });
        //viewHolder.getBackgroundImage().setImageBitmap(theBitmap);

        if (position==0) {
            FrameLayout mylayout = (FrameLayout) viewHolder.getBackgroundImage().getParent();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mylayout.getLayoutParams();
            params.setMargins(0, (int) Tools.convertDpToPixel(50,context), 0, 0);
            ((FrameLayout) viewHolder.getBackgroundImage().getParent()).setLayoutParams(params);
        }
        else {
            FrameLayout mylayout = (FrameLayout) viewHolder.getBackgroundImage().getParent();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mylayout.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            ((FrameLayout) viewHolder.getBackgroundImage().getParent()).setLayoutParams(params);
        }
        viewHolder.getTextView().setText(fabrics.get(position).getTitle().toUpperCase());

        // # CAUTION:
        // Important to call this method
        viewHolder.getBackgroundImage().reuse();
    }

    @Override
    public int getItemCount() {
        return fabrics.size();
    }

    /**
     * # CAUTION:
     * ViewHolder must extend from ParallaxViewHolder
     */
    public static class ViewHolder extends ParallaxViewHolder {

        private final TextView textView;


        public ViewHolder(View v) {
            super(v);

            textView = (TextView) v.findViewById(R.id.fabric_title);
        }

        @Override
        public int getParallaxImageId() {
            return R.id.fabric_img;
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
