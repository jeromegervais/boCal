package com.example.testapp.utils;

import com.example.testapp.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ActivityExtended extends Activity {

	protected static String ACCOUNT_NAME;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAccountName();
	}
	
	private void setAccountName() {
		AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		Account[] list = manager.getAccounts();

		if (ACCOUNT_NAME == null) {
			for (Account account : list) {
			    if (account.type.equalsIgnoreCase("com.google")) {
			    	ACCOUNT_NAME = account.name.replace("@gmail.com", "");
			        break;
			    }
			}
			if (ACCOUNT_NAME == null)
				ACCOUNT_NAME = getResources().getString(R.string.anonymous);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends View> T $(int id) {
		return (T) findViewById(id);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends View> T $(int id, Class<T> c) {
		return (T) findViewById(id);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends View> T $(View v, int id) {
		return (T) v.findViewById(id);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends View> T $(View v, int id, Class<T> c) {
		return (T) v.findViewById(id);
	}
}
