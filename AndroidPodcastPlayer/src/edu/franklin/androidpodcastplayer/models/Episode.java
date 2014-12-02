package edu.franklin.androidpodcastplayer.models;

public class Episode {
	
	private long episodeId;
	private long podcastId;
	private String name;
	private String url;
	private String filepath = ""; //the filepath to the stored episode
	private String image = ""; //the filepath to the episode's image, if different from podcast image
	private long totalTime = 0L; //the total time of the episode
	private long playedTime = 0L; //time stamp of the farthest point that the episode has played to
	private boolean newEpisode = true; //true if the episode has not been played yet
	private boolean completed = false; //true if the episode has played all the way through
	
	public long getEpisodeId() {
		return episodeId;
	}
	public void setEpisodeId(long episodeId) {
		this.episodeId = episodeId;
	}
	public long getPodcastId() {
		return podcastId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (podcastId ^ (podcastId >>> 32));
		return result;
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Episode other = (Episode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (podcastId != other.podcastId)
			return false;
		return true;
	}
	public String toString() {
		return "Episode [episodeId=" + episodeId + ", podcastId=" + podcastId
				+ ", name=" + name + ", url=" + url + ", filepath=" + filepath
				+ ", image=" + image + ", totalTime=" + totalTime
				+ ", playedTime=" + playedTime + ", newEpisode=" + newEpisode
				+ ", completed=" + completed + "]";
	}
	
	public static String longToString(long time)
	{
		int hours = time > 0 ? (int)(time / (60 * 60)) : 0;
		time -= (hours * 60 * 60);
		int minutes = time > 0 ? (int)(time / 60) : 0;
		time -= (minutes * 60);
		int seconds = (int)time;
		String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		return timeString;
	}
	
	public static long stringToLong(String string)
	{
		long time = 0;
		if(string != null && !string.equals("0") && string.contains(":"))
		{
			String[] tokens = string.split(":");
    		for(int i = 0; i < tokens.length; i++)
    		{
    			time = (time * 60) + (Integer.parseInt(tokens[i])); 
    		}
		}
		return time;
	}
}
