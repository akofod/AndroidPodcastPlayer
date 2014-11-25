package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;

public class PodcastData {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.PODCAST_COLUMN_PODCASTID, 
			DatabaseHelper.PODCAST_COLUMN_NAME, DatabaseHelper.PODCAST_COLUMN_DESCRIPTION, 
			DatabaseHelper.PODCAST_COLUMN_IMAGE, DatabaseHelper.PODCAST_COLUMN_NUMEPISODES, 
			DatabaseHelper.PODCAST_COLUMN_FEEDURL, DatabaseHelper.PODCAST_COLUMN_DIR, 
			DatabaseHelper.PODCAST_COLUMN_OLDESTFIRST, DatabaseHelper.PODCAST_COLUMN_AUTODOWNLOAD, 
			DatabaseHelper.PODCAST_COLUMN_AUTODELETE};
	private EpisodesData episodesData;

	// Logcat tag
	private static final String LOG = "PodcastData";

	/**
	 * Constructs an instance of PodcastData.
	 * 
	 * @param context
	 */
	public PodcastData(Context context) {
		dbHelper = new DatabaseHelper(context);
		episodesData =  new EpisodesData(context);
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
	
	public Podcast createPodcast(Podcast podcast) 
	{	
		Podcast pd = retrievePodcastByName(podcast.getName());
		//could not find it, so make one
		if(pd == null) {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.PODCAST_COLUMN_NAME, dbHelper.escapeString(podcast.getName()));
			values.put(DatabaseHelper.PODCAST_COLUMN_DESCRIPTION, dbHelper.escapeString(podcast.getDescription()));
			values.put(DatabaseHelper.PODCAST_COLUMN_IMAGE, dbHelper.escapeString(podcast.getImage()));
			values.put(DatabaseHelper.PODCAST_COLUMN_NUMEPISODES, podcast.getNumEpisodes());
			values.put(DatabaseHelper.PODCAST_COLUMN_FEEDURL, dbHelper.escapeString(podcast.getFeedUrl()));
			values.put(DatabaseHelper.PODCAST_COLUMN_DIR, dbHelper.escapeString(podcast.getDir()));
			
			int oldestFirst;
			if (podcast.isOldestFirst()) {
				oldestFirst = 1;
			}
			else {
				oldestFirst = 0;
			}
			values.put(DatabaseHelper.PODCAST_COLUMN_OLDESTFIRST, oldestFirst);
			
			int autoDownload;
			if (podcast.isAutoDownload()) {
				autoDownload = 1;
			}
			else {
				autoDownload = 0;
			}
			values.put(DatabaseHelper.PODCAST_COLUMN_AUTODOWNLOAD, autoDownload);
			
			int autoDelete;
			if (podcast.isAutoDelete()) {
				autoDelete = 1;
			}
			else {
				autoDelete = 0;
			}
			values.put(DatabaseHelper.PODCAST_COLUMN_AUTODELETE, autoDelete);
			
			long insertId = db.insert(DatabaseHelper.TABLE_PODCAST, null, values);
			Cursor cursor = db.query(DatabaseHelper.TABLE_PODCAST, allColumns, 
					DatabaseHelper.PODCAST_COLUMN_PODCASTID + " = " + insertId, null, null, null, null);
			if(cursor.moveToFirst())
			{
				pd = cursorToPodcast(cursor);
			}
			cursor.close();
		}
		//return either the newly created podcast, or the old one.
		return pd;
	}
	
	public Podcast retrievePodcastByName(String podcastName) {
		Podcast podcast = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_PODCAST, allColumns, 
					DatabaseHelper.PODCAST_COLUMN_NAME + " = ?", 
					new String[]{dbHelper.escapeString(podcastName)}, null, null, null);
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			podcast = cursorToPodcast(cursor);
		}
		
		cursor.close();
		
		return podcast;
	}
	
	public boolean updateImagePath(Long podId, String imagePath)
	{
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.PODCAST_COLUMN_IMAGE, dbHelper.escapeString(imagePath));
		int rows = db.update(DatabaseHelper.TABLE_PODCAST, values, 
			DatabaseHelper.PODCAST_COLUMN_PODCASTID + " = " + podId.longValue(), null);
		return rows == 1;
	}
	
	public Podcast getPodcastById(Long id)
	{
		Podcast podcast = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_PODCAST, allColumns, 
					DatabaseHelper.PODCAST_COLUMN_PODCASTID + " = " + id.longValue(), 
					null, null, null, null);
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			podcast = cursorToPodcast(cursor);
		}
		
		cursor.close();
		
		return podcast;
	}
	
	public ArrayList<Podcast> getAllPodcasts() {
		ArrayList<Podcast> allPodcasts = new ArrayList<Podcast>();
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_PODCAST, allColumns, 
				null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			Podcast podcast = cursorToPodcast(cursor);
			allPodcasts.add(podcast);
			cursor.moveToNext();
		}
		cursor.close();
		return allPodcasts;
	}
	
	public void purgePodcast(Long podId)
	{
		//get rid of the episode info
		episodesData.purgeEpisodes(podId);
		//now get rid of the podcast
		dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_PODCAST, 
			DatabaseHelper.PODCAST_COLUMN_PODCASTID + "=" + podId.longValue(), null);
	}

	public Podcast cursorToPodcast(Cursor cursor) {
		Podcast podcast = new Podcast();
		long podcastId = cursor.getLong(0);
		podcast.setPodcastId(podcastId);
		podcast.setName(dbHelper.unescapeString(cursor.getString(1)));
		podcast.setDescription(dbHelper.unescapeString(cursor.getString(2)));
		podcast.setImage(dbHelper.unescapeString(cursor.getString(3)));
		podcast.setNumEpisodes(cursor.getLong(4));
		podcast.setFeedUrl(dbHelper.unescapeString(cursor.getString(5)));
		podcast.setDir(dbHelper.unescapeString(cursor.getString(6)));
		
		boolean oldestFirst = false;
		if (cursor.getLong(7) == 1) {
			oldestFirst = true;
		}
		podcast.setOldestFirst(oldestFirst);
		
		boolean autoDownload = false;
		if (cursor.getLong(8) == 1) {
			autoDownload = true;
		}
		podcast.setAutoDownload(autoDownload);
		
		boolean autoDelete = false;
		if (cursor.getLong(9) == 1) {
			autoDelete = true;
		}
		podcast.setAutoDelete(autoDelete);
		
		ArrayList<Episode> episodes = episodesData.getAllEpisodes(podcastId);
		podcast.setEpisodes(episodes);
		
		return podcast;
	}
}
