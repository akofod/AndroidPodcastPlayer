package edu.franklin.androidpodcastplayer.models;

public class Episode {
	
	private long episodeId;
	private long podcastId;
	private String name;
	private String filepath; //the filepath to the stored episode
	private String image; //the filepath to the episode's image, if different from podcast image
	private long totalTime; //the total time of the episode
	private long playedTime; //time stamp of the farthest point that the episode has played to
	private boolean newEpisode; //true if the episode has not been played yet
	private boolean completed; //true if the episode has played all the way through
	
	public long getEpisodeId() {
		return episodeId;
	}
	public void setEpisodeId(long episodeId) {
		this.episodeId = episodeId;
	}
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
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	public long getPlayedTime() {
		return playedTime;
	}
	public void setPlayedTime(long playedTime) {
		this.playedTime = playedTime;
	}
	public boolean isNewEpisode() {
		return newEpisode;
	}
	public void setNewEpisode(boolean newEpisode) {
		this.newEpisode = newEpisode;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
