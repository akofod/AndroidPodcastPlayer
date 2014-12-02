package edu.franklin.androidpodcastplayer;

import java.io.File;

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
import edu.franklin.androidpodcastplayer.tasks.DownloadFileTask;
import edu.franklin.androidpodcastplayer.tasks.DownloadHandler;

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
			try
			{
				rss = new Rss();
				rss.initializeFromUrl(url);
				podcast = rssToPodcast(rss, false);
			}
			catch(Exception e)
			{
				Log.e("PodcastDetails", "Could not fetch the RSS from " + url, e);
				Toast.makeText(this, "Could not parse the RSS at location " + url + ", check the logs", Toast.LENGTH_LONG).show();
				return;
			}
		}
		else
		{
			String podcastName = getIntent().getExtras().getString("podcastName");
			podcast = podcastData.retrievePodcastByName(podcastName);
			subscribed = true;
		}

		setPodcast(podcast);
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
			podcast = rssToPodcast(rss, true);
			Toast.makeText(getApplicationContext(), "You are now subscribed to this podcast", Toast.LENGTH_SHORT).show();
			subscribed = true;
		}
		else
		{
			//create a dialog box and allow the user a second chance to back out of
			//getting rid of this podcast.
			//wipe the database
			podcastData.purgePodcast(podcast.getPodcastId());
			String podcastDir = Podcast.getPodcastDirectory(podcast.getName());
			//wipe the directory
			fileManager.deleteDir(podcastDir);
			Toast.makeText(getApplicationContext(), "You are no longer subscribed to this podcast", Toast.LENGTH_SHORT).show();
			subscribed = false;
			//TODO - if we unsubscribe from a podcast that was already in the db...
			// then we try to subscribe again. we are going to barf as the RSS is gone...
			// we may need to test for this and simply reinitialize the rss from the Podcast url
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
				saveSubscriptionData(subscriptionSettings.getSubscription());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}
		});
		//now show the settings dialog to the user
		builder.create().show();
	}
	
	private void saveSubscriptionData(final Subscription sub)
	{
		subData.updateSubscription(sub);
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
	
	private Podcast rssToPodcast(Rss rss, boolean subscribe)
	{
		//first things first, grab an image for this guy
		Channel channel = rss.getChannel();
		Image image = channel.getImage();
		String podcastTitle = channel.getTitle();
		String podcastHomeDir = Podcast.getPodcastDirectory(podcastTitle);
		Podcast pc = new Podcast();
		//if the imagePath is set, then use it
		if(imagePath != null && new File(imagePath).exists())
		{
			pc.setImage(imagePath);
		}
		//fetch the actual image from the webs
		//try to download the image attached to the actual rss first.
		else if(image != null && image.getUrl() != null && image.getUrl().contains("/"))
		{
			String imageName = image.getUrl().substring(image.getUrl().lastIndexOf("/") + 1);
			downloadFile(Podcast.IMAGES, imageName, image.getUrl());
		}
		else if(logo_url != null && !logo_url.equals("null"))
		{
			String imageName = logo_url.substring(logo_url.lastIndexOf("/") + 1);
			downloadFile(Podcast.IMAGES, imageName, logo_url);
		}
		//make a dir for the podcast and any temp episodes
		fileManager.mkDir(podcastHomeDir);

		pc.setName(podcastTitle);
		pc.setDescription(channel.getDescription());
		pc.setNumEpisodes(0L);
		pc.setFeedUrl(channel.getLink());
		pc.setDir(fileManager.getAbsoluteFilePath(podcastHomeDir, null));
		//that should be enough to persist this guy
		if(subscribe) pc = podcastData.createPodcast(pc);
		else pc.setPodcastId(0);
		
		if(pc != null)
		{
			//the podcast is in the db...add in the episode info
			for(Item item : channel.getItemList())
			{
				Episode e = new Episode();
				e.setPodcastId(subscribe ? pc.getPodcastId() : pc.getEpisodes().size());
				e.setCompleted(false);
				
				//item objects don't have images
				e.setImage("");
				e.setName(item.getTitle());
				String link = item.getLink();
				//if there is an enclosure, use that for the url
				if(item.getEnclosure() != null)
				{
					Enclosure enc = item.getEnclosure();
					link = enc.getUrl().length() > 0 ? enc.getUrl() : link;
				}
				e.setUrl(link);
				//we haven't downloaded it yet...or have we
				String dir = Podcast.getPodcastDirectory(pc.getName());
				String file = link.substring(link.lastIndexOf("/") + 1);
				String filePath = fileManager.getAbsoluteFilePath(dir, file);
				File episodeFile = new File(filePath);
				e.setFilepath(episodeFile.exists() && episodeFile.length() > 0 ? filePath : "");
				e.setNewEpisode(false);
				e.setPlayedTime(0);
				//Use the duration if it was provided by the file.
				e.setTotalTime(item.getDuration());
				if(subscribe) e = episodeData.createEpisode(e);
				
				if(e != null)
				{
					pc.addEpisode(e);
				}
			}
		}
		return pc;
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
	
	public void downloadFile(final String dir, final String file, final String url)
	{
		Log.i("PodcastDetails", "Downloading " + dir + ":" + file + " from " + url);
		DownloadFileTask dft = new DownloadFileTask(this);
		dft.setHandler(this);
		dft.execute(url, dir, file);
	}
}
