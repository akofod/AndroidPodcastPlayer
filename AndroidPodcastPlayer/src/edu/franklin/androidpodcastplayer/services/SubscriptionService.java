package edu.franklin.androidpodcastplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The Subscription Service will launch when the application launches
 * and iterate over the subscribed podcasts to see if it needs to download
 * its files.
 * 
 * Ideally this would only happen at some set time, once per day (maybe 12:00) to
 * see if new episodes are ready.
 * 
 * @author rennardhutchinson
 *
 */
public class SubscriptionService extends Service
{
	//called before the service is even started
	public void onCreate()
	{
		super.onCreate();
	}
	
	//called when the service is about to be shut down
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	//called when an activity invokes the service
	public int onStartCommand(Intent intent) 
	{
		return Service.START_STICKY;
	}

	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}
