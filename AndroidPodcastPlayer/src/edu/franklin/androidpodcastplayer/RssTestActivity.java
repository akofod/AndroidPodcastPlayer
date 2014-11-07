package edu.franklin.androidpodcastplayer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;
import edu.franklin.androidpodcastplayer.data.ConfigData;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Channel;
import edu.franklin.androidpodcastplayer.models.Image;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Rss;
import edu.franklin.androidpodcastplayer.services.DownloadService;
import edu.franklin.androidpodcastplayer.services.FileManager;
import edu.franklin.androidpodcastplayer.services.FileManager.FileManagerBinder;

public class RssTestActivity extends ActionBarActivity 
{
	//the top level folder for the subscriptions
	private static final String PODCASTS = "podcast_subscriptions";
	private Context context = this;
	//we are going to ignore the config stuff for now...but a real
	//subscription would want to be able to set user preferences
	private ConfigData configData = new ConfigData(context);
	private PodcastData podcastData = new PodcastData(this);
	private EpisodesData episodesData = new EpisodesData(this);
	//need to save off the images and episodes we fetch
	private FileManager fileManager = null;
	private DownloadManager dm = null;
	//we bind to the file manager so we can use it directly.
	private boolean bound = false;
	//a map of queued downloads to thier file names
	Map<Long, String> downloadMap = new HashMap<Long, String>();
	
	//to be bound, we need to create a service connection
	private ServiceConnection mConnection = new ServiceConnection() 
	{

		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to fileManager, cast the IBinder and get
			// fileManager instance
			FileManagerBinder binder = (FileManagerBinder) service;
			fileManager = binder.getService();
			bound = true;
			//once we are bound to the filemanager service, 
			//we can try to subscribe
			subscribeToRawRssFeeds();
		}

