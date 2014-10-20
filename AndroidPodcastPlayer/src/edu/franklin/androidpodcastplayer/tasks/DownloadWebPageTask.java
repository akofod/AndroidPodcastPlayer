package edu.franklin.androidpodcastplayer.tasks;

import java.io.BufferedInputStream;
import java.util.Scanner;

import edu.franklin.androidpodcastplayer.utilities.Downloader;

import android.os.AsyncTask;

public class DownloadWebPageTask extends AsyncTask<String, Void, String>
{
	protected String doInBackground(String... params) 
	{
		String string = "";
		try
		{
			Scanner s = new Scanner(Downloader.downloadUrl(params[0])).useDelimiter("\\A");
		    if(s.hasNext()) string = s.next();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return string;
	}
}
