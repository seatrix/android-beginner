package com.mipt.fileexplorer.remote.smb;

public class LanNodeInfo {
	int num;
	String ip;
	String name;
	String mac;
	String group;
	public LanNodeInfo(String ip, String name, String group,String mac) {
		this.name = name;
		this.group = group;
		this.ip = ip;
		this.mac = mac;
		
	}
	@Override
	public String toString() {
		return "LanNodeInfo [ip=" + ip + ", name=" + name + ", mac=" + mac
				+ ", group=" + group + "]";
	}
}
