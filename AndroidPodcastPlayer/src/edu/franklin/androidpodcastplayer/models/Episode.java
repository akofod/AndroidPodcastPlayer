package edu.franklin.androidpodcastplayer.models;

public class Episode {
	
	private int episodeId;
	private int podcastId;
	private String name;
	private String filepath; //the filepath to the stored episode
	private String image; //the filepath to the episode's image, if different from podcast image
	private int totalTime; //the total time of the episode
	private int playedTime; //time stamp of the farthest point that the episode has played to
	private boolean newEpisode; //true if the episode has not been played yet
	private boolean completed; //true if the episode has played all the way through
	
	public int getEpisodeId() {
		return episodeId;
	}
	public void setEpisodeId(int episodeId) {
		this.episodeId = episodeId;
	}
	public int getPodcastId() {
		return podcastId;
	}
	public void setPodcastId(int podcastId) {
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
	public int getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}
	public int getPlayedTime() {
		return playedTime;
	}
	public void setPlayedTime(int playedTime) {
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
