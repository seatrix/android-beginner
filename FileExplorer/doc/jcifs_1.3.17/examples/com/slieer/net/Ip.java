package com.slieer.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Ip {
	public static void main(String[] args) {
		try {
			// getRemoteName();
			// getLocalIp1();
			getNetWay();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getLocalIp() throws UnknownHostException,
			SocketException {
		return Inet4Address.getLocalHost().toString();
	}

	public static void getLocalIp1() throws SocketException {
		Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface
				.getNetworkInterfaces();
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
					.nextElement();
			System.out.println(netInterface.getName());
			Enumeration<InetAddress> addresses = netInterface
					.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					System.out.println("本机的IP = " + ip.getHostAddress());
				}
			}
		}
	}

	public static void getRemoteName() throws UnknownHostException {
		InetAddress i = Inet4Address.getByName("192.168.51.79");
		System.out.println("name:" + i.getCanonicalHostName());

	}

	public static void getNetWay() {

		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString();
			System.out.println(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
