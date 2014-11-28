package edu.franklin.androidpodcastplayer.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.franklin.androidpodcastplayer.services.FileManager;
import edu.franklin.androidpodcastplayer.utilities.Downloader;

public class DownloadFileTask extends AsyncTask<String, Void, String>
{
	private FileManager fm = null;
	private DownloadHandler handler = null;
	private String url;
	private String dir;
	private String file;
	
	public DownloadFileTask(Context context)
	{
		super();
		fm = new FileManager(context);
	}
	
	public void setHandler(DownloadHandler handler)
	{
		this.handler = handler;
	}
	
	protected String doInBackground(String... params) 
	{
		url = params[0];
		dir = params[1];
		file = params[2];
		Log.i("DL", "Downloading file into " + fm.getAbsoluteFilePath(dir, file));
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
				return "";
			}
		}
		catch(Exception e)
		{
			Log.e("DownloadTask", "Problem downloading file " + dir + ":" + file + " from " + url, e);
			//if there was a problem. get rid of the partial
			outFile.delete();
			return "";
		}
		return outFile.getAbsolutePath();
	}
	
	protected void onPostExecute(String result)
	{
		if(handler != null)
		{
			if(result.length() > 0)
			{
				handler.downloadFinished(dir, file);
			}
			else
			{
				handler.downloadFailed(url, dir, file);
			}
		}
	}
}
