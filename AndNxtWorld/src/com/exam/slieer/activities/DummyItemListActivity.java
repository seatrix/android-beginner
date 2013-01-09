
package com.exam.slieer.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.exam.slieer.R;
import com.exam.slieer.ui.fragment.DummyItemDetailFragment;
import com.exam.slieer.ui.fragment.DummyItemListFragment;

/**
 * 程序入口, FragmentActivity 在内部的某个ViewGroup内动态添加或替代一个Fragment
 */
public class DummyItemListActivity extends FragmentActivity implements
        DummyItemListFragment.Callbacks {
    private final static String TAG = "ItemListActivity";
    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*左侧带导航模式*/
        setContentView(R.layout.activity_item_twopane);
        fragmentManager = getSupportFragmentManager();
        DummyItemListFragment itemListFragment = (DummyItemListFragment)fragmentManager
                .findFragmentById(R.id.item_list);
        itemListFragment.setActivateOnItemClick(true);
    }

    @Override
    public void onItemSelected(String id) {
        DummyItemDetailFragment fragment = new DummyItemDetailFragment();

        /* 在FragmentActivity内，切换Fragment. */
        Bundle arguments = new Bundle();
        arguments.putString(DummyItemDetailFragment.ARG_ITEM_ID, id);
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
            /*一个fragment替换为另一个, 并在后台堆栈中保留之前的状态，
             * 通过调用 addToBackStack(), replace事务被保存到back stack, 
             * 用户通过按下BACK按键带回前一个fragment.
             * */
            .addToBackStack(null)
            .replace(R.id.item_detail_container, fragment).commit();
    }
}
