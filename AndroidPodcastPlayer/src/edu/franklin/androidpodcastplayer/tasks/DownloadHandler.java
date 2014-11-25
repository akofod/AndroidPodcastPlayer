package edu.franklin.androidpodcastplayer.tasks;

public interface DownloadHandler 
{
	public void downloadFinished(String dir, String file);
	public void downloadFailed(String url, String dir, String file);
}
