package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.DirViewFragment;
import com.mipt.mediacenter.center.MediaCenterApplication;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.ActivitiesManager;
import com.mipt.mediacenter.utils.Util;

/**
 * @author fang
 * @version $Id: 2013-01-21 09:26:01Z slieer $ 
 */
public class FileMainActivity extends Activity {
    private static final String TAG = "FileActivity";
    private static final String DATA_BUNDEL = "data_bundel";
    private int viewType;
    private DeviceInfo dInfo;
    //private ArrayList<FileInfo> dataList;
    //private Context cxt;
    //private LinearLayout progressBar;
    //private boolean isCheck;
    private String[] tyeStr;
    private FileSortHelper fsortHelper;
    private TextView currentPath;
    private TextView viewTypeTag;
    private boolean isUserPause;
    private TextView currentNum;
    
    private LinearLayout menuLayout;
    private FrameLayout mmLayout;
    private String[] menuListItem;
    private boolean menuShow = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cm_file_list);
        ((LinearLayout) findViewById(R.id.tail_other_tag))
                .setVisibility(View.VISIBLE);
        //cxt = FileMainActivity.this;
        currentPath = (TextView) findViewById(R.id.current_path_tag);
        viewTypeTag = (TextView) findViewById(R.id.view_type_tag);
        currentNum = (TextView) findViewById(R.id.current_num_tag);
        dInfo = (DeviceInfo) getIntent().getSerializableExtra(
                MediacenterConstant.INTENT_EXTRA);
       if (savedInstanceState != null) {
           dInfo = (DeviceInfo) savedInstanceState.getBundle(DATA_BUNDEL).get(
                    MediacenterConstant.INTENT_EXTRA);
        }
        fsortHelper = FileSortHelper.getInstance();
        //dataList = MediaCenterApplication.getInstance().getData();
        //albumList = MediaCenterApplication.getInstance().getAlbumData();
        //progressBar = (LinearLayout) findViewById(R.id.cm_progress_small);
        ActivitiesManager.getInstance().registerActivity(
                ActivitiesManager.ACTIVITY_FILE_VIEW, this);
        
        posPath = null;
        isUserPause = false;
        //isCheck = false;
        Log.i(TAG, "viewType:" + viewType + ",dInfo:" + dInfo);
        addFragmentToStack(-1, dInfo);

        //add right menu
        menuListItem = getResources().getStringArray(R.array.items_fm);
        LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        mmLayout = (FrameLayout) mInflater.inflate(R.layout.pp_menu_layout,
                menuLayout);        
        ListView menuList = (ListView) mmLayout.findViewById(R.id.menuList);
        menuList.setAdapter(new ToolAdapter(this, R.id.tool_name, menuListItem));
        menuLayout = (LinearLayout)findViewById(R.id.menu_layout);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle budle = new Bundle();
        budle.putSerializable(MediacenterConstant.INTENT_EXTRA, dInfo);
        outState.putBundle(DATA_BUNDEL, budle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        dInfo = (DeviceInfo) intent
                .getSerializableExtra(MediacenterConstant.INTENT_EXTRA);       
        posPath = null;
        isUserPause = false;
        addFragmentToStack(viewType, dInfo);
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {        
        tyeStr = new String[] { this.getString(R.string.file_view_type),
                this.getString(R.string.all_file_view_type) };

        if (posPath != null
                && viewType != MediacenterConstant.FileViewType.VIEW_DIR
                && dInfo.type != DeviceInfo.TYPE_DLAN && !isUserPause) {
        }
        super.onResume();
    }
    
    private void addFragmentToStack(int _viewTpe, DeviceInfo _dInfo) {
        MediaCenterApplication.getInstance().resetData();
        Fragment fg = getFragmentManager().findFragmentById(R.id.file_content);
        if (fg != null) {
            fg.onDetach();
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment newFragment = null;
        if(_dInfo.type == DeviceInfo.TYPE_LOCAL || _dInfo.type == DeviceInfo.TYPE_USB) {
            currentPath.setText(Util.handlePath(dInfo.devPath));

            Log.i(TAG, "view local  file info....");
            viewTypeTag.setText(getString(R.string.all_file_view_type));
            newFragment = DirViewFragment.newInstance(_dInfo.devPath, _dInfo.type);
        }else{
            //smb
            //......
        }
        ft.replace(R.id.file_content, newFragment);
        ft.commit();        
    }

    public interface IBackPressedListener {
        boolean onBack();
    }

	public interface FileInfoCallback {
		public void fileInfoLoaded(String reallyName, String filePath);
	}

    public interface DataChangeListener {
        void dataChange();

        void setBackPos(final int pos, final String path);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) getFragmentManager()
                .findFragmentById(R.id.file_content);
        if (backPressedListener != null && !backPressedListener.onBack()) {
            super.onBackPressed();
        }        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_MENU:
                showMenu();
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }        
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ActivitiesManager.getInstance().unRegisterActivity(
                ActivitiesManager.ACTIVITY_FILE_VIEW);
    }
    
    private void showMenu() {        
        Log.i(TAG, "showMenu");
        if (menuShow) {
            hideMenu(true);
            return;
        }
        mmLayout.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.vp_menu_in));
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        menuLayout.addView(mmLayout, layoutParams);
        menuLayout.requestFocus();
        menuShow = true;
    }
    
    private void hideMenu(boolean isAnimation) {
        Log.i(TAG, "hideMenu");
        if (!menuShow) {
            return;
        }
        if (isAnimation) {
            mmLayout.setAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.vp_menu_out));
        }
        menuLayout.removeView(mmLayout);
        menuShow = false;
    }
    
    private class ToolAdapter extends ArrayAdapter<String>{
        public ToolAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) FileMainActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater
                        .inflate(R.layout.vp_tool_list_item, null);
            }
            TextView name = (TextView) convertView.findViewById(R.id.tool_name);
            //TextView value = (TextView) convertView.findViewById(R.id.tool_value);
            name.setText(menuListItem[position]);
            name.setTextColor(getContext().getResources().getColor(R.color.white));
            
            return convertView;
        }
    }
    
    class MenuOnItemClickListener implements OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Toast.makeText(FileMainActivity.this, "onitemclick...", Toast.LENGTH_SHORT).show();
            switch (arg2) {
                case 0:
                    //排序方式
                    
                    break;
                case 1:
                    //文件操作,进入文件选择模式
                    
                    break;
                default:
                    break;
            }
        }        
    }
    
    
    public DeviceInfo getCurrentDeviceInfo() {
        return dInfo;
    }

    class ViewTypeChooseDialog extends Dialog {
        private OnItemClickListener listener;
        private Context context;
        private String[] mStrings;
        private ArrayAdapter<String> adapter;
        private String lastPosName;

        public ViewTypeChooseDialog(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.context = context;
        }

        public ViewTypeChooseDialog(Context context, int theme) {
            super(context, theme);
            this.context = context;
        }

        public ViewTypeChooseDialog(Context context, int theme,
                OnItemClickListener listener, final String[] strs,
                final String _lastPos) {
            super(context, theme);
            this.context = context;
            this.listener = listener;
            mStrings = strs;
            lastPosName = _lastPos;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.cm_pop_dialog);
            iniUI();
        }

        private void iniUI() {
            ListView lv = (ListView) findViewById(R.id.tpe_item_select);
            lv.setDividerHeight(0);
            adapter = new ArrayAdapter<String>(context, R.layout.cm_pop_item,
                    mStrings);

            // adapter.get
            lv.setAdapter(adapter);
            lv.setSelection(getPos(lastPosName, mStrings));
            lv.setOnItemClickListener(listener);
        }

        private int getPos(String posName, String[] names) {
            for (int i = 0; i < names.length; i++) {
                if (posName.endsWith(names[i])) {
                    return i;
                }
            }
            return 0;
        }
    }

