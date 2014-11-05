package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;

import edu.franklin.androidpodcastplayer.models.Episode;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EpisodesData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.EPISODES_COLUMN_EPISODEID, 
			DatabaseHelper.EPISODES_COLUMN_PODCASTID, DatabaseHelper.EPISODES_COLUMN_NAME, 
			DatabaseHelper.EPISODES_COLUMN_FILEPATH, DatabaseHelper.EPISODES_COLUMN_IMAGE, 
			DatabaseHelper.EPISODES_COLUMN_TOTALTIME, DatabaseHelper.EPISODES_COLUMN_PLAYEDTIME, 
			DatabaseHelper.EPISODES_COLUMN_NEW, DatabaseHelper.EPISODES_COLUMN_COMPLETED};

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

	public ArrayList<Episode> getAllEpisodes(long podcastId) {
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_EPISODES, allColumns, 
					DatabaseHelper.EPISODES_COLUMN_PODCASTID + " = " + podcastId, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			Episode episode = cursorToEpisode(cursor);
			episodes.add(episode);
			cursor.moveToNext();
		}
		cursor.close();
		return episodes;
	}

	private Episode cursorToEpisode(Cursor cursor) {
		Episode episode = new Episode();
		episode.setEpisodeId(cursor.getLong(0));
		episode.setPodcastId(cursor.getLong(1));
		episode.setName(cursor.getString(2));
		episode.setFilepath(cursor.getString(3));
		episode.setImage(cursor.getString(4));
		episode.setTotalTime(cursor.getLong(5));
		episode.setPlayedTime(cursor.getLong(6));
		
		boolean newEpisode = false;
		if(cursor.getLong(7) == 1) {
			newEpisode = true;
		}
		episode.setNewEpisode(newEpisode);
		
		boolean completed = false;
		if(cursor.getLong(8) == 1) {
			completed = true;
		}
		episode.setCompleted(completed);
		
		return episode;
	}
	
	
}
