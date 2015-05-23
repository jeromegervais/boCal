package com.example.testapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp.business.Logic;
import com.example.testapp.business.RendezVous;
import com.example.testapp.business.RendezVous.Period;
import com.example.testapp.utils.ActivityExtended;
import com.example.testapp.utils.DatePickerClickListener;

public class DetailDateActivity extends ActivityExtended {

	private static final String TEXT_FIELD = "text";
	private static final String PERIOD_FIELD = "period";

	private static final int DAY_COLOR = Color.parseColor("#FAD7AF");
	private static final int EVENING_COLOR = Color.parseColor("#6897BB");
	
	private String date;
	private final Calendar current = Calendar.getInstance();

	private View alertView;
	private EditText editText;
	private Button newDateButton;
	private TextView newDateText;
	private ListView rdvsView;
	private Button addRdvButton;

	private List<HashMap<String, String>> listRdvs;
	private BaseAdapter adapter;

	private final DialogInterface.OnClickListener voidClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// do nothing
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		Intent i = getIntent();
		date = i.getStringExtra(MainActivity.DATE);
		
		current.setTime(Logic.parse(date).getTime());
		
		$(R.id.dateTitle, TextView.class).setText(Logic.display(current) + " - " + date);

		rdvsView = $(R.id.listRdvs);
		addRdvButton = $(R.id.addRdv);
		
		listRdvs = new ArrayList<HashMap<String, String>>();
		for (RendezVous rdv : Logic.getRdvs(date)) {
			listRdvs.add(getMapFromRdv(rdv));
		}
		sortListRdvs();

		setComponents();
		setListeners();
	}

	private void setComponents() {
		adapter = new ArrayAdapter<HashMap<String, String>>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, listRdvs) {
			@Override
			public View getView(int position, View convertView,
					android.view.ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				
				HashMap<String, String> elem = listRdvs.get(position);
				String message = elem.get(TEXT_FIELD);
				Period period = Period.valueOf(elem.get(PERIOD_FIELD));

				view.setBackgroundColor((period == Period.Day) ? DAY_COLOR : EVENING_COLOR);

				$(view, android.R.id.text1, TextView.class).setText(message);
				return view;
			}
		};
		
		rdvsView.setAdapter(adapter);
		rdvsView.setBackgroundColor(Color.WHITE);
	}
	
	private void setListeners() {
		
		// click sur un item => modification du message
		rdvsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, final long id) {
				final HashMap<String, String> elem = listRdvs.get(position);

				setOrAddRdv(elem);
			}
		});

		// long click sur un item => suppression apres confirmation
		rdvsView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, final long id) {
				final HashMap<String, String> elem = listRdvs.get(position);

				new AlertDialog.Builder(DetailDateActivity.this)
						.setTitle(R.string.remove)
						.setMessage(String.format(getResources().getString(R.string.remove_rdv_message), ACCOUNT_NAME))
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
							
									@Override
									public void onClick(final DialogInterface dialog, final int which) {
										RendezVous rdv = getRdvFromMap(elem);
										Logic.removeRdv(date, rdv);
										listRdvs.remove(position);
										adapter.notifyDataSetChanged();
									}
								})
						.setNegativeButton(android.R.string.no, voidClickListener)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
				return true;
			}
		});

		// click sur 'add' => dialog + ajout d'un rdv
		addRdvButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setOrAddRdv(null);
			}
		});
	}

	private void setOrAddRdv(final Map<String, String> elem) {
		alertView = getLayoutInflater().inflate(R.layout.alert_rdv, null);
		editText = $(alertView, R.id.editMessage);
		newDateButton = $(alertView, R.id.newDateButton);
		newDateText = $(alertView, R.id.newDateText);
		
		final RendezVous rdv;
		if (elem != null) {
			rdv = getRdvFromMap(elem);
			editText.setText(rdv.getMessage());
			editText.setSelection(rdv.getMessage().length());
			$(alertView, R.id.rgDayOrEvening, RadioGroup.class)
				.check(rdv.getPeriod() == Period.Day ? R.id.radioDay : R.id.radioEvening);
			newDateButton.setVisibility(View.VISIBLE);
			newDateText.setVisibility(View.VISIBLE);
			
			newDateButton.setOnClickListener(new DatePickerClickListener(this, current) {
				
				@Override
				public void onDateChosen(final Calendar chosenDate) {
					newDateText.setText(Logic.format(chosenDate));
				}
			});
		}
		else {
			rdv = null;
			newDateButton.setVisibility(View.GONE);
			newDateText.setVisibility(View.GONE);
		}
		
		new AlertDialog.Builder(DetailDateActivity.this)
				.setView(alertView)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(final DialogInterface dialog, final int which) {
								String newMessage = editText.getText().toString();

								if (newMessage.isEmpty()) {
									Toast
										.makeText(DetailDateActivity.this, R.string.emptyRdv, Toast.LENGTH_SHORT)
										.show();
									return;
								}
								
								boolean isDayTime = $(alertView, R.id.radioDay, RadioButton.class).isChecked();
								Period newPeriod = isDayTime ? Period.Day : Period.Evening;
								
								if (rdv != null) {
									Logic.removeRdv(date, rdv);
									listRdvs.remove(elem);
								}
								
								RendezVous newRdv = new RendezVous();
								newRdv.setMessage(newMessage);
								newRdv.setPeriod(newPeriod);
								
								// pas de changement de date
								if (newDateText.getText().length() == 0) {
									Logic.saveRdv(date, newRdv);
									listRdvs.add(getMapFromRdv(newRdv));
									sortListRdvs();
								}
								// changement de date
								else {
									Logic.saveRdv(newDateText.getText().toString(), newRdv);
								}

								adapter.notifyDataSetChanged();
							}
						})
				.setNegativeButton(android.R.string.no, voidClickListener)
				.setIcon(android.R.drawable.ic_dialog_info)
				.show();
	}
	
	private RendezVous getRdvFromMap(final Map<String, String> elem) {
		RendezVous rdv = new RendezVous();
		rdv.setMessage(elem.get(TEXT_FIELD));
		rdv.setPeriod(Period.valueOf(elem.get(PERIOD_FIELD)));
		return rdv;
	}
	
	private HashMap<String, String> getMapFromRdv(final RendezVous rdv) {
		HashMap<String, String> element = new HashMap<String, String>();
		element.put(TEXT_FIELD, rdv.getMessage());
		element.put(PERIOD_FIELD, rdv.getPeriod().toString());
		return element;
	}
	
	private void sortListRdvs() {
		Collections.sort(listRdvs, new Comparator<Map<String, String>>() {

			@Override
			public int compare(Map<String, String> lhs, Map<String, String> rhs) {
				int c1 = Integer.valueOf(Period.valueOf(lhs.get(PERIOD_FIELD)).getOrder())
						.compareTo(Integer.valueOf(Period.valueOf(rhs.get(PERIOD_FIELD)).getOrder()));
				if (c1 == 0)
					return lhs.get(TEXT_FIELD).compareTo(rhs.get(TEXT_FIELD));
				return c1;
			}
		});
	}
	
	
	
