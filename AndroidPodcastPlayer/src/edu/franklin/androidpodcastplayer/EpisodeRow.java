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
	private EpisodesData data = null;
	private Episode episode = null;
	private FileManager fileManager = null;
	private long downloadId = 0L;
	private Timer timer = new Timer();
	private float density = 0;
	private Context context = null;
	
	@SuppressLint("NewApi") public EpisodeRow(Context context, Episode e, Podcast pc, EpisodesData data) 
	{
		super(context);
		this.context = context;
		int baseId = getIdForEpisode(e);
		this.data = new EpisodesData(context);
		fileManager = new FileManager(context);
		parentActivity = (Activity)context;
		this.podcast = pc;
		this.density = getResources().getDisplayMetrics().density;
		//create the relative layout to hold the rest
		RelativeLayout rl = new RelativeLayout(context);
		rl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, (int)(50 * density)));
		
		button = new Button(context);
		button.setId(baseId + 3);
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
		titleView.setId(baseId + 1);
		titleView.setTextSize(12);
		titleView.setEllipsize(TruncateAt.END);
		titleView.setLines(2);
		RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		titleLayout.addRule(RelativeLayout.LEFT_OF, baseId + 3);
		rl.addView(titleView, titleLayout);
		
		durationView = new TextView(context);
		durationView.setId(baseId + 2);
		durationView.setTextSize(10);
		durationView.setPadding(0, 12, 0, 5);
		RelativeLayout.LayoutParams durationLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		durationLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		durationLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		rl.addView(durationView, durationLayout);
		
		downloadProgress = new ProgressBar(context);
		downloadProgress.setId(baseId + 4);
		RelativeLayout.LayoutParams progressLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(density * 40));
		progressLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 3);
		//assume not visible unless we are actually downloading something
		downloadProgress.setVisibility(INVISIBLE);
		rl.addView(downloadProgress, progressLayout);
		
		//now add the view to the table row parent
		addView(rl);
		setEpisode(e);
	}
	
	public static int getIdForEpisode(Episode e)
	{
		return (int)(e.getEpisodeId() * 1000);
	}
	
	public void checkForDownload()
	{
		int status = DownloadService.getInstance(context).getDownloadStatus(podcast, episode);
		if(status == DownloadService.DOWNLOADING)
		{
			String filename = episode.getUrl().substring(episode.getUrl().lastIndexOf("/") + 1);
			episode.setFilepath(fileManager.getAbsoluteFilePath(Podcast.getPodcastDirectory(podcast.getName()), filename));
			waitForDownload();
		}
		else
		{
			setEpisode(episode);
		}
	}
	
	public void cancelTimer()
	{
		try{ timer.cancel(); } catch(Exception e) { Log.e("ER", "Could not cancel timer");}
	}
	
	public void setEpisode(Episode e)
	{
		this.setId(getIdForEpisode(e));
		titleView.setText(e.getName());
		durationView.setText(getDurationString(e.getTotalTime()));
		//also hang onto the reference
		episode = e;
		if(episode.isNewEpisode())
		{
			titleView.setTextColor(Color.BLUE);
			durationView.setTextColor(Color.BLUE);
		}
		updateButtonText(DownloadService.getInstance(context).getDownloadStatus(podcast, episode));
	}
	
	private void updateButtonText(int status)
	{
		button.setEnabled(true);
		if(status != DownloadService.DOWNLOADING)
		{
			if(filePresent())
			{
				button.setText("Play");
			}
			else
			{
				button.setText("Download");
			}
		}
		else if(status == DownloadService.PAUSED)
		{
			button.setText("Paused");
			button.setEnabled(false);
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
			downloadEpisode();
		}
	}
	
	public void downloadEpisode()
	{
		if(downloadId == 0L)
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
		updateButtonsOnUiThread(DownloadService.DOWNLOADING);
		//start a timer to check the progress
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				int status = DownloadService.getInstance(context).getDownloadStatus(podcast, episode);
				//Log.e("ER", "Status is " + status);
				if(status == DownloadService.NOT_DOWNLOADING || status == DownloadService.UNKNOWN)
				{
					if(podcast.getPodcastId() != 0L)
					{
						//reload the episode?
						data.open();
						episode = data.retrieveEpisodeByName(podcast.getPodcastId(), episode.getName());
						data.close();
					}
					timer.cancel();
					updateButtonsOnUiThread(status);
				}
			}
		}, 5000, 5000);
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
	
	private void updateButtonsOnUiThread(final int status)
	{
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				if(status == DownloadService.DOWNLOADING)
				{
					downloadProgress.setVisibility(VISIBLE);
					button.setVisibility(INVISIBLE);
				}
				else
				{
					downloadProgress.setVisibility(INVISIBLE);
					button.setVisibility(VISIBLE);
					if(podcast.getPodcastId() != 0L && new File(episode.getFilepath()).exists() && episode.getPlayedTime() == 0L)
	        		{
	        			titleView.setTextColor(Color.BLUE);
	        			durationView.setTextColor(Color.BLUE);
	        		}
				}
        		updateButtonText(status);
			}
		};
		parentActivity.runOnUiThread(runnable);
	}
}
