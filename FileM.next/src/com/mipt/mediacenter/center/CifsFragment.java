package com.mipt.mediacenter.center;

import java.util.ArrayList;
import java.util.List;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.utils.cifs.LanNodeInfo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CifsFragment extends Fragment{
    private Activity activity; 
    private List<LanNodeInfo> servers = new ArrayList<LanNodeInfo>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = this.getActivity();
        // this.getListView().setBackgroundResource(R.drawable.cm_view_background);
        View rootView = inflater.inflate(R.layout.cm_device_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.device_content);
        CifsLanBrowserAdpter.LanBrowserAdapter adpter = new CifsLanBrowserAdpter.LanBrowserAdapter(activity, servers); 
        listView.setAdapter(adpter);
        
        
        return rootView;
    }
}