//	public enum SwipeType {
//		ToRight,
//		ToLeft,
//		None
//	}
//	
//	private final class SwipeItemListener extends OnSwipeTouchListener
//	implements OnItemClickListener, OnTouchListener {
//
//		private SwipeType swipeType = SwipeType.None;
//		
//		public SwipeItemListener(Context context) {
//			super(context);
//		}
//
//		@Override
//		public void onItemClick(final AdapterView<?> parent, final View view,
//				final int position, final long id) {
//			if (swipeType == SwipeType.ToRight) {
//				return;
//			}
//			if (swipeType == SwipeType.None) {
//				final HashMap<String, String> elem = listRdvs.get(position);
//				setOrAddRdv(elem);
//				return;
//			}
//			
//			final HashMap<String, String> elem = listRdvs.get(position);
//
//			new AlertDialog.Builder(DetailDateActivity.this)
//					.setTitle(R.string.remove)
//					.setMessage(R.string.remove_rdv_message)
//					.setPositiveButton(android.R.string.yes,
//							new DialogInterface.OnClickListener() {
//						
//								@Override
//								public void onClick(final DialogInterface dialog, final int which) {
//									RendezVous rdv = getRdvFromMap(elem);
//									Logic.removeRdv(date, rdv);
//									listRdvs.remove(position);
//									adapter.notifyDataSetChanged();
//								}
//							})
//					.setNegativeButton(android.R.string.no, voidClickListener)
//					.setIcon(android.R.drawable.ic_dialog_alert)
//					.show();
//		}
//
//		@Override
//		public void onSwipeToLeft() {
//			swipeType = SwipeType.ToLeft;
//		}
//
//		@Override
//		public void onSwipeToRight() {
//			swipeType = SwipeType.ToRight;
//		}
//	}
}
