package edu.franklin.androidpodcastplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.services.FileManager;

public class EpisodeRow extends TableRow 
{
	private Activity parentActivity = null;
	private TextView titleView = null;
	private TextView durationView = null;
	private Button button = null;
	private ProgressBar downloadProgress = null;
	private Podcast podcast = null;
	private Episode episode = null;
	private EpisodesData data = null;
	private String fileName = null;
	//in case the user wants to fetch the episode
	private DownloadManager dm = null;
	private FileManager fileManager = null;
	private long downloadId = 0L;
	
	//Screen dimensions for more exact positioning
	private int screenHeight;
	private int screenWidth;
	
	@SuppressLint("NewApi") public EpisodeRow(Context context, Episode e, Podcast pc, EpisodesData data) 
	{
		super(context);
		fileManager = new FileManager(context);
		parentActivity = (Activity)context;
		this.podcast = pc;
		this.data = data;
		
		//create the relative layout to hold the rest
		RelativeLayout rl = new RelativeLayout(context);
		rl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 50));
		
		button = new Button(context);
		button.setId(3);
		button.setTextSize(10);
		RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 40);
		buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
		rl.addView(button, buttonLayout);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				handleEpisode(v);
			}
		});

		titleView = new TextView(context);
		titleView.setId(1);
		titleView.setTextSize(12);
		titleView.setEllipsize(TruncateAt.END);
		titleView.setLines(2);
		RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		titleLayout.addRule(RelativeLayout.LEFT_OF, 3);
		rl.addView(titleView, titleLayout);
		
		durationView = new TextView(context);
		durationView.setId(2);
		durationView.setTextSize(10);
		durationView.setPadding(0, 12, 0, 5); //TODO: CHANGED
		RelativeLayout.LayoutParams durationLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		durationLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		durationLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1); //TODO: CHANGED
		rl.addView(durationView, durationLayout);
		
		downloadProgress = new ProgressBar(context);
		downloadProgress.setId(4);
		RelativeLayout.LayoutParams progressLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 40);
		progressLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 3);
		//assume not visible unless we are actually downloading something
		downloadProgress.setVisibility(INVISIBLE);
		rl.addView(downloadProgress, progressLayout);
		
		//now add the view to the table row parent
		addView(rl);
		setEpisode(e);
	}
	
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		dm = (DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		getContext().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	
	public void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		getContext().unregisterReceiver(receiver);
	}
	
	public void setEpisode(Episode e)
	{
		this.setId((int)e.getEpisodeId());
		titleView.setText(e.getName());
		durationView.setText(getDurationString(e.getTotalTime()));
		//also hang onto the reference
		episode = e;
		updateButtonText();
	}
	
	private void updateButtonText()
	{
		downloadProgress.setVisibility(INVISIBLE);
		if(filePresent())
		{
			button.setText("Play");
		}
		else
		{
			button.setText("Download");
		}
	}
	
	//we get here by button pressing
	public void handleEpisode(View view)
	{
		//play it
		if(filePresent())
		{
			//play it by invoking the MediaPlayer
			Intent intent = new Intent(getContext(), PlayPodcastActivity.class);
			intent.putExtra("ID", episode.getPodcastId());
			intent.putExtra("NAME", episode.getName());
			getContext().startActivity(intent);
		}
		//download it
		else
		{
			String link = episode.getUrl();
			String eName = link.substring(link.lastIndexOf("/") + 1);
			downloadFile(Podcast.getPodcastDirectory(podcast.getName()), eName, link);
			episode.setFilepath(fileManager.getAbsoluteFilePath(Podcast.getPodcastDirectory(podcast.getName()), eName));
			Log.d("Rss----", "Episode file path is now " + episode.getFilepath());
		}
	}
	
	public void downloadFile(final String dir, final String file, final String url)
	{
		//hide the button...
		button.setVisibility(INVISIBLE);
		//show the progress
		downloadProgress.setVisibility(VISIBLE);
		Runnable downloadRunnable = new Runnable()
		{
			public void run()
			{
				Log.d("Rss------", "Downloading " + dir + ":" + file + " located at : " + url);
				Uri uri = Uri.parse(url);
				fileName = file;
				Request request = new Request(uri);
				downloadId = dm.enqueue(request);
			}
		};
		Thread t = new Thread(downloadRunnable);
		t.start();
	}
	
	private boolean filePresent()
	{
		String link = episode.getUrl();
		String eName = link.substring(link.lastIndexOf("/") + 1);
		return fileManager.fileExists(Podcast.getPodcastDirectory(podcast.getName()), eName);
	}
	
	private String getDurationString(long duration)
	{
		final int MINUTE = 60;
		final int HOUR = 60 * MINUTE;
		if(duration > 0)
		{
			int hours = (int) duration / HOUR;
			duration -= HOUR * hours;
			int minutes = (int) duration / MINUTE;
			duration -= MINUTE * minutes;
			int seconds = (int)duration;
			String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			return timeString;
		}
		return "-";
	}
	
	private void updateButtonsOnUiThread()
	{
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				//get rid of the progress bar
        		downloadProgress.setVisibility(INVISIBLE);
        		//show the button so they can play their file
        		button.setVisibility(VISIBLE);
        		updateButtonText();
			}
		};
		parentActivity.runOnUiThread(runnable);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			//if we aren't downloading anything, bail...we don't care about the notification
			if(downloadId == 0) return;
			
			final Intent rIntent = intent;
			Runnable r = new Runnable()
			{
				public void run()
				{
					String action = rIntent.getAction();
					Log.d("Episode Download", "Got back an action " + action);
		            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
		            {
		                long id = rIntent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		                if(id != downloadId)
		                {
		                	return;
		                }
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
		                    Log.d("Episode Download", "Status of " + fileLocation + " is " + status);
		                    if (DownloadManager.STATUS_SUCCESSFUL == status) 
		                    {
		                		//put the downloaded file into our storage
		                		fileManager.moveFile(fileLocation, Podcast.getPodcastDirectory(podcast.getName()), fileName);
		                    	dm.remove(downloadId);
		                    	//now update the filepath in the episode object
		                    	boolean updated = data.updateFilePath(podcast.getPodcastId(), episode.getEpisodeId(), episode.getFilepath());
		                    	if(updated)
		                    	{
			                    	updateButtonsOnUiThread();	
		                    	}
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
