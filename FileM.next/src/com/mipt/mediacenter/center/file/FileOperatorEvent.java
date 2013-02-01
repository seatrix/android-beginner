
package com.mipt.mediacenter.center.file;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.DirViewFragment;
import com.mipt.mediacenter.center.FileItemAdapter;
import com.mipt.mediacenter.center.server.FileInfo;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.MediacenterConstant;
import com.mipt.mediacenter.center.server.FileSortHelper.SortMethod;
import com.mipt.mediacenter.utils.FileOperationHelper;
import com.mipt.mediacenter.utils.ToastFactory;

public class FileOperatorEvent {
    private final static String TAG = "FileOperatorEvent";
    public final static String MODEL_TAG = "model";
    public final static String COPY_STATUS_TAG = "copy_status";
    
    public enum Model{UNKNOWN,DEFAULT_BROSWER_MODEL, SELECT_MODEL};
    
    public enum OrderStyle{
        OrderbySize, OrderbyType, OrderbyAlphabetical, OrderbyLastModified
        
    }
    public static List<FileInfo> SELECTED_FILES = new ArrayList<FileInfo>();
    
    public static void onClickOrder(final Activity activity, final View v) {
        final Context context = v.getContext();
        String[] vals = context.getResources().getStringArray(R.array.file_order_style);
        FileOdrerStyleDialog fDialog = new FileOdrerStyleDialog(context,
                R.style.show_choose_type_dialog, vals);
        fDialog.show();
        fDialog.getListView().setOnItemClickListener(new OnClickOrderStyle(activity, fDialog));
        fDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }

    public void onClickBookMark(View v) {

    }

    public static void onClickSelect(Activity activity, View v) {
        switchoverView(activity, Model.SELECT_MODEL);
    }
    
    //copy or plaster.
    public static void onClickCopy(Activity activity, View v) {
        Log.i(TAG, "onClickCopy...");
        Context cxt = v.getContext();
        if(SELECTED_FILES.size() == 0){
            ToastFactory.getInstance()
            .getToast(cxt, cxt.getString(R.string.cm_selected_toast)).show();
            return ;
        }
        
        SharedPreferences share = cxt.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Resources res = v.getContext().getResources();
        Button button = (Button)v;
        if(share.getInt(COPY_STATUS_TAG, -1) == -1){
            //click copy button.
            String text = res.getString(R.string.right_menu_plaster);
            button.setText(text);
            
            switchoverView(activity, Model.DEFAULT_BROSWER_MODEL);
            
            SharedPreferences.Editor ed = share.edit();
            ed.putInt(COPY_STATUS_TAG, -1);
            ed.commit();
        }else{
            //
            
            //click plaster button.
            String text = res.getString(R.string.right_menu_plaster);
            button.setText(text);
            
            //如果是当前目录
            
            //其它目录
            
            //粘贴成功时
        }

        
        
    }

    public static void onClickDelete(Activity activity, View v) {
        Log.i(TAG, "onClickDelete...");
        Context cxt = v.getContext();
        if(SELECTED_FILES.size() == 0){
            ToastFactory.getInstance()
            .getToast(cxt, cxt.getString(R.string.cm_selected_toast)).show();
            return ;
        }else{
            //确认是否要做删除
            ToastFactory.getInstance()
            .getToast(cxt, cxt.getString(R.string.cm_selected_toast)).show();
        }
        
        FileOperationHelper helper = new FileOperationHelper(null);
        List<File> fileList = new ArrayList<File>();
        for(FileInfo file : SELECTED_FILES){
            File f = new File(file.filePath);
            fileList.add(f);
        }
        helper.Delete(SELECTED_FILES);
        
        DirViewFragment f1 = (DirViewFragment) activity.getFragmentManager()
                .findFragmentByTag(DirViewFragment.TAG);
        f1.getFileViewInteractionHub().refreshFileList();
    }

    /*    public void onClickUndo(View v) {

    }
    */
    
