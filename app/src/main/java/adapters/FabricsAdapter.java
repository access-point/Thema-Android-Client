package adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yayandroid.parallaxrecyclerview.ParallaxViewHolder;

import java.util.ArrayList;

import creativedays.com.dilzas.R;
import objects.Fabric;
import utilities.Tools;


/**
 * Created by sergios on 18/7/2016.
 */
public class FabricsAdapter extends RecyclerView.Adapter<FabricsAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    ArrayList<Fabric>fabrics;

    /*
    private int[] imageIds = new int[]{R.mipmap.test_image_1,
            R.mipmap.test_image_2, R.mipmap.test_image_3,
            R.mipmap.test_image_4, R.mipmap.test_image_5};
    */




    public FabricsAdapter(Context context, ArrayList<Fabric>fabrics) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.fabrics = fabrics;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            return new ViewHolder(inflater.inflate(R.layout.item_fabric, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.getBackgroundImage().setImageResource(fabrics.get(position).getImageId());
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
