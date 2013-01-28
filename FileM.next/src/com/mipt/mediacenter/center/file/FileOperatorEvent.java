
package com.mipt.mediacenter.center.file;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mipt.fileMgr.R;
import com.mipt.mediacenter.center.DirViewFragment;
import com.mipt.mediacenter.center.server.FileSortHelper;
import com.mipt.mediacenter.center.server.FileSortHelper.SortMethod;

public class FileOperatorEvent {
    private final static String TAG = "FileOperatorEvent";
    
    public enum OrderStyle{
        OrderbySize, OrderbyType, OrderbyAlphabetical, OrderbyLastModified
        
    }
    public static void onClickOrder(final View v, final Activity activity) {
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

    public void onClickSelect(View v) {

    }

    public void onClickCopy(View v) {

    }

    public void onClickDelete(View v) {

    }

    public void onClickUndo(View v) {

    }
    
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
                    .findFragmentByTag("dirViewFragment");
            
            Log.i(TAG, "f:" + ", f1:" + f1);
            
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
