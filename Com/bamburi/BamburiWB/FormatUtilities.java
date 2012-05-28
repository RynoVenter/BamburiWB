package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatUtilities
{/*Perform general formating using the utilities in this class.
	*/
	
		int yr;
		int mon;
		int dy;
		int hr;
		int min;
		int sec;
		Calendar calendar;
		VtiUserExit vUE;
		
	public String shortDate (String rDate)
	{
		String year = rDate.substring(0,4);
		String month = rDate.substring(4,6);
		String day = rDate.substring(rDate.length()-2,rDate.length());
		
		String sDate = day + "/" + month + "/" + year;
		
		return sDate;
	}
	
	public String shortTime (String rTime)
	{
		/*added post go live*/
		if(rTime.length() == 5)
			rTime = "0" + rTime;
		if(rTime.length() == 4)
			rTime = "00" + rTime;
		if(rTime.length() == 3)
			rTime = "000" + rTime;
		if(rTime.length() == 2)
			rTime ="0000" + rTime;
		if(rTime.length() == 1)
			rTime = "00000" + rTime;
		if(rTime.length() == 0)
			rTime = "000000";
		
			//
								
		String hour = rTime.substring(0,2);
		String minute = rTime.substring(2,4);
		String sec = rTime.substring(4,rTime.length());
		
		String sTime = hour + ":" + minute + ":" + sec;
		
		return sTime;
	}
	
	public Calendar setCalendar()//yyyymmdd, HHmmss
	{
		
		Calendar myCalendar = Calendar.getInstance();
			
		yr = myCalendar.get(Calendar.YEAR);
		mon  = myCalendar.get(Calendar.MONTH);
		dy = myCalendar.get(Calendar.DAY_OF_MONTH);
		
		hr = myCalendar.get(Calendar.HOUR_OF_DAY);
		min = myCalendar.get(Calendar.MINUTE);
		sec = myCalendar.get(Calendar.SECOND);
		calendar = myCalendar;
		return myCalendar;
	}
	
	public Calendar setCalendar( String skyDate, String skyTime)//yyyymmdd, HHmmss
	{
		if(skyTime.length() == 1)
			skyTime = "00000" + skyTime;
		if(skyTime.length() == 2)
			skyTime = "0000" + skyTime;
		if(skyTime.length() == 3)
			skyTime = "000" + skyTime;
		if(skyTime.length() == 4)
			skyTime = "00" + skyTime;
		if(skyTime.length() == 5)
			skyTime = "0" + skyTime;
		
		vUE.logInfo("skyDate & skyTime is " + skyDate + " & " + skyTime);
		String year = skyDate.substring(0,4);
		yr = Integer.parseInt(year);
		String month = skyDate.substring(4,6);
		mon  =  Integer.parseInt(month);
		String day = skyDate.substring(6,skyDate.length());
		dy = Integer.parseInt(day);
		
		String hour = skyTime.substring(0,2);
		hr = Integer.parseInt(hour);
		String minute = skyTime.substring(2,4);
		min = Integer.parseInt(minute);
		String seconds = skyTime.substring(4,skyTime.length());
		sec = Integer.parseInt(seconds);
		
		Calendar myCalendar = Calendar.getInstance();
		
		myCalendar.set(yr,mon,dy,hr,min,sec);
		calendar = myCalendar;
		return myCalendar;
	}
	public void upDate()
	{
		yr = calendar.get(Calendar.YEAR);
		mon = calendar.get(Calendar.MONTH);
		dy = calendar.get(Calendar.DAY_OF_MONTH);
		hr = calendar.get(Calendar.HOUR_OF_DAY);
		min = calendar.get(Calendar.MINUTE);
		sec = calendar.get(Calendar.SECOND);
	}
	
	public Calendar getCalendar()
	{
		return calendar;
	}
	public int getYear()
	{
		return yr;
	}
	
	public int getMonth()
	{
		return mon;
	}
	
	public int getDay()
	{
		return dy;
	}
	
	public int getHour()
	{
		return hr;
	}

	public int getMin()
	{
		return min;
	}

	public int getSec()
	{
		return sec;
	}
	
}
