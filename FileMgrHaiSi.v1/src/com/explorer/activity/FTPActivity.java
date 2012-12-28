package com.explorer.activity;

import com.explorer.R;
import com.explorer.common.CommonActivity;
import com.explorer.common.FileUtil;
import com.explorer.common.MyDialog;
import com.explorer.ftp.ControlFtpAdapter;
import com.explorer.ftp.DBHelper;
import com.explorer.ftp.FtpFileAdapter;
import com.explorer.jni.FtpClient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FTP客户端操作
 * 
 * @author qian_wei
 */

public class FTPActivity extends CommonActivity {

	// 确定按钮
	Button myOkBut;

	// 取消按钮
	Button myCancelBut;

	// 新建、编辑Dialog
	MyDialog dialog;

	// 进程Dialog
	ProgressDialog proDialog;

	// 服务器地址输入框
	EditText serverAddress;

	// 端口输入框
	// EditText portID;

	// 用户名输入框
	EditText userName;

	// 密码输入框
	EditText userPass;

	// 昵称输入框
	EditText nickName;

	// TextView text;

	// 文件路径显示框
	private TextView titlePath;

	ListView layoutGrid;

	// ImageView imageIcon;

	// 匿名登录标识 true:匿名 false:全名
	CheckBox loginCheck;

	// 下载删除文件列表数据容器
	CheckedTextView myCheck;

	List<Map<String, Object>> list;

	// boolean flag = false;

	// Ftp客户端
	FtpClient client;

	// String dirPath = "";

	// 客户端返回结果
	String[] result = new String[] {};

	List<String> fileList;

	// 根目录路径
	static String rootDir = null;

	List<String> fileNameList;

	List<String> nameList;

	// // 服务器列表
	// GridView iconList;

	File file;

	String nameArray;

	// 文字索引
	int Num = 0;

	// 文本框内容长度
	int tempLength = 0;

	// // 过滤类型
	// private Spinner filterSpinner;
	//
	// // 展示方式
	// private Spinner showSpinner;
	//
	// // 排序方法
	// private Spinner sortMethod;

	// 缩略图数据容器
	private GridView gridView;

	private DBHelper dbHelper;

	private SQLiteDatabase sqlite;

	List<String> resultList;

	// 文件大小列表
	List<Long> sizeList;

	// 游标
	Cursor cursor;

	// 昵称
	String nickname = "";

//	List<String> arrayFile;
//
//	List<String> arrayDir;

	// List<String> existFile;

	int controlFlag = -1;

	// 程序的返回结果
	static int resultCode = 0;

	String sIp = null;

	String sName = null;

	String sPwd = null;

	// LinearLayout layout = null;

	// 下载目录
	static String downPath = "/sdcard/ftp";

	String myName = "";

	Dialog alertDialog;

	static Button okBut;

	static Button cancleBut;

	int operation = 0;

	// 目录点击位置索引
	int parentPosition = 0;

	// 记录目录点击位置
	List<Integer> intList;

	// 根目录后点击次数
	int clickCount = 0;

	// 端口输入框
	private EditText serverport;

//	private ImageButton sortBut;
//
//	private ImageButton filterBut;
//
//	private ImageButton showBut;

//	int[] sortArray;
//
//	int[] filterArray;
//
//	int[] showArray;
	private static String FTP_PATH = "/sdcard/ftp";

	// 加载JNI包
	static {
		System.loadLibrary("android_runtime");
	}

