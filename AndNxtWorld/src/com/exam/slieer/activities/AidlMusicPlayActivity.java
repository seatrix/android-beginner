
package com.exam.slieer.activities;

import com.exam.slieer.R;
import com.exam.slieer.services.MusicService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AidlMusicPlayActivity extends Activity {

    // 为日志工具设置标签
    private static String TAG = "MusicService";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aidl_test_play_music);

        // 输出Toast消息和日志记录
        Toast.makeText(this, "MusicServiceActivity", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "MusicServiceActivity");

        initlizeViews();
    }

    private void initlizeViews() {
        Button btnStart = (Button)findViewById(R.id.startMusic);
        Button btnStop = (Button)findViewById(R.id.stopMusic);
        Button btnBind = (Button)findViewById(R.id.bindMusic);
        Button btnUnbind = (Button)findViewById(R.id.unbindMusic);

        // 定义点击监听器
        OnClickListener ocl = new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 显示指定 intent所指的对象是个 service
                Intent intent = new Intent(AidlMusicPlayActivity.this, MusicService.class);
                switch (v.getId()) {
                    case R.id.startMusic:
                        // 开始服务
                        startService(intent);
                        break;
                    case R.id.stopMusic:
                        // 停止服务
                        stopService(intent);
                        break;
                    case R.id.bindMusic:
                        // 绑定服务
                        bindService(intent, conn, Context.BIND_AUTO_CREATE);
                        break;
                    case R.id.unbindMusic:
                        // 解绑服务
                        unbindService(conn);
                        break;
                }
            }
        };

        // 绑定点击监听
        btnStart.setOnClickListener(ocl);
        btnStop.setOnClickListener(ocl);
        btnBind.setOnClickListener(ocl);
        btnUnbind.setOnClickListener(ocl);
    }

    // 定义服务链接对象
    final ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(AidlMusicPlayActivity.this, "MusicServiceActivity onSeviceDisconnected",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "MusicServiceActivity onSeviceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(AidlMusicPlayActivity.this, "MusicServiceActivity onServiceConnected",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "MusicServiceActivity onServiceConnected");
        }
    };
}
