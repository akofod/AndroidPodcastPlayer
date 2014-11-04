package edu.franklin.androidpodcastplayer.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PodcastData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {  };

	// Logcat tag
	private static final String LOG = "PodcastData";

	/**
	 * Constructs an instance of PodcastData.
	 * 
	 * @param context
	 */
	public PodcastData(Context context) {
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
