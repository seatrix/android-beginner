package com.exam.slieer.utils.jni;

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
        
        Object o = null;
    }

}
