package com.exam.slieer.utils;

import java.io.File;

import com.exam.slieer.activities.FragmentLoaderDemoActivity.AppListLoader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * java bean
 * 
 * @author terry
 * 
 */
public class AppEntry {
    private final AppListLoader mLoader;
    private final ApplicationInfo mInfo;
    private final File mApkFile;
    private String mLable;
    private Drawable mIcon;
    private boolean mMounted;

    public AppEntry(AppListLoader loader, ApplicationInfo info) {
        mLoader = loader;
        mInfo = info;
        mApkFile = new File(info.sourceDir);
    }

    public ApplicationInfo getApplicationInfo() {
        return mInfo;
    }

    public String getLable() {
        return mLable;
    }

    public Drawable getIcon() {
        if (mIcon == null) {
            if (mApkFile.exists()) {
                mIcon = mInfo.loadIcon(mLoader.mPm);
                return mIcon;
            } else {
                mMounted = false;
            }
        } else if (!mMounted) {
            if (mApkFile.exists()) {
                mMounted = true;
                mIcon = mInfo.loadIcon(mLoader.mPm);
                return mIcon;
            }
        } else {
            return mIcon;
        }
        return mLoader.getContext().getResources()
                .getDrawable(android.R.drawable.sym_def_app_icon);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return mLable.toString();
    }

    public void loadLable(Context mContext) {
        if (mLable == null || !mMounted) {
            if (!mApkFile.exists()) {
                mMounted = false;
                mLable = mInfo.packageName;
            } else {
                mMounted = true;
                CharSequence lable = mInfo.loadLabel(mContext
                        .getPackageManager());
                mLable = lable != null ? lable.toString()
                        : mInfo.packageName;
            }
        }
    }
}
