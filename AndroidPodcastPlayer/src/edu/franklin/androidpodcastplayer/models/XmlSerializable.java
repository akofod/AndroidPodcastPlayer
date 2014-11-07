package edu.franklin.androidpodcastplayer.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/**
 * This is essentially just a stub to allow other classes to initialize themselves
 * from an xml block. The idea is, we will have an RSS reader that starts off
 * the reading of the xml file and invoking the initializeFromXmlParser method
 * to set its values using helper methods in the class.
 * @author Ren Hutchinson
 *
 */
public abstract class XmlSerializable extends DefaultHandler
{
	public abstract void initializeFromXmlParser(XmlPullParser xml, String ns) throws XmlPullParserException, IOException;
	
	/**
	 * This code snippet taken from Android developer website:
	 * http://developer.android.com/training/basics/network-ops/xml.html
	 * @param parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static void skip(XmlPullParser parser) throws XmlPullParserException, IOException 
	{
	    if (parser.getEventType() != XmlPullParser.START_TAG) 
	    {
	    	Log.e("XML Parsing", "Event Type is " + parser.getEventType() + ":" + XmlPullParser.START_TAG);
	        throw new IllegalStateException();
	    }
	    //start with 1, so we enter the skip loop
	    int depth = 1;
	    
	    while (depth != 0) 
	    {
	    	
	        switch (parser.next()) 
	        {
	        	//if we hit an end tag, we can decrement the count...if we hit 0 we are done
	        	case XmlPullParser.END_TAG:
	        		depth--;
	        		break;
	        	//otherwise, we have an open tag and need to increment the depth so 
	        	//we continue to process elements
	        	case XmlPullParser.START_TAG:
	        		depth++;
	        		break;
	        }
	    }
	}
	
	public String getNextString(XmlPullParser xml, String ns, String tag) throws XmlPullParserException, IOException
	{
		xml.require(XmlPullParser.START_TAG, ns, tag);
	    String value = readText(xml);
	    xml.require(XmlPullParser.END_TAG, ns, tag);
	    return value;

	}
	
	public int getNextInt(XmlPullParser xml, String ns, String tag) throws XmlPullParserException, IOException
	{
		try
		{
			return Integer.parseInt(getNextString(xml, ns, tag));
		}
		catch(NumberFormatException e)
		{
			//
		}
		return -1;
	}
	
	public long getNextLong(XmlPullParser xml, String ns, String tag) throws XmlPullParserException, IOException
	{
		try
		{
			return Long.parseLong(getNextString(xml, ns, tag));
		}
		catch(NumberFormatException e)
		{
			//
		}
		return -1;
	}
	
	private String readText(XmlPullParser xml) throws IOException, XmlPullParserException 
	{    
		String result = xml.nextText();
	    
		if (xml.getEventType() != XmlPullParser.END_TAG) 
	    {
	    	xml.nextTag();
	    }
	    
	    return result;
	}
	
	public Map<String, String> getAttributeMap(XmlPullParser xml) throws IOException, XmlPullParserException
	{
	    Map<String,String> attributeMap = new HashMap<String, String>();
	    int attributeCount = xml.getAttributeCount();
	    if(attributeCount >= 0) 
	    {
	        for(int i = 0; i < attributeCount; i++) 
	        {
//	        	Log.d("XML", "Attribute is " + xml.getAttributeName(i));
//	        	Log.d("XML", "Value is " + xml.getAttributeValue(i));
	            attributeMap.put(xml.getAttributeName(i), xml.getAttributeValue(i));
	        }
	    }

	    return attributeMap;
	}
}
