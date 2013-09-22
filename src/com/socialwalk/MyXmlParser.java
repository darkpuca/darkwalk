package com.socialwalk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.location.Location;
import android.location.LocationManager;
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
					if (tagName.equals("Listing"))
						adData = new SlideAdData();
					else if (tagName.equals("ClickUrl"))
						adData.TargetUrl = parser.nextText();
					else if (tagName.equals("thumbimage"))
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
					if (tagName.equals("walking"))
						history = new WalkHistory();
					else if (tagName.equals("startTime"))
						history.StartTime = dateFromXmlString(parser.nextText());
					else if (tagName.equals("endTime"))
						history.EndTime = dateFromXmlString(parser.nextText());
					else if (tagName.equals("location"))
						logItem = history.new WalkLogItem();
					else if (tagName.equals("date"))
						logItem.LogTime = dateFromXmlString(parser.nextText());
					else if (tagName.equals("isValid"))
						logItem.IsValid = Boolean.parseBoolean(parser.nextText());
					else if (tagName.equals("latitude"))
					{
						String text = parser.nextText();
						double latitude = Double.parseDouble(text);
						logItem.LogLocation.setLatitude(latitude);
//						logItem.LogLocation.setLatitude(Double.parseDouble(parser.nextText()));
					}
					else if (tagName.equals("longitude"))
						logItem.LogLocation.setLongitude(Double.parseDouble(parser.nextText()));
					else if (tagName.equals("altitude"))
						logItem.LogLocation.setAltitude(Double.parseDouble(parser.nextText()));
					else if (tagName.equals("provider"))
						logItem.LogLocation.setProvider(parser.nextText());
					else if (tagName.equals("accuracy"))
						logItem.LogLocation.setAccuracy(Float.parseFloat(parser.nextText()));
					
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals("location"))
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
		SimpleDateFormat  format = new SimpleDateFormat(Globals.XML_DATE_FORMAT);  
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
					if (tagName.equals("errors"))
						response = new SWResponse();
					else if (tagName.equals("code"))
						response.Code = parser.nextText();
					else if (tagName.equals("message"))
						response.Message = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
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
		public String Code;
		public String Message;
	}
}
