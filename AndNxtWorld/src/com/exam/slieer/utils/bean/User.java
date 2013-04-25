package com.exam.slieer.utils.bean;

/**
 * jni test bean.
 * 
 * @author slieer
 * Create Date 2013-4-25
 * version 1.0
 */
public class User {
    public long id;
    public String userName;
    public boolean isMan;
    public int age;

    public void init(){
        
    }
    
    public User() {
    }

    public User(long id, String userName, boolean isMan, int age) {
        super();
        this.id = id;
        this.userName = userName;
        this.isMan = isMan;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", userName=" + userName + ", isMan=" + isMan
                + ", age=" + age + "]";
    }
}