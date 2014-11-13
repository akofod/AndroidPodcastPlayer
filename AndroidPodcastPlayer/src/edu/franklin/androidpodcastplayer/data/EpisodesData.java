package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;

import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EpisodesData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.EPISODES_COLUMN_EPISODEID, 
			DatabaseHelper.EPISODES_COLUMN_PODCASTID, DatabaseHelper.EPISODES_COLUMN_NAME,
			DatabaseHelper.EPISODES_COLUMN_URL,
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
	
	
	public Episode createEpisode(Episode episode)
	{
		Episode ep = this.retrieveEpisodeByName(episode.getPodcastId(), episode.getName());
		if(ep == null)
		{
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.EPISODES_COLUMN_NAME, dbHelper.escapeString(episode.getName()));
			values.put(DatabaseHelper.EPISODES_COLUMN_URL, dbHelper.escapeString(episode.getUrl()));
			values.put(DatabaseHelper.EPISODES_COLUMN_COMPLETED, (episode.getPlayedTime() == episode.getTotalTime() && episode.getTotalTime() != 0));
			values.put(DatabaseHelper.EPISODES_COLUMN_FILEPATH, dbHelper.escapeString(episode.getFilepath()));
			values.put(DatabaseHelper.EPISODES_COLUMN_IMAGE, dbHelper.escapeString(episode.getImage()));
			values.put(DatabaseHelper.EPISODES_COLUMN_PLAYEDTIME, episode.getPlayedTime());
			values.put(DatabaseHelper.EPISODES_COLUMN_PODCASTID, episode.getPodcastId());
			values.put(DatabaseHelper.EPISODES_COLUMN_TOTALTIME, episode.getTotalTime());
			long epId = db.insert(DatabaseHelper.TABLE_EPISODES, null, values);
			Cursor cursor = db.query(DatabaseHelper.TABLE_EPISODES, allColumns, 
					DatabaseHelper.EPISODES_COLUMN_EPISODEID + " = " + epId, null, null, null, null);
			if(cursor.moveToFirst())
			{
				ep = cursorToEpisode(cursor);
			}
			cursor.close();
		}
		return ep;
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
	
	public void purgeEpisodes(Long podcastId)
	{
		dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_EPISODES, 
			DatabaseHelper.EPISODES_COLUMN_PODCASTID + "=" + podcastId.longValue(), null);
	}
	public Episode retrieveEpisodeByName(Long podcastId, String episodeName) 
	{
		Episode episode = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_EPISODES, allColumns, 
			DatabaseHelper.EPISODES_COLUMN_PODCASTID + " = " + podcastId.longValue() + " AND " +
			DatabaseHelper.EPISODES_COLUMN_NAME + " = ?", 
			new String[]{dbHelper.escapeString(episodeName)}, null, null, null);
		if(cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			episode = cursorToEpisode(cursor);
		}
		cursor.close();
		
		return episode;
	}
	
	public boolean updateFilePath(Long podId, Long epId, String filePath)
	{
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.EPISODES_COLUMN_FILEPATH, dbHelper.escapeString(filePath));
		int rows = db.update(DatabaseHelper.TABLE_EPISODES, values, 
			DatabaseHelper.EPISODES_COLUMN_PODCASTID + " = " + podId.longValue() + " AND " +
			DatabaseHelper.EPISODES_COLUMN_EPISODEID + " = " + epId.longValue()	, null);
		return rows == 1;
	}

	private Episode cursorToEpisode(Cursor cursor) {
		Episode episode = new Episode();
		episode.setEpisodeId(cursor.getLong(0));
		episode.setPodcastId(cursor.getLong(1));
		episode.setName(dbHelper.unescapeString(cursor.getString(2)));
		episode.setUrl(dbHelper.unescapeString(cursor.getString(3)));
		episode.setFilepath(dbHelper.unescapeString(cursor.getString(4)));
		episode.setImage(dbHelper.unescapeString(cursor.getString(5)));
		episode.setTotalTime(cursor.getLong(6));
		episode.setPlayedTime(cursor.getLong(7));
		
		boolean newEpisode = false;
		if(cursor.getLong(8) == 1) {
			newEpisode = true;
		}
		episode.setNewEpisode(newEpisode);
		
		boolean completed = false;
		if(cursor.getLong(9) == 1) {
			completed = true;
		}
		episode.setCompleted(completed);
		
		return episode;
	}
}
