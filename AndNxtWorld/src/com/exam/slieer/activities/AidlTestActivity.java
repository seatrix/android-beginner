
package com.exam.slieer.activities;

import com.exam.slieer.R;
import com.exam.slieer.services.MyService;
import com.exam.slieer.services.ForActivity;
import com.exam.slieer.services.ForService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AidlTestActivity extends Activity {
    private static final String TAG = "AIDLActivity";
    private Button btnOk;
    private Button btnCancel;
    private Button btnCallBack;

    private ForService mService;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.aidl_test_activity);
        btnOk = (Button)findViewById(R.id.btn_ok);
        btnCancel = (Button)findViewById(R.id.btn_cancel);
        btnCallBack = (Button)findViewById(R.id.btn_callback);

        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle args = new Bundle();
                Intent intent = new Intent(AidlTestActivity.this, MyService.class);
                intent.putExtras(args);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                startService(intent);
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                unbindService(mConnection);
                // stopService(intent);
            }
        });
        btnCallBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mService.invokCallBack();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private ForActivity mCallback = new ForActivity.Stub() {
        public void performAction() throws RemoteException {
            Toast.makeText(AidlTestActivity.this, "this toast is called from service", 1).show();
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ForService.Stub.asInterface(service);
            try {
                mService.registerTestCall(mCallback);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "disconnect service");
            mService = null;
        }
    };
}
