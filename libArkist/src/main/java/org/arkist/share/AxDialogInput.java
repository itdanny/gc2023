package org.arkist.share;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AxDialogInput extends Dialog {
	//final private static String TAG = MyDialogInput.class.getSimpleName();
	
	public Callback readyListener;
	private EditText editText;
//	private boolean isWithKeyPad;
	private String titleText;
	private String inputContent;
//	private Context context;
	
//	private EditText.CallBack editTextCallBack = new EditText.CallBack() {
//		@Override
//		public void onFinish(boolean isOK) {
//			if (isOK) {
//				dialogOK();
//			} else {
//				dialogCancel();
//			}			
//		}
//	};
	public interface Callback {
		public boolean ready(AxDialogInput dialog, String newString);
	}

	private void initSetup(Context context, String titleText,
			Callback readyListener, boolean isWithKeyPad, String inputContent) {
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));				
		this.readyListener = readyListener;
		this.inputContent = inputContent;
		this.titleText = titleText;
//		this.context = context;
//		this.isWithKeyPad = isWithKeyPad;
		this.setCancelable(true);
		this.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogCancel();
			}
		});
	}

	public AxDialogInput(Context context, String titleText,
			Callback readyListener, boolean isWithKeyPad) {
		super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		initSetup(context, titleText, readyListener, isWithKeyPad, "");
	}
	public AxDialogInput(Context context, String titleText,
			Callback readyListener, String inputContent) {
		super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		initSetup(context, titleText, readyListener, false, inputContent);
	}
	public AxDialogInput(Context context, String titleText,
			Callback readyListener) {
		super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		initSetup(context, titleText, readyListener, false, "");
	}

	private void dialogCancel() {
		// MyApplog(TAG,"DialogCancel");
		// hideKeyboard();
		AxDialogInput.this.readyListener.ready(AxDialogInput.this, "");
		AxDialogInput.this.dismiss();
	}

	private void dialogOK() {
		// MyApplog(TAG,"DialogOK");
		if (AxDialogInput.this.readyListener.ready(AxDialogInput.this, editText
				.getText().toString())) {
			// hideKeyboard();
			AxDialogInput.this.dismiss();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { // Call MyDialogList
														// before call onCreate
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ax_dialog_input);

		TextView title = (TextView) findViewById(R.id.myListTitle);
		if (this.titleText == null || this.titleText.equals("")) {
			title.setVisibility(View.GONE);
		} else {
			title.setText(this.titleText);
			title.setVisibility(View.VISIBLE);
		}

		editText = (EditText) findViewById(R.id.dialogInputEditText);
//		if (isWithKeyPad) {
//			editText.initSetup(true, true, (KeyboardView) findViewById(R.id.xmlKeyboardView),editTextCallBack);			
//		} else {
//			editText.initSetup(false, false, null, editTextCallBack);				
//		}
		editText.setText(inputContent);

		final Button btnCancel = (Button) findViewById(R.id.dialogListCancelBtn);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogCancel();
			}
		});

		final Button btnOk = (Button) findViewById(R.id.dialogListOkBtn);
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogOK();
			}
		});
		
		//MyApp.zoomOut((LinearLayout) findViewById(R.id.xmlDialog));
	}
}
