package com.explorer.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SambaManager;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.explorer.R;
import com.explorer.common.CommonActivity;
import com.explorer.common.ControlListAdapter;
import com.explorer.common.FileAdapter;
import com.explorer.common.FileUtil;
import com.explorer.common.MyDialog;
import com.explorer.common.NewCreateDialog;
import com.explorer.common.SocketClient;
import com.explorer.ftp.DBHelper;
import com.explorer.jni.Jni;
import com.explorer.jni.SambaTreeNative;

/**
 * @author liu_tianbao
 */
public class SambaActivity extends CommonActivity implements Runnable {

    static final String TAG = "SambaActivity";

    // 编辑快捷方式
    private static final int MENU_EDIT = Menu.FIRST + 14;

    // 添加快捷方式
    private static final int MENU_SHORT = Menu.FIRST + 16;

    // 当前父路径
    private String parentPath = "";

    // 文件列表对象
    private List<File> fileArray = null;

    // server 列表,设置服务器列表数据源
    List<Map<String, Object>> list = null;

    // directorys 保存folder_position值,即本地映射
    private String directorys = "/sdcard";

    // JNI程序执行结果
    private int result;

    // 切换显示菜单内容标识位,默认为服务器列表获得焦点标识
    private int temp = 0;

    // 服务器路径
    private String folder_position = "";

    // 点击的item中的服务器名字
    private String serverName = "";

    // 用户服务器Ip地址
    private String Userserver = "";

    // 用户名
    private String Username = "";

    // 用户密码
    private String Userpass = "";

    // 服务器名
    private String display = "";

    // 显示文件列表集合
    //	private List<File> li = null;

    // 服务器IP输入框
    private EditText editServer;

    // 用户名输入框
    private EditText editName;

    // 用户密码输入框
    private EditText editpass;

    // 服务器名显示框
    private EditText editdisplay;

    // 共享目录输入框
    private EditText position;

    // 子操作菜单
    SubMenu suboper;

    // 当前光标位置
    private int myPosition = 0;

    // 服务器信息编辑提示框
    MyDialog dialog;

    // 提示框确定按钮
    Button myOkBut;

    // 提示框取消按钮
    Button myCancelBut;

    // 数据库使用
    private DBHelper dbHelper;

    // 数据库查询
    private SQLiteDatabase sqlite;

    // 数据游标
    Cursor cursor;

    // 被选择文件名集合
    List<String> selected;

    // 编辑、新建标识
    int controlFlag = 0;

    // 编辑数据ID
    int id = 0;

    // 操作标识
    int flagItem = 0;

    // 删除提示框
    AlertDialog alertDialog;

    // 删除数据容器
    ListView listViews;

    // 编辑前昵称
    String prevName = "";

    // 编辑前 共享目录
    String prevFolder = "";

    // 编辑前用户名
    String prevUser = "";

    // 编辑前密码
    String prevPass = "";

    // 点击位置集合
    List<Integer> intList;

    // 本地方法对象
    Jni jni;

    // 本地挂载路径
    String localPath = "";

    // 数据库字段
    static final String TABLE_NAME = "samba";

    // 数据库主键id
    static final String ID = "_id";

    // 服务器ip地址
    static final String SERVER_IP = "server_ip";

    // 服务器昵称或者计算机名
    static final String NICK_NAME = "nick_name";

    // 共享工作目录
    static final String WORK_PATH = "work_path";

    // 本地挂载点
    static final String MOUNT_POINT = "mount_point";

    // 账户
    static final String ACCOUNT = "account";

    // 密码
    static final String PASSWORD = "password";

    // 图片标识
    static final String IMAGE = "image";

    // 快捷方式、全部网络标识
    static final String SHORT = "short";

    // 共享目录集合
    List<Map<String, Object>> workFolderList;

    // 服务器介绍信息key值
    static final String SERVER_INTRO = "infos";

    // 点击前文件路径
    private String prePath = "";

    // 是否加入快捷方式复选框
    private CheckBox netCheck;

//	// 文件数据适配器·
//	FileAdapter adapter;

    // 点击服务器需要输入用户名密码提示框
    AlertDialog nServerLogonDlg;

    // 空字符串
    private static final String BLANK = "";

    // 进入服务器需要输入用户名密码
    static final String NEED_INPUT_PASSWORD = "NT_STATUS_ACCESS_DENIED";

    // 文件数目和当前位置显示
    private TextView numInfo;

    // 服务器登录用户名
    private String loginName = "";

    // 服务器登录密码
    private String loginPass = "";
    // =============== SambaTree ====================================
    // 等待对话框
    ProgressDialog pgsDlg;

    // 等待时间长度,没5秒中变化一次
    long waitLong;

    // 工作组信息
    String strWorkgrpups;

    // Smbtree 本地方法
    SambaTreeNative sambaTree;

    // 总的等待时长
    long totalLong;

    Timer timer;

    // static final String DIRECTORY_DETAIL = "dirDetail";

    // 是否时搜索到得服务器目录标志位
    // static final String IS_NET_DIR = "isNetDir";

    static final String DIR_ICON = "dirIcon";

    // 用于记录在服务器列表中点击的网络服务器
    Map<String, Object> clickedNetServer;

    // 放置点击服务器后获取到的共享目录列表groupDetails
    Map<String, List<Map<String, Object>>> server2groupDirs = new HashMap<String, List<Map<String, Object>>>(
            1);

    // 粘贴
    private MenuItem pasteMenuItem;

    // 剪切
    private MenuItem cutMenuItem;

    // 拷贝
    private MenuItem copyMenuItem;

    // 重命名
    private MenuItem renameMenuItem;

    // 删除
    private MenuItem deleteMenuItem;

    // 用户名、密码错误
    private final static int USER_OR_PASS_ERROR = -6;

    // 网络异常
    private final static int NET_ERROR = -5;

    // 上一次输入IP
    private String prevServer;

    // begin add by qian_wei/zhou_yong 2011/10/28
    // for add some variable
    // 服务器地址和共享目录相连
    private StringBuilder builder = null;

    // 快捷方式或者共享目录集合
    private Map<String, Object> clickedServerDirItem;

    // 新增挂载
    private static final int MOUNT_RESULT_1 = 11;

    // 编辑挂载
    private static final int MOUNT_RESULT_2 = 12;

    // 快捷方式或者共享目录挂载
    private static final int MOUNT_RESULT_3 = 13;

    // 匿名挂载失败或者快捷方式挂载失败
    private static final int MOUNT_RESULT_4 = 14;

    // 匿名进入确认添加快捷方式对话框
    private AlertDialog sureDialog;

    // 快捷方式或共享目录标识
    private int allOrShort = 0;
    // end add by qian_wei/zhou_yong 2011/10/28

    static {
        System.loadLibrary("android_runtime");
    }

