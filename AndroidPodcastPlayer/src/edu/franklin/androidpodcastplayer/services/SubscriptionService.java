package edu.franklin.androidpodcastplayer.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.data.SubscriptionData;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Rss;
import edu.franklin.androidpodcastplayer.models.Subscription;
import edu.franklin.androidpodcastplayer.utilities.PodcastFactory;
import android.content.Context;
import android.util.Log;

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
	private EpisodesData epData = null;
	private Context context = null;
	boolean initialized = false;
	private static SubscriptionService INST = null;
	
	private SubscriptionService()
	{
		//
	}
	
	public static SubscriptionService getInstance(Context context)
	{
		if(INST == null)
		{
			INST = new SubscriptionService();
		}
		INST.initialize(context);
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
			epData = new EpisodesData(context);
			epData.open();
			this.context = context;
			initialized = true;
		}
	}
	
	public void close()
	{
		if(initialized)
		{
			podData.close();
			subData.close();
			epData.close();
			initialized = false;
		}
	}
	
	//invoked when the app starts
	public void updateSubscriptions()
	{
		List<Subscription> subs = subData.getAllSubscriptions();
		for(Subscription s : subs)
		{
			long now = System.currentTimeMillis();
			long last = s.getLastUpdate();
			long frequency = s.getFrequency();
			Podcast pc = s.getPodcast();
			//do we update the episode list manually or every x days
			if(s.getFrequency() != Subscription.MANUAL && (now - frequency > last))
			{
				//update the subscription podcast with any new episodes from the webs
				s.setPodcast(getNewEpisodes(pc));
				//update the timestamp of the subscription
				s.setLastUpdate(System.currentTimeMillis());
				subData.updateSubscription(s);
			}
			else
			{
				Log.i("SUB", "Skipping update of " + pc.getName() + ", last update was " + new Date(last));
			}
			//do we need to download anything?
			List<Episode> neededEpisodes = getNeededEpisodes(s);
			Log.i("SUB", "Downloading " + neededEpisodes.size() + " for " + pc.getName());
			for(Episode e : neededEpisodes)
			{
				DownloadService.getInstance(context).downloadEpisode(pc, e);
			}
		}
	}
	
	public List<Episode> getNeededEpisodes(Subscription sub)
	{
		List<Episode> neededList = new ArrayList<Episode>();
		if(sub.isAutoDownload())
		{
			Podcast pc = sub.getPodcast();
			ArrayList<Episode> epList = pc.getEpisodes();
			//sort the episodes to put them in order
			Collections.sort(epList);
			int needed = (int)sub.getEpisodes();
			for(int i = 0; i < needed; i++)
			{
				Episode e = epList.get(i);
				//do we have this guy already?
				String filePath = e.getFilepath();
				//the file path was set to somethig, check the file that is there
				if(filePath != null && filePath.length() > 5)
				{
					File f = new File(filePath);
					//if the file is missing, we need it
					//if the file is zero bytes, we need it
					if(!f.exists() || f.length() == 0)
					{
						neededList.add(e);
					}
				}
				else
				{
					neededList.add(e);
				}
			}
		}
		
		return neededList;
	}
	
	private Podcast getNewEpisodes(Podcast pod)
	{
		try
		{
			long podId = pod.getPodcastId();
			Rss rss = new Rss();
			//get a new version of the podcast
			rss.initializeFromUrl(pod.getFeedUrl());
			Podcast pc = PodcastFactory.getInstance(context).createPodcast(rss, null, pod.getImageUrl());
			for(Episode e : pc.getEpisodes())
			{
				Episode existing = epData.retrieveEpisodeByName(podId, e.getName());
				//did not find this episode in the database
				if(existing == null)
				{
					//set the correct id
					e.setPodcastId(podId);
					//drop it into the db
					e = epData.createEpisode(e);
					//add it to the podcast
					pod.addEpisode(e);
				}
			}
		}
		catch(Exception e)
		{
			Log.e("Sub", "Could not get the new episode list for the subscription", e);
		}
		//ship the podcast back
		return pod;
	}
	
}
