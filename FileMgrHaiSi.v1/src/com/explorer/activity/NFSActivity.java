package com.explorer.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.explorer.common.NfsSvrAdapter;
import com.explorer.common.SocketClient;
import com.explorer.jni.Mountinfo;
import com.explorer.jni.NfsClient;

import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.IBinder;

/**
 * 浏览NFS网络文件
 *
 * @author ni_guanhua
 */
public class NFSActivity extends CommonActivity {

    static final String TAG = " NFSActivity ";

	// 挂载失败
	private static final int MOUNT_FAILD = 10;
	private static final int  NFSRESULT_GETSELFIPERROR = 3;

    // 挂载成功
    private static final int MOUNT_SUCCESS = 11;

    // 空字符串
    static final String BLANK = "";

    // ip地址正则表达式
    static final String IP_REGEX = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])"
            + "\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])"
            + "\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])"
            + "\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";

    // 服务器路径路径
    static final String MOUNT_ROOT_PATH = "/mnt";

    // native方法调用对象
    NfsClient nNfsClient;

    // 服务器IP
    EditText edtIpAddress;

    // 服务器共享目录
    EditText edtServerFolder;

    // 新增挂载按钮
    Button btnAddAuto;

    // 是否自动挂载复选框
    CheckBox checkAuto;

    // NFS挂载列表
    NfsSvrAdapter adpt;

    // 删除对话框中列表组件
    ListView dlgLstView;

    // NFS挂载对象集合
    List<Mountinfo> postFormatedMountinfos;

    // 对话框构造器
    AlertDialog.Builder builder;

    // 对话框对象
    AlertDialog dlg;

    // 设置自动挂载
    protected final static int MENU_SET_AUTO = Menu.FIRST + 16;

    // 删除挂载
    protected final static int MENU_DELETE_MOUNT = Menu.FIRST + 17;

    // 父目录路径
    private String parentPath = "";

    // 操作文件列表
    private List<File> fileArray = null;

    // 挂载IP地址
    private String pSvrIP = "";

    // 文件列表集合
    //    private List<File> li = null;

    // 文件列表的点击位置
    private int myPosition = 0;

    // 输入框字符串起始位置
    int Num = 0;

    // 输入框字符串长度
    int tempLength = 0;

    // 选中的文件列表
    List<String> selectList = null;

    // 对话框列表数据
    ListView list;

    // 挂载列表
    List<Map<String, Object>> sdlist;

    // 点击次数
    int clickCount = 0;

    // 显示文件列表
//    private List<File> listFile;

    // 删除挂载、取消自动挂载列表对话框
    AlertDialog dialog;

    // 新增挂在对话框
    Dialog nfsAddDlg;

    // 存放点击位置集合
    List<Integer> intList;

    // 搜索后的文件集合
    List<File> fileL = null;

    // 文件适配器对象
//    FileAdapter adapter;

    // 菜单操作标识
    int menu_item = 0;

    // 显示数据信息
    private TextView numInfo;

    // 挂载成功路径
    private String baseStr = "/mnt/nfsclt";

    // 挂载ISO所在目录
