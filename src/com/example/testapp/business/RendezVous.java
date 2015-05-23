package com.example.testapp.business;

import java.io.Serializable;

public class RendezVous implements Serializable {

	private static final long serialVersionUID = 1115776961744988606L;
	
	private static final String SEPARATOR = "###";
	
	public enum Period {
		Day(0), Evening(1);
		
		private int order;
		
		Period(int order) {
			this.order = order;
		}
		
		public int getOrder() {
			return order;
		}
	}
	
	private String message;
	private Period period;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Period getPeriod() {
		return period;
	}
	public void setPeriod(Period period) {
		this.period = period;
	}
	
	public static RendezVous fromString(String s) {
		RendezVous result = new RendezVous();
		String[] split = s.split(SEPARATOR);
		result.setMessage(split[0]);
		if (split.length > 1)
			result.setPeriod(Period.valueOf(split[1]));
		else
			result.setPeriod(Period.Day);
		return result;		
	}
	
	@Override
	public String toString() {
		return message + SEPARATOR + period;
	}
}
