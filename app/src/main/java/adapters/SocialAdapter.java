package adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import fragments.SocialFragment;
import objects.SocialNetwork;

/**
 * Created by sergios on 27/7/2016.
 */
public class SocialAdapter extends FragmentPagerAdapter {

    ArrayList<SocialNetwork> socialNetworks;

    public SocialAdapter(FragmentManager fm, ArrayList<SocialNetwork> socialNetworks) {
        super(fm);
        this.socialNetworks=socialNetworks;
    }

    @Override
    public Fragment getItem(int position) {
        SocialFragment fragment=new SocialFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("social_network",socialNetworks.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return socialNetworks.size();
    }
}
