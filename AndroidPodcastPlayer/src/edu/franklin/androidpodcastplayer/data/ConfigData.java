package edu.franklin.androidpodcastplayer.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ConfigData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {  };

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

	/**
	 * Closes the database.
	 */
	public void close() {
		dbHelper.close();
	}
	
	
}