/*    static final int MESSAGE_SCAN_BEGIN = 101;
    static final int MESSAGE_SCAN_END = 102;
    static final int MESSAGE_SCAN_END_NO_DATA = 103;
    static final int MESSAGE_CHANGE_DATA = 104;
    static final int MESSAGE_SET_POS = 105;
*/    // boolean isCheck = true;

    public ArrayList<FileInfo> handleTreeList(ArrayList<FileInfo> _orginList,
            Comparator<FileInfo> comparator) {
        ArrayList<FileInfo> orginList = new ArrayList<FileInfo>();
        orginList.addAll(_orginList);
        ArrayList<FileInfo> returnList = new ArrayList<FileInfo>();
        for (FileInfo fi : orginList) {
            String date = Util.formatDateString(fi.modifiedDate);
            if (isHas(date, returnList)) {
                ArrayList<FileInfo> child = getChilelist(date, returnList);
                if (!isHasFilePath(fi.filePath, child)) {
                    child.add(fi);
                }
            } else {
                FileInfo nFile = new FileInfo();
                nFile.fileName = date;
                nFile.fileId = fi.fileId;
                ArrayList<FileInfo> child = new ArrayList<FileInfo>();
                child.add(fi);
                nFile.childs = child;
                returnList.add(nFile);
            }
        }
        // Collections.sort(returnList, comparator);
        for (FileInfo f : returnList) {
            // Collections.sort(f.childs, comparator);
            f.count = f.childs.size();
        }
        return returnList;
    }

    private boolean isHas(String name, ArrayList<FileInfo> list) {
        boolean isHas = false;
        if (name == null || "".equals(name.trim()) || list == null
                || list.isEmpty()) {
            return isHas;
        }
        for (FileInfo fi : list) {
            if (name.equals(fi.fileName)) {
                isHas = true;
                break;
            }
        }
        return isHas;
    }

    private boolean isHasFilePath(String filePath, ArrayList<FileInfo> list) {
        boolean isHas = false;
        if (filePath == null || "".equals(filePath.trim()) || list == null
                || list.isEmpty()) {
            return isHas;
        }
        for (FileInfo fi : list) {
            if (filePath.equals(fi.filePath)) {
                isHas = true;
                break;
            }
        }
        return isHas;
    }

    private ArrayList<FileInfo> getChilelist(String name,
            ArrayList<FileInfo> list) {
        for (FileInfo fi : list) {
            if (name.equals(fi.fileName)) {
                return fi.childs;
            }
        }
        return null;
    }

    private String getSdCardsPath(String orginPath) {
        String selectPath = orginPath;
        if (selectPath.indexOf("/sdcard") == 0) {
            selectPath = "/mnt" + selectPath;
        }
        return selectPath;
    }
    
    String posPath = null;

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.i(TAG,
                "0000onActivityResult00000000000000000000000000requestCode:"
                        + requestCode + ",resultCode:" + resultCode);
        if (requestCode == MediacenterConstant.ACTIVITYR_RESULT_CODE) {
        }
    }

    public void resetCurrentNum() {
        currentNum.setText("0/0");
    }
    public void setCurrentNum(String num) {
        currentNum.setText(num);
    }
    public void setCurrentPath(String path) {
        currentPath.setText(path);
    }
}
