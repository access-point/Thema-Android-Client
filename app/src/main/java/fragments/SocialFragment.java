package fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import creativedays.com.dilzas.R;
import objects.SocialNetwork;


public class SocialFragment extends Fragment {
    String ARG_PARAM1 = "social_network";
    private SocialNetwork network;
    ImageView img;



    public SocialFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            network = (SocialNetwork) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       View rootView=inflater.inflate(R.layout.fragment_social, container, false);
        img=(ImageView)rootView.findViewById(R.id.social_img);

        img.setImageResource(network.getImgId());
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (network.getImgId()) {
                    case R.drawable.internet :
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://thema.com.gr/index.php/en/"));
                        startActivity(intent);
                        break;
                    case R.drawable.facebook :
                        intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://www.facebook.com/themasofa/"));
                        startActivity(intent);
                        break;
                    default :
                        Toast.makeText(getActivity(),getString(R.string.coming_soon),Toast.LENGTH_SHORT).show();
                        break;

                }

            }
        });

        return rootView;
    }





}
