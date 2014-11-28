package edu.franklin.androidpodcastplayer.models;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Subscription implements Cloneable
{
	public static final long MANUAL = 0;
	//milliseconds * seconds * minutes = hour * 24 = 1 day
	public static final long DAILY = 1000 * 60 * 60 * 24;
	public static final long WEEKLY = DAILY * 7;
	public static final long ONE = 1;
	public static final long THREE = 3;
	public static final long FIVE = 5;
	public static final long ALL = 200;
	private Podcast podcast = null;
	private long episodes = 1;
	private boolean autoDownload = true;
	private boolean newestFirst = true;
	private long lastUpdate = Subscription.normalizeTime(System.currentTimeMillis());
	private long frequency = DAILY;
	
	public Subscription()
	{
		//
	}

	public Podcast getPodcast() 
	{
		return podcast;
	}

	public void setPodcast(Podcast podcast) 
	{
		this.podcast = podcast;
	}

	public long getEpisodes() 
	{
		return episodes;
	}

	public void setEpisodes(long episodes) 
	{
		this.episodes = episodes;
	}
	
	public long getFrequency()
	{
		return this.frequency;
	}
	
	public void setFrequency(long frequency)
	{
		this.frequency = frequency;
	}
	
	public boolean isAutoDownload() 
	{
		return autoDownload;
	}

	public void setAutoDownload(boolean autoDownload) 
	{
		this.autoDownload = autoDownload;
	}

	public boolean isNewestFirst() 
	{
		return newestFirst;
	}

	public void setNewestFirst(boolean newestFirst) 
	{
		this.newestFirst = newestFirst;
	}

	public long getLastUpdate() 
	{
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) 
	{
		this.lastUpdate = normalizeTime(lastUpdate);
	}
	
	public static long normalizeTime(long time)
	{
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(time > 0 ? time : System.currentTimeMillis());
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 1);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
	
	public String toString()
	{
		return podcast != null ? (podcast.getName() + "[" + episodes + "] " + autoDownload + ", frequency " + frequency + ", last updated " + new Date(lastUpdate).toString()) : "Subscription Podcast Missing";
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Subscription)
		{
			Subscription that = (Subscription)obj;
			return that.getPodcast().getPodcastId() == this.getPodcast().getPodcastId();
		}
		return false;
	}
	
	public Subscription copy()
	{
		try 
		{
			return (Subscription)clone();
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}
