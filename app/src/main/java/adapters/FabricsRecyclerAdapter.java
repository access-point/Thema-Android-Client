package adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import creativedays.com.dilzas.R;
import objects.Fabric;

/**
 * Created by Sergios on 25/9/2016.
 */

public class FabricsRecyclerAdapter extends RecyclerView.Adapter<FabricsRecyclerAdapter.MyViewHolder> {

    ArrayList<Fabric> fabrics;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView img;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.fabric_txt);
            img = (ImageView) view.findViewById(R.id.fabric_img);
        }
    }

    public FabricsRecyclerAdapter(ArrayList<Fabric> fabrics) {
        this.fabrics = fabrics;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fabric_small, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Fabric fabric = fabrics.get(position);
        holder.title.setText(fabric.getTitle());
        holder.img.setImageResource(fabric.getImageId());
    }

    @Override
    public int getItemCount() {
        return fabrics.size();
    }



}
