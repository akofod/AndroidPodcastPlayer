package edu.franklin.androidpodcastplayer;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.services.FileManager;

public class EpisodeRow extends TableRow 
{
	private TextView titleView = null;
	private TextView durationView = null;
	private Button button = null;
	private Podcast podcast = null;
	private Episode episode = null;
	private EpisodesData data = null;
	private String fileName = null;
	//in case the user wants to fetch the episode
	private DownloadManager dm = null;
	private FileManager fileManager = null;
	
	public EpisodeRow(Context context, Episode e, Podcast pc, EpisodesData data) 
	{
		super(context);
		fileManager = new FileManager(context);
		this.podcast = pc;
		this.data = data;
		
		//create the relative layout to hold the rest
		RelativeLayout rl = new RelativeLayout(context);
		rl.setLayoutParams(new LayoutParams(288, 30));

		titleView = new TextView(context);
		titleView.setId(1);
		titleView.setTextSize(10);
		RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		rl.addView(titleView, titleLayout);
		
		durationView = new TextView(context);
		durationView.setId(2);
		durationView.setTextSize(8);
		durationView.setPadding(0, 11, 0, 0);
		RelativeLayout.LayoutParams durationLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		durationLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		durationLayout.addRule(RelativeLayout.BELOW, 2);
		rl.addView(durationView, durationLayout);
		
		button = new Button(context);
		button.setId(3);
		button.setTextSize(10);
		RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
		buttonLayout.addRule(RelativeLayout.CENTER_VERTICAL, 1);
		rl.addView(button, buttonLayout);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				handleEpisode(v);
			}
		});
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
		//maybe some magic here for the button text and action...
				//if we are already downloaded, the button could be labeled 'play'.
				//if not, we could have it say download
		if(filePresent())
		{
			button.setText("Play");
		}
		else
		{
			button.setText("Download");
		}
	}
	
	public void handleEpisode(View view)
	{
		//play it
		if(filePresent())
		{
			//play it by invoking the MediaPlayer
			Intent intent = new Intent(getContext(), PlayPodcastActivity.class);
			intent.putExtra("ID", episode.getEpisodeId());
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
	
	public void downloadFile(String dir, String file, String url)
	{
		Log.d("Rss------", "Downloading " + dir + ":" + file + " located at : " + url);
		Uri uri = Uri.parse(url);
		fileName = file;
		Request request = new Request(uri);
        dm.enqueue(request);
	}
	
	private boolean filePresent()
	{
		return episode.getFilepath() != null && episode.getFilepath().length() > 0;
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
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			Bundle bundle = intent.getExtras();
			String action = intent.getAction();
			Log.d("Episode Download", "Got back an action " + action);
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
            {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
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
                    		updateButtonText();
                    	}
                    }
                }
                //now close up the cursor
                c.close();
            }
		}
	};
}
