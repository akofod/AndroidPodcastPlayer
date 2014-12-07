package edu.franklin.androidpodcastplayer.services;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import edu.franklin.androidpodcastplayer.data.DownloadData;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Download;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;

/**
 * The DownlodService is a one off that can be used to fetch the file
 * and write it to the specified filepath. 
 * 
 * I am going to rework this
 * as a singleton object so it can be used to load up any unfinished
 * downloads on application startup, monitor them, and 
 * move them into place once they have finished.
 * 
 * This should let us have the episodes get downloaded by a service,
 * and let the episode row show the progress bar while it waits.
 * 
 * This should also let us do things like pause a download, cancel it,
 * etc.
 * @author rennardhutchinson
 *
 */
public class DownloadService 
{
	public static final String URL = "urlpath";
	public static final String DIR = "dir";
	public static final String FILE = "filename";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "receiver";
	public static final int UNKNOWN = DownloadManager.ERROR_UNKNOWN;
	public static final int DOWNLOADING = DownloadManager.STATUS_RUNNING;
	public static final int PAUSED = DownloadManager.STATUS_PAUSED;
	public static final int NOT_DOWNLOADING = DownloadManager.STATUS_SUCCESSFUL;
	private DownloadManager dm = null;
	private Context context = null;
	private FileManager fileManager = null;
	private boolean initialized = false;
	private DownloadData data = null;
	private PodcastData podData = null;
	private EpisodesData episodeData = null;
	
	private static DownloadService INST = null;

	private  DownloadService() 
	{
		//
	}
	
	public static DownloadService getInstance(Context context)
	{
		if(INST == null)
		{
			INST = new DownloadService();
		}
		INST.initialize(context);
		return INST;
	}
	
	public void initialize(Context context)
	{
		if(!initialized)
		{
			this.context = context;
			fileManager = new FileManager(context);
			dm = (DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
			data = new DownloadData(context);
			data.open();
			episodeData = new EpisodesData(context);
			episodeData.open();
			podData = new PodcastData(context);
			podData.open();
			this.context = context;
			//now listen for download complete messages
			initialized = true;
			context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		}
	}
	
	public void close()
	{
		context.unregisterReceiver(receiver);
		data.close();
		episodeData.close();
		podData.close();
		initialized = false;
	}
	
	public long downloadEpisode(Podcast podcast, Episode episode)
	{
		long downloadId = 0L;
		Download dl = data.getDownload(podcast.getName(), episode.getName());
		//cancel any funky states we may be in from bad downloads.
		if(dl != null)
		{
			dm.remove(dl.getDownloadId());
		}
		String url = episode.getUrl();
		Uri uri = Uri.parse(url);
		Request request = new Request(uri);
		downloadId = dm.enqueue(request);
		dl = data.createDownload(downloadId, podcast, episode);
		
		return downloadId;
	}
	
	public int getDownloadStatus(Podcast podcast, Episode e)
	{
		int status = DownloadService.UNKNOWN;
		Download dl = data.getDownload(podcast.getName(), e.getName());
		
		if(dl != null)
		{
			Query query = new Query();
	        query.setFilterById(dl.getDownloadId());
	        Cursor c = dm.query(query);
	        if(c.getCount() > 0)
	        {
	        	c.moveToFirst();
	        	int statusIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
	        	int dlStatus = c.getInt(statusIndex);
	        	switch(dlStatus)
	        	{
		        	case DownloadManager.STATUS_FAILED:
		        	case DownloadManager.STATUS_SUCCESSFUL:
		        		status = DownloadService.NOT_DOWNLOADING;
		        		break;
		        	case DownloadManager.STATUS_PENDING:
		        	case DownloadManager.STATUS_RUNNING:
		        		status = DownloadService.DOWNLOADING;
		        		break;
		        	case DownloadManager.STATUS_PAUSED:
		        		status = DownloadService.PAUSED;
	        	}
	        }
	        c.close();
		}
		return status;
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, final Intent intent) 
		{
			Runnable r = new Runnable()
			{
				public void run()
				{
					String action = intent.getAction();
		            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
		            {
		                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		                Download dl = data.getDownload(downloadId);
		                if(dl == null)
		                {
		                	return;
		                }
		                //otherwise, get the info
		                Query query = new Query();
		                query.setFilterById(downloadId);
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
		                    if (DownloadManager.STATUS_SUCCESSFUL == status) 
		                    {
		                		//put the downloaded file into our storage
		                		fileManager.moveFile(fileLocation, dl.getDir(), dl.getFile());
		                		Podcast pc = podData.retrievePodcastByName(dl.getPodcastName());
		                		//if we have this pc ,then we are subscribed...update the episode file path
		                		if(pc != null)
		                		{
		                			Episode ep = episodeData.retrieveEpisodeByName(pc.getPodcastId(), dl.getEpisodeName());
		                			if(ep != null)
		                			{
		                				episodeData.updateFilePath(pc.getPodcastId(), ep.getEpisodeId(), fileManager.getAbsoluteFilePath(dl.getDir(), dl.getFile()));
		                				episodeData.setNewFlag(pc.getPodcastId(), ep.getEpisodeId(), true);
		                			}
		                			//now we can update the saved count
		                			podData.updateSavedCount(pc.getPodcastId());
	                			}
		                    	dm.remove(downloadId);
		                    	//get rid of this because we don't need it anymore
		                    	data.delete(downloadId);
		                    }
		                }
		                //now close up the cursor
		                c.close();
		            }
				}
			};
			Thread t = new Thread(r);
			t.start();
		}
	};
}
