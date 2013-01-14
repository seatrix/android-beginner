/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.fileMgr.dlna.network;


public class Item {
	public String stringid = "";
	public String title = "";
	public String artist = "";
	public String album = "";
	public String objectClass = "";
	public String res = "";
	public int duration = 0;
	public String albumarturi = "";
	public String childCount = "";
	public long date = 0;
	public int size = 0;
	
	public Item() {
		
	}
	
	public Item(String stringid, String title, String artist, String album, String objectClass) {
		setStringid(stringid);
		setTitle(title);
		setArtist(artist);
		setAlbum(album);
		setObjectClass(objectClass);
	}
	
	public String getShowString(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("stringid = " + stringid+ "\n" + 
							"title = " + title+ "\n" + 
							"artist = " + artist + "\n" + 
							"album = " + album + "\n" + 						
							"objectClass = " + objectClass + "\n" + 
							"res = " + res + "\n" + 
							"duration = " + duration + "\n" + 
							"albumUri = " + albumarturi + "\n" + 
							"childCount = " + childCount + "\n" + 
							"date = " + date + "\n" + 
							"size = " + size);
		
		return stringBuffer.toString();
	}
	
	public String getStringid() {
		return stringid;
	}
	public void setStringid(String stringid) {
		this.stringid = (stringid != null ? stringid : "");
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = (title != null ? title : "");
	}
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = (artist != null ? artist : "");
	}
	
	public void setAlbum(String album) {
		this.album = (album != null ? album : "");
	}
	public String getAlbum() {
		return album;
	}
	
	public void setObjectClass(String objectClass) {
		this.objectClass = (objectClass != null ? objectClass : "");
	}
	public String getObjectClass() {
		return objectClass;
	}

	public void setRes(String res) {
		this.res = (res != null ? res : "");
	}
	public String getRes() {
		return res;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getDuration() {
		return duration;
	}
	

	public String getAlbumUri(){
		return albumarturi;
	}
	public void setAlbumUri(String albumUri){
		this.albumarturi = (albumUri != null ? albumUri : "");
	}
	
	public String getchildCount() {
		return childCount;
	}
	public void setchildCount(String childCount) {
		this.childCount = (childCount != null ? childCount : "");
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}

}