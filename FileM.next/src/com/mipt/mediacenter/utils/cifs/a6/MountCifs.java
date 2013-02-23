
package com.mipt.mediacenter.utils.cifs.a6;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.os.Message;
import android.os.SambaManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.explorer.jni.Jni;
import com.mipt.fileMgr.center.CifsBrowserActivity;

public class MountCifs {
    private final static String TAG = "MountCifs";
    // 新增挂载
    private static final int MOUNT_RESULT_1 = 11;

    private static final int MOUNTED = 10;

    static {
        System.loadLibrary("android_runtime");
    }

    private Activity activity;
    // 用户服务器Ip地址
    private String server = "";
    // 用户名
    private String user = "";
    // 用户密码
    private String pass = "";
    // 服务器路径
    private String sharefolder = "";
    // 本地方法对象
    private Jni jni = new Jni();

    private StringBuilder builder = null;

    private ProgressDialog progress = null;

    /**
     * Userserver, folder_position, Username, Userpass:192.168.51.230, 156SHARE,g, 
     * s, f, n, p:192.168.51.230,SHAREDDOCS,g, 
     * s, f, n, p:192.168.51.15,JBOSS-4.2.2.GA,mipt,200819901103
     * 
     * @param activity
     * @param serv
     * @param user
     * @param pass
     */
    public MountCifs(Activity activity, String serv, String user, String pass, String sharefolder) {
        this.activity = activity;
        this.server = serv;
        this.user = user;
        this.pass = pass;
        this.sharefolder = sharefolder;
    }

    public void mountPath() {
        // allOrShort = flag;
        builder = new StringBuilder(server);
        builder.append("/").append(sharefolder);

        String returnStr = jni.getMountList(builder.toString());
        Log.i(TAG, "returnStr:" + returnStr);
        if (returnStr.equals("ERROR")) {
            progress = new ProgressDialog(activity);
            progress.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                    return true;
                }
            });
            progress.show();
            MountThread thread = new MountThread();
            thread.start();
            // end modify by qian_wei/zhou_yong 2011/10/28
        } else {
            // mounted
            Message m = handler.obtainMessage();
            m.what = MOUNTED;
            m.obj = returnStr;
            handler.sendMessage(m);
        }
    }

    private class MountThread extends Thread {
        public MountThread() {
        }

        public void run() {
            SambaManager samba = null;
            samba = (SambaManager)activity.getSystemService("Samba");
            samba.start("", "", "", "");
            int result = jni.UImount(server, sharefolder, " ", user, pass);

            String localPath = jni.getMountList(builder.toString());
            Log.i(TAG, "mount result:" + result);
            Message m = handler.obtainMessage();
            m.what = result;
            m.obj = localPath;
            handler.sendMessage(m);
        }

    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG, "msg.what:" + msg.what);
            Log.i(TAG, "msg.arg1:" + msg.arg1);
            Log.i(TAG, "msg.ojb:" + msg.obj);
            progress.dismiss();

            CifsBrowserActivity.listShareDir(activity, (String)msg.obj);
        }
    };
}
