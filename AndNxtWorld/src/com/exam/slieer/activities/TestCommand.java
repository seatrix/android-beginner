package com.exam.slieer.activities;

import android.app.Activity;
import android.os.Bundle;
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		layout.addView(btn);
		setContentView(layout);
	}
}
