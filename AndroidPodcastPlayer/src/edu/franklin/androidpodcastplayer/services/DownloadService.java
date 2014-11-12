package edu.franklin.androidpodcastplayer.services;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * The DownlodService is a one off that can be used to fetch the file
 * and write it to the specified filepath
 * @author rennardhutchinson
 *
 */
public class DownloadService extends IntentService 
{
	public static final String URL = "urlpath";
	public static final String DIR = "dir";
	public static final String FILE = "filename";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "receiver";
	private String dir = null;
	private String file = null;
	private Long queueId = null;
	private DownloadManager dm = null;
	private FileManager fileManager = null;
	private boolean done = false;

	public DownloadService() 
	{
		super("DownloadService");
		fileManager = new FileManager(this);
	}

	// will be called asynchronously by Android
	protected void onHandleIntent(Intent intent) 
	{
		//the file to fetch
		String urlPath = intent.getStringExtra(URL);
		//where to put it when it has been fetched;
		dir = intent.getStringExtra(DIR);
		file = intent.getStringExtra(FILE);
		//now get the system download manager
		dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		//create a request so we can fetch our download
		Request request = new Request(Uri.parse(urlPath));
		//now wait for it to finish
		queueId = Long.valueOf(dm.enqueue(request));
		//listen for it to finish
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //really hackish way to block but not forever...
		while(!done)
		{
			try
			{
				Thread.sleep(50);
			}
			catch(Exception e)
			{
				//
			}
		}
		unregisterReceiver(receiver);
	}

	//tell the sender the download has finished
	private void publishResults(int result) 
	{
		Intent intent = new Intent(DownloadService.NOTIFICATION);
		intent.putExtra(DIR, dir);
		intent.putExtra(FILE, file);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			Bundle bundle = intent.getExtras();
			String action = intent.getAction();
			Log.d("Download", "Got back an action " + action);
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
            {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                //ignore values we don't care about
                if(downloadId != queueId.longValue())
                {
                	return;
                }
                //otherwise, get the info
                Query query = new Query();
                query.setFilterById(queueId);
                Cursor c = dm.query(query);
                //anything to look at?
                if(c.getCount() > 0)
                {
                	//set initial cursor spot
                	c.moveToFirst();
            		int statusIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = c.getInt(statusIndex);
                    int fileLocationIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    String fileLocation = c.getString(fileLocationIndex);
                    Log.d("Download", "Status of " + fileLocation + " is " + status);
                    if (DownloadManager.STATUS_SUCCESSFUL == status) 
                    {
                		//put the downloaded file into our storage
                		fileManager.moveFile(fileLocation, dir, file);
                		Log.d("Download", file + " has been downloaded to " + fileManager.getAbsoluteFilePath(dir, file)); 
                    	dm.remove(downloadId);
                    	//tell the guy that started this whole mess
                    	publishResults(Activity.RESULT_OK);
                    }
                }
                //now close up the cursor
                c.close();
                //exit the loop we made above
                done = true;
            }
		}
	};
}
