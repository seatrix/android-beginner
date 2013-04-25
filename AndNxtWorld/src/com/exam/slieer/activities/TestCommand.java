package com.exam.slieer.activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.exam.slieer.utils.Utils;
import com.exam.slieer.utils.jni.HelloNative;
import com.exam.slieer.utils.jni.TestNativeCodes;

public class TestCommand extends Activity {
	private static String TAG = "TestCommand";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		LinearLayout layout = new LinearLayout(this);
		Button btn = new Button(this);
		btn.setText("up");
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
				    //Utils.testString();
					//exec();
					//exec1();
				    TestNativeCodes.testHelloNative();
				    
				    //execMount();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		layout.addView(btn);
		setContentView(layout);
	}
	
	static void execMount(){
	    String cmdA4 = "mount -t iso9660 -o loop /mnt/usbhost1/test/rhel-server-6.0-x86_64-boot.iso /mnt/iso/";
	    
	    String cmdA8 = "mount -t iso9660 -o loop /mnt/usbhost1/test/rhel-server-6.0-x86_64-boot.iso /mnt/iso/";
	    String cifsCommand = "mount -t cifs -o username=\"slieer\",password=\"slieer\" //192.168.51.59/linux-share /mnt/smb/";
	    String cmd = cifsCommand;
	    try {
            exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
    private static void exec(String cmd) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        int status = process.waitFor();
        Log.i(TAG, "status:" + status);
        print(process);
        printError(process);
    }

    private static void print(Process process) throws IOException {
        InputStream is = process.getInputStream();
        LineNumberReader input = new LineNumberReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            Log.i(TAG, line);
        }
    } 

    
    private static void printError(Process process) throws IOException {
        InputStream is = process.getErrorStream();
        LineNumberReader input = new LineNumberReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            Log.i(TAG, line);
        }
    }
}
