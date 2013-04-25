package com.exam.slieer.utils.jni;

public class HelloNextNative {
    static {
        System.loadLibrary("HelloNative");
    }

    public native int getInt();

    public native void setInt(int i);
}