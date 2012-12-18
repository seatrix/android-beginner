package com.exam.slieer;

import com.example.helloworld.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

/**
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 * 
 * 程序入口, 
 * FragmentActivity 在内部的某个ViewGroup内动态添加或替代一个Fragment
 */
public class ItemListActivity extends FragmentActivity implements
		ItemListFragment.Callbacks {
	private final static String TAG = "ItemListActivity";
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*左侧带导航模式
		 */
		setContentView(R.layout.activity_item_twopane);		
		mTwoPane = true;
		// In two-pane mode, list items should be given the
		// 'activated' state when touched.
		FragmentManager fragmentManager = getSupportFragmentManager();
		ItemListFragment itemListFragment = (ItemListFragment) fragmentManager.findFragmentById(R.id.item_list);
		itemListFragment.setActivateOnItemClick(true);
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
			ItemDetailFragment fragment = new ItemDetailFragment();
			
			/*在FragmentActivity内，切换Fragment.*/
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();

		} else {
			Log.i(TAG, "In single-pane mode");
		}
	}
}
