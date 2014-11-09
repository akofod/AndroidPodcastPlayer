package edu.franklin.androidpodcastplayer.models;

import java.io.IOException;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class Enclosure extends XmlSerializable
{
	private static final String ENCLOSURE = "enclosure";
	private static final String URL = "url";
	private static final String LENGTH = "length";
	private static final String TYPE = "type";
	
	private String url;
	//how many bytes is it?
	private long length = 0;
	//the mime type;
	private String type = "audio/mpg";
	
	public Enclosure()
	{
		//
	}

	public String getUrl() 
	{
		return url;
	}

	public void setUrl(String url) 
	{
		this.url = url;
	}

	public long getLength() 
	{
		return length;
	}

	public void setLength(long length) 
	{
		this.length = length;
	}

	public String getType() 
	{
		return type;
	}

	public void setType(String type) 
	{
		this.type = type;
	}

	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Enclosure other = (Enclosure) obj;
		if (length != other.length)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	public void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException
	{
		xml.require(XmlPullParser.START_TAG, ns, ENCLOSURE);

        try
	    {
	    	Map<String, String> attributeMap = this.getAttributeMap(xml);
			//set the domain to what was in the map
			setLength(Long.parseLong(attributeMap.get(LENGTH)));
			setType(attributeMap.get(TYPE));
			setUrl(attributeMap.get(URL));
			//try to advance?
			xml.next();
			xml.require(XmlPullParser.END_TAG, ns, ENCLOSURE);
	    }
	    catch(Exception e)
	    {
	    	
	    }
	}

	public String toString() {
		return "Enclosure [url=" + url + ", length=" + length + ", type="
				+ type + "]";
	}
	
}
