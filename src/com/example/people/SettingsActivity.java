package com.example.people;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.google.android.gms.common.AccountPicker;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {

  private static final int REQUEST_ACCOUNT = 0;
  
  private Settings mSettings;
  private AccountManager mAccountManager;

  private Preference mAccountPref;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mSettings = new Settings(this);
    addPreferencesFromResource(R.xml.preferences);
    
    mAccountPref = findPreference("account");
    mAccountPref.setOnPreferenceClickListener(this);
    
    mAccountManager = AccountManager.get(this);
    updateAccountPref();
  }

  private void updateAccountPref() {
    String accountName = mSettings.getSyncAccount();
    mAccountPref.setTitle(accountName);
  }
  
  @Override
  public boolean onPreferenceClick(Preference preference) {
    String selectedAccountName = mSettings.getSyncAccount();
    Account[] accounts = mAccountManager.getAccountsByType("com.google");
    Account selectedAccount = null;
    for (Account account : accounts) {
      if (account.name.equals(selectedAccountName)) {
        selectedAccount = account;
        break;
      }
    }    
    Intent intent = AccountPicker.newChooseAccountIntent(selectedAccount, null, new String[]{"com.google"},
        true, null, null, null, null);
    startActivityForResult(intent, REQUEST_ACCOUNT);
    return true;
  }  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_ACCOUNT && resultCode == RESULT_OK) {
      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      mSettings.setSyncAccount(accountName);
      updateAccountPref();
    }
  }
}
