package com.example.sushiyu.smartshot;
 
import java.util.Calendar;
 
import com.example.sushiyu.smartshot.DateTimepicker.OnDateTimeChangedListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
 
public class DateTimepickerDialog extends AlertDialog  {
	private DateTimepicker mDateTimePicker;
	private int Hour,Minute,Second;
	private OnDateTimeSetListener mOnDateTimeSetListener;
	private String datetimeStr;
	public DateTimepickerDialog(Context context, long date) {
		super(context);
		mDateTimePicker = new DateTimepicker(context);
		setView(mDateTimePicker);//装载刚才建立的布局，把定义好的日期时间布局显示在这个自定义对话框上
		/* 
		 *实现DateTimepicker里的接口
		 */  
		mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() {
            public void onDateTimeChanged(DateTimepicker view, 
            		int year, int month, int day, int hour, int minute, int second) {
                Hour = hour;
                Minute = minute;
                Second = second;
            }  
        });
		setTitle("请选择");

		setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				datetimeStr = String.format("%02d", Hour)+"  :  "+
						String.format("%02d", Minute)+"  :  "+
						String.format("%02d", Second);
				if (mOnDateTimeSetListener != null) {
					mOnDateTimeSetListener.OnDateTimeSet(dialog, datetimeStr);
					}
				}
			}); 
		setButton(DialogInterface.BUTTON_NEGATIVE,"取消", (OnClickListener) null); 
		setCanceledOnTouchOutside(false);//点击对话框外无法关闭对话框
	}  
	/* 
	 *   
	 *接口回调
	 */  
	public interface OnDateTimeSetListener {  
		void OnDateTimeSet(DialogInterface dialog, String datetimestr);
	}  
  
 
	/* 
	 *对外公开方法让Activity实现 
	 */  
	public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {  
		mOnDateTimeSetListener = callBack;  
	}  
}

