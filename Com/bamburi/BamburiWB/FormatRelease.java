package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatRelease extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrStatus = getScreenField("STATUS");
		VtiUserExitScreenField btnRelease = getScreenField("BTN_RELEASE");
		VtiUserExitScreenField lblRel = getScreenField("LBLREL");
		VtiUserExitScreenField scrRelUser = getScreenField("REL_USER");
		VtiUserExitScreenField lblAt = getScreenField("LBLAT");
		VtiUserExitScreenField lblDepTime = getScreenField("DEPTIME");
		VtiUserExitScreenField scrArchQDate = getScreenField("QUEUEARCHDATE");
	
		Calendar currNow = Calendar.getInstance();
		currNow.add(Calendar.DAY_OF_MONTH,-5);
		
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow.getTime());
		
		scrArchQDate.setFieldValue(currDate);
			
		if(!scrStatus.getFieldValue().equalsIgnoreCase("COMPLETE"))
		{
			if(scrRelUser.getFieldValue().length() == 0)
			{
				btnRelease.setHiddenFlag(true);
				lblRel.setHiddenFlag(true);
				scrRelUser.setHiddenFlag(true);
				lblAt.setHiddenFlag(true);
				lblDepTime.setHiddenFlag(true);
			}
		}
		else
		{
			if(scrRelUser.getFieldValue().length() == 0)
			{
				lblRel.setHiddenFlag(true);
				scrRelUser.setHiddenFlag(true);
				lblAt.setHiddenFlag(true);
				lblDepTime.setHiddenFlag(true);
			}
		}
		
		if(scrRelUser.getFieldValue().length() > 0)
		{
			btnRelease.setHiddenFlag(true);
		}	
		
		return new VtiUserExitResult();
	}
}
