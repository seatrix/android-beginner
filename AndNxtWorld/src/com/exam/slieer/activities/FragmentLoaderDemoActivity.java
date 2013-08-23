package com.exam.slieer.activities;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.exam.slieer.R;
import com.exam.slieer.utils.AppEntry;

/**
 *  android-sdk-windows/docs/reference/android/content/AsyncTaskLoader.html
 * @author slieer
 * Create Date2013-5-24
 * version 1.0
 */
public class FragmentLoaderDemoActivity extends Activity {

    public static String[] array = { "text1,", "text2", "text3", "text4",
            "text5,", "text6", "text7", "text8" };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FragmentManager fm = getFragmentManager();
        //
        // // Create the list fragment and add it as our sole content.
        // if (fm.findFragmentById(android.R.id.content) == null) {
        // DetailsFragment list = new DetailsFragment();
        // fm.beginTransaction().add(android.R.id.content, list).commit();
        // }
        setContentView(R.layout.loader_main);
    }

    /**
     * 继承asyncTaskLoader
     * 
     * @author terry
     * 
     */
    public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {

        public final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        public final PackageManager mPm;

        List<AppEntry> mApps;
        packageIntentReceiver mPackageObserver;

        public AppListLoader(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            mPm = getContext().getPackageManager();
        }

        @Override
        public List<AppEntry> loadInBackground() {
            // TODO Auto-generated method stub
            List<ApplicationInfo> apps = mPm
                    .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
                            | PackageManager.GET_DISABLED_COMPONENTS);
            if (apps == null)
                apps = new ArrayList<ApplicationInfo>();
            final Context mContext = getContext();
            List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
            for (ApplicationInfo info : apps) {
                AppEntry entry = new AppEntry(this, info);
                entry.loadLable(mContext);
                entries.add(entry);
            }
            Collections.sort(entries, ALPHA_COMPARATOR);
            return entries;
        }

        @Override
        public void deliverResult(List<AppEntry> data) {
            // TODO Auto-generated method stub
            if (isReset()) {
                if (data != null) {
                    // 释放资源处理
                }
            }

            List<AppEntry> oladApps = data;
            mApps = data;
            if (isStarted()) {
                super.deliverResult(data);
            }

            if (oladApps != null) {
                // 释放资源
            }
        }

        protected void onStartLoading() {
            if (mApps != null)
                deliverResult(mApps);

            if (mPackageObserver == null)
                mPackageObserver = new packageIntentReceiver(this);

            boolean configChange = mLastConfig.applyNewConfig(getContext()
                    .getResources());

            if (takeContentChanged() || mApps == null || configChange) {
                forceLoad();
            }
        };

        @Override
        public void onCanceled(List<AppEntry> data) {
            // TODO Auto-generated method stub
            super.onCanceled(data);
            cancelLoad();
        }

        @Override
        protected void onReset() {
            // TODO Auto-generated method stub
            super.onReset();
            onStopLoading();

            if (mApps != null) {
                // 释放资源
                mApps = null;
            }

            if (mPackageObserver != null) {
                getContext().unregisterReceiver(mPackageObserver);
                mPackageObserver = null;
            }
        }

    }

    public static class InterestingConfigChanges {
        final Configuration mConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res) {
            int configChanges = mConfiguration.updateFrom(res
                    .getConfiguration());
            boolean desityChange = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (desityChange
                    || (configChanges & (ActivityInfo.CONFIG_LOCALE
                            | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.getLable(), object2.getLable());
        }
    };

    /**
     * 广播监听应用程序变化
     * 
     * @author terry
     * 
     */
    public static class packageIntentReceiver extends BroadcastReceiver {

        final AppListLoader mLoader;

        public packageIntentReceiver(AppListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // 当发生变化时通知loader 自动刷新数据
            mLoader.onContentChanged();
        }

    }

    /**
     * 做数据源
     * 
     * @author terry
     * 
     */
    public static class AppListAdapter extends ArrayAdapter<AppEntry> {

        private LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            // TODO Auto-generated constructor stub
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public void setData(List<AppEntry> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.loader_list_item_icon_text,
                        parent, false);
            } else {
                view = convertView;
            }

            AppEntry item = getItem(position);
            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item
                    .getIcon());
            ((TextView) view.findViewById(R.id.text)).setText(item.getLable());
            return view;
        }

    }
}