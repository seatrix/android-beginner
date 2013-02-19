package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.CifsLanBrowserAdpter;
import com.mipt.mediacenter.utils.cifs.LanNodeInfo;

public class CifsActivity extends Activity {
    private static final String TAG = "CifsActivity";
    private TextView currentPath;
    //private TextView viewTypeTag;
    //private TextView currentNum;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cm_file_list);

        currentPath = (TextView) findViewById(R.id.current_path_tag);
        currentPath.setText("所有设备/网络共享设备");
        //viewTypeTag = (TextView) findViewById(R.id.view_type_tag);
        //currentNum = (TextView) findViewById(R.id.current_num_tag);
        
        Fragment fg = getFragmentManager().findFragmentById(R.id.file_content);
        if (fg != null) {
            fg.onDetach();
        }
        
        Log.i(TAG, "view local  file info....");
        //viewTypeTag.setText(getString(R.string.all_file_view_type));

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment newFragment = new CifsFragment(this);
        ft.replace(R.id.file_content, newFragment, CifsFragment.TAG);
        ft.commit();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }
    
    public static class CifsFragment extends Fragment{
        public final static String TAG = "CifsFragment";
        private Activity activity;
        private ProgressBar bar; 
        private ListView listView;
        private List<LanNodeInfo> servers = new ArrayList<LanNodeInfo>();
        
        public CifsFragment(Activity activity){
            this.activity = activity;
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
            View mRootView = inflater.inflate(R.layout.smb_device_list, container,
                    false);
            
            listView = (ListView) mRootView.findViewById(R.id.list_view);
            bar = (ProgressBar)mRootView.findViewById(R.id.progress_bar);
            //Log.i(TAG, "onCreateView.listView:" + listView);
            
            CifsLanBrowserAdpter.LanBrowserAdapter adpter = new CifsLanBrowserAdpter
                    .LanBrowserAdapter(activity, servers); 
            listView.setAdapter(adpter);
            
            Log.i(TAG, "listView:" + listView);
            Handler handler = new CifsLanBrowserAdpter
                    .BakLanBrowserHandler(activity, bar, listView, servers);
            Runnable r = new CifsLanBrowserAdpter.BakLanBrowserRunnable(handler);
            new Thread(r).start();

            listView.setOnItemClickListener(new ItemClick(servers));
            return mRootView;
        }
        
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }
    
    static class ItemClick implements OnItemClickListener{
        private List<LanNodeInfo> servers;
        public ItemClick(List<LanNodeInfo> servers){
            this.servers = servers;
        }
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "");
        }
        
    }
}
