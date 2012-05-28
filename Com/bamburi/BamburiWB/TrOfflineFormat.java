package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class TrOfflineFormat extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		VtiUserExitScreenField scrPurchOrd = getScreenField("EBELN");
		if(scrPurchOrd == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		
		long offEbeln = 0;
		
			try
			{
				offEbeln = getNextNumberFromNumberRange("YSWB_OFFSTO");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next Slip No.",ee);
				return new VtiUserExitResult(999,"Unable to generate offline Transfer Order.");
			}
		
			scrPurchOrd.setFieldValue(Long.toString(offEbeln));
			
		return new VtiUserExitResult();
			
	}
}