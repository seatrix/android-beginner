
package com.mipt.mediacenter.utils.cifs;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mipt.mediacenter.center.server.FileInfo;

import android.util.Log;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class ShareFile {
    private final static String TAG = "ShareFile";
    
    /**
     * 
     * @param ip
     * @param user
     * @param password
     * @return [filePath, fileName, isDir]
     */
    public static List<FileInfo>  asynRequestShareFile(final String ip, final String user, final String password) {
        Callable<List<FileInfo>> func = new Callable<List<FileInfo>>() {
            public List<FileInfo> call() throws Exception {
                List<FileInfo> list = ShareFile.getFileInfo(ip, user, password);
                
                Log.i(TAG, "asynRequestShareFile:" + (list == null));
                return list;
            }
        };
        
        FutureTask<List<FileInfo>> futureTask = new FutureTask<List<FileInfo>>(func);
        Thread newThread = new Thread(futureTask);
        newThread.start();

        List<FileInfo> result = null;
        try {
            result = futureTask.get(1000 , TimeUnit.MILLISECONDS);;
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
        } catch (TimeoutException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }
    
    private static List<FileInfo> getFileInfo(String ip, String user, String password) {
        SmbFile[] list = null;
        List<FileInfo> re = null;
        try {
            Log.i(TAG, "ip,user,password:" + ip + "," + user + "," + password);
            list = dirDetail(ip, user, password);
            Log.i(TAG, list.toString());
            if (list != null) {
                re = new ArrayList<FileInfo>();
                for (SmbFile s : list) {
                    if(s.getType() == SmbFile.TYPE_SHARE && s.getName().indexOf("$") == -1){
                        FileInfo info = new FileInfo();
                        info.filePath = s.getPath();
                        info.fileName = s.getName().replace("/", "");
                        re.add(info);
                        try {
                            info.isDir = s.isDirectory();
                        } catch (SmbException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (SmbAuthException e) {
            Log.e(TAG, e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (SmbException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return re;
    }

    /**
     * //String smbStr = "smb://slieer:slieer@192.168.51.8/"; //smbStr =
     * "smb://192.168.51.230/";
     * 
     * @param ip
     * @param user
     * @param password
     * @throws MalformedURLException
     * @throws SmbException
     */
    private static SmbFile[] dirDetail(String ip, String user, String password)
            throws MalformedURLException, SmbException, SmbAuthException {

        String smbStr = null;
        if (user == null || password == null) {
            smbStr = "smb://".concat(ip).concat("/");
        } else {
            smbStr = "smb://".concat(user).concat(":").concat(password).concat("@").concat(ip)
                    .concat("/");
        }

        Log.i(TAG, "sbmstr:" + smbStr);
        SmbFile f = null;
        f = new SmbFile(smbStr);
        if (f.isDirectory()) {
            SmbFile[] files = f.listFiles();
            return files;
            /*
             * for(int i = 0; i < files.length; i++){
             * System.out.println(files[i].toString() + files[i].getType()); }
             */
        } else {
            return new SmbFile[] {
                f
            };
        }
    }

}
