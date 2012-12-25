package com.slieer;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class Test1 {
	public static void main(String[] args) throws Exception {
		test2();
	}

	public static void test2() throws Exception {
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("","slieer","slieer");
		// ȡ�þ��������е���(workgroup������)
		//SmbFile workgroup = new SmbFile("smb://",auth);
		SmbFile workgroup = new SmbFile("smb://192.168.51.44/");//, auth);
		SmbFile[] servers = workgroup.listFiles();
		System.out.println(servers.length);
		for(SmbFile file : servers) {
			String path = file.getPath();
			System.out.println(path);
		}

		// ���ñ��룺
		jcifs.Config.setProperty("jcifs.encoding", "GBK");
	}
}
