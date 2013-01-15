/**
 * @auth  yujie.wang
 * @email lance.wyj@gmail.com
 * @date 2012-12-6
 * @description nothing to say
*/

package com.mipt.mediacenter.dlna.util;


import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class CommonUtil {

	private static final CommonLog log = LogFactory.createLog();
	
	public static boolean checkNetState(Context context)
    {
    	boolean netstate = false;
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED) 
					{
						netstate = true;
						break;
					}
				}
			}
		}
		return netstate;
    }
	
	public static void showToask(Context context, String tip){
		Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
	}
	
	

	public static int getScreenWidth(Context context) {
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}
	
	public static int getScreenHeight(Context context) {
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}
	
//	public static ViewSize getFitSize(Context context, MediaPlayer mediaPlayer)
//	{
//		int videoWidth = mediaPlayer.getVideoWidth();  
//	    int videoHeight = mediaPlayer.getVideoHeight();  	    
//	    double fit1 = videoWidth * 1.0 / videoHeight;	 
//	    
//	    int width2 = getScreenWidth(context);
//	    int height2 = getScreenHeight(context);      
//	    double fit2 = width2 * 1.0 / height2;  
//	    
//	    log.e("videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ",fit1 = " + fit1);
//	    log.e("width2 = " + width2 + ", height2 = " + height2 + ",fit2 = " + fit2);
//	    
//	    double fit = 1;
//	    if (fit1 > fit2)
//	    {
//	    	fit = width2 * 1.0 / videoWidth;
//	    }else{
//	    	fit = height2 * 1.0 / videoHeight;
//	    }
//	    
//	    log.e("fit = " + fit);
//	    
//	    ViewSize viewSize = new ViewSize();
//	    viewSize.width = (int) (fit * videoWidth);
//	    viewSize.height = (int) (fit * videoHeight);
//
//	    return viewSize;
//	}
//
//	public static class ViewSize
//	{
//		public int width = 0;
//		public int height = 0;
//	}
    
}
