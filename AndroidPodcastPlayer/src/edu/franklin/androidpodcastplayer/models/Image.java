package edu.franklin.androidpodcastplayer.models;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Image extends XmlSerializable
{
	private static final String IMAGE = "image";
	private static final String URL = "url";
	private static final String TITLE = "title";
	private static final String LINK = "link";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String DESCRIPTION = "description";
	
	//required
	private String url = "";
	private String title = "";
	private String link = "";
	//optional
	private int width = 88;
	private int height = 31;
	private String description = "";
	
	public Image()
	{
		//
	}
	
	public Image(String url, String title, String link)
	{
		this.url = url;
		this.title = title;
		this.link = link;
	}

	public String getUrl() 
	{
		return url;
	}

	public void setUrl(String url) 
	{
		this.url = url;
	}

	public String getTitle() 
	{
		return title;
	}

	public void setTitle(String title) 
	{
		this.title = title;
	}

	public String getLink() 
	{
		return link;
	}

	public void setLink(String link) 
	{
		this.link = link;
	}
	
	public int getWidth() 
	{
		return width;
	}

	public void setWidth(int width) 
	{
		//max width is 144 in rss
		if(width > 144) width = 144;
		this.width = width;
	}

	public int getHeight() 
	{
		return height;
	}

	public void setHeight(int height) 
	{
		//max height is 400 in rss
		if(height > 400) height = 400;
		this.height = height;
	}

	public String getDescription() 
	{
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Image other = (Image) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	@Override
	public void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException
	{
		xml.require(XmlPullParser.START_TAG, ns, IMAGE);
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
	        if(name.equals(TITLE))
	        {
	        	setTitle(this.getNextString(xml, ns, TITLE));
	        }
	        else if(name.equals(LINK))
	        {
	        	setLink(this.getNextString(xml, ns, LINK));
	        }
	        else if(name.equals(URL))
	        {
	        	setUrl(this.getNextString(xml, ns, URL));
	        }
	        else if(name.equals(DESCRIPTION))
	        {
	        	setDescription(this.getNextString(xml, ns, DESCRIPTION));
	        }
	        else if(name.equals(WIDTH))
	        {
	        	setWidth(getNextInt(xml, ns, WIDTH));
	        }
	        else if(name.equals(HEIGHT))
	        {
	        	setHeight(getNextInt(xml, ns, HEIGHT));
	        }
	        //image should not have any extra elements mixed in,
	        //but if it does, ignore them
	        else
	        {
	        	XmlSerializable.skip(xml);
	        }
	    } 
	}

	public String toString() 
	{
		return " [url=" + url + ", title=" + title + ", link=" + link
				+ "]";
	}
}
