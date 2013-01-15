package com.mipt.mediacenter.center.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupInfo {
	// 设备类型节点集合
	// private List<GroupInfo> groupOladList = new ArrayList<GroupInfo>();
	// 设备标签
	final static String MOUNT_LABLE = "mountLable";

	// 设备类型
	final static String MOUNT_TYPE = "mountType";

	// 设备路径
	final static String MOUNT_PATH = "mountPath";

	// 设备卷标
	final static String MOUNT_NAME = "mountName";
	private String name;// group name
	private List<Map<String, String>> childList;// Group following a collection
												// of friends

	public GroupInfo() {
		childList = new ArrayList<Map<String, String>>();
	}

	public GroupInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Map<String, String>> getChildList() {
		return childList;
	}

	public void setChildList(List<Map<String, String>> childList) {
		this.childList = childList;
	}

}
