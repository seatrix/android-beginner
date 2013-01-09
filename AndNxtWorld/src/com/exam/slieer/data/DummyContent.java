package com.exam.slieer.data;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;

public class DummyContent {
	public final static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
	public final static SparseArray<DummyItem> ITEM_MAP = new SparseArray<DummyItem>();
	public final static int DOWNLOAD_IMG = 1;
	
	static {
		// Add 3 sample items.
		addItem(new DummyItem(1, "Image"));
		addItem(new DummyItem(2, "Item 2"));
		addItem(new DummyItem(3, "Item 3"));
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	public static class DummyItem {
		public Integer id;
		public String content;

		public DummyItem(Integer id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
