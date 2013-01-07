package com.exam.slieer.activities;

import java.util.ArrayList;
import java.util.List;

import com.exam.slieer.R;
import com.exam.slieer.ui.LoadListView;
import com.exam.slieer.ui.LoadListView.OnRefreshListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadListActivity extends Activity {

	private List<String> data;
	private BaseAdapter adapter;
	private ArrayList<View> pageViews;
	private int[] ar = { R.drawable.one, R.drawable.two,
			R.drawable.three};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_list_main);
		
		pageViews = new ArrayList<View>();

		for (int i = 0; i < ar.length; i++) {
			ImageView img = new ImageView(this);
			img.setBackgroundResource(ar[i]);
			pageViews.add(img);
		}
		data = new ArrayList<String>();
		data.add("a");
		data.add("b");
		data.add("c");

		final LoadListView listView = (LoadListView) findViewById(R.id.listView);
		adapter = new BaseAdapter() {
			public View getView(int position, View convertView, ViewGroup parent) {
				if(position==0){
					LayoutInflater inflater=LayoutInflater.from(LoadListActivity.this);
					View v=inflater.inflate(R.layout.load_list_page, null);
					ViewPager viewPager = (ViewPager)v. findViewById(R.id.pic_pages);
					viewPager.setAdapter(new GuidePageAdapter());
					convertView=v;
					
				}else{
					TextView tv = new TextView(LoadListActivity.this);
					tv.setText(data.get(position-1));
					convertView=tv;
				}
				return convertView;
			}

			public long getItemId(int position) {
				return 0;
			}

			public Object getItem(int position) {
				return null;
			}

			public int getCount() {
				return data.size()+1;
			}
		};
		listView.setAdapter(adapter);

		listView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						data.add("刷新后添加的内容");
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						adapter.notifyDataSetChanged();
						listView.onRefreshComplete();
					}

				}.execute();
			}
		});
	}
	class GuidePageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).addView(pageViews.get(arg1));
			return pageViews.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub

		}
	}
}