    // iso文件所在的当前路径
    private String isoParentPath = new String();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lan);

        // 初始化组件及相关对象
        init();
    }

    /**
     * 初始化组件及相关对象
     */
    private void init() {

        showBut = (ImageButton) findViewById(R.id.showBut);
        sortBut = (ImageButton) findViewById(R.id.sortBut);
        filterBut = (ImageButton) findViewById(R.id.filterBut);
        jni = new Jni();
        listFile = new ArrayList<File>();
        intList = new ArrayList<Integer>();
        fileL = new ArrayList<File>();
        gridlayout = R.layout.gridfile_row;
        listlayout = R.layout.file_row;
        listView = (ListView) findViewById(R.id.listView);
        pathTxt = (TextView) findViewById(R.id.textPath);
        gridView = (GridView) findViewById(R.id.gridView);
        // 初始化服务器数据列表
        list = new ArrayList<Map<String, Object>>(1);

        dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, null,
                DBHelper.DATABASE_VERSION);
        sqlite = dbHelper.getWritableDatabase();
        selected = new ArrayList<String>();

        numInfo = (TextView) findViewById(R.id.title);

        // 获取服务器信息，并显示到UI上

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(IMAGE, R.drawable.mainfile);
        map.put(NICK_NAME, getString(R.string.all_network));
        map.put(SHORT, 0);
        list.add(map);
        list.addAll(getServer());
        servers();

        isNetworkFile = true;
    }

    /**
     * 获取服务器信息并设置到UI列表信息中
     */
    private void servers() {
        Log.w("111", "111");
        SimpleAdapter serveradapter = new SimpleAdapter(this, list,
                R.layout.file_row, new String[] { IMAGE, NICK_NAME },
                new int[] { R.id.image_Icon, R.id.text });
        // 显示服务器GridView,设置服务器列表
        listView.setAdapter(serveradapter);
        listView.setOnItemClickListener(ItemClickListener);
        listView.setOnItemSelectedListener(itemSelect);
        listView.setSelection(clickPos);

    }

    /**
     * 获取samba在本地映射目录中的文件列表
     *
     * @param path
     *            本地挂载路径
     */
    private void getDirectory(String path) {

        temp = 1;// 给temp赋值1,换显示菜单内容标识位
        // directorys 保存folder_position值,即本地映射
        // 本地挂载文件夹路径
        directorys = path;
        // 挂载文件夹父路径
        parentPath = path;
        // 当前文件字符串信息
        currentFileString = path;

        getFiles(path);
    }

    /**
     * 获取封装成key-value对的列表
     *
     * @return list 封装了key-value服务器信息列表
     */
    private List<Map<String, Object>> getServer() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        cursor = sqlite.query(TABLE_NAME, new String[] { ID, NICK_NAME,
                WORK_PATH, SERVER_IP, ACCOUNT, PASSWORD }, null, null, null,
                null, null);
        while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(IMAGE, R.drawable.folder_file);
            String nickName = "\\\\"
                    + cursor.getString(cursor.getColumnIndex(NICK_NAME)) + "\\"
                    + cursor.getString(cursor.getColumnIndex(WORK_PATH));
            Log.d("============566", "SHOWNAME = " + nickName);
            map.put(NICK_NAME, nickName);
            map.put(SHORT, 1);
            map.put(MOUNT_POINT, " ");
            map.put(SERVER_IP, cursor.getString(cursor
                    .getColumnIndex(SERVER_IP)));
            map.put(ACCOUNT, cursor.getString(cursor.getColumnIndex(ACCOUNT)));
            map
                    .put(PASSWORD, cursor.getString(cursor
                            .getColumnIndex(PASSWORD)));
            map.put(WORK_PATH, cursor.getString(cursor
                    .getColumnIndex(WORK_PATH)));
            list.add(map);// 将封装好的key-value对放入到列表
        }
        cursor.close();
        return list;
    }

    /**
     * create options menus
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d("SambaActivity[onCreateOptionsMenu]", "create_menu");

        // hide tab menu
        menu.add(Menu.NONE, MENU_TAB, 0, R.string.hide_tab);

        // add sub menu
        suboper = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, R.string.operation);

        menu.add(Menu.NONE, MENU_ADD, 0, R.string.str_server);

        // default "Search Servers" when temp=0 on start
        menu.add(Menu.NONE, MENU_SEARCH, 0, R.string.search);// getResources().getString(R.string.search));
        menu.add(Menu.NONE, MENU_EDIT, 0, getResources().getString(
                R.string.edit));
        menu.add(Menu.NONE, MENU_SHORT, 0, R.string.add_shortcut);

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
        menu.add(Menu.NONE, MENU_HELP, 0, R.string.help);
        return true;
    };

    /**
     * @tag : begin modify by qian_wei 2011/7/4 for 2203 2283
     * @brief : 共享目录弹出菜单，进入挂载目录后搜索置灰
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
        // start to prepare dynamic menu shown to users
        // 服务器列表没有获得焦点,nameList 放置服务器名
        // 有服务器列表,但是服务器列表没有获得焦点
        // 初始化时,显示菜单时，默认搜索菜单显示“搜索服务器”标题
        String[] splitedPath = pathTxt.getText().toString().split("/");
        if (splitedPath.length == 1) {
            temp = 0;
            menu.getItem(7).setEnabled(false);
            menu.getItem(6).setEnabled(false);
            suboper.clear();
            menu.getItem(1).setEnabled(true);
            suboper.add(Menu.NONE, MENU_DELETE, 0, getResources().getString(
                    R.string.delete));
            // 根目录或者服务器列表目录
            if (pathTxt.getText().toString().equals("")) {
                if (intList.size() == 0) {
                    menu.getItem(2).setEnabled(true);
                } else {
                    menu.getItem(2).setEnabled(false);
                }
                if (list.size() <= 1 || intList.size() == 1) {
                    menu.getItem(1).setEnabled(false);
                } else {
                    menu.getItem(1).setEnabled(true);
                }
                menu.getItem(3).setEnabled(false);
                if (listView.getSelectedItemPosition() > 0
                        && intList.size() == 0) {
                    menu.getItem(4).setEnabled(true);
                } else {
                    menu.getItem(4).setEnabled(false);
                }
                menu.getItem(5).setEnabled(false);
            }
            // 服务器共享目录列表目录
            else {
                menu.getItem(1).setEnabled(false);
                menu.getItem(2).setEnabled(false);
                menu.getItem(3).setEnabled(false);
                menu.getItem(4).setEnabled(false);
                if (listView.getSelectedItemPosition() >= 0) {
                    if (hasTheShortCut()) {
                        menu.getItem(5).setEnabled(false);
                    } else {
                        menu.getItem(5).setEnabled(true);
                    }

                } else {
                    menu.getItem(5).setEnabled(false);
                }
            }
        }
        // 文件操作
        else {
            temp = 1;
            menu.getItem(7).setEnabled(true);
            menu.getItem(6).setEnabled(true);
            if (hasTheShortCut()) {
                menu.getItem(5).setEnabled(false);
            } else {
                menu.getItem(5).setEnabled(true);
            }
            suboper.clear();
            copyMenuItem = suboper.add(Menu.NONE, MENU_COPY, 0, getResources()
                    .getString(R.string.copy));
            cutMenuItem = suboper.add(Menu.NONE, MENU_CUT, 0, getResources()
                    .getString(R.string.cut));
            pasteMenuItem = suboper.add(Menu.NONE, MENU_PASTE, 0,
                    getResources().getString(R.string.paste));
            deleteMenuItem = suboper.add(Menu.NONE, MENU_DELETE, 0,
                    getResources().getString(R.string.delete));
            renameMenuItem = suboper.add(Menu.NONE, MENU_RENAME, 0,
                    getResources().getString(R.string.str_rename));
            SharedPreferences share = getSharedPreferences("OPERATE", SHARE_MODE);
            int num = share.getInt("NUM", 0);
            // 目录下无文件
            if (listFile.size() == 0) {
                if (num == 0) {
                    menu.getItem(1).setEnabled(false);
                } else {
                    menu.getItem(1).setEnabled(true);
                    pasteMenuItem.setEnabled(true);
                    copyMenuItem.setEnabled(false);
                    cutMenuItem.setEnabled(false);
                    deleteMenuItem.setEnabled(false);
                    renameMenuItem.setEnabled(false);
                }
                menu.getItem(3).setEnabled(false);
            } else {
                if (num == 0) {
                    menu.getItem(1).setEnabled(true);
                    pasteMenuItem.setEnabled(false);
                    copyMenuItem.setEnabled(true);
                    cutMenuItem.setEnabled(true);
                    deleteMenuItem.setEnabled(true);
                    renameMenuItem.setEnabled(true);
                } else {
                    menu.getItem(1).setEnabled(true);
                    pasteMenuItem.setEnabled(true);
                    copyMenuItem.setEnabled(true);
                    cutMenuItem.setEnabled(true);
                    deleteMenuItem.setEnabled(true);
                    renameMenuItem.setEnabled(true);
                }
                menu.getItem(3).setEnabled(true);
            }
            menu.getItem(2).setEnabled(true);
            Log.d("SambaActivity[onPrepareOptionsMenu]", "533::file_temp="
                    + temp);
            menu.getItem(4).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单的选择回调方法
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case MENU_ADD:
            controlFlag = 0;
            showDialogs(MENU_ADD);
            // newDirOrFile(false);
            break;
        // search menu
        case MENU_SEARCH:
            // 在文件列表获得焦点时, 进行文件搜索
            if (temp == 1) {
                Log.d("SambaActivity[onOptionsItemSelected]",
                        "619::call_searchFileDialog()");
                searchFileDialog();
            }
            // else if (temp == 0) {
            // Log.d("SambaActivity[onOptionsItemSelected]",
            // "622::call_searchServers()");
            // // 服务器列表获取焦点时,搜索samba服务器
            // searchServers();
            // }
            break;
        // cut menu
        case MENU_CUT:
            managerF(myPosition, MENU_CUT);
            break;
        // paste menu
        case MENU_PASTE:
            managerF(myPosition, MENU_PASTE);
            break;
        // delete menu
        case MENU_DELETE:
            managerF(myPosition, MENU_DELETE);
            break;
        // rename menu
        case MENU_RENAME:
            managerF(myPosition, MENU_RENAME);
            break;
        // copy menu
        case MENU_COPY:
            managerF(myPosition, MENU_COPY);
            break;
        case MENU_EDIT:
            controlFlag = 1;
            showDialogs(MENU_EDIT);
            break;
        case MENU_SHORT:
            addShortCut();
            break;
        case MENU_HELP:
            FileMenu.setHelpFlag(2);
            FileMenu.filterType(SambaActivity.this, MENU_HELP, null);
            break;
        }

        return true;
    }

    /**
     * 根据点击的菜单,显示对话框,执行不同的操作
     *
     * @param item
     */
    private void showDialogs(final int item) {
        // LayoutInflater factory = LayoutInflater.from(SambaActivity.this);
        // 在点击“新建”和“编辑”菜单下显示对话框
        if (item == MENU_ADD || item == MENU_EDIT) {
            // 服务器列表获得焦点情况下
            if (temp == 0) {
                dialog = new MyDialog(this, R.layout.new_server);
                dialog.show();
                // View myView01 = factory.inflate(R.layout.new_server, null);
                editServer = (EditText) dialog.findViewById(R.id.editServer);
                editName = (EditText) dialog.findViewById(R.id.editName);
                editpass = (EditText) dialog.findViewById(R.id.editpass);
                editdisplay = (EditText) dialog.findViewById(R.id.editdisplay);
                position = (EditText) dialog.findViewById(R.id.position);
                netCheck = (CheckBox) dialog.findViewById(R.id.add_shortcut);
                // position.setText(folder_position);
                netCheck.setVisibility(View.GONE);

                if (controlFlag == 1) {
                    serverName = pathTxt.getText().toString();
                    prevFolder = list.get(listView.getSelectedItemPosition())
                            .get(WORK_PATH).toString();
                    position.setText(prevFolder);
                    prevServer = list.get(listView.getSelectedItemPosition())
                            .get(SERVER_IP).toString();
                    editServer.setText(prevServer);
                    String nick = list.get(listView.getSelectedItemPosition())
                            .get(NICK_NAME).toString();
                    editdisplay.setText(nick.substring(2, nick.substring(2)
                            .indexOf("\\") + 2));
                    cursor = sqlite.query(TABLE_NAME, new String[] { ID,
                            SERVER_IP, ACCOUNT, PASSWORD }, NICK_NAME
                            + "=? and " + WORK_PATH + "=?", new String[] {
                            editdisplay.getText().toString(), prevFolder },
                            null, null, null);
                    if (cursor.moveToFirst()) {
                        id = cursor.getInt(cursor.getColumnIndex(ID));
                    }
                    cursor.close();
                    prevUser = list.get(listView.getSelectedItemPosition())
                            .get(ACCOUNT).toString();
                    if (prevUser.equals("g")) {
                        editName.setText("");
                    } else {
                        editName.setText(prevUser);
                    }
                    prevPass = list.get(listView.getSelectedItemPosition())
                            .get(PASSWORD).toString();
                    editpass.setText(prevPass);
                }
                myOkBut = (Button) dialog.findViewById(R.id.myOkBut);
                myCancelBut = (Button) dialog.findViewById(R.id.myCancelBut);
                myOkBut.setOnClickListener(butClick);
                myCancelBut.setOnClickListener(butClick);
            } else {
                // 在服务器列表没有获得焦点时,调用新建或者编辑文件
                FileUtil util = new FileUtil(this);
                util.createNewDir(currentFileString);
            }
        }
    }

    // update by qw
    OnClickListener butClick = new OnClickListener() {
        public void onClick(View v) {
            if (v.equals(myOkBut)) {
                // begin modify by qian_wei/xiong_cuifan 2011/11/05
                // for new mount can add two shortcut
                myOkBut.setEnabled(false);
                Userserver = editServer.getText().toString();
                Username = editName.getText().toString();
                if (Username.equals("")) {
                    Username = "g";
                }
                Userpass = editpass.getText().toString();
                display = jni.getPcName(Userserver);
                if (display.equals("ERROR")) {
                    display = Userserver;
                }
                editdisplay.setText(display);
                folder_position = position.getText().toString().toUpperCase();

                if (folder_position.startsWith("/")) {
                    folder_position = folder_position.substring(1);
                }

                if (folder_position.endsWith("/")) {
                    folder_position = folder_position.substring(0,
                            folder_position.length() - 1);
                }

                if (Userserver.trim().equals("")) {
                    myOkBut.setEnabled(true);
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.input_server));
                } else if (folder_position.trim().equals("")) {
                    myOkBut.setEnabled(true);
                    // end modify by qian_wei/xiong_cuifan 2011/11/06
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.work_path_null));
                } else {
                    doMount();
                }
            } else {
                dialog.cancel();
            }
        }
    };

    /**
     * 执行挂载操作
     */
    private void doMount() {
        // begin modify by qian_wei/zhou_yong 2011/10/28
        // for extend the mount time
//		StringBuilder builder = new StringBuilder(Userserver);
        builder = new StringBuilder(Userserver);
        builder.append("/").append(folder_position);
        if (controlFlag == 1) {
            boolean bServer = prevServer.equals(Userserver);
            boolean bDir = prevFolder.equals(folder_position);
            boolean bUser = prevUser.equals(Username);
            boolean bPass = prevPass.equals(Userpass);
            if (bDir && bUser && bPass && bServer) {
                dialog.cancel();
            } else {
                cursor = sqlite
                        .query(TABLE_NAME, new String[] { ID }, SERVER_IP
                                + "=? and " + WORK_PATH + "=?", new String[] {
                                Userserver, folder_position }, null, null, null);
                if (cursor.moveToFirst()) {
                    // begin modify by qian_wei/xiong_cuifan 2011/11/05
                    // for new mount can add two shortcut
                    myOkBut.setEnabled(true);
                    // end modify by qian_wei/xiong_cuifan 2011/11/06
                    FileUtil.showToast(this, getString(R.string.shortcut_exist));
                } else {
//                    builder = new StringBuilder(prevServer);
//                    builder.append("/").append(prevFolder);
//                    String returnStr = jni.getMountList(builder.toString());
//                    if (returnStr.equals("ERROR")) {
//                        SambaManager samba = (SambaManager) getSystemService("Samba");
//                        samba.start("", "", "", "");
//                        result = jni.UImount(Userserver, folder_position, " ",
//                                Username, Userpass);
//                    } else {
//                        SambaManager samba = (SambaManager) getSystemService("Samba");
//                        samba.start("", "", "", "");
//                        int code = jni.myUmount(jni.getMountList(builder
//                                .toString()));
//                        Log.w("CODE", " = " + code);
//                        samba = (SambaManager) getSystemService("Samba");
//                        samba.start("", "", "", "");
//                        result = jni.UImount(Userserver, folder_position, " ",
//                                Username, Userpass);
//                    }
//                    if (result == 0) {
//                        builder = new StringBuilder(Userserver);
//                        builder.append("/").append(folder_position);
//                        localPath = jni.getMountList(builder.toString());
//                        Log.d("-------911-------", "LOCAL " + localPath);
//                        showLading();
//                        dialog.cancel();
//                    } else if (result == USER_OR_PASS_ERROR) {
//                        FileUtil.showToast(SambaActivity.this,
//                                getString(R.string.user_or_pass));
//
//                    // begin modify by qian_wei/yang_haibing 2011/10/14
//                    // for determine the mount result value when the network is disconnected
////                  } else if (result == -7) {
////                        FileUtil.showToast(SambaActivity.this,
////                                getString(R.string.mount_exist));
//                    } else if (result == NET_ERROR) {
//                        FileUtil.showToast(SambaActivity.this,
//                                getString(R.string.network_error));
//                    // begin modify by qian_wei/yang_haibing 2011/10/14
//                    }else {
//                        FileUtil.showToast(SambaActivity.this,
//                                getString(R.string.new_server_error));
//                    }
                    progress = new ProgressDialog(this);
                    progress.setOnKeyListener(new OnKeyListener() {
                        public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                            return true;
                        }
                    });
                    progress.show();
                    MountThread thread = new MountThread(MOUNT_RESULT_2);
                    thread.start();
                }
                cursor.close();
            }
        } else {
            cursor = sqlite.query(TABLE_NAME, new String[] { ID }, NICK_NAME
                    + "=? and " + WORK_PATH + "=?",
                    new String[] { display, folder_position }, null, null, null);
            if (cursor.moveToFirst()) {
                // begin modify by qian_wei/xiong_cuifan 2011/11/05
                // for new mount can add two shortcut
                myOkBut.setEnabled(true);
                // end modify by qian_wei/xiong_cuifan 2011/11/06
                FileUtil.showToast(this, getString(R.string.shortcut_exist));
            } else {
                localPath = jni.getMountList(builder.toString());
                Log.w("TAG1", builder.toString());
                Log.w("TAG2", localPath);
                if(!localPath.equals("ERROR")) {
                    showLading();
                    dialog.cancel();
                } else {
//	                 SambaManager samba = (SambaManager) getSystemService("Samba");
//	                    samba.start("", "", "", "");
//	                    result = jni.UImount(Userserver, folder_position, " ",
//	                            Username, Userpass);
//	                    if (result == 0) {
//	                        localPath = jni.getMountList(builder.toString());
//	                        Log.d("-------956-------", "LOCAL " + localPath);
//	                        showLading();
//	                        dialog.cancel();
//	                    } else if (result == USER_OR_PASS_ERROR) {
//	                        FileUtil.showToast(SambaActivity.this,
//	                                getString(R.string.user_or_pass));
//
//	                    //  begin modify by qian_wei/yang_haibing 2011/10/14
//	                    //  for determine the mount result value when the network is disconnected
////	                  } else if (result == -7) {
////	                      FileUtil.showToast(SambaActivity.this,
////	                              getString(R.string.mount_exist));
//	                    } else if (result == NET_ERROR) {
//	                        FileUtil.showToast(SambaActivity.this,
//	                                getString(R.string.network_error));
//	                    // begin modify by qian_wei/yang_haibing 2011/10/14
//	                    } else {
//	                        FileUtil.showToast(SambaActivity.this,
//	                                getString(R.string.new_server_error));
//	                    }
                    progress = new ProgressDialog(this);
                    progress.setOnKeyListener(new OnKeyListener() {
                        public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                            return true;
                        }
                    });
                    progress.show();
                    MountThread thread = new MountThread(MOUNT_RESULT_1);
                    thread.start();
                    // end modify by qian_wei/zhou_yong 2011/10/28
                }
            }
            cursor.close();
        }

    }

    /**
     * 显示等待对话框
     */
    private void showLading() {
        // 显示ProgressDialog对话框
        progress = new ProgressDialog(SambaActivity.this);
        progress.setTitle(getResources().getString(R.string.adding_server));
        progress.setMessage(getResources().getString(R.string.please_waitting));
        progress.show();

        // 开启线程
        Thread threas = new Thread(SambaActivity.this);
        threas.start();
    }

    public void run() {
        Log.d("------1008-----", "Start");
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case SEARCH_RESULT:
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }

                synchronized(lock){
                    // 无搜索结果
                    if (arrayFile.size() == 0 && arrayDir.size() == 0) {
                        FileUtil.showToast(SambaActivity.this,
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
                    adapter = new FileAdapter(SambaActivity.this, listFile,
                            listlayout);
                    listView.setAdapter(adapter);
                    listView.setOnItemSelectedListener(itemSelect);
                    listView.setOnItemClickListener(ItemClickListener);
                } else if (gridView.getVisibility() == View.VISIBLE) {
                    adapter = new FileAdapter(SambaActivity.this, listFile,
                            gridlayout);
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
//				File fileISO = new File(ISO_PATH);
//				File[] ISOList = fileISO.listFiles();
//				for (int i=0; i < ISOList.length; i++)
//			    {
//					if(ISOList[i].getName().equalsIgnoreCase("bdmv"))
//					{
//						File[] TempList = ISOList[i].listFiles();
//						for(int j=0; j < TempList.length; j++)
//						{
//							if(TempList[j].getName().equalsIgnoreCase("stream"))
//							{
//								OpenFilePath = TempList[j].getPath();
//							}
//						}
//					}
//					else if(ISOList[i].getName().equalsIgnoreCase("video_ts"))
//					{
//						OpenFilePath = ISOList[i].getPath();
//					}
//				}
                //END : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
                getFiles(OpenFilePath);
                break;
            // end modify by yuejun 2011/12/14
            case ISO_MOUNT_FAILD:
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                FileUtil.showToast(SambaActivity.this,
                        getString(R.string.new_mout_fail));
                break;

            case END_SEARCH:
            case NET_NORMAL:
                Log.d(TAG, "2398::dismiss_dialog");
                if (SambaActivity.this != null
                        && !SambaActivity.this.isFinishing()) {
                    pgsDlg.dismiss();// 关闭等待对话框
                }
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                // 在等待ProgressDialog关闭后,调用解析字符串方法
                if (!strWorkgrpups.toLowerCase().equals("error")) {
                    Log.d(TAG, "2424::strWorkgroups=" + strWorkgrpups);
                    if (willClickNetDir) {
                        Log.e("ADD2100", "POSITION");
                        intList.add(myPosition);
                    }
                    updateServerListAfterParse(strWorkgrpups);
                }
                /*
                 * @tag :begin modified by ni_guanhua 2011/7/4
                 *
                 * @brief :搜索超时给出的提示
                 */
                else {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.net_time_out));
                }
                break;
            case NET_ERROR:
                if (SambaActivity.this != null
                        && !SambaActivity.this.isFinishing()) {
                    pgsDlg.dismiss();// 关闭等待对话框
                }
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                strWorkgrpups = "";
                FileUtil.showToast(SambaActivity.this,
                        getString(R.string.no_server));
                break;
            // begin add by qian_wei/zhou_yong 2011/10/28
            // for treatment the result of mount
            case MOUNT_RESULT_1:
                Log.w("MOUNT_RESULT_1", "MOUNT_RESULT_1");
                // begin modify by qian_wei/xiong_cuifan 2011/11/05
                // for new mount can add two shortcut
                myOkBut.setEnabled(true);
                // end  modify by qian_wei/xiong_cuifan 2011/11/06
                if(progress != null && progress.isShowing()) {
                    progress.dismiss();
                }

                if (result == 0) {
                    localPath = jni.getMountList(builder.toString());
                    Log.d("-------956-------", "LOCAL " + localPath);
                    showLading();
                    dialog.cancel();
                } else if (result == USER_OR_PASS_ERROR) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.user_or_pass));

                //  begin modify by qian_wei/yang_haibing 2011/10/14
                //  for determine the mount result value when the network is disconnected
