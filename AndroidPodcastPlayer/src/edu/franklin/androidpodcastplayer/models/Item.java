package edu.franklin.androidpodcastplayer.models;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Item extends XmlSerializable
{
	private static final String ITEM = "item";
	private static final String TITLE = "title";
	private static final String LINK = "link";
	private static final String DESCRIPTION = "description";
	private static final String CATEGORY = "category";
	
	//Required fields At least one of title or description must be set
	private String title;
	//Link and URL type field must begin with:
	//http://, https://, news://, mailto: and ftp://
	private String link;
	//if the Item is complete (no link needs to be hit to fetch contents)
	//then the description field will contain the payload.
	//this can be html, xml, or whatever is appropriate for this feed.
	//If this is the case, the title and link may be empty...so watch out.
	private String description;
	
	//optional fields.
	//the author is the email address of the author, not his name
	private String author = "";
	//something to group this with other objects in a similar category
	private Category category = null;
	//TODO look into what this actually is...
	private Enclosure enclosure = null;
	//global unique identifier to see if an item has already been fetched or is new.
	private Guid guid = null;
	//a Date object saying when this item was published. It can be used to tell
	//the reader to ignore it (maybe it is too old)...we will just treat as a string for now.
	private String pubDate = "";
	//the name of the rss channel that this item came from
	private String source = "";
	//if present, indicates the url of the comments page to leave comments abut
	//the feed...we will probably ignore this.
	private String comments = "";
	
	public Item()
	{
		//
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

	public String getDescription() 
	{
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	public String getAuthor() 
	{
		return author;
	}

	public void setAuthor(String author) 
	{
		this.author = author;
	}

	public Category getCategory() 
	{
		return category;
	}

	public void setCategory(Category category) 
	{
		this.category = category;
	}

	public Enclosure getEnclosure() 
	{
		return enclosure;
	}

	public void setEnclosure(Enclosure enclosure) 
	{
		this.enclosure = enclosure;
	}

	public Guid getGuid() 
	{
		return guid;
	}

	public void setGuid(Guid guid) 
	{
		this.guid = guid;
	}

	public String getPubDate() 
	{
		return pubDate;
	}

	public void setPubDate(String pubDate) 
	{
		this.pubDate = pubDate;
	}

	public String getSource() 
	{
		return source;
	}

	public void setSource(String source) 
	{
		this.source = source;
	}

	public String getComments() 
	{
		return comments;
	}

	public void setComments(String comments) 
	{
		this.comments = comments;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
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
		Item other = (Item) obj;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		return true;
	}
	
	public void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException
	{
		xml.require(XmlPullParser.START_TAG, ns, ITEM);
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
	        if(name.equals(LINK))
	        {
	        	setTitle(this.getNextString(xml, ns, LINK));
	        }
	        if(name.equals(DESCRIPTION))
	        {
	        	setTitle(this.getNextString(xml, ns, DESCRIPTION));
	        }
	        else if(name.equals(CATEGORY))
	        {
	        	Category category = new Category();
	        	category.initializeFromXmlParser(xml, ns);
	        	setCategory(category);
	        }
	        else
	        {
	        	skip(xml);
	        }
	    }
	}	
}
