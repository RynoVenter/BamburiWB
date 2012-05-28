package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ICOfflineFormat extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		VtiUserExitScreenField scrICOrd = getScreenField("VBELN");
		if(scrICOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		
		long offIC = 0;
		
			try
			{
				offIC = getNextNumberFromNumberRange("YSWB_OFFIC");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next Slip No.",ee);
				return new VtiUserExitResult(999,"Unable to generate offline Inter Company Order.");
			}
		
			scrICOrd.setFieldValue(Long.toString(offIC));
			
		return new VtiUserExitResult();
	}
}