	/**
	 * 加载页面
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ftp_layout);

		showBut = (ImageButton) findViewById(R.id.showBut);
		showArray = new int[] { R.drawable.show_by_list,
				R.drawable.show_by_thum };

		sortBut = (ImageButton) findViewById(R.id.sortBut);
		sortArray = new int[] { R.drawable.sort_by_name,
				R.drawable.sort_by_size, R.drawable.sort_by_modifytime };

		filterBut = (ImageButton) findViewById(R.id.filterBut);
		filterArray = new int[] { R.drawable.filter_by_file,
				R.drawable.filter_by_picture, R.drawable.filter_by_audio,
				R.drawable.filter_by_video };

		// 开启数据库
		dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, null,
				DBHelper.DATABASE_VERSION);
		sqlite = dbHelper.getWritableDatabase();

		intList = new ArrayList<Integer>();
		titlePath = (TextView) findViewById(R.id.title_path);
		// iconList = (GridView) findViewById(R.id.sdList);
		client = new FtpClient();
		layoutGrid = (ListView) findViewById(R.id.listView);
		gridView = (GridView) findViewById(R.id.gridView);
		list = new ArrayList<Map<String, Object>>();
		resultList = new ArrayList<String>();
		sizeList = new ArrayList<Long>();
		fileList = new ArrayList<String>();
		fileNameList = new ArrayList<String>();
		nameList = new ArrayList<String>();
		// existFile = new ArrayList<String>();
		dialog = new MyDialog(this, R.layout.ftp_layout_dialog);
		// layout = (LinearLayout) findViewById(R.id.mylayout);
		// filterSpinner = (Spinner) findViewById(R.id.spinner01);
		// showSpinner = (Spinner) findViewById(R.id.spinner03);
		// sortMethod = (Spinner) findViewById(R.id.sortMethod);
		// filterSpinner.setVisibility(View.INVISIBLE);
		// showSpinner.setVisibility(View.INVISIBLE);
		// sortMethod.setVisibility(View.INVISIBLE);

		// // 初始化过滤类型下拉框
		// ArrayAdapter<String> adapter01 = new ArrayAdapter<String>(this,
		// R.layout.spinner_lay,
		// getResources().getStringArray(R.array.spinner_operation01));
		// adapter01.setDropDownViewResource(R.layout.spinner_down);
		// filterSpinner.setAdapter(adapter01);
		// filterSpinner.setOnItemSelectedListener(itemListener);
		//
		// // 初始化展示方式下拉框
		// ArrayAdapter<String> adapter03 = new ArrayAdapter<String>(this,
		// R.layout.spinner_lay,
		// getResources().getStringArray(R.array.spinner_operation03));
		// adapter03.setDropDownViewResource(R.layout.spinner_down);
		// showSpinner.setAdapter(adapter03);
		// showSpinner.setOnItemSelectedListener(itemListener);
		//
		// // 初始化文件排序下拉框
		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		// R.layout.spinner_lay,
		// getResources().getStringArray(R.array.sort_method));
		// adapter.setDropDownViewResource(R.layout.spinner_down);
		// sortMethod.setAdapter(adapter);
		// sortMethod.setOnItemSelectedListener(itemListener);

		// 更新FTP连接列表
		updateData();

		// // 连接列表点击事件
		// layoutGrid.setOnItemClickListener(new OnItemClickListener() {
		// public void onItemClick(AdapterView<?> parent, View view, final int
		// position, long id) {
		// TextView txt = (TextView) view.findViewById(R.id.text);
		// if(titlePath.getText().toString().endsWith(""))
		// {
		//
		// nickname = txt.getText().toString().trim();
		// proDialog = new ProgressDialog(FTPActivity.this);
		// proDialog.setMessage(getResources().getString(R.string.connecting));
		// proDialog.show();
		// // 开启连接线程
		// Conn conn = new Conn();
		// conn.start();
		// }
		// else
		// {
		// if (fileList.get(position).split("\\|")[0].equals("d")) {
		// intList.add(position);
		// String pa = titlePath.getText().toString() + "/"
		// + fileList.get(position).split("\\|")[1].trim();
		// getProperty();
		// // 进入下级目录
		// int resultCode = client.changeRemoteDir(sIp, sName, sPwd, pa);
		// if (resultCode == 250) {
		// clickCount = 0;
		// result = client.result;
		// getList();
		// titlePath.setText(pa);
		// }
		// }
		// }
		// }
		// });
	}

	// 文件列表点击事件
	OnItemClickListener onclick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				final int position, long id) {
			TextView txt = (TextView) view.findViewById(R.id.text);
			if (titlePath.getText().toString().endsWith("")) {
				intList.add(position);
				nickname = txt.getText().toString().trim();
				proDialog = new ProgressDialog(FTPActivity.this);
				proDialog.setMessage(getResources().getString(
						R.string.connecting));
				proDialog.show();
				// 开启连接线程
				Conn conn = new Conn();
				conn.start();
			} else {
				if (fileList.get(position).split("\\|")[0].equals("d")) {
					intList.add(position);
					String pa = titlePath.getText().toString() + "/"
							+ fileList.get(position).split("\\|")[1].trim();
					getProperty();
					// 进入下级目录
					int resultCode = client.changeRemoteDir(sIp, sName, sPwd,
							pa);
					if (resultCode == 250) {
						clickCount = 0;
						result = client.result;
						getList();
						titlePath.setText(pa);
					}
				}
			}
		}
	};

	/**
	 * 新建菜单
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(1, 1, 1, R.string.hide_tab);
		menu.add(1, 2, 2, R.string.add_server);
		menu.add(1, 3, 3, R.string.delete_server);
		menu.add(1, 4, 4, R.string.quit_server);
		SubMenu operatFile = menu.addSubMenu(1, 5, 5, getResources().getString(
				R.string.operation));
		operatFile.add(1, 6, 6, R.string.down_file);
		operatFile.add(1, 7, 7, R.string.del_file);
		operatFile.add(1, 8, 8, R.string.del_folder);
		operatFile.add(1, 9, 9, R.string.str_rename);
		menu.add(1, 10, 10, R.string.edit_server);
		return true;
	}

	/**
	 * 控制菜单的状态
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		// // 文件路径为空
		// if (getTitlePath().equals("")) {
		// if ((list.size() == 0) || (layoutGrid.getSelectedItemPosition() ==
		// -1)) {
		// menu.getItem(2).setEnabled(false);
		// menu.getItem(5).setEnabled(false);
		// } else {
		// menu.getItem(2).setEnabled(true);
		// menu.getItem(5).setEnabled(true);
		// }
		// menu.getItem(3).setEnabled(false);
		// menu.getItem(4).setEnabled(false);
		// } else {
		// // 文件列表获得焦点
		// if ((layoutGrid.findFocus() != null) || (gridView.findFocus() !=
		// null)) {
		// menu.getItem(4).setEnabled(true);
		// menu.getItem(2).setEnabled(false);
		// menu.getItem(3).setEnabled(false);
		// menu.getItem(5).setEnabled(false);
		// } else {
		// menu.getItem(4).setEnabled(false);
		// menu.getItem(2).setEnabled(true);
		// menu.getItem(3).setEnabled(true);
		// menu.getItem(5).setEnabled(true);
		// }
		//
		// // 文件列表为空
		// if ((result.length == 0)) {
		// menu.getItem(4).setEnabled(false);
		// } else {
		//
		// if (arrayFile.size() == 0) {
		// menu.getItem(4).getSubMenu().getItem(0).setEnabled(false);
		// menu.getItem(4).getSubMenu().getItem(1).setEnabled(false);
		// } else {
		// menu.getItem(4).getSubMenu().getItem(0).setEnabled(true);
		// menu.getItem(4).getSubMenu().getItem(1).setEnabled(true);
		// }
		//
		// if (arrayDir.size() == 0) {
		// menu.getItem(4).getSubMenu().getItem(2).setEnabled(false);
		// } else {
		// menu.getItem(4).getSubMenu().getItem(2).setEnabled(true);
		// }
		// }
		// }

		if (list.size() == 0) {
			menu.getItem(4).setEnabled(false);
			menu.getItem(2).setEnabled(false);
			menu.getItem(3).setEnabled(false);
			menu.getItem(5).setEnabled(false);
		} else {
			if (getTitlePath().equals("")) {
				if (layoutGrid.findFocus() != null
						|| gridView.findFocus() != null) {
					menu.getItem(4).setEnabled(false);
					menu.getItem(2).setEnabled(true);
					menu.getItem(3).setEnabled(true);
					menu.getItem(5).setEnabled(true);
				}
			} else {
				// 文件列表为空
				if ((result.length == 0)) {
					menu.getItem(4).setEnabled(false);
				} else {

					if (arrayFile.size() == 0) {
						menu.getItem(4).getSubMenu().getItem(0).setEnabled(
								false);
						menu.getItem(4).getSubMenu().getItem(1).setEnabled(
								false);
					} else {
						menu.getItem(4).getSubMenu().getItem(0)
								.setEnabled(true);
						menu.getItem(4).getSubMenu().getItem(1)
								.setEnabled(true);
					}

					if (arrayDir.size() == 0) {
						menu.getItem(4).getSubMenu().getItem(2).setEnabled(
								false);
					} else {
						menu.getItem(4).getSubMenu().getItem(2)
								.setEnabled(true);
					}
				}

				menu.getItem(1).setEnabled(false);
				menu.getItem(2).setEnabled(false);
				menu.getItem(3).setEnabled(false);
				menu.getItem(5).setEnabled(false);
			}
		}

		return true;
	}

	/**
	 * 菜单操作
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// 显示、隐藏TAB
		if (item.getItemId() == 1) {
			if (item.getTitle().equals(getString(R.string.hide_tab))) {
				TabBarExample.getWidget().setVisibility(View.GONE);
				item.setTitle(R.string.show_tab);
			} else {
				TabBarExample.getWidget().setVisibility(View.VISIBLE);
				item.setTitle(R.string.hide_tab);
			}
		}
		// 新增服务连接
		else if (item.getItemId() == 2) {
			controlFlag = 0;
			dialog.show();
			myOkBut = (Button) dialog.findViewById(R.id.myOkBut);
			myCancelBut = (Button) dialog.findViewById(R.id.myCancelBut);
			serverAddress = (EditText) dialog.findViewById(R.id.serverAddress);
			serverAddress.setText("");
			serverAddress.requestFocus();
			serverport = (EditText) dialog.findViewById(R.id.serverport);
			serverport.setText(R.string.num);
			userName = (EditText) dialog.findViewById(R.id.userName);
			userName.setText("");
			userPass = (EditText) dialog.findViewById(R.id.userPass);
			userPass.setText("");
			nickName = (EditText) dialog.findViewById(R.id.nickName);
			nickName.setText("");
			loginCheck = (CheckBox) dialog.findViewById(R.id.loginCheck);
			loginCheck.setChecked(false);
			loginCheck
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								userName.setEnabled(false);
								userName.setFocusable(false);
								userPass.setEnabled(false);
								userPass.setFocusable(false);
							} else {
								userName.setEnabled(true);
								userName.setFocusable(true);
								userPass.setEnabled(true);
								userPass.setFocusable(true);
							}
						}
					});
			myOkBut.setOnClickListener(myOnClickListener);
			myCancelBut.setOnClickListener(myOnClickListener);

		}
		// 删除服务
		else if (item.getItemId() == 3) {
			new AlertDialog.Builder(FTPActivity.this).setMessage(
					getString(R.string.delete_conn, nameList.size()))
					.setPositiveButton(
							FTPActivity.this.getResources().getString(
									R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									View view = layoutGrid.getSelectedView();
									TextView txt = (TextView) view
											.findViewById(R.id.text);
									nickname = txt.getText().toString().trim();
									sqlite.delete("ftp", "nick=?",
											new String[] { nickname });
									updateData();
									titlePath.setText("");
									rootDir = "";
									result = new String[] {};
									getList();
									// filterSpinner.setVisibility(View.INVISIBLE);
									// showSpinner.setVisibility(View.INVISIBLE);
									// sortMethod.setVisibility(View.INVISIBLE);
								}
							}).setNegativeButton(
							FTPActivity.this.getResources().getString(
									R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
		// 断开服务
		else if (item.getItemId() == 4) {
			new AlertDialog.Builder(FTPActivity.this).setMessage(
					getString(R.string.quit_conn, nameList.size()))
					.setPositiveButton(
							FTPActivity.this.getResources().getString(
									R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									titlePath.setText("");
									rootDir = "";
									result = new String[] {};
									getList();
									// filterSpinner.setVisibility(View.INVISIBLE);
									// showSpinner.setVisibility(View.INVISIBLE);
									// sortMethod.setVisibility(View.INVISIBLE);
								}
							}).setNegativeButton(
							FTPActivity.this.getResources().getString(
									R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
		// 下载文件
		else if (item.getItemId() == 6) {
			operation(result, 6);
		}
		// 删除文件
		else if (item.getItemId() == 7) {
			operation(result, 7);
		}
		// 删除单个文件夹
		else if (item.getItemId() == 8) {
			int position = -1;
			if (layoutGrid.getVisibility() == View.VISIBLE) {
				position = layoutGrid.getSelectedItemPosition();
			} else if (gridView.getVisibility() == View.VISIBLE) {
				position = gridView.getSelectedItemPosition();
			}
			String type = result[position].split("\\|")[0];
			if (type.equals("d")) {
				new AlertDialog.Builder(FTPActivity.this).setMessage(
						getString(R.string.d_delete, nameList.size()))
						.setPositiveButton(
								FTPActivity.this.getResources().getString(
										R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										proDialog = new ProgressDialog(
												FTPActivity.this);
										proDialog
												.setMessage(getResources()
														.getString(
																R.string.d_deleting));
										proDialog.show();
										DeleD deleD = new DeleD();
										deleD.start();
									}
								}).setNegativeButton(
								FTPActivity.this.getResources().getString(
										R.string.cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		}
		// 重命名文件/目录
		else if (item.getItemId() == 9) {
			int position = -1;
			if (layoutGrid.getVisibility() == View.VISIBLE) {
				position = layoutGrid.getSelectedItemPosition();
			} else if (gridView.getVisibility() == View.VISIBLE) {
				position = gridView.getSelectedItemPosition();
			}
			String remoteDirName = fileList.get(position).split("\\|")[1];
			String type = fileList.get(position).split("\\|")[0];
			if (remoteDirName.contains("\n")) {
				remoteDirName = remoteDirName.substring(0, remoteDirName
						.indexOf("\n"))
						+ remoteDirName.substring(
								remoteDirName.indexOf("\n") + 1, remoteDirName
										.length());
			}
			reNameFile(remoteDirName, type);
		}
		// 编辑服务器
		else if (item.getItemId() == 10) {
			controlFlag = 1;
			View view = layoutGrid.getSelectedView();
			TextView txt = (TextView) view.findViewById(R.id.text);
			nickname = txt.getText().toString().trim();
			dialog.show();
			myOkBut = (Button) dialog.findViewById(R.id.myOkBut);
			myOkBut.setOnClickListener(myOnClickListener);
			myCancelBut = (Button) dialog.findViewById(R.id.myCancelBut);
			myCancelBut.setOnClickListener(myOnClickListener);
			serverAddress = (EditText) dialog.findViewById(R.id.serverAddress);
			serverAddress.requestFocus();
			serverport = (EditText) dialog.findViewById(R.id.serverport);// 添加端口号

			userName = (EditText) dialog.findViewById(R.id.userName);
			userPass = (EditText) dialog.findViewById(R.id.userPass);
			nickName = (EditText) dialog.findViewById(R.id.nickName);
			loginCheck = (CheckBox) dialog.findViewById(R.id.loginCheck);
			loginCheck
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								userName.setEnabled(false);
								userName.setFocusable(false);
								userPass.setEnabled(false);
								userPass.setFocusable(false);
							} else {
								userName.setEnabled(true);
								userName.setFocusable(true);
								userPass.setEnabled(true);
								userPass.setFocusable(true);
							}
						}
					});
			cursor = sqlite.query("ftp", new String[] { "_id", "ip", "port",
					"name", "pwd", "flag", "nick" }, "nick=?",
					new String[] { nickname }, null, null, null);
			String sIp = null;
			String sPort = null;
			String sName = null;
			String sPwd = null;
			int flag = -1;
			String nick = null;
			while (cursor.moveToNext()) {
				sIp = cursor.getString(cursor.getColumnIndex("ip"));
				sPort = cursor.getString(cursor.getColumnIndex("port"));
				sName = cursor.getString(cursor.getColumnIndex("name"));
				sPwd = cursor.getString(cursor.getColumnIndex("pwd"));
				nick = cursor.getString(cursor.getColumnIndex("nick"));
				flag = cursor.getInt(cursor.getColumnIndex("flag"));
			}
			// 为对应输入框填充数据
			myName = nick;
			serverAddress.setText(sIp);
			serverport.setText(sPort);
			nickName.setText(nick);
			if (flag == 1) {
				loginCheck.setChecked(true);
			} else {
				loginCheck.setChecked(false);
				userName.setText(sName);
				userPass.setText(sPwd);
			}
			cursor.close();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 按钮点击操作
	 */
	OnClickListener myOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v.equals(myOkBut)) {
				String newName = "";
				if (nickName.getText().toString().trim().equals("")) {
					newName = serverAddress.getText().toString().trim();
				} else {
					newName = nickName.getText().toString().trim();
				}
				StringBuilder builder = new StringBuilder(serverAddress
						.getText().toString().trim());
				String ip1 = builder.append(" ").append(
						serverport.getText().toString().trim()).toString();
				String ip = serverAddress.getText().toString().trim();
				String port = serverport.getText().toString().trim();
				String name = userName.getText().toString().trim();
				String pwd = userPass.getText().toString().trim();
				if (ip.equals("")) {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.null_server));
				} else if (port.equals("")) {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.port_null));
				}
				// 匿名登录
				else if (loginCheck.isChecked()) {
					nickName.setText(newName);
					if (client.openConnect(ip1, "anonymous", "123") == 230) {
						int resu = 0;

						if (!myName.equals(newName)) {
							for (int i = 0; i < list.size(); i++) {
								if (list.get(i).get("conName").equals(newName)) {
									resu = 1;
									break;
								}
							}
						}

						if (resu == 1) {
							FileUtil.showToast(FTPActivity.this,
									FTPActivity.this
											.getString(R.string.nick_exist));
						} else {
							ContentValues values = new ContentValues();
							values.put("nick", newName);
							values.put("ip", ip);
							values.put("port", port);
							values.put("name", "anonymous");
							values.put("pwd", "123");
							values.put("flag", 1);
							if (controlFlag == 1) {
								cursor = sqlite.query("ftp", new String[] {
										"_id", "ip", "name", "pwd", "nick" },
										"nick=?", new String[] { nickname },
										null, null, null);
								if (cursor.moveToNext()) {
									int id = cursor.getInt(cursor
											.getColumnIndex("_id"));
									sqlite
											.update("ftp", values, "_id=?",
													new String[] { String
															.valueOf(id) });
								}
							} else {
								sqlite.insert("ftp", "_id", values);
							}
							dialog.dismiss();
						}
					} else {
						FileUtil.showToast(FTPActivity.this, FTPActivity.this
								.getString(R.string.login_fail));
					}
					updateData();
				} else {
					if (!name.equals("") && !pwd.equals("")) {
						nickName.setText(newName);
						if (client.openConnect(ip1, name, pwd) == 230) {
							int resu = 0;
							if (!myName.equals(newName)) {
								for (int i = 0; i < list.size(); i++) {
									if (list.get(i).get("conName").equals(
											newName)) {
										resu = 1;
										break;
									}
								}
							}
							// 判断昵称是否存在
							if (resu == 1) {
								FileUtil.showToast(FTPActivity.this,
										getString(R.string.nick_exist));
							} else {
								ContentValues values = new ContentValues();
								values.put("nick", newName);
								values.put("ip", ip);
								values.put("port", port);
								values.put("name", name);
								values.put("pwd", pwd);
								values.put("flag", 0);
								if (controlFlag == 1) {
									cursor = sqlite.query("ftp",
											new String[] { "_id", "ip", "name",
													"pwd", "nick" }, "nick=?",
											new String[] { nickname }, null,
											null, null);
									if (cursor.moveToNext()) {
										int id = cursor.getInt(cursor
												.getColumnIndex("_id"));
										sqlite.update("ftp", values, "_id=?",
												new String[] { String
														.valueOf(id) });
									}
								} else {
									sqlite.insert("ftp", "_id", values);
								}
								dialog.cancel();
							}
						} else {
							FileUtil.showToast(FTPActivity.this,
									FTPActivity.this
											.getString(R.string.login_fail));
						}
						cursor.close();
						updateData();
					} else {
						FileUtil.showToast(FTPActivity.this, FTPActivity.this
								.getString(R.string.null_user));
					}
				}
			} else if (v.equals(myCancelBut)) {
				dialog.cancel();
			} else if (v.equals(okBut)) {
				if (nameList.size() != 0) {
					alertDialog.dismiss();
					// 下载操作
					if (operation == 6) {
						if (Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							file = new File(FTP_PATH);
							if (!file.exists()) {
								file.mkdirs();
							}

							// 判断要下载文件在文件夹中是否存在
							final StringBuilder str = new StringBuilder();
							final ArrayList<String> strList = new ArrayList<String>();
							for (int i = 0; i < nameList.size(); i++) {
								for (int j = 0; j < file.listFiles().length; j++) {
									if (nameList.get(i).equals(
											file.listFiles()[j].getName())) {
										strList.add(nameList.get(i));
									}
								}
							}

							// 以,分隔文件名
							for (int i = 0; i < strList.size(); i++) {
								if (i == strList.size() - 1) {
									str.append(strList.get(i));
								} else {
									str.append(strList.get(i)).append(",");
								}
							}

							new AlertDialog.Builder(FTPActivity.this)
									.setMessage(
											getString(R.string.s_down, nameList
													.size()))
									.setPositiveButton(
											FTPActivity.this.getResources()
													.getString(R.string.ok),
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
//													long i = 0;
//													for (Long in : sizeList) {
//														i += in;
//													}
													// int code =
													// FileUtil.getSdcardSpace(i,
													// "/mnt/ftp");
													// if (code == 1) {
													// proDialog = new
													// ProgressDialog(
													// FTPActivity.this);
													// proDialog.setMessage(getResources()
													// .getString(R.string.f_downing));
													// if (strList.size() > 0) {
													// new AlertDialog.Builder(
													// FTPActivity.this)
													// .setMessage(
													// getString(
													// R.string.override_file,
													// str.toString()))
													// .setPositiveButton(
													// FTPActivity.this
													// .getResources()
													// .getString(
													// R.string.ok),
													// new
													// DialogInterface.OnClickListener()
													// {
													// public void onClick(
													// DialogInterface dialog,
													// int which) {
													// getNameString();
													// proDialog
													// .show();
													// DownF downF = new
													// DownF();
													// downF.start();
													// }
													// })
													// .setNegativeButton(
													// FTPActivity.this
													// .getResources()
													// .getString(
													// R.string.cancel),
													// new
													// DialogInterface.OnClickListener()
													// {
													// public void onClick(
													// DialogInterface dialog,
													// int which) {
													// for (String str :
													// strList) {
													// nameList.remove(str);
													// }
													// getNameString();
													// if (nameList
													// .size() > 0) {
													// proDialog
													// .show();
													// DownF downF = new
													// DownF();
													// downF.start();
													// }
													//
													// }
													// }).show();
													// } else {
													// getNameString();
													// proDialog.show();
													// DownF downF = new
													// DownF();
													// downF.start();
													// }
													// } else if (code == 0) {
													// FileUtil.showToast(
													// FTPActivity.this,
													// FTPActivity.this
													// .getString(R.string.sdcard_no_capacity));
													// } else {
													// FileUtil.showToast(
													// FTPActivity.this,
													// FTPActivity.this
													// .getString(R.string.sdcard_dele));
													// }
												}
											})
									.setNegativeButton(
											FTPActivity.this.getResources()
													.getString(R.string.cancel),
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
												}
											}).show();
						} else {
							FileUtil.showToast(FTPActivity.this,
									FTPActivity.this
											.getString(R.string.sdcard_dele));
						}
					} else {
						// 删除文件操作
						getNameString();
						new AlertDialog.Builder(FTPActivity.this).setMessage(
								getString(R.string.s_delete, nameList.size()))
								.setPositiveButton(
										FTPActivity.this.getResources()
												.getString(R.string.ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												proDialog = new ProgressDialog(
														FTPActivity.this);
												proDialog
														.setMessage(getResources()
																.getString(
																		R.string.f_deleting));
												proDialog.show();
												DeleF deleF = new DeleF();
												deleF.start();

											}
										}).setNegativeButton(
										FTPActivity.this.getResources()
												.getString(R.string.cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										}).show();

					}
				} else {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.choice_file));
				}
			} else if (v.equals(cancleBut)) {
				alertDialog.cancel();
			}
		}
	};

	/**
	 * 更新服务器连接列表
	 */
	public void updateData() {
		list.clear();
		cursor = sqlite.query("ftp", new String[] { "_id", "ip", "port",
				"name", "pwd", "nick" }, null, null, null, null, null);
		while (cursor.moveToNext()) {
			Map<String, Object> map = null;
			map = new HashMap<String, Object>();
			map.put("conName", cursor.getString(cursor.getColumnIndex("nick")));
			map.put("conImg", R.drawable.mainfile);
			list.add(map);
		}
		// if (list.size() > 0) {
		// layout.setVisibility(View.VISIBLE);
		// } else {
		// layout.setVisibility(View.GONE);
		// }
		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.file_row, new String[] { "conImg", "conName" },
				new int[] { R.id.image_Icon, R.id.text });
		layoutGrid.setAdapter(adapter);
		layoutGrid.setOnItemClickListener(onclick);
		cursor.close();
	}

	/**
	 * 格式化文件名 文件名空格使用\ 代替
	 */
	public void getNameString() {
		StringBuilder build = new StringBuilder();
		for (int i = 0; i < nameList.size(); i++) {
			String name = nameList.get(i);
			if (name.contains(" ")) {
				name = name.replace(" ", "\\ ");
			}
			if (i == nameList.size() - 1) {
				build.append(name);
			} else {
				build.append(name);
				build.append(",");
			}
		}
		nameArray = build.toString();
	}

	/**
	 * 更新文件列表
	 */
	public void getList() {
		sortBut.setOnClickListener(clickListener);
		showBut.setOnClickListener(clickListener);
		filterBut.setOnClickListener(clickListener);
		getSortList();
		if (fileList.size() > 0) {
			FtpFileAdapter adapter = new FtpFileAdapter(this, fileList,
					R.layout.file_row);
			layoutGrid.setAdapter(adapter);
			layoutGrid.setOnItemClickListener(onclick);
			adapter = new FtpFileAdapter(this, fileList, R.layout.gridfile_row);
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(onclick);
		} else {
			// sortMethod.requestFocus();
			FtpFileAdapter adapter = new FtpFileAdapter(this, fileList,
					R.layout.file_row);
			layoutGrid.setAdapter(adapter);
			adapter = new FtpFileAdapter(this, fileList, R.layout.gridfile_row);
			gridView.setAdapter(adapter);
		}
	}

	/**
	 * 列表点击事件
	 */
	private OnItemClickListener itemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> l, View v, final int position,
				long id) {
			myCheck = (CheckedTextView) v.findViewById(R.id.check);
			if (myCheck.isChecked()) {
				nameList.remove(fileNameList.get(position).split("\\|")[1]
						.trim());
				sizeList.remove(Long.parseLong(fileNameList.get(position)
						.split("\\|")[3]));
				myCheck.setChecked(false);
			} else {
				nameList.add(fileNameList.get(position).split("\\|")[1].trim());
				sizeList.add(Long.parseLong(fileNameList.get(position).split(
						"\\|")[3]));
				myCheck.setChecked(true);
			}
		}
	};

	/**
	 * 下载、删除文件
	 * 
	 * @param resultArray
	 *            操作的文件列表
	 * @param operation
	 *            操作ID
	 */
	private void operation(String[] resultArray, final int operation) {
		this.operation = operation;
		int position = -1;
		if (layoutGrid.getVisibility() == View.VISIBLE) {
			position = layoutGrid.getSelectedItemPosition();
		} else if (gridView.getVisibility() == View.VISIBLE) {
			position = gridView.getSelectedItemPosition();
		}
		LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View) in.inflate(R.layout.dele, null);
		final ListView list = (ListView) view.findViewById(R.id.list);
		okBut = (Button) view.findViewById(R.id.okBut);
		okBut.setOnClickListener(myOnClickListener);
		cancleBut = (Button) view.findViewById(R.id.cancleBut);
		cancleBut.setOnClickListener(myOnClickListener);
		nameList.clear();
		fileNameList.clear();
		sizeList.clear();
		for (String re : fileList) {
			String[] array = re.split("\\|");
			if (array[0] != "" && array[1] != "") {
				if (array[1].contains("\n")) {
					array[1] = array[1].substring(0, array[1].indexOf("\n"))
							+ array[1].substring(array[1].indexOf("\n") + 1,
									array[1].length());
				}
				if (array[0].equals("f")) {
					fileNameList.add(re);
				}
			}
		}
		if (fileNameList.size() > 0) {
			// 设置多选
			list.setItemsCanFocus(false);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			list.setAdapter(new ControlFtpAdapter(this, fileNameList,
					R.layout.control_row));
			if (position >= arrayDir.size()) {
				int pos = position - arrayDir.size();
				if (pos >= 0) {
					list.setItemChecked(pos, true);
					list.setSelection(pos);
					nameList.add(fileNameList.get(position - arrayDir.size())
							.split("\\|")[1].trim());
					sizeList.add(Long.parseLong(fileNameList.get(
							position - arrayDir.size()).split("\\|")[3]));
				}
			}
			list.setOnItemClickListener(itemClick);
			list.clearFocus();
			okBut.requestFocus();
			alertDialog = new AlertDialog.Builder(FTPActivity.this).setView(
					view).create();
			alertDialog.show();
		} else {
			FileUtil.showToast(FTPActivity.this, FTPActivity.this
					.getString(R.string.no_downfile));
		}
	}

	/**
	 * 重命名文件
	 * 
	 * @param oName
	 *            原文件名
	 * @param type
	 *            文件类型(目录、文件)
	 */
	public void reNameFile(final String oName, final String type) {
		final String end;
		LayoutInflater factory = LayoutInflater.from(this);
		View myView = factory.inflate(R.layout.rename_alert, null);
		final EditText myEditText = (EditText) myView
				.findViewById(R.id.rename_edit);

		myEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				tempLength = s.length();
				Num = start;
			}

			/**
			 * 控制输入长度及内容
			 */
			public void afterTextChanged(Editable s) {
				if (s.length() > tempLength) {
					if (s.charAt(Num) == '/' || s.charAt(Num) == '\\'
							|| s.charAt(Num) == ':' || s.charAt(Num) == '*'
							|| s.charAt(Num) == '?' || s.charAt(Num) == '\"'
							|| s.charAt(Num) == '<' || s.charAt(Num) == '>'
							|| s.charAt(Num) == '|') {
						s.delete(Num, Num + 1);
						FileUtil.showToast(FTPActivity.this, FTPActivity.this
								.getString(R.string.name_falid));
					} else if (s.length() > 128) {
						s.delete(Num, Num + 1);
						FileUtil.showToast(FTPActivity.this, FTPActivity.this
								.getString(R.string.name_long));
					}
				}
			}
		});

		// 获得文件后缀
		if (oName.lastIndexOf(".") != -1) {
			end = oName.trim().substring(oName.trim().lastIndexOf(".") + 1,
					oName.trim().length()).toLowerCase();
			myEditText.setText(oName.trim().substring(0,
					oName.trim().length() - end.length() - 1));
		} else {
			end = "";
			myEditText.setText(oName.trim());
		}
		new AlertDialog.Builder(this).setView(myView).setPositiveButton(
				this.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						getProperty();
						String oldName = oName.trim();
						if (oldName.contains(" ")) {
							oldName = oldName.replace(" ", "\\ ");
						}
						String name = myEditText.getText().toString().trim();
						if (name.contains(" ")) {
							name = name.replace(" ", "\\ ");
						}
						String newName = "";
						if (type.equals("d")) {
							newName = name;
						} else {
							newName = name + "." + end;
						}
						// 执行重命名操作
						int resultCode = client.renameFile(sIp, sName, sPwd,
								titlePath.getText().toString(), oldName,
								newName);
						if (resultCode == 250) {
							FileUtil
									.showToast(
											FTPActivity.this,
											FTPActivity.this
													.getString(R.string.update_name_ok));
							result = client.result;
							getList();
						} else {
							FileUtil
									.showToast(
											FTPActivity.this,
											FTPActivity.this
													.getString(R.string.update_name_error));
						}
					}
				}).setNegativeButton(
				this.getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	/**
	 * 接收消息
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message mes) {
			proDialog.dismiss();
			// 连接操作
			if (mes.what == 1) {
				if (resultCode == 230) {
					rootDir = client.path;
					result = client.result;
					titlePath.setText(rootDir);
					// filterSpinner.setVisibility(View.VISIBLE);
					// showSpinner.setVisibility(View.VISIBLE);
					// sortMethod.setVisibility(View.VISIBLE);
				}
				getList();

			}
			// 删除文件
			else if (mes.what == 2) {
				if (resultCode == 250) {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.delete_v));
					result = client.result;
					getList();
				} else {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.delete_error));
				}
			}
			// 删除文件夹
			else if (mes.what == 3) {
				if (resultCode == 250) {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.delete_v));
					result = client.result;
					getList();
				} else {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.delete_error));
				}
			}
			// 下载文件
			else if (mes.what == 4) {
				if (resultCode == 226) {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.download_succ));
				} else {
					FileUtil.showToast(FTPActivity.this, FTPActivity.this
							.getString(R.string.download_fail));
				}
			}
		}
	};

	/**
	 * 连接服务器
	 * 
	 * @author qian_wei
	 */
	class Conn extends Thread {
		public void run() {
			try {
				sleep(2000);
				Looper.prepare();
				getProperty();
				resultCode = client.openConnect(sIp, sName, sPwd);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(1);
		}
	};

	/**
	 * 删除文件
	 * 
	 * @author qian_wei
	 */
	class DeleF extends Thread {
		public void run() {
			try {
				sleep(2000);
				Looper.prepare();
				getProperty();
				resultCode = client.deleteFile(sIp, sName, sPwd, titlePath
						.getText().toString(), nameArray);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(2);
		}
	};

	/**
	 * 删除文件夹
	 * 
	 * @author qian_wei
	 */
	class DeleD extends Thread {
		public void run() {
			try {
				sleep(2000);
				Looper.prepare();
				getProperty();
				int position = -1;
				// 获取文件在列表中位置
				if (layoutGrid.getVisibility() == View.VISIBLE) {
					position = layoutGrid.getSelectedItemPosition();
				} else if (gridView.getVisibility() == View.VISIBLE) {
					position = gridView.getSelectedItemPosition();
				}
				String remoteDirName = fileList.get(position).split("\\|")[1];
				if (remoteDirName.contains("\n")) {
					remoteDirName = remoteDirName.substring(0, remoteDirName
							.indexOf("\n"))
							+ remoteDirName.substring(remoteDirName
									.indexOf("\n") + 1, remoteDirName.length());
				}
				resultCode = client.delRemoteDir(sIp, sName, sPwd, titlePath
						.getText().toString(), remoteDirName);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(3);
		}
	};

	/**
	 * 下载文件
	 * 
	 * @author qian_wei
	 */
	class DownF extends Thread {
		public void run() {
			try {
				sleep(2000);
				Looper.prepare();
				getProperty();
				resultCode = client.getFile(sIp, sName, sPwd, titlePath
						.getText().toString(), file.getPath(), nameArray);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(4);
		}
	}

	/**
	 * KeyDown事件
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		// 添加对enter键和dpad_center键支持
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			super.onKeyDown(KeyEvent.KEYCODE_ENTER, event);
			return true;

		case 4:// KeyEvent.KEYCODE_BACK
			String iPath = getTitlePath();
			if (rootDir != null) {
				parentPosition++;
				if (rootDir.equals("")) {
					clickCount++;
					if (clickCount == 1) {
						FileUtil.showToast(FTPActivity.this,
								getString(R.string.quit_app));
					} else if (clickCount == 2) {
						onBackPressed();
					}
				} else {
					if (getTitlePath().equals(rootDir)) {
						rootDir = "";
						titlePath.setText(rootDir);
						showBut.setOnClickListener(null);
						sortBut.setOnClickListener(null);
						filterBut.setOnClickListener(null);
						gridView.setVisibility(View.INVISIBLE);
						layoutGrid.setVisibility(View.VISIBLE);
						updateData();
					} else {
						String[] array = iPath.split("/");
						String pa = iPath.substring(0, iPath.length()
								- array[array.length - 1].length() - 1);
						getProperty();
						// run return previous directory
						client.changeRemoteDir(sIp, sName, sPwd, pa);
						// 获得当前目录下的文件列表
						result = client.result;
						// 更新显示
						getList();
						titlePath.setText(pa);
					}
				}
				if (intList.size() >= parentPosition) {
					int pos = intList.size() - parentPosition;
					if (layoutGrid.getVisibility() == View.VISIBLE) {
						layoutGrid.requestFocus();
						layoutGrid.setSelection(intList.get(pos));
						intList.remove(pos);
						parentPosition--;
					} else if (gridView.getVisibility() == View.VISIBLE) {
						gridView.requestFocus();
						gridView.setSelection(intList.get(pos));
						intList.remove(pos);
						parentPosition--;
					}
				}
			} else {
				clickCount++;
				if (clickCount == 1) {
					FileUtil.showToast(FTPActivity.this,
							getString(R.string.quit_app));
				} else if (clickCount == 2) {
					onBackPressed();
				}
			}
			return true;
		}
		return false;
	}

	// /**
	// * 下拉框选择事件
	// */
	// private OnItemSelectedListener itemListener = new
	// OnItemSelectedListener() {
	// public void onItemSelected(AdapterView<?> parent, View view, int
	// position, long id) {
	// if (showSpinner.equals(parent)) {
	// if (position == 0) {
	// gridView.setVisibility(View.INVISIBLE);
	// layoutGrid.setVisibility(View.VISIBLE);
	//
	// } else if (position == 1) {
	// gridView.setVisibility(View.VISIBLE);
	// layoutGrid.setVisibility(View.INVISIBLE);
	// }
	// }
	// getList();
	// }
	//
	// public void onNothingSelected(AdapterView<?> arg0) {
	//
	// }
	// };

	/**
	 * 获取过滤后的文件列表
	 */
	public void getDataList() {
		int filter = filterCount;
		if (filter == 0) {
			resultList.clear();
			for (String re : result) {
				resultList.add(re);
			}
		}
		// 图片过滤
		else if (filter == 1) {
			resultList.clear();
			for (String re : result) {
				if (re.startsWith("f")) {
					String type = FileUtil.getMIMEType(re.split("\\|")[1]
							.trim(), this);
					if (type.contains("image")) {
						resultList.add(re);
					}
				}
			}
		}
		// 音频过滤
		else if (filter == 2) {
			resultList.clear();
			for (String re : result) {
				if (re.startsWith("f")) {
					String type = FileUtil.getMIMEType(re.split("\\|")[1]
							.trim(), this);
					if (type.contains("audio")) {
						resultList.add(re);
					}
				}
			}
		}
		// 视频过滤
		else if (filter == 3) {
			resultList.clear();
			for (String re : result) {
				if (re.startsWith("f")) {
					String type = FileUtil.getMIMEType(re.split("\\|")[1]
							.trim(), this);
					if (type.contains("video")) {
						resultList.add(re);
					}
				}
			}
		}
	}

	/**
	 * 获取排序列表
	 */
	public void getSortList() {
//		int sort = sortCount;
//		getDataList();
//		arrayFile = new ArrayList<String>();
//		arrayDir = new ArrayList<String>();
//		for (String str : resultList) {
//			if (str.startsWith("d")) {
//				arrayDir.add(str);
//			} else {
//				arrayFile.add(str);
//			}
//		}
//		fileList.clear();
//		fileList = FileUtil.sortFile(arrayFile, arrayDir, getResources()
//				.getStringArray(R.array.sort_method)[sort], this);
	}

	/**
	 * 关闭数据库
	 */
	protected void onStop() {
		super.onStop();
		if (cursor != null) {
			cursor.close();
			sqlite.close();
		}
	}

	// 获得昵称
	public String getNickname() {
		return nickname;
	}

	// 获得服务器文件路径
	public String getTitlePath() {
		return titlePath.getText().toString();
	}

	// 根据昵称查询出对应的服务器配置信息
	public void getProperty() {
		cursor = sqlite.query("ftp", new String[] { "_id", "ip", "port",
				"name", "pwd", "nick" }, "nick=?", new String[] { nickname },
				null, null, null);
		while (cursor.moveToNext()) {
			sIp = cursor.getString(cursor.getColumnIndex("ip"));
			sName = cursor.getString(cursor.getColumnIndex("name"));
			sPwd = cursor.getString(cursor.getColumnIndex("pwd"));
		}
		cursor.close();
	}

	public void listValues() {
		if (FileMenu.resultCode == 150) {
			getProperty();
			// 进入下级目录
			int resultCode = client.changeRemoteDir(sIp, sName, sPwd, titlePath
					.getText().toString());
			if (resultCode == 250) {
				clickCount = 0;
				result = client.result;
				getList();
				FileMenu.resultCode = 0;
			}
		}
	}

	protected void onResume() {
		listValues();
		super.onResume();
	}

	public ListView getIconList() {
		return layoutGrid;
	}

//	int showCount = 0;
//
//	int sortCount = 0;
//
//	int filterCount = 0;
//
//	OnClickListener clickListener = new OnClickListener() {
//		public void onClick(View v) {
//			if (v.equals(showBut)) {
//				if (showCount == showArray.length - 1) {
//					showCount = 0;
//				} else {
//					showCount++;
//				}
//				showBut.setImageResource(showArray[showCount]);
//
//				if (showCount == 0) {
//					gridView.setVisibility(View.INVISIBLE);
//					layoutGrid.setVisibility(View.VISIBLE);
//
//				}
//				// 缩略图方式
//				else if (showCount == 1) {
//					gridView.setVisibility(View.VISIBLE);
//					layoutGrid.setVisibility(View.INVISIBLE);
//				}
//			} else if (v.equals(sortBut)) {
//				if (sortCount == sortArray.length - 1) {
//					sortCount = 0;
//				} else {
//					sortCount++;
//				}
//				sortBut.setImageResource(sortArray[sortCount]);
//			} else if (v.equals(filterBut)) {
//				if (filterCount == filterArray.length - 1) {
//					filterCount = 0;
//				} else {
//					filterCount++;
//				}
//				filterBut.setImageResource(filterArray[filterCount]);
//			}
//			getList();
//		}
//	};

	public void fill(File file) {
	}

	public Handler getHandler() {
		return null;
	}

	public void updateList(boolean flag) {
	}

	public void operateSearch(boolean b) {
	}

	public void getFiles(String path) {

	}

}
