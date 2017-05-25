package adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import fragments.CouchPartFragment;

/**
 * Created by sergios on 27/7/2016.
 */
public class CouchPartAdapter extends FragmentStatePagerAdapter {

    ArrayList<String> partURLs;

    public CouchPartAdapter(FragmentManager fm, ArrayList<String> partURLs) {
        super(fm);
        this.partURLs=partURLs;
    }

    @Override
    public Fragment getItem(int position) {
        CouchPartFragment fragment=new CouchPartFragment();
        Bundle bundle=new Bundle();
        bundle.putString("part_url",partURLs.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return partURLs.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
