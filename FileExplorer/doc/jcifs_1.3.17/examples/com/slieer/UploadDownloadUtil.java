package com.slieer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class UploadDownloadUtil {

    /**
     * 从共享目录拷贝文件到本地
     * 
     * @param remoteUrl
     *            共享目录上的文件路径
     * @param localDir
     *            本地目录
     */
    public static void smbGetFile(String remoteUrl, String localDir) {
        InputStream in = null;
        OutputStream out = null;
        try {
            SmbFile remoteFile = new SmbFile(remoteUrl);
            remoteFile.connect();
            String fileName = remoteFile.getName();
            File localFile = new File(localDir + File.separator + fileName);
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从本地上传文件到共享目录
     * 
     * @Version1.0 Sep 25, 2009 3:49:00 PM
     * @param remoteUrl
     *            共享文件目录
     * @param localFilePath
     *            本地文件绝对路径
     */
    public static void smbPut(String remoteUrl, String localFilePath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(localFilePath);
            System.out.println(localFile.getAbsolutePath());
            String fileName = localFile.getName();
            SmbFile remoteFile = new SmbFile(remoteUrl + "/" + fileName);
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void smbGetDir() {

    }

    public static void main(String[] args) {
        String smbStr = "smb://slieer:slieer@192.168.51.2/android-sdk-windows/SDK Readme.txt";
        UploadDownloadUtil.smbGetFile(smbStr,"D:/temp");

        
        // smb:域名;用户名:密码@目的IP/文件夹/文件名.xxx
        // test.smbGet("smb://szpcg;jiang.t:xxx@192.168.193.13/Jake/test.txt",
        // "c://") ;

        // 用户名密码不能有强字符，也就是不能有特殊字符，否则会被作为分断处理
        /*
         * test.smbGet(
         * "smb://CHINA;xieruilin:123456Xrl@10.70.36.121/project/report/网上问题智能分析助手使用文档.doc"
         * , "c://Temp/");
         */
    }

}