package edu.franklin.androidpodcastplayer.models;

import java.util.ArrayList;

public class Podcast {
	
	private long podcastId;
	private String name;
	private String description;
	private String image; //the filepath to the podcast image
	private long numEpisodes; //the number of episodes stored on the device
	private String feedUrl;
	private String dir; //the file directory for this podcast
	private boolean oldestFirst = true; //config setting to determine if episodes should be displayed oldest to newest or newest to oldest
	private boolean autoDownload = false; //config setting to determine if new episodes should download automatically
	private boolean autoDelete = false; //config setting to determine if episodes should be deleted once they have been completed
	private ArrayList<Episode> episodes;
	
	
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
}