//              } else if (result == -7) {
//                  FileUtil.showToast(SambaActivity.this,
//                          getString(R.string.mount_exist));
                } else if (result == NET_ERROR) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.network_error));
                // begin modify by qian_wei/yang_haibing 2011/10/14
                } else {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.new_server_error));
                }
                break;
            case MOUNT_RESULT_2:
                // begin modify by qian_wei/xiong_cuifan 2011/11/05
                // for new mount can add two shortcut
                myOkBut.setEnabled(true);
                // end modify by qian_wei/xiong_cuifan 2011/11/06
                if(progress != null && progress.isShowing()) {
                    progress.dismiss();
                }

                if (result == 0) {
                    builder = new StringBuilder(Userserver);
                    builder.append("/").append(folder_position);
                    localPath = jni.getMountList(builder.toString());
                    Log.d("-------911-------", "LOCAL " + localPath);
                    showLading();
                    dialog.cancel();
                } else if (result == USER_OR_PASS_ERROR) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.user_or_pass));

                // begin modify by qian_wei/yang_haibing 2011/10/14
                // for determine the mount result value when the network is disconnected
//              } else if (result == -7) {
//                    FileUtil.showToast(SambaActivity.this,
//                            getString(R.string.mount_exist));
                } else if (result == NET_ERROR) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.network_error));
                // begin modify by qian_wei/yang_haibing 2011/10/14
                }else {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.new_server_error));
                }
                break;
            case MOUNT_RESULT_3:
                Log.w("MOUNT_RESULT_3", "MOUNT_RESULT_3");
                if(progress != null && progress.isShowing()) {
                    progress.dismiss();
                }

                  if (result == 0) {
                      if (allOrShort == -1) {
                          showData(builder.toString(), sureDialog, myPosition);
                      } else {
                          cursor = sqlite.query(TABLE_NAME, new String[] { ID,
                                  ACCOUNT, PASSWORD, NICK_NAME }, SERVER_IP
                                  + " =? and " + WORK_PATH + " = ?", new String[] {
                                  Userserver, folder_position }, null, null, null);
                          if (cursor.moveToFirst()) {
                              showData(builder.toString(), sureDialog, myPosition);
                          } else {
                              AlertDialog.Builder alert = new AlertDialog.Builder(SambaActivity.this);
                              alert.setMessage(getString(R.string.is_add_short));
                              alert.setPositiveButton(getString(R.string.ok),
                                      new DialogInterface.OnClickListener() {

                                          public void onClick(DialogInterface dialog,
                                                  int which) {
                                              addShortCut();
                                              showData(builder.toString(),
                                                      sureDialog, myPosition);
                                          }
                                      });
                              alert.setNegativeButton(getString(R.string.cancel),
                                      new DialogInterface.OnClickListener() {

                                          public void onClick(DialogInterface dialog,
                                                  int which) {
                                              showData(builder.toString(),
                                                      sureDialog, myPosition);
                                          }
                                      });
                              sureDialog = alert.create();
                              sureDialog.show();
                          }
                          cursor.close();
                      }
                  } else {
                      // begin add by qian_wei/cao_shanshan 2011/10/21
                      // for show notification while the net is down
                      if (result == NET_ERROR) {
                          FileUtil.showToast(SambaActivity.this,
                                    getString(R.string.network_error));
                      } else {
                          if(allOrShort == -1) {
                              mountShortCut(myPosition);
                          } else {
                              showSetNewMountDlg(list.get(intList.get(0)),
                                      workFolderList.get(myPosition), myPosition);
                          }
                      }
                  }
                break;
            case MOUNT_RESULT_4:

                Log.w("MOUNT_RESULT_4", "MOUNT_RESULT_4");

                if(progress != null && progress.isShowing()) {
                    progress.dismiss();
                }

                if (result == 0) {
                    // workFlag = true;
                    // 在成功挂载后对点击的服务器置空，
                    // 以便在点击共享或进入到下一级目录
//                    clickedNetServer1 = null;
                    // 全局置空
                    SambaActivity.this.clickedNetServer = null;

                    // 在挂载工程后，对点击共享目录的值做一定的变化
                    // 修改是否已设置挂载，1标识已经设置挂载
                    // clickedServerDirItem.put(DIR_HAS_MOUNTED, (byte) 1);
                    Log.d("!!!!SambaActivity[ButtonSetOnNewShareDirListener]",
                            "3079::clickedServerDirItem="
                                    + clickedServerDirItem);

                    builder = new StringBuilder(Userserver);
                    builder.append("/").append(folder_position);
                    // 获得本地挂载路径
                    Log.i("========================3503", builder.toString());
                    String returnStr = jni.getMountList(builder.toString());
                    if (returnStr.equals("ERROR")) {
                        FileUtil.showToast(SambaActivity.this,
                                getString(R.string.user_or_pass));
                    } else {
                        localPath = returnStr;
                        if (netCheck.isChecked()) {
                            cursor = sqlite.query(TABLE_NAME,
                                    new String[] { ID }, SERVER_IP + " =? and "
                                            + WORK_PATH + " = ?", new String[] {
                                            Userserver, folder_position },
                                    null, null, null);
                            if (cursor.moveToFirst()) {
                                int id = cursor.getInt(cursor
                                        .getColumnIndex(ID));
                                ContentValues values = new ContentValues();
                                // values.put(MOUNT_POINT, localPath);
                                values.put(ACCOUNT, Username);
                                values.put(PASSWORD, Userpass);
                                sqlite.update(TABLE_NAME, values, ID + "=?",
                                        new String[] { String.valueOf(id) });
                            } else {
                                setValues(Userserver, display, folder_position
                                        .replace("/", "\\"), localPath,
                                        Username, Userpass, 1);

                            }
                            cursor.close();
                        }

                        // 修改挂载目录位置
                        clickedServerDirItem.put(MOUNT_POINT, localPath);
                        FileUtil.showToast(SambaActivity.this,
                                getString(R.string.new_mout_successfully));
                        // 获取本地映射挂载目录中的内容
                        getDirectory(localPath);
                        if (getFileFlag(currentFileString, prePath) == 1) {
                            Log.e("ADD2773", "POSITION");
                            intList.add(myPosition);
                        }
                    }
                }
                /*
                 * @tag :begin modified by ni_guanhua 2011/7/4 for 2286
                 *
                 * @brief :在输入密码，或者账号错误时，给出相应提示
                 */
                else if (result == USER_OR_PASS_ERROR) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.user_or_pass));

                // begin modify by qian_wei/yang_haibing 2011/10/14
                // for determine the mount result value when the network is disconnected
