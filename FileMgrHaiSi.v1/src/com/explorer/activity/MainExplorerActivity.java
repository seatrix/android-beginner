package com.explorer.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView.OnChildClickListener;

import com.explorer.R;
import com.explorer.common.CommonActivity;
import com.explorer.common.ControlListAdapter;
import com.explorer.common.ExpandAdapter;
import com.explorer.common.FileAdapter;
import com.explorer.common.FileUtil;
import com.explorer.common.FilterType;
import com.explorer.common.GroupInfo;
import com.explorer.common.MountInfo;
import com.explorer.common.NewCreateDialog;
import com.explorer.common.SocketClient;

import android.net.Uri;

import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.IBinder;

/**
 * 本地浏览文件
 *
 * @author liu_tianbao
 */
public class MainExplorerActivity extends CommonActivity {

    private static final String TAG = "MainExplorerActivity";

    // 父目录路径
    private String parentPath = "";

    // 操作文件列表
    private List<File> fileArray = null;

    // 盘符
    private String directorys = "/sdcard";

    // 文件列表集合
    //    private List<File> li = null;

    // 文件集合
//    private List<File> listFile;

    // 文件列表的点击位置
    private int myPosition = 0;

    // 输入框字符串起始位置
    int Num = 0;

    // 输入框字符串长度
    int tempLength = 0;

    // 选中的文件列表
    List<String> selectList = null;

    // 操作列表
    ListView list;

    // 挂载列表
    List<Map<String, Object>> sdlist;

    // 点击次数
    int clickCount = 0;

    // 操作对话框
    AlertDialog dialog;

    // 存放点击位置集合
    List<Integer> intList;

    // 需要排序的文件列表
    File[] sortFile;

    // 设备标签
    final static String MOUNT_LABLE = "mountLable";

    // 设备类型
    final static String MOUNT_TYPE = "mountType";

    // 设备路径
    final static String MOUNT_PATH = "mountPath";

    // 设备卷标
    final static String MOUNT_NAME = "mountName";

    // 数据适配器
//    FileAdapter adapter;

    // 操作索引
    int menu_item = 0;

    // 树形控件
    ExpandableListView expandableListView;

    // 设备类型节点集合
    List<GroupInfo> groupList;

    // 设备子节点集合
    List<Map<String, String>> childList;

    // 设备列表类别位置
    int groupPosition = -1;

    // 索引显示
    TextView numInfo;;

    // Intent从VP传输
    boolean subFlag = false;

    // 文件工具
    FileUtil util;

    // 加载JNI库
    static {
        System.loadLibrary("android_runtime");
    }

    // iso文件所在的当前路径
    private String isoParentPath = new String();

    /**
     * 加载页面文件
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        // 填充默认的过滤类型
        FilterType.filterType(MainExplorerActivity.this);
        init();
        selectList = new ArrayList<String>();
        getUSB();
    }

    /**
     * 初始化控件
     */
    private void init() {

        showBut = (ImageButton) findViewById(R.id.showBut);

        sortBut = (ImageButton) findViewById(R.id.sortBut);

        filterBut = (ImageButton) findViewById(R.id.filterBut);

        intList = new ArrayList<Integer>();
        listFile = new ArrayList<File>();
        gridlayout = R.layout.gridfile_row;
        listlayout = R.layout.file_row;
        listView = (ListView) findViewById(R.id.listView);
        gridView = (GridView) findViewById(R.id.gridView);

        pathTxt = (TextView) findViewById(R.id.pathTxt);

        numInfo = (TextView) findViewById(R.id.ptxt);

        expandableListView = (ExpandableListView) findViewById(R.id.expandlistView);
        getsdList();

        isNetworkFile = false;
    }

