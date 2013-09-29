package com.socialwalk;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.socialwalk.WalkHistory.WalkLogItem;

public class MyXmlWriter
{
	private static final String TAG = "SW-XML";
	
	public static String Login(String userId, String password)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "login");
			serializer.startTag("", "user_id");
			serializer.text(userId);
			serializer.endTag("", "user_id");
			serializer.startTag("", "user_pw");
			serializer.text(password);
			serializer.endTag("", "user_pw");
			serializer.endTag("", "login");
			serializer.endTag("", "request");
			
			serializer.endDocument();
			
			return writer.toString();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			return "";
		}
	}
	
	public static String CreateGroup(String userSequence, String name, String desc)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "community");
			serializer.startTag("", "user_seq");
			serializer.text(userSequence);
			serializer.endTag("", "user_seq");
			serializer.startTag("", "community_name");
			serializer.text(name);
			serializer.endTag("", "community_name");
			serializer.startTag("", "community_desc");
			serializer.text(desc);
			serializer.endTag("", "community_desc");
			serializer.endTag("", "community");
			serializer.endTag("", "request");
			
			serializer.endDocument();
			
			return writer.toString();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			return "";
		}
	}
	
	public static String CommunityPosting(String userSequence, int communityId, String contents)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "community_info");
			serializer.startTag("", "community_user_seq");
			serializer.text(userSequence);
			serializer.endTag("", "community_user_seq");
			serializer.startTag("", "content");
			serializer.text(contents);
			serializer.endTag("", "content");
			serializer.endTag("", "community_info");
			serializer.endTag("", "request");
			
			serializer.endDocument();
			
			return writer.toString();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			return "";
		}
	}



	public static String ChangePassword(String oldPassword, String newPassword)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "users");
			serializer.startTag("", "old_password");
			serializer.text(oldPassword);
			serializer.endTag("", "old_password");
			serializer.startTag("", "new_password");
			serializer.text(newPassword);
			serializer.endTag("", "new_password");
			serializer.endTag("", "users");
			
			serializer.endDocument();
			
			return writer.toString();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			return "";
		}
	}
	

	public static String GetWalkingDataXML(WalkHistory log)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			SimpleDateFormat dateFormatter = new SimpleDateFormat(Globals.DATE_FORMAT_FOR_HISTORY, Locale.US);
			
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "walking")
			;
			serializer.startTag("", "startTime");
			serializer.text(dateFormatter.format(log.StartTime));
			serializer.endTag("", "startTime");
			
			serializer.startTag("", "endTime");
			serializer.text(dateFormatter.format(log.EndTime));
			serializer.endTag("", "endTime");

			serializer.startTag("", "locations");
			for (WalkLogItem item : log.LogItems)
			{
				serializer.startTag("", "location");
				
				serializer.startTag("", "date");
				serializer.text(dateFormatter.format(item.LogTime));
				serializer.endTag("", "date");
				
				serializer.startTag("", "isValid");
				serializer.text(Boolean.toString(item.IsValid));
				serializer.endTag("", "isValid");
				
				serializer.startTag("", "latitude");
				serializer.text(Double.toString(item.LogLocation.getLatitude()));				
				serializer.endTag("", "latitude");
				
				serializer.startTag("", "longitude");
				serializer.text(Double.toString(item.LogLocation.getLongitude()));
				serializer.endTag("", "longitude");

				serializer.startTag("", "altitude");
				serializer.text(Double.toString(item.LogLocation.getAltitude()));
				serializer.endTag("", "altitude");

				serializer.startTag("", "provider");
				serializer.text(item.LogLocation.getProvider());
				serializer.endTag("", "provider");

				serializer.startTag("", "accuracy");
				serializer.text(Float.toString(item.LogLocation.getAccuracy()));
				serializer.endTag("", "accuracy");

				serializer.endTag("", "location");
			}
			serializer.endTag("", "locations");
			

			serializer.endTag("", "walking");
			
			serializer.endDocument();
			
			return writer.toString();
			
		}
		catch (Exception e)
		{
			
		}
		
		return "";
	}
}
