package com.softwinner.TvdFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.softwinner.netshare.SmbFile;
import com.softwinner.netshare.NtlmPasswordAuthentication;
import com.softwinner.netshare.OnReceiverListenner;
import com.softwinner.netshare.SmbFile.SmbReceiver;

public class SambaManager
{
	private Context mContext;
	private ArrayList<SmbFile> mWorkgroupList = null;
	private ArrayList<SmbFile> mServiceList = null;
	private ArrayList<SmbFile> mShareList = null;
	private ArrayList<String> mMountedPointList = null;
	private HashMap<String, String> mMap = null;
	
	private boolean manuallyCancel = false;
	private SmbFile[] mSmbList = null;
	private SmbLoginDB mLoginDB = null;
	private static File mountRoot = null;
	private ProgressDialog pr_dialog;
	
	private String TAG = "SambaManager";
	
	public SambaManager(Context context)
	{
		mContext = context;
		mWorkgroupList = new ArrayList<SmbFile>();
		mServiceList = new ArrayList<SmbFile>();
		mShareList = new ArrayList<SmbFile>();
		mMountedPointList = new ArrayList<String>();
		mMap = new HashMap<String, String>();
		mLoginDB = new SmbLoginDB(context);
		
		mountRoot = mContext.getDir("share", 0);
	}
	
