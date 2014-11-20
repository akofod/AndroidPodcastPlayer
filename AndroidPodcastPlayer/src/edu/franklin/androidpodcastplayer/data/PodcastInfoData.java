package edu.franklin.androidpodcastplayer.data;

import java.util.ArrayList;
import java.util.List;

import edu.franklin.androidpodcastplayer.models.PodcastInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PodcastInfoData 
{
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String[] allColumns = {DatabaseHelper.PC_INFO_ID, 
			DatabaseHelper.PC_INFO_NAME, DatabaseHelper.PC_INFO_DESCRIPTION,
			DatabaseHelper.PC_INFO_URL, DatabaseHelper.PC_INFO_IMAGE_URL, DatabaseHelper.PC_INFO_POSITION};

	// Logcat tag
	private static final String LOG = "PodcastInfoData";
	
	/**
	 * Constructs an instance of PodcastInfoData.
	 * 
	 * @param context
	 */
	public PodcastInfoData(Context context) 
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
	
	
	public PodcastInfo createPodcastInfo(PodcastInfo info)
	{
		PodcastInfo in = this.retrievePodcastInfoByPosition(info.getPosition());
		if(in != null)
		{
			//if the podcasts are different, get rid of the old one
			if(!in.getName().equals(info.getName()))
			{
				purgeExisting(in);
				in = null;
			}
		}
		if(in == null)
		{
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.PC_INFO_NAME, dbHelper.escapeString(info.getName()));
			values.put(DatabaseHelper.PC_INFO_DESCRIPTION, dbHelper.escapeString(info.getDescription()));
			values.put(DatabaseHelper.PC_INFO_URL, dbHelper.escapeString(info.getUrl()));
			values.put(DatabaseHelper.PC_INFO_IMAGE_URL, dbHelper.escapeString(info.getImageUrl()));
			values.put(DatabaseHelper.PC_INFO_POSITION, info.getPosition());
			long inId = db.insert(DatabaseHelper.TABLE_PODCAST_INFO, null, values);
			Cursor cursor = db.query(DatabaseHelper.TABLE_PODCAST_INFO, allColumns, 
				DatabaseHelper.PC_INFO_ID + " = " + inId, null, null, null, null);
			if(cursor.moveToFirst())
			{
				in = cursorToPodcastInfo(cursor);
			}
			cursor.close();
		}
		return in;
	}
	
	public void purgeAllInfo()
	{
		dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_PODCAST_INFO, null, null);
	}
	
	public void purgeExisting(PodcastInfo info)
	{
		db.delete(dbHelper.TABLE_PODCAST_INFO, dbHelper.PC_INFO_POSITION + "=" + info.getPosition(), null);
	}
	
	public PodcastInfo retrievePodcastInfoByPosition(int position) 
	{
		PodcastInfo info = null;
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_PODCAST_INFO, allColumns, 
			DatabaseHelper.PC_INFO_POSITION + " = " + position, 
			null, null, null, null);
		if(cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			info = cursorToPodcastInfo(cursor);
		}
		cursor.close();
		
		return info;
	}
	
	public PodcastInfo[] getPodcastInfoByPosition(int start, int end)
	{
		List<PodcastInfo> list = new ArrayList<PodcastInfo>();
		SQLiteDatabase readDB = dbHelper.getReadableDatabase();
		Cursor cursor = readDB.query(DatabaseHelper.TABLE_PODCAST_INFO, allColumns, 
			DatabaseHelper.PC_INFO_POSITION + " >= " + start + " AND " +
			DatabaseHelper.PC_INFO_POSITION + " <= " + end, 
			null, null, null, null);
		if(cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) 
			{
				list.add(cursorToPodcastInfo(cursor));
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		return list.toArray(new PodcastInfo[]{});
	}

	private PodcastInfo cursorToPodcastInfo(Cursor cursor) {
		PodcastInfo info = new PodcastInfo();
		info.setId(cursor.getLong(0));
		info.setName(dbHelper.unescapeString(cursor.getString(1)));
		info.setDescription(dbHelper.unescapeString(cursor.getString(2)));
		info.setUrl(dbHelper.unescapeString(cursor.getString(3)));
		info.setImageUrl(dbHelper.unescapeString(cursor.getString(4)));
		info.setPosition(cursor.getInt(5));
		
		return info;
	}
}

