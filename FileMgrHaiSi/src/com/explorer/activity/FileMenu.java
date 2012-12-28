package com.explorer.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import android.app.Activity;
import android.os.Bundle;
import android.net.Uri;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;


import com.explorer.R;
import com.explorer.common.CommonActivity;
import com.explorer.common.FileUtil;
import com.explorer.common.NewCreateDialog;
import com.explorer.common.RemoveFilterAdapter;

/**
 * 菜单操作
 * 
 * @author qian_wei 实现菜单具体操作
 */


public class FileMenu {

    private Context mContext;
    // 当前页面视图
	private View myView;

	// 输入框
	private static EditText myEditText;

	// 复制操作
	protected final static int MENU_COPY = Menu.FIRST + 2;

	// 剪切操作
	protected final static int MENU_CUT = Menu.FIRST + 3;

	// 粘贴操作
	protected final static int MENU_PASTE = Menu.FIRST + 4;

	// 删除操作
	protected final static int MENU_DELETE = Menu.FIRST + 5;

	// 重命名操作
	protected final static int MENU_RENAME = Menu.FIRST + 6;

	// 添加过滤音频
	protected final static int ADD_MENU_AUDIO = Menu.FIRST + 7;

	// 添加过滤视频
	protected final static int ADD_MENU_VIDEO = Menu.FIRST + 8;

	// 添加过滤图片
	protected final static int ADD_MENU_IMAGE = Menu.FIRST + 9;

	// 删除过滤音频
	protected final static int REMOVE_MENU_AUDIO = Menu.FIRST + 10;

	// 删除过滤视频
	protected final static int REMOVE_MENU_VIDEO = Menu.FIRST + 11;

	// 删除过滤图片
	protected final static int REMOVE_MENU_IMAGE = Menu.FIRST + 12;

	// 帮助
	protected final static int MENU_HELP = Menu.FIRST + 13;

	// // 上传文件
	// protected final static int MENU_UPLOAD = Menu.FIRST + 14;

	// 复制文件
	private static final String COPYFILE = "COPYFILE";

	// 复制文件夹
	private static final String COPYDIR = "COPYDIR";

	// 剪切文件
	private static final String CUTFILE = "CUTFILE";

	// 剪切文件夹
	private static final String CUTDIR = "CUTDIR";

	// 选中的文件集合
	public List<File> file;

	// 已存在的文件列表
	public List<Map<String, Object>> existFile;

	// 选中的文件路径
	public static String selected;

	// 关联页面
	public static CommonActivity MA;

	// 粘贴标识符
	private int flag = 0;

	// 新文件路径
	private String newFilePath;

	// 当前操作文件
	private File mySelFile;

	// 进程提示框
	ProgressDialog proDialog;

	// SHARE文件
	private static SharedPreferences sp;

	// // 数据库公共类
	// private static DBHelper dbHelper;
	//
	// // 数据库操作对象
	// private static SQLiteDatabase sqlite;

	// 过滤类型集合
	static List<String> li;

	// 删除的过滤类型集合
	static List<String> nameList;

	// 文件长度
	long fileLenght = 0;

	// 上传结果吗
	static int resultCode = 0;

	// 服务器地址
	// String sIp = null;

	// 账号
	// String sName = null;

	// 密码
	// String sPwd = null;

	// ftp客户端
	// FtpClient client;

	// 上传的文件集合名
	// String fileName = "";

	// 操作Dialog
	static AlertDialog alertDialog = null;

	// 确定按钮
	// static Button okBut;

	// 取消按钮
	// static Button cancleBut;

	// 粘贴操作结果
	// int result = -2;

	// 粘贴源文件
	// File oFile;

	// 粘贴目的文件
	// File nFile;

	// 原文件、目标文件集合
	List<Map<String, File>> listMap;

	// 原文件key
	private static String oTag = "SourceFile";

	// 目标文件key
	private static String nTag = "NewFile";

	List<File> dirList = null;

	int cutFile = 0;

	public static int helpFlag = 0;

	private int pasteFlag = 0;

	private SharedPreferences.Editor editor;
	private static boolean showFlag = true;
	
	
	private List<Map<String, String>> moveSameDirectory;
    
    
    /*public String pasteFilePath;

    public String oldFilePath;

    public String renameFilePath;

    public String deleteFilePath;*/
    
    /*public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        IntentFilter intentFilter_ScanFinish = new IntentFilter();
        intentFilter_ScanFinish.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter_ScanFinish.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter_ScanFinish.addDataScheme("file");
        BroadcastReceiver mReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                Log.e("tangxiaodi","recive--------------------action=" + intent.getAction());
            }
        };
        registerReceiver(mReceiver, intentFilter_ScanFinish);
    }*/
	/**
	 * 添加过滤条件
	 * 
	 * @param ctx 关联页面
	 * @param item 操作
	 * @param sps 对应的配置文件
	 */
	public static void filterType(CommonActivity ctx, final int item,
			SharedPreferences sps) {
		MA = ctx;
		sp = sps;
		switch (item) {
		// 添加音频条件
		case ADD_MENU_AUDIO:
			addFilterDialog(sps, 0);
			break;
		// 添加视频条件
		case ADD_MENU_VIDEO:
			addFilterDialog(sps, 1);
			break;
		// 添加图片条件
		case ADD_MENU_IMAGE:
			addFilterDialog(sps, 2);
			break;
		// 删除音频条件
		case REMOVE_MENU_AUDIO:
			removeFilterDialog(sps);
			break;
		// 删除视频条件
		case REMOVE_MENU_VIDEO:
			removeFilterDialog(sps);
			break;
		// 删除图片条件
		case REMOVE_MENU_IMAGE:
			removeFilterDialog(sps);
			break;
		// 帮助
		case MENU_HELP:
			showHelp();
			break;
		}
	}

	/**
	 * 操作文件的Dialog
	 * 
	 * @param ctx 关联页面
	 * @param selFile 当前操作文件
	 * @param selectedfile 操作的文件列表
	 * @param sps 剪贴板文件
	 * @param which 操作
	 */
	int index = 0;

