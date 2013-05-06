package com.example.people;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
  
  private static final String KEY_ACCOUNT_NAME = "accountName";

  private Context mContext;
  private SharedPreferences mPreferences;

  public Settings(Context context) {
    mContext = context;
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }
  
  public String getSyncAccount() {
    String accountName = mPreferences.getString(KEY_ACCOUNT_NAME, "");
    if (accountName.isEmpty()) {
      AccountManager accountManager = AccountManager.get(mContext);
      Account[] accounts = accountManager.getAccountsByType("com.google");
      accountName = accounts[0].name;
      setSyncAccount(accountName);
    }
    return accountName;
  }
  
  public void setSyncAccount(String accountName) {
    mPreferences.edit().putString(KEY_ACCOUNT_NAME, accountName).commit();
  }
}
