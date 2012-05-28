package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetStartTime extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		
		Date now = new Date();
		String currTime = DateFormatter.format("HH:mm:ss", now);
		
		//Get Screen Elements
		VtiUserExitScreenField scrFSTime = getScreenField("STARTTIME");
		
		if(scrFSTime == null) return new VtiUserExitResult (999,"Failed to initialise STARTTIME.");
		
		if(scrFSTime.getFieldValue().length() > 0)
			return new VtiUserExitResult (999,"Start time already recorded.");
				
		scrFSTime.setStringFieldValue(currTime);
		
		return new VtiUserExitResult();
	
	}
}
