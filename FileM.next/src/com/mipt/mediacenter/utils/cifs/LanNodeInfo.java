package com.mipt.mediacenter.utils.cifs;

public class LanNodeInfo {
	public int num;
	public String ip;
	public String name;
	public String mac;
	public String group;
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
