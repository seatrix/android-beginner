package com.mipt.mediacenter.utils;

import java.util.HashMap;

import android.os.Handler;

/**
 * 
 * @author fang
 * 
 */
public class HandlerManager {
	public static final String MainHandler = "MainHandler";

	private static HandlerManager instance;

	private HashMap<String, Handler> handlers = new HashMap<String, Handler>();

	private HandlerManager() {
	}

	public void registerHandler(String name, Handler _handler) {
		handlers.put(name, _handler);
	}

	public void unRegisterHandler(String name) {
		handlers.remove(name);
	}

	public Handler getHandler(String name) {
		return handlers.get(name);
	}

	public static HandlerManager getInstance() {
		if (instance == null)
			instance = new HandlerManager();
		return instance;
	}
}
