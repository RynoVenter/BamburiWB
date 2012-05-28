package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RailCheckInspStatus extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Screen Fields
		VtiUserExitScreenField scrInsp = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrInsp1 = getScreenField("STATUS1");
	
		//Screenfield Validation
		if (scrInsp == null) return new VtiUserExitResult(999, "Unable to initialise screen field INSPSTATUS.");
		if (scrInsp1 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS1.");
		if (scrRegNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field REGNO.");
		if (scrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field TIME.");
		
		
			if(scrInsp1.getFieldValue().equalsIgnoreCase("NONE"))
				if(scrRegNo.getFieldValue().length() != 0)
					return new VtiUserExitResult(500, "No inspection was performed.");
			
			if(scrInsp1.getFieldValue().equalsIgnoreCase("PASSED"))
				{
					scrInsp.setFieldValue("P");
				}
				else
				{
					scrInsp.setFieldValue("F");
				}
			
		Date currNow = new Date();
		
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		scrTime.setFieldValue(currTime);
		return new VtiUserExitResult();
	}
}
