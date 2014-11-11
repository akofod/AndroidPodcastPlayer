package edu.franklin.androidpodcastplayer;

import java.io.File;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;

public class PodcastDetails extends ActionBarActivity 
{
	private TableLayout episodeTable = null;
	private ImageView view = null;
	private PodcastData podcastData = new PodcastData(this);
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_podcast_details);
		episodeTable = (TableLayout) findViewById(R.id.episodeTable);
		view = (ImageView)findViewById(R.id.podcastImage);
		podcastData.open();
		//grab a podcast and see if we can load it?
		Podcast podcast = podcastData.retrievePodcastByName("Coder Radio MP3");
		setPodcast(podcast);
	}
	
	protected void onDestroy()
	{
		podcastData.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.podcast_details, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
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
	
	public void subscribeToPodcast(View view)
	{
		Toast.makeText(getApplicationContext(), "Subscribe to this podcast!", Toast.LENGTH_SHORT).show();
	}
	
	private void setPodcast(Podcast podcast)
	{	
		if(podcast != null)
		{
			//set the image for the podcast if we have it
			String imagePath = podcast.getImage();
			//load the image using an image loader
			if(imagePath.length() > 0)
			{
				Toast.makeText(this, "Trying to load " + imagePath, Toast.LENGTH_LONG).show();
				Picasso.with(this).load(new File(imagePath)).
				resize(128, 128).centerCrop().into(view);
			}
			//now make rows for each of the episodes
			for(Episode e : podcast.getEpisodes())
			{
				TableRow row = new TableRow(this);
				row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				row.setId((int)e.getEpisodeId());
				
				TextView episodeTitle = new TextView(this);
				String title = e.getName().length() > 100 ? e.getName().substring(0, 100) : e.getName();
				episodeTitle.setId(1);
				episodeTitle.setText(title);
				episodeTitle.setPadding(5, 5, 5, 0);
				episodeTitle.setTextSize(12);
				row.addView(episodeTitle);
				
				TextView episodeDuration = new TextView(this);
				episodeDuration.setId(2);
				episodeDuration.setText(e.getTotalTime() + "");
				episodeDuration.setPadding(5, 5, 5, 0);
				episodeDuration.setTextSize(12);
				row.addView(episodeDuration);
				
				episodeTable.addView(row);
			}
		}
	}
}
