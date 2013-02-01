package com.mipt.mediacenter.utils.cifs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.util.Log;

/**
 * local ip, LAN net segment, LAN NetBios Name list.
 * 
 * @author slieer
 *
 */
public class LanInfo {
	public static final String TAG = "LanInfo";
	//0-63
	//64 -127
	//128-191
	//192-255

	/**
	 * string[ip, netSegment]
	 * @return
	 */
	public static String[] getIPInfo(){
		String ip = null;
		try {
			ip = getLocalIp1();
		} catch (Exception e1) {			
			e1.printStackTrace();
		}
		
		Log.i(TAG, "local ip:" + ip);
		String[] re = null;
		if(ip != null){
			int len = ip.lastIndexOf(".");
			String segment = ip.substring(0, len);
			re = new String[]{ip, segment};
		}
		
		return re;
	}
	
	
/*    private static String getLocalIp() throws UnknownHostException,
            SocketException {
    	return Inet4Address.getLocalHost().toString();
    }
*/
    private static String getLocalIp1() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface
                .getNetworkInterfaces();
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            String netInterfaceName = netInterface.getName();
            if(netInterfaceName != null && netInterfaceName.equals("lo"))continue;
            //Log.i(TAG, "netInterface.getName:" + netInterface.getName());
            Enumeration<InetAddress> addresses = netInterface
                    .getInetAddresses();
            while(addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address) {
                	//Log.i(TAG, "本机的IP = " + ip.getHostAddress());
                	return ip.getHostAddress();  	
                }
            }
        }
        return null;
    }    
}
