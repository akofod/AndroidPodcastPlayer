package edu.franklin.androidpodcastplayer.models;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Guid extends XmlSerializable
{
	private String id = "";
	private boolean isPermaLink = false;
	
	public Guid(String id)
	{
		this(id, false);
	}
	
	public Guid(String id, boolean isPermaLink)
	{
		this.id = id;
		this.isPermaLink = isPermaLink;
	}

	public String getId() 
	{
		return id;
	}

	public void setId(String id) 
	{
		this.id = id;
	}

	public boolean isPermaLink() 
	{
		return isPermaLink;
	}

	public void setPermaLink(boolean isPermaLink) 
	{
		this.isPermaLink = isPermaLink;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Guid other = (Guid) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException 
	{
		
	}
	
}