//              } else if (result == -7) {
//                  FileUtil.showToast(SambaActivity.this,
//                  getString(R.string.mount_exist));
                } else if (result == NET_ERROR) {
                    FileUtil.showToast(SambaActivity.this,
                              getString(R.string.network_error));
                // end modify by qian_wei/yang_haibing 2011/10/14
                }
                // end modified
                else {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.new_server_error));
                }
                // 不论挂载成功或挂载失败，对话框关闭
                dialog.dismiss();
                // }
                break;
             // end add by qian_wei/zhou_yong 2011/10/28
            default:
                progress.dismiss();
                Log.d("------1015-----", "dismiss");
                ContentValues values = new ContentValues();
                values.put(SERVER_IP, Userserver);
                values.put(ACCOUNT, Username);
                values.put(PASSWORD, Userpass);
                values.put(WORK_PATH, folder_position.replace("/", "\\"));
                values.put(NICK_NAME, display);
                // values.put(DIR_HAS_MOUNTED, 1);
                Map<String, Object> map = null;
                if (controlFlag == 0) {
                    Log.d("------1027-------", "INSERT");
                    sqlite.insert(TABLE_NAME, ID, values);
                    boolean flag = false;
                    for (Map<String, Object> maps : list) {
                        if (!maps.containsValue(display)) {
                            flag = true;
                            break;
                        }
                    }
                    Log.w("FLAG", " = " + flag);
                    if (flag) {
                        map = new HashMap<String, Object>();
                        map.put(IMAGE, R.drawable.folder_file);
                        String nickName = "\\\\" + display + "\\"
                                + folder_position.replace("/", "\\");
                        Log.d("============1081", "SHOWNAME = " + nickName);
                        map.put(NICK_NAME, nickName);
                        map.put(SHORT, 1);
                        map.put(MOUNT_POINT, localPath);
                        map.put(SERVER_IP, Userserver);
                        map.put(ACCOUNT, Username);
                        map.put(PASSWORD, Userpass);
                        map.put(WORK_PATH, folder_position);
                        list.add(map);
                    }
                } else {
                    Log.d("------1038-------", "ID " + id);
                    sqlite.update(TABLE_NAME, values, ID + "=?",
                            new String[] { String.valueOf(id) });
                    map = new HashMap<String, Object>();
                    map.put(IMAGE, R.drawable.folder_file);
                    String nickName = "\\\\" + display + "\\"
                            + folder_position.replace("/", "\\");
                    Log.d("============1081", "SHOWNAME = " + nickName);
                    map.put(NICK_NAME, nickName);
                    map.put(SHORT, 1);
                    map.put(MOUNT_POINT, localPath);
                    map.put(SERVER_IP, Userserver);
                    map.put(ACCOUNT, Username);
                    map.put(PASSWORD, Userpass);
                    map.put(WORK_PATH, folder_position);

                    if (pathTxt.getText().toString().equals("")) {
                        list.set(listView.getSelectedItemPosition(), map);
                    }

                }
                pathTxt.setText("");
                intList.clear();
                servers();
                listView.requestFocus();
                highlightLastItem(listView, list.size());
                break;
            }
        }
    };

    // 标识是否点击的是网络服务器或者是或者是网络文件夹
    boolean willClickNetDir = false;

    /**
     * 服务器列表GridView_ItemClickListener
     */
    private OnItemClickListener ItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position,
                long id) {
			if (IsNetworkDisconnect()) {
				return;
			}
            if(listFile.size() > 0) {
                if(position >= listFile.size()){
                    position = listFile.size()-1;
                }
            }
            myPosition = position;
            Log.d(TAG, "1107::server_list_item_clicked");
            // 若点击服务器列表中的item
            String[] splitedPath = pathTxt.getText().toString().split("/");
            TextView text = (TextView) v.findViewById(R.id.text);
            if (parent.equals(listView) && (splitedPath.length == 1)) {
                // 判定当前点击的是否为从网上搜索到得服务器
                // SambaTree 功能
                fileL.clear();

                Log.e("=============1239", "PATH"+ pathTxt.getText().toString());
                if (pathTxt.getText().toString().equals("")
                        && (intList.size() == 0)) {
                    Log.w("SHORT", list.get(position).get(SHORT).toString());
                    if (list.get(position).get(SHORT).toString().equals("1")) {
                        Userserver = list.get(position).get(SERVER_IP)
                                .toString();
                        folder_position = list.get(position).get(WORK_PATH)
                                .toString().replace("\\", "/");
                        Username = list.get(position).get(ACCOUNT).toString();
                        Userpass = list.get(position).get(PASSWORD).toString();
                        String nick = list.get(position).get(NICK_NAME)
                                .toString();
                        serverName = nick.substring(2, nick.length()
                                - folder_position.length() - 1);
                        mountPath(position, -1);
                    } else {
                        // 获得所有网络
                        clickPos = 0;
                        willClickNetDir = true;
                        Log.w("UMOUNT1014", "UMOUNT");
                        searchServers();
                        // willClickNetDir = false;
                        // pathTxt.setText(R.string.all_network);
                    }
                } else if (intList.size() == 1) {
                    serverName = text.getText().toString();
                    // intList.add(position);
                    Log.e("==1225==", " SERVERNAME " + serverName);
                    willClickNetDir = true;
                    // begin add by qian_wei/cao_shanshan 2011/10/22
                    // for clear the loginName
                    loginName = "";
                    // end add by qian_wei/cao_shanshan 2011/10/22
                    showServerFolderList(text.getText().toString(), "", "");
                } else if (intList.size() == 2) {
                    if (workFolderList != null) {
                        Log.w("1025", " = " + serverName);
                        for (int i = 0; i < workFolderList.size(); i++) {
                            String str = workFolderList.get(i).get(WORK_PATH)
                                    .toString().replace("\\", "/");
                            Log.w("1030", " = " + str);
                        }
                        Userserver = workFolderList.get(position)
                                .get(SERVER_IP).toString();
                        folder_position = workFolderList.get(position).get(
                                WORK_PATH).toString().replace("\\", "/").toUpperCase();
                        // begin modify by qian_wei/cao_shanshan 2011/10/22
                        // for click shortcut will cause ANR
//						if (loginName != "") {
//                            Username = loginName;
//                            Userpass = loginPass;
//                        } else {
//                            Username = "g";
//                            Userpass = "";
//                        }

                        cursor = sqlite.query(TABLE_NAME, new String[] { ID, ACCOUNT,
                                PASSWORD, NICK_NAME }, SERVER_IP + " =? and "
                                        + WORK_PATH + " = ?", new String[] { Userserver,
                                folder_position }, null, null, null);
                        if(cursor.moveToFirst()) {
                            Username = cursor.getString(cursor.getColumnIndex(ACCOUNT));
                            Userpass = cursor.getString(cursor.getColumnIndex(PASSWORD));
                        } else {
                            if (loginName != "") {
                                Username = loginName;
                                Userpass = loginPass;
                            } else {
                                Username = "g";
                                Userpass = "";
                            }
                        }
                        cursor.close();
                        // end modify by qian_wei/cao_shanshan 2011/10/22
                        mountPath(position, 0);
                    }

                }
            } else {
                // begin add by qian_wei/zhou_yong 2011/10/20
                // for chmod the file
                //chmodFile(listFile.get(position).getPath());
                // end modify by qian_wei/zhou_yong 2011/10/20
                if (listFile.get(position).canRead()) {
                    if (listFile.get(position).isDirectory()) {
                        intList.add(myPosition);
                        Log.e("ADD1047", "POSITION");
                        clickPos = 0;
                    } else {
                        clickPos = position;
                    }
                    // begin modify by qian_wei/xiong_cuifan 2011/11/05
                    // for broken into the directory contains many files,click again error
                    preCurrentPath = currentFileString;
                    keyBack = false;
                    // end modify by qian_wei/xiong_cuifan 2011/11/08
                    // myPosition = 0;
                    getFiles(listFile.get(position).getPath());
                } else {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.file_cannot_read));
                }
            }
        }
    };

//	AlertDialog sureDialog;

    private void mountPath(final int position, int flag) {
        // begin modify by qian_wei/zhou_yong 2011/10/28
        // for extend the mount time
        allOrShort = flag;
        builder = new StringBuilder(Userserver);
//		final StringBuilder builder = new StringBuilder(Userserver);
        builder.append("/").append(folder_position);
        Log.e("=================1284", " = " + folder_position);
        String returnStr = jni.getMountList(builder.toString());
        if (returnStr.equals("ERROR")) {
//	          SambaManager samba = (SambaManager) getSystemService("Samba");
//	            samba.start("", "", "", "");
//	            Log.w("NAME = PASS", " = "+Username+" = "+Userpass);
//	            result = jni.UImount(Userserver, folder_position, " ", Username,
//	                    Userpass);
//	            Log.w("RESULT", " = "+result);
//	            if (result == 0) {
//	                if (flag == -1) {
//	                    showData(builder.toString(), sureDialog, position);
//	                } else {
//	                    cursor = sqlite.query(TABLE_NAME, new String[] { ID,
//	                            ACCOUNT, PASSWORD, NICK_NAME }, SERVER_IP
//	                            + " =? and " + WORK_PATH + " = ?", new String[] {
//	                            Userserver, folder_position }, null, null, null);
//	                    if (cursor.moveToFirst()) {
//	                        showData(builder.toString(), sureDialog, position);
//	                    } else {
//	                        AlertDialog.Builder alert = new AlertDialog.Builder(
//	                                this);
//	                        alert.setMessage(getString(R.string.is_add_short));
//	                        alert.setPositiveButton(getString(R.string.ok),
//	                                new DialogInterface.OnClickListener() {
//
//	                                    public void onClick(DialogInterface dialog,
//	                                            int which) {
//	                                        addShortCut();
//	                                        showData(builder.toString(),
//	                                                sureDialog, position);
//	                                    }
//	                                });
//	                        alert.setNegativeButton(getString(R.string.cancel),
//	                                new DialogInterface.OnClickListener() {
//
//	                                    public void onClick(DialogInterface dialog,
//	                                            int which) {
//	                                        showData(builder.toString(),
//	                                                sureDialog, position);
//	                                    }
//	                                });
//	                        sureDialog = alert.create();
//	                        sureDialog.show();
//	                    }
//	                    cursor.close();
//	                }
//	            } else {
//	                // begin add by qian_wei/cao_shanshan 2011/10/21
//	                // for show notification while the net is down
//	                if (result == NET_ERROR) {
//	                    FileUtil.showToast(SambaActivity.this,
//	                              getString(R.string.network_error));
//	                } else {
//	                    if(flag == -1) {
//	                        mountShortCut(position);
//	                    } else {
//	                        showSetNewMountDlg(list.get(intList.get(0)),
//	                                workFolderList.get(position), position);
//	                    }
//	                }

//	                cursor = sqlite.query(TABLE_NAME, new String[] { ID, ACCOUNT,
//	                        PASSWORD, NICK_NAME }, SERVER_IP + " =? and "
//	                        + WORK_PATH + " = ?", new String[] { Userserver,
//	                        folder_position }, null, null, null);
    //
//	                if (cursor.moveToFirst()) {
//	                    Username = cursor.getString(cursor.getColumnIndex(ACCOUNT));
//	                    Userpass = cursor
//	                            .getString(cursor.getColumnIndex(PASSWORD));
//	                    serverName = cursor.getString(cursor
//	                            .getColumnIndex(NICK_NAME));
//
//	                    samba = (SambaManager) getSystemService("Samba");
//	                    samba.start("", "", "", "");
//	                    result = jni.UImount(Userserver, folder_position, " ",
//	                            Username, Userpass);
//	                    if (result == 0) {
//	                        returnStr = jni.getMountList(builder.toString());
//	                        localPath = returnStr;
//	                        mountSdPath = localPath;
//	                        clickPos = 0;
//	                        getDirectory(returnStr);
//	                        if (getFileFlag(currentFileString, prePath) == 1) {
//	                            Log.e("ADD1120", "POSITION");
//	                            intList.add(position);
//	                        }
//	                    } else {
//	                        mountShortCut(position);
//	                    }
//	                } else {
//	                    if (flag == 0) {
//	                        showSetNewMountDlg(list.get(intList.get(0)),
//	                                workFolderList.get(position), position);
//	                    } else {
//	                        mountShortCut(position);
//	                    }
//	                }
//
//	                cursor.close();
                 // end add by qian_wei/cao_shanshan 2011/10/21
//	            }
            progress = new ProgressDialog(this);
            progress.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                    return true;
                }
            });
            progress.show();
            MountThread thread = new MountThread(MOUNT_RESULT_3);
            thread.start();
            // end modify by qian_wei/zhou_yong 2011/10/28
        } else {
            localPath = returnStr;
            mountSdPath = localPath;
            clickPos = 0;
            File file = new File(localPath);
            if (file.isDirectory() && file.canRead()) {
                Log.e("ADD1165", "POSITION");
                intList.add(position);
            }
            myPosition = 0;
            getDirectory(returnStr);
            // Log.w("CURRENT", " = "+currentFileString);
            // Log.w("PREVIEW", " = "+prePath);
            // if (getFileFlag(currentFileString, prePath) == 1) {
            // intList.add(position);
            // }
        }
    }

    private void showData(String str, AlertDialog dialog, int position) {
        String returnStr = jni.getMountList(str.toString());
        localPath = returnStr;
        mountSdPath = localPath;
        clickPos = 0;
        File file = new File(localPath);
        if (file.isDirectory() && file.canRead()) {
            intList.add(position);
            Log.e("ADD1090", "POSITION");
        }
        getDirectory(returnStr);
    }

    private void mountShortCut(int position) {
        List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        cursor = sqlite.query(TABLE_NAME, new String[] { ID, SERVER_IP,
                WORK_PATH, NICK_NAME }, SERVER_IP + " =? and " + WORK_PATH
                + " = ?", new String[] { Userserver, folder_position }, null,
                null, null);
        // 将具体的多个共享目录加入到数据库中
        if (cursor.moveToFirst()) {
            map = new HashMap<String, Object>();
            map.put(IMAGE, R.drawable.folder_file);
            String serverIp = cursor
                    .getString(cursor.getColumnIndex(SERVER_IP));
            map.put(SERVER_IP, serverIp);
            String workPath = cursor
                    .getString(cursor.getColumnIndex(WORK_PATH));
            map.put(WORK_PATH, workPath);
            map.put(NICK_NAME, cursor.getString(cursor
                    .getColumnIndex(NICK_NAME)));
            groupList.add(map);
        }
        cursor.close();
        if (groupList.size() == 0) {
            FileUtil.showToast(this, getString(R.string.quary_error));
        } else {
            showSetNewMountDlg(list.get(position), groupList.get(0), position);
        }
    }

    private void showServerFolderList(String nickName, String account,
            String pass) {

        List<Map<String, Object>> groupDetails;
        Username = account;
        Userpass = pass;
        if (loginName != "") {
            Username = loginName;
            Userpass = loginPass;
        } else {
            Username = "";
            Userpass = "";
        }

        String detailGroup = sambaTree.getDetailsBy(nickName, Username,
                Userpass);
        // 输入用户名密码
        Log.i("===================1317", detailGroup);
        if (null != detailGroup
                && detailGroup.equals("NT_STATUS_ACCESS_DENIED")) {
            nServerLogonDlg = new NewCreateDialog(SambaActivity.this);
            View view = inflateLayout(SambaActivity.this,
                    R.layout.server_log_on);
            nServerLogonDlg.setView(view);
            // nServerLogonDlg.setTitle(title)
            nServerLogonDlg.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.ok), new DialogClickListener(nickName));
            nServerLogonDlg.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.cancel), new DialogClickListener());

            nServerLogonDlg.show();
            nServerLogonDlg.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextAppearance(SambaActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
            nServerLogonDlg.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextAppearance(SambaActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
        } else if (null != detailGroup
                && detailGroup.equals("NT_STATUS_LOGON_FAILURE")) {
            loginName = "";
            FileUtil.showToast(SambaActivity.this,
                    getString(R.string.login_server_error));
            return;
        } else if (null != detailGroup && detailGroup.equals("ERROR")) {
            FileUtil.showToast(SambaActivity.this,
                    getString(R.string.error_folder));
            return;
        }
        // 显示共享目录
        else {
            if (null != detailGroup && !"".equals(detailGroup)
                    && !detailGroup.toLowerCase().equals("error")) {
                // 解析多个共享目录信息
                groupDetails = parse2DetailDirectories(detailGroup);
                workFolderList = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = null;
                // 将具体的多个共享目录加入到数据库中
                for (int i = 0; i < groupDetails.size(); i++) {
                    map = new HashMap<String, Object>();
                    map.put(IMAGE, R.drawable.folder_file);
                    String serverIp = groupDetails.get(i).get(SERVER_IP)
                            .toString();
                    Log.d(TAG, "2826::server_ip=" + serverIp);
                    map.put(SERVER_IP, serverIp);
                    String workPath = groupDetails.get(i).get(WORK_PATH)
                            .toString();
                    Log.d(TAG, "2830::work_path=" + workPath);
                    map.put(WORK_PATH, workPath);
                    map.put(NICK_NAME, nickName);
                    workFolderList.add(map);
                }
                if (willClickNetDir) {
                    Log.e("ADD1236", "POSITION");
                    intList.add(myPosition);
                }
                SimpleAdapter adapter = new SimpleAdapter(SambaActivity.this,
                        workFolderList, R.layout.file_row, new String[] {
                                IMAGE, WORK_PATH }, new int[] {
                                R.id.image_Icon, R.id.text });
                Log.d("===1445====", nickName);
                pathTxt.setText(nickName);
                Log.d(TAG, "1372::[showServerFolderList]workFolderList.size="
                        + workFolderList.size());
                Log.d(TAG, "1372::[showServerFolderList]listview=" + listView);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(ItemClickListener);
                listView.setOnItemSelectedListener(itemSelect);
            }
        }
    }

    /**
     * 删除操作监听器
     */
    private OnItemClickListener deleListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            ControlListAdapter adapter = (ControlListAdapter) l.getAdapter();
            CheckedTextView check = (CheckedTextView) v
                    .findViewById(R.id.check);
            String path = adapter.getList().get(position).getPath();
            if (check.isChecked()) {
                selected.remove(path);
                check.setChecked(false);
            } else {
                selected.add(path);
                check.setChecked(true);
            }
        }
    };

    /**
     * 获取本地映射目录中所有文件信息
     *
     * @param path
     *            本地映射目录路径
     */
