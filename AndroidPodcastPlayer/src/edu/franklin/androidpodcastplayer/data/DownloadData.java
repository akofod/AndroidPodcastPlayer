package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import edu.franklin.androidpodcastplayer.models.Download;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;

public class DownloadData 
{
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.DL_ID, 
			DatabaseHelper.DL_PODCAST_NAME, DatabaseHelper.DL_EPISODE_NAME,
			DatabaseHelper.DL_DIRECTORY, DatabaseHelper.DL_FILE};

	// Logcat tag
	private static final String LOG = "DownloadData";
	
	/**
	 * Constructs an instance of SubscriptionData.
	 * 
	 * @param context
	 */
	public DownloadData(Context context) 
	{
		dbHelper = new DatabaseHelper(context);
	}

	/**
	 * Opens the database.
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException 
	{
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * Closes the database.
	 */
	public void close() 
	{
		dbHelper.close();
	}
	
	public Download getDownload(long id)
	{
		Download dl = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_DOWNLOADS, allColumns, 
				DatabaseHelper.DL_ID + "=" + id, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) 
		{
			dl = cursorToDownload(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		return dl;
	}
	
	public Download getDownload(String podcastName, String episodeName)
	{
		Download dl = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_DOWNLOADS, allColumns, 
				DatabaseHelper.DL_PODCAST_NAME + "= ? AND " + DatabaseHelper.DL_EPISODE_NAME + "= ?",
				new String[]{podcastName, episodeName}, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) 
		{
			dl = cursorToDownload(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		return dl;
	}
	
	public List<Download> getAllDownloads()
	{
		List<Download> dls = new ArrayList<Download>();
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_DOWNLOADS, allColumns, 
				null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) 
		{
			Download dl = cursorToDownload(cursor);
			dls.add(dl);
			cursor.moveToNext();
		}
		cursor.close();
		
		return dls;
	}
	
	public Download createDownload(long id, Podcast podcast, Episode episode)
	{
		String dir = Podcast.getPodcastDirectory(podcast.getName());
		String url = episode.getUrl();
		String file = url.substring(url.lastIndexOf("/") + 1);
		//add it to the db
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.DL_ID, id);
		values.put(DatabaseHelper.DL_PODCAST_NAME, dbHelper.escapeString(podcast.getName()));
		values.put(DatabaseHelper.DL_EPISODE_NAME, dbHelper.escapeString(episode.getName()));
		values.put(DatabaseHelper.DL_DIRECTORY, dbHelper.escapeString(dir));
		values.put(DatabaseHelper.DL_FILE, dbHelper.escapeString(file));
		db.insert(DatabaseHelper.TABLE_DOWNLOADS, null, values);
		//ship it back
		Download dl = new Download();
		dl.setPodcastName(podcast.getName());
		dl.setEpisodeName(podcast.getName());
		dl.setDownloadId(id);
		dl.setDir(dir);
		dl.setFile(file);
		return dl;
	}
	
	public void delete(long id)
	{
		db.delete(DatabaseHelper.TABLE_DOWNLOADS, DatabaseHelper.DL_ID + "=" + id, null);
	}

	private Download cursorToDownload(Cursor cursor) 
	{
		Download dl = new Download();
		dl.setDownloadId(cursor.getLong(0));
		dl.setPodcastName(dbHelper.unescapeString(cursor.getString(1)));
		dl.setEpisodeName(dbHelper.unescapeString(cursor.getString(2)));
		dl.setDir(dbHelper.unescapeString(cursor.getString(3)));
		dl.setFile(dbHelper.unescapeString(cursor.getString(4)));
		return dl;
	}
}