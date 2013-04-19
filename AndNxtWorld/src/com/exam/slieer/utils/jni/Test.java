package com.exam.slieer.utils.jni;

public class Test {
    static {
        System.loadLibrary("libHelloNative");
    }

    public static void main(String[] args) {
        HelloNative obj = new HelloNative();
        System.out.println(obj.message + "call before ");
        obj.callCppFunction();
        System.out.println(obj.message + "call end");
        for (int each : obj.arrays)
            System.out.println(each);
    }

}
