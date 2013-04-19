package com.exam.slieer.utils;

import java.io.File;

import android.util.Log;

public class Utils {
    final static String TAG = "Utils";
    public static void testString(){
        String path = "/mnt/sdb/sdb1";
        path = "/storage/external_storage/sda1";
        String p = "Planet Earth Natural World Desert Lions & Snow Leopard 2006 Blu-ray 1080i VC-1 DTS-HD HR 5.1-TTG";
        
        path  = path + "/" + p;
        File f = new File(path);
        for(File ff : f.listFiles()){
            String string = testStringSplit(ff.getAbsolutePath());
            Log.i(TAG, string);
        }
    }
    
    /**
     * 友好的显示文件路径
     * @param fileName
     * @return
     */
    public static String testStringSplit(String fileName){
        //String fileName = "/mnt/sdb/sdb1/台湾阿里山视频";
        
        String[] arr = fileName.split("/");
        if(arr.length > 4){
            //String dirDepth0 = arr[0];
            String dirDepth1 = arr[1];
            String dirDepth2 = arr[2];
            String dirDepth3 = arr[3];
            String name = arr[arr.length -1];
            Log.i(TAG, "" + name.length());
            if(name.length() > 15){
                name = "...".concat(name.substring(name.length() - 10));
            }
            //Log.i(TAG,dirDepth0);
            //Log.i(TAG,dirDepth1);
            //Log.i(TAG,dirDepth2);
            //Log.i(TAG,dirDepth3);
            //Log.i(TAG,dirDepthEnd);
            
            fileName = new StringBuilder()
                .append("/").append(dirDepth1)
                .append("/").append(dirDepth2)
                .append("/").append(dirDepth3)
                .append("/").append("...")
                .append("/").append(name)
                .toString();
            
        }
        return fileName;
    }

}
