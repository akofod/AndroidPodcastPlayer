package edu.franklin.androidpodcastplayer.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EpisodesData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {  };

	// Logcat tag
	private static final String LOG = "EpisodesData";

	/**
	 * Constructs an instance of EpisodesData.
	 * 
	 * @param context
	 */
	public EpisodesData(Context context) {
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
