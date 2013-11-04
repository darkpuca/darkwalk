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

import com.socialwalk.dataclass.AccountData;
import com.socialwalk.dataclass.AccountHeart;
import com.socialwalk.dataclass.AreaItem;
import com.socialwalk.dataclass.AroundersItems;
import com.socialwalk.dataclass.AroundersItems.AroundersItem;
import com.socialwalk.dataclass.Beneficiaries;
import com.socialwalk.dataclass.Beneficiary;
import com.socialwalk.dataclass.BeneficiarySummary;
import com.socialwalk.dataclass.Communities;
import com.socialwalk.dataclass.Community;
import com.socialwalk.dataclass.CommunityDetail;
import com.socialwalk.dataclass.CommunityMembers;
import com.socialwalk.dataclass.CommunityPostReplies;
import com.socialwalk.dataclass.CommunityMembers.CommunityMemberItem;
import com.socialwalk.dataclass.CommunityPostReplies.CommunityPostReply;
import com.socialwalk.dataclass.CommunityPosts;
import com.socialwalk.dataclass.CommunityPosts.CommunityPostItem;
import com.socialwalk.dataclass.HeartRanks;
import com.socialwalk.dataclass.HeartRanks.RankItem;
import com.socialwalk.dataclass.NeoClickItems;
import com.socialwalk.dataclass.NeoClickItems.NeoClickItem;
import com.socialwalk.dataclass.WalkHistory;
import com.socialwalk.dataclass.WalkHistory.WalkLogItem;

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
	
	public Vector<AreaItem> GetAreaItems()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			Vector<AreaItem> items = null;
			AreaItem item = null;
			
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
					if (tagName.equalsIgnoreCase("items"))
						items = new Vector<AreaItem>();
					else if (tagName.equalsIgnoreCase("item"))
						item = new AreaItem();
					else if (tagName.equalsIgnoreCase("first_code"))
						item.ParentCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("second_code"))
						item.Code = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("status_code"))
						item.Status = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("first_name"))
						item.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("second_name"))
						item.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("reg_date"))
						item.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("modify_date"))
						item.ModifyDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("item"))
					{
						if (null != items && null != item)
						{
							if (0 < item.ParentCode && 0 == item.Code)
							{
								item.Code = item.ParentCode;
								item.ParentCode = 0;
							}
							
							items.add(item);
							item = null;
						}
					}
					else if (tagName.equalsIgnoreCase("items"))
					{
						return items;
					}
					break;
				}					
				eventType = parser.next();
			}
			return null;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
		
	}
	
	public NeoClickItems GetNeoClickItems()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			NeoClickItems items = null;
			NeoClickItem item = null;
			
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
					if (tagName.equalsIgnoreCase("Results"))
						items = new NeoClickItems();
					else if (tagName.equalsIgnoreCase("Listing"))
						item = items.new NeoClickItem();
					else if (tagName.equalsIgnoreCase("AdId"))
						item.Sequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("ClickUrl"))
						item.TargetUrl = parser.nextText();
					else if (tagName.equalsIgnoreCase("thumbimage"))
						item.ThumbnailUrl = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("Results"))
						return items;
					else if (tagName.equalsIgnoreCase("Listing"))
						items.Items.add(item);
					break;
				}					
				eventType = parser.next();
			}
			
			return null;
			
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
						history.StartTime = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_HISTORY);
					else if (tagName.equalsIgnoreCase("endTime"))
						history.EndTime = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_HISTORY);
					else if (tagName.equalsIgnoreCase("weight"))
						history.setWeight(Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("heartRatio"))
						history.setHeartStepDistance(Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("location"))
						logItem = history.new WalkLogItem();
					else if (tagName.equalsIgnoreCase("date"))
						logItem.LogTime = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_HISTORY);
					else if (tagName.equalsIgnoreCase("isValid"))
						logItem.IsValid = Boolean.parseBoolean(parser.nextText());
					else if (tagName.equalsIgnoreCase("latitude"))
						logItem.LogLocation.setLatitude(Double.parseDouble(parser.nextText()));
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
						history.ReCalculate();
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

	private Date dateFromString(String xmlText, String strFormat)
	{
		if (null == xmlText || 0 == xmlText.length()) return null;
		
		SimpleDateFormat  format = new SimpleDateFormat(strFormat, Locale.US);  
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
	
	public AccountData GetAccountData()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			AccountData acc = null;
			
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
					if (tagName.equalsIgnoreCase("users"))
						acc = new AccountData();
					else if (tagName.equalsIgnoreCase("user_seq"))
						acc.Sequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("user_id"))
						acc.UserId = parser.nextText();
					else if (tagName.equalsIgnoreCase("user_pw"))
						acc.Password = parser.nextText();
					else if (tagName.equalsIgnoreCase("realname"))
						acc.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("user_type"))
						acc.UserType = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("first_code"))
						acc.AreaCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("second_code"))
						acc.AreaSubCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("gender"))
						acc.Gender = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("birth_date"))
						acc.Birthday = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("weight"))
						acc.Weight = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("recommender_seq"))
						acc.RecommendedUserId = parser.nextText();
					else if (tagName.equalsIgnoreCase("community_id"))
					{
						String val = parser.nextText();
						if (0 == val.length())
							acc.CommunitySeq = 0;
						else
							acc.CommunitySeq = Integer.parseInt(val);
					}
					else if (tagName.equalsIgnoreCase("verified"))
						acc.Verified = (1 == Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("reg_date"))
						acc.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("modify_date"))
						acc.ModifyDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("first_name"))
						acc.AreaName = parser.nextText();
					else if (tagName.equalsIgnoreCase("second_name"))
						acc.AreaSubName = parser.nextText();
					else if (tagName.equalsIgnoreCase("community_name"))
						acc.CommunityName = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					break;
				}					
				eventType = parser.next();
			}
			
			return acc;
			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	public Communities GetCommunities()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			Communities commItems = null;
			Community item = null;
			
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
					if (tagName.equalsIgnoreCase("community"))
						commItems = new Communities();
					else if (tagName.equalsIgnoreCase("total"))
						commItems.TotalCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("item"))
						item = new Community();
					else if (tagName.equalsIgnoreCase("community_id"))
						item.Sequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_type"))
						item.Type = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("owner_id"))
						item.OwnerSequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_name"))
						item.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("approve_type"))
						item.SignupType = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_desc"))
						item.Description = parser.nextText();
					else if (tagName.equalsIgnoreCase("verified"))
						item.VerifyCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("opening_seq"))
						item.MakerSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("opening_name"))
						item.MakerName = parser.nextText();
					else if (tagName.equalsIgnoreCase("operator_seq"))
						item.MasterSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("operator_name"))
						item.MasterName = parser.nextText();
					else if (tagName.equalsIgnoreCase("reg_date"))
						item.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("modify_date"))
						item.ModifyDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("item"))
						commItems.Items.add(item);
					break;
				}					
				eventType = parser.next();
			}
			
			return commItems;
			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}

	}
	
	public CommunityDetail GetCommunityDetail()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			CommunityDetail detail = null;
			
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
					if (tagName.equalsIgnoreCase("community"))
						detail = new CommunityDetail();
					else if (tagName.equalsIgnoreCase("community_id"))
						detail.Id = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_type"))
						detail.Type = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("owner_id"))
						detail.OwnerId = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_name"))
						detail.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("approve_type"))
						detail.SignupType = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_desc"))
						detail.Description = parser.nextText();
					else if (tagName.equalsIgnoreCase("owner_first_name"))
						detail.AreaName = parser.nextText();
					else if (tagName.equalsIgnoreCase("owner_second_name"))
						detail.AreaSubName = parser.nextText();
					else if (tagName.equalsIgnoreCase("opening_seq"))
						detail.MakerSequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("opening_name"))
						detail.MakerName = parser.nextText();
					else if (tagName.equalsIgnoreCase("operator_seq"))
						detail.Mastersequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("operator_name"))
						detail.MasterName = parser.nextText();
					else if (tagName.equalsIgnoreCase("community_member_count"))
						detail.MemberCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("reg_date"))
						detail.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("modify_date"))
						detail.ModifyDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					break;
				case XmlPullParser.END_TAG:
					break;
				}					
				eventType = parser.next();
			}			
			return detail;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	public CommunityPosts GetCommunityPosts()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			CommunityPosts items = null;
			CommunityPostItem item = null;
			
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
					if (tagName.equalsIgnoreCase("community_info"))
						items = new CommunityPosts();
					else if (tagName.equalsIgnoreCase("total"))
						items.TotalCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("item"))
						item = items.new CommunityPostItem();
					else if (tagName.equalsIgnoreCase("community_info_seq"))
						item.Sequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_user_seq"))
						item.UserSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("content"))
						item.Contents = parser.nextText();
					else if (tagName.equalsIgnoreCase("attach_image"))
						item.ImageUrl = parser.nextText();
					else if (tagName.equalsIgnoreCase("view_count"))
						item.ViewCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("reply_count"))
						item.ReplyCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("verified"))
						item.Verified = (boolean)(1 == Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("reg_date"))
						item.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("modify_date"))
						item.ModifyDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("community_user_realname"))
						item.Writer = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("item"))
						items.Items.add(item);
					else if (tagName.equalsIgnoreCase("community_info"))
						return items;
						
					break;
				}					
				eventType = parser.next();
			}
			return null;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	public CommunityPostItem GetCommunityPostItem()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			CommunityPosts items = new CommunityPosts();
			CommunityPostItem item = null;
			
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
					if (tagName.equalsIgnoreCase("community_info"))
						item = items.new CommunityPostItem();
					else if (tagName.equalsIgnoreCase("community_info_seq"))
						item.Sequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("community_user_seq"))
						item.UserSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("community_user_realname"))
						item.Writer = parser.nextText();
					else if (tagName.equalsIgnoreCase("content"))
						item.Contents = parser.nextText();
					else if (tagName.equalsIgnoreCase("attach_image"))
						item.ImageUrl = parser.nextText();
					else if (tagName.equalsIgnoreCase("view_count"))
						item.ViewCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("reply_count"))
						item.ReplyCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("reg_date"))
						item.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("modify_date"))
						item.ModifyDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("community_info"))
						return item;
						
					break;
				}					
				eventType = parser.next();
			}
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
		}
		return null;
	}

	public CommunityPostReplies GetCommunityReplies()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			CommunityPostReplies items = null;
			CommunityPostReply item = null;
			
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
					if (tagName.equalsIgnoreCase("reply_info"))
						items = new CommunityPostReplies();
					else if (tagName.equalsIgnoreCase("total"))
						items.TotalCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("item"))
						item = items.new CommunityPostReply();
					else if (tagName.equalsIgnoreCase("reply_seq"))
						item.Sequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("reply_user_seq"))
						item.UserSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("reply_realname"))
						item.Writer = parser.nextText();
					else if (tagName.equalsIgnoreCase("reply_data"))
						item.Contents = parser.nextText();
					else if (tagName.equalsIgnoreCase("reg_date"))
						item.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("item"))
						items.Items.add(item);
					else if (tagName.equalsIgnoreCase("reply_info"))
						return items;
						
					break;
				}					
				eventType = parser.next();
			}
			return null;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	public BeneficiarySummary GetBeneficiarySummary()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			BeneficiarySummary summary = null;
			
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
					if (tagName.equalsIgnoreCase("benefit_group"))
						summary = new BeneficiarySummary();
					else if (tagName.equalsIgnoreCase("benefit_price"))
						summary.TargetMoney = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("accumuler_price"))
						summary.CurrentMoney = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("first_code"))
						summary.AreaCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("second_code"))
						summary.AreaSubCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("start_date"))
						summary.StartDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("end_date"))
						summary.EndDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("reg_date"))
						summary.RegDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("participant"))
						summary.Sequence = Integer.parseInt(parser.nextText());
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("benefit_group"))
						return summary;
					break;
				}					
				eventType = parser.next();
			}			
			return null;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	
	public Beneficiary GetBeneficiary()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			Beneficiary item = null;
			
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
					if (tagName.equalsIgnoreCase("benefit"))
						item = new Beneficiary();
					else if (tagName.equalsIgnoreCase("benefit_seq"))
						item.Sequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_group_seq"))
						item.GroupSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_type"))
						item.Type = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("first_code"))
						item.AreaCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("first_name"))
						item.AreaName = parser.nextText();
					else if (tagName.equalsIgnoreCase("second_code"))
						item.AreaSubCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("second_name"))
						item.AreaSubName = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_name"))
						item.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_age"))
						item.Age = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("benefit_gender"))
						item.Gender = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("profile_image"))
						item.ProfileUrl = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_desc"))
						item.Description = parser.nextText();
					else if (tagName.equalsIgnoreCase("target_price"))
						item.TargetMoney = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("current_price"))
						item.CurrentMoney = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("participant"))
						item.Participants = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("start_date"))
						item.StartDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("end_date"))
						item.EndDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("benefit_date"))
						item.EndDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("delivery_desc"))
						item.ReviewsUrl = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("benefit"))
						return item;
					break;
				}					
				eventType = parser.next();
			}			
			return null;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	
	public Beneficiaries GetBeneficiaries()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			Beneficiaries beneficiaries = null;
			Beneficiary item = null;
			
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
					if (tagName.equalsIgnoreCase("benefit"))
						beneficiaries = new Beneficiaries();
					else if (tagName.equalsIgnoreCase("total"))
						beneficiaries.TotalCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("item"))
						item = new Beneficiary();
					else if (tagName.equalsIgnoreCase("benefit_seq"))
						item.Sequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_group_seq"))
						item.GroupSequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("benefit_name"))
						item.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("first_code"))
						item.AreaCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("second_code"))
						item.AreaSubCode = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("target_price"))
						item.TargetMoney = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("start_date"))
						item.StartDate = dateFromString(parser.nextText(), Globals.DATETIME_FORMAT_FOR_SERVER);
					else if (tagName.equalsIgnoreCase("first_name"))
						item.AreaName = parser.nextText();
					else if (tagName.equalsIgnoreCase("second_name"))
						item.AreaSubName = parser.nextText();
					else if (tagName.equalsIgnoreCase("status_code"))
						item.InProgress = (1 == Integer.parseInt(parser.nextText()));
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("item"))
						beneficiaries.Items.add(item);
					else if (tagName.equalsIgnoreCase("benefit"))
						return beneficiaries;
					break;
				}					
				eventType = parser.next();
			}			
			return null;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	public AroundersItems GetArounders()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			AroundersItems items = null;
			AroundersItem item = null;
			
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
						items = new AroundersItems();
					else if (tagName.equalsIgnoreCase("inventory"))
						item = items.new AroundersItem();
					else if (tagName.equalsIgnoreCase("company"))
						item.Company = parser.nextText();
					else if (tagName.equalsIgnoreCase("promotion"))
						item.Promotion = parser.nextText();
					else if (tagName.equalsIgnoreCase("icon"))
						item.IconURL = parser.nextText();
					else if (tagName.equalsIgnoreCase("banner"))
						item.BannerURL = parser.nextText();
					else if (tagName.equalsIgnoreCase("url"))
						item.setTargetURL(parser.nextText());
					else if (tagName.equalsIgnoreCase("distance"))
						item.Distance = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("latitude"))
						item.Latitude = Double.parseDouble(parser.nextText());
					else if (tagName.equalsIgnoreCase("longitude"))
						item.Longitude = Double.parseDouble(parser.nextText());
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("inventory"))
					{
						if (null != items && null != item)
						{
							items.Items.add(item);
							item = null;
						}
					}
					else if (tagName.equalsIgnoreCase("inventories"))
					{
						return items;
					}
					break;
				}					
				eventType = parser.next();
			}
			return null;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	
	public HeartRanks GetRanks()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			HeartRanks ranks = null;
			RankItem item = null;
			
			int parseTargetType = 0;
			
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
					if (tagName.equalsIgnoreCase("rank"))
						ranks = new HeartRanks();
					else if (tagName.equalsIgnoreCase("myRank"))
						parseTargetType = 0;
					else if (tagName.equalsIgnoreCase("privateRank"))
						parseTargetType = 1;
					else if (tagName.equalsIgnoreCase("groupTotalRank"))
						parseTargetType = 2;
					else if (tagName.equalsIgnoreCase("groupAvgRank"))
						parseTargetType = 3;
					else if (tagName.equalsIgnoreCase("item"))
						item = ranks.new RankItem();
					else if (tagName.equalsIgnoreCase("user_point"))
					{
						if (0 != parseTargetType)
							item.UserPoint = Integer.parseInt(parser.nextText());
					}
					else if (tagName.equalsIgnoreCase("group_point"))
					{
						if (0 != parseTargetType)
							item.GroupPoint = Integer.parseInt(parser.nextText());
					}
					else if (tagName.equalsIgnoreCase("member_count"))
					{
						if (0 != parseTargetType)
							item.Members = Integer.parseInt(parser.nextText());
					}
					else if (tagName.equalsIgnoreCase("user_average"))
					{
						if (0 != parseTargetType)
							item.AveragePoints = Integer.parseInt(parser.nextText());
					}
					else if (tagName.equalsIgnoreCase("realname"))
					{
						if (0 != parseTargetType)
							item.Name = parser.nextText();
					}
					else if (tagName.equalsIgnoreCase("community_name"))
					{
						if (0 != parseTargetType)
							item.CommunityName = parser.nextText();
					}
					else if (tagName.equalsIgnoreCase("second_name"))
					{
						if (0 != parseTargetType)
							item.LocalName = parser.nextText();
					}
					
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("rank"))
						return ranks;
					else if (tagName.equalsIgnoreCase("item"))
					{
						 if (1 == parseTargetType)
							ranks.PersonalItems.add(item);
						 else if (2 == parseTargetType)
							 ranks.GroupSumItems.add(item);
						 else if (3 == parseTargetType)
							 ranks.GroupAvgItems.add(item);
					}

					break;
				}					
				eventType = parser.next();
			}
			return null;
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	public CommunityMembers GetCommunityMembers()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			CommunityMembers members = null;
			CommunityMemberItem item = null;
			
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
					if (tagName.equalsIgnoreCase("community_member"))
						members = new CommunityMembers();
					else if (tagName.equalsIgnoreCase("total"))
						members.TotalCount = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("item"))
						item = members.new CommunityMemberItem();
					else if (tagName.equalsIgnoreCase("member_seq"))
						item.MemberSequence = Integer.parseInt(parser.nextText());
					else if (tagName.equalsIgnoreCase("user_seq"))
						item.Sequence = parser.nextText();
					else if (tagName.equalsIgnoreCase("realname"))
						item.Name = parser.nextText();
					else if (tagName.equalsIgnoreCase("user_id"))
						item.Email = parser.nextText();
					else if (tagName.equalsIgnoreCase("req_message"))
						item.Message = parser.nextText();
					else if (tagName.equalsIgnoreCase("verified"))
						item.IsAllowed = (1 == Integer.parseInt(parser.nextText()));
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equalsIgnoreCase("item"))
						members.Items.add(item);
					else if (tagName.equalsIgnoreCase("community_member"))
						return members;
					break;
				}					
				eventType = parser.next();
			}			
			return null;			
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

	public int GetReturnId()
	{
		if (null == m_xml || 0 == m_xml.length()) return -1;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
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
					if (tagName.equalsIgnoreCase("community_id"))
						return Integer.parseInt(parser.nextText());
					break;
				case XmlPullParser.END_TAG:
					break;
				}					
				eventType = parser.next();
			}
			
			return -1;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return -1;
		}
	}
	
	public AccountHeart GetHearts()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			AccountHeart hearts = null;
			
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
					if (tagName.equalsIgnoreCase("users"))
						hearts = new AccountHeart();
					else if (tagName.equalsIgnoreCase("green_heart"))
					{
						String points = parser.nextText();
						if (0 == points.length())
							hearts.setGreenPoint(0);
						else
							hearts.setGreenPoint(Integer.parseInt(points));
					}
					else if (tagName.equalsIgnoreCase("total"))
						hearts.setRedPointTotal(Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("walk"))
						hearts.setRedPointByWalk(Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("touch"))
						hearts.setRedPointByTouch(Integer.parseInt(parser.nextText()));
					break;
				case XmlPullParser.END_TAG:
					break;
				}					
				eventType = parser.next();
			}
			
			return hearts;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	public AccountHeart GetAccumulatedHearts()
	{
		if (null == m_xml || 0 == m_xml.length()) return null;
		
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(m_xml));
			
			AccountHeart hearts = null;
			
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
					if (tagName.equalsIgnoreCase("users"))
						hearts = new AccountHeart();
					else if (tagName.equalsIgnoreCase("red_total"))
						hearts.setRedPointTotal(Integer.parseInt(parser.nextText()));
					else if (tagName.equalsIgnoreCase("green_total"))
						hearts.setGreenPoint(Integer.parseInt(parser.nextText()));
					break;
				case XmlPullParser.END_TAG:
					break;
				}					
				eventType = parser.next();
			}
			
			return hearts;			
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getLocalizedMessage());
			return null;
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
}
