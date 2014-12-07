package edu.franklin.androidpodcastplayer.models;

import java.util.ArrayList;

import android.util.Log;

public class Podcast {
	
	public static final String PODCASTS = "podcast_subscriptions";
	public static final String IMAGES = "images";
	public static final String RSS = "rss_files";
	//lets use this to allow users to download an episode and play it before subscribing
	public static final long TRIAL_PODCAST_ID = 123456789;
	private long podcastId;
	private String name;
	private String description;
	private String image; //the filepath to the podcast image
	private String imageUrl;
	private long numEpisodes; //the number of episodes stored on the device
	private String feedUrl;
	private String dir; //the file directory for this podcast
	private boolean oldestFirst = true; //config setting to determine if episodes should be displayed oldest to newest or newest to oldest
	private boolean autoDownload = false; //config setting to determine if new episodes should download automatically
	private boolean autoDelete = false; //config setting to determine if episodes should be deleted once they have been completed
	private ArrayList<Episode> episodes = new ArrayList<Episode>();
	
	
	public long getPodcastId() {
		return podcastId;
	}
	public void setPodcastId(long podcastId) {
		this.podcastId = podcastId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getImageUrl()
	{
		return imageUrl;
	}
	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}
	public long getNumEpisodes() {
		return numEpisodes;
	}
	public void setNumEpisodes(long numEpisodes) {
		this.numEpisodes = numEpisodes;
	}
	public String getFeedUrl() {
		return feedUrl;
	}
	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public boolean isOldestFirst() {
		return oldestFirst;
	}
	public void setOldestFirst(boolean oldestFirst) {
		this.oldestFirst = oldestFirst;
	}
	public boolean isAutoDownload() {
		return autoDownload;
	}
	public void setAutoDownload(boolean autoDownload) {
		this.autoDownload = autoDownload;
	}
	public boolean isAutoDelete() {
		return autoDelete;
	}
	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}
	public ArrayList<Episode> getEpisodes() {
		return episodes;
	}
	public void setEpisodes(ArrayList<Episode> episodes) {
		this.episodes = episodes;
	}
	
	public void addEpisode(Episode episode)
	{
		//get rid of any old version of this episode
		if(episodes.contains(episode))
		{
			episodes.remove(episode);
		}
		//add it in
		episodes.add(episode);
	}
	
	public static String getPodcastDirectory(String dir)
	{
		return Podcast.PODCASTS + "/" + dir;
	}
	
	public boolean imageIsUrl()
	{
		return image != null ? image.startsWith("http") : false;
	}
}
