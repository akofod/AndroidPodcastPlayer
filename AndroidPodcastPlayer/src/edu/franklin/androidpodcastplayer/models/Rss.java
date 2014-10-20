package edu.franklin.androidpodcastplayer.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import edu.franklin.androidpodcastplayer.utilities.Downloader;

public class Rss extends XmlSerializable 
{
	private static final String RSS = "rss";
	private static final String CHANNEL = "channel";
	private XmlPullParser xml = null;
	private String ns = null;
	//an rss feed will contain a single channel
	private Channel channel = new Channel();
	
	public Rss() throws XmlPullParserException
	{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        xml = factory.newPullParser();  
	}
	
	/**
	 * This method can fetch an rss xml doc from a url and parse it.
	 * If using a namespace, be sure to set it before invoking this so
	 * the xml is parsed correctly.
	 * @param urlName
	 * @throws IOException
	 */
	public void initializeFromUrl(String urlName) throws IOException
	{
		InputStream is = Downloader.downloadUrl(urlName);
		InputStreamReader reader = new InputStreamReader(is);
		initializeFromReader(reader);
	}
	
	/**
	 * This method can read an xml doc from the file system.
	 * If using a namespace, be sure to set it before invoking this so
	 * the xml is parsed correctly.
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public void initializeFromFile(String filename) throws FileNotFoundException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
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
	private void initializeFromReader(Reader reader)
	{
		try
		{
			xml.setInput(reader);
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
		catch(Exception e)
		{
			//something bad happened
			e.printStackTrace();
		}
		finally
		{
			//no matter what, free up the reader resources.
			try { reader.close(); } catch(Exception ex) {}
		}
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
	
	@Override
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
}
