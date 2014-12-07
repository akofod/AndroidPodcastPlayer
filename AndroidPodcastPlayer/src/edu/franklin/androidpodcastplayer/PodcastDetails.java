package edu.franklin.androidpodcastplayer;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.data.SubscriptionData;
import edu.franklin.androidpodcastplayer.models.Channel;
import edu.franklin.androidpodcastplayer.models.Enclosure;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Image;
import edu.franklin.androidpodcastplayer.models.Item;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Rss;
import edu.franklin.androidpodcastplayer.models.Subscription;
import edu.franklin.androidpodcastplayer.services.FileManager;
import edu.franklin.androidpodcastplayer.services.SubscriptionService;
import edu.franklin.androidpodcastplayer.tasks.DownloadFileTask;
import edu.franklin.androidpodcastplayer.tasks.DownloadHandler;
import edu.franklin.androidpodcastplayer.utilities.PodcastFactory;

public class PodcastDetails extends ActionBarActivity implements DownloadHandler
{
	public static final String PODCAST_NAME = "podcastName";
	private TableLayout episodeTable = null;
	private ImageView view = null;
	private TextView titleView = null;
	private TextView episodeCountView = null;
	private Button subscribeButton = null;
	private Button settingsButton = null;
	private PodcastData podcastData = new PodcastData(this);
	private EpisodesData episodeData = new EpisodesData(this);
	private SubscriptionData subData = new SubscriptionData(this, podcastData);
	private FileManager fileManager = null;
	private Podcast podcast = null;
	private String url = null;
	private String logo_url = null;
	private boolean subscribed = false;
	private Rss rss = null;
	private String imagePath = null;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_podcast_details);
		fileManager = new FileManager(this);
		episodeTable = (TableLayout) findViewById(R.id.episodeTable);
		view = (ImageView)findViewById(R.id.podcastImage);
		titleView = (TextView)findViewById(R.id.podcastTitle);
		episodeCountView = (TextView)findViewById(R.id.podcastEpisodes);
		subscribeButton = (Button)findViewById(R.id.subscribeButton);
		settingsButton = (Button)findViewById(R.id.podcastSettingsButton);
		settingsButton.setTextSize(10);
		episodeData.open();
		podcastData.open();
		subData.open();
		//grab a podcast and load it. The name of the podcast is passed through the intent"
		url = getIntent().getExtras().getString("url");
		logo_url = getIntent().getExtras().getString("logo_url");
		if(url != null)
		{
			setRss(url);
			podcast = PodcastFactory.getInstance(this).createPodcast(rss, this, logo_url);
		}
		else
		{
			String podcastName = getIntent().getExtras().getString("podcastName");
			podcast = podcastData.retrievePodcastByName(podcastName);
			subscribed = true;
			setRss(podcast.getFeedUrl());
		}

		setPodcast(podcast);
	}
	
	private void setRss(String url)
	{
		try
		{
			rss = new Rss();
			rss.initializeFromUrl(url);
		}
		catch(Exception e)
		{
			Log.e("PodcastDetails", "Could not fetch the RSS from " + url, e);
		}
	}
	
	protected void onStop()
	{
		//podcastData.close();
		//episodeData.close();
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
		if (id == R.id.action_home) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void subscribeToPodcast(View view)
	{
		//if we are not yet subscribed...do it now
		if(!subscribed)
		{
			//try to rebuild the podcast and insert the podcast into the db
			podcast = PodcastFactory.getInstance(this).subscribeToPodcast(podcast);
			subscribed = true;
			handleSettings(view);
		}
		else
		{
			//create a dialog box and allow the user a second chance to back out of
			//getting rid of this podcast.
			//wipe the database
			podcastData.purgePodcast(podcast.getPodcastId());
			subData.purgeSubscription(podcast.getPodcastId());
			String podcastDir = Podcast.getPodcastDirectory(podcast.getName());
			//wipe the directory
			fileManager.deleteDir(podcastDir);
			Toast.makeText(getApplicationContext(), "You are no longer subscribed to this podcast", Toast.LENGTH_SHORT).show();
			subscribed = false;
			podcast = PodcastFactory.getInstance(this).createPodcast(rss, this, logo_url);
		}
		//reset the podcast
		setPodcast(podcast);
	}
	
	public void showDetails(View view)
	{
		AlertDialog.Builder detailsDialog = new AlertDialog.Builder(this);
		detailsDialog.setPositiveButton("Back", new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}
			
		});
		detailsDialog.setMessage(podcast.getDescription());
		detailsDialog.create().show();
	}
	
	public void handleSettings(View view)
	{
		//fetch the subscription for this podcast
		Subscription sub = subData.getSubscriptionById(podcast.getPodcastId());
		//set it into the view
		final SettingsDialog subscriptionSettings = new SettingsDialog(this);
		subscriptionSettings.setSubscription(sub);
		//build a popup window
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(podcast.getName() + " Settings");
		builder.setView(subscriptionSettings);
		//handle cancel and save actions
		builder.setPositiveButton("Save", new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				saveSubscriptionData(subscriptionSettings.getSubscription(), true);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				saveSubscriptionData(subscriptionSettings.getSubscription(), false);
				dialog.dismiss();
			}
		});
		//now show the settings dialog to the user
		builder.create().show();
	}
	
	private void saveSubscriptionData(final Subscription sub, boolean persistChanges)
	{
		//if the user pressed save, do it
		if(persistChanges)
		{
			subData.updateSubscription(sub);
		}
		//either way see if we need to kick off a download
		//grab the episodes we need to download
		List<Episode> neededEpisodes = SubscriptionService.getInstance(this).getNeededEpisodes(sub);
		for(Episode e : neededEpisodes)
		{
			View v = episodeTable.findViewById(EpisodeRow.getIdForEpisode(e));
			Log.i("PD", "The view at " + e.getEpisodeId() + " is " + v);
			//kick off the download
			if(v instanceof EpisodeRow)
			{
				((EpisodeRow)v).downloadEpisode();
			}
		}
	}
	
	private void updateButtons()
	{
		if(subscribed)
		{
			subscribeButton.setText("Unsubscribe");
			settingsButton.setVisibility(View.VISIBLE);
		}
		else
		{
			subscribeButton.setText("Subscribe");
			settingsButton.setVisibility(View.INVISIBLE);
		}
	}
	
	private void setPodcast(Podcast podcast)
	{	
		if(podcast != null)
		{
			//set the image for the podcast if we have it
			String imagePath = podcast.getImage();
			Log.i("PodcastDetails", "Loading image at " + imagePath);
			//load the image using an image loader
			if(imagePath != null && imagePath.length() > 0)
			{
				Picasso.with(this).load(new File(imagePath)).
				resize(128, 128).centerCrop().into(view);
			}
			String podcastName = podcast.getName();
			int length = podcastName.length();
			//adjust the font if the name is a bit big
			if(length > 20)
			{
				titleView.setTextSize(18);
			}
			//if it is way too big, truncate it
			if(length > 30)
			{
				podcastName = podcastName.substring(0, 30) + "...";
			}
			//set the text views for this podcast
			titleView.setText(podcast.getName());
			episodeCountView.setText(podcast.getEpisodes().size() + " Episodes");
			//update the button text
			updateButtons();
			//purge the old view stuff maybe?
			episodeTable.removeAllViews();
			//now make rows for each of the episodes
			for(Episode e : podcast.getEpisodes())
			{
				EpisodeRow row = new EpisodeRow(this, e, podcast, episodeData);
				episodeTable.addView(row);
			}
		}
	}
	
	public void downloadFinished(String dir, String file)
	{
		imagePath = fileManager.getAbsoluteFilePath(dir, file);
		updatePodcastImage(imagePath);
	}
	
	public void updatePodcastImage(String path)
	{
		if(path.length() > 0)
		{
			podcast.setImage(path);
			//now update the view to show it.
			Picasso.with(this).load(new File(path)).
			resize(128, 128).centerCrop().into(view);
			if(subscribed)
			{
				Log.i("PodcastDetails", "Updating the stored image path to " + path);
				podcastData.updateImagePath(podcast.getPodcastId(), path);
			}
		}
	}
	
	public void downloadFailed(String url, String dir, String file) 
	{
		Log.e("PodcastDetails", "Download failed...from " + url + " for " + dir + "/" + file + " trying backup url");
		//we failed once...try to do a download task using the logo url
		if(logo_url != null && !logo_url.equals("null"))
		{
			DownloadFileTask dft = new DownloadFileTask(this);
			dft.setHandler(this);
			dft.execute(logo_url, dir, file);
			//now set the log_url to null...so we don't spiral
			logo_url = null;
		}
	}
}
