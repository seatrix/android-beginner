package com.mipt.fileMgr.center;

import com.mipt.fileMgr.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * @author fang
 * 
 */
public class ExitDialog extends Dialog {
	public Context context;
	public boolean isExitBtn = true;
	public View.OnClickListener listener;
	public String titleMsg;

	public ExitDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ExitDialog(Context context, int theme, boolean isExitBtn,
			String _title, View.OnClickListener listener) {
		super(context, theme);
		this.isExitBtn = isExitBtn;
		this.listener = listener;
		this.titleMsg = _title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cm_exit_dialog);
		iniUI();
	}

	private void iniUI() {
		((TextView) findViewById(R.id.cm_exit_title)).setText(titleMsg);
		Button buttonOk = (Button) findViewById(R.id.cm_exit_btn_ok);
		Button buttonCancel = (Button) findViewById(R.id.cm_exit_btn_cancel);
		if (listener != null) {
			buttonOk.setOnClickListener(listener);
		}
		if (isExitBtn) {
			buttonCancel.setVisibility(View.VISIBLE);
			buttonCancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dismiss();
				}
			});
		} else {
			buttonCancel.setVisibility(View.GONE);
		}

	}

}
