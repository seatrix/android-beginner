
package com.exam.slieer.activities.menu;

import com.exam.slieer.R;
import com.exam.slieer.ui.TabMenu;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class PopupMenuActivity extends Activity {
    TabMenu.MenuBodyAdapter bodyAdapter = new TabMenu.MenuBodyAdapter(this, new int[] {
            R.drawable.menu_01,
            R.drawable.menu_02,
            R.drawable.menu_03,
            R.drawable.menu_04
    });
    
    TabMenu tabMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_menu);
        tabMenu = new TabMenu(this, new BodyClickEvent(), R.drawable.menu_bg);// 出现与消失的动画
        tabMenu.update();
        tabMenu.SetBodyAdapter(bodyAdapter);
    }

    class BodyClickEvent implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            tabMenu.SetBodySelect(arg2, Color.GRAY);
            Log.i("Log", " BodyClickEvent implements OnItemClickListener " + arg2);
        }
    }

    /**
     * 创建MENU
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");// 必须创建一项
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 拦截MENU
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (tabMenu != null) {
            if (tabMenu.isShowing())
                tabMenu.dismiss();
            else {
                tabMenu.showAtLocation(findViewById(R.id.LinearLayout01), Gravity.BOTTOM, 0, 0);
            }
        }
        return false;// 返回为true 则显示系统menu
    }
}
