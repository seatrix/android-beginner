package com.exam.slieer.utils.jni;

import com.exam.slieer.utils.bean.User;

import android.util.Log;

public class TestNativeCodes {
    public final static String TAG = "TestNativeCodes";
    
    public static void testHelloNative() {
        HelloNative obj = new HelloNative();
        Log.i(TAG, obj.message + "call before ");
        obj.callCppFunction();
        Log.i(TAG, obj.message + "call end");
        for (int each : obj.arrays)
            Log.i(TAG, "" + each);
        
        helloNextNative();
        
        helloJavaBeanNative();
    }

    public static void helloNextNative(){
        HelloNextNative h = new HelloNextNative();
        Log.i(TAG, "getInt:" + h.getInt());
        h.setInt(1000);
        Log.i(TAG, "getInt:" + h.getInt());
    }
    
    public static void helloJavaBeanNative(){
        Log.i(TAG, "helloJavaBeanNative...");
        HelloJavaBeanNative h = new HelloJavaBeanNative();
        h.setUser("slieer");
        Log.i(TAG, "setUser execute finish !");
        
        //Log.i(TAG, h.getUser().toString());
        
        User user = h.getUser();
        Log.i(TAG, "userName:" + user.getUserName());
    }
}
