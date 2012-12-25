package com.mipt.fileexplorer.remote.smb;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.util.Log;

public class SmbUtils {
	private static String TAG = "SmbUtils"; 
	
	public static int getLocalSmbServer() throws Exception {
		SmbFile workgroup = new SmbFile("smb://workgroup");
		SmbFile[] servers = workgroup.listFiles();
		Log.i(TAG, String.valueOf(servers.length));
		return servers.length;
	}
	
	public static String URL_EXAMLPLE = "smb://slieer:slieer@"  + Constants.TEST_IP + "/android-sdk-windows/";
	
	public static String URL_SHOW_LAN_NODE = "smb://";
    
	public static class SmbResult{
		public static int STATUS_OK = 0;
		public static int STATUS_SMB_ERROR = 2;
		public static int STATUS_OTHER_ERROR = 3;
		int status;
		long ntStatus;
		SmbFile[] smbFiles;
		public SmbResult(int... status) {
			this.status = status[0];
			this.ntStatus = status[1];
		}
		public SmbResult(SmbFile... smbFiles) {
			this.status = STATUS_OK;
			this.smbFiles = smbFiles;
		}
		@Override
		public String toString() {
			return "SmbResult [status=" + status + ", ntStatus=" + ntStatus
					+ ", smbFiles=" + Arrays.toString(smbFiles) + "]";
		}
	}
	/**
	 * 
	 * @param url
	 * @param userName
	 * @param passwd
	 * @return
	 */
	public static SmbResult dirDetail(String url){
        try {
        	SmbFile f = null;
        	f = new SmbFile(url);

        	if(f.isDirectory()){
        		SmbFile[] files = f.listFiles();
			    return new SmbResult(files);
			}else{
				return new SmbResult(f);
			}
		} catch (SmbException e) {
			
			Log.e(TAG, "NTstatus:" + e.getNtStatus());
			e.printStackTrace();
			return new SmbResult(SmbResult.STATUS_SMB_ERROR, e.getNtStatus());
			
		} catch (Exception e) {
			e.printStackTrace();
			return new SmbResult(SmbResult.STATUS_OTHER_ERROR);
		}
    }
	
    public static void scanPort446(){
    	
    }
    
    public static void getLocalIp() throws UnknownHostException,
            SocketException {
        System.out.println(Inet4Address.getLocalHost().toString());
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
    
	
	
}
