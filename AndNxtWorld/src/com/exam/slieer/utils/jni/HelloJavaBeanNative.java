package com.exam.slieer.utils.jni;

import com.exam.slieer.utils.bean.User;

public class HelloJavaBeanNative {
    static {
        System.loadLibrary("HelloNative");
    }
    //public native int get();
    //public native void set(int i);
    
    public native void setUser(String userName);
    public native User getUser();
    
    //public native ArrayList<User> getUserList();
    //public native void setUserList(ArrayList<User> userList);
}
