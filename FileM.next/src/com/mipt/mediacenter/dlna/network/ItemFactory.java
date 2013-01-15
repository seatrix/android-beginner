/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/


package com.mipt.mediacenter.dlna.network;


import android.content.Intent;



public class ItemFactory {

	
	public  static void putItemToIntent(Item item, Intent intent){
		intent.putExtra("title", item.getTitle());
		intent.putExtra("artist", item.getArtist());
		intent.putExtra("album", item.getAlbum());
		intent.putExtra("stringid", item.getStringid());
		intent.putExtra("objectClass", item.getObjectClass());
		intent.putExtra("res", item.getRes());
		intent.putExtra("duration", item.getDuration());
		intent.putExtra("albumarturi", item.getAlbumUri());
		intent.putExtra("childCount", item.getchildCount());
		intent.putExtra("date", item.getDate());
		intent.putExtra("size", item.getSize());

	}
	
	public static Item getItemFromIntent( Intent intent){
		Item item = new Item();
		item.setTitle(intent.getStringExtra("title"));
		item.setArtist(intent.getStringExtra("artist"));
		item.setAlbum(intent.getStringExtra("album"));
		item.setStringid(intent.getStringExtra("stringid"));
		item.setObjectClass(intent.getStringExtra("objectClass"));
		item.setRes(intent.getStringExtra("res"));
		item.setDuration(intent.getIntExtra("duration", 0));
		item.setAlbumUri(intent.getStringExtra("albumarturi"));
		item.setchildCount(intent.getStringExtra("childCount"));
		item.setDate(intent.getIntExtra("date", 0));
		item.setSize(intent.getIntExtra("size", 0));
		return item;
	}
}
