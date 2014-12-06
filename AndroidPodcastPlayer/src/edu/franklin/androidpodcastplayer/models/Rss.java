package edu.franklin.androidpodcastplayer.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.util.Log;
import edu.franklin.androidpodcastplayer.tasks.DownloadWebPageTask;

public class Rss extends XmlSerializable 
{
	private static final String RSS = "rss";
	private static final String CHANNEL = "channel";
	private XmlPullParser xml = null;
	private String ns = null;
	//an rss feed will contain a single channel
	private Channel channel = new Channel();
	private boolean initialized = false;
	private String url = null;
	
	public Rss() throws XmlPullParserException
	{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		xml = factory.newPullParser();
	}
	
	/**
	 * This method can fetch an rss xml doc from a url and parse it.
	 * If using a namespace, be sure to set it before invoking this so
	 * the xml is parsed correctly.
	 * @param urlName
	 * @throws IOException
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws XmlPullParserException 
	 */
	public void initializeFromUrl(String urlName) throws IOException, InterruptedException, ExecutionException, XmlPullParserException
	{
		AsyncTask<String, Void, String> pageTask = new DownloadWebPageTask().execute(urlName);
		this.url = urlName;
		String text = pageTask.get();
		StringReader reader = new StringReader(text);
		initializeFromReader(reader);
	}
	
	/**
	 * This method can read an xml doc from the file system.
	 * If using a namespace, be sure to set it before invoking this so
	 * the xml is parsed correctly.
	 * @param filename
	 * @throws XmlPullParserException 
	 * @throws IOException 
	 */
	public void initializeFromFile(String filename) throws IOException, XmlPullParserException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		initializeFromReader(reader);
	}
	
	public void initializeFromRaw(InputStream stream) throws IOException, XmlPullParserException
	{
		InputStreamReader reader = new InputStreamReader(stream);
		initializeFromReader(reader);
	}
	
	/**
	 * The main workhorse of Rss. It will build our object
	 * from the found xml file.
	 * @param reader
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException 
	 */
	private void initializeFromReader(Reader reader) throws IOException, XmlPullParserException
	{
		xml.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		xml.setInput(reader);
		xml.nextTag();
		initializeFromXmlParser(xml, ns);
	    initialized = true;
	}
	
	public Channel getChannel()
	{
		return channel;
	}
	
	public void setChannel(Channel channel)
	{
		this.channel = channel;
	}
	
	public String getNamespace() 
	{
		return ns;
	}

	public void setNamespace(String namespace) 
	{
		this.ns = namespace;
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException 
	{
		xml.require(XmlPullParser.START_TAG, ns, RSS);
		//read the entire RSS file, if we hit an ending tag before 
		//we are supposed to, bail
	    while (xml.next() != XmlPullParser.END_TAG) 
	    {
	        if (xml.getEventType() != XmlPullParser.START_TAG) 
	        {
	            continue;
	        }
	        String name = xml.getName();
//	        Log.d("RSS", "Rss Tage name is " + name);
	        // Starts by looking for the entry tag
	        if(name.equals(CHANNEL))
	        {
	        	channel.initializeFromXmlParser(xml, ns);
	        }
	        else
	        {
	        	XmlSerializable.skip(xml);
	        }
	    } 
	}
	
	public String toHtml(String url)
	{
		//start with a vanilla html page with a title for the url
		StringBuilder sb = new StringBuilder("<html><head><title>Rss feed from " + url + "</title></head><body>");
		if(isInitialized())
		{
			sb.append("<h3>Your Rss feed is below!</h3><p><pre>");
			sb.append(toString().replaceAll("\\n", "<br>"));
			sb.append("</pre></p>");
		}
		else
		{
			sb.append("<p>The Rss feed was not initialized. Maybe there was a problem?</p>");
		}
		//close out the html
		sb.append("</body></html>");
		return sb.toString();
	}
	
	public String toString() 
	{
		return "Rss \nChannel\n" + channel;
	}
	
	public String getUrl()
	{
		return url;
	}
}
