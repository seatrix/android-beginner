package com.exam.slieer.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.R.bool;
import android.util.Log;

public class LinuxMountCifs {
	private static String TAG = "LinuxMountCifs";
	
	public static void mount(String user, String password, String remotePath,
            String targetDir){
		Runtime runtime = Runtime.getRuntime();
		
		String dir = createDirCommand(targetDir);
		String mount = mountCommand(user, password, remotePath, targetDir);
		Log.i(TAG, "dirCommand:" + dir);
		Log.i(TAG, "mountCommand:" + mount);
		File f = new File(dir);
		if(! f.exists() && !f.isDirectory()){
			boolean re = f.mkdirs();
			Log.i(TAG, "craete dir:" + re);
		}
		Process process = null;
		try {
			//process = runtime.exec(new String[]{"sh",dir});//, mount});
			process = runtime.exec("mount -t cifs -o username=\"slieer\",password=\"slieer\" //192.168.51.42/linux-share /sdcard/smb");
			int code = process.waitFor();
			
			Log.i(TAG, "command return :" + code);
			print(process);
		} catch (IOException e) {
			
			e.printStackTrace();
		}catch (InterruptedException e) {
			
			e.printStackTrace();
		} finally {
			if(process != null){
				process.destroy();
			}
		}
	}
	
	static class Constants{
		static String MOUNT_SMB_ROOT = "/sdcard/smb";
	}
	
    public static String mountA4Path(String remotePath) {
        String localPath = Constants.MOUNT_SMB_ROOT + "/" + remotePath;
        if(localPath.endsWith("/")){
            localPath = localPath.substring(0, localPath.length() - 1);
        }
        Log.i(TAG, "localPath:" + localPath);
        return localPath;
    }	
	
	private static void print(Process process) throws IOException {
		InputStream is = process.getInputStream();
		LineNumberReader input = new LineNumberReader (new InputStreamReader(is));
		
		String line = null;
		while ((line = input.readLine ()) != null){
			Log.i(TAG, line);
		}
	}
	
	private static String createDirCommand(String localPath) {
		localPath = localPath.replace(" ", "\\ ");
	    return "mkdir -p " + localPath;
	}

    /*
     * mount -t cifs -o username="slieer",password="slieer"
     * //192.168.51.42/linux-share /mnt/smb/smb1 mount -t cifs -o
     * username="",password="" //192.168.51.230/SharedDocs /mnt/smbtest
     */
    private static String mountCommand(String user, String password, String remotePath,
            String targetDir) {
        if (user == null || password == null) {
            user = "";
            password = "";
        }

        if (remotePath == null || targetDir == null) {
            return null;
        }
        StringBuilder comm = new StringBuilder();
        comm.append("mount -t cifs -o username=\"").append(user).append("\",password=\"")
                .append(password).append("\" //").append(remotePath).append(" ").append(targetDir);

        return comm.toString();
    }
}
