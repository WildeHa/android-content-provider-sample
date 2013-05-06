package com.example.people;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.ListView;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

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
    selectAccount(selectedAccount);
    return true;
  }
  
  private void selectAccount(Account selectedAccount) {
    int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (status == ConnectionResult.SUCCESS) {
      Intent intent = AccountPicker.newChooseAccountIntent(selectedAccount, null, new String[]{"com.google"},
        true, null, null, null, null);
      startActivityForResult(intent, REQUEST_ACCOUNT);
    } else {
      showDialog(DIALOG_SELECT_ACCOUNT);
    }
  }
  
  private static final int DIALOG_SELECT_ACCOUNT = 0;
  
  @Override
  @Deprecated
  protected Dialog onCreateDialog(int id) {
    if (DIALOG_SELECT_ACCOUNT == id) {
      Account[] accounts = mAccountManager.getAccountsByType("com.google");
      final String[] accountNames = new String[accounts.length];
      final int[] selectedAccountIndices = new int[1];
      for (int i=0; i<accounts.length; i++) {
        accountNames[i] = accounts[i].name;
      }      
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      dialog.setTitle("Choose an account");
      dialog.setSingleChoiceItems(accountNames, -1, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          selectedAccountIndices[0] = which;
        }
      });
      dialog.setCancelable(true);
      dialog.setNegativeButton(android.R.string.cancel, null);
      dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          setAccount(accountNames[selectedAccountIndices[0]]);
        }
      });
      return dialog.create();
    }
    return null;
  }
  
  @Override
  @Deprecated
  protected void onPrepareDialog(int id, Dialog dialog) {
    if (DIALOG_SELECT_ACCOUNT == id) {
      Account[] accounts = mAccountManager.getAccountsByType("com.google");
      String selectedAccountName = mSettings.getSyncAccount();
      ListView dialogListView = ((AlertDialog) dialog).getListView();
      for (int i=0; i<accounts.length; i++) {
        boolean isSelectedAccount = accounts[i].name.equals(selectedAccountName);
        dialogListView.setItemChecked(i, isSelectedAccount);
      }      
    }
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_ACCOUNT && resultCode == RESULT_OK) {
      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      setAccount(accountName);
    }
  }
  
  private void setAccount(String accountName) {
    mSettings.setSyncAccount(accountName);
    updateAccountPref();
  }
}
