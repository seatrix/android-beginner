package com.explorer.bd;

import java.io.File;

import android.util.Log;

import com.hisilicon.hibdplayer.HiBDInfo;

public class BDInfo
{
	private static final String TAG = "BDInfo";

	private HiBDInfo mHiBDInfo;

	public BDInfo()
	{
		mHiBDInfo = new HiBDInfo();
	}

	/**
	 * 检查蓝光信息
	 *
	 * @param pPath
	 *            蓝光目录路径
	 * @return 命令执行结果 0-是蓝光目录 非0-不是蓝光目录
	 */
	public synchronized int checkDiscInfo(String pPath)
	{
		int _Result = 0;
		mHiBDInfo.openBluray(pPath);
		_Result = mHiBDInfo.checkDiscInfo();
		mHiBDInfo.closeBluray();

		return _Result;
	}

	/**
	 * 是否是蓝光文件
	 *
	 * @param pPath
	 *            蓝光目录路径
	 * @return true - 是 false - 否
	 */
	public boolean isBDFile(String pPath)
	{
		Log.v(TAG, "path is " + pPath);

		if (!hasBDMVDir(pPath))
		{
			return false;
		}

		if (checkDiscInfo(pPath) < 0)
		{
			return false;
		}

		return true;
	}

	/**
	 * 是否包含BDMV目录
	 *
	 * @param pPath
	 *            蓝光目录路径
	 * @return true - 是 false - 否
	 */
	public boolean hasBDMVDir(String pPath)
	{
		File _File = new File(pPath);

		if (!_File.exists())
		{
			return false;
		}

		File[] _Files = _File.listFiles();

		if (_Files == null)
		{
			return false;
		}

		for (int i = 0; i < _Files.length; i++)
		{
			if (_Files[i].getName().equalsIgnoreCase("BDMV"))
			{
				return true;
			}
		}

		return false;
	}
}
