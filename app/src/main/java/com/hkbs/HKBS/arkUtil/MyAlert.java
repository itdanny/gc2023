package com.hkbs.HKBS.arkUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hkbs.HKBS.R;

import org.arkist.share.AxDebug;

/*
MyAlert myAlert = new MyAlert(MainActivity.this, getResources().getString(R.string.WordViewMore), new MyAlert.OkListener() {
					@Override
					public void ready(MyAlert alert) {
					    .
					    .
					    .
						alert.dismiss();
					}
				});	    
		    	myAlert.show();
*/

public class MyAlert extends Dialog {
	private Button btnOk;
	private Button btnCancel;
	private TextView textView;
	private String message;
	private OkListener okListener;
	private CancelListener cancelListener;

	public interface OkListener {
        public void ready(MyAlert alert);
    }
	public interface CancelListener {
        public void reject(MyAlert alert);
    }
//	static public void show(Activity activity, String message){
//		if (activity!=null && !activity.isFinishing()) {
//			MyAlert alert = new MyAlert(activity, message);
//			alert.show();
//		}
//	}
//	public MyAlert(Activity activity, String message) {
//		super(activity);
//		this.message = message;
//		this.okListener = null;
//		this.cancelListener = null;
//		initSetup();
//	}
	static public void show(Activity activity, String message, OkListener okListener) {
		if (activity!=null && !activity.isFinishing()) {
			MyAlert alert = new MyAlert(activity, message, okListener);
			alert.show();
		}
	}
	public MyAlert(Activity activity, String message, OkListener okListener) {
		super(activity);
		this.message = message;
		this.okListener = okListener;
		this.cancelListener = null;
		initSetup();		
	}
	static public void show(Activity activity, String message, OkListener okListener, CancelListener cancelListener) {
		if (activity!=null && !activity.isFinishing()) {
			MyAlert alert = new MyAlert(activity, message, okListener, cancelListener);
			alert.show();
		}
	}
	public MyAlert(Activity activity, String message, OkListener okListener, CancelListener cancelListener) {
		super(activity);
		this.message = message;
		this.okListener = okListener;
		this.cancelListener = cancelListener;
		initSetup();		
	}
    private View getLayout(Context context, int id){
        //return getActivity().getLayoutInflater().inflate(id, null);
        View view=null;
        try {
            view = LayoutInflater.from(context).inflate(id, null);
        } catch (Exception e1){
            try { // DCv80 try different ways ....
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(id, null);
            } catch (Exception e2){
                // DCv71:
                AxDebug.error(this, ((context == null ? "null" : "Something") + "_id_" + id)+e2.getMessage());
                return null;
            }
        }
        return view;
    }
	private void initSetup(){
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//this.setTitle(R.string.TxtConfirm);		
		View promptsView = getLayout(getContext(), R.layout.my_alert);
		btnOk = (Button) promptsView.findViewById(R.id.alertOk);
		btnCancel = (Button) promptsView.findViewById(R.id.alertCancel);
		if (okListener==null) btnCancel.setVisibility(View.GONE);
		textView = (TextView) promptsView.findViewById(R.id.alertMessage);
		setContentView(promptsView);
		setCancelable(true);
		setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dismiss();
			}
		});
		setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				textView.setText(message);
		        btnOk.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (okListener!=null) okListener.ready(MyAlert.this);
						dismiss();
					}
				});
		        btnCancel.setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		            	if (cancelListener!=null) cancelListener.reject(MyAlert.this);
		            	dismiss();
		            };
		        });
			}
		});
	}
}