//    private String prevPath = "";

    // 父目录点击位置
    private int parentPosition;

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
        nNfsClient = new NfsClient();
        init();
        selectList = new ArrayList<String>();
    }

    // 等待对话框
    ProgressDialog nPgrDlgWaiting;

    /**
     * 初始化控件
     */
    private void init() {

        // 使树形不可见
        ExpandableListView expandableList = (ExpandableListView) findViewById(R.id.expandlistView);
        expandableList.setVisibility(View.GONE);

        showBut = (ImageButton) findViewById(R.id.showBut);

        sortBut = (ImageButton) findViewById(R.id.sortBut);

        filterBut = (ImageButton) findViewById(R.id.filterBut);

        intList = new ArrayList<Integer>();

        listFile = new ArrayList<File>();
        fileL = new ArrayList<File>();
        gridlayout = R.layout.gridfile_row;
        listlayout = R.layout.file_row;
        listView = (ListView) findViewById(R.id.listView);
        listView.setVisibility(ListView.VISIBLE);
        gridView = (GridView) findViewById(R.id.gridView);
        // 提示路径
        pathTxt = (TextView) findViewById(R.id.pathTxt);

        numInfo = (TextView) findViewById(R.id.ptxt);

        postFormatedMountinfos = new ArrayList<Mountinfo>();
        // 新增挂在对话框
        nfsAddDlg = new MyDialog(this, R.layout.nfs_add);

        showWaitingDlg();

        // 开启进程获取列表
        new Thread(new Runnable() {
            public void run() {
                // 获得挂在列表字符串
                String mountedList = nNfsClient.getMountedList();

                Log.d(TAG, "397::init()_mountedList=" + mountedList);
                if (mountedList.length() > 0) {
                    List<Mountinfo> fmtMountList = wrap2Mountinfo(mountedList);
                    Log.i(TAG, "400::[init]fmtMountList.size="
                            + fmtMountList.size());
                    for (int i = 0; i < fmtMountList.size(); i++) {
                        Log.i(TAG, "402::[init]isAuto="
                                + fmtMountList.get(i).getUcIsMounted());
                        Log.i(TAG, "403::[init]client path="
                                + fmtMountList.get(i).getSzCltFold());
                        // begin modify by qian_wei/zhou_yong 2011/10/26
                        // for determine whether the changes need to mount
//                        if (fmtMountList.get(i).getUcIsAuto() == 1) {
                        if (fmtMountList.get(i).getUcIsMounted() == 0) {
                        // end modify by qian_wei/zhou_yong 2011/10/26
                            int flag = nNfsClient.mountNFSSvr(fmtMountList.get(
                                    i).getSzSvrIP(), fmtMountList.get(i)
                                    .getSzSvrFold(), fmtMountList.get(i)
                                    .getSzCltFold(), 1);
                            Log.w("TAG", " FLAG= " + flag);
                            if (flag == 0) {
                                fmtMountList.get(i).setUcIsMounted(1);
                            }
                        }
                        if (i == fmtMountList.size() - 1) {
                            postFormatedMountinfos.clear();
                            postFormatedMountinfos.addAll(fmtMountList);
                            handler.sendEmptyMessage(MOUNT_DATA);
                            return;
                        }
                    }
                    Log.i(TAG, "419::[run]postFormatedMountinfos="
                            + postFormatedMountinfos.size());
                } else {
                    handler.sendEmptyMessage(MOUNT_DATA);
                }
            }

        }).start();

        isNetworkFile = true;
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MOUNT_DATA:
                if (nPgrDlgWaiting.isShowing()) {
                    nPgrDlgWaiting.dismiss();
                }
                setServerAdapter();
                break;
            case SEARCH_RESULT:
                if (progress != null && progress.isShowing()) {
                    progress.cancel();
                }

                synchronized(lock){
                    // 无搜索结果
                    if (arrayFile.size() == 0 && arrayDir.size() == 0) {
                        FileUtil.showToast(NFSActivity.this,
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
                Log.w("TAG", " = " + listFile.size());
                if (listView.getVisibility() == View.VISIBLE) {
                    adapter = new FileAdapter(NFSActivity.this, listFile,
                            listlayout);
                    listView.setAdapter(adapter);
                    listView.setOnItemSelectedListener(itemSelect);
                    listView.setOnItemClickListener(ItemClickListener);
                } else if (gridView.getVisibility() == View.VISIBLE) {
                    adapter = new FileAdapter(NFSActivity.this, listFile,
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
                //BEN  : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
                getFiles(OpenFilePath);
                break;
            // end modify by yuejun 2011/12/14
            case ADD_MOUNT: // 新增挂载
			{
				int nMountResult = msg.arg1;
				closeWaitingDlg();
				if (nMountResult == 0) {
					Mountinfo info = new Mountinfo();
					info.setSzSvrIP(pSvrIP);
					info.setSzSvrFold(pSvrFold);
					info.setSzCltFold(getCltFolder(pSvrIP, pSvrFold));
					info.setUcIsAuto(ucIsAuto);
					info.setPcName(pSvrIP);
					// begin modify by qian_wei/cao_shanshan 2011/10/25
					// for modify the return result
//					info.setUcIsMounted(0);
					info.setUcIsMounted(1);
					// end modify by qian_wei/cao_shanshan 2011/10/25
					postFormatedMountinfos.add(info);
					FileUtil.showToast(NFSActivity.this,
							getString(R.string.new_mout_successfully));
					refreshView();// 更新显示列表
				// begin modify by qian_wei/cao_shanshan 2011/10/25
				// for add the mount self can not allow	
//				} else {
//                    FileUtil.showToast(NFSActivity.this,
//                            getString(R.string.mount_self));
				} else if (nMountResult == 1) {
					FileUtil.showToast(NFSActivity.this,
							getString(R.string.new_mout_fail));
				} else if (NFSRESULT_GETSELFIPERROR == nMountResult)
				{
					FileUtil.showToast(NFSActivity.this,
							getString(R.string.network_nfsmount_getiperror));
				}
				else {
				    FileUtil.showToast(NFSActivity.this,
				            getString(R.string.mount_self));
				}
				// end modify by qian_wei/cao_shanshan 2011/10/25
				edtIpAddress.setText("");
				edtServerFolder.setText("");
				checkAuto.setChecked(false);
				edtIpAddress.requestFocus();
				break;
			}
			case DEL_MOUNT: // 卸载挂载
				closeWaitingDlg();
				// 删除失败
				if (delFlag == 1) {
					FileUtil.showToast(NFSActivity.this,
							getString(R.string.delete_error));
				}
				// 删除成功
				if (delFlag == 0) {
					FileUtil.showToast(NFSActivity.this,
							getString(R.string.delete_v));
				}
				Log.d(TAG, "569::postFormatedMountinfos="
						+ postFormatedMountinfos);
				refreshView();
				break;
			case ISO_MOUNT_FAILD: // 挂载ISO失败
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
				}
				FileUtil.showToast(NFSActivity.this,
						getString(R.string.new_mout_fail));
				break;
			case MOUNT_FAILD:
			{
				int nMountResult = msg.arg1;
				String strTipString = getString(R.string.new_mout_fail);
				if (progress != null && progress.isShowing()) {
					progress.cancel();
				}
				if (NFSRESULT_GETSELFIPERROR == nMountResult)
				{
					strTipString = getString(R.string.network_nfsmount_getiperror);
				}
				FileUtil.showToast(NFSActivity.this,strTipString);
				break;
			}
			case MOUNT_SUCCESS:
				if (progress != null && progress.isShowing()) {
					progress.cancel();
				}
				getFiles(baseStr);
				break;
			}

        }
    };

    /**
     * 显示等待对话框
     */
    void showWaitingDlg() {
        nPgrDlgWaiting = new ProgressDialog(NFSActivity.this);
        nPgrDlgWaiting.setMessage(getString(R.string.please_waitting));
        nPgrDlgWaiting.show();
    }

    /**
     * 设置参数
     */
    void setServerAdapter() {
        Log.i(TAG, "457::[setServerAdapter]postFormatedMountinfos.size="
                + postFormatedMountinfos.size());
        adpt = new NfsSvrAdapter(this, postFormatedMountinfos);
        listView.setAdapter(adpt);
        listView.setOnItemClickListener(ItemClickListener);
    }

    /**
     * 初始化对话框组件资源
     */
    private void initDialogWidget() {
        btnAddAuto = (Button) nfsAddDlg.findViewById(R.id.addAutoBut);
        btnAddAuto.setOnClickListener(new ClickListener());// 注册监听
        edtIpAddress = (EditText) nfsAddDlg.findViewById(R.id.ipAddress1);
        edtIpAddress.requestFocus();// 在显示的时候是服务器地址编辑框获得焦点
        edtServerFolder = (EditText) nfsAddDlg.findViewById(R.id.serverFolder1);
        checkAuto = (CheckBox) nfsAddDlg.findViewById(R.id.checkAuto);

        edtIpAddress.setText(BLANK);
        edtServerFolder.setText(MOUNT_ROOT_PATH);
        checkAuto.setChecked(true);
        edtIpAddress.requestFocus();
    }

    // 子菜单
    SubMenu operatFile;

    /**
     * 创建菜单
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // 显示、隐藏标签
        menu.add(2, MENU_TAB, 1, getString(R.string.hide_tab));

        // 新增挂载
        menu.add(1, ADD_NFS_MOUNT, 2, R.string.add_auto);

        // 操作文件
        operatFile = menu.addSubMenu(2, 20, 3, R.string.operation);

        // 新建、搜索、帮助
        menu.add(2, MENU_ADD, 4, R.string.str_new);
        Log.v(TAG, "455::[onCreateOptionsMenu]menu index="
                + menu.getItem(3).getItemId());
        menu.add(2, MENU_SEARCH, 5, R.string.search);

        // 添加过滤类型
        SubMenu addFilter = menu.addSubMenu(Menu.NONE + 2, Menu.NONE, 6,
                getString(R.string.add_filter));
        addFilter.add(Menu.NONE + 2, ADD_MENU_AUDIO, 0,
                getString(R.string.music));
        addFilter.add(Menu.NONE + 2, ADD_MENU_VIDEO, 0,
                getString(R.string.video));
        addFilter.add(Menu.NONE + 2, ADD_MENU_IMAGE, 0,
                getString(R.string.image));

        // 删除过滤类型
        SubMenu removeFilter = menu.addSubMenu(Menu.NONE + 2, Menu.NONE, 7,
                getString(R.string.remove_filter));
        removeFilter.add(Menu.NONE + 2, REMOVE_MENU_AUDIO, 0,
                getString(R.string.music));
        removeFilter.add(Menu.NONE + 2, REMOVE_MENU_VIDEO, 0,
                getString(R.string.video));
        removeFilter.add(Menu.NONE + 2, REMOVE_MENU_IMAGE, 0,
                getString(R.string.image));

        menu.add(2, MENU_HELP, 8, R.string.help);
        Log.v(TAG, "457::[onCreateOptionsMenu]menu index="
                + menu.getItem(4).getItemId());
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
        if (!pathTxt.getText().toString().equals(BLANK)) {
            menu.getItem(6).setEnabled(true);
            menu.getItem(5).setEnabled(true);
            menu.getItem(1).setEnabled(false);
            operatFile.clear();
            operatFile.add(Menu.NONE, MENU_COPY, 0, getString(R.string.copy));
            operatFile.add(Menu.NONE, MENU_CUT, 0, getString(R.string.cut));
            operatFile.add(Menu.NONE, MENU_PASTE, 0, getString(R.string.paste));
            operatFile.add(Menu.NONE, MENU_DELETE, 0,
                    getString(R.string.delete));
            operatFile.add(Menu.NONE, MENU_RENAME, 0,
                    getString(R.string.str_rename));
            // 文件列表为空
            if (listFile.size() == 0) {
                menu.getItem(2).getSubMenu().getItem(0).setEnabled(false);
                menu.getItem(2).getSubMenu().getItem(1).setEnabled(false);
                menu.getItem(2).getSubMenu().getItem(3).setEnabled(false);
                menu.getItem(2).getSubMenu().getItem(4).setEnabled(false);
                // menu.getItem(3).getSubMenu().getItem(5).setEnabled(false);
                if (num == 0) {
                    menu.getItem(2).setEnabled(false);
                } else {
                    menu.getItem(2).setEnabled(true);
                    menu.getItem(2).getSubMenu().getItem(2).setEnabled(true);
                }
                menu.getItem(3).setEnabled(true);
                menu.getItem(4).setEnabled(false);
            }
            // 文件列表不为空
            else {
                menu.getItem(2).setEnabled(true);
                menu.getItem(2).getSubMenu().getItem(0).setEnabled(true);
                menu.getItem(2).getSubMenu().getItem(1).setEnabled(true);
                menu.getItem(2).getSubMenu().getItem(3).setEnabled(true);
                menu.getItem(2).getSubMenu().getItem(4).setEnabled(true);

                // 粘贴菜单控制
                if (num == 0) {
                    menu.getItem(2).getSubMenu().getItem(2).setEnabled(false);
                } else {
                    menu.getItem(2).getSubMenu().getItem(2).setEnabled(true);
                }
                menu.getItem(3).setEnabled(true);
                menu.getItem(4).setEnabled(true);
            }
        }
        // 挂载列表界面
        else {
            menu.getItem(6).setEnabled(false);
            menu.getItem(5).setEnabled(false);
            menu.getItem(1).setEnabled(true);
            if (postFormatedMountinfos.size() == 0) {
                menu.getItem(2).setEnabled(false);
            } else {
                menu.getItem(2).setEnabled(true);
            }
            String[] svrOpts = getResources().getStringArray(R.array.nfs_opts);
            operatFile.clear();
            operatFile.add(Menu.NONE, MENU_SET_AUTO, 0, svrOpts[0]);
            operatFile.add(Menu.NONE, MENU_DELETE_MOUNT, 1, svrOpts[1]);
            menu.getItem(3).setEnabled(false);
            menu.getItem(4).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单操作
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        // 新增挂载
        case ADD_NFS_MOUNT:
            nfsAddDlg.show();
            if (nfsAddDlg.isShowing()) {
                initDialogWidget();
            }
            break;
        // 设置自动挂载
        case MENU_SET_AUTO:
            showSetAutoDlg();
            break;

        // 删除挂载
        case MENU_DELETE_MOUNT:
            showUninstallDlg();
            break;

        // 新建文件夹
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
            FileMenu.setHelpFlag(3);
            FileMenu.filterType(NFSActivity.this, MENU_HELP, null);
            break;
        }
        return true;
    }

    /**
     * 文件列表点击事件
     */
    private OnItemClickListener ItemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View convertView,
                int position, long id) {
            if (IsNetworkDisconnect()) {
                return;
            }
            myPosition = position;
            // 路径提示内容
            String path = pathTxt.getText().toString().trim();
            Log.i(TAG, "704::PATH=" + path + " = PATH");
            if (null == path || BLANK.equals(path)) {
                // begin modify by qian_wei/zhou_yong 2011/10/26
                // for if mounted, into the directory, other mount the directory
//                progress = new ProgressDialog(NFSActivity.this);
//              progress.show();
//              MountThread thread = new MountThread();
//              thread.start();
                Mountinfo info = postFormatedMountinfos.get(myPosition);
                if(info.getUcIsMounted() == 1) {
                    intList.add(myPosition);
                    parentPosition = 0;
                    baseStr = info.getSzCltFold();
                    mountSdPath = baseStr;
                    getFiles(baseStr);
                } else {
                    progress = new ProgressDialog(NFSActivity.this);
                    progress.show();
                    MountThread thread = new MountThread();
                    thread.start();
                }
                // modify by qian_wei/zhou_yong 2011/10/26
            } else {
                // 如果是文件夹，记录点击位置
                if(listFile.size() > 0) {
                    if(position >= listFile.size()){
                        position = listFile.size()-1;
                    }
                    File f = listFile.get(position);
                    // begin add by qian_wei/zhou_yong 2011/10/20
                    // for chmod the file
                    //START : change by z00120637 for chmod nfs file cuase error
                    //chmodFile(f.getPath());
                    //END   : change by z00120637 for chmod nfs file cuase error
                    // end modify by qian_wei/zhou_yong 2011/10/20
                    if (f.canRead()) {
                        if(f.isDirectory()) {
                            intList.add(position);
                            parentPosition = 0;
                            fileL.clear();
                        } else {
                            parentPosition = position;
                        }
                        // begin modify by qian_wei/xiong_cuifan 2011/11/05
                        // for broken into the directory contains many files,click again error
                        preCurrentPath = currentFileString;
                        keyBack = false;
                        // end modify by qian_wei/xiong_cuifan 2011/11/08
                        getFiles(f.getPath());
                    } else {
                        FileUtil.showToast(NFSActivity.this,
                                getString(R.string.file_cannot_read));
                    }
                }
            }
        }
    };

    class MountThread extends Thread {

        public void run() {
            Mountinfo info = postFormatedMountinfos.get(myPosition);
            Log.w("INFO", " = "+info.getSzCltFold());

            // begin add by qian_wei/ma_zhongying 2011/10/17
            // for sometimes can not mount the has mounted nfs server
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // end modify by qian_wei/ma_zhongying 2011/10/17

            int mountResult = nNfsClient.mountNFSSvr(info.getSzSvrIP(), info
                    .getSzSvrFold(), info.getSzCltFold(), info.getUcIsAuto());
            if (mountResult == 0) {
                Log.w(TAG, "444 = mount success");
                intList.add(myPosition);
                parentPosition = 0;
                baseStr = info.getSzCltFold();
                mountSdPath = baseStr;
                // begin modify by qian_wei/zhou_yong 2011/10/26
                // for modify the return result
//              info.setUcIsMounted(0);
                info.setUcIsMounted(1);
                // end modify by qian_wei/zhou_yong 2011/10/26
                // begin modify by qian_wei/zhou_yong 2011/10/21
                // for click back key quit application, then into error
//                handler.sendEmptyMessage(MOUNT_SUCCESS);
                if(!NFSActivity.this.isFinishing()) {
                    handler.sendEmptyMessage(MOUNT_SUCCESS);
                }
            } else {
                Log.w(TAG, "555 = mount fa");
//                handler.sendEmptyMessage(MOUNT_FAILD);
                if(!NFSActivity.this.isFinishing()) {
					Message message = new Message();
		        	message.what = MOUNT_FAILD;
		    		message.arg1 = mountResult;
		    		handler.sendMessage(message);
                }
                // end modify by by qian_wei/zhou_yong 2011/10/21
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
     * @param path 文件路径
     */
//    SocketClient socketClient = null;
//    private File openFile;

    public void getFiles(String path) {
        openFile = new File(path);
        Log.w("FILENAME", " = "+openFile.getName());
        if (openFile.isDirectory()) {
            // begin modify by qian_wei/xiong_cuifan 2011/11/05
            // for broken into the directory contains many files,click again error
            Log.w("DIR", "DIR");
//            if (currentFileString.length() < path.length()) {
//                myPosition = 0;
//            }
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

    };

    /**
     * 将文件列表填充到数据容器中
     *
     * @param files 文件列表
     * @param fileroot 文件目录
     */
    public void fill(File fileroot) {
        try {
//            li = adapter.getFiles();
//            Log.w("LIST", " = " + li.size());
            // 设置路径文件框字体颜色
            // pathTxt.setTextColor(Color.BLACK);
            if(parentPosition >= listFile.size()) {
                parentPosition = listFile.size() -1;
            }
            numInfo.setText((parentPosition + 1) + "/" + listFile.size());
            if (!fileroot.getPath().equals(baseStr)) {
                parentPath = fileroot.getParent();
                currentFileString = fileroot.getPath();
                if (fileroot.getPath().startsWith(ISO_PATH)) {
                    pathTxt.setText(currentFileString);
                } else {
                    pathTxt.setText(fileroot.getPath().substring(
                            baseStr.length()));
                }
            } else {
                currentFileString = baseStr;
                pathTxt.setText(fileroot.getPath().substring(baseStr.length())
                        + "/");
            }

            if (listFile.size() == 0) {
                numInfo.setText(0 + "/" + 0);
            }

            if ((listFile.size() == 0) && (showBut.findFocus() == null)
                    && (filterBut.findFocus() == null)) {
                sortBut.requestFocus();
            }

            Log.w("INFO NUM", numInfo.getText().toString());
            Log.w("INFO PATH", pathTxt.getText().toString());
            if(parentPosition >= 0) {
            if (listView.getVisibility() == View.VISIBLE) {
                listView.requestFocus();
                listView.setSelection(parentPosition);
            } else if (gridView.getVisibility() == View.VISIBLE) {
                gridView.requestFocus();
                gridView.setSelection(parentPosition);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 管理文件操作
     *
     * @param position 操作文件在列表中位置
     * @param item 操作类型
     */
    private void managerF(final int position, final int item) {

        // begin modify by qian_wei/cao_shanshan 2011/10/24
        // for while first delete more than one file then cause exception
//        if (position == listFile.size()) {
        if (position >= listFile.size()) {
            myPosition = listFile.size() - 1;
        }
        // end modify by qian_wei/cao_shanshan 2011/10/24

        menu_item = item;
        // 执行具体操作
        if ((item == MENU_PASTE) || (item == MENU_RENAME)) {
            getMenu(myPosition, item, list);
        } else {

            /**
             * @tag : begin modify by qian_wei 2011/7/5
             * @brief : 支持遥控器左右键移到到确定、取消按钮上
             */
            LayoutInflater inflater = LayoutInflater.from(NFSActivity.this);
            View view = inflater.inflate(R.layout.samba_server_list_dlg_layout,
                    null);
            dialog = new NewCreateDialog(NFSActivity.this);
            dialog.setView(view);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.ok), imageButClick);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.cancel), imageButClick);
            dialog.show();
            dialog = FileUtil.setDialogParams(dialog, NFSActivity.this);

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextAppearance(NFSActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextAppearance(NFSActivity.this,
                            android.R.style.TextAppearance_Large_Inverse);
            list = (ListView) dialog.findViewById(R.id.lvSambaServer);

            // 让列表为多选模式
            list.setItemsCanFocus(false);
            list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            selectList.clear();

            list.setAdapter(new ControlListAdapter(NFSActivity.this, listFile));
            list.setItemChecked(myPosition, true);
            list.setSelection(myPosition);
            selectList.add(listFile.get(myPosition).getPath());
            list.setOnItemClickListener(deleListener);

            list.clearFocus();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();

        }
    }

    /**
     * 操作菜单
     *
     * @param position 目标文件位置
     * @param item 操作
     * @param list 数据容器
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
            file = new File(currentFileString + "/"
                    + listFile.get(selectionRowID).getName());
            fileArray.add(file);
            menu.getTaskMenuDialog(NFSActivity.this, myFile, fileArray, sp,
                    item, 0);
        }
        // 粘贴操作
        else if (item == MENU_PASTE) {
            fileArray = new ArrayList<File>();
            menu.getTaskMenuDialog(NFSActivity.this, myFile, fileArray, sp,
                    item, 0);
        }
        // 其余操作
        else {
            fileArray = new ArrayList<File>();
            for (int i = 0; i < selectList.size(); i++) {
                file = new File(selectList.get(i));
                fileArray.add(file);
            }
            menu.getTaskMenuDialog(NFSActivity.this, myFile, fileArray, sp,
                    item, 0);
        }

    }

    /**
     * 文件列表选择事件
     */
    OnItemSelectedListener itemSelect = new OnItemSelectedListener() {

        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            if (parent.equals(listView) || parent.equals(gridView)) {
                myPosition = position;
            }
            if (!pathTxt.getText().toString().equals("")) {
                numInfo.setText((position + 1) + "/" + listFile.size());
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * 文件列表排序
     *
     * @param sort 排序方式
     */
    FileUtil util;

    public void updateList(boolean flag) {
        if (flag) {
            Log.w(TAG, "TRUE");
            // 让按钮能够点击
            showBut.setOnClickListener(clickListener);
            sortBut.setOnClickListener(clickListener);
            filterBut.setOnClickListener(clickListener);
            // begin modify by qian_wei/xiong_cuifan 2011/11/05
            // for broken into the directory contains many files,click again error
            listFile.clear();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            progress = new ProgressDialog(NFSActivity.this);
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
                        intList.add(parentPosition);
                    } else {
                        parentPosition = myPosition;
                        currentFileString = preCurrentPath;
                        Log.v("\33[32m Main1","onCancel" + currentFileString +"\33[0m");
                        intList.remove(intList.size()-1);
                    }
                    FileUtil.showToast(NFSActivity.this, getString(R.string.cause_anr));
                }
            });
            // end modify by qian_wei/xiong_cuifan 2011/11/08
            thread.start();
        } else {
            Log.w("TAG", "FALSE");
            adapter.notifyDataSetChanged();
            // begin delete by qian_wei/zhou_yong 2011/10/21
            // for delete failed then can not do nothing
//            listFile = new ArrayList<File>();
            // end delete by qian_wei/zhou_yong 2011/10/21
            fill(new File(currentFileString));
        }
    }

    /**
     * 前进、回退、刷新按钮处理事件
     */
    DialogInterface.OnClickListener imageButClick = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Log.d("=====", "=====");
                if (selectList.size() > 0) {
                    getMenu(myPosition, menu_item, list);
                    dialog.cancel();
                } else {
                    FileUtil.showToast(NFSActivity.this, NFSActivity.this
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.w("NFSACTIVITY", " = " + keyCode);
        switch (keyCode) {
        // 添加对enter与dpad_center键触发同一功能
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_CENTER:
            super.onKeyDown(KeyEvent.KEYCODE_ENTER, event);
            return true;

        case KeyEvent.KEYCODE_BACK:// KEYCODE_BACK
            keyBack = true;
            String newName = pathTxt.getText().toString().trim();
            Log.i(TAG, "1344::[onKeyDown]path hint=" + newName);
            // 已经返回到挂在目录列表
            if (BLANK.equals(newName) || null == newName) {
                clickCount++;
                if (clickCount == 1) {
                    // getFiles(parentPath);
                    // updateList(sort);
                    FileUtil.showToast(NFSActivity.this,
                            getString(R.string.quit_app));
                } else if (clickCount == 2) {
                    // 清空剪贴板内容
                    SharedPreferences share = getSharedPreferences("OPERATE",
                            SHARE_MODE);
                    share.edit().clear().commit();
                    if (FileUtil.getToast() != null) {
                        FileUtil.getToast().cancel();
                    }
                    onBackPressed();
                }
            } else {
                clickCount = 0;
                Log.i(TAG, "1350::[onKeyDown]parent path=" + parentPath);
                Log.i(TAG, "1351::[onKeyDown]currentFileString="
                        + currentFileString);
                Log.i(TAG, "1352::[onKeyDown]baseStr=" + baseStr);
                // 返回的服务器目录
                if (currentFileString.equals(baseStr)) {
                    Log.i(TAG, "intList.size()=" + (intList.size() - 1));
                    // begin modify by qian_wei/cao_shanshan 2011/10/25
                    // for prevent the index < 0
//                    parentPosition = intList.get(intList.size() - 1);
                    if(intList.size() > 0) {
                        parentPosition = intList.get(intList.size() - 1);
                    } else {
                        parentPosition = 0;
                    }
                    // end modify by qian_wei/cao_shanshan 2011/10/25
                    listFile.clear();
                    refreshView();// 刷新服务器列表
                    pathTxt.setText(BLANK);
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
                    parentPosition = 0;
                } else {
                    Log.i(TAG, "1434::[onKeyDown]parentPath=" + parentPath);
                    fileL.clear();
                    if (newName.equals(ISO_PATH)) {
                        Log.w("PREV", " = " + prevPath);
                        getFiles(prevPath);
                    } else {
                        getFiles(parentPath);
                    }
                }

                // 点击的父目录位置
                int pos = 0;
                pos = intList.size() - 1;
                if (pos >= 0) {
                    if (listView.getVisibility() == View.VISIBLE) {
                        listView.requestFocus();
                        parentPosition = intList.get(pos);
                        intList.remove(pos);
                    } else if (gridView.getVisibility() == View.VISIBLE) {
                        gridView.requestFocus();
                        parentPosition = intList.get(pos);
                        intList.remove(pos);
                    }
                    myPosition = parentPosition;
                }
            }
            return true;

        case KeyEvent.KEYCODE_SEARCH: // search
            if (!pathTxt.getText().toString().equals("")) {
                searchFileDialog();
            }
            return true;
        case KeyEvent.KEYCODE_INFO: // info
            if (!pathTxt.getText().toString().equals("")) {
                FileUtil util = new FileUtil(this);
                util.showFileInfo(listFile.get(myPosition));
            }
            return true;
        case KeyEvent.KEYCODE_HELP: // help
            FileMenu.setHelpFlag(3);
            FileMenu.filterType(NFSActivity.this, MENU_HELP, null);
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

    public ListView getListView() {
        return listView;
    }

    /**
     * @author ni_guanhua 获取格式化的mountinfo对象列表，并设置视图适配器
     * @return
     */
    private boolean refreshView() {
        parentPath = "";
        adpt = new NfsSvrAdapter(this, postFormatedMountinfos);
        Log.v(TAG, "1734::[refreshView]NfsSvrAdapter=" + adpt);
        listView.setAdapter(adpt);
        listView.setSelection(parentPosition);
        listView.setOnItemClickListener(ItemClickListener);
        return true;
    }

    /**
     * 将挂载列表格式化成Mountinfo对象列表
     *
     * @author ni_guanhua
     * @param mountedList 调用jni返回的挂载列表字符串
     * @return 挂载信息对象列表
     */
    public List<Mountinfo> wrap2Mountinfo(String mountedList) {
        String[] preFmtedMntinfos = mountedList.split("\\|");
        List<Mountinfo> fmtedMountinfos = new ArrayList<Mountinfo>(1);

        Mountinfo postFmtedInfo;
        for (int i = 0; i < preFmtedMntinfos.length; i++) {
            postFmtedInfo = new Mountinfo();
            String[] infos = preFmtedMntinfos[i].split(":");
            postFmtedInfo.setSzSvrIP(infos[0]);
            postFmtedInfo.setSzSvrFold(infos[1]);
            postFmtedInfo.setSzCltFold(infos[2]);
            postFmtedInfo.setUcIsAuto(Integer.parseInt(infos[3]));
            postFmtedInfo.setUcIsMounted(Integer.parseInt(infos[4]));
            postFmtedInfo.setPcName(infos[0]);
            fmtedMountinfos.add(postFmtedInfo);
        }
        return fmtedMountinfos;
    }

    /**
     * 添加自动挂载对话框按钮监听器
     */
    private class ClickListener implements View.OnClickListener {

        public void onClick(View v) {
            if (v.getId() == R.id.addAutoBut) {
                mounted();
            }
        }
    }

    /**
     * 点击新增挂载菜单，在新增对话框中，进行输入后， 点击新增挂载按钮
     */
    String pSvrFold;
    int ucIsAuto = 0;
    int flag = -1;

    void mounted() {
        // 关闭对话框
        // nfsAddDlg.cancel();
        // 点击自动挂载按钮
        pSvrIP = edtIpAddress.getText().toString().trim();
        Log.d(TAG, "242::ip_address=" + pSvrIP);
        pSvrFold = edtServerFolder.getText().toString().trim();
        Log.d(TAG, "244::server_folder=" + pSvrIP);
        // String pCltFold = edtLocalFolder.getText().toString().trim();
        // Log.d(TAG, "246::mout_folder=" + pCltFold);
        if (checkAuto.isChecked()) {
            ucIsAuto = 1;
        } else {
            ucIsAuto = 0;
        }
        // 服务器ip地址
        if (BLANK.equals(pSvrIP) || null == pSvrIP) {
            FileUtil.showToast(NFSActivity.this, getString(R.string.nfsAddHint,
                    getString(R.string.ip_address)));
        } else if (!pSvrIP.matches(IP_REGEX)) {
            FileUtil.showToast(NFSActivity.this,
                    getString(R.string.server_ip_fmt_err));
        }
        // 服务器目录
        else if (BLANK.equals(pSvrFold) || null == pSvrFold) {
            FileUtil.showToast(NFSActivity.this, getString(R.string.nfsAddHint,
                    getString(R.string.server_folder)));
        } else {

            if (!pSvrFold.startsWith("/")) {
                pSvrFold = "/" + pSvrFold;
            }

            if (pSvrFold.endsWith("/")) {
                pSvrFold = pSvrFold.substring(0, pSvrFold.length() - 1);
            }

            if (getCltFolder(pSvrIP, pSvrFold) != null) {
                FileUtil.showToast(NFSActivity.this,
                        getString(R.string.mount_exist));
            } else {
                nfsAddDlg.cancel();
                // 显示等待对话框
                showWaitingDlg();
                // 开启线程进行挂载
                new Thread(new Runnable() {
                    public void run() {
                        // 当输入内容均不为空时
						int nMountResult = nNfsClient.mountNFSSvr(pSvrIP, pSvrFold, BLANK,
								ucIsAuto);
						Log.d(TAG, "326::new_mount_flag=" + nMountResult);
						Message message = new Message();
			        	message.what = ADD_MOUNT;
			    		message.arg1 = nMountResult;
			    		handler.sendMessage(message);
                    }
                }).start();
            }
        }
    }

    /**
     * 关闭等待对话框
     */
    void closeWaitingDlg() {
        if (null != nPgrDlgWaiting) {
            nPgrDlgWaiting.dismiss();
        }
    }

    // 多选对话框是否是首次显示
    boolean isFirstShown;

    /**
     * 显示设置自动挂载对话框
     *
     * @author ni_guanhua
     */
    private void showSetAutoDlg() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nfs_del_mount_dlg_layout, null);

        dlg = new NewCreateDialog(NFSActivity.this);
        dlg.setView(view);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.yesStr), new DlgClickListener());
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancelStr), new DlgClickListener());
        dlg.show();
        dlg = FileUtil.setDialogParams(dlg, NFSActivity.this);

        dlg.getButton(DialogInterface.BUTTON_POSITIVE).setTextAppearance(
                NFSActivity.this, android.R.style.TextAppearance_Large_Inverse);
        dlg.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();
        dlg.getButton(DialogInterface.BUTTON_NEGATIVE).setTextAppearance(
                NFSActivity.this, android.R.style.TextAppearance_Large_Inverse);

        ListView lvSetAutoShareList = (ListView) view
                .findViewById(R.id.lvDelShareList);
        Log.d(TAG, "361::listView=" + lvSetAutoShareList);
        // 使列表item可以获得焦点
        lvSetAutoShareList.setItemsCanFocus(true);
        lvSetAutoShareList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // 设置listview可以点击
        lvSetAutoShareList.setClickable(true);

        // 重新获取列表
        String mountedList = nNfsClient.getMountedList();
        Log.d(TAG, "39::showSetAutoDlg()_getMountedList()=" + mountedList);
        if (mountedList.length() > 0) {
            List<Mountinfo> refreshedlist = wrap2Mountinfo(mountedList);
            Log.d(TAG, "229::refreshedlist_size=" + refreshedlist.size());
            postFormatedMountinfos.clear();
            Log.d(TAG, "231::refreshView()_preAdd_postFormatedMountinfos_size="
                    + postFormatedMountinfos.size());
            postFormatedMountinfos.addAll(refreshedlist);
            Log.d(TAG, "233::refreshView()_postFormatedMountinfos_size="
                    + postFormatedMountinfos.size());
        } else if (mountedList.length() == 0) {
            postFormatedMountinfos.clear();
        }

        Log.d(TAG, "367::list_size()=" + postFormatedMountinfos.size());
        // 设置数据适配器
        lvSetAutoShareList.setAdapter(new SetAutoShareDataAdapter(
                NFSActivity.this, postFormatedMountinfos));

        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.nfsDlgOpts);

        builder.setView(view);
        builder.setPositiveButton(R.string.yesStr, new DlgClickListener());
        builder.setNegativeButton(R.string.cancelStr, new DlgClickListener());

        isFirstShown = true;// 是否时首次显示

    }

    /**
     * 对话框多选监听器
     *
     * @author ni_guanhua
     */
    private class DlgClickListener implements DialogInterface.OnClickListener {

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                ListView dlgLstView = (ListView) ((AlertDialog) dialog)
                        .findViewById(R.id.lvDelShareList);
                int count = dlgLstView.getCount();
                Log.d(TAG, "664::COUNT=" + count);

                Mountinfo info = null;

                int flag = Integer.MAX_VALUE;
                // 进行设置自动挂载
                for (int i = 0; i < count; i++) {
                    info = postFormatedMountinfos.get(i);
                    // 判断状态修改标志位
                    if (dlgLstView.getCheckedItemPositions().get(i)) {
                        Log.d(TAG, "667::checked_isAuto=" + info.getUcIsAuto());
                        if (info.getUcIsAuto() == 0) {
                            info.setUcIsAuto(1);
                            flag = nNfsClient.setAutomount(info.getSzSvrIP(),
                                    info.getSzSvrFold(), info.getSzCltFold(),
                                    info.getUcIsAuto());
                        }
                    } else {
                        Log.d(TAG, "674::checked_isAuto=" + info.getUcIsAuto());
                        if (info.getUcIsAuto() == 1) {
                            info.setUcIsAuto(0);
                            flag = nNfsClient.setAutomount(info.getSzSvrIP(),
                                    info.getSzSvrFold(), info.getSzCltFold(),
                                    info.getUcIsAuto());
                        }
                    }

                    if (flag == 1) {
                        dialog.dismiss();
                        break;
                    }
                }
                // 设置失败
                if (flag == 1) {
                    FileUtil.showToast(NFSActivity.this,
                            getString(R.string.set_fail));
                    dialog.dismiss();
                }

                // 设置自动挂载成功
                if (flag == 0) {
                    FileUtil.showToast(NFSActivity.this,
                            getString(R.string.set_success));
                    dialog.dismiss();
                }
                refreshView();
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            }

        }

    }

    /**
     * 设置自动挂载数据适配器
     *
     * @author ni_guanhua
     */
    class SetAutoShareDataAdapter extends BaseAdapter {
        Context context;

        List<Mountinfo> mountList;

        public SetAutoShareDataAdapter(Context context,
                List<Mountinfo> mountList) {
            this.context = context;
            this.mountList = mountList;
        }

        public int getCount() {
            return mountList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ListView listView = (ListView) parent
                    .findViewById(R.id.lvDelShareList);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            Log.d(TAG, "683::getView()_listView=" + listView);

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CheckedTextView chktv = (CheckedTextView) inflater.inflate(
                    R.layout.control_row, null);
            String clientFolder = mountList.get(position).getSzSvrIP() + ":"
                    + mountList.get(position).getSzSvrFold();
            Log.d(TAG, "847::IS_AUTO=" + mountList.get(position).getUcIsAuto());
            // 如果要显示的内容比CheckedTextView的宽度宽，设置跑马灯效果
            if (chktv.getPaint().measureText(clientFolder) > chktv.getWidth()) {
                chktv.setEllipsize(TruncateAt.MARQUEE);
                chktv.setMarqueeRepeatLimit(-1);
                chktv.setHorizontallyScrolling(true);
            }
            // 使用是否是首次显示控制之前选中的此次是否可以被取消
            if (isFirstShown) {
                if (position == getCount() - 1) {
                    isFirstShown = false;
                }
                if (mountList.get(position).getUcIsAuto() == 1) {
                    Log.d(TAG, "855::checked");
                    listView.setItemChecked(position, true);
                }
            }
            chktv.setText(clientFolder);
            return chktv;
        }
    }

    /**
     * 显示卸载列表对话框
     *
     * @author ni_guanhua
     */
    private void showUninstallDlg() {
        // 获取在显示列表中选中的位置项
        // 在删除类表中将相应的位置选中
        int selectedPosition = listView.getSelectedItemPosition();
        if (selectedPosition == -1) {
            selectedPosition = 0;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nfs_del_mount_dlg_layout, null);

        dlg = new NewCreateDialog(NFSActivity.this);
        dlg.setTitle(R.string.optUninstallStr);
        dlg.setView(view);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.yesStr), new UninstallDlgClickListener());
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancelStr), new UninstallDlgClickListener());
        dlg.show();

        dlg = FileUtil.setDialogParams(dlg, NFSActivity.this);

        dlg.getButton(DialogInterface.BUTTON_POSITIVE).setTextAppearance(
                NFSActivity.this, android.R.style.TextAppearance_Large_Inverse);
        dlg.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();
        dlg.getButton(DialogInterface.BUTTON_NEGATIVE).setTextAppearance(
                NFSActivity.this, android.R.style.TextAppearance_Large_Inverse);

        ListView lvDelShareList = (ListView) view
                .findViewById(R.id.lvDelShareList);
        // 使列表item可以获得焦点
        lvDelShareList.setItemsCanFocus(true);
        lvDelShareList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // 设置数据适配器
        lvDelShareList.setAdapter(new DelShareDataAdapter(NFSActivity.this,
                postFormatedMountinfos));
        // 设置listview选中位置
        lvDelShareList.setItemChecked(selectedPosition, true);
    }

    /**
     * 删除挂载数据
     *
     * @author ni_guanhua
     */
    static class DelShareDataAdapter extends BaseAdapter {
        Context context;

        List<Mountinfo> mountList;

        public DelShareDataAdapter(Context context, List<Mountinfo> mountList) {
            this.context = context;
            this.mountList = mountList;
        }

        public int getCount() {
            return mountList.size();
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
            CheckedTextView chktv = (CheckedTextView) inflater.inflate(
                    R.layout.control_row, null);
            String clientFolder = mountList.get(position).getSzSvrIP() + ":"
                    + mountList.get(position).getSzSvrFold();
            // 如果要显示的内容比CheckedTextView的宽度宽，设置跑马灯效果
            if (chktv.getPaint().measureText(clientFolder) > chktv.getWidth()) {
                chktv.setEllipsize(TruncateAt.MARQUEE);
                chktv.setMarqueeRepeatLimit(-1);
                chktv.setHorizontallyScrolling(true);
            }
            chktv.setText(clientFolder);
            return chktv;
        }

    }

    /**
     * 卸载挂载列表单机监听器
     *
     * @author ni_guanhua
     */
    private class UninstallDlgClickListener implements
            DialogInterface.OnClickListener {

        public void onClick(final DialogInterface dialog, int which) {
            Log.d(TAG, "515::postFormatedMountinfos=" + postFormatedMountinfos);
            if (which == DialogInterface.BUTTON_POSITIVE) {
                AlertDialog confirmDelDlg = null;
                // 确认删除对话框
                confirmDelDlg = new AlertDialog.Builder(NFSActivity.this)
                        .setTitle(R.string.confirm_delete_dlg_title)
                        .setMessage(R.string.comfirm_delete_hint).setIcon(
                                R.drawable.alert).setPositiveButton(
                                R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    // 在点击确认删除对话框后，对选项进行删除
                                    public void onClick(
                                            DialogInterface dialog1, int which) {
                                        // 显示等待对话框
                                        showWaitingDlg();
                                        new Thread(new Runnable() {

                                            public void run() {
                                                uninstallMount();
                                                handler.sendEmptyMessage(DEL_MOUNT);
                                            }
                                        }).start();

                                    }
                                }).setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(
                                            DialogInterface dialog1, int which) {
                                        dialog1.dismiss();
                                        // dialog.dismiss();// 关闭删除选项对话框
                                    }
                                }).create();
                // 显示确认删除对话框
                confirmDelDlg.show();
                // 设置按钮字体
                confirmDelDlg.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextAppearance(NFSActivity.this,
                                android.R.style.TextAppearance_Large_Inverse);
                confirmDelDlg.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setTextAppearance(NFSActivity.this,
                                android.R.style.TextAppearance_Large_Inverse);
            } else {
                dialog.dismiss();
            }
        }
    }

    /**
     * 进行删除操作
     *
     * @param dialog 卸载对话框
     */
    private int delFlag = Integer.MAX_VALUE;

    void uninstallMount() {
        dlgLstView = (ListView) dlg.findViewById(R.id.lvDelShareList);
        // 关闭对话框
        dlg.cancel();
        Log.d(TAG, "512::dlgLstView=" + dlgLstView);
        int count = dlgLstView.getCount();// 获取列表中的条目数
        Log.d(TAG, "459::postFormatedMountinfos_size="
                + postFormatedMountinfos.size());
        Log.d(TAG, "460::list_count=" + count);
        Mountinfo info = null;
        for (int i = count - 1; i >= 0; i--) {
            Log.d(TAG, "463::checked_i=" + i);
            Log.d(TAG, "465::moutinfo=" + info);
            boolean isChecked = dlgLstView.getCheckedItemPositions().get(i);
            info = postFormatedMountinfos.get(i);
            Log.d(TAG, "4645::info=" + info.getSzSvrFold());
            Log.d(TAG, "467::isChecked=" + isChecked);
            if (isChecked) {
                // 从配置文件中删除
                delFlag = nNfsClient.umountNFS(info.getSzCltFold());
				Log.d(TAG, "467::delete_flag=" + delFlag);
                // 在删除失败后，退出循环并关闭对话框
                if (delFlag == 1) {
                    break;
                } else {
                    postFormatedMountinfos.remove(info);
                    count--;
                }
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    // begin modify by qian_wei/xiong_cuifan 2011/11/05
    // for broken into the directory contains many files,click again error
    class MyThread extends MyThreadBase {
        public void run() {
            if (getFlag()) {
                setFlag(false);
                synchronized(lock){
                    util = new FileUtil(NFSActivity.this, filterCount, arrayDir,
                        arrayFile, currentFileString);
                }
            } else {
                util = new FileUtil(NFSActivity.this, filterCount,
                        currentFileString);
            }
            listFile = util.getFiles(sortCount, "net");

//            //BEGIN : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来
//            // begin modify by yuejun 2011/12/16
//            List<File> temp1ListFile = new ArrayList<File>();
//            List<File> temp2ListFile = new ArrayList<File>();
//            if(currentFileString.toLowerCase().contains("bdmv"))
//            {
//                temp1ListFile.add(getMaxFile(listFile));
//                listFile=temp1ListFile;
//            }
//            else if(currentFileString.toLowerCase().contains("video_ts"))
//            {
//                for(int i=0; i<listFile.size(); i++)
//                {
//                    if(listFile.get(i).toString().substring(listFile.get(i).toString().lastIndexOf(".")).equalsIgnoreCase(".vob"))
//                        temp1ListFile.add(listFile.get(i));
//                }
//
//                for(int j=0; j<temp1ListFile.size(); j++)
//                {
//                    if(temp1ListFile.get(j).length() >= (long)100*1024*1024)
//                        temp2ListFile.add(temp1ListFile.get(j));
//                }
//                listFile = temp2ListFile;
//            }
//            // end modify by yuejun 2011/12/16
//            //END : z00120637 暂时屏蔽掉ISO过滤功能，如果需要时再开放开来

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
            // end modify by qian_wei/xiong_cuifan 2011/11/05
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

    private String getCltFolder(String sIp, String sFol) {
        String mountedList = nNfsClient.getMountedList();
        if (mountedList.length() > 0) {
            String[] preFmtedMntinfos = mountedList.split("\\|");
            for (int i = 0; i < preFmtedMntinfos.length; i++) {
                String[] infos = preFmtedMntinfos[i].split(":");
                if (sIp.equals(infos[0]) && sFol.equals(infos[1])) {
                    Log.w(TAG, " 31 = " + infos[2]);
                    return infos[2];
                }
            }
        } else {
            return null;
        }
        return null;
    }

    // begin add by qian_wei/xiong_cuifan 2011/11/07
    // for grally3D delete the file, flush the data
    protected void onResume() {
        super.onResume();
        if(!currentFileString.equals("") && preCurrentPath.equals(currentFileString)) {
            updateList(true);
        }
    }
    // end add by qian_wei/xiong_cuifan 2011/11/08
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
