package com.example.testapp.business;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONDAY;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public final class Logic {

	private static final DateFormat DF = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
	
	// private static Context applicationContext;
	private static SharedPreferences settings;
	
	public static void setApplicationcontext(Context applicationContext) {
		// Logic.applicationContext = applicationContext;
		settings = PreferenceManager.getDefaultSharedPreferences(applicationContext);
	}
	
	public static void setToFirstDayOfWeek(final Calendar c) {
		c.set(DAY_OF_WEEK, MONDAY);
	}
	
	public static Calendar parse(final String date) {
		final Calendar c = Calendar.getInstance();
		try {
			c.setTime(Logic.DF.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public static String format(final Calendar cal) {
		return Logic.DF.format(cal.getTime());
	}
	
	public static String display(final Calendar cal) {
		return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
	}
	
	public static void saveRdv(final String date, final RendezVous rdv) {
		Set<String> rdvs = new HashSet<String>(settings.getStringSet(date, new HashSet<String>()));
		rdvs.add(rdv.toString());
		Editor editSettings = settings.edit();
		editSettings.remove(date);
		editSettings.putStringSet(date, rdvs);
		editSettings.commit();
		Log.d("TestApp", String.format("rdv {0} saved for date {1}", rdv, date));
	}
	
	public static void removeRdv(final String date, final RendezVous rdv) {
		Set<String> rdvs = new HashSet<String>(settings.getStringSet(date, new HashSet<String>()));
		if (rdvs.remove(rdv.toString())) {
			Editor editSettings = settings.edit();
			editSettings.remove(date);
			editSettings.putStringSet(date, rdvs);
			editSettings.commit();
			Log.d("TestApp", String.format("rdv {0} removed from date {1}", rdv, date));
		}
	}
	
	public static void removeRdvs(final String date) {
		Editor editSettings = settings.edit();
		editSettings.remove(date);
		editSettings.commit();
		Log.d("TestApp", String.format("all rdvs removed from date {0}", date));
	}
	
	public static Set<RendezVous> getRdvs(final String date) {
		Set<String> rdvs = settings.getStringSet(date, new HashSet<String>());
		Set<RendezVous> result = new HashSet<RendezVous>(rdvs.size());
		for (String s : rdvs) {
			result.add(RendezVous.fromString(s));
		}
		return result;
	}
}
