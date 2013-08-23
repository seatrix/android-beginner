package com.exam.jni;

import java.util.List;

public class Order {
    static class Stu{
        int id;
        String name;
    }
    
    
    public static native void sort(List<String> list); 
    
    public static native void sortInts(int[] ch);
    
    public static native void sortStr(String ch);
    /**
    native method not support overload.
    public static native void sort(List<Stu> list);
    */

    public static native void sortStu(List<Stu> list);
}
