package edu.franklin.androidpodcastplayer;

import java.io.File;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;

public class PodcastDetails extends ActionBarActivity 
{
	public static final String PODCAST_NAME = "podcastName";
	private TableLayout episodeTable = null;
	private ImageView view = null;
	private TextView titleView = null;
	private TextView authorView = null;
	private TextView episodeCountView = null;
	private PodcastData podcastData = new PodcastData(this);
	private EpisodesData episodeData = new EpisodesData(this);
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_podcast_details);
		episodeTable = (TableLayout) findViewById(R.id.episodeTable);
		view = (ImageView)findViewById(R.id.podcastImage);
		titleView = (TextView)findViewById(R.id.podcastTitle);
		authorView = (TextView)findViewById(R.id.podcastAuthor);
		episodeCountView = (TextView)findViewById(R.id.podcastEpisodes);
		episodeData.open();
		podcastData.open();
		//grab a podcast and load it. The name of the podcast is passed through the intent"
		String podcastName = getIntent().getExtras().getString("podcastName");		
		Podcast podcast = podcastData.retrievePodcastByName(podcastName);
		setPodcast(podcast);
	}
	
	protected void onStop()
	{
		podcastData.close();
		episodeData.close();
		super.onStop();
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
		Toast.makeText(getApplicationContext(), "Implement Subscribe to this podcast!", Toast.LENGTH_SHORT).show();
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
				Picasso.with(this).load(new File(imagePath)).
				resize(128, 128).centerCrop().into(view);
			}
			//set the text views for this podcast
			titleView.setText(podcast.getName());
			//no author for now...maybe add one to the parsing and db
			authorView.setText("");
			episodeCountView.setText(podcast.getEpisodes().size() + " Episodes");
			//now make rows for each of the episodes
			for(Episode e : podcast.getEpisodes())
			{
				EpisodeRow row = new EpisodeRow(this, e, podcast, episodeData);
				episodeTable.addView(row);
			}
		}
	}
}