	/* 搜索网络邻居,结果以回调方式返回 */
	public void startSearch(final String smbUrl, final OnSearchListenner ls)
	{
		Log.d("chen", "search smb:" + smbUrl);
		manuallyCancel = false;
		
		/* 弹出搜索对话框 */
		pr_dialog = showProgressDialog(R.drawable.icon, mContext.getResources().getString(R.string.search), 
				null, ProgressDialog.STYLE_SPINNER, true);
		pr_dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ls.onFinish(true);
				/* 如果是由于获取不到信息而自动取消,则再弹出登陆框要求输入正确密码 */
				if(mSmbList == null && !manuallyCancel)
				{
					
				}
			}
		});
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				SmbFile smbFile = new SmbFile("smb://");
				if(smbUrl.equals("smb://"))
				{
					/* 搜索下面所有的workgroup*/
					smbFile = new SmbFile(smbUrl);
				}
				else if(isSambaWorkgroup(smbUrl))
				{
					/* 搜索下面所有的service */
					smbFile = getSambaWorkgroup(smbUrl);
				}
				else if(isSambaServices(smbUrl))
				{
					/* 搜索下面所有的shared文件夹 */
					smbFile = getSambaService(smbUrl);
				}
				else if(isSambaShare(smbUrl))
				{
					/* 挂载shared文件夹 */
					smbFile = getSambaShare(smbUrl);
				}
				boolean ret = startLogin(smbFile, ls);
				final SmbFile f = smbFile;
				pr_dialog.cancel();
				if(!ret)
				{
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							createLoginDialog(f, ls);
						}
					});
					
				}
			}
		});
		thread.start();
	}
	
	private ProgressDialog showProgressDialog(int icon, String title, String message, int style, final boolean cancelable)
	{
		ProgressDialog pr_dialog = null;
		pr_dialog = new ProgressDialog(mContext);
		pr_dialog.setProgressStyle(style);
		pr_dialog.setIcon(icon);
		pr_dialog.setTitle(title);
		pr_dialog.setIndeterminate(false);
		pr_dialog.setCancelable(cancelable);
		if(message != null)
		{
			pr_dialog.setMessage(message);
		}
		pr_dialog.show();
		pr_dialog.getWindow().setLayout(600, 300);
		return pr_dialog;
	}
	
	private int addFileList(SmbFile samba, final ArrayList<SmbFile> list, final OnSearchListenner ls)
	{
		SmbReceiver receiver = new SmbReceiver() {
			@Override
			public void accept(SmbFile smbFile) {
				if(pr_dialog.isShowing())
				{
					if(!smbFile.getPath().endsWith("$")){
						list.add(smbFile);
						Log.d("chen", smbFile.getPath());
						ls.onReceiver(smbFile.getPath());
					}
				}
			}
		};
		return samba.list(receiver);
	}
	
	private boolean login(SmbFile samba,final ArrayList<SmbFile> list, final OnSearchListenner ls)
	{
		int ret = addFileList(samba, list, ls);
		switch(ret)
		{
		case SmbFile.SUCCESS:
			Log.d(TAG, "list success");
			return true;
		case SmbFile.EACCES:
			Log.e(TAG, "can not access to " + samba.getPath() + ", due to permission." +
					"maybe because get the wrong account or password");
			NtlmPasswordAuthentication ntlm = getLoginDataFromDB(samba);
			if(ntlm != null)
			{
				samba.setNtlm(ntlm);
			}
			int r = addFileList(samba, mServiceList, ls);
			if(r == SmbFile.SUCCESS)
				return true;
			else
			{
				mLoginDB.delete(samba.getPath());
				return false;
			}
		case SmbFile.EINVAL:
			Log.e(TAG, "can not parse the url " + samba.getPath());
			showMessage(R.string.access_fail);
			break;
		case SmbFile.ENOENT:
			Log.e(TAG, "file does not exist");
			showMessage(R.string.file_does_not_exist);
			break;
		case SmbFile.ENODEV:
		case SmbFile.EPERM:
			Log.e(TAG, "Workgroup or server can not be found");
			showMessage(R.string.smbfile_no_exist);
			break;
		default:
			Log.e(TAG, "other error code:" + ret);
			showMessage(R.string.access_fail);
		}
		return true;
	}
	
	private void showMessage(final int resId)
	{
		((Activity)mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
				}catch (Exception e) {
				}
			}
		});
	}
	
	/**
	 * 
	 * @param samba
	 * @param ls
	 * @return 返回真表示登陆成功
	 */
	public boolean startLogin(SmbFile samba,final OnSearchListenner ls)
	{
		NtlmPasswordAuthentication ntlm = null;
		Log.d("chen","getType " + samba.getType());
		switch(samba.getType())
		{
		case SmbFile.TYPE_WORKGROUP:
			mServiceList.clear();
			return login(samba, mServiceList, ls);
		case SmbFile.TYPE_SERVER:
			mShareList.clear();
			return login(samba, mShareList, ls);
		case SmbFile.TYPE_FILE_SHARE:
			/* 如果之前已经成功登陆过,则跳转到挂载点 */
			String mountPoint = getSambaMountedPoint(samba.getPath());
			if(mountPoint != null)
			{
				ls.onReceiver(mountPoint);
				return true;
			}
			/* 否则尝试无输入账号密码登陆,如果成功则返回下面所有的文件 */
			mountPoint = createNewMountedPoint(samba.getPath());
			SmbFile.umount(mountPoint);		//确保该路径不属于挂载状态,防止由于程序以外中断,导致某个服务器没被卸载,下次尝试挂载时会失败
			int ret = SmbFile.mount(samba.getPath(), mountPoint, "", "");
			if(ret == 0)
			{
				mMountedPointList.add(mountPoint);
				mMap.put(samba.getPath(), mountPoint);
				ls.onReceiver(mountPoint);
				return true;
			}
			else
			{
				ntlm = getLoginDataFromDB(samba);
				if(ntlm != null)
				{
					samba.setNtlm(ntlm);
				}
				ret = SmbFile.mount(samba.getPath(), mountPoint, samba.getNtlm().mUsername, samba.getNtlm().mPassword);
				if(ret == 0)
				{
					mMountedPointList.add(mountPoint);
					mMap.put(samba.getPath(), mountPoint);
					ls.onReceiver(mountPoint);
					return true;
				}
				/* 失败则弹出登陆框进行登陆 */
				else
				{
					mLoginDB.delete(samba.getPath());
					return false;
				}
			}
		default:
			samba = new SmbFile("smb://");
			mWorkgroupList.clear();
			return login(samba, mWorkgroupList, ls);
		}
	}
	
	private NtlmPasswordAuthentication getLoginDataFromDB(SmbFile file)
	{
		if(file == null)
			return null;
		final int SMB_PATH_COLUME = 0;
		final int DOMAIN_COLUME = 1;
		final int USERNAME_COLUME = 2;
		final int PASSWORD_COLUME = 3;
		String[] columns = null;
		String selection = SmbLoginDB.SMB_PATH + "=?";
		String selectionArgs[] = {file.getPath()};
		String domain = null;
		String username = null;
		String password = null;
		NtlmPasswordAuthentication ntlm = null;
		Cursor cr = mLoginDB.query(columns, selection, selectionArgs, null);
		if(cr != null)
		{
			try
			{
				while (cr.moveToNext()) {
					domain = cr.getString(DOMAIN_COLUME);
					Log.d("chen","------------------get ntlm ------------------");
					Log.d("chen","fileDom  " + file.getNtlm().mDomain);
					Log.d("chen","path     " + cr.getString(SMB_PATH_COLUME));
					Log.d("chen","domain   " + domain);
					Log.d("chen","username " + cr.getString(USERNAME_COLUME));
					Log.d("chen","password " + cr.getString(PASSWORD_COLUME));
					Log.d("chen","------------------end ntlm ------------------");
					if(domain != null && domain.equals(file.getNtlm().mDomain))
					{
						username = cr.getString(USERNAME_COLUME);
						password = cr.getString(PASSWORD_COLUME);
						ntlm = new NtlmPasswordAuthentication(domain, username, password);
						break;
					}
				}
			}finally
			{
				cr.close();
				cr = null;
			}
		}
		return ntlm;
	}
	
	private void createLoginDialog(final SmbFile samba, final OnSearchListenner ls)
	{
		final Dialog dg = new Dialog(mContext, R.style.menu_dialog);
		dg.setCancelable(true);
		LayoutInflater infrater = LayoutInflater.from(mContext);
		View v = infrater.inflate(R.layout.login_dialog, null);
		dg.setContentView(v);
		final EditText account = (EditText) v.findViewById(R.id.account);
		final EditText password = (EditText) v.findViewById(R.id.password);
		Button ok = (Button) v.findViewById(R.id.login_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dg.dismiss();
				/* 开始登陆 */
				final ProgressDialog pdg = showProgressDialog(R.drawable.icon, mContext.getResources().getString(R.string.login), 
						null, ProgressDialog.STYLE_SPINNER, true);
				NtlmPasswordAuthentication ntlm = new NtlmPasswordAuthentication(samba.getNtlm().mDomain, 
						account.getEditableText().toString(), password.getEditableText().toString());
				samba.setNtlm(ntlm);
				switch (samba.getType()) 
				{
				/* 如果samba类型为server,则列出所有的share */
				case SmbFile.TYPE_SERVER:
					SmbFile[] shareList = samba.list();
					if(shareList == null)
					{
						showMessage(R.string.login_fail);
					}
					else
					{
						addLoginMessage(samba);
						mShareList.clear();
						for(SmbFile file:shareList)
						{
							if(!file.getPath().endsWith("$")){
								mShareList.add(file);
								ls.onReceiver(file.getPath());
							}
						}
						ls.onFinish(true);
					}
					break;
				/* 如果samba类型为share,挂载该share,并列出所有的子文件 */
				case SmbFile.TYPE_FILE_SHARE:
					String mountedPoint = createNewMountedPoint(samba.getPath());
					int success = SmbFile.mount(samba.getPath(), mountedPoint, samba.getNtlm().mUsername, samba.getNtlm().mPassword);
					/* 挂载成功,列出子目录文件 */
					if(success == 0)
					{
						addLoginMessage(samba);
						mMountedPointList.add(mountedPoint);
						mMap.put(samba.getPath(), mountedPoint);
						ls.onReceiver(mountedPoint);
						ls.onFinish(true);
					}
					else
					{
						showMessage(R.string.login_fail);
					}
					break;
				}
				pdg.dismiss();
			}
		});
		Button cancel = (Button) v.findViewById(R.id.login_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dg.dismiss();	
			}
		});
		
		Window dialogWindow = dg.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = 300;
		dialogWindow.setAttributes(lp);
		dg.show();
	}
	
	/* 添加登录信息到数据库中 */
	public void addLoginMessage(SmbFile file)
	{
		Log.d("chen", "-------------add login message------------");
		Log.d("chen", "path     " + file.getPath());
		Log.d("chen", "domain   " + file.getNtlm().mDomain);
		Log.d("chen", "username " + file.getNtlm().mUsername);
		Log.d("chen", "password " + file.getNtlm().mPassword);
		Log.d("chen", "-------------end adding-------------------");
		mLoginDB.insert(file.getPath(), file.getNtlm().mDomain, 
				file.getNtlm().mUsername, file.getNtlm().mPassword);
	}
	
	/* 清除所有登陆点 */
	public void clear()
	{
		for(String mountPoint:mMountedPointList)
		{
			SmbFile.umount(mountPoint);
		}
		mLoginDB.closeDB();
	}
	
	public boolean isSambaServices(String path)
	{
		for(int i = 0; i < mServiceList.size(); i++)
		{
			if(mServiceList.get(i).getPath().equals(path))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isSambaShare(String path)
	{
		for(int i = 0; i < mShareList.size(); i++)
		{
			if(mShareList.get(i).getPath().equals(path))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isSambaWorkgroup(String path)
	{
		for(int i = 0; i < mWorkgroupList.size(); i++)
		{
			if(mWorkgroupList.get(i).getPath().equals(path))
			{
				return true;
			}
		}
		return false;
	}
	
	public SmbFile getSambaWorkgroup(String sambaFile)
	{
		for(int i = 0; i < mWorkgroupList.size(); i++)
		{
			SmbFile file = mWorkgroupList.get(i);
			if(file.getPath().equals(sambaFile))
			{
				return file;
			}
		}
		return null;
	}
	
	public SmbFile getSambaService(String sambaFile)
	{
		for(int i = 0; i < mServiceList.size(); i++)
		{
			SmbFile file = mServiceList.get(i);
			if(file.getPath().equals(sambaFile))
			{
				return file;
			}
		}
		return null;
	}
	
	public SmbFile getSambaShare(String sambaFile)
	{
		for(int i = 0; i < mShareList.size(); i++)
		{
			SmbFile file = mShareList.get(i);
			if(file.getPath().equals(sambaFile))
			{
				return file;
			}
		}
		return null;
	}
	public ArrayList<SmbFile> getAllSmbWorkgroup()
	{
		return (ArrayList<SmbFile>) mWorkgroupList.clone();
	}
	
	public ArrayList<SmbFile> getAllSmbServices()
	{
		return (ArrayList<SmbFile>) mServiceList.clone();
	}
	
	public ArrayList<SmbFile> getAllSmbShared()
	{
		return (ArrayList<SmbFile>) mShareList.clone();
	}
	
	public static String createNewMountedPoint(String path)
	{ 
		String mountedPoint = null;
		
		/* for test */
		path = path.replaceFirst("smb://", "smb_");
		path = path.replace("/", "_");
		mountedPoint = path;
		File file = new File(mountRoot,mountedPoint);
		Log.d("chen","mounted create:  " + file.getPath());
		if(!file.exists())
		{
			try {
				file.mkdir();
			} catch (Exception e) {
				Log.e("chen", "create " + mountedPoint + " fail");
				e.printStackTrace();
			}
		}
		return file.getAbsolutePath();
	}
	
	private String getSambaMountedPoint(String samba)
	{
		String mountedPoint = null;
		mountedPoint = mMap.get(samba);
		return mountedPoint;
	}
	
	public boolean isSambaMountedPoint(String mountedPoint)
	{
		Log.d("chen","mountedPoint   " + mountedPoint);
		if(mMountedPointList.size() == 0)
		{
			Log.d("chen","list is 0");
		}
		for(String item:mMountedPointList)
		{
			Log.d("chen","list.....  " + item);
			if(item.equals(mountedPoint))
			{
				return true;
			}
		}
		return false;
	}
	
	public interface OnSearchListenner
	{
		void onReceiver(String path);
		void onFinish(boolean finish);
	}
	
	public interface OnLoginFinishListenner
	{
		void onLoginFinish(String mountedPoint);
	}
}

