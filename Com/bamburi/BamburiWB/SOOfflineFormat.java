package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SOOfflineFormat extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrBtnSave = getScreenField("BTNSAVE");
		VtiUserExitScreenField scrSalesOrd = getScreenField("VBELN");
		if(scrSalesOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		
		long offVbeln = 0;
		
			try
			{
				offVbeln = getNextNumberFromNumberRange("YSWB_OFFSO");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next Slip No.",ee);
				return new VtiUserExitResult(999,"Unable to generate offline Sales Order.");
			}
		
			scrSalesOrd.setFieldValue(Long.toString(offVbeln));
			scrBtnSave.setHiddenFlag(true);
			
		return new VtiUserExitResult();
	}
}