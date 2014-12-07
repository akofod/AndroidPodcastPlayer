package edu.franklin.androidpodcastplayer.utilities;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.impl.cookie.DateUtils;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.util.Log;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Channel;
import edu.franklin.androidpodcastplayer.models.Enclosure;
import edu.franklin.androidpodcastplayer.models.Episode;
import edu.franklin.androidpodcastplayer.models.Image;
import edu.franklin.androidpodcastplayer.models.Item;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Rss;
import edu.franklin.androidpodcastplayer.services.FileManager;
import edu.franklin.androidpodcastplayer.tasks.DownloadFileTask;
import edu.franklin.androidpodcastplayer.tasks.DownloadHandler;

public class PodcastFactory
{
	private PodcastData podData = null;
	private EpisodesData epData = null;
	private boolean initialized = false;
	private FileManager fileManager = null;
	private Context context;
	
	private static PodcastFactory INST = null;
	
	private PodcastFactory()
	{
		//
	}
	
	public static PodcastFactory getInstance(Context context)
	{
		if(INST == null)
		{
			INST = new PodcastFactory();
			INST.initialize(context);
		}
		return INST;
	}
	
	public void initialize(Context context)
	{
		if(!initialized)
		{
			this.context = context;
			fileManager = new FileManager(context);
			podData = new PodcastData(context);
			epData = new EpisodesData(context);
			podData.open();
			epData.open();
			this.initialized = true;
		}
	}
	
	public void close()
	{
		if(initialized)
		{
			epData.close();
			podData.close();
		}
	}
	
	public Podcast createPodcast(Rss rss, DownloadHandler handler, String logoUrl)
	{
		Podcast pc = new Podcast();
		//first things first, grab an image for this guy
		Channel channel = rss.getChannel();
		Image image = channel.getImage();
		String podcastTitle = channel.getTitle();
		String podcastHomeDir = Podcast.getPodcastDirectory(podcastTitle);
		//make a dir for the podcast and any temp episodes
		fileManager.mkDir(podcastHomeDir);
		pc.setName(podcastTitle);
		pc.setDescription(channel.getDescription());
		pc.setNumEpisodes(0L);
		pc.setImage("");
		if(logoUrl != null && !logoUrl.equals("null"))
		{
			String imageName = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
			downloadFile(Podcast.IMAGES, imageName, logoUrl, handler);
			pc.setImageUrl(logoUrl);
		}
		else if(image != null && image.getUrl() != null && image.getUrl().contains("/"))
		{
			String imageName = image.getUrl().substring(image.getUrl().lastIndexOf("/") + 1);
			downloadFile(Podcast.IMAGES, imageName, image.getUrl(), handler);
			pc.setImageUrl(image.getUrl());
		}
		pc.setFeedUrl(rss.getUrl() != null ? rss.getUrl() : channel.getLink());
		pc.setDir(fileManager.getAbsoluteFilePath(podcastHomeDir, null));
		pc.setPodcastId(0);
		
		if(pc != null)
		{
			//the podcast is in the db...add in the episode info
			for(Item item : channel.getItemList())
			{
				Episode e = new Episode();
				e.setEpisodeId(pc.getEpisodes().size());
				e.setPodcastId(pc.getPodcastId());
				e.setCompleted(false);
				//item objects don't have images
				e.setImage("");
				e.setName(item.getTitle());
				String link = item.getLink();
				//if there is an enclosure, use that for the url
				if(item.getEnclosure() != null)
				{
					Enclosure enc = item.getEnclosure();
					link = enc.getUrl().length() > 0 ? enc.getUrl() : link;
				}
				e.setUrl(link);
				String dir = Podcast.getPodcastDirectory(pc.getName());
				String file = link.substring(link.lastIndexOf("/") + 1);
				String filePath = fileManager.getAbsoluteFilePath(dir, file);
				File episodeFile = new File(filePath);
				e.setFilepath(episodeFile.exists() && episodeFile.length() > 0 ? filePath : "");
				e.setNewEpisode(false);
				e.setPlayedTime(0);
				//Use the duration if it was provided by the file.
				e.setTotalTime(item.getDuration());		
				//any pubDate?
				if(item.getPubDate() != null && item.getPubDate().length() > 0)
				{
					try
					{
						e.setPubDate(DateUtils.parseDate(item.getPubDate()).getTime());
					}
					catch(Exception ex) { Log.e("PD", "Could not parse the pub date " + item.getPubDate());}
				}
				pc.addEpisode(e);
			}
		}
		return pc;		
	}
	
	//this is for converting a previously viewed podcast into a real version that 
	//lives in the database
	public Podcast subscribeToPodcast(Podcast podcast)
	{
		Podcast pc = podData.createPodcast(podcast);
		long id = pc.getPodcastId();
		//now that we have the podcast id, update the episodes
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		for(Episode e : podcast.getEpisodes())
		{
			e.setPodcastId(id);
			Episode ee = epData.createEpisode(e);
			episodes.add(ee);
		}
		pc.setEpisodes(episodes);
		//go ahead and update the stored count for this podcast (we may have already downloaded some).
		podData.updateSavedCount(podcast.getPodcastId());
		return pc;
	}
	
	public Podcast updateImagePath(Podcast pc, String image)
	{
		podData.updateImagePath(pc.getPodcastId(), image);
		pc.setImage(image);
		return pc;
	}
	
	public void downloadFile(final String dir, final String file, final String url, DownloadHandler handler)
	{
		if(handler != null)
		{
			Log.i("PodcastDetails", "Downloading " + dir + ":" + file + " from " + url);
			DownloadFileTask dft = new DownloadFileTask(context);
			dft.setHandler(handler);
			dft.execute(url, dir, file);
		}
	}
}
