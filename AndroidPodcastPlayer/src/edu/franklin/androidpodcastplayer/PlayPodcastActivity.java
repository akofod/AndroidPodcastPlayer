package edu.franklin.androidpodcastplayer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.squareup.picasso.Picasso;

import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class PlayPodcastActivity extends ActionBarActivity 
{
	private static int rewindTime = 15000;
	private static int forwardTime = 15000;
	
	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;
	
	private Handler appHandler = new Handler();
	
	private long podcastId;
	private String episodeName;
	
	// UI Controls
	private TextView nowPlayingText;
	private TextView timeElapsedText;
	private TextView overallTimeText;
	
	private SeekBar volumeControl;
	private SeekBar timeElapsedControl;
	
	private ImageButton playPauseButton;
	//album art
	private ImageView albumView = null;
	
	// Timers
	private long currentTime = 0;
	private long overallTime = 0;
	private EpisodesData data = null;
	private Episode episode = null;
	private boolean episodeLoaded = false;
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if (mediaPlayer.isPlaying())
		{
			Log.i("Player", "Pausing playback");
			mediaPlayer.pause();
			playPauseButton.setImageResource(R.drawable.ic_media_play);
		}
		else
		{
			playPauseButton.setImageResource(R.drawable.ic_media_pause);
		}
		writeTimeElapsed();
		this.finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playpodcast);
		setControls();
		data = new EpisodesData(this);
		if(getIntent().getExtras() != null)
		{
			podcastId = getIntent().getExtras().getLong("ID");
			episodeName = getIntent().getExtras().getString("NAME");
			//load from subscribed episode if we can
			if(podcastId != 0L)
			{
				Log.i("Player", "Loading media player with podcastID " + podcastId + " and episode name " + episodeName);
				getMediaInfo(podcastId, episodeName);
			}
			//or load a saved off episode that has been downloaded by the user.
			//this could happen if the user wants to take the podcast for a testdrive
			else
			{
				String episodePath = getIntent().getExtras().getString("FILE");
				String imagePath = getIntent().getExtras().getString("IMAGE");
				Long time = getIntent().getExtras().getLong("TOTAL");
				Episode ep = this.createFakeEpisode(episodeName, episodePath, time);
				getMediaInfo(ep, imagePath);
			}
			setTimeControl();
			setVolumeControl();
			appHandler.postDelayed(UpdateProgress, 250);
		}
		
		setTimerControl("CURRENT", currentTime);
		setTimerControl("OVERALL", overallTime);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.podcast_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_home) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void MediaControl(View view)
	{
		if(!episodeLoaded) return;
		
		if (mediaPlayer.isPlaying())
		{
			Log.i("Player", "Pausing playback");
			mediaPlayer.pause();
			playPauseButton.setImageResource(R.drawable.ic_media_play);
			writeTimeElapsed();
		}
		else
		{
			Log.i("Player", "Starting playback");
			mediaPlayer.start();
			playPauseButton.setImageResource(R.drawable.ic_media_pause);
		}
	}
	

	/**
	 * Method to rewind the media by 15 seconds;
	 * @param view
	 */
	public void rewind(View view)
	{
		if(!episodeLoaded) return;
		
		if (mediaPlayer.getCurrentPosition() - rewindTime < 0)
		{
			mediaPlayer.seekTo(0);
		}
		else
		{
			mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - rewindTime);
		}
	}
	
	
	/**
	 * Method to fast forward 15 seconds
	 * @param view
	 */
	public void forward(View view)
	{
		if(!episodeLoaded) return;
		
		mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + forwardTime);
	}
	
	/**
	 * Method to update the UI Timers
	 * @param timer Specifies which timer to update
	 * @param time Specifies the time to update the selected timer
	 */
	private void setTimerControl(String timer, long time)
	{
		if(timer.equalsIgnoreCase("CURRENT"))
		{
			timeElapsedText.setText(Episode.longToString(currentTime));
		}
		else if(timer.equalsIgnoreCase("OVERALL"))
		{
			overallTimeText.setText(Episode.longToString(overallTime));
		}
	}

	/**
	 * Method to set up the Volume Control and handle volume changes
	 */
	private void setVolumeControl()
	{
		volumeControl.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volumeControl.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) 
			{
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);	
			}
		});
	}
	
	private void setControls()
	{
		nowPlayingText = (TextView)findViewById(R.id.textViewNowPlaying);
		timeElapsedText = (TextView)findViewById(R.id.textViewTimeElapsed);
		overallTimeText = (TextView)findViewById(R.id.textViewIOverall);	
		playPauseButton = (ImageButton)findViewById(R.id.imageButtonPlayPause);
		volumeControl = (SeekBar)findViewById(R.id.seekVolume);
		timeElapsedControl = (SeekBar)findViewById(R.id.seekBarTimer);
		albumView = (ImageView)findViewById(R.id.imageViewAlbumArt);
	}
	
	private void setTitle(String title)
	{
		nowPlayingText.setText(title);
	}

	private void setTimeControl()
	{
		timeElapsedControl.setMax((int) overallTime);
		timeElapsedControl.setProgress((int)currentTime);
		timeElapsedControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) 
			{
				mediaPlayer.seekTo(seekBar.getProgress() * 1000);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) 
			{
				
			}
		});
	}
	
	private Runnable UpdateProgress = new Runnable() {
		public void run()
		{
			currentTime = mediaPlayer.getCurrentPosition() / 1000;
			if (currentTime == overallTime)
			{
				mediaPlayer.stop();
				currentTime = 0;
				writeTimeElapsed();
				episode.setCompleted(true);
				playPauseButton.setImageResource(R.drawable.ic_media_play);
			}
			timeElapsedText.setText(Episode.longToString(currentTime));
			timeElapsedControl.setProgress((int)currentTime);
			appHandler.postDelayed(this, 500);
		}
	};
	
	public Episode createFakeEpisode(String name, String filePath, long totalTime)
	{
		Episode episode = new Episode();
		episode.setPodcastId(0);
		episode.setName(name);
		episode.setFilepath(filePath);
		episode.setTotalTime(totalTime);
		return episode;
	}
	
	public void getMediaInfo(Episode episode, String imagePath)
	{
		episodeLoaded = episode != null;
		//load the image using an image loader
		if(imagePath.length() > 0)
		{
			Picasso.with(this).load(new File(imagePath)).into(albumView);
		}
		Log.i("PodcastPlayer", "Hopefully have an episode " + episode.getName() + ":" + episode.getFilepath() + ":" + episode.getTotalTime());
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mediaPlayer = new MediaPlayer();
		try 
		{
			Log.i("MediaPlayer", "Current value of currentTime pre load: " + currentTime);
			mediaPlayer.setDataSource(episode.getFilepath());
			mediaPlayer.prepare();
			
			// set up timers
			overallTime = episode.getTotalTime();
			currentTime = episode.getPlayedTime();
			Log.i("MediaPlayer", "Current value of currentTime post load: " + currentTime);
			mediaPlayer.seekTo((int)(currentTime * 1000));
			setTitle(episode.getName());
		} 
		catch (Exception e) 
		{
			Log.e("Player", "Problem playing episode ", e);
		} 
	}
	
	//fetch the episode from the database
	public void getMediaInfo(long id, String name)
	{
		episode = data.retrieveEpisodeByName(id, name);
		Log.i("Player", "Fetching episode using " + id + ":" + name + " and got back " + episode);
		PodcastData podcastData = new PodcastData(getApplicationContext());
		Podcast podcast = podcastData.getPodcastById(id);
		//set the image for the podcast if we have it
		String imagePath = podcast.getImage();
		getMediaInfo(episode, imagePath);
	}
	
	private void writeTimeElapsed()
	{
		data = new EpisodesData(this);
		data.open();
		Log.i("Player", "Writing " + currentTime + " to the database for episode " + episode.getEpisodeId());
		data.updatePlayedTime(podcastId, episode.getEpisodeId(), currentTime);
		data.close();
	}
}