	public void getTaskMenuDialog(CommonActivity ctx, final File selFile,
			List<File> selectedfile, SharedPreferences sps, final int which,
			int pasteFlag) {
        MA = ctx;
        mContext = ctx;
		this.pasteFlag = pasteFlag;
		// client = new FtpClient();
		file = selectedfile;
		mySelFile = selFile;
        

        Log.v("log", "getTaskMenuDialog header");
		sp = sps;
		switch (which) {
		case MENU_COPY: {
			MA.isFileCut = false;
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.commit();
			editor.putString("operate", "copy");
			for (int i = 0; i < file.size(); i++) {
				// 复制文件夹
				if (file.get(i).isDirectory()) {
					editor.putString(COPYDIR + i, file.get(i).getPath());
					editor.putString(COPYFILE, null);
					editor.putString(CUTFILE, null);
					editor.putString(CUTDIR, null);
					editor.putInt("NUM", file.size());
					Log.v("log", file.get(i).getPath());
				}
				// 复制文件
				else {
					editor.putString(COPYFILE + i, file.get(i).getPath());
					editor.putString(COPYDIR, null);
					editor.putString(CUTFILE, null);
					editor.putString(CUTDIR, null);
					editor.putInt("NUM", file.size());
				}
			}
			editor.commit();
			return;
		}
			// 剪切操作
		case MENU_CUT: {
			MA.isFileCut = true;
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.commit();
			editor.putString("operate", "cut");
			for (int i = 0; i < file.size(); i++) {
				// 剪切文件夹
				if (file.get(i).isDirectory()) {
					editor.putString(CUTDIR + i, file.get(i).getPath());
					editor.putString(COPYFILE, null);
					editor.putString(CUTFILE, null);
					editor.putString(COPYDIR, null);
					editor.putInt("NUM", file.size());
				}
				// 剪切文件
				else {
					editor.putString(CUTFILE + i, file.get(i).getPath());
					editor.putString(COPYFILE, null);
					editor.putString(COPYDIR, null);
					editor.putString(CUTDIR, null);
					editor.putInt("NUM", file.size());
				}
			}
			editor.commit();
			return;
		}
			// 粘贴操作
		case MENU_PASTE: {
			try {
				listMap = new ArrayList<Map<String, File>>();
				existFile = new ArrayList<Map<String, Object>>();
				nExistFile = new ArrayList<Map<String, Object>>();
				fileList = new ArrayList<Map<String, Object>>();
				moveSameDirectory = new ArrayList<Map<String, String>>();
				if (sp != null) {
					for (int i = 0; i < sp.getInt("NUM", 1); i++) {
						editor = sp.edit();
						if (sp.getString(COPYFILE + i, null) != null) {
							doPaste(sp, COPYFILE, "file", i);
						} else if (sp.getString(COPYDIR + i, null) != null) {
							doPaste(sp, COPYDIR, "dir", i);
						} else if (sp.getString(CUTFILE + i, null) != null) {
							doPaste(sp, CUTFILE, "file", i);
						} else if (sp.getString(CUTDIR + i, null) != null) {
							doPaste(sp, CUTDIR, "dir", i);
						}
					}
				}

				Log.w("TAG = TAG", " = " + pasteFlag);
				// 父目录向子目录中粘贴
				if (flag == -2) {
					FileUtil.showToast(MA, MA.getString(R.string.sub_dir));
					MA.updateList(false);
				} else if (pasteFlag == 1
						&& FileUtil.getSdcardSpace(fileLenght, MA.mountSdPath)) {
					FileUtil.showToast(MA, MA
							.getString(R.string.sdcard_no_capacity));
				} else {
					dirList = new ArrayList<File>();
					if (existFile.size() > 0) {
						pasteFiles();
					} else {
						fileList.addAll(nExistFile);
						for (int i = 0; i < fileList.size(); i++) {
							String operate = fileList.get(i).get("operate")
									.toString();
							File file = (File) fileList.get(i).get("file");
							int index = Integer.parseInt(fileList.get(i).get(
									"index").toString());
							// 拷贝文件
							if (operate.equals(COPYFILE)) {
								copyFile(file.getPath(), selFile.getPath());
							}
							// 拷贝文件夹
							else if (operate.equals(COPYDIR)) {
								copyDir(file.getPath(), selFile.getPath());
							}
							// 剪切文件
							else if (operate.equals(CUTFILE)) {
								moveFile(file.getPath(), selFile.getPath());
								editor.putString(CUTFILE + index, null);
								editor.putInt("NUM", sp.getInt("NUM", 0) - 1);
								editor.commit();
							}
							// 剪切文件夹
							else if (operate.equals(CUTDIR)) {
								dirList.add(file);
								moveDir(file.getPath(), selFile.getPath());
								editor.putString(CUTDIR + index, null);
								editor.putInt("NUM", sp.getInt("NUM", 0) - 1);
								editor.commit();
							}
						}
						proDialog = new ProgressDialog(MA);
						proDialog.setMessage(MA.getString(R.string.paste_str));
						proDialog.show();
						CopyThread copyThread = new CopyThread();
						copyThread.start();
					}
				}
				// }
			} catch (Exception e) {
				e.printStackTrace();
				FileUtil.showToast(MA, MA.getString(R.string.paste_error));
			}
			return;
		}
			// 删除操作
		case MENU_DELETE: {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < file.size(); i++) {
				String fi = file.get(i).getName();
				if (i == file.size() - 1) {
					builder.append(fi);
				} else {
					builder.append(fi).append(",");
				}
                
            }
            doDelete(builder.toString());

            
            
			return;
		}
			// 重名名操作
		case MENU_RENAME: {
			modifyFileOrDir(file.get(0));
			return;
		}
			// // 上传操作
			// case MENU_UPLOAD: {
			// proDialog = new ProgressDialog(MA);
			// proDialog.setMessage(MA.getString(R.string.file_up));
			// proDialog.show();
			// PutF putF = new PutF();
			// putF.start();
			// return;
			// }
		}
	}
    
	private void doDelete(String str) {
		new AlertDialog.Builder(MA).setTitle(MA.getString(R.string.notice))
				.setIcon(R.drawable.alert).setMessage(
						MA.getString(R.string.ok_delete, str))
				.setPositiveButton(MA.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								proDialog = new ProgressDialog(MA);
								proDialog.show();
								DelThread delThread = new DelThread();
								delThread.start();
							}
						}).setNegativeButton(MA.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create().show();
	}

	private boolean delFlag = true;

	class DelThread extends Thread {
		public void run() {

			// 执行删除操作
			for (int i = 0; i < file.size(); i++)
            {
				if (file.get(i).isDirectory())
                {
					delFlag = delDir(file.get(i));
				} else {
					delFlag = delFile(file.get(i));
				}
				Log.v("log", file.get(i).getPath());
			}
			MA.operateSearch(delFlag);
			handler.sendEmptyMessage(3);
		}
	};

	/**
	 * 修改文件名或者文件夹名
	 * 
	 * @param f 文件或者文件夹
	 */
	private void modifyFileOrDir(File f) {
		final File f_old = f;
		LayoutInflater factory = LayoutInflater.from(MA);
		// 获得对话框布局
		myView = factory.inflate(R.layout.rename_alert, null);
		myEditText = (EditText) myView.findViewById(R.id.rename_edit);
		myEditText.setText(f_old.getName());
		showFlag = true;
		myEditText.addTextChangedListener(splitWatcher);
		OnClickListener listenerFileEdit = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 取得修改的文件名
				final String modName = myEditText.getText().toString();
				// 取得该文件路径
				final String pFile = f_old.getParentFile().getPath() + "/";
				// 新的文件路径+文件名
				final String newPath = pFile + modName;
				Log.w("TAG", " = "+newPath);
				final File f_new = new File(newPath);
				if (f_new.exists()) {
					// 重名提示信息
					if (!modName.equals(f_old.getName())) {
						if (modName.equals("")) {
							FileUtil.showToast(MA, MA.getString(
									R.string.Rename_null, modName));
						} else {
							FileUtil.showToast(MA, MA.getString(
									R.string.your_file, modName));
						}

					} else {
						FileUtil.showToast(MA, MA
								.getString(R.string.name_no_update));
					}
				} else {
					doRename(f_old, f_new, modName);
                    
				}
			};
		};
		AlertDialog renameDialog = new AlertDialog.Builder(MA).create();
		renameDialog.setView(myView);
		renameDialog.setButton(AlertDialog.BUTTON_POSITIVE, MA
				.getString(R.string.ok), listenerFileEdit);
		renameDialog.setButton(AlertDialog.BUTTON_NEGATIVE, MA
				.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		renameDialog.show();
	}

	private void doRename(final File f_old, final File f_new, String modName) {
		String str = (f_old.isDirectory() ? MA.getResources().getString(
				R.string.dir) : MA.getResources().getString(R.string.file));
		// 更改确认信息
		new AlertDialog.Builder(MA).setTitle(
				MA.getResources().getString(R.string.notice)).setIcon(
				R.drawable.alert)
				.setMessage(
						MA.getString(R.string.ok_update, str, f_old.getName(),
								modName)).setPositiveButton(
						MA.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) 
											{
											if (f_old.renameTo(f_new)) 
                                            {
												MA.updateList(true);
						                    	FileUtil.showToast(MA,MA.getString(R.string.update_name_ok));
                                                if(f_old.getPath().contains("/mnt/nand") || f_old.getPath().contains("/mnt/sd"))
                                                {
                                                    Uri uri = Uri.parse("file://" + f_old.getParent());
                                                    Intent intent = new Intent("MEDIA_SCANNER_DESIGNATED_PATH",uri);
                                                    //mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                                                    intent.putExtra("scan", true);
                                                    mContext.sendBroadcast(intent);
                                                    Log.e("tangxiaodi","rename file-------------f_old="+f_old.getParent()+" f_new="+f_new.getPath());
                                                }
											}
                                            else 
                                            {
												// 修改失败
            									FileUtil.showToast(	MA,MA.getString(R.string.update_name_error));
											}
										}
									}).setNegativeButton(
						MA.getResources().getString(R.string.cancel),
									new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath String 原文件路径 如：/xx
	 * @param newPath String 复制后路径 如：/xx/ss
	 * @return boolean 粘贴是否成功
	 */
	public boolean copyFile(String oldPath, String newPath) {
		try {
			String f_new = "";
			File f_old = new File(oldPath);
			if (newPath.endsWith(File.separator)) {
				f_new = newPath + f_old.getName();
			} else {
				f_new = newPath + File.separator + f_old.getName();
			}
			newFilePath = f_new;
			// begin modify by qian_wei/zhou_yong 2011/10/20
			// for self-cover, the size is 0
			if(!f_new.equals(oldPath)) {
			    Map<String, File> map = new HashMap<String, File>();
			    Log.i("--------O--------", f_old.getPath());
			    Log.i("--------N--------", f_new);
			    map.put(oTag, f_old);
			    map.put(nTag, new File(f_new));
			    listMap.add(map);
			}
			// end modify by qian_wei/zhou_yong 2011/10/20
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void pasteFiles() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MA);
		builder.setTitle(R.string.notice);
		builder.setIcon(R.drawable.alert).setMessage(
				MA.getString(R.string.override_file, ((File) existFile.get(
						index).get("file")).getName()));
		builder.setPositiveButton(MA.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Editor editor = sp.edit();
						int num = sp.getInt("NUM", -1);
						fileList.add(existFile.get(index));
						if (nExistFile.size() > 0) {
							fileList.addAll(nExistFile);
						}
						index++;
						if (index < existFile.size()) {
							pasteFiles();
						} else {
							for (int i = 0; i < fileList.size(); i++) {
								String operate = fileList.get(i).get("operate")
										.toString();
								File file = (File) fileList.get(i).get("file");
								int index = Integer.parseInt(fileList.get(i)
										.get("index").toString());
								if (operate.equals(COPYFILE)) {
									copyFile(file.getPath(), mySelFile
											.getPath());
								} else if (operate.equals(COPYDIR)) {
									copyDir(file.getPath(), mySelFile.getPath());
								} else if (operate.equals(CUTFILE)) {
									moveFile(file.getPath(), mySelFile
											.getPath());
									editor.putString(CUTFILE + index, null);
									editor.putInt("NUM", num - 1);
									editor.commit();
								} else if (operate.equals(CUTDIR)) {
									dirList.add(file);
									moveDir(file.getPath(), mySelFile.getPath());
									editor.putString(CUTDIR + index, null);
									editor.putInt("NUM", num - 1);
									editor.commit();
								}
							}
							proDialog = new ProgressDialog(MA);
							proDialog.setMessage(MA
									.getString(R.string.paste_str));
							proDialog.show();
							CopyThread copyThread = new CopyThread();
							copyThread.start();
						}
					}
				});
		builder.setNegativeButton(MA.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Editor editor = sp.edit();
						int num = sp.getInt("NUM", -1);

						if (nExistFile.size() > 0) {
							fileList.addAll(nExistFile);
						}

						index++;
						if (index < existFile.size()) {
							pasteFiles();
						} else {
							for (int i = 0; i < fileList.size(); i++) {
								String operate = fileList.get(i).get("operate")
										.toString();
								File file = (File) fileList.get(i).get("file");
								int index = Integer.parseInt(fileList.get(i)
										.get("index").toString());
								if (operate.equals(COPYFILE)) {
									copyFile(file.getPath(), mySelFile
											.getPath());
								} else if (operate.equals(COPYDIR)) {
									copyDir(file.getPath(), mySelFile.getPath());
								} else if (operate.equals(CUTFILE)) {
									moveFile(file.getPath(), mySelFile
											.getPath());
									editor.putString(CUTFILE + index, null);
									editor.putInt("NUM", num - 1);
									editor.commit();
								} else if (operate.equals(CUTDIR)) {
									dirList.add(file);
									moveDir(file.getPath(), mySelFile.getPath());
									editor.putString(CUTDIR + index, null);
									editor.putInt("NUM", num - 1);
									editor.commit();
								}
							}
							if (fileList.size() > 0) {
								proDialog = new ProgressDialog(MA);
								proDialog.setMessage(MA
										.getString(R.string.paste_str));
								proDialog.show();
								CopyThread copyThread = new CopyThread();
								copyThread.start();
							}
						}
					}
				}).show();
	}

	/**
	 * 拷贝线程
	 * 
	 * @author qian_wei 实现文件拷贝
	 */
	class CopyThread extends Thread {
		public void run() {

			File f1 = null;
			File f2 = null;
			// 文件流
			FileInputStream in = null;
			FileOutputStream out = null;
			// 每次拷贝大小
			int length = 1024 * 1024;
			// 管道
			FileChannel inc = null;
			FileChannel outc = null;
			try {
			    // begin modify by qian_wei/zhou_yong 2011/10/26
			    // for results inconsistent with the actual shear
				if (listMap.size() == 0) {
				    // 同一挂载目录或者设备中文件剪切
					if(moveSameDirectory.size() > 0) {
					    int resultCode = 0;
						for(int i = 0; i < moveSameDirectory.size(); i++) {
							f1 = new File(moveSameDirectory.get(i).get(oTag));
							f2 = new File(moveSameDirectory.get(i).get(nTag));
//							moveFiles(moveSameDirectory.get(i).get(oTag), moveSameDirectory.get(i).get(nTag));
							resultCode = moveFiles(moveSameDirectory.get(i).get(oTag), moveSameDirectory.get(i).get(nTag));
							if(resultCode == 0) {
							    continue;
							} else {
			                    moveSameDirectory.clear();
							    handler.sendEmptyMessage(2);
							    return;
							}
						}
	                    moveSameDirectory.clear();
						handler.sendEmptyMessage(1);
					} else {
					    // 空文件夹粘贴
						for (File file : dirList) {
							delDir(file);
						}
						handler.sendEmptyMessage(1);
					}
//					handler.sendEmptyMessage(1);
                    if(f2.getPath().contains("/mnt/nand") || f2.getPath().contains("/mnt/sd"))
                    {
                        Uri uri = Uri.parse("file://" + f1.getParent());
                        Intent intent = new Intent("MEDIA_SCANNER_DESIGNATED_PATH",uri);
                        //mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                        intent.putExtra("scan", true);
                        if(mContext != null)
                            mContext.sendBroadcast(intent);
                        Log.e("tangxiaodi","paste file-------------f1="+f1.getParent()+" f2="+f2.getPath());
                    }
				} else {
//					if(moveSameDirectory.size() > 0) {
//						for(int i = 0; i < moveSameDirectory.size(); i++) {
//							f1 = new File(moveSameDirectory.get(i).get(oTag));
//							f2 = new File(moveSameDirectory.get(i).get(nTag));
//							moveFiles(moveSameDirectory.get(i).get(oTag), moveSameDirectory.get(i).get(nTag));
//						}
//						moveSameDirectory.clear();
//					} else {
				        // 文件粘贴
						for (int i = 0; i < listMap.size();) {
							f1 = listMap.get(i).get(oTag);
							Log.w("IN", "INPUT");
							f2 = listMap.get(i).get(nTag);
							in = new FileInputStream(f1);
							out = new FileOutputStream(f2);
							Log.w("IN", "OUTPUT");
							// 原文件管道
							inc = in.getChannel();
							// 目标文件管道
							outc = out.getChannel();
							ByteBuffer b = ByteBuffer.allocateDirect(length);
							while (true) {
								if (proDialog.isShowing()) {
								    // begin modify by qian_wei/zhou_yong 2011/10/27
								    // for unplug the device when the system is still do paste operation
								    if(f1.exists() && f2.exists()) {
								        int ret = inc.read(b);
								        if (ret == -1) {
								            inc.close();
								            outc.close();
								            in.close();
								            out.flush();
								            out.close();
								            try {
								                // 文件路径含有空格时，使用此方法无需转义
								            	Log.e("FileMenu","==== zhl [CopyThread] file="+f2.getPath());
								                String[] cmdArray = {"chmod", "777", f2.getPath()};
								                Runtime runtime = Runtime.getRuntime();
								                Process process = runtime.exec(cmdArray);
								                Log.w("processResult", " = "+process.waitFor());
								            } catch (IOException e) {
								                e.printStackTrace();
								            }
								            
								            if (cutFile == -1) {
								                delFile(listMap.get(i).get(oTag));
								            }
								            
								            if (i == listMap.size() - 1) {
								                Log.e("=========size==========", ""
								                        + dirList.size());
								                for (File file : dirList) {
								                    delDir(file);
								                }
								                handler.sendEmptyMessage(1);
								            }
								            i++;
								            break;
								        }
								        b.flip();
								        outc.write(b);
								        b.clear();
								    } else {
								        handler.sendEmptyMessage(2);
						                return;
								    }
								    // end modify by qian_wei/zhou_yong 2011/10/27
								} else {
									Log.w("========TAG", " = "
											+ System.currentTimeMillis());
									inc.close();
									outc.close();
									in.close();
									out.flush();
									out.close();
									i++;
									if (f1.length() != 0
											&& f2.length() < f1.length()) {
										f2.delete();
									}
									handler.sendEmptyMessage(4);
									return;
								}
								Thread.sleep(50);
							}
                            /*if(f1.getPath().contains("/mnt/nand") || f1.getPath().contains("/mnt/sd"))
                            {
                                Log.e("tangxiaodi","rename file-------------f1="+f1.getParent());
                                Uri uri_1 = Uri.parse("file://" + f1.getParent());
                                Intent intent_1 = new Intent("MEDIA_SCANNER_DESIGNATED_PATH",uri_1);
                                intent_1.putExtra("scan", true);
                                if(mContext != null)
                                    mContext.sendBroadcast(intent_1);
                            }*/
                            if(f2.getPath().contains("/mnt/nand") || f2.getPath().contains("/mnt/sd"))
                            {
                                Log.e("tangxiaodi","rename file-------------f2="+f2.getParent());
                                Uri uri_2 = Uri.parse("file://" + f2.getParent());
                                Intent intent_2 = new Intent("MEDIA_SCANNER_DESIGNATED_PATH",uri_2);
                                //mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                                intent_2.putExtra("scan", true);
                                if(mContext != null)
                                    mContext.sendBroadcast(intent_2); 
						    }
                        }
					}
//				}
			// end modify by qian_wei/zhou_yong 2011/10/26
			} catch (Exception e) {
				Log.d("Exception", "E " + e);
				e.printStackTrace();
				// 发生异常
				handler.sendEmptyMessage(2);
				return;
			} finally {
				if (inc != null)
					if (inc.isOpen())
						try {
							inc.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				if (outc != null)
					try {
						outc.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
            
		}
	};

	/**
	 * 同一挂载或者设备目录下剪切文件
	 * @param oldPath 源文件
	 * @param newPath 目标文件
	 * @return 执行结果
	 */
	private int moveFiles(String oldPath, String newPath) {
	    // begin modify by qian_wei/zhou_yong 2011/10/26
	    // for get the result of the mv command
		int resultCode = 0;
	    try {
//	        Runtime.getRuntime().exec(new String[]{"mv", oldPath, newPath}).waitFor();
	        resultCode = Runtime.getRuntime().exec(new String[]{"mv", oldPath, newPath}).waitFor();
//	    } catch (IOException e) {
	    } catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	    Log.w("RESULTCODE", " = "+resultCode);
	    return resultCode;
	    // end modify by qian_wei/zhou_yong 2011/10/26
	}
	
	/**
	 * 复制文件夹
	 * 
	 * @param oldPath String 原文件路径 如：/aa/bb 11,22
	 * @param newPath String 复制后路径 如：/ss/cc
	 * @return boolean 粘贴是否成功
	 */
	public boolean copyDir(String oldPath, String newPath) {
		boolean ret = true;
		try {
			// 要复制的文件夹
			File f_old = new File(oldPath);
			String d_old = "";
			// 新文件夹路径
			String d_new = newPath + File.separator + f_old.getName();
			newFilePath = d_new;
			if (oldPath.equals(newPath)) {
				ret = false;
			} else {
				// 如果文件夹不存在 则建立新文件夹
				File f_new = new File(d_new);
				f_new.mkdirs();
				try {
					String command_chmod = "chmod 777 " + f_new.getPath();
					Runtime runtime = Runtime.getRuntime();
					runtime.exec(command_chmod);
				} catch (IOException e) {
					e.printStackTrace();
				}
				File[] files = f_old.listFiles();
				for (int i = 0; i < files.length; i++) {
					// 复制的文件夹下的文件
					d_old = oldPath + File.separator + files[i].getName();
					if (files[i].isFile()) {
						// 复制文件
						copyFile(d_old, d_new);
					} else {
						// 复制文件夹
						copyDir(d_old, d_new);
					}
				}
				ret = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	/**
	 * 剪切并粘贴文件到指定目录
	 * 
	 * @param oldPath String 如：/fqf.txt
	 * @param newPath String 如：/xx/fqf.txt
	 */
	public boolean moveFile(String oldPath, String newPath) {
		boolean ret = false;
		try {
			if(oldPath.startsWith(MA.mountSdPath) 
					&& newPath.startsWith(MA.mountSdPath)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(oTag, oldPath);
				map.put(nTag, newPath);
				newFilePath = newPath;
				moveSameDirectory.add(map);
			} else {
				cutFile = -1;
				copyFile(oldPath, newPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return ret;
	}

	/**
	 * 剪切并粘贴文件夹到指定目录
	 * 
	 * @param oldPath String 如：/xx
	 * @param newPath String 如：/cc/xx
	 */
	public boolean moveDir(String oldPath, String newPath) {
		boolean ret = false;
		try {
			if(oldPath.startsWith(MA.mountSdPath) 
					&& newPath.startsWith(MA.mountSdPath)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(oTag, oldPath);
				map.put(nTag, newPath);
				newFilePath = newPath;
				moveSameDirectory.add(map);
			} else {
				copyDir(oldPath, newPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return ret;
	}

	/**
	 * 删除单个文件
	 * 
	 * @param file 要删除的文件
	 * @return 文件是否删除
	 */
	public boolean delFile(File f) {
		boolean ret = false;
		try {
			if (f.exists()) {
				f.delete();
				// 文件删除后仍然存在说明删除失败
				if (f.exists()) {
					ret = false;
				} 
                else
                {
					ret = true;
                    if(f.getPath().contains("/mnt/nand") || f.getPath().contains("/mnt/sd"))
                    {
                        Uri uri = Uri.parse("file://" + f.getParent());
                        Intent intent = new Intent("MEDIA_SCANNER_DESIGNATED_PATH",uri);
                        intent.putExtra("scan", true);
                        mContext.sendBroadcast(intent);
                        Log.e("tangxiaodi","delFile-------------------------delete file="+f.getParent());
                    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return ret;
	}

	/**
	 * 删除文件夹
	 * 
	 * @param file 文件夹
	 * @return 文件夹是否删除成功
	 */
	public boolean delDir(File f) {
		boolean ret = false;
		try {
			if (f.exists()) {
				File[] files = f.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						// 文件夹删除后仍然存在说明删除失败
						if (!delDir(files[i])) {
							return false;
						}
					} else {
						files[i].delete();
						// 文件删除后仍然存在说明删除失败
						if (files[i].exists()) {
							return false;
						}
					}
				}
				ret = f.delete(); // 删除空文件夹
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return ret;
	}

	/**
	 * @param sp SharedPreferences 存放数据容器
	 * @param operate 操作
	 * @param type 文件类型(文件、文件夹)
	 * @param i 文件在选中文件集合中的索引
	 */
	// boolean existFlag = false;
	List<Map<String, Object>> fileList;
	List<Map<String, Object>> nExistFile;

    public void doPaste(SharedPreferences sp, String operate, String type, int i) {
        Map<String, Object> existMap = null;
        selected = sp.getString(operate + i, null);
        File f = new File(selected);
        if (pasteFlag == 1) {
            if (f.isDirectory()) {
                fileLenght += FileUtil.getDirSize(f);
            } else {
                fileLenght += f.length();
            }
        }

        String filenameString = f.getName();
        // 新文件
        File fnew = new File(mySelFile + "/" + filenameString);

        /**
         * 文件或者文件夹已存在时
         */

        Log.w("FLAG2", " = " + fnew.getPath().startsWith(f.getPath()));
        // begin modify by qian_wei/zhou_yong 2011/10/20
        // for modify determine whether the directory is the parent-child relationship
//        if (fnew.getPath().startsWith(f.getPath())) {
//            boolean childFlag = String.valueOf(
//                    fnew.getPath().charAt(f.getPath().length())).equals("/");
//            Log.w("FLAG1", " = " + childFlag);
//            if (childFlag) {
//                flag = -2;
//                return;
//            } else {
//                if (fnew.exists()) {
//                    existMap = new HashMap<String, Object>();
//                    existMap.put("file", new File(selected));
//                    existMap.put("type", type);
//                    existMap.put("index", i);
//                    existMap.put("operate", operate);
//                    existFile.add(existMap);
//                } else {
//                    existMap = new HashMap<String, Object>();
//                    existMap.put("file", new File(selected));
//                    existMap.put("type", type);
//                    existMap.put("index", i);
//                    existMap.put("operate", operate);
//                    nExistFile.add(existMap);
//                }
//            }
//        } else {
//            if (fnew.exists()) {
//                existMap = new HashMap<String, Object>();
//                existMap.put("file", new File(selected));
//                existMap.put("type", type);
//                existMap.put("index", i);
//                existMap.put("operate", operate);
//                existFile.add(existMap);
//            } else {
//                existMap = new HashMap<String, Object>();
//                existMap.put("file", new File(selected));
//                existMap.put("type", type);
//                existMap.put("index", i);
//                existMap.put("operate", operate);
//                nExistFile.add(existMap);
//            }
//        }
        
        // 同一文件粘贴
        if(fnew.equals(f)) {
            existMap = new HashMap<String, Object>();
            existMap.put("file", new File(selected));
            existMap.put("type", type);
            existMap.put("index", i);
            existMap.put("operate", operate);
            existFile.add(existMap);
            return;
        }
        
        // 父子目录粘贴
        if(f.isDirectory()) {
            if(fnew.getPath().length() > f.getPath().length()) {
                if(fnew.getPath().startsWith(f.getPath()+"/")) {
                    flag = -2;
                    return;
                }
            }
        }
        
        if (fnew.exists()) {
            existMap = new HashMap<String, Object>();
            existMap.put("file", new File(selected));
            existMap.put("type", type);
            existMap.put("index", i);
            existMap.put("operate", operate);
            existFile.add(existMap);
        } else {
            existMap = new HashMap<String, Object>();
            existMap.put("file", new File(selected));
            existMap.put("type", type);
            existMap.put("index", i);
            existMap.put("operate", operate);
            nExistFile.add(existMap);
        }
        // end modify by qian_wei/zhou_yong 2011/10/20
    }

	/**
	 * 添加过滤
	 * 
	 * @param sp
	 */
	private static void addFilterDialog(final SharedPreferences sp, int flag) {

		String type = "";
		if (flag == 0) {
			type = MA.getString(R.string.music);
		} else if (flag == 1) {
			type = MA.getString(R.string.video);
		} else if (flag == 2) {
			type = MA.getString(R.string.image);
		}

		StringBuilder builder = new StringBuilder();
		Iterator<?> it = sp.getAll().values().iterator();
		// 迭代share里的数据
		builder.append("\n");
		int i = 0;
		while (it.hasNext()) {
			i++;
			if (i == sp.getAll().size()) {
				builder.append(it.next().toString());
			} else {
				builder.append(it.next().toString()).append(", ");
			}
		}
		LayoutInflater factory = LayoutInflater.from(MA);
		// 获得对话框布局
		View myView = factory.inflate(R.layout.add_filter, null);
		alertDialog = new NewCreateDialog(MA);
		alertDialog.setView(myView);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, MA
				.getString(R.string.ok), clickListener);
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, MA
				.getString(R.string.cancel), clickListener);
		alertDialog.show();
		alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
				.setTextAppearance(MA,
						android.R.style.TextAppearance_Large_Inverse);
		alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
				.setTextAppearance(MA,
						android.R.style.TextAppearance_Large_Inverse);

		myEditText = (EditText) alertDialog.findViewById(R.id.add_filter);
		showFlag = false;
		myEditText.addTextChangedListener(splitWatcher);
		TextView text = (TextView) alertDialog.findViewById(R.id.list);
		text.setText(MA.getString(R.string.hold_type, type, builder.toString())
				.toUpperCase());
		text
				.setTextAppearance(MA,
						android.R.style.TextAppearance_Large_Inverse);
		text.setTextColor(Color.WHITE);
		myEditText.requestFocus();
	}

	/**
	 * 删除过滤条件
	 * 
	 * @param sp
	 */
	private static void removeFilterDialog(final SharedPreferences sp) {
		final SharedPreferences share = sp;
		final SharedPreferences.Editor editor = sp.edit();
		nameList = new ArrayList<String>();
		li = new ArrayList<String>();
		Iterator<?> it = share.getAll().values().iterator();
		// 迭代share里的数据
		while (it.hasNext()) {
			li.add((String) it.next());
		}
		if (li.size() > 0) {
			LayoutInflater factory = LayoutInflater.from(MA);
			// 获得对话框布局
			View myView = factory.inflate(R.layout.remove_filter, null);
			alertDialog = new NewCreateDialog(MA);
			alertDialog.setView(myView);
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, MA
					.getString(R.string.ok), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (nameList.size() > 0) {
						int flag = 0;
						String str = "";
						// 初始化类型不可删除
						for (int i = 0; i < nameList.size(); i++) {
							if (nameList.get(i).equals("mp3")
									|| nameList.get(i).equals("jpg")
									|| nameList.get(i).equals("bmp")
									|| nameList.get(i).equals("gif")
									|| nameList.get(i).equals("mp4")
									|| nameList.get(i).equals("3gp")) {
								flag = 1;
								str = nameList.get(i);
								break;
							} else {
								editor.remove(nameList.get(i));
								editor.commit();
								alertDialog.dismiss();
								MA.updateList(true);
							}
						}

						if (flag == 1) {
							FileUtil.showToast(MA, MA.getString(
									R.string.type_undelete, str));
						} else {
							FileUtil.showToast(MA, MA
									.getString(R.string.delete_v));
						}

					} else {
						FileUtil.showToast(MA, MA
								.getString(R.string.select_type));
					}
				}
			});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, MA
					.getString(R.string.cancel), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					alertDialog.dismiss();
				}
			});
			alertDialog.show();
			alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setTextAppearance(MA,
							android.R.style.TextAppearance_Large_Inverse);
			alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
					.setTextAppearance(MA,
							android.R.style.TextAppearance_Large_Inverse);
			ListView list = (ListView) alertDialog.findViewById(R.id.list);
			list.setItemsCanFocus(false);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			RemoveFilterAdapter adapter = new RemoveFilterAdapter(MA, li);
			list.setAdapter(adapter);
			list.setOnItemClickListener(deleListener);
		}
	}

	/**
	 * 列表点击事件
	 */
	private static OnItemClickListener deleListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			CheckedTextView check = (CheckedTextView) v
					.findViewById(R.id.check);
			if (check.isChecked()) {
				nameList.remove(li.get(position));
				check.setChecked(false);
			} else {
				nameList.add(li.get(position));
				check.setChecked(true);
			}
		}
	};

	/**
	 * 显示帮助对话框
	 */
	private static void showHelp() {
		try {
			InputStream is = null;
			Log.w("FLAG", " = " + helpFlag);
			if (helpFlag == 1) {
				is = MA.getResources().openRawResource(R.raw.help_local);
			} else if (helpFlag == 2) {
				is = MA.getResources().openRawResource(R.raw.help_samba);
			} else {
				is = MA.getResources().openRawResource(R.raw.help_nfs);
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append("\n");
			byte[] b = new byte[is.available()];
			do {
				int count = is.read(b);
				if (count < 0) {
					break;
				}
				String str = new String(b, 0, count, "utf-8");
				buffer.append(str);
			} while (true);
			is.close();
			LayoutInflater factory = LayoutInflater.from(MA);
			// 获得对话框布局
			View myView = factory.inflate(R.layout.help, null);
			TextView textView = (TextView) myView.findViewById(R.id.help_text);
			textView.setText(buffer.toString());
			new AlertDialog.Builder(MA).setView(myView).setPositiveButton(
					MA.getString(R.string.close), null).create().show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取结果信息
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message mes) {

			if (proDialog != null && proDialog.isShowing()) {
				proDialog.dismiss();
			}

			if (mes.what == 1) {
				Log.d(" ------>NewPath", newFilePath);
				if (new File(selected).exists()
						&& sp.getString("operate", "").equals("cut")
						&& !new File(newFilePath).exists()) {
					FileUtil.showToast(MA, MA.getString(R.string.paste_error));
				} else if (!new File(newFilePath).exists()) {
					FileUtil.showToast(MA, MA.getString(R.string.paste_error));
				} else {
					if (sp.getString("operate", "").equals("cut")) {
						sp.edit().clear().commit();
					}
					FileUtil.showToast(MA, MA.getString(R.string.paste_v));
					MA.updateList(true);
				}
			} else if (mes.what == 2) {
				FileUtil.showToast(MA, MA.getString(R.string.paste_error));
			} else if (mes.what == 3) {
				if (delFlag) {
					FileUtil.showToast(MA, MA.getString(R.string.delete_v));
				} else {
					FileUtil.showToast(MA, MA.getString(R.string.delete_error));
				}
				MA.updateList(false);
			} else if (mes.what == 4) {
				MA.updateList(true);
			}
			try {
				String command_chmod = "sync";
				Runtime runtime = Runtime.getRuntime();
				runtime.exec(command_chmod);
			} catch (IOException e) {
				Log.w("TAG", " = " + e);
				e.printStackTrace();
			}
		}
	};

	// /**
	// * 执行上传操作
	// *
	// * @author qian_wei
	// */
	// class PutF extends Thread {
	// public void run() {
	// try {
	// sleep(2000);
	// Looper.prepare();
	// if ((FTPActivity.getNickname().equals("")) ||
	// FTPActivity.getTitlePath().equals("")) {
	// resultCode = 0;
	// } else {
	// getProperty();
	// resultCode = client.putFile(sIp, sName, sPwd, mySelFile.getPath(),
	// FTPActivity.getTitlePath(), fileName);
	// }
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// handler.sendEmptyMessage(0);
	// }
	// }

	/**
	 * 添加过滤类型输入框输入限制
	 */
	static TextWatcher splitWatcher = new TextWatcher() {

		int tempLength = 0;
		int Num = 0;

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			tempLength = s.length();
			Num = start;
		}

		/**
		 * 输入框文字改变
		 */
		public void afterTextChanged(Editable s) {
			try {
				int len = s.toString().getBytes("GBK").length;
				if (s.length() > tempLength) {
					if (s.charAt(Num) == '/' || s.charAt(Num) == '\\'
							|| s.charAt(Num) == ':' || s.charAt(Num) == '*'
							|| s.charAt(Num) == '?' || s.charAt(Num) == '\"'
							|| s.charAt(Num) == '<' || s.charAt(Num) == '>'
							|| s.charAt(Num) == '|') {
						s.delete(Num, Num + 1);
						if (showFlag) {
							FileUtil.showToast(MA, MA.getString(R.string.name_falid));
						} else {
							FileUtil.showToast(MA, MA.getString(R.string.suffix_falid));
						}
					} else if (showFlag) {
						if (len > 128) {
							s.delete(Num, Num + 1);
							FileUtil.showToast(MA, MA
									.getString(R.string.name_long));
						}
					} else {
						if (s.length() > 10) {
							s.delete(Num, Num + 1);
							FileUtil.showToast(MA, MA
									.getString(R.string.suffix_long));
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	};

	// /**
	// * 上传时获取对应的服务器信息 封装要上传的文件名
	// */
	// public void getProperty() {
	// if ((FTPActivity.getNickname().equals("")) ||
	// FTPActivity.getTitlePath().equals("")) {
	// FileUtil.showToast(MA, MA.getString(R.string.no_conn));
	// } else {
	// dbHelper = new DBHelper(MA, DBHelper.DATABASE_NAME, null,
	// DBHelper.DATABASE_VERSION);
	// sqlite = dbHelper.getWritableDatabase();
	// StringBuilder build = new StringBuilder();
	// // 封装上传文件名
	// for (int i = 0; i < file.size(); i++) {
	// String name = "";
	// if (file.get(i).getName().trim().contains(" ")) {
	// name = file.get(i).getName().trim().replace(" ", "\\ ");
	// } else {
	// name = file.get(i).getName().trim();
	// }
	// if (i == file.size() - 1) {
	// build.append(name);
	// } else {
	// build.append(name);
	// build.append(",");
	// }
	// }
	// fileName = build.toString();
	// Cursor cursor = sqlite.query("ftp", new String[] {
	// "_id", "ip", "name", "pwd", "nick"
	// }, "nick=?", new String[] {
	// FTPActivity.getNickname()
	// }, null, null, null);
	// // 对应赋值
	// while (cursor.moveToNext()) {
	// sIp = cursor.getString(cursor.getColumnIndex("ip"));
	// sName = cursor.getString(cursor.getColumnIndex("name"));
	// sPwd = cursor.getString(cursor.getColumnIndex("pwd"));
	// }
	// cursor.close();
	// }
	//
	// }

	/**
	 * 添加过滤类型操作
	 */
	static DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				Editor editor = sp.edit();
				
				// 过滤类型输入为空
				if (myEditText.getText().toString().equals("")) {
					FileUtil.showToast(MA, MA.getString(R.string.suffix_no));
				}
				// 过滤类型已存在
				else if (hasFilterExist(myEditText.getText().toString()
						.toUpperCase())) {
					FileUtil.showToast(MA, MA.getString(R.string.suffix_exist));

				}
				// 正确添加
				else {
					editor.putString(myEditText.getText().toString().toUpperCase(),
							myEditText.getText().toString().toUpperCase());
					editor.commit();
					FileUtil.showToast(MA, MA.getString(R.string.add_ok));
					alertDialog.dismiss();
					MA.updateList(true);
				}
			} else {
				alertDialog.dismiss();
			}
		}
	};

	/**
	 * 判断过滤条件是否存在
	 * @param filter 过滤条件
	 * @return 存在标识
	 */
	private static boolean hasFilterExist(String filter) {
		// 读取音频条件
		SharedPreferences shareAudio = MA.getSharedPreferences("AUDIO",
				Context.MODE_WORLD_READABLE);
		String strAudio = shareAudio.getString(filter, "");
		// 读取视频条件
		SharedPreferences shareVideo = MA.getSharedPreferences("VIDEO",
				Context.MODE_WORLD_READABLE);
		String strVideo = shareVideo.getString(filter, "");
		// 读取图片条件
		SharedPreferences shareImage = MA.getSharedPreferences("IMAGE",
				Context.MODE_WORLD_READABLE);
		String strImage = shareImage.getString(filter, "");

		if (strAudio.equals("") && strVideo.equals("") && strImage.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	public static void setHelpFlag(int flag) {
		helpFlag = flag;
	}
}
