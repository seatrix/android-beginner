package com.mipt.mediacenter.center.server;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 
 * @author fang
 * 
 */
public class HttpRequestCache {
	public static final int MAX_SIZE = 10;
	private LinkedList<String> urlList;
	private HashMap<String, Object> data;

	public HttpRequestCache() {
		urlList = new LinkedList<String>();
		data = new HashMap<String, Object>();
	}

	public void putData(String _url, Object _data) {
		urlList.add(_url);
		if (urlList.size() > MAX_SIZE) {
			String oldUrl = urlList.poll();
			data.remove(oldUrl);
		}
		data.put(_url, _data);
	}

	public Object getData(String _url) {
		return data.get(_url);
	}
}
