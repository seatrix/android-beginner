package com.slieer;

import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class DirOp {

    public static void main(String argv[]) {
        try {
            dirDetail();
            //localNet();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            System.out.println("NtStatus:" + e.getNtStatus());
            System.out.println("Message:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void dirDetail() throws MalformedURLException, SmbException {
        //String smbStr = "smb://slieer:slieer@192.168.51.8/android-sdk-windows/";
        String smbStr = "smb://slieer:slieer@192.168.51.8/";
        smbStr = "smb://slieer:slieer@192.168.1.100/Users/";
        
        smbStr = "smb://192.168.51.44/";
        SmbFile f = new SmbFile(smbStr);
        if(f.isDirectory()){
            SmbFile[] files = f.listFiles();
            for(int i = 0; i < files.length; i++){
                System.out.println(files[i].toString() + ":" + files[i].getLastModified() + ":" + files[i].length());
            }
        }
    }
    
    private static void localNet() throws MalformedURLException, SmbException{
        jcifs.Config.setProperty( "jcifs.encoding", "GBK");
        String smbStr = "smb://";
        smbStr = "smb://workgroup/";
        
        jcifs.Config.setProperty( "jcifs.encoding", "GBK");
        SmbFile f = new SmbFile(smbStr);
        //SmbFile f = new SmbFile(smbStr, new NtlmPasswordAuthentication("workgroup","slieer","slieer"));
        SmbFile[] servers = f.listFiles();
        
        System.out.println(servers.length);
    }
}
