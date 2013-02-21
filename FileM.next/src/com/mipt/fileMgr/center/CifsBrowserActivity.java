package com.mipt.fileMgr.center;

import java.util.ArrayList;
import java.util.List;

import com.mipt.fileMgr.R;
import com.mipt.fileMgr.center.CifsActivity.CifsFragment;
import com.mipt.mediacenter.center.server.FileInfo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
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

public class CifsBrowserActivity extends Activity {
    public static final String TAG = "CifsBrowserActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cm_file_list);
        
        TextView currentPath = (TextView)findViewById(R.id.current_path_tag);
        String ip = getIntent().getStringExtra(CifsActivity.IP);
        currentPath.setText("所有设备/".concat(ip));
        
        ArrayList<FileInfo> list = getIntent().getParcelableArrayListExtra(CifsActivity.DATA);
        if(list == null || list.size() == 0){
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setContentView(R.layout.cm_file_list);
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
    
    private static class CifsBrowserFragment extends Fragment{
        private Context context;
        private List<FileInfo> fileInfos;
        public CifsBrowserFragment(Context container, List<FileInfo> fileInfos){
            this.context = container;
            this.fileInfos = fileInfos;
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View mRootView = inflater.inflate(R.layout.smb_device_list, container, false);

            ListView listView = (ListView)mRootView.findViewById(R.id.list_view);
            listView.setAdapter(new Adapt(context, fileInfos));
            listView.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FileInfo fileInfo = fileInfos.get(position);
                    String mountPath = fileInfo.filePath;
                    Toast.makeText(context, "mountPath:" + mountPath, Toast.LENGTH_SHORT).show();
                }
                
            });
            return mRootView;
        }
    }
    
    static class Adapt extends ArrayAdapter<FileInfo>{

        public Adapt(Context context, List<FileInfo> objects) {
            super(context,R.layout.smb_device_list_item, objects);
        }
        
        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
