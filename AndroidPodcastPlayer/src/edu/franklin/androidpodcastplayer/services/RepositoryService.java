package edu.franklin.androidpodcastplayer.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import edu.franklin.androidpodcastplayer.data.PodcastInfoData;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.PodcastInfo;
import edu.franklin.androidpodcastplayer.tasks.DownloadFileTask;

/**
 * The repository service will be invoked when the application starts up.
 * It will hit the repository and fetch info about the top 50 podcasts,
 * then stick those entries into the database so they can be used in the
 * repo view.
 * 
 * @author rennardhutchinson
 *
 */
public class RepositoryService extends IntentService 
{
	private static final int TOP_COUNT = 50;
	private final String jsonGetTop = "https://gpodder.net/toplist/" + TOP_COUNT + ".json";
	private FileManager fm = null;
	private PodcastInfoData data = null;

	public RepositoryService() 
	{
		super("RepositoryService");
		fm = new FileManager(this);
	}

	protected void onHandleIntent(Intent intent) 
	{
		//open up the data for business
		data = new PodcastInfoData(this);
		data.open();
		//now get the new...maybe wait until we get something before blowing it all away...
		JSONArray pods = fetchTopPodcasts();
		if(pods != null)
		{
			for(int i = 0; i < pods.length(); i++)
			{
				try
				{
					JSONObject obj = pods.getJSONObject(i);
					PodcastInfo info = new PodcastInfo();
					info.setName(obj.getString("title"));
					info.setDescription(obj.getString("description"));
					info.setUrl(obj.getString("url"));
					info.setImageUrl(obj.getString("logo_url"));
					info.setPosition(i + 1);
					//now load up the image
					String imageName = info.getImageUrl().substring(info.getImageUrl().lastIndexOf("/") + 1);
					//fetch the image if we don't already have it
					if(!fm.fileExists(Podcast.IMAGES, imageName) && info.getImageUrl() != null && !info.getImageUrl().equalsIgnoreCase("null"))
					{
						DownloadFileTask dl = new DownloadFileTask(this);
						dl.execute(info.getImageUrl(), Podcast.IMAGES, imageName);
						dl.get();
					}
					else
					{
						Log.i("RepoService", "Skipping download of " + info.getName() + " image " + " url is " + info.getImageUrl());
					}
					if(!fm.fileExists(Podcast.RSS, info.getName()))
					{
						DownloadFileTask dl = new DownloadFileTask(this);
						dl.execute(info.getUrl(), Podcast.RSS, info.getName());
						dl.get();
					}
					//persist the info data
					data.createPodcastInfo(info);
					Log.i("RepoService", "Info " + (i + 1) + " is in the db");
				}
				catch(Exception e)
				{
					Log.e("RepoService", "Could not parse the JSON Object", e);
				}
			}
			Log.i("RepoService", "Podcast Info has been updated");
		}
		data.close();
	}
	
	private JSONArray fetchTopPodcasts()
	{
		StringBuffer sb = new StringBuffer();
		String line = "";
		
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(jsonGetTop);
		
		try
		{
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200)
			{
				HttpEntity httpEntity = httpResponse.getEntity();
				BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
				
				while ((line = reader.readLine()) != null)
				{
					sb.append(line);
				}
				return new JSONArray(sb.toString()); 
			}
		}
		catch(Exception e)
		{
			Log.e("RepoService", "Could not get the top tags!", e);
		}
		return null;
	}
}
