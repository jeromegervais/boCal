package com.example.testapp.utils;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

public abstract class DatePickerClickListener implements OnClickListener {
	
	private final Context context;
	private final Calendar defaultDate;
	
	public DatePickerClickListener(final Context context, final Calendar defaultDate) {
		this.context = context;
		this.defaultDate = defaultDate;
	}
	
	@Override
	public void onClick(View v) {
		new DatePickerDialog(context, new OnDateSetListener() {

			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				Calendar tmp = Calendar.getInstance();
				tmp.clear();
				tmp.set(year, monthOfYear, dayOfMonth);
				
				//newDateText.setText(Logic.format(tmp));
				onDateChosen(tmp);
			}

		}, defaultDate.get(Calendar.YEAR), defaultDate.get(Calendar.MONTH),
		defaultDate.get(Calendar.DAY_OF_MONTH)).show();
	}
	
	public abstract void onDateChosen(final Calendar chosenDate);

}
