package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckInspStatus extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Screen Fields
		VtiUserExitScreenField scrInsp = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrInsp1 = getScreenField("STATUS1");
		VtiUserExitScreenField scrInsp2 = getScreenField("STATUS2");
		VtiUserExitScreenField scrInsp3 = getScreenField("STATUS3");
		VtiUserExitScreenField scrInsp4 = getScreenField("STATUS4");
		VtiUserExitScreenField scrPref = getScreenField("PREF");
		VtiUserExitScreenField scrPO = getScreenField("EBELN");
		
		//Screenfield Validation
		if (scrInsp == null) return new VtiUserExitResult(999, "Unable to initialise screen field INSPSTATUS.");
		if (scrInsp1 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS1.");
		if (scrInsp2 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS2.");
		if (scrInsp3 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS3.");
		if (scrInsp4 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS4.");
		if (scrRegNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field REGNO.");
		if (scrPref == null) return new VtiUserExitResult(999, "Unable to initialise screen field PREF.");
		if (scrPO == null) return new VtiUserExitResult(999, "Unable to initialise screen field EBELN.");
		if (scrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field TIME.");
		
		
			if(scrInsp1.getFieldValue().equalsIgnoreCase("NONE") || scrInsp2.getFieldValue().equalsIgnoreCase("NONE")
			  || scrInsp3.getFieldValue().equalsIgnoreCase("NONE") || scrInsp4.getFieldValue().equalsIgnoreCase("NONE"))
				if(scrRegNo.getFieldValue().length() != 0)
					return new VtiUserExitResult(500, "No inspection was performed.");
			
			if(scrInsp1.getFieldValue().equalsIgnoreCase("PASSED") && scrInsp2.getFieldValue().equalsIgnoreCase("PASSED")
			   && scrInsp3.getFieldValue().equalsIgnoreCase("PASSED") && scrInsp4.getFieldValue().equalsIgnoreCase("PASSED"))
				{
					scrInsp.setFieldValue("P");
				}
				else
				{
					scrInsp.setFieldValue("F");
				}
			
			if(scrPO.getFieldValue().length() > 0)
				scrPref.setDisplayOnlyFlag(false);
			else
			{
				scrPref.setDisplayOnlyFlag(true);
				scrPref.setFieldValue("");
			}
			
		Date currNow = new Date();
		
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		scrTime.setFieldValue(currTime);
		return new VtiUserExitResult();
	}
}