		public void onServiceDisconnected(ComponentName componentName) 
		{
			bound = false;
		}
	};
	
	protected void onResume() 
	{
		super.onResume();
		//if we got paused, re-register for notifications
		registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
	}

	protected void onPause() 
	{
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	protected void onStart() 
	{
		super.onStart();
		// Bind to the FileManager
		Intent intent = new Intent(this, FileManager.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	protected void onStop() 
	{
		super.onStop();
		// Unbind from the FileManager
		if (bound) {
			unbindService(mConnection);
			bound = false;
		}
		//close down the db stuff?
		configData.close();
		podcastData.close();
		episodesData.close();
	}
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss_test);
		//get a ref to the download manager
		dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		//listen for finishing downloads...
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		//open up the data helpers into the database
		configData.open();
		podcastData.open();
		episodesData.open();
	}
	
	private void subscribeToRawRssFeeds()
	{
		try
		{
			//something to fetch the raw junk with
			Resources resources = getResources();
			//the ids we want to fetch
			int[] rawFeeds = new int[]{
				R.raw.androd_dev_backstage_rss,
				R.raw.coder_radio_rss,
				R.raw.java_posse_rss,
				R.raw.technophilia_rss
			};
			//now go over the feed ids and initialize an Rss object from the xml
			for(int id : rawFeeds)
			{
				Rss rss = new Rss();
				rss.initializeFromRaw(resources.openRawResource(id));
				Log.i("Raw Rss Test", "Got back an Rss object!\n" + rss.toString());
				//for the demo, we just assume a subscription
				subscribeToRss(rss);			
			}
		}
		catch(Exception e)
		{
			Log.e("Raw RSS", "Could not load the raw rss stuff", e);
		}
	}
	
	private void subscribeToRss(Rss rss)
	{
		//first things first, grab an image for this guy
		Channel channel = rss.getChannel();
		Image image = channel.getImage();
		String podcastTitle = channel.getTitle();
		String podcastHomeDir = getPodcastDirectory(podcastTitle);
		Podcast pc = new Podcast();
		//fetch the actual image from the webs
		if(image != null)
		{
			String imageName = image.getUrl().substring(image.getUrl().lastIndexOf("/") + 1);
			downloadFile(podcastHomeDir, imageName, image.getUrl());
			Log.d("Raw Rss Sub", "Fetching an " + imageName + " at " + image.getUrl());
			pc.setImage(fileManager.getAbsoluteFilePath(podcastHomeDir, imageName));
		}
		//or just make a dir for this podcast
		else
		{
			fileManager.mkDir(podcastHomeDir);
		}
		Log.d("Raw Rss Sub", podcastTitle + " saved to " + fileManager.getAbsoluteFilePath(podcastHomeDir, null));
		//while we are at it, grab an episode as well?
		//TODO, fetch an image maybe...but that could take a while
		//either way, make an entry in the database for this podcast
		pc.setName(podcastTitle);
		pc.setDescription(channel.getDescription());
		pc.setNumEpisodes(0L);
		pc.setFeedUrl(channel.getLink());
		pc.setDir(fileManager.getAbsoluteFilePath(podcastHomeDir, null));
		//that should be enough to persist this guy
		//TODO, the sqlite is throwing an exception here...
//		pc = podcastData.createPodcast(pc);
//		Toast.makeText(getApplicationContext(), podcastTitle + " is in the database", Toast.LENGTH_SHORT);
	}
	
	public void downloadFile(String dir, String file, String url)
	{
		Uri uri = Uri.parse(url);
		Request request = new Request(uri);
        Long queuedId = Long.valueOf(dm.enqueue(request));
        downloadMap.put(queuedId, dir + ":" + file);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_settings) 
		{
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void fetchRss(View view)
	{
		int duration = Toast.LENGTH_SHORT;
		//grab the text box and get its text
		EditText editText = (EditText) findViewById(R.id.urlField);
		//get its text
		String url = editText.getText().toString();
		//show what we are doing
		Toast.makeText(getApplicationContext(), "Fetching Rss at " + url, duration).show();
		try
		{
			Rss rss = new Rss();
			rss.initializeFromUrl(url);
			subscribeToRss(rss);
			WebView wv = (WebView)findViewById(R.id.webView1);
			wv.loadData(rss.toHtml(url), "text/html", null);
			
		}
		catch(Exception e)
		{
			Log.e("RSS", "Could not parse the RSS!", e);
			Toast.makeText(getApplicationContext(), 
					"Rss could not be parsed. Are you sure the url is valid? Check LogCat for problems.", 
					Toast.LENGTH_LONG).show();
		}
	}
	
	private String getPodcastDirectory(String dir)
	{
		return PODCASTS + "/" + dir;
	}
	
	private void showDownloadStatus(String filename, boolean success)
	{
		Toast.makeText(getApplicationContext(), success ? 
			filename + " has been downloaded!" :
			filename + " failed to download. Checl the logs to see what blew up.", 
			Toast.LENGTH_SHORT).show();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
//			Bundle bundle = intent.getExtras();
			String action = intent.getAction();
			Log.d("Rss Sub Download", "Got back an action " + action);
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Query query = new Query();
                long[] idArray = new long[downloadMap.size()];
                int index = 0;
                for(Long idLong : downloadMap.keySet())
                {
                	idArray[index++] = idLong.longValue();
                }
                query.setFilterById(idArray);
                Cursor c = dm.query(query);
                //anything to look at?
                if(c.getCount() > 0)
                {
                	//set initial cursor spot
                	c.moveToFirst();
                	do
                	{
                		int statusIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = c.getInt(statusIndex);
                        int fileLocationIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        String fileLocation = c.getString(fileLocationIndex);
                        Log.d("Rss Sub Download", "Status of " + fileLocation + " is " + status);
                        if (DownloadManager.STATUS_SUCCESSFUL == status) 
                        {
                        	String fileInfo = downloadMap.get(Long.valueOf(downloadId));
                        	if(fileInfo != null)
                        	{
                        		String[] tokens = fileInfo.split(":");
                        		String dir = tokens[0];
                        		String file = tokens[1];
                        		//put the downloaded file into our storage
                        		boolean moved = fileManager.moveFile(fileLocation, dir, file);
                        		Log.d("Rss Sub Download", 
                        				fileLocation + " moved to " + 
                						fileManager.getAbsoluteFilePath(dir, file) + " = " + moved);
                        		//remove this entry from the ones we are waiting on...it is done
                            	downloadMap.remove(downloadId);
                            	showDownloadStatus(file, moved);
                        	}
                        }
                	}
                	while(c.moveToNext());
                }
                else
                {
                	Log.d("Rss Sub Download", "Got a download event, but no rows were returned");
                }
                //now close up the cursor
                c.close();
            }
		}
	};
}
