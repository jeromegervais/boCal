package com.example.testapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.testapp.business.Logic;
import com.example.testapp.business.RendezVous;
import com.example.testapp.business.RendezVous.Period;
import com.example.testapp.utils.ActivityExtended;
import com.example.testapp.utils.DatePickerClickListener;
import com.example.testapp.utils.OnSwipeTouchListener;

public class MainActivity extends ActivityExtended {

	public final static int DETAIL_ACTIVITY_REQUEST_CODE = 1;
	
	public final static String DATE = "com.example.testapp.DATE";
	// public final static String DAY_NAME = "com.example.testapp.DAY_NAME";
	
	private final static String DATE_FIELD = "date";
	private final static String DAY_FIELD = "jour";

	private final static int NUMBER_DAYS_OF_WEEK = 7;

	private ListView view;
	private Button button;
	private Button todayButton;
	private Button toLeft;
	private Button toRight;

	private List<HashMap<String, String>> daysList;
	private ArrayAdapter<HashMap<String, String>> adapter;
	private final Calendar firstDayOfWeek = Calendar.getInstance(Locale.getDefault());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Logic.setApplicationcontext(this);
		
		view = $(R.id.myListView);
		button = $(R.id.dateButton);
		todayButton = $(R.id.todayButton);
		toLeft = $(R.id.toLeft);
		toRight = $(R.id.toRight);

		Logic.setToFirstDayOfWeek(firstDayOfWeek);
		daysList = new ArrayList<HashMap<String, String>>();

		setComponents();
		setListeners();
		
		view.setAdapter(adapter);

		fillDates();
	}

	private void setComponents() {
		adapter = new ArrayAdapter<HashMap<String, String>>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1, daysList) {

			@Override
			public View getView(int position, View convertView,
					android.view.ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				
				HashMap<String, String> elem = daysList.get(position);

				String day = elem.get(DAY_FIELD);
				String date = elem.get(DATE_FIELD);

				Set<RendezVous> rdvs = Logic.getRdvs(date);
				boolean hasDayRdv = false;
				boolean hasEveningRdv = false;
				for (RendezVous rdv : rdvs) {
					if (rdv.getPeriod() == Period.Day) {
						hasDayRdv = true;
					}
					if (rdv.getPeriod() == Period.Evening) {
						hasEveningRdv = true;
					}
				}
				
//				if (hasDayRdv && hasEveningRdv) {
//					Drawable draw = getResources().getDrawable(R.drawable.bgday_evening);
//					view.setBackgroundDrawable(draw);
//				}
//				else if (hasDayRdv) {
//					view.setBackgroundDrawable(getResources().getDrawable(R.drawable.bgday));
//				}
//				else if (hasEveningRdv) {
//					view.setBackgroundDrawable(getResources().getDrawable(R.drawable.bgevening));
//				}
				if (hasDayRdv || hasEveningRdv) {
					view.setBackgroundColor(Color.parseColor("#FAD7AF"));
				}
				else {
					view.setBackgroundColor(Color.WHITE);
				}

				TextView tv1 = $(view, android.R.id.text1);
				TextView tv2 = $(view, android.R.id.text2);
				tv1.setText(day);
				tv2.setText(date);
				
				return view;
			}
		};
	}
	
	private void setListeners() {
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, String> elem = daysList.get(position);

				Intent i = new Intent(MainActivity.this,
						DetailDateActivity.class);
				i.putExtra(DATE, elem.get(DATE_FIELD));
				// i.putExtra(DAY_NAME, elem.get(DAY_FIELD));
				startActivityForResult(i, DETAIL_ACTIVITY_REQUEST_CODE);
			}

		});

		button.setOnClickListener(new DatePickerClickListener(this, firstDayOfWeek) {
				
				@Override
				public void onDateChosen(final Calendar chosenDate) {
					firstDayOfWeek.clear();
					firstDayOfWeek.setTime(chosenDate.getTime());
					Logic.setToFirstDayOfWeek(firstDayOfWeek);
					fillDates();
				}
			});

		todayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				firstDayOfWeek.clear();
				firstDayOfWeek.setTime(Calendar.getInstance(Locale.getDefault()).getTime());
				Logic.setToFirstDayOfWeek(firstDayOfWeek);
				fillDates();
			}
		});
		
		toLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeWeek(false);
			}
		});

		toRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeWeek(true);
			}
		});	
		
		view.setOnTouchListener(new OnSwipeTouchListener(this) {
			
			@Override
			public void onSwipeToRight() {
				changeWeek(false);
			}
			
			@Override
			public void onSwipeToLeft() {
				changeWeek(true);
			}
		});
	}
	
	private void changeWeek(boolean goForward) {
		firstDayOfWeek.add(Calendar.DATE, (goForward ? 1 : -1 ) * NUMBER_DAYS_OF_WEEK);
		fillDates();
	}
	
	private void fillDates() {		
		HashMap<String, String> element;
		Calendar tmp = (Calendar) firstDayOfWeek.clone();

		daysList.clear();
		for (int i = 0; i < NUMBER_DAYS_OF_WEEK; i++) {
			element = new HashMap<String, String>();
			tmp.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY + i);
			element.put(DATE_FIELD, Logic.format(tmp));
			element.put(DAY_FIELD, Logic.display(tmp));
			daysList.add(element);
		}
		
		view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
		adapter.notifyDataSetChanged();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE) {
			fillDates();
		}
	}
}