    /**
     * 获得挂载列表
     */
    public void getsdList() {

        getMountEquipmentList();
        ExpandAdapter adapter = new ExpandAdapter(this, groupList);
        expandableListView.setAdapter(adapter);
        Log.e(TAG,"==== zhl [getsdList]=== groupList.size()="+groupList.size());
        for (int i = 0; i < groupList.size(); i++) {
            expandableListView.expandGroup(i);
        }

        expandableListView.setOnChildClickListener(new OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
                String path = groupList.get(groupPosition).getChildList().get(
                        childPosition).get(MOUNT_PATH);
                mountSdPath = path;
                Log.e("PATJH", "PATJH = " + path);
                MainExplorerActivity.this.groupPosition = groupPosition;
                arrayFile.clear();
                arrayDir.clear();
                directorys = path;
                expandableListView.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
                listFile.clear();
                clickPos = 0;
                myPosition = 0;
                // FileAdapter adapter = new FileAdapter(
                // MainExplorerActivity.this, li, listlayout);
                // listView.setAdapter(adapter);
                geDdirectory(directorys);
                intList.add(childPosition);
                updateList(true);
                return false;
            }
        });
    }

    /**
     * 创建菜单
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // 显示、隐藏标签
        menu.add(Menu.NONE, MENU_TAB, 0, getString(R.string.hide_tab));
        // 操作文件
        SubMenu operatFile = menu.addSubMenu(Menu.NONE, Menu.NONE, 0,
                getString(R.string.operation));
        operatFile.add(Menu.NONE, MENU_COPY, 0, getString(R.string.copy));
        operatFile.add(Menu.NONE, MENU_CUT, 0, getString(R.string.cut));
        operatFile.add(Menu.NONE, MENU_PASTE, 0, getString(R.string.paste));
        operatFile.add(Menu.NONE, MENU_DELETE, 0, getString(R.string.delete));
        operatFile.add(Menu.NONE, MENU_RENAME, 0,
                getString(R.string.str_rename));
        // operatFile.add(Menu.NONE, MENU_UPLOAD, 0,
        // getString(R.string.str_upload));

        // 新建、搜索、帮助
        menu.add(Menu.NONE, MENU_ADD, 0, getString(R.string.str_new));
        menu.add(Menu.NONE, MENU_SEARCH, 0, getString(R.string.search));
        // 添加过滤类型
        SubMenu addFilter = menu.addSubMenu(Menu.NONE, Menu.NONE, 0,
                getString(R.string.add_filter));
        addFilter.add(Menu.NONE, ADD_MENU_AUDIO, 0, getString(R.string.music));
        addFilter.add(Menu.NONE, ADD_MENU_VIDEO, 0, getString(R.string.video));
        addFilter.add(Menu.NONE, ADD_MENU_IMAGE, 0, getString(R.string.image));

        // 删除过滤类型
        SubMenu removeFilter = menu.addSubMenu(Menu.NONE, Menu.NONE, 0,
                getString(R.string.remove_filter));
        removeFilter.add(Menu.NONE, REMOVE_MENU_AUDIO, 0,
                getString(R.string.music));
        removeFilter.add(Menu.NONE, REMOVE_MENU_VIDEO, 0,
                getString(R.string.video));
        removeFilter.add(Menu.NONE, REMOVE_MENU_IMAGE, 0,
                getString(R.string.image));
        menu.add(Menu.NONE, MENU_HELP, 0, getString(R.string.help));
        return true;
    };

    /**
     * 控制菜单的状态
     */
    public boolean onPrepareOptionsMenu(Menu menu) {

        // 隐藏菜单项显示字符
        if (TabBarExample.getWidget().isShown()) {
            menu.getItem(0).setTitle(R.string.hide_tab);
        } else {
            menu.getItem(0).setTitle(R.string.show_tab);
        }
        SharedPreferences share = getSharedPreferences("OPERATE", SHARE_MODE);
        // 粘贴板中是否有内容
        int num = share.getInt("NUM", 0);
        if (!pathTxt.getText().toString().equals("")) {
            menu.getItem(4).setEnabled(true);
            menu.getItem(5).setEnabled(true);
            // 文件列表为空
            if (listFile.size() == 0) {
                menu.getItem(1).getSubMenu().getItem(0).setEnabled(false);
                menu.getItem(1).getSubMenu().getItem(1).setEnabled(false);
                menu.getItem(1).getSubMenu().getItem(3).setEnabled(false);
                menu.getItem(1).getSubMenu().getItem(4).setEnabled(false);
                // menu.getItem(3).getSubMenu().getItem(5).setEnabled(false);
                if (num == 0) {
                    menu.getItem(1).setEnabled(false);
                } else {
                    menu.getItem(1).setEnabled(true);
                    menu.getItem(1).getSubMenu().getItem(2).setEnabled(true);
                }
                menu.getItem(2).setEnabled(true);
                menu.getItem(3).setEnabled(false);
            }
            // 文件列表不为空
            else {
                menu.getItem(1).setEnabled(true);
                menu.getItem(1).getSubMenu().getItem(0).setEnabled(true);
                menu.getItem(1).getSubMenu().getItem(1).setEnabled(true);
                menu.getItem(1).getSubMenu().getItem(3).setEnabled(true);
                menu.getItem(1).getSubMenu().getItem(4).setEnabled(true);
                // if (arrayFile.size() == 0) {
                // menu.getItem(3).getSubMenu().getItem(5).setEnabled(false);
                // } else {
                // menu.getItem(3).getSubMenu().getItem(5).setEnabled(true);
                // }
                // 粘贴菜单控制
                if (num == 0) {
                    menu.getItem(1).getSubMenu().getItem(2).setEnabled(false);
                } else {
                    menu.getItem(1).getSubMenu().getItem(2).setEnabled(true);
                }
                menu.getItem(1).setEnabled(true);
                menu.getItem(2).setEnabled(true);
                menu.getItem(3).setEnabled(true);
            }
        } else {
            menu.getItem(1).setEnabled(false);
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(false);
            menu.getItem(4).setEnabled(false);
            menu.getItem(5).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单操作
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // invisiable();
        switch (item.getItemId()) {
        // 新建
        case MENU_ADD:
            FileUtil util = new FileUtil(this);
            util.createNewDir(currentFileString);
            break;
        // 搜索
        case MENU_SEARCH:
            searchFileDialog();
            break;
        // 剪切
        case MENU_CUT:
            managerF(myPosition, MENU_CUT);
            break;
        // 粘贴
        case MENU_PASTE:
            managerF(myPosition, MENU_PASTE);
            break;
        // 删除
        case MENU_DELETE:
            managerF(myPosition, MENU_DELETE);
            break;
        // 重命名
        case MENU_RENAME:
            managerF(myPosition, MENU_RENAME);
            break;
        // 拷贝
        case MENU_COPY:
            managerF(myPosition, MENU_COPY);
            break;
        // 帮助
        case MENU_HELP:
            FileMenu.setHelpFlag(1);
            FileMenu.filterType(MainExplorerActivity.this, MENU_HELP, null);
            break;
        // // 上传
        // case MENU_UPLOAD:
        // managerF(myPosition, MENU_UPLOAD);
        // break;
        }
        return true;
    }

    /**
     * 文件列表点击事件
     */
    private OnItemClickListener ItemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            // 如果是文件夹，记录点击位置
            if(listFile.size() > 0) {
                if(position >= listFile.size()){
                    position = listFile.size()-1;
                }
                // begin add by qian_wei/zhou_yong 2011/10/20
                // for chmod the file
                //chmodFile(listFile.get(position).getPath());
                // end modify by qian_wei/zhou_yong 2011/10/20
                if (listFile.get(position).isDirectory() && listFile.get(position).canRead()) {
                    intList.add(position);
                    clickPos = 0;
                } else {
                    clickPos = position;
                }
                myPosition = position;
                arrayFile.clear();
                arrayDir.clear();
                // begin modify by qian_wei/xiong_cuifan 2011/11/05
                // for broken into the directory contains many files,click again error
                preCurrentPath = currentFileString;
                keyBack = false;
                // end modify by qian_wei/xiong_cuifan 2011/11/08
                getFiles(listFile.get(position).getPath());
            }
        }
    };

    /**
     * 对话框中列表点击事件
     */
    private OnItemClickListener deleListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            ControlListAdapter adapter = (ControlListAdapter) list.getAdapter();
            CheckedTextView check = (CheckedTextView) v
                    .findViewById(R.id.check);
            String path = adapter.getList().get(position).getPath();
            // 记录点击的文件位置
            if (check.isChecked()) {
                selectList.remove(path);
                check.setChecked(false);
            } else {
                selectList.add(path);
                check.setChecked(true);
            }
        }
    };

    /**
     * 根据文件路径判断执行的操作 目录:进入目录 文件:系统应用打开文件
     *
     * @param path
     *            文件路径
     */
