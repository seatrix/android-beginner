package com.slieer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class ReadShareFile {

	public static void main(String[] args) {
		// getFile();
		try {
			getFile1();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SmbException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getFile1() throws MalformedURLException, SmbException, UnknownHostException {
		byte buffer[] = new byte[1024];
		int readed = 0;
		String smbUrl = "smb://" + Constants.IP
				+ "/linux-share/XcapService.java";
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
				"workgroup", "slieer", "slieer");
		
		SmbFile file = new SmbFile(smbUrl, auth);
		SmbFileInputStream in = new SmbFileInputStream(file); // 建立smb文件输入流
		try {
			while ((readed = in.read(buffer)) != -1) {
				System.out.write(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void getFile() {
		byte buffer[] = new byte[1024];
		int readed = 0;
		SmbFileInputStream in = null;
		String smbUrl = "smb://slieer:slieer@" + Constants.IP
				+ "/linux-share/XcapService.java";
		try {
			in = new SmbFileInputStream(smbUrl); // 建立smb文件输入流
			while ((readed = in.read(buffer)) != -1) {
				System.out.write(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
