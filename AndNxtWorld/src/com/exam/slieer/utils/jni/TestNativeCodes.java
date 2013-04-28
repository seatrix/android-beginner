package com.exam.slieer.utils.jni;

import java.io.PrintWriter;

import com.exam.slieer.utils.bean.User;
import com.exam.slieer.utils.jni.corejava8jni.Printf2;
import com.exam.slieer.utils.jni.corejava8jni.Printf4;

import android.util.Log;

public class TestNativeCodes {
    public final static String TAG = "TestNativeCodes";
    static {
        System.loadLibrary("HelloNative");
    }
    
    public static void testHelloNative() {
        /**
         * 
        HelloNative obj = new HelloNative();
        Log.i(TAG, obj.message + "call before ");
        obj.callCppFunction();
        Log.i(TAG, obj.message + "call end");
        for (int each : obj.arrays)
            Log.i(TAG, "" + each);
        
        helloNextNative();
        helloJavaBeanNative();
         */
        
        
        corejava8Native();
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
    
    public static void corejava8Native() {
        {
            double price = 44.95;
            double tax = 7.75;
            double amountDue = price * (1 + tax / 100);

            String s = Printf2.sprint("Amount due = %8.2f", amountDue);
            System.out.println(s);
        }
        {
            double price = 44.95;
            double tax = 7.75;
            double amountDue = price * (1 + tax / 100);
            PrintWriter out = new PrintWriter(System.out);
            Printf2.fprint(out, "Amount due = %8.2f\n", amountDue);
            out.flush();
        }
        {
            double price = 44.95;
            double tax = 7.75;
            double amountDue = price * (1 + tax / 100);
            PrintWriter out = new PrintWriter(System.out);
            /* This call will throw an exception--note the %% */
            Printf4.fprint(out, "Amount due = %%8.2f\n", amountDue);
            out.flush();
        }

    }
}
