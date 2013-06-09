package com.example.people;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.zumero.sqlite.SQLiteDatabase;
import com.zumero.sqlite.SQLiteException;

public class ZumeroBackend {
  
  private static final String ZUMERO_URL = "https://zinsta4f216f61b7.s.zumero.net";
  private static final String ADMIN_DBFILE = "zumero_users_admin";
  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PWD = "G1LjkNE2";  // TODO: obfuscate
  private static final String DBFILE_PREFIX = "test1_";
  private static final String USERS_DBFILE = DBFILE_PREFIX + "users";
  
  public static void adoptTable(SQLiteDatabase db, String table) {
    db.rawQuery("SELECT zumero_adopt_existing_table(?, ?)",
        new String[] {
            "main",
            table
        });
  }
  
  public static void addUser(SQLiteDatabase db, String accountName) {
    try {
      Cursor c = db.rawQuery("SELECT zumero_internal_auth_add_user(?, ?, zumero_internal_auth_scheme(?), ?, ?, ?, ?)",
          new String[] {
              ZUMERO_URL,
              USERS_DBFILE,
              ADMIN_DBFILE,
              ADMIN_USER,
              ADMIN_PWD,
              usernameFor(accountName),
              passwordFor(accountName)
          });
      c.moveToFirst();
    } catch (SQLiteException e) {
      if (e.getMessage().contains("zumero:unique_constraint_violation")) {
        Log.i("People", accountName + " already has an account");
      } else {
        throw e;
      }
    }
  }
  
  public static Cursor sync(Context context, SQLiteDatabase db, String accountName) {
    Cursor cursor = db.rawQuery("SELECT zumero_sync('main', ?, ?, zumero_internal_auth_scheme(?), ?, ?, ?)",
        new String[] {
            ZUMERO_URL,
            dbFile(accountName),
            USERS_DBFILE,
            usernameFor(accountName),
            passwordFor(accountName),
            context.getCacheDir().getAbsolutePath()
        });
    return cursor;
  } 
    
  private static String dbFile(String accountName) {
    // dbfiles can contain only lower-case letters, digits, and underscores
    String dbfile = DBFILE_PREFIX + accountName.toLowerCase(Locale.US).replaceAll("[^a-z0-9_]+", "_");
    return dbfile;
  }
  
  private static String usernameFor(String accountName) {
    return accountName;
  }
  
  private static String passwordFor(String accountName) {
    String pwd = sha1(accountName);
    return pwd;
  }
  
  private static String sha1(String str) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] result = digest.digest(str.getBytes("UTF-8"));
      StringBuilder sb = new StringBuilder();
      for (byte b : result) {
        sb.append(String.format("%02X", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      return str;
    } catch (UnsupportedEncodingException e) {
      return str;
    }
  }
}
