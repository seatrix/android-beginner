package com.exam.slieer.ui.fragment;

import java.util.List;

import com.exam.slieer.activities.FragmentLoaderDemoActivity;
import com.exam.slieer.activities.FragmentLoaderDemoActivity.AppListAdapter;
import com.exam.slieer.utils.AppEntry;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView.OnQueryTextListener;

/**
 * @author terry
 * 
 */
public class DetailsFragment extends ListFragment implements
        OnQueryTextListener, LoaderCallbacks<List<AppEntry>> {

    private AppListAdapter mAdapter;
    private String mCurFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        mAdapter = new AppListAdapter(getActivity());
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    public static DetailsFragment newInstance(int index) {
        DetailsFragment details = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        details.setArguments(args);
        return details;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    // @Override
    // public View onCreateView(LayoutInflater inflater, ViewGroup
    // container,
    // Bundle savedInstanceState) {
    // // TODO Auto-generated method stub
    // if (container == null)
    // return null;
    //
    // ScrollView scroller = new ScrollView(getActivity());
    // TextView text = new TextView(getActivity());
    //
    // int padding = (int) TypedValue.applyDimension(
    // TypedValue.COMPLEX_UNIT_DIP, 4, getActivity()
    // .getResources().getDisplayMetrics());
    // text.setPadding(padding, padding, padding, padding);
    // scroller.addView(text);
    //
    // text.setText(array[getShownIndex()]);
    // return scroller;
    // }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Menu 1a")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("Menu 1b")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        return super.onOptionsItemSelected(item);
    }

    public boolean onQueryTextChange(String newText) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }

    public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
        // TODO Auto-generated method stub
        return new FragmentLoaderDemoActivity.AppListLoader(getActivity());
    }

    /**
     * Load 完成后
     */
    public void onLoadFinished(Loader<List<AppEntry>> arg0,
            List<AppEntry> arg1) {
        // TODO Auto-generated method stub
        mAdapter.setData(arg1);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /**
     * Loader 重置时
     */
    public void onLoaderReset(Loader<List<AppEntry>> arg0) {
        // TODO Auto-generated method stub
        mAdapter.setData(null);
    }
}
