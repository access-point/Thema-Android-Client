package adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import fragments.FabricTypeFragment;
import objects.FabricType;
import utilities.Tools;

/**
 * Created by sergios on 27/7/2016.
 */
public class FabricTypeAdapter extends FragmentPagerAdapter {

    ArrayList<FabricType> fabricTypes;

    public FabricTypeAdapter(FragmentManager fm, ArrayList<FabricType> fabricTypes) {
        super(fm);
        this.fabricTypes=fabricTypes;
    }

    @Override
    public Fragment getItem(int position) {
        FabricTypeFragment fragment=new FabricTypeFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("fabric_type",fabricTypes.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return fabricTypes.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Tools.removePunctuationGreek(fabricTypes.get(position).getName());
    }
}
