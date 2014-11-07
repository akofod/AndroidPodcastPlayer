package edu.franklin.androidpodcastplayer.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class Channel extends XmlSerializable
{
	//some tags we care about
	private static final String CHANNEL = "channel";
	private static final String ITEM = "item";
	private static final String TITLE = "title";
	private static final String LINK = "link";
	private static final String DESCRIPTION = "description";
	private static final String CATEGORY = "category";
	private static final String IMAGE = "image";
	
	//Required fields
	private String title = "";
	//Link and URL type field must begin with:
	//http://, https://, news://, mailto: and ftp://
	private String link = "";
	private String description = "";
	
	//Optional fields
	//the language of the channel
	private String language = "";
	//copyright notice
	private String copyright = "";
	//managing editor email address
	private String managingEditor = "";
	//email address of person responsible for techical issues with the feed
	private String webMaster = "";
	//The publication date of the channel
	private String pubDate = "";
	//The last time the channel changed.
	private String lastBuildDate = "";
	//Specify one or more categories that the channel belongs to.
	private Category category = null;
	//The name of the program that generated this channel.
	private String generator = "";
	//a url that points to the documentation for the format of this RSS file.
	private String docs = "";
	//Allows processes to be registered with a cloud to be notified of updates
	private String cloud = "";
	//how long should the feed be cached before hitting it again (in minutes)?
	private int ttl = 0;
	//Specifies a GIF, JPEG, or PNG image to be displayed
	private Image image = null;
	//The PICS rating
	private String rating = "";
	//should the channel display the textbox feature?
	private boolean textInput = false;
	//which hours can the aggregator skip?
	private int skipHours = 0;
	//which days should be skipped?
	private int skipDays = 0;
	
	//and any Items this channel may contain
	private List<Item> itemList = new ArrayList<Item>();
	
	public Channel()
	{
		//
	}
	
	public Channel(String title, String link, String description)
	{
		this.title = title;
		this.link = link;
		this.description = description;
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

	public String getLanguage() 
	{
		return language;
	}

	public void setLanguage(String language) 
	{
		this.language = language;
	}

	public String getCopyright() 
	{
		return copyright;
	}

	public void setCopyright(String copyright) 
	{
		this.copyright = copyright;
	}

	public String getManagingEditor() 
	{
		return managingEditor;
	}

	public void setManagingEditor(String managingEditor) 
	{
		this.managingEditor = managingEditor;
	}

	public String getWebMaster() 
	{
		return webMaster;
	}

	public void setWebMaster(String webMaster) 
	{
		this.webMaster = webMaster;
	}

	public String getPubDate() 
	{
		return pubDate;
	}

	public void setPubDate(String pubDate) 
	{
		this.pubDate = pubDate;
	}

	public String getLastBuildDate() 
	{
		return lastBuildDate;
	}

	public void setLastBuildDate(String lastBuildDate) 
	{
		this.lastBuildDate = lastBuildDate;
	}

	public Category getCategory() 
	{
		return category;
	}

	public void setCategory(Category category) 
	{
		this.category = category;
	}

	public String getGenerator() 
	{
		return generator;
	}

	public void setGenerator(String generator) 
	{
		this.generator = generator;
	}

	public String getDocs() 
	{
		return docs;
	}

	public void setDocs(String docs) 
	{
		this.docs = docs;
	}

	public String getCloud() 
	{
		return cloud;
	}

	public void setCloud(String cloud) 
	{
		this.cloud = cloud;
	}

	public int getTtl() 
	{
		return ttl;
	}

	public void setTtl(int ttl) 
	{
		this.ttl = ttl;
	}

	public Image getImage() 
	{
		return image;
	}

	public void setImage(Image image) 
	{
		this.image = image;
	}

	public String getRating() 
	{
		return rating;
	}

	public void setRating(String rating) 
	{
		this.rating = rating;
	}

	public boolean isTextInput() 
	{
		return textInput;
	}

	public void setTextInput(boolean textInput) 
	{
		this.textInput = textInput;
	}

	public int getSkipHours() 
	{
		return skipHours;
	}

	public void setSkipHours(int skipHours) 
	{
		this.skipHours = skipHours;
	}

	public int getSkipDays() 
	{
		return skipDays;
	}

	public void setSkipDays(int skipDays) 
	{
		this.skipDays = skipDays;
	}

	public List<Item> getItemList() 
	{
		return itemList;
	}

	public void setItemList(List<Item> itemList) 
	{
		this.itemList = itemList;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Channel other = (Channel) obj;
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
		return true;
	}

	@Override
	public void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException
	{
		xml.require(XmlPullParser.START_TAG, ns, CHANNEL);
		//read the entire RSS file, if we hit an ending tag before 
		//we are supposed to, bail
	    while (xml.next() != XmlPullParser.END_TAG) 
	    {
	        if (xml.getEventType() != XmlPullParser.START_TAG) 
	        {
	            continue;
	        }
	        String name = xml.getName();
//	        Log.d("Channel", "Tag name is " + name);
	        // Starts by looking for the entry tag
	        if(name.equals(TITLE))
	        {
	        	setTitle(this.getNextString(xml, ns, TITLE));
//	        	Log.d("Channel", "Title is " + title);
	        }
	        else if(name.equals(LINK))
	        {
	        	setLink(this.getNextString(xml, ns, LINK));
//	        	Log.d("Channel", "Link is " + link);
	        }
	        else if(name.equals(DESCRIPTION))
	        {
	        	setDescription(this.getNextString(xml, ns, DESCRIPTION));
//	        	Log.d("Channel", "Description is " + description);
	        }
	        else if(name.equals(CATEGORY))
	        {
	        	category = new Category();
	        	category.initializeFromXmlParser(xml, ns);
//	        	Log.d("Channel", "Category is " + category);
	        }
	        else if(name.equals(IMAGE))
	        {
	        	image = new Image();
	        	image.initializeFromXmlParser(xml, ns);
//	        	Log.d("Channel", "Image is " + image);
	        }
	        else if(name.equals(ITEM))
	        {
	        	Item item = new Item();
	        	item.initializeFromXmlParser(xml, ns);
	        	itemList.add(item);
	        }
	        else
	        {
	        	skip(xml);
	        }
	    }
		
	}

	public String toString() 
	{
		return "title=" + title + ", \nlink=" + link + ", \ndescription="
				+ description + (category != null ? ", \ncategory=" + category : "") + ", \nimage=" + image + "\n" + "Item Count=" + itemList.size();
	}	
}
