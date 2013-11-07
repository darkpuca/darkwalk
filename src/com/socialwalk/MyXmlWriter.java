package com.socialwalk;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.socialwalk.dataclass.AccountData;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistory.WalkLogItem;

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

	public static String ReplyPosting(String userSequence, int postSequence, String contents)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "reply_info");
			serializer.startTag("", "reply_user_seq");
			serializer.text(userSequence);
			serializer.endTag("", "reply_user_seq");
			serializer.startTag("", "reply_data");
			serializer.text(contents);
			serializer.endTag("", "reply_data");
			serializer.endTag("", "reply_info");
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

	public static String CommunityJoin(String userSequence, String message)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "community_member");
			serializer.startTag("", "user_seq");
			serializer.text(userSequence);
			serializer.endTag("", "user_seq");
			serializer.startTag("", "req_message");
			serializer.text(message);
			serializer.endTag("", "req_message");
			serializer.endTag("", "community_member");
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
	
	public static String UpdateProfile(AccountData profile)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "users");
			serializer.startTag("", "realname");
			serializer.text(profile.Name);
			serializer.endTag("", "realname");
			serializer.startTag("", "first_code");
			serializer.text(Integer.toString(profile.AreaCode));
			serializer.endTag("", "first_code");
			serializer.startTag("", "second_code");
			serializer.text(Integer.toString(profile.AreaSubCode));
			serializer.endTag("", "second_code");
			serializer.startTag("", "gender");
			serializer.text(Integer.toString(profile.Gender));
			serializer.endTag("", "gender");
			serializer.startTag("", "birth_date");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			serializer.text(formatter.format(profile.Birthday));
			serializer.endTag("", "birth_date");
			serializer.startTag("", "weight");
			serializer.text(Integer.toString(profile.Weight));
			serializer.endTag("", "weight");
			serializer.endTag("", "users");
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
	
	public static String CommunityMemberAllow(String memberSequence, int allowCode)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "community_member");
			serializer.startTag("", "item");
			serializer.startTag("", "user_seq");
			serializer.text(memberSequence);
			serializer.endTag("", "user_seq");
			serializer.startTag("", "verified");
			serializer.text(Integer.toString(allowCode));
			serializer.endTag("", "verified");
			serializer.endTag("", "item");
			serializer.endTag("", "community_member");
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

	
	public static String AccumulateHeart(String userSequence, String adSequence, int point)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "ad_info");
			serializer.startTag("", "user_seq");
			serializer.text(userSequence);
			serializer.endTag("", "user_seq");
			serializer.startTag("", "ad_seq");
			serializer.text(adSequence);
			serializer.endTag("", "ad_seq");
			serializer.startTag("", "heart");
			serializer.text(Integer.toString(point));
			serializer.endTag("", "heart");
			serializer.endTag("", "ad_info");
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
	
	public static String WalkResult(String userSequence, WalkHistory history)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", "request");
			serializer.startTag("", "benefit_history");
			serializer.startTag("", "user_seq");
			serializer.text(userSequence);
			serializer.endTag("", "user_seq");
			serializer.startTag("", "benefit_point");
			serializer.text(Long.toString(history.RedHearts()));
			serializer.endTag("", "benefit_point");
			serializer.startTag("", "benefit_info");
			serializer.startTag("", "time");
			serializer.text(history.TotalWalkingTimeString());
			serializer.endTag("", "time");
			serializer.startTag("", "distance");
			serializer.text(history.TotalDistanceString());
			serializer.endTag("", "distance");
			serializer.startTag("", "calorie");
			serializer.text(history.TotalCalories());
			serializer.endTag("", "calorie");
			serializer.endTag("", "benefit_info");
			serializer.startTag("", "saving_type");
			serializer.text(Integer.toString(0));
			serializer.endTag("", "saving_type");
			serializer.endTag("", "benefit_history");
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

	public static String GetWalkingDataXML(WalkHistory log)
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			SimpleDateFormat dateFormatter = new SimpleDateFormat(Globals.DATETIME_FORMAT_FOR_HISTORY, Locale.US);
			
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
			
			serializer.startTag("", "weight");
			serializer.text(Integer.toString(log.getWeight()));
			serializer.endTag("", "weight");

			serializer.startTag("", "heartRatio");
			serializer.text(Integer.toString(log.getHeartStepDistance()));
			serializer.endTag("", "heartRatio");

			serializer.startTag("", "adTouchCount");
			serializer.text(Integer.toString(log.getAdTouchCount()));
			serializer.endTag("", "adTouchCount");
			
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
			System.out.println(e.getLocalizedMessage());
		}
		
		return "";
	}
}
