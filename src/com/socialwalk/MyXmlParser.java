package com.socialwalk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.socialwalk.WalkHistory.WalkLogItem;

public class MyXmlParser
{
	private static final String TAG = "SW-PARSER";
	private String m_xml;
	
	public MyXmlParser(String strXml)
	{
		m_xml = strXml;
	}
	
	public MyXmlParser(File xmlFile)
	{
		StringBuilder text = new StringBuilder();

		try
		{
		    BufferedReader br = new BufferedReader(new FileReader(xmlFile));
		    String line;

		    while (null != (line = br.readLine()))
		    {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		    
		    m_xml = text.toString();
		}
		catch (IOException e)
		{
			Log.d(TAG, e.getLocalizedMessage() + ", " + xmlFile.getPath());
		}
	}
	
	public SlideAdData GetAdData()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			SlideAdData adData = null;
			
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String tagName = null;
				
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.END_DOCUMENT:						
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("Listing"))
						adData = new SlideAdData();
					else if (tagName.equalsIgnoreCase("ClickUrl"))
						adData.TargetUrl = parser.nextText();
					else if (tagName.equalsIgnoreCase("thumbimage"))
						adData.ThumbnailUrl = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					break;
				}					
				eventType = parser.next();
			}
			
			return adData;
			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}

	}

	public WalkHistory GetWalkHistory()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			WalkHistory history = null;
			WalkLogItem logItem = null;
			
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String tagName = null;
				
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.END_DOCUMENT:						
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("walking"))
						history = new WalkHistory();
					else if (tagName.equalsIgnoreCase("startTime"))
						history.StartTime = dateFromXmlString(parser.nextText());
					else if (tagName.equalsIgnoreCase("endTime"))
						history.EndTime = dateFromXmlString(parser.nextText());
					else if (tagName.equalsIgnoreCase("location"))
						logItem = history.new WalkLogItem();
					else if (tagName.equalsIgnoreCase("date"))
						logItem.LogTime = dateFromXmlString(parser.nextText());
					else if (tagName.equalsIgnoreCase("isValid"))
						logItem.IsValid = Boolean.parseBoolean(parser.nextText());
					else if (tagName.equalsIgnoreCase("latitude"))
					{
						String text = parser.nextText();
						double latitude = Double.parseDouble(text);
						logItem.LogLocation.setLatitude(latitude);
//						logItem.LogLocation.setLatitude(Double.parseDouble(parser.nextText()));
					}
					else if (tagName.equalsIgnoreCase("longitude"))
						logItem.LogLocation.setLongitude(Double.parseDouble(parser.nextText()));
					else if (tagName.equalsIgnoreCase("altitude"))
						logItem.LogLocation.setAltitude(Double.parseDouble(parser.nextText()));
					else if (tagName.equalsIgnoreCase("provider"))
						logItem.LogLocation.setProvider(parser.nextText());
					else if (tagName.equalsIgnoreCase("accuracy"))
						logItem.LogLocation.setAccuracy(Float.parseFloat(parser.nextText()));
					
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("location"))
						history.LogItems.add(logItem);
					else if (tagName.equalsIgnoreCase("walking"))
						history.ReCalculateDistance();
					break;
				}					
				eventType = parser.next();
			}
			return history;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	private Date dateFromXmlString(String xmlText)
	{
		SimpleDateFormat  format = new SimpleDateFormat(Globals.XML_DATE_FORMAT, Locale.US);  
		try
		{
		    Date parseDate = format.parse(xmlText);
		    return parseDate;
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		    return null;
		}
	}
	
	public Vector<AroundersItem> GetArounders()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			Vector<AroundersItem> aroundersItems = null;
			AroundersItem aroundersItem = null;
			
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String tagName = null;
				
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.END_DOCUMENT:						
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("inventories"))
						aroundersItems = new Vector<AroundersItem>();
					else if (tagName.equalsIgnoreCase("inventory"))
						aroundersItem = new AroundersItem();
					else if (tagName.equalsIgnoreCase("company"))
						aroundersItem.Company = parser.nextText();
					else if (tagName.equalsIgnoreCase("promotion"))
						aroundersItem.Promotion = parser.nextText();
					else if (tagName.equalsIgnoreCase("icon"))
						aroundersItem.IconURL = parser.nextText();
					else if (tagName.equalsIgnoreCase("banner"))
						aroundersItem.BannerURL = parser.nextText();
					else if (tagName.equalsIgnoreCase("url"))
						aroundersItem.TargetURL = parser.nextText();
					else if (tagName.equalsIgnoreCase("distance"))
						aroundersItem.Distance = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("latitude"))
						aroundersItem.Latitude = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("longitude"))
						aroundersItem.Longitude = Double.parseDouble(parser.nextText());
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("inventory"))
					{
						if (null != aroundersItems && null != aroundersItem)
						{
							aroundersItems.add(aroundersItem);
							aroundersItem = null;
						}
					}
					else if (tagName.equalsIgnoreCase("inventories"))
					{
						
					}
					break;
				}					
				eventType = parser.next();
			}
			
			return aroundersItems;
			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}

	}

	
	
	public SWResponse GetResponse()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			SWResponse response = null;
			
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				String tagName = null;
				
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.END_DOCUMENT:						
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("errors"))
						response = new SWResponse();
					else if (tagName.equalsIgnoreCase("code"))
						response.CodeBlock = parser.nextText();
					else if (tagName.equalsIgnoreCase("message"))
						response.Message = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("errors"))
					{
						int len = response.CodeBlock.length();
						if (2 <= len)
							response.Code = Integer.parseInt(response.CodeBlock.substring(len - 2, len));
						
					}
						
					break;
				}					
				eventType = parser.next();
			}
			
			return response;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	
	

	public class SlideAdData
	{
		public String TargetUrl;
		public String ThumbnailUrl;		
		public Date LastAccess;
		
		public SlideAdData()
		{
			LastAccess = new Date();
		}
	}	
	
	public class SWResponse
	{
		public String CodeBlock;
		public String Message;
		public int Code;
		
		public SWResponse()
		{
			this.Code = -1;
		}
	}
	
	public class AroundersItem
	{
		public String Company;
		public String Promotion;
		public String IconURL;
		public String BannerURL;
		public String TargetURL;
		public int Distance;
		public double Latitude;
		public double Longitude;
	}
}