//	SocketClient socketClient = null;
//	private File openFile;
//	private String prevPath = "";

    public void getFiles(String path) {
        // item = item01;
        openFile = new File(path);
        if (openFile.isDirectory()) {
            // begin modify by qian_wei/xiong_cuifan 2011/11/05
            // for broken into the directory contains many files,click again error
//			if (currentFileString.length() < path.length()) {
//				myPosition = 0;
//			}
            // end modify by qian_wei/xiong_cuifan 2011/11/08
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
                  socketClient.writeMess("mountiso " + super.tranString(openFile.getPath()));
                }catch(Exception e)
                {
                    Log.e(TAG," mountiso file error e="+e);
                    FileUtil.showToast(this, "mountiso file error");
                }
            } else if (openFile.isFile()) {
                super.openFile(this, openFile);
            } else {
                FileUtil.showToast(this, getString(R.string.network_error));
            }
        }

    };

    /**
     * 列出文件列表
     *
     * @param files
     *            挂载到本地目录中的文件列表， 即服务器共享目录中的文件列表
     * @param fileroot
     *            本地挂载路径对象
     */
    public void fill(File fileroot) {
        try {
            Log.w(" = == IN 1364", " = == IN 1364");
            // li = new ArrayList<File>();//
//			li = adapter.getFiles();
            if(clickPos >= listFile.size()) {
                clickPos = listFile.size() -1;
            }
            numInfo.setText((clickPos + 1) + "/" + listFile.size());
            if (!fileroot.getPath().equals(directorys)) {
                parentPath = fileroot.getParent();
                currentFileString = fileroot.getPath();
                // Log.v("root", fileroot.getParent());
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
            pathTxt.setText(serverName + fileroot.getPath());

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

    // 批量删除对话框
    AlertDialog attrServerDeletingDialog;

    /**
     * 对菜单操作进行细化操作： 对文件操作 “粘贴”，“重命名”及“删除” 对服务器操作
     */
    private void managerF(final int position, final int item) {
        flagItem = item;
        if (temp == 1)// operate file
        {
            // begin modify by qian_wei/cao_shanshan 2011/10/24
            // for while first delete more than one file then cause exception
//			if(position == listFile.size()){
            if(position >= listFile.size()){
                myPosition = listFile.size()-1;
            }
            // end modify by qian_wei/cao_shanshan 2011/10/24
            Log.e("------------------>", "------------->" + myPosition);
            if ((item == MENU_PASTE) || (item == MENU_RENAME)) {
                getMenu(myPosition, item, listViews);
            } else {// 进行删除操作

                View view = inflateLayout(SambaActivity.this,
                        R.layout.samba_server_list_dlg_layout);
                alertDialog = new NewCreateDialog(SambaActivity.this);
                alertDialog.setView(view);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.ok), imageButClick);
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel), imageButClick);
                alertDialog.show();
                alertDialog = FileUtil.setDialogParams(alertDialog,
                        SambaActivity.this);

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextAppearance(SambaActivity.this,
                                android.R.style.TextAppearance_Large_Inverse);
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .requestFocus();
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setTextAppearance(SambaActivity.this,
                                android.R.style.TextAppearance_Large_Inverse);

                listViews = (ListView) alertDialog
                        .findViewById(R.id.lvSambaServer);

                selected.clear();
                listViews.setItemsCanFocus(false);
                listViews.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listViews.setAdapter(new ControlListAdapter(SambaActivity.this,
                        listFile));
                listViews.setItemChecked(myPosition, true);
                listViews.setSelection(myPosition);
                selected.add(listFile.get(myPosition).getPath());
                listViews.setOnItemClickListener(deleListener);
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .requestFocus();

            }
        } else {// server
            // 获得当前焦点所在位置view
            // View v = listView.getSelectedView();
            // 获取当前焦点所在位置值
            int selectedPos = listView.getSelectedItemPosition() - 1;
            Log.d(TAG, "1571::managerF()_selectedPosition=" + selectedPos);
            if(selectedPos == -1) {
                selectedPos = 0;
            }
            Log.d(TAG,"1595::=============start_delete_show===============");
            View view = inflateLayout(SambaActivity.this,
                    R.layout.samba_server_list_dlg_layout);
            attrServerDeletingDialog = new NewCreateDialog(SambaActivity.this);
            attrServerDeletingDialog.setView(view);
            attrServerDeletingDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.ok), new DialogClickListener(
                            SambaActivity.this, list));
            attrServerDeletingDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.cancel), new DialogClickListener(
                            SambaActivity.this, list));
            attrServerDeletingDialog.show();
            attrServerDeletingDialog = FileUtil.setDialogParams(
                    attrServerDeletingDialog, SambaActivity.this);

            //
            ListView lvServer = (ListView) attrServerDeletingDialog
                    .findViewById(R.id.lvSambaServer);
            // 设置服务器数据源
            lvServer.setAdapter(new SambaServerAdapter(SambaActivity.this,
                    convertMapToHashMap(list), -1));

            lvServer.setItemsCanFocus(false);
            lvServer.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lvServer.setClickable(true);
            lvServer.setItemChecked(selectedPos, true);

            attrServerDeletingDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .requestFocus();

            attrServerDeletingDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextAppearance(SambaActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
            attrServerDeletingDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .requestFocus();
            attrServerDeletingDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextAppearance(SambaActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
        }

    }

    /**
     *
     * @param position
     * @param item
     * @param list
     */
    private void getMenu(final int position, final int item, final ListView list) {
        int selectionRowID = (int) position;
        File file = null;
        File myFile = new File(currentFileString);
        FileMenu menu = new FileMenu();
        SharedPreferences sp = getSharedPreferences("OPERATE", SHARE_MODE);
        if (item == MENU_RENAME) {
            fileArray = new ArrayList<File>();
            file = new File(currentFileString + "/"
                    + listFile.get(selectionRowID).getName());
            fileArray.add(file);
            menu.getTaskMenuDialog(SambaActivity.this, myFile, fileArray, sp,
                    item, 0);
        } else if (item == MENU_PASTE) {
            fileArray = new ArrayList<File>();
            menu.getTaskMenuDialog(SambaActivity.this, myFile, fileArray, sp,
                    item, 0);
        } else {
            fileArray = new ArrayList<File>();
            for (int i = 0; i < selected.size(); i++) {
                file = new File(selected.get(i));
                fileArray.add(file);
            }
            menu.getTaskMenuDialog(SambaActivity.this, myFile, fileArray, sp,
                    item, 0);
        }

    }

    List<File> fileL = null;

    // ImageButton 按钮单击事件
    private DialogInterface.OnClickListener imageButClick = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (selected.size() > 0) {
                    getMenu(myPosition, flagItem, listViews);
                    alertDialog.cancel();
                } else {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.select_file));
                }
            } else {
                alertDialog.cancel();
            }
        }
    };

    /**
     * 文件列表（包括列表形式和缩略图显示）项选择监听
     */
    OnItemSelectedListener itemSelect = new OnItemSelectedListener() {

        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            if (!pathTxt.getText().toString().equals("")
                    && !pathTxt.getText().toString().equals(serverName)) {
                myPosition = position;
                numInfo.setText((position + 1) + "/" + listFile.size());
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    File[] sortFile;
    /**
     * 根据不同排序,更新文件列表
     *
     * @param sort
     */
    FileUtil util;

    public void updateList(boolean flag) {
        if (flag) {
            Log.i(TAG, "updateList");
            // begin add by qian_wei/xiong_cuifan 2011/11/05
            // for broken into the directory contains many files,click again error
            listFile.clear();
            showBut.setOnClickListener(clickListener);
            sortBut.setOnClickListener(clickListener);
            filterBut.setOnClickListener(clickListener);
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            progress = new ProgressDialog(SambaActivity.this);
            progress.show();
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }

            waitThreadToIdle(thread);

            thread = new MyThread();
            thread.setStopRun(false);
            progress.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Log.v("\33[32m Main1","onCancel" + "\33[0m");
                    thread.setStopRun(true);
                    if(keyBack) {
                        intList.add(clickPos);
                    } else {
                        clickPos = myPosition;
                        currentFileString = preCurrentPath;
                        Log.v("\33[32m Main1","onCancel" + currentFileString +"\33[0m");
                        intList.remove(intList.size()-1);
                    }
                    FileUtil.showToast(SambaActivity.this, getString(R.string.cause_anr));
                }
            });
            // end add by qian_wei/xiong_cuifan 2011/11/08
            thread.start();
        } else {
            adapter.notifyDataSetChanged();
            fill(new File(currentFileString));
        }
        // util = new FileUtil(this, filterCount, fileL, currentFileString);
        // util.fillData(sortCount, adapter);
    }

    int clickCount = 0;
    int clickPos = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("@@@@SambaActivity[onKeyDown]", "2173::keycode=" + keyCode);
        boolean flag = pathTxt.getText().toString().equals(serverName);
        switch (keyCode) {

        // 添加对enter键和dpad_center键支持
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_CENTER:
            super.onKeyDown(KeyEvent.KEYCODE_ENTER, event);
            return true;

        case KeyEvent.KEYCODE_BACK:// KEYCODE_BACK
            keyBack = true;
            willClickNetDir = false;
            if (intList.size() == 0) {
                clickCount++;
                if (clickCount == 1) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.quit_app));
                } else if (clickCount == 2) {
                    SharedPreferences share = getSharedPreferences("OPERATE",
                            SHARE_MODE);
                    share.edit().clear().commit();
                    if (FileUtil.getToast() != null) {
                        FileUtil.getToast().cancel();
                    }
                    onBackPressed();
                }
            } else {
                Log.w(" ---------1903", " = " + pathTxt.getText().toString());
                if (!pathTxt.getText().toString().equals("")) {
                    clickCount = 0;
                    // 未挂载状态
                    if (currentFileString.equals("") && flag) {
                        pathTxt.setText("");
                        // servers();
                        willClickNetDir = false;
                        Log.w("UMOUNT1857", "UMOUNT");
                        searchServers();

                    } else if (currentFileString.equals(localPath)) {

                        // 退出挂载目录后，清空按钮点击状态
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
                        listView.setVisibility(View.VISIBLE);
                        currentFileString = "";
                        listFile.clear();
                        if (list.get(intList.get(0)).get(SHORT).toString()
                                .equals("1")) {
                            pathTxt.setText("");
                            clickPos = intList.get(intList.size() - 1);
                            servers();
                        } else {
                            Log.e("POS 2378", " = " + intList.size());
                            // 到达共享目录层
                            willClickNetDir = false;
                            if (loginName != "") {
                                showServerFolderList(workFolderList.get(
                                        intList.get(intList.size() - 1)).get(
                                        NICK_NAME).toString(), loginName,
                                        loginPass);
                            } else {
                                showServerFolderList(workFolderList.get(
                                        intList.get(intList.size() - 1)).get(
                                        NICK_NAME).toString(), Username,
                                        Userpass);
                            }
                            pathTxt.setText(serverName);
                        }

                    } else {
                        fileL.clear();
                        if (currentFileString.equals(ISO_PATH)) {
                            Log.w("PREV", " = " + prevPath);
                            getFiles(prevPath);
                        } else {
                            getFiles(parentPath);
                        }
                    }
                    if (intList.size() > 0) {
                        int pos = intList.size() - 1;
                        if (listView.getVisibility() == View.VISIBLE) {
                            clickPos = intList.get(pos);
                            listView.setSelection(clickPos);
                            Log.e("REMOVE1898", "POSITION");
                            intList.remove(pos);
                        } else if (gridView.getVisibility() == View.VISIBLE) {
                            clickPos = intList.get(pos);
                            Log.e("REMOVE1902", "POSITION");
                            intList.remove(pos);
                        }
                    }
                    Log.w(" = = == = == = 1968", " = " + intList.size());
                } else {
                    servers();
                    Log.w("222", "111");
                    if (intList.size() > 0) {
                        int pos = intList.size() - 1;
                        if (listView.getVisibility() == View.VISIBLE) {
                            clickPos = intList.get(pos);
                            listView.setSelection(clickPos);
                            Log.e("REMOVE1915", "POSITION");
                            intList.remove(pos);
                            Log.w("333", "111");
                        }

                    }
                }
            }

            return true;

        case KeyEvent.KEYCODE_SEARCH: // search
            if (!pathTxt.getText().toString().equals("")
                    && !pathTxt.getText().toString().equals(serverName)) {
                searchFileDialog();
            }
            return true;
        case KeyEvent.KEYCODE_INFO: // info
            if (!pathTxt.getText().toString().equals("")
                    && !pathTxt.getText().toString().equals(serverName)) {
                FileUtil util = new FileUtil(this);
                util.showFileInfo(listFile.get(myPosition));
            }
            return true;
        case KeyEvent.KEYCODE_HELP: // help
            FileMenu.setHelpFlag(2);
            FileMenu.filterType(SambaActivity.this, MENU_HELP, null);
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

    public String getCurrentFileString() {
        return currentFileString;
    }

	/**
	 * 执行系统命令 z00120637
	 */
	private void doCmdSystem(String strCmd) {
		SocketClient sktClint = new SocketClient(this, false);

		try {
			sktClint.writeMess("system " + strCmd);
			// sktClint.readNetResponseSync();
		} catch (Exception e) {
			Log.e(TAG, " doCmdSystem do " + strCmd + " ,,!error=" + e);
		}
		sktClint = null;
	}

	/*
     * 搜索samba服务器
     */
    void searchServers() {
		//BEGIN : ADD FOR NOT FIND THE SAMBA SERVER
		doCmdSystem("rm /data/app/samba/var/locks/gencache.tdb");
		doCmdSystem("rm /data/app/samba/var/locks/gencache_notrans.tdb");
		//END   : ADD FOR NOT FIND THE SAMBA SERVER

        sambaTree = new SambaTreeNative();
        timer = new Timer();
        waitLong = 0;
        Log.d(TAG, "2385::init_waitLong=" + waitLong);
        totalLong = 120 * 1000;
        // 显示等待对话框
        pgsDlg = new ProgressDialog(SambaActivity.this);
        pgsDlg.setTitle(R.string.wait_str);
        pgsDlg.setMessage(getString(R.string.search_str));
        pgsDlg.show();
        Log.d(TAG, "2392::progress_dialog_shown");
        // 开启一个线程，每隔5秒钟执行,控制ProgressDialog的显示状态
        timer.schedule(new TimerTask() {
            public void run() {

                if(pgsDlg != null && pgsDlg.isShowing())
                {
                    waitLong += 3000;// 没5秒中进行一次增加
                    Log.d(TAG, "2347::waitLong=" + waitLong);
                    // Message msg = new Message();
                    // 比较时间
                    if (waitLong >= totalLong) {
                        Log.d(TAG, "2351::dismiss_progress_dialog");
                        strWorkgrpups = "error";
                        handler.sendEmptyMessage(END_SEARCH);
                        return;
                    }
                    Log.d(TAG, "2355::not_dismiss");

                    // 当获取到得字符串有值,关闭等待对话框
                    if (!"".equals(strWorkgrpups) && null != strWorkgrpups) {
                        if (strWorkgrpups.toLowerCase().equals("error")) {
                            handler.sendEmptyMessage(NET_ERROR);// 发送消息
                        } else {
                            handler.sendEmptyMessage(NET_NORMAL);// 发送消息
                        }

                        return;
                    }
                }
                else
                {
                    return;
                }
            }

        }, 1000, 3000);

        // 开启一个线程，进行服务器搜索
        new Thread(new Runnable() {

            public void run() {
                Log.d(TAG, "2508::call_getWorkgroups()");
                // 当没有进行搜索时，进入判断，调用搜索方法
                if (strWorkgrpups == null || "".equals(strWorkgrpups)
                        || strWorkgrpups.toLowerCase().equals("error")) {
                    Log.d(TAG, "2511::call_getWorkgroups()");
                    strWorkgrpups = sambaTree.getWorkgroups();
                }
            }
        }).start();

    }

    // // 用于控制ProgressDialog的显示状态
    // Handler progressHandler = new Handler() {
    // public void handleMessage(Message msg) {
    // super.handleMessage(msg);
    // Log.d(TAG, "2376::handle_message");
    // switch (msg.what) {
    // case 1:
    // case 2:
    // Log.d(TAG, "2398::dismiss_dialog");
    // if (SambaActivity.this != null
    // && !SambaActivity.this.isFinishing()) {
    // pgsDlg.dismiss();// 关闭等待对话框
    // }
    // if (timer != null) {
    // timer.cancel();
    // timer.purge();
    // }
    // // 在等待ProgressDialog关闭后,调用解析字符串方法
    // if (!strWorkgrpups.toLowerCase().equals("error")) {
    // Log.d(TAG, "2424::strWorkgroups=" + strWorkgrpups);
    // if (willClickNetDir) {
    // Log.e("ADD2100", "POSITION");
    // intList.add(myPosition);
    // }
    // updateServerListAfterParse(strWorkgrpups);
    // }
    // /*
    // * @tag :begin modified by ni_guanhua 2011/7/4
    // *
    // * @brief :搜索超时给出的提示
    // */
    // else {
    // FileUtil.showToast(SambaActivity.this,
    // getString(R.string.net_time_out));
    // }
    // break;
    // case -1:
    // if (SambaActivity.this != null
    // && !SambaActivity.this.isFinishing()) {
    // pgsDlg.dismiss();// 关闭等待对话框
    // }
    // if (timer != null) {
    // timer.cancel();
    // timer.purge();
    // }
    // strWorkgrpups = "";
    // FileUtil.showToast(SambaActivity.this,
    // getString(R.string.no_server));
    // break;
    // }
    // };
    // };

    /**
     * 对格式化的字符串进行解析，解析后再弹出对话框中 显示解析后的字符串列表
     *
     * @param workgroups
     *            格式化后的字符串参数
     * @return
     */
    private void updateServerListAfterParse(String workgroup) {
        Log.d(TAG, "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&updateServerListAfterParse");
        List<HashMap<String, Object>> groups = new ArrayList<HashMap<String, Object>>(
                1);
        if (!workgroup.equals("")) {
            String[] workgroups = workgroup.split("\\|");
            Log.d(TAG, "2481::workgroups_length=" + workgroups.length);
            HashMap<String, Object> detailMap;// 放置具体服务器及其相关信息
            // 对信息进行分割
            for (int i = 0; i < workgroups.length; i++) {
                detailMap = new HashMap<String, Object>(1);
                // 对每个pc信息进行解析
                String[] details = workgroups[i].split(":");
                // 过滤去除“\\”
                String trimStr = details[0].trim();
                String pcName = trimStr.substring(
                        trimStr.lastIndexOf("\\") + 1, trimStr.length());
                detailMap.put(NICK_NAME, pcName);// 放入Pc名

                // 当信息中同时存在pc名和信息介绍
                if (details.length == 2) {
                    // Log.d(TAG, "2494::pc_details=" + details[1].trim());
                    detailMap.put(SERVER_INTRO, details[1].trim());// 放入pc内容

                } else if (details.length == 1) {// 在信息中只存在pc名，不存在pc减少
                    // Log.d(TAG, "2498::pc_info=" + details[0].trim());
                    detailMap.put(SERVER_INTRO, "No Details");// 放入pc内容
                }
                // 放入图片
                detailMap.put(IMAGE, R.drawable.mainfile);
                groups.add(detailMap);// 将格式化的内容加入到列表中
            }
        }
        Log.w("SIZE = =", " = " + intList.size());
        // 设置显示服务器列表
        listSearchedServers(groups);// 更新列表操作
        Log.d(TAG, "2511::server_list_size=" + list.size());

    }

    // 显示samba服务器列表对话框
    AlertDialog serverDialog;

    /**
     * 在弹出对话框中罗列搜索到得samba服务器
     *
     * @param groups
     *            经解析后得到的具体的服务器列表
     */

    SambaServerAdapter smbAdapter;

    private void listSearchedServers(List<HashMap<String, Object>> groups) {

        Log.d(TAG, "2524::groups_size=" + groups.size());
        smbAdapter = new SambaServerAdapter(this, groups, 0);
        listView.setAdapter(smbAdapter);
        listView.setSelection(clickPos);
        listView.setOnItemClickListener(ItemClickListener);
        listView.setOnItemSelectedListener(itemSelect);
    }

    /**
     * samba-server服务端适配器
     *
     * @author ni_guanhua
     */
    static class SambaServerAdapter extends BaseAdapter {

        Context context;

        List<HashMap<String, Object>> groups;

        int flag;

        public SambaServerAdapter(Context context,
                List<HashMap<String, Object>> groups, int flag) {
            this.context = context;
            this.groups = groups;
            this.flag = flag;
        }

        public int getCount() {
            return groups.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (flag == 0) {
                RelativeLayout layout = (RelativeLayout) inflater.inflate(
                        R.layout.file_row, null);
                ImageView img = (ImageView) layout
                        .findViewById(R.id.image_Icon);
                TextView text = (TextView) layout.findViewById(R.id.text);
                text.setText(groups.get(position).get(NICK_NAME).toString());
                img.setImageResource(R.drawable.mainfile);
                return layout;
            } else {
                CheckedTextView chktv = (CheckedTextView) inflater.inflate(
                        R.layout.samba_server_checked_text_view, null);
                String textSamba = (String) groups.get(position).get(NICK_NAME);
                // 若需要显示的内容长度大于view自身的长度,显示跑马灯效果
                if (chktv.getWidth() < chktv.getPaint().measureText(textSamba)) {
                    chktv.setEllipsize(TruncateAt.MARQUEE);
                    chktv.setMarqueeRepeatLimit(-1);
                    chktv.setHorizontallyScrolling(true);
                }
                chktv.setText(textSamba);
                return chktv;
            }
        }
    }

    /**
     * 服务器列表对话框按钮事件
     *
     * @author ni_guanhua
     */
    class DialogClickListener implements DialogInterface.OnClickListener {
        // Context context;

        // 放置解析搜索后的服务器信息
        // List<HashMap<String, Object>> groups;

        // list对象，放置服务器HashMap
        List<Map<String, Object>> mainList;

        String svrName;

        public DialogClickListener() {
            Log.d(TAG, "2645::DialogClickListener_constructor_list=");
        }

        public DialogClickListener(String serverName) {
            this.svrName = serverName;
        }

        public DialogClickListener(Context context,
                List<Map<String, Object>> list) {
            this(context, null, list);
            Log.d(TAG, "2650::DialogClickListener_constructor_list=" + list);
        }

        public DialogClickListener(Context context,
                List<HashMap<String, Object>> groups,
                List<Map<String, Object>> mainList) {
            // this.context = context;
            // this.groups = groups;
            this.mainList = mainList;
            Log
                    .d(TAG, "2659::DialogClickListener_constructor_list="
                            + mainList);
        }

        public void onClick(final DialogInterface dialog, int which) {
            if (nServerLogonDlg == dialog) {
                if (DialogInterface.BUTTON_POSITIVE == which) {
                    EditText edtServerAccount = (EditText) ((AlertDialog) dialog)
                            .findViewById(R.id.edtServerAccount);
                    EditText edtServerPassword = (EditText) ((AlertDialog) dialog)
                            .findViewById(R.id.edtServerPassword);
                    loginName = edtServerAccount.getText().toString().trim();
                    loginPass = edtServerPassword.getText().toString().trim();
                    if (null == loginName || BLANK.equals(loginName)) {
                        // || null == password || BLANK.equals(password)) {
                        FileUtil.showToast(SambaActivity.this,
                                getString(R.string.input_name_pass));
                        nServerLogonDlg.show();
                        return;
                    }
                    showServerFolderList(svrName, loginName, loginPass);
                    return;
                }
                if (DialogInterface.BUTTON_NEGATIVE == which) {
                    dialog.dismiss();
                    return;
                }

            }

            // 进行批量删除服务器对话框
            if (attrServerDeletingDialog == dialog) {
                Log.d(TAG,"2734::==============show_delete_dialog");
                Log.d(TAG, "2724::mainList=" + mainList);
                if (DialogInterface.BUTTON_POSITIVE == which) {
                    AlertDialog confirmDialog = new AlertDialog.Builder(
                            SambaActivity.this).setIcon(R.drawable.alert)
                            .setTitle(R.string.del_server_dlg_title)
                            .setMessage(R.string.comfirm_delete_hint)
                            .setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(
                                                DialogInterface confirmDialog,
                                                int which) {
                                            // 实际进行删除操作
                                            doDeleteSeletedServers(dialog,
                                                    which, mainList);
                                        }
                                    }).setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(
                                                DialogInterface confirmDialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    }).create();
                    Log
                            .d(TAG,
                                    "2754::==============confirm_dialog_shown=========================");
                    confirmDialog.show();
                    return;
                }
                if (DialogInterface.BUTTON_NEGATIVE == which) {
                    dialog.dismiss();
                    return;
                }
            }
        }
    }

    /**
     * 设置要插入到数据库中的值
     *
     * @param ip
     *            服务器地址
     * @param nichName
     *            昵称或者计算机名称
     * @param workPath
     *            共享目录
     * @param mountPoint
     *            挂载点
     * @param account
     *            账号
     * @param password
     *            密码
     * @param isMounted
     *            是否已经挂载 0 没有挂载 1 已经挂载
     * @return ContentValues对象
     */
    private void setValues(String ip, String nichName, String workPath,
            String mountPoint, String account, String password, int isMounted) {
        Log.d(TAG, "2924::setValues===");

        ContentValues values = new ContentValues();
        values.put(NICK_NAME, nichName);
        values.put(SERVER_IP, ip);
        values.put(WORK_PATH, workPath);
        values.put(ACCOUNT, account);
        values.put(PASSWORD, password);
        // values.put(DIR_HAS_MOUNTED, isMounted);
        sqlite.insert(TABLE_NAME, ID, values);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(IMAGE, R.drawable.folder_file);
        String nickName = "\\\\" + nichName + "\\"
                + workPath.replace("/", "\\");
        Log.d("============1081", "SHOWNAME = " + nickName);
        map.put(NICK_NAME, nickName);
        map.put(SHORT, 1);
        map.put(MOUNT_POINT, mountPoint);
        map.put(SERVER_IP, ip);
        map.put(ACCOUNT, account);
        map.put(PASSWORD, password);
        map.put(WORK_PATH, workPath);
        list.add(map);
    }

    /**
     * 批量删除
     *
     * @param dialog
     * @param which
     */
    private void doDeleteSeletedServers(DialogInterface dialog, int which,
            List<Map<String, Object>> mainList) {
        Log
                .d(TAG,
                        "2772::==========  doDeleteSeletedServers()  =================");
        Log.d(TAG, "2969::mainList=" + mainList);
        // 获取对话框的列表组件
        ListView lvServerList = (ListView) ((AlertDialog) dialog)
                .findViewById(R.id.lvSambaServer);
        lvServerList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvServerList.setItemsCanFocus(true);
        lvServerList.setClickable(true);
        // 保存删除的服务器名字
        List<String> deletedServerNames = new ArrayList<String>(1);
        int count = lvServerList.getCount();
        Log.d(TAG, "2978::doDeleteSeletedServers()_count=" + count);
        int flag = 0;// 默认为删除成功
        boolean foo = true;// 总体删除成功
        String failedName = null;
        SambaManager samba = null;// = (SambaManager) getSystemService("Samba");

        // 循环批量删除被勾选的服务器，执行在后台
        for (int i = 0; i < count; i++) {
            Log.d(TAG, "2985::lvServerList_getCheckedItemPositions="
                    + lvServerList.getCheckedItemPositions());
            // 对勾选中的选项进行相应的删除操作
            if (lvServerList.getCheckedItemPositions().get(i)) {
                // 若包含NET_SHARE_MOUNT_POS键值，说明是本地新建服务
                String svrName = mainList.get(i + 1).get(NICK_NAME).toString();
                Log.d(TAG, "2992::SERVER_NAME=" + svrName);

                int index = svrName.indexOf("\\", 2);

                String nickname = svrName.substring(2, index);
                String workpath = svrName
                        .substring(index + 1, svrName.length());
                cursor = sqlite.query(TABLE_NAME,
                        new String[] { ID, SERVER_IP }, NICK_NAME + "=? and "
                                + WORK_PATH + "=?", new String[] { nickname,
                                workpath }, null, null, null);
                // 根据从数据库获取到得多个目录挂载点，从服务器umount挂载
                String serverip = null;
                int id = 0;
                while (cursor.moveToNext()) {
                    serverip = cursor.getString(cursor
                            .getColumnIndex(SERVER_IP));
                    id = cursor.getInt(cursor.getColumnIndex(ID));
                    // if (!" ".equals(mountPoint) && null != mountPoint) {

                    StringBuilder builder = new StringBuilder(serverip);
                    builder.append("/").append(workpath);
                    // 获得本地挂载路径
                    Log.i("========================3503", builder.toString());
                    String returnStr = jni.getMountList(builder.toString());
                    if (!returnStr.equals("ERROR")) {
                        samba = (SambaManager) getSystemService("Samba");
                        samba.start("", "", "", "");
                        flag = jni.myUmount(returnStr);
                    }
                    // }

                    Log.d(TAG, "3008::doDeleteSelectedServers()_flag=" + flag);
                    if (flag != 0) {// 删除失败
                        failedName = mainList.get(i + 1).get(NICK_NAME)
                                .toString();
                        foo = false;
                        break;
                    }
                    Log.d(TAG, "3014::selected_name="
                            + mainList.get(i).get(NICK_NAME).toString());
                }
                // umount成功后,从数据库中删除所有与此服务器有关的共享目录
                if (flag == 0) {
                    deletedServerNames.add(svrName);// umount成功后，在删除名列表中添加删除的名字
                    int deleteNums = sqlite.delete(TABLE_NAME, ID + "=?",
                            new String[] { String.valueOf(id) });
                    Log.d(TAG, "3022::deleted_name_numbers=" + deleteNums);
                }
                cursor.close();
                // 在umount删除成功后，从数据库走红删除
                if (flag != 0) {
                    FileUtil.showToast(SambaActivity.this, getString(
                            R.string.pos_delete_failed, failedName));
                }
            }
        }// end for
        // 在界面上进行删除操作
        for (int j = 0; j < deletedServerNames.size(); j++) {
            Log.d(TAG, "3033::delete_name=" + deletedServerNames.get(j));
            for (int k = 1; k < mainList.size(); k++) {
                Log.d(TAG, "3035::deleted_name_in_list="
                        + mainList.get(k).get(NICK_NAME).toString());
                // 删除的名字与在服务器列表中的名字相等
                if (deletedServerNames.get(j).equals(
                        mainList.get(k).get(NICK_NAME).toString())) {
                    mainList.remove(k);
                    break;
                }
            }
        }
        // 如果全部删除成功
        if (foo) {
            FileUtil
                    .showToast(SambaActivity.this, getString(R.string.delete_v));
        }
        // gridView.setVisibility(View.INVISIBLE);
        servers();
    }

    /**
     * 解析根据group名获取到得具体的共享目录信息
     *
     * @param detailGroup
     * @return
     */
    private List<Map<String, Object>> parse2DetailDirectories(String detailGroup) {
        Log.d(TAG, "2853::detailGroup=" + detailGroup);
        List<Map<String, Object>> detailInfos = new ArrayList<Map<String, Object>>(
                1);
        String[] details = detailGroup.split("\\|");
        HashMap<String, Object> map;
        // ip地址
        String[] ipDetail = details[0].split(":");

        for (int i = 1; i < details.length; i++) {
            map = new HashMap<String, Object>(1);
            // 放入ip地址
            map.put(SERVER_IP, ipDetail[1]);
            // 分割共享目录信息
            String[] dirDetails = details[i].split(":");
            // 目录名称
            String dir = dirDetails[0].trim();
            // 截取共享目录名称
            String dirName = dir.substring(dir.lastIndexOf("\\") + 1, dir
                    .length());
            map.put(WORK_PATH, dirName);
            // 挂载到得本地目录位置
            map.put(MOUNT_POINT, "");
            Log.d(TAG, "2893::map=" + map);
            detailInfos.add(map);
        }
        Log.d(TAG, "2896::detailInfos=" + detailInfos);
        return detailInfos;
    }

    /**
     * 显示新建挂载点目录对话框，在点击服务器目录后， 需要新建新建挂载点进行挂载
     *
     * @param clickedServerItem
     *            被点击的挂载目录
     */
    private void showSetNewMountDlg(Map<String, Object> clickedNetServer,
            Map<String, Object> clickedServerItem, int clickPosition) {
        this.clickedNetServer = clickedNetServer;
        clickedServerDirItem = clickedServerItem;
        Log.d(TAG, "2893::showSetNewMountDlg_clickedNetServer="
                + clickedNetServer);
        Log.d(TAG, "2894::showSetNewMountDlg_clickedServerItem="
                + clickedServerItem);
        dialog = new MyDialog(this, R.layout.new_server);
        dialog.show();

        // 服务器名称
        editServer = (EditText) dialog.findViewById(R.id.editServer);
        editServer.setText(clickedServerItem.get(SERVER_IP).toString());
        editServer.setFocusable(false);
        editServer.setClickable(false);
        // editServer.setFocusable(false);// 不可获得焦点
        Log.d(TAG, "2924::serverUrl="
                + clickedServerItem.get(WORK_PATH).toString());

        // 账号
        editName = (EditText) dialog.findViewById(R.id.editName);
        // editName.setText(clickedNetServer.get(ACCOUNT).toString());
        // 密码
        editpass = (EditText) dialog.findViewById(R.id.editpass);
        // editpass.setText(clickedNetServer.get(PASSWORD).toString());
        // 昵称
        editdisplay = (EditText) dialog.findViewById(R.id.editdisplay);
        netCheck = (CheckBox) dialog.findViewById(R.id.add_shortcut);
        // 显示昵称
        editdisplay.setText(clickedServerItem.get(NICK_NAME).toString());
        editdisplay.setFocusable(false);// 昵称不可获得焦点
        // 将点击的服务器对象置空

        // 工作目录
        position = (EditText) dialog.findViewById(R.id.position);
        position.setText(clickedServerItem.get(WORK_PATH).toString());
        position.setFocusable(false);
        position.setClickable(false);
        // position.setFocusable(false);

        // begin add by qian_wei/cao_shanshan 2011/10/22
        // for while the net is error ,show the login dialog
        if(hasTheShortCut()) {
            netCheck.setChecked(true);
            netCheck.setVisibility(View.GONE);
        } else {
            netCheck.setVisibility(View.VISIBLE);
        }
        // end add by qian_wei/cao_shanshan 2011/10/22

        // 确定按钮
        myOkBut = (Button) dialog.findViewById(R.id.myOkBut);
        // 取消按钮
        myCancelBut = (Button) dialog.findViewById(R.id.myCancelBut);

        myOkBut.setOnClickListener(new ButtonSetOnNewShareDirListener(
                SambaActivity.this, dialog, clickedServerItem,
                clickedNetServer, clickPosition));
        myCancelBut.setOnClickListener(new ButtonSetOnNewShareDirListener(
                SambaActivity.this, dialog, clickedServerItem,
                clickedNetServer, clickPosition));
    }

    /**
     * 点击共享目录后，新建共享挂载
     *
     * @author ni_guanhua
     */
    class ButtonSetOnNewShareDirListener implements View.OnClickListener {
        // Context context;

        Dialog newShareDialog;

        // 点击的共享目录项目
        Map<String, Object> clickedServerDirItem;

        // 点击的服务器
//		Map<String, Object> clickedNetServer1;

        int clickPosition;

        public ButtonSetOnNewShareDirListener(Context context,
                Dialog newShareDialog, Map<String, Object> clickedServerItem,
                Map<String, Object> clickedNetServer, int clickPosition) {
            // this.context = context;
            this.newShareDialog = newShareDialog;
            this.clickedServerDirItem = clickedServerItem;
//			this.clickedNetServer1 = clickedNetServer;
            this.clickPosition = clickPosition;
        }

        public void onClick(View v) {
//			Log.d(TAG, "3003::clickedNetServer=" + clickedNetServer1);

            // 确定按钮
            if (v.getId() == R.id.myOkBut) {
                Userserver = editServer.getText().toString();
                Log.d("!!!!!SambaActivity[ButtonSetOnNewShareDirListener]",
                        "2877::mount_userServer=" + Userserver);
                Username = editName.getText().toString();
                Log.d("!!!!!SambaActivity[ButtonSetOnNewShareDirListener]",
                        "2877::mount_userName=" + Username);
                Userpass = editpass.getText().toString();
                Log.d("!!!!!SambaActivity[ButtonSetOnNewShareDirListener]",
                        "2877::mount_userPass=" + Userpass);
                // 昵称
                display = editdisplay.getText().toString();
                Log.d("!!!!!SambaActivity[ButtonSetOnNewShareDirListener]",
                        "2877::mount_nickName=" + display);

                folder_position = position.getText().toString().toUpperCase();
                Log.d(TAG, "3027::mount_piosition=" + folder_position);

                // 判断内容是否为空
                if (null == Userserver || Userserver.trim().equals("")) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.input_server));
                    return;
                }
                // if (null == Username || Username.trim().equals("")) {
                // FileUtil.showToast(SambaActivity.this,
                // getString(R.string.input_name_pass));
                // return;
                // }
                // if (null == Userpass || Userpass.trim().equals("")) {
                // FileUtil.showToast(SambaActivity.this,
                // getString(R.string.input_name_pass));
                // return;
                // }
                if (null == folder_position || "".equals(folder_position)) {
                    FileUtil.showToast(SambaActivity.this,
                            getString(R.string.work_path_null));
                    return;
                }
                // begin modify by qian_wei/zhou_yong 2011/10/28
                // for extend the mount time
