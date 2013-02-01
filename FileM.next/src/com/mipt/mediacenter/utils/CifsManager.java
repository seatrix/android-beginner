package com.mipt.mediacenter.utils;

import java.util.List;

import android.content.Context;

import com.mipt.mediacenter.center.server.DeviceInfo;

public class CifsManager {
    private final static CifsManager INSTANCE = new CifsManager();
    private CifsManager(){
        
    }
    
    public static CifsManager getInstance(){
        return INSTANCE;
    }
    
    //用户成功登录的的, 计出数据库
    public void addDevice(DeviceInfo info){
        
    }
    
    public List<DeviceInfo> getDevices(Context context){
        
        
        return null;
    }
}