    public static void switchoverView(Activity activity, Model model){
        DirViewFragment f1 = (DirViewFragment) activity.getFragmentManager()
                .findFragmentByTag(DirViewFragment.TAG);
        
        String currentPath = f1.getFileViewInteractionHub().getCurrentPath();
        int type = f1.getArguments().getInt(MediacenterConstant.INTENT_TYPE_VIEW);
        //FileItemAdapter.SELECT_FLAG = true;
        setModel(activity, model);
        
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        DirViewFragment newFragment = DirViewFragment.newInstance(currentPath, type);
        ft.replace(R.id.file_content, newFragment, DirViewFragment.TAG);
        ft.commit();

/*
        GridView grid = (GridView)f1.getView().findViewById(R.id.file_content);
 *         FileItemAdapter adapter = (FileItemAdapter)grid.getAdapter();        
        FileItemAdapter newSelectAdapter = new FileItemAdapter(activity, adapter.getDataList());
        grid.setAdapter(newSelectAdapter);
        if(model.equals(Model.SELECT_MODEL)){
            grid.setOnItemClickListener(new SelectFile(activity));
        }else if(model.equals(Model.DEFAULT_BROSWER_MODEL)){
            //grid.setOnItemClickListener(listener);
            FileViewInteractionHub hub = f1.getFileViewInteractionHub();
            hub.setupFileListView();
            //hub.refreshFileList();
        }
*/    }
    
    public static void setModel(Activity activity, Model model){
        SharedPreferences.Editor ed = activity.getSharedPreferences(TAG, Activity.MODE_PRIVATE).edit();
        ed.putInt(MODEL_TAG, model.ordinal());
        ed.commit();
    }
    
    public static Model getModel(Activity activity){
        SharedPreferences ed = activity.getSharedPreferences(TAG, Activity.MODE_PRIVATE);
        int m = ed.getInt(MODEL_TAG, Model.UNKNOWN.ordinal());
        return Model.values()[m];
    }
        
    public static class SelectFile implements OnItemClickListener{
        private Activity activity;
        
        public SelectFile(Activity activity){
            this.activity = activity;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CheckBox box = (CheckBox)view.findViewById(R.id.select);
            //Log.i(TAG, box.toString());
            box.setChecked(true);
            
            DirViewFragment f1 = (DirViewFragment) this.activity.getFragmentManager()
                    .findFragmentByTag(DirViewFragment.TAG);
            FileInfo info = (FileInfo)f1.getAllFiles().toArray()[position];
            SELECTED_FILES.add(info);
        }
    };
    
    
    private static class OnClickOrderStyle implements OnItemClickListener {
        private Dialog dialog;
        private Activity activity;
        
        public OnClickOrderStyle(Activity activity, Dialog dialog){
            this.dialog = dialog;
            this.activity = activity;
        }
        
        @Override
        public void onItemClick(AdapterView<?> arg0,
                View arg1, int arg2, long arg3) {
            
            //DirViewFragment f = (DirViewFragment) activity.getFragmentManager()
                    //.findFragmentById(R.id.tabcontent);
            DirViewFragment f1 = (DirViewFragment) activity.getFragmentManager()
                    .findFragmentByTag(DirViewFragment.TAG);
            //Log.i(TAG, "f:" + ", f1:" + f1);
            
            final String name = (String) arg0
                    .getItemAtPosition(arg2);
            
            Log.i(TAG, "position:" + arg2 + ",name:" + name + ",itemId:" + arg3);
            OrderStyle index = OrderStyle.values()[arg2];
            SortMethod sortMethod = null;
            switch (index) {
                case OrderbySize:
                    sortMethod =  SortMethod.size;
                    break;
                case OrderbyType:
                    sortMethod =  SortMethod.type;
                    break;

                case OrderbyAlphabetical:
                    sortMethod =  SortMethod.name;
                    break;
                case OrderbyLastModified:
                    sortMethod =  SortMethod.date;
                    break;

                default:
                    break;
            }
            
            FileSortHelper helper = FileSortHelper.getInstance();
            helper.setSortMethod(sortMethod);
            f1.sortCurrentList(helper);
            
            dialog.dismiss();
        }

    }
    
    private static class FileOdrerStyleDialog extends Dialog {
        private Context context;
        private String[] mStrings;
        private ArrayAdapter<String> adapter;
        private TextView title;
        private ListView lv;
        
        public FileOdrerStyleDialog(Context _context, int theme,String[] strs) {
            super(_context, theme);
            this.context = _context;
            mStrings = strs;
        }
        
        public ListView getListView(){
            return lv;
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.cm_pop_dialog);
            iniUI();
        }

        private void iniUI() {
            lv = (ListView) findViewById(R.id.tpe_item_select);
            lv.setDividerHeight(0);
            title = (TextView) findViewById(R.id.item_select_title_tag);
            title.setText(context.getString(R.string.file_order_pop_title));
            adapter = new ArrayAdapter<String>(context, R.layout.cm_pop_item,
                    mStrings);
            lv.setAdapter(adapter);
            
            //OnClickOrderStyle
        }
    }
    
}