//                SambaManager samba = (SambaManager) getSystemService("Samba");
//                samba.start("", "", "", "");
//                // Jni jni = new Jni();
//                result = jni.UImount(Userserver, folder_position, " ",
//                        Username, Userpass);
//                if (result == 0) {
//                    // workFlag = true;
//                    // 在成功挂载后对点击的服务器置空，
//                    // 以便在点击共享或进入到下一级目录
//                    clickedNetServer1 = null;
//                    // 全局置空
//                    SambaActivity.this.clickedNetServer = null;
//
//                    // 在挂载工程后，对点击共享目录的值做一定的变化
//                    // 修改是否已设置挂载，1标识已经设置挂载
//                    // clickedServerDirItem.put(DIR_HAS_MOUNTED, (byte) 1);
//                    Log.d("!!!!SambaActivity[ButtonSetOnNewShareDirListener]",
//                            "3079::clickedServerDirItem="
//                                    + clickedServerDirItem);
//
//                    StringBuilder builder = new StringBuilder(Userserver);
//                    builder.append("/").append(folder_position);
//                    // 获得本地挂载路径
//                    Log.i("========================3503", builder.toString());
//                    String returnStr = jni.getMountList(builder.toString());
//                    if (returnStr.equals("ERROR")) {
//                        FileUtil.showToast(SambaActivity.this,
//                                getString(R.string.user_or_pass));
//                    } else {
//                        localPath = returnStr;
//
//                        if (netCheck.isChecked()) {
//                            cursor = sqlite.query(TABLE_NAME,
//                                    new String[] { ID }, SERVER_IP + " =? and "
//                                            + WORK_PATH + " = ?", new String[] {
//                                            Userserver, folder_position },
//                                    null, null, null);
//                            if (cursor.moveToFirst()) {
//                                int id = cursor.getInt(cursor
//                                        .getColumnIndex(ID));
//                                ContentValues values = new ContentValues();
//                                // values.put(MOUNT_POINT, localPath);
//                                values.put(ACCOUNT, Username);
//                                values.put(PASSWORD, Userpass);
//                                sqlite.update(TABLE_NAME, values, ID + "=?",
//                                        new String[] { String.valueOf(id) });
//                            } else {
//                                setValues(Userserver, display, folder_position
//                                        .replace("/", "\\"), localPath,
//                                        Username, Userpass, 1);
//
//                            }
//                            cursor.close();
//                        }
//
//                        // 修改挂载目录位置
//                        clickedServerDirItem.put(MOUNT_POINT, localPath);
//                        FileUtil.showToast(SambaActivity.this,
//                                getString(R.string.new_mout_successfully));
//                        // 获取本地映射挂载目录中的内容
//                        getDirectory(localPath);
//                        if (getFileFlag(currentFileString, prePath) == 1) {
//                            Log.e("ADD2773", "POSITION");
//                            intList.add(clickPosition);
//                        }
//                    }
//                }
//                /*
//                 * @tag :begin modified by ni_guanhua 2011/7/4 for 2286
//                 *
//                 * @brief :在输入密码，或者账号错误时，给出相应提示
//                 */
//                else if (result == USER_OR_PASS_ERROR) {
//                    FileUtil.showToast(SambaActivity.this,
//                            getString(R.string.user_or_pass));
//
//                // begin modify by qian_wei/yang_haibing 2011/10/14
//                // for determine the mount result value when the network is disconnected
////              } else if (result == -7) {
////                  FileUtil.showToast(SambaActivity.this,
////                  getString(R.string.mount_exist));
//                } else if (result == NET_ERROR) {
//                    FileUtil.showToast(SambaActivity.this,
//                              getString(R.string.network_error));
//                // end modify by qian_wei/yang_haibing 2011/10/14
//                }
//                // end modified
//                else {
//                    FileUtil.showToast(SambaActivity.this,
//                            getString(R.string.new_server_error));
//                }
//                // 不论挂载成功或挂载失败，对话框关闭
//                newShareDialog.dismiss();
//                // }
                progress = new ProgressDialog(SambaActivity.this);
                progress.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                        return true;
                    }
                });
                progress.show();
                MountThread thread = new MountThread(MOUNT_RESULT_4);
                thread.start();
                // end modify by qian_wei/zhou_yong 2011/10/28
                return;
            }
            // 取消按钮
            if (v.getId() == R.id.myCancelBut) {
                Log.w("========2870", "CANCLE");
                newShareDialog.dismiss();
                return;
            }

        }
    }

    public ListView getListView() {
        return listView;
    }

    /**
     * @tag : begin modify by qian_wei 2011/7/4 for 2365
     * @brief : 在samba中，选择并打开一个文件查看完点击返回， 返回到服务器列表页面，再次点击进入该服务器，页面强制退出
     */
    protected void onDestroy() {
        if (cursor != null) {
            cursor.close();
            sqlite.close();
        }
        super.onDestroy();
    }

    /**
     * 将一个xml文件对应的生成一个View
     *
     * @param context
     * @param resId
     * @return
     */
    private View inflateLayout(Context context, int resId) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resId, null);
        return view;
    }

    /**
     * 将Map强转为HashMap
     *
     * @param listMap
     * @return
     */
    private List<HashMap<String, Object>> convertMapToHashMap(
            List<Map<String, Object>> listMap) {
        List<HashMap<String, Object>> lst = new ArrayList<HashMap<String, Object>>(
                1);
        Log.d(TAG, "3126::convertMapToHashMap()_listMap.size=" + listMap);
        HashMap<String, Object> map = null;
        for (int i = 1; i < listMap.size(); i++) {
            map = (HashMap<String, Object>) listMap.get(i);
            lst.add(map);
        }
        return lst;
    }

    /**
     * 高亮显示添加后的最后一行
     *
     * @param lvList
     * @param dataSize
     */
    private void highlightLastItem(ListView lvList, int dataSize) {
        lvList.smoothScrollToPosition(dataSize - 1);
        lvList.setSelection(dataSize - 1);
    }

    /**
     * 判断是否成功进入
     *
     * @param currentPath
     * @param prePath
     * @return
     */
    public int getFileFlag(String currentPath, String prePath) {
        // 进入子目录
        if (currentPath.length() > prePath.length()) {
            prePath = currentPath;
            return 1;
        }
        // 返回父目录
        else if (currentPath.length() < prePath.length()) {
            prePath = currentPath;
            return -1;
        }
        // 操作不成功
        else {
            return 0;
        }
    }

    /**
     * 添加快捷方式
     */
    private void addShortCut() {

        Log.i("==============SERVERNAME", " = " + serverName);
        Log.i("==============folder_position", " = " + folder_position);
        cursor = sqlite.query(TABLE_NAME, new String[] { ID }, NICK_NAME
                + "=? and " + WORK_PATH + "=?", new String[] { serverName,
                folder_position }, null, null, null);
        // 已存在，但是用户名、密码被改变
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(ID));
            ContentValues values = new ContentValues();
            values.put(ACCOUNT, Username);
            values.put(PASSWORD, Userpass);
            sqlite.update(TABLE_NAME, values, ID + "=?", new String[] { String
                    .valueOf(id) });
        } else {
            setValues(Userserver, serverName, folder_position
                    .replace("/", "\\"), " ", Username, Userpass, 1);
        }
        FileUtil.showToast(this, getString(R.string.add_short_succ));
        cursor.close();
    }

    /**
     * 判断快捷方式是否存在
     *
     * @return 存在标识
     */
    private boolean hasTheShortCut() {
        String nick = "\\\\" + serverName + "\\"
                + folder_position.replace("/", "\\");
        Log.w("TAG", nick);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).containsValue(nick)) {
                return true;
            }
        }
        return false;
    }

    // begin modify by qian_wei/xiong_cuifan 2011/11/05
    // for broken into the directory contains many files,click again error
    /**
     * @author liu_tianbao 获得排序后的文件列表
     */
    class MyThread extends MyThreadBase {

        public void run() {
            if (getFlag()) {
                setFlag(false);
                synchronized(lock){
                    util = new FileUtil(SambaActivity.this, filterCount, arrayDir,
                        arrayFile, currentFileString);
                }
            } else {
                Log.i(TAG, "isSearch = false");
                util = new FileUtil(SambaActivity.this, filterCount,
                        currentFileString);
            }

            listFile = util.getFiles(sortCount, "net");

            //BEGIN : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
            // begin modify by yuejun 2011/12/16
//			List<File> temp1ListFile = new ArrayList<File>();
//			List<File> temp2ListFile = new ArrayList<File>();
//			Log.e(TAG,"==== zhl ==== currentFileString="+currentFileString);
//			if(currentFileString.toLowerCase().contains("bdmv"))
//			{
//				temp1ListFile.add(getMaxFile(listFile));
//				listFile=temp1ListFile;
//			}
//			else if(currentFileString.toLowerCase().contains("video_ts"))
//			{
//				for(int i=0; i<listFile.size(); i++)
//				{
//					if(listFile.get(i).toString().substring(listFile.get(i).toString().lastIndexOf(".")).equalsIgnoreCase(".vob"))
//						temp1ListFile.add(listFile.get(i));
//				}
//
//				for(int j=0; j<temp1ListFile.size(); j++)
//				{
//					if(temp1ListFile.get(j).length() >= (long)100*1024*1024)
//						temp2ListFile.add(temp1ListFile.get(j));
//				}
//				listFile = temp2ListFile;
//			}
//			// end modify by yuejun 2011/12/16
            //END : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来

            Log.v("\33[32m UTIL","getRunFlag()" + getStopRun() + "\33[0m");
            if(getStopRun()) {
                if(keyBack) {
                    if(pathTxt.getText().toString().substring(serverName.length()).equals(ISO_PATH)) {
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
                Log.v("\33[32m SMB"," listFile.size(1) " + listFile.size() + "\33[0m");
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

    public String getServerName() {
        return serverName;
    }


    // begin add by qian_wei/zhou_yong 2011/10/28
    // for extend the mount timeout to 30s
    /**
     * 执行挂载操作
     * @author qian_wei
     */
    class MountThread extends Thread{

        private int mountFlag = 0;
        public MountThread(int mountFlag) {
            this.mountFlag = mountFlag;
        }

        public void run() {
            SambaManager samba = null;
            switch(mountFlag) {
                case MOUNT_RESULT_1:
                case MOUNT_RESULT_3:
                case MOUNT_RESULT_4:
                    samba = (SambaManager) getSystemService("Samba");
                    samba.start("", "", "", "");
                    result = jni.UImount(Userserver, folder_position, " ",
                            Username, Userpass);
                    handler.sendEmptyMessage(mountFlag);
                    break;
                case MOUNT_RESULT_2:
                    builder = new StringBuilder(prevServer);
                    builder.append("/").append(prevFolder);
                    String returnStr = jni.getMountList(builder.toString());
                    if (returnStr.equals("ERROR")) {
                        samba = (SambaManager) getSystemService("Samba");
                        samba.start("", "", "", "");
                        result = jni.UImount(Userserver, folder_position, " ",
                                Username, Userpass);
                    } else {
                        samba = (SambaManager) getSystemService("Samba");
                        samba.start("", "", "", "");
                        int code = jni.myUmount(returnStr);
                        Log.w("CODE", " = " + code);
                        samba = (SambaManager) getSystemService("Samba");
                        samba.start("", "", "", "");
                        result = jni.UImount(Userserver, folder_position, " ",
                                    Username, Userpass);
                    }
                    handler.sendEmptyMessage(MOUNT_RESULT_2);
                    break;
            }
        }
    }
    // end add by qian_wei/zhou_yong 2011/10/28

    // begin add by qian_wei/xiong_cuifan 2011/11/07
    // for grally3D delete the file, flush the data
    protected void onResume() {
        super.onResume();
        if(!currentFileString.equals("") && preCurrentPath.equals(currentFileString)) {
            updateList(true);
        }
    }
    // end add by qian_wei/xiong_cuifan 2011/11/08
}
