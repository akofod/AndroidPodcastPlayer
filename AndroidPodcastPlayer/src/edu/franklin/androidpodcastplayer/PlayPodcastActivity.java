package edu.franklin.androidpodcastplayer;

import java.util.concurrent.TimeUnit;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class PlayPodcastActivity extends ActionBarActivity 
{
	private static int rewindTime = 15000;
	private static int forwardTime = 15000;
	
	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;
	
	private Handler appHandler = new Handler();
	
	// UI Controls
	private TextView nowPlayingText;
	private TextView timeElapsedText;
	private TextView overallTimeText;
	
	private SeekBar volumeControl;
	private SeekBar timeElapsedControl;
	
	private ImageButton play_pauseButton;

	
	// Timers
	private long currentTime;
	private long overallTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playpodcast);
		
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
		
		// set up timers
		overallTime = mediaPlayer.getDuration();
		currentTime = 0;
		
		setControls();
		setTimerControl("CURRENT", currentTime);
		setTimerControl("OVERALL", overallTime);
		setTimeControl();
		setTitle("Test Title");
		setVolumeControl();
		
		appHandler.postDelayed(UpdateProgress, 250);
	}
	
	
	public void play_pause(View view)
	{
		if (mediaPlayer.isPlaying())
		{
			mediaPlayer.pause();
			play_pauseButton.setBackgroundResource(R.drawable.ic_media_play);
		}
		else
		{
			mediaPlayer.start();
			play_pauseButton.setBackgroundResource(R.drawable.ic_media_pause);
		}
	}
	
	/**
	 * Method to rewind the media by 15 seconds;
	 * @param view
	 */
	public void rewind(View view)
	{
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
			timeElapsedText.setText(String.format("%02d:%02d", 
					TimeUnit.MILLISECONDS.toMinutes(currentTime),
					TimeUnit.MILLISECONDS.toSeconds(currentTime) -
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentTime))));
		}
		else if(timer.equalsIgnoreCase("OVERALL"))
		{
			overallTimeText.setText(String.format("%02d:%02d", 
					TimeUnit.MILLISECONDS.toMinutes(overallTime),
					TimeUnit.MILLISECONDS.toSeconds(overallTime) -
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(overallTime))));
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
		play_pauseButton = (ImageButton)findViewById(R.id.imageButtonPlayPause);
		volumeControl = (SeekBar)findViewById(R.id.seekVolume);
		timeElapsedControl = (SeekBar)findViewById(R.id.seekBarTimer);
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
				mediaPlayer.seekTo(timeElapsedControl.getProgress());
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
			currentTime = mediaPlayer.getCurrentPosition();
			if (currentTime == overallTime)
			{
				mediaPlayer.stop();
			}
			setTimerControl("CURRENT",currentTime);
			timeElapsedControl.setProgress((int)currentTime);
			appHandler.postDelayed(this, 500);
		}
	};
}