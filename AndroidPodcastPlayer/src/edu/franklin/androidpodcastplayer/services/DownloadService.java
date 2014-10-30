package edu.franklin.androidpodcastplayer.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;

/**
 * The DownlodService is a one off that can be used to fetch the file
 * and write it to the specified filepath
 * @author rennardhutchinson
 *
 */
public class DownloadService extends IntentService 
{
	private int result = Activity.RESULT_CANCELED;
	public static final String URL = "urlpath";
	public static final String FILEPATH = "filepath";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "receiver";

	public DownloadService() 
	{
		super("DownloadService");
	}

	// will be called asynchronously by Android
	protected void onHandleIntent(Intent intent) 
	{
		String urlPath = intent.getStringExtra(URL);
		String filepath = intent.getStringExtra(FILEPATH);

		InputStream stream = null;
		FileOutputStream fos = null;
		
		try 
		{
			fos = new FileOutputStream(new File(filepath));
			URL url = new URL(urlPath);
			stream = url.openConnection().getInputStream();
			InputStreamReader reader = new InputStreamReader(stream);
			int next = -1;
			while ((next = reader.read()) != -1) 
			{
				fos.write(next);
			}
			// successfully finished
			result = Activity.RESULT_OK;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if (stream != null) 
			{
				try 
				{
					stream.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			if (fos != null) 
			{
				try 
				{
					fos.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		publishResults(filepath, result);
	}

	private void publishResults(String filepath, int result) 
	{
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(FILEPATH, filepath);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}
}
