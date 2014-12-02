package edu.franklin.androidpodcastplayer.models;

public class Download 
{
	private long downloadId;
	private String podcastName;
	private String episodeName;
	private String dir;
	private String file;
	
	public Download()
	{
		
	}

	public long getDownloadId() 
	{
		return downloadId;
	}

	public void setDownloadId(long downloadId) 
	{
		this.downloadId = downloadId;
	}

	public String getDir() 
	{
		return dir;
	}

	public void setDir(String dir) 
	{
		this.dir = dir;
	}

	public String getPodcastName() 
	{
		return podcastName;
	}

	public void setPodcastName(String podcastName) 
	{
		this.podcastName = podcastName;
	}

	public String getEpisodeName() 
	{
		return episodeName;
	}

	public void setEpisodeName(String episodeName) 
	{
		this.episodeName = episodeName;
	}

	public String getFile() 
	{
		return file;
	}

	public void setFile(String file) 
	{
		this.file = file;
	}
	
	public boolean equals(Object other)
	{
		if(other instanceof Download)
		{
			return this.downloadId == ((Download)other).getDownloadId();
		}
		return false;
	}
	
	public String toString() 
	{
		return "Download [downloadId=" + downloadId + ", podcastName="
				+ podcastName + ", episodeName=" + episodeName + ", dir=" + dir
				+ ", file=" + file + "]";
	}
}
