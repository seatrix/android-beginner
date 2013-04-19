package com.exam.slieer.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.exam.slieer.utils.LinuxMountCifs;
import com.exam.slieer.utils.Utils;

public class TestCommand extends Activity {
	private static String TAG = "TestCommand";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.testString();
		
		LinearLayout layout = new LinearLayout(this);
		Button btn = new Button(this);
		btn.setText("up");
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//exec();
					//exec1();
					String user = "slieer"; 
					String password = "slieer"; 
					String remotePath = "192.168.51.33/linux-share", 
					targetDir = LinuxMountCifs.mountA4Path(remotePath);
					
					LinuxMountCifs.mount(user, password, remotePath, targetDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		layout.addView(btn);
		setContentView(layout);
	}

//	static void exec() throws Exception {
//		Runtime runtime = Runtime.getRuntime();
//		Process process = runtime.exec("chmod 777 /mnt");
//		int status = process.waitFor();
//		Log.i(TAG, "status:" + status);
//		if (status == 0) {
//			Log.i(TAG, "chmod success");
//		}
//		print(process);
//	}
//	
//	static void exec1() throws Exception{
//		Runtime runtime = Runtime.getRuntime();
//		Process process = runtime.exec("mount");
//		int status = process.waitFor();
//		Log.i(TAG, "status:" + status);
//		
//		print(process);
//	}
//
//	private static void print(Process process) throws IOException {
//		InputStream is = process.getInputStream();
//		LineNumberReader input = new LineNumberReader (new InputStreamReader(is));
//
//		String line = null;
//		while ((line = input.readLine ()) != null){
//			Log.i(TAG, line);
//		}
//	}	
}
