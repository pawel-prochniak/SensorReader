package com.example.pprochniak.sensorreader.utils;

import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pprochniak.sensorreader.GATT.ServicesFragment_;
import com.example.pprochniak.sensorreader.MainActivity;
import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.deviceDiscovery.DevicesFragment;
import com.example.pprochniak.sensorreader.deviceDiscovery.DevicesFragment_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringArrayRes;

import java.lang.annotation.Retention;
import java.util.ArrayList;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Henny on 2017-06-20.
 */

@EBean
public class DrawerController implements ListView.OnItemClickListener {
    public ArrayList<String> drawerItems = new ArrayList<>();

    @Retention(SOURCE)
    @StringDef({
            DEVICES_FRAGMENT,
            SIGNAL_FRAGMENT
    })
    public @interface FragmentName {}
    public static final String DEVICES_FRAGMENT = "Devices";
    public static final String SIGNAL_FRAGMENT = "Signal Chart";

    @RootContext MainActivity activity;
    DrawerLayout drawerLayout;
    ListView drawerList;

    public DrawerController() {
        drawerItems.add(DEVICES_FRAGMENT);
        drawerItems.add(SIGNAL_FRAGMENT);
    }

    public void setupDrawer(DrawerLayout drawerLayout, ListView drawerList) {
        this.drawerLayout = drawerLayout;
        this.drawerList = drawerList;
        this.drawerList.setAdapter(new ArrayAdapter<>(activity, R.layout.drawer_item, drawerItems));
        this.drawerList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    public void setFragment(@FragmentName String name){
        selectItem(drawerItems.indexOf(name));
    }

    private void setFragment(Fragment fragment, String tag, String title) {
        // setTitle(title);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.main_container, fragment, tag)
                .commit();
    }

    private Fragment getFragmentByName(@FragmentName String name) {
        Fragment fragment;
        switch (name) {
            case SIGNAL_FRAGMENT:
                fragment = new ServicesFragment_();
                break;
            case DEVICES_FRAGMENT:
            default:
                fragment = new DevicesFragment_();
                break;
        }
        return fragment;
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        @FragmentName String fragmentName = drawerItems.get(position);
        Fragment fragment = getFragmentByName(fragmentName);

        this.drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);

        setFragment(fragment, fragment.getTag(), fragmentName);

    }

    private void setTitle(CharSequence title) {
        activity.getActionBar().setTitle(title);
    }

}
