package com.explorer.common;

import java.util.List;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.util.Log;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.Preference;
import android.content.Context;

import com.explorer.common.SocketClient;
import java.io.*;

public class MountInfo {
	private static final String TAG="MountInfo";
	public String[] path = new String[64];
	public int[] type = new int[64];
	public String[] label = new String[64];
	public String[] partition = new String[64];
	public int index = 0;
	private StorageManager mStorageManager = null;

	//SocketClient socketClient = null;

	public MountInfo(Context context) {
		try {
			if(mStorageManager == null){
				mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
			}
			StorageVolume[] storageVolumes	;
			storageVolumes = mStorageManager.getVolumeList();
			String[] devicePath = getDevicePath(storageVolumes);
			index = devicePath.length;
			//Log.e("MountInfo", "MountInfo--------1");
			for (int i = 0; i < index; i++) {
				path[i]=getUpdateFilePath(storageVolumes,devicePath[i]);
				label[i] = devicePath[i];
				partition[i]=label[i];
				//Log.e("MountInfo", "MountInfo--------2");
				//IBinder service = ServiceManager.getService("mount");
				//IMountService mountService = IMountService.Stub.asInterface(service);
				//List<android.os.storage.ExtraInfo> mountList = mountService.getAllExtraInfos();
				//String typeStr = mountList.get(i).mDevType;
				if (path[i].contains("/mnt/nand")) {
					type[i] = 2;
					label[i] = "";
				}
				else
				{
					// yuejun 20120829 SATA differs from USB
					//isSata(i);
					type[i] = 1;
				}
			}

			/*
			IBinder service = ServiceManager.getService("mount");
			if (service != null) {
				IMountService mountService = IMountService.Stub
						.asInterface(service);
				List<android.os.storage.MountInfo> mountList = mountService.getAllMountInfo();
				index = mountList.size();
				int i=0;
				for(i=0;i<index;i++)
				{
					path[i]=mountList.get(i).mPath;
					partition[i]=mountList.get(i).mLinkedLabel;

					label[i] = mountList.get(i).mVolumeLabel;
					if (label[i] == null) {
						label[i] = "";
					}

					Log.w("LABLE", label[i]);
					Log.w("partition", partition[i]);
					String typeStr = mountList.get(i).mDevType;

					if (path[i].contains("/mnt/nand")) {
							type[i] = 2;
							label[i] = "";
					} else if (typeStr.equals("SDCARD")) {
						type[i] = 2;
					} else if (typeStr.equals("USB")) {
						type[i] = 1;
					} else if (typeStr.equals("SATA")) {
						type[i] = 0;
					} else if (typeStr.equals("UNKOWN")) {
						type[i] = 3;
					}
				}
			}*/
		}catch (Exception e) {
			System.out.println(e);
		}
	}


	public String getMountDevices(String path){
		int start=0;
		start = path.lastIndexOf("/");
		String mountPath = path.substring(start+1);
		return mountPath;
	 }

	private String[] getDevicePath(StorageVolume[] storageVolumes) {
		String[] tmpPath = new String[storageVolumes.length];
		for (int i = 0; i < storageVolumes.length; i++) {
			tmpPath[i] = getMountDevices(storageVolumes[i].getPath());
		}
		int count = storageVolumes.length;
		//delete repeat
		for (int i = 0; i < storageVolumes.length; i++) {
			for (int j = i + 1; j < storageVolumes.length; j++) {
				try {
					if (tmpPath[i] != null) {
						if (tmpPath[j].equals(tmpPath[i]) && tmpPath[j] != null) {
							tmpPath[j] = null;
							count--;
						}
					}
				} catch (Exception e) {

				}
			}
		}
		String[] path = new String[count];
		int j = 0;
		for (int i = 0; i < storageVolumes.length; i++) {
			if (tmpPath[i] != null) {
				path[j] = tmpPath[i];
				j++;
			}
		}
		//sort
		for (int i = 0; i < count; i++) {
			for (int k = i + 1; k < count; k++) {
				if (path[i].compareTo(path[k]) > 0) {
					String tmp = path[k];
					path[k] = path[i];
					path[i] = tmp;
				}
			}
		}
		return path;
	}

	private String getUpdateFilePath(StorageVolume[] storageVolumes,String fileSuffix){
		if(storageVolumes != null && storageVolumes.length > 0){
			for (int i = 0; i < storageVolumes.length; i++) {
				if(storageVolumes[i].getPath().contains(fileSuffix)){
					Log.e(TAG,"---YUEJUN---- storageVolumes[i].getPath() = " + i +" >> "+storageVolumes[i].getPath());
					return storageVolumes[i].getPath();
				}
			}
		}
		return "/mnt/nand";

	}

	// begin by yuejun 20120829 SATA differs from USB

	public void isSata(int i)
	{
		Log.e(TAG,"----YUEJUN---- isSATA = 1");

		//SocketClient socketClient = null;
		//socketClient = new SocketClient(context.this);

		SocketClient sc1 = new SocketClient();
	    sc1.writeMess(
		"system /system/busybox/bin/find /sys/devices/platform/hiusb-ehci.0/usb1/1-1 -name \"sd*\" >> /mnt/SATA.txt");
		sc1.readNetResponseSync();

		Log.e(TAG,"----YUEJUN---- isSATA = 2");

		//SocketClient sc2 = new SocketClient();
		//sc2.writeMess(
		//"system /system/busybox/bin/chmod 777 /mnt/SATA.txt");
		//sc2.readNetResponseSync();

		String fileName = "/mnt/SATA.txt";
		File f = new File(fileName);
		long size = f.length();


		if(size > 0)
		{

			Log.e(TAG,"----YUEJUN---- isSATA = 3");
			String out = "";
			out = readFile(fileName);

			String sataName = "";
			sataName = out.substring(out.lastIndexOf("/"));
			//Log.e(TAG,"----YUEJUN--- sataName = " +sataName);

			if(path[i].contains(sataName))
			{
				type[i] = 0;

			}
			else
			{
				type[i] = 1;
			}

			SocketClient sc3 = new SocketClient();
			sc3.writeMess(
			"system /system/busybox/bin/rm /mnt/SATA.txt");
			sc3.readNetResponseSync();

		}
		else
		{

			type[i] = 1;

			SocketClient sc4 = new SocketClient();
			sc4.writeMess(
			"system /system/busybox/bin/rm /mnt/SATA.txt");
			sc4.readNetResponseSync();

		}

	}

	public String readFile(String fileName)
	{
		String output = "";

        File file = new File(fileName);

        if(file.exists())
		{
            if(file.isFile())
			{

                try{

					//FileReader fr = new FileReader(file);
                    BufferedReader input = new BufferedReader(new FileReader(file));
                    //StringBuffer buffer = new StringBuffer();

                    //buffer.append(input.readLine());
                    //output = buffer.toString();
					output = input.readLine();

					Log.e(TAG, "----YUEJUN---- ouput = "+ output);
					input.close();
                }
                catch(IOException ioException)
				{
                    System.err.println("File Error!");
                }

            }
        }
        else
		{
            System.err.println("File Does Not Exit!");
        }

        return output;
	}

	// end by yuejun 20120829 SATA differs from USB
}