//    SocketClient socketClient = null;
//    private File openFile;
//    private String prevPath = "";

    public void getFiles(String path) {
        openFile = new File(path);
        if (openFile.exists()) {
            if (openFile.isDirectory()) {
                // begin modify by qian_wei/xiong_cuifan 2011/11/05
                // for broken into the directory contains many files,click again error
//                if (currentFileString.length() < path.length()) {
//                    myPosition = 0;
//                }
                // end modify by qian_wei/xiong_cuifan 2011/11/08

            if (mIsSupportBD) {
                if (FileUtil.getMIMEType(openFile, this).equals("video/bd"))
                {
                    launchHiBDPlayer(path);
                    return;
                }
            }


                currentFileString = path;
                updateList(true);
            } else {
                if (FileUtil.getMIMEType(openFile, this).equals("video/iso")) {
                    prevPath = openFile.getParent();
                    // begin modify by yuejun 2011/12/21
                    isoParentPath = openFile.getPath();
                    // end modify by yuejun 2011/12/21
                    socketClient = new SocketClient(this);
                    progress = new ProgressDialog(this);
                    progress.show();
                    try
                    {
						String mntPath = getMountService().mountISO(openFile.getPath());
                      socketClient.writeMess("mountiso " + super.tranString(openFile.getPath()) + " " + mntPath);
                      mBDISOName = openFile.getName();
                      mBDISOPath = openFile.getAbsolutePath();
                    }catch(Exception e)
                    {
                        Log.e(TAG," error e="+e);
                        FileUtil.showToast(this, "mountiso file error");
                    }

                } else {
                    super.openFile(this, openFile);
                }
            }
        } else {
            refushList();
        }
    };

    /**
     * 将文件列表填充到数据容器中
     *
     * @param files
     *            文件列表
     * @param fileroot
     *            文件目录
     */
    public void fill(File fileroot) {
        try {
//            li = adapter.getFiles();
            // 设置路径文件框字体颜色
            if(clickPos >= listFile.size()) {
                clickPos = listFile.size() -1;
            }
            pathTxt.setText(fileroot.getPath());
            numInfo.setText((clickPos + 1) + "/" + listFile.size());
            if (!fileroot.getPath().equals(directorys)) {
                parentPath = fileroot.getParent();
                currentFileString = fileroot.getPath();
            } else {
                currentFileString = directorys;
            }

            if (listFile.size() == 0) {
                numInfo.setText(0 + "/" + 0);
            }

            if ((listFile.size() == 0) && (showBut.findFocus() == null)
                    && (filterBut.findFocus() == null)) {
                sortBut.requestFocus();
            }

            if(clickPos >= 0) {
            if (listView.getVisibility() == View.VISIBLE) {
                listView.requestFocus();
                listView.setSelection(clickPos);
            } else if (gridView.getVisibility() == View.VISIBLE) {
                gridView.requestFocus();
                gridView.setSelection(clickPos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 管理文件操作
     *
     * @param position
     *            操作文件在列表中位置
     * @param item
     *            操作类型
     */
    private void managerF(final int position, final int item) {

        // begin modify by qian_wei/cao_shanshan 2011/10/24
        // for while first delete more than one file then cause exception
//        if(position == listFile.size()){
        if(position >= listFile.size()){
        //YLY modify
            if(listFile.size() != 0)
            {
                myPosition = listFile.size()-1;
            }
            else
            {
                myPosition = 0;
            }
         //YLY modify end
        }
        // end modify by qian_wei/cao_shanshan 2011/10/24

        // flagItem = item;

        // if (item != MENU_PASTE) {
        // if (li.size() == 0) {
        // return;
        // }
        // }

        // LayoutInflater in = (LayoutInflater)
        // getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // View view = (View) in.inflate(R.layout.dele, null);
        // list = (ListView) view.findViewById(R.id.list);
        // okBut = (Button) view.findViewById(R.id.okBut);
        //
        // // okBut.setOnClickListener(imageButClick);
        // // 响应ENTER或者DPAD_CENTER键
        // okBut.setOnKeyListener(new OnKeyListener() {
        // public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode ==
        // KeyEvent.KEYCODE_DPAD_CENTER) {
        // if (selectList.size() > 0) {
        // // getMenu(myPosition, flagItem, list);
        // getMenu(myPosition, item, list);
        // dialog.cancel();
        // } else {
        // FileUtil.showToast(MainExplorerActivity.this,
        // MainExplorerActivity.this.getString(R.string.select_file));
        // }
        //
        // }
        // return false;
        // }
        // });
        // cancleBut = (Button) view.findViewById(R.id.cancleBut);
        // cancleBut.setOnClickListener(imageButClick);
        // // 让列表为多选模式
        // list.setItemsCanFocus(false);
        // list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // selectList.clear();
        // 上传文件集合
        // if (item == MENU_UPLOAD) {
        // uploadFile = new ArrayList<File>();
        // for (File file : li) {
        // if (file.isFile()) {
        // uploadFile.add(file);
        // }
        // }
        // list.setAdapter(new ControlListAdapter(MainExplorerActivity.this,
        // uploadFile,
        // R.layout.control_row));
        // int pos = myPosition - arrayDir.size();
        // if (pos >= 0) {
        // list.setItemChecked(pos, true);
        // list.setSelection(pos);
        // selectList.add(uploadFile.get(pos).getPath());
        // }
        // } else
        // if ((item != MENU_PASTE) && (item != MENU_RENAME)) {
        // list.setAdapter(new ControlListAdapter(MainExplorerActivity.this, li,
        // R.layout.control_row));
        // list.setItemChecked(myPosition, true);
        // list.setSelection(myPosition);
        // selectList.add(li.get(position).getPath());
        // }
        //
        // list.setOnItemClickListener(deleListener);

        menu_item = item;
        // 执行具体操作
        if ((item == MENU_PASTE) || (item == MENU_RENAME)) {
            if(currentFileString.startsWith("/mnt/nand")){
                //socketClient = new SocketClient(this);
                //Log.e(TAG,"==== zhl [managerF] currentFileString="+currentFileString);
                //socketClient.writeMess("system /system/busybox/bin/chmod -R 777 " +super.tranString(currentFileString));
                getMenu(myPosition, menu_item, list);
            } else {
                getMenu(myPosition, menu_item, list);
            }
        } else {

            /**
             * @tag : begin modify by qian_wei 2011/7/5
             * @brief : 支持遥控器左右键移到到确定、取消按钮上
             */
            LayoutInflater inflater = LayoutInflater
                    .from(MainExplorerActivity.this);
            View view = inflater.inflate(R.layout.samba_server_list_dlg_layout,
                    null);
            dialog = new NewCreateDialog(MainExplorerActivity.this);
            dialog.setView(view);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.ok), imageButClick);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.cancel), imageButClick);
            dialog.show();
            list = (ListView) view.findViewById(R.id.lvSambaServer);
            dialog = FileUtil
                    .setDialogParams(dialog, MainExplorerActivity.this);

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextAppearance(MainExplorerActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextAppearance(MainExplorerActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);

            // 让列表为多选模式
            list.setItemsCanFocus(false);
            list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            selectList.clear();

            list.setAdapter(new ControlListAdapter(MainExplorerActivity.this,
                    listFile));
            list.setItemChecked(myPosition, true);
            list.setSelection(myPosition);
//            list.setItemChecked(myPosition, true);
//            list.setSelection(myPosition);
            selectList.add(listFile.get(myPosition).getPath());
            list.setOnItemClickListener(deleListener);

            list.clearFocus();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();
        }
    }

    /**
     * 操作菜单
     *
     * @param position
     *            目标文件位置
     * @param item
     *            操作
     * @param list
     *            数据容器
     */
    private void getMenu(final int position, final int item, final ListView list) {
        int selectionRowID = (int) position;
        File file = null;
        File myFile = null;
        myFile = new File(currentFileString);
        FileMenu menu = new FileMenu();
        SharedPreferences sp = getSharedPreferences("OPERATE", SHARE_MODE);

        // 重命名操作
        if (item == MENU_RENAME) {
            fileArray = new ArrayList<File>();
            // if (!currentFileString.equals("/")) {
            file = new File(currentFileString + "/"
                    + listFile.get(selectionRowID).getName());
            // } else {
            // file = new File(currentFileString + items.get(selectionRowID));
            // }
            fileArray.add(file);
            menu.getTaskMenuDialog(MainExplorerActivity.this, myFile,
                    fileArray, sp, item, 1);
        }
        // 粘贴操作
        else if (item == MENU_PASTE) {
            fileArray = new ArrayList<File>();
            menu.getTaskMenuDialog(MainExplorerActivity.this, myFile,
                    fileArray, sp, item, 1);
        }
        /*else if (item == MENU_DELETE)
        {
            fileArray = new ArrayList<File>();
            for (int i = 0; i < selectList.size(); i++)
            {
                file = new File(selectList.get(i));
                fileArray.add(file);
                Uri uri = Uri.parse("file://" + file.getAbsolutePath());
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                Log.e("tangxiaodi","-------------------------delete file="+file.getAbsolutePath());
            }
            menu.getTaskMenuDialog(MainExplorerActivity.this, myFile,
                    fileArray, sp, item, 1);
        }*/
        // 其余操作
        else {
            fileArray = new ArrayList<File>();
            for (int i = 0; i < selectList.size(); i++) {
                file = new File(selectList.get(i));
                fileArray.add(file);
            }
            menu.getTaskMenuDialog(MainExplorerActivity.this, myFile,
                    fileArray, sp, item, 1);
        }

    }


    private Handler handler = new Handler() {
        public synchronized void handleMessage(Message msg) {
            switch (msg.what) {
            case SEARCH_RESULT:
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }

                synchronized(lock){
                    // 无搜索结果
                    if (arrayFile.size() == 0 && arrayDir.size() == 0) {
                        FileUtil.showToast(MainExplorerActivity.this,
                                getString(R.string.no_search_file));
                        return;
                    } else {
                        updateList(true);
                    }
                }
                break;
            case UPDATE_LIST:
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                if (listView.getVisibility() == View.VISIBLE) {
                    adapter = new FileAdapter(MainExplorerActivity.this,
                            listFile, listlayout);
                    listView.setAdapter(adapter);
                    listView.setOnItemSelectedListener(itemSelect);
                    listView.setOnItemClickListener(ItemClickListener);
                } else if (gridView.getVisibility() == View.VISIBLE) {
                    adapter = new FileAdapter(MainExplorerActivity.this,
                            listFile, gridlayout);
                    gridView.setAdapter(adapter);
                    gridView.setOnItemClickListener(ItemClickListener);
                    gridView.setOnItemSelectedListener(itemSelect);
                }
                fill(new File(currentFileString));
                break;
            // begin modify by yuejun 2011/12/14
            case ISO_MOUNT_SUCCESS:
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                intList.add(myPosition);
                String OpenFilePath = ISO_PATH;
                //BEGIN : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
//                File fileISO = new File(ISO_PATH);
//                File[] ISOList = fileISO.listFiles();
//                for (int i=0; i < ISOList.length; i++)
//                {
//                    if(ISOList[i].getName().equalsIgnoreCase("bdmv"))
//                    {
//                        File[] TempList = ISOList[i].listFiles();
//                        for(int j=0; j < TempList.length; j++)
//                        {
//                            if(TempList[j].getName().equalsIgnoreCase("stream"))
//                            {
//                                OpenFilePath = TempList[j].getPath();
//                            }
//                        }
//                    }
//                    else if(ISOList[i].getName().equalsIgnoreCase("video_ts"))
//                    {
//                        OpenFilePath = ISOList[i].getPath();
//                    }
//                }
                //END : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
                getFiles(OpenFilePath);
                break;
            // end modify by yuejun 2011/12/14
            case ISO_MOUNT_FAILD:
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                FileUtil.showToast(MainExplorerActivity.this,
                        getString(R.string.new_mout_fail));
                break;
            case CHMOD_FILE:
                getMenu(myPosition, menu_item, list);
                break;
            }
        }
    };

    /**
     * 文件列表选择事件
     */
    OnItemSelectedListener itemSelect = new OnItemSelectedListener() {

        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            // invisiable();
            if (parent.equals(listView) || parent.equals(gridView)) {
                myPosition = position;
            }
            numInfo.setText((position + 1) + "/" + listFile.size());
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * 文件列表排序
     *
     * @param sort 排序方式
     */
    public void updateList(boolean flag) {

        if (flag) {
            // begin modify by qian_wei/xiong_cuifan 2011/11/05
            // for broken into the directory contains many files,click again error
            listFile.clear();
            Log.i(TAG, "updateList");
            // 让按钮能够点击
            sortBut.setOnClickListener(clickListener);
            showBut.setOnClickListener(clickListener);
            filterBut.setOnClickListener(clickListener);

            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

            progress = new ProgressDialog(MainExplorerActivity.this);
            progress.show();

            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
            waitThreadToIdle(thread);
            thread = new MyThread();
            thread.setStopRun(false);
            progress.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    thread.setStopRun(true);
                    if(keyBack) {
                        intList.add(clickPos);
                    } else {
                        clickPos = myPosition;
                        currentFileString = preCurrentPath;
                        Log.v("\33[32m Main1","onCancel" + currentFileString +"\33[0m");
                        intList.remove(intList.size()-1);
                    }
                    FileUtil.showToast(MainExplorerActivity.this, getString(R.string.cause_anr));
                }
            });
            // end modify by qian_wei/xiong_cuifan 2011/11/08
            thread.start();
            /*
             * if (listView.getVisibility() == View.VISIBLE) { adapter = new
             * FileAdapter(this, li, listlayout); listView.setAdapter(adapter);
             * listView.setOnItemSelectedListener(itemSelect);
             * listView.setOnItemClickListener(ItemClickListener); } else if
             * (gridView.getVisibility() == View.VISIBLE) {
             * gridView.setOnItemClickListener(ItemClickListener);
             * gridView.setOnItemSelectedListener(itemSelect); adapter = new
             * FileAdapter(this, li, gridlayout); gridView.setAdapter(adapter);
             * }
             */
            // util = new FileUtil(this, filterCount, fileL, currentFileString);
            // util.fillData(sortCount, adapter);
        } else {
            adapter.notifyDataSetChanged();
            fill(new File(currentFileString));
        }

    }

    /**
     * 获得目录下文件集合
     *
     * @param path
     *            文件路径
     */
    private void geDdirectory(String path) {
        directorys = path;
        parentPath = path;
        currentFileString = path;
    }

    /**
     * 前进、回退、刷新按钮处理事件
     */
    DialogInterface.OnClickListener imageButClick = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Log.d("=====", "=====");
                if (selectList.size() > 0) {
//                    if(currentFileString.startsWith("/mnt/nand")){
//                        //socketClient = new SocketClient(MainExplorerActivity.this);
//                        //Log.e(TAG,"==== zhl [imageButClick] currentFileString="+currentFileString);
//                        //socketClient.writeMess("system /system/busybox/bin/chmod -R 777 " +MainExplorerActivity.super.tranString(currentFileString));
//                        getMenu(myPosition, menu_item, list);
//                    } else {
//                        getMenu(myPosition, menu_item, list);
//                    }
                    getMenu(myPosition, menu_item, list);
                    dialog.cancel();
                } else {
                    FileUtil.showToast(MainExplorerActivity.this,
                            MainExplorerActivity.this
                                    .getString(R.string.select_file));
                }
            } else {
                dialog.cancel();
            }
        }
    };

    /**
     * 重写keyDown事件
     */
    int clickPos = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.w(" = ", " = " + keyCode);
        switch (keyCode) {
        // 添加对enter与dpad_center键出发同一功能
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_CENTER:
            super.onKeyDown(KeyEvent.KEYCODE_ENTER, event);
            return true;

        case KeyEvent.KEYCODE_BACK:// KEYCODE_BACK
            keyBack = true;
            String newName = pathTxt.getText().toString();
            // 当前目录是根目录
            if (newName.equals("")) {
                clickCount++;
                if (clickCount == 1) {
                    // begin modify by qian_wei/cao_shanshan 2011/10/24
                    // for not choice the srt file then quit the FileM
//                    FileUtil.showToast(MainExplorerActivity.this,
//                            getString(R.string.quit_app));
                    if (getIntent().getBooleanExtra("subFlag", false)) {
                        Intent intent = new Intent();
                        intent.setClassName("com.huawei.activity",
                                "com.huawei.activity.VideoActivity");
                        intent.putExtra("path", "");
                        intent.putExtra("pathFlag", false);
                        startActivity(intent);
                        finish();
                    } else {
                        FileUtil.showToast(MainExplorerActivity.this,
                                getString(R.string.quit_app));
                    }
                } else if (clickCount == 2) {
//                    if (getIntent().getBooleanExtra("subFlag", false)) {
//                        Intent intent = new Intent();
//                        intent.setClassName("com.huawei.activity",
//                                "com.huawei.activity.VideoActivity");
//                        intent.putExtra("path", "");
//                        intent.putExtra("pathFlag", false);
//                        startActivity(intent);
//                        finish();
//                    } else {
                        // 清空剪贴板内容
                        SharedPreferences share = getSharedPreferences(
                                "OPERATE", SHARE_MODE);
                        share.edit().clear().commit();
                        if (FileUtil.getToast() != null) {
                            FileUtil.getToast().cancel();
                        }
                        onBackPressed();
//                    }
                    // end modify by qian_wei/cao_shanshan 2011/10/24
                }
            } else {
                clickCount = 0;
                if (!currentFileString.equals(directorys)) {
                    arrayDir.clear();
                    arrayFile.clear();
                    if (newName.equals(ISO_PATH)) {
                        Log.w("PREV", " = " + prevPath);
                        getFiles(prevPath);
                    } else {
                        getFiles(parentPath);
                    }
                } else {
                    // 退出根目录时，清空按钮点击状态
                    pathTxt.setText("");
                    numInfo.setText("");
                    showBut.setOnClickListener(null);
                    showBut.setImageResource(showArray[0]);
                    showCount = 0;
                    sortBut.setOnClickListener(null);
                    sortBut.setImageResource(sortArray[0]);
                    sortCount = 0;
                    filterBut.setOnClickListener(null);
                    filterBut.setImageResource(filterArray[0]);
                    filterCount = 0;
                    gridView.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                    expandableListView.setVisibility(View.VISIBLE);
                    listFile.clear();
                    getsdList();
                }
                // 点击的父目录位置
                int pos = -1;
                if (intList.size() <= 0) {
                    groupPosition = 0;
                    intList.add(0);
                }

                pos = intList.size() - 1;
                if (pos >= 0) {
                    if (listView.getVisibility() == View.VISIBLE) {
                        clickPos = intList.get(pos);
                        myPosition = clickPos;
                        intList.remove(pos);
                    } else if (gridView.getVisibility() == View.VISIBLE) {
                        clickPos = intList.get(pos);
                        myPosition = clickPos;
                        intList.remove(pos);
                    } else if (expandableListView.getVisibility() == View.VISIBLE) {
                        expandableListView.requestFocus();
                        if (groupPosition < groupList.size()) {
                            if (intList.get(pos) >= groupList
                                    .get(groupPosition).getChildList().size()) {
                                expandableListView.setSelectedChild(
                                        groupPosition, 0, true);
                            } else {
                                expandableListView.setSelectedChild(
                                        groupPosition, intList.get(pos), true);
                            }
                        } else {
                            groupPosition = 0;
                            expandableListView.setSelectedChild(groupPosition,
                                    intList.get(pos), true);
                        }
                    }

                }
            }
            return true;
        case KeyEvent.KEYCODE_SEARCH: // search
            if (expandableListView.getVisibility() == View.INVISIBLE) {
                searchFileDialog();
            }
            return true;
        case KeyEvent.KEYCODE_INFO: // info
            if (expandableListView.getVisibility() == View.INVISIBLE) {
                FileUtil util = new FileUtil(this);
                util.showFileInfo(listFile.get(myPosition));
            }
            return true;
        case KeyEvent.KEYCODE_HELP: // help
            FileMenu.setHelpFlag(1);
            FileMenu.filterType(MainExplorerActivity.this, MENU_HELP, null);
            return true;

        case KeyEvent.KEYCODE_PAGE_UP:
            super.onKeyDown(keyCode, event);
            break;

        case KeyEvent.KEYCODE_PAGE_DOWN:
            super.onKeyDown(keyCode, event);
            break;
        }
        return false;
    }

    /**
     * 获取当前文件路径
     *
     * @return 当前文件路径
     */
    public String getCurrentFileString() {
        return currentFileString;
    }

    /**
     * 跳转页面后调用
     */
    protected void onResume() {
        super.onResume();
        // 下载成功后自动刷新页面
        if (pathTxt.getText().toString().startsWith(FTPActivity.downPath)) {
            if (FTPActivity.resultCode == 226) {
                updateList(true);
            }
        }

        // begin add by qian_wei/xiong_cuifan 2011/11/07
        // for grally3D delete the file, flush the data
        if(!currentFileString.equals("") && preCurrentPath.equals(currentFileString)) {
            updateList(true);
        }
        // end add by qian_wei/xiong_cuifan 2011/11/08
    }

    public ListView getListView() {
        return listView;
    }

    /**
     * 获取挂载列表
     *
     * @return 挂载列表
     */
    private void getMountEquipmentList() {
        String[] mountType = getResources().getStringArray(R.array.mountType);
        MountInfo info = new MountInfo(this);
        groupList = new ArrayList<GroupInfo>();
        childList = new ArrayList<Map<String, String>>();
        GroupInfo group = null;
        for (int j = 0; j < mountType.length; j++) {
            group = new GroupInfo();
            childList = new ArrayList<Map<String, String>>();
            for (int i = 0; i < info.index; i++) {
                if (info.type[i] == j) {
                    Log.e(TAG,"==== zhl [getMountEquipmentList] ====i = "+i+",j="+j+",path="+info.path[i]);
                    if (info.path[i] != null && info.path[i].contains("/mnt")) {
                        Map<String, String> map = new HashMap<String, String>();
                        // map.put(MOUNT_DEV, info.dev[i]);
                        map.put(MOUNT_TYPE, String.valueOf(info.type[i]));
                        map.put(MOUNT_PATH, info.path[i]);
//                        map.put(MOUNT_LABLE, info.label[i]);
                        map.put(MOUNT_LABLE, "");
                        map.put(MOUNT_NAME, info.partition[i]);
                        childList.add(map);
                    }
                }
            }
            if (childList.size() > 0) {
                group.setChildList(childList);
                group.setName(mountType[j]);
                groupList.add(group);
            }
        }
    }
    // 接收广播--USB状态
    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                mHandler.removeMessages(0);
                Message msg = new Message();
                msg.what = 0;
                mHandler.sendMessageDelayed(msg, 1000);
                if (!pathTxt.getText().toString().equals("")) {
                    if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                        FileUtil.showToast(context,getString(R.string.uninstall_equi));
                    }
                }
                else if(action.equals(Intent.ACTION_MEDIA_MOUNTED))
                    FileUtil.showToast(context,getString(R.string.install_equi));
            }
        }
    };

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            //if (pathTxt.getText().toString().equals("")) {
                refushList();
            //}
            super.handleMessage(msg);
        }

    };


    private void refushList() {
        getMountEquipmentList();
        expandableListView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
        intList.clear();
        numInfo.setText("");
        pathTxt.setText("");
        ExpandAdapter adapter = new ExpandAdapter(MainExplorerActivity.this,
                groupList);
        expandableListView.setAdapter(adapter);
        Log.e("2189========", " SIZE = " + groupList.size());
        for (int i = 0; i < groupList.size(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    private void getUSB() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addDataScheme("file");
        registerReceiver(usbReceiver, usbFilter);
    }

    protected void onDestroy() {
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }

    // begin modify by qian_wei/xiong_cuifan 2011/11/05
    // for broken into the directory contains many files,click again error
    class MyThread extends MyThreadBase {
        public void run() {
            if (getFlag()) {
                setFlag(false);
                synchronized(lock){
                    util = new FileUtil(MainExplorerActivity.this, filterCount,
                            arrayDir, arrayFile, currentFileString);
                }
            } else {
                util = new FileUtil(MainExplorerActivity.this, filterCount,
                        currentFileString);
            }
            if(currentFileString.startsWith(ISO_PATH)){
                listFile = util.getFiles(sortCount, "net");

                //BEGIN : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
//                // begin modify by yuejun 2011/12/16
//                List<File> temp1ListFile = new ArrayList<File>();
//                List<File> temp2ListFile = new ArrayList<File>();
//                if(currentFileString.toLowerCase().contains("bdmv"))
//                {
//                    temp1ListFile.add(getMaxFile(listFile));
//                    listFile=temp1ListFile;
//                }
//                else if(currentFileString.toLowerCase().contains("video_ts"))
//                {
//                    for(int i=0; i<listFile.size(); i++)
//                    {
//                        if(listFile.get(i).toString().substring(listFile.get(i).toString().lastIndexOf(".")).equalsIgnoreCase(".vob"))
//                            temp1ListFile.add(listFile.get(i));
//                    }
//
//                    for(int j=0; j<temp1ListFile.size(); j++)
//                    {
//                        if(temp1ListFile.get(j).length() >= (long)100*1024*1024)
//                            temp2ListFile.add(temp1ListFile.get(j));
//                    }
//                    listFile = temp2ListFile;
//                }
//                // end modify by yuejun 2011/12/16
                //END : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
            }
            else {
                listFile = util.getFiles(sortCount, "local");
            }
            if(getStopRun()) {
                if(keyBack) {
                    if(pathTxt.getText().toString().equals(ISO_PATH)) {
                        currentFileString = util.currentFilePath;
                    }
                }
            } else {
                // begin modify by yuejun 2011/12/21
                if(util.currentFilePath.startsWith(ISO_PATH))
                {
                    util.currentFilePath = isoParentPath;
                }
                // end modify by yuejun 2011/12/21
                currentFileString = util.currentFilePath;
                handler.sendEmptyMessage(UPDATE_LIST);
            }
            // end modify by qian_wei/xiong_cuifan 2011/11/08
        }

        /**
         * 过滤蓝光ISO文件，获取最大视频文件
         */
        // begin modify by yuejun 2011/12/16
        public File getMaxFile(List<File> listFile){
            int temp = 0;
            for(int i=0; i<listFile.size(); i++){
                if(listFile.get(temp).length() <= listFile.get(i).length())
                    temp = i;
            }
            return listFile.get(temp);
        }
        // end modify by yuejun 2011/12/16
    }

    public Handler getHandler() {
        return handler;
    }

    /**
     * 如果删除成功在列表中删除
     */
    public void operateSearch(boolean b) {
        if (b) {
            for (int i = 0; i < fileArray.size(); i++) {
                listFile.remove(fileArray.get(i));
            }
        }
    }

    protected void onStop() {
        super.onStop();
        super.cancleToast();
    }

    public TextView getPathTxt() {
        return pathTxt;
    }

	private static IMountService getMountService() {
		IBinder service = ServiceManager.getService("mount");
		if (service != null) {
			return IMountService.Stub.asInterface(service);
		} else {
			Log.e(TAG, "Can't get mount service");
		}
		return null;
	}
}
