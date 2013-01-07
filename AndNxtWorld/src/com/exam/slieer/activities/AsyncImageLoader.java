package com.exam.slieer.activities;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
	//��Ż����ͼƬ����
	private HashMap<String, SoftReference<Drawable>> imageCache;

	public AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}
	//�ж��Ƿ��л����ͼƬ���еĻ����ػ��棬û�û������߳�����
	public Drawable loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		//�е����
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			System.out.println("�õĻ����Ƭ");
			if (drawable != null) {
				return drawable;
			}
		}
		//û�����
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				if (imageCallback != null) {
					imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				System.out.println("�����ش�Ƭ");
				Drawable drawable = loadImageFromUrl(imageUrl);
				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}
	//���ͼƬ�����ַ����һ��Drawable
	public static Drawable loadImageFromUrl(String url) {
		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		Drawable d = null;
		if (i != null) {
			d = Drawable.createFromStream(i, "src");
		}
		return d;
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}
}
