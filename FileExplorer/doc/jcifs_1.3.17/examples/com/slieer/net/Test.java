package com.slieer.net;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.omg.CORBA.PUBLIC_MEMBER;

import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;


public class Test {
	public static void main(String[] args) {
		//getLanDevicesNameListMutilThread();
		long start = System.currentTimeMillis();
		getLanDevicesNameListSingleThread();
		//getLanDevicesNameList();
		long end = System.currentTimeMillis();
		
		System.out.print("time lenth:" + (end - start));
	}
	public static void getLanDevicesNameListSingleThread(){
		List<LanNodeInfo> list = new ArrayList<LanNodeInfo>();
		
		String[] ipInfo = getIPInfo();
		if(ipInfo != null){
			System.out.println("net ip is " + ipInfo[0]);
			System.out.println("net segment is " + ipInfo[1]);

			int idx = 1;
			for (int i = 0; i < 256; i++) {
				String adr = ipInfo[1].concat(".").concat(String.valueOf(i));
				try {
					
					
					UdpGetClientMacAddr udp = new UdpGetClientMacAddr(adr);
					LanNodeInfo info = udp.getRemoteMacAddr();
					info.num = idx++;
					//System.out.println(info.name + "," + info.ip);
					System.out.println(info);
				} catch(SocketTimeoutException e){
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void getLanDevicesNameList(){
		List<LanNodeInfo> list = new ArrayList<LanNodeInfo>();
		
		String[] ipInfo = getIPInfo();
		if(ipInfo != null){
			System.out.println("net ip is " + ipInfo[0]);
			System.out.println("net segment is " + ipInfo[1]);

			try {
				int idx = 1;
				for (int i = 0; i < 256; i++) {
					String adr = ipInfo[1].concat(".").concat(String.valueOf(i));
					getOne(adr);
				}
			} catch(Exception e){
				
			}
		}
		
	}
		
	public static void getOne(String ip) throws UnknownHostException{
	    UniAddress ua = UniAddress.getByName(ip);
	    String cn = ua.firstCalledName();
	    System.out.println(ip + "," + ua.firstCalledName() + "," + ua.nextCalledName());
/*	    do {
	        System.out.println( "calledName=" + cn );
	    } while(( cn = ua.nextCalledName() ) != null && !cn.startsWith(NbtAddress.SMBSERVER_NAME));
*/		
	}
	
	/**
	 * string[ip, netSegment]
	 * @return
	 */
	public static String[] getIPInfo(){
		String ip = null;
		try {
			ip = Ip.getLocalIp();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (SocketException e1) {			
			e1.printStackTrace();
		}
		
		System.out.println("local ip:" + ip);
		String[] ipInfos = ip.split("/");
		String[] re = null;
		if(ip != null){
			int len = ipInfos[1].lastIndexOf(".");
			String segment = ipInfos[1].substring(0, len);
			re = new String[]{ip, segment};
		}
		return re;
	}
	//0-63
	//64 -127
	//128-191
	//192-255
}
