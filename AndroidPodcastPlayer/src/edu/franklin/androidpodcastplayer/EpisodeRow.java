package edu.franklin.androidpodcastplayer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.franklin.androidpodcastplayer.data.DownloadData;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.models.Download;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.services.DownloadService;
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
	private DownloadData downloadData = null;
	private FileManager fileManager = null;
	private long downloadId = 0L;
	private Timer timer = new Timer();
	private float density = 0;
	
	@SuppressLint("NewApi") public EpisodeRow(Context context, Episode e, Podcast pc, EpisodesData data) 
	{
		super(context);
		fileManager = new FileManager(context);
		parentActivity = (Activity)context;
		this.podcast = pc;
		this.downloadData = new DownloadData(context);
		this.density = getResources().getDisplayMetrics().density;
		//create the relative layout to hold the rest
		RelativeLayout rl = new RelativeLayout(context);
		rl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, (int)(50 * density)));
		
		button = new Button(context);
		button.setId(3);
		button.setTextSize(10);
		RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(40 * density));
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
		RelativeLayout.LayoutParams progressLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(density * 40));
		progressLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 3);
		//assume not visible unless we are actually downloading something
		downloadProgress.setVisibility(INVISIBLE);
		rl.addView(downloadProgress, progressLayout);
		
		//now add the view to the table row parent
		addView(rl);
		setEpisode(e);
		//now see if this episode is being downloaded so we can update the button visibility
		downloadData.open();
		Download dl = downloadData.getDownload(podcast.getName(), e.getName());
		if(dl != null)
		{
			downloadId = dl.getDownloadId();
			waitForDownload();
		}
	}
	
	public void setEpisode(Episode e)
	{
		this.setId((int)e.getEpisodeId());
		titleView.setText(e.getName());
		durationView.setText(getDurationString(e.getTotalTime()));
		//also hang onto the reference
		episode = e;
		if(episode.isNewEpisode())
		{
			titleView.setTextColor(Color.BLUE);
			durationView.setTextColor(Color.BLUE);
		}
		updateButtonText();
	}
	
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
	}
	
	public void onDetachedFromWindow()
	{
		try
		{
			timer.cancel();
		}
		catch(Exception e)
		{
			//
		}
		super.onDetachedFromWindow();
	}
	
	private void updateButtonText()
	{
		if(filePresent())
		{
			button.setText("Play");
		}
		else
		{
			button.setText("Download");
		}
		//newEpisodeIndicator.setVisibility(episode.isNewEpisode() ? View.VISIBLE : View.INVISIBLE);
	}
	
	//we get here by button pressing
	public void handleEpisode(View view)
	{
		//play it
		if(filePresent())
		{
			//play it by invoking the MediaPlayer
			Intent intent = new Intent(getContext(), PlayPodcastActivity.class);
			intent.putExtra("NAME", episode.getName());
			intent.putExtra("ID", episode.getPodcastId());
			if(podcast.getPodcastId() == 0L)
			{	
				intent.putExtra("FILE", episode.getFilepath());
				intent.putExtra("TOTAL", episode.getTotalTime());
				intent.putExtra("IMAGE", podcast.getImage());
			}
			getContext().startActivity(intent);
		}
		//download it
		else
		{
			downloadId = DownloadService.getInstance(getContext()).downloadEpisode(podcast, episode);
			//update the filepath reference in the episode
			String filename = episode.getUrl().substring(episode.getUrl().lastIndexOf("/") + 1);
			episode.setFilepath(fileManager.getAbsoluteFilePath(Podcast.getPodcastDirectory(podcast.getName()), filename));
			waitForDownload();
		}
	}
	
	public void waitForDownload()
	{
		updateButtonsOnUiThread(true);
		//start a timer to check the progress
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				Download dl = downloadData.getDownload(downloadId);
				if(dl == null)
				{
					updateButtonsOnUiThread(false);
					timer.cancel();
				}
			}
		}, 1000, 1000);
	}
	
	private boolean filePresent()
	{
		//if the episode path has been set, try to use that
		if(episode.getFilepath().length() > 5)
		{
			File file = new File(episode.getFilepath());
			return file.exists() && file.length() > 0;
		}
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
	
	private void updateButtonsOnUiThread(final boolean downloading)
	{
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				//get rid of the progress bar
        		downloadProgress.setVisibility(downloading ? VISIBLE : INVISIBLE);
        		//show the button so they can play their file
        		button.setVisibility(!downloading ? VISIBLE : INVISIBLE);
        		updateButtonText();
			}
		};
		parentActivity.runOnUiThread(runnable);
	}
}
