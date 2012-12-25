package com.slieer;

import java.net.UnknownHostException;

import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;

public class NameTest {
    public static void get() throws Exception {
        UniAddress ua;
        String cn;
        
        String[] argv = new String[]{"192.168.51.44"};
        ua = UniAddress.getByName( argv[0] );

        cn = ua.firstCalledName();
        do {
            System.out.println( "calledName=" + cn );
        } while(( cn = ua.nextCalledName() ) != null && !cn.startsWith(NbtAddress.SMBSERVER_NAME));
    }
	
	
	public static void main(String[] args) {
		try {
			NbtAddress adr = NbtAddress.getByName("192.168.51.44");
			System.out.println(adr.getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
