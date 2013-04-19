package com.exam.slieer.utils.jni;

public class HelloNative {
    public int[] arrays = { 1, 2, 3, 3, 4, 5, 6, 7, 8, 9 };
    public String message = "before string";

    public native void callCppFunction();

    public int getArrayLen() {
        return arrays.length;
    }
}
