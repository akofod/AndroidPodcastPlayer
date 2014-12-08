package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;
import java.util.List;

import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Subscription;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SubscriptionData 
{
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.SUB_ID, 
			DatabaseHelper.SUB_AUTO, DatabaseHelper.SUB_EPISODES,
			DatabaseHelper.SUB_NEWEST, DatabaseHelper.SUB_FREQUENCY,
			DatabaseHelper.SUB_LAST_UPDATE};
	private PodcastData podData = null;

	// Logcat tag
	private static final String LOG = "SubscriptionData";
	
	/**
	 * Constructs an instance of SubscriptionData.
	 * 
	 * @param context
	 */
	public SubscriptionData(Context context, PodcastData podData) 
	{
		dbHelper = new DatabaseHelper(context);
		this.podData = podData;
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
	
	public Subscription getSubscriptionById(long id)
	{
		Subscription sub = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_SUBSCRIPTION, allColumns, 
				DatabaseHelper.SUB_ID + "=" + id, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) 
		{
			sub = cursorToSubscription(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		return sub;
	}
	
	public List<Subscription> getAllSubscriptions()
	{
		List<Subscription> subs = new ArrayList<Subscription>();
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_SUBSCRIPTION, allColumns, 
				null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) 
		{
			Subscription sub = cursorToSubscription(cursor);
			subs.add(sub);
			cursor.moveToNext();
		}
		cursor.close();
		
		return subs;
	}
	
	private ContentValues getValuesForSubscription(Subscription sub)
	{
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.SUB_ID, sub.getPodcast().getPodcastId());
		values.put(DatabaseHelper.SUB_AUTO, sub.isAutoDownload() ? 1 : 0);
		values.put(DatabaseHelper.SUB_EPISODES, sub.getEpisodes());
		values.put(DatabaseHelper.SUB_NEWEST, sub.isAutoDownload() ? 1 : 0);
		values.put(DatabaseHelper.SUB_FREQUENCY, sub.getFrequency());
		values.put(DatabaseHelper.SUB_LAST_UPDATE, sub.getLastUpdate());
		return values;
	}
	
	public Subscription createSubscription(Podcast podcast)
	{
		Subscription sub = new Subscription();
		sub.setPodcast(podcast);
		ContentValues values = getValuesForSubscription(sub);
		db.insert(DatabaseHelper.TABLE_SUBSCRIPTION, null, values);
		return sub;
	}
	
	public void updateSubscription(Subscription sub)
	{
		ContentValues values = getValuesForSubscription(sub);
		db.update(DatabaseHelper.TABLE_SUBSCRIPTION, values, DatabaseHelper.SUB_ID + "=" + sub.getPodcast().getPodcastId(), null);
		podData.setAutoDelete(sub.getPodcast().getPodcastId(), sub.getPodcast().isAutoDelete());
	}

	private Subscription cursorToSubscription(Cursor cursor) 
	{
		Subscription sub = new Subscription();
		Long podId = cursor.getLong(0);
		sub.setPodcast(podData.getPodcastById(podId));
		sub.setAutoDownload(cursor.getInt(1) == 1 ? true : false);
		sub.setEpisodes(cursor.getInt(2));
		sub.setNewestFirst(cursor.getInt(3) == 1 ? true : false);
		sub.setFrequency(cursor.getInt(4));
		sub.setLastUpdate(cursor.getInt(5));
		return sub;
	}
	
	public void purgeSubscription(long id)
	{
		db.delete(DatabaseHelper.TABLE_SUBSCRIPTION, DatabaseHelper.SUB_ID + "=" + id, null);
	}
}

