package edu.franklin.androidpodcastplayer.services;

import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.data.SubscriptionData;
import android.content.Context;

/**
 * The Subscription Service will launch when the application launches
 * and iterate over the subscribed podcasts to see if it needs to download
 * its files.
 * 
 * Ideally this would only happen at some set time, once per day (maybe 12:00) to
 * see if new episodes are ready. Maybe we can achieve that with a simple timer.
 * 
 * @author rennardhutchinson
 *
 */
public class SubscriptionService
{
	private PodcastData podData = null;
	private SubscriptionData subData = null;
	private Context context = null;
	boolean initialized = false;
	private static SubscriptionService INST = null;
	
	private SubscriptionService()
	{
		//
	}
	
	public static SubscriptionService getInstance()
	{
		if(INST == null)
		{
			INST = new SubscriptionService();
		}
		return INST;
	}
	
	public void initialize(Context context)
	{
		if(!initialized)
		{
			podData = new PodcastData(context);
			podData.open();
			subData = new SubscriptionData(context, podData);
			subData.open();
		}
	}
	
	public void close()
	{
		if(initialized)
		{
			podData.close();
			subData.close();
			initialized = false;
		}
	}
}
