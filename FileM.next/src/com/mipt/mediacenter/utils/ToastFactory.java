package com.mipt.mediacenter.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mipt.fileMgr.R;

/**
 * 
 * @author fang
 * 
 */
public class ToastFactory {

	private Toast toast = null;
	private TextView textView;
	private static ToastFactory instance;

	private ToastFactory() {
	}

	public static ToastFactory getInstance() {
		if (instance == null)
			instance = new ToastFactory();
		return instance;
	}

	public Toast getToast(final Context context, String txt) {
		if (toast == null) {
			View toastRoot = ((LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.cm_toast_bg, null);
			textView = (TextView) toastRoot.findViewById(R.id.cm_toast_name);
			toast = new Toast(context);
			textView.setText(txt);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(toastRoot);

		} else {
			textView.setText(txt);
		}
		return toast;

	}

	public void cancelToast() {
		if (toast != null) {
			toast.cancel();
		}

	}
}