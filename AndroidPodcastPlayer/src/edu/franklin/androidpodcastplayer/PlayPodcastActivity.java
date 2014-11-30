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
	private boolean episodeLoaded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playpodcast);
		setControls();
		if(getIntent().getExtras() != null)
		{
			podcastId = getIntent().getExtras().getLong("ID");
			episodeName = getIntent().getExtras().getString("NAME");
			Log.i("Player", "Loading media player with podcastID " + podcastId + " and episode name " + episodeName);
			getMediaInfo(podcastId, episodeName);	
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
			mediaPlayer.pause();
			playPauseButton.setImageResource(R.drawable.ic_media_play);
		}
		else
		{
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
			timeElapsedText.setText(String.format("%02d:%02d:%02d", 
					TimeUnit.SECONDS.toHours(currentTime), 
					TimeUnit.SECONDS.toMinutes(currentTime),
					TimeUnit.SECONDS.toSeconds(currentTime) -
					TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(currentTime))));
		}
		else if(timer.equalsIgnoreCase("OVERALL"))
		{
			overallTimeText.setText(String.format("%02d:%02d:%02d", 
					TimeUnit.SECONDS.toHours(overallTime), 
					TimeUnit.SECONDS.toMinutes(overallTime),
					TimeUnit.SECONDS.toSeconds(overallTime) -
					TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(overallTime))));
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
				if(episodeLoaded) mediaPlayer.seekTo(timeElapsedControl.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {}
		});
	}
	
	private Runnable UpdateProgress = new Runnable() {
		public void run()
		{
			currentTime = mediaPlayer.getCurrentPosition() / 1000;
			if (currentTime == overallTime)
			{
				mediaPlayer.stop();
			}
			setTimerControl("CURRENT",currentTime);
			timeElapsedControl.setProgress((int)currentTime);
			appHandler.postDelayed(this, 500);
		}
	};
	
	public void getMediaInfo(long id, String name)
	{
		EpisodesData episodeData = new EpisodesData(getApplicationContext());
		Episode episode = episodeData.retrieveEpisodeByName(id, name);
		Log.i("Player", "Fetching episode using " + id + ":" + name + " and got back " + episode);
		PodcastData podcastData = new PodcastData(getApplicationContext());
		Podcast podcast = podcastData.getPodcastById(id);
		episodeLoaded = episode != null;
		//set the image for the podcast if we have it
		String imagePath = podcast.getImage();
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
			mediaPlayer.setDataSource(episode.getFilepath());
			mediaPlayer.prepare();
			
			// set up timers
			overallTime = episode.getTotalTime();
			currentTime = episode.getPlayedTime();
			
			setTitle(episode.getName());
		} 
		catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} 
		catch (SecurityException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalStateException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}