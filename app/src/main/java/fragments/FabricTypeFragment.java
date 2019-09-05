package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;

import activities.FabricsActivity;
import creativedays.com.dilzas.R;
import objects.FabricType;


public class FabricTypeFragment extends Fragment {
    String ARG_PARAM1 = "fabric_type";
    private FabricType fabricType;
    KenBurnsView ken;
    TextView name;



    public FabricTypeFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fabricType = (FabricType) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       View rootView=inflater.inflate(R.layout.fragment_fabric_type, container, false);
        ken=(KenBurnsView) rootView.findViewById(R.id.ken);
        name=(TextView)rootView.findViewById(R.id.fabric_type_title);
        name.setText(fabricType.getName());
        ken.setImageResource(fabricType.getImgId());

        ken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), FabricsActivity.class);
                intent.putExtra("fabrics",fabricType.getName());
                getActivity().startActivity(intent);
            }
        });
        return rootView;
    }





}
