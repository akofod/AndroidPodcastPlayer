package edu.franklin.androidpodcastplayer.models;

import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

public class Category extends XmlSerializable
{
	private static final String CATEGORY = "category";
	private static final String DOMAIN = "domain";
	
	//required
	private String category = "";
	//optional
	private String domain = null;
	
	public Category()
	{
		//
	}

	public String getCategory() 
	{
		return category;
	}

	public void setCategory(String category) 
	{
		this.category = category;
	}

	public String getDomain() 
	{
		return domain;
	}

	public void setDomain(String domain) 
	{
		this.domain = domain;
	}
	
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
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
		Category other = (Category) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		return true;
	}

	@Override
	public void initializeFromXmlParser(XmlPullParser xml, String ns) 
	{
		try
		{
			xml.require(XmlPullParser.START_TAG, ns, CATEGORY);			
		    //is there a domain?
			Map<String, String> attributeMap = this.getAttributeMap(xml);
			//set the domain to what was in the map
			setDomain(attributeMap.get(DOMAIN));
			setCategory(getNextString(xml, ns, CATEGORY));
	        xml.require(XmlPullParser.END_TAG, ns, CATEGORY);
		}
		catch(Exception e)
		{
			//
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		return category;
	}
}
