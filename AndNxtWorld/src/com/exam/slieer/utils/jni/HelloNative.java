package com.exam.slieer.utils.jni;

public class HelloNative {
    public int[] arrays = { 7, 9, 3, 3, 4, 5, 6, 7, 8, 1 };
    public String message = "before string";

    public native void callCppFunction();

    public int getArrayLen() {
        return arrays.length;
    }
    
    static {
        System.loadLibrary("HelloNative");
    }
}
