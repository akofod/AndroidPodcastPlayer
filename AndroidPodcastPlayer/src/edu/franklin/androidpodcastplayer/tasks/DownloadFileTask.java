package edu.franklin.androidpodcastplayer.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import edu.franklin.androidpodcastplayer.services.FileManager;
import edu.franklin.androidpodcastplayer.utilities.Downloader;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class DownloadFileTask extends AsyncTask<String, Void, String>
{
	private FileManager fm = null;
	private String url;
	private String dir;
	private String file;
	
	public DownloadFileTask(Context context)
	{
		super();
		fm = new FileManager(context);
	}
	
	protected String doInBackground(String... params) 
	{
		url = params[0];
		dir = params[1];
		file = params[2];
		File outFile = new File(fm.getAbsoluteFilePath(dir, file));
		try
		{
			//get rid of the old
			if(outFile.exists())
			{
				outFile.delete();
			}
			FileOutputStream outStream = new FileOutputStream(outFile);
			InputStream input = Downloader.downloadUrl(url);
			int read = 0;
			byte[] buffer = new byte[2048];
			//now copy the file
			while((read = input.read(buffer)) != -1)
			{
				outStream.write(buffer, 0, read);
			}
			outStream.close();
			input.close();
			//if nothing was written, get rid of it.
			if(outFile.length() == 0)
			{
				outFile.delete();
			}
			return url + " has been downloaded";
		}
		catch(Exception e)
		{
			Log.e("DownloadTask", "Problem downloading file " + e);
			//if there was a problem. get rid of the partial
			outFile.delete();
		}
		
		return url + " failed to download";
	}
}
