package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/**
 * ConfigData table will have only one record that
 * is written to and read
 * 
 */
public class ConfigData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.CONFIG_COLUMN_CONFIGID, 
			DatabaseHelper.CONFIG_COLUM_WIFIONLY, 
			DatabaseHelper.CONFIG_COLUMN_EXTSTORAGE};

	// Logcat tag
	private static final String LOG = "ItemData";

	/**
	 * Constructs an instance of ItemData.
	 * 
	 * @param context
	 */
	public ConfigData(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	/**
	 * Opens the database.
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		
	}
	
	public void init() {
	//initial values for testing
		ArrayList<Integer> val = new ArrayList<Integer>();
		val.add(Integer.valueOf(1));
		val.add(Integer.valueOf(0));
		this.updateSettings(val);
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		dbHelper.close();
	}
	
	public void updateSettings(ArrayList<Integer> settings) {
	//find number of rows
		SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
		String sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONFIG;
		SQLiteStatement statement = dbRead.compileStatement(sql);
		Integer cnt = (int) statement.simpleQueryForLong();
		
	//clear the table if not empty
		if(cnt != 0) {
			db.delete(DatabaseHelper.TABLE_CONFIG, null, null);
		}
	//insert values
			ContentValues newVal = new ContentValues();
			newVal.put(DatabaseHelper.CONFIG_COLUM_WIFIONLY, settings.get(1));
			newVal.put(DatabaseHelper.CONFIG_COLUMN_EXTSTORAGE, settings.get(0));
			db.insert(DatabaseHelper.TABLE_CONFIG, null, newVal);
	}
	
	public ArrayList<Integer> getSettings() {
		
		ArrayList<Integer> settings = new ArrayList<Integer>();
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_CONFIG, allColumns,
				null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			if(!cursor.isNull(0)) {
				settings.add(cursor.getInt(1));
				settings.add(cursor.getInt(2));
			}
			cursor.moveToNext();
		}
		return settings;
		
	}	
}
