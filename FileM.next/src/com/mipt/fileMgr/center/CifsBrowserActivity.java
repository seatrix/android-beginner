
package com.mipt.fileMgr.center;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.CifsActivity.CifsFragment;
import com.mipt.mediacenter.center.server.DeviceInfo;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.utils.cifs.a6.MountCifs;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;

public class CifsBrowserActivity extends Activity {
    public static final String TAG = "CifsBrowserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cm_file_list);

        TextView currentPath = (TextView)findViewById(R.id.current_path_tag);
        String ip = getIntent().getStringExtra(CifsActivity.IP);

        String allDevices = getResources().getString(R.string.all_devices);
        currentPath.setText(allDevices.concat("/").concat(ip));

        ArrayList<FileInfo> list = getIntent().getParcelableArrayListExtra(CifsActivity.DATA);
        if (list == null || list.size() == 0) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            return;
        }

        Fragment fg = getFragmentManager().findFragmentById(R.id.file_content);
        if (fg != null) {
            fg.onDetach();
        }

        Log.i(TAG, "view local  file info....");
        // viewTypeTag.setText(getString(R.string.all_file_view_type));
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment newFragment = new CifsBrowserFragment(this, list);
        ft.replace(R.id.file_content, newFragment, CifsFragment.TAG);
        ft.commit();

    }

    private static class CifsBrowserFragment extends Fragment {
        private Activity context;
        private List<FileInfo> fileInfos;

        public CifsBrowserFragment(Activity activity, List<FileInfo> fileInfos) {
            this.context = activity;
            this.fileInfos = fileInfos;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View mRootView = inflater.inflate(R.layout.smb_device_list, container, false);

            ListView listView = (ListView)mRootView.findViewById(R.id.list_view);
            listView.setAdapter(new Adapt(context, fileInfos));
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FileInfo fileInfo = fileInfos.get(position);
                    mountOp(context, fileInfo.filePath);
                }
            });
            return mRootView;
        }
    }

    /*
     * smb://slieer:slieer@192.168.1.100/Users/
     * smb://192.168.51.230/SharedDocs/
     */
    private static void mountOp(Activity context, String smbPath) {
        Log.i(TAG, "smbPath:" + smbPath);
        String[] strArr = smbPath.split("@");
        String remotePath = null;
        String user = null;
        String password = null;
        if(strArr.length > 1){
            remotePath = strArr[1];
            String[] userInfo = strArr[0].replace("smb://", "").split(":");
            user = userInfo[0];
            password = userInfo[1];
        }else{
            remotePath = smbPath.replace("smb://", "");
        }
        if(remotePath.endsWith("/")){
            remotePath = remotePath.substring(0, remotePath.length() - 1);
        }

        Log.i(TAG, "user, password, remotePath, localPath:" + user + "," + password + ","
                + remotePath);

/*        if (RootTools.isRootAvailable()) {
            if (RootTools.isAccessGiven()) {
                Log.i(TAG, "Root access has been granted!");
            }
        } else {
            // do something else
            Log.e(TAG, "get root privilege fail.");
        }
*/            //Log.i(TAG, "this System is rooted...");
        
        Log.i(TAG, "AndroidModel:" + android.os.Build.MODEL);
        if ("A6".equals(android.os.Build.MODEL)){
            String[] array = remotePath.split("/");
            String ip = array[0];
            String shareFolder = array[1];
            Log.i(TAG, "ip,shareFolder:" + ip + "," + shareFolder);
            if(user == null){
                user = "g";
                password = "";
            }
            new MountCifs(context, ip, user, password, shareFolder).mountPath();
        }else if("A4".equals(android.os.Build.MODEL)){
            String localPath = "/mnt/smbmount/" + remotePath;
            if(localPath.endsWith("/")){
                localPath = localPath.substring(0, localPath.length() - 1);
            }
            Log.i(TAG, "localPath:" + localPath);
            
            mountA4(context, user, password, remotePath, localPath);
        }
    }

    private static void mountA4(Activity context, String user, String password, String remotePath,
            String localPath) {
        String createLocalPathCommand = " mkdir -p " + localPath;
        
        String linuxMountCommand = mountCommand(user, password, remotePath, localPath);
        Log.i(TAG, "create dir Command:" + createLocalPathCommand);
        Log.i(TAG, "mount command:" + linuxMountCommand);

        Command command = new Command(0, createLocalPathCommand , linuxMountCommand) {
            @Override
            public void output(int id, String line) {
                Log.i(TAG, "commandoutput:" + line);
            }
        };
        try {
            RootTools.getShell(true).add(command).waitForFinish();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (TimeoutException e) {
            Log.e(TAG, e.getMessage());
        } catch (RootDeniedException e) {

            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "other--" + e.getMessage(), e);
        }
        
        try {
            List<Mount> mountList = RootTools.getMounts();
            for(Mount m : mountList){
                //Log.i(TAG, "mountList:" + m);
                if(m.toString().contains(localPath)){
                    //mount ok
                    //FragmentTransaction ft = context.getFragmentManager().beginTransaction();
                    //Fragment newFragment = DirViewFragment.newInstance(localPath, -1);
                    //ft.replace(R.id.file_content, newFragment, DirViewFragment.TAG);
                    //ft.commit();
                    listShareDir(context, localPath);
                    
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getMounts--" + e.getMessage(), e);
        }
    }

    public static void listShareDir(Activity context, String localPath) {
        Intent intent = new Intent();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.devPath = localPath;
        deviceInfo.type = DeviceInfo.TYPE_CIFS;
        intent.putExtra(MediacenterConstant.INTENT_EXTRA, deviceInfo);
        intent.setClass(context, FileMainActivity.class);
        context.startActivity(intent);
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

    private static class Adapt extends ArrayAdapter<FileInfo> {

        public Adapt(Context context, List<FileInfo> objects) {
            super(context, R.layout.smb_device_list_item, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.smb_device_list_item, parent, false);
            }
            ImageView image = (ImageView)row.findViewById(R.id.lan_icon);
            TextView name = (TextView)row.findViewById(R.id.netbios_name);

            FileInfo info = getItem(position);
            name.setText(info.fileName);
            TextView ip = (TextView)row.findViewById(R.id.ip);
            ip.setVisibility(View.GONE);

            Drawable drawable = getContext().getResources().getDrawable(R.drawable.cm_folder);
            image.setImageDrawable(drawable);
            return row;
        }
    }

}
