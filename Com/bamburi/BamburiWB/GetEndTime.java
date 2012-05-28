package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetEndTime extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		Date now = new Date();
		String currTime = DateFormatter.format("HH:mm:ss", now);
		
		//Get Screen Elements
		VtiUserExitScreenField scrFETime = getScreenField("ENDTIME");
		
		if(scrFETime == null) return new VtiUserExitResult (999,"Failed to initialise ENDTIME.");
		
		if(scrFETime.getFieldValue().length() > 0)
			return new VtiUserExitResult (999,"End time already recorded.");
		
		scrFETime.setFieldValue(currTime);

		return new VtiUserExitResult();
	
	}
}
