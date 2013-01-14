package com.mipt.fileMgr.utils;

import java.util.HashMap;

import android.app.Activity;

/**
 * 
 * @author fang
 * 
 */
public class ActivitiesManager {
	public static final String ACTIVITY_FILE_VIEW = "FileView";
	public static final String ACTIVITY_POP_VIEW = "popView";

	private static ActivitiesManager instance;

	private HashMap<String, Activity> activities;

	private ActivitiesManager() {
		activities = new HashMap<String, Activity>();
	}

	public void registerActivity(String name, Activity a) {
		activities.put(name, a);
	}

	public void unRegisterActivity(String name) {
		activities.remove(name);
	}

	public Activity getActivity(String name) {
		return activities.get(name);
	}

	public static ActivitiesManager getInstance() {
		if (instance == null)
			instance = new ActivitiesManager();
		return instance;
	}
}
