package edu.franklin.androidpodcastplayer.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This snippet was taken from the Android Developer website example 
 * located at: http://developer.android.com/training/basics/network-ops/xml.html
 * 
 *
 */
public class Downloader 
{
	public static InputStream downloadUrl(String urlString) throws IOException 
	{
	    URL url = new URL(urlString);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setReadTimeout(10000 /* milliseconds */);
	    conn.setConnectTimeout(15000 /* milliseconds */);
	    conn.setRequestMethod("GET");
	    conn.setDoInput(true);
	    // Starts the query
	    conn.connect();
	    return conn.getInputStream();
	}
}
