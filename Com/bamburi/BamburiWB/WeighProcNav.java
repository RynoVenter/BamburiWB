package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class WeighProcNav extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrScreen = getScreenField("SCREEN");
		VtiUserExitScreenField scrStatus = getScreenField("STATUS");
		
		if(scrScreen == null) return new VtiUserExitResult(999,"Field SCREEN did not initialize.");
		if(scrStatus == null) return new VtiUserExitResult(999,"Field STATUS did not initialize.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
		if(scrStatus.getFieldValue().equalsIgnoreCase("F") || 
		   scrStatus.getFieldValue().equalsIgnoreCase("C") || 
		   scrStatus.getFieldValue().length() == 0 ||
		   scrStatus.getFieldValue().equalsIgnoreCase("P")||
		   scrStatus.getFieldValue().equalsIgnoreCase("O"))
		{
			return new VtiUserExitResult(999,"Truck can not be weighed yet, check status.");
		}
		else
		{
			sessionHeader.setNextFunctionId(scrScreen.getFieldValue());
		}
			
		return new VtiUserExitResult();
	}
}
