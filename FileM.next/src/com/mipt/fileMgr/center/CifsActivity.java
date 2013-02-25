
package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.CifsLanBrowserAdpter;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.utils.cifs.LanNodeInfo;
import com.mipt.mediacenter.utils.cifs.ShareFile;

/**
 * @author slieer
 *
 */
public class CifsActivity extends Activity {
    public static final String TAG = "CifsActivity";
    public static final String NODE = "node";
    public static final String DATA = "data";
    private TextView currentPath;

    // private TextView viewTypeTag;
    // private TextView currentNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cm_file_list);

        currentPath = (TextView)findViewById(R.id.current_path_tag);
        
        String allDevices = getResources().getString(R.string.all_devices);
        String shareDevices = getResources().getString(R.string.net_share_device);
        currentPath.setText(allDevices.concat("/").concat(shareDevices));
        // viewTypeTag = (TextView) findViewById(R.id.view_type_tag);
        // currentNum = (TextView) findViewById(R.id.current_num_tag);

        Fragment fg = getFragmentManager().findFragmentById(R.id.file_content);
        if (fg != null) {
            fg.onDetach();
        }

        Log.i(TAG, "view local  file info....");
        // viewTypeTag.setText(getString(R.string.all_file_view_type));
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment newFragment = new CifsFragment(this);
        ft.replace(R.id.file_content, newFragment, CifsFragment.TAG);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public static class CifsFragment extends Fragment {
        public final static String TAG = "CifsFragment";
        private Activity activity;
        private ProgressBar bar;
        private ListView listView;
        private List<LanNodeInfo> servers = new ArrayList<LanNodeInfo>();

        public CifsFragment(Activity activity) {
            this.activity = activity;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
            View mRootView = inflater.inflate(R.layout.smb_device_list, container, false);

            listView = (ListView)mRootView.findViewById(R.id.list_view);
            bar = (ProgressBar)activity.findViewById(R.id.progress_bar);
            // Log.i(TAG, "onCreateView.listView:" + listView);

            CifsLanBrowserAdpter.LanBrowserAdapter adpter = new CifsLanBrowserAdpter.LanBrowserAdapter(
                    activity, servers);
            listView.setAdapter(adpter);

            Log.i(TAG, "listView:" + listView);
            Handler handler = new CifsLanBrowserAdpter.BakLanBrowserHandler(activity, bar,
                    listView, servers);
            Runnable r = new CifsLanBrowserAdpter.BakLanBrowserRunnable(handler);
            new Thread(r).start();

            listView.setOnItemClickListener(new ItemClick(activity, servers));
            return mRootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

    static class ItemClick implements OnItemClickListener {
        private List<LanNodeInfo> servers;
        private Context context;

        public ItemClick(Context context, List<LanNodeInfo> servers) {
            this.servers = servers;
            this.context = context;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "ItemClick...");
            LanNodeInfo node = servers.get(position);

            List<FileInfo> files = ShareFile.asynRequestShareFile(node.ip, null, null);
            if (files != null) {
                // no password request success.
                Log.i(TAG, "without password get shara list.size:" + files.size());
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(DATA, (ArrayList<FileInfo>)files);
                intent.putExtra(NODE, node);
                intent.setClass(context, CifsBrowserActivity.class);
                context.startActivity(intent);
            } else {
                // pop account input dialog.
                showInputUserInfoDialog(context, R.style.show_choose_type_dialog, node);
            }
        }
    }

    public static void showInputUserInfoDialog(final Context context,
            int theme, final LanNodeInfo node) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.smb_input_user_info_dialog, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "ip adr is:" + node);
                        EditText nameEdit = (EditText)view.findViewById(R.id.user_name);
                        EditText passEdit = (EditText)view.findViewById(R.id.user_password);
                        
                        String name = nameEdit.getText().toString();
                        String pass = passEdit.getText().toString();
                        
                        ArrayList<FileInfo> files = (ArrayList<FileInfo>)ShareFile.asynRequestShareFile(node.ip, name, pass);
                        Log.i(TAG, "get shara list.size:" + (files != null ? files.size() : " files is null"));

                        Intent intent = new Intent();
                        intent.putParcelableArrayListExtra(DATA, files);
                        intent.putExtra(NODE, node);
                        intent.setClass(context, CifsBrowserActivity.class);
                        context.startActivity(intent);
                    }
                });
        alertDialogBuilder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialogBuilder.create().show();
    }

}
