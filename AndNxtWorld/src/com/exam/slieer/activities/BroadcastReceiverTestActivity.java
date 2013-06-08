package com.exam.slieer.activities;

import com.exam.slieer.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BroadcastReceiverTestActivity extends Activity {
    Button btnInternal1,btnInternal2,btnSystem;
    static final String INTENAL_ACTION_1 = "com.testBroadcastReceiver.Internal_1";
    static final String INTENAL_ACTION_2 = "com.testBroadcastReceiver.Internal_2";
    static final String INTENAL_ACTION_3 = "com.testBroadcastReceiver.Internal_3";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broadcast_test);
        btnInternal1=(Button)this.findViewById(R.id.Button01);
        btnInternal1.setOnClickListener(new ClickEvent());
        btnInternal2=(Button)this.findViewById(R.id.Button02);
        btnInternal2.setOnClickListener(new ClickEvent());
        btnSystem=(Button)this.findViewById(R.id.Button03);
        btnSystem.setOnClickListener(new ClickEvent());
        //动态注册广播消息
        registerReceiver(bcrIntenal1, new IntentFilter(INTENAL_ACTION_1));
    }
    class ClickEvent implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if(v==btnInternal1)//给动态注册的BroadcastReceiver发送数据
            {
                Intent intent = new Intent(INTENAL_ACTION_1);
                sendBroadcast(intent);
            }
            else if(v==btnInternal2)//给静态注册的BroadcastReceiver发送数据
            {
                Intent intent = new Intent(INTENAL_ACTION_2);
                sendBroadcast(intent);
            }
            else if(v==btnSystem)//动态注册 接收2组信息的BroadcastReceiver
            {
                IntentFilter filter = new IntentFilter();//
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);//系统电量检测信息
                filter.addAction(INTENAL_ACTION_3);//第三组自定义消息
                registerReceiver(batInfoReceiver, filter);
                
                Intent intent = new Intent(INTENAL_ACTION_3);
                intent.putExtra("Name", "hellogv");
                intent.putExtra("Blog", "http://blog.csdn.net/hellogv");
                sendBroadcast(intent);//传递过去
            }
        }
        
    }
    
    /*
     * 接收动态注册广播的BroadcastReceiver
     */
    private BroadcastReceiver bcrIntenal1 = new BroadcastReceiver() {
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(context, "动态:"+action, 1000).show();
        }
    };
    

    private BroadcastReceiver batInfoReceiver = new BroadcastReceiver() {
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //如果捕捉到的action是ACTION_BATTERY_CHANGED
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                //当未知Intent包含的内容，则需要通过以下方法来列举
                Bundle b=intent.getExtras();
                Object[] lstName=b.keySet().toArray();

                for(int i=0;i<lstName.length;i++)
                {
                    String keyName=lstName[i].toString();
                    Log.e(keyName,String.valueOf(b.get(keyName)));
                }
            }
            //如果捕捉到的action是INTENAL_ACTION_3
            if (INTENAL_ACTION_3.equals(action)) {
                //当未知Intent包含的内容，则需要通过以下方法来列举
                Bundle b=intent.getExtras();
                Object[] lstName=b.keySet().toArray();

                for(int i=0;i<lstName.length;i++)
                {
                    String keyName=lstName[i].toString();
                    Log.e(keyName,b.getString(keyName));
                }
            }
        }
    };


}