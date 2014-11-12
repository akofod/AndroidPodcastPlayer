package edu.franklin.androidpodcastplayer;

import android.app.DownloadManager;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import edu.franklin.androidpodcastplayer.models.Episode;

public class EpisodeRow extends TableRow 
{
	private TextView titleView = null;
	private TextView durationView = null;
	private Button button = null;
	private Episode episode = null;
	//in case the user wants to fetch the episode
	private DownloadManager dm = null;
	//in case the user fetches the episode
	
	public EpisodeRow(Context context, Episode e) 
	{
		super(context);
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
	
	public void setEpisode(Episode e)
	{
		this.setId((int)e.getEpisodeId());
		titleView.setText(e.getName());
		durationView.setText(getDurationString(e.getTotalTime()));
		//also hang onto the reference
		episode = e;
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
		Toast.makeText(getContext(), episode.getName() + " Clicked", Toast.LENGTH_SHORT).show();
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
